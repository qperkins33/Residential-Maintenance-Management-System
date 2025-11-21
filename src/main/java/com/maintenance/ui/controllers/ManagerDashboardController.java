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
import java.util.List;

public class ManagerDashboardController {
    private final ViewFactory viewFactory;
    private final AuthenticationService authService;
    private final MaintenanceRequestDAO requestDAO;
    private final UserDAO userDAO;
    private TableView<MaintenanceRequest> requestTable;

    public ManagerDashboardController(ViewFactory viewFactory) {
        this.viewFactory = viewFactory;
        this.authService = AuthenticationService.getInstance();
        this.requestDAO = new MaintenanceRequestDAO();
        this.userDAO = new UserDAO();
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

        Label titleLabel = new Label("🏢 Manager Dashboard");
        titleLabel.getStyleClass().add("top-bar-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

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

    private VBox createSidebar() {
        VBox sidebar = new VBox(15);
        sidebar.setPadding(new Insets(20));
        sidebar.setStyle("-fx-background-color: #2c3e50;");
        sidebar.setPrefWidth(250);

        Label menuLabel = new Label("MENU");
        menuLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        menuLabel.setTextFill(Color.web("#95a5a6"));

        Button dashboardBtn = DashboardUIHelper.createSidebarButton("📊 Dashboard", true);
        Button allRequestsBtn = DashboardUIHelper.createSidebarButton("📋 All Requests", false);
        Button reportsBtn = DashboardUIHelper.createSidebarButton("📈 Reports", false);
        Button settingsBtn = DashboardUIHelper.createSidebarButton("⚙️ Settings", false);

        sidebar.getChildren().addAll(menuLabel, dashboardBtn, allRequestsBtn, reportsBtn, settingsBtn);
        return sidebar;
    }

    private VBox createCenterContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        content.setFillWidth(true);

        HBox statsBox = createStatsCards();
        VBox requestsSection = createRequestsSection();

        VBox.setVgrow(requestsSection, Priority.ALWAYS);

        content.getChildren().addAll(statsBox, requestsSection);
        return content;
    }

    private HBox createStatsCards() {
        HBox statsBox = new HBox(20);

        List<MaintenanceRequest> allRequests = requestDAO.getAllRequests();

        long unassigned = allRequests.stream()
                .filter(r -> r.getStatus() == RequestStatus.SUBMITTED)
                .count();

        long inProgress = allRequests.stream()
                .filter(r -> r.getStatus() == RequestStatus.IN_PROGRESS
                        || r.getStatus() == RequestStatus.REOPENED)
                .count();

        long completed = allRequests.stream()
                .filter(r -> r.getStatus() == RequestStatus.COMPLETED)
                .count();

        long cancelled = allRequests.stream()
                .filter(r -> r.getStatus() == RequestStatus.CANCELLED)
                .count();

        VBox totalCard = DashboardUIHelper.createStatCard("Total Requests", String.valueOf(allRequests.size()), "#667eea", "📋");
        VBox unassignedCard = DashboardUIHelper.createStatCard("Unassigned", String.valueOf(unassigned), "#2196f3", "👀");
        VBox inProgressCard = DashboardUIHelper.createStatCard("In Progress", String.valueOf(inProgress), "#ff9800", "👷️");
        VBox completedCard = DashboardUIHelper.createStatCard("Completed", String.valueOf(completed), "#4caf50", "✅");
        VBox cancelledCard = DashboardUIHelper.createStatCard("Cancelled", String.valueOf(cancelled), "#f44336", "❌");

        statsBox.getChildren().addAll(totalCard, unassignedCard, inProgressCard, completedCard, cancelledCard);
        return statsBox;
    }

    private VBox createRequestsSection() {
        VBox section = new VBox(15);
        section.setFillWidth(true);

        Label sectionTitle = new Label("All Maintenance Requests");
        sectionTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        requestTable = new TableView<>();
        requestTable.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        requestTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        requestTable.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(requestTable, Priority.ALWAYS);

        TableColumn<MaintenanceRequest, String> idCol = new TableColumn<>("Request ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("requestId"));
        idCol.setPrefWidth(100);

        TableColumn<MaintenanceRequest, String> aptCol = new TableColumn<>("Apartment");
        aptCol.setCellValueFactory(new PropertyValueFactory<>("apartmentNumber"));
        aptCol.setPrefWidth(100);

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
        actionCol.setPrefWidth(220);
        actionCol.setMinWidth(220);
        actionCol.setResizable(false);
        actionCol.setStyle("-fx-alignment: CENTER;");

        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button assignBtn = new Button();
            private final Button viewBtn = new Button("View");
            private final HBox box = new HBox(8);

            {
                String btnStyle = "-fx-background-color: #667eea; -fx-text-fill: white; -fx-padding: 5 12; -fx-background-radius: 3; -fx-cursor: hand;";
                assignBtn.setStyle(btnStyle);
                viewBtn.setStyle(btnStyle);
                box.setAlignment(Pos.CENTER);

                assignBtn.setOnAction(event -> {
                    MaintenanceRequest request = getTableView().getItems().get(getIndex());
                    showAssignDialog(request);
                });
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
                        assignBtn.setText("Assign");
                    } else {
                        assignBtn.setText("Reassign");
                    }
                    box.getChildren().setAll(assignBtn, viewBtn);
                    setGraphic(box);
                }
            }
        });

        requestTable.getColumns().setAll(java.util.Arrays.asList(
                idCol, aptCol, categoryCol, descCol,
                priorityCol, statusCol, dateCol, actionCol));

        loadRequests();

        section.getChildren().addAll(sectionTitle, requestTable);
        VBox.setVgrow(requestTable, Priority.ALWAYS);
        return section;
    }

    private void loadRequests() {
        requestTable.setItems(FXCollections.observableArrayList(requestDAO.getAllRequests()));
    }

    private void showAssignDialog(MaintenanceRequest request) {
        Dialog<MaintenanceStaff> dialog = new Dialog<>();
        dialog.setTitle("Assign Staff");
        dialog.setHeaderText("Assign maintenance staff to request #" + request.getRequestId());

        ButtonType assignButtonType = new ButtonType("Assign", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(assignButtonType, ButtonType.CANCEL);

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        ComboBox<MaintenanceStaff> staffBox = new ComboBox<>();
        List<MaintenanceStaff> availableStaff = userDAO.getAllAvailableStaff();
        staffBox.getItems().addAll(availableStaff);
        staffBox.setPromptText("Select staff member");

        staffBox.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(MaintenanceStaff staff, boolean empty) {
                super.updateItem(staff, empty);
                if (empty || staff == null) {
                    setText(null);
                } else {
                    setText(staff.getFullName() + " (Workload: " +
                            staff.getCurrentWorkload() + "/" + staff.getMaxCapacity() + ")");
                }
            }
        });

        content.getChildren().addAll(new Label("Select Staff:"), staffBox);
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == assignButtonType && staffBox.getValue() != null) {
                return staffBox.getValue();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(staff -> {
            request.setAssignedStaffId(staff.getStaffId());
            request.setStatus(RequestStatus.ASSIGNED);
            if (requestDAO.updateRequest(request)) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText("Request Assigned");
                alert.setContentText("Request has been assigned to " + staff.getFullName());
                alert.showAndWait();
                loadRequests();
            }
        });
    }
}
