package com.stockapp.controllers;

import com.stockapp.models.User;
import com.stockapp.models.UserRole;
import com.stockapp.services.AuthService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class UserFormController {

    @FXML private TextField usernameField;
    @FXML private TextField fullNameField;
    @FXML private TextField passwordField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;


    private Stage stage;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void initialize() {
        saveButton.setOnAction(e -> {
            String fullName = fullNameField.getText();
            String username = usernameField.getText();
            String password = passwordField.getText();
            UserRole role = UserRole.valueOf(roleComboBox.getValue());

            stage.close();
        });
    }

    // Pre-fill fields for modify
    public void loadUserData(String username) {
        // Here you would fetch data from your table or database
        usernameField.setText(username);
        fullNameField.setText("John Doe");
        roleComboBox.setValue("USER");
    }


    public void cancelButtonOnAction(ActionEvent actionEvent) {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}
