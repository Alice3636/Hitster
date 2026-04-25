package com.hitster.controllers;

import com.hitster.client.utils.SceneNavigator;
import com.hitster.network.AuthNetworkService;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

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
                    SceneNavigator.loadScene(SceneNavigator.LOGIN_SCREEN);
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
        SceneNavigator.loadScene(SceneNavigator.LOGIN_SCREEN);
    }

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}