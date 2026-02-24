package controllers;

import entities.Destination;
import services.DestinationCRUD;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class ModifierDestinationController implements Initializable {

    @FXML private Label lblDestinationName;
    @FXML private Label lblDestinationId;
    @FXML private Label lblLastModified;
    @FXML private TextField tfNom;
    @FXML private TextField tfPays;
    @FXML private TextArea taDescription;
    @FXML private ComboBox<String> cbClimat;
    @FXML private ComboBox<String> cbSaison;
    @FXML private Label lblNomCounter;
    @FXML private Label lblPaysCounter;
    @FXML private Label lblDescCounter;
    @FXML private Button btnUpdate;
    @FXML private Button btnCancel;

    private DestinationCRUD destinationCRUD;
    private DestinationBackController parentController;
    private Destination destinationToEdit;
    private final String[] climats = {"Méditerranéen", "Tropical", "Continental", "Désertique", "Montagnard", "Océanique", "Polaire"};
    private final String[] saisons = {"Printemps", "Été", "Automne", "Hiver", "Toute l'année"};

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        destinationCRUD = new DestinationCRUD();

        cbClimat.getItems().addAll(climats);
        cbSaison.getItems().addAll(saisons);

        setupValidation();
        setupCounters();

        btnUpdate.setOnAction(event -> handleUpdate());
        btnCancel.setOnAction(event -> closeWindow());

        btnUpdate.setDisable(true);
    }

    public void setParentController(DestinationBackController controller) {
        this.parentController = controller;
    }

    public void setDestination(Destination destination) {
        this.destinationToEdit = destination;

        // Display destination name in header
        lblDestinationName.setText(destination.getNom_destination());
        lblDestinationId.setText(String.valueOf(destination.getId_destination()));

        // Pre-fill form
        tfNom.setText(destination.getNom_destination());
        tfPays.setText(destination.getPays_destination());
        taDescription.setText(destination.getDescription_destination());
        cbClimat.setValue(destination.getClimat_destination());
        cbSaison.setValue(destination.getSaison_destination());

        // Set last modified time
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        lblLastModified.setText("Dernière modification: " + LocalDateTime.now().format(formatter));

        validateForm();
    }

    private void setupValidation() {
        tfNom.textProperty().addListener((obs, old, newVal) -> validateForm());
        tfPays.textProperty().addListener((obs, old, newVal) -> validateForm());
        cbClimat.valueProperty().addListener((obs, old, newVal) -> validateForm());
        cbSaison.valueProperty().addListener((obs, old, newVal) -> validateForm());
    }

    private void setupCounters() {
        tfNom.textProperty().addListener((obs, old, newVal) ->
                lblNomCounter.setText(newVal.length() + "/30"));
        tfPays.textProperty().addListener((obs, old, newVal) ->
                lblPaysCounter.setText(newVal.length() + "/30"));
        taDescription.textProperty().addListener((obs, old, newVal) ->
                lblDescCounter.setText(newVal.length() + "/1000"));
    }

    private void validateForm() {
        boolean isValid = tfNom.getText().trim().length() >= 3 &&
                tfNom.getText().trim().length() <= 30 &&
                tfPays.getText().trim().length() >= 2 &&
                tfPays.getText().trim().length() <= 30 &&
                cbClimat.getValue() != null &&
                cbSaison.getValue() != null &&
                taDescription.getText().length() <= 1000;

        btnUpdate.setDisable(!isValid);
    }

    private void handleUpdate() {
        if (!validateAllFields()) return;

        try {
            destinationToEdit.setNom_destination(tfNom.getText().trim());
            destinationToEdit.setPays_destination(tfPays.getText().trim());
            destinationToEdit.setDescription_destination(taDescription.getText().trim());
            destinationToEdit.setClimat_destination(cbClimat.getValue());
            destinationToEdit.setSaison_destination(cbSaison.getValue());

            destinationCRUD.modifier(destinationToEdit);

            showSuccessAlert("Destination modifiée avec succès!");

            if (parentController != null) parentController.refreshAfterModification();
            closeWindow();

        } catch (SQLException e) {
            showErrorAlert("Erreur lors de la modification", e.getMessage());
        }
    }

    private boolean validateAllFields() {
        if (tfNom.getText().trim().length() < 3) {
            showWarning("Le nom doit contenir au moins 3 caractères");
            return false;
        }
        if (tfPays.getText().trim().length() < 2) {
            showWarning("Le pays doit contenir au moins 2 caractères");
            return false;
        }
        if (taDescription.getText().length() > 1000) {
            showWarning("La description ne peut pas dépasser 1000 caractères");
            return false;
        }
        return true;
    }

    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Validation");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccessAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void closeWindow() {
        ((Stage) btnCancel.getScene().getWindow()).close();
    }
}