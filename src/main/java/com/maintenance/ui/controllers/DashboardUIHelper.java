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

/**
 * Shared UI helper for all dashboards.
 * Handles common styling, stat cards, table columns, and request details/edit dialogs.
 */
public final class DashboardUIHelper {

    // Shared DAOs used by dialogs that need tenant/staff or photo info
    private static final PhotoDAO PHOTO_DAO = new PhotoDAO();
    private static final UserDAO USER_DAO = new UserDAO();

    // Utility class, no instances
    private DashboardUIHelper() {}

    /**
     * Attach the global stylesheet and base root style class to an AnchorPane.
     */
    public static void applyRootStyles(AnchorPane root, Class<?> clazz) {
        root.getStylesheets().add(
                Objects.requireNonNull(clazz.getResource("/css/styles.css")).toExternalForm()
        );
        root.getStyleClass().add("app-root");
    }

    /**
     * Create a sidebar button with hover behavior and optional "active" styling.
     */
    public static Button createSidebarButton(String text, boolean active) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setFont(Font.font("Arial", 14));

        // Base style depends on whether this item is considered active
        if (active) {
            btn.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; " +
                    "-fx-padding: 12 15; -fx-background-radius: 5;");
        } else {
            btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #ecf0f1; " +
                    "-fx-padding: 12 15; -fx-background-radius: 5;");
        }

        // Simple hover effect for inactive buttons
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

    /**
     * Create a stat card used at the top of dashboards (Total, In Progress, etc).
     */
    public static VBox createStatCard(String title, String value, String color, Image iconImage) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");
        card.setPrefWidth(220);
        card.setAlignment(Pos.TOP_LEFT);

        // Header row contains icon and title
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

        // Big number value
        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        valueLabel.setTextFill(Color.web(color));

        card.getChildren().addAll(headerBox, valueLabel);

        // Hover effect to make the card feel clickable
        card.setOnMouseEntered(e ->
                card.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 15, 0, 0, 5); -fx-cursor: hand;"));
        card.setOnMouseExited(e ->
                card.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);"));

        return card;
    }

    /**
     * Load a stat icon from the shared images folder.
     */
    public static Image loadStatIcon(String imageFileName) {
        var url = DashboardUIHelper.class.getResource("/images/" + imageFileName);
        if (url == null) {
            throw new IllegalArgumentException("Icon not found: " + imageFileName);
        }
        return new Image(url.toExternalForm());
    }

    /**
     * Reusable "Priority" table column with CSS based pills per priority level.
     */
    public static TableColumn<MaintenanceRequest, PriorityLevel> createPriorityColumn() {
        TableColumn<MaintenanceRequest, PriorityLevel> priorityCol = new TableColumn<>("Priority");
        priorityCol.setCellValueFactory(new PropertyValueFactory<>("priority"));
        priorityCol.setPrefWidth(100);
        priorityCol.setStyle("-fx-alignment: CENTER;");
        priorityCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(PriorityLevel item, boolean empty) {
                super.updateItem(item, empty);
                // Always clear previous state
                setText(null);
                setStyle("");
                getStyleClass().removeAll("priority-urgent", "priority-high", "priority-medium", "priority-else");

                if (empty || item == null) {
                    return;
                }

                setText(item.getDisplayName());

                // Map enum to CSS class for colored pill
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

    /**
     * Reusable "Status" table column with CSS based pills per status value.
     */
    public static TableColumn<MaintenanceRequest, RequestStatus> createStatusColumn() {
        TableColumn<MaintenanceRequest, RequestStatus> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(120);
        statusCol.setStyle("-fx-alignment: CENTER;");
        statusCol.setCellFactory(column -> new TableCell<>() {
            // Known status CSS classes so we can clear them cleanly
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

                // Map enum to CSS class for status pill
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

    /**
     * Reusable "Submitted" date column rendered as a small, gray pill.
     */
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

    /**
     * Utility for adding a 2 column label/value row to a GridPane.
     */
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

    /**
     * Top level helper for showing request details, including current photo if present.
     */
    public static void showRequestDetailsDialog(MaintenanceRequest request) {
        String photoUri = null;
        if (request != null && request.getRequestId() != null) {
            // Pull the most recent photo for this request if one exists
            photoUri = PHOTO_DAO.getLatestPhotoPathForRequest(request.getRequestId());
        }
        if (request != null) {
            showRequestDetailsDialog(request, photoUri);
        }
    }

    /**
     * Internal method that builds the actual details dialog, including tenant and staff info.
     */
    private static void showRequestDetailsDialog(MaintenanceRequest request, String photoUri) {
        String tenantName = null;
        String tenantPhone = null;
        String tenantEmail = null;
        String staffPhone = null;
        String staffEmail = null;
        String staffName = null;

        // Resolve tenant details if available
        if (request.getTenantId() != null && !request.getTenantId().isBlank()) {
            Tenant tenant = USER_DAO.getTenantById(request.getTenantId());
            if (tenant != null) {
                tenantName = tenant.getFullName();
                tenantPhone = tenant.getPhoneNumber();
                tenantEmail = tenant.getEmail();
            }
        }

        // Resolve staff details if available
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

        // Two column layout for labels and values
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

        // Basic request metadata
        addDetailRow(grid, row++, "Request ID:", request.getRequestId());
        addDetailRow(grid, row++, "Apartment:", request.getApartmentNumber());

        // Tenant info if known
        if (tenantName != null && !tenantName.isBlank()) {
            addDetailRow(grid, row++, "Tenant:", tenantName);
        }
        if (tenantPhone != null && !tenantPhone.isBlank()) {
            addDetailRow(grid, row++, "Tenant Phone:", tenantPhone);
        }
        if (tenantEmail != null && !tenantEmail.isBlank()) {
            addDetailRow(grid, row++, "Tenant Email:", tenantEmail);
        }

        // Staff info if assigned
        if (staffName != null && !staffName.isBlank()) {
            addDetailRow(grid, row++, "Assigned Staff:", staffName);
        }
        if (staffPhone != null && !staffPhone.isBlank()) {
            addDetailRow(grid, row++, "Staff Phone:", staffPhone);
        }
        if (staffEmail != null && !staffEmail.isBlank()) {
            addDetailRow(grid, row++, "Assigned Staff Email:", staffEmail);
        }

        // Category, priority, status
        addDetailRow(grid, row++, "Category:", request.getCategory().getDisplayName());
        addDetailRow(grid, row++, "Priority:", request.getPriority().getDisplayName());
        addDetailRow(grid, row++, "Status:", request.getStatus().getDisplayName());

        // Dates if present
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

        // Description block
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

        // Staff update block
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

        // Resolution block only for closed requests
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

        // Optional photo preview if the request has one
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

        // Wrap details in a scroll pane to handle long content
        ScrollPane scrollPane = new ScrollPane(grid);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefSize(660, 500);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        pane.setContent(scrollPane);

        dialog.showAndWait();
    }

    /**
     * Shared tenant edit dialog used on the tenant dashboard.
     * Supports editing category, description, priority, and limited status transitions
     * (reopen completed or cancelled, or cancel an active request).
     */
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

        // Editable fields for category, description, priority
        ComboBox<CategoryType> categoryBox = new ComboBox<>();
        categoryBox.getItems().addAll(CategoryType.values());
        categoryBox.setValue(request.getCategory());

        TextArea descArea = new TextArea(request.getDescription());
        descArea.setPrefRowCount(5);

        ComboBox<PriorityLevel> priorityBox = new ComboBox<>();
        priorityBox.getItems().addAll(PriorityLevel.values());
        priorityBox.setValue(request.getPriority());

        // Status options row (reopen, cancel, or read only)
        HBox statusOptions = new HBox(10);
        statusOptions.setAlignment(Pos.CENTER_LEFT);

        RequestStatus originalStatus = request.getStatus();
        final RequestStatus[] selectedStatus = { originalStatus };

        // If the request is closed, allow tenant to reopen
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

            // For active but not "new" statuses, allow tenant to cancel
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
            // Submitted or Assigned are effectively read only status wise from here
            String statusText = originalStatus.name().replace('_', ' ');
            Label statusLabel = new Label(statusText);
            statusLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 13));
            statusOptions.getChildren().add(statusLabel);
            selectedStatus[0] = originalStatus;
        }

        // Build form layout
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

        // Validate and persist on Save
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

            // Update edited fields
            request.setCategory(categoryBox.getValue());
            request.setDescription(descArea.getText().trim());
            request.setPriority(priorityBox.getValue());
            request.setStatus(newStatus);
            request.setLastUpdated(LocalDateTime.now());

            // Only when a closed request is explicitly reopened do we clear archive flags.
            // This is how tenant reopens a completed or cancelled request and pulls it
            // back into both tenant and staff dashboards.
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

        // Only notify and call afterSave if the update actually succeeded
        if (updated[0]) {
            if (afterSave != null) {
                afterSave.run();
            }
            new Alert(Alert.AlertType.INFORMATION, "Request updated successfully.").showAndWait();
        }
    }

    /**
     * Shared styling helper for "toggle like" labeled controls (CheckBox, etc) that act as actions.
     */
    public static void styleActionToggleButton(Labeled b, String base, String hover, String pressed) {
        String common = "; -fx-text-fill: white; -fx-padding: 6 12; -fx-background-radius: 4;";
        b.setStyle("-fx-background-color: " + base + common);
        b.setOnMouseEntered(ev -> b.setStyle("-fx-background-color: " + hover + common));
        b.setOnMouseExited(ev -> b.setStyle("-fx-background-color: " + base + common));
        b.setOnMousePressed(ev -> b.setStyle("-fx-background-color: " + pressed + common));
        b.setOnMouseReleased(ev -> b.setStyle("-fx-background-color: " + hover + common));
    }
}
