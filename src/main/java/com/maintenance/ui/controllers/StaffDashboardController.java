package com.maintenance.ui.controllers;

import com.maintenance.dao.MaintenanceRequestDAO;
import com.maintenance.enums.PriorityLevel;
import com.maintenance.enums.RequestStatus;
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
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class StaffDashboardController {
    private final ViewFactory viewFactory;
    private final AuthenticationService authService;
    private final MaintenanceRequestDAO requestDAO;
    private TableView<MaintenanceRequest> requestTable;

    public StaffDashboardController(ViewFactory viewFactory) {
        this.viewFactory = viewFactory;
        this.authService = AuthenticationService.getInstance();
        this.requestDAO = new MaintenanceRequestDAO();
    }

    public void createDashboardUI(AnchorPane root) {
        root.getStyleClass().add("staff-dashboard-root");

        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(0));

        // Top bar
        HBox topBar = createTopBar();
        mainLayout.setTop(topBar);

        // Left sidebar
        VBox sidebar = createSidebar();
        mainLayout.setLeft(sidebar);

        // Center content
        VBox centerContent = createCenterContent();
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

        Label titleLabel = new Label("🔧 Staff Dashboard");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        titleLabel.getStyleClass().add("top-bar-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        MaintenanceStaff staff = (MaintenanceStaff) authService.getCurrentUser();
        Label userLabel = new Label("👤 " + staff.getFullName() + " (Maintenance Staff)");
        userLabel.setFont(Font.font("Arial", 14));
        userLabel.getStyleClass().add("top-bar-user");

        Button logoutButton = new Button("Logout");
        logoutButton.getStyleClass().addAll("button", "button-danger");
        logoutButton.setOnAction(e -> {
            authService.logout();
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            viewFactory.closeStage(stage);
            viewFactory.showLoginWindow();
        });

        topBar.getChildren().addAll(titleLabel, spacer, userLabel, logoutButton);
        return topBar;
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox(15);
        sidebar.setPadding(new Insets(20));
        sidebar.setPrefWidth(250);
        sidebar.getStyleClass().add("sidebar");

        Label menuLabel = new Label("MENU");
        menuLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        menuLabel.getStyleClass().add("sidebar-menu-label");

        Button dashboardBtn = createSidebarButton("📊 Dashboard", true);
        Button assignedBtn = createSidebarButton("📋 Assigned Tasks", false);
        Button historyBtn = createSidebarButton("📜 History", false);
        Button profileBtn = createSidebarButton("👤 Profile", false);
        Button settingsBtn = createSidebarButton("⚙️ Settings", false);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Availability toggle
        VBox availabilityBox = createAvailabilityToggle();

        sidebar.getChildren().addAll(menuLabel, dashboardBtn, assignedBtn, historyBtn,
                profileBtn, settingsBtn, spacer, availabilityBox);
        return sidebar;
    }

    private Button createSidebarButton(String text, boolean active) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setFont(Font.font("Arial", 14));
        btn.getStyleClass().add("sidebar-button");
        if (active) {
            btn.getStyleClass().add("sidebar-button-active");
        } else {
            btn.getStyleClass().add("sidebar-button-inactive");
        }
        return btn;
    }

    private VBox createAvailabilityToggle() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(15));
        box.getStyleClass().add("availability-box");

        Label statusLabel = new Label("Availability Status");
        statusLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        statusLabel.getStyleClass().add("availability-status-label");

        MaintenanceStaff staff = (MaintenanceStaff) authService.getCurrentUser();

        CheckBox availableCheck = new CheckBox("Available for assignments");
        availableCheck.setSelected(staff.isAvailable());
        availableCheck.getStyleClass().add("availability-checkbox");

        availableCheck.setOnAction(e -> {
            staff.updateAvailability(availableCheck.isSelected());
            showStatusNotification(availableCheck.isSelected());
        });

        Label workloadLabel = new Label("Workload: " + staff.getCurrentWorkload() +
                "/" + staff.getMaxCapacity());
        workloadLabel.setFont(Font.font("Arial", 11));
        workloadLabel.getStyleClass().add("availability-workload-label");

        box.getChildren().addAll(statusLabel, availableCheck, workloadLabel);
        return box;
    }

    private void showStatusNotification(boolean available) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Status Updated");
        alert.setHeaderText(null);
        alert.setContentText("Availability status updated to: " +
                (available ? "Available" : "Unavailable"));
        alert.showAndWait();
    }

    private VBox createCenterContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        content.setFillWidth(true); // allow children to use full width

        // Welcome section
        VBox welcomeBox = createWelcomeSection();

        // Stats cards
        HBox statsBox = createStatsCards();

        // Requests section
        VBox requestsSection = createRequestsSection();

        // make the requests area take the leftover vertical space
        VBox.setVgrow(requestsSection, Priority.ALWAYS);

        content.getChildren().addAll(welcomeBox, statsBox, requestsSection);
        return content;
    }

    private VBox createWelcomeSection() {
        VBox welcomeBox = new VBox(5);

        MaintenanceStaff staff = (MaintenanceStaff) authService.getCurrentUser();

        Label welcomeLabel = new Label("Welcome back, " + staff.getFirstName() + "!");
        welcomeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        welcomeLabel.getStyleClass().add("welcome-heading");

        Label dateLabel = new Label("Today: " +
                java.time.LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy")));
        dateLabel.setFont(Font.font("Arial", 13));
        dateLabel.getStyleClass().add("welcome-subheading");

        welcomeBox.getChildren().addAll(welcomeLabel, dateLabel);
        return welcomeBox;
    }

    private HBox createStatsCards() {
        HBox statsBox = new HBox(20);

        MaintenanceStaff staff = (MaintenanceStaff) authService.getCurrentUser();
        List<MaintenanceRequest> myRequests = requestDAO.getRequestsByStaff(staff.getStaffId());

        long assigned = myRequests.stream()
                .filter(r -> r.getStatus() == RequestStatus.ASSIGNED)
                .count();

        long inProgress = myRequests.stream()
                .filter(r -> r.getStatus() == RequestStatus.IN_PROGRESS
                        || r.getStatus() == RequestStatus.REOPENED)
                .count();

        long completed = myRequests.stream()
                .filter(r -> r.getStatus() == RequestStatus.COMPLETED)
                .count();

        long urgent = myRequests.stream()
                .filter(r -> r.getPriority() == PriorityLevel.URGENT ||
                        r.getPriority() == PriorityLevel.EMERGENCY)
                .count();

        long cancelled = myRequests.stream()
                .filter(r -> r.getStatus() == RequestStatus.CANCELLED)
                .count();

        VBox assignedCard = createStatCard("Assigned Tasks", String.valueOf(assigned), "stat-card-value-primary", "📋");
        VBox inProgressCard = createStatCard("In Progress", String.valueOf(inProgress), "stat-card-value-warning", "⚙️");
        VBox completedCard = createStatCard("Completed Today", String.valueOf(completed), "stat-card-value-success", "✅");
        VBox urgentCard = createStatCard("Urgent", String.valueOf(urgent), "stat-card-value-alert", "🚨");
        VBox cancelledCard = createStatCard("Cancelled", String.valueOf(cancelled), "stat-card-value-danger", "❌");

        statsBox.getChildren().addAll(assignedCard, inProgressCard, urgentCard, completedCard, cancelledCard);
        return statsBox;
    }

    private VBox createStatCard(String title, String value, String valueStyleClass, String icon) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(20));
        card.setPrefWidth(220);
        card.setAlignment(Pos.TOP_LEFT);
        card.getStyleClass().addAll("card", "stat-card");

        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font(24));

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 13));
        titleLabel.getStyleClass().add("stat-card-title");

        headerBox.getChildren().addAll(iconLabel, titleLabel);

        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        valueLabel.getStyleClass().addAll("stat-card-value", valueStyleClass);

        card.getChildren().addAll(headerBox, valueLabel);

        return card;
    }

    private VBox createRequestsSection() {
        VBox section = new VBox(15);
        section.setFillWidth(true);

        HBox headerBox = new HBox(20);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label sectionTitle = new Label("My Assigned Requests");
        sectionTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        sectionTitle.getStyleClass().add("section-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        ComboBox<String> filterBox = new ComboBox<>();
        filterBox.getItems().addAll("All Tasks", "Assigned", "In Progress", "Urgent Only");
        filterBox.setValue("All Tasks");
        filterBox.getStyleClass().add("filter-box");
        filterBox.setOnAction(e -> filterRequests(filterBox.getValue()));

        Button refreshBtn = new Button("🔄 Refresh");
        refreshBtn.getStyleClass().addAll("button", "button-primary", "refresh-button");
        refreshBtn.setOnAction(e -> loadRequests());

        headerBox.getChildren().addAll(sectionTitle, spacer, filterBox, refreshBtn);

        requestTable = new TableView<>();
        requestTable.getStyleClass().add("request-table");
        requestTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<MaintenanceRequest, String> idCol = new TableColumn<>("Request ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("requestId"));
        idCol.setPrefWidth(100);
        idCol.setCellFactory(column -> createCenteredTextCell());

        TableColumn<MaintenanceRequest, String> aptCol = new TableColumn<>("Apartment");
        aptCol.setCellValueFactory(new PropertyValueFactory<>("apartmentNumber"));
        aptCol.setPrefWidth(100);
        aptCol.setCellFactory(column -> createCenteredTextCell());

        TableColumn<MaintenanceRequest, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        categoryCol.setPrefWidth(130);

        TableColumn<MaintenanceRequest, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        descCol.setPrefWidth(280);
        descCol.setCellFactory(column -> new TableCell<>() {
            private final Label label = new Label();

            {
                label.setWrapText(true);
                label.setMaxWidth(Double.MAX_VALUE);
                label.getStyleClass().add("request-description");
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(null);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    label.setText(item);
                    setGraphic(label);
                    setPrefHeight(Control.USE_COMPUTED_SIZE);
                }
            }
        });

        // CORRECTED Priority Column - uses PriorityLevel type instead of String
        TableColumn<MaintenanceRequest, PriorityLevel> priorityCol = new TableColumn<>("Priority");
        priorityCol.setCellValueFactory(new PropertyValueFactory<>("priority"));
        priorityCol.setPrefWidth(100);
        priorityCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(PriorityLevel item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("priority-badge", "priority-emergency", "priority-urgent",
                        "priority-high", "priority-medium", "priority-low");
                if (empty || item == null) {
                    setText(null);
                    setAlignment(Pos.CENTER);
                    setGraphic(null);
                } else {
                    setText(item.getDisplayName());
                    setAlignment(Pos.CENTER);
                    setGraphic(null);
                    getStyleClass().add("priority-badge");
                    if (item == PriorityLevel.EMERGENCY) {
                        getStyleClass().add("priority-emergency");
                    } else if (item == PriorityLevel.URGENT) {
                        getStyleClass().add("priority-urgent");
                    } else if (item == PriorityLevel.HIGH) {
                        getStyleClass().add("priority-high");
                    } else if (item == PriorityLevel.MEDIUM) {
                        getStyleClass().add("priority-medium");
                    } else {
                        getStyleClass().add("priority-low");
                    }
                }
            }
        });

        // CORRECTED Status Column - uses RequestStatus type instead of String
        TableColumn<MaintenanceRequest, RequestStatus> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(120);
        statusCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(RequestStatus item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("status-badge", "status-completed", "status-in-progress",
                        "status-reopened", "status-assigned", "status-cancelled", "status-default");
                if (empty || item == null) {
                    setText(null);
                    setAlignment(Pos.CENTER);
                    setGraphic(null);
                } else {
                    setText(item.getDisplayName());
                    setAlignment(Pos.CENTER);
                    setGraphic(null);
                    getStyleClass().add("status-badge");
                    if (item == RequestStatus.COMPLETED) {
                        getStyleClass().add("status-completed");
                    } else if (item == RequestStatus.IN_PROGRESS) {
                        getStyleClass().add("status-in-progress");
                    } else if (item == RequestStatus.REOPENED) {
                        getStyleClass().add("status-reopened");
                    } else if (item == RequestStatus.ASSIGNED) {
                        getStyleClass().add("status-assigned");
                    } else if (item == RequestStatus.CANCELLED) {
                        getStyleClass().add("status-cancelled");
                    } else {
                        getStyleClass().add("status-default");
                    }
                }
            }
        });

        TableColumn<MaintenanceRequest, String> dateCol = new TableColumn<>("Submitted");
        dateCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getSubmissionDate().format(
                                DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm"))
                )
        );
        dateCol.setPrefWidth(150);
        dateCol.setCellFactory(column -> createCenteredTextCell());

        TableColumn<MaintenanceRequest, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setPrefWidth(180);
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button startBtn = new Button("Start");
            private final Button completeBtn = new Button("Complete");
            private final Button viewBtn = new Button("View");
            private final HBox buttonBox = new HBox(5);

            {
                startBtn.getStyleClass().addAll("button", "button-primary", "table-action-button");
                completeBtn.getStyleClass().addAll("button", "button-success", "table-action-button");
                viewBtn.getStyleClass().addAll("button", "button-secondary", "table-action-button");

                startBtn.setOnAction(event -> {
                    MaintenanceRequest request = getTableView().getItems().get(getIndex());
                    startWork(request);
                });

                completeBtn.setOnAction(event -> {
                    MaintenanceRequest request = getTableView().getItems().get(getIndex());
                    showCompleteDialog(request);
                });

                viewBtn.setOnAction(event -> {
                    MaintenanceRequest request = getTableView().getItems().get(getIndex());
                    showRequestDetails(request);
                });

                buttonBox.setAlignment(Pos.CENTER);
                buttonBox.getStyleClass().add("action-button-group");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    MaintenanceRequest request = getTableView().getItems().get(getIndex());
                    buttonBox.getChildren().clear();

                    if (request.getStatus() == RequestStatus.ASSIGNED) {
                        buttonBox.getChildren().addAll(startBtn, viewBtn);
                    } else if (request.getStatus() == RequestStatus.IN_PROGRESS || request.getStatus() == RequestStatus.REOPENED) {
                        buttonBox.getChildren().addAll(completeBtn, viewBtn);
                    } else if (request.getStatus() == RequestStatus.COMPLETED) {
                        buttonBox.getChildren().add(viewBtn);
                    } else {
                        buttonBox.getChildren().add(viewBtn);
                    }

                    setGraphic(buttonBox);
                }
            }
        });

        // Old version
