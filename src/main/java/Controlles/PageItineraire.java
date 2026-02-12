package Controlles;

import Services.itineraireCRUD;
import Entites.Itineraire;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class PageItineraire {
    @FXML private ComboBox<String> comboDestination;
    @FXML private DatePicker dateDebut;
    @FXML private DatePicker dateFin;
    @FXML private Button btnRechercher;
    @FXML private Label lblTotalItinerairesTable;
    @FXML private VBox emptyPlaceholder;
    @FXML private Button btnCreerPremierItineraire;

    private itineraireCRUD itineraireCRUD;

    @FXML
    public void initialize() {
        itineraireCRUD = new itineraireCRUD();
        loadItineraries();
    }

    private void loadItineraries() {
        try {
            List<Itineraire> itineraires = itineraireCRUD.afficher();
            updateItineraryDisplay(itineraires);
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les itinéraires: " + e.getMessage());
        }
    }

    private void updateItineraryDisplay(List<Itineraire> itineraires) {
        if (itineraires.isEmpty()) {
            emptyPlaceholder.setVisible(true);
            emptyPlaceholder.setManaged(true);
        } else {
            emptyPlaceholder.setVisible(false);
            emptyPlaceholder.setManaged(false);
            // TODO: Update the UI with itineraries
            lblTotalItinerairesTable.setText(itineraires.size() + " itinéraires");
        }
    }

    @FXML
    private void handleCreerClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/PageAjoutItineraire.fxml"));
            DialogPane dialogPane = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setTitle("Nouvel itinéraire");

            Scene scene = new Scene(dialogPane);
            dialogStage.setScene(scene);

            dialogStage.showAndWait();

            // Refresh the list after dialog closes
            loadItineraries();

        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage());
        }
    }

    @FXML
    private void handleRechercherClick() {
        // TODO: Implement search functionality
        loadItineraries();
    }

    @FXML
    private void handleVoirTousProchainsDeparts() {
        // TODO: Implement "see all" functionality
    }

    @FXML
    private void handleTrierParDate() {
        // TODO: Implement sort by date
    }

    @FXML
    private void handleExporter() {
        // TODO: Implement export functionality
    }

    @FXML
    private void handleItineraireClick() {
        // TODO: Navigate to itinerary details
    }

    @FXML
    private void handleJourClick() {
        // TODO: Navigate to day details
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}