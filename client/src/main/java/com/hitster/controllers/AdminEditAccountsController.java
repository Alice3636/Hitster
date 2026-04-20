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
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hitster.network.AdminNetworkService;

public class AdminEditAccountsController {

    @FXML private Button navSongsButton;
    @FXML private Button navUsersButton;
    @FXML private TextField searchField;
    @FXML private Button deleteSelectedButton;
    @FXML private TableView<UserRow> usersTable;
    @FXML private TableColumn<UserRow, Boolean> selectColumn;
    @FXML private TableColumn<UserRow, Integer> idColumn;
    @FXML private TableColumn<UserRow, String> usernameColumn;
    @FXML private TableColumn<UserRow, String> emailColumn;
    @FXML private Button backButton;

    private final ObservableList<UserRow> masterUserData = FXCollections.observableArrayList();
    private FilteredList<UserRow> filteredData;

    private final AdminNetworkService adminService = new AdminNetworkService();

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());
        usernameColumn.setCellValueFactory(cellData -> cellData.getValue().usernameProperty());
        emailColumn.setCellValueFactory(cellData -> cellData.getValue().emailProperty());

        selectColumn.setCellValueFactory(cellData -> cellData.getValue().selectedProperty());
        selectColumn.setCellFactory(CheckBoxTableCell.forTableColumn(selectColumn));

        usersTable.setEditable(true);

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

        SortedList<UserRow> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(usersTable.comparatorProperty());
        usersTable.setItems(sortedData);

        navSongsButton.setOnAction(e -> navigateToNode((Node) e.getSource(), "/views/AdminEditSongs.fxml"));
        deleteSelectedButton.setOnAction(this::handleDeleteSelected);

        loadUserData();
    }

    private void loadUserData() {
        adminService.getAllUsers().thenAccept(response -> {
            Platform.runLater(() -> {
                if (response.statusCode() == 200) {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        List<Map<String, Object>> users = mapper.readValue(
                                response.body(),
                                new TypeReference<List<Map<String, Object>>>() {
                                });

                        masterUserData.clear();

                        for (Map<String, Object> u : users) {
                            int id = ((Number) u.get("id")).intValue();
                            String username = (String) u.get("username");
                            String email = (String) u.get("email");

                            masterUserData.add(new UserRow(id, username, email));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        showAlert("Parse Error", "Failed to read user data from server.", Alert.AlertType.ERROR);
                    }
                } else {
                    showAlert("Server Error", "Failed to load users. Status: " + response.statusCode(), Alert.AlertType.ERROR);
                }
            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> showAlert("Connection Error", "Could not reach the server.\n" + ex.getMessage(), Alert.AlertType.ERROR));
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
                masterUserData.removeAll(selectedUsers);
                showAlert("Success", "Users deleted successfully.", Alert.AlertType.INFORMATION);
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
            Scene scene = new Scene(root);
            Stage stage = (Stage) sourceNode.getScene().getWindow();
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to load screen: " + fxmlPath);
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