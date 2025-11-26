package com.stockapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Main extends Application {
	@Override
	public void start(Stage primaryStage) throws Exception {
		// Parent root =
		// FXMLLoader.load(getClass().getResource("/fxml/cashier/CartView.fxml"));
		// Parent root =
		// FXMLLoader.load(getClass().getResource("/fxml/cashier/CashierView.fxml"));
		// Parent root =
		// FXMLLoader.load(getClass().getResource("/fxml/cashier/QuantityForm.fxml"));
		Parent root = FXMLLoader.load(getClass().getResource("/fxml/Login.fxml"));
		primaryStage.initStyle(StageStyle.DECORATED);
		primaryStage.setScene(new Scene(root, 500, 400));
		primaryStage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}
}
