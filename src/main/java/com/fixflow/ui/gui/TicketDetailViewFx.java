package com.fixflow.ui.gui;

import com.fixflow.exception.InvalidTicketStatusException;
import com.fixflow.exception.ValidationException;
import com.fixflow.model.Feedback;
import com.fixflow.model.Notification;
import com.fixflow.model.Role;
import com.fixflow.model.SlaStatus;
import com.fixflow.model.Ticket;
import com.fixflow.model.TicketStatus;
import com.fixflow.model.User;
import com.fixflow.service.AssignmentService;
import com.fixflow.service.FeedbackService;
import com.fixflow.service.NotificationService;
import com.fixflow.service.SLAService;
import com.fixflow.service.TicketService;
import com.fixflow.service.UserService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Detailed Ticket Inspection View featuring SLA Panel, Lifecycle Timeline, Resolution Notes,
 * Feedback, and Role-Based Action handlers.
 */
public class TicketDetailViewFx extends ScrollPane {

    private final TicketService ticketService;
    private final AssignmentService assignmentService;
    private final SLAService slaService;
    private final FeedbackService feedbackService;
    private final NotificationService notificationService;
    private final UserService userService;
    private final User currentUser;
    private Ticket currentTicket;
    private final Runnable onBack;
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final VBox contentBox = new VBox(20);

    public TicketDetailViewFx(TicketService ticketService,
                              AssignmentService assignmentService,
                              SLAService slaService,
                              FeedbackService feedbackService,
                              NotificationService notificationService,
                              UserService userService,
                              User currentUser,
                              Ticket ticket,
                              Runnable onBack) {
        this.ticketService = ticketService;
        this.assignmentService = assignmentService;
        this.slaService = slaService;
        this.feedbackService = feedbackService;
        this.notificationService = notificationService;
        this.userService = userService;
        this.currentUser = currentUser;
        this.currentTicket = ticket;
        this.onBack = onBack;

        setFitToWidth(true);
        contentBox.setPadding(new Insets(24, 32, 32, 32));

        renderView();
        setContent(contentBox);
    }

