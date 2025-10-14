package com.maintenance.ui.controllers;

import com.maintenance.dao.MaintenanceRequestDAO;
import com.maintenance.enums.CategoryType;
import com.maintenance.enums.PriorityLevel;
import com.maintenance.enums.RequestStatus;
import com.maintenance.models.MaintenanceRequest;
import com.maintenance.models.Tenant;
import com.maintenance.service.AuthenticationService;
import com.maintenance.service.TicketingSystem;
import com.maintenance.ui.views.ViewFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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

public class TenantDashboardController {
    private final ViewFactory viewFactory;
    private final AuthenticationService authService;
    private final TicketingSystem ticketingSystem;
    private final MaintenanceRequestDAO requestDAO;
    private TableView<MaintenanceRequest> requestTable;

    public TenantDashboardController(ViewFactory viewFactory) {
        this.viewFactory = viewFactory;
        this.authService = AuthenticationService.getInstance();
        this.ticketingSystem = new TicketingSystem();
        this.requestDAO = new MaintenanceRequestDAO();
    }

    public void createDashboardUI(AnchorPane root) {
        root.setStyle("-fx-background-color: #f5f7fa;");

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
        topBar.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; " +
                "-fx-border-width: 0 0 1 0;");

        Label titleLabel = new Label("🏢 Residential Maintenance");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        titleLabel.setTextFill(Color.web("#667eea"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Tenant tenant = (Tenant) authService.getCurrentUser();
        Label userLabel = new Label("👤 " + tenant.getFullName() + " (Apt: " + tenant.getApartmentNumber() + ")");
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
        Button requestsBtn = createSidebarButton("📝 My Requests", false);
        Button newRequestBtn = createSidebarButton("➕ New Request", false);

        sidebar.getChildren().addAll(menuLabel, dashboardBtn, requestsBtn, newRequestBtn);
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

    private VBox createCenterContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));

        // Stats cards
        HBox statsBox = createStatsCards();

        // Requests section
        VBox requestsSection = createRequestsSection();

