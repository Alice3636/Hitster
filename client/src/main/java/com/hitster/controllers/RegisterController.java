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
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
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
    public void initialize() {

        if (passwordTextField != null && passwordField != null) {
            passwordTextField.textProperty().bindBidirectional(passwordField.textProperty());
        }
        if (confirmPasswordTextField != null && confirmPasswordField != null) {
            confirmPasswordTextField.textProperty().bindBidirectional(confirmPasswordField.textProperty());
        }

        // Bind the text click event programmatically since it's missing from the FXML
        if (LoginHereText != null) {
            LoginHereText.setOnMouseClicked(this::goToLogin);
        }
    }

    @FXML
    void handleImageUpload(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Picture");
        
        // Filter for image files only
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        // Get the current stage to display the file chooser dialog
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            // Convert file path to URI and set the image
            String imagePath = selectedFile.toURI().toString();
            Image image = new Image(imagePath);
            profileImageView.setImage(image);
            
            // Make the image view visible once a picture is selected
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
                    navigateToNode((Node) event.getSource(), "/views/login.fxml");
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
        navigateToNode((Node) event.getSource(), "/views/login.fxml");
    }

    @FXML
    void handleBack(ActionEvent event) {
        navigateToNode((Node) event.getSource(), "/views/login.fxml");
    }

    private void navigateToNode(Node sourceNode, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) sourceNode.getScene().getWindow();
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to load screen: " + fxmlPath);
        }
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
