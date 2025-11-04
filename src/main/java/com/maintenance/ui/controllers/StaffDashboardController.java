package com.maintenance.ui.controllers;

import com.maintenance.dao.MaintenanceRequestDAO;
import com.maintenance.enums.CategoryType;
import com.maintenance.enums.PriorityLevel;
import com.maintenance.enums.RequestStatus;
import com.maintenance.models.MaintenanceRequest;
import com.maintenance.models.MaintenanceStaff;
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
import java.util.Objects;

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
        root.getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/css/styles.css")).toExternalForm()
        );
        root.getStyleClass().add("app-root");

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
        titleLabel.getStyleClass().add("top-bar-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        MaintenanceStaff staff = (MaintenanceStaff) authService.getCurrentUser();
        Label userLabel = new Label("👤 " + staff.getFullName() + " (Maintenance Staff)");
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

    private VBox createSidebar() {
        VBox sidebar = new VBox(15);
        sidebar.setPadding(new Insets(20));
        sidebar.setStyle("-fx-background-color: #2c3e50;");
        sidebar.setPrefWidth(250);

        Label menuLabel = new Label("MENU");
        menuLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        menuLabel.setTextFill(Color.web("#95a5a6"));

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

        if (active) {
            btn.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; " +
                    "-fx-padding: 12 15; -fx-background-radius: 5;");
        } else {
            btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #ecf0f1; " +
                    "-fx-padding: 12 15; -fx-background-radius: 5;");
        }

        btn.setOnMouseEntered(e -> {
            if (!active) {
                btn.setStyle("-fx-background-color: #34495e; -fx-text-fill: white; " +
                        "-fx-padding: 12 15; -fx-background-radius: 5;");
            }
        });
        btn.setOnMouseExited(e -> {
            if (!active) {
                btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #ecf0f1; " +
                        "-fx-padding: 12 15; -fx-background-radius: 5;");
            }
        });

        return btn;
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

        Label workloadLabel = new Label("Workload: " + staff.getCurrentWorkload() +
                "/" + staff.getMaxCapacity());
        workloadLabel.setFont(Font.font("Arial", 11));
        workloadLabel.setTextFill(Color.web("#bdc3c7"));

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
        welcomeLabel.setTextFill(Color.web("#2c3e50"));

        Label dateLabel = new Label("Today: " +
                java.time.LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy")));
        dateLabel.setFont(Font.font("Arial", 13));
        dateLabel.setTextFill(Color.GRAY);

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

        VBox assignedCard = createStatCard("Assigned Tasks", String.valueOf(assigned), "#667eea", "📋");
        VBox inProgressCard = createStatCard("In Progress", String.valueOf(inProgress), "#ff9800", "⚙️");
        VBox completedCard = createStatCard("Completed Today", String.valueOf(completed), "#4caf50", "✅");
        VBox urgentCard = createStatCard("Urgent", String.valueOf(urgent), "#f44336", "🚨");
        VBox cancelledCard = createStatCard("Cancelled", String.valueOf(cancelled), "#f44336", "❌");

        statsBox.getChildren().addAll(assignedCard, inProgressCard, urgentCard, completedCard, cancelledCard);
        return statsBox;
    }

    private VBox createStatCard(String title, String value, String color, String icon) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");
        card.setPrefWidth(220);
        card.setAlignment(Pos.TOP_LEFT);

        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font(24));

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 13));
        titleLabel.setTextFill(Color.GRAY);

        headerBox.getChildren().addAll(iconLabel, titleLabel);

        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        valueLabel.setTextFill(Color.web(color));

        card.getChildren().addAll(headerBox, valueLabel);

        // Hover effect
        card.setOnMouseEntered(e ->
                card.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 15, 0, 0, 5); " +
                        "-fx-cursor: hand;"));
        card.setOnMouseExited(e ->
                card.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);"));

        return card;
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
        filterBox.getItems().addAll("All Tasks", "Assigned", "In Progress", "Urgent Only");
        filterBox.setValue("All Tasks");
        filterBox.setStyle("-fx-background-radius: 5; -fx-padding: 5 10;");
        filterBox.setOnAction(e -> filterRequests(filterBox.getValue()));

        Button refreshBtn = new Button("🔄 Refresh");
        refreshBtn.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; " +
                "-fx-padding: 8 15; -fx-background-radius: 5; -fx-cursor: hand;");
        refreshBtn.setOnAction(e -> loadRequests());

        headerBox.getChildren().addAll(sectionTitle, spacer, filterBox, refreshBtn);

        requestTable = new TableView<>();
        requestTable.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        requestTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

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
        categoryCol.setPrefWidth(130);

        TableColumn<MaintenanceRequest, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        descCol.setPrefWidth(280);
        descCol.setStyle("-fx-wrap-text: true;");

        TableColumn<MaintenanceRequest, PriorityLevel> priorityCol = new TableColumn<>("Priority");
        priorityCol.setCellValueFactory(new PropertyValueFactory<>("priority"));
        priorityCol.setPrefWidth(100);
        priorityCol.setStyle("-fx-alignment: CENTER;");
        priorityCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(PriorityLevel item, boolean empty) {
                super.updateItem(item, empty);
                setText(null);
                setStyle(""); // clear inline in case
                getStyleClass().removeAll("priority-urgent","priority-high","priority-medium","priority-else");
                if (empty || item == null) return;
                setText(item.getDisplayName());
                getStyleClass().add(
                        (item == PriorityLevel.EMERGENCY || item == PriorityLevel.URGENT) ? "priority-urgent" :
                                item == PriorityLevel.HIGH ? "priority-high" :
                                        item == PriorityLevel.MEDIUM ? "priority-medium" : "priority-else"
                );
            }

        });

        TableColumn<MaintenanceRequest, RequestStatus> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(120);
        statusCol.setStyle("-fx-alignment: CENTER;");
        statusCol.setCellFactory(column -> new TableCell<>() {

            private final List<String> statusClasses = List.of(
                    "status-completed",
                    "status-in-progress",
                    "status-reopened",
                    "status-assigned",
                    "status-cancelled",
                    "status-else"
            );

            @Override
            protected void updateItem(RequestStatus item, boolean empty) {
                super.updateItem(item, empty);
                setText(null);
                setStyle("");                      // clear any inline styles
                getStyleClass().removeAll(statusClasses);  // clear old classes
                if (empty || item == null) return;

                setText(item.getDisplayName());
                switch (item) {
                    case COMPLETED -> getStyleClass().add("status-completed");
                    case IN_PROGRESS -> getStyleClass().add("status-in-progress");
                    case REOPENED -> getStyleClass().add("status-reopened");
                    case ASSIGNED -> getStyleClass().add("status-assigned");
                    case CANCELLED -> getStyleClass().add("status-cancelled");
                    default -> getStyleClass().add("status-else");
                }
            }
        });

        TableColumn<MaintenanceRequest, String> dateCol = new TableColumn<>("Submitted");
        dateCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getSubmissionDate().format(
                                DateTimeFormatter.ofPattern("MM/dd/yyyy"))
                )
        );
        dateCol.setPrefWidth(120);
        dateCol.setMinWidth(120);
        dateCol.setResizable(false);

        dateCol.setStyle("-fx-alignment: CENTER;");

        dateCol.setCellFactory(col -> new TableCell<>() {
            private final Label pill = new Label();
            {
                pill.getStyleClass().add("submitted-grey");
                setAlignment(Pos.CENTER);
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(null);
                setGraphic(null);
                if (empty || item == null) return;
                pill.setText(item);
                setGraphic(pill);
            }
        });

        TableColumn<MaintenanceRequest, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setPrefWidth(220); // wider actions column
        actionCol.setMinWidth(220);
        actionCol.setMaxWidth(300);
        actionCol.setResizable(false);
        actionCol.setStyle("-fx-alignment: CENTER;");
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button startBtn = new Button("Start");
            private final Button completeBtn = new Button("Complete");
            private final Button viewBtn = new Button("View");
            private final HBox buttonBox = new HBox(5);

            {
                editBtn.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; " +
                        "-fx-padding: 5 12; -fx-background-radius: 3; -fx-cursor: hand; ");
                startBtn.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; " +
                        "-fx-padding: 5 12; -fx-background-radius: 3; -fx-cursor: hand; ");
                completeBtn.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; " +
                        "-fx-padding: 5 12; -fx-background-radius: 3; -fx-cursor: hand; ");
                viewBtn.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; " +
                        "-fx-padding: 5 12; -fx-background-radius: 3; -fx-cursor: hand; ");

                editBtn.setOnAction(e -> {
                    MaintenanceRequest request = getTableView().getItems().get(getIndex());
                    showEditRequestDialog(request);
                });

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
                        buttonBox.getChildren().addAll(completeBtn, editBtn, viewBtn);
                    } else if (request.getStatus() == RequestStatus.COMPLETED) {
                        buttonBox.getChildren().addAll(editBtn, viewBtn);
                    } else {
                        buttonBox.getChildren().addAll(editBtn, viewBtn);
                    }

                    setGraphic(buttonBox);
                }
            }
        });

        requestTable.getColumns().setAll(java.util.Arrays.asList(
                idCol, aptCol, categoryCol, descCol, priorityCol, statusCol, dateCol, actionCol
        ));

        loadRequests();

        // Empty state
        Label emptyLabel = new Label("No requests assigned yet");
        emptyLabel.setFont(Font.font("Arial", 14));
        emptyLabel.setTextFill(Color.GRAY);
        requestTable.setPlaceholder(emptyLabel);

        requestTable.setMaxHeight(Double.MAX_VALUE); // allow vertical growth
        VBox.setVgrow(requestTable, Priority.ALWAYS); // already present is fine; keep it

        section.getChildren().addAll(headerBox, requestTable);
        VBox.setVgrow(requestTable, Priority.ALWAYS);

        return section;
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
        grid.setStyle("-fx-background-color: white;");

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
        TextArea descArea = new TextArea(request.getDescription());
        descArea.setWrapText(true);
        descArea.setEditable(false);
        descArea.setPrefRowCount(3);

        grid.add(descLabel, 0, row);
        grid.add(descArea, 1, row++);

        if (request.getResolutionNotes() != null && !request.getResolutionNotes().isEmpty()) {
            Label resLabel = new Label("Resolution:");
            resLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
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
        lblLabel.setTextFill(Color.web("#555"));

        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Arial", 12));
        valueLabel.setWrapText(true);

        grid.add(lblLabel, 0, row);
        grid.add(valueLabel, 1, row);
    }

    private void showEditRequestDialog(MaintenanceRequest request) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Edit Maintenance Request");
        dialog.setHeaderText("Update your request");

        ButtonType saveBtnType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtnType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        ComboBox<CategoryType> categoryBox = new ComboBox<>();
        categoryBox.getItems().addAll(CategoryType.values());
        categoryBox.setValue(request.getCategory());

        TextArea descArea = new TextArea(request.getDescription());
        descArea.setPrefRowCount(5);

        ComboBox<PriorityLevel> priorityBox = new ComboBox<>();
        priorityBox.getItems().addAll(PriorityLevel.values());
        priorityBox.setValue(request.getPriority());

        HBox statusButtons = new HBox(10);
        statusButtons.setAlignment(Pos.CENTER_LEFT);

        final RequestStatus[] selectedStatus = {request.getStatus()};

        if (request.getStatus() == RequestStatus.COMPLETED || request.getStatus() == RequestStatus.CANCELLED) {
            Button reopenBtn = new Button("Reopen Request");
            cancelReopenButton(reopenBtn, "#4caf50", "#43a047", "#388e3c"); // green, hover, pressed
            reopenBtn.setOnAction(e -> {
                selectedStatus[0] = RequestStatus.REOPENED;
                new Alert(Alert.AlertType.INFORMATION, "Request reopened.").showAndWait();
            });
            statusButtons.getChildren().add(reopenBtn);
        } else if (request.getStatus() != RequestStatus.CANCELLED) {
            Button cancelBtn = new Button("Cancel Request");
            cancelReopenButton(cancelBtn, "#e53935", "#d32f2f", "#c62828"); // red, hover, pressed
            cancelBtn.setOnAction(e -> {
                selectedStatus[0] = RequestStatus.CANCELLED;
                new Alert(Alert.AlertType.INFORMATION, "Request cancelled.").showAndWait();
            });
            statusButtons.getChildren().add(cancelBtn);
        }

        grid.add(new Label("Category:"), 0, 0);
        grid.add(categoryBox, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descArea, 1, 1);
        grid.add(new Label("Priority:"), 0, 2);
        grid.add(priorityBox, 1, 2);
        grid.add(new Label("Status:"), 0, 3);
        grid.add(statusButtons, 1, 3);

        dialog.getDialogPane().setContent(grid);

        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveBtnType);
        boolean[] updated = {false};
        saveButton.addEventFilter(ActionEvent.ACTION, event -> {
            if (categoryBox.getValue() == null || priorityBox.getValue() == null || descArea.getText().isBlank()) {
                new Alert(Alert.AlertType.WARNING, "All fields are required.").showAndWait();
                event.consume();
                return;
            }

            request.setCategory(categoryBox.getValue());
            request.setDescription(descArea.getText().trim());
            request.setPriority(priorityBox.getValue());
            request.setStatus(selectedStatus[0]);
            request.setLastUpdated(LocalDateTime.now());

            if (!requestDAO.updateRequest(request)) {
                new Alert(Alert.AlertType.ERROR, "Unable to update request. Please try again.").showAndWait();
                event.consume();
            } else {
                updated[0] = true;
            }
        });

        dialog.showAndWait();

        if (updated[0]) {
            loadRequests();
            new Alert(Alert.AlertType.INFORMATION, "Request updated successfully.").showAndWait();
        }
    }

    private static void cancelReopenButton(Button b, String base, String hover, String pressed) {
        b.setStyle("-fx-background-color: " + base + "; -fx-text-fill: white; -fx-padding: 6 12; -fx-background-radius: 4;");
        b.setOnMouseEntered(ev -> b.setStyle("-fx-background-color: " + hover + "; -fx-text-fill: white; -fx-padding: 6 12; -fx-background-radius: 4;"));
        b.setOnMouseExited(ev  -> b.setStyle("-fx-background-color: " + base  + "; -fx-text-fill: white; -fx-padding: 6 12; -fx-background-radius: 4;"));
        b.setOnMousePressed(ev -> b.setStyle("-fx-background-color: " + pressed + "; -fx-text-fill: white; -fx-padding: 6 12; -fx-background-radius: 4;"));
        b.setOnMouseReleased(ev-> b.setStyle("-fx-background-color: " + hover + "; -fx-text-fill: white; -fx-padding: 6 12; -fx-background-radius: 4;"));
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Operation Failed");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
