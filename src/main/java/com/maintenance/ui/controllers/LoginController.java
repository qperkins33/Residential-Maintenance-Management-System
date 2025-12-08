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

/**
 * Controller responsible for rendering and handling the login screen.
 * Handles:
 *  - Login form layout and styling
 *  - Authentication logic delegation
 *  - Routing to the correct dashboard based on user role
 *  - Displaying error messages for invalid or inactive accounts
 */
public class LoginController {
    // Factory for opening and closing application stages/views
    private final ViewFactory viewFactory;
    // Authentication service for login, logout, and current user management
    private final AuthenticationService authService;
    // Shared database manager used for low-level queries (e.g., inactive account check)
    private final DatabaseManager dbManager;

    /**
     * Constructs a LoginController with the shared ViewFactory and service singletons.
     *
     * @param viewFactory shared factory for creating and closing application windows
     */
    public LoginController(ViewFactory viewFactory) {
        this.viewFactory = viewFactory;
        this.authService = AuthenticationService.getInstance();
        this.dbManager = DatabaseManager.getInstance();
    }

    /**
     * Creates and attaches the login UI to the given root AnchorPane.
     * This includes the branded left panel and the right login card.
     *
     * @param root AnchorPane which the login UI will be drawn into
     */
    public void createLoginUI(AnchorPane root) {
        // Attach global app styles so login shares the same visual system
        DashboardUIHelper.applyRootStyles(root, getClass());

        // Clear any previous content and set the gradient background
        root.getChildren().clear();
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #667eea 0%, #764ba2 100%);");

        // Outer container that fills the screen and adds padding around the centered card
        StackPane outer = new StackPane();
        outer.setPadding(new Insets(60));
        outer.setAlignment(Pos.CENTER);

        // Shell holds the branding panel on the left and login card on the right
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

        // Application icon used on the login card
        Image appIcon = new Image(Objects.requireNonNull(getClass().getResource("/images/apartment.png")).toExternalForm());

        // LEFT PANEL
        VBox brandingPanel = new VBox(18);
        brandingPanel.setAlignment(Pos.TOP_LEFT);
        brandingPanel.setPrefWidth(380);
        brandingPanel.setPadding(new Insets(28));
        brandingPanel.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #667eea, #764ba2); " +
                        "-fx-background-radius: 22;"
        );

