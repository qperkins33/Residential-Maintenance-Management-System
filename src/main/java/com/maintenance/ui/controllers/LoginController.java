package com.maintenance.ui.controllers;

import com.maintenance.models.*;
import com.maintenance.service.AuthenticationService;
import com.maintenance.ui.views.ViewFactory;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class LoginController {
    private final ViewFactory viewFactory;
    private final AuthenticationService authService;

    public LoginController(ViewFactory viewFactory) {
        this.viewFactory = viewFactory;
        this.authService = AuthenticationService.getInstance();
    }

    public void createLoginUI(AnchorPane root) {
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #667eea 0%, #764ba2 100%);");

        VBox mainContainer = new VBox(20);
        mainContainer.setAlignment(Pos.CENTER);
        mainContainer.setPadding(new Insets(40));
        mainContainer.setMaxWidth(400);
        mainContainer.setStyle("-fx-background-color: white; -fx-background-radius: 15; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 15, 0, 0, 5);");

        // Title
        Label titleLabel = new Label("Maintenance System");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        titleLabel.setTextFill(Color.web("#667eea"));

        Label subtitleLabel = new Label("Sign in to continue");
        subtitleLabel.setFont(Font.font("Arial", 14));
        subtitleLabel.setTextFill(Color.GRAY);

        // Username field
        Label usernameLabel = new Label("Username");
        usernameLabel.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 13));
        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter username");
        usernameField.setStyle("-fx-padding: 10; -fx-background-radius: 5; -fx-border-color: #ddd; -fx-border-radius: 5;");

        // Password field
        Label passwordLabel = new Label("Password");
        passwordLabel.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 13));
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter password");
        passwordField.setStyle("-fx-padding: 10; -fx-background-radius: 5; -fx-border-color: #ddd; -fx-border-radius: 5;");

        // Error label
        Label errorLabel = new Label();
        errorLabel.setTextFill(Color.RED);
        errorLabel.setVisible(false);

        // Login button
        Button loginButton = new Button("LOGIN");
        loginButton.setMaxWidth(Double.MAX_VALUE);
        loginButton.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; -fx-font-size: 14; " +
                "-fx-font-weight: bold; -fx-padding: 12; -fx-background-radius: 5; " +
                "-fx-cursor: hand;");

        loginButton.setOnMouseEntered(e ->
                loginButton.setStyle("-fx-background-color: #5568d3; -fx-text-fill: white; -fx-font-size: 14; " +
                        "-fx-font-weight: bold; -fx-padding: 12; -fx-background-radius: 5; -fx-cursor: hand;"));
        loginButton.setOnMouseExited(e ->
                loginButton.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; -fx-font-size: 14; " +
                        "-fx-font-weight: bold; -fx-padding: 12; -fx-background-radius: 5; -fx-cursor: hand;"));

        loginButton.setOnAction(e -> {
            String username = usernameField.getText();
            String password = passwordField.getText();

            if (username.isEmpty() || password.isEmpty()) {
                errorLabel.setText("Please fill in all fields");
                errorLabel.setVisible(true);
                return;
            }

            if (authService.login(username, password)) {
                Stage currentStage = (Stage) loginButton.getScene().getWindow();
                viewFactory.closeStage(currentStage);

                Stage newStage = new Stage();
                User currentUser = authService.getCurrentUser();

                if (currentUser instanceof Tenant) {
                    viewFactory.showTenantDashboard(newStage);
                } else if (currentUser instanceof BuildingManager) {
                    viewFactory.showManagerDashboard(newStage);
                } else if (currentUser instanceof MaintenanceStaff) {
                    viewFactory.showStaffDashboard(newStage);
                }
            } else {
                errorLabel.setText("Invalid username or password");
                errorLabel.setVisible(true);
            }
        });

        // Demo credentials info
        Label infoLabel = new Label("Demo: tenant1/pass123, manager1/pass123, staff1/pass123");
        infoLabel.setFont(Font.font("Arial", 10));
        infoLabel.setTextFill(Color.GRAY);
        infoLabel.setWrapText(true);
        infoLabel.setAlignment(Pos.CENTER);

        mainContainer.getChildren().addAll(
                titleLabel, subtitleLabel,
                new VBox(5, usernameLabel, usernameField),
                new VBox(5, passwordLabel, passwordField),
                errorLabel, loginButton, infoLabel
        );

        AnchorPane.setTopAnchor(mainContainer, 0.0);
        AnchorPane.setBottomAnchor(mainContainer, 0.0);
        AnchorPane.setLeftAnchor(mainContainer, 0.0);
        AnchorPane.setRightAnchor(mainContainer, 0.0);

        root.getChildren().add(mainContainer);
    }
}
