package com.maintenance;

import com.maintenance.database.DatabaseInitializer;
import com.maintenance.ui.views.ViewFactory;
import javafx.application.Application;
import javafx.stage.Stage;

public class MainApplication extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Initialize database
            DatabaseInitializer.initialize();

            // Show login window
            ViewFactory viewFactory = new ViewFactory();
            viewFactory.showLoginWindow();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error starting application: " + e.getMessage());
        }
    }

    @Override
    public void stop() {
        // Close database connections
        try {
            com.maintenance.database.DatabaseManager.getInstance().disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
