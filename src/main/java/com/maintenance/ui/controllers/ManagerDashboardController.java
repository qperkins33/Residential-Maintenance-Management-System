package com.maintenance.ui.controllers;

import com.maintenance.dao.MaintenanceRequestDAO;
import com.maintenance.dao.UserDAO;
import com.maintenance.enums.RequestStatus;
import com.maintenance.models.BuildingManager;
import com.maintenance.models.MaintenanceRequest;
import com.maintenance.models.MaintenanceStaff;
import com.maintenance.service.AuthenticationService;
import com.maintenance.ui.views.ViewFactory;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller for the Building Manager dashboard.
 * Responsibilities:
 *  - Render manager dashboard layout (top bar, sidebar, center content)
 *  - Show request-level stats and filterable request table
 *  - Enforce staff capacity when assigning or reassigning requests
 *  - Route back to login on logout
 */
public class ManagerDashboardController {
    // Used to open and close views / stages
    private final ViewFactory viewFactory;
    // Provides current user and logout functionality
    private final AuthenticationService authService;
    // DAO for loading and updating maintenance requests
    private final MaintenanceRequestDAO requestDAO;
    // DAO for loading staff data, including workload
    private final UserDAO userDAO;
    // Table showing all (or filtered) maintenance requests
    private TableView<MaintenanceRequest> requestTable;
    // Container for stat cards (total, in progress, etc.)
    private HBox statsBox;
    // Filter combo box that controls which requests are shown in the table
    private final ComboBox<String> filterBox = new ComboBox<>();

    /**
     * Create a ManagerDashboardController with the shared ViewFactory and service singletons.
     *
     * @param viewFactory view factory for stage/window management
     */
    public ManagerDashboardController(ViewFactory viewFactory) {
        this.viewFactory = viewFactory;
        this.authService = AuthenticationService.getInstance();
        this.requestDAO = new MaintenanceRequestDAO();
        this.userDAO = new UserDAO();
    }

    /**
     * Entry point for building the manager dashboard UI and attaching it to the root AnchorPane.
     *
     * @param root AnchorPane in which the dashboard will be constructed
     */
    public void createDashboardUI(AnchorPane root) {
        // Apply shared styles (fonts, colors, etc.)
        DashboardUIHelper.applyRootStyles(root, getClass());

        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(0));

        // Top bar: title, manager name, logout
        HBox topBar = createTopBar();
        mainLayout.setTop(topBar);

        // Left sidebar: navigation (currently just Dashboard button)
        VBox sidebar = createSidebar();
        mainLayout.setLeft(sidebar);

        // Center content: welcome text, stats, request table
        VBox centerContent = createCenterContent();
        VBox.setVgrow(centerContent, Priority.ALWAYS);
        mainLayout.setCenter(centerContent);

        // Anchor the main layout to fill the entire root pane
        AnchorPane.setTopAnchor(mainLayout, 0.0);
        AnchorPane.setBottomAnchor(mainLayout, 0.0);
        AnchorPane.setLeftAnchor(mainLayout, 0.0);
        AnchorPane.setRightAnchor(mainLayout, 0.0);

        root.getChildren().add(mainLayout);
    }

    /**
     * Builds the top bar for the dashboard, showing the title, manager identity, and logout button.
     *
     * @return configured HBox top bar
     */
    private HBox createTopBar() {
        HBox topBar = new HBox(20);
        topBar.setPadding(new Insets(15, 30, 15, 30));
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.getStyleClass().add("top-bar");

        Label titleLabel = new Label("🏢 Manager Dashboard");
        titleLabel.getStyleClass().add("top-bar-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Safe cast assuming only managers reach this controller
        BuildingManager manager = (BuildingManager) authService.getCurrentUser();
        Label userLabel = new Label("👤 " + manager.getFullName() + " (Manager)");
        userLabel.setFont(Font.font("Arial", 14));

        Button logoutButton = new Button("Logout");
        logoutButton.setStyle("-fx-background-color: #ff5252; -fx-text-fill: white; " +
                "-fx-padding: 8 20; -fx-background-radius: 5; -fx-cursor: hand;");
        logoutButton.setOnAction(e -> {
            authService.logout();
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            viewFactory.closeStage(stage);
            viewFactory.showLoginWindow();
        });

        topBar.getChildren().addAll(titleLabel, spacer, userLabel, logoutButton);
        return topBar;
    }

    /**
     * Builds the left sidebar navigation. Currently just a highlighted "Dashboard" button.
     *
     * @return VBox sidebar container
     */
    private VBox createSidebar() {
        VBox sidebar = new VBox(15);
        sidebar.setPadding(new Insets(20));
        sidebar.setStyle("-fx-background-color: #2c3e50;");
        sidebar.setPrefWidth(250);

        Label menuLabel = new Label("MENU");
        menuLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        menuLabel.setTextFill(Color.web("#95a5a6"));

        // Highlighted as the active section
        Button dashboardBtn = DashboardUIHelper.createSidebarButton("📊 Dashboard", true);
//        Button allRequestsBtn = DashboardUIHelper.createSidebarButton("📋 All Requests", false);
//        Button reportsBtn = DashboardUIHelper.createSidebarButton("📈 Reports", false);
//        Button settingsBtn = DashboardUIHelper.createSidebarButton("⚙️ Settings", false);

        sidebar.getChildren().addAll(menuLabel, dashboardBtn);
        return sidebar;
    }

    /**
     * Builds the main center content area:
     *  - Welcome section
     *  - Stats cards row
     *  - Requests table section
     *
     * @return VBox containing the assembled center content
     */
    private VBox createCenterContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        content.setFillWidth(true);

        VBox welcomeBox = createWelcomeSection();
        statsBox = new HBox(20);
        VBox requestsSection = createRequestsSection();
        VBox.setVgrow(requestsSection, Priority.ALWAYS);

        content.getChildren().addAll(welcomeBox, statsBox, requestsSection);

        // Initial stats calculation
        refreshStats();
        return content;
    }

    /**
     * Builds the top welcome section that greets the manager and shows today's date.
     *
     * @return VBox containing welcome text
     */
    private VBox createWelcomeSection() {
        VBox welcomeBox = new VBox(5);

        BuildingManager manager = (BuildingManager) authService.getCurrentUser();

        Label welcomeLabel = new Label("Welcome back, " + manager.getFirstName() + "!");
        welcomeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        welcomeLabel.setTextFill(Color.web("#2c3e50"));

        Label dateLabel = new Label("Today: " + java.time.LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy")));
        dateLabel.setFont(Font.font("Arial", 13));
        dateLabel.setTextFill(Color.GRAY);

        welcomeBox.getChildren().addAll(welcomeLabel, dateLabel);
        return welcomeBox;
    }

    /**
     * Rebuilds the stat cards based on current request data.
     * Shows counts for total, unassigned, not started, in progress, completed, and cancelled.
     * Each stat card is clickable and sets the request filter accordingly.
     */
    private void refreshStats() {
        if (statsBox == null) {
            return;
        }

        statsBox.getChildren().clear();

        List<MaintenanceRequest> allRequests = requestDAO.getAllRequests();

        long unassigned = allRequests.stream()
                .filter(r -> r.getStatus() == RequestStatus.SUBMITTED)
                .count();

        long inProgress = allRequests.stream().filter(this::isInProgress).count();
        long completed = allRequests.stream().filter(this::isCompleted).count();
        long cancelled = allRequests.stream().filter(this::isCancelled).count();
        long notStarted = allRequests.stream().filter(this::isNotStarted).count();

        VBox totalCard = DashboardUIHelper.createStatCard(
                "Total Requests",
                String.valueOf(allRequests.size()),
                "#667eea",
                DashboardUIHelper.loadStatIcon("request.png")
        );
        VBox unassignedCard = DashboardUIHelper.createStatCard(
                "Unassigned",
                String.valueOf(unassigned),
                "#2196f3",
                DashboardUIHelper.loadStatIcon("unassigned.png")
        );
        VBox pendingCard = DashboardUIHelper.createStatCard(
                "Not Started",
                String.valueOf(notStarted),
                "#2196f3",
                DashboardUIHelper.loadStatIcon("not-started.png")
        );
        VBox inProgressCard = DashboardUIHelper.createStatCard(
                "In Progress",
                String.valueOf(inProgress),
                "#ff9800",
                DashboardUIHelper.loadStatIcon("in-progress.png")
        );
        VBox completedCard = DashboardUIHelper.createStatCard(
                "Completed",
                String.valueOf(completed),
                "#4caf50",
                DashboardUIHelper.loadStatIcon("completed.png")
        );
        VBox cancelledCard = DashboardUIHelper.createStatCard(
                "Cancelled",
                String.valueOf(cancelled),
                "#f44336",
                DashboardUIHelper.loadStatIcon("cancelled.png")
        );

        // Make cards clickable to change filter
        totalCard.setOnMouseClicked(e -> setFilterFromCard("All Requests"));
        unassignedCard.setOnMouseClicked(e -> setFilterFromCard("Unassigned"));
        pendingCard.setOnMouseClicked(e -> setFilterFromCard("Not Started"));
        inProgressCard.setOnMouseClicked(e -> setFilterFromCard("In Progress"));
        completedCard.setOnMouseClicked(e -> setFilterFromCard("Completed"));
        cancelledCard.setOnMouseClicked(e -> setFilterFromCard("Cancelled"));

        statsBox.getChildren().addAll(
                totalCard,
                unassignedCard,
                pendingCard,
                inProgressCard,
                completedCard,
                cancelledCard
        );
    }

    /**
     * Builds the "All Maintenance Requests" section including:
     *  - Header row with title, filter combo box, and refresh button
     *  - Table of maintenance requests with staff, priority, status, etc.
     *
     * @return VBox containing header and table
     */
    private VBox createRequestsSection() {
        VBox section = new VBox(15);
        section.setFillWidth(true);

        HBox headerBox = new HBox(20);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label sectionTitle = new Label("All Maintenance Requests");
        sectionTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Filter options bound to the request status categories
        filterBox.getItems().addAll(
                "All Requests",
                "Unassigned",
                "In Progress",
                "Not Started",
                "Completed",
                "Cancelled"
        );
        filterBox.setValue("All Requests");
        filterBox.setStyle("-fx-background-radius: 5; -fx-padding: 5 10;");
//        filterBox.setOnAction(e -> filterRequests(filterBox.getValue()));

        // React when user picks a new filter value
        filterBox.valueProperty().addListener((obs, oldFilter, newFilter) -> {
            if (newFilter != null) {
                filterRequests(newFilter);
            }
        });

        // Manual refresh button to pull latest request data
        Button refreshBtn = new Button("🔄 Refresh");
        refreshBtn.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; " +
                "-fx-padding: 8 15; -fx-background-radius: 5; -fx-cursor: hand;");
        refreshBtn.setOnAction(e -> loadRequests());

        headerBox.getChildren().addAll(sectionTitle, spacer, filterBox, refreshBtn);

        // Request table setup
        requestTable = new TableView<>();
        requestTable.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        requestTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        requestTable.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(requestTable, Priority.ALWAYS);

        TableColumn<MaintenanceRequest, String> idCol = new TableColumn<>("Request ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("requestId"));
        idCol.setPrefWidth(100);

        // Custom "Assigned Staff" column that resolves staffId to staff full name
        TableColumn<MaintenanceRequest, String> staffCol = getMaintenanceRequestStringTableColumn();

        TableColumn<MaintenanceRequest, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        categoryCol.setPrefWidth(110);

        TableColumn<MaintenanceRequest, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        descCol.setPrefWidth(240);
        descCol.setStyle("-fx-wrap-text: true;");

        // Reuse helper-created columns for priority, status, and submitted date
        TableColumn<MaintenanceRequest, ?> priorityCol = DashboardUIHelper.createPriorityColumn();
        TableColumn<MaintenanceRequest, ?> statusCol = DashboardUIHelper.createStatusColumn();
        TableColumn<MaintenanceRequest, ?> dateCol = DashboardUIHelper.createSubmittedDateColumn();

        // Actions column that shows "Assign" / "Reassign" / "View"
        TableColumn<MaintenanceRequest, Void> actionCol = getMaintenanceRequestVoidTableColumn();

        requestTable.getColumns().setAll(java.util.List.of(
                idCol,
                staffCol,
                categoryCol,
                descCol,
                priorityCol,
                statusCol,
                dateCol,
                actionCol
        ));

        // Initial load
        loadRequests();

        // DEFAULT SORT: newest at top
        dateCol.setSortType(TableColumn.SortType.DESCENDING);
        requestTable.getSortOrder().setAll(dateCol);
        requestTable.sort();

        section.getChildren().addAll(headerBox, requestTable);
        VBox.setVgrow(requestTable, Priority.ALWAYS);
        return section;
    }

    /**
     * Helper to sync filter combo box when the user clicks a stat card.
     * Triggers the filter request logic via the value listener.
     *
     * @param filter label of the filter to apply
     */
    private void setFilterFromCard(String filter) {
        filterBox.setValue(filter);
    }

    /**
     * Builds the "Assigned Staff" column that displays the full staff name instead of raw staffId.
     * Falls back to blank if unassigned or the staff lookup fails.
     *
     * @return configured TableColumn for staff name display
     */
    private TableColumn<MaintenanceRequest, String> getMaintenanceRequestStringTableColumn() {
        TableColumn<MaintenanceRequest, String> staffCol = new TableColumn<>("Assigned Staff");
        staffCol.setCellValueFactory(new PropertyValueFactory<>("assignedStaffId"));
        staffCol.setPrefWidth(140);

        staffCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String staffId, boolean empty) {
                super.updateItem(staffId, empty);

                if (empty) {
                    setText(null);
                    return;
                }

                MaintenanceRequest req = getTableView().getItems().get(getIndex());

                // For submitted or unassigned requests, show nothing
                if (req.getStatus() == RequestStatus.SUBMITTED || staffId == null || staffId.isBlank()) {
                    setText("");
                    return;
                }

                MaintenanceStaff staff = userDAO.getStaffByStaffId(staffId);
                setText(staff != null ? staff.getFullName() : "");
            }
        });
        return staffCol;
    }

    /**
     * Builds the "Actions" column that shows:
     *  - Assign + View for unassigned requests
     *  - Reassign + View for active non-completed requests
     *  - View only for completed requests
     *
     * @return configured TableColumn for per-row action buttons
     */
    private TableColumn<MaintenanceRequest, Void> getMaintenanceRequestVoidTableColumn() {
        TableColumn<MaintenanceRequest, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setPrefWidth(195);
        actionCol.setMinWidth(195);
        actionCol.setResizable(false);
        actionCol.setStyle("-fx-alignment: CENTER;");

        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button assignBtn = new Button("Assign");
            private final Button viewBtn = new Button("View");
            private final HBox box = new HBox(8);

            {
                String btnStyle = "-fx-background-color: #667eea; -fx-text-fill: white; -fx-padding: 5 12; -fx-background-radius: 3; -fx-cursor: hand;";
                assignBtn.setStyle(btnStyle);
                viewBtn.setStyle(btnStyle);
                box.setAlignment(Pos.CENTER);

                // Assign / Reassign handler
                assignBtn.setOnAction(event -> {
                    MaintenanceRequest request = getTableView().getItems().get(getIndex());
                    showAssignDialog(request);
                });
                // View details dialog handler
                viewBtn.setOnAction(event -> {
                    MaintenanceRequest request = getTableView().getItems().get(getIndex());
                    DashboardUIHelper.showRequestDetailsDialog(request);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    MaintenanceRequest request = getTableView().getItems().get(getIndex());

                    if (request.getStatus() == RequestStatus.SUBMITTED) {
                        // Explicitly reset text for new / unassigned rows
                        assignBtn.setText("Assign");
                        box.getChildren().setAll(assignBtn, viewBtn);
                    } else if (request.getStatus() != RequestStatus.COMPLETED) {
                        // Anything active but not completed
                        assignBtn.setText("Reassign");
                        box.getChildren().setAll(assignBtn, viewBtn);
                    } else {
                        // Completed: only view
                        box.getChildren().setAll(viewBtn);
                    }

                    setGraphic(box);
                }
            }
        });
        return actionCol;
    }

    /**
     * Reloads all requests from the DAO, refreshes stats, reapplies sort,
     * and reset the filter selection to "All Requests".
     */
    private void loadRequests() {
        requestTable.setItems(FXCollections.observableArrayList(requestDAO.getAllRequests()));
        refreshStats();
        requestTable.sort();   // keep sort by date desc
        filterBox.setValue("All Requests");
    }

    /**
     * Applies the current filter selection by loading all requests and then
     * filtering them based on status category.
     *
     * @param filter human-readable filter label from the combo box
     */
    private void filterRequests(String filter) {
        List<MaintenanceRequest> requests = requestDAO.getAllRequests();

        switch (filter) {
            case "Unassigned" -> requests = requests.stream()
                    .filter(r -> r.getStatus() == RequestStatus.SUBMITTED)
                    .toList();
            case "In Progress" -> requests = requests.stream()
                    .filter(this::isInProgress)
                    .toList();
            case "Not Started" -> requests = requests.stream()
                    .filter(this::isNotStarted)
                    .toList();
            case "Completed" -> requests = requests.stream()
                    .filter(this::isCompleted)
                    .toList();
            case "Cancelled" -> requests = requests.stream()
                    .filter(this::isCancelled)
                    .toList();
            default -> { }
        }

        requestTable.setItems(FXCollections.observableArrayList(requests));
        requestTable.sort();   // reapply same sort
    }

    /**
     * Shows the assign/reassign dialog for a single request.
     * Enforces the staff capacity rule:
     *  - Only staff with activeWorkload < maxCapacity appear in the dropdown
     *  - A final capacity check runs again before applying the assignment
     *
     * @param request the MaintenanceRequest being assigned or reassigned
     */
    private void showAssignDialog(MaintenanceRequest request) {
        String currentlyAssignedId = request.getAssignedStaffId();
        boolean isReassign = currentlyAssignedId != null && !currentlyAssignedId.isBlank();

        Dialog<MaintenanceStaff> dialog = new Dialog<>();
        dialog.setTitle(isReassign ? "Reassign Staff" : "Assign Staff");
        dialog.setHeaderText(
                (isReassign ? "Reassign" : "Assign") +
                        " maintenance staff for request #" + request.getRequestId()
        );

        ButtonType assignButtonType = new ButtonType(
                isReassign ? "Reassign" : "Assign",
                ButtonBar.ButtonData.OK_DONE
        );
        dialog.getDialogPane().getButtonTypes().addAll(assignButtonType, ButtonType.CANCEL);

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        ComboBox<MaintenanceStaff> staffBox = new ComboBox<>();

        // Start from all active staff (userDAO handles active filter)
        List<MaintenanceStaff> availableStaff = userDAO.getAllActiveStaff();

        // If reassigning, remove the staff member who is already assigned to this request
        if (isReassign) {
            availableStaff = availableStaff.stream()
                    .filter(s -> !currentlyAssignedId.equals(s.getStaffId()))
                    .toList();
        }

        // Enforce capacity cap: only include staff whose active workload < maxCapacity
        availableStaff = availableStaff.stream()
                .filter(staff -> {
                    long activeWorkload = requestDAO.getRequestsByStaff(staff.getStaffId()).stream()
                            .filter(r -> !isCompleted(r) && !isCancelled(r))
                            .count();
                    // Keep staff object's workload consistent with current snapshot
                    staff.setCurrentWorkload((int) activeWorkload);
                    return activeWorkload < staff.getMaxCapacity();
                })
                .toList();

        // If there is nobody with capacity, show a message and bail
        if (availableStaff.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("No Staff With Capacity");
            alert.setHeaderText(null);
            alert.setContentText(
                    "No available staff members have remaining capacity for new active requests.\n\nYou cannot assign this request until a staff member completes or cancels existing work."
            );
            alert.showAndWait();
            return;
        }

        staffBox.getItems().addAll(availableStaff);
        staffBox.setPromptText("Select staff member");

        // Cell factory for dropdown list items, showing workload and capacity
        staffBox.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(MaintenanceStaff staff, boolean empty) {
                super.updateItem(staff, empty);
                if (empty || staff == null) {
                    setText(null);
                } else {
                    long activeWorkload = requestDAO.getRequestsByStaff(staff.getStaffId()).stream()
                            .filter(r -> !isCompleted(r) && !isCancelled(r))
                            .count();
                    staff.setCurrentWorkload((int) activeWorkload);
                    setText(staff.getFullName() + " (Workload: " +
                            activeWorkload + "/" + staff.getMaxCapacity() + ")");
                }
            }
        });

        // Button cell displays the chosen staff summary when combo box is closed
        staffBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(MaintenanceStaff staff, boolean empty) {
                super.updateItem(staff, empty);
                if (empty || staff == null) {
                    setText(null);
                } else {
                    long activeWorkload = requestDAO.getRequestsByStaff(staff.getStaffId()).stream()
                            .filter(r -> !isCompleted(r) && !isCancelled(r))
                            .count();
                    setText(staff.getFullName() + " (Workload: " +
                            activeWorkload + "/" + staff.getMaxCapacity() + ")");
                }
            }
        });

        content.getChildren().addAll(new Label("Select Staff:"), staffBox);
        dialog.getDialogPane().setContent(content);

        // Only return a MaintenanceStaff if the OK button was pressed and a value was selected
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == assignButtonType && staffBox.getValue() != null) {
                return staffBox.getValue();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(staff -> {
            // Final guard in case workload changed while dialog was open
            long activeWorkload = requestDAO.getRequestsByStaff(staff.getStaffId()).stream()
                    .filter(r -> !isCompleted(r) && !isCancelled(r))
                    .count();
            if (activeWorkload >= staff.getMaxCapacity()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Capacity Reached");
                alert.setHeaderText(null);
                alert.setContentText(
                        staff.getFullName() + " is now at full capacity (" +
                                activeWorkload + "/" + staff.getMaxCapacity() + ").\n" +
                                "Please choose another staff member."
                );
                alert.showAndWait();
                return;
            }

            // Apply assignment and persist to DB
            request.setAssignedStaffId(staff.getStaffId());
            request.setStatus(RequestStatus.ASSIGNED);
            if (requestDAO.updateRequest(request)) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText(isReassign ? "Request Reassigned" : "Request Assigned");
                alert.setContentText("Request has been " +
                        (isReassign ? "reassigned to " : "assigned to ") +
                        staff.getFullName());
                alert.showAndWait();
                loadRequests();
            }
        });
    }

    // Shared status grouping helpers (same across controllers)

    /**
     * Determines if a request is "not started" (submitted or assigned but no work done).
     *
     * @param r maintenance request to check
     * @return true if submitted or assigned, false otherwise
     */
    private boolean isNotStarted(MaintenanceRequest r) {
        return r.getStatus() == RequestStatus.SUBMITTED
                || r.getStatus() == RequestStatus.ASSIGNED;
    }

    /**
     * Determines if a request is "in progress" (actively being worked or reopened).
     *
     * @param r maintenance request to check
     * @return true if in progress or reopened, false otherwise
     */
    private boolean isInProgress(MaintenanceRequest r) {
        return r.getStatus() == RequestStatus.IN_PROGRESS
                || r.getStatus() == RequestStatus.REOPENED;
    }

    /**
     * Determines if a request is completed.
     *
     * @param r maintenance request to check
     * @return true if status is COMPLETED
     */
    private boolean isCompleted(MaintenanceRequest r) {
        return r.getStatus() == RequestStatus.COMPLETED;
    }

    /**
     * Determines if a request was cancelled.
     *
     * @param r maintenance request to check
     * @return true if status is CANCELLED
     */
    private boolean isCancelled(MaintenanceRequest r) {
        return r.getStatus() == RequestStatus.CANCELLED;
    }
}
