package com.fixflow.ui.gui;

import com.fixflow.model.Role;
import com.fixflow.model.Ticket;
import com.fixflow.model.User;
import com.fixflow.qa.TestingCenterService;
import com.fixflow.service.AssignmentService;
import com.fixflow.service.AuthenticationService;
import com.fixflow.service.FeedbackService;
import com.fixflow.service.NotificationService;
import com.fixflow.service.PriorityService;
import com.fixflow.service.ReportService;
import com.fixflow.service.SLAService;
import com.fixflow.service.TicketService;
import com.fixflow.service.UserService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

/**
 * Master Application Shell featuring Left Navigation Sidebar, User Session Status, and dynamic view switching.
 */
public class MainShellView extends BorderPane {

    private final UserService userService;
    private final AuthenticationService authService;
    private final TicketService ticketService;
    private final AssignmentService assignmentService;
    private final PriorityService priorityService;
    private final SLAService slaService;
    private final NotificationService notificationService;
    private final FeedbackService feedbackService;
    private final ReportService reportService;
    private final TestingCenterService testingCenterService;
    private final User currentUser;
    private final Runnable onLogout;

    private final StackPane centerContainer = new StackPane();
    private final List<Button> navButtons = new ArrayList<>();

    public MainShellView(UserService userService,
                         AuthenticationService authService,
                         TicketService ticketService,
                         AssignmentService assignmentService,
                         PriorityService priorityService,
                         SLAService slaService,
                         NotificationService notificationService,
                         FeedbackService feedbackService,
                         ReportService reportService,
                         TestingCenterService testingCenterService,
                         User currentUser,
                         Runnable onLogout) {
        this.userService = userService;
        this.authService = authService;
        this.ticketService = ticketService;
        this.assignmentService = assignmentService;
        this.priorityService = priorityService;
        this.slaService = slaService;
        this.notificationService = notificationService;
        this.feedbackService = feedbackService;
        this.reportService = reportService;
        this.testingCenterService = testingCenterService;
        this.currentUser = currentUser;
        this.onLogout = onLogout;

        setLeft(buildSidebar());
        setCenter(centerContainer);

        // Default to Dashboard
        showDashboard();
    }

    private VBox buildSidebar() {
        VBox sidebar = new VBox(10);
        sidebar.getStyleClass().add("sidebar");

        // Brand Logo Box
        VBox brandBox = new VBox(4);
        brandBox.setAlignment(Pos.CENTER_LEFT);

        ImageView logoView = FixFlowThemeFx.createBrandLogoView(160, 50);
        if (logoView != null) {
            brandBox.getChildren().add(logoView);
        } else {
            Label brandTitle = new Label("FixFlow");
            brandTitle.getStyleClass().add("sidebar-brand-title");
            Label brandSub = new Label("Smart Maintenance");
            brandSub.getStyleClass().add("sidebar-brand-sub");
            brandBox.getChildren().addAll(brandTitle, brandSub);
        }

        sidebar.getChildren().addAll(brandBox, new Separator());

        // Nav Buttons
        Button dashBtn = createNavButton("📊  Dashboard", this::showDashboard);
        Button ticketsBtn = createNavButton("📋  Tickets Directory", this::showTickets);
        sidebar.getChildren().addAll(dashBtn, ticketsBtn);

        if (currentUser.getRole() == Role.TECHNICIAN || currentUser.getRole() == Role.ADMIN) {
            Button workBtn = createNavButton("🔧  Technician Workbench", this::showTechnicianWorkspace);
            sidebar.getChildren().add(workBtn);
        }

        Button notesBtn = createNavButton("🔔  Notifications", this::showNotifications);
        Button slaBtn = createNavButton("🛡️  SLA Monitoring", this::showSlaMonitoring);
        Button repBtn = createNavButton("📈  Reports & Analytics", this::showReports);
        Button qaBtn = createNavButton("🧪  Testing Center", this::showTestingCenter);
        Button profBtn = createNavButton("⚙️  Profile & Settings", this::showProfileSettings);

        sidebar.getChildren().addAll(notesBtn, slaBtn, repBtn, qaBtn, profBtn);

        Region spacer = new Region();
        VBox.setVgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        sidebar.getChildren().add(spacer);

        // User Profile Footer Card
        VBox userCard = new VBox(6);
        userCard.setStyle("-fx-background-color: #1e293b; -fx-background-radius: 8px; -fx-padding: 12px; -fx-border-color: #334155; -fx-border-radius: 8px;");

        Label nameLbl = new Label(currentUser.getFullName());
        nameLbl.setStyle("-fx-text-fill: #f8fafc; -fx-font-weight: bold; -fx-font-size: 12px;");

        HBox roleBox = new HBox(6);
        roleBox.setAlignment(Pos.CENTER_LEFT);
        Label roleBadge = new Label(currentUser.getRole().name());
        roleBadge.setStyle("-fx-background-color: #0284c7; -fx-text-fill: white; -fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 2px 6px; -fx-background-radius: 4px;");
        Label statusDot = new Label("● Online");
        statusDot.setStyle("-fx-text-fill: #10b981; -fx-font-size: 11px;");
        roleBox.getChildren().addAll(roleBadge, statusDot);

        Button logoutBtn = new Button("Sign Out");
        logoutBtn.setStyle("-fx-background-color: #334155; -fx-text-fill: #e2e8f0; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 6px; -fx-background-radius: 4px; -fx-cursor: hand;");
        logoutBtn.setMaxWidth(Double.MAX_VALUE);
        logoutBtn.setOnAction(e -> onLogout.run());

        userCard.getChildren().addAll(nameLbl, roleBox, logoutBtn);
        sidebar.getChildren().add(userCard);

        return sidebar;
    }

