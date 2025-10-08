package com.maintenance.ui.views;

import com.maintenance.ui.controllers.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class ViewFactory {

    public void showLoginWindow() {
        try {
            Stage stage = new Stage();
            LoginController controller = new LoginController(this);

            AnchorPane root = new AnchorPane();
            controller.createLoginUI(root);

            Scene scene = new Scene(root, 500, 400);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

            stage.setTitle("Residential Maintenance System - Login");
            stage.setScene(scene);
            stage.setResizable(false);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showTenantDashboard(Stage stage) {
        try {
            TenantDashboardController controller = new TenantDashboardController(this);

            AnchorPane root = new AnchorPane();
            controller.createDashboardUI(root);

            Scene scene = new Scene(root, 1200, 700);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

            stage.setTitle("Tenant Dashboard");
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showManagerDashboard(Stage stage) {
        try {
            ManagerDashboardController controller = new ManagerDashboardController(this);

            AnchorPane root = new AnchorPane();
            controller.createDashboardUI(root);

            Scene scene = new Scene(root, 1200, 700);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

            stage.setTitle("Manager Dashboard");
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showStaffDashboard(Stage stage) {
        try {
            StaffDashboardController controller = new StaffDashboardController(this);

            AnchorPane root = new AnchorPane();
            controller.createDashboardUI(root);

            Scene scene = new Scene(root, 1200, 700);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

            stage.setTitle("Staff Dashboard");
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void closeStage(Stage stage) {
        stage.close();
    }
}
