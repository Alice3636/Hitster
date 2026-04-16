package com.hitster.controllers;

import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
//import com.fasterxml.jackson.databind.deser.DataFormatReaders.Match;
//import com.hitster.DatabaseLogic;
//import com.hitster.model.PlayerScore;
import com.hitster.model.MatchHistoryObj;
import com.hitster.network.StatsNetworkService;
import com.hitster.session.UserSession;
import java.lang.reflect.Type;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
//import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
//import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class ProfileController {

    private final StatsNetworkService statsService = new StatsNetworkService();
    @FXML
    private Button backButton;

    @FXML
    private TableColumn<MatchHistoryObj, String> dateCol;

    @FXML
    private Label emailLabel;

    @FXML
    private TableView<MatchHistoryObj> matchHistoryTable;

    @FXML
    private TableColumn<MatchHistoryObj, String> opponentCol;

    @FXML
    private ImageView profileImageView;

    @FXML
    private TableColumn<MatchHistoryObj, String> resultCol;

    @FXML
    private Label totalWinsLabel;

    @FXML
    private Label usernameLabel;

    @FXML
    private Label winRateLabel;

    @FXML
    public void initialize() {
        opponentCol.setCellValueFactory(new PropertyValueFactory<>("enemy_username"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        resultCol.setCellValueFactory(new PropertyValueFactory<>("gameStatus")); // Or "result", match your object!

        fillMatchHistory();
        /*
         * int currentUserId = 9;
         * String username = DatabaseLogic.getUsername(currentUserId);
         * String email = DatabaseLogic.getEmail(currentUserId);
         * int winnings = DatabaseLogic.getTotalWinnings(currentUserId);
         * String winRate = DatabaseLogic.getWinRate(currentUserId);
         * 
         * fillMatchHistory();
         * 
         * if (totalWinsLabel != null) {
         * totalWinsLabel.setText(String.valueOf(winnings));
         * }
         * 
         * if (usernameLabel != null) {
         * usernameLabel.setText(username);
         * }
         * 
         * if (emailLabel != null) {
         * emailLabel.setText(email);
         * }
         * 
         * if (winRateLabel != null) {
         * winRateLabel.setText(winRate);
         * }
         */
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

    @FXML
    void fillMatchHistory() {
        Long myUserId = UserSession.getInstance().getUserId();

        if (myUserId == null)
            return;

        statsService.getMatchHistory(myUserId).thenAccept(response -> {
            if (response.statusCode() == 200) {
                Gson gson = new Gson();
                Type listType = new TypeToken<List<MatchHistoryObj>>() {
                }.getType();
                List<MatchHistoryObj> historyData = gson.fromJson(response.body(), listType);

                Platform.runLater(() -> {
                    ObservableList<MatchHistoryObj> matchHistory = FXCollections.observableArrayList(historyData);
                    matchHistoryTable.setItems(matchHistory);
                });
            }
        });
    }

    @FXML
    void handleDeleteAccount(ActionEvent event) {

    }

    @FXML
    void handleEditEmail(ActionEvent event) {

    }

    @FXML
    void handleEditUsername(ActionEvent event) {

    }

    @FXML
    void handleUploadImage(MouseEvent event) {

    }

}