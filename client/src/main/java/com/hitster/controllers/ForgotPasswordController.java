package com.hitster.controllers;

import com.hitster.client.utils.SceneNavigator;
import com.hitster.network.AuthNetworkService;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import com.hitster.client.utils.ResponsiveScaler;

/**
 * Controls the password recovery flow from reset-code request through password replacement.
 */
public class ForgotPasswordController {
    private final AuthNetworkService authService = new AuthNetworkService();

    @FXML
    private AnchorPane rootPane;
    @FXML
    private Button backButton;
    @FXML
    private Label subtitleLabel;

    @FXML
    private VBox emailStepBox;
    @FXML
    private TextField emailField;
    @FXML
    private Button resetButton;

    @FXML
    private VBox otpStepBox;
    @FXML
    private TextField codeField;
    @FXML
    private PasswordField newPasswordField;
    @FXML
    private Button confirmResetButton;

    /**
     * Initializes responsive scaling for the forgot-password screen.
     */
    public void initialize() {
        ResponsiveScaler.bindToWidth(rootPane);
    }

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

                    emailStepBox.setVisible(false);
                    emailStepBox.setManaged(false);

                    otpStepBox.setVisible(true);
                    otpStepBox.setManaged(true);

                    subtitleLabel.setText("Enter the 6-digit code sent to your email");
                    showAlert("Success", "A verification code has been sent to your email.",
                            Alert.AlertType.INFORMATION);
                } else {
                    String errorMessage = response.body();
                    if (errorMessage == null || errorMessage.trim().isEmpty()) {
                        errorMessage = "Failed to process request. Please try again.";
                    }
                    showAlert("Error", errorMessage, Alert.AlertType.ERROR);
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
    void handleConfirmReset(ActionEvent event) {
        String email = emailField.getText();
        String code = codeField.getText();
        String newPassword = newPasswordField.getText();

        if (code == null || code.trim().isEmpty() || newPassword == null || newPassword.trim().isEmpty()) {
            showAlert("Error", "Please enter both the verification code and your new password.", Alert.AlertType.ERROR);
            return;
        }

        confirmResetButton.setDisable(true);
        confirmResetButton.setText("VERIFYING...");

        authService.resetPassword(email, code, newPassword).thenAccept(response -> {
            Platform.runLater(() -> {
                confirmResetButton.setDisable(false);
                confirmResetButton.setText("CONFIRM RESET");

                if (response.statusCode() == 200) {
                    showAlert("Success", "Your password has been successfully reset! You can now log in.",
                            Alert.AlertType.INFORMATION);
                    SceneNavigator.loadScene(SceneNavigator.LOGIN_SCREEN);
                } else {
                    showAlert("Error", "Invalid code or failed to reset password.", Alert.AlertType.ERROR);
                }
            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> {
                confirmResetButton.setDisable(false);
                confirmResetButton.setText("CONFIRM RESET");
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
