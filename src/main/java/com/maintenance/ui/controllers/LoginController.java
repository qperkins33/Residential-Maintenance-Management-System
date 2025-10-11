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
        // Full window background
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #667eea 0%, #764ba2 100%);");

        // Outer container that fills the screen and adds padding around the centered card
        StackPane outer = new StackPane();
        outer.setPadding(new Insets(80));
        outer.setAlignment(Pos.CENTER);

        // Centered card container
        VBox mainContainer = new VBox();
        mainContainer.setAlignment(Pos.CENTER);
        mainContainer.setPadding(new Insets(40));
        mainContainer.setSpacing(20);
        mainContainer.setMaxWidth(460);
        mainContainer.setStyle(
                "-fx-background-color: white; -fx-background-radius: 15; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 15, 0, 0, 5);"
        );

        showLoginView(mainContainer, null);

        // Put card into outer container
        outer.getChildren().add(mainContainer);

        // Anchor outer to fill the root
        AnchorPane.setTopAnchor(outer, 0.0);
        AnchorPane.setBottomAnchor(outer, 0.0);
        AnchorPane.setLeftAnchor(outer, 0.0);
        AnchorPane.setRightAnchor(outer, 0.0);

        root.getChildren().add(outer);
    }

    private void showLoginView(VBox container, String successMessage) {
        container.getChildren().clear();
        container.setAlignment(Pos.CENTER);
        container.setSpacing(20);

        Label titleLabel = new Label("Maintenance System");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        titleLabel.setTextFill(Color.web("#667eea"));

        Label subtitleLabel = new Label("Sign in to continue");
        subtitleLabel.setFont(Font.font("Arial", 14));
        subtitleLabel.setTextFill(Color.GRAY);

        Label messageLabel = new Label();
        messageLabel.setFont(Font.font("Arial", 12));
        messageLabel.setTextFill(Color.web("#2e7d32"));
        messageLabel.setWrapText(true);
        messageLabel.setVisible(successMessage != null);
        messageLabel.setManaged(successMessage != null);
        if (successMessage != null) {
            messageLabel.setText(successMessage);
        }

        Label usernameLabel = new Label("Username");
        usernameLabel.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 13));
        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter username");
        usernameField.setMaxWidth(Double.MAX_VALUE);
        usernameField.setStyle("-fx-padding: 10; -fx-background-radius: 5; -fx-border-color: #ddd; -fx-border-radius: 5;");

        Label passwordLabel = new Label("Password");
        passwordLabel.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 13));
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter password");
        passwordField.setMaxWidth(Double.MAX_VALUE);
        passwordField.setStyle("-fx-padding: 10; -fx-background-radius: 5; -fx-border-color: #ddd; -fx-border-radius: 5;");

        Label errorLabel = new Label();
        errorLabel.setTextFill(Color.RED);
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        Button loginButton = new Button("LOGIN");
        loginButton.setDefaultButton(true);
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
                errorLabel.setManaged(true);
                messageLabel.setVisible(false);
                messageLabel.setManaged(false);
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
                errorLabel.setManaged(true);
                messageLabel.setVisible(false);
                messageLabel.setManaged(false);
            }
        });

        Label infoLabel = new Label("Demo: tenant1/pass123, manager1/pass123, staff1/pass123");
        infoLabel.setFont(Font.font("Arial", 10));
        infoLabel.setTextFill(Color.GRAY);
        infoLabel.setWrapText(true);
        infoLabel.setAlignment(Pos.CENTER);

        Hyperlink createAccountLink = new Hyperlink("Create an account");
        createAccountLink.setFont(Font.font("Arial", 12));
        createAccountLink.setOnAction(e -> showRegistrationView(container));

        VBox userBox = new VBox(5, usernameLabel, usernameField);
        VBox passBox = new VBox(5, passwordLabel, passwordField);
        userBox.setMaxWidth(Double.MAX_VALUE);
        passBox.setMaxWidth(Double.MAX_VALUE);

        container.getChildren().addAll(
                titleLabel,
                subtitleLabel,
                messageLabel,
                userBox,
                passBox,
                errorLabel,
                loginButton,
                infoLabel,
                createAccountLink
        );
    }

    private void showRegistrationView(VBox container) {
        container.getChildren().clear();
        container.setAlignment(Pos.TOP_CENTER);
        container.setSpacing(15);

        String fieldStyle = "-fx-padding: 10; -fx-background-radius: 5; -fx-border-color: #ddd; -fx-border-radius: 5;";

        Label titleLabel = new Label("Create Account");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 26));
        titleLabel.setTextFill(Color.web("#667eea"));

        Label subtitleLabel = new Label("Tell us about yourself to get started");
        subtitleLabel.setFont(Font.font("Arial", 13));
        subtitleLabel.setTextFill(Color.GRAY);

        Label errorLabel = new Label();
        errorLabel.setTextFill(Color.RED);
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        errorLabel.setWrapText(true);

        TextField usernameField = new TextField();
        usernameField.setPromptText("Choose a username");
        usernameField.setStyle(fieldStyle);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Create a password");
        passwordField.setStyle(fieldStyle);

        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm password");
        confirmPasswordField.setStyle(fieldStyle);

        TextField firstNameField = new TextField();
        firstNameField.setPromptText("First name");
        firstNameField.setStyle(fieldStyle);

        TextField lastNameField = new TextField();
        lastNameField.setPromptText("Last name");
        lastNameField.setStyle(fieldStyle);

        TextField emailField = new TextField();
        emailField.setPromptText("Email address");
        emailField.setStyle(fieldStyle);

        TextField phoneField = new TextField();
        phoneField.setPromptText("Phone number");
        phoneField.setStyle(fieldStyle);

        CheckBox tenantCheck = new CheckBox("Tenant");
        CheckBox managerCheck = new CheckBox("Manager");
        CheckBox staffCheck = new CheckBox("Maintenance Staff");

        HBox userTypeRow = new HBox(10, tenantCheck, managerCheck, staffCheck);
        userTypeRow.setAlignment(Pos.CENTER_LEFT);

        // Tenant specific
        TextField apartmentField = new TextField();
        apartmentField.setPromptText("Apartment number");
        apartmentField.setStyle(fieldStyle);

        DatePicker leaseStartPicker = new DatePicker();
        leaseStartPicker.setPromptText("Lease start date");
        leaseStartPicker.setStyle(fieldStyle);

        DatePicker leaseEndPicker = new DatePicker();
        leaseEndPicker.setPromptText("Lease end date");
        leaseEndPicker.setStyle(fieldStyle);

        TextField emergencyContactField = new TextField();
        emergencyContactField.setPromptText("Emergency contact (optional)");
        emergencyContactField.setStyle(fieldStyle);

        TextField emergencyPhoneField = new TextField();
        emergencyPhoneField.setPromptText("Emergency phone (optional)");
        emergencyPhoneField.setStyle(fieldStyle);

        VBox tenantSection = new VBox(8,
                new Label("Tenant details"),
                apartmentField,
                leaseStartPicker,
                leaseEndPicker,
                emergencyContactField,
                emergencyPhoneField
        );
        tenantSection.setVisible(false);
        tenantSection.setManaged(false);

        // Manager specific
        TextField employeeIdField = new TextField();
        employeeIdField.setPromptText("Employee ID");
        employeeIdField.setStyle(fieldStyle);

        TextField departmentField = new TextField();
        departmentField.setPromptText("Department");
        departmentField.setStyle(fieldStyle);

        VBox managerSection = new VBox(8,
                new Label("Manager details"),
                employeeIdField,
                departmentField
        );
        managerSection.setVisible(false);
        managerSection.setManaged(false);

        // Staff specific
        TextField staffIdField = new TextField();
        staffIdField.setPromptText("Staff ID");
        staffIdField.setStyle(fieldStyle);

        TextField specializationField = new TextField();
        specializationField.setPromptText("Specializations (comma separated)");
        specializationField.setStyle(fieldStyle);

        Spinner<Integer> capacitySpinner = new Spinner<>(1, 50, 10);
        capacitySpinner.setEditable(true);
        capacitySpinner.setMaxWidth(Double.MAX_VALUE);

        VBox staffSection = new VBox(8,
                new Label("Maintenance staff details"),
                staffIdField,
                specializationField,
                capacitySpinner
        );
        staffSection.setVisible(false);
        staffSection.setManaged(false);

        final String[] selectedType = {null};

        Runnable updateSections = () -> {
            if (tenantCheck.isSelected()) {
                selectedType[0] = "TENANT";
            } else if (managerCheck.isSelected()) {
                selectedType[0] = "MANAGER";
            } else if (staffCheck.isSelected()) {
                selectedType[0] = "STAFF";
            } else {
                selectedType[0] = null;
            }

            setSectionVisibility(tenantSection, tenantCheck.isSelected());
            setSectionVisibility(managerSection, managerCheck.isSelected());
            setSectionVisibility(staffSection, staffCheck.isSelected());
        };

        tenantCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                managerCheck.setSelected(false);
                staffCheck.setSelected(false);
            }
            updateSections.run();
        });
        managerCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                tenantCheck.setSelected(false);
                staffCheck.setSelected(false);
            }
            updateSections.run();
        });
        staffCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                tenantCheck.setSelected(false);
                managerCheck.setSelected(false);
            }
            updateSections.run();
        });

        VBox formFields = new VBox(12,
                new Label("Account information"),
                usernameField,
                passwordField,
                confirmPasswordField,
                new Label("Contact details"),
                firstNameField,
                lastNameField,
                emailField,
                phoneField,
                new Label("Select your user type"),
                userTypeRow,
                tenantSection,
                managerSection,
                staffSection
        );
        formFields.setFillWidth(true);

        ScrollPane scrollPane = new ScrollPane(formFields);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setPrefViewportHeight(360);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        Button registerButton = new Button("CREATE ACCOUNT");
        registerButton.setMaxWidth(Double.MAX_VALUE);
        registerButton.setStyle(
                "-fx-background-color: #667eea; -fx-text-fill: white; -fx-font-size: 14; " +
                        "-fx-font-weight: bold; -fx-padding: 12; -fx-background-radius: 5; -fx-cursor: hand;"
        );

        registerButton.setOnMouseEntered(e ->
                registerButton.setStyle(
                        "-fx-background-color: #5568d3; -fx-text-fill: white; -fx-font-size: 14; " +
                                "-fx-font-weight: bold; -fx-padding: 12; -fx-background-radius: 5; -fx-cursor: hand;"
                )
        );
        registerButton.setOnMouseExited(e ->
                registerButton.setStyle(
                        "-fx-background-color: #667eea; -fx-text-fill: white; -fx-font-size: 14; " +
                                "-fx-font-weight: bold; -fx-padding: 12; -fx-background-radius: 5; -fx-cursor: hand;"
                )
        );

        registerButton.setOnAction(e -> {
            errorLabel.setTextFill(Color.RED);
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);

            String username = usernameField.getText().trim();
            String password = passwordField.getText();
            String confirmPassword = confirmPasswordField.getText();
            String firstName = firstNameField.getText().trim();
            String lastName = lastNameField.getText().trim();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();

            if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() ||
                    firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || phone.isEmpty()) {
                showRegistrationError(errorLabel, "Please complete all required fields.");
                return;
            }

            if (!password.equals(confirmPassword)) {
                showRegistrationError(errorLabel, "Passwords do not match.");
                return;
            }

            if (selectedType[0] == null) {
                showRegistrationError(errorLabel, "Select a user type to continue.");
                return;
            }

            if (authService.isUsernameTaken(username)) {
                showRegistrationError(errorLabel, "That username is already in use.");
                return;
            }

            UserRegistrationData registrationData = new UserRegistrationData();
            registrationData.setUsername(username);
            registrationData.setPassword(password);
            registrationData.setFirstName(firstName);
            registrationData.setLastName(lastName);
            registrationData.setEmail(email);
            registrationData.setPhoneNumber(phone);
            registrationData.setUserType(selectedType[0]);

            switch (selectedType[0]) {
                case "TENANT":
                    String apartment = apartmentField.getText().trim();
                    if (apartment.isEmpty()) {
                        showRegistrationError(errorLabel, "Please provide an apartment number.");
                        return;
                    }

                    if (leaseStartPicker.getValue() != null && leaseEndPicker.getValue() != null &&
                            leaseEndPicker.getValue().isBefore(leaseStartPicker.getValue())) {
                        showRegistrationError(errorLabel, "Lease end date cannot be before the start date.");
                        return;
                    }

                    registrationData.setApartmentNumber(apartment);
                    registrationData.setLeaseStartDate(leaseStartPicker.getValue());
                    registrationData.setLeaseEndDate(leaseEndPicker.getValue());
                    registrationData.setEmergencyContact(emergencyContactField.getText().trim().isEmpty() ? null : emergencyContactField.getText().trim());
                    registrationData.setEmergencyPhone(emergencyPhoneField.getText().trim().isEmpty() ? null : emergencyPhoneField.getText().trim());
                    break;
                case "MANAGER":
                    String employeeId = employeeIdField.getText().trim();
                    String department = departmentField.getText().trim();
                    if (employeeId.isEmpty() || department.isEmpty()) {
                        showRegistrationError(errorLabel, "Provide both employee ID and department.");
                        return;
                    }
                    registrationData.setEmployeeId(employeeId);
                    registrationData.setDepartment(department);
                    break;
                case "STAFF":
                    String staffId = staffIdField.getText().trim();
                    if (staffId.isEmpty()) {
                        showRegistrationError(errorLabel, "Staff ID is required for maintenance staff.");
                        return;
                    }
                    registrationData.setStaffId(staffId);
                    registrationData.setSpecializations(specializationField.getText().trim());
                    registrationData.setMaxCapacity(capacitySpinner.getValue());
                    break;
                default:
                    break;
            }

            if (authService.registerUser(registrationData)) {
                showLoginView(container, "Account created successfully! You can now log in.");
            } else {
                showRegistrationError(errorLabel, "We couldn't create your account. Please try again.");
            }
        });

        Hyperlink backToLogin = new Hyperlink("Back to login");
        backToLogin.setFont(Font.font("Arial", 12));
        backToLogin.setOnAction(e -> showLoginView(container, null));

        container.getChildren().addAll(
                titleLabel,
                subtitleLabel,
                scrollPane,
                errorLabel,
                registerButton,
                backToLogin
        );
    }

    private void showRegistrationError(Label errorLabel, String message) {
        errorLabel.setText(message);
        errorLabel.setTextFill(Color.RED);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void setSectionVisibility(VBox section, boolean visible) {
        section.setVisible(visible);
        section.setManaged(visible);
    }
}
