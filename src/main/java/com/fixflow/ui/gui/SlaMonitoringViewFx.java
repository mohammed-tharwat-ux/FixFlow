package com.fixflow.ui.gui;

import com.fixflow.model.Priority;
import com.fixflow.model.SlaStatus;
import com.fixflow.model.Ticket;
import com.fixflow.model.TicketStatus;
import com.fixflow.service.SLAService;
import com.fixflow.service.TicketService;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.Duration;
import java.util.List;
import java.util.function.Consumer;

/**
 * Real-time SLA Compliance Monitoring & Deadline Surveillance View.
 */
public class SlaMonitoringViewFx extends ScrollPane {

    private final TicketService ticketService;
    private final SLAService slaService;
    private final Consumer<Ticket> onOpenTicketDetails;

    public SlaMonitoringViewFx(TicketService ticketService,
                               SLAService slaService,
                               Consumer<Ticket> onOpenTicketDetails) {
        this.ticketService = ticketService;
        this.slaService = slaService;
        this.onOpenTicketDetails = onOpenTicketDetails;

        setFitToWidth(true);
        VBox root = new VBox(20);
        root.setPadding(new Insets(24, 32, 32, 32));

        Label titleLbl = new Label("SLA Real-time Compliance Monitoring");
        titleLbl.getStyleClass().add("text-header-1");

        Label subLbl = new Label("Multi-tier incident deadline enforcement, overdue tracking, and SLA resolution performance");
        subLbl.getStyleClass().add("text-muted");

        root.getChildren().addAll(titleLbl, subLbl);

        List<Ticket> all = ticketService.listAllTickets();
        long met = all.stream().filter(t -> slaService.calculateSlaStatus(t, null) == SlaStatus.MET).count();
        long violated = all.stream().filter(t -> slaService.calculateSlaStatus(t, null) == SlaStatus.VIOLATED).count();
        long pending = all.stream().filter(t -> slaService.calculateSlaStatus(t, null) == SlaStatus.PENDING).count();

        FlowPane statPane = new FlowPane(16, 16);
        statPane.getChildren().addAll(
                FixFlowThemeFx.createStatCard("Total Monitored", String.valueOf(all.size()), "🛡️"),
                FixFlowThemeFx.createStatCard("SLA Compliant (Met)", String.valueOf(met), "✓"),
                FixFlowThemeFx.createStatCard("SLA Violations", String.valueOf(violated), "⚠️"),
                FixFlowThemeFx.createStatCard("Active / Pending", String.valueOf(pending), "⏱️")
        );
        root.getChildren().add(statPane);

        // SLA Policies Info Card
        HBox policyBox = new HBox(20);
        policyBox.getStyleClass().add("card");
        policyBox.setAlignment(Pos.CENTER_LEFT);

        policyBox.getChildren().addAll(
                createPolicyItem("CRITICAL", "2 Hours", "🚨 Emergency / Hazard"),
                createPolicyItem("HIGH", "8 Hours", "⚡ System / Server"),
                createPolicyItem("MEDIUM", "24 Hours", "🔧 Hardware / Network"),
                createPolicyItem("LOW", "72 Hours", "📋 Routine Facility")
        );
        root.getChildren().add(policyBox);

        // Table
        VBox tableCard = new VBox(12);
        tableCard.getStyleClass().add("card");
        Label tTitle = new Label("Live Incident SLA Performance Table");
        tTitle.getStyleClass().add("text-header-2");

        TableView<Ticket> table = new TableView<>();
        table.setPrefHeight(380);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Ticket, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(d -> new SimpleStringProperty("#" + d.getValue().getId()));
        idCol.setPrefWidth(60);

        TableColumn<Ticket, String> titleCol = new TableColumn<>("Incident Title");
        titleCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTitle()));
        titleCol.setPrefWidth(220);

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

        TableColumn<Ticket, String> targetCol = new TableColumn<>("SLA Target");
        targetCol.setCellValueFactory(d -> new SimpleStringProperty(
                slaService.getSlaTargetDuration(d.getValue().getPriority()).toHours() + " Hours"
        ));

        TableColumn<Ticket, String> remCol = new TableColumn<>("Remaining Time");
        remCol.setCellValueFactory(d -> {
            Ticket t = d.getValue();
            if (t.getStatus() == TicketStatus.RESOLVED || t.getStatus() == TicketStatus.CLOSED) {
                Duration res = slaService.calculateResolutionDuration(t);
                return new SimpleStringProperty("Resolved (" + res.toHours() + "h " + res.toMinutesPart() + "m)");
            }
            Duration r = slaService.getRemainingTime(t, null);
            if (r.isNegative()) {
                return new SimpleStringProperty("OVERDUE (" + Math.abs(r.toHours()) + "h)");
            }
            return new SimpleStringProperty(r.toHours() + "h " + r.toMinutesPart() + "m");
        });

        TableColumn<Ticket, Void> statCol = new TableColumn<>("SLA Status");
        statCol.setCellFactory(col -> new javafx.scene.control.TableCell<>() {
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

        table.getColumns().addAll(idCol, titleCol, priCol, targetCol, remCol, statCol);
        table.getItems().addAll(all);

        table.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && table.getSelectionModel().getSelectedItem() != null) {
                onOpenTicketDetails.accept(table.getSelectionModel().getSelectedItem());
            }
        });

        tableCard.getChildren().addAll(tTitle, table);
        root.getChildren().add(tableCard);

        setContent(root);
    }

    private VBox createPolicyItem(String name, String window, String desc) {
        VBox box = new VBox(2);
        Label nLbl = new Label(name + " : " + window);
        nLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #0284c7;");
        Label dLbl = new Label(desc);
        dLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");
        box.getChildren().addAll(nLbl, dLbl);
        return box;
    }
}
