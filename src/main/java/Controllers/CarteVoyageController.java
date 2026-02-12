package Controllers;

import Entities.Voyage;
import Services.VoyageCRUD;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Optional;

public class CarteVoyageController {

    @FXML
    private Label iconLabel;
    @FXML
    private Label titreVoyage;
    @FXML
    private Label statutVoyage;
    @FXML
    private Label datesVoyage;
    @FXML
    private Label destinationVoyage;
    @FXML
    private Label participantsVoyage;
    @FXML
    private Label activitesVoyage;
    @FXML
    private Label btnModifier;
    @FXML
    private Label btnParticipants;
    @FXML
    private Label btnSupprimer;

    private Voyage voyage;
    private String nomDestination;

    public void setDonnees(Voyage voyage, String nomDestination) {
        this.voyage = voyage;
        this.nomDestination = nomDestination;

        titreVoyage.setText(voyage.getTitre_voyage());
        destinationVoyage.setText(nomDestination);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.FRENCH);
        String dateDebutStr = dateFormat.format(voyage.getDate_debut());
        String dateFinStr = dateFormat.format(voyage.getDate_fin());
        datesVoyage.setText(dateDebutStr + " - " + dateFinStr);

        String statutTexte = voyage.getStatut() != null ? voyage.getStatut() : "À venir";
        statutVoyage.setText("● " + statutTexte);

        String statutCouleur = "#10b981";
        if ("Terminé".equalsIgnoreCase(statutTexte)) {
            statutCouleur = "#64748b";
        } else if ("En cours".equalsIgnoreCase(statutTexte)) {
            statutCouleur = "#f59e0b";
        } else if ("Annulé".equalsIgnoreCase(statutTexte)) {
            statutCouleur = "#ef4444";
        }
        statutVoyage.setStyle("-fx-background-color: " + statutCouleur + "; -fx-text-fill: white; " +
                "-fx-background-radius: 20; -fx-padding: 3 10; -fx-font-size: 10; -fx-font-weight: 600;");

        iconLabel.setText(getIconePourDestination(nomDestination));

        btnModifier.setOnMouseClicked(e -> handleModifier());
        btnParticipants.setOnMouseClicked(e -> handleParticipants());
        btnSupprimer.setOnMouseClicked(e -> handleSupprimer());
    }

    private String getIconePourDestination(String nomDestination) {
        if (nomDestination == null) return "🏖️";
        String dest = nomDestination.toLowerCase();
        if (dest.contains("paris")) return "🗼";
        if (dest.contains("rome")) return "🏛️";
        if (dest.contains("londres")) return "🇬🇧";
        if (dest.contains("new york")) return "🗽";
        if (dest.contains("tokyo")) return "🗾";
        return "🏖️";
    }

    private void handleModifier() {
        System.out.println("Modifier voyage ID: " + voyage.getId_voyage());
        if (VoyageController.instance != null) {
            VoyageController.instance.chargerVoyagePourModification(voyage);
        }
    }

    private void handleParticipants() {
        System.out.println("Participants voyage ID: " + voyage.getId_voyage());
    }

    private void handleSupprimer() {
        try {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation de suppression");
            alert.setHeaderText(null);
            alert.setContentText("Voulez-vous vraiment supprimer le voyage \"" + voyage.getTitre_voyage() + "\" ?");

            Optional<ButtonType> result = alert.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.YES) {
                VoyageCRUD vc = new VoyageCRUD();
                vc.supprimer(voyage.getId_voyage());

                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Succès");
                successAlert.setHeaderText(null);
                successAlert.setContentText("Voyage supprimé avec succès!");
                successAlert.showAndWait();

                if (VoyageController.instance != null) {
                    VoyageController.instance.chargerVoyages();
                    System.out.println("Rafraîchissement réussi!");
                } else {
                    System.err.println("ERREUR: VoyageController.instance est null!");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Erreur");
            errorAlert.setHeaderText(null);
            errorAlert.setContentText("Erreur lors de la suppression: " + e.getMessage());
            errorAlert.showAndWait();
        }
    }
}