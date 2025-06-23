package com.example.stores;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;

/**
 * JavaFX Main class updated to work without module-info.java
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Set up the user interface
        try {
            // Load login interface from FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/stores/view/RoleSelection.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);

            // Set up the window
            primaryStage.setTitle("CELLCOMP STORE - Đăng nhập");
            primaryStage.setScene(scene);

            // Đặt kích thước cố định thay vì kích thước tối thiểu
            primaryStage.setWidth(1010);
            primaryStage.setHeight(700);

            // Vô hiệu hóa khả năng thay đổi kích thước
            primaryStage.setResizable(false);

            primaryStage.show();
            primaryStage.centerOnScreen();

        } catch (IOException e) {
            System.err.println("Error loading login interface: \n");
            e.printStackTrace();
            showErrorAlert("Startup Error", "Could not load login interface: " + e.getMessage());
            Platform.exit();
        } catch (Exception e) {
            System.err.println("Unknown error: \n");
            e.printStackTrace();
            showErrorAlert("Unknown Error", e.getMessage());
            Platform.exit();
        }
    }

    /**
     * Display error alert dialog
     */
    private void showErrorAlert(String title, String content) {
        try {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        } catch (Exception e) {
            System.err.println("Could not display error screen: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Standard main() method to run the application both in IDE and from jar
     */
    public static void main(String[] args) {
        // Connect to database
        Connection conn = DatabaseConnection.getConnection();
        if (conn != null) {
            System.out.println("Database connection successful!");
        }

        // Launch JavaFX application
        launch(args);
    }

    @Override
    public void stop() {
        // Close database connection when application exits
        System.out.println("Database connection closed");
        System.out.println("Application stopping, cleaning up resources...");
    }
}