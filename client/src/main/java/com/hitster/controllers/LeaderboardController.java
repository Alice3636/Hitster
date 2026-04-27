package com.hitster.controllers;

import com.google.gson.Gson;
import com.hitster.client.utils.ResponsiveScaler;
import com.hitster.client.utils.SceneNavigator;
import com.hitster.dto.user.LeaderboardEntryDTO;
import com.hitster.dto.user.LeaderboardResponseDTO;
import com.hitster.network.UserNetworkService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;

/**
 * Controls the leaderboard screen and displays ranked player results.
 */
public class LeaderboardController {

    @FXML
    private Button backButton;

    @FXML
    private TableView<LeaderboardEntryDTO> leaderboardTable;

    @FXML
    private TableColumn<LeaderboardEntryDTO, String> playerColumn;

    @FXML
    private TableColumn<LeaderboardEntryDTO, Integer> rankColumn;

    @FXML
    private TableColumn<LeaderboardEntryDTO, Integer> winsColumn;

    private final UserNetworkService userNetworkService = new UserNetworkService();

    @FXML 
    private AnchorPane rootPane;

    /**
     * Initializes leaderboard table columns, responsive scaling, and data loading.
     */
    public void initialize() {
        ResponsiveScaler.bindToWidth(rootPane);
        rankColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().rank()));
            
        playerColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().player()));
            
        winsColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().winnings()));

        fillLeaderboard();
    }

    void fillLeaderboard() {
        userNetworkService.getLeaderboard().thenAccept(response -> {
            if (response.statusCode() == 200) {
                String jsonBody = response.body();
                
                Gson gson = new Gson();
                LeaderboardResponseDTO leaderboardResponse = gson.fromJson(jsonBody, LeaderboardResponseDTO.class);
                
                ObservableList<LeaderboardEntryDTO> leaderboardData = FXCollections.observableArrayList(leaderboardResponse.entries());
                
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
        SceneNavigator.loadScene(SceneNavigator.LOBBY_SCREEN);
    }
}
