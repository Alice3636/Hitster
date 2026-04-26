package com.hitster.controllers;

import com.google.gson.Gson;
import com.hitster.network.AdminNetworkService;
import com.hitster.dto.admin.SongsResponseDTO;
import com.hitster.dto.game.SongDTO;
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

public class AdminEditSongsController {

    @FXML
    private Button navSongsButton;
    @FXML
    private Button navUsersButton;
    @FXML
    private TextField searchField;
    @FXML
    private Button addSongButton;
    @FXML
    private Button deleteSelectedButton;
    @FXML
    private TableView<SongRow> songsTable;
    @FXML
    private TableColumn<SongRow, Boolean> selectColumn;
    @FXML
    private TableColumn<SongRow, Long> idColumn;
    @FXML
    private TableColumn<SongRow, String> titleColumn;
    @FXML
    private TableColumn<SongRow, String> artistColumn;
    @FXML
    private TableColumn<SongRow, Integer> yearColumn;
    @FXML
    private TableColumn<SongRow, String> linkColumn;
    @FXML
    private TableColumn<SongRow, Void> editColumn;
    @FXML
    private Button backButton;

    private final ObservableList<SongRow> masterSongData = FXCollections.observableArrayList();
    private FilteredList<SongRow> filteredData;

    // 1. Added the Network Service!
    private final AdminNetworkService adminService = new AdminNetworkService();

    @FXML 
    private AnchorPane rootPane;

    public void initialize() {
        ResponsiveScaler.bindToWidth(rootPane);
        // Setup Standard Columns
        idColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());        titleColumn.setCellValueFactory(cellData -> cellData.getValue().titleProperty());
        artistColumn.setCellValueFactory(cellData -> cellData.getValue().artistProperty());
        yearColumn.setCellValueFactory(cellData -> cellData.getValue().yearProperty().asObject());
        linkColumn.setCellValueFactory(cellData -> cellData.getValue().linkProperty());

        // Setup Checkbox Column
        selectColumn.setCellValueFactory(cellData -> cellData.getValue().selectedProperty());
        selectColumn.setCellFactory(CheckBoxTableCell.forTableColumn(selectColumn));
        songsTable.setEditable(true);

        // Setup Custom "Edit" Button Column
        setupEditColumn();