        // Large app name for branding
        Label appNameLarge = new Label("Residential Maintenance Management System");
        appNameLarge.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 30));
        appNameLarge.setTextFill(Color.WHITE);
        appNameLarge.setWrapText(true);
        appNameLarge.setMaxWidth(260);
        HBox.setHgrow(appNameLarge, Priority.ALWAYS);

        // Tagline describing the application's purpose
        Label tagline = new Label(
                "Centralize tenant issues, assign work orders, and keep your buildings running smoothly."
        );
        tagline.setWrapText(true);
        tagline.setFont(Font.font("Arial", 17));
        tagline.setTextFill(Color.web("#f1f3ff"));

        // Space above and below
        VBox.setMargin(tagline, new Insets(6, 0, 6, 0));

        // Header grouping app name (and could include icon if desired later)
        HBox heroHeader = new HBox(12, appNameLarge);
        heroHeader.setAlignment(Pos.CENTER_LEFT);

        // Role pills visually represent the different roles supported by the system
        HBox rolePills = new HBox(8);
        rolePills.setAlignment(Pos.CENTER_LEFT);
        rolePills.getChildren().addAll(
                createRolePill("Tenant"),
                createRolePill("Staff"),
                createRolePill("Manager"),
                createRolePill("Admin")
        );
        rolePills.setPadding(new Insets(4, 0, 0, 0));

        // Spacer pushes environment label to the bottom of the branding panel
        Region brandingSpacer = new Region();
        VBox.setVgrow(brandingSpacer, Priority.ALWAYS);

        // Short environment / capability tagline
        Label envLabel = new Label("Secure access • Real-time updates • Multi-role dashboards");
        envLabel.setFont(Font.font("Arial", 11));
        envLabel.setTextFill(Color.web("#e5e7ff"));

        // Assemble left branding panel
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
                "-fx-background-color: white; -fx-background-radius: 22; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.35), 25, 0, 0, 10);"
        );

        // Small app icon within the login card header
        ImageView appIconView = new ImageView(appIcon);
        appIconView.setFitWidth(26);
        appIconView.setFitHeight(26);

        // Header title and subtitle for login form
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

        // Username label + field
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

        // Password label + hidden password field
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

        // Visible password field (for "show password" toggle)
        TextField visiblePasswordField = new TextField();
        visiblePasswordField.setPromptText("Enter password");
        visiblePasswordField.setMaxWidth(Double.MAX_VALUE);
        visiblePasswordField.setStyle(
                "-fx-background-color: #f3f4ff; -fx-padding: 10; " +
                        "-fx-background-radius: 10; -fx-border-radius: 10; " +
                        "-fx-border-color: transparent;"
        );
        // Start hidden
        visiblePasswordField.setManaged(false);
        visiblePasswordField.setVisible(false);

        // Keep password text synced between the visible and hidden fields
        visiblePasswordField.textProperty().bindBidirectional(passwordField.textProperty());

        // Load eye icons for password visibility toggle
        Image eyeOpen = new Image(Objects.requireNonNull(getClass().getResource("/images/eye-open.png")).toExternalForm());
        Image eyeClosed = new Image(Objects.requireNonNull(getClass().getResource("/images/eye-closed.png")).toExternalForm());

        ImageView eyeIconView = new ImageView(eyeClosed);
        eyeIconView.setFitWidth(20);
        eyeIconView.setFitHeight(20);
        eyeIconView.setPreserveRatio(true);
        eyeIconView.setCursor(javafx.scene.Cursor.HAND);

        // Toggle between showing and hiding password text on click
        eyeIconView.setOnMouseClicked(e -> {
            boolean isVisible = visiblePasswordField.isVisible();
            visiblePasswordField.setVisible(!isVisible);
            visiblePasswordField.setManaged(!isVisible);
            passwordField.setVisible(isVisible);
            passwordField.setManaged(isVisible);
            eyeIconView.setImage(isVisible ? eyeClosed : eyeOpen);
        });

        // Container to overlay the eye icon on the password field stack
        StackPane passwordFieldContainer = new StackPane();
        passwordFieldContainer.setAlignment(Pos.CENTER_RIGHT);

        // Stack that swaps between PasswordField and TextField
        StackPane passwordStack = new StackPane(passwordField, visiblePasswordField);
        passwordStack.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(passwordStack, Priority.ALWAYS);
        passwordField.setPadding(new Insets(10, 36, 10, 10));
        visiblePasswordField.setPadding(new Insets(10, 36, 10, 10));

        StackPane.setMargin(eyeIconView, new Insets(0, 10, 0, 0));
        passwordFieldContainer.getChildren().addAll(passwordStack, eyeIconView);

        // Error label used for all login-related validation and feedback messages
        Label errorLabel = new Label();
        errorLabel.setTextFill(Color.web("#b91c1c"));
        errorLabel.setWrapText(true);
        errorLabel.setStyle(
                "-fx-background-color: rgba(220,38,38,0.08); " +
                        "-fx-padding: 6 10 6 10; -fx-background-radius: 8;"
        );
        errorLabel.setVisible(false);

        // Create the login button and wire it up to validation and authentication logic
        Button loginButton = getButton(usernameField, passwordField, errorLabel);
        loginButton.setMaxWidth(Double.MAX_VALUE);
        loginButton.setStyle(
                "-fx-background-color: linear-gradient(to right, #667eea, #764ba2); " +
                        "-fx-text-fill: white; -fx-font-size: 14; " +
                        "-fx-font-weight: bold; -fx-padding: 12; " +
                        "-fx-background-radius: 9999; -fx-cursor: hand;"
        );
        // Hover style adjustment
        loginButton.setOnMouseEntered(e ->
                loginButton.setStyle(
                        "-fx-background-color: linear-gradient(to right, #5568d3, #6a4099); " +
                                "-fx-text-fill: white; -fx-font-size: 14; " +
                                "-fx-font-weight: bold; -fx-padding: 12; " +
                                "-fx-background-radius: 9999; -fx-cursor: hand;"
                )
        );
        // Reset style on mouse exit
        loginButton.setOnMouseExited(e ->
                loginButton.setStyle(
                        "-fx-background-color: linear-gradient(to right, #667eea, #764ba2); " +
                                "-fx-text-fill: white; -fx-font-size: 14; " +
                                "-fx-font-weight: bold; -fx-padding: 12; " +
                                "-fx-background-radius: 9999; -fx-cursor: hand;"
                )
        );

        // Arrange label + field pairs in vertical containers
        VBox userBox = new VBox(6, usernameLabel, usernameField);
        VBox passBox = new VBox(6, passwordLabel, passwordFieldContainer);
        userBox.setMaxWidth(Double.MAX_VALUE);
        passBox.setMaxWidth(Double.MAX_VALUE);

        // Spacer to push sign-in info label to the bottom (mirrors left branding spacer)
        Region loginSpacer = new Region();
        VBox.setVgrow(loginSpacer, Priority.ALWAYS);

        // Small sign-in related info at the bottom of the card
        Label signInInfoLabel = new Label("For security, always sign out and close the app when you are finished.");
        signInInfoLabel.setFont(Font.font("Arial", 11));
        signInInfoLabel.setTextFill(Color.web("#6b7280"));
        signInInfoLabel.setWrapText(true);

        // Assemble right login card contents
        mainContainer.getChildren().addAll(
                headerRow,
                userBox,
                passBox,
                errorLabel,
                loginButton,
                loginSpacer,
                signInInfoLabel
        );

        // Add left branding and right login card to shell
        shell.getChildren().addAll(brandingPanel, mainContainer);
        HBox.setHgrow(mainContainer, Priority.ALWAYS);

        // Center shell inside outer container
        outer.getChildren().add(shell);

        // Attach outer container to all edges of the root anchor pane
        AnchorPane.setTopAnchor(outer, 0.0);
        AnchorPane.setBottomAnchor(outer, 0.0);
        AnchorPane.setLeftAnchor(outer, 0.0);
        AnchorPane.setRightAnchor(outer, 0.0);

        root.getChildren().add(outer);
    }

    /**
     * Creates a pill-like label used in the branding panel to display supported roles.
     *
     * @param text display text for the pill
     * @return a styled Label instance
     */
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

    /**
     * Creates and configures the login button, including validation and routing logic.
     *
     * @param usernameField TextField holding the username
     * @param passwordField PasswordField holding the password
     * @param errorLabel    Label used to show validation and error messages
     * @return fully configured Button ready to be placed in the UI
     */
    private Button getButton(TextField usernameField, PasswordField passwordField, Label errorLabel) {
        Button loginButton = new Button("Sign in");
        // Make this the default button so Enter triggers it
        loginButton.setDefaultButton(true);

        // Handle login action on click or Enter key
        loginButton.setOnAction(e -> {
            String username = usernameField.getText();
            String password = passwordField.getText();

            // Basic empty field validation
            if (username.isEmpty() || password.isEmpty()) {
                errorLabel.setText("Please fill in all fields.");
                errorLabel.setVisible(true);
                return;
            }

            errorLabel.setVisible(false);

            // Delegate authentication to AuthenticationService
            if (authService.login(username, password)) {
                User currentUser = authService.getCurrentUser();

                // Guard: authenticated but account marked inactive
                if (currentUser != null && !currentUser.isActive()) {
                    errorLabel.setText("Your account is inactive. Contact an administrator.");
                    errorLabel.setVisible(true);
                    authService.logout();
                    return;
                }

                // Close the login window
                Stage currentStage = (Stage) loginButton.getScene().getWindow();
                viewFactory.closeStage(currentStage);

                // Prepare a new stage for the dashboard
                Stage newStage = new Stage();

                // Route to the correct dashboard based on the concrete user type
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
                // Authentication failed: distinguish between invalid credentials and inactive account
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

    /**
     * Checks if a user with the supplied credentials exists but is inactive.
     * This is used to provide a more specific error message when login fails.
     *
     * @param username username to check
     * @param password password to check
     * @return true if a matching user exists and is inactive, false otherwise
     */
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
