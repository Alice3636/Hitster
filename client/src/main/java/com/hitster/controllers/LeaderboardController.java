package com.hitster.controllers;

import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    // Fixed the service name here!
    private final StatsNetworkService statsNetworkService = new StatsNetworkService();

    @FXML
    public void initialize() {
        rankColumn.setCellValueFactory(new PropertyValueFactory<>("rank"));
        playerColumn.setCellValueFactory(new PropertyValueFactory<>("player"));
        winsColumn.setCellValueFactory(new PropertyValueFactory<>("winnings"));

        fillLeaderboard();
    }

    void fillLeaderboard() {
        // Make sure your StatsNetworkService has a getLeaderboard() method!
        statsNetworkService.getLeaderboard().thenAccept(response -> {
            if (response.statusCode() == 200) {
                try {
                    String jsonBody = response.body();

                    // Swapped GSON for Jackson (ObjectMapper) to match your Admin screens
                    ObjectMapper mapper = new ObjectMapper();
                    List<PlayerScore> scoreList = mapper.readValue(
                            jsonBody,
                            new TypeReference<List<PlayerScore>>() {
                            });

                    ObservableList<PlayerScore> leaderboardData = FXCollections.observableArrayList(scoreList);

                    Platform.runLater(() -> {
                        leaderboardTable.setItems(leaderboardData);
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Failed to parse leaderboard data.");
                }
            } else {
                System.out.println("Failed to fetch leaderboard: " + response.statusCode());
            }
        }).exceptionally(ex -> {
            ex.printStackTrace();
            return null;
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
            System.out.println("Error returning to lobby.");
        }
    }
}