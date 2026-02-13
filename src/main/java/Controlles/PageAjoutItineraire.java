package Controlles;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import Utils.MyBD;

public class PageAjoutItineraire {

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

    @FXML
    public void initialize() {
        chargerVoyages();
    }

    public void setParentController(PageItineraire parentController) {
        this.parentController = parentController;
    }

    private void chargerVoyages() {
        ObservableList<String> voyagesList = FXCollections.observableArrayList();
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
                int idVoyage = rs.getInt("id_voyage");
                String titre = rs.getString("titre_voyage");
                String nomDestination = rs.getString("nom_destination");

                String displayText = titre;
                if (nomDestination != null && !nomDestination.isEmpty()) {
                    displayText += " - " + nomDestination;
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
            // Fermer seulement le ResultSet et le Statement, pas la connexion
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleCreer() {
        // Validation des champs
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
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = MyBD.getInstance().getConn();
            String insertQuery = "INSERT INTO itineraire (nom_itineraire, description_itineraire, id_voyage) VALUES (?, ?, ?)";
            stmt = conn.prepareStatement(insertQuery);

            stmt.setString(1, nomField.getText().trim());
            stmt.setString(2, descriptionField.getText() != null ? descriptionField.getText().trim() : null);
            stmt.setInt(3, idVoyage);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                if (parentController != null) {
                    parentController.refreshItineraries();
                }
                handleAnnuler();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            errorLabel.setText("Erreur lors de la création: " + e.getMessage());
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
        } finally {
            // Fermer seulement le Statement, pas la connexion
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
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
}