package com.hitster.client;

import com.hitster.client.utils.SceneNavigator;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Main JavaFX application class responsible for preparing the primary stage.
 */
public class ClientApp extends Application {

    /**
     * Initializes the primary stage and opens the login screen.
     *
     * @param primaryStage JavaFX stage supplied by the runtime
     */
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Hitster");

        SceneNavigator.setPrimaryStage(primaryStage);
        SceneNavigator.loadScene(SceneNavigator.LOGIN_SCREEN);
    }

    /**
     * Launches the JavaFX runtime for the client application.
     *
     * @param args command-line arguments passed to the application
     */
    public static void main(String[] args) {
        launch(args);
    }
}
