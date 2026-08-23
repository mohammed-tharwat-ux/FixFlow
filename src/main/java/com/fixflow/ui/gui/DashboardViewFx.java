package com.fixflow.ui.gui;

import com.fixflow.model.Category;
import com.fixflow.model.Priority;
import com.fixflow.model.Role;
import com.fixflow.model.SystemSummaryReport;
import com.fixflow.model.Ticket;
import com.fixflow.model.TicketStatus;
import com.fixflow.model.User;
import com.fixflow.service.AssignmentService;
import com.fixflow.service.ReportService;
import com.fixflow.service.SLAService;
import com.fixflow.service.TicketService;
import com.fixflow.service.UserService;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;


import java.util.List;
import java.util.function.Consumer;

/**
 * Role-tailored Executive & Operational Dashboard View for JavaFX.
 */
public class DashboardViewFx extends ScrollPane {

    private final TicketService ticketService;
    private final UserService userService;
    private final AssignmentService assignmentService;
    private final ReportService reportService;
    private final SLAService slaService;
    private final User currentUser;
    private final Consumer<Ticket> onOpenTicketDetails;
    private final Runnable onNavigateToCreateTicket;

    public DashboardViewFx(TicketService ticketService,
                           UserService userService,
                           AssignmentService assignmentService,
                           ReportService reportService,
                           SLAService slaService,
                           User currentUser,
                           Consumer<Ticket> onOpenTicketDetails,
                           Runnable onNavigateToCreateTicket) {
        this.ticketService = ticketService;
        this.userService = userService;
        this.assignmentService = assignmentService;
        this.reportService = reportService;
        this.slaService = slaService;
        this.currentUser = currentUser;
        this.onOpenTicketDetails = onOpenTicketDetails;
        this.onNavigateToCreateTicket = onNavigateToCreateTicket;

        setFitToWidth(true);
        VBox root = new VBox(24);
        root.setPadding(new Insets(24, 32, 32, 32));

        // Header
        VBox headerBox = new VBox(4);
        Label titleLbl = new Label("Dashboard Overview");
        titleLbl.getStyleClass().add("text-header-1");

        Label welcomeLbl = new Label("Welcome back, " + currentUser.getFullName() + "  |  Role: " + currentUser.getRole());
        welcomeLbl.getStyleClass().add("text-muted");

        headerBox.getChildren().addAll(titleLbl, welcomeLbl);
        root.getChildren().add(headerBox);

        // Render Role-specific Dashboard
        if (currentUser.getRole() == Role.ADMIN) {
            buildAdminDashboard(root);
        } else if (currentUser.getRole() == Role.TECHNICIAN) {
            buildTechnicianDashboard(root);
        } else {
            buildUserDashboard(root);
        }

        setContent(root);
    }

    private void buildAdminDashboard(VBox root) {
        SystemSummaryReport report = reportService.generateSummaryReport();

        // KPI Stat Cards
        FlowPane kpiPane = new FlowPane(16, 16);
        kpiPane.getChildren().addAll(
                FixFlowThemeFx.createStatCard("Total Incidents", String.valueOf(report.totalTickets()), "📊"),
                FixFlowThemeFx.createStatCard("Open / Unassigned", String.valueOf(report.openTickets()), "📂"),
                FixFlowThemeFx.createStatCard("In Progress", String.valueOf(report.inProgressTickets()), "⚡"),
                FixFlowThemeFx.createStatCard("Resolved & Closed", String.valueOf(report.resolvedTickets() + report.closedTickets()), "✓"),
                FixFlowThemeFx.createStatCard("Critical Priority", String.valueOf(report.criticalTickets()), "🚨"),
                FixFlowThemeFx.createStatCard("SLA Compliance", String.format("%.1f%%", report.slaComplianceRatePercentage()), "🛡️"),
                FixFlowThemeFx.createStatCard("SLA Violations", String.valueOf(report.slaViolations()), "⚠️")
        );
        root.getChildren().add(kpiPane);

        // Recent System Incidents
        VBox recentBox = new VBox(12);
        recentBox.getStyleClass().add("card");
        Label recTitle = new Label("Recent System Incidents");
        recTitle.getStyleClass().add("text-header-2");

        TableView<Ticket> table = buildTicketsTable(ticketService.listAllTickets().stream().limit(6).toList());
        recentBox.getChildren().addAll(recTitle, table);
        root.getChildren().add(recentBox);
    }

