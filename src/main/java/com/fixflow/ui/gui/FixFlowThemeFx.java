package com.fixflow.ui.gui;

import com.fixflow.model.Priority;
import com.fixflow.model.SlaStatus;
import com.fixflow.model.TicketStatus;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.InputStream;

/**
 * JavaFX Theme, Badge Factory, and High-Resolution Brand Asset Builder for FixFlow.
 */
public final class FixFlowThemeFx {

    private static Image cachedLogo = null;

    private FixFlowThemeFx() {
    }

    public static Image getBrandLogoImage() {
        if (cachedLogo == null) {
            try (InputStream in = FixFlowThemeFx.class.getResourceAsStream("/com/fixflow/ui/gui/logo.png")) {
                if (in != null) {
                    cachedLogo = new Image(in);
                }
            } catch (Exception ignored) {
            }
        }
        return cachedLogo;
    }

    public static ImageView createBrandLogoView(double fitWidth, double fitHeight) {
        Image img = getBrandLogoImage();
        if (img != null && !img.isError()) {
            ImageView view = new ImageView(img);
            view.setPreserveRatio(true);
            view.setSmooth(true);
            if (fitWidth > 0) view.setFitWidth(fitWidth);
            if (fitHeight > 0) view.setFitHeight(fitHeight);
            return view;
        }
        return null;
    }

    public static Label createStatusBadge(TicketStatus status) {
        if (status == null) {
            Label label = new Label("UNKNOWN");
            label.getStyleClass().addAll("badge", "badge-closed");
            return label;
        }
        Label badge = new Label(status.name());
        badge.getStyleClass().add("badge");
        switch (status) {
            case OPEN -> badge.getStyleClass().add("badge-open");
            case ASSIGNED -> badge.getStyleClass().add("badge-medium");
            case IN_PROGRESS -> badge.getStyleClass().add("badge-in-progress");
            case RESOLVED -> badge.getStyleClass().add("badge-resolved");
            case CLOSED, CANCELLED -> badge.getStyleClass().add("badge-closed");
        }
        return badge;
    }

    public static Label createPriorityBadge(Priority priority) {
        if (priority == null) {
            Label label = new Label("NONE");
            label.getStyleClass().addAll("badge", "badge-low");
            return label;
        }
        Label badge = new Label(priority == Priority.CRITICAL ? "CRITICAL !" : priority.name());
        badge.getStyleClass().add("badge");
        switch (priority) {
            case CRITICAL -> badge.getStyleClass().add("badge-critical");
            case HIGH -> badge.getStyleClass().add("badge-high");
            case MEDIUM -> badge.getStyleClass().add("badge-medium");
            case LOW -> badge.getStyleClass().add("badge-low");
        }
        return badge;
    }

    public static Label createSlaBadge(SlaStatus status) {
        if (status == null) {
            Label label = new Label("UNKNOWN");
            label.getStyleClass().addAll("badge", "badge-closed");
            return label;
        }
        Label badge = new Label(status.name());
        badge.getStyleClass().add("badge");
        switch (status) {
            case MET -> badge.getStyleClass().add("badge-sla-met");
            case VIOLATED -> badge.getStyleClass().add("badge-sla-violated");
            case PENDING -> badge.getStyleClass().add("badge-sla-pending");
        }
        return badge;
    }

    public static VBox createStatCard(String labelText, String valueText, String icon) {
        VBox card = new VBox(6);
        card.getStyleClass().add("card-stat");
        card.setAlignment(Pos.CENTER_LEFT);

        Label iconAndLabel = new Label((icon != null ? icon + " " : "") + labelText);
        iconAndLabel.getStyleClass().add("stat-label");

        Label val = new Label(valueText);
        val.getStyleClass().add("stat-value");

        card.getChildren().addAll(iconAndLabel, val);
        return card;
    }
}
