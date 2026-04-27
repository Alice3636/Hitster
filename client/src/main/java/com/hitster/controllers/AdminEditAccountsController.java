package com.hitster.controllers;

import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.AnchorPane;

import com.hitster.client.utils.ResponsiveScaler;
import com.hitster.client.utils.SceneNavigator;

import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.hitster.dto.admin.UserEntryDTO;
import com.hitster.dto.admin.UsersResponseDTO;
import com.hitster.network.AdminNetworkService;

/**
 * Controls the administrator account management screen for viewing, searching, and deleting users.
 */
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

    // Keep the unfiltered backing list separate so search and sorting can be recomputed locally.
    private final ObservableList<UserRow> masterUserData = FXCollections.observableArrayList();
    private FilteredList<UserRow> filteredData;

    @FXML
    private final AdminNetworkService adminService = new AdminNetworkService();

    @FXML 
    private AnchorPane rootPane;

    /**
     * Initializes account table bindings, search filtering, navigation, and initial data loading.
     */
    public void initialize() {
        ResponsiveScaler.bindToWidth(rootPane);

        // Table columns bind to row properties so JavaFX sorting and filtering stay live.
        idColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());
        usernameColumn.setCellValueFactory(cellData -> cellData.getValue().usernameProperty());
        emailColumn.setCellValueFactory(cellData -> cellData.getValue().emailProperty());

        // Checkbox edits update row selection state used by the bulk delete action.
        selectColumn.setCellValueFactory(cellData -> cellData.getValue().selectedProperty());
        selectColumn.setCellFactory(CheckBoxTableCell.forTableColumn(selectColumn));

        // Table editing is required for CheckBoxTableCell to commit selection changes.
        usersTable.setEditable(true);

        // Filter against the backing list so the search field never mutates the source data.
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

        // SortedList lets TableView sorting compose with the active search predicate.
        SortedList<UserRow> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(usersTable.comparatorProperty());
        usersTable.setItems(sortedData);

        // Navigation is wired here because this controller owns admin-mode transitions.
        navSongsButton.setOnAction(e -> SceneNavigator.loadScene(SceneNavigator.ADMIN_EDIT_SONGS_SCREEN));

        // Bulk deletion operates on the row selection state maintained by the checkbox column.
        deleteSelectedButton.setOnAction(this::handleDeleteSelected);

        // Load initial data after table bindings are ready to receive rows.
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
                                            (int) user.id(), // Table rows store ids as int while the API exposes Long ids.
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
                        .map(user -> (long) user.getId()) // Convert UI row ids to the Long ids expected by the API.
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
        SceneNavigator.loadScene(SceneNavigator.LOBBY_SCREEN);
    }

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Table row model used by the administrator user-management table.
     */
    public static class UserRow {
        private final BooleanProperty selected;
        private final IntegerProperty id;
        private final StringProperty username;
        private final StringProperty email;

        /**
         * Creates a row model for one user account.
         *
         * @param id user id displayed in the table
         * @param username username displayed in the table
         * @param email email address displayed in the table
         */
        public UserRow(int id, String username, String email) {
            this.selected = new SimpleBooleanProperty(false);
            this.id = new SimpleIntegerProperty(id);
            this.username = new SimpleStringProperty(username);
            this.email = new SimpleStringProperty(email);
        }

        /**
         * Indicates whether this row is selected for bulk deletion.
         *
         * @return {@code true} when the row is selected
         */
        public boolean isSelected() {
            return selected.get();
        }

        /**
         * Updates whether this row is selected for bulk deletion.
         *
         * @param value selected state to apply
         */
        public void setSelected(boolean value) {
            selected.set(value);
        }

        /**
         * Returns the selected property used by the table checkbox column.
         *
         * @return selected state property
         */
        public BooleanProperty selectedProperty() {
            return selected;
        }

        /**
         * Returns the user id displayed in the table.
         *
         * @return user id
         */
        public int getId() {
            return id.get();
        }

        /**
         * Returns the user id property used by the table binding.
         *
         * @return user id property
         */
        public IntegerProperty idProperty() {
            return id;
        }

        /**
         * Returns the username displayed in the table.
         *
         * @return username
         */
        public String getUsername() {
            return username.get();
        }

        /**
         * Returns the username property used by the table binding.
         *
         * @return username property
         */
        public StringProperty usernameProperty() {
            return username;
        }

        /**
         * Returns the email address displayed in the table.
         *
         * @return email address
         */
        public String getEmail() {
            return email.get();
        }

        /**
         * Returns the email property used by the table binding.
         *
         * @return email property
         */
        public StringProperty emailProperty() {
            return email;
        }
    }
}
