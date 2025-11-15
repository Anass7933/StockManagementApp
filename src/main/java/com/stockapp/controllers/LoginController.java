package com.stockapp.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import com.stockapp.services.*;

import java.sql.SQLException;

public class LoginController {

    @FXML
    private Button cancelButton;

    @FXML
    private Button loginButton;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label incorrectLabel;

    @FXML
    private void loginButtonOnAction(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        try {
            if (AuthService.validateLogin(username, password)) {
                incorrectLabel.setText("Login successful!");
            } else {
                incorrectLabel.setText("Invalid username or password.");
            }
        } catch (SQLException e) {
            incorrectLabel.setText("Database error.");
            e.printStackTrace();
        }
    }

    @FXML
    private void cancelButtonOnAction(ActionEvent e) {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}
