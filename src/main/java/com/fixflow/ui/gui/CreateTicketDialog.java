package com.fixflow.ui.gui;

import com.fixflow.exception.ValidationException;
import com.fixflow.model.Category;
import com.fixflow.model.Priority;
import com.fixflow.model.Ticket;
import com.fixflow.model.User;
import com.fixflow.service.TicketService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

/**
 * Validated Ticket Creation View/Form.
 */
public class CreateTicketDialog extends ScrollPane {

    private final TicketService ticketService;
    private final User currentUser;
    private final Consumer<Ticket> onTicketCreated;
    private final Runnable onCancel;

    public CreateTicketDialog(TicketService ticketService,
                              User currentUser,
                              Consumer<Ticket> onTicketCreated,
                              Runnable onCancel) {
        this.ticketService = ticketService;
        this.currentUser = currentUser;
        this.onTicketCreated = onTicketCreated;
        this.onCancel = onCancel;

        setFitToWidth(true);
        VBox root = new VBox(20);
        root.setPadding(new Insets(24, 32, 32, 32));
        root.setAlignment(Pos.CENTER);

        VBox formCard = new VBox(14);
        formCard.getStyleClass().add("card");
        formCard.setMaxWidth(620);

        Label titleLbl = new Label("Report New Maintenance Incident");
        titleLbl.getStyleClass().add("text-header-1");

        Label subLbl = new Label("Please provide clear details regarding the maintenance problem.");
        subLbl.getStyleClass().add("text-muted");

        Label errorLabel = new Label();
        errorLabel.getStyleClass().add("error-label");
        errorLabel.setWrapText(true);
        errorLabel.setVisible(false);

        Label successLabel = new Label();
        successLabel.getStyleClass().add("success-label");
        successLabel.setVisible(false);

        // Fields
        Label tLbl = new Label("Incident Title *");
        tLbl.getStyleClass().add("form-label");
        TextField titleField = new TextField();
        titleField.setPromptText("Brief headline (3 - 100 characters)");

        Label catLbl = new Label("Incident Category *");
        catLbl.getStyleClass().add("form-label");
        ComboBox<Category> categoryCombo = new ComboBox<>();
        categoryCombo.getItems().addAll(Category.values());
        categoryCombo.setValue(Category.FACILITY);

        Label locLbl = new Label("Specific Location *");
        locLbl.getStyleClass().add("form-label");
        TextField locationField = new TextField();
        locationField.setPromptText("e.g. Building B, Room 302 or Server Room 2");

        Label priLbl = new Label("Priority Level (Optional - Auto-evaluated if left default)");
        priLbl.getStyleClass().add("form-label");
        ComboBox<Priority> priorityCombo = new ComboBox<>();
        priorityCombo.getItems().addAll(Priority.values());
        priorityCombo.setValue(Priority.LOW);

        Label descLbl = new Label("Detailed Description *");
        descLbl.getStyleClass().add("form-label");
        TextArea descArea = new TextArea();
        descArea.setPromptText("Describe the symptom, safety hazard, or problem details (5 - 1000 characters)...");
        descArea.setPrefRowCount(4);

        HBox actionBox = new HBox(12);
        actionBox.setAlignment(Pos.CENTER_RIGHT);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("button-secondary");
        cancelBtn.setOnAction(e -> onCancel.run());

        Button submitBtn = new Button("Submit Ticket");
        submitBtn.getStyleClass().add("button-primary");
        submitBtn.setOnAction(e -> {
            errorLabel.setVisible(false);
            successLabel.setVisible(false);
            try {
                Ticket created = ticketService.createTicket(
                        currentUser.getId(),
                        titleField.getText(),
                        descArea.getText(),
                        categoryCombo.getValue(),
                        locationField.getText(),
                        priorityCombo.getValue()
                );
                successLabel.setText("Ticket #" + created.getId() + " created successfully!");
                successLabel.setVisible(true);
                onTicketCreated.accept(created);
            } catch (ValidationException ex) {
                errorLabel.setText(ex.getMessage());
                errorLabel.setVisible(true);
            } catch (Exception ex) {
                errorLabel.setText("Error creating ticket: " + ex.getMessage());
                errorLabel.setVisible(true);
            }
        });

        actionBox.getChildren().addAll(cancelBtn, submitBtn);

        formCard.getChildren().addAll(
                titleLbl, subLbl, errorLabel, successLabel,
                tLbl, titleField,
                catLbl, categoryCombo,
                locLbl, locationField,
                priLbl, priorityCombo,
                descLbl, descArea,
                actionBox
        );

        root.getChildren().add(formCard);
        setContent(root);
    }
}
