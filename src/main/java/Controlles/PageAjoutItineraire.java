package Controlles;

import Services.itineraireCRUD;
import Entites.Itineraire;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import java.sql.SQLException;
import java.time.LocalDate;

public class PageAjoutItineraire {
    @FXML private TextField nomField;
    @FXML private TextArea descriptionField;
    @FXML private ComboBox<String> destinationCombo;
    @FXML private Spinner<Integer> nbJoursSpinner;
    @FXML private DatePicker dateDebutPicker;
    @FXML private ComboBox<String> emojiCombo;

    private itineraireCRUD itineraireCRUD;

    @FXML
    public void initialize() {
        itineraireCRUD = new itineraireCRUD();

        // Initialize spinner
        SpinnerValueFactory<Integer> valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 30, 1);
        nbJoursSpinner.setValueFactory(valueFactory);

        // Initialize emoji combo
        emojiCombo.getItems().addAll("🏖️", "🏔️", "🏛️", "🌆", "🏝️", "🏜️");

        // Load destinations from database
        loadDestinations();
    }

    private void loadDestinations() {
        // TODO: Load destinations from database
        destinationCombo.getItems().addAll("Paris", "Rome", "Barcelone", "Chamonix");
    }

    @FXML
    public void handleCreer(ActionEvent event) {
        try {
            Itineraire itineraire = new Itineraire();
            itineraire.setNom_itineraire(nomField.getText());
            itineraire.setDescription_itineraire(descriptionField.getText());
            itineraire.setId_voyage(1); // TODO: Get actual voyage ID
            itineraire.setNombre_jour(nbJoursSpinner.getValue());

            itineraireCRUD.ajouter(itineraire);

            // Close dialog
            ((DialogPane) nomField.getScene().getRoot()).getScene().getWindow().hide();

        } catch (SQLException e) {
            showAlert("Erreur", "Impossible d'ajouter l'itinéraire: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}