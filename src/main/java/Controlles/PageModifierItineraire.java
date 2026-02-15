package Controlles;

import Entites.Itineraire;
import Services.itineraireCRUD;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import Utils.MyBD;

public class PageModifierItineraire {

    @FXML
    private TextField nomField;

    @FXML
    private TextArea descriptionField;

    @FXML
    private ComboBox<String> voyageCombo;

    @FXML
    private Label errorLabel;

    private Map<String, Integer> voyageMap = new HashMap<>();
    private PageItineraire parentController;
    private Itineraire itineraireActuel;
    private itineraireCRUD itineraireCRUD = new itineraireCRUD();

    @FXML
    public void initialize() {
        chargerVoyages();
    }

    public void setParentController(PageItineraire parentController) {
        this.parentController = parentController;
    }

    public void setItineraire(Itineraire itineraire) {
        this.itineraireActuel = itineraire;
        remplirChamps();
    }

    private void remplirChamps() {
        if (itineraireActuel != null) {
            nomField.setText(itineraireActuel.getNom_itineraire());
            descriptionField.setText(itineraireActuel.getDescription_itineraire());

            // Trouver et sélectionner le voyage correspondant
            for (Map.Entry<String, Integer> entry : voyageMap.entrySet()) {
                if (entry.getValue() == itineraireActuel.getId_voyage()) {
                    voyageCombo.setValue(entry.getKey());
                    break;
                }
            }
        }
    }

    private void chargerVoyages() {
        ObservableList<String> voyagesList = FXCollections.observableArrayList();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = MyBD.getInstance().getConn();
            String query = "SELECT v.id_voyage, v.titre_voyage, v.date_debut, v.date_fin, d.nom_destination " +
                    "FROM voyage v " +
                    "LEFT JOIN destination d ON v.id_destination = d.id_destination " +
                    "ORDER BY v.titre_voyage";
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();

            while (rs.next()) {
                int idVoyage = rs.getInt("id_voyage");
                String titre = rs.getString("titre_voyage");
                String nomDestination = rs.getString("nom_destination");
                Date dateDebut = rs.getDate("date_debut");
                Date dateFin = rs.getDate("date_fin");

                int duree = 0;
                if (dateDebut != null && dateFin != null) {
                    long diff = dateFin.getTime() - dateDebut.getTime();
                    duree = (int) (diff / (1000 * 60 * 60 * 24)) + 1;
                }

                String displayText = titre;
                if (nomDestination != null && !nomDestination.isEmpty()) {
                    displayText += " - " + nomDestination;
                }
                if (duree > 0) {
                    displayText += " (" + duree + " jour" + (duree > 1 ? "s" : "") + ")";
                }

                voyagesList.add(displayText);
                voyageMap.put(displayText, idVoyage);
            }

            voyageCombo.setItems(voyagesList);
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);

        } catch (SQLException e) {
            e.printStackTrace();
            errorLabel.setText("Erreur lors du chargement des voyages: " + e.getMessage());
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleModifier() {
        if (nomField.getText() == null || nomField.getText().trim().isEmpty()) {
            errorLabel.setText("Le nom de l'itinéraire est requis");
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
            return;
        }

        if (nomField.getText().length() > 10) {
            errorLabel.setText("Le nom ne doit pas dépasser 10 caractères");
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
            return;
        }

        if (voyageCombo.getValue() == null) {
            errorLabel.setText("Veuillez sélectionner un voyage associé");
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
            return;
        }

        int idVoyage = voyageMap.get(voyageCombo.getValue());

        itineraireActuel.setNom_itineraire(nomField.getText().trim());
        itineraireActuel.setDescription_itineraire(descriptionField.getText() != null ? descriptionField.getText().trim() : null);
        itineraireActuel.setId_voyage(idVoyage);

        try {
            itineraireCRUD.modifier(itineraireActuel);

            if (parentController != null) {
                parentController.refreshItineraries();
            }

            handleAnnuler();
            showAlert("Succès", "Itinéraire modifié avec succès !");

        } catch (SQLException e) {
            e.printStackTrace();
            errorLabel.setText("Erreur lors de la modification: " + e.getMessage());
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
        }
    }

    @FXML
    private void handleAnnuler() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/PageItineraire.fxml"));
            BorderPane itineraireView = loader.load();

            Scene currentScene = nomField.getScene();
            BorderPane rootPane = (BorderPane) currentScene.getRoot();
            rootPane.setCenter(itineraireView);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}