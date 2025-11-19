package com.stockapp.controllers;

import com.stockapp.models.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import com.stockapp.services.AuthService;

import java.io.IOException;
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
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        try {
            boolean valid = AuthService.validateLogin(username, password);
            String role = AuthService.loginRole(username, password);

            if (valid && role.equals("ADMIN")) {

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AdminUsersDashboard.fxml"));
                Parent root = loader.load();

                AdminUsersController controller = loader.getController();
                controller.setLoggedUser(username);

                Stage stage = (Stage) loginButton.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
            }
            else if (valid && role.equals("STOCK_MANAGER")) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/StockManagerDashboard.fxml"));
                Scene scene = new Scene(loader.load());

                StockManagerDashboardController controller = loader.getController();
                controller.setLoggedUser(username);

                Stage stage = (Stage) loginButton.getScene().getWindow();
                stage.setScene(scene);
                stage.show();
            } else if (valid && role.equals("CASHIER")) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CashierDashboard.fxml"));
                Scene scene = new Scene(loader.load());

                Stage stage = (Stage) loginButton.getScene().getWindow();
                stage.setScene(scene);
                stage.show();
            } else {
                incorrectLabel.setText("Invalid username or password.");
            }

        } catch (SQLException e) {
            incorrectLabel.setText("Database error.");
            e.printStackTrace();
        } catch (IOException e) {
            incorrectLabel.setText("Cannot load next page.");
            e.printStackTrace();
        }
    }

    @FXML
    private void cancelButtonOnAction(ActionEvent e) {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}