//        requestTable.getColumns().addAll(idCol, aptCol, categoryCol, descCol,
//                priorityCol, statusCol, dateCol, actionCol);

        requestTable.getColumns().setAll(java.util.Arrays.asList(
                idCol, aptCol, categoryCol, descCol, priorityCol, statusCol, dateCol, actionCol
        ));

        loadRequests();

        // Empty state
        Label emptyLabel = new Label("No requests assigned yet");
        emptyLabel.setFont(Font.font("Arial", 14));
        emptyLabel.getStyleClass().add("table-empty-placeholder");
        requestTable.setPlaceholder(emptyLabel);

        requestTable.setMaxHeight(Double.MAX_VALUE); // allow vertical growth
        VBox.setVgrow(requestTable, Priority.ALWAYS); // already present is fine; keep it

        section.getChildren().addAll(headerBox, requestTable);
        VBox.setVgrow(requestTable, Priority.ALWAYS);

        return section;
    }

    private TableCell<MaintenanceRequest, String> createCenteredTextCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item);
                    setGraphic(null);
                }
                setAlignment(Pos.CENTER);
            }
        };
    }

    private void loadRequests() {
        MaintenanceStaff staff = (MaintenanceStaff) authService.getCurrentUser();
        List<MaintenanceRequest> requests = requestDAO.getRequestsByStaff(staff.getStaffId());
        requestTable.setItems(FXCollections.observableArrayList(requests));
    }

    private void filterRequests(String filter) {
        MaintenanceStaff staff = (MaintenanceStaff) authService.getCurrentUser();
        List<MaintenanceRequest> requests = requestDAO.getRequestsByStaff(staff.getStaffId());

        switch (filter) {
            case "Assigned":
                requests = requests.stream()
                        .filter(r -> r.getStatus() == RequestStatus.ASSIGNED)
                        .toList();
                break;
            case "In Progress":
                requests = requests.stream()
                        .filter(r -> r.getStatus() == RequestStatus.IN_PROGRESS || r.getStatus() == RequestStatus.REOPENED)
                        .toList();
                break;
            case "Urgent Only":
                requests = requests.stream()
                        .filter(r -> r.getPriority() == PriorityLevel.URGENT ||
                                r.getPriority() == PriorityLevel.EMERGENCY)
                        .toList();
                break;
            default:
                // All tasks - no filter
                break;
        }

        requestTable.setItems(FXCollections.observableArrayList(requests));
    }

    private void startWork(MaintenanceRequest request) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Start Work");
        confirm.setHeaderText("Start working on this request?");
        confirm.setContentText("Request #" + request.getRequestId() + " will be marked as 'In Progress'");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                request.setStatus(RequestStatus.IN_PROGRESS);
                request.setLastUpdated(java.time.LocalDateTime.now());

                if (requestDAO.updateRequest(request)) {
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

    private void showCompleteDialog(MaintenanceRequest request) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Complete Request");
        dialog.setHeaderText("Mark Request #" + request.getRequestId() + " as Complete");

        ButtonType completeButtonType = new ButtonType("Complete", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(completeButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));
        grid.getStyleClass().add("dialog-grid");

        TextArea resolutionArea = new TextArea();
        resolutionArea.setPromptText("Describe what was done to resolve the issue...");
        resolutionArea.setPrefRowCount(5);
        resolutionArea.setPrefColumnCount(40);
        resolutionArea.setWrapText(true);

        TextField hoursField = new TextField();
        hoursField.setPromptText("Hours spent");

        TextField costField = new TextField();
        costField.setPromptText("Cost (if applicable)");

        grid.add(new Label("Resolution Notes:"), 0, 0);
        grid.add(resolutionArea, 0, 1, 2, 1);
        grid.add(new Label("Hours Spent:"), 0, 2);
        grid.add(hoursField, 1, 2);
        grid.add(new Label("Actual Cost ($):"), 0, 3);
        grid.add(costField, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == completeButtonType) {
                if (!resolutionArea.getText().isEmpty()) {
                    return resolutionArea.getText();
                } else {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Missing Information");
                    alert.setContentText("Please provide resolution notes");
                    alert.showAndWait();
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(resolution -> {
            try {
                if (!costField.getText().isEmpty()) {
                    double cost = Double.parseDouble(costField.getText());
                    request.setActualCost(cost);
                }
            } catch (NumberFormatException e) {
                // Ignore if cost is not a valid number
            }

            request.close(resolution);

            if (requestDAO.updateRequest(request)) {
                Alert success = new Alert(Alert.AlertType.INFORMATION);
                success.setTitle("Success");
                success.setHeaderText("Request Completed!");
                success.setContentText("Request #" + request.getRequestId() +
                        " has been marked as completed successfully.");
                success.showAndWait();
                loadRequests();
            } else {
                showError("Failed to update request");
            }
        });
    }

    private void showRequestDetails(MaintenanceRequest request) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Request Details");
        dialog.setHeaderText("Request #" + request.getRequestId());

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));
        grid.getStyleClass().add("details-grid");

        int row = 0;

        addDetailRow(grid, row++, "Request ID:", request.getRequestId());
        addDetailRow(grid, row++, "Apartment:", request.getApartmentNumber());
        addDetailRow(grid, row++, "Category:", request.getCategory().getDisplayName());
        addDetailRow(grid, row++, "Priority:", request.getPriority().getDisplayName());
        addDetailRow(grid, row++, "Status:", request.getStatus().getDisplayName());
        addDetailRow(grid, row++, "Submitted:",
                request.getSubmissionDate().format(DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm")));

        if (request.getScheduledDate() != null) {
            addDetailRow(grid, row++, "Scheduled:",
                    request.getScheduledDate().format(DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm")));
        }

        Label descLabel = new Label("Description:");
        descLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        descLabel.getStyleClass().add("detail-section-label");
        TextArea descArea = new TextArea(request.getDescription());
        descArea.setWrapText(true);
        descArea.setEditable(false);
        descArea.setPrefRowCount(3);

        grid.add(descLabel, 0, row);
        grid.add(descArea, 1, row++);

        if (request.getResolutionNotes() != null && !request.getResolutionNotes().isEmpty()) {
            Label resLabel = new Label("Resolution:");
            resLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            resLabel.getStyleClass().add("detail-section-label");
            TextArea resArea = new TextArea(request.getResolutionNotes());
            resArea.setWrapText(true);
            resArea.setEditable(false);
            resArea.setPrefRowCount(3);

            grid.add(resLabel, 0, row);
            grid.add(resArea, 1, row);
        }

        ScrollPane scrollPane = new ScrollPane(grid);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefSize(500, 400);

        dialog.getDialogPane().setContent(scrollPane);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        dialog.showAndWait();
    }

    private void addDetailRow(GridPane grid, int row, String label, String value) {
        Label lblLabel = new Label(label);
        lblLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        lblLabel.getStyleClass().add("detail-label");

        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Arial", 12));
        valueLabel.setWrapText(true);
        valueLabel.getStyleClass().add("detail-value");

        grid.add(lblLabel, 0, row);
        grid.add(valueLabel, 1, row);
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Operation Failed");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
