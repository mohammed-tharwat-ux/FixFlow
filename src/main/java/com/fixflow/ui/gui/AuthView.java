package com.fixflow.ui.gui;

import com.fixflow.data.DemoDataLoader;
import com.fixflow.exception.AuthenticationException;
import com.fixflow.exception.UserAlreadyExistsException;
import com.fixflow.exception.ValidationException;
import com.fixflow.model.Role;
import com.fixflow.model.User;
import com.fixflow.service.AuthenticationService;
import com.fixflow.service.UserService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

/**
 * Graphical Authentication and User Registration view for FixFlow Desktop Application.
 */
public class AuthView extends StackPane {

    private final AuthenticationService authService;
    private final UserService userService;
    private final Consumer<User> onLoginSuccess;

    private final VBox loginCard;
    private final VBox registerCard;

    public AuthView(AuthenticationService authService,
                    UserService userService,
                    Consumer<User> onLoginSuccess) {
        this.authService = authService;
        this.userService = userService;
        this.onLoginSuccess = onLoginSuccess;

        setAlignment(Pos.CENTER);
        setPadding(new Insets(30));
        setStyle("-fx-background-color: #0f172a;");

        this.loginCard = buildLoginCard();
        this.registerCard = buildRegisterCard();

        getChildren().add(loginCard);
    }

    private VBox buildLoginCard() {
        VBox card = new VBox(12);
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 12px; -fx-padding: 28px 32px; -fx-border-color: #e2e8f0; -fx-border-radius: 12px;");
        card.setMaxWidth(420);

        ImageView logoView = FixFlowThemeFx.createBrandLogoView(200, 70);
        if (logoView != null) {
            card.getChildren().add(logoView);
        } else {
            Label brandTitle = new Label("FixFlow");
            brandTitle.getStyleClass().add("text-header-1");
            brandTitle.setStyle("-fx-text-fill: #0284c7; -fx-font-weight: bold;");
            card.getChildren().add(brandTitle);
        }

        Label subTitle = new Label("Sign in to your maintenance portal account");
        subTitle.getStyleClass().add("text-muted");
        card.getChildren().add(subTitle);

        Label errorLabel = new Label();
        errorLabel.getStyleClass().add("error-label");
        errorLabel.setWrapText(true);
        errorLabel.setVisible(false);

        VBox formBox = new VBox(10);
        formBox.setAlignment(Pos.CENTER_LEFT);

        Label userLbl = new Label("Username");
        userLbl.getStyleClass().add("form-label");
        TextField userField = new TextField();
        userField.setPromptText("e.g. admin, tech_bob, john_user");

        Label passLbl = new Label("Password");
        passLbl.getStyleClass().add("form-label");
        PasswordField passField = new PasswordField();
        passField.setPromptText("Enter password");

        Button loginBtn = new Button("SIGN IN");
        loginBtn.getStyleClass().add("button-primary");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setOnAction(e -> {
            errorLabel.setVisible(false);
            try {
                User user = authService.login(userField.getText(), passField.getText());
                onLoginSuccess.accept(user);
            } catch (AuthenticationException ex) {
                errorLabel.setText(ex.getMessage());
                errorLabel.setVisible(true);
            } catch (Exception ex) {
                errorLabel.setText("Login error: " + ex.getMessage());
                errorLabel.setVisible(true);
            }
        });

        formBox.getChildren().addAll(userLbl, userField, passLbl, passField, loginBtn);

        // Quick Demo Fill Shortcuts
        VBox demoBox = new VBox(6);
        demoBox.setAlignment(Pos.CENTER_LEFT);
        Label quickLbl = new Label("Quick Demo Credentials:");
        quickLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b; -fx-font-weight: bold;");

        HBox demoButtons = new HBox(8);
        demoButtons.setAlignment(Pos.CENTER_LEFT);
        Button adminBtn = new Button("Admin");
        adminBtn.setStyle("-fx-font-size: 11px; -fx-padding: 4px 8px; -fx-background-color: #e0f2fe; -fx-text-fill: #0369a1; -fx-font-weight: bold; -fx-cursor: hand;");
        adminBtn.setOnAction(e -> {
            userField.setText("admin");
            passField.setText(DemoDataLoader.DEFAULT_PASSWORD);
        });

        Button techBtn = new Button("Tech (Bob)");
        techBtn.setStyle("-fx-font-size: 11px; -fx-padding: 4px 8px; -fx-background-color: #fef3c7; -fx-text-fill: #b45309; -fx-font-weight: bold; -fx-cursor: hand;");
        techBtn.setOnAction(e -> {
            userField.setText("tech_bob");
            passField.setText(DemoDataLoader.DEFAULT_PASSWORD);
        });

        Button userBtn = new Button("User (John)");
        userBtn.setStyle("-fx-font-size: 11px; -fx-padding: 4px 8px; -fx-background-color: #d1fae5; -fx-text-fill: #047857; -fx-font-weight: bold; -fx-cursor: hand;");
        userBtn.setOnAction(e -> {
            userField.setText("john_user");
            passField.setText(DemoDataLoader.DEFAULT_PASSWORD);
        });

        demoButtons.getChildren().addAll(adminBtn, techBtn, userBtn);
        demoBox.getChildren().addAll(quickLbl, demoButtons);

        HBox linkBox = new HBox();
        linkBox.setAlignment(Pos.CENTER);
        Hyperlink regLink = new Hyperlink("Don't have an account? Register new user");
        regLink.setOnAction(e -> {
            getChildren().clear();
            getChildren().add(registerCard);
        });
        linkBox.getChildren().add(regLink);

        card.getChildren().addAll(
                errorLabel,
                formBox,
                new Separator(),
                demoBox,
                linkBox
        );
        return card;
    }

