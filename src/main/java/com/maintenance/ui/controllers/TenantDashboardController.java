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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller for the tenant dashboard.
 * Handles:
 *  - Layout and rendering of the tenant view (top bar, sidebar, center content)
 *  - Loading, filtering, and archiving of maintenance requests created by the tenant
 *  - Submitting new requests and attaching photos
 */
public class TenantDashboardController {
    // Factory for stage navigation (login, dashboards)
    private final ViewFactory viewFactory;
    // Central authentication service (current tenant, logout)
    private final AuthenticationService authService;
    // DAO for reading and updating maintenance requests
    private final MaintenanceRequestDAO requestDAO;
    // DAO for storing and linking photos to requests
    private final PhotoDAO photoDAO;
    // Main table of tenant requests
    private TableView<MaintenanceRequest> requestTable;
    // Row of stats cards
    private HBox statsBox;
    // Filter drop-down for narrowing request list
    private final ComboBox<String> filterBox = new ComboBox<>();

    /**
     * Creates a TenantDashboardController using shared services.
     *
     * @param viewFactory shared view factory instance for windows
     */
    public TenantDashboardController(ViewFactory viewFactory) {
        this.viewFactory = viewFactory;
        this.authService = AuthenticationService.getInstance();
        this.requestDAO = new MaintenanceRequestDAO();
        this.photoDAO = new PhotoDAO();
    }

    /**
     * Builds the tenant dashboard UI and anchors it into the given root pane.
     *
     * @param root AnchorPane that will host the dashboard layout
     */
    public void createDashboardUI(AnchorPane root) {
        // Attach global app styles so tenant dashboard shares common visual system
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

        // Fill entire root
        AnchorPane.setTopAnchor(mainLayout, 0.0);
        AnchorPane.setBottomAnchor(mainLayout, 0.0);
        AnchorPane.setLeftAnchor(mainLayout, 0.0);
        AnchorPane.setRightAnchor(mainLayout, 0.0);

        root.getChildren().add(mainLayout);
    }

    /**
     * Creates the top bar with app title, tenant info, and logout button.
     *
     * @return configured HBox
     */
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

    /**
     * Creates the sidebar for the tenant dashboard.
     * Currently only a single Dashboard menu entry.
     *
     * @return VBox sidebar
     */
    private VBox createSidebar() {
        VBox sidebar = new VBox(15);
        sidebar.setPadding(new Insets(20));
        sidebar.setStyle("-fx-background-color: #2c3e50;");
        sidebar.setPrefWidth(250);

        Label menuLabel = new Label("MENU");
        menuLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        menuLabel.setTextFill(Color.web("#95a5a6"));

        Button dashboardBtn = DashboardUIHelper.createSidebarButton("📊 Dashboard", true);

        sidebar.getChildren().addAll(menuLabel, dashboardBtn);
        return sidebar;
    }

