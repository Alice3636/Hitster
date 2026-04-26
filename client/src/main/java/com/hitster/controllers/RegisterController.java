package com.hitster.controllers;

import com.hitster.client.utils.ResponsiveScaler;
import com.hitster.client.utils.SceneNavigator;
import com.hitster.network.AuthNetworkService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class RegisterController {

    @FXML
    private Button backButton;
    @FXML
    private TextField usernameField;
    @FXML
    private TextField emailField;
    @FXML
    private ImageView profileImageView;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField passwordTextField;
    @FXML
    private ImageView passwordEyeIcon;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private TextField confirmPasswordTextField;
    @FXML
    private ImageView confirmPasswordEyeIcon;
    @FXML
    private Button registerButton;
    @FXML
    private Text LoginHereText;

    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;
    private final AuthNetworkService authService = new AuthNetworkService();

    @FXML 
    private AnchorPane rootPane;

    public void initialize() {
        ResponsiveScaler.bindToWidth(rootPane);
        if (passwordTextField != null && passwordField != null) {
            passwordTextField.textProperty().bindBidirectional(passwordField.textProperty());
        }
        if (confirmPasswordTextField != null && confirmPasswordField != null) {
            confirmPasswordTextField.textProperty().bindBidirectional(confirmPasswordField.textProperty());
        }

        if (LoginHereText != null) {
            LoginHereText.setOnMouseClicked(this::goToLogin);
        }
    }

    @FXML
    void handleImageUpload(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Picture");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            String imagePath = selectedFile.toURI().toString();
            Image image = new Image(imagePath);
            profileImageView.setImage(image);
            profileImageView.setVisible(true);
            profileImageView.setManaged(true);
        }
    }

    @FXML
    void handleRegister(ActionEvent event) {
        String username = usernameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String picturePath = profileImageView.getImage() != null ? profileImageView.getImage().getUrl() : null;

        if (username == null || username.isEmpty() || email == null || email.isEmpty() ||
                password == null || password.isEmpty() || confirmPassword == null || confirmPassword.isEmpty()) {
            showAlert("Error", "Please fill in all fields.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showAlert("Error", "Passwords do not match!");
            return;
        }

        registerButton.setDisable(true);
        registerButton.setText("REGISTERING...");

        authService.register(username, email, password, picturePath).thenAccept(response -> {
            Platform.runLater(() -> {
                registerButton.setDisable(false);
                registerButton.setText("CREATE ACCOUNT");

                if (response.statusCode() == 200) {
                    showInformation("Success", "Your account has been created. Please log in.", Alert.AlertType.INFORMATION);
                    SceneNavigator.loadScene(SceneNavigator.LOGIN_SCREEN);
                } else {
                    showAlert("Registration Failed", response.body());
                }
            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> {
                registerButton.setDisable(false);
                registerButton.setText("CREATE ACCOUNT");
                showAlert("Connection Error", "Could not reach the server. Is it running?\n" + ex.getMessage());
            });
            return null;
        });
    }

    @FXML
    void togglePasswordVisibility(MouseEvent event) {
        isPasswordVisible = !isPasswordVisible;
        if (isPasswordVisible) {
            passwordTextField.setVisible(true);
            passwordField.setVisible(false);
            passwordEyeIcon.setImage(new Image(getClass().getResourceAsStream("/images/eyepurpleopen.png")));
        } else {
            passwordTextField.setVisible(false);
            passwordField.setVisible(true);
            passwordEyeIcon.setImage(new Image(getClass().getResourceAsStream("/images/eyepurpleclosed.png")));
        }
    }

    @FXML
    void toggleConfirmPasswordVisibility(MouseEvent event) {
        isConfirmPasswordVisible = !isConfirmPasswordVisible;
        if (isConfirmPasswordVisible) {
            confirmPasswordTextField.setVisible(true);
            confirmPasswordField.setVisible(false);
            confirmPasswordEyeIcon.setImage(new Image(getClass().getResourceAsStream("/images/eyepurpleopen.png")));
        } else {
            confirmPasswordTextField.setVisible(false);
            confirmPasswordField.setVisible(true);
            confirmPasswordEyeIcon.setImage(new Image(getClass().getResourceAsStream("/images/eyepurpleclosed.png")));
        }
    }

    @FXML
    void goToLogin(MouseEvent event) {
        SceneNavigator.loadScene(SceneNavigator.LOGIN_SCREEN);
    }

    @FXML
    void handleBack(ActionEvent event) {
        SceneNavigator.loadScene(SceneNavigator.LOGIN_SCREEN);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInformation(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}