    private void renderView() {
        contentBox.getChildren().clear();

        // Refresh current ticket from repository
        currentTicket = ticketService.getTicketById(currentTicket.getId());

        // Header
        HBox topBar = new HBox(12);
        topBar.setAlignment(Pos.CENTER_LEFT);

        Button backBtn = new Button("<- Back to Tickets");
        backBtn.getStyleClass().add("button-secondary");
        backBtn.setOnAction(e -> onBack.run());

        Label titleLbl = new Label("Ticket #" + currentTicket.getId() + " - " + currentTicket.getTitle());
        titleLbl.getStyleClass().add("text-header-1");

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        HBox badges = new HBox(8);
        badges.setAlignment(Pos.CENTER_RIGHT);
        badges.getChildren().addAll(
                FixFlowThemeFx.createPriorityBadge(currentTicket.getPriority()),
                FixFlowThemeFx.createStatusBadge(currentTicket.getStatus())
        );

        topBar.getChildren().addAll(backBtn, titleLbl, spacer, badges);
        contentBox.getChildren().add(topBar);

        // Core Information Grid Card
        VBox infoCard = new VBox(14);
        infoCard.getStyleClass().add("card");

        Label cardTitle = new Label("Incident Core Metadata");
        cardTitle.getStyleClass().add("text-header-2");

        GridPane grid = new GridPane();
        grid.setHgap(30);
        grid.setVgap(10);

        grid.add(new Label("Category:"), 0, 0);
        Label catVal = new Label(currentTicket.getCategory().name());
        catVal.setStyle("-fx-font-weight: bold;");
        grid.add(catVal, 1, 0);

        grid.add(new Label("Location:"), 0, 1);
        Label locVal = new Label(currentTicket.getLocation());
        locVal.setStyle("-fx-font-weight: bold;");
        grid.add(locVal, 1, 1);

        grid.add(new Label("Reported By:"), 0, 2);
        Label repVal = new Label(currentTicket.getReporter() != null ? currentTicket.getReporter().getFullName() + " (" + currentTicket.getReporter().getEmail() + ")" : "N/A");
        grid.add(repVal, 1, 2);

        grid.add(new Label("Assigned Technician:"), 2, 0);
        Label techVal = new Label(currentTicket.getAssignedTechnician() != null ? currentTicket.getAssignedTechnician().getFullName() + " (" + currentTicket.getAssignedTechnician().getUsername() + ")" : "Unassigned");
        techVal.setStyle("-fx-font-weight: bold; -fx-text-fill: #0284c7;");
        grid.add(techVal, 3, 0);

        grid.add(new Label("Created At:"), 2, 1);
        grid.add(new Label(currentTicket.getCreatedAt() != null ? currentTicket.getCreatedAt().format(dtf) : "N/A"), 3, 1);

        grid.add(new Label("Resolved At:"), 2, 2);
        grid.add(new Label(currentTicket.getResolvedAt() != null ? currentTicket.getResolvedAt().format(dtf) : "Pending Resolution"), 3, 2);

        VBox descBox = new VBox(4);
        Label descTitle = new Label("Problem Description:");
        descTitle.getStyleClass().add("form-label");
        Label descText = new Label(currentTicket.getDescription());
        descText.setWrapText(true);
        descText.setStyle("-fx-background-color: #f8fafc; -fx-padding: 12px; -fx-background-radius: 6px; -fx-border-color: #e2e8f0; -fx-border-radius: 6px;");
        descBox.getChildren().addAll(descTitle, descText);

        infoCard.getChildren().addAll(cardTitle, grid, new Separator(), descBox);
        contentBox.getChildren().add(infoCard);

        // SLA Performance Card
        VBox slaCard = new VBox(12);
        slaCard.getStyleClass().add("card");
        Label slaTitle = new Label("SLA Target & Deadline Performance");
        slaTitle.getStyleClass().add("text-header-2");

        Duration targetDuration = slaService.getSlaTargetDuration(currentTicket.getPriority());
        SlaStatus slaStatus = slaService.calculateSlaStatus(currentTicket, null);
        Duration remaining = slaService.getRemainingTime(currentTicket, null);

        HBox slaStats = new HBox(30);
        slaStats.setAlignment(Pos.CENTER_LEFT);

        VBox s1 = new VBox(2);
        s1.getChildren().addAll(new Label("Target Window:"), new Label(targetDuration.toHours() + " Hours"));
        s1.getChildren().get(1).setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        VBox s2 = new VBox(2);
        String remText = (currentTicket.getStatus() == TicketStatus.RESOLVED || currentTicket.getStatus() == TicketStatus.CLOSED)
                ? "Resolved in " + slaService.calculateResolutionDuration(currentTicket).toHours() + "h " + slaService.calculateResolutionDuration(currentTicket).toMinutesPart() + "m"
                : (remaining.isNegative() ? "OVERDUE by " + Math.abs(remaining.toHours()) + " hrs" : remaining.toHours() + "h " + remaining.toMinutesPart() + "m remaining");
        s2.getChildren().addAll(new Label("Time Performance:"), new Label(remText));
        s2.getChildren().get(1).setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        VBox s3 = new VBox(2);
        s3.getChildren().addAll(new Label("SLA Status:"), FixFlowThemeFx.createSlaBadge(slaStatus));

        slaStats.getChildren().addAll(s1, s2, s3);
        slaCard.getChildren().addAll(slaTitle, slaStats);
        contentBox.getChildren().add(slaCard);

        // Lifecycle Timeline Card
        VBox timelineCard = new VBox(10);
        timelineCard.getStyleClass().add("card");
        Label timeTitle = new Label("Incident Progress Timeline");
        timeTitle.getStyleClass().add("text-header-2");

        HBox stepsBox = new HBox(12);
        stepsBox.setAlignment(Pos.CENTER_LEFT);

        stepsBox.getChildren().addAll(
                createTimelineStep("1. Reported", true),
                createTimelineArrow(),
                createTimelineStep("2. Assigned", currentTicket.getAssignedTechnician() != null),
                createTimelineArrow(),
                createTimelineStep("3. In Progress", currentTicket.getStatus() == TicketStatus.IN_PROGRESS || currentTicket.getStatus() == TicketStatus.RESOLVED || currentTicket.getStatus() == TicketStatus.CLOSED),
                createTimelineArrow(),
                createTimelineStep("4. Resolved", currentTicket.getStatus() == TicketStatus.RESOLVED || currentTicket.getStatus() == TicketStatus.CLOSED),
                createTimelineArrow(),
                createTimelineStep("5. Closed", currentTicket.getStatus() == TicketStatus.CLOSED)
        );

        timelineCard.getChildren().addAll(timeTitle, stepsBox);
        contentBox.getChildren().add(timelineCard);

        // Resolution Notes
        if (currentTicket.getResolutionNotes() != null && !currentTicket.getResolutionNotes().isEmpty()) {
            VBox resCard = new VBox(8);
            resCard.getStyleClass().add("card");
            Label resTitle = new Label("Technician Resolution Notes");
            resTitle.getStyleClass().add("text-header-2");
            Label resContent = new Label("\"" + currentTicket.getResolutionNotes() + "\"");
            resContent.setStyle("-fx-font-style: italic; -fx-text-fill: #0f172a; -fx-font-size: 13px;");
            resCard.getChildren().addAll(resTitle, resContent);
            contentBox.getChildren().add(resCard);
        }

        // Feedback Card
        Optional<Feedback> feedback = feedbackService.getFeedbackForTicket(currentTicket.getId());
        if (feedback.isPresent()) {
            VBox fCard = new VBox(8);
            fCard.getStyleClass().add("card");
            Label fTitle = new Label("Customer Satisfaction Feedback");
            fTitle.getStyleClass().add("text-header-2");
            Label fRating = new Label("Rating: " + "* ".repeat(feedback.get().getRating()) + " (" + feedback.get().getRating() + " / 5 Stars)");
            fRating.setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold; -fx-font-size: 14px;");
            Label fComment = new Label("\"" + feedback.get().getComment() + "\"");
            fCard.getChildren().addAll(fTitle, fRating, fComment);
            contentBox.getChildren().add(fCard);
        }

        // Action Toolbar
        HBox actionsBar = new HBox(12);
        actionsBar.setAlignment(Pos.CENTER_RIGHT);
        actionsBar.setPadding(new Insets(10, 0, 0, 0));

        // Admin: Assign Tech
        if (currentUser.getRole() == Role.ADMIN && currentTicket.getStatus() == TicketStatus.OPEN) {
            Button assignBtn = new Button("Assign Technician");
            assignBtn.getStyleClass().add("button-primary");
            assignBtn.setOnAction(e -> showAssignTechnicianDialog());
            actionsBar.getChildren().add(assignBtn);
        }

        // Technician: Start Work / Resolve
        if (currentUser.getRole() == Role.TECHNICIAN || currentUser.getRole() == Role.ADMIN) {
            if (currentTicket.getStatus() == TicketStatus.ASSIGNED) {
                Button startBtn = new Button("Start Work (IN_PROGRESS)");
                startBtn.getStyleClass().add("button-primary");
                startBtn.setOnAction(e -> {
                    try {
                        ticketService.startProgress(currentTicket.getId(), currentUser.getId());
                        renderView();
                    } catch (Exception ex) {
                        showErrorAlert("Could not start work", ex.getMessage());
                    }
                });
                actionsBar.getChildren().add(startBtn);
            }

            if (currentTicket.getStatus() == TicketStatus.IN_PROGRESS) {
                Button resolveBtn = new Button("Resolve Ticket");
                resolveBtn.getStyleClass().add("button-success");
                resolveBtn.setOnAction(e -> showResolveDialog());
                actionsBar.getChildren().add(resolveBtn);
            }
        }

        // User / Admin: Close Ticket & Feedback
        if ((currentUser.getRole() == Role.USER || currentUser.getRole() == Role.ADMIN) && currentTicket.getStatus() == TicketStatus.RESOLVED) {
            Button closeBtn = new Button("Close Ticket");
            closeBtn.getStyleClass().add("button-primary");
            closeBtn.setOnAction(e -> {
                try {
                    ticketService.closeTicket(currentTicket.getId(), currentUser.getId());
                    renderView();
                } catch (Exception ex) {
                    showErrorAlert("Could not close ticket", ex.getMessage());
                }
            });
            actionsBar.getChildren().add(closeBtn);
        }

        if (currentUser.getRole() == Role.USER && (currentTicket.getStatus() == TicketStatus.RESOLVED || currentTicket.getStatus() == TicketStatus.CLOSED) && feedback.isEmpty()) {
            Button feedbackBtn = new Button("Submit Feedback");
            feedbackBtn.getStyleClass().add("button-secondary");
            feedbackBtn.setOnAction(e -> showFeedbackDialog());
            actionsBar.getChildren().add(feedbackBtn);
        }

        if (!actionsBar.getChildren().isEmpty()) {
            contentBox.getChildren().add(actionsBar);
        }
    }

