package com.stockapp.controllers;
import com.stockapp.models.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

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
    private void initialize() {

        addButton.setOnAction(e -> openUserForm(null));

        modifyButton.setOnAction(e -> {
            User selected = usersTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert("Select a user first.");
                return;
            }
            openUserForm(selected.getUserName());
        });

        deleteButton.setOnAction(e -> {
            User selected = usersTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert("Select a user to delete.");
                return;
            }
            usersTable.getItems().remove(selected);
        });

        sighOutButton.setOnAction(e -> signOut());


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
                controller.loadUserData(usernameToEdit);
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





}