    private void buildTechnicianDashboard(VBox root) {
        List<Ticket> myTickets = ticketService.getTicketsByTechnician(currentUser.getId());
        long pending = myTickets.stream().filter(t -> t.getStatus() == TicketStatus.ASSIGNED).count();
        long inProgress = myTickets.stream().filter(t -> t.getStatus() == TicketStatus.IN_PROGRESS).count();
        long resolved = myTickets.stream().filter(t -> t.getStatus() == TicketStatus.RESOLVED || t.getStatus() == TicketStatus.CLOSED).count();
        long critical = myTickets.stream().filter(t -> t.getPriority() == Priority.CRITICAL && t.getStatus() != TicketStatus.CLOSED).count();

        FlowPane kpiPane = new FlowPane(16, 16);
        kpiPane.getChildren().addAll(
                FixFlowThemeFx.createStatCard("Assigned to Me", String.valueOf(myTickets.size()), "🔧"),
                FixFlowThemeFx.createStatCard("Pending Action", String.valueOf(pending), "⏱️"),
                FixFlowThemeFx.createStatCard("Work In Progress", String.valueOf(inProgress), "⚡"),
                FixFlowThemeFx.createStatCard("Completed", String.valueOf(resolved), "✓"),
                FixFlowThemeFx.createStatCard("Critical Incidents", String.valueOf(critical), "🚨")
        );
        root.getChildren().add(kpiPane);

        VBox queueBox = new VBox(12);
        queueBox.getStyleClass().add("card");
        Label qTitle = new Label("My Active Work Queue");
        qTitle.getStyleClass().add("text-header-2");

        List<Ticket> active = myTickets.stream()
                .filter(t -> t.getStatus() != TicketStatus.CLOSED && t.getStatus() != TicketStatus.RESOLVED)
                .toList();

        TableView<Ticket> table = buildTicketsTable(active);
        queueBox.getChildren().addAll(qTitle, table);
        root.getChildren().add(queueBox);
    }

    private void buildUserDashboard(VBox root) {
        List<Ticket> myTickets = ticketService.getTicketsByReporter(currentUser.getId());
        long open = myTickets.stream().filter(t -> t.getStatus() == TicketStatus.OPEN || t.getStatus() == TicketStatus.ASSIGNED).count();
        long inProgress = myTickets.stream().filter(t -> t.getStatus() == TicketStatus.IN_PROGRESS).count();
        long resolved = myTickets.stream().filter(t -> t.getStatus() == TicketStatus.RESOLVED).count();
        long closed = myTickets.stream().filter(t -> t.getStatus() == TicketStatus.CLOSED).count();

        HBox topAction = new HBox();
        Button newTicketBtn = new Button("+ Report New Incident");
        newTicketBtn.getStyleClass().add("button-primary");
        newTicketBtn.setOnAction(e -> onNavigateToCreateTicket.run());
        topAction.getChildren().add(newTicketBtn);

        FlowPane kpiPane = new FlowPane(16, 16);
        kpiPane.getChildren().addAll(
                FixFlowThemeFx.createStatCard("My Reported Tickets", String.valueOf(myTickets.size()), "📋"),
                FixFlowThemeFx.createStatCard("Awaiting Assignment", String.valueOf(open), "⏱️"),
                FixFlowThemeFx.createStatCard("Being Repaired", String.valueOf(inProgress), "⚡"),
                FixFlowThemeFx.createStatCard("Resolved (Pending Review)", String.valueOf(resolved), "✓"),
                FixFlowThemeFx.createStatCard("Closed", String.valueOf(closed), "📁")
        );
        root.getChildren().addAll(topAction, kpiPane);

        VBox myBox = new VBox(12);
        myBox.getStyleClass().add("card");
        Label mTitle = new Label("My Recent Incident Reports");
        mTitle.getStyleClass().add("text-header-2");

        TableView<Ticket> table = buildTicketsTable(myTickets);
        myBox.getChildren().addAll(mTitle, table);
        root.getChildren().add(myBox);
    }

    private TableView<Ticket> buildTicketsTable(List<Ticket> tickets) {
        TableView<Ticket> table = new TableView<>();
        table.setPrefHeight(220);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Ticket, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(d -> new SimpleStringProperty("#" + d.getValue().getId()));
        idCol.setPrefWidth(60);

        TableColumn<Ticket, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTitle()));
        titleCol.setPrefWidth(220);

        TableColumn<Ticket, String> catCol = new TableColumn<>("Category");
        catCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCategory().name()));

        TableColumn<Ticket, Void> priCol = new TableColumn<>("Priority");
        priCol.setCellFactory(col -> new javafx.scene.control.TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Ticket t = getTableRow().getItem();
                    setGraphic(FixFlowThemeFx.createPriorityBadge(t.getPriority()));
                }
            }
        });

        TableColumn<Ticket, Void> statCol = new TableColumn<>("Status");
        statCol.setCellFactory(col -> new javafx.scene.control.TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Ticket t = getTableRow().getItem();
                    setGraphic(FixFlowThemeFx.createStatusBadge(t.getStatus()));
                }
            }
        });

        TableColumn<Ticket, String> techCol = new TableColumn<>("Technician");
        techCol.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getAssignedTechnician() != null ? d.getValue().getAssignedTechnician().getFullName() : "Unassigned"
        ));

        TableColumn<Ticket, Void> actionCol = new TableColumn<>("Action");
        actionCol.setCellFactory(col -> new javafx.scene.control.TableCell<>() {
            private final Button btn = new Button("Inspect");
            {
                btn.setStyle("-fx-font-size: 11px; -fx-padding: 3px 8px; -fx-background-color: #0284c7; -fx-text-fill: white; -fx-cursor: hand;");
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Ticket t = getTableRow().getItem();
                    btn.setOnAction(e -> onOpenTicketDetails.accept(t));
                    setGraphic(btn);
                }
            }
        });

        table.getColumns().addAll(idCol, titleCol, catCol, priCol, statCol, techCol, actionCol);
        table.getItems().addAll(tickets);

        table.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && table.getSelectionModel().getSelectedItem() != null) {
                onOpenTicketDetails.accept(table.getSelectionModel().getSelectedItem());
            }
        });

        return table;
    }
}
