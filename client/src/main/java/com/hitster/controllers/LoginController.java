package com.hitster.controllers;

import com.google.gson.Gson;
import com.hitster.dto.auth.LoginResponseDTO;
import com.hitster.network.AuthNetworkService;
import com.hitster.session.UserSession;

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
import javafx.stage.Stage;

public class LoginController {

    @FXML
    private Button backButton;
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField passwordTextField;
    @FXML
    private ImageView passwordEyeIcon;
    @FXML
    private Button loginButton;
    @FXML
    private Text RegisterHereText;

    private boolean isPasswordVisible = false;
    private final AuthNetworkService authService = new AuthNetworkService();

    @FXML
    public void initialize() {

        passwordTextField.textProperty().bindBidirectional(passwordField.textProperty());
    }

    @FXML
    void handleLogin(ActionEvent event) {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            showAlert("Error", "Please enter both email and password.");
            return;
        }

        loginButton.setDisable(true);
        loginButton.setText("LOGGING IN...");

        authService.login(email, password).thenAccept(response -> {
            Platform.runLater(() -> {
                loginButton.setDisable(false);
                loginButton.setText("LOGIN");

                if (response.statusCode() == 200) {
                    Gson gson = new Gson();
                    LoginResponseDTO loginResponse = gson.fromJson(response.body(), LoginResponseDTO.class);

                    UserSession.getInstance().setToken(loginResponse.token());
                    UserSession.getInstance().setUserName(loginResponse.username());
                    UserSession.getInstance().setIsAdmin(loginResponse.isAdmin());
                    UserSession.getInstance().setUserId(loginResponse.userId());

                    navigateTo(event, "/views/lobby.fxml");
                } else {
                    showAlert("Login Failed", "Invalid email or password.");
                }
            });
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
    void goToRegister(MouseEvent event) {

        navigateToNode((Node) event.getSource(), "/views/register.fxml");
    }

    @FXML
    void handleBack(ActionEvent event) {

    }

    private void navigateTo(ActionEvent event, String fxmlPath) {
        navigateToNode((Node) event.getSource(), fxmlPath);
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

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    void goToForgotPassword(MouseEvent event) {
        navigateToNode((Node) event.getSource(), "/views/forgotPassword.fxml");
    }
}