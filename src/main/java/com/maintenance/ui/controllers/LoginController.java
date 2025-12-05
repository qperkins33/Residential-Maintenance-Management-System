package com.maintenance.ui.controllers;

import com.maintenance.database.DatabaseManager;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

public class LoginController {
    private final ViewFactory viewFactory;
    private final AuthenticationService authService;
    private final DatabaseManager dbManager;

    public LoginController(ViewFactory viewFactory) {
        this.viewFactory = viewFactory;
        this.authService = AuthenticationService.getInstance();
        this.dbManager = DatabaseManager.getInstance();
    }

    public void createLoginUI(AnchorPane root) {
        // Full window gradient background
        root.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #667eea 0%, #764ba2 100%);"
        );

        StackPane outer = new StackPane();
        outer.setPadding(new Insets(60));
        outer.setAlignment(Pos.CENTER);

        HBox shell = new HBox(40);
        shell.setAlignment(Pos.CENTER_LEFT);
        shell.setMaxWidth(960);
        shell.setPadding(new Insets(35));
        shell.setStyle(
                "-fx-background-color: rgba(255,255,255,0.10); " +
                        "-fx-background-radius: 30; " +
                        "-fx-border-color: rgba(255,255,255,0.25); " +
                        "-fx-border-radius: 30; " +
                        "-fx-border-width: 1.2;"
        );

        Image appIcon = new Image(Objects.requireNonNull(
                getClass().getResource("/images/apartment.png")).toExternalForm()
        );

