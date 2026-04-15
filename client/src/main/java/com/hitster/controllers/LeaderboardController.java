package com.hitster.controllers;

import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hitster.dto.PlayerScoreDTO;
import com.hitster.network.UserNetworkService;
import java.lang.reflect.Type;
import java.util.List;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import javafx.scene.control.cell.PropertyValueFactory;
//import javafx.scene.control.cell.PropertyValueFactory; 
import javafx.stage.Stage;

public class LeaderboardController {

    @FXML
    private Button backButton;

    @FXML
    private TableView<PlayerScoreDTO> leaderboardTable;

    @FXML
    private TableColumn<PlayerScoreDTO, String> playerColumn;

    @FXML
    private TableColumn<PlayerScoreDTO, Integer> rankColumn;

    @FXML
    private TableColumn<PlayerScoreDTO, Integer> winsColumn;

    private final UserNetworkService userNetworkService = new UserNetworkService();
    

    @FXML
        public void initialize() {
            rankColumn.setCellValueFactory(new PropertyValueFactory<>("rank"));
            playerColumn.setCellValueFactory(new PropertyValueFactory<>("player"));
            winsColumn.setCellValueFactory(new PropertyValueFactory<>("winnings"));

            fillLeaderboard();
        }

        void fillLeaderboard() {
            userNetworkService.getLeaderboard().thenAccept(response -> {
                if (response.statusCode() == 200) {
                    String jsonBody = response.body();
                    
                    Gson gson = new Gson();
                    Type listType = new TypeToken<List<PlayerScoreDTO>>(){}.getType();
                    List<PlayerScoreDTO> scoreList = gson.fromJson(jsonBody, listType);
                    
                    ObservableList<PlayerScoreDTO> leaderboardData = FXCollections.observableArrayList(scoreList);
                    
                    Platform.runLater(() -> {
                        leaderboardTable.setItems(leaderboardData);
                    });
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