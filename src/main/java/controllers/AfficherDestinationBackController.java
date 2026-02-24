package controllers;

import entities.Destination;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AfficherDestinationBackController implements Initializable {

    @FXML private Label lblBreadcrumbDestination;
    @FXML private Label lblDestinationIcon;
    @FXML private Label lblDestinationName;
    @FXML private Label lblDestinationCountry;
    @FXML private Label lblDestinationScore;
    @FXML private Label lblDestinationId;
    @FXML private Label lblClimate;
    @FXML private Label lblSeason;
    @FXML private Label lblDescription;
    @FXML private Label lblLatitude;
    @FXML private Label lblLongitude;
    @FXML private Label lblScoreValue;
    @FXML private Label lblStatClimate;
    @FXML private Label lblStatSeason;
    @FXML private Label lblStatCountry;
    @FXML private Label lblStatId;
    @FXML private Label lblTagCountry;
    @FXML private Label lblTagClimate;
    @FXML private Label lblTagSeason;
    @FXML private Label lblTagScore;
    @FXML private ProgressBar scoreProgress;
    @FXML private HBox btnClose;
    @FXML private Button btnClose2;
    @FXML private Button btnVoirHebergements; // Make sure this matches your FXML fx:id

    private Destination destination;
    private DestinationBackController parentController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Setup close buttons
        btnClose.setOnMouseClicked(event -> closeWindow());
        btnClose2.setOnAction(event -> closeWindow());



        // Setup voir hébergements button
        if (btnVoirHebergements != null) {
            btnVoirHebergements.setOnAction(event -> handleVoirHebergements());
        }
    }

    public void setDestination(Destination destination) {
        this.destination = destination;
        populateFields();
    }

    public void setParentController(DestinationBackController controller) {
        this.parentController = controller;
    }

    private void populateFields() {
        if (destination == null) return;

        // Set icon based on country or name
        String icon = getIconForDestination(destination);
        lblDestinationIcon.setText(icon);

        // Basic info
        lblDestinationName.setText(destination.getNom_destination());
        lblDestinationCountry.setText(destination.getPays_destination());
        lblDestinationId.setText("ID: " + destination.getId_destination());
        lblBreadcrumbDestination.setText(destination.getNom_destination());

        // Climate and season
        String climate = destination.getClimat_destination() != null ? destination.getClimat_destination() : "Non spécifié";
        String season = destination.getSaison_destination() != null ? destination.getSaison_destination() : "Non spécifié";

        lblClimate.setText(climate);
        lblSeason.setText(season);
        lblStatClimate.setText(climate);
        lblStatSeason.setText(season);
        lblStatCountry.setText(destination.getPays_destination());
        lblStatId.setText("#" + destination.getId_destination());

        // Description
        String desc = destination.getDescription_destination() != null ?
                destination.getDescription_destination() : "Aucune description disponible.";
        lblDescription.setText(desc);

        // Coordinates
        double lat = destination.getLatitude_destination();
        double lon = destination.getLongitude_destination();

        String latStr = String.format("%.4f° %s", Math.abs(lat), lat >= 0 ? "N" : "S");
        String lonStr = String.format("%.4f° %s", Math.abs(lon), lon >= 0 ? "E" : "W");

        lblLatitude.setText(latStr);
        lblLongitude.setText(lonStr);

        // Score
        double score = destination.getScore_destination();
        String scoreText = String.format("%.1f/5", score);
        lblDestinationScore.setText(scoreText);
        lblScoreValue.setText(scoreText);

        // Progress bar (score out of 5)
        double progress = Math.min(score / 5.0, 1.0);
        scoreProgress.setProgress(progress);

        // Tags
        lblTagCountry.setText("🇫🇷 " + destination.getPays_destination());
        lblTagClimate.setText("🌡️ " + climate);
        lblTagSeason.setText("🗓️ " + season);
        lblTagScore.setText("⭐ " + scoreText);
    }

    private String getIconForDestination(Destination destination) {
        String country = destination.getPays_destination().toLowerCase();
        String name = destination.getNom_destination().toLowerCase();

        if (country.contains("france") || name.contains("paris")) return "🗼";
        if (country.contains("italie") || country.contains("italy") || name.contains("rome") || name.contains("venise")) return "🏛️";
        if (country.contains("tunisie") || country.contains("tunisia") || name.contains("djerba")) return "🏖️";
        if (country.contains("suisse") || country.contains("switzerland") || name.contains("chamonix") || name.contains("zermatt")) return "🏔️";
        if (country.contains("indonésie") || country.contains("indonesia") || name.contains("bali")) return "🏝️";
        if (country.contains("grèce") || country.contains("greece") || name.contains("athènes")) return "🏛️";
        if (country.contains("espagne") || country.contains("spain") || name.contains("barcelone") || name.contains("madrid")) return "💃";
        if (country.contains("japon") || country.contains("japan") || name.contains("tokyo") || name.contains("kyoto")) return "🗾";
        if (country.contains("egypte") || country.contains("egypt") || name.contains("le caire")) return "🐫";
        if (country.contains("maroc") || country.contains("morocco") || name.contains("marrakech")) return "🕌";
        if (country.contains("royaume-uni") || country.contains("londres") || name.contains("london")) return "🇬🇧";
        if (country.contains("états-unis") || country.contains("new york") || name.contains("nyc")) return "🗽";

        return "🌍";
    }

    /**
     * Handles the "Voir Hébergements" button click
     * Navigates to Hébergement back office with pre-filter for this destination
     */
    private void handleVoirHebergements() {
        if (destination == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Aucune destination sélectionnée");
            return;
        }

        try {
            // Load the Hebergement back office FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/HebergementBack.fxml"));
            Parent root = loader.load();

            // Get the controller and pass the destination to filter by
            HebergementBackController controller = loader.getController();

            // IMPORTANT: Set the filter before showing the window
            // This ensures the hébergements are filtered immediately
            controller.filterByDestination(destination);

            // Get the current stage and set the new scene
            Stage stage = (Stage) btnVoirHebergements.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TravelMate - Hébergements à " + destination.getNom_destination() + " (Admin)");
            stage.setMaximized(true);

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir les hébergements: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void closeWindow() {
        Stage stage = (Stage) btnClose.getScene().getWindow();
        stage.close();
    }
}