package com.hitster.controllers;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.hitster.client.utils.ResponsiveScaler;
import com.hitster.client.utils.SceneNavigator;
import com.hitster.network.GameNetworkService;
import com.hitster.session.GameManager;
import com.hitster.session.UserSession;
import com.hitster.dto.lobby.LobbyStatusResponseDTO;

import javafx.application.Platform;
import com.google.gson.Gson;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

/**
 * Controls lobby navigation, matchmaking queue actions, and match status polling.
 */
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
    private AnchorPane rootPane;

    /**
     * Initializes lobby layout scaling and hides administrator navigation for non-admin users.
     */
    public void initialize() {
        ResponsiveScaler.bindToWidth(rootPane);


        if (!UserSession.getInstance().getIsAdmin()) {
            adminModeButton.setVisible(false); 
            adminModeButton.setManaged(false); 
        }
    }

    @FXML
    void goToAdminMode(ActionEvent event) {
        SceneNavigator.loadScene(SceneNavigator.ADMIN_EDIT_SONGS_SCREEN);
    }

    @FXML
    void goToLeaderboard(ActionEvent event) {
        SceneNavigator.loadScene(SceneNavigator.LEADERBOARD_SCREEN);
    }

    @FXML
    void goToProfile(ActionEvent event) {
        SceneNavigator.loadScene(SceneNavigator.PROFILE_SCREEN);
    }

    @FXML
    void handleLogout(ActionEvent event) {
        UserSession.getInstance().cleanUserSession();
        SceneNavigator.loadScene(SceneNavigator.LOGIN_SCREEN);
    }

    @FXML
    void handlePlay(ActionEvent event) {
        if (playButton.getText().equals("Cancel")) {
            stopPolling();
            statusLabel.setVisible(false);
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
                            SceneNavigator.loadScene(SceneNavigator.GAME_VIEW_SCREEN);
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
