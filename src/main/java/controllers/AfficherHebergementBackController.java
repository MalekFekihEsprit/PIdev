package controllers;

import entities.Hebergement;
import entities.Destination;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AfficherHebergementBackController implements Initializable {

    @FXML private Label lblBreadcrumbHebergement;
    @FXML private Label lblHebergementIcon;
    @FXML private Label lblHebergementName;
    @FXML private Label lblHebergementNote;
    @FXML private Label lblHebergementDestination;
    @FXML private Label lblHebergementId;
    @FXML private Label lblHebergementType;
    @FXML private Label lblPrix;
    @FXML private Label lblScore;
    @FXML private Label lblAdresse;
    @FXML private Label lblLatitude;
    @FXML private Label lblLongitude;
    @FXML private Label lblDestNom;
    @FXML private Label lblDestPays;
    @FXML private Label lblDestClimat;
    @FXML private Label lblTagType;
    @FXML private Label lblTagNote;
    @FXML private Label lblTagScore;
    @FXML private Label lblTagPrix;
    @FXML private HBox btnClose;
    @FXML private Button btnClose2;
    @FXML private Button btnModifier;
    @FXML private Button btnVoirDestination;

    private Hebergement hebergement;
    private HebergementBackController parentController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Setup close buttons
        btnClose.setOnMouseClicked(event -> closeWindow());
        btnClose2.setOnAction(event -> closeWindow());

        // Setup modifier button
        btnModifier.setOnAction(event -> handleModifier());

        // Setup voir destination button
        btnVoirDestination.setOnAction(event -> handleVoirDestination());
    }

    public void setHebergement(Hebergement hebergement) {
        this.hebergement = hebergement;
        populateFields();
    }

    public void setParentController(HebergementBackController controller) {
        this.parentController = controller;
    }

    private void populateFields() {
        if (hebergement == null) return;

        // Set icon based on type
        String icon = getIconForType(hebergement.getType_hebergement());
        lblHebergementIcon.setText(icon);

        // Basic info
        lblHebergementName.setText(hebergement.getNom_hebergement());
        lblHebergementNote.setText(String.format("%.1f", hebergement.getNote_hebergement()));
        lblHebergementId.setText("ID: " + hebergement.getId_hebergement());
        lblHebergementType.setText(hebergement.getType_hebergement().toUpperCase());
        lblBreadcrumbHebergement.setText(hebergement.getNom_hebergement());

        // Price and score
        lblPrix.setText(String.format("%.2f €", hebergement.getPrixNuit_hebergement()));
        lblScore.setText(String.format("%.1f/10", hebergement.getScore_hebergement()));

        // Address
        lblAdresse.setText(hebergement.getAdresse_hebergement());

        // Coordinates
        double lat = hebergement.getLatitude_hebergement();
        double lon = hebergement.getLongitude_hebergement();

        String latStr = String.format("%.4f° %s", Math.abs(lat), lat >= 0 ? "N" : "S");
        String lonStr = String.format("%.4f° %s", Math.abs(lon), lon >= 0 ? "E" : "W");

        lblLatitude.setText(latStr);
        lblLongitude.setText(lonStr);

        // Destination info
        Destination dest = hebergement.getDestination();
        if (dest != null) {
            lblHebergementDestination.setText(dest.getNom_destination() + ", " + dest.getPays_destination());
            lblDestNom.setText(dest.getNom_destination());
            lblDestPays.setText(dest.getPays_destination());
            lblDestClimat.setText(dest.getClimat_destination() != null ? dest.getClimat_destination() : "Non spécifié");
        } else {
            lblHebergementDestination.setText("Non spécifié");
            lblDestNom.setText("Non spécifié");
            lblDestPays.setText("Non spécifié");
            lblDestClimat.setText("Non spécifié");
        }

        // Tags
        lblTagType.setText("🏨 " + hebergement.getType_hebergement());
        lblTagNote.setText(String.format("⭐ %.1f/5", hebergement.getNote_hebergement()));
        lblTagScore.setText(String.format("📊 Score: %.1f", hebergement.getScore_hebergement()));
        lblTagPrix.setText(String.format("💰 %.2f€/nuit", hebergement.getPrixNuit_hebergement()));
    }

    private String getIconForType(String type) {
        if (type == null) return "🏨";

        String typeLower = type.toLowerCase();
        if (typeLower.contains("hôtel") || typeLower.contains("hotel")) return "🏨";
        if (typeLower.contains("appartement")) return "🏢";
        if (typeLower.contains("villa")) return "🏡";
        if (typeLower.contains("auberge")) return "🏠";
        if (typeLower.contains("camping")) return "⛺";
        if (typeLower.contains("chalet")) return "🏔️";
        if (typeLower.contains("riad") || typeLower.contains("maison d'hôte")) return "🕌";

        return "🏨";
    }

    private void handleModifier() {
        // Close current window
        Stage currentStage = (Stage) btnModifier.getScene().getWindow();
        currentStage.close();

        // Open modifier form with this hebergement
        if (parentController != null) {
            parentController.handleModifier(hebergement);
        }
    }

    private void handleVoirDestination() {
        if (hebergement == null || hebergement.getDestination() == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherDestinationBack.fxml"));
            Parent root = loader.load();

            AfficherDestinationBackController controller = loader.getController();
            controller.setDestination(hebergement.getDestination());

            Stage stage = new Stage();
            stage.setTitle("Destination - " + hebergement.getDestination().getNom_destination());
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) btnClose.getScene().getWindow();
        stage.close();
    }
}