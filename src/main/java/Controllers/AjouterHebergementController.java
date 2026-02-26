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
import java.util.List;
import java.util.ResourceBundle;

public class AjouterHebergementController implements Initializable {

    @FXML private TextField tfNom;
    @FXML private ComboBox<String> cbType;
    @FXML private TextField tfPrix;
    @FXML private TextField tfAdresse;
    @FXML private TextField tfNote;
    @FXML private ComboBox<Destination> cbDestination;
    @FXML private Label lblNomCounter;
    @FXML private Label lblUniquenessWarning;
    @FXML private Button btnSave;
    @FXML private Button btnCancel;

    private HebergementCRUD hebergementCRUD;
    private DestinationCRUD destinationCRUD;
    private HebergementBackController parentController;
    private List<Hebergement> existingHebergements;

    private final String[] types = {"Hôtel", "Appartement", "Villa", "Auberge", "Camping", "Chalet", "Riad", "Maison d'hôte"};

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        hebergementCRUD = new HebergementCRUD();
        destinationCRUD = new DestinationCRUD();

        // Load existing hébergements for uniqueness check
        loadExistingHebergements();

        // Initialize type combo box
        cbType.getItems().addAll(types);

        // Load destinations
        loadDestinations();

        setupValidation();
        setupCounters();

        btnSave.setOnAction(event -> handleSave());
        btnCancel.setOnAction(event -> closeWindow());

        btnSave.setDisable(true);
        if (lblUniquenessWarning != null) {
            lblUniquenessWarning.setVisible(false);
        }
    }

    private void loadExistingHebergements() {
        try {
            existingHebergements = hebergementCRUD.afficher();
        } catch (SQLException e) {
            existingHebergements = List.of();
            System.err.println("Could not load existing hébergements: " + e.getMessage());
        }
    }

    public void setParentController(HebergementBackController controller) {
        this.parentController = controller;
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
        tfNom.textProperty().addListener((obs, old, newVal) -> {
            validateForm();
            checkUniqueness();
        });
        cbType.valueProperty().addListener((obs, old, newVal) -> {
            validateForm();
            checkUniqueness();
        });
        tfPrix.textProperty().addListener((obs, old, newVal) -> {
            validateForm();
            checkUniqueness();
        });
        tfAdresse.textProperty().addListener((obs, old, newVal) -> {
            validateForm();
            checkUniqueness();
        });
        tfNote.textProperty().addListener((obs, old, newVal) -> {
            validateForm();
            checkUniqueness();
        });
        cbDestination.valueProperty().addListener((obs, old, newVal) -> {
            validateForm();
            checkUniqueness();
        });

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

    private void checkUniqueness() {
        String nom = tfNom.getText().trim();
        Destination destination = cbDestination.getValue();

        if (nom.isEmpty() || destination == null || existingHebergements == null) {
            if (lblUniquenessWarning != null) {
                lblUniquenessWarning.setVisible(false);
            }
            return;
        }

        boolean exists = existingHebergements.stream()
                .anyMatch(h -> h.getNom_hebergement().equalsIgnoreCase(nom)
                        && h.getDestination() != null
                        && h.getDestination().getId_destination() == destination.getId_destination());

        if (exists) {
            lblUniquenessWarning.setText("⚠️ Un hébergement avec ce nom existe déjà dans cette destination!");
            lblUniquenessWarning.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11; -fx-font-weight: 600;");
            lblUniquenessWarning.setVisible(true);
        } else {
            lblUniquenessWarning.setVisible(false);
        }

        validateForm();
    }

    private void validateForm() {
        String nom = tfNom.getText().trim();
        Destination destination = cbDestination.getValue();

        boolean isDuplicate = existingHebergements != null &&
                !nom.isEmpty() && destination != null &&
                existingHebergements.stream()
                        .anyMatch(h -> h.getNom_hebergement().equalsIgnoreCase(nom)
                                && h.getDestination() != null
                                && h.getDestination().getId_destination() == destination.getId_destination());

        boolean isValid = !nom.isEmpty() && nom.length() <= 100 &&
                cbType.getValue() != null &&
                !tfPrix.getText().trim().isEmpty() &&
                !tfAdresse.getText().trim().isEmpty() &&
                !tfNote.getText().trim().isEmpty() &&
                destination != null &&
                !isDuplicate;

        btnSave.setDisable(!isValid);
    }

    private void handleSave() {
        if (!validateAllFields()) return;

        String nom = tfNom.getText().trim();
        Destination destination = cbDestination.getValue();

        // Double-check uniqueness before saving
        if (existingHebergements != null && destination != null) {
            boolean exists = existingHebergements.stream()
                    .anyMatch(h -> h.getNom_hebergement().equalsIgnoreCase(nom)
                            && h.getDestination() != null
                            && h.getDestination().getId_destination() == destination.getId_destination());

            if (exists) {
                showValidationAlert("Un hébergement avec ce nom existe déjà dans cette destination!");
                return;
            }
        }

        try {
            Hebergement newHebergement = new Hebergement(
                    nom,
                    cbType.getValue(),
                    Double.parseDouble(tfPrix.getText().trim()),
                    tfAdresse.getText().trim(),
                    Double.parseDouble(tfNote.getText().trim()),
                    0.0, // latitude par défaut
                    0.0, // longitude par défaut
                    destination
            );

            hebergementCRUD.ajouter(newHebergement);

            showSuccessAlert("Hébergement ajouté avec succès!");

            if (parentController != null) parentController.refreshAfterModification();
            closeWindow();

        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate") || e.getMessage().contains("duplicate")) {
                showValidationAlert("Cet hébergement existe déjà dans cette destination!");
            } else {
                showErrorAlert("Erreur lors de l'ajout", e.getMessage());
            }
        } catch (NumberFormatException e) {
            showValidationAlert("Veuillez vérifier les valeurs numériques");
        }
    }

    private boolean validateAllFields() {
        String nom = tfNom.getText().trim();

        if (nom.length() < 3) {
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

        if (cbDestination.getValue() == null) {
            showValidationAlert("Veuillez sélectionner une destination");
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