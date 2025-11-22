package com.stockapp.controllers;

import com.stockapp.models.entities.User;
import com.stockapp.models.enums.UserRole;
import com.stockapp.services.impl.UserServiceImpl;
import com.stockapp.services.interfaces.UserService;
import com.stockapp.utils.PasswordUtils;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.time.OffsetDateTime;

public class UserFormController {

	@FXML
	private TextField usernameField;
	@FXML
	private TextField fullNameField;
	@FXML
	private TextField passwordField;
	@FXML
	private ComboBox<String> roleComboBox;
	@FXML
	private Button saveButton;
	@FXML
	private Button cancelButton;

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
			UserRole role = UserRole.valueOf(roleComboBox.getValue().replace(" ", "_").toUpperCase());
			UserService userService = new UserServiceImpl();

			User user = new User(username, PasswordUtils.hashPassword(password), fullName, role, OffsetDateTime.now());

			if (editingUserId == 0) {
				userService.create(user);
			} else {
				user.setId(editingUserId);
				userService.update(user);
			}

			stage.close();
		});
	}

	// Pre-fill fields for modify
	private long editingUserId;

	public void loadUserData(long userId) {
		editingUserId = userId;
		UserService userService = new UserServiceImpl();
		userService.read(userId).ifPresent(user -> {
			usernameField.setText(user.getUserName());
			fullNameField.setText(user.getFullName());
			roleComboBox.setValue(user.getRole().name());
			passwordField.clear();
		});
	}

	public void cancelButtonOnAction(ActionEvent actionEvent) {
		stage.close();
	}
}
