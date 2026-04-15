package com.hitster.controllers;

import com.hitster.dto.MatchHistoryDTO;
import com.hitster.dto.UserProfileDTO;
import com.hitster.network.UserNetworkService;

import java.util.Optional;

import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class ProfileController {

    @FXML
    private Button backButton;

    @FXML
    private TableColumn<MatchHistoryDTO, String> dateCol;

    @FXML
    private Label emailLabel;

    @FXML
    private TableView<MatchHistoryDTO> matchHistoryTable;

    @FXML
    private TableColumn<MatchHistoryDTO, String> opponentCol;

    @FXML
    private ImageView profileImageView;

    @FXML
    private TableColumn<MatchHistoryDTO, String> resultCol;

    @FXML
    private Label totalWinsLabel;

    @FXML
    private Label usernameLabel;

    @FXML
    private Label winRateLabel;

    private final UserNetworkService userNetworkService = new UserNetworkService();

    @FXML
    public void initialize() {
        opponentCol.setCellValueFactory(new PropertyValueFactory<>("enemyUsername"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        resultCol.setCellValueFactory(new PropertyValueFactory<>("gameStatus"));

        loadUserProfile();
    }

    private void loadUserProfile() {
        userNetworkService.getUserProfile().thenAccept(response -> {
            if (response.statusCode() == 200) {
                String jsonBody = response.body();
                
                Gson gson = new Gson();
                UserProfileDTO userProfile = gson.fromJson(jsonBody, UserProfileDTO.class);
                
                Platform.runLater(() -> {
                    if (usernameLabel != null) usernameLabel.setText(userProfile.getUsername());
                    if (emailLabel != null) emailLabel.setText(userProfile.getEmail());
                    if (totalWinsLabel != null) totalWinsLabel.setText(String.valueOf(userProfile.getTotalWins()));
                    if (winRateLabel != null) winRateLabel.setText(String.format("%.1f%%", userProfile.getWinRate()));
                    
                    if (userProfile.getMatchHistory() != null) {
                        ObservableList<MatchHistoryDTO> tableData = FXCollections.observableArrayList(userProfile.getMatchHistory());
                        matchHistoryTable.setItems(tableData);
                    }
                });
            } else {
                System.out.println("Failed to load profile. Status: " + response.statusCode());
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

    

    @FXML
    void handleEditUsername(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog(usernameLabel.getText());
        dialog.setTitle("Edit Username");
        dialog.setHeaderText("Choose a new username");
        dialog.setContentText("Username:");

        Optional<String> result = dialog.showAndWait();
        
        result.ifPresent(newUsername -> {
            if (!newUsername.trim().isEmpty() && !newUsername.equals(usernameLabel.getText())) {
                sendUpdateRequest(newUsername, emailLabel.getText());
            }
        });
    }

    @FXML
    void handleEditEmail(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog(emailLabel.getText());
        dialog.setTitle("Edit Email");
        dialog.setHeaderText("Enter your new email address");
        dialog.setContentText("Email:");

        Optional<String> result = dialog.showAndWait();
        
        result.ifPresent(newEmail -> {
            if (!newEmail.trim().isEmpty() && !newEmail.equals(emailLabel.getText())) {
                sendUpdateRequest(usernameLabel.getText(), newEmail);
            }
        });
    }

    private void sendUpdateRequest(String newUsername, String newEmail) {
        userNetworkService.updateProfileDetails(newUsername, newEmail).thenAccept(response -> {
            Platform.runLater(() -> {
                if (response.statusCode() == 200) {
                    usernameLabel.setText(newUsername);
                    emailLabel.setText(newEmail);
                    System.out.println("Profile updated successfully");
                } else if (response.statusCode() == 409) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Update Failed");
                    alert.setHeaderText("Error");
                    alert.setContentText("This username or email is already taken!");
                    alert.showAndWait();
                } else {
                    System.out.println("Failed to update. Server returned: " + response.statusCode());
                }
            });
        }).exceptionally(ex -> {
            ex.printStackTrace();
            return null;
        });
    }

    @FXML
    void handleUploadImage(MouseEvent event) {}
}