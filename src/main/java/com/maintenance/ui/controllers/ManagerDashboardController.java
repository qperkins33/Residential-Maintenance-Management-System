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
import java.util.Objects;

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
        root.getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/css/styles.css")).toExternalForm()
        );
        root.getStyleClass().add("app-root");

        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(0));

        HBox topBar = createTopBar();
        mainLayout.setTop(topBar);

        VBox sidebar = createSidebar();
        mainLayout.setLeft(sidebar);

        VBox centerContent = createCenterContent();
        VBox.setVgrow(centerContent, Priority.ALWAYS); // helps center fill vertically
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
        content.setFillWidth(true);

        HBox statsBox = createStatsCards();
        VBox requestsSection = createRequestsSection();

        VBox.setVgrow(requestsSection, Priority.ALWAYS); // take remaining height

        content.getChildren().addAll(statsBox, requestsSection);
        return content;
    }

    private HBox createStatsCards() {
        HBox statsBox = new HBox(20);

        List<MaintenanceRequest> allRequests = requestDAO.getAllRequests();

        long pending = allRequests.stream()
                .filter(r -> r.getStatus() == RequestStatus.SUBMITTED ||
                        r.getStatus() == RequestStatus.IN_PROGRESS ||
                        r.getStatus() == RequestStatus.REOPENED)
                .count();

        long completed = allRequests.stream()
                .filter(r -> r.getStatus() == RequestStatus.COMPLETED)
                .count();

        long urgent = allRequests.stream()
                .filter(r -> r.getPriority().name().contains("URGENT") ||
                        r.getPriority().name().contains("EMERGENCY"))
                .count();

        long cancelled = allRequests.stream()
                .filter(r -> r.getStatus() == RequestStatus.CANCELLED)
                .count();

        VBox totalCard = createStatCard("Total Requests", String.valueOf(allRequests.size()), "#667eea");
        VBox pendingCard = createStatCard("Pending", String.valueOf(pending), "#ff9800");
        VBox completedCard = createStatCard("Completed", String.valueOf(completed), "#4caf50");
        VBox urgentCard = createStatCard("Urgent", String.valueOf(urgent), "#f44336");
        VBox cancelledCard = createStatCard("Cancelled", String.valueOf(cancelled), "#f44336");

        statsBox.getChildren().addAll(totalCard, pendingCard, urgentCard, completedCard, cancelledCard);
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
        section.setFillWidth(true);

        Label sectionTitle = new Label("All Maintenance Requests");
        sectionTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        requestTable = new TableView<>();
        requestTable.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        requestTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        requestTable.setMaxHeight(Double.MAX_VALUE);      // allow vertical growth
        VBox.setVgrow(requestTable, Priority.ALWAYS);     // fill leftover space

        TableColumn<MaintenanceRequest, String> idCol = new TableColumn<>("Request ID");
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

        TableColumn<MaintenanceRequest, String> dateCol = new TableColumn<>("Submitted");
        dateCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getSubmissionDate().format(DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm"))
                )
        );
        dateCol.setPrefWidth(150);
        dateCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<MaintenanceRequest, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setPrefWidth(220); // wider actions column
        actionCol.setMinWidth(220);
        actionCol.setMaxWidth(300);
        actionCol.setResizable(false);
        actionCol.setStyle("-fx-alignment: CENTER;");

        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button assignBtn = new Button();
            private final Button viewBtn   = new Button("View");
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
                    showRequestDetails(request);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    MaintenanceRequest request = getTableView().getItems().get(getIndex());
                    assignBtn.setText(request.getStatus() == RequestStatus.SUBMITTED ? "Assign" : "Reassign");

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
