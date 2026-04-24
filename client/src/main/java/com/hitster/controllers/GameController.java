package com.hitster.controllers;

import com.google.gson.Gson;
import com.hitster.client.utils.SceneNavigator;
import com.hitster.dto.game.CardDTO;
import com.hitster.dto.game.GameStateDTO;
import com.hitster.network.GameplayNetworkService;
import com.hitster.session.GameManager;
import com.hitster.session.UserSession;

import javafx.animation.Animation;
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

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GameController {

    @FXML private AnchorPane rootPane;
    @FXML private HBox opponentTimelineHBox;
    @FXML private HBox playerTimelineHBox;
    @FXML private TextField guessArtistField;
    @FXML private TextField guessSongField;
    @FXML private Button submitGuessButton;
    @FXML private Button useTokenButton;
    @FXML private Button backButton;
    @FXML private Label timerLabel;
    @FXML private ImageView centerCardImage;

    private final GameplayNetworkService networkService = new GameplayNetworkService();
    private ScheduledExecutorService pollingExecutor;
    private final Gson gson = new Gson();

    private String currentGameId;
    private Long currentSongId = null;
    private String currentSongUrl = null;
    private boolean isChallengeMode = false;
    private boolean isMyTurn = false;
    private Long currentTurnPlayerId = null;

    private StackPane countdownOverlay;
    private Label countdownLabel;
    private int countdownValue = 3;
    private boolean isCountdownFinished = false;
    
    private Timeline localTimer;
    private int localSecondsLeft = 60;

    private MediaPlayer mediaPlayer;

    private HBox challengePanel;
    private Timeline challengeTimer;
    private int challengeSecondsLeft;

    @FXML
    public void initialize() {
        currentGameId = GameManager.getInstance().getGameId();

        submitGuessButton.setOnAction(e -> handleGuessSubmit());
        useTokenButton.setOnAction(e -> handleUseToken());

        setupDragAndDropSource();

        startGameStatePolling();
        startCountdown();
    }

    private void setupDragAndDropSource() {
        if (centerCardImage != null) {
            centerCardImage.setOnDragDetected(event -> {
                if (!isMyTurn || currentSongId == null || challengePanel != null) {
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
    }

    private void startCountdown() {
        countdownOverlay = new StackPane();
        countdownOverlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.85);");
        
        AnchorPane.setTopAnchor(countdownOverlay, 0.0);
        AnchorPane.setBottomAnchor(countdownOverlay, 0.0);
        AnchorPane.setLeftAnchor(countdownOverlay, 0.0);
        AnchorPane.setRightAnchor(countdownOverlay, 0.0);

        countdownLabel = new Label("3");
        countdownLabel.setFont(Font.font("System", FontWeight.BOLD, 150));
        countdownLabel.setTextFill(Color.web("#00ffff"));
        countdownOverlay.getChildren().add(countdownLabel);
        StackPane.setAlignment(countdownLabel, Pos.CENTER);

        rootPane.getChildren().add(countdownOverlay);

        Timeline timeline = new Timeline();
        timeline.setCycleCount(4);

        KeyFrame keyFrame = new KeyFrame(Duration.seconds(1), event -> {
            countdownValue--;
            if (countdownValue > 0) {
                countdownLabel.setText(String.valueOf(countdownValue));
            } else if (countdownValue == 0) {
                countdownLabel.setText("GO!");
                countdownLabel.setTextFill(Color.web("#39ff14"));
            }
        });

        timeline.getKeyFrames().add(keyFrame);

        timeline.setOnFinished(event -> {
            rootPane.getChildren().remove(countdownOverlay);
            isCountdownFinished = true;
            startLocalTimer();
            
            if (currentSongUrl != null && challengePanel == null) {
                playSong(currentSongUrl);
            }
        });

        timeline.play();
    }

    private void startLocalTimer() {
        if (localTimer != null) localTimer.stop();
        localTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            // הוספנו תנאי: הטיימר המקומי רץ רק אם פאנל ה-Challenge לא פתוח
            if (localSecondsLeft > 0 && challengePanel == null) {
                localSecondsLeft--;
                updateTimerDisplay();
            }
        }));
        localTimer.setCycleCount(Timeline.INDEFINITE);
        localTimer.play();
    }

    private void updateTimerDisplay() {
        if (timerLabel != null) {
            timerLabel.setText(String.format("00:%02d", localSecondsLeft));
            if (localSecondsLeft <= 10) {
                timerLabel.setTextFill(Color.RED);
            } else {
                timerLabel.setTextFill(Color.WHITE);
            }
        }
    }

    private void playSong(String audioUrl) {
        stopSong();
        if (audioUrl == null || audioUrl.trim().isEmpty() || challengePanel != null) {
            return; // לעולם לא נגן אם האתגר פתוח
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

    private void startGameStatePolling() {
        pollingExecutor = Executors.newSingleThreadScheduledExecutor();
        pollingExecutor.scheduleAtFixedRate(this::fetchGameStateForce, 0, 2, TimeUnit.SECONDS);
    }

    private void fetchGameStateForce() {
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

    private void stopPolling() {
        if (pollingExecutor != null && !pollingExecutor.isShutdown()) {
            pollingExecutor.shutdown();
        }
        if (localTimer != null) {
            localTimer.stop();
        }
        if (challengeTimer != null) {
            challengeTimer.stop();
        }
        stopSong();
    }

    private void showChallengeActionPanel() {
        if (challengePanel != null && rootPane.getChildren().contains(challengePanel)) return;

        stopSong(); // מוודא שהמוזיקה עוצרת

        challengePanel = new HBox(20);
        challengePanel.setAlignment(Pos.CENTER);
        challengePanel.setStyle("-fx-background-color: rgba(20, 20, 20, 0.9); -fx-padding: 15 30; -fx-background-radius: 40; -fx-border-color: #ff9900; -fx-border-radius: 40; -fx-border-width: 2;");
        
        AnchorPane.setBottomAnchor(challengePanel, 360.0);
        AnchorPane.setLeftAnchor(challengePanel, 700.0);
        AnchorPane.setRightAnchor(challengePanel, 700.0);

        challengeSecondsLeft = 10;
        Label chalTimerLabel = new Label("10s");
        chalTimerLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        chalTimerLabel.setTextFill(Color.web("#ff9900"));

        Button challengeBtn = new Button("CHALLENGE!");
        challengeBtn.setStyle("-fx-background-color: #ff0033; -fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 20; -fx-cursor: hand;");
        
        DropShadow glow = new DropShadow();
        glow.setColor(Color.web("#ff0033"));
        glow.setRadius(20);
        glow.setSpread(0.5);
        challengeBtn.setEffect(glow);

        ScaleTransition pulse = new ScaleTransition(Duration.millis(600), challengeBtn);
        pulse.setFromX(1.0); pulse.setFromY(1.0);
        pulse.setToX(1.05); pulse.setToY(1.05);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(Animation.INDEFINITE);
        pulse.play();

        Button skipBtn = new Button("SKIP ⏭");
        skipBtn.setStyle("-fx-background-color: #333333; -fx-text-fill: white; -fx-font-size: 16px; -fx-padding: 10 20; -fx-background-radius: 20; -fx-cursor: hand;");

        challengePanel.getChildren().addAll(chalTimerLabel, challengeBtn, skipBtn);
        rootPane.getChildren().add(challengePanel);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), challengePanel);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();

        challengeBtn.setOnAction(e -> {
            cleanupChallengePanel();
            isChallengeMode = true;
            useTokenButton.setText("CANCEL CHALLENGE");
            useTokenButton.setStyle("-fx-background-color: #ff4d4d; -fx-text-fill: white;");
            fetchGameStateForce();
        });

        skipBtn.setOnAction(e -> handleSkipChallenge());

        challengeTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            challengeSecondsLeft--;
            chalTimerLabel.setText(challengeSecondsLeft + "s");
            if (challengeSecondsLeft <= 3) chalTimerLabel.setTextFill(Color.RED);
            
            if (challengeSecondsLeft <= 0) {
                handleSkipChallenge();
            }
        }));
        challengeTimer.setCycleCount(10);
        challengeTimer.play();
    }

    private void handleSkipChallenge() {
        cleanupChallengePanel();
        networkService.skipChallenge(currentGameId).thenAccept(res -> {
            fetchGameStateForce();
        });
    }

    private void cleanupChallengePanel() {
        if (challengeTimer != null) challengeTimer.stop();
        if (challengePanel != null && rootPane.getChildren().contains(challengePanel)) {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), challengePanel);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(ev -> rootPane.getChildren().remove(challengePanel));
            fadeOut.play();
        }
        challengePanel = null;
        
        // לאחר הסגירה, אם יש שיר חדש, מנגן אותו ומשחרר את התור
        if (currentSongUrl != null) {
            playSong(currentSongUrl);
        }
    }

    private void updateUI(GameStateDTO gameState) {
        if ("FINISHED".equalsIgnoreCase(gameState.getGameStatus())) {
            stopPolling();
            showGameOverAlert(gameState.getWinnerName());
            return;
        }

        Long myId = UserSession.getInstance().getUserId();
        Long activePlayer = gameState.getCurrentPlayerId();
        
        boolean previouslyMyTurn = this.isMyTurn;
        this.isMyTurn = (myId != null && activePlayer != null && myId.equals(activePlayer));

        List<CardDTO> myTimeline = myId.equals(gameState.getPlayer1Id()) ? gameState.getPlayer1Timeline() : gameState.getPlayer2Timeline();
        List<CardDTO> opponentTimeline = myId.equals(gameState.getPlayer1Id()) ? gameState.getPlayer2Timeline() : gameState.getPlayer1Timeline();
        int myTokens = myId.equals(gameState.getPlayer1Id()) ? gameState.getPlayer1Tokens() : gameState.getPlayer2Tokens();

        int totalCards = (myTimeline != null ? myTimeline.size() : 0) + (opponentTimeline != null ? opponentTimeline.size() : 0);
        if (!previouslyMyTurn && this.isMyTurn && totalCards >= 2) {
            stopSong(); 
            showChallengeActionPanel();
        }

        // מתעלם מזמן השרת אם אני באמצע החלטה של אתגר
        if (challengePanel == null) {
            if (currentTurnPlayerId == null || !currentTurnPlayerId.equals(activePlayer)) {
                currentTurnPlayerId = activePlayer;
                localSecondsLeft = 60;
            } else if (gameState.getTimeLeftSeconds() > 0) {
                // מקפיא את הטיימר לפי השרת אם זה משהו שהשתבש
                localSecondsLeft = gameState.getTimeLeftSeconds();
            }
        }
        updateTimerDisplay();

        if (gameState.getCurrentSong() != null) {
            Long newSongId = gameState.getCurrentSong().songId();
            String newSongUrl = gameState.getCurrentSong().audioUrl();
            
            // מראה את הקלף רק אם אין פאנל אתגר
            centerCardImage.setVisible(challengePanel == null);

            if (currentSongId == null || !currentSongId.equals(newSongId)) {
                currentSongId = newSongId;
                currentSongUrl = newSongUrl;
                
                if (isCountdownFinished && challengePanel == null) {
                    playSong(currentSongUrl);
                }
            }
        } else {
            currentSongId = null;
            currentSongUrl = null;
            centerCardImage.setVisible(false);
            stopSong();
        }

        boolean disableActions = !isMyTurn || challengePanel != null;
        submitGuessButton.setDisable(disableActions);
        guessArtistField.setDisable(disableActions);
        guessSongField.setDisable(disableActions);
        useTokenButton.setDisable(disableActions || myTokens <= 0);

        renderTimeline(playerTimelineHBox, myTimeline, true);
        renderTimeline(opponentTimelineHBox, opponentTimeline, false);
    }

    private void renderTimeline(HBox timelineBox, List<CardDTO> cards, boolean isMyTimeline) {
        timelineBox.getChildren().clear();
        if (cards == null) return;

        boolean showSlots = (isMyTimeline && isMyTurn && !isChallengeMode && challengePanel == null) || 
                            (!isMyTimeline && !isMyTurn && isChallengeMode);

        if (showSlots) {
            timelineBox.getChildren().add(createPlacementSlot(0, !isMyTimeline));
        }

        for (int i = 0; i < cards.size(); i++) {
            timelineBox.getChildren().add(createCardUI(cards.get(i)));
            
            if (showSlots) {
                timelineBox.getChildren().add(createPlacementSlot(i + 1, !isMyTimeline));
            }
        }
    }

    private Button createPlacementSlot(int index, boolean isChallenge) {
        Button slotBtn = new Button("+");
        slotBtn.setStyle("-fx-background-color: rgba(255, 255, 255, 0.15); -fx-text-fill: #00ffff; -fx-font-size: 24px; -fx-cursor: hand; -fx-border-color: #00ffff; -fx-border-radius: 10; -fx-background-radius: 10;");
        slotBtn.setPrefSize(80, 160);
        
        slotBtn.setOnAction(e -> {
            if (isChallenge) {
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
                if (isChallenge) {
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

    private StackPane createCardUI(CardDTO card) {
        StackPane cardPane = new StackPane();
        cardPane.setPrefSize(140, 180);

        try {
            ImageView bgImage = new ImageView(new Image(getClass().getResourceAsStream("/images/cardfortimeline.jpg")));
            bgImage.setFitWidth(140);
            bgImage.setFitHeight(180);
            cardPane.getChildren().add(bgImage);
        } catch (Exception e) {
        }

        VBox textBox = new VBox(5);
        textBox.setAlignment(Pos.CENTER);
        textBox.setStyle("-fx-padding: 10;");

        Label yearLabel = new Label(card.year() > 0 ? String.valueOf(card.year()) : "????");
        yearLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: white; -fx-font-weight: bold;");
        
        Label titleLabel = new Label(card.title() != null ? card.title() : "???");
        titleLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white; -fx-font-size: 14px;");
        titleLabel.setTextAlignment(TextAlignment.CENTER);
        titleLabel.setWrapText(true);
        
        Label artistLabel = new Label(card.artist() != null ? card.artist() : "???");
        artistLabel.setStyle("-fx-text-fill: #e0e0e0; -fx-font-size: 12px;");
        artistLabel.setTextAlignment(TextAlignment.CENTER);
        artistLabel.setWrapText(true);

        textBox.getChildren().addAll(yearLabel, titleLabel, artistLabel);
        cardPane.getChildren().add(textBox);
        
        return cardPane;
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
        st.setFromX(0.1); st.setFromY(0.1);
        st.setToX(1.0); st.setToY(1.0);

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

    private void handleServerResponse(int statusCode, String responseBody, String actionType) {
        centerCardImage.setVisible(false);
        stopSong();
        fetchGameStateForce();

        if (statusCode == 200 || statusCode == 201) {
            boolean isCorrect = true;
            
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> resultObj = gson.fromJson(responseBody, Map.class);
                if (resultObj != null) {
                    if (actionType.equals("Placement") && resultObj.containsKey("placementCorrect")) {
                        isCorrect = (Boolean) resultObj.get("placementCorrect");
                    } else if (actionType.equals("Guess") && resultObj.containsKey("guessCorrect")) {
                        isCorrect = (Boolean) resultObj.get("guessCorrect");
                    }
                }
            } catch (Exception e) {}

            if (isCorrect) {
                showAnimatedFeedback("CORRECT!", "Great Job!", Color.web("#39ff14"));
            } else {
                showAnimatedFeedback("WRONG!", "Bad placement!", Color.web("#ff0033"));
            }
        } else {
            String errorMsg = "Action rejected.";
            try {
                @SuppressWarnings("unchecked")
                Map<String, String> errorMap = gson.fromJson(responseBody, Map.class);
                if (errorMap != null && errorMap.containsKey("errorMessage")) {
                    errorMsg = errorMap.get("errorMessage");
                }
            } catch (Exception e) {}
            
            showAnimatedFeedback("ERROR!", errorMsg, Color.web("#ff9900"));
        }
    }

    private void handleGuessSubmit() {
        String artist = guessArtistField.getText();
        String song = guessSongField.getText();
        
        if (artist.isEmpty() || song.isEmpty()) {
            showAlert("Missing Details", "Please enter both artist and song name.");
            return;
        }

        networkService.submitGuess(currentGameId, artist, song).thenAccept(response -> {
            Platform.runLater(() -> {
                if (response.statusCode() == 200 || response.statusCode() == 201) {
                    guessArtistField.clear();
                    guessSongField.clear();
                }
                handleServerResponse(response.statusCode(), response.body(), "Guess");
            });
        });
    }

    private void handleUseToken() {
        isChallengeMode = !isChallengeMode;
        if (isChallengeMode) {
            useTokenButton.setText("CANCEL CHALLENGE");
            useTokenButton.setStyle("-fx-background-color: #ff4d4d; -fx-text-fill: white;");
        } else {
            useTokenButton.setText("USE TOKEN");
            useTokenButton.setStyle("");
        }
    }

    private void sendPlaceSongRequest(int indexPosition) {
        if (currentSongId == null) return;
        
        networkService.placeSong(currentGameId, indexPosition, currentSongId).thenAccept(response -> {
            Platform.runLater(() -> {
                handleServerResponse(response.statusCode(), response.body(), "Placement");
            });
        });
    }

    private void sendChallengeRequest(int suggestedIndex) {
        networkService.challenge(currentGameId, suggestedIndex).thenAccept(response -> {
            Platform.runLater(() -> {
                isChallengeMode = false;
                useTokenButton.setText("USE TOKEN");
                useTokenButton.setStyle("");
                handleServerResponse(response.statusCode(), response.body(), "Challenge");
            });
        });
    }

    @FXML
    void handleBack(ActionEvent event) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, 
            "Are you sure you want to quit? You will forfeit the match.", 
            ButtonType.YES, ButtonType.NO);
            
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                stopPolling();
                networkService.quitGame(currentGameId);
                try {
                    SceneNavigator.loadScene(SceneNavigator.LOBBY_SCREEN);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void showGameOverAlert(String winner) {
        String myUsername = UserSession.getInstance().getUserName();
        String message = myUsername.equals(winner) ? "Congratulations! You Won! 🎉" : "Game Over! " + winner + " won.";
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Match Finished");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.setOnHidden(e -> {
            try {
                SceneNavigator.loadScene(SceneNavigator.LOBBY_SCREEN);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
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