package Controlles;

import Entites.etape;
import Services.etapeCRUD;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalTime;

public class PageAjoutEtape {

    @FXML
    private TextField lieuField;

    @FXML
    private TextArea descriptionField;

    @FXML
    private Spinner<Integer> heureSpinner;

    @FXML
    private Spinner<Integer> minuteSpinner;

    @FXML
    private ComboBox<String> activiteCombo;

    @FXML
    private Label errorLabel;

    private etapeCRUD etapeCRUD = new etapeCRUD();
    private int idItineraire;
    private int numeroJour;
    private Runnable onEtapeAjoutee;

    @FXML
    public void initialize() {
        // Configuration des spinners pour l'heure
        SpinnerValueFactory<Integer> heureFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 8);
        heureSpinner.setValueFactory(heureFactory);
        heureSpinner.setEditable(true);

        SpinnerValueFactory<Integer> minuteFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0);
        minuteSpinner.setValueFactory(minuteFactory);
        minuteSpinner.setEditable(true);

        // Formatage pour afficher 2 chiffres
        minuteSpinner.getValueFactory().setConverter(new javafx.util.StringConverter<Integer>() {
            @Override
            public String toString(Integer value) {
                return String.format("%02d", value);
            }
            @Override
            public Integer fromString(String string) {
                try {
                    return Integer.parseInt(string);
                } catch (NumberFormatException e) {
                    return 0;
                }
            }
        });

        // Charger les activités disponibles
        chargerActivites();

        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    private void chargerActivites() {
        // À implémenter selon votre table activité
        ObservableList<String> activites = FXCollections.observableArrayList(
                "Visite guidée",
                "Randonnée",
                "Dégustation",
                "Shopping",
                "Détente",
                "Culture",
                "Sport",
                "Gastronomie"
        );
        activiteCombo.setItems(activites);
    }

    public void setJourInfo(int numeroJour, int idItineraire) {
        this.numeroJour = numeroJour;
        this.idItineraire = idItineraire;
    }

    public void setOnEtapeAjoutee(Runnable callback) {
        this.onEtapeAjoutee = callback;
    }

    @FXML
    private void handleAjouter() {
        // Validation
        if (lieuField.getText() == null || lieuField.getText().trim().isEmpty()) {
            showError("Le lieu est requis");
            return;
        }

        if (activiteCombo.getValue() == null) {
            showError("Veuillez sélectionner une activité");
            return;
        }

        // Créer l'objet Time à partir des spinners
        int heure = heureSpinner.getValue();
        int minute = minuteSpinner.getValue();
        Time time = Time.valueOf(LocalTime.of(heure, minute));

        // Créer l'étape
        etape nouvelleEtape = new etape();
        nouvelleEtape.setHeure(time);
        nouvelleEtape.setLieu(lieuField.getText().trim());
        nouvelleEtape.setDescription_etape(descriptionField.getText() != null ? descriptionField.getText().trim() : "");
        nouvelleEtape.setId_itineraire(idItineraire);

        // TODO: Remplacer par l'ID réel de l'activité sélectionnée
        nouvelleEtape.setId_activite(1); // À modifier selon votre logique

        try {
            etapeCRUD.ajouter(nouvelleEtape);

            if (onEtapeAjoutee != null) {
                onEtapeAjoutee.run();
            }

            // Fermer la fenêtre
            Stage stage = (Stage) lieuField.getScene().getWindow();
            stage.close();

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur lors de l'ajout: " + e.getMessage());
        }
    }

    @FXML
    private void handleAnnuler() {
        Stage stage = (Stage) lieuField.getScene().getWindow();
        stage.close();
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }
}