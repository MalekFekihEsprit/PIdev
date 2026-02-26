package controllers;

import entities.Destination;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AfficherDestinationfrontController implements Initializable {

    // ============== FXML INJECTED ELEMENTS ==============

    @FXML private Label lblBreadcrumbDestination;
    @FXML private Label lblDestinationIcon;
    @FXML private Label lblDestinationName;
    @FXML private Label lblDestinationCountry;
    @FXML private Label lblDestinationScore;
    @FXML private Label lblClimate;
    @FXML private Label lblSeason;
    @FXML private TextArea taDescription;
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
    @FXML private Button btnVoirHebergements;

    // ============== CLASS VARIABLES ==============

    private Destination destination;

    // ============== INITIALIZATION ==============

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Setup button actions
        setupButtons();

        // Make TextArea read-only and ensure wrapping
        if (taDescription != null) {
            taDescription.setEditable(false);
            taDescription.setWrapText(true);
            taDescription.setStyle("-fx-font-size: 15;");
        }
    }

    private void setupButtons() {
        // Close button (HBox)
        if (btnClose != null) {
            btnClose.setOnMouseClicked(event -> closeWindow());
        }

        // Close button (Button)
        if (btnClose2 != null) {
            btnClose2.setOnAction(event -> closeWindow());
        }

        // Voir Hébergements button
        if (btnVoirHebergements != null) {
            btnVoirHebergements.setOnAction(event -> handleVoirHebergements());
        }
    }

    // ============== PUBLIC METHODS ==============

    /**
     * Sets the destination to display and populates all fields
     * @param destination The destination object to display
     */
    public void setDestination(Destination destination) {
        if (destination == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Aucune destination à afficher");
            return;
        }

        this.destination = destination;
        populateFields();
    }

    /**
     * Getter for the destination (useful for the hébergements feature later)
     */
    public Destination getDestination() {
        return destination;
    }

    // ============== PRIVATE METHODS ==============

    /**
     * Populates all UI fields with the destination data
     */
    private void populateFields() {
        try {
            // Set icon based on destination
            lblDestinationIcon.setText(getIconForDestination(destination));

            // Basic information
            lblDestinationName.setText(safeString(destination.getNom_destination()));
            lblDestinationCountry.setText(safeString(destination.getPays_destination()));
            lblBreadcrumbDestination.setText(safeString(destination.getNom_destination()));

            // Climate and season
            String climate = safeString(destination.getClimat_destination(), "Non spécifié");
            String season = safeString(destination.getSaison_destination(), "Non spécifié");

            lblClimate.setText(climate);
            lblSeason.setText(season);
            lblStatClimate.setText(climate);
            lblStatSeason.setText(season);
            lblStatCountry.setText(safeString(destination.getPays_destination()));
            lblStatId.setText("#" + destination.getId_destination());

            // Description - using TextArea with large text
            String desc = safeString(destination.getDescription_destination(), "Aucune description disponible.");
            taDescription.setText(desc);

            // Coordinates
            double lat = destination.getLatitude_destination();
            double lon = destination.getLongitude_destination();

            lblLatitude.setText(formatCoordinate(lat, "N", "S"));
            lblLongitude.setText(formatCoordinate(lon, "E", "W"));

            // Score
            double score = destination.getScore_destination();
            String scoreText = String.format("%.1f/5", score);
            lblDestinationScore.setText(scoreText);
            lblScoreValue.setText(scoreText);

            // Progress bar (score out of 5)
            double progress = Math.min(score / 5.0, 1.0);
            scoreProgress.setProgress(progress);

            // Tags
            lblTagCountry.setText("🇫🇷 " + safeString(destination.getPays_destination()));
            lblTagClimate.setText("🌡️ " + climate);
            lblTagSeason.setText("🗓️ " + season);
            lblTagScore.setText("⭐ " + scoreText);

        } catch (Exception e) {
            System.err.println("Error populating destination fields: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'afficher les détails de la destination");
        }
    }

    /**
     * Returns an appropriate icon emoji based on the destination
     */
    private String getIconForDestination(Destination destination) {
        if (destination == null) return "🌍";

        String country = safeString(destination.getPays_destination()).toLowerCase();
        String name = safeString(destination.getNom_destination()).toLowerCase();

        // France / Paris
        if (country.contains("france") || name.contains("paris")) return "🗼";

        // Italy
        if (country.contains("italie") || country.contains("italy") ||
                name.contains("rome") || name.contains("venise") || name.contains("florence")) return "🏛️";

        // Tunisia / Beach destinations
        if (country.contains("tunisie") || country.contains("tunisia") ||
                name.contains("djerba") || name.contains("hammamet") || name.contains("sousse")) return "🏖️";

        // Switzerland / Mountains
        if (country.contains("suisse") || country.contains("switzerland") ||
                name.contains("chamonix") || name.contains("zermatt") || name.contains("alpes")) return "🏔️";

        // Indonesia / Tropical
        if (country.contains("indonésie") || country.contains("indonesia") ||
                name.contains("bali") || name.contains("java")) return "🏝️";

        // Greece
        if (country.contains("grèce") || country.contains("greece") ||
                name.contains("athènes") || name.contains("santorin") || name.contains("mykonos")) return "🏛️";

        // Spain
        if (country.contains("espagne") || country.contains("spain") ||
                name.contains("barcelone") || name.contains("madrid") || name.contains("ibiza")) return "💃";

        // Japan
        if (country.contains("japon") || country.contains("japan") ||
                name.contains("tokyo") || name.contains("kyoto") || name.contains("osaka")) return "🗾";

        // Egypt
        if (country.contains("egypte") || country.contains("egypt") ||
                name.contains("le caire") || name.contains("cairo") || name.contains("pyramides")) return "🐫";

        // Morocco
        if (country.contains("maroc") || country.contains("morocco") ||
                name.contains("marrakech") || name.contains("casablanca") || name.contains("fès")) return "🕌";

        // United Kingdom / London
        if (country.contains("royaume-uni") || country.contains("united kingdom") ||
                country.contains("angleterre") || country.contains("england") ||
                name.contains("londres") || name.contains("london")) return "🇬🇧";

        // USA / New York
        if (country.contains("états-unis") || country.contains("usa") ||
                country.contains("america") || name.contains("new york") ||
                name.contains("los angeles") || name.contains("san francisco")) return "🗽";

        // Default icon
        return "🌍";
    }

    /**
     * Formats a coordinate value with proper direction
     */
    private String formatCoordinate(double value, String positiveDir, String negativeDir) {
        String direction = value >= 0 ? positiveDir : negativeDir;
        return String.format("%.4f° %s", Math.abs(value), direction);
    }

    /**
     * Safe string conversion that returns the input or empty string if null
     */
    private String safeString(String str) {
        return str != null ? str : "";
    }

    /**
     * Safe string conversion with default value
     */
    private String safeString(String str, String defaultValue) {
        return str != null && !str.trim().isEmpty() ? str : defaultValue;
    }

    /**
     * Handles the "Voir Hébergements" button click
     * Navigates to Hébergement front office with filter for this destination
     */
    private void handleVoirHebergements() {
        if (destination == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Aucune destination sélectionnée");
            return;
        }

        try {
            // Load the Hebergement front office FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/HebergementFront.fxml"));
            Parent root = loader.load();

            // Get the controller and pass the destination to filter by
            HebergementFrontController controller = loader.getController();
            controller.filterByDestination(destination);

            // Get the current stage and set the new scene
            Stage stage = (Stage) btnVoirHebergements.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TravelMate - Hébergements à " + destination.getNom_destination());
            stage.setMaximized(true);

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir les hébergements: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Closes the current window
     */
    private void closeWindow() {
        try {
            Stage stage = (Stage) btnClose.getScene().getWindow();
            stage.close();
        } catch (Exception e) {
            System.err.println("Error closing window: " + e.getMessage());
        }
    }

    /**
     * Shows an alert dialog
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}