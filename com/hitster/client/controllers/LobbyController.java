package com.hitster.client.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
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

    @FXML
    void goToAdminMode(ActionEvent event) {
        
    }

    @FXML
    void goToLeaderboard(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/views/leaderboard.fxml"));
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/views/profile.fxml"));
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
            statusLabel.setVisible(false);
            statusLabel.setManaged(false);
            playButton.setText("LETS ROLL!");
            leaderboardButton.setDisable(false);
            hamburgerButton.setDisable(false);
        }
        else {
            statusLabel.setVisible(true);
            statusLabel.setManaged(true);
            playButton.setText("Cancel");
            leaderboardButton.setDisable(true);
            hamburgerButton.setDisable(true);
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