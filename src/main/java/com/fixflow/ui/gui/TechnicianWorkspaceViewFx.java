package com.fixflow.ui.gui;

import com.fixflow.model.Priority;
import com.fixflow.model.SlaStatus;
import com.fixflow.model.Ticket;
import com.fixflow.model.TicketStatus;
import com.fixflow.model.User;
import com.fixflow.service.SLAService;
import com.fixflow.service.TicketService;
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
 * Technician Dedicated Workbench View.
 */
public class TechnicianWorkspaceViewFx extends ScrollPane {

    private final TicketService ticketService;
    private final SLAService slaService;
    private final User currentUser;
    private final Consumer<Ticket> onOpenTicketDetails;

    public TechnicianWorkspaceViewFx(TicketService ticketService,
                                     SLAService slaService,
                                     User currentUser,
                                     Consumer<Ticket> onOpenTicketDetails) {
        this.ticketService = ticketService;
        this.slaService = slaService;
        this.currentUser = currentUser;
        this.onOpenTicketDetails = onOpenTicketDetails;

        setFitToWidth(true);
        VBox root = new VBox(20);
        root.setPadding(new Insets(24, 32, 32, 32));

        Label titleLbl = new Label("Technician Incident Workbench");
        titleLbl.getStyleClass().add("text-header-1");

        Label subLbl = new Label("Manage assigned repairs, track SLA risk alerts, and update resolution notes");
        subLbl.getStyleClass().add("text-muted");

        root.getChildren().addAll(titleLbl, subLbl);

        List<Ticket> myTickets = ticketService.getTicketsByTechnician(currentUser.getId());
        List<Ticket> active = myTickets.stream()
                .filter(t -> t.getStatus() == TicketStatus.ASSIGNED || t.getStatus() == TicketStatus.IN_PROGRESS)
                .toList();

        long criticalCount = active.stream().filter(t -> t.getPriority() == Priority.CRITICAL).count();

        FlowPane statPane = new FlowPane(16, 16);
        statPane.getChildren().addAll(
                FixFlowThemeFx.createStatCard("Active Queue", String.valueOf(active.size()), "⚡"),
                FixFlowThemeFx.createStatCard("Critical Incidents", String.valueOf(criticalCount), "🚨"),
                FixFlowThemeFx.createStatCard("Total Completed", String.valueOf(myTickets.size() - active.size()), "✓")
        );
        root.getChildren().add(statPane);

        VBox tableCard = new VBox(12);
        tableCard.getStyleClass().add("card");
        Label tTitle = new Label("Active Assignments Queue");
        tTitle.getStyleClass().add("text-header-2");

        TableView<Ticket> table = new TableView<>();
        table.setPrefHeight(340);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Ticket, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(d -> new SimpleStringProperty("#" + d.getValue().getId()));
        idCol.setPrefWidth(60);

        TableColumn<Ticket, String> titleCol = new TableColumn<>("Incident Title");
        titleCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTitle()));
        titleCol.setPrefWidth(220);

        TableColumn<Ticket, String> locCol = new TableColumn<>("Location");
        locCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getLocation()));

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

        TableColumn<Ticket, Void> slaCol = new TableColumn<>("SLA Status");
        slaCol.setCellFactory(col -> new javafx.scene.control.TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Ticket t = getTableRow().getItem();
                    SlaStatus sla = slaService.calculateSlaStatus(t, null);
                    setGraphic(FixFlowThemeFx.createSlaBadge(sla));
                }
            }
        });

        TableColumn<Ticket, Void> actCol = new TableColumn<>("Actions");
        actCol.setCellFactory(col -> new javafx.scene.control.TableCell<>() {
            private final Button inspectBtn = new Button("Open Ticket");
            {
                inspectBtn.getStyleClass().add("button-primary");
                inspectBtn.setStyle("-fx-font-size: 11px; -fx-padding: 4px 10px;");
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Ticket t = getTableRow().getItem();
                    inspectBtn.setOnAction(e -> onOpenTicketDetails.accept(t));
                    setGraphic(inspectBtn);
                }
            }
        });

        table.getColumns().addAll(idCol, titleCol, locCol, priCol, statCol, slaCol, actCol);
        table.getItems().addAll(active);

        tableCard.getChildren().addAll(tTitle, table);
        root.getChildren().add(tableCard);

        setContent(root);
    }
}