    /**
     * Creates the center content, including:
     *  - Welcome section
     *  - Stats card row
     *  - Requests table and controls
     *
     * @return VBox home content
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

        refreshStats();
        return content;
    }

    /**
     * Creates the welcome section at the top of the tenant dashboard.
     * Shows tenant first name and the current date.
     *
     * @return VBox welcome section
     */
    private VBox createWelcomeSection() {
        VBox welcomeBox = new VBox(5);

        Tenant tenant = (Tenant) authService.getCurrentUser();

        Label welcomeLabel = new Label("Welcome back, " + tenant.getFirstName() + "!");
        welcomeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        welcomeLabel.setTextFill(Color.web("#2c3e50"));

        Label dateLabel = new Label("Today: " + java.time.LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy")));
        dateLabel.setFont(Font.font("Arial", 13));
        dateLabel.setTextFill(Color.GRAY);

        welcomeBox.getChildren().addAll(welcomeLabel, dateLabel);
        return welcomeBox;
    }

    /**
     * Rebuilds the stats cards row using only non-archived requests.
     * Cards reflect:
     *  - Total Requests
     *  - Pending Start (submitted or assigned)
     *  - In Progress
     *  - Completed
     *  - Cancelled
     * Clicking a card updates the table filter.
     */
    private void refreshStats() {
        if (statsBox == null) {
            return;
        }

        statsBox.getChildren().clear();

        Tenant tenant = (Tenant) authService.getCurrentUser();
        // Use only NON-archived requests for stats
        List<MaintenanceRequest> allRequests = requestDAO.getRequestsByTenant(tenant.getUserId());
        List<MaintenanceRequest> activeRequests = allRequests.stream()
                .filter(r -> !r.isTenantArchived())
                .toList();

        long notStarted = activeRequests.stream().filter(this::isNotStarted).count();
        long inProgress = activeRequests.stream().filter(this::isInProgress).count();
        long completed = activeRequests.stream().filter(this::isCompleted).count();
        long cancelled = activeRequests.stream().filter(this::isCancelled).count();

        VBox totalCard = DashboardUIHelper.createStatCard(
                "Total Requests",
                String.valueOf(activeRequests.size()),
                "#667eea",
                DashboardUIHelper.loadStatIcon("request.png")
        );
        VBox pendingCard = DashboardUIHelper.createStatCard(
                "Pending Start",
                String.valueOf(notStarted),
                "#2196f3",
                DashboardUIHelper.loadStatIcon("pending.png")
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
        pendingCard.setOnMouseClicked(e -> setFilterFromCard("Pending Start"));
        inProgressCard.setOnMouseClicked(e -> setFilterFromCard("In Progress"));
        completedCard.setOnMouseClicked(e -> setFilterFromCard("Completed"));
        cancelledCard.setOnMouseClicked(e -> setFilterFromCard("Cancelled"));

        statsBox.getChildren().addAll(
                totalCard,
                pendingCard,
                inProgressCard,
                completedCard,
                cancelledCard
        );
    }

    /**
     * Creates the main requests section containing:
     *  - Header with filter, refresh, and new request button
     *  - Table of maintenance requests
     *
     * @return VBox section
     */
    private VBox createRequestsSection() {
        VBox section = new VBox(15);
        section.setFillWidth(true);

        HBox headerBox = new HBox(20);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label sectionTitle = new Label("My Maintenance Requests");
        sectionTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Tenant filters including Archived
        filterBox.getItems().addAll(
                "All Requests",
                "Pending Start",
                "In Progress",
                "Completed",
                "Cancelled",
                "Archived"
        );
        filterBox.setValue("All Requests");
        filterBox.setStyle("-fx-background-radius: 5; -fx-padding: 5 10;");
//        filterBox.setOnAction(e -> filterRequests(filterBox.getValue()));

        filterBox.valueProperty().addListener((obs, oldFilter, newFilter) -> {
            if (newFilter != null) {
                filterRequests(newFilter);
            }
        });

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

        // Reuse shared helper columns
        TableColumn<MaintenanceRequest, ?> priorityCol = DashboardUIHelper.createPriorityColumn();
        TableColumn<MaintenanceRequest, ?> statusCol = DashboardUIHelper.createStatusColumn();
        TableColumn<MaintenanceRequest, ?> dateCol = DashboardUIHelper.createSubmittedDateColumn();

        // "Actions" column for edit, archive, unarchive, view
        TableColumn<MaintenanceRequest, Void> actionCol = getMaintenanceRequestVoidTableColumn();

        requestTable.getColumns().setAll(java.util.List.of(
                idCol,
                categoryCol,
                descCol,
                priorityCol,
                statusCol,
                dateCol,
                actionCol
        ));

        Label emptyLabel = new Label("No requests yet");
        emptyLabel.setFont(Font.font("Arial", 14));
        emptyLabel.setTextFill(Color.GRAY);
        requestTable.setPlaceholder(emptyLabel);

        // Initial load
        loadRequests();

        dateCol.setSortType(TableColumn.SortType.DESCENDING);
        requestTable.getSortOrder().setAll(dateCol);
        requestTable.sort();

        section.getChildren().addAll(headerBox, requestTable);
        VBox.setVgrow(requestTable, Priority.ALWAYS);
        return section;
    }

    /**
     * Sets filter from a stats card click, which triggers filtering via listener.
     *
     * @param filter filter label
     */
    private void setFilterFromCard(String filter) {
        filterBox.setValue(filter);
    }

    /**
     * Builds the "Actions" column for tenant requests.
     * Logic:
     *  - If tenantArchived: show Unarchive + Edit + View
     *  - Else if Completed or Cancelled: show Archive + Edit + View
     *  - Else: show Edit + View
     *
     * @return configured TableColumn with dynamic buttons
     */
    private TableColumn<MaintenanceRequest, Void> getMaintenanceRequestVoidTableColumn() {
        TableColumn<MaintenanceRequest, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setPrefWidth(250);
        actionCol.setMinWidth(250);
        actionCol.setResizable(false);
        actionCol.setStyle("-fx-alignment: CENTER;");
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button archiveBtn = new Button("Archive");
            private final Button unarchiveBtn = new Button("Unarchive");
            private final Button viewBtn = new Button("View");
            private final HBox buttonBox = new HBox(5);

            {
                String btnStyle = "-fx-background-color: #667eea; -fx-text-fill: white; -fx-padding: 5 12; -fx-background-radius: 3; -fx-cursor: hand;";
                editBtn.setStyle(btnStyle);
                archiveBtn.setStyle(btnStyle);
                unarchiveBtn.setStyle(btnStyle);
                viewBtn.setStyle(btnStyle);

                // Opens edit dialog for existing request
                editBtn.setOnAction(e -> {
                    MaintenanceRequest request = getTableView().getItems().get(getIndex());
                    DashboardUIHelper.showEditRequestDialog(
                            request,
                            requestDAO,
                            TenantDashboardController.this::loadRequests
                    );
                });

                // Archives completed or cancelled request for tenant
                archiveBtn.setOnAction(e -> {
                    MaintenanceRequest request = getTableView().getItems().get(getIndex());
                    archiveAsTenant(request);
                });

                // Brings archived request back into active list
                unarchiveBtn.setOnAction(e -> {
                    MaintenanceRequest request = getTableView().getItems().get(getIndex());
                    unarchiveAsTenant(request);
                });

                // Opens read-only details dialog
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
                    return;
                }

                MaintenanceRequest request = getTableView().getItems().get(getIndex());
                if (request == null) {
                    setGraphic(null);
                    return;
                }

                buttonBox.getChildren().clear();

                if (request.isTenantArchived()) {
                    // Archived view: show Unarchive first
                    buttonBox.getChildren().add(unarchiveBtn);
                } else if (isCompleted(request) || request.getStatus() == RequestStatus.CANCELLED) {
                    // Completed or cancelled and not archived: show Archive
                    buttonBox.getChildren().add(archiveBtn);
                }

                // Edit and View are always available for now
                buttonBox.getChildren().addAll(editBtn, viewBtn);
                setGraphic(buttonBox);
            }
        });
        return actionCol;
    }

    /**
     * Unarchives a request from the tenant perspective, making it visible in the main list again.
     *
     * @param request request to unarchive
     */
    private void unarchiveAsTenant(MaintenanceRequest request) {
        if (request == null) {
            return;
        }

        if (!request.isTenantArchived()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Not Archived");
            alert.setHeaderText(null);
            alert.setContentText("This request is not archived.");
            alert.showAndWait();
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Unarchive Request");
        confirm.setHeaderText("Unarchive request #" + request.getRequestId() + "?");
        confirm.setContentText("This request will reappear in your main list.");
        confirm.showAndWait().ifPresent(response -> {
            if (response != ButtonType.OK) {
                return;
            }

            request.setTenantArchived(false);
            request.setLastUpdated(LocalDateTime.now());

            if (!requestDAO.updateRequest(request)) {
                new Alert(Alert.AlertType.ERROR,
                        "Unable to unarchive request. Please try again.").showAndWait();
            } else {
                loadRequests();
            }
        });
    }

    /**
     * Loads all maintenance requests for the current tenant and:
     *  - Filters out tenant-archived requests for default view
     *  - Refreshes stats
     *  - Resets filter to "All Requests"
     */
    private void loadRequests() {
        Tenant tenant = (Tenant) authService.getCurrentUser();
        List<MaintenanceRequest> allRequests = requestDAO.getRequestsByTenant(tenant.getUserId());
        List<MaintenanceRequest> visible = allRequests.stream()
                        .filter(r -> !r.isTenantArchived())
                        .toList();

        ObservableList<MaintenanceRequest> requests = FXCollections.observableArrayList(visible);
        requestTable.setItems(requests);
        refreshStats();
        requestTable.sort();
        filterBox.setValue("All Requests");
    }

    /**
     * Filters the request table based on current tenant and selected filter label.
     * If "Archived", shows tenant-archived requests only.
     * Otherwise, starts from non-archived requests and narrows by status.
     *
     * @param filter label from filterBox
     */
    private void filterRequests(String filter) {
        Tenant tenant = (Tenant) authService.getCurrentUser();
        List<MaintenanceRequest> all = requestDAO.getRequestsByTenant(tenant.getUserId());
        List<MaintenanceRequest> filtered;

        if ("Archived".equals(filter)) {
            filtered = all.stream()
                    .filter(MaintenanceRequest::isTenantArchived)
                    .toList();
        } else {
            filtered = all.stream()
                    .filter(r -> !r.isTenantArchived())
                    .toList();

            switch (filter) {
                case "Pending Start" -> filtered = filtered.stream()
                        .filter(this::isNotStarted)
                        .toList();
                case "In Progress" -> filtered = filtered.stream()
                        .filter(this::isInProgress)
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
        requestTable.sort();
    }

    /**
     * Archives a request from the tenant perspective.
     * Only completed or cancelled requests can be archived.
     *
     * @param request request to archive
     */
    private void archiveAsTenant(MaintenanceRequest request) {
        if (request == null) {
            return;
        }

        if (!isCompleted(request) && request.getStatus() != RequestStatus.CANCELLED) {
            new Alert(Alert.AlertType.INFORMATION,
                    "Only completed or cancelled requests can be archived.").showAndWait();
            return;
        }

        if (request.isTenantArchived()) {
            new Alert(Alert.AlertType.INFORMATION,
                    "This request is already archived.").showAndWait();
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Archive Request");
        confirm.setHeaderText("Archive request #" + request.getRequestId() + "?");
        confirm.setContentText("Archived requests are hidden from your main list. " +
                "You can still view them by changing the filter to Archived.");
        confirm.showAndWait().ifPresent(response -> {
            if (response != ButtonType.OK) {
                return;
            }

            request.setTenantArchived(true);
            request.setLastUpdated(LocalDateTime.now());

            if (!requestDAO.updateRequest(request)) {
                new Alert(Alert.AlertType.ERROR,
                        "Unable to archive request. Please try again.").showAndWait();
            } else {
                loadRequests();
            }
        });
    }

    /**
     * Shows the dialog for submitting a new maintenance request.
     * Tenant can:
     *  - Pick a category
     *  - Enter description
     *  - Optionally attach a photo (image file)
     * After successful save, request is persisted and photo record is stored if provided.
     */
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

        // Track selected photo file
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

        // Build new request on submit if validation passes and DAO save succeeds
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
            // Persist photo metadata if attached and request ID exists
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
            alert.setContentText("Your request #" + request.getRequestId() +
                    " has been submitted successfully!");
            alert.showAndWait();
            loadRequests();
        });
    }

    /**
     * Helper: request is considered "not started" if it is SUBMITTED or ASSIGNED.
     */
    private boolean isNotStarted(MaintenanceRequest r) {
        return r.getStatus() == RequestStatus.SUBMITTED
                || r.getStatus() == RequestStatus.ASSIGNED;
    }

    /**
     * Helper: request is in progress if status is IN_PROGRESS or REOPENED.
     */
    private boolean isInProgress(MaintenanceRequest r) {
        return r.getStatus() == RequestStatus.IN_PROGRESS
                || r.getStatus() == RequestStatus.REOPENED;
    }

    /**
     * Helper: request is completed if status is COMPLETED.
     */
    private boolean isCompleted(MaintenanceRequest r) {
        return r.getStatus() == RequestStatus.COMPLETED;
    }

    /**
     * Helper: request is cancelled if status is CANCELLED.
     */
    private boolean isCancelled(MaintenanceRequest r) {
        return r.getStatus() == RequestStatus.CANCELLED;
    }
}
