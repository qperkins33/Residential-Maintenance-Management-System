package com.maintenance.ui.controllers;

import com.maintenance.dao.MaintenanceRequestDAO;
import com.maintenance.database.DatabaseManager;
import com.maintenance.enums.RequestStatus;
import com.maintenance.models.Admin;
import com.maintenance.models.MaintenanceRequest;
import com.maintenance.service.AuthenticationService;
import com.maintenance.ui.views.ViewFactory;
import com.maintenance.util.IDGenerator;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class AdminDashboardController {

    private final ViewFactory viewFactory;
    private final AuthenticationService authService;
    private final DatabaseManager dbManager;
    private final MaintenanceRequestDAO requestDAO;

    private TableView<UserRow> userTable;
    private HBox statsBox;

    public AdminDashboardController(ViewFactory viewFactory) {
        this.viewFactory = viewFactory;
        this.authService = AuthenticationService.getInstance();
        this.dbManager = DatabaseManager.getInstance();
        this.requestDAO = new MaintenanceRequestDAO();
    }

    public void createDashboardUI(AnchorPane root) {
        DashboardUIHelper.applyRootStyles(root, getClass());

        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(0));

        HBox topBar = createTopBar();
        mainLayout.setTop(topBar);

//        VBox sidebar = createSidebar();
//        mainLayout.setLeft(sidebar);

        VBox centerContent = createCenterContent();
        VBox.setVgrow(centerContent, Priority.ALWAYS);
        mainLayout.setCenter(centerContent);

        AnchorPane.setTopAnchor(mainLayout, 0.0);
        AnchorPane.setBottomAnchor(mainLayout, 0.0);
        AnchorPane.setLeftAnchor(mainLayout, 0.0);
        AnchorPane.setRightAnchor(mainLayout, 0.0);

        root.getChildren().add(mainLayout);
    }

    private HBox createTopBar() {
        HBox topBar = new HBox(20);
        topBar.setPadding(new Insets(15, 30, 15, 30));
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.getStyleClass().add("top-bar");

        Label titleLabel = new Label("🔐 Admin Dashboard");
        titleLabel.getStyleClass().add("top-bar-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Admin admin = (Admin) authService.getCurrentUser();
        String name = admin != null ? admin.getFullName() : "Admin";
        Label userLabel = new Label("👤 " + name + " (Admin)");
        userLabel.setFont(Font.font("Arial", 14));

        Button logoutButton = new Button("Logout");
        logoutButton.setStyle(
                "-fx-background-color: #ff5252; -fx-text-fill: white; " +
                        "-fx-padding: 8 20; -fx-background-radius: 5; -fx-cursor: hand;"
        );
        logoutButton.setOnAction(e -> {
            authService.logout();
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            viewFactory.closeStage(stage);
            viewFactory.showLoginWindow();
        });

        topBar.getChildren().addAll(titleLabel, spacer, userLabel, logoutButton);
        return topBar;
    }

//    private VBox createSidebar() {
//        VBox sidebar = new VBox(15);
//        sidebar.setPadding(new Insets(20));
//        sidebar.setStyle("-fx-background-color: #2c3e50;");
//        sidebar.setPrefWidth(250);
//
//        Label menuLabel = new Label("MENU");
//        menuLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
//        menuLabel.setTextFill(javafx.scene.paint.Color.web("#95a5a6"));
//
//        Button dashboardBtn = DashboardUIHelper.createSidebarButton("📊 Dashboard", true);
////        Button usersBtn = DashboardUIHelper.createSidebarButton("👥 Users", false);
////        Button settingsBtn = DashboardUIHelper.createSidebarButton("⚙️ Settings", false);
//
//        sidebar.getChildren().addAll(menuLabel, dashboardBtn);
//        return sidebar;
//    }

    private VBox createCenterContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        content.setFillWidth(true);

        statsBox = new HBox(20);

        VBox usersSection = createUsersSection();
        VBox.setVgrow(usersSection, Priority.ALWAYS);

        content.getChildren().addAll(statsBox, usersSection);

        loadUsers();

        return content;
    }

    private VBox createUsersSection() {
        VBox section = new VBox(15);
        section.setFillWidth(true);

        HBox headerBox = new HBox(20);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label sectionTitle = new Label("All Users");
        sectionTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button newUserBtn = new Button("+ New User");
        newUserBtn.setStyle(
                "-fx-background-color: #667eea; -fx-text-fill: white; " +
                        "-fx-padding: 10 20; -fx-background-radius: 5; -fx-cursor: hand;"
        );
        newUserBtn.setOnAction(e -> showCreateUserDialog());

        headerBox.getChildren().addAll(sectionTitle, spacer, newUserBtn);

        userTable = new TableView<>();
        userTable.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        userTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        userTable.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(userTable, Priority.ALWAYS);

        TableColumn<UserRow, String> idCol = new TableColumn<>("User ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("userId"));
        idCol.setPrefWidth(120);

        TableColumn<UserRow, String> usernameCol = new TableColumn<>("Username");
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        usernameCol.setPrefWidth(120);

        TableColumn<UserRow, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        nameCol.setPrefWidth(160);

        TableColumn<UserRow, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailCol.setPrefWidth(180);

        TableColumn<UserRow, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        phoneCol.setPrefWidth(120);

        TableColumn<UserRow, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("userType"));
        typeCol.setPrefWidth(100);

        TableColumn<UserRow, String> activeCol = new TableColumn<>("Active");
        activeCol.setCellValueFactory(new PropertyValueFactory<>("activeText"));
        activeCol.setPrefWidth(80);

        TableColumn<UserRow, String> dateCol = new TableColumn<>("Created");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("dateCreated"));
        dateCol.setPrefWidth(120);

        TableColumn<UserRow, Void> actionCol = getUserRowVoidTableColumn();

        userTable.getColumns().setAll(List.of(
                idCol,
                usernameCol,
                nameCol,
                emailCol,
                phoneCol,
                typeCol,
                activeCol,
                dateCol,
                actionCol
        ));

        Label emptyLabel = new Label("No users found");
        emptyLabel.setFont(Font.font("Arial", 14));
        emptyLabel.setTextFill(javafx.scene.paint.Color.GRAY);
        userTable.setPlaceholder(emptyLabel);

        section.getChildren().addAll(headerBox, userTable);
        VBox.setVgrow(userTable, Priority.ALWAYS);
        return section;
    }

    private TableColumn<UserRow, Void> getUserRowVoidTableColumn() {
        TableColumn<UserRow, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setPrefWidth(210);
        actionCol.setStyle("-fx-alignment: CENTER;");
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button viewBtn = new Button("View");
            private final Button editBtn = new Button("Edit");
            private final HBox box = new HBox(5, editBtn, viewBtn);

            {
                box.setAlignment(Pos.CENTER);
                String btnStyle =
                        "-fx-background-color: #667eea; -fx-text-fill: white; " +
                                "-fx-padding: 5 12; -fx-background-radius: 3; -fx-cursor: hand;";
                viewBtn.setStyle(btnStyle);
                editBtn.setStyle(btnStyle);

                viewBtn.setOnAction(e -> {
                    UserRow row = getTableView().getItems().get(getIndex());
                    if (row != null) {
                        showUserDetailsDialog(row);
                    }
                });

                editBtn.setOnAction(e -> {
                    UserRow row = getTableView().getItems().get(getIndex());
                    if (row != null) {
                        showEditUserDialog(row);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(box);
                }
            }
        });
        return actionCol;
    }

    private void loadUsers() {
        List<UserRow> users = fetchAllUsers();
        userTable.setItems(FXCollections.observableArrayList(users));
        refreshStats(users);
    }

    private List<UserRow> fetchAllUsers() {
        List<UserRow> list = new ArrayList<>();
        Connection conn = dbManager.getConnection();
        if (conn == null) {
            System.err.println("Database connection is null in AdminDashboardController.fetchAllUsers");
            return list;
        }

        String sql = "SELECT user_id, username, first_name, last_name, " +
                "email, phone_number, user_type, date_created, is_active " +
                "FROM users";

        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM/dd/yyyy");

            while (rs.next()) {
                String userId = rs.getString("user_id");
                String username = rs.getString("username");
                String firstName = rs.getString("first_name");
                String lastName = rs.getString("last_name");
                String email = rs.getString("email");
                String phone = rs.getString("phone_number");
                String userType = rs.getString("user_type");
                boolean active = rs.getBoolean("is_active");

                Timestamp ts = rs.getTimestamp("date_created");
                String created = "";
                if (ts != null) {
                    created = ts.toLocalDateTime().format(fmt);
                }

                String fullName = ((firstName != null ? firstName : "") + " " +
                        (lastName != null ? lastName : "")).trim();

                list.add(new UserRow(
                        userId,
                        username,
                        fullName,
                        email,
                        phone,
                        userType,
                        active,
                        created
                ));
            }

        } catch (SQLException e) {
            System.err.println("Error fetching users: " + e.getMessage());
        }

        return list;
    }

    private void refreshStats(List<UserRow> users) {
        statsBox.getChildren().clear();

        int total = users.size();
        long tenants = users.stream().filter(u -> "TENANT".equalsIgnoreCase(u.getUserType())).count();
        long staff = users.stream().filter(u -> "STAFF".equalsIgnoreCase(u.getUserType())).count();
        long managers = users.stream().filter(u -> "MANAGER".equalsIgnoreCase(u.getUserType())).count();
        long admins = users.stream().filter(u -> "ADMIN".equalsIgnoreCase(u.getUserType())).count();
        long inactive = users.stream().filter(u -> !u.isActive()).count();

        VBox totalCard = DashboardUIHelper.createStatCard("Total Users",
                String.valueOf(total), "#667eea", "👥");
        VBox tenantCard = DashboardUIHelper.createStatCard("Tenants",
                String.valueOf(tenants), "#2196f3", "🏠");
        VBox staffCard = DashboardUIHelper.createStatCard("Staff",
                String.valueOf(staff), "#ff9800", "🛠");
        VBox managerCard = DashboardUIHelper.createStatCard("Managers",
                String.valueOf(managers), "#4caf50", "📋");
        VBox adminCard = DashboardUIHelper.createStatCard("Admins",
                String.valueOf(admins), "#9c27b0", "🔐");
        VBox inactiveCard = DashboardUIHelper.createStatCard("Inactive",
                String.valueOf(inactive), "#f44336", "⏸");

        statsBox.getChildren().addAll(
                totalCard, tenantCard, staffCard, managerCard, adminCard, inactiveCard
        );
    }

    private void showCreateUserDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Create New User");
        dialog.setHeaderText("Enter user details");

        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        GridPane baseGrid = new GridPane();
        baseGrid.setHgap(10);
        baseGrid.setVgap(10);
        baseGrid.setPadding(new Insets(20));

        TextField userIdField = new TextField();
        userIdField.setPromptText("Leave blank for auto");

        TextField usernameField = new TextField();
        usernameField.setPromptText("username");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("password");

        TextField firstNameField = new TextField();
        firstNameField.setPromptText("First name");

        TextField lastNameField = new TextField();
        lastNameField.setPromptText("Last name");

        TextField emailField = new TextField();
        emailField.setPromptText("Email");

        TextField phoneField = new TextField();
        phoneField.setPromptText("Phone");

        ComboBox<String> userTypeBox = new ComboBox<>();
        userTypeBox.getItems().addAll("TENANT", "STAFF", "MANAGER", "ADMIN");
        userTypeBox.setPromptText("Select user type");

        CheckBox activeCheckBox = new CheckBox("Active");
        activeCheckBox.setSelected(true);

        int row = 0;
        baseGrid.add(new Label("User ID:"), 0, row);
        baseGrid.add(userIdField, 1, row++);
        baseGrid.add(new Label("Username:"), 0, row);
        baseGrid.add(usernameField, 1, row++);
        baseGrid.add(new Label("Password:"), 0, row);
        baseGrid.add(passwordField, 1, row++);
        baseGrid.add(new Label("First Name:"), 0, row);
        baseGrid.add(firstNameField, 1, row++);
        baseGrid.add(new Label("Last Name:"), 0, row);
        baseGrid.add(lastNameField, 1, row++);
        baseGrid.add(new Label("Email:"), 0, row);
        baseGrid.add(emailField, 1, row++);
        baseGrid.add(new Label("Phone:"), 0, row);
        baseGrid.add(phoneField, 1, row++);
        baseGrid.add(new Label("User Type:"), 0, row);
        baseGrid.add(userTypeBox, 1, row++);
        baseGrid.add(new Label("Status:"), 0, row);
        baseGrid.add(activeCheckBox, 1, row);

        // Tenant specific section
        GridPane tenantGrid = new GridPane();
        tenantGrid.setHgap(10);
        tenantGrid.setVgap(10);

        TextField aptField = new TextField();
        aptField.setPromptText("A101");

        DatePicker leaseStartPicker = new DatePicker();
        DatePicker leaseEndPicker = new DatePicker();

        TextField emergencyContactField = new TextField();
        emergencyContactField.setPromptText("Contact name");

        TextField emergencyPhoneField = new TextField();
        emergencyPhoneField.setPromptText("Contact phone");

        int tRow = 0;
        tenantGrid.add(new Label("Apartment Number:"), 0, tRow);
        tenantGrid.add(aptField, 1, tRow++);
        tenantGrid.add(new Label("Lease Start:"), 0, tRow);
        tenantGrid.add(leaseStartPicker, 1, tRow++);
        tenantGrid.add(new Label("Lease End:"), 0, tRow);
        tenantGrid.add(leaseEndPicker, 1, tRow++);
        tenantGrid.add(new Label("Emergency Contact:"), 0, tRow);
        tenantGrid.add(emergencyContactField, 1, tRow++);
        tenantGrid.add(new Label("Emergency Phone:"), 0, tRow);
        tenantGrid.add(emergencyPhoneField, 1, tRow);

        VBox tenantSection = new VBox(8, new Label("Tenant Details"), tenantGrid);
        tenantSection.setPadding(new Insets(10, 0, 0, 0));

        // Staff specific section
        GridPane staffGrid = new GridPane();
        staffGrid.setHgap(10);
        staffGrid.setVgap(10);

        TextField staffIdField = new TextField();
        staffIdField.setPromptText("STF001");

        TextField specializationsField = new TextField();
        specializationsField.setPromptText("Plumbing,Electrical");

        Spinner<Integer> maxCapacitySpinner = new Spinner<>(1, 100, 10);

        int sRow = 0;
        staffGrid.add(new Label("Staff ID:"), 0, sRow);
        staffGrid.add(staffIdField, 1, sRow++);
        staffGrid.add(new Label("Specializations:"), 0, sRow);
        staffGrid.add(specializationsField, 1, sRow++);
        staffGrid.add(new Label("Max Capacity:"), 0, sRow);
        staffGrid.add(maxCapacitySpinner, 1, sRow);

        VBox staffSection = new VBox(8, new Label("Staff Details"), staffGrid);
        staffSection.setPadding(new Insets(10, 0, 0, 0));

        // Manager specific section
        GridPane managerGrid = new GridPane();
        managerGrid.setHgap(10);
        managerGrid.setVgap(10);

        TextField employeeIdField = new TextField();
        employeeIdField.setPromptText("EMP001");

        TextField departmentField = new TextField();
        departmentField.setPromptText("Operations");

        int mRow = 0;
        managerGrid.add(new Label("Employee ID:"), 0, mRow);
        managerGrid.add(employeeIdField, 1, mRow++);
        managerGrid.add(new Label("Department:"), 0, mRow);
        managerGrid.add(departmentField, 1, mRow);

        VBox managerSection = new VBox(8, new Label("Manager Details"), managerGrid);
        managerSection.setPadding(new Insets(10, 0, 0, 0));

        // Initially hide type sections
        tenantSection.setVisible(false);
        tenantSection.setManaged(false);
        staffSection.setVisible(false);
        staffSection.setManaged(false);
        managerSection.setVisible(false);
        managerSection.setManaged(false);

        userTypeBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            boolean isTenant = "TENANT".equals(newVal);
            boolean isStaff = "STAFF".equals(newVal);
            boolean isManager = "MANAGER".equals(newVal);

            tenantSection.setVisible(isTenant);
            tenantSection.setManaged(isTenant);

            staffSection.setVisible(isStaff);
            staffSection.setManaged(isStaff);

            managerSection.setVisible(isManager);
            managerSection.setManaged(isManager);
        });

        VBox container = new VBox(15);
        container.setPadding(new Insets(10));
        container.getChildren().addAll(baseGrid, tenantSection, staffSection, managerSection);

        ScrollPane scrollPane = new ScrollPane(container);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefViewportHeight(500);

        dialog.getDialogPane().setContent(scrollPane);

        Button createButton = (Button) dialog.getDialogPane().lookupButton(createButtonType);
        final boolean[] created = {false};

        createButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            String customUserId = userIdField.getText().trim();
            String username = usernameField.getText().trim();
            String password = passwordField.getText();
            String firstName = firstNameField.getText().trim();
            String lastName = lastNameField.getText().trim();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();
            String userType = userTypeBox.getValue();
            boolean active = activeCheckBox.isSelected();

            if (customUserId.length() > 50) {
                new Alert(Alert.AlertType.WARNING,
                        "User ID must be 50 characters or less.").showAndWait();
                event.consume();
                return;
            }

            if (username.isEmpty() || password.isEmpty() ||
                    firstName.isEmpty() || lastName.isEmpty() || userType == null) {
                new Alert(Alert.AlertType.WARNING,
                        "User ID (optional), username, password, first name, last name, and user type are required.")
                        .showAndWait();
                event.consume();
                return;
            }

            if (email.isEmpty()) {
                new Alert(Alert.AlertType.WARNING,
                        "Email is required.")
                        .showAndWait();
                event.consume();
                return;
            }
            if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
                new Alert(Alert.AlertType.WARNING,
                        "Please enter a valid email address.")
                        .showAndWait();
                event.consume();
                return;
            }

            if (phone.isEmpty()) {
                new Alert(Alert.AlertType.WARNING,
                        "Phone number is required.")
                        .showAndWait();
                event.consume();
                return;
            }
            if (!phone.matches("^[0-9()+\\-\\s]{7,20}$")) {
                new Alert(Alert.AlertType.WARNING,
                        "Please enter a valid phone number (digits and basic punctuation only).")
                        .showAndWait();
                event.consume();
                return;
            }

            switch (userType) {
                case "TENANT" -> {
                    if (aptField.getText().trim().isEmpty()) {
                        new Alert(Alert.AlertType.WARNING,
                                "Apartment number is required for tenants.")
                                .showAndWait();
                        event.consume();
                        return;
                    }
                }
                case "STAFF" -> {
                    if (staffIdField.getText().trim().isEmpty()) {
                        new Alert(Alert.AlertType.WARNING,
                                "Staff ID is required for maintenance staff.")
                                .showAndWait();
                        event.consume();
                        return;
                    }
                }
                case "MANAGER" -> {
                    if (employeeIdField.getText().trim().isEmpty()) {
                        new Alert(Alert.AlertType.WARNING,
                                "Employee ID is required for managers.")
                                .showAndWait();
                        event.consume();
                        return;
                    }
                }
            }

            boolean success = createUserInDatabase(
                    customUserId,
                    username,
                    password,
                    firstName,
                    lastName,
                    email,
                    phone,
                    userType,
                    active,
                    aptField.getText().trim(),
                    leaseStartPicker.getValue(),
                    leaseEndPicker.getValue(),
                    emergencyContactField.getText().trim(),
                    emergencyPhoneField.getText().trim(),
                    staffIdField.getText().trim(),
                    specializationsField.getText().trim(),
                    maxCapacitySpinner.getValue(),
                    employeeIdField.getText().trim(),
                    departmentField.getText().trim()
            );

            if (!success) {
                new Alert(Alert.AlertType.ERROR,
                        "Unable to create user. Check logs and ensure User ID / Username are unique.")
                        .showAndWait();
                event.consume();
            } else {
                created[0] = true;
            }
        });

        dialog.showAndWait();

        if (created[0]) {
            loadUsers();
            new Alert(Alert.AlertType.INFORMATION,
                    "User created successfully.")
                    .showAndWait();
        }
    }

    private boolean createUserInDatabase(
            String customUserId,
            String username,
            String password,
            String firstName,
            String lastName,
            String email,
            String phone,
            String userType,
            boolean active,
            String apartmentNumber,
            LocalDate leaseStart,
            LocalDate leaseEnd,
            String emergencyContact,
            String emergencyPhone,
            String staffId,
            String specializations,
            Integer maxCapacity,
            String employeeId,
            String department
    ) {
        Connection conn = dbManager.getConnection();
        if (conn == null) {
            System.err.println("Database connection is null in createUserInDatabase");
            return false;
        }

        String userId;
        if (customUserId != null && !customUserId.isBlank()) {
            userId = customUserId;
        } else {
            userId = IDGenerator.generateUserId();
        }

        try {
            conn.setAutoCommit(false);

            String userSql = "INSERT INTO users (" +
                    "user_id, username, password, first_name, last_name, " +
                    "email, phone_number, user_type, date_created, is_active) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?)";

            try (PreparedStatement userStmt = conn.prepareStatement(userSql)) {
                userStmt.setString(1, userId);
                userStmt.setString(2, username);
                userStmt.setString(3, password);
                userStmt.setString(4, firstName);
                userStmt.setString(5, lastName);
                userStmt.setString(6, email);
                userStmt.setString(7, phone);
                userStmt.setString(8, userType);
                userStmt.setBoolean(9, active);
                userStmt.executeUpdate();
            }

            if ("TENANT".equals(userType)) {
                String tenantSql = "INSERT INTO tenants (" +
                        "user_id, apartment_number, lease_start_date, lease_end_date, " +
                        "emergency_contact, emergency_phone) " +
                        "VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement tenantStmt = conn.prepareStatement(tenantSql)) {
                    tenantStmt.setString(1, userId);
                    tenantStmt.setString(2, apartmentNumber);
                    if (leaseStart != null) {
                        tenantStmt.setDate(3, Date.valueOf(leaseStart));
                    } else {
                        tenantStmt.setDate(3, null);
                    }
                    if (leaseEnd != null) {
                        tenantStmt.setDate(4, Date.valueOf(leaseEnd));
                    } else {
                        tenantStmt.setDate(4, null);
                    }
                    tenantStmt.setString(5, emergencyContact);
                    tenantStmt.setString(6, emergencyPhone);
                    tenantStmt.executeUpdate();
                }
            } else if ("STAFF".equals(userType)) {
                String staffSql = "INSERT INTO maintenance_staff (" +
                        "user_id, staff_id, specializations, current_workload, " +
                        "max_capacity, is_available) " +
                        "VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement staffStmt = conn.prepareStatement(staffSql)) {
                    staffStmt.setString(1, userId);
                    staffStmt.setString(2, staffId);
                    staffStmt.setString(3, specializations != null ? specializations : "");
                    staffStmt.setInt(4, 0);
                    staffStmt.setInt(5, maxCapacity != null ? maxCapacity : 10);
                    staffStmt.setBoolean(6, true);
                    staffStmt.executeUpdate();
                }
            } else if ("MANAGER".equals(userType)) {
                String managerSql = "INSERT INTO building_managers (" +
                        "user_id, employee_id, department) " +
                        "VALUES (?, ?, ?)";
                try (PreparedStatement mgrStmt = conn.prepareStatement(managerSql)) {
                    mgrStmt.setString(1, userId);
                    mgrStmt.setString(2, employeeId);
                    mgrStmt.setString(3, department != null ? department : "Operations");

                    mgrStmt.executeUpdate();
                }
            }  // no extra table for admins


            conn.commit();
            return true;

        } catch (SQLException e) {
            System.err.println("Error creating user: " + e.getMessage());
            try {
                conn.rollback();
            } catch (SQLException ex) {
                System.err.println("Error rolling back transaction: " + ex.getMessage());
            }
            return false;
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("Error resetting autoCommit: " + e.getMessage());
            }
        }
    }

    /**
     * Edit User dialog: only activate/deactivate toggle.
     */
    private void showEditUserDialog(UserRow row) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Edit User");
        dialog.setHeaderText("Edit user: " + row.getFullName());

        ButtonType saveBtnType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtnType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        Label statusLabel = new Label("Account status:");

        CheckBox toggle = new CheckBox();
        boolean originalActive = row.isActive();
        final boolean[] targetActive = {originalActive};

        Admin currentAdmin = (Admin) authService.getCurrentUser();
        final String currentUserId = currentAdmin != null ? currentAdmin.getUserId() : null;

        if (originalActive) {
            toggle.setText("Deactivate user");
            DashboardUIHelper.styleActionToggleButton(
                    toggle,
                    "#e53935",
                    "#d32f2f",
                    "#c62828"
            );
        } else {
            toggle.setText("Activate user");
            DashboardUIHelper.styleActionToggleButton(
                    toggle,
                    "#4caf50",
                    "#43a047",
                    "#388e3c"
            );
        }

        final boolean[] internalChange = {false};

        toggle.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            if (internalChange[0]) {
                return;
            }

            if (row.getUserId().equals(currentUserId)) {
                new Alert(
                        Alert.AlertType.WARNING,
                        "You cannot change your own active status from this screen."
                ).showAndWait();

                internalChange[0] = true;
                toggle.setSelected(wasSelected);
                internalChange[0] = false;
                return;
            }

            if (originalActive) {
                targetActive[0] = !isSelected;
            } else {
                targetActive[0] = isSelected;
            }
        });

        String existingFullName = row.getFullName() != null ? row.getFullName() : "";
        String[] nameParts = existingFullName.split(" ", 2);

        TextField firstNameField = new TextField(
                nameParts.length > 0 ? nameParts[0] : ""
        );
        firstNameField.setPromptText("First Name");

        TextField lastNameField = new TextField(
                nameParts.length > 1 ? nameParts[1] : ""
        );
        lastNameField.setPromptText("Last Name");

        TextField emailField = new TextField(
                row.getEmail() != null ? row.getEmail() : ""
        );
        emailField.setPromptText("Email");

        TextField phoneField = new TextField(
                row.getPhoneNumber() != null ? row.getPhoneNumber() : ""
        );
        phoneField.setPromptText("Phone");

        int r = 0;
        grid.add(statusLabel, 0, r);
        grid.add(toggle, 1, r++);

        grid.add(new Label("First Name"), 0, r);
        grid.add(firstNameField, 1, r++);

        grid.add(new Label("Last Name"), 0, r);
        grid.add(lastNameField, 1, r++);

        grid.add(new Label("Email"), 0, r);
        grid.add(emailField, 1, r++);

        grid.add(new Label("Phone"), 0, r);
        grid.add(phoneField, 1, r);

        dialog.getDialogPane().setContent(grid);

        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveBtnType);
        final boolean[] updated = {false};

        saveButton.addEventFilter(ActionEvent.ACTION, event -> {
            String newFirst = firstNameField.getText().trim();
            String newLast = lastNameField.getText().trim();
            String newEmail = emailField.getText().trim();
            String newPhone = phoneField.getText().trim();

            if (newFirst.isEmpty() || newLast.isEmpty()) {
                new Alert(
                        Alert.AlertType.WARNING,
                        "First and last name are required."
                ).showAndWait();
                event.consume();
                return;
            }

            if (newEmail.isEmpty()) {
                new Alert(
                        Alert.AlertType.WARNING,
                        "Email is required."
                ).showAndWait();
                event.consume();
                return;
            }

            if (!newEmail.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
                new Alert(
                        Alert.AlertType.WARNING,
                        "Please enter a valid email address."
                ).showAndWait();
                event.consume();
                return;
            }

            if (newPhone.isEmpty()) {
                new Alert(
                        Alert.AlertType.WARNING,
                        "Phone number is required."
                ).showAndWait();
                event.consume();
                return;
            }

            if (!newPhone.matches("^[0-9()+\\-\\s]{7,20}$")) {
                new Alert(
                        Alert.AlertType.WARNING,
                        "Please enter a valid phone number (digits and basic punctuation only)."
                ).showAndWait();
                event.consume();
                return;
            }

            boolean success = updateUserInfo(
                    row.getUserId(),
                    newFirst,
                    newLast,
                    newEmail,
                    newPhone,
                    targetActive[0]
            );

            if (!success) {
                new Alert(
                        Alert.AlertType.ERROR,
                        "Unable to update user. Please try again."
                ).showAndWait();
                event.consume();
            } else {
                updated[0] = true;
            }
        });

        dialog.showAndWait();

        if (updated[0]) {
            loadUsers();
            new Alert(
                    Alert.AlertType.INFORMATION,
                    "User details updated successfully."
            ).showAndWait();
        }
    }

    private boolean updateUserInfo(
            String userId,
            String firstName,
            String lastName,
            String email,
            String phone,
            boolean active
    ) {
        Connection conn = dbManager.getConnection();
        if (conn == null) {
            System.err.println("Database connection is null in updateUserInfo");
            return false;
        }

        String sql = "UPDATE users " +
                "SET first_name = ?, " +
                "    last_name = ?, " +
                "    email = ?, " +
                "    phone_number = ?, " +
                "    is_active = ? " +
                "WHERE user_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, firstName);
            ps.setString(2, lastName);
            ps.setString(3, email);
            ps.setString(4, phone);
            ps.setBoolean(5, active);
            ps.setString(6, userId);

            int updated = ps.executeUpdate();
            return updated == 1;
        } catch (SQLException e) {
            System.err.println("Error updating user info: " + e.getMessage());
            return false;
        }
    }

    private void showUserDetailsDialog(UserRow row) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("User Details");
        dialog.setHeaderText(row.getFullName() + " (" + row.getUserType() + ")");

        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        int r = 0;

        addUserDetailRow(grid, r++, "User ID:", row.getUserId());
        addUserDetailRow(grid, r++, "Username:", row.getUsername());
        addUserDetailRow(grid, r++, "Full Name:", row.getFullName());
        addUserDetailRow(grid, r++, "Email:", row.getEmail());
        addUserDetailRow(grid, r++, "Phone:", row.getPhoneNumber());
        addUserDetailRow(grid, r++, "Type:", row.getUserType());
        addUserDetailRow(grid, r++, "Active:", row.getActiveText());
        addUserDetailRow(grid, r++, "Created:", row.getDateCreated());

        String userType = row.getUserType();
        Connection conn = dbManager.getConnection();
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("MM/dd/yyyy");

        if (conn != null) {
            try {
                if ("TENANT".equalsIgnoreCase(userType)) {
                    String sql = "SELECT apartment_number, lease_start_date, lease_end_date, " +
                            "emergency_contact, emergency_phone FROM tenants WHERE user_id = ?";
                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setString(1, row.getUserId());
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                String apt = rs.getString("apartment_number");
                                Date leaseStart = rs.getDate("lease_start_date");
                                Date leaseEnd = rs.getDate("lease_end_date");
                                String emContact = rs.getString("emergency_contact");
                                String emPhone = rs.getString("emergency_phone");

                                addSectionLabel(grid, r++, "Tenant Details");
                                addUserDetailRow(grid, r++, "Apartment:", apt);
                                addUserDetailRow(grid, r++, "Lease Start:",
                                        leaseStart != null ? leaseStart.toLocalDate().format(dateFmt) : "");
                                addUserDetailRow(grid, r++, "Lease End:",
                                        leaseEnd != null ? leaseEnd.toLocalDate().format(dateFmt) : "");
                                addUserDetailRow(grid, r++, "Emergency Contact:", emContact);
                                addUserDetailRow(grid, r, "Emergency Phone:", emPhone);
                            }
                        }
                    }
                } else if ("STAFF".equalsIgnoreCase(userType)) {
                    String sql = "SELECT staff_id, specializations, max_capacity, is_available " +
                            "FROM maintenance_staff WHERE user_id = ?";
                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setString(1, row.getUserId());
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                String staffId = rs.getString("staff_id");
                                String specs = rs.getString("specializations");
                                int capacity = rs.getInt("max_capacity");
                                boolean available = rs.getBoolean("is_available");

                                int activeWorkload = 0;
                                if (staffId != null && !staffId.isBlank()) {
                                    List<MaintenanceRequest> staffRequests =
                                            requestDAO.getRequestsByStaff(staffId);
                                    activeWorkload = (int) staffRequests.stream()
                                            .filter(rq ->
                                                    rq.getStatus() != RequestStatus.COMPLETED
                                                            && rq.getStatus() != RequestStatus.CANCELLED
                                            )
                                            .count();
                                }

                                addSectionLabel(grid, r++, "Staff Details");
                                addUserDetailRow(grid, r++, "Staff ID:", staffId);
                                addUserDetailRow(grid, r++, "Specializations:", specs);
                                addUserDetailRow(grid, r++, "Current Workload:",
                                        activeWorkload + " / " + capacity);
                                addUserDetailRow(grid, r, "Available:",
                                        available ? "Yes" : "No");
                            }
                        }
                    }
                } else if ("MANAGER".equalsIgnoreCase(userType)) {
                    String sql = "SELECT employee_id, department " +
                            "FROM building_managers WHERE user_id = ?";
                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setString(1, row.getUserId());
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                String empId = rs.getString("employee_id");
                                String dept = rs.getString("department");

                                addSectionLabel(grid, r++, "Manager Details");
                                addUserDetailRow(grid, r++, "Employee ID:", empId);
                                addUserDetailRow(grid, r, "Department:", dept);
                            }
                        }
                    }
                }
            } catch (SQLException e) {
                System.err.println("Error loading user details for dialog: " + e.getMessage());
            }
        }

        ScrollPane scrollPane = new ScrollPane(grid);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefViewportHeight(450);

        dialog.getDialogPane().setContent(scrollPane);
        dialog.showAndWait();
    }

    private void addUserDetailRow(GridPane grid, int row, String label, String value) {
        Label l = new Label(label);
        l.setFont(Font.font("Arial", FontWeight.BOLD, 12));

        Label v = new Label(value == null ? "" : value);
        v.setFont(Font.font("Arial", 12));
        v.setWrapText(true);

        grid.add(l, 0, row);
        grid.add(v, 1, row);
    }

    private void addSectionLabel(GridPane grid, int row, String text) {
        Label section = new Label(text);
        section.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        section.setUnderline(true);
        GridPane.setColumnSpan(section, 2);
        grid.add(section, 0, row);
    }

    public static class UserRow {
        private final String userId;
        private final String username;
        private final String fullName;
        private final String email;
        private final String phoneNumber;
        private final String userType;
        private final boolean active;
        private final String dateCreated;

        public UserRow(String userId,
                       String username,
                       String fullName,
                       String email,
                       String phoneNumber,
                       String userType,
                       boolean active,
                       String dateCreated) {
            this.userId = userId;
            this.username = username;
            this.fullName = fullName;
            this.email = email;
            this.phoneNumber = phoneNumber;
            this.userType = userType;
            this.active = active;
            this.dateCreated = dateCreated;
        }

        public String getUserId() {
            return userId;
        }

        public String getUsername() {
            return username;
        }

        public String getFullName() {
            return fullName;
        }

        public String getEmail() {
            return email;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public String getUserType() {
            return userType;
        }

        public boolean isActive() {
            return active;
        }

        public String getActiveText() {
            return active ? "Yes" : "No";
        }

        public String getDateCreated() {
            return dateCreated;
        }
    }
}
