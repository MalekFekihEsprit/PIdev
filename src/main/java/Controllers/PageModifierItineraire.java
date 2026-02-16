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

public class PageModifierItineraire {

    @FXML
    private TextField nomField;

    @FXML
    private TextArea descriptionField;

    @FXML
    private ComboBox<PageAjoutItineraire.VoyageItem> voyageCombo;

    @FXML
    private Label errorLabel;

    private itineraireCRUD itineraireCRUD = new itineraireCRUD();
    private Itineraire itineraireAModifier;
    private PageItineraire parentController;
    private MainLayoutController mainLayoutController;

    @FXML
    public void initialize() {
        chargerVoyages();
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        mainLayoutController = MainLayoutController.getInstance();
    }

    private void chargerVoyages() {
        List<PageAjoutItineraire.VoyageItem> voyages = new ArrayList<>();
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
                voyages.add(new PageAjoutItineraire.VoyageItem(
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

    public void setParentController(PageItineraire parentController) {
        this.parentController = parentController;
    }

    private void preRemplirFormulaire() {
        if (itineraireAModifier != null) {
            nomField.setText(itineraireAModifier.getNom_itineraire());
            descriptionField.setText(itineraireAModifier.getDescription_itineraire());

            for (PageAjoutItineraire.VoyageItem item : voyageCombo.getItems()) {
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

        PageAjoutItineraire.VoyageItem selectedVoyage = voyageCombo.getSelectionModel().getSelectedItem();
        if (selectedVoyage == null) {
            showError("Veuillez sélectionner un voyage");
            return;
        }

        try {
            itineraireAModifier.setNom_itineraire(nomField.getText().trim());
            itineraireAModifier.setDescription_itineraire(descriptionField.getText() != null ? descriptionField.getText().trim() : "");
            itineraireAModifier.setId_voyage(selectedVoyage.getId());

            itineraireCRUD.modifier(itineraireAModifier);

            if (parentController != null) {
                parentController.refreshItineraries();
            }

            if (mainLayoutController != null) {
                mainLayoutController.loadPage("/PageItineraire.fxml");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur lors de la modification: " + e.getMessage());
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