    private Label createTimelineStep(String label, boolean active) {
        Label step = new Label(label);
        step.setStyle(active
                ? "-fx-background-color: #e0f2fe; -fx-text-fill: #0284c7; -fx-font-weight: bold; -fx-padding: 6px 12px; -fx-background-radius: 12px;"
                : "-fx-background-color: #f1f5f9; -fx-text-fill: #94a3b8; -fx-padding: 6px 12px; -fx-background-radius: 12px;"
        );
        return step;
    }

    private Label createTimelineArrow() {
        Label arrow = new Label("->");
        arrow.setStyle("-fx-text-fill: #94a3b8; -fx-font-weight: bold;");
        return arrow;
    }

    private void showAssignTechnicianDialog() {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Assign Technician");
        dialog.setHeaderText("Select an active technician for Ticket #" + currentTicket.getId());

        ComboBox<User> techCombo = new ComboBox<>();
        techCombo.getItems().addAll(assignmentService.getAvailableTechnicians());
        if (!techCombo.getItems().isEmpty()) techCombo.setValue(techCombo.getItems().get(0));

        VBox box = new VBox(10);
        box.getChildren().addAll(new Label("Select Technician:"), techCombo);
        dialog.getDialogPane().setContent(box);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> btn == ButtonType.OK ? techCombo.getValue() : null);
        Optional<User> result = dialog.showAndWait();
        result.ifPresent(tech -> {
            try {
                assignmentService.assignTechnician(currentTicket.getId(), tech.getId(), currentUser.getId());
                renderView();
            } catch (Exception ex) {
                showErrorAlert("Assignment Failed", ex.getMessage());
            }
        });
    }

    private void showResolveDialog() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Resolve Ticket");
        dialog.setHeaderText("Enter resolution summary notes for Ticket #" + currentTicket.getId());

        TextArea notesArea = new TextArea();
        notesArea.setPromptText("Describe the repair or resolution actions taken...");
        notesArea.setPrefRowCount(4);

        VBox box = new VBox(10);
        box.getChildren().addAll(new Label("Resolution Notes:"), notesArea);
        dialog.getDialogPane().setContent(box);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> btn == ButtonType.OK ? notesArea.getText() : null);
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(notes -> {
            try {
                ticketService.resolveTicket(currentTicket.getId(), currentUser.getId(), notes);
                renderView();
            } catch (Exception ex) {
                showErrorAlert("Resolution Failed", ex.getMessage());
            }
        });
    }

    private void showFeedbackDialog() {
        Dialog<FeedbackInput> dialog = new Dialog<>();
        dialog.setTitle("Customer Feedback");
        dialog.setHeaderText("Rate the maintenance repair service for Ticket #" + currentTicket.getId());

        ComboBox<Integer> ratingCombo = new ComboBox<>();
        ratingCombo.getItems().addAll(5, 4, 3, 2, 1);
        ratingCombo.setValue(5);

        TextField commentField = new TextField();
        commentField.setPromptText("Optional comments...");

        VBox box = new VBox(10);
        box.getChildren().addAll(new Label("Rating (1-5 Stars):"), ratingCombo, new Label("Comments:"), commentField);
        dialog.getDialogPane().setContent(box);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> btn == ButtonType.OK ? new FeedbackInput(ratingCombo.getValue(), commentField.getText()) : null);
        Optional<FeedbackInput> result = dialog.showAndWait();
        result.ifPresent(input -> {
            try {
                feedbackService.submitFeedback(currentTicket.getId(), currentUser.getId(), input.rating, input.comment);
                renderView();
            } catch (Exception ex) {
                showErrorAlert("Feedback Error", ex.getMessage());
            }
        });
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private record FeedbackInput(int rating, String comment) {}
}
