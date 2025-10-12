package com.maintenance.ui.controllers;


import com.maintenance.models.*;
import com.maintenance.service.AuthenticationService;
import com.maintenance.ui.views.ViewFactory;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.net.URL;

public class LoginController {
    private final ViewFactory viewFactory;
    private final AuthenticationService authService;


    public LoginController(ViewFactory viewFactory) {
        this.viewFactory = viewFactory;
        this.authService = AuthenticationService.getInstance();
    }


    public void createLoginUI(AnchorPane root) {
        // Full window background
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #667eea 0%, #764ba2 100%);");

        // Outer container that fills the screen and adds padding around the centered card
        StackPane outer = new StackPane();
        outer.setPadding(new Insets(80));
        outer.setAlignment(Pos.CENTER);

        // Centered login card
        VBox mainContainer = new VBox(20);
        mainContainer.setAlignment(Pos.CENTER);
        mainContainer.setPadding(new Insets(40));
        mainContainer.setMaxWidth(400);
        mainContainer.setStyle(
                "-fx-background-color: white; -fx-background-radius: 15; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 15, 0, 0, 5);"
        );


        // Load the apartment icon image
        Image appIcon = loadImage("/images/apartment.png");
        ImageView appIconView = createImageView(appIcon, 30, 30);

        // Title
        Label titleLabel = new Label("Maintenance System");
        if (appIconView != null) {
            titleLabel.setGraphic(appIconView);
            titleLabel.setContentDisplay(ContentDisplay.LEFT);
            titleLabel.setGraphicTextGap(10);
        }
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
        usernameField.setMaxWidth(Double.MAX_VALUE);
        usernameField.setStyle("-fx-padding: 10; -fx-background-radius: 5; -fx-border-color: #ddd; -fx-border-radius: 5;");


        // Password field
        Label passwordLabel = new Label("Password");
        passwordLabel.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 13));


        // Hidden password field
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter password");
        passwordField.setMaxWidth(Double.MAX_VALUE);
        passwordField.setStyle("-fx-padding: 10; -fx-background-radius: 5; -fx-border-color: #ddd; -fx-border-radius: 5;");


        // Visible password field
        TextField visiblePasswordField = new TextField();
        visiblePasswordField.setPromptText("Enter password");
        visiblePasswordField.setStyle("-fx-padding: 10; -fx-background-radius: 5; -fx-border-color: #ddd; -fx-border-radius: 5;");
        visiblePasswordField.setManaged(false);
        visiblePasswordField.setVisible(false);


        // Keep text synced between both fields
        visiblePasswordField.textProperty().bindBidirectional(passwordField.textProperty());


        // Load eye icons
        Image eyeOpen = loadImage("/images/eye-open.png");
        Image eyeClosed = loadImage("/images/eye-closed.png");

        Node toggleControl;
        ImageView eyeIconView = createImageView(eyeClosed, 20, 20);
        if (eyeIconView != null && eyeOpen != null && eyeClosed != null) {
            eyeIconView.setCursor(javafx.scene.Cursor.HAND);
            toggleControl = eyeIconView;
        } else {
            Label toggleLabel = new Label("Show");
            toggleLabel.setTextFill(Color.web("#667eea"));
            toggleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            toggleLabel.setCursor(javafx.scene.Cursor.HAND);
            toggleControl = toggleLabel;
        }

        Node finalToggleControl = toggleControl;

        // Toggle between hidden/visible password
        toggleControl.setOnMouseClicked(e -> {
            boolean isVisible = visiblePasswordField.isVisible();
            visiblePasswordField.setVisible(!isVisible);
            visiblePasswordField.setManaged(!isVisible);
            passwordField.setVisible(isVisible);
            passwordField.setManaged(isVisible);

            if (finalToggleControl instanceof ImageView iconView && eyeOpen != null && eyeClosed != null) {
                iconView.setImage(isVisible ? eyeClosed : eyeOpen);
            } else if (finalToggleControl instanceof Label toggleLabel) {
                toggleLabel.setText(isVisible ? "Show" : "Hide");
            }
        });

        // Container to hold password field and toggle control
        StackPane passwordFieldContainer = new StackPane();
        passwordFieldContainer.setAlignment(Pos.CENTER_RIGHT);

        // StackPane with both visible and hidden fields
        StackPane passwordStack = new StackPane(passwordField, visiblePasswordField);
        passwordStack.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(passwordStack, Priority.ALWAYS);
        passwordField.setPadding(new Insets(10, 35, 10, 10));
        visiblePasswordField.setPadding(new Insets(10, 35, 10, 10));
        StackPane.setMargin(finalToggleControl, new Insets(0, 10, 0, 0));
        passwordFieldContainer.getChildren().addAll(passwordStack, finalToggleControl);


        // Error label
        Label errorLabel = new Label();
        errorLabel.setTextFill(Color.RED);
        errorLabel.setVisible(false);


        // Login button
        Button loginButton = new Button("LOGIN");
        loginButton.setDefaultButton(true); // Enter key submits
        loginButton.setMaxWidth(Double.MAX_VALUE);
        loginButton.setStyle(
                "-fx-background-color: #667eea; -fx-text-fill: white; -fx-font-size: 14; " +
                        "-fx-font-weight: bold; -fx-padding: 12; -fx-background-radius: 5; -fx-cursor: hand;"
        );


        loginButton.setOnMouseEntered(e ->
                loginButton.setStyle(
                        "-fx-background-color: #5568d3; -fx-text-fill: white; -fx-font-size: 14; " +
                                "-fx-font-weight: bold; -fx-padding: 12; -fx-background-radius: 5; -fx-cursor: hand;"
                )
        );
        loginButton.setOnMouseExited(e ->
                loginButton.setStyle(
                        "-fx-background-color: #667eea; -fx-text-fill: white; -fx-font-size: 14; " +
                                "-fx-font-weight: bold; -fx-padding: 12; -fx-background-radius: 5; -fx-cursor: hand;"
                )
        );


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

        // Group inputs
        VBox userBox = new VBox(5, usernameLabel, usernameField);
        VBox passBox = new VBox(5, passwordLabel, passwordFieldContainer);
        userBox.setMaxWidth(Double.MAX_VALUE);
        passBox.setMaxWidth(Double.MAX_VALUE);

        mainContainer.getChildren().addAll(
                titleLabel, subtitleLabel,
                userBox, passBox,
                errorLabel, loginButton, infoLabel
        );

        // Put card into outer container
        outer.getChildren().add(mainContainer);

        // Anchor outer to fill the root
        AnchorPane.setTopAnchor(outer, 0.0);
        AnchorPane.setBottomAnchor(outer, 0.0);
        AnchorPane.setLeftAnchor(outer, 0.0);
        AnchorPane.setRightAnchor(outer, 0.0);

        root.getChildren().add(outer);
    }

    private Image loadImage(String path) {
        try {
            URL resource = getClass().getResource(path);
            if (resource == null) {
                System.err.println("Missing resource: " + path);
                return null;
            }
            return new Image(resource.toExternalForm());
        } catch (Exception e) {
            System.err.println("Failed to load image " + path + ": " + e.getMessage());
            return null;
        }
    }

    private ImageView createImageView(Image image, double width, double height) {
        if (image == null) {
            return null;
        }
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);
        imageView.setPreserveRatio(true);
        return imageView;
    }
}
