package com.hitster.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hitster.network.AdminNetworkService;

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

public class AdminEditSongsController {

    @FXML private Button navSongsButton;
    @FXML private Button navUsersButton;
    @FXML private TextField searchField;
    @FXML private Button addSongButton;
    @FXML private Button deleteSelectedButton;
    @FXML private TableView<SongRow> songsTable;
    @FXML private TableColumn<SongRow, Boolean> selectColumn;
    @FXML private TableColumn<SongRow, Integer> idColumn;
    @FXML private TableColumn<SongRow, String> titleColumn;
    @FXML private TableColumn<SongRow, String> artistColumn;
    @FXML private TableColumn<SongRow, Integer> yearColumn;
    @FXML private TableColumn<SongRow, String> linkColumn;
    @FXML private TableColumn<SongRow, Void> editColumn;
    @FXML private Button backButton;

    private final ObservableList<SongRow> masterSongData = FXCollections.observableArrayList();
    private FilteredList<SongRow> filteredData;

    private final AdminNetworkService adminService = new AdminNetworkService();

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());
        titleColumn.setCellValueFactory(cellData -> cellData.getValue().titleProperty());
        artistColumn.setCellValueFactory(cellData -> cellData.getValue().artistProperty());
        yearColumn.setCellValueFactory(cellData -> cellData.getValue().yearProperty().asObject());
        linkColumn.setCellValueFactory(cellData -> cellData.getValue().linkProperty());

        selectColumn.setCellValueFactory(cellData -> cellData.getValue().selectedProperty());
        selectColumn.setCellFactory(CheckBoxTableCell.forTableColumn(selectColumn));
        songsTable.setEditable(true);

        setupEditColumn();

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

        navUsersButton.setOnAction(e -> navigateToNode((Node) e.getSource(), "/views/AdminEditAccounts.fxml"));
        addSongButton.setOnAction(this::handleAddSong);
        deleteSelectedButton.setOnAction(this::handleDeleteSelected);

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

    private void loadSongData() {
        adminService.getAllSongs().thenAccept(response -> {
            Platform.runLater(() -> {
                if (response.statusCode() == 200) {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        List<Map<String, Object>> songs = mapper.readValue(
                                response.body(),
                                new TypeReference<List<Map<String, Object>>>() {
                                });

                        masterSongData.clear();

                        for (Map<String, Object> s : songs) {
                            int id = ((Number) s.get("id")).intValue();
                            String title = (String) s.get("title");
                            String artist = (String) s.get("artist");
                            int year = ((Number) s.get("year")).intValue();
                            String link = (String) s.get("link");

                            masterSongData.add(new SongRow(id, title, artist, year, link));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        showAlert("Parse Error", "Failed to read song data from server.", Alert.AlertType.ERROR);
                    }
                } else {
                    showAlert("Server Error", "Failed to load songs. Status: " + response.statusCode(), Alert.AlertType.ERROR);
                }
            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> showAlert("Connection Error", "Could not reach the server.\n" + ex.getMessage(), Alert.AlertType.ERROR));
            return null;
        });
    }

    private void handleAddSong(ActionEvent event) {
        showAlert("Add Song", "This will open the song creation form.", Alert.AlertType.INFORMATION);
    }

    private void handleEditSong(SongRow song) {
        showAlert("Edit Song", "Editing: " + song.getTitle(), Alert.AlertType.INFORMATION);
    }

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
                List<Integer> selectedIds = selectedSongs.stream()
                        .map(SongRow::getId)
                        .collect(Collectors.toList());

                adminService.deleteSongs(selectedIds).thenAccept(apiResponse -> {
                    Platform.runLater(() -> {
                        if (apiResponse.statusCode() == 200) {
                            masterSongData.removeAll(selectedSongs);
                            showAlert("Success", "Songs deleted successfully.", Alert.AlertType.INFORMATION);
                        } else {
                            showAlert("Error", "Failed to delete songs: " + apiResponse.body(), Alert.AlertType.ERROR);
                        }
                    });
                }).exceptionally(ex -> {
                    Platform.runLater(() -> showAlert("Connection Error", "Could not reach the server.\n" + ex.getMessage(), Alert.AlertType.ERROR));
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

    public static class SongRow {
        private final BooleanProperty selected;
        private final IntegerProperty id;
        private final StringProperty title;
        private final StringProperty artist;
        private final IntegerProperty year;
        private final StringProperty link;

        public SongRow(int id, String title, String artist, int year, String link) {
            this.selected = new SimpleBooleanProperty(false);
            this.id = new SimpleIntegerProperty(id);
            this.title = new SimpleStringProperty(title);
            this.artist = new SimpleStringProperty(artist);
            this.year = new SimpleIntegerProperty(year);
            this.link = new SimpleStringProperty(link);
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

        public String getTitle() {
            return title.get();
        }

        public StringProperty titleProperty() {
            return title;
        }

        public String getArtist() {
            return artist.get();
        }

        public StringProperty artistProperty() {
            return artist;
        }

        public int getYear() {
            return year.get();
        }

        public IntegerProperty yearProperty() {
            return year;
        }

        public String getLink() {
            return link.get();
        }

        public StringProperty linkProperty() {
            return link;
        }
    }
}