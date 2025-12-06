package com.maintenance.ui.controllers;

import com.maintenance.dao.UserDAO;
import com.maintenance.models.MaintenanceStaff;
import com.maintenance.models.Tenant;
import com.maintenance.dao.MaintenanceRequestDAO;
import com.maintenance.dao.PhotoDAO;
import com.maintenance.enums.CategoryType;
import com.maintenance.enums.PriorityLevel;
import com.maintenance.enums.RequestStatus;
import com.maintenance.models.MaintenanceRequest;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.event.ActionEvent;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

public final class DashboardUIHelper {

    private static final PhotoDAO PHOTO_DAO = new PhotoDAO();
    private static final UserDAO USER_DAO = new UserDAO();

    private DashboardUIHelper() {}

    public static void applyRootStyles(AnchorPane root, Class<?> clazz) {
        root.getStylesheets().add(
                Objects.requireNonNull(clazz.getResource("/css/styles.css")).toExternalForm()
        );
        root.getStyleClass().add("app-root");
    }

    public static Button createSidebarButton(String text, boolean active) {
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

    public static VBox createStatCard(String title, String value, String color, Image iconImage) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");
        card.setPrefWidth(220);
        card.setAlignment(Pos.TOP_LEFT);

        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        ImageView iconView = new ImageView(iconImage);
        iconView.setFitWidth(24);
        iconView.setFitHeight(24);
        iconView.setPreserveRatio(true);

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 13));
        titleLabel.setTextFill(Color.GRAY);

        headerBox.getChildren().addAll(iconView, titleLabel);

        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        valueLabel.setTextFill(Color.web(color));

        card.getChildren().addAll(headerBox, valueLabel);

        card.setOnMouseEntered(e ->
                card.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 15, 0, 0, 5); -fx-cursor: hand;"));
        card.setOnMouseExited(e ->
                card.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);"));

        return card;
    }

    public static Image loadStatIcon(String imageFileName) {
        var url = DashboardUIHelper.class.getResource("/images/" + imageFileName);
        if (url == null) {
            throw new IllegalArgumentException("Icon not found: " + imageFileName);
        }
        return new Image(url.toExternalForm());
    }

    public static TableColumn<MaintenanceRequest, PriorityLevel> createPriorityColumn() {
        TableColumn<MaintenanceRequest, PriorityLevel> priorityCol = new TableColumn<>("Priority");
        priorityCol.setCellValueFactory(new PropertyValueFactory<>("priority"));
        priorityCol.setPrefWidth(100);
        priorityCol.setStyle("-fx-alignment: CENTER;");
        priorityCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(PriorityLevel item, boolean empty) {
                super.updateItem(item, empty);
                setText(null);
                setStyle("");
                getStyleClass().removeAll("priority-urgent", "priority-high", "priority-medium", "priority-else");
                if (empty || item == null) {
                    return;
                }
                setText(item.getDisplayName());
                if (item == PriorityLevel.EMERGENCY || item == PriorityLevel.URGENT) {
                    getStyleClass().add("priority-urgent");
                } else if (item == PriorityLevel.HIGH) {
                    getStyleClass().add("priority-high");
                } else if (item == PriorityLevel.MEDIUM) {
                    getStyleClass().add("priority-medium");
                } else {
                    getStyleClass().add("priority-else");
                }
            }
        });
        return priorityCol;
    }

    public static TableColumn<MaintenanceRequest, RequestStatus> createStatusColumn() {
        TableColumn<MaintenanceRequest, RequestStatus> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(120);
        statusCol.setStyle("-fx-alignment: CENTER;");
        statusCol.setCellFactory(column -> new TableCell<>() {
            private final List<String> statusClasses = List.of(
                    "status-submitted",
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
                setStyle("");
                getStyleClass().removeAll(statusClasses);
                if (empty || item == null) {
                    return;
                }
                setText(item.getDisplayName());
                switch (item) {
                    case COMPLETED -> getStyleClass().add("status-completed");
                    case IN_PROGRESS -> getStyleClass().add("status-in-progress");
                    case REOPENED -> getStyleClass().add("status-reopened");
                    case ASSIGNED -> getStyleClass().add("status-assigned");
                    case SUBMITTED -> getStyleClass().add("status-submitted");
                    case CANCELLED -> getStyleClass().add("status-cancelled");
                    default -> getStyleClass().add("status-else");
                }
            }
        });
        return statusCol;
    }

    public static TableColumn<MaintenanceRequest, String> createSubmittedDateColumn() {
        TableColumn<MaintenanceRequest, String> dateCol = new TableColumn<>("Submitted");
        dateCol.setCellValueFactory(cellData -> {
            LocalDateTime dt = cellData.getValue().getSubmissionDate();
            String formatted = dt != null
                    ? dt.format(DateTimeFormatter.ofPattern("MM/dd/yyyy"))
                    : "";
            return new ReadOnlyStringWrapper(formatted);
        });
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
                if (empty || item == null) {
                    return;
                }
                pill.setText(item);
                setGraphic(pill);
            }
        });
        return dateCol;
    }

    public static void addDetailRow(GridPane grid, int row, String label, String value) {
        Label lblLabel = new Label(label);
        lblLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        lblLabel.setTextFill(Color.web("#555"));
        lblLabel.setMinWidth(Region.USE_PREF_SIZE);
        lblLabel.getStyleClass().add("request-details-label");

        Label valueLabel = new Label(value == null ? "" : value);
        valueLabel.setFont(Font.font("Arial", 12));
        valueLabel.setWrapText(true);
        valueLabel.setMaxWidth(Double.MAX_VALUE);
        valueLabel.getStyleClass().add("request-details-value");

        GridPane.setHgrow(valueLabel, Priority.ALWAYS);

        grid.add(lblLabel, 0, row);
        grid.add(valueLabel, 1, row);
    }

    public static void showRequestDetailsDialog(MaintenanceRequest request) {
        String photoUri = null;
        if (request != null && request.getRequestId() != null) {
            photoUri = PHOTO_DAO.getLatestPhotoPathForRequest(request.getRequestId());
        }
        if (request != null) {
            showRequestDetailsDialog(request, photoUri);
        }
    }

    private static void showRequestDetailsDialog(MaintenanceRequest request, String photoUri) {
        String tenantName = null;
        String tenantPhone = null;
        String tenantEmail = null;
        String staffPhone = null;
        String staffEmail = null;
        String staffName = null;

        if (request.getTenantId() != null && !request.getTenantId().isBlank()) {
            Tenant tenant = USER_DAO.getTenantById(request.getTenantId());
            if (tenant != null) {
                tenantName = tenant.getFullName();
                tenantPhone = tenant.getPhoneNumber();
                tenantEmail = tenant.getEmail();
            }
        }

        if (request.getAssignedStaffId() != null && !request.getAssignedStaffId().isBlank()) {
            MaintenanceStaff staff = USER_DAO.getStaffByStaffId(request.getAssignedStaffId());
            if (staff != null) {
                staffName = staff.getFullName();
                staffPhone = staff.getPhoneNumber();
                staffEmail = staff.getEmail();
            }
        }

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Request Details");
        dialog.setHeaderText("Request #" + request.getRequestId());

        DialogPane pane = dialog.getDialogPane();
        pane.getButtonTypes().add(ButtonType.CLOSE);

        pane.setMinWidth(700);
        pane.setPrefWidth(700);

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setMinWidth(110);
        col1.setPrefWidth(130);
        col1.setHalignment(HPos.RIGHT);

        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);

        grid.getColumnConstraints().setAll(col1, col2);

        int row = 0;

        addDetailRow(grid, row++, "Request ID:", request.getRequestId());
        addDetailRow(grid, row++, "Apartment:", request.getApartmentNumber());

        if (tenantName != null && !tenantName.isBlank()) {
            addDetailRow(grid, row++, "Tenant:", tenantName);
        }
        if (tenantPhone != null && !tenantPhone.isBlank()) {
            addDetailRow(grid, row++, "Tenant Phone:", tenantPhone);
        }
        if (tenantEmail != null && !tenantEmail.isBlank()) {
            addDetailRow(grid, row++, "Tenant Email:", tenantEmail);
        }

        if (staffName != null && !staffName.isBlank()) {
            addDetailRow(grid, row++, "Assigned Staff:", staffName);
        }
        if (staffPhone != null && !staffPhone.isBlank()) {
            addDetailRow(grid, row++, "Staff Phone:", staffPhone);
        }
        if (staffEmail != null && !staffEmail.isBlank()) {
            addDetailRow(grid, row++, "Assigned Staff Email:", staffEmail);
        }

        addDetailRow(grid, row++, "Category:", request.getCategory().getDisplayName());
        addDetailRow(grid, row++, "Priority:", request.getPriority().getDisplayName());
        addDetailRow(grid, row++, "Status:", request.getStatus().getDisplayName());

        if (request.getSubmissionDate() != null) {
            addDetailRow(
                    grid,
                    row++,
                    "Submitted:",
                    request.getSubmissionDate().format(DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm"))
            );
        }

        if (request.getScheduledDate() != null) {
            addDetailRow(
                    grid,
                    row++,
                    "Scheduled:",
                    request.getScheduledDate().format(DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm"))
            );
        }

        Label descLabel = new Label("Description:");
        descLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));

        TextArea descArea = new TextArea(request.getDescription());
        descArea.setWrapText(true);
        descArea.setEditable(false);
        descArea.setPrefRowCount(3);
        descArea.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(descArea, Priority.ALWAYS);

        grid.add(descLabel, 0, row);
        grid.add(descArea, 1, row++);

        Label updateLabel = new Label("Staff Update:");
        updateLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));

        TextArea updateArea = new TextArea();
        updateArea.setWrapText(true);
        updateArea.setEditable(false);
        updateArea.setPrefRowCount(3);
        updateArea.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(updateArea, Priority.ALWAYS);

        String staffUpdate = request.getStaffUpdateNotes();
        if (staffUpdate != null && !staffUpdate.isBlank()) {
            updateArea.setText(staffUpdate);
        } else {
            updateArea.setText("No staff updates yet.");
            updateArea.setStyle("-fx-text-fill: #7f8c8d; -fx-font-style: italic;");
        }

        grid.add(updateLabel, 0, row);
        grid.add(updateArea, 1, row++);

        if (request.getResolutionNotes() != null && !request.getResolutionNotes().isEmpty()) {
            Label resLabel = new Label("Resolution:");
            resLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));

            TextArea resArea = new TextArea(request.getResolutionNotes());
            resArea.setWrapText(true);
            resArea.setEditable(false);
            resArea.setPrefRowCount(3);
            resArea.setMaxWidth(Double.MAX_VALUE);
            GridPane.setHgrow(resArea, Priority.ALWAYS);

            grid.add(resLabel, 0, row);
            grid.add(resArea, 1, row++);
        }

        if (photoUri != null && !photoUri.isBlank()) {
            Label photoLabel = new Label("Photo:");
            photoLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));

            ImageView imageView = new ImageView();
            imageView.setPreserveRatio(true);
            imageView.setSmooth(true);
            imageView.setFitWidth(320);

            try {
                imageView.setImage(new Image(photoUri, true));
                grid.add(photoLabel, 0, row);
                grid.add(imageView, 1, row++);
            } catch (Exception ex) {
                Label errorLabel = new Label("Unable to load image");
                errorLabel.setTextFill(Color.RED);
                grid.add(photoLabel, 0, row);
                grid.add(errorLabel, 1, row);
            }
        }

        ScrollPane scrollPane = new ScrollPane(grid);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefSize(660, 500);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        pane.setContent(scrollPane);

        dialog.showAndWait();
    }

    public static void showEditRequestDialog(MaintenanceRequest request,
                                             MaintenanceRequestDAO requestDAO,
                                             Runnable afterSave) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Edit Maintenance Request");
        dialog.setHeaderText("Edit your request");

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

        HBox statusOptions = new HBox(10);
        statusOptions.setAlignment(Pos.CENTER_LEFT);

        RequestStatus originalStatus = request.getStatus();
        final RequestStatus[] selectedStatus = { originalStatus };

        // COMPLETED / CANCELLED => allow reopen
        if (originalStatus == RequestStatus.COMPLETED || originalStatus == RequestStatus.CANCELLED) {
            CheckBox reopenCheck = new CheckBox("Reopen request");
            styleActionToggleButton(reopenCheck, "#4caf50", "#43a047", "#388e3c");
            reopenCheck.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
                if (isSelected) {
                    selectedStatus[0] = RequestStatus.REOPENED;
                } else {
                    selectedStatus[0] = originalStatus;
                }
            });
            statusOptions.getChildren().add(reopenCheck);

            // Any other active state (not SUBMITTED/ASSIGNED) => allow cancel
        } else if (originalStatus != RequestStatus.SUBMITTED && originalStatus != RequestStatus.ASSIGNED) {
            CheckBox cancelCheck = new CheckBox("Cancel request");
            styleActionToggleButton(cancelCheck, "#e53935", "#d32f2f", "#c62828");
            cancelCheck.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
                if (isSelected) {
                    selectedStatus[0] = RequestStatus.CANCELLED;
                } else {
                    selectedStatus[0] = originalStatus;
                }
            });
            statusOptions.getChildren().add(cancelCheck);
        } else {
            String statusText = originalStatus.name().replace('_', ' ');
            Label statusLabel = new Label(statusText);
            statusLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 13));
            statusOptions.getChildren().add(statusLabel);
            selectedStatus[0] = originalStatus;
        }

        grid.add(new Label("Category:"), 0, 0);
        grid.add(categoryBox, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descArea, 1, 1);
        grid.add(new Label("Priority:"), 0, 2);
        grid.add(priorityBox, 1, 2);
        grid.add(new Label("Status:"), 0, 3);
        grid.add(statusOptions, 1, 3);

        dialog.getDialogPane().setContent(grid);

        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveBtnType);
        boolean[] updated = {false};
        saveButton.addEventFilter(ActionEvent.ACTION, event -> {
            if (categoryBox.getValue() == null ||
                    priorityBox.getValue() == null ||
                    descArea.getText().isBlank()) {
                new Alert(Alert.AlertType.WARNING,
                        "All fields are required.").showAndWait();
                event.consume();
                return;
            }

            RequestStatus newStatus = selectedStatus[0];

            request.setCategory(categoryBox.getValue());
            request.setDescription(descArea.getText().trim());
            request.setPriority(priorityBox.getValue());
            request.setStatus(newStatus);
            request.setLastUpdated(LocalDateTime.now());

            // Only when a CLOSED request is REOPENED do we unarchive it
            if ((originalStatus == RequestStatus.COMPLETED || originalStatus == RequestStatus.CANCELLED)
                    && newStatus == RequestStatus.REOPENED) {
                request.setTenantArchived(false);
                request.setStaffArchived(false);
            }

            if (!requestDAO.updateRequest(request)) {
                new Alert(Alert.AlertType.ERROR, "Unable to update request. Please try again.").showAndWait();
                event.consume();
            } else {
                updated[0] = true;
            }
        });

        dialog.showAndWait();

        if (updated[0]) {
            if (afterSave != null) {
                afterSave.run();
            }
            new Alert(Alert.AlertType.INFORMATION, "Request updated successfully.").showAndWait();
        }
    }

    public static void styleActionToggleButton(Labeled b, String base, String hover, String pressed) {
        String common = "; -fx-text-fill: white; -fx-padding: 6 12; -fx-background-radius: 4;";
        b.setStyle("-fx-background-color: " + base + common);
        b.setOnMouseEntered(ev -> b.setStyle("-fx-background-color: " + hover + common));
        b.setOnMouseExited(ev -> b.setStyle("-fx-background-color: " + base + common));
        b.setOnMousePressed(ev -> b.setStyle("-fx-background-color: " + pressed + common));
        b.setOnMouseReleased(ev -> b.setStyle("-fx-background-color: " + hover + common));
    }
}