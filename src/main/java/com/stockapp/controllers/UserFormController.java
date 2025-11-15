package com.stockapp.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class UserFormController {

    @FXML private TextField usernameField;
    @FXML private TextField fullNameField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private Button saveButton;

    private Stage stage;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void initialize() {
        saveButton.setOnAction(e -> {
            String username = usernameField.getText();
            String fullName = fullNameField.getText();
            String role = roleComboBox.getValue();

            System.out.println("Saving: " + username + ", " + fullName + ", " + role);
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


}
