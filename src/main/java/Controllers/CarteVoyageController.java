package Controllers;

import Entities.Voyage;
import Services.VoyageCRUD;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
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

        // Titre
        titreVoyage.setText(voyage.getTitre_voyage());

        // Destination
        destinationVoyage.setText(nomDestination);

        // Formatage des dates
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.FRENCH);
        String dateDebutStr = voyage.getDate_debut().toLocalDate().format(formatter);
        String dateFinStr = voyage.getDate_fin().toLocalDate().format(formatter);
        datesVoyage.setText(dateDebutStr + " - " + dateFinStr);

        // Statut avec style
        String statutTexte = voyage.getStatut() != null ? voyage.getStatut() : "a venir";
        statutVoyage.setText("● " + statutTexte);

        // Couleur selon statut
        String statutCouleur = "#10b981"; // Vert
        if ("Terminé".equalsIgnoreCase(statutTexte)) {
            statutCouleur = "#64748b"; // Gris
        } else if ("En cours".equalsIgnoreCase(statutTexte)) {
            statutCouleur = "#f59e0b"; // Orange
        } else if ("Annulé".equalsIgnoreCase(statutTexte)) {
            statutCouleur = "#ef4444"; // Rouge
        } else if ("a venir".equalsIgnoreCase(statutTexte)) {
            statutCouleur = "#10b981"; // Vert
        }

        statutVoyage.setStyle("-fx-background-color: " + statutCouleur + "; -fx-text-fill: white; " +
                "-fx-background-radius: 20; -fx-padding: 3 10; -fx-font-size: 10; -fx-font-weight: 600;");

        // Icône
        iconLabel.setText(getIconePourDestination(nomDestination));

        // Valeurs par défaut
        participantsVoyage.setText("0 participants");
        activitesVoyage.setText("0 activités");

        // Actions
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
        try {
            // Ouvrir la page des participants
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/PageParticipation.fxml"));
            Parent root = loader.load();

            // Récupérer le contrôleur et passer les données du voyage
            ParticipationController participationController = loader.getController();
            participationController.initData(
                    voyage.getId_voyage(),
                    voyage.getTitre_voyage(),
                    nomDestination,
                    datesVoyage.getText()
            );

            // Changer de scène
            Stage stage = (Stage) btnParticipants.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la page des participants: " + e.getMessage());
        }
    }

    private void handleSupprimer() {
        try {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation de suppression");
            alert.setHeaderText(null);
            alert.setContentText("Voulez-vous vraiment supprimer le voyage \"" + voyage.getTitre_voyage() + "\" ?");

            Optional<ButtonType> result = alert.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.OK) {
                VoyageCRUD vc = new VoyageCRUD();
                vc.supprimer(voyage.getId_voyage());

                showAlert(Alert.AlertType.INFORMATION, "Succès", "Voyage supprimé avec succès!");

                if (VoyageController.instance != null) {
                    VoyageController.instance.chargerVoyages();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la suppression: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}