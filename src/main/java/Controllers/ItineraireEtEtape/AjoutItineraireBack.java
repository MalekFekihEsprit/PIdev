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

public class AjoutItineraireBack {

    @FXML
    private TextField nomField;

    @FXML
    private TextArea descriptionField;

    @FXML
    private ComboBox<VoyageItem> voyageCombo;

    @FXML
    private Label errorLabel;

    private itineraireCRUD itineraireCRUD = new itineraireCRUD();
    private Voyage voyageActuel;
    private GestionItinerairesController parentController;

    public static class VoyageItem {
        private int id;
        private String titre;
        private String destination;

        public VoyageItem(int id, String titre, String destination) {
            this.id = id;
            this.titre = titre;
            this.destination = destination;
        }

        public int getId() { return id; }
        public String getTitre() { return titre; }
        public String getDestination() { return destination; }

        @Override
        public String toString() {
            return titre + (destination != null && !destination.isEmpty() ? " - " + destination : "");
        }
    }

    @FXML
    public void initialize() {
        chargerVoyages();
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    private void chargerVoyages() {
        List<VoyageItem> voyages = new ArrayList<>();
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
                voyages.add(new VoyageItem(
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

    public void setParentController(GestionItinerairesController controller) {
        this.parentController = controller;
    }

    public void setVoyageActuel(Voyage voyage) {
        this.voyageActuel = voyage;
    }

    public void preselectionnerVoyage(int idVoyage) {
        for (VoyageItem item : voyageCombo.getItems()) {
            if (item.getId() == idVoyage) {
                voyageCombo.getSelectionModel().select(item);
                break;
            }
        }
    }

    @FXML
    private void handleCreer() {
        if (nomField.getText() == null || nomField.getText().trim().isEmpty()) {
            showError("Le nom de l'itinéraire est requis");
            return;
        }

        if (nomField.getText().length() > 10) {
            showError("Le nom ne doit pas dépasser 10 caractères");
            return;
        }

        VoyageItem selectedVoyage = voyageCombo.getSelectionModel().getSelectedItem();
        if (selectedVoyage == null) {
            showError("Veuillez sélectionner un voyage");
            return;
        }

        try {
            Itineraire itineraire = new Itineraire();
            itineraire.setNom_itineraire(nomField.getText().trim());
            itineraire.setDescription_itineraire(descriptionField.getText() != null ? descriptionField.getText().trim() : "");
            itineraire.setId_voyage(selectedVoyage.getId());

            itineraireCRUD.ajouter(itineraire);

            AlertUtil.showInfo("Succès", "Itinéraire créé avec succès !");

            // Retour à la page de gestion Back
            if (parentController != null) {
                parentController.refreshAfterModification();
            }

            // Fermer la fenêtre
            Stage stage = (Stage) nomField.getScene().getWindow();
            stage.close();

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur lors de la création: " + e.getMessage());
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