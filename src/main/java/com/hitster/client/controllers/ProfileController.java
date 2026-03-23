package com.hitster.client.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

public class ProfileController {

    @FXML
    private Button backButton;

    @FXML
    private TableColumn<?, ?> dateCol;

    @FXML
    private Label emailLabel;

    @FXML
    private TableView<?> matchHistoryTable;

    @FXML
    private TableColumn<?, ?> opponentCol;

    @FXML
    private ImageView profileImageView;

    @FXML
    private TableColumn<?, ?> resultCol;

    @FXML
    private Label totalWinsLabel;

    @FXML
    private Label usernameLabel;

    @FXML
    private Label winRateLabel;

    @FXML
    void handleBack(ActionEvent event) {

    }

    @FXML
    void handleDeleteAccount(ActionEvent event) {

    }

    @FXML
    void handleEditEmail(ActionEvent event) {

    }

    @FXML
    void handleEditUsername(ActionEvent event) {

    }

    @FXML
    void handleUploadImage(MouseEvent event) {

    }

}
