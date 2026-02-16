package Controllers;

import Entites.Itineraire;
import Services.itineraireCRUD;
import Utils.MyBD;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PageAjoutItineraire {

    @FXML
    private TextField nomField;

    @FXML
    private TextArea descriptionField;

    @FXML
    private ComboBox<VoyageItem> voyageCombo;

    @FXML
    private Label errorLabel;

    private itineraireCRUD itineraireCRUD = new itineraireCRUD();
    private PageItineraire parentController;
    private MainLayoutController mainLayoutController;

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
        mainLayoutController = MainLayoutController.getInstance();
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
                    "LEFT JOIN destination d ON v.id_destination = d.id_destination";
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

    public void setParentController(PageItineraire parentController) {
        this.parentController = parentController;
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

            if (parentController != null) {
                parentController.refreshItineraries();
            }

            // Retourner à la liste
            if (mainLayoutController != null) {
                mainLayoutController.loadPage("/PageItineraire.fxml");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur lors de la création: " + e.getMessage());
        }
    }

    @FXML
    private void handleAnnuler() {
        if (mainLayoutController != null) {
            mainLayoutController.loadPage("/PageItineraire.fxml");
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: #ef4444;");
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }
}