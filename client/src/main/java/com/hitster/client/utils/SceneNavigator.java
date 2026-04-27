package com.hitster.client.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

/**
 * Centralizes JavaFX scene loading and navigation between FXML screens.
 */
public class SceneNavigator {

    public static final String LOGIN_SCREEN = "/views/login.fxml";
    public static final String LOBBY_SCREEN = "/views/lobby.fxml";
    public static final String PROFILE_SCREEN = "/views/profile.fxml";
    public static final String FORGOT_PASSWORD_SCREEN = "/views/forgotPassword.fxml";
    public static final String ADMIN_EDIT_SONGS_SCREEN = "/views/AdminEditSongs.fxml";
    public static final String ADMIN_EDIT_USERS_SCREEN = "/views/AdminEditAccounts.fxml";
    public static final String GAME_VIEW_SCREEN = "/views/gameView.fxml";
    public static final String LEADERBOARD_SCREEN = "/views/leaderboard.fxml";
    public static final String REGISTER_SCREEN = "/views/register.fxml"; 

    private static Stage primaryStage;

    /**
     * Registers the primary stage used for all scene transitions.
     *
     * @param stage application stage managed by the JavaFX runtime
     */
    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    /**
     * Loads an FXML screen into the primary stage and displays it maximized.
     *
     * @param fxmlPath classpath path to the FXML resource
     * @throws IllegalStateException if the primary stage has not been registered
     * @throws RuntimeException if the requested FXML resource cannot be loaded
     */
    public static void loadScene(String fxmlPath) {
        try {
            if (primaryStage == null) {
                throw new IllegalStateException("Primary Stage is not set!");
            }

            FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource(fxmlPath));
            Parent root = loader.load();

            if (primaryStage.getScene() == null) {
                primaryStage.setScene(new Scene(root));
            } else {
                primaryStage.getScene().setRoot(root);
            }

            primaryStage.setMaximized(true);
            primaryStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load scene: " + fxmlPath);
        }
    }
}
