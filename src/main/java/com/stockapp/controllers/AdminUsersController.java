package com.stockapp.controllers;

import com.stockapp.models.entities.User;
import com.stockapp.services.interfaces.UserService;
import com.stockapp.services.impl.UserServiceImpl;

import javafx.animation.Timeline;
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
import java.util.List;

public class AdminUsersController {

	@FXML
	private Label userNameLabel;
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

	private User loggedUser;

	private final Timeline refreshTimeline = new Timeline(
			new KeyFrame(Duration.ZERO, e -> refreshUsers()),
			new KeyFrame(Duration.seconds(2), e -> refreshUsers()));

	public void setLoggedUser(String username) {
		userNameLabel.setText("Hi, " + username);
	}

	@FXML
	private void initialize() {
		idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
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
			openUserForm(String.valueOf(selected.getId()));
		});

		deleteButton.setOnAction(e -> {
			User selected = usersTable.getSelectionModel().getSelectedItem();
			if (selected == null) {
				showAlert("Select a user to delete.");
				return;
			}
			UserService userService = new UserServiceImpl();
			userService.delete(selected.getId());
			refreshUsers();
		});

		sighOutButton.setOnAction(e -> signOut());

		productsButton.setOnAction(e -> {
			try {
				FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AdminProductsDashboard.fxml"));
				Parent root = loader.load();

				AdminProductsController controller = loader.getController();
				controller.setLoggedUser(userNameLabel.getText().replace("Hi, ", ""));

				Stage stage = (Stage) productsButton.getScene().getWindow();
				stage.setScene(new Scene(root));
				stage.show();

			} catch (IOException ex) {
				throw new RuntimeException(ex);
			}
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
		UserService userService = new UserServiceImpl();
		List<User> users = userService.readAll();
		users.removeIf(u -> u.getId() == userService.findByUsername("admin").getId());

		User selected = usersTable.getSelectionModel().getSelectedItem();
		ObservableList<User> data = FXCollections.observableArrayList(users);
		usersTable.setItems(data);

		if (selected != null) {
			data.stream()
					.filter(u -> u.getId() == selected.getId())
					.findFirst()
					.ifPresent(u -> usersTable.getSelectionModel().select(u));
		}

		javafx.application.Platform.runLater(() -> autoResizeTable());

	}

	private void autoResizeTable() {
		double headerHeight = 30; // fallback

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
