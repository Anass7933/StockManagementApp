package com.stockapp.controllers;
import com.stockapp.models.User;
import com.stockapp.services.AuthService;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class AdminController {

    @FXML
    private Button addButton;

    @FXML
    private Button modifyButton;

    @FXML
    private Button deleteButton;

    @FXML
    private TableView<User> usersTable;

    @FXML
    private TableColumn<User, Integer> idColumn;

    @FXML
    private TableColumn<User, String> userNameColumn;

    @FXML
    private TableColumn<User, String> fullNameColumn;

    @FXML
    private TableColumn<User, String> roleColumn;

    @FXML
    private TableColumn<User, String> createdAtColumn;

    @FXML
    private Button sighOutButton;

    @FXML
    private Button productsButton;

    private final Timeline refreshTimeline = new Timeline(
    new KeyFrame(Duration.ZERO, e -> refreshUsers()),
    new KeyFrame(Duration.seconds(2), e -> refreshUsers())
    );


    @FXML
    private void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));
        userNameColumn.setCellValueFactory(new PropertyValueFactory<>("userName"));
        fullNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        createdAtColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));


        refreshUsers();

        addButton.setOnAction(e -> openUserForm(null));

        modifyButton.setOnAction(e -> {
            User selected = usersTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert("Select a user first.");
                return;
            }
            openUserForm(String.valueOf(selected.getUserId()));
        });

        deleteButton.setOnAction(e -> {
            User selected = usersTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert("Select a user to delete.");
                return;
            }
            AuthService.deleteUser(selected.getUserId());
            refreshUsers();
        });

        sighOutButton.setOnAction(e -> signOut());

        productsButton.setOnAction(e -> {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/StockManagerDashboard.fxml"));
            Scene scene = null;
            try {
                scene = new Scene(loader.load());
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            Stage stage = (Stage) productsButton.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        });

        refreshUsers();

        refreshTimeline.setCycleCount(Animation.INDEFINITE);
        refreshTimeline.play();
}


    private void openUserForm(String usernameToEdit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/UserForm.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("User Form");
            stage.setResizable(false);
            stage.setScene(new Scene(root));

            UserFormController controller = loader.getController();
            controller.setStage(stage);

            if (usernameToEdit != null) {
                controller.loadUserData(Long.parseLong(usernameToEdit));
            }

            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void signOut() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            Parent root = loader.load();

            Stage currentStage = (Stage) sighOutButton.getScene().getWindow();
            currentStage.setScene(new Scene(root));
            currentStage.setTitle("Login");
            currentStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void refreshUsers() {
        User selected = usersTable.getSelectionModel().getSelectedItem();
        try {
            List<User> users = AuthService.loadUsers();
            ObservableList<User> data = FXCollections.observableArrayList(users);
            usersTable.setItems(data);

            if (selected != null) {
                data.stream()
                        .filter(u -> u.getUserId() == selected.getUserId())
                        .findFirst()
                        .ifPresent(u -> usersTable.getSelectionModel().select(u));
            }

            // resize after row count changes
            javafx.application.Platform.runLater(() -> autoResizeTable());

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void autoResizeTable() {
        double headerHeight = 30;  // fallback

        var header = usersTable.lookup(".column-header-background");
        if (header != null) {
            headerHeight = header.prefHeight(-1);
        }

        int rows = usersTable.getItems().size();
        double rowHeight = usersTable.getFixedCellSize();

        double totalHeight = headerHeight + rows * rowHeight;

        double maxHeight = 500;

        usersTable.setPrefHeight(Math.min(totalHeight, maxHeight));
    }



}