    private VBox buildRegisterCard() {
        VBox card = new VBox(11);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 12px; -fx-padding: 26px 30px; -fx-border-color: #e2e8f0; -fx-border-radius: 12px;");
        card.setMaxWidth(440);

        Label brandTitle = new Label("Create Account");
        brandTitle.getStyleClass().add("text-header-2");
        brandTitle.setStyle("-fx-text-fill: #0284c7; -fx-font-weight: bold;");

        Label subTitle = new Label("Join FixFlow Maintenance Management System");
        subTitle.getStyleClass().add("text-muted");

        Label errorLabel = new Label();
        errorLabel.getStyleClass().add("error-label");
        errorLabel.setWrapText(true);
        errorLabel.setVisible(false);

        Label nameLbl = new Label("Full Name");
        nameLbl.getStyleClass().add("form-label");
        TextField nameField = new TextField();
        nameField.setPromptText("2 - 50 characters");

        Label userLbl = new Label("Username");
        userLbl.getStyleClass().add("form-label");
        TextField userField = new TextField();
        userField.setPromptText("3 - 20 characters (alphanumeric)");

        Label emailLbl = new Label("Email Address");
        emailLbl.getStyleClass().add("form-label");
        TextField emailField = new TextField();
        emailField.setPromptText("valid.email@example.com");

        Label passLbl = new Label("Password");
        passLbl.getStyleClass().add("form-label");
        PasswordField passField = new PasswordField();
        passField.setPromptText("8 - 64 characters with upper, lower, digit, special");

        Label confirmLbl = new Label("Confirm Password");
        confirmLbl.getStyleClass().add("form-label");
        PasswordField confirmField = new PasswordField();
        confirmField.setPromptText("Re-type your password");

        Button regBtn = new Button("CREATE ACCOUNT");
        regBtn.getStyleClass().add("button-primary");
        regBtn.setMaxWidth(Double.MAX_VALUE);
        regBtn.setOnAction(e -> {
            errorLabel.setVisible(false);
            if (!passField.getText().equals(confirmField.getText())) {
                errorLabel.setText("Passwords do not match.");
                errorLabel.setVisible(true);
                return;
            }
            try {
                User registered = userService.registerUser(
                        nameField.getText(),
                        userField.getText(),
                        emailField.getText(),
                        passField.getText(),
                        Role.USER
                );
                onLoginSuccess.accept(registered);
            } catch (ValidationException | UserAlreadyExistsException ex) {
                errorLabel.setText(ex.getMessage());
                errorLabel.setVisible(true);
            } catch (Exception ex) {
                errorLabel.setText("Registration error: " + ex.getMessage());
                errorLabel.setVisible(true);
            }
        });

        HBox linkBox = new HBox();
        linkBox.setAlignment(Pos.CENTER);
        Hyperlink loginLink = new Hyperlink("Already have an account? Sign in");
        loginLink.setOnAction(e -> {
            getChildren().clear();
            getChildren().add(loginCard);
        });
        linkBox.getChildren().add(loginLink);

        card.getChildren().addAll(
                brandTitle, subTitle, errorLabel,
                nameLbl, nameField,
                userLbl, userField,
                emailLbl, emailField,
                passLbl, passField,
                confirmLbl, confirmField,
                regBtn,
                linkBox
        );
        return card;
    }
}
