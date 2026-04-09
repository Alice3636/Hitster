package com.hitster.controllers;

import com.hitster.model.PlayerScore;
//import com.hitster.DatabaseLogic;
//import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
//import javafx.scene.control.cell.PropertyValueFactory; 
import javafx.stage.Stage;

public class LeaderboardController {

    @FXML
    private Button backButton;

    @FXML
    private TableView<PlayerScore> leaderboardTable;

    @FXML
    private TableColumn<PlayerScore, String> playerColumn;

    @FXML
    private TableColumn<PlayerScore, Integer> rankColumn;

    @FXML
    private TableColumn<PlayerScore, Integer> winsColumn;

    @FXML
    public void initialize() {
        fillLeaderboard();
    }

    @FXML
    void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/lobby.fxml"));
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

    void fillLeaderboard() {
        /*
        rankColumn.setCellValueFactory(new PropertyValueFactory<>("rank"));
        playerColumn.setCellValueFactory(new PropertyValueFactory<>("player"));
        winsColumn.setCellValueFactory(new PropertyValueFactory<>("winnings"));
        
        ObservableList<PlayerScore> leaderboard = DatabaseLogic.getLeaderboardData();
        
        leaderboardTable.setItems(leaderboard);
        */
    }

}