        // Setup Search Filter
        filteredData = new FilteredList<>(masterSongData, p -> true);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(song -> {
                if (newValue == null || newValue.isEmpty())
                    return true;
                String lowerCaseFilter = newValue.toLowerCase();
                return song.getTitle().toLowerCase().contains(lowerCaseFilter) ||
                        song.getArtist().toLowerCase().contains(lowerCaseFilter);
            });
        });

        SortedList<SongRow> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(songsTable.comparatorProperty());
        songsTable.setItems(sortedData);

        // Button Actions & Navigation
        navUsersButton.setOnAction(e -> SceneNavigator.loadScene(SceneNavigator.ADMIN_EDIT_USERS_SCREEN));
        addSongButton.setOnAction(this::handleAddSong);
        deleteSelectedButton.setOnAction(this::handleDeleteSelected);

        // Load Initial Data from DB
        loadSongData();
    }

    private void setupEditColumn() {
        editColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");

            {
                editBtn.setOnAction(event -> {
                    SongRow song = getTableView().getItems().get(getIndex());
                    handleEditSong(song);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(editBtn);
                }
            }
        });
    }

    // 2. Updated to fetch data from the Spring Boot Backend
    private void loadSongData() {
        adminService.getAllSongs().thenAccept(response -> {
            Platform.runLater(() -> {
                if (response.statusCode() == 200) {
                    try {
                        Gson gson = new Gson();

                        SongsResponseDTO songsResponse = gson.fromJson(response.body(), SongsResponseDTO.class);
                        masterSongData.clear();

                        for (SongDTO song : songsResponse.songs()) {
                            masterSongData.add(
                                    new SongRow(
                                            song.songId(),
                                            song.title(),
                                            song.artist(),
                                            song.releaseYear(),
                                            song.audioUrl()
                                    )
                            );
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        showAlert("Parse Error", "Failed to read song data from server.", Alert.AlertType.ERROR);
                    }
                } else {
                    showAlert("Server Error", "Failed to load songs. Status: " + response.statusCode(),
                            Alert.AlertType.ERROR);
                }
            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> showAlert("Connection Error", "Could not reach the server.\n" + ex.getMessage(),
                    Alert.AlertType.ERROR));
            return null;
        });
    }

    private void handleAddSong(ActionEvent event) {
        // TODO: Open an "Add Song" pop-up dialog or navigate to a new screen
        showAlert("Add Song", "This will open the song creation form.", Alert.AlertType.INFORMATION);
    }

    private void handleEditSong(SongRow song) {
        // TODO: Open an "Edit Song" pop-up pre-filled with this song's data
        showAlert("Edit Song", "Editing: " + song.getTitle(), Alert.AlertType.INFORMATION);
    }

    // 3. Updated to send Delete requests to the Backend
    private void handleDeleteSelected(ActionEvent event) {
        List<SongRow> selectedSongs = masterSongData.stream()
                .filter(SongRow::isSelected)
                .collect(Collectors.toList());

        if (selectedSongs.isEmpty()) {
            showAlert("No Selection", "Please select at least one song to delete.", Alert.AlertType.WARNING);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Are you sure you want to delete " + selectedSongs.size() + " song(s)?",
                ButtonType.YES, ButtonType.NO);

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                // Extract just the IDs from the selected rows
                // Change the list type to Long
                List<Long> selectedIds = selectedSongs.stream()
                        .map(SongRow::getId)
                        .collect(Collectors.toList());

                // Send to backend
                adminService.deleteSongs(selectedIds).thenAccept(apiResponse -> {
                    Platform.runLater(() -> {
                        if (apiResponse.statusCode() == 200) {
                            // If DB deletion was successful, remove from UI table
                            masterSongData.removeAll(selectedSongs);
                            showAlert("Success", "Songs deleted successfully.", Alert.AlertType.INFORMATION);
                        } else {
                            showAlert("Error", "Failed to delete songs: " + apiResponse.body(), Alert.AlertType.ERROR);
                        }
                    });
                }).exceptionally(ex -> {
                    Platform.runLater(() -> showAlert("Connection Error",
                            "Could not reach the server.\n" + ex.getMessage(), Alert.AlertType.ERROR));
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

    // --- INNER CLASS FOR TABLE DATA ---
    public static class SongRow {
        private final BooleanProperty selected;
        private final LongProperty id; // Changed from IntegerProperty
        private final StringProperty title;
        private final StringProperty artist;
        private final IntegerProperty year;
        private final StringProperty link;

        public SongRow(long id, String title, String artist, int year, String link) {
            this.selected = new SimpleBooleanProperty(false);
            this.id = new SimpleLongProperty(id);
            this.title = new SimpleStringProperty(title);
            this.artist = new SimpleStringProperty(artist);
            this.year = new SimpleIntegerProperty(year);
            this.link = new SimpleStringProperty(link);
        }

        public boolean isSelected() { return selected.get(); }
        public void setSelected(boolean value) { selected.set(value); }
        public BooleanProperty selectedProperty() { return selected; }

        public long getId() { return id.get(); }
        public LongProperty idProperty() { return id; }

        public String getTitle() { return title.get(); }
        public StringProperty titleProperty() { return title; }

        public String getArtist() { return artist.get(); }
        public StringProperty artistProperty() { return artist; }

        public int getYear() { return year.get(); }
        public IntegerProperty yearProperty() { return year; }

        public String getLink() { return link.get(); }
        public StringProperty linkProperty() { return link; }
    }
}