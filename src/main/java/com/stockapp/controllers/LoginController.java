package com.stockapp.controllers;

import com.stockapp.utils.DatabaseUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
            if (validateLogin(username, password)) {
                incorrectLabel.setText("Login successful!");
                // move to next scene here
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

    private boolean validateLogin(String username, String password) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ? AND password_hash = ?";

        try (Connection conn = DatabaseUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
}
