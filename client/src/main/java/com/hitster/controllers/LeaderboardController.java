package com.hitster.controllers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hitster.model.PlayerScore;
import com.hitster.network.StatsNetworkService;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.lang.reflect.Type;
import java.util.List;

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
    private final StatsNetworkService statsService = new StatsNetworkService();

    @FXML
    public void initialize() {
        rankColumn.setCellValueFactory(new PropertyValueFactory<>("rank"));
        playerColumn.setCellValueFactory(new PropertyValueFactory<>("player"));
        winsColumn.setCellValueFactory(new PropertyValueFactory<>("winnings"));

        fillLeaderboard();
    }

    void fillLeaderboard() {
        statsService.getLeaderboard().thenAccept(response -> {
            if (response.statusCode() == 200) {
                // Parse the JSON array into a List of PlayerScore objects
                Gson gson = new Gson();
                Type listType = new TypeToken<List<PlayerScore>>() {
                }.getType();
                List<PlayerScore> serverData = gson.fromJson(response.body(), listType);

                // Convert to JavaFX ObservableList and update UI
                Platform.runLater(() -> {
                    ObservableList<PlayerScore> leaderboard = FXCollections.observableArrayList(serverData);
                    leaderboardTable.setItems(leaderboard);
                });
            } else {
                System.err.println("Failed to load leaderboard: " + response.statusCode());
            }
        });
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
        }
    }
}