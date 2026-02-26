package Controllers;

import Entities.Destination;
import Entities.Hebergement;
import Services.DestinationCRUD;
import Services.HebergementCRUD;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class ModifierHebergementController implements Initializable {

    @FXML private Label lblBreadcrumb;
    @FXML private Label lblHebergementName;
    @FXML private Label lblHebergementId;
    @FXML private Label lblLastModified;
    @FXML private TextField tfNom;
    @FXML private ComboBox<String> cbType;
    @FXML private TextField tfPrix;
    @FXML private TextField tfAdresse;
    @FXML private TextField tfNote;
    @FXML private ComboBox<Destination> cbDestination;
    @FXML private Label lblNomCounter;
    @FXML private Button btnUpdate;
    @FXML private Button btnCancel;

    private HebergementCRUD hebergementCRUD;
    private DestinationCRUD destinationCRUD;
    private HebergementBackController parentController;
    private Hebergement hebergementToEdit;

    private final String[] types = {"Hôtel", "Appartement", "Villa", "Auberge", "Camping", "Chalet", "Riad", "Maison d'hôte"};

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        hebergementCRUD = new HebergementCRUD();
        destinationCRUD = new DestinationCRUD();

        // Initialize type combo box
        cbType.getItems().addAll(types);

        // Load destinations
        loadDestinations();

        setupValidation();
        setupCounters();

        btnUpdate.setOnAction(event -> handleUpdate());
        btnCancel.setOnAction(event -> closeWindow());

        btnUpdate.setDisable(true);
    }

    public void setParentController(HebergementBackController controller) {
        this.parentController = controller;
    }

    public void setHebergement(Hebergement hebergement) {
        this.hebergementToEdit = hebergement;

        // Display hebergement name in header
        lblHebergementName.setText(hebergement.getNom_hebergement());
        lblHebergementId.setText(String.valueOf(hebergement.getId_hebergement()));
        lblBreadcrumb.setText("Modification - " + hebergement.getNom_hebergement());

        // Pre-fill form
        tfNom.setText(hebergement.getNom_hebergement());
        cbType.setValue(hebergement.getType_hebergement());
        tfPrix.setText(String.valueOf(hebergement.getPrixNuit_hebergement()));
        tfAdresse.setText(hebergement.getAdresse_hebergement());
        tfNote.setText(String.valueOf(hebergement.getNote_hebergement()));
        cbDestination.setValue(hebergement.getDestination());

        // Set last modified time
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        lblLastModified.setText("Dernière modification: " + LocalDateTime.now().format(formatter));

        validateForm();
    }

    private void loadDestinations() {
        try {
            List<Destination> destinations = destinationCRUD.afficher();
            cbDestination.getItems().addAll(destinations);

            // Custom cell factory to show "nom, pays"
            cbDestination.setCellFactory(param -> new ListCell<Destination>() {
                @Override
                protected void updateItem(Destination item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getNom_destination() + ", " + item.getPays_destination());
                    }
                }
            });

            cbDestination.setButtonCell(new ListCell<Destination>() {
                @Override
                protected void updateItem(Destination item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getNom_destination() + ", " + item.getPays_destination());
                    }
                }
            });

        } catch (SQLException e) {
            if (parentController != null) {
                parentController.showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les destinations: " + e.getMessage());
            }
            e.printStackTrace();
        }
    }

    private void setupValidation() {
        tfNom.textProperty().addListener((obs, old, newVal) -> validateForm());
        cbType.valueProperty().addListener((obs, old, newVal) -> validateForm());
        tfPrix.textProperty().addListener((obs, old, newVal) -> validateForm());
        tfAdresse.textProperty().addListener((obs, old, newVal) -> validateForm());
        tfNote.textProperty().addListener((obs, old, newVal) -> validateForm());
        cbDestination.valueProperty().addListener((obs, old, newVal) -> validateForm());

        // Numeric validation for price and note
        tfPrix.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*\\.?\\d*")) {
                tfPrix.setText(oldValue);
            }
        });

        tfNote.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*\\.?\\d*")) {
                tfNote.setText(oldValue);
            }
        });
    }

    private void setupCounters() {
        tfNom.textProperty().addListener((obs, old, newVal) ->
                lblNomCounter.setText(newVal.length() + "/100"));
    }

    private void validateForm() {
        boolean isValid = !tfNom.getText().trim().isEmpty() &&
                tfNom.getText().trim().length() <= 100 &&
                cbType.getValue() != null &&
                !tfPrix.getText().trim().isEmpty() &&
                !tfAdresse.getText().trim().isEmpty() &&
                !tfNote.getText().trim().isEmpty() &&
                cbDestination.getValue() != null;

        btnUpdate.setDisable(!isValid);
    }

    private void handleUpdate() {
        if (!validateAllFields()) return;

        try {
            hebergementToEdit.setNom_hebergement(tfNom.getText().trim());
            hebergementToEdit.setType_hebergement(cbType.getValue());
            hebergementToEdit.setPrixNuit_hebergement(Double.parseDouble(tfPrix.getText().trim()));
            hebergementToEdit.setAdresse_hebergement(tfAdresse.getText().trim());
            hebergementToEdit.setNote_hebergement(Double.parseDouble(tfNote.getText().trim()));
            hebergementToEdit.setDestination(cbDestination.getValue());

            hebergementCRUD.modifier(hebergementToEdit);

            showSuccessAlert("Hébergement modifié avec succès!");

            if (parentController != null) parentController.refreshAfterModification();
            closeWindow();

        } catch (SQLException e) {
            showErrorAlert("Erreur lors de la modification", e.getMessage());
        } catch (NumberFormatException e) {
            showValidationAlert("Veuillez vérifier les valeurs numériques");
        }
    }

    private boolean validateAllFields() {
        if (tfNom.getText().trim().length() < 3) {
            showValidationAlert("Le nom doit contenir au moins 3 caractères");
            return false;
        }

        try {
            double prix = Double.parseDouble(tfPrix.getText().trim());
            if (prix <= 0) {
                showValidationAlert("Le prix doit être positif");
                return false;
            }
        } catch (NumberFormatException e) {
            showValidationAlert("Prix invalide");
            return false;
        }

        try {
            double note = Double.parseDouble(tfNote.getText().trim());
            if (note < 0 || note > 5) {
                showValidationAlert("La note doit être comprise entre 0 et 5");
                return false;
            }
        } catch (NumberFormatException e) {
            showValidationAlert("Note invalide");
            return false;
        }

        return true;
    }

    private void showValidationAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Erreur de validation");
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
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }
}