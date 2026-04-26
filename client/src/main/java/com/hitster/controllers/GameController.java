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
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GameController {

    private static final String CARD_DRAG_PREFIX = "CARD:";
    private static final String CHALLENGE_TOKEN_DRAG_VALUE = "CHALLENGE_TOKEN";

    @FXML private AnchorPane rootPane;
    @FXML private HBox opponentTimelineHBox;
    @FXML private HBox playerTimelineHBox;
    @FXML private ScrollPane opponentTimelineScrollPane;
    @FXML private ScrollPane playerTimelineScrollPane;
    @FXML private TextField guessArtistField;
    @FXML private TextField guessSongField;
    @FXML private Button backButton;
    @FXML private Label timerLabel;
    @FXML private Label scoreLabel;
    @FXML private Label tokensLabel;
    @FXML private ImageView centerCardImage;
    @FXML private ImageView centerTokenImage;

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
    private boolean isSkippingChallenge = false;

    private Integer lastFeedbackTurnNumber = null;

    public void initialize() {
        ResponsiveScaler.bindToWidth(rootPane);
        currentGameId = GameManager.getInstance().getGameId();

        setupDragAndDropSources();
        setupTimelineScrollControls();
        startGameStatePolling();
        startLocalTimer();
    }

    private void setupDragAndDropSources() {
        if (centerCardImage != null) {
            centerCardImage.setOnDragDetected(event -> {
                if (!isMyTurn || currentSongId == null || canChallenge) {
                    event.consume();
                    return;
                }

                Dragboard db = centerCardImage.startDragAndDrop(TransferMode.ANY);
                ClipboardContent content = new ClipboardContent();
                content.putString(CARD_DRAG_PREFIX + currentSongId);
                db.setContent(content);
                event.consume();
            });
        }

        if (centerTokenImage != null) {
            centerTokenImage.setOnDragDetected(event -> {
                if (!canChallenge) {
                    event.consume();
                    return;
                }

                Dragboard db = centerTokenImage.startDragAndDrop(TransferMode.ANY);
                ClipboardContent content = new ClipboardContent();
                content.putString(CHALLENGE_TOKEN_DRAG_VALUE);
                db.setContent(content);
                event.consume();
            });
        }
    }

    private void setupTimelineScrollControls() {
        bindTimelineScrollButtons(opponentTimelineScrollPane);
        bindTimelineScrollButtons(playerTimelineScrollPane);
    }

    private void bindTimelineScrollButtons(ScrollPane scrollPane) {
        if (scrollPane == null || !(scrollPane.getParent() instanceof HBox timelineContainer)) {
            return;
        }

        List<javafx.scene.Node> children = timelineContainer.getChildren();
        if (children.size() < 3) {
            return;
        }

        Button leftButton = children.get(0) instanceof Button button ? button : null;
        Button rightButton = children.get(children.size() - 1) instanceof Button button ? button : null;

        if (leftButton != null) {
            leftButton.setOnAction(e -> scrollTimeline(scrollPane, -1));
        }

        if (rightButton != null) {
            rightButton.setOnAction(e -> scrollTimeline(scrollPane, 1));
        }
    }

    private void scrollTimeline(ScrollPane scrollPane, int direction) {
        double step = 0.18;
        double nextValue = scrollPane.getHvalue() + direction * step;
        scrollPane.setHvalue(Math.max(0.0, Math.min(1.0, nextValue)));
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

        if (gameState.phase() != GamePhase.CHALLENGE_WINDOW || !canChallenge) {
            isSkippingChallenge = false;
        }

        localSecondsLeft = resolveDisplayedTime(gameState);
        updateTimerDisplay();

        updateCurrentSong(gameState);
        updateCenterToken(gameState);
        updatePlayerStats(me);
        updateControls(gameState, me);

        renderTimeline(playerTimelineHBox, me, true, gameState);
        renderTimeline(opponentTimelineHBox, opponent, false, gameState);

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
            centerCardImage.setManaged(false);
            stopSong();
            return;
        }

        Long newSongId = gameState.currentSong().songId();
        String newSongUrl = gameState.currentSong().audioUrl();

        boolean shouldShowCenterCard =
                gameState.phase() == GamePhase.PLAYER_TURN;

        centerCardImage.setVisible(shouldShowCenterCard);
        centerCardImage.setManaged(shouldShowCenterCard);

        boolean songChanged = currentSongId == null || !currentSongId.equals(newSongId);
        if (songChanged) {
            currentSongId = newSongId;
            currentSongUrl = newSongUrl;
            stopSong();
        }

        boolean shouldPlaySong =
                (gameState.phase() == GamePhase.PLAYER_TURN ||
                        gameState.phase() == GamePhase.CHALLENGE_WINDOW ||
                        gameState.phase() == GamePhase.TURN_RESOLVED) &&
                currentSongUrl != null;

        if (shouldPlaySong && mediaPlayer == null) {
            playSong(currentSongUrl);
        }

        if (!shouldPlaySong) {
            stopSong();
        }
    }

    private void updateCenterToken(GameStateDTO gameState) {
        if (centerTokenImage == null) {
            return;
        }

        boolean shouldShowToken =
                gameState.phase() == GamePhase.CHALLENGE_WINDOW &&
                gameState.challengeState() != null;

        centerTokenImage.setVisible(shouldShowToken);
        centerTokenImage.setManaged(shouldShowToken);
    }

    private void updateControls(GameStateDTO gameState, PlayerGameStateDTO me) {
        boolean canPlayTurn =
                gameState.phase() == GamePhase.PLAYER_TURN &&
                isMyTurn;

        guessArtistField.setDisable(!canPlayTurn || isSubmittingTurn);
        guessSongField.setDisable(!canPlayTurn || isSubmittingTurn);

    }

    private void updatePlayerStats(PlayerGameStateDTO me) {
        if (scoreLabel != null) {
            scoreLabel.setText("Score: " + me.score());
        }

        if (tokensLabel != null) {
            tokensLabel.setText("Tokens: " + me.tokens());
        }
    }

    private void renderTimeline(
            HBox timelineBox,
            PlayerGameStateDTO owner,
            boolean isMyTimeline,
            GameStateDTO gameState
    ) {
        timelineBox.getChildren().clear();
        List<CardDTO> cards = owner.timeline();
        if (cards == null) return;

        boolean showPlacementSlots =
                gameState.phase() == GamePhase.PLAYER_TURN &&
                isMyTurn &&
                isMyTimeline;

        boolean showPendingCard = shouldShowPendingChallengeCard(owner, gameState);
        Integer pendingCardIndex = showPendingCard
                ? gameState.challengeState().originalPlacedIndex()
                : null;

        boolean showChallengeSlots =
                gameState.phase() == GamePhase.CHALLENGE_WINDOW &&
                canChallenge &&
                isChallengeTargetTimeline(owner, gameState);

        boolean showSlots = showPlacementSlots || showChallengeSlots;

        for (int slotIndex = 0; slotIndex <= cards.size(); slotIndex++) {
            boolean isOriginalChallengeSlot =
                    pendingCardIndex != null &&
                    pendingCardIndex == slotIndex;

            if (showSlots && !isOriginalChallengeSlot) {
                timelineBox.getChildren().add(createPlacementSlot(slotIndex, showChallengeSlots));
            }

            if (isOriginalChallengeSlot) {
                timelineBox.getChildren().add(createPendingCardUI());
            }

            if (slotIndex < cards.size()) {
                timelineBox.getChildren().add(createCardUI(cards.get(slotIndex)));
            }
        }
    }

    private boolean shouldShowPendingChallengeCard(PlayerGameStateDTO owner, GameStateDTO gameState) {
        return gameState.phase() == GamePhase.CHALLENGE_WINDOW &&
                gameState.challengeState() != null &&
                owner.playerId() != null &&
                owner.playerId().equals(gameState.challengeState().challengedPlayerId());
    }

    private boolean isChallengeTargetTimeline(PlayerGameStateDTO owner, GameStateDTO gameState) {
        return gameState.challengeState() != null &&
                owner.playerId() != null &&
                owner.playerId().equals(gameState.challengeState().challengedPlayerId());
    }

    private Button createPlacementSlot(int index, boolean isChallengeSlot) {
        final double SLOT_WIDTH = 115;
        final double SLOT_HEIGHT = 85;

        Button slotBtn = new Button("+");

        slotBtn.setMinSize(SLOT_WIDTH, SLOT_HEIGHT);
        slotBtn.setPrefSize(SLOT_WIDTH, SLOT_HEIGHT);
        slotBtn.setMaxSize(SLOT_WIDTH, SLOT_HEIGHT);

        slotBtn.setStyle(
                "-fx-background-color: rgba(255, 255, 255, 0.08);" +
                "-fx-text-fill: #00ffff;" +
                "-fx-font-size: 24px;" +
                "-fx-font-weight: bold;" +
                "-fx-cursor: hand;" +
                "-fx-border-color: #00ffff;" +
                "-fx-border-width: 2;" +
                "-fx-border-radius: 14;" +
                "-fx-background-radius: 14;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,255,255,0.55), 14, 0.35, 0, 0);"
        );

        slotBtn.setOnAction(e -> {
            if (isChallengeSlot) {
                sendChallengeRequest(index);
            } else {
                sendPlaceSongRequest(index);
            }
        });

        slotBtn.setOnDragOver(event -> {
            if (event.getGestureSource() != slotBtn && isExpectedDrag(event.getDragboard(), isChallengeSlot)) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
            event.consume();
        });

        slotBtn.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;

            if (isExpectedDrag(db, isChallengeSlot)) {
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

    private StackPane createPendingCardUI() {
        final double CARD_WIDTH = 115;
        final double CARD_HEIGHT = 85;

        StackPane cardPane = new StackPane();

        cardPane.setMinSize(CARD_WIDTH, CARD_HEIGHT);
        cardPane.setPrefSize(CARD_WIDTH, CARD_HEIGHT);
        cardPane.setMaxSize(CARD_WIDTH, CARD_HEIGHT);

        cardPane.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #1a1a1a, #050505);" +
                "-fx-background-radius: 14;" +
                "-fx-border-color: #ff9900;" +
                "-fx-border-width: 2;" +
                "-fx-border-radius: 14;" +
                "-fx-effect: dropshadow(gaussian, rgba(255,153,0,0.75), 16, 0.45, 0, 0);"
        );

        Label questionLabel = new Label("?");
        questionLabel.setFont(Font.font("System", FontWeight.BOLD, 46));
        questionLabel.setTextFill(Color.web("#ffcc66"));
        questionLabel.setTextAlignment(TextAlignment.CENTER);
        questionLabel.setAlignment(Pos.CENTER);

        cardPane.getChildren().add(questionLabel);

        return cardPane;
    }

    private boolean isExpectedDrag(Dragboard db, boolean isChallengeSlot) {
        if (db == null || !db.hasString()) {
            return false;
        }

        String value = db.getString();

        if (isChallengeSlot) {
            return CHALLENGE_TOKEN_DRAG_VALUE.equals(value);
        }

        return value != null && value.startsWith(CARD_DRAG_PREFIX);
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
                    !isSkippingChallenge) {
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

    }

    private void playSong(String audioUrl) {
        stopSong();

        if (audioUrl == null || audioUrl.trim().isEmpty()) {
            return;
        }

        try {
            Media media = new Media(toMediaSource(audioUrl));
            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setVolume(0.5);
            mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            mediaPlayer.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String toMediaSource(String audioUrl) throws URISyntaxException, MalformedURLException {
        String source = audioUrl.trim();

        if (!source.startsWith("http") && !source.startsWith("file:")) {
            String path = source.startsWith("/") ? source : "/" + source;
            return new URI("http", null, "localhost", 8080, path, null, null).toASCIIString();
        }

        try {
            return new URI(source).toASCIIString();
        } catch (URISyntaxException invalidRawUri) {
            if (!source.startsWith("http")) {
                throw invalidRawUri;
            }

            URL url = new URL(source);
            return new URI(
                    url.getProtocol(),
                    url.getUserInfo(),
                    url.getHost(),
                    url.getPort(),
                    url.getPath(),
                    url.getQuery(),
                    url.getRef()
            ).toASCIIString();
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
        final double CARD_WIDTH = 115;
        final double CARD_HEIGHT = 85;

        StackPane cardPane = new StackPane();

        cardPane.setMinSize(CARD_WIDTH, CARD_HEIGHT);
        cardPane.setPrefSize(CARD_WIDTH, CARD_HEIGHT);
        cardPane.setMaxSize(CARD_WIDTH, CARD_HEIGHT);

        cardPane.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #24114d, #080014);" +
                "-fx-background-radius: 14;" +
                "-fx-border-color: #b388ff;" +
                "-fx-border-width: 2;" +
                "-fx-border-radius: 14;" +
                "-fx-effect: dropshadow(gaussian, rgba(179,136,255,0.65), 14, 0.35, 0, 0);"
        );

        try {
            ImageView bgImage = new ImageView(
                    new Image(getClass().getResourceAsStream("/images/cardfortimeline.jpg"))
            );

            bgImage.setFitWidth(CARD_WIDTH);
            bgImage.setFitHeight(CARD_HEIGHT);
            bgImage.setPreserveRatio(false);
            bgImage.setOpacity(0.35);

            cardPane.getChildren().add(bgImage);
        } catch (Exception ignored) {
        }

        VBox textBox = new VBox(5);
        textBox.setAlignment(Pos.CENTER);
        textBox.setMaxWidth(CARD_WIDTH - 12);
        textBox.setStyle("-fx-padding: 8;");

        Label yearLabel = new Label(card.year() > 0 ? String.valueOf(card.year()) : "????");
        yearLabel.setStyle(
                "-fx-font-size: 15px;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-effect: dropshadow(gaussian, #00ffff, 8, 0.45, 0, 0);"
        );

        Label titleLabel = new Label(card.title() != null ? card.title() : "???");
        titleLabel.setStyle(
                "-fx-font-size: 10px;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;"
        );
        titleLabel.setTextAlignment(TextAlignment.CENTER);
        titleLabel.setAlignment(Pos.CENTER);
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(CARD_WIDTH - 14);

        Label artistLabel = new Label(card.artist() != null ? card.artist() : "???");
        artistLabel.setStyle(
                "-fx-font-size: 9px;" +
                "-fx-text-fill: #d8c8ff;"
        );
        artistLabel.setTextAlignment(TextAlignment.CENTER);
        artistLabel.setAlignment(Pos.CENTER);
        artistLabel.setWrapText(true);
        artistLabel.setMaxWidth(CARD_WIDTH - 14);

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

    private void sendChallengeRequest(int suggestedIndex) {
        networkService.challenge(currentGameId, suggestedIndex).thenAccept(response -> {
            Platform.runLater(() -> {
                fetchGameStateForce();
            });
        });
    }

    private void handleSkipChallenge() {
        if (isSkippingChallenge) {
            return;
        }

        isSkippingChallenge = true;

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
