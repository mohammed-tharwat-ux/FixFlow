package com.fixflow.ui.gui;

import com.fixflow.model.Category;
import com.fixflow.model.Priority;
import com.fixflow.model.SlaStatus;
import com.fixflow.model.Ticket;
import com.fixflow.model.TicketStatus;
import com.fixflow.model.User;
import com.fixflow.service.SLAService;
import com.fixflow.service.TicketService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

/**
 * Full Ticket Management View with Search, Multi-criteria Filters, and Inspection triggers.
 */
public class TicketsViewFx extends VBox {

    private final TicketService ticketService;
    private final SLAService slaService;
    private final User currentUser;
    private final Consumer<Ticket> onOpenTicketDetails;
    private final Runnable onOpenCreateTicket;
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final ObservableList<Ticket> masterData = FXCollections.observableArrayList();
    private final FilteredList<Ticket> filteredData = new FilteredList<>(masterData, p -> true);
    private final TableView<Ticket> tableView = new TableView<>();

    public TicketsViewFx(TicketService ticketService,
                         SLAService slaService,
                         User currentUser,
                         Consumer<Ticket> onOpenTicketDetails,
                         Runnable onOpenCreateTicket) {
        this.ticketService = ticketService;
        this.slaService = slaService;
        this.currentUser = currentUser;
        this.onOpenTicketDetails = onOpenTicketDetails;
        this.onOpenCreateTicket = onOpenCreateTicket;

        setSpacing(18);
        setPadding(new Insets(24, 32, 32, 32));

        // Header Toolbar
        HBox topBar = new HBox();
        topBar.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(2);
        Label titleLbl = new Label("Ticket Management Directory");
        titleLbl.getStyleClass().add("text-header-1");
        Label subLbl = new Label("Filter, search, track SLA, and inspect maintenance requests across the system");
        subLbl.getStyleClass().add("text-muted");
        titleBox.getChildren().addAll(titleLbl, subLbl);

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        Button createBtn = new Button("+ Create Ticket");
        createBtn.getStyleClass().add("button-primary");
        createBtn.setOnAction(e -> onOpenCreateTicket.run());

        topBar.getChildren().addAll(titleBox, spacer, createBtn);
        getChildren().add(topBar);

        // Filter Bar Card
        HBox filterCard = new HBox(12);
        filterCard.getStyleClass().add("card");
        filterCard.setAlignment(Pos.CENTER_LEFT);
        filterCard.setPadding(new Insets(14, 18, 14, 18));

        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Search by title or location...");
        searchField.setPrefWidth(240);

        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().add("All Statuses");
        for (TicketStatus s : TicketStatus.values()) statusCombo.getItems().add(s.name());
        statusCombo.setValue("All Statuses");

        ComboBox<String> priorityCombo = new ComboBox<>();
        priorityCombo.getItems().add("All Priorities");
        for (Priority p : Priority.values()) priorityCombo.getItems().add(p.name());
        priorityCombo.setValue("All Priorities");

        ComboBox<String> categoryCombo = new ComboBox<>();
        categoryCombo.getItems().add("All Categories");
        for (Category c : Category.values()) categoryCombo.getItems().add(c.name());
        categoryCombo.setValue("All Categories");

        Button resetBtn = new Button("Reset");
        resetBtn.getStyleClass().add("button-secondary");
        resetBtn.setOnAction(e -> {
            searchField.clear();
            statusCombo.setValue("All Statuses");
            priorityCombo.setValue("All Priorities");
            categoryCombo.setValue("All Categories");
        });

        // Filter predicate logic
        Runnable applyFilters = () -> {
            String query = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
            String selStatus = statusCombo.getValue();
            String selPriority = priorityCombo.getValue();
            String selCategory = categoryCombo.getValue();

            filteredData.setPredicate(ticket -> {
                boolean matchesQuery = query.isEmpty() ||
                        ticket.getTitle().toLowerCase().contains(query) ||
                        ticket.getLocation().toLowerCase().contains(query) ||
                        ticket.getDescription().toLowerCase().contains(query);

                boolean matchesStatus = "All Statuses".equals(selStatus) || ticket.getStatus().name().equals(selStatus);
                boolean matchesPriority = "All Priorities".equals(selPriority) || ticket.getPriority().name().equals(selPriority);
                boolean matchesCategory = "All Categories".equals(selCategory) || ticket.getCategory().name().equals(selCategory);

                return matchesQuery && matchesStatus && matchesPriority && matchesCategory;
            });
        };

        searchField.textProperty().addListener((obs, oldV, newV) -> applyFilters.run());
        statusCombo.valueProperty().addListener((obs, oldV, newV) -> applyFilters.run());
        priorityCombo.valueProperty().addListener((obs, oldV, newV) -> applyFilters.run());
        categoryCombo.valueProperty().addListener((obs, oldV, newV) -> applyFilters.run());

        filterCard.getChildren().addAll(searchField, statusCombo, priorityCombo, categoryCombo, resetBtn);
        getChildren().add(filterCard);

        // Table View
        buildTable();
        VBox.setVgrow(tableView, javafx.scene.layout.Priority.ALWAYS);
        getChildren().add(tableView);

        refreshData();
    }

    public void refreshData() {
        masterData.clear();
        masterData.addAll(ticketService.listAllTickets());
    }

    private void buildTable() {
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

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

        TableColumn<Ticket, String> dateCol = new TableColumn<>("Created At");
        dateCol.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getCreatedAt() != null ? d.getValue().getCreatedAt().format(dtf) : "N/A"
        ));

        TableColumn<Ticket, Void> actCol = new TableColumn<>("Action");
        actCol.setCellFactory(col -> new javafx.scene.control.TableCell<>() {
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

        tableView.getColumns().addAll(idCol, titleCol, catCol, priCol, statCol, techCol, slaCol, dateCol, actCol);
        tableView.setItems(filteredData);

        tableView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && tableView.getSelectionModel().getSelectedItem() != null) {
                onOpenTicketDetails.accept(tableView.getSelectionModel().getSelectedItem());
            }
        });
    }
}
