package com.hitster.controllers;

import com.google.gson.Gson;
import com.hitster.dto.admin.SongsResponseDTO;
import com.hitster.dto.game.SongDTO;
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
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
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
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();

                return song.getTitle().toLowerCase().contains(lowerCaseFilter)
                        || song.getArtist().toLowerCase().contains(lowerCaseFilter)
                        || String.valueOf(song.getYear()).contains(lowerCaseFilter)
                        || String.valueOf(song.getId()).contains(lowerCaseFilter);
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
                setGraphic(empty ? null : editBtn);
            }
        });
    }

    private void loadSongData() {
        adminService.getAllSongs().thenAccept(response -> {
            Platform.runLater(() -> {
                if (response.statusCode() == 200) {
                    try {
                        Gson gson = new Gson();
                        SongsResponseDTO songsResponse = gson.fromJson(response.body(), SongsResponseDTO.class);

                        masterSongData.clear();

                        for (SongDTO song : songsResponse.songs()) {
                            masterSongData.add(new SongRow(
                                    song.songId(),
                                    song.title(),
                                    song.artist(),
                                    song.releaseYear(),
                                    song.audioUrl()
                            ));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        showAlert("Parse Error", "Failed to read song data from server.", Alert.AlertType.ERROR);
                    }
                } else {
                    showAlert(
                            "Server Error",
                            "Failed to load songs.\nStatus: " + response.statusCode() + "\nBody: " + response.body(),
                            Alert.AlertType.ERROR
                    );
                }
            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> showAlert(
                    "Connection Error",
                    "Could not reach the server.\n" + ex.getMessage(),
                    Alert.AlertType.ERROR
            ));
            return null;
        });
    }

    private void handleAddSong(ActionEvent event) {
        Dialog<AddSongFormResult> dialog = new Dialog<>();
        dialog.setTitle("Add Song");
        dialog.setHeaderText("Create a new song");

        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        TextField titleField = new TextField();
        titleField.setPromptText("Title");

        TextField artistField = new TextField();
        artistField.setPromptText("Artist");

        TextField yearField = new TextField();
        yearField.setPromptText("Release year");

        TextField filePathField = new TextField();
        filePathField.setEditable(false);
        filePathField.setPromptText("Choose audio file");

        Button chooseFileButton = new Button("Choose File");

        final Path[] selectedFilePath = new Path[1];

        chooseFileButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Choose audio file");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Audio files", "*.mp3", "*.wav", "*.ogg"),
                    new FileChooser.ExtensionFilter("MP3 files", "*.mp3"),
                    new FileChooser.ExtensionFilter("WAV files", "*.wav"),
                    new FileChooser.ExtensionFilter("OGG files", "*.ogg")
            );

            Stage stage = (Stage) songsTable.getScene().getWindow();
            File selectedFile = fileChooser.showOpenDialog(stage);

            if (selectedFile != null) {
                selectedFilePath[0] = selectedFile.toPath();
                filePathField.setText(selectedFile.getAbsolutePath());
            }
        });

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleField, 1, 0);

        grid.add(new Label("Artist:"), 0, 1);
        grid.add(artistField, 1, 1);

        grid.add(new Label("Release year:"), 0, 2);
        grid.add(yearField, 1, 2);

        grid.add(new Label("Audio file:"), 0, 3);
        grid.add(filePathField, 1, 3);
        grid.add(chooseFileButton, 2, 3);

        dialog.getDialogPane().setContent(grid);

        Node addButton = dialog.getDialogPane().lookupButton(addButtonType);
        addButton.disableProperty().bind(
                titleField.textProperty().isEmpty()
                        .or(artistField.textProperty().isEmpty())
                        .or(yearField.textProperty().isEmpty())
                        .or(filePathField.textProperty().isEmpty())
        );

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                return new AddSongFormResult(
                        titleField.getText(),
                        artistField.getText(),
                        yearField.getText(),
                        selectedFilePath[0]
                );
            }
            return null;
        });

        Optional<AddSongFormResult> result = dialog.showAndWait();

        result.ifPresent(form -> {
            Integer releaseYear = parseYearOrShowError(form.releaseYearText());
            if (releaseYear == null) {
                return;
            }

            adminService.addSong(
                    form.title().trim(),
                    form.artist().trim(),
                    releaseYear,
                    form.audioFile()
            ).thenAccept(response -> {
                Platform.runLater(() -> {
                    if (response.statusCode() == 200 || response.statusCode() == 201) {
                        showAlert("Success", "Song added successfully.", Alert.AlertType.INFORMATION);
                        loadSongData();
                    } else {
                        showAlert(
                                "Add Song Failed",
                                "Status: " + response.statusCode() + "\nBody: " + response.body(),
                                Alert.AlertType.ERROR
                        );
                    }
                });
            }).exceptionally(ex -> {
                Platform.runLater(() -> showAlert(
                        "Connection Error",
                        "Could not add song.\n" + ex.getMessage(),
                        Alert.AlertType.ERROR
                ));
                return null;
            });
        });
    }

    private void handleEditSong(SongRow song) {
        if (song == null) {
            showAlert("No Song", "No song selected.", Alert.AlertType.WARNING);
            return;
        }

        Dialog<EditSongFormResult> dialog = new Dialog<>();
        dialog.setTitle("Edit Song");
        dialog.setHeaderText("Edit song ID: " + song.getId());

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        TextField titleField = new TextField(song.getTitle());
        TextField artistField = new TextField(song.getArtist());
        TextField yearField = new TextField(String.valueOf(song.getYear()));
        TextField audioUrlField = new TextField(song.getLink());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleField, 1, 0);

        grid.add(new Label("Artist:"), 0, 1);
        grid.add(artistField, 1, 1);

        grid.add(new Label("Release year:"), 0, 2);
        grid.add(yearField, 1, 2);

        grid.add(new Label("Audio URL:"), 0, 3);
        grid.add(audioUrlField, 1, 3);

        dialog.getDialogPane().setContent(grid);

        Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.disableProperty().bind(
                titleField.textProperty().isEmpty()
                        .or(artistField.textProperty().isEmpty())
                        .or(yearField.textProperty().isEmpty())
                        .or(audioUrlField.textProperty().isEmpty())
        );

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                return new EditSongFormResult(
                        titleField.getText(),
                        artistField.getText(),
                        yearField.getText(),
                        audioUrlField.getText()
                );
            }
            return null;
        });

        Optional<EditSongFormResult> result = dialog.showAndWait();

        result.ifPresent(form -> {
            Integer releaseYear = parseYearOrShowError(form.releaseYearText());
            if (releaseYear == null) {
                return;
            }

            adminService.updateSong(
                    song.getId(),
                    form.title().trim(),
                    form.artist().trim(),
                    releaseYear,
                    form.audioUrl().trim()
            ).thenAccept(response -> {
                Platform.runLater(() -> {
                    if (response.statusCode() == 200) {
                        showAlert("Success", "Song updated successfully.", Alert.AlertType.INFORMATION);
                        loadSongData();
                    } else {
                        showAlert(
                                "Update Failed",
                                "Status: " + response.statusCode() + "\nBody: " + response.body(),
                                Alert.AlertType.ERROR
                        );
                    }
                });
            }).exceptionally(ex -> {
                Platform.runLater(() -> showAlert(
                        "Connection Error",
                        "Could not update song.\n" + ex.getMessage(),
                        Alert.AlertType.ERROR
                ));
                return null;
            });
        });
    }

    private Integer parseYearOrShowError(String value) {
        try {
            int year = Integer.parseInt(value.trim());

            if (year <= 0) {
                showAlert("Invalid Year", "Release year must be positive.", Alert.AlertType.ERROR);
                return null;
            }

            return year;
        } catch (NumberFormatException e) {
            showAlert("Invalid Year", "Release year must be a number.", Alert.AlertType.ERROR);
            return null;
        }
    }

    private void handleDeleteSelected(ActionEvent event) {
        List<SongRow> selectedSongs = masterSongData.stream()
                .filter(SongRow::isSelected)
                .collect(Collectors.toList());

        if (selectedSongs.isEmpty()) {
            showAlert("No Selection", "Please select at least one song to delete.", Alert.AlertType.WARNING);
            return;
        }

        Alert confirm = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Are you sure you want to delete " + selectedSongs.size() + " song(s)?",
                ButtonType.YES,
                ButtonType.NO
        );

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                List<Long> selectedIds = selectedSongs.stream()
                        .map(SongRow::getId)
                        .collect(Collectors.toList());

                adminService.deleteSongs(selectedIds).thenAccept(apiResponse -> {
                    Platform.runLater(() -> {
                        if (apiResponse.statusCode() == 200) {
                            masterSongData.removeAll(selectedSongs);
                            showAlert("Success", "Songs deleted successfully.", Alert.AlertType.INFORMATION);
                        } else {
                            showAlert(
                                    "Error",
                                    "Failed to delete songs.\nStatus: " + apiResponse.statusCode()
                                            + "\nBody: " + apiResponse.body(),
                                    Alert.AlertType.ERROR
                            );
                        }
                    });
                }).exceptionally(ex -> {
                    Platform.runLater(() -> showAlert(
                            "Connection Error",
                            "Could not reach the server.\n" + ex.getMessage(),
                            Alert.AlertType.ERROR
                    ));
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
            showAlert("Navigation Error", "Error loading: " + fxmlPath, Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private record AddSongFormResult(
            String title,
            String artist,
            String releaseYearText,
            Path audioFile
    ) {
    }

    private record EditSongFormResult(
            String title,
            String artist,
            String releaseYearText,
            String audioUrl
    ) {
    }

    public static class SongRow {
        private final BooleanProperty selected;
        private final LongProperty id;
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

        public boolean isSelected() {
            return selected.get();
        }

        public void setSelected(boolean value) {
            selected.set(value);
        }

        public BooleanProperty selectedProperty() {
            return selected;
        }

        public long getId() {
            return id.get();
        }

        public LongProperty idProperty() {
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