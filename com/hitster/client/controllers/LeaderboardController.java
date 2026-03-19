package com.hitster.client.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

public class LeaderboardController {

    @FXML
    private Button backButton;

    @FXML
    private TableView<?> leaderboardTable;

    @FXML
    private TableColumn<?, ?> playerColumn;

    @FXML
    private TableColumn<?, ?> rankColumn;

    @FXML
    private TableColumn<?, ?> winsColumn;

    @FXML
    void handleBack(ActionEvent event) {
        try {
            java.io.File file = new java.io.File("resources/views/lobby.fxml");
            java.net.URL url = file.toURI().toURL();
            
            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();
            Scene scene = new Scene(root);
            
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error returning to lobby.");
        }
    }
}
