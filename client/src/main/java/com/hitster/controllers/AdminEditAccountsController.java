package com.hitster.controllers;

import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.stage.Stage;

import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.hitster.dto.admin.UserEntryDTO;
import com.hitster.dto.admin.UsersResponseDTO;
import com.hitster.network.AdminNetworkService;

public class AdminEditAccountsController {

    @FXML
    private Button navSongsButton;
    @FXML
    private Button navUsersButton;
    @FXML
    private TextField searchField;
    @FXML
    private Button deleteSelectedButton;
    @FXML
    private TableView<UserRow> usersTable;
    @FXML
    private TableColumn<UserRow, Boolean> selectColumn;
    @FXML
    private TableColumn<UserRow, Integer> idColumn;
    @FXML
    private TableColumn<UserRow, String> usernameColumn;
    @FXML
    private TableColumn<UserRow, String> emailColumn;
    @FXML
    private Button backButton;

    // The raw data list and the filtered list for the search bar
    private final ObservableList<UserRow> masterUserData = FXCollections.observableArrayList();
    private FilteredList<UserRow> filteredData;

    @FXML
    private final AdminNetworkService adminService = new AdminNetworkService();

    public void initialize() {
        // 1. Setup the Table Columns
        idColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());
        usernameColumn.setCellValueFactory(cellData -> cellData.getValue().usernameProperty());
        emailColumn.setCellValueFactory(cellData -> cellData.getValue().emailProperty());

        // Setup the Checkbox column
        selectColumn.setCellValueFactory(cellData -> cellData.getValue().selectedProperty());
        selectColumn.setCellFactory(CheckBoxTableCell.forTableColumn(selectColumn));

        // Make the table editable so checkboxes can be clicked
        usersTable.setEditable(true);

        // 2. Setup the Search Filter
        filteredData = new FilteredList<>(masterUserData, p -> true);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(user -> {
                if (newValue == null || newValue.isEmpty())
                    return true;
                String lowerCaseFilter = newValue.toLowerCase();
                return user.getUsername().toLowerCase().contains(lowerCaseFilter) ||
                        user.getEmail().toLowerCase().contains(lowerCaseFilter);
            });
        });

        // Bind the sorted/filtered data to the table
        SortedList<UserRow> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(usersTable.comparatorProperty());
        usersTable.setItems(sortedData);

        // 3. Navigation Bindings
        navSongsButton.setOnAction(e -> navigateToNode((Node) e.getSource(), "/views/AdminEditSongs.fxml"));

        // 4. Delete Action
        deleteSelectedButton.setOnAction(this::handleDeleteSelected);

        // 5. Load Data from Backend (Simulated here)
        loadUserData();
    }

    private void loadUserData() {
        adminService.getAllUsers().thenAccept(response -> {
            Platform.runLater(() -> {
                if (response.statusCode() == 200) {
                    try {
                        Gson gson = new Gson();

                        UsersResponseDTO usersResponse = gson.fromJson(response.body(), UsersResponseDTO.class);
                        masterUserData.clear();

                        for (UserEntryDTO user : usersResponse.users()) {
                            masterUserData.add(
                                    new UserRow(
                                            (int) user.id(), // your UI still uses int
                                            user.username(),
                                            user.email()
                                    )
                            );
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        showAlert("Parse Error", "Failed to read user data from server.", Alert.AlertType.ERROR);
                    }
                } else {
                    showAlert("Server Error",
                            "Failed to load users. Status: " + response.statusCode(),
                            Alert.AlertType.ERROR);
                }
            });
        }).exceptionally(ex -> {
            Platform.runLater(() ->
                    showAlert("Connection Error",
                            "Could not reach the server.\n" + ex.getMessage(),
                            Alert.AlertType.ERROR)
            );
            return null;
        });
    }

    private void handleDeleteSelected(ActionEvent event) {
        List<UserRow> selectedUsers = masterUserData.stream()
                .filter(UserRow::isSelected)
                .collect(Collectors.toList());

        if (selectedUsers.isEmpty()) {
            showAlert("No Selection", "Please select at least one user to delete.", Alert.AlertType.WARNING);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Are you sure you want to delete " + selectedUsers.size() + " user(s)? This cannot be undone.",
                ButtonType.YES, ButtonType.NO);

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {

                List<Long> userIds = selectedUsers.stream()
                        .map(user -> (long) user.getId()) // convert int → long
                        .collect(Collectors.toList());
                
                deleteSelectedButton.setDisable(true);
                adminService.deleteUsers(userIds)
                        .thenAccept(res -> {
                            Platform.runLater(() -> {
                                deleteSelectedButton.setDisable(false);
                                if (res.statusCode() == 200) {
                                    masterUserData.removeAll(selectedUsers);

                                    showAlert("Success", "Users deleted successfully.", Alert.AlertType.INFORMATION);
                                } else {
                                    showAlert("Error",
                                            "Failed to delete users. Status: " + res.statusCode(),
                                            Alert.AlertType.ERROR);
                                }
                            });
                        })
                        .exceptionally(ex -> {
                            Platform.runLater(() ->
                                    showAlert("Connection Error",
                                            "Could not reach the server.\n" + ex.getMessage(),
                                            Alert.AlertType.ERROR)
                            );
                            return null;
                        });
            }
        });
    }

    @FXML
    void handleBack(ActionEvent event) {
        navigateToNode((Node) event.getSource(), "/views/lobby.fxml");
    }

    private void navigateToNode(Node sourceNode, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            
            Scene scene = sourceNode.getScene();
            Stage stage = (Stage) scene.getWindow();
            
            scene.setRoot(root);
            
            if (!stage.isMaximized()) {
                stage.setMaximized(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error loading: " + fxmlPath);
        }
    }

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class UserRow {
        private final BooleanProperty selected;
        private final IntegerProperty id;
        private final StringProperty username;
        private final StringProperty email;

        public UserRow(int id, String username, String email) {
            this.selected = new SimpleBooleanProperty(false);
            this.id = new SimpleIntegerProperty(id);
            this.username = new SimpleStringProperty(username);
            this.email = new SimpleStringProperty(email);
        }

        public boolean isSelected() {
            return selected.get();
        }

        public void setSelected(boolean value) {
            selected.set(value);
        }

        public BooleanProperty selectedProperty() {
            return selected;
        }

        public int getId() {
            return id.get();
        }

        public IntegerProperty idProperty() {
            return id;
        }

        public String getUsername() {
            return username.get();
        }

        public StringProperty usernameProperty() {
            return username;
        }

        public String getEmail() {
            return email.get();
        }

        public StringProperty emailProperty() {
            return email;
        }
    }
}