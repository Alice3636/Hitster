package com.hitster.controllers;

import com.google.gson.Gson;
import com.hitster.client.utils.ResponsiveScaler;
import com.hitster.client.utils.SceneNavigator;
import com.hitster.dto.game.CardDTO;
import com.hitster.dto.game.ChallengeStateDTO;
import com.hitster.dto.game.GamePhase;
import com.hitster.dto.game.GameStateDTO;
import com.hitster.dto.game.PlayerGameStateDTO;
import com.hitster.network.GameplayNetworkService;
import com.hitster.session.GameManager;
import com.hitster.session.UserSession;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GameController {

    @FXML private AnchorPane rootPane;
    @FXML private HBox opponentTimelineHBox;
    @FXML private HBox playerTimelineHBox;
    @FXML private TextField guessArtistField;
    @FXML private TextField guessSongField;
    @FXML private Button useTokenButton;
    @FXML private Button backButton;
    @FXML private Label timerLabel;
    @FXML private ImageView centerCardImage;

    private final GameplayNetworkService networkService = new GameplayNetworkService();
    private final Gson gson = new Gson();

    private ScheduledExecutorService pollingExecutor;

    private String currentGameId;
    private Long currentSongId;
    private String currentSongUrl;

    private boolean isMyTurn = false;
    private boolean canChallenge = false;
    private boolean isSubmittingTurn = false;

    private Timeline localTimer;
    private int localSecondsLeft = 0;

    private MediaPlayer mediaPlayer;

    private HBox challengePanel;
    private Label challengeTimerLabel;

    private Integer lastFeedbackTurnNumber = null;

    public void initialize() {
        ResponsiveScaler.bindToWidth(rootPane);
        currentGameId = GameManager.getInstance().getGameId();

        useTokenButton.setOnAction(e -> handleChallengeButtonClick());

        setupDragAndDropSource();
        startGameStatePolling();
        startLocalTimer();
    }

    private void setupDragAndDropSource() {
        if (centerCardImage == null) return;

        centerCardImage.setOnDragDetected(event -> {
            if (!isMyTurn || currentSongId == null || canChallenge) {
                event.consume();
                return;
            }

            Dragboard db = centerCardImage.startDragAndDrop(TransferMode.ANY);
            ClipboardContent content = new ClipboardContent();
            content.putString(String.valueOf(currentSongId));
            db.setContent(content);
            event.consume();
        });
    }

    private void startGameStatePolling() {
        pollingExecutor = Executors.newSingleThreadScheduledExecutor();
        pollingExecutor.scheduleAtFixedRate(this::fetchGameStateForce, 0, 2, TimeUnit.SECONDS);
    }

    private void fetchGameStateForce() {
        if (currentGameId == null) return;

        networkService.getGameState(currentGameId).thenAccept(response -> {
            if (response.statusCode() == 200) {
                try {
                    GameStateDTO gameState = gson.fromJson(response.body(), GameStateDTO.class);
                    Platform.runLater(() -> updateUI(gameState));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void updateUI(GameStateDTO gameState) {
        if (gameState == null) return;

        GameManager.getInstance().updateGameState(gameState);

        if (gameState.phase() == GamePhase.FINISHED) {
            stopPolling();
            showGameOverAlert(gameState.winnerName());
            return;
        }

        Long myId = UserSession.getInstance().getUserId();

        PlayerGameStateDTO me = GameManager.getInstance().getMe();
        PlayerGameStateDTO opponent = GameManager.getInstance().getOpponent();

        if (me == null || opponent == null) {
            return;
        }

        isMyTurn = myId != null && myId.equals(gameState.activePlayerId());

        ChallengeStateDTO challengeState = gameState.challengeState();
        canChallenge =
                gameState.phase() == GamePhase.CHALLENGE_WINDOW &&
                challengeState != null &&
                challengeState.challengeAvailable() &&
                myId != null &&
                myId.equals(challengeState.challengerPlayerId()) &&
                me.tokens() > 0;

        localSecondsLeft = resolveDisplayedTime(gameState);
        updateTimerDisplay();

        updateCurrentSong(gameState);
        updateControls(gameState, me);
        updateChallengePanel(gameState);

        renderTimeline(playerTimelineHBox, me.timeline(), true, gameState);
        renderTimeline(opponentTimelineHBox, opponent.timeline(), false, gameState);

        showResultFeedbackIfNeeded(gameState);
    }

    private int resolveDisplayedTime(GameStateDTO gameState) {
        if (gameState.phase() == GamePhase.CHALLENGE_WINDOW && gameState.challengeState() != null) {
            return gameState.challengeState().timeLeftSeconds();
        }

        return gameState.timeLeftSeconds();
    }

    private void updateCurrentSong(GameStateDTO gameState) {
        if (gameState.currentSong() == null) {
            currentSongId = null;
            currentSongUrl = null;
            centerCardImage.setVisible(false);
            stopSong();
            return;
        }

        Long newSongId = gameState.currentSong().songId();
        String newSongUrl = gameState.currentSong().audioUrl();

        boolean shouldShowCenterCard =
                gameState.phase() == GamePhase.PLAYER_TURN &&
                isMyTurn;

        centerCardImage.setVisible(shouldShowCenterCard);

        if (currentSongId == null || !currentSongId.equals(newSongId)) {
            currentSongId = newSongId;
            currentSongUrl = newSongUrl;
        }

        boolean shouldPlaySong =
                gameState.phase() == GamePhase.PLAYER_TURN &&
                isMyTurn &&
                currentSongUrl != null;

        if (shouldPlaySong && mediaPlayer == null) {
            playSong(currentSongUrl);
        }

        if (!shouldPlaySong) {
            stopSong();
        }
    }

    private void updateControls(GameStateDTO gameState, PlayerGameStateDTO me) {
        boolean canPlayTurn =
                gameState.phase() == GamePhase.PLAYER_TURN &&
                isMyTurn;

        guessArtistField.setDisable(!canPlayTurn || isSubmittingTurn);
        guessSongField.setDisable(!canPlayTurn || isSubmittingTurn);

        boolean canUseChallenge =
                gameState.phase() == GamePhase.CHALLENGE_WINDOW &&
                canChallenge &&
                me.tokens() > 0;

        useTokenButton.setDisable(!canUseChallenge);
        useTokenButton.setText("CHALLENGE");
        useTokenButton.setStyle(canUseChallenge
                ? "-fx-background-color: #ff0033; -fx-text-fill: white;"
                : "");
    }

    private void updateChallengePanel(GameStateDTO gameState) {
        if (gameState.phase() == GamePhase.CHALLENGE_WINDOW && canChallenge) {
            showChallengeActionPanel();
        } else {
            cleanupChallengePanel();
        }
    }

    private void renderTimeline(
            HBox timelineBox,
            List<CardDTO> cards,
            boolean isMyTimeline,
            GameStateDTO gameState
    ) {
        timelineBox.getChildren().clear();
        if (cards == null) return;

        boolean showPlacementSlots =
                gameState.phase() == GamePhase.PLAYER_TURN &&
                isMyTurn &&
                isMyTimeline;

        boolean showChallengeSlots =
                gameState.phase() == GamePhase.CHALLENGE_WINDOW &&
                canChallenge &&
                !isMyTimeline;

        boolean showSlots = showPlacementSlots || showChallengeSlots;

        Integer blockedChallengeIndex = null;
        if (showChallengeSlots && gameState.challengeState() != null) {
            blockedChallengeIndex = gameState.challengeState().originalPlacedIndex();
        }

        if (showSlots && !isBlockedChallengeSlot(0, showChallengeSlots, blockedChallengeIndex)) {
            timelineBox.getChildren().add(createPlacementSlot(0, showChallengeSlots));
        }

        for (int i = 0; i < cards.size(); i++) {
            timelineBox.getChildren().add(createCardUI(cards.get(i)));

            int slotIndex = i + 1;

            if (showSlots && !isBlockedChallengeSlot(slotIndex, showChallengeSlots, blockedChallengeIndex)) {
                timelineBox.getChildren().add(createPlacementSlot(slotIndex, showChallengeSlots));
            }
        }
    }

    private boolean isBlockedChallengeSlot(
            int slotIndex,
            boolean showChallengeSlots,
            Integer blockedChallengeIndex
    ) {
        return showChallengeSlots &&
                blockedChallengeIndex != null &&
                blockedChallengeIndex == slotIndex;
    }
    private Button createPlacementSlot(int index, boolean isChallengeSlot) {
        Button slotBtn = new Button("+");
        slotBtn.setStyle("-fx-background-color: rgba(255, 255, 255, 0.15); -fx-text-fill: #00ffff; -fx-font-size: 24px; -fx-cursor: hand; -fx-border-color: #00ffff; -fx-border-radius: 10; -fx-background-radius: 10;");
        slotBtn.setPrefSize(80, 160);

        slotBtn.setOnAction(e -> {
            if (isChallengeSlot) {
                sendChallengeRequest(index);
            } else {
                sendPlaceSongRequest(index);
            }
        });

        slotBtn.setOnDragOver(event -> {
            if (event.getGestureSource() != slotBtn && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
            event.consume();
        });

        slotBtn.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;

            if (db.hasString()) {
                if (isChallengeSlot) {
                    sendChallengeRequest(index);
                } else {
                    sendPlaceSongRequest(index);
                }
                success = true;
            }

            event.setDropCompleted(success);
            event.consume();
        });

        return slotBtn;
    }

    private void showChallengeActionPanel() {
        if (challengePanel != null && rootPane.getChildren().contains(challengePanel)) {
            return;
        }

        stopSong();

        challengePanel = new HBox(20);
        challengePanel.setAlignment(Pos.CENTER);
        challengePanel.setStyle("-fx-background-color: rgba(20, 20, 20, 0.9); -fx-padding: 15 30; -fx-background-radius: 40; -fx-border-color: #ff9900; -fx-border-radius: 40; -fx-border-width: 2;");

        AnchorPane.setBottomAnchor(challengePanel, 360.0);
        AnchorPane.setLeftAnchor(challengePanel, 700.0);
        AnchorPane.setRightAnchor(challengePanel, 700.0);

        challengeTimerLabel = new Label();
        challengeTimerLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        challengeTimerLabel.setTextFill(Color.web("#ff9900"));

        Button challengeInfoBtn = new Button("Choose a slot to challenge");
        challengeInfoBtn.setDisable(true);
        challengeInfoBtn.setStyle("-fx-background-color: #ff0033; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 20;");

        DropShadow glow = new DropShadow();
        glow.setColor(Color.web("#ff0033"));
        glow.setRadius(20);
        glow.setSpread(0.5);
        challengeInfoBtn.setEffect(glow);

        ScaleTransition pulse = new ScaleTransition(Duration.millis(600), challengeInfoBtn);
        pulse.setFromX(1.0);
        pulse.setFromY(1.0);
        pulse.setToX(1.05);
        pulse.setToY(1.05);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(Timeline.INDEFINITE);
        pulse.play();

        Button skipBtn = new Button("SKIP ⏭");
        skipBtn.setStyle("-fx-background-color: #333333; -fx-text-fill: white; -fx-font-size: 16px; -fx-padding: 10 20; -fx-background-radius: 20; -fx-cursor: hand;");
        skipBtn.setOnAction(e -> handleSkipChallenge());

        challengePanel.getChildren().addAll(challengeTimerLabel, challengeInfoBtn, skipBtn);
        rootPane.getChildren().add(challengePanel);
        updateTimerDisplay();

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), challengePanel);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }

    private void cleanupChallengePanel() {
        if (challengePanel != null && rootPane.getChildren().contains(challengePanel)) {
            rootPane.getChildren().remove(challengePanel);
        }

        challengePanel = null;
        challengeTimerLabel = null;
    }

    private void startLocalTimer() {
        if (localTimer != null) {
            localTimer.stop();
        }

        localTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            GameStateDTO state = GameManager.getInstance().getCurrentGameState();

            if (state == null) return;

            if (localSecondsLeft > 0) {
                localSecondsLeft--;
                updateTimerDisplay();
            }

            if (localSecondsLeft <= 0 &&
                    state.phase() == GamePhase.CHALLENGE_WINDOW &&
                    canChallenge &&
                    challengePanel != null) {
                handleSkipChallenge();
            }
        }));

        localTimer.setCycleCount(Timeline.INDEFINITE);
        localTimer.play();
    }

    private void updateTimerDisplay() {
        if (timerLabel == null) return;

        int secondsLeft = Math.max(localSecondsLeft, 0);
        int minutes = secondsLeft / 60;
        int seconds = secondsLeft % 60;

        timerLabel.setText(String.format("%02d:%02d", minutes, seconds));

        if (localSecondsLeft <= 10) {
            timerLabel.setTextFill(Color.RED);
        } else {
            timerLabel.setTextFill(Color.WHITE);
        }

        if (challengeTimerLabel != null) {
            challengeTimerLabel.setText(secondsLeft + "s");
            challengeTimerLabel.setTextFill(secondsLeft <= 3 ? Color.RED : Color.web("#ff9900"));
        }
    }

    private void playSong(String audioUrl) {
        stopSong();

        if (audioUrl == null || audioUrl.trim().isEmpty()) {
            return;
        }

        try {
            if (!audioUrl.startsWith("http") && !audioUrl.startsWith("file:")) {
                audioUrl = "http://localhost:8080" + (audioUrl.startsWith("/") ? "" : "/") + audioUrl;
            }

            Media media = new Media(audioUrl);
            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setVolume(0.5);
            mediaPlayer.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopSong() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
            mediaPlayer = null;
        }
    }

    private StackPane createCardUI(CardDTO card) {
        final double CARD_WIDTH = 140;
        final double CARD_HEIGHT = 180;

        StackPane cardPane = new StackPane();

        cardPane.setMinSize(CARD_WIDTH, CARD_HEIGHT);
        cardPane.setPrefSize(CARD_WIDTH, CARD_HEIGHT);
        cardPane.setMaxSize(CARD_WIDTH, CARD_HEIGHT);

        cardPane.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #24114d, #080014);" +
                "-fx-background-radius: 16;" +
                "-fx-border-color: #b388ff;" +
                "-fx-border-width: 2;" +
                "-fx-border-radius: 16;" +
                "-fx-effect: dropshadow(gaussian, rgba(179,136,255,0.65), 18, 0.4, 0, 0);"
        );

        try {
            ImageView bgImage = new ImageView(
                    new Image(getClass().getResourceAsStream("/images/cardfortimeline.jpg"))
            );

            bgImage.setFitWidth(CARD_WIDTH);
            bgImage.setFitHeight(CARD_HEIGHT);
            bgImage.setPreserveRatio(false);
            bgImage.setOpacity(0.45);

            cardPane.getChildren().add(bgImage);
        } catch (Exception ignored) {
        }

        VBox textBox = new VBox(8);
        textBox.setAlignment(Pos.CENTER);
        textBox.setMaxWidth(CARD_WIDTH - 20);
        textBox.setStyle("-fx-padding: 12;");

        Label yearLabel = new Label(card.year() > 0 ? String.valueOf(card.year()) : "????");
        yearLabel.setStyle(
                "-fx-font-size: 26px;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-effect: dropshadow(gaussian, #00ffff, 10, 0.5, 0, 0);"
        );

        Label titleLabel = new Label(card.title() != null ? card.title() : "???");
        titleLabel.setStyle(
                "-fx-font-size: 14px;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;"
        );
        titleLabel.setTextAlignment(TextAlignment.CENTER);
        titleLabel.setAlignment(Pos.CENTER);
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(CARD_WIDTH - 24);

        Label artistLabel = new Label(card.artist() != null ? card.artist() : "???");
        artistLabel.setStyle(
                "-fx-font-size: 12px;" +
                "-fx-text-fill: #d8c8ff;"
        );
        artistLabel.setTextAlignment(TextAlignment.CENTER);
        artistLabel.setAlignment(Pos.CENTER);
        artistLabel.setWrapText(true);
        artistLabel.setMaxWidth(CARD_WIDTH - 24);

        textBox.getChildren().addAll(yearLabel, titleLabel, artistLabel);
        cardPane.getChildren().add(textBox);

        return cardPane;
    }

    private void sendPlaceSongRequest(int indexPosition) {
        if (currentSongId == null || isSubmittingTurn) return;

        String artist = guessArtistField.getText();
        String title = guessSongField.getText();

        if (artist == null || artist.isBlank() || title == null || title.isBlank()) {
            showAlert("Missing Details", "Please enter both artist and song name before placing the card.");
            return;
        }

        isSubmittingTurn = true;
        guessArtistField.setDisable(true);
        guessSongField.setDisable(true);

        Long songId = currentSongId;

        networkService.submitGuess(currentGameId, artist, title).thenCompose(guessResponse -> {
            if (!isSuccessfulResponse(guessResponse.statusCode())) {
                return CompletableFuture.completedFuture(guessResponse);
            }

            return networkService.placeSong(currentGameId, indexPosition, songId);
        }).thenAccept(response -> {
            Platform.runLater(() -> {
                isSubmittingTurn = false;

                if (isSuccessfulResponse(response.statusCode())) {
                    guessArtistField.clear();
                    guessSongField.clear();
                    stopSong();
                    centerCardImage.setVisible(false);
                } else {
                    showAlert("Turn Submit Failed", "Could not submit your turn. Please try again.");
                }

                fetchGameStateForce();
            });
        }).exceptionally(error -> {
            Platform.runLater(() -> {
                isSubmittingTurn = false;
                showAlert("Network Error", "Could not submit your turn. Please try again.");
                fetchGameStateForce();
            });

            return null;
        });
    }

    private boolean isSuccessfulResponse(int statusCode) {
        return statusCode >= 200 && statusCode < 300;
    }

    private void handleChallengeButtonClick() {
        showAlert("Challenge", "Choose a slot in your opponent's timeline.");
    }

    private void sendChallengeRequest(int suggestedIndex) {
        networkService.challenge(currentGameId, suggestedIndex).thenAccept(response -> {
            Platform.runLater(() -> {
                cleanupChallengePanel();
                fetchGameStateForce();
            });
        });
    }

    private void handleSkipChallenge() {
        cleanupChallengePanel();

        networkService.skipChallenge(currentGameId).thenAccept(response -> {
            Platform.runLater(this::fetchGameStateForce);
        });
    }

    private void showResultFeedbackIfNeeded(GameStateDTO gameState) {
        if (gameState.phase() != GamePhase.TURN_RESOLVED) {
            return;
        }

        if (lastFeedbackTurnNumber != null &&
                lastFeedbackTurnNumber == gameState.turnNumber()) {
            return;
        }

        lastFeedbackTurnNumber = gameState.turnNumber();

        if (gameState.lastChallengeResult() != null) {
            if (gameState.lastChallengeResult().challengeCorrect()) {
                showAnimatedFeedback("CHALLENGE!", "Card transferred!", Color.web("#39ff14"));
            } else if (gameState.lastChallengeResult().tokenSpent()) {
                showAnimatedFeedback("FAILED!", "Token spent.", Color.web("#ff0033"));
            }
            return;
        }

        if (gameState.lastTurnResult() != null) {
            boolean allCorrect =
                    gameState.lastTurnResult().titleCorrect() &&
                    gameState.lastTurnResult().artistCorrect() &&
                    gameState.lastTurnResult().placementCorrect();

            if (allCorrect) {
                showAnimatedFeedback("PERFECT!", "You earned a token!", Color.web("#39ff14"));
            } else if (gameState.lastTurnResult().placementCorrect()) {
                showAnimatedFeedback("CORRECT!", "Good placement!", Color.web("#39ff14"));
            } else {
                showAnimatedFeedback("WRONG!", "Bad placement!", Color.web("#ff0033"));
            }
        }
    }

    private void showAnimatedFeedback(String title, String subtitle, Color color) {
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");

        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER);

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 120));
        titleLabel.setTextFill(color);

        Label subLabel = new Label(subtitle);
        subLabel.setFont(Font.font("System", FontWeight.BOLD, 30));
        subLabel.setTextFill(Color.WHITE);

        box.getChildren().addAll(titleLabel, subLabel);
        overlay.getChildren().add(box);
        StackPane.setAlignment(box, Pos.CENTER);

        AnchorPane.setTopAnchor(overlay, 0.0);
        AnchorPane.setBottomAnchor(overlay, 0.0);
        AnchorPane.setLeftAnchor(overlay, 0.0);
        AnchorPane.setRightAnchor(overlay, 0.0);

        rootPane.getChildren().add(overlay);

        ScaleTransition st = new ScaleTransition(Duration.millis(300), box);
        st.setFromX(0.1);
        st.setFromY(0.1);
        st.setToX(1.0);
        st.setToY(1.0);

        Timeline wait = new Timeline(new KeyFrame(Duration.seconds(1.5)));
        wait.setOnFinished(e -> {
            FadeTransition ftOut = new FadeTransition(Duration.millis(300), overlay);
            ftOut.setFromValue(1.0);
            ftOut.setToValue(0.0);
            ftOut.setOnFinished(e2 -> rootPane.getChildren().remove(overlay));
            ftOut.play();
        });

        st.setOnFinished(e -> wait.play());
        st.play();
    }

    private void stopPolling() {
        if (pollingExecutor != null && !pollingExecutor.isShutdown()) {
            pollingExecutor.shutdown();
        }

        if (localTimer != null) {
            localTimer.stop();
        }

        stopSong();
    }

    @FXML
    void handleBack(ActionEvent event) {
        Alert confirm = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Are you sure you want to quit? You will forfeit the match.",
                ButtonType.YES,
                ButtonType.NO
        );

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                stopPolling();
                networkService.quitGame(currentGameId);
                SceneNavigator.loadScene(SceneNavigator.LOBBY_SCREEN);
            }
        });
    }

    private void showGameOverAlert(String winner) {
        String myUsername = UserSession.getInstance().getUserName();
        String message = myUsername != null && myUsername.equals(winner)
                ? "Congratulations! You Won!"
                : "Game Over! " + winner + " won.";

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Match Finished");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.setOnHidden(e -> {
            GameManager.getInstance().endGame();
            SceneNavigator.loadScene(SceneNavigator.LOBBY_SCREEN);
        });

        alert.show();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }
}
