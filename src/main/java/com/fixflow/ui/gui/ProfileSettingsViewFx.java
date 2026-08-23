package com.fixflow.ui.gui;

import com.fixflow.model.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;

/**
 * User Profile & System Environment Settings View.
 */
public class ProfileSettingsViewFx extends ScrollPane {

    private final User currentUser;
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ProfileSettingsViewFx(User currentUser) {
        this.currentUser = currentUser;

        setFitToWidth(true);
        VBox root = new VBox(20);
        root.setPadding(new Insets(24, 32, 32, 32));

        Label titleLbl = new Label("Account Profile & System Settings");
        titleLbl.getStyleClass().add("text-header-1");

        Label subLbl = new Label("Inspect your authenticated session parameters and environment configurations");
        subLbl.getStyleClass().add("text-muted");

        root.getChildren().addAll(titleLbl, subLbl);

        // Account Details Card
        VBox profCard = new VBox(14);
        profCard.getStyleClass().add("card");
        Label pTitle = new Label("User Profile Information");
        pTitle.getStyleClass().add("text-header-2");

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(12);

        grid.add(new Label("User ID:"), 0, 0);
        grid.add(createBoldLabel("#" + currentUser.getId()), 1, 0);

        grid.add(new Label("Full Name:"), 0, 1);
        grid.add(createBoldLabel(currentUser.getFullName()), 1, 1);

        grid.add(new Label("Username:"), 0, 2);
        grid.add(createBoldLabel(currentUser.getUsername()), 1, 2);

        grid.add(new Label("Email Address:"), 0, 3);
        grid.add(createBoldLabel(currentUser.getEmail()), 1, 3);

        grid.add(new Label("Assigned Role:"), 0, 4);
        grid.add(createBoldLabel(currentUser.getRole().name()), 1, 4);

        grid.add(new Label("Account Status:"), 0, 5);
        Label statLabel = new Label("ACTIVE");
        statLabel.setStyle("-fx-background-color: #d1fae5; -fx-text-fill: #065f46; -fx-font-weight: bold; -fx-padding: 3px 8px; -fx-background-radius: 8px;");
        grid.add(statLabel, 1, 5);

        grid.add(new Label("Member Since:"), 0, 6);
        grid.add(new Label(currentUser.getCreatedAt() != null ? currentUser.getCreatedAt().format(dtf) : "N/A"), 1, 6);

        grid.add(new Label("Security Credentials:"), 0, 7);
        Label secLabel = new Label("PBKDF2-HMAC-SHA256 (Protected Salted Hash)");
        secLabel.setStyle("-fx-text-fill: #0284c7; -fx-font-weight: bold;");
        grid.add(secLabel, 1, 7);

        profCard.getChildren().addAll(pTitle, grid);
        root.getChildren().add(profCard);

        // System Settings Card
        VBox sysCard = new VBox(14);
        sysCard.getStyleClass().add("card");
        Label sTitle = new Label("FixFlow Desktop Application Environment");
        sTitle.getStyleClass().add("text-header-2");

        GridPane sysGrid = new GridPane();
        sysGrid.setHgap(20);
        sysGrid.setVgap(10);

        sysGrid.add(new Label("Runtime Platform:"), 0, 0);
        sysGrid.add(createBoldLabel("Java 21 LTS (Temurin / Adoptium Hotspot)"), 1, 0);

        sysGrid.add(new Label("GUI Framework:"), 0, 1);
        sysGrid.add(createBoldLabel("OpenJFX 21 (Desktop Native UI)"), 1, 1);

        sysGrid.add(new Label("Build Management:"), 0, 2);
        sysGrid.add(createBoldLabel("Apache Maven 3.9+"), 1, 2);

        sysGrid.add(new Label("Testing Framework:"), 0, 3);
        sysGrid.add(createBoldLabel("JUnit Jupiter 5.10.2 (199 Automated Tests)"), 1, 3);

        sysGrid.add(new Label("Repository Layer:"), 0, 4);
        sysGrid.add(createBoldLabel("In-Memory Thread-Safe Repositories with Defensive Copying"), 1, 4);

        sysGrid.add(new Label("Incident SLA Engine:"), 0, 5);
        sysGrid.add(createBoldLabel("Active Multi-Tier Surveillance (2h / 8h / 24h / 72h)"), 1, 5);

        sysCard.getChildren().addAll(sTitle, sysGrid);
        root.getChildren().add(sysCard);

        setContent(root);
    }

    private Label createBoldLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-weight: bold; -fx-text-fill: #0f172a;");
        return l;
    }
}
