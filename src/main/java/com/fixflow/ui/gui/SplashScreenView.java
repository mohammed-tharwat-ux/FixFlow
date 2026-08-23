package com.fixflow.ui.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Official DEPI Splash & Welcome Introduction Screen for FixFlow Desktop Application.
 */
public class SplashScreenView extends VBox {

    public SplashScreenView(Runnable onGetStarted) {
        setAlignment(Pos.CENTER);
        setSpacing(20);
        setPadding(new Insets(40, 60, 40, 60));
        setStyle("-fx-background-color: #0f172a;");

        // DEPI Header Card
        VBox depiHeader = new VBox(4);
        depiHeader.setAlignment(Pos.CENTER);

        Label depiTag = new Label("DIGITAL EGYPT PIONEERS INITIATIVE (DEPI)");
        depiTag.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold; -fx-font-size: 15px; -fx-letter-spacing: 1px;");

        Label mcitLabel = new Label("Ministry of Communications & Information Technology (MCIT)");
        mcitLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");

        depiHeader.getChildren().addAll(depiTag, mcitLabel);

        // FixFlow Main Brand Container
        VBox brandBox = new VBox(6);
        brandBox.setAlignment(Pos.CENTER);
        brandBox.setStyle("-fx-background-color: #1e293b; -fx-background-radius: 12px; -fx-padding: 24px 36px; -fx-border-color: #334155; -fx-border-radius: 12px; -fx-border-width: 1px;");
        brandBox.setMaxWidth(780);

        Label logoLabel = new Label("FIXFLOW");
        logoLabel.setStyle("-fx-text-fill: #38bdf8; -fx-font-size: 32px; -fx-font-weight: bold; -fx-letter-spacing: 2px;");

        Label subtitleLabel = new Label("Smart Maintenance & Incident Management System");
        subtitleLabel.setStyle("-fx-text-fill: #f8fafc; -fx-font-size: 16px; -fx-font-weight: 600;");

        Label descLabel = new Label(
                "\"FixFlow is a smart maintenance and incident management system designed to organize maintenance " +
                "requests from reporting to resolution. It helps users submit incidents, administrators assign technicians, " +
                "technicians manage repairs, and the system monitor priorities, SLA performance, notifications, feedback, and reports.\""
        );
        descLabel.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 13px; -fx-text-alignment: center;");
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(700);

        brandBox.getChildren().addAll(logoLabel, subtitleLabel, new Separator(), descLabel);

        // Project Team & Supervision Card
        HBox creditsBox = new HBox(30);
        creditsBox.setAlignment(Pos.CENTER);
        creditsBox.setMaxWidth(780);

        VBox supervBox = new VBox(6);
        supervBox.setStyle("-fx-background-color: #1e293b; -fx-background-radius: 8px; -fx-padding: 16px; -fx-pref-width: 360px; -fx-border-color: #334155; -fx-border-radius: 8px;");
        Label supTitle = new Label("UNDER THE SUPERVISION OF");
        supTitle.setStyle("-fx-text-fill: #38bdf8; -fx-font-size: 11px; -fx-font-weight: bold;");
        Label supName1 = new Label("Team Dr/ Hacker");
        supName1.setStyle("-fx-text-fill: #f8fafc; -fx-font-weight: 600;");
        Label supName2 = new Label("Dr / Mina S. Younan");
        supName2.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold; -fx-font-size: 14px;");
        supervBox.getChildren().addAll(supTitle, supName1, supName2);

        VBox teamBox = new VBox(6);
        teamBox.setStyle("-fx-background-color: #1e293b; -fx-background-radius: 8px; -fx-padding: 16px; -fx-pref-width: 360px; -fx-border-color: #334155; -fx-border-radius: 8px;");
        Label teamTitle = new Label("PRESENTED BY: TEAM DR/ HACKER");
        teamTitle.setStyle("-fx-text-fill: #38bdf8; -fx-font-size: 11px; -fx-font-weight: bold;");
        Label teamMembers = new Label("Mohamed Tharwat  |  Mariam Samy  |  Shahd Moaz  |  Salah Reda");
        teamMembers.setStyle("-fx-text-fill: #e2e8f0; -fx-font-size: 12px;");
        teamBox.getChildren().addAll(teamTitle, teamMembers);

        creditsBox.getChildren().addAll(supervBox, teamBox);

        // Testing Focus Note
        Label testingNote = new Label(
                "Software Testing is a core foundation of FixFlow: Featuring Unit, Integration, Regression, " +
                "Validation, Security, Negative Scenarios, BVA, and Equivalence Partitioning (199 Tests, 100% Pass Rate)."
        );
        testingNote.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px; -fx-font-style: italic; -fx-text-alignment: center;");
        testingNote.setWrapText(true);
        testingNote.setMaxWidth(720);

        // Action Button
        Button getStartedBtn = new Button("GET STARTED ->");
        getStartedBtn.setStyle(
                "-fx-background-color: #0284c7; -fx-text-fill: #ffffff; -fx-font-weight: bold; " +
                "-fx-font-size: 14px; -fx-padding: 12px 36px; -fx-background-radius: 8px; -fx-cursor: hand;"
        );
        getStartedBtn.setOnAction(e -> onGetStarted.run());

        getChildren().addAll(depiHeader, brandBox, creditsBox, testingNote, getStartedBtn);
    }
}
