package com.hitster.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class AdminEditSongsController {

    @FXML
    private Button addSongButton;

    @FXML
    private TableColumn<?, ?> artistColumn;

    @FXML
    private Button backButton;

    @FXML
    private Button deleteSelectedButton;

    @FXML
    private TableColumn<?, ?> editColumn;

    @FXML
    private TableColumn<?, ?> idColumn;

    @FXML
    private TableColumn<?, ?> linkColumn;

    @FXML
    private Button navSongsButton;

    @FXML
    private Button navUsersButton;

    @FXML
    private TextField searchField;

    @FXML
    private TableColumn<?, ?> selectColumn;

    @FXML
    private TableView<?> songsTable;

    @FXML
    private TableColumn<?, ?> titleColumn;

    @FXML
    private TableColumn<?, ?> yearColumn;

    @FXML
    void handleBack(ActionEvent event) {

    }

}