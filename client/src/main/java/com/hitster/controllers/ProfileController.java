package com.hitster.controllers;

import com.hitster.client.utils.ResponsiveScaler;
import com.hitster.client.utils.SceneNavigator;
import com.hitster.dto.user.MatchHistoryDTO;
import com.hitster.dto.user.UserProfileResponseDTO;
import com.hitster.network.UserNetworkService;
import com.hitster.session.UserSession;

import java.util.Optional;

import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

/**
 * Controls the profile screen, including profile data display, edits, and account deletion.
 */
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
    private AnchorPane rootPane;

    /**
     * Initializes profile table columns, responsive scaling, and profile data loading.
     */
    public void initialize() {
        ResponsiveScaler.bindToWidth(rootPane);
        opponentCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().enemyUsername()));
            
        dateCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().date()));
            
        resultCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().gameStatus()));

        loadUserProfile();
    }

    private void loadUserProfile() {
        userNetworkService.getUserProfile().thenAccept(response -> {
            if (response.statusCode() == 200) {
                String jsonBody = response.body();
                
                Gson gson = new Gson();
                UserProfileResponseDTO userProfile = gson.fromJson(jsonBody, UserProfileResponseDTO.class);
                
                Platform.runLater(() -> {
                    if (usernameLabel != null) usernameLabel.setText(userProfile.username());
                    if (emailLabel != null) emailLabel.setText(userProfile.email());
                    if (totalWinsLabel != null) totalWinsLabel.setText(String.valueOf(userProfile.totalWins()));
                    if (winRateLabel != null) winRateLabel.setText(String.format("%.1f%%", userProfile.winRate()));
                    
                    if (userProfile.matchHistory() != null) {
                        ObservableList<MatchHistoryDTO> tableData = FXCollections.observableArrayList(userProfile.matchHistory());
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
        SceneNavigator.loadScene(SceneNavigator.LOBBY_SCREEN);
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
    private void onDeleteAccountClick() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Delete Account");
        confirmAlert.setHeaderText("Are you sure you want to delete your account?");
        confirmAlert.setContentText("This action is final and your game data cannot be restored.");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                userNetworkService.deleteAccount().thenAccept(httpResponse -> {
                    Platform.runLater(() -> {
                        if (httpResponse.statusCode() == 200) {
                            handleDeletionSuccess();
                        } else {
                            showErrorAlert("Deletion Error", "The server failed to delete the account. Please try again later.");
                        }
                    });
                }).exceptionally(ex -> {
                    Platform.runLater(() -> showErrorAlert("Network Error", "Could not connect to the server."));
                    return null;
                });
            }
        });
    }

    private void handleDeletionSuccess() {
        UserSession.getInstance().cleanUserSession();
        SceneNavigator.loadScene(SceneNavigator.LOGIN_SCREEN);
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    void handleUploadImage(MouseEvent event) {}
}
