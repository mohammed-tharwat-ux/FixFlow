package com.fixflow.ui.gui;

import com.fixflow.model.Notification;
import com.fixflow.model.User;
import com.fixflow.service.NotificationService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * In-System Notifications Center View for JavaFX.
 */
public class NotificationsViewFx extends ScrollPane {

    private final NotificationService notificationService;
    private final User currentUser;
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public NotificationsViewFx(NotificationService notificationService, User currentUser) {
        this.notificationService = notificationService;
        this.currentUser = currentUser;

        setFitToWidth(true);
        VBox root = new VBox(20);
        root.setPadding(new Insets(24, 32, 32, 32));

        HBox topBar = new HBox();
        topBar.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(2);
        Label titleLbl = new Label("Notification Center");
        titleLbl.getStyleClass().add("text-header-1");

        Label subLbl = new Label("Real-time event dispatches, technician assignments, ticket updates, and SLA alerts");
        subLbl.getStyleClass().add("text-muted");
        titleBox.getChildren().addAll(titleLbl, subLbl);

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        Button markAllBtn = new Button("Mark All as Read");
        markAllBtn.getStyleClass().add("button-secondary");

        topBar.getChildren().addAll(titleBox, spacer, markAllBtn);
        root.getChildren().add(topBar);

        List<Notification> list = notificationService.getUserNotifications(currentUser.getId());

        VBox listCard = new VBox(12);
        listCard.getStyleClass().add("card");

        if (list.isEmpty()) {
            Label emptyLbl = new Label("You have no notifications at this time.");
            emptyLbl.getStyleClass().add("text-muted");
            listCard.getChildren().add(emptyLbl);
        } else {
            for (Notification n : list) {
                HBox item = new HBox(14);
                item.setAlignment(Pos.CENTER_LEFT);
                item.setPadding(new Insets(10, 14, 10, 14));
                item.setStyle(n.isRead()
                        ? "-fx-background-color: #f8fafc; -fx-background-radius: 6px; -fx-border-color: #e2e8f0; -fx-border-radius: 6px;"
                        : "-fx-background-color: #f0f9ff; -fx-background-radius: 6px; -fx-border-color: #bae6fd; -fx-border-radius: 6px;"
                );

                Label typeBadge = new Label(n.getType().name());
                typeBadge.setStyle("-fx-background-color: #0284c7; -fx-text-fill: white; -fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 3px 6px; -fx-background-radius: 4px;");

                VBox content = new VBox(2);
                Label tLbl = new Label(n.getTitle());
                tLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #0f172a;");
                Label mLbl = new Label(n.getMessage());
                mLbl.setStyle("-fx-text-fill: #334155;");
                Label timeLbl = new Label(n.getCreatedAt() != null ? n.getCreatedAt().format(dtf) : "");
                timeLbl.setStyle("-fx-font-size: 10px; -fx-text-fill: #94a3b8;");
                content.getChildren().addAll(tLbl, mLbl, timeLbl);

                HBox.setHgrow(content, javafx.scene.layout.Priority.ALWAYS);
                item.getChildren().addAll(typeBadge, content);
                listCard.getChildren().add(item);

                notificationService.markAsRead(n.getId());
            }
        }

        markAllBtn.setOnAction(e -> {
            list.forEach(n -> notificationService.markAsRead(n.getId()));
            markAllBtn.setText("✓ All Marked as Read");
            markAllBtn.setDisable(true);
        });

        root.getChildren().add(listCard);
        setContent(root);
    }
}
