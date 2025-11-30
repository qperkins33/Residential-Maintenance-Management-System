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

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class StaffDashboardController {
    private final ViewFactory viewFactory;
    private final AuthenticationService authService;
    private final MaintenanceRequestDAO requestDAO;
    private TableView<MaintenanceRequest> requestTable;
    private Label workloadLabel;
    private HBox statsBox;

    public StaffDashboardController(ViewFactory viewFactory) {
        this.viewFactory = viewFactory;
        this.authService = AuthenticationService.getInstance();
        this.requestDAO = new MaintenanceRequestDAO();
    }

    public void createDashboardUI(AnchorPane root) {
        DashboardUIHelper.applyRootStyles(root, getClass());

        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(0));

        HBox topBar = createTopBar();
        mainLayout.setTop(topBar);

        VBox sidebar = createSidebar();
        mainLayout.setLeft(sidebar);

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
        titleLabel.getStyleClass().add("top-bar-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

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

    private VBox createSidebar() {
        VBox sidebar = new VBox(15);
        sidebar.setPadding(new Insets(20));
        sidebar.setStyle("-fx-background-color: #2c3e50;");
        sidebar.setPrefWidth(250);

        Label menuLabel = new Label("MENU");
        menuLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        menuLabel.setTextFill(Color.web("#95a5a6"));

        Button dashboardBtn = DashboardUIHelper.createSidebarButton("📊 Dashboard", true);
        Button assignedBtn = DashboardUIHelper.createSidebarButton("📋 Assigned Tasks", false);
        Button historyBtn = DashboardUIHelper.createSidebarButton("📜 History", false);
        Button profileBtn = DashboardUIHelper.createSidebarButton("👤 Profile", false);
        Button settingsBtn = DashboardUIHelper.createSidebarButton("⚙️ Settings", false);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        VBox availabilityBox = createAvailabilityToggle();

        sidebar.getChildren().addAll(menuLabel, dashboardBtn, assignedBtn, historyBtn, profileBtn, settingsBtn, spacer, availabilityBox);
        return sidebar;
    }

    private VBox createAvailabilityToggle() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(15));
        box.setStyle("-fx-background-color: #34495e; -fx-background-radius: 8;");

        Label statusLabel = new Label("Availability Status");
        statusLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        statusLabel.setTextFill(Color.web("#ecf0f1"));

        MaintenanceStaff staff = (MaintenanceStaff) authService.getCurrentUser();

        CheckBox availableCheck = new CheckBox("Available for assignments");
        availableCheck.setTextFill(Color.WHITE);
        availableCheck.setSelected(staff.isAvailable());
        availableCheck.setStyle("-fx-font-size: 12px;");

        availableCheck.setOnAction(e -> {
            staff.updateAvailability(availableCheck.isSelected());
            showStatusNotification(availableCheck.isSelected());
        });

        workloadLabel = new Label();
        workloadLabel.setFont(Font.font("Arial", 11));
        workloadLabel.setTextFill(Color.web("#bdc3c7"));

        refreshWorkload();

        box.getChildren().addAll(statusLabel, availableCheck, workloadLabel);
        return box;
    }

    private void showStatusNotification(boolean available) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Status Updated");
        alert.setHeaderText(null);
        alert.setContentText("Availability status updated to: " + (available ? "Available" : "Unavailable"));
        alert.showAndWait();
    }

    private VBox createCenterContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        content.setFillWidth(true);

        VBox welcomeBox = createWelcomeSection();
        statsBox = new HBox(20);
        VBox requestsSection = createRequestsSection();
        VBox.setVgrow(requestsSection, Priority.ALWAYS);

        content.getChildren().addAll(welcomeBox, statsBox, requestsSection);

        refreshStats();
        return content;
    }

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

    private void refreshStats() {
        if (statsBox == null) return;

        statsBox.getChildren().clear();

        MaintenanceStaff staff = (MaintenanceStaff) authService.getCurrentUser();
        List<MaintenanceRequest> myRequests = requestDAO.getRequestsByStaff(staff.getStaffId());

        long notStarted = myRequests.stream().filter(this::isNotStarted).count();
        long inProgress = myRequests.stream().filter(this::isInProgress).count();
        long completed = myRequests.stream().filter(this::isCompleted).count();
        long cancelled = myRequests.stream().filter(this::isCancelled).count();

        long urgent = myRequests.stream()
                .filter(r -> (r.getPriority() == PriorityLevel.URGENT || r.getPriority() == PriorityLevel.EMERGENCY) && !isCompleted(r) && !isCancelled(r))
                .count();

        VBox totalCard = DashboardUIHelper.createStatCard("Total Requests", String.valueOf(myRequests.size()), "#667eea", "📋");
        VBox urgentCard = DashboardUIHelper.createStatCard("Urgent (Active)", String.valueOf(urgent), "#f44336", "🚨");
        VBox inProgressCard = DashboardUIHelper.createStatCard("In Progress", String.valueOf(inProgress), "#ff9800", "👷");
        VBox pendingCard = DashboardUIHelper.createStatCard("Not Started", String.valueOf(notStarted), "#2196f3", "⏸️");
        VBox completedCard = DashboardUIHelper.createStatCard("Completed", String.valueOf(completed), "#4caf50", "✅");
        VBox cancelledCard = DashboardUIHelper.createStatCard("Cancelled", String.valueOf(cancelled), "#f44336", "❌");

        statsBox.getChildren().addAll(totalCard, urgentCard, inProgressCard, pendingCard, completedCard, cancelledCard);
    }

    private HBox createStatsCards() {
        return statsBox;
    }

    private VBox createRequestsSection() {
        VBox section = new VBox(15);
        section.setFillWidth(true);

        HBox headerBox = new HBox(20);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label sectionTitle = new Label("My Assigned Requests");
        sectionTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        ComboBox<String> filterBox = new ComboBox<>();
        filterBox.getItems().addAll("All Tasks", "Assigned", "In Progress", "Urgent Only", "Cancelled");
        filterBox.setValue("All Tasks");
        filterBox.setStyle("-fx-background-radius: 5; -fx-padding: 5 10;");
        filterBox.setOnAction(e -> filterRequests(filterBox.getValue()));

        Button refreshBtn = new Button("🔄 Refresh");
        refreshBtn.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; -fx-padding: 8 15; -fx-background-radius: 5; -fx-cursor: hand;");
        refreshBtn.setOnAction(e -> loadRequests());

        headerBox.getChildren().addAll(sectionTitle, spacer, filterBox, refreshBtn);

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

        TableColumn<MaintenanceRequest, ?> priorityCol = DashboardUIHelper.createPriorityColumn();
        TableColumn<MaintenanceRequest, ?> statusCol = DashboardUIHelper.createStatusColumn();
        TableColumn<MaintenanceRequest, ?> dateCol = DashboardUIHelper.createSubmittedDateColumn();

        TableColumn<MaintenanceRequest, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setPrefWidth(260);
        actionCol.setMinWidth(260);
        actionCol.setResizable(false);
        actionCol.setStyle("-fx-alignment: CENTER;");
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button updateBtn = new Button("Update");
            private final Button startBtn = new Button("Start");
            private final Button completeBtn = new Button("Complete");
            private final Button viewBtn = new Button("View");
            private final HBox buttonBox = new HBox(5);

            {
                String btnStyle = "-fx-background-color: #667eea; -fx-text-fill: white; -fx-padding: 5 12; -fx-background-radius: 3; -fx-cursor: hand;";
                updateBtn.setStyle(btnStyle);
                startBtn.setStyle(btnStyle);
                completeBtn.setStyle(btnStyle);
                viewBtn.setStyle(btnStyle);

                updateBtn.setOnAction(e -> {
                    MaintenanceRequest request = getTableView().getItems().get(getIndex());
                    DashboardUIHelper.showStaffUpdateDialog(request, requestDAO, StaffDashboardController.this::loadRequests);
                });

                startBtn.setOnAction((ActionEvent event) -> {
                    MaintenanceRequest request = getTableView().getItems().get(getIndex());
                    startWork(request);
                });

                completeBtn.setOnAction((ActionEvent event) -> {
                    MaintenanceRequest request = getTableView().getItems().get(getIndex());
                    showCompleteDialog(request);
                });

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
                } else {
                    MaintenanceRequest request = getTableView().getItems().get(getIndex());
                    buttonBox.getChildren().clear();

                    if (request.getStatus() == RequestStatus.ASSIGNED) {
                        buttonBox.getChildren().addAll(startBtn, viewBtn);
                    } else if (isInProgress(request)) {
                        buttonBox.getChildren().addAll(completeBtn, updateBtn, viewBtn);
                    } else if (isCompleted(request)) {
                        buttonBox.getChildren().addAll(updateBtn, viewBtn);
                    } else {
                        buttonBox.getChildren().addAll(updateBtn, viewBtn);
                    }

                    setGraphic(buttonBox);
                }
            }
        });

        requestTable.getColumns().setAll(java.util.Arrays.asList(
                idCol, aptCol, categoryCol, descCol, priorityCol, statusCol, dateCol, actionCol
        ));

        loadRequests();

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

    private void loadRequests() {
        MaintenanceStaff staff = (MaintenanceStaff) authService.getCurrentUser();
        List<MaintenanceRequest> requests = requestDAO.getRequestsByStaff(staff.getStaffId());
        requestTable.setItems(FXCollections.observableArrayList(requests));
        refreshWorkload();
        refreshStats();
    }

    private void filterRequests(String filter) {
        MaintenanceStaff staff = (MaintenanceStaff) authService.getCurrentUser();
        List<MaintenanceRequest> requests = requestDAO.getRequestsByStaff(staff.getStaffId());

        switch (filter) {
            case "Assigned" -> requests = requests.stream().filter(r -> r.getStatus() == RequestStatus.ASSIGNED).toList();
            case "In Progress" -> requests = requests.stream().filter(this::isInProgress).toList();
            case "Urgent Only" -> requests = requests.stream()
                    .filter(r -> (r.getPriority() == PriorityLevel.URGENT || r.getPriority() == PriorityLevel.EMERGENCY) && !isCompleted(r) && !isCancelled(r))
                    .toList();
            case "Cancelled" -> requests = requests.stream().filter(this::isCancelled).toList();
            default -> { }
        }

        requestTable.setItems(FXCollections.observableArrayList(requests));
        refreshWorkload();
    }

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
                    String tech = resolveTechnicianName(request);

                    requestDAO.findTenantEmailByRequestId(request.getRequestId()).ifPresent(to ->
                            CompletableFuture.runAsync(() -> {
                                String subject = "Request " + request.getRequestId() + " status updated: In progress";
                                String body =
                                        "Hello,\n\n" +
                                                "Your maintenance request was updated.\n\n" +
                                                "Request ID: " + request.getRequestId() + "\n" +
                                                "Status:     In progress\n" +
                                                "Apartment:  " + nullToDash(request.getApartmentNumber()) + "\n" +
                                                "Technician: " + tech + "\n\n" +
                                                "Reply to this email if you have questions.\n" +
                                                "Residential Maintenance";
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
        dialog.getDialogPane().setStyle("-fx-background-color: white;");

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
            } catch (NumberFormatException ignored) { }

            request.close(resolution);

            if (requestDAO.updateRequest(request)) {
                String tech = resolveTechnicianName(request);

                requestDAO.findTenantEmailByRequestId(request.getRequestId()).ifPresent(to ->
                        CompletableFuture.runAsync(() -> {
                            String subject = "Request " + request.getRequestId() + " status updated: Completed";
                            String body =
                                    "Hello,\n\n" +
                                            "Your maintenance request is now completed.\n\n" +
                                            "Request ID: " + request.getRequestId() + "\n" +
                                            "Status:     Completed\n" +
                                            "Apartment:  " + nullToDash(request.getApartmentNumber()) + "\n" +
                                            "Technician: " + tech + "\n\n" +
                                            "Resolution: " + resolution + "\n\n" +
                                            "Reply to this email if you have questions.\n" +
                                            "Residential Maintenance";
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

    private String resolveTechnicianName(MaintenanceRequest r) {
        var u = authService.getCurrentUser();
        if (u instanceof MaintenanceStaff ms && ms.getFullName() != null && !ms.getFullName().isBlank()) {
            return ms.getFullName();
        }
        // fallback to DB by assigned staff id if available
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

    private static String nullToDash(String s) {
        return s == null ? "-" : s;
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Operation Failed");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean isNotStarted(MaintenanceRequest r) {
        return r.getStatus() == RequestStatus.SUBMITTED || r.getStatus() == RequestStatus.ASSIGNED;
    }

    private boolean isInProgress(MaintenanceRequest r) {
        return r.getStatus() == RequestStatus.IN_PROGRESS || r.getStatus() == RequestStatus.REOPENED;
    }

    private boolean isCompleted(MaintenanceRequest r) {
        return r.getStatus() == RequestStatus.COMPLETED;
    }

    private boolean isCancelled(MaintenanceRequest r) {
        return r.getStatus() == RequestStatus.CANCELLED;
    }
}
