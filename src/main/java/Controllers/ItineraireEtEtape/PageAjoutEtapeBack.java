package Controllers.ItineraireEtEtape;

import Entities.etape;
import Services.etapeCRUD;
import Services.itineraireCRUD;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

public class PageAjoutEtapeBack {

    @FXML
    private TextArea descriptionField;

    @FXML
    private Spinner<Integer> heureSpinner;

    @FXML
    private Spinner<Integer> minuteSpinner;

    @FXML
    private ComboBox<etapeCRUD.ActiviteItem> activiteCombo;

    @FXML
    private ComboBox<ItineraireItem> itineraireCombo;

    @FXML
    private ComboBox<Integer> jourCombo;

    @FXML
    private Label errorLabel;

    @FXML
    private Label titreFormulaire;

    private etapeCRUD etapeCRUD = new etapeCRUD();
    private itineraireCRUD itineraireCRUD = new itineraireCRUD();

    private int idItineraire;
    private int numeroJour;
    private Runnable onEtapeAjoutee;

    private etape etapeAModifier;
    private boolean isModification = false;

    private List<etape> etapesExistantes;

    @FXML
    public void initialize() {
        System.out.println("=== Initialisation PageAjoutEtapeBack (Back Office) ===");

        // Configuration des spinners
        SpinnerValueFactory<Integer> heureFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 8);
        heureSpinner.setValueFactory(heureFactory);
        heureSpinner.setEditable(true);

        SpinnerValueFactory<Integer> minuteFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0);
        minuteSpinner.setValueFactory(minuteFactory);
        minuteSpinner.setEditable(true);

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

        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        // Charger les itinéraires
        chargerItineraires();

        // Écouter le changement d'itinéraire pour charger les jours
        itineraireCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                chargerJours(newVal.getId());
                chargerActivitesPourItineraire(newVal.getId());
            }
        });
    }

    private void chargerItineraires() {
        try {
            List<ItineraireItem> itineraires = itineraireCRUD.afficher().stream()
                    .map(i -> new ItineraireItem(i.getId_itineraire(), i.getNom_itineraire()))
                    .collect(Collectors.toList());

            itineraireCombo.setItems(FXCollections.observableArrayList(itineraires));
            System.out.println("Itinéraires chargés: " + itineraires.size());
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur lors du chargement des itinéraires");
        }
    }

    private void chargerJours(int idItineraire) {
        try {
            int nbJours = etapeCRUD.getNombreJoursItineraire(idItineraire);
            ObservableList<Integer> jours = FXCollections.observableArrayList();
            for (int i = 1; i <= nbJours; i++) {
                jours.add(i);
            }
            jourCombo.setItems(jours);
            if (!jours.isEmpty()) {
                jourCombo.getSelectionModel().selectFirst();
            }
            System.out.println("Jours chargés pour l'itinéraire " + idItineraire + ": " + nbJours + " jours");
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur lors du chargement des jours");
        }
    }

    private void chargerActivitesPourItineraire(int idItineraire) {
        try {
            int idVoyage = etapeCRUD.getVoyageIdFromItineraire(idItineraire);
            if (idVoyage == -1) {
                showError("Impossible de trouver le voyage associé");
                return;
            }

            List<etapeCRUD.ActiviteItem> toutesActivites = etapeCRUD.getActivitesByVoyage(idVoyage);
            activiteCombo.setItems(FXCollections.observableArrayList(toutesActivites));

            if (!toutesActivites.isEmpty()) {
                activiteCombo.getSelectionModel().selectFirst();
            }

            System.out.println("Activités chargées pour le voyage " + idVoyage + ": " + toutesActivites.size());

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur lors du chargement des activités");
        }
    }

    public void setDialogPane(DialogPane dialogPane) {
        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            event.consume();
            handleAjouter();
        });
    }

    public void setOnEtapeAjoutee(Runnable callback) {
        this.onEtapeAjoutee = callback;
    }

    @FXML
    private void handleAjouter() {
        System.out.println("=== Traitement de l'ajout étape (Back Office) ===");

        // Validation itinéraire
        ItineraireItem selectedItineraire = itineraireCombo.getSelectionModel().getSelectedItem();
        if (selectedItineraire == null) {
            showError("Veuillez sélectionner un itinéraire");
            return;
        }

        // Validation jour
        Integer selectedJour = jourCombo.getSelectionModel().getSelectedItem();
        if (selectedJour == null) {
            showError("Veuillez sélectionner un jour");
            return;
        }

        // Validation activité
        etapeCRUD.ActiviteItem selectedActivite = activiteCombo.getSelectionModel().getSelectedItem();
        if (selectedActivite == null) {
            showError("Veuillez sélectionner une activité");
            return;
        }

        int heure = heureSpinner.getValue();
        int minute = minuteSpinner.getValue();
        Time time = Time.valueOf(LocalTime.of(heure, minute));

        try {
            // AJOUT
            etape nouvelleEtape = new etape();
            nouvelleEtape.setHeure(time);
            nouvelleEtape.setDescription_etape(descriptionField.getText());
            nouvelleEtape.setId_itineraire(selectedItineraire.getId());
            nouvelleEtape.setId_activite(selectedActivite.getId());

            etapeCRUD.ajouter(nouvelleEtape);
            System.out.println("Ajout réussi - ID généré: " + nouvelleEtape.getId_etape());
            showSuccess("Étape ajoutée avec succès !");

            if (onEtapeAjoutee != null) {
                onEtapeAjoutee.run();
            }

            // Fermer la fenêtre après 1.5 secondes
            new Thread(() -> {
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {}
                javafx.application.Platform.runLater(() -> {
                    Stage stage = (Stage) activiteCombo.getScene().getWindow();
                    stage.close();
                });
            }).start();

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur: " + e.getMessage());
        }
    }

    @FXML
    private void handleAnnuler() {
        Stage stage = (Stage) activiteCombo.getScene().getWindow();
        stage.close();
    }

    private void showError(String message) {
        errorLabel.setText("❌ " + message);
        errorLabel.setStyle("-fx-text-fill: #ef4444;");
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void showSuccess(String message) {
        errorLabel.setText("✅ " + message);
        errorLabel.setStyle("-fx-text-fill: #10b981;");
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    // Classe interne pour les itinéraires
    public static class ItineraireItem {
        private int id;
        private String nom;

        public ItineraireItem(int id, String nom) {
            this.id = id;
            this.nom = nom;
        }

        public int getId() { return id; }
        public String getNom() { return nom; }

        @Override
        public String toString() {
            return nom;
        }
    }
}