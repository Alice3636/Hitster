package com.hitster.controllers;

import java.io.IOException;
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
        UserSession.getInstance().setUserName("Alice");
        UserSession.getInstance().setIsAdmin(true);
        UserSession.getInstance().setUserId(36L);

        if (!UserSession.getInstance().getIsAdmin()) {
            adminModeButton.setVisible(false); 
            adminModeButton.setManaged(false); 
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