        content.getChildren().addAll(statsBox, requestsSection);
        return content;
    }

    private HBox createStatsCards() {
        HBox statsBox = new HBox(20);

        Tenant tenant = (Tenant) authService.getCurrentUser();
        var requests = requestDAO.getRequestsByTenant(tenant.getUserId());

        long pending = requests.stream()
                .filter(r -> r.getStatus() == RequestStatus.SUBMITTED ||
                        r.getStatus() == RequestStatus.ACKNOWLEDGED ||
                        r.getStatus() == RequestStatus.ASSIGNED ||
                        r.getStatus() == RequestStatus.IN_PROGRESS ||
                        r.getStatus() == RequestStatus.REOPENED)
                .count();

        long completed = requests.stream()
                .filter(r -> r.getStatus() == RequestStatus.COMPLETED ||
                        r.getStatus() == RequestStatus.CLOSED)
                .count();

        VBox totalCard = createStatCard("Total Requests", String.valueOf(requests.size()), "#667eea");
        VBox pendingCard = createStatCard("Pending", String.valueOf(pending), "#ff9800");
        VBox completedCard = createStatCard("Completed", String.valueOf(completed), "#4caf50");

        statsBox.getChildren().addAll(totalCard, pendingCard, completedCard);
        return statsBox;
    }

    private VBox createStatCard(String title, String value, String color) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");
        card.setPrefWidth(200);

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", 12));
        titleLabel.setTextFill(Color.GRAY);

        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        valueLabel.setTextFill(Color.web(color));

        card.getChildren().addAll(titleLabel, valueLabel);
        return card;
    }

    private VBox createRequestsSection() {
        VBox section = new VBox(15);

        HBox headerBox = new HBox(20);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label sectionTitle = new Label("My Maintenance Requests");
        sectionTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button newRequestBtn = new Button("+ New Request");
        newRequestBtn.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; " +
                "-fx-padding: 10 20; -fx-background-radius: 5; -fx-cursor: hand;");
        newRequestBtn.setOnAction(e -> showNewRequestDialog());

        headerBox.getChildren().addAll(sectionTitle, spacer, newRequestBtn);

        requestTable = new TableView<>();
        requestTable.setStyle("-fx-background-color: white; -fx-background-radius: 10;");

        TableColumn<MaintenanceRequest, String> idCol = new TableColumn<>("Request ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("requestId"));
        idCol.setPrefWidth(120);

        TableColumn<MaintenanceRequest, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        categoryCol.setPrefWidth(150);

        TableColumn<MaintenanceRequest, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        descCol.setPrefWidth(300);

        TableColumn<MaintenanceRequest, String> priorityCol = new TableColumn<>("Priority");
        priorityCol.setCellValueFactory(new PropertyValueFactory<>("priority"));
        priorityCol.setPrefWidth(100);

        TableColumn<MaintenanceRequest, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(120);

        TableColumn<MaintenanceRequest, String> dateCol = new TableColumn<>("Submitted");
        dateCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getSubmissionDate().format(DateTimeFormatter.ofPattern("MM/dd/yyyy"))
                )
        );
        dateCol.setPrefWidth(100);

        TableColumn<MaintenanceRequest, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setPrefWidth(100);
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button editButton = new Button("Edit");

            {
                editButton.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; " +
                        "-fx-padding: 6 12; -fx-background-radius: 5; -fx-cursor: hand;");
                editButton.setOnAction(e -> {
                    MaintenanceRequest request = getTableView().getItems().get(getIndex());
                    showEditRequestDialog(request);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    MaintenanceRequest request = getTableView().getItems().get(getIndex());
                    editButton.setDisable(!canTenantEdit(request));
                    setAlignment(Pos.CENTER);
                    setGraphic(editButton);
                }
            }
        });

        requestTable.getColumns().addAll(idCol, categoryCol, descCol, priorityCol, statusCol, dateCol, actionCol);

        loadRequests();

        section.getChildren().addAll(headerBox, requestTable);
        return section;
    }

    private void loadRequests() {
        Tenant tenant = (Tenant) authService.getCurrentUser();
        ObservableList<MaintenanceRequest> requests =
                FXCollections.observableArrayList(requestDAO.getRequestsByTenant(tenant.getUserId()));
        requestTable.setItems(requests);
    }

    private boolean canTenantEdit(MaintenanceRequest request) {
        RequestStatus status = request.getStatus();
        return status == RequestStatus.SUBMITTED ||
                status == RequestStatus.REOPENED ||
                status == RequestStatus.COMPLETED ||
                status == RequestStatus.CLOSED;
    }

    private void showNewRequestDialog() {
        Dialog<MaintenanceRequest> dialog = new Dialog<>();
        dialog.setTitle("Submit New Maintenance Request");
        dialog.setHeaderText("Please provide details about the issue");

        ButtonType submitButtonType = new ButtonType("Submit", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(submitButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        ComboBox<CategoryType> categoryBox = new ComboBox<>();
        categoryBox.getItems().addAll(CategoryType.values());
        categoryBox.setPromptText("Select category");

        TextArea descArea = new TextArea();
        descArea.setPromptText("Describe the issue...");
        descArea.setPrefRowCount(4);

        grid.add(new Label("Category:"), 0, 0);
        grid.add(categoryBox, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descArea, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == submitButtonType) {
                if (categoryBox.getValue() != null && !descArea.getText().isEmpty()) {
                    Tenant tenant = (Tenant) authService.getCurrentUser();
                    MaintenanceRequest request = new MaintenanceRequest();
                    request.setTenantId(tenant.getUserId());
                    request.setApartmentNumber(tenant.getApartmentNumber());
                    request.setCategory(categoryBox.getValue());
                    request.setDescription(descArea.getText());
                    request.setPriority(request.calculatePriority());

                    if (requestDAO.saveRequest(request)) {
                        return request;
                    }
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(request -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText("Request Submitted");
            alert.setContentText("Your request #" + request.getRequestId() + " has been submitted successfully!");
            alert.showAndWait();
            loadRequests();
        });
    }

    private void showEditRequestDialog(MaintenanceRequest request) {
        Dialog<MaintenanceRequest> dialog = new Dialog<>();
        dialog.setTitle("Edit Maintenance Request");
        dialog.setHeaderText("Update Request #" + request.getRequestId());

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        ComboBox<CategoryType> categoryBox = new ComboBox<>();
        categoryBox.getItems().addAll(CategoryType.values());
        categoryBox.setValue(request.getCategory());

        TextArea descArea = new TextArea();
        descArea.setPrefRowCount(4);
        descArea.setText(request.getDescription());

        PriorityLevel initialPriority = request.getPriority() != null ?
                request.getPriority() : calculateDefaultPriority(request.getCategory());
        Label priorityLabel = new Label(initialPriority != null ?
                initialPriority.getDisplayName() : "-");

        categoryBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                priorityLabel.setText(calculateDefaultPriority(newVal).getDisplayName());
            } else {
                priorityLabel.setText("-");
            }
        });

        ComboBox<RequestStatus> statusBox = new ComboBox<>();
        statusBox.setPrefWidth(200);

        RequestStatus currentStatus = request.getStatus();
        if (currentStatus == RequestStatus.CLOSED) {
            statusBox.getItems().addAll(RequestStatus.CLOSED, RequestStatus.REOPENED);
        } else if (currentStatus == RequestStatus.SUBMITTED) {
            statusBox.getItems().addAll(RequestStatus.SUBMITTED, RequestStatus.CLOSED);
        } else if (currentStatus == RequestStatus.REOPENED) {
            statusBox.getItems().addAll(RequestStatus.REOPENED, RequestStatus.CLOSED);
        } else {
            statusBox.getItems().add(currentStatus);
            statusBox.setDisable(true);
        }
        statusBox.getSelectionModel().select(currentStatus);

        grid.add(new Label("Category:"), 0, 0);
        grid.add(categoryBox, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descArea, 1, 1);
        grid.add(new Label("Default Priority:"), 0, 2);
        grid.add(priorityLabel, 1, 2);
        grid.add(new Label("Status:"), 0, 3);
        grid.add(statusBox, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (categoryBox.getValue() == null || descArea.getText().trim().isEmpty()) {
                    Alert warning = new Alert(Alert.AlertType.WARNING);
                    warning.setTitle("Missing Information");
                    warning.setHeaderText(null);
                    warning.setContentText("Please provide both a category and description.");
                    warning.showAndWait();
                    return null;
                }

                CategoryType selectedCategory = categoryBox.getValue();
                PriorityLevel updatedPriority = calculateDefaultPriority(selectedCategory);
                RequestStatus selectedStatus = statusBox.getSelectionModel().getSelectedItem();
                if (selectedStatus == null) {
                    selectedStatus = currentStatus;
                }

                request.setCategory(selectedCategory);
                request.setDescription(descArea.getText().trim());
                request.setPriority(updatedPriority);

                if (selectedStatus == RequestStatus.REOPENED) {
                    request.setCompletionDate(null);
                    request.setResolutionNotes(null);
                }
                if (selectedStatus == RequestStatus.CLOSED && request.getCompletionDate() == null) {
                    request.setCompletionDate(LocalDateTime.now());
                }

                request.setStatus(selectedStatus);
                request.setLastUpdated(LocalDateTime.now());
                return request;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(updated -> {
            if (requestDAO.updateRequest(updated)) {
                Alert success = new Alert(Alert.AlertType.INFORMATION);
                success.setTitle("Success");
                success.setHeaderText("Request Updated");
                success.setContentText("Your changes to request #" + updated.getRequestId() + " have been saved.");
                success.showAndWait();
                loadRequests();
            } else {
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setTitle("Update Failed");
                error.setHeaderText(null);
                error.setContentText("Unable to update your request. Please try again later.");
                error.showAndWait();
            }
        });
    }

    private PriorityLevel calculateDefaultPriority(CategoryType category) {
        if (category == null) {
            return PriorityLevel.LOW;
        }
        switch (category) {
            case EMERGENCY:
                return PriorityLevel.EMERGENCY;
            case ELECTRICAL:
            case SAFETY_SECURITY:
                return PriorityLevel.URGENT;
            case PLUMBING:
            case HVAC:
                return PriorityLevel.HIGH;
            case APPLIANCE:
                return PriorityLevel.MEDIUM;
            default:
                return PriorityLevel.LOW;
        }
    }
}
