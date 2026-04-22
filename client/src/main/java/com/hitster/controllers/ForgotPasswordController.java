package com.hitster.controllers;

import com.hitster.network.AuthNetworkService;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ForgotPasswordController {

    @FXML private Button backButton;
    @FXML private TextField emailField;
    @FXML private Button resetButton;

    private final AuthNetworkService authService = new AuthNetworkService();

    @FXML
    void handleResetPassword(ActionEvent event) {
        String email = emailField.getText();

        if (email == null || email.trim().isEmpty()) {
            showAlert("Error", "Please enter your email address.", Alert.AlertType.ERROR);
            return;
        }

        resetButton.setDisable(true);
        resetButton.setText("SENDING...");

        authService.forgotPassword(email).thenAccept(response -> {
            Platform.runLater(() -> {
                resetButton.setDisable(false);
                resetButton.setText("SEND RESET LINK");

                if (response.statusCode() == 200) {
                    // Show a success message and send them back to login
                    showAlert("Success", "A reset link has been sent.", Alert.AlertType.INFORMATION);
                    navigateToNode((Node) event.getSource(), "/views/login.fxml");
                } else {
                    showAlert("Error", "Failed to process request. Please try again.", Alert.AlertType.ERROR);
                }
            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> {
                resetButton.setDisable(false);
                resetButton.setText("SEND RESET LINK");
                showAlert("Connection Error", "Could not reach the server.\n" + ex.getMessage(), Alert.AlertType.ERROR);
            });
            return null;
        });
    }

    @FXML
    void handleBack(ActionEvent event) {
        navigateToNode((Node) event.getSource(), "/views/login.fxml");
    }

    private void navigateToNode(Node sourceNode, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            
            Scene currentScene = sourceNode.getScene();
            Stage stage = (Stage) currentScene.getWindow();
            
            currentScene.setRoot(root);
            
            if (!stage.isMaximized()) {
                stage.setMaximized(true);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to load screen: " + fxmlPath);
        }
    }

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}