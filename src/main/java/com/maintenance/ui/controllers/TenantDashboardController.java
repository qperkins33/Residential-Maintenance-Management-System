package com.maintenance.ui.controllers;

import com.maintenance.dao.MaintenanceRequestDAO;
import com.maintenance.dao.PhotoDAO;
import com.maintenance.enums.CategoryType;
import com.maintenance.enums.RequestStatus;
import com.maintenance.models.MaintenanceRequest;
import com.maintenance.models.Tenant;
import com.maintenance.service.AuthenticationService;
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
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;

public class TenantDashboardController {
    private final ViewFactory viewFactory;
    private final AuthenticationService authService;
    private final MaintenanceRequestDAO requestDAO;
    private final PhotoDAO photoDAO;
    private TableView<MaintenanceRequest> requestTable;
    private HBox statsBox;

    public TenantDashboardController(ViewFactory viewFactory) {
        this.viewFactory = viewFactory;
        this.authService = AuthenticationService.getInstance();
        this.requestDAO = new MaintenanceRequestDAO();
        this.photoDAO = new PhotoDAO();
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

        Label titleLabel = new Label("🏢 Residential Maintenance");
        titleLabel.getStyleClass().add("top-bar-title");

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

        Button dashboardBtn = DashboardUIHelper.createSidebarButton("📊 Dashboard", true);
        Button requestsBtn = DashboardUIHelper.createSidebarButton("📝 My Requests", false);
        Button newRequestBtn = DashboardUIHelper.createSidebarButton("➕ New Request", false);
        Button settingsBtn = DashboardUIHelper.createSidebarButton("⚙️ Settings", false);

        sidebar.getChildren().addAll(menuLabel, dashboardBtn, requestsBtn, newRequestBtn, settingsBtn);
        return sidebar;
    }

    private VBox createCenterContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        content.setFillWidth(true);

        statsBox = new HBox(20);

        VBox requestsSection = createRequestsSection();
        VBox.setVgrow(requestsSection, Priority.ALWAYS);

        content.getChildren().addAll(statsBox, requestsSection);

        // Initial stats render
        refreshStats();

        return content;
    }

    private void refreshStats() {
        if (statsBox == null) {
            return;
        }

        statsBox.getChildren().clear();

        Tenant tenant = (Tenant) authService.getCurrentUser();
        List<MaintenanceRequest> requests = requestDAO.getRequestsByTenant(tenant.getUserId());

        long notStarted = requests.stream().filter(this::isNotStarted).count();
        long inProgress = requests.stream().filter(this::isInProgress).count();
        long completed = requests.stream().filter(this::isCompleted).count();
        long cancelled = requests.stream().filter(this::isCancelled).count();

        VBox totalCard = DashboardUIHelper.createStatCard(
                "Total Requests",
                String.valueOf(requests.size()),
                "#667eea",
                "📋"
        );
        VBox pendingCard = DashboardUIHelper.createStatCard(
                "Not Started",
                String.valueOf(notStarted),
                "#2196f3",
                "⏸️"
        );
        VBox inProgressCard = DashboardUIHelper.createStatCard(
                "In Progress",
                String.valueOf(inProgress),
                "#ff9800",
                "👷"
        );
        VBox completedCard = DashboardUIHelper.createStatCard(
                "Completed",
                String.valueOf(completed),
                "#4caf50",
                "✅"
        );
        VBox cancelledCard = DashboardUIHelper.createStatCard(
                "Cancelled",
                String.valueOf(cancelled),
                "#f44336",
                "❌"
        );

        statsBox.getChildren().addAll(
                totalCard,
                pendingCard,
                inProgressCard,
                completedCard,
                cancelledCard
        );
    }

    private VBox createRequestsSection() {
        VBox section = new VBox(15);
        section.setFillWidth(true);

        HBox headerBox = new HBox(20);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label sectionTitle = new Label("My Maintenance Requests");
        sectionTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        ComboBox<String> filterBox = new ComboBox<>();
        filterBox.getItems().addAll(
                "All Requests",
                "Not Started",
                "In Progress",
                "Completed",
                "Cancelled"
        );
        filterBox.setValue("All Requests");
        filterBox.setStyle("-fx-background-radius: 5; -fx-padding: 5 10;");
        filterBox.setOnAction(e -> filterRequests(filterBox.getValue()));

        Button refreshBtn = new Button("🔄 Refresh");
        refreshBtn.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; " +
                "-fx-padding: 8 15; -fx-background-radius: 5; -fx-cursor: hand;");
        refreshBtn.setOnAction(e -> loadRequests());

        Button newRequestBtn = new Button("+ New Request");
        newRequestBtn.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; " +
                "-fx-padding: 10 20; -fx-background-radius: 5; -fx-cursor: hand;");
        newRequestBtn.setOnAction(e -> showNewRequestDialog());

        headerBox.getChildren().addAll(sectionTitle, spacer, filterBox, refreshBtn, newRequestBtn);

        requestTable = new TableView<>();
        requestTable.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        requestTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        requestTable.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(requestTable, Priority.ALWAYS);

        TableColumn<MaintenanceRequest, String> idCol = new TableColumn<>("Request ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("requestId"));
        idCol.setPrefWidth(120);
        idCol.setStyle("-fx-alignment: CENTER;");

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

        TableColumn<MaintenanceRequest, Void> actionCol = getMaintenanceRequestVoidTableColumn();

        requestTable.getColumns().setAll(java.util.Arrays.asList(
                idCol, categoryCol, descCol, priorityCol, statusCol, dateCol, actionCol
        ));

        Label emptyLabel = new Label("No requests yet");
        emptyLabel.setFont(Font.font("Arial", 14));
        emptyLabel.setTextFill(Color.GRAY);
        requestTable.setPlaceholder(emptyLabel);

        loadRequests();

        section.getChildren().addAll(headerBox, requestTable);
        VBox.setVgrow(requestTable, Priority.ALWAYS);
        return section;
    }

    private TableColumn<MaintenanceRequest, Void> getMaintenanceRequestVoidTableColumn() {
        TableColumn<MaintenanceRequest, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setPrefWidth(195);
        actionCol.setMinWidth(195);
        actionCol.setResizable(false);
        actionCol.setStyle("-fx-alignment: CENTER;");
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button viewBtn = new Button("View");
            private final HBox buttonBox = new HBox(5);

            {
                String btnStyle = "-fx-background-color: #667eea; -fx-text-fill: white; -fx-padding: 5 12; -fx-background-radius: 3; -fx-cursor: hand;";
                editBtn.setStyle(btnStyle);
                viewBtn.setStyle(btnStyle);

                editBtn.setOnAction(e -> {
                    MaintenanceRequest request = getTableView().getItems().get(getIndex());
                    DashboardUIHelper.showEditRequestDialog(request, requestDAO, TenantDashboardController.this::loadRequests);
                });
                viewBtn.setOnAction(e -> {
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
                    buttonBox.getChildren().setAll(editBtn, viewBtn);
                    setGraphic(buttonBox);
                }
            }
        });
        return actionCol;
    }

    private void loadRequests() {
        Tenant tenant = (Tenant) authService.getCurrentUser();
        ObservableList<MaintenanceRequest> requests =
                FXCollections.observableArrayList(requestDAO.getRequestsByTenant(tenant.getUserId()));
        requestTable.setItems(requests);
        refreshStats();
    }

    private void filterRequests(String filter) {
        Tenant tenant = (Tenant) authService.getCurrentUser();
        List<MaintenanceRequest> requests = requestDAO.getRequestsByTenant(tenant.getUserId());

        switch (filter) {
            case "Not Started" -> requests = requests.stream()
                    .filter(this::isNotStarted)
                    .toList();
            case "In Progress" -> requests = requests.stream()
                    .filter(this::isInProgress)
                    .toList();
            case "Completed" -> requests = requests.stream()
                    .filter(this::isCompleted)
                    .toList();
            case "Cancelled" -> requests = requests.stream()
                    .filter(this::isCancelled)
                    .toList();
            default -> {
                // "All Requests"
            }
        }

        requestTable.setItems(FXCollections.observableArrayList(requests));
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

        final File[] selectedPhotoFile = {null};

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Photo");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        Button attachPhotoBtn = new Button("Attach Photo");
        attachPhotoBtn.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; " +
                "-fx-padding: 6 12; -fx-background-radius: 3; -fx-cursor: hand;");

        Label photoNameLabel = new Label("No file selected");
        photoNameLabel.setFont(Font.font("Arial", 12));
        photoNameLabel.setTextFill(Color.GRAY);

        attachPhotoBtn.setOnAction(e -> {
            File file = fileChooser.showOpenDialog(null);
            if (file != null) {
                selectedPhotoFile[0] = file;
                photoNameLabel.setText(file.getName());
                photoNameLabel.setTextFill(Color.BLACK);
            }
        });

        HBox photoRow = new HBox(10, attachPhotoBtn, photoNameLabel);
        photoRow.setAlignment(Pos.CENTER_LEFT);

        grid.add(new Label("Category:"), 0, 0);
        grid.add(categoryBox, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descArea, 1, 1);
        grid.add(new Label("Photo (optional):"), 0, 2);
        grid.add(photoRow, 1, 2);

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
            if (selectedPhotoFile[0] != null && request.getRequestId() != null) {
                File file = selectedPhotoFile[0];
                String uri = file.toURI().toString();
                long size = file.length();
                String fileName = file.getName();
                photoDAO.savePhotoForRequest(request.getRequestId(), fileName, uri, size);
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText("Request Submitted");
            alert.setContentText("Your request #" + request.getRequestId() + " has been submitted successfully!");
            alert.showAndWait();
            loadRequests();
        });
    }

    // Shared status grouping helpers (same across controllers)
    private boolean isNotStarted(MaintenanceRequest r) {
        return r.getStatus() == RequestStatus.SUBMITTED
                || r.getStatus() == RequestStatus.ASSIGNED;
    }

    private boolean isInProgress(MaintenanceRequest r) {
        return r.getStatus() == RequestStatus.IN_PROGRESS
                || r.getStatus() == RequestStatus.REOPENED;
    }

    private boolean isCompleted(MaintenanceRequest r) {
        return r.getStatus() == RequestStatus.COMPLETED;
    }

    private boolean isCancelled(MaintenanceRequest r) {
        return r.getStatus() == RequestStatus.CANCELLED;
    }
}
