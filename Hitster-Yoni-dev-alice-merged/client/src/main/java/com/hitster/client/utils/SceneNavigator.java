package com.hitster.client.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class SceneNavigator {

    public static final String LOGIN_SCREEN = "/views/login.fxml";
    public static final String LOBBY_SCREEN = "/views/lobby.fxml";
    public static final String PROFILE_SCREEN = "/views/profile.fxml";
    public static final String FORGOT_PASSWORD_SCREEN = "/views/forgotPassword.fxml";
    public static final String ADMIN_EDIT_SONGS_SCREEN = "/views/AdminEditSongs.fxml";
    public static final String ADMIN_EDIT_USERS_SCREEN = "/views/AdminEditUsers.fxml";
    public static final String GAME_VIEW_SCREEN = "/views/gameView.fxml";
    public static final String LEADERBOARD_SCREEN = "/views/leaderboard.fxml";
    public static final String REGISTER_SCREEN = "/views/register.fxml"; 

    private static Stage primaryStage;

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    public static void loadScene(String fxmlPath) throws IOException {
        if (primaryStage == null) {
            throw new IllegalStateException("Primary Stage is not set!");
        }

        FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource(fxmlPath));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true); 
        primaryStage.show();
    }
}