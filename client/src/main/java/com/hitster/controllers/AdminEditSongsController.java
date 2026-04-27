package com.hitster.controllers;

import com.google.gson.Gson;
import com.hitster.client.utils.ResponsiveScaler;
import com.hitster.client.utils.SceneNavigator;
import com.hitster.dto.admin.AddSongRequestDTO;
import com.hitster.dto.admin.SongsResponseDTO;
import com.hitster.dto.admin.UpdateSongRequestDTO;
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
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AdminEditSongsController {

    @FXML private Button navSongsButton;
    @FXML private Button navUsersButton;
    @FXML private TextField searchField;
    @FXML private Button addSongButton;
    @FXML private Button deleteSelectedButton;
    @FXML private TableView<SongRow> songsTable;
    @FXML private TableColumn<SongRow, Boolean> selectColumn;
    @FXML private TableColumn<SongRow, Long> idColumn;
    @FXML private TableColumn<SongRow, String> titleColumn;
    @FXML private TableColumn<SongRow, String> artistColumn;
    @FXML private TableColumn<SongRow, Integer> yearColumn;
    @FXML private TableColumn<SongRow, String> linkColumn;
    @FXML private TableColumn<SongRow, Void> editColumn;
    @FXML private Button backButton;
    @FXML private AnchorPane rootPane;

    private final ObservableList<SongRow> masterSongData = FXCollections.observableArrayList();
    private final AdminNetworkService adminService = new AdminNetworkService();
    private FilteredList<SongRow> filteredData;

    @FXML
    public void initialize() {
        ResponsiveScaler.bindToWidth(rootPane);
        setupTableColumns();
        setupSearchFilter();
        setupButtons();
        loadSongData();
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());
        titleColumn.setCellValueFactory(cellData -> cellData.getValue().titleProperty());
        artistColumn.setCellValueFactory(cellData -> cellData.getValue().artistProperty());
        yearColumn.setCellValueFactory(cellData -> cellData.getValue().yearProperty().asObject());
        linkColumn.setCellValueFactory(cellData -> cellData.getValue().linkProperty());

        selectColumn.setCellValueFactory(cellData -> cellData.getValue().selectedProperty());
        selectColumn.setCellFactory(CheckBoxTableCell.forTableColumn(selectColumn));
        songsTable.setEditable(true);

        setupEditColumn();
    }

    private void setupEditColumn() {
        editColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button("Edit");

            {
                editButton.setOnAction(event -> {
                    SongRow song = getTableView().getItems().get(getIndex());
                    handleEditSong(song);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : editButton);
            }
        });
    }

    private void setupSearchFilter() {
        filteredData = new FilteredList<>(masterSongData, song -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) ->
                filteredData.setPredicate(song -> matchesSearch(song, newValue))
        );

        SortedList<SongRow> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(songsTable.comparatorProperty());
        songsTable.setItems(sortedData);
    }

    private boolean matchesSearch(SongRow song, String searchText) {
        if (searchText == null || searchText.isBlank()) return true;

        String filter = searchText.toLowerCase();

        return song.getTitle().toLowerCase().contains(filter)
                || song.getArtist().toLowerCase().contains(filter)
                || String.valueOf(song.getYear()).contains(filter)
                || String.valueOf(song.getId()).contains(filter);
    }

    private void setupButtons() {
        navUsersButton.setOnAction(e -> SceneNavigator.loadScene(SceneNavigator.ADMIN_EDIT_USERS_SCREEN));
        addSongButton.setOnAction(this::handleAddSong);
        deleteSelectedButton.setOnAction(this::handleDeleteSelected);
    }

    private void loadSongData() {
        adminService.getAllSongs().thenAccept(response -> {
            Platform.runLater(() -> {
                if (response.statusCode() == 200) {
                    populateSongTable(response.body());
                } else {
                    showAlert("Server Error",
                            "Failed to load songs.\nStatus: " + response.statusCode() + "\nBody: " + response.body(),
                            Alert.AlertType.ERROR);
                }
            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> showAlert("Connection Error",
                    "Could not reach the server.\n" + ex.getMessage(),
                    Alert.AlertType.ERROR));
            return null;
        });
    }

    private void populateSongTable(String responseBody) {
        try {
            SongsResponseDTO songsResponse = new Gson().fromJson(responseBody, SongsResponseDTO.class);
            masterSongData.clear();

            if (songsResponse == null || songsResponse.songs() == null) return;

            for (SongDTO song : songsResponse.songs()) {
                masterSongData.add(new SongRow(song.songId(), song.title(), song.artist(), song.releaseYear(), song.audioUrl()));
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Parse Error", "Failed to read song data from server.", Alert.AlertType.ERROR);
        }
    }

    private void handleAddSong(ActionEvent event) {
        Optional<AddSongFormResult> result = showAddSongDialog();

        result.ifPresent(form -> {
            Integer releaseYear = parseYearOrShowError(form.releaseYearText());
            if (releaseYear == null) return;

            AddSongRequestDTO requestDTO = new AddSongRequestDTO(
                    form.title().trim(),
                    form.artist().trim(),
                    releaseYear,
                    form.audioFile()
            );

            adminService.addSong(requestDTO).thenAccept(response -> {
                Platform.runLater(() -> {
                    if (isSuccessfulResponse(response.statusCode())) {
                        showAlert("Success", "Song added successfully.", Alert.AlertType.INFORMATION);
                        loadSongData();
                    } else {
                        showAlert("Add Song Failed",
                                "Status: " + response.statusCode() + "\nBody: " + response.body(),
                                Alert.AlertType.ERROR);
                    }
                });
            }).exceptionally(ex -> {
                Platform.runLater(() -> showAlert("Connection Error",
                        "Could not add song.\n" + ex.getMessage(),
                        Alert.AlertType.ERROR));
                return null;
            });
        });
    }

    private Optional<AddSongFormResult> showAddSongDialog() {
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

        GridPane grid = createSongFormGrid(titleField, artistField, yearField, filePathField, chooseFileButton);
        dialog.getDialogPane().setContent(grid);

        Node addButton = dialog.getDialogPane().lookupButton(addButtonType);
        addButton.disableProperty().bind(
                titleField.textProperty().isEmpty()
                        .or(artistField.textProperty().isEmpty())
                        .or(yearField.textProperty().isEmpty())
                        .or(filePathField.textProperty().isEmpty())
        );

        dialog.setResultConverter(dialogButton -> dialogButton == addButtonType
                ? new AddSongFormResult(titleField.getText(), artistField.getText(), yearField.getText(), selectedFilePath[0])
                : null);

        return dialog.showAndWait();
    }

    private GridPane createSongFormGrid(TextField titleField, TextField artistField, TextField yearField, TextField audioField, Node audioChooserNode) {
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
        grid.add(new Label("Audio:"), 0, 3);
        grid.add(audioField, 1, 3);
        grid.add(audioChooserNode, 2, 3);

        return grid;
    }

    private void handleEditSong(SongRow song) {
        if (song == null) {
            showAlert("No Song", "No song selected.", Alert.AlertType.WARNING);
            return;
        }

        Optional<EditSongFormResult> result = showEditSongDialog(song);

        result.ifPresent(form -> {
            Integer releaseYear = parseYearOrShowError(form.releaseYearText());
            if (releaseYear == null) return;

            UpdateSongRequestDTO requestDTO = new UpdateSongRequestDTO(
                    form.title().trim(),
                    form.artist().trim(),
                    releaseYear,
                    form.audioUrl().trim()
            );

            adminService.updateSong(song.getId(), requestDTO).thenAccept(response -> {
                Platform.runLater(() -> {
                    if (isSuccessfulResponse(response.statusCode())) {
                        showAlert("Success", "Song updated successfully.", Alert.AlertType.INFORMATION);
                        loadSongData();
                    } else {
                        showAlert("Update Failed",
                                "Status: " + response.statusCode() + "\nBody: " + response.body(),
                                Alert.AlertType.ERROR);
                    }
                });
            }).exceptionally(ex -> {
                Platform.runLater(() -> showAlert("Connection Error",
                        "Could not update song.\n" + ex.getMessage(),
                        Alert.AlertType.ERROR));
                return null;
            });
        });
    }

    private Optional<EditSongFormResult> showEditSongDialog(SongRow song) {
        Dialog<EditSongFormResult> dialog = new Dialog<>();
        dialog.setTitle("Edit Song");
        dialog.setHeaderText("Edit song ID: " + song.getId());

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        TextField titleField = new TextField(song.getTitle());
        TextField artistField = new TextField(song.getArtist());
        TextField yearField = new TextField(String.valueOf(song.getYear()));
        TextField audioUrlField = new TextField(song.getLink());

        GridPane grid = createSongFormGrid(titleField, artistField, yearField, audioUrlField, new Label(""));
        dialog.getDialogPane().setContent(grid);

        Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.disableProperty().bind(
                titleField.textProperty().isEmpty()
                        .or(artistField.textProperty().isEmpty())
                        .or(yearField.textProperty().isEmpty())
                        .or(audioUrlField.textProperty().isEmpty())
        );

        dialog.setResultConverter(dialogButton -> dialogButton == saveButtonType
                ? new EditSongFormResult(titleField.getText(), artistField.getText(), yearField.getText(), audioUrlField.getText())
                : null);

        return dialog.showAndWait();
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

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Are you sure you want to delete " + selectedSongs.size() + " song(s)?",
                ButtonType.YES,
                ButtonType.NO);

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                deleteSelectedSongs(selectedSongs);
            }
        });
    }

    private void deleteSelectedSongs(List<SongRow> selectedSongs) {
        List<Long> selectedIds = selectedSongs.stream()
                .map(SongRow::getId)
                .collect(Collectors.toList());

        adminService.deleteSongs(selectedIds).thenAccept(response -> {
            Platform.runLater(() -> {
                if (isSuccessfulResponse(response.statusCode())) {
                    masterSongData.removeAll(selectedSongs);
                    showAlert("Success", "Songs deleted successfully.", Alert.AlertType.INFORMATION);
                } else {
                    showAlert("Error",
                            "Failed to delete songs.\nStatus: " + response.statusCode() + "\nBody: " + response.body(),
                            Alert.AlertType.ERROR);
                }
            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> showAlert("Connection Error",
                    "Could not reach the server.\n" + ex.getMessage(),
                    Alert.AlertType.ERROR));
            return null;
        });
    }

    @FXML
    void handleBack(ActionEvent event) {
        SceneNavigator.loadScene(SceneNavigator.LOBBY_SCREEN);
    }

    private boolean isSuccessfulResponse(int statusCode) {
        return statusCode >= 200 && statusCode < 300;
    }

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private record AddSongFormResult(String title, String artist, String releaseYearText, Path audioFile) {}

    private record EditSongFormResult(String title, String artist, String releaseYearText, String audioUrl) {}

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