    private Button createNavButton(String text, Runnable action) {
        Button btn = new Button(text);
        btn.getStyleClass().add("nav-button");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setOnAction(e -> {
            highlightNav(btn);
            action.run();
        });
        navButtons.add(btn);
        return btn;
    }

    private void highlightNav(Button active) {
        for (Button b : navButtons) {
            b.getStyleClass().remove("nav-button-active");
        }
        active.getStyleClass().add("nav-button-active");
    }

    private void setView(Node view) {
        centerContainer.getChildren().clear();
        centerContainer.getChildren().add(view);
    }

    public void showDashboard() {
        if (!navButtons.isEmpty()) highlightNav(navButtons.get(0));
        setView(new DashboardViewFx(
                ticketService, userService, assignmentService, reportService, slaService,
                currentUser,
                this::showTicketDetails,
                this::showCreateTicket
        ));
    }

    public void showTickets() {
        if (navButtons.size() > 1) highlightNav(navButtons.get(1));
        setView(new TicketsViewFx(
                ticketService, slaService, currentUser,
                this::showTicketDetails,
                this::showCreateTicket
        ));
    }

    public void showCreateTicket() {
        setView(new CreateTicketDialog(
                ticketService, currentUser,
                created -> showTicketDetails(created),
                this::showTickets
        ));
    }

    public void showTicketDetails(Ticket ticket) {
        setView(new TicketDetailViewFx(
                ticketService, assignmentService, slaService, feedbackService, notificationService, userService,
                currentUser, ticket,
                this::showTickets
        ));
    }

    public void showTechnicianWorkspace() {
        setView(new TechnicianWorkspaceViewFx(
                ticketService, slaService, currentUser,
                this::showTicketDetails
        ));
    }

    public void showNotifications() {
        setView(new NotificationsViewFx(notificationService, currentUser));
    }

    public void showSlaMonitoring() {
        setView(new SlaMonitoringViewFx(ticketService, slaService, this::showTicketDetails));
    }

    public void showReports() {
        setView(new ReportsAnalyticsViewFx(reportService));
    }

    public void showTestingCenter() {
        setView(new TestingCenterViewFx(testingCenterService));
    }

    public void showProfileSettings() {
        setView(new ProfileSettingsViewFx(currentUser));
    }
}
