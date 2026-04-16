package com.hitster.client;

import com.hitster.client.utils.SceneNavigator;

import javafx.application.Application;
import javafx.stage.Stage;

public class ClientApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
       SceneNavigator.setPrimaryStage(primaryStage);
        SceneNavigator.loadScene(SceneNavigator.LOBBY_SCREEN);
    }
    

    public static void main(String[] args) {
        launch(args); 
    }
}