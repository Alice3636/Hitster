package com.hitster.controllers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hitster.client.utils.ResponsiveScaler;
import com.hitster.client.utils.SceneNavigator;
import com.hitster.dto.auth.LoginResponseDTO;
import com.hitster.network.AuthNetworkService;
import com.hitster.session.UserSession;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;

import java.util.Map;

/**
 * Controls the login screen and stores authenticated user details after a successful login.
 */
public class LoginController {

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
    private AnchorPane rootPane;

    /**
     * Initializes responsive scaling and links the password field with its visible-text counterpart.
     */
    public void initialize() {
        ResponsiveScaler.bindToWidth(rootPane);
        passwordTextField.textProperty().bindBidirectional(passwordField.textProperty());
    }

    @FXML
    void handleLogin(ActionEvent event) {
        String email = emailField.getText();
        String password = passwordField.getText();

        // Client-side validation mirroring the server's IllegalArgumentException
        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            showAlert("Validation Error", "Email and password are required.");
            return;
        }

        loginButton.setDisable(true);
        loginButton.setText("LOGGING IN...");

        authService.login(email, password).thenAccept(response -> {
            Platform.runLater(() -> {
                loginButton.setDisable(false);
                loginButton.setText("LOGIN");

                if (response.statusCode() == 200) {
                    try {
                        Gson gson = new Gson();
                        LoginResponseDTO loginResponse = gson.fromJson(response.body(), LoginResponseDTO.class);

                        UserSession.getInstance().setToken(loginResponse.token());
                        UserSession.getInstance().setUserName(loginResponse.username());
                        UserSession.getInstance().setIsAdmin(loginResponse.isAdmin());
                        UserSession.getInstance().setUserId(loginResponse.userId());

                        SceneNavigator.loadScene(SceneNavigator.LOBBY_SCREEN);

                    } catch (Exception e) {
                        e.printStackTrace();
                        showAlert("Error", "Failed to parse login response.");
                    }
                } else {

                    String errorMessage = "An unexpected error occurred.";
                    try {
                        Gson gson = new Gson();

                        Map<String, Object> errorMap = gson.fromJson(response.body(),
                                new TypeToken<Map<String, Object>>() {
                                }.getType());

                        if (errorMap != null && errorMap.containsKey("message")) {
                            errorMessage = (String) errorMap.get("message");
                        } else if (response.body() != null && !response.body().isEmpty()) {

                            errorMessage = response.body();
                        }
                    } catch (Exception e) {

                        if (response.statusCode() == 401) {
                            errorMessage = "Invalid email or password.";
                        } else if (response.statusCode() == 400) {
                            errorMessage = "Email and password are required.";
                        }
                    }

                    showAlert("Login Failed", errorMessage);
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
        SceneNavigator.loadScene(SceneNavigator.REGISTER_SCREEN);
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
        SceneNavigator.loadScene(SceneNavigator.FORGOT_PASSWORD_SCREEN);
    }
}