        // LEFT PANEL
        VBox brandingPanel = new VBox(18);
        brandingPanel.setAlignment(Pos.TOP_LEFT);
        brandingPanel.setPrefWidth(380); // slightly wider
        brandingPanel.setPadding(new Insets(28));
        brandingPanel.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #667eea, #764ba2); " +
                        "-fx-background-radius: 22;"
        );

        Label appNameLarge = new Label("Building Maintenance Hub");
        appNameLarge.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 30));
        appNameLarge.setTextFill(Color.WHITE);
        appNameLarge.setWrapText(true);                 // allow wrapping
        appNameLarge.setMaxWidth(260);                  // prevents cutoff in HBox
        HBox.setHgrow(appNameLarge, Priority.ALWAYS);   // let it use remaining space

        Label tagline = new Label(
                "Centralize tenant issues, assign work orders, and keep your buildings running smoothly."
        );
        tagline.setWrapText(true);
        tagline.setFont(Font.font("Arial", 13));
        tagline.setTextFill(Color.web("#f1f3ff"));

        HBox heroHeader = new HBox(12, appNameLarge);
        heroHeader.setAlignment(Pos.CENTER_LEFT);

        HBox rolePills = new HBox(8);
        rolePills.setAlignment(Pos.CENTER_LEFT);
        rolePills.getChildren().addAll(
                createRolePill("Tenant"),
                createRolePill("Staff"),
                createRolePill("Manager"),
                createRolePill("Admin")
        );
        rolePills.setPadding(new Insets(4, 0, 0, 0));

        Region brandingSpacer = new Region();
        VBox.setVgrow(brandingSpacer, Priority.ALWAYS);

        Label envLabel = new Label("Secure access • Real-time updates • Multi-role dashboards");
        envLabel.setFont(Font.font("Arial", 11));
        envLabel.setTextFill(Color.web("#e5e7ff"));

        brandingPanel.getChildren().addAll(
                heroHeader,
                tagline,
                rolePills,
                brandingSpacer,
                envLabel
        );

        // RIGHT LOGIN CARD
        VBox mainContainer = new VBox(22);
        mainContainer.setAlignment(Pos.TOP_LEFT);
        mainContainer.setPadding(new Insets(30));
        mainContainer.setPrefWidth(380);
        mainContainer.setStyle(
                "-fx-background-color: white; " +
                        "-fx-background-radius: 22; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.35), 25, 0, 0, 10);"
        );

        ImageView appIconView = new ImageView(appIcon);
        appIconView.setFitWidth(26);
        appIconView.setFitHeight(26);

        Label titleLabel = new Label("Sign in");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        titleLabel.setTextFill(Color.web("#111827"));

        Label subtitleLabel = new Label("Use your assigned account to access the maintenance portal.");
        subtitleLabel.setFont(Font.font("Arial", 12));
        subtitleLabel.setTextFill(Color.web("#6b7280"));
        subtitleLabel.setWrapText(true);

        VBox headerText = new VBox(4, titleLabel, subtitleLabel);
        HBox headerRow = new HBox(10, appIconView, headerText);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        Label usernameLabel = new Label("Username");
        usernameLabel.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 13));
        usernameLabel.setTextFill(Color.web("#374151"));

        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter username");
        usernameField.setMaxWidth(Double.MAX_VALUE);
        usernameField.setStyle(
                "-fx-background-color: #f3f4ff; -fx-padding: 10; " +
                        "-fx-background-radius: 10; -fx-border-radius: 10; " +
                        "-fx-border-color: transparent;"
        );

        Label passwordLabel = new Label("Password");
        passwordLabel.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 13));
        passwordLabel.setTextFill(Color.web("#374151"));

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter password");
        passwordField.setMaxWidth(Double.MAX_VALUE);
        passwordField.setStyle(
                "-fx-background-color: #f3f4ff; -fx-padding: 10; " +
                        "-fx-background-radius: 10; -fx-border-radius: 10; " +
                        "-fx-border-color: transparent;"
        );

        TextField visiblePasswordField = new TextField();
        visiblePasswordField.setPromptText("Enter password");
        visiblePasswordField.setMaxWidth(Double.MAX_VALUE);
        visiblePasswordField.setStyle(
                "-fx-background-color: #f3f4ff; -fx-padding: 10; " +
                        "-fx-background-radius: 10; -fx-border-radius: 10; " +
                        "-fx-border-color: transparent;"
        );
        visiblePasswordField.setManaged(false);
        visiblePasswordField.setVisible(false);

        visiblePasswordField.textProperty().bindBidirectional(passwordField.textProperty());

        // Load eye icons
        Image eyeOpen = new Image(Objects.requireNonNull(getClass().getResource("/images/eye-open.png")).toExternalForm());
        Image eyeClosed = new Image(Objects.requireNonNull(getClass().getResource("/images/eye-closed.png")).toExternalForm());

        ImageView eyeIconView = new ImageView(eyeClosed);
        eyeIconView.setFitWidth(20);
        eyeIconView.setFitHeight(20);
        eyeIconView.setPreserveRatio(true);
        eyeIconView.setCursor(javafx.scene.Cursor.HAND);

        eyeIconView.setOnMouseClicked(e -> {
            boolean isVisible = visiblePasswordField.isVisible();
            visiblePasswordField.setVisible(!isVisible);
            visiblePasswordField.setManaged(!isVisible);
            passwordField.setVisible(isVisible);
            passwordField.setManaged(isVisible);
            eyeIconView.setImage(isVisible ? eyeClosed : eyeOpen);
        });

        StackPane passwordFieldContainer = new StackPane();
        passwordFieldContainer.setAlignment(Pos.CENTER_RIGHT);

        StackPane passwordStack = new StackPane(passwordField, visiblePasswordField);
        passwordStack.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(passwordStack, Priority.ALWAYS);
        passwordField.setPadding(new Insets(10, 36, 10, 10));
        visiblePasswordField.setPadding(new Insets(10, 36, 10, 10));

        StackPane.setMargin(eyeIconView, new Insets(0, 10, 0, 0));
        passwordFieldContainer.getChildren().addAll(passwordStack, eyeIconView);

        // Error label
        Label errorLabel = new Label();
        errorLabel.setTextFill(Color.web("#b91c1c"));
        errorLabel.setWrapText(true);
        errorLabel.setStyle(
                "-fx-background-color: rgba(220,38,38,0.08); " +
                        "-fx-padding: 6 10 6 10; -fx-background-radius: 8;"
        );
        errorLabel.setVisible(false);

        // Button
        Button loginButton = getButton(usernameField, passwordField, errorLabel);
        loginButton.setMaxWidth(Double.MAX_VALUE);
        loginButton.setStyle(
                "-fx-background-color: linear-gradient(to right, #667eea, #764ba2); " +
                        "-fx-text-fill: white; -fx-font-size: 14; " +
                        "-fx-font-weight: bold; -fx-padding: 12; " +
                        "-fx-background-radius: 9999; -fx-cursor: hand;"
        );
        loginButton.setOnMouseEntered(e ->
                loginButton.setStyle(
                        "-fx-background-color: linear-gradient(to right, #5568d3, #6a4099); " +
                                "-fx-text-fill: white; -fx-font-size: 14; " +
                                "-fx-font-weight: bold; -fx-padding: 12; " +
                                "-fx-background-radius: 9999; -fx-cursor: hand;"
                )
        );
        loginButton.setOnMouseExited(e ->
                loginButton.setStyle(
                        "-fx-background-color: linear-gradient(to right, #667eea, #764ba2); " +
                                "-fx-text-fill: white; -fx-font-size: 14; " +
                                "-fx-font-weight: bold; -fx-padding: 12; " +
                                "-fx-background-radius: 9999; -fx-cursor: hand;"
                )
        );

        Label infoLabel = new Label("Demo access: admin1 / pass123");
        infoLabel.setFont(Font.font("Arial", 10));
        infoLabel.setTextFill(Color.web("#6b7280"));
        infoLabel.setStyle(
                "-fx-background-color: #f3f4ff; -fx-padding: 6 10 6 10; " +
                        "-fx-background-radius: 9999;"
        );
        infoLabel.setWrapText(true);

        VBox userBox = new VBox(6, usernameLabel, usernameField);
        VBox passBox = new VBox(6, passwordLabel, passwordFieldContainer);
        userBox.setMaxWidth(Double.MAX_VALUE);
        passBox.setMaxWidth(Double.MAX_VALUE);

        // Remember and forgot removed, so inputs go directly to error/button
        mainContainer.getChildren().addAll(
                headerRow,
                userBox,
                passBox,
                errorLabel,
                loginButton,
                infoLabel
        );

        shell.getChildren().addAll(brandingPanel, mainContainer);
        HBox.setHgrow(mainContainer, Priority.ALWAYS);

        outer.getChildren().add(shell);

        AnchorPane.setTopAnchor(outer, 0.0);
        AnchorPane.setBottomAnchor(outer, 0.0);
        AnchorPane.setLeftAnchor(outer, 0.0);
        AnchorPane.setRightAnchor(outer, 0.0);

        root.getChildren().clear();
        root.getChildren().add(outer);
    }

    private Label createRolePill(String text) {
        Label pill = new Label(text);
        pill.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        pill.setTextFill(Color.WHITE);
        pill.setStyle(
                "-fx-background-color: rgba(255,255,255,0.22); " +
                        "-fx-padding: 4 10 4 10; " +
                        "-fx-background-radius: 9999;"
        );
        return pill;
    }

    private Button getButton(TextField usernameField, PasswordField passwordField, Label errorLabel) {
        Button loginButton = new Button("Sign in");
        loginButton.setDefaultButton(true);

        loginButton.setOnAction(e -> {
            String username = usernameField.getText();
            String password = passwordField.getText();

            if (username.isEmpty() || password.isEmpty()) {
                errorLabel.setText("Please fill in all fields.");
                errorLabel.setVisible(true);
                return;
            }

            errorLabel.setVisible(false);

            if (authService.login(username, password)) {
                User currentUser = authService.getCurrentUser();

                if (currentUser != null && !currentUser.isActive()) {
                    errorLabel.setText("Your account is inactive. Contact an administrator.");
                    errorLabel.setVisible(true);
                    authService.logout();
                    return;
                }

                Stage currentStage = (Stage) loginButton.getScene().getWindow();
                viewFactory.closeStage(currentStage);

                Stage newStage = new Stage();

                if (currentUser instanceof Tenant) {
                    viewFactory.showTenantDashboard(newStage);
                } else if (currentUser instanceof BuildingManager) {
                    viewFactory.showManagerDashboard(newStage);
                } else if (currentUser instanceof MaintenanceStaff) {
                    viewFactory.showStaffDashboard(newStage);
                } else if (currentUser instanceof Admin) {
                    viewFactory.showAdminDashboard(newStage);
                }
            } else {
                if (isInactiveUser(username, password)) {
                    errorLabel.setText("Your account is inactive. Contact an administrator.");
                } else {
                    errorLabel.setText("Invalid username or password.");
                }
                errorLabel.setVisible(true);
            }
        });
        return loginButton;
    }

    private boolean isInactiveUser(String username, String password) {
        Connection conn = dbManager.getConnection();
        if (conn == null) {
            System.err.println("Database connection is null in isInactiveUser");
            return false;
        }

        String sql = "SELECT is_active FROM users WHERE username = ? AND password = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    boolean active = rs.getBoolean("is_active");
                    return !active;
                }
            }
        } catch (SQLException ex) {
            System.err.println("Error checking inactive user during login: " + ex.getMessage());
        }
        return false;
    }
}
