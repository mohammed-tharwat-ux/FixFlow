package com.fixflow.ui.gui;

import com.fixflow.model.Category;
import com.fixflow.model.SystemSummaryReport;
import com.fixflow.service.ReportService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.Map;

/**
 * Executive Analytics & Reports View featuring JavaFX PieCharts and BarCharts.
 */
public class ReportsAnalyticsViewFx extends ScrollPane {

    private final ReportService reportService;

    public ReportsAnalyticsViewFx(ReportService reportService) {
        this.reportService = reportService;

        setFitToWidth(true);
        VBox root = new VBox(20);
        root.setPadding(new Insets(24, 32, 32, 32));

        Label titleLbl = new Label("Executive Analytics & Operational Reports");
        titleLbl.getStyleClass().add("text-header-1");

        Label subLbl = new Label("Real-time aggregated metrics, SLA compliance performance, and incident category distributions");
        subLbl.getStyleClass().add("text-muted");

        root.getChildren().addAll(titleLbl, subLbl);

        SystemSummaryReport report = reportService.generateSummaryReport();

        // Stat Cards
        FlowPane statPane = new FlowPane(16, 16);
        statPane.getChildren().addAll(
                FixFlowThemeFx.createStatCard("Total Volume", String.valueOf(report.totalTickets()), "📊"),
                FixFlowThemeFx.createStatCard("SLA Compliance Rate", String.format("%.2f%%", report.slaComplianceRatePercentage()), "🛡️"),
                FixFlowThemeFx.createStatCard("Avg Resolution Time", String.format("%.2f hrs", report.averageResolutionTimeHours()), "⏱️"),
                FixFlowThemeFx.createStatCard("Total SLA Violations", String.valueOf(report.slaViolations()), "⚠️")
        );
        root.getChildren().add(statPane);

        // Visual Charts Container
        HBox chartsBox1 = new HBox(20);
        chartsBox1.setAlignment(Pos.CENTER);

        // Chart 1: Status Distribution
        VBox card1 = new VBox(10);
        card1.getStyleClass().add("card");
        card1.setPrefWidth(420);
        Label c1Title = new Label("Incidents by Status");
        c1Title.getStyleClass().add("text-header-2");

        ObservableList<PieChart.Data> statusData = FXCollections.observableArrayList(
                new PieChart.Data("OPEN (" + report.openTickets() + ")", report.openTickets()),
                new PieChart.Data("ASSIGNED (" + report.assignedTickets() + ")", report.assignedTickets()),
                new PieChart.Data("IN PROGRESS (" + report.inProgressTickets() + ")", report.inProgressTickets()),
                new PieChart.Data("RESOLVED (" + report.resolvedTickets() + ")", report.resolvedTickets()),
                new PieChart.Data("CLOSED (" + report.closedTickets() + ")", report.closedTickets())
        );
        PieChart statusChart = new PieChart(statusData);
        statusChart.setPrefHeight(260);
        statusChart.setLegendVisible(true);
        card1.getChildren().addAll(c1Title, statusChart);

        // Chart 2: Category Distribution
        VBox card2 = new VBox(10);
        card2.getStyleClass().add("card");
        card2.setPrefWidth(420);
        Label c2Title = new Label("Incidents by Category");
        c2Title.getStyleClass().add("text-header-2");

        ObservableList<PieChart.Data> catData = FXCollections.observableArrayList();
        for (Category c : Category.values()) {
            long count = report.ticketsByCategory().getOrDefault(c, 0L);
            if (count > 0) {
                catData.add(new PieChart.Data(c.name() + " (" + count + ")", count));
            }
        }
        PieChart catChart = new PieChart(catData);
        catChart.setPrefHeight(260);
        card2.getChildren().addAll(c2Title, catChart);

        chartsBox1.getChildren().addAll(card1, card2);
        root.getChildren().add(chartsBox1);

        // Chart 3: Technician Workload Bar Chart
        VBox card3 = new VBox(10);
        card3.getStyleClass().add("card");
        Label c3Title = new Label("Technician Incident Assignments & Workload");
        c3Title.getStyleClass().add("text-header-2");

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Technician");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Assigned Tickets");

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setPrefHeight(260);
        barChart.setLegendVisible(false);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (Map.Entry<String, Long> entry : report.ticketsByTechnician().entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }
        barChart.getData().add(series);

        card3.getChildren().addAll(c3Title, barChart);
        root.getChildren().add(card3);

        setContent(root);
    }
}
