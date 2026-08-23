package com.fixflow.ui.gui;

import com.fixflow.data.DemoDataLoader;
import com.fixflow.model.User;
import com.fixflow.qa.TestingCenterService;
import com.fixflow.repository.FeedbackRepository;
import com.fixflow.repository.InMemoryFeedbackRepository;
import com.fixflow.repository.InMemoryNotificationRepository;
import com.fixflow.repository.InMemoryTicketRepository;
import com.fixflow.repository.InMemoryUserRepository;
import com.fixflow.repository.NotificationRepository;
import com.fixflow.repository.TicketRepository;
import com.fixflow.repository.UserRepository;
import com.fixflow.security.PBKDF2PasswordHasher;
import com.fixflow.security.PasswordHasher;
import com.fixflow.service.AssignmentService;
import com.fixflow.service.AuthenticationService;
import com.fixflow.service.FeedbackService;
import com.fixflow.service.NotificationService;
import com.fixflow.service.PriorityService;
import com.fixflow.service.ReportService;
import com.fixflow.service.SLAService;
import com.fixflow.service.TicketService;
import com.fixflow.service.UserService;
import com.fixflow.validation.TicketValidator;
import com.fixflow.validation.UserValidator;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.Objects;

/**
 * Main JavaFX Desktop Application Launcher for FixFlow.
 */
public class FixFlowDesktopApp extends Application {

    private UserService userService;
    private AuthenticationService authService;
    private TicketService ticketService;
    private AssignmentService assignmentService;
    private PriorityService priorityService;
    private SLAService slaService;
    private NotificationService notificationService;
    private FeedbackService feedbackService;
    private ReportService reportService;
    private TestingCenterService testingCenterService;

    private Stage primaryStage;
    private StackPane rootPane;
    private Scene mainScene;

    @Override
    public void init() {
        UserRepository userRepository = new InMemoryUserRepository();
        TicketRepository ticketRepository = new InMemoryTicketRepository();
        NotificationRepository notificationRepository = new InMemoryNotificationRepository();
        FeedbackRepository feedbackRepository = new InMemoryFeedbackRepository();

        PasswordHasher passwordHasher = new PBKDF2PasswordHasher();
        UserValidator userValidator = new UserValidator();
        TicketValidator ticketValidator = new TicketValidator();

        this.userService = new UserService(userRepository, userValidator, passwordHasher);
        this.authService = new AuthenticationService(userRepository, passwordHasher, userValidator);
        this.notificationService = new NotificationService(notificationRepository);
        this.priorityService = new PriorityService();
        this.slaService = new SLAService();
        this.ticketService = new TicketService(ticketRepository, userRepository, ticketValidator, notificationService, priorityService);
        this.assignmentService = new AssignmentService(ticketRepository, userRepository, notificationService);
        this.feedbackService = new FeedbackService(feedbackRepository, ticketRepository);
        this.reportService = new ReportService(ticketRepository, slaService);
        this.testingCenterService = new TestingCenterService();

        // Seed data
        DemoDataLoader.loadDemoData(userService, ticketService, assignmentService, slaService);
    }

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        this.primaryStage.setTitle("FixFlow — Smart Maintenance & Incident Management System");
        this.primaryStage.setMinWidth(1000);
        this.primaryStage.setMinHeight(700);

        rootPane = new StackPane();
        mainScene = new Scene(rootPane, 1200, 800);

        // Attach stylesheet
        try {
            String css = Objects.requireNonNull(getClass().getResource("styles.css")).toExternalForm();
            mainScene.getStylesheets().add(css);
        } catch (Exception e) {
            System.err.println("Warning: Could not load styles.css directly: " + e.getMessage());
        }

        primaryStage.setScene(mainScene);
        showSplashScreen();
        primaryStage.show();
    }

    public void showSplashScreen() {
        SplashScreenView splash = new SplashScreenView(this::showAuthScreen);
        rootPane.getChildren().setAll(splash);
    }

    public void showAuthScreen() {
        AuthView authView = new AuthView(authService, userService, this::showMainAppShell);
        rootPane.getChildren().setAll(authView);
    }

    public void showMainAppShell(User authenticatedUser) {
        MainShellView shell = new MainShellView(
                userService, authService, ticketService, assignmentService,
                priorityService, slaService, notificationService, feedbackService,
                reportService, testingCenterService, authenticatedUser,
                this::showAuthScreen
        );
        rootPane.getChildren().setAll(shell);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
