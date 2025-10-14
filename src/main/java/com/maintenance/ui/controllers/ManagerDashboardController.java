package com.maintenance.ui.controllers;

import com.maintenance.dao.*;
import com.maintenance.enums.RequestStatus;
import com.maintenance.models.*;
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
        root.setStyle("-fx-background-color: #f5f7fa;");

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
        topBar.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");

        Label titleLabel = new Label("🏢 Manager Dashboard");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        titleLabel.setTextFill(Color.web("#667eea"));

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

        Button dashboardBtn = createSidebarButton("📊 Dashboard", true);
        Button allRequestsBtn = createSidebarButton("📋 All Requests", false);
        Button reportsBtn = createSidebarButton("📈 Reports", false);
        Button settingsBtn = createSidebarButton("⚙️ Settings", false);

        sidebar.getChildren().addAll(menuLabel, dashboardBtn, allRequestsBtn, reportsBtn, settingsBtn);
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

        return btn;
    }

    private VBox createCenterContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));

        HBox statsBox = createStatsCards();
        VBox requestsSection = createRequestsSection();

        content.getChildren().addAll(statsBox, requestsSection);
        return content;
    }

    private HBox createStatsCards() {
        HBox statsBox = new HBox(20);

        List<MaintenanceRequest> allRequests = requestDAO.getAllRequests();

        long pending = allRequests.stream()
                .filter(r -> r.getStatus() == RequestStatus.SUBMITTED ||
                        r.getStatus() == RequestStatus.IN_PROGRESS)
                .count();

        long completed = allRequests.stream()
                .filter(r -> r.getStatus() == RequestStatus.COMPLETED)
                .count();

        long urgent = allRequests.stream()
                .filter(r -> r.getPriority().name().contains("URGENT") ||
                        r.getPriority().name().contains("EMERGENCY"))
                .count();

        VBox totalCard = createStatCard("Total Requests", String.valueOf(allRequests.size()), "#667eea");
        VBox pendingCard = createStatCard("Pending", String.valueOf(pending), "#ff9800");
        VBox completedCard = createStatCard("Completed", String.valueOf(completed), "#4caf50");
        VBox urgentCard = createStatCard("Urgent", String.valueOf(urgent), "#f44336");

        statsBox.getChildren().addAll(totalCard, pendingCard, completedCard, urgentCard);
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

        Label sectionTitle = new Label("All Maintenance Requests");
        sectionTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        requestTable = new TableView<>();
        requestTable.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        requestTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<MaintenanceRequest, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("requestId"));
        idCol.setPrefWidth(100);

        TableColumn<MaintenanceRequest, String> aptCol = new TableColumn<>("Apartment");
        aptCol.setCellValueFactory(new PropertyValueFactory<>("apartmentNumber"));
        aptCol.setPrefWidth(100);

        TableColumn<MaintenanceRequest, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        categoryCol.setPrefWidth(120);

        TableColumn<MaintenanceRequest, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        descCol.setPrefWidth(250);

        TableColumn<MaintenanceRequest, String> priorityCol = new TableColumn<>("Priority");
        priorityCol.setCellValueFactory(new PropertyValueFactory<>("priority"));
        priorityCol.setPrefWidth(100);

        TableColumn<MaintenanceRequest, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(120);

        TableColumn<MaintenanceRequest, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getSubmissionDate().format(DateTimeFormatter.ofPattern("MM/dd/yyyy"))
                )
        );
        dateCol.setPrefWidth(120);
        dateCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<MaintenanceRequest, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setPrefWidth(200);
        actionCol.setStyle("-fx-alignment: CENTER;");

        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button assignBtn = new Button("Assign");

            {
                assignBtn.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; " +
                        "-fx-padding: 5 12; -fx-background-radius: 3; -fx-cursor: hand; ");
                assignBtn.setOnAction(event -> {
                    MaintenanceRequest request = getTableView().getItems().get(getIndex());
                    showAssignDialog(request);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(assignBtn);
                }
            }
        });

        requestTable.getColumns().addAll(idCol, aptCol, categoryCol, descCol,
                priorityCol, statusCol, dateCol, actionCol);

        loadRequests();

        section.getChildren().addAll(sectionTitle, requestTable);
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
