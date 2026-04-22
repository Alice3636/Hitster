package com.hitster.controllers;

import java.io.IOException;
import java.util.Base64;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.hitster.client.utils.SceneNavigator;
import com.hitster.network.GameNetworkService;
import com.hitster.session.GameManager;
import com.hitster.session.UserSession;
import com.hitster.dto.lobby.LobbyStatusResponseDTO;

import javafx.application.Platform;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class LobbyController {

    @FXML
    private Button adminModeButton;
    
    @FXML
    private Label searchingLabel; 

    @FXML
    private Button hamburgerButton;

    @FXML
    private Button leaderboardButton;

    @FXML
    private Button logoutButton;

    @FXML
    private Button playButton;

    @FXML
    private Button profileButton;

    @FXML
    private VBox sideMenuPanel;

    @FXML
    private Label statusLabel;

    private final GameNetworkService networkService = new GameNetworkService();
    private ScheduledExecutorService pollingExecutor;
    private final Gson gson = new Gson();

    @FXML
    public void initialize() {
        // מפענח את הטוקן ומושך את הנתונים האמיתיים של המשתמש שהתחבר
        decodeTokenAndSetSession();

        // מסתיר את כפתור האדמין אם המשתמש הוא לא אדמין
        if (!UserSession.getInstance().getIsAdmin()) {
            adminModeButton.setVisible(false); 
            adminModeButton.setManaged(false); 
        }
    }

    private void decodeTokenAndSetSession() {
        String token = UserSession.getInstance().getToken();
        if (token != null && !token.isEmpty()) {
            try {
                // JWT בנוי מ-3 חלקים מופרדים בנקודה. החלק האמצעי (אינדקס 1) הוא ה-Payload
                String[] chunks = token.split("\\.");
                if (chunks.length > 1) {
                    Base64.Decoder decoder = Base64.getUrlDecoder();
                    String payload = new String(decoder.decode(chunks[1]));
                    
                    JsonObject jsonObject = JsonParser.parseString(payload).getAsJsonObject();
                    
                    // חילוץ הנתונים בדיוק לפי איך שמוגדר ב-JwtUtil.java בשרת שלכם
                    if (jsonObject.has("userId")) {
                        UserSession.getInstance().setUserId(jsonObject.get("userId").getAsLong());
                    }
                    if (jsonObject.has("username")) {
                        UserSession.getInstance().setUserName(jsonObject.get("username").getAsString());
                    }
                    if (jsonObject.has("isAdmin")) {
                        UserSession.getInstance().setIsAdmin(jsonObject.get("isAdmin").getAsBoolean());
                    }
                }
            } catch (Exception e) {
                System.err.println("Failed to decode JWT: " + e.getMessage());
            }
        }
    }

    @FXML
    void goToAdminMode(ActionEvent event) {
         try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/AdminEditSongs.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setMaximized(true); 
            stage.show();
        } 
        catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error loading screen.");
        }
    }

    @FXML
    void goToLeaderboard(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/leaderboard.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setMaximized(true); 
            stage.show();
        } 
        catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error loading screen.");
        }
    }

    @FXML
    void goToProfile(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/profile.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setMaximized(true); 
            stage.show();
        } 
        catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error loading screen.");
        }
    }

    @FXML
    void handleLogout(ActionEvent event) {
    }

    @FXML
    void handlePlay(ActionEvent event) {
        if (playButton.getText().equals("Cancel")) {
            stopPolling();
            statusLabel.setVisible(false);
            statusLabel.setManaged(false);
            playButton.setText("LETS ROLL!");
            leaderboardButton.setDisable(false);
            hamburgerButton.setDisable(false);
            networkService.leaveLobby();
        }
        else {
            networkService.joinLobby().thenAccept(response -> {
                if (response.statusCode() == 200) {
                    Platform.runLater(() -> {
                        statusLabel.setText("Looking for an opponent...");
                        playButton.setText("Cancel");
                        statusLabel.setVisible(true);
                        statusLabel.setManaged(true);
                        leaderboardButton.setDisable(true);
                        hamburgerButton.setDisable(true);
                    });
                    startMatchmakingPolling();
                } else {
                    Platform.runLater(() ->  statusLabel.setText("Failed joining the queue!"));
                }
            });
        }
    }

    private void startMatchmakingPolling() {
        pollingExecutor = Executors.newSingleThreadScheduledExecutor();
        pollingExecutor.scheduleAtFixedRate(() -> {
            networkService.checkMatchStatus().thenAccept(response -> {
                if (response.statusCode() == 200) {
                    LobbyStatusResponseDTO statusResponse = gson.fromJson(response.body(), LobbyStatusResponseDTO.class);
                    
                    if ("FOUND".equals(statusResponse.status())) {
                        stopPolling();
                        
                        GameManager.getInstance().startGame(statusResponse.gameId());
                        
                        Platform.runLater(() -> {
                            try {
                                SceneNavigator.loadScene(SceneNavigator.GAME_VIEW_SCREEN);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
                    }
                }
            });
        }, 0, 2, TimeUnit.SECONDS);
    }

    private void stopPolling() {
        if (pollingExecutor != null && !pollingExecutor.isShutdown()) {
            pollingExecutor.shutdown();
        }
    }

    @FXML
    void toggleSideMenu(ActionEvent event) {
        sideMenuPanel.setVisible(true);
    }

    @FXML
    void closeSideMenu(ActionEvent event) {
        sideMenuPanel.setVisible(false);
    }
}