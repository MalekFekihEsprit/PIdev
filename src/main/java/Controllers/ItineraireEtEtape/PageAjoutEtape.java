package Controllers.ItineraireEtEtape;

import Entities.etape;
import Services.etapeCRUD;
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

public class PageAjoutEtape {

    @FXML
    private TextArea descriptionField;

    @FXML
    private Spinner<Integer> heureSpinner;

    @FXML
    private Spinner<Integer> minuteSpinner;

    @FXML
    private ComboBox<etapeCRUD.ActiviteItem> activiteCombo;

    @FXML
    private Label errorLabel;

    @FXML
    private Label titreFormulaire;

    private etapeCRUD etapeCRUD = new etapeCRUD();

    private int idItineraire;
    private int numeroJour;
    private Runnable onEtapeAjoutee;

    private etape etapeAModifier;
    private boolean isModification = false;

    private List<etape> etapesExistantes;

    @FXML
    public void initialize() {
        System.out.println("=== Initialisation PageAjoutEtapeController ===");

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
    }

    public void setDialogPane(DialogPane dialogPane) {
        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            event.consume();
            handleAjouter();
        });
    }

    public void setJourInfo(int numeroJour, int idItineraire) {
        this.numeroJour = numeroJour;
        this.idItineraire = idItineraire;
        this.titreFormulaire.setText("Ajouter une étape - Jour " + numeroJour);

        chargerEtapesExistantes();
        chargerActivites();
    }

    public void setEtapeAModifier(etape etape) {
        this.etapeAModifier = etape;
        this.idItineraire = etape.getId_itineraire();
        this.numeroJour = etape.getNumeroJour(); // Récupérer le jour de l'étape à modifier
        this.isModification = true;
        this.titreFormulaire.setText("Modifier l'étape - Jour " + numeroJour);

        chargerEtapesExistantes();
        chargerActivites();
    }

    private void chargerEtapesExistantes() {
        try {
            etapesExistantes = etapeCRUD.getEtapesByItineraire(idItineraire);
        } catch (SQLException e) {
            e.printStackTrace();
            etapesExistantes = List.of();
        }
    }

    private void chargerActivites() {
        try {
            int idVoyage = etapeCRUD.getVoyageIdFromItineraire(idItineraire);

            if (idVoyage == -1) {
                showError("Impossible de trouver le voyage associé à cet itinéraire");
                return;
            }

            List<etapeCRUD.ActiviteItem> toutesActivites = etapeCRUD.getActivitesByVoyage(idVoyage);

            // Récupérer TOUTES les étapes de l'itinéraire (pas seulement le jour courant)
            List<etape> toutesEtapesItineraire = etapeCRUD.getEtapesByItineraire(idItineraire);

            // Extraire les IDs des activités déjà utilisées dans CET itinéraire (tous jours confondus)
            List<Integer> idsActivitesUtilisees = toutesEtapesItineraire.stream()
                    .map(etape -> etape.getId_activite())
                    .collect(Collectors.toList());

            List<etapeCRUD.ActiviteItem> activitesDisponibles;

            if (isModification && etapeAModifier != null) {
                // En modification, on garde l'activité actuelle même si elle est utilisée ailleurs
                activitesDisponibles = toutesActivites.stream()
                        .filter(a -> {
                            // Si c'est l'activité en cours de modification, on la garde
                            if (a.getId() == etapeAModifier.getId_activite()) {
                                return true;
                            }
                            // Sinon, on vérifie qu'elle n'est pas utilisée ailleurs
                            return !idsActivitesUtilisees.contains(a.getId());
                        })
                        .collect(Collectors.toList());
            } else {
                // En ajout, on filtre les activités déjà utilisées dans l'itinéraire
                activitesDisponibles = toutesActivites.stream()
                        .filter(a -> !idsActivitesUtilisees.contains(a.getId()))
                        .collect(Collectors.toList());
            }

            if (activitesDisponibles.isEmpty()) {
                if (isModification) {
                    showError("Toutes les activités sont déjà utilisées dans cet itinéraire");
                } else {
                    showError("Toutes les activités de ce voyage sont déjà planifiées dans cet itinéraire");
                }
                return;
            }

            ObservableList<etapeCRUD.ActiviteItem> observableActivites = FXCollections.observableArrayList(activitesDisponibles);
            activiteCombo.setItems(observableActivites);

            if (isModification && etapeAModifier != null) {
                preRemplirFormulaire();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur lors du chargement des activités: " + e.getMessage());
        }
    }

    private void preRemplirFormulaire() {
        if (etapeAModifier.getHeure() != null) {
            LocalTime time = etapeAModifier.getHeure().toLocalTime();
            heureSpinner.getValueFactory().setValue(time.getHour());
            minuteSpinner.getValueFactory().setValue(time.getMinute());
        }

        descriptionField.setText(etapeAModifier.getDescription_etape());

        for (etapeCRUD.ActiviteItem item : activiteCombo.getItems()) {
            if (item.getId() == etapeAModifier.getId_activite()) {
                activiteCombo.getSelectionModel().select(item);
                break;
            }
        }
    }

    public void setOnEtapeAjoutee(Runnable callback) {
        this.onEtapeAjoutee = callback;
    }

    private boolean verifierHeureDisponible(Time heure, Integer idActivite, Integer idEtapeExclue) {
        for (etape e : etapesExistantes) {
            if (idEtapeExclue != null && e.getId_etape() == idEtapeExclue) {
                continue;
            }
            // Vérifier l'heure uniquement pour le même jour
            if (e.getNumeroJour() == numeroJour && e.getHeure() != null && e.getHeure().equals(heure)) {
                showError("Une autre étape existe déjà à cette heure pour le jour " + numeroJour);
                return false;
            }
        }
        return true;
    }

    @FXML
    private void handleAjouter() {
        etapeCRUD.ActiviteItem selectedActivite = activiteCombo.getSelectionModel().getSelectedItem();
        if (selectedActivite == null) {
            showError("Veuillez sélectionner une activité");
            return;
        }

        int heure = heureSpinner.getValue();
        int minute = minuteSpinner.getValue();

        Time time = Time.valueOf(LocalTime.of(heure, minute));

        if (!verifierHeureDisponible(time, selectedActivite.getId(),
                isModification ? etapeAModifier.getId_etape() : null)) {
            return;
        }

        try {
            if (isModification && etapeAModifier != null) {
                etapeAModifier.setHeure(time);
                etapeAModifier.setDescription_etape(descriptionField.getText());
                etapeAModifier.setId_activite(selectedActivite.getId());
                etapeAModifier.setNumeroJour(numeroJour); // Mettre à jour le jour

                etapeCRUD.modifier(etapeAModifier);
                showSuccess("Étape modifiée avec succès !");
            } else {
                etape nouvelleEtape = new etape();
                nouvelleEtape.setHeure(time);
                nouvelleEtape.setDescription_etape(descriptionField.getText());
                nouvelleEtape.setId_itineraire(idItineraire);
                nouvelleEtape.setId_activite(selectedActivite.getId());
                nouvelleEtape.setNumeroJour(numeroJour); // AJOUT : stocker le numéro du jour

                etapeCRUD.ajouter(nouvelleEtape);
                showSuccess("Étape ajoutée avec succès !");
            }

            if (onEtapeAjoutee != null) {
                onEtapeAjoutee.run(); // Recharger les étapes dans PageEtape
            }

            // Fermer la fenêtre immédiatement
            Stage stage = (Stage) activiteCombo.getScene().getWindow();
            stage.close();

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
}