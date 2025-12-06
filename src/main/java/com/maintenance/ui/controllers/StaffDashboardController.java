package com.maintenance.ui.controllers;

import com.maintenance.dao.MaintenanceRequestDAO;
import com.maintenance.dao.UserDAO;
import com.maintenance.enums.PriorityLevel;
import com.maintenance.enums.RequestStatus;
import com.maintenance.models.MaintenanceRequest;
import com.maintenance.models.MaintenanceStaff;
import com.maintenance.notification.Email;
import com.maintenance.service.AuthenticationService;
import com.maintenance.ui.views.ViewFactory;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Controller for the maintenance staff dashboard.
 * Handles:
 *  - Layout and rendering of the staff view (top bar, sidebar, center content)
 *  - Loading and filtering of requests assigned to the logged-in staff member
 *  - Actions on requests (start, complete, update, archive/unarchive)
 *  - Email notifications sent to tenants when staff take actions
 */
public class StaffDashboardController {
    // Factory for creating and switching application windows
    private final ViewFactory viewFactory;
    // Central authentication service (current user, login, logout)
    private final AuthenticationService authService;
    // DAO for reading and updating maintenance requests
    private final MaintenanceRequestDAO requestDAO;
    // Table showing the staff member's requests
    private TableView<MaintenanceRequest> requestTable;
    // Label showing current workload vs capacity in sidebar
    private Label workloadLabel;
    // Container for dashboard stat cards
    private HBox statsBox;
    // Filter for narrowing visible tasks in the table
    private final ComboBox<String> filterBox = new ComboBox<>();

    /**
     * Creates a StaffDashboardController using shared services.
     *
     * @param viewFactory shared ViewFactory instance for stage management
     */
    public StaffDashboardController(ViewFactory viewFactory) {
        this.viewFactory = viewFactory;
        this.authService = AuthenticationService.getInstance();
        this.requestDAO = new MaintenanceRequestDAO();
    }

    /**
     * Builds the staff dashboard UI and anchors it into the given root.
     *
     * @param root AnchorPane which will host the dashboard layout
     */
    public void createDashboardUI(AnchorPane root) {
        // Attach global styles so staff dashboard shares common look and feel
        DashboardUIHelper.applyRootStyles(root, getClass());

        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(0));

        // Top bar: title, staff name, logout
        HBox topBar = createTopBar();
        mainLayout.setTop(topBar);

        // Left sidebar: navigation and workload widget
        VBox sidebar = createSidebar();
        mainLayout.setLeft(sidebar);

        // Center content: welcome text, stats, request table
        VBox centerContent = createCenterContent();
        mainLayout.setCenter(centerContent);

        // Make the dashboard fill the entire root pane
        AnchorPane.setTopAnchor(mainLayout, 0.0);
        AnchorPane.setBottomAnchor(mainLayout, 0.0);
        AnchorPane.setLeftAnchor(mainLayout, 0.0);
        AnchorPane.setRightAnchor(mainLayout, 0.0);

        root.getChildren().add(mainLayout);
    }

    /**
     * Creates the top bar for the staff dashboard including title, user label, and logout button.
     *
     * @return configured HBox top bar
     */
    private HBox createTopBar() {
        HBox topBar = new HBox(20);
        topBar.setPadding(new Insets(15, 30, 15, 30));
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.getStyleClass().add("top-bar");

        Label titleLabel = new Label("🔧 Staff Dashboard");
        titleLabel.getStyleClass().add("top-bar-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Logged-in staff user
        MaintenanceStaff staff = (MaintenanceStaff) authService.getCurrentUser();
        Label userLabel = new Label("👤 " + staff.getFullName() + " (Maintenance Staff)");
        userLabel.setFont(Font.font("Arial", 14));

        Button logoutButton = new Button("Logout");
        logoutButton.setStyle("-fx-background-color: #ff5252; -fx-text-fill: white; -fx-padding: 8 20; -fx-background-radius: 5; -fx-cursor: hand;");
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
     * Builds the left sidebar containing navigation and workload status box.
     *
     * @return VBox representing the sidebar
     */
    private VBox createSidebar() {
        VBox sidebar = new VBox(15);
        sidebar.setPadding(new Insets(20));
        sidebar.setStyle("-fx-background-color: #2c3e50;");
        sidebar.setPrefWidth(250);

        Label menuLabel = new Label("MENU");
        menuLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        menuLabel.setTextFill(Color.web("#95a5a6"));

        // Only one nav item for now; marked as active
        Button dashboardBtn = DashboardUIHelper.createSidebarButton("📊 Dashboard", true);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Workload / active request status widget
        VBox availabilityBox = createActiveRequestStatus();

        sidebar.getChildren().addAll(menuLabel, dashboardBtn, spacer, availabilityBox);
        return sidebar;
    }

    /**
     * Creates the small card in the sidebar that shows the staff member's active workload.
     *
     * @return VBox status box
     */
    private VBox createActiveRequestStatus() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(15));
        box.setStyle("-fx-background-color: #34495e; -fx-background-radius: 8;");

        Label statusLabel = new Label("Active Request Status");
        statusLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        statusLabel.setTextFill(Color.web("#ecf0f1"));

        workloadLabel = new Label();
        workloadLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        workloadLabel.setTextFill(Color.web("#bdc3c7"));

        // Initial workload value
        refreshWorkload();

        box.getChildren().addAll(statusLabel, workloadLabel);
        return box;
    }

    /**
     * Creates the main center content: welcome text, stats, and requests table.
     *
     * @return VBox containing all central components
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
     * Creates the welcome section for the logged-in staff and displays today's date.
     *
     * @return VBox containing welcome label and date
     */
    private VBox createWelcomeSection() {
        VBox welcomeBox = new VBox(5);

        MaintenanceStaff staff = (MaintenanceStaff) authService.getCurrentUser();

        Label welcomeLabel = new Label("Welcome back, " + staff.getFirstName() + "!");
        welcomeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        welcomeLabel.setTextFill(Color.web("#2c3e50"));

        Label dateLabel = new Label("Today: " + java.time.LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy")));
        dateLabel.setFont(Font.font("Arial", 13));
        dateLabel.setTextFill(Color.GRAY);

        welcomeBox.getChildren().addAll(welcomeLabel, dateLabel);
        return welcomeBox;
    }

    /**
     * Rebuilds the stat cards row (total, urgent, in progress, assigned, completed, cancelled)
     * based on the staff member's non-archived requests.
     */
    private void refreshStats() {
        if (statsBox == null) return;

        statsBox.getChildren().clear();

        MaintenanceStaff staff = (MaintenanceStaff) authService.getCurrentUser();
        // Use only NON-archived requests for stats
        List<MaintenanceRequest> allRequests = requestDAO.getRequestsByStaff(staff.getStaffId());
        List<MaintenanceRequest> activeRequests = allRequests.stream()
                .filter(r -> !r.isStaffArchived())
                .toList();

        long notStarted = activeRequests.stream().filter(this::isNotStarted).count();
        long inProgress = activeRequests.stream().filter(this::isInProgress).count();
        long completed = activeRequests.stream().filter(this::isCompleted).count();
        long cancelled = activeRequests.stream().filter(this::isCancelled).count();
        long urgent = activeRequests.stream()
                .filter(r -> (r.getPriority() == PriorityLevel.URGENT || r.getPriority() == PriorityLevel.EMERGENCY)
                        && !isCompleted(r) && !isCancelled(r))
                .count();

        VBox totalCard = DashboardUIHelper.createStatCard(
                "Total Requests",
                String.valueOf(activeRequests.size()),
                "#667eea",
                DashboardUIHelper.loadStatIcon("request.png")
        );
        VBox urgentCard = DashboardUIHelper.createStatCard(
                "Urgent (Active)",
                String.valueOf(urgent),
                "#f44336",
                DashboardUIHelper.loadStatIcon("urgent.png")
        );
        VBox inProgressCard = DashboardUIHelper.createStatCard(
                "In Progress",
                String.valueOf(inProgress),
                "#ff9800",
                DashboardUIHelper.loadStatIcon("in-progress.png")
        );
        VBox assignedCard = DashboardUIHelper.createStatCard(
                "Assigned",
                String.valueOf(notStarted),
                "#2196f3",
                DashboardUIHelper.loadStatIcon("to-do.png")
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
        totalCard.setOnMouseClicked(e -> setFilterFromCard("All Tasks"));
        assignedCard.setOnMouseClicked(e -> setFilterFromCard("Assigned"));
        inProgressCard.setOnMouseClicked(e -> setFilterFromCard("In Progress"));
        urgentCard.setOnMouseClicked(e -> setFilterFromCard("Urgent (Active)"));
        completedCard.setOnMouseClicked(e -> setFilterFromCard("Completed"));
        cancelledCard.setOnMouseClicked(e -> setFilterFromCard("Cancelled"));

        statsBox.getChildren().addAll(
                totalCard,
                assignedCard,
                inProgressCard,
                urgentCard,
                completedCard,
                cancelledCard
        );
    }

    /**
     * Builds the "My Assigned Requests" section including filter controls and the main table.
     *
     * @return VBox containing header and table
     */
    private VBox createRequestsSection() {
        VBox section = new VBox(15);
        section.setFillWidth(true);

        HBox headerBox = new HBox(20);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label sectionTitle = new Label("My Assigned Requests");
        sectionTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Filter options for staff view
        filterBox.getItems().addAll(
                "All Tasks",
                "Assigned",
                "In Progress",
                "Urgent (Active)",
                "Completed",
                "Cancelled",
                "Archived"
        );
        filterBox.setValue("All Tasks");
        filterBox.setStyle("-fx-background-radius: 5; -fx-padding: 5 10;");
//        filterBox.setOnAction(e -> filterRequests(filterBox.getValue()));

        // Use listener instead of onAction to react to changes
        filterBox.valueProperty().addListener((obs, oldFilter, newFilter) -> {
            if (newFilter != null) {
                filterRequests(newFilter);
            }
        });

        Button refreshBtn = new Button("🔄 Refresh");
        refreshBtn.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; -fx-padding: 8 15; -fx-background-radius: 5; -fx-cursor: hand;");
        // Reload from DB and reset filter
        refreshBtn.setOnAction(e -> loadRequests());

        headerBox.getChildren().addAll(sectionTitle, spacer, filterBox, refreshBtn);

        // Build main requests table
        requestTable = new TableView<>();
        requestTable.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        requestTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        requestTable.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(requestTable, Priority.ALWAYS);

        TableColumn<MaintenanceRequest, String> idCol = new TableColumn<>("Request ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("requestId"));
        idCol.setPrefWidth(100);
        idCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<MaintenanceRequest, String> aptCol = new TableColumn<>("Apartment");
        aptCol.setCellValueFactory(new PropertyValueFactory<>("apartmentNumber"));
        aptCol.setPrefWidth(100);
        aptCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<MaintenanceRequest, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        categoryCol.setPrefWidth(110);

        TableColumn<MaintenanceRequest, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        descCol.setPrefWidth(240);
        descCol.setStyle("-fx-wrap-text: true;");

        // Reuse helper columns for priority, status, and submitted date
        TableColumn<MaintenanceRequest, ?> priorityCol = DashboardUIHelper.createPriorityColumn();
        TableColumn<MaintenanceRequest, ?> statusCol = DashboardUIHelper.createStatusColumn();
        TableColumn<MaintenanceRequest, ?> dateCol = DashboardUIHelper.createSubmittedDateColumn();

        // Action column with update/start/complete/archive/unarchive options
        TableColumn<MaintenanceRequest, Void> actionCol = getMaintenanceRequestVoidTableColumn();

        requestTable.getColumns().setAll(java.util.List.of(
                idCol,
                aptCol,
                categoryCol,
                descCol,
                priorityCol,
                statusCol,
                dateCol,
                actionCol
        ));

        // Initial load
        loadRequests();

        // Default sort by newest submitted date
        dateCol.setSortType(TableColumn.SortType.DESCENDING);
        requestTable.getSortOrder().setAll(dateCol);
        requestTable.sort();

        Label emptyLabel = new Label("No requests assigned yet");
        emptyLabel.setFont(Font.font("Arial", 14));
        emptyLabel.setTextFill(Color.GRAY);
        requestTable.setPlaceholder(emptyLabel);

        requestTable.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(requestTable, Priority.ALWAYS);

        section.getChildren().addAll(headerBox, requestTable);
        VBox.setVgrow(requestTable, Priority.ALWAYS);

        return section;
    }

    /**
     * Sets filter in the combo box from a stat card click.
     * Triggers filterRequests via the value listener.
     *
     * @param filter filter label to select
     */
    private void setFilterFromCard(String filter) {
        filterBox.setValue(filter);
    }

    /**
     * Builds the action column for each row with context-dependent controls:
     *  - ASSIGNED: [Start] [View]
     *  - IN_PROGRESS/REOPENED: [Complete] [Update] [View]
     *  - COMPLETED: [Archive] [Post Update] [View]
     *  - CANCELLED: [Archive] [Update] [View]
     *  - STAFF ARCHIVED: [Unarchive] plus update or post-update and view
     *
     * @return TableColumn with configured cell factory
     */
    private TableColumn<MaintenanceRequest, Void> getMaintenanceRequestVoidTableColumn() {
        TableColumn<MaintenanceRequest, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setPrefWidth(280);
        actionCol.setMinWidth(280);
        actionCol.setResizable(false);
        actionCol.setStyle("-fx-alignment: CENTER;");
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button updateBtn = new Button("Update");
            private final Button postUpdateBtn = new Button("Post Update");
            private final Button startBtn = new Button("Start");
            private final Button completeBtn = new Button("Complete");
            private final Button archiveBtn = new Button("Archive");
            private final Button unarchiveBtn = new Button("Unarchive");
            private final Button viewBtn = new Button("View");
            private final HBox buttonBox = new HBox(5);

            {
                String btnStyle = "-fx-background-color: #667eea; -fx-text-fill: white; -fx-padding: 5 12; -fx-background-radius: 3; -fx-cursor: hand;";
                updateBtn.setStyle(btnStyle);
                postUpdateBtn.setStyle(btnStyle);
                startBtn.setStyle(btnStyle);
                completeBtn.setStyle(btnStyle);
                archiveBtn.setStyle(btnStyle);
                unarchiveBtn.setStyle(btnStyle);
                viewBtn.setStyle(btnStyle);

                // Open staff update dialog (pre or post completion)
                updateBtn.setOnAction(e -> {
                    MaintenanceRequest request = getTableView().getItems().get(getIndex());
                    showStaffUpdateDialog(request);
                });

                postUpdateBtn.setOnAction(e -> {
                    MaintenanceRequest request = getTableView().getItems().get(getIndex());
                    showStaffUpdateDialog(request);
                });

                // Move request to "In Progress"
                startBtn.setOnAction((ActionEvent event) -> {
                    MaintenanceRequest request = getTableView().getItems().get(getIndex());
                    startWork(request);
                });

                // Complete the request and capture resolution details
                completeBtn.setOnAction((ActionEvent event) -> {
                    MaintenanceRequest request = getTableView().getItems().get(getIndex());
                    showCompleteDialog(request);
                });

                // Archive for this staff member
                archiveBtn.setOnAction(e -> {
                    MaintenanceRequest request = getTableView().getItems().get(getIndex());
                    archiveAsStaff(request);
                });

                // Unarchive back to active list
                unarchiveBtn.setOnAction(e -> {
                    MaintenanceRequest request = getTableView().getItems().get(getIndex());
                    unarchiveAsStaff(request);
                });

                // View read-only details dialog
                viewBtn.setOnAction((ActionEvent event) -> {
                    MaintenanceRequest request = getTableView().getItems().get(getIndex());
                    DashboardUIHelper.showRequestDetailsDialog(request);
                });

                buttonBox.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }

                MaintenanceRequest request = getTableView().getItems().get(getIndex());
                if (request == null) {
                    setGraphic(null);
                    return;
                }

                buttonBox.getChildren().clear();

                // If staff archived this, show Unarchive + appropriate update button + view
                if (request.isStaffArchived()) {
                    // Realistically only COMPLETED tasks get archived, but this is safe
                    buttonBox.getChildren().add(unarchiveBtn);
                    if (isCompleted(request)) {
                        buttonBox.getChildren().add(postUpdateBtn);
                    } else {
                        buttonBox.getChildren().add(updateBtn);
                    }
                    buttonBox.getChildren().add(viewBtn);
                    setGraphic(buttonBox);
                    return;
                }

                // Normal non-archived behavior based on status
                if (request.getStatus() == RequestStatus.ASSIGNED) {
                    buttonBox.getChildren().addAll(startBtn, viewBtn);
                } else if (isInProgress(request)) {
                    buttonBox.getChildren().addAll(completeBtn, updateBtn, viewBtn);
                } else if (isCompleted(request)) {
                    buttonBox.getChildren().addAll(archiveBtn, postUpdateBtn, viewBtn);
                } else if (request.getStatus() == RequestStatus.CANCELLED) {
                    buttonBox.getChildren().addAll(archiveBtn, updateBtn, viewBtn);
                } else {
                    buttonBox.getChildren().addAll(updateBtn, viewBtn);
                }

                setGraphic(buttonBox);
            }
        });
        return actionCol;
    }

    /**
     * Unarchives a request for the current staff member, returning it to the active list.
     *
     * @param request request to unarchive
     */
    private void unarchiveAsStaff(MaintenanceRequest request) {
        if (request == null) {
            return;
        }

        if (!request.isStaffArchived()) {
            showError("This request is not archived.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Unarchive Request");
        confirm.setHeaderText("Unarchive request #" + request.getRequestId() + " to your dashboard?");
        confirm.setContentText("It will return to your main task list.");
        confirm.showAndWait().ifPresent(response -> {
            if (response != ButtonType.OK) {
                return;
            }

            request.setStaffArchived(false);
            request.setLastUpdated(LocalDateTime.now());

            if (!requestDAO.updateRequest(request)) {
                showError("Unable to unarchive request. Please try again.");
            } else {
                loadRequests();
            }
        });
    }

    /**
     * Shows dialog to add or edit staff update notes for a request.
     * Also sends an email to the tenant with the new update.
     *
     * @param request request to update
     */
    private void showStaffUpdateDialog(MaintenanceRequest request) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Staff Update");
        dialog.setHeaderText("Add update for Request #" + request.getRequestId());

        ButtonType saveBtnType = new ButtonType("Save Update", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtnType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        // Read-only original description for context
        TextArea descArea = new TextArea(request.getDescription());
        descArea.setEditable(false);
        descArea.setWrapText(true);
        descArea.setPrefRowCount(4);

        // Editable staff update text
        TextArea updateArea = new TextArea();
        updateArea.setWrapText(true);
        updateArea.setPrefRowCount(4);
        updateArea.setPromptText("Add a staff update for this request...");

        String existing = request.getStaffUpdateNotes();
        if (existing != null && !existing.isBlank()) {
            updateArea.setText(existing);
        }

        grid.add(new Label("Original Description:"), 0, 0);
        grid.add(descArea, 1, 0);
        grid.add(new Label("Staff Update:"), 0, 1);
        grid.add(updateArea, 1, 1);

        dialog.getDialogPane().setContent(grid);

        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveBtnType);
        saveButton.addEventFilter(ActionEvent.ACTION, event -> {
            String text = updateArea.getText().trim();
            if (text.isEmpty()) {
                new Alert(Alert.AlertType.WARNING,
                        "Please enter an update or cancel.").showAndWait();
                event.consume();
                return;
            }

            // Persist staff notes and ensure tenant sees it again (reset tenant archive flag)
            request.setStaffUpdateNotes(text);
            request.setTenantArchived(false);
            request.setLastUpdated(LocalDateTime.now());

            if (!requestDAO.updateRequest(request)) {
                new Alert(Alert.AlertType.ERROR,
                        "Unable to save staff update. Please try again.").showAndWait();
                event.consume();
            } else {
                String staff = resolveTechnicianName(request);
                String staffEmail = requestDAO
                        .findStaffEmailByRequestId(request.getRequestId())
                        .orElse("");
                final String updateText = text;

                final String tenantName = requestDAO
                        .findTenantNameByRequestId(request.getRequestId())
                        .orElse("");

                // Send tenant email in background to avoid blocking UI thread
                requestDAO.findTenantEmailByRequestId(request.getRequestId()).ifPresent(to ->
                        CompletableFuture.runAsync(() -> {
                            String namePart = tenantName.isBlank() ? "" : " " + tenantName;
                            String subject = "Maintenance request update: Staff message";
                            String body =
                                    "Hello" + namePart + ",\n\n" +
                                            "There is a new update on your maintenance request.\n\n" +
                                            "Request ID: " + request.getRequestId() + "\n" +
                                            "Status: " + request.getStatus() + "\n" +
                                            "Apartment: " + nullToDash(request.getApartmentNumber()) + "\n" +
                                            "Technician: " + staff + "\n\n" +
                                            "Description: " + request.getDescription() + "\n\n" +
                                            "Update from technician:\n" +
                                            updateText + "\n\n" +
                                            "Reply to the following email if you have questions:\n" +
                                            staffEmail + "\n\n" +
                                            "Thank you,\nResidential Maintenance";
                            Email.send(to, subject, body);
                        })
                );

                loadRequests();
                new Alert(Alert.AlertType.INFORMATION,
                        "Staff update saved and emailed to tenant.").showAndWait();
            }
        });

        dialog.showAndWait();
    }

    /**
     * Loads all requests for the logged-in staff member and applies:
     *  - Staff-archive filter (only non-archived)
     *  - Stats and workload refresh
     *  - Default "All Tasks" filter selection reset
     */
    private void loadRequests() {
        MaintenanceStaff staff = (MaintenanceStaff) authService.getCurrentUser();
        List<MaintenanceRequest> all = requestDAO.getRequestsByStaff(staff.getStaffId());
        List<MaintenanceRequest> visible = all.stream()
                .filter(r -> !r.isStaffArchived())
                .toList();

        requestTable.setItems(FXCollections.observableArrayList(visible));
        refreshWorkload();
        refreshStats();
        requestTable.sort();
        filterBox.setValue("All Tasks");
    }

    /**
     * Applies the selected status filter for the current staff member.
     * Filters against all assigned requests, then narrows down based on the chosen label.
     *
     * @param filter label from the filter combo box
     */
    private void filterRequests(String filter) {
        MaintenanceStaff staff = (MaintenanceStaff) authService.getCurrentUser();
        List<MaintenanceRequest> all = requestDAO.getRequestsByStaff(staff.getStaffId());
        List<MaintenanceRequest> filtered;

        if ("Archived".equals(filter)) {
            // Show only staff-archived items
            filtered = all.stream()
                    .filter(MaintenanceRequest::isStaffArchived)
                    .toList();
        } else {
            // Start from non-archived
            filtered = all.stream()
                    .filter(r -> !r.isStaffArchived())
                    .toList();

            // Narrow based on filter label
            switch (filter) {
                case "Assigned" -> filtered = filtered.stream()
                        .filter(r -> r.getStatus() == RequestStatus.ASSIGNED)
                        .toList();
                case "In Progress" -> filtered = filtered.stream()
                        .filter(this::isInProgress)
                        .toList();
                case "Urgent (Active)" -> filtered = filtered.stream()
                        .filter(r -> (r.getPriority() == PriorityLevel.URGENT ||
                                r.getPriority() == PriorityLevel.EMERGENCY) &&
                                !isCompleted(r) && !isCancelled(r))
                        .toList();
                case "Completed" -> filtered = filtered.stream()
                        .filter(this::isCompleted)
                        .toList();
                case "Cancelled" -> filtered = filtered.stream()
                        .filter(this::isCancelled)
                        .toList();
                default -> {
                }
            }
        }

        requestTable.setItems(FXCollections.observableArrayList(filtered));
        refreshWorkload();
        requestTable.sort();
    }

    /**
     * Updates the workload label in the sidebar with active request count and capacity.
     * Active means not completed and not cancelled.
     */
    private void refreshWorkload() {
        if (workloadLabel == null) return;

        MaintenanceStaff staff = (MaintenanceStaff) authService.getCurrentUser();
        if (staff == null) return;

        long activeWorkload = requestDAO.getRequestsByStaff(staff.getStaffId()).stream()
                .filter(r -> !isCompleted(r) && !isCancelled(r))
                .count();

        staff.setCurrentWorkload((int) activeWorkload);
        workloadLabel.setText("Workload: " + activeWorkload + "/" + staff.getMaxCapacity());
    }

    /**
     * Archives a request from the staff member's point of view, leaving it accessible in "Archived" filter.
     * Only completed or cancelled requests can be archived.
     *
     * @param request request to archive
     */
    private void archiveAsStaff(MaintenanceRequest request) {
        if (request == null) {
            return;
        }

        if (!isCompleted(request) && request.getStatus() != RequestStatus.CANCELLED) {
            showError("Only completed or cancelled requests can be archived.");
            return;
        }

        if (request.isStaffArchived()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Already Archived");
            alert.setHeaderText(null);
            alert.setContentText("This request is already archived for you.");
            alert.showAndWait();
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Archive Request");
        confirm.setHeaderText("Archive request #" + request.getRequestId() + " from your dashboard?");
        confirm.setContentText("You can still view it later by changing the filter to Archived.");
        confirm.showAndWait().ifPresent(response -> {
            if (response != ButtonType.OK) {
                return;
            }

            request.setStaffArchived(true);
            request.setLastUpdated(LocalDateTime.now());

            if (!requestDAO.updateRequest(request)) {
                showError("Unable to archive request. Please try again.");
            } else {
                loadRequests();
            }
        });
    }

    /**
     * Moves a request into "In Progress" status after confirmation and sends a status email to the tenant.
     *
     * @param request request to transition
     */
    private void startWork(MaintenanceRequest request) {
        RequestStatus previousStatus = request.getStatus();

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Start Work");
        confirm.setHeaderText("Start working on this request?");
        confirm.setContentText("Request #" + request.getRequestId() + " will be marked as 'In Progress'");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                request.setStatus(RequestStatus.IN_PROGRESS);
                request.setLastUpdated(java.time.LocalDateTime.now());

                if (requestDAO.updateRequest(request)) {
                    String staff = resolveTechnicianName(request);
                    String staffEmail = requestDAO
                            .findStaffEmailByRequestId(request.getRequestId())
                            .orElse("");

                    String previousStatusText = previousStatus.toString();

                    final String tenantName = requestDAO
                            .findTenantNameByRequestId(request.getRequestId())
                            .orElse("");

                    // Email tenant about status change (non-blocking)
                    requestDAO.findTenantEmailByRequestId(request.getRequestId()).ifPresent(to ->
                            CompletableFuture.runAsync(() -> {
                                String namePart = tenantName.isBlank() ? "" : " " + tenantName;
                                String subject = "Maintenance request status: " + previousStatusText + " -> In Progress";
                                String body =
                                        "Hello" + namePart + ",\n\n" +
                                                "Your maintenance request status has been updated.\n\n" +
                                                "Request ID: " + request.getRequestId() + "\n" +
                                                "Status: In Progress\n" +
                                                "Apartment: " + nullToDash(request.getApartmentNumber()) + "\n" +
                                                "Technician: " + staff + "\n\n" +
                                                "Description: " + request.getDescription() + "\n\n" +
                                                "Old Status: 'Assigned'\n" +
                                                "New Status: 'In Progress'\n\n" +
                                                "Reply to the following email if you have questions:\n" +
                                                staffEmail + "\n\n" +
                                                "Thank you,\nResidential Maintenance";
                                Email.send(to, subject, body);
                            })
                    );

                    Alert success = new Alert(Alert.AlertType.INFORMATION);
                    success.setTitle("Success");
                    success.setHeaderText("Work Started");
                    success.setContentText("Request status updated to 'In Progress'");
                    success.showAndWait();
                    loadRequests();
                } else {
                    showError("Failed to update request status");
                }
            }
        });
    }

    /**
     * Shows a dialog for the staff member to complete a request, capturing
     * resolution notes, hours spent, and optional cost.
     * On success, marks request completed and emails tenant.
     *
     * @param request request to complete
     */
    private void showCompleteDialog(MaintenanceRequest request) {
        RequestStatus previousStatus = request.getStatus();

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Complete Request");
        dialog.setHeaderText("Mark Request #" + request.getRequestId() + " as Complete");

        ButtonType completeButtonType = new ButtonType("Complete", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(completeButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));

        TextArea resolutionArea = new TextArea();
        resolutionArea.setPromptText("Describe what was done to resolve the issue...");
        resolutionArea.setPrefRowCount(5);
        resolutionArea.setPrefColumnCount(40);
        resolutionArea.setWrapText(true);

        TextField hoursField = new TextField();
        hoursField.setPromptText("Hours Spent");

        TextField costField = new TextField();
        costField.setPromptText("Cost (if applicable)");

        grid.add(new Label("Resolution Notes:"), 0, 0);
        grid.add(resolutionArea, 0, 1, 2, 1);
        grid.add(new Label("Hours Spent:"), 0, 2);
        grid.add(hoursField, 1, 2);
        grid.add(new Label("Actual Cost ($):"), 0, 3);
        grid.add(costField, 1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setStyle("-fx-background-color: white;");

        // Validate fields and return resolution text only when all checks pass
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == completeButtonType) {
                String resolutionText = resolutionArea.getText().trim();
                String hoursText = hoursField.getText().trim();
                String costText = costField.getText().trim();

                if (resolutionText.isEmpty() || hoursText.isEmpty()) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Missing Information");
                    alert.setContentText("Please provide resolution notes and hours spent.");
                    alert.showAndWait();
                    return null;
                }

                try {
                    Double.parseDouble(hoursText);
                } catch (NumberFormatException e) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Invalid Hours");
                    alert.setContentText("Hours spent must be a valid number.");
                    alert.showAndWait();
                    return null;
                }

                if (!costText.isEmpty()) {
                    try {
                        Double.parseDouble(costText);
                    } catch (NumberFormatException e) {
                        Alert alert = new Alert(Alert.AlertType.WARNING);
                        alert.setTitle("Invalid Cost");
                        alert.setContentText("Cost must be a valid number or left blank.");
                        alert.showAndWait();
                        return null;
                    }
                }

                return resolutionText;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(resolution -> {
            double cost = 0.0;
            try {
                String costText = costField.getText().trim();
                if (!costText.isEmpty()) {
                    cost = Double.parseDouble(costText);
                }
            } catch (NumberFormatException ignored) {
                // Ignore parsing issues here; we already validated above
            }

            // Persist cost and close request with resolution notes
            request.setActualCost(cost);
            request.close(resolution);

            if (requestDAO.updateRequest(request)) {
                String staff = resolveTechnicianName(request);
                String staffEmail = requestDAO
                        .findStaffEmailByRequestId(request.getRequestId())
                        .orElse("");

                String previousStatusText = previousStatus.toString();
                final String formattedCost = String.format("%.2f", cost);

                final String tenantName = requestDAO
                        .findTenantNameByRequestId(request.getRequestId())
                        .orElse("");

                // Email tenant about completion
                requestDAO.findTenantEmailByRequestId(request.getRequestId()).ifPresent(to ->
                        CompletableFuture.runAsync(() -> {
                            String namePart = tenantName.isBlank() ? "" : " " + tenantName;
                            String subject = "Maintenance request status: " + previousStatusText + " -> Completed";
                            String body =
                                    "Hello" + namePart + ",\n\n" +
                                            "Your maintenance request is now completed.\n\n" +
                                            "Request ID: " + request.getRequestId() + "\n" +
                                            "Status: Completed\n" +
                                            "Apartment: " + nullToDash(request.getApartmentNumber()) + "\n" +
                                            "Technician: " + staff + "\n\n" +
                                            "Description: " + request.getDescription() + "\n\n" +
                                            "Resolution: " + resolution + "\n" +
                                            "Cost: $" + formattedCost + "\n\n" +
                                            "Reply to the following email if you have questions:\n" +
                                            staffEmail + "\n\n" +
                                            "Thank you,\nResidential Maintenance";
                            Email.send(to, subject, body);
                        })
                );

                Alert success = new Alert(Alert.AlertType.INFORMATION);
                success.setTitle("Success");
                success.setHeaderText("Request Completed");
                success.setContentText("Request #" + request.getRequestId() + " has been marked as completed successfully.");
                success.showAndWait();
                loadRequests();
            } else {
                showError("Failed to update request");
            }
        });
    }

    /**
     * Attempts to resolve the best display name for the technician assigned to a request:
     *  - First uses the current logged-in MaintenanceStaff if available
     *  - Then falls back to a new UserDAO lookup by assignedStaffId
     *  - Finally falls back to "Maintenance Staff" if nothing else is available
     *
     * @param r request for which to resolve technician name
     * @return technician full name or a generic label
     */
    private String resolveTechnicianName(MaintenanceRequest r) {
        var u = authService.getCurrentUser();
        if (u instanceof MaintenanceStaff ms && ms.getFullName() != null && !ms.getFullName().isBlank()) {
            return ms.getFullName();
        }
        String staffId = r.getAssignedStaffId();
        if (staffId != null && !staffId.isBlank()) {
            var userDAO = new UserDAO();
            var staff = userDAO.getStaffByStaffId(staffId);
            if (staff != null && staff.getFullName() != null && !staff.getFullName().isBlank()) {
                return staff.getFullName();
            }
        }
        return "Maintenance Staff";
    }

    /**
     * Utility to convert null apartment or other text to a dash when printing.
     *
     * @param s input string
     * @return s, or "-" if s is null
     */
    private static String nullToDash(String s) {
        return s == null ? "-" : s;
    }

    /**
     * Shows a generic error dialog with the given message.
     *
     * @param message error text
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Operation Failed");
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Returns true if the request has not been started yet (submitted or assigned).
     *
     * @param r request to check
     * @return true if status is SUBMITTED or ASSIGNED
     */
    private boolean isNotStarted(MaintenanceRequest r) {
        return r.getStatus() == RequestStatus.SUBMITTED || r.getStatus() == RequestStatus.ASSIGNED;
    }

    /**
     * Returns true if the request is currently in progress or reopened.
     *
     * @param r request to check
     * @return true if status is IN_PROGRESS or REOPENED
     */
    private boolean isInProgress(MaintenanceRequest r) {
        return r.getStatus() == RequestStatus.IN_PROGRESS || r.getStatus() == RequestStatus.REOPENED;
    }

    /**
     * Returns true if the request is completed.
     *
     * @param r request to check
     * @return true if status is COMPLETED
     */
    private boolean isCompleted(MaintenanceRequest r) {
        return r.getStatus() == RequestStatus.COMPLETED;
    }

    /**
     * Returns true if the request has been cancelled.
     *
     * @param r request to check
     * @return true if status is CANCELLED
     */
    private boolean isCancelled(MaintenanceRequest r) {
        return r.getStatus() == RequestStatus.CANCELLED;
    }
}
