package Controllers.ItineraireEtEtape;

import Entities.Itineraire;
import Entities.Voyage;
import Services.itineraireCRUD;
import Utils.MyBD;
import Utils.AlertUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ModifierItineraireBack {

    @FXML
    private TextField nomField;

    @FXML
    private TextArea descriptionField;

    @FXML
    private ComboBox<AjoutItineraireBack.VoyageItem> voyageCombo;

    @FXML
    private Label errorLabel;

    private itineraireCRUD itineraireCRUD = new itineraireCRUD();
    private Itineraire itineraireAModifier;
    private Voyage voyageActuel;
    private GestionItinerairesController parentController;

    @FXML
    public void initialize() {
        chargerVoyages();
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    private void chargerVoyages() {
        List<AjoutItineraireBack.VoyageItem> voyages = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = MyBD.getInstance().getConn();
            String query = "SELECT v.id_voyage, v.titre_voyage, d.nom_destination " +
                    "FROM voyage v " +
                    "LEFT JOIN destination d ON v.id_destination = d.id_destination " +
                    "ORDER BY v.titre_voyage";
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();

            while (rs.next()) {
                voyages.add(new AjoutItineraireBack.VoyageItem(
                        rs.getInt("id_voyage"),
                        rs.getString("titre_voyage"),
                        rs.getString("nom_destination")
                ));
            }

            voyageCombo.getItems().addAll(voyages);

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur lors du chargement des voyages: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void setItineraire(Itineraire itineraire) {
        this.itineraireAModifier = itineraire;
        preRemplirFormulaire();
    }

    public void setParentController(GestionItinerairesController controller) {
        this.parentController = controller;
    }

    public void setVoyageActuel(Voyage voyage) {
        this.voyageActuel = voyage;
    }

    private void preRemplirFormulaire() {
        if (itineraireAModifier != null) {
            nomField.setText(itineraireAModifier.getNom_itineraire());
            descriptionField.setText(itineraireAModifier.getDescription_itineraire());

            for (AjoutItineraireBack.VoyageItem item : voyageCombo.getItems()) {
                if (item.getId() == itineraireAModifier.getId_voyage()) {
                    voyageCombo.getSelectionModel().select(item);
                    break;
                }
            }
        }
    }

    @FXML
    private void handleModifier() {
        if (nomField.getText() == null || nomField.getText().trim().isEmpty()) {
            showError("Le nom de l'itinéraire est requis");
            return;
        }

        if (nomField.getText().length() > 10) {
            showError("Le nom ne doit pas dépasser 10 caractères");
            return;
        }

        AjoutItineraireBack.VoyageItem selectedVoyage = voyageCombo.getSelectionModel().getSelectedItem();
        if (selectedVoyage == null) {
            showError("Veuillez sélectionner un voyage");
            return;
        }

        try {
            itineraireAModifier.setNom_itineraire(nomField.getText().trim());
            itineraireAModifier.setDescription_itineraire(descriptionField.getText() != null ? descriptionField.getText().trim() : "");
            itineraireAModifier.setId_voyage(selectedVoyage.getId());

            itineraireCRUD.modifier(itineraireAModifier);

            AlertUtil.showInfo("Succès", "Itinéraire modifié avec succès !");

            // Retour à la page de gestion Back
            if (parentController != null) {
                parentController.refreshAfterModification();
            }

            // Fermer la fenêtre
            Stage stage = (Stage) nomField.getScene().getWindow();
            stage.close();

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur lors de la modification: " + e.getMessage());
        }
    }

    @FXML
    private void handleAnnuler() {
        Stage stage = (Stage) nomField.getScene().getWindow();
        stage.close();
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: #ef4444;");
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }
}