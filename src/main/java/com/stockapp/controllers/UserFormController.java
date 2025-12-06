package com.stockapp.controllers;

import com.stockapp.models.entities.User;
import com.stockapp.models.enums.UserRole;
import com.stockapp.services.impl.UserServiceImpl;
import com.stockapp.services.interfaces.UserService;
import java.time.OffsetDateTime;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import static com.stockapp.utils.PasswordUtils.hashPassword;

public class UserFormController {

    @FXML private TextField usernameField;
    @FXML private TextField fullNameField;
    @FXML private TextField passwordField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private Stage stage;
    private long editingUserId = 0;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void initialize() {

        saveButton.setOnAction(e -> handleSave());
        cancelButton.setOnAction(e -> stage.close());
    }

    private void handleSave() {
        String username = usernameField.getText();
        String fullName = fullNameField.getText();
        String password = passwordField.getText();
        String roleValue = roleComboBox.getValue();

        if (!validateInputs(username, fullName, password, roleValue)) {
            return;
        }

        UserRole role = UserRole.valueOf(roleValue.replace(" ", "_").toUpperCase());
        UserService userService = new UserServiceImpl();

        String finalPasswordHash;
        if (editingUserId != 0 && (password == null || password.isBlank())) {
            finalPasswordHash = userService.read(editingUserId)
                    .orElseThrow()
                    .getPasswordHash();
        } else {
            finalPasswordHash = hashPassword(password);
        }

        User user = new User(username, finalPasswordHash, fullName, role, OffsetDateTime.now());

        if (editingUserId == 0) {
            userService.create(user);
        } else {
            user.setId(editingUserId);
            userService.update(user);
        }

        stage.close();
    }

    private boolean validateInputs(String username, String fullName, String password, String roleValue) {
        if (fullName == null || fullName.isBlank()) {
            showAlert("Full name cannot be empty.");
            return false;
        }
        if (username == null || username.isBlank()) {
            showAlert("Username cannot be empty.");
            return false;
        }
        if (editingUserId == 0 && (password == null || password.isBlank())) {
            showAlert("Password cannot be empty.");
            return false;
        }
        if (roleValue == null) {
            showAlert("You must select a role.");
            return false;
        }
        return true;
    }

    public void loadUserData(long userId) {
        editingUserId = userId;
        UserService userService = new UserServiceImpl();

        userService.read(userId).ifPresent(user -> {
            usernameField.setText(user.getUserName());
            fullNameField.setText(user.getFullName());
            roleComboBox.setValue(user.getRole().name());
            passwordField.clear(); // Always empty for security
        });
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
