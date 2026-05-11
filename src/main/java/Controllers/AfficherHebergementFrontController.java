package Controllers;

import Entities.Destination;
import Entities.Hebergement;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AfficherHebergementFrontController implements Initializable {

    @FXML private Label lblBreadcrumbHebergement;
    @FXML private Label lblHebergementIcon;
    @FXML private Label lblHebergementName;
    @FXML private Label lblHebergementNote;
    @FXML private Label lblHebergementDestination;
    @FXML private Label lblHebergementType;
    @FXML private Label lblPrix;
    @FXML private Label lblAdresse;
    @FXML private Label lblDestNom;
    @FXML private Label lblDestPays;
    @FXML private Label lblDestClimat;
    @FXML private Label lblTagType;
    @FXML private Label lblTagNote;
    @FXML private Label lblTagPrix;
    @FXML private HBox btnClose;
    @FXML private Button btnClose2;
    @FXML private Button btnVoirDestination;

    // Image banner
    @FXML private javafx.scene.layout.HBox imageBannerBox;
    @FXML private javafx.scene.image.ImageView imageBannerView;

    private Hebergement hebergement;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Setup close buttons
        btnClose.setOnMouseClicked(event -> closeWindow());
        btnClose2.setOnAction(event -> closeWindow());

        // Setup voir destination button
        btnVoirDestination.setOnAction(event -> handleVoirDestination());
    }

    public void setHebergement(Hebergement hebergement) {
        this.hebergement = hebergement;
        populateFields();
        loadImageBanner();
    }

    private void loadImageBanner() {
        if (imageBannerBox == null || imageBannerView == null || hebergement == null) return;
        String imageName = hebergement.getImage_name();
        if (imageName == null || imageName.isBlank()) {
            imageBannerBox.setVisible(false);
            imageBannerBox.setManaged(false);
            return;
        }
        try {
            javafx.scene.image.Image img;
            if (imageName.startsWith("http://") || imageName.startsWith("https://")) {
                img = new javafx.scene.image.Image(imageName, true);
            } else {
                java.net.URL res = getClass().getResource("/images/" + imageName);
                if (res != null) {
                    img = new javafx.scene.image.Image(res.toExternalForm(), true);
                } else {
                    img = new javafx.scene.image.Image(new java.io.File(imageName).toURI().toString(), true);
                }
            }
            imageBannerView.setImage(img);
            // Show at natural size, capped to 400px tall, ratio preserved
            imageBannerView.setPreserveRatio(true);
            imageBannerView.setFitHeight(400);
            imageBannerView.setFitWidth(0); // 0 = unconstrained, driven by fitHeight + ratio
            imageBannerBox.setVisible(true);
            imageBannerBox.setManaged(true);
        } catch (Exception e) {
            System.err.println("Could not load banner image: " + e.getMessage());
            imageBannerBox.setVisible(false);
            imageBannerBox.setManaged(false);
        }
    }

    private void populateFields() {
        if (hebergement == null) return;

        // Set icon based on type
        String icon = getIconForType(hebergement.getType_hebergement());
        lblHebergementIcon.setText(icon);

        // Basic info
        lblHebergementName.setText(safeText(hebergement.getNom_hebergement()));
        lblHebergementNote.setText(formatNote(hebergement.getNote_hebergement()));
        lblHebergementType.setText(safeTypeLabel(hebergement.getType_hebergement()));
        lblBreadcrumbHebergement.setText(safeText(hebergement.getNom_hebergement()));

        // Price
        lblPrix.setText(formatPrice(hebergement.getPrix_nuit_hebergement()));

        // Address
        lblAdresse.setText(safeText(hebergement.getAdresse_hebergement()));

        // Destination info
        Destination dest = hebergement.getDestination();
        if (dest != null) {
            lblHebergementDestination.setText(formatDestination(dest));
            lblDestNom.setText(safeText(dest.getNom_destination()));
            lblDestPays.setText(safeText(dest.getPays_destination()));
            lblDestClimat.setText(safeText(dest.getClimat_destination()));
        } else {
            lblHebergementDestination.setText("Non spécifié");
            lblDestNom.setText("Non spécifié");
            lblDestPays.setText("Non spécifié");
            lblDestClimat.setText("Non spécifié");
        }

        // Tags
        lblTagType.setText("🏨 " + safeText(hebergement.getType_hebergement()));
        lblTagNote.setText("⭐ " + formatNote(hebergement.getNote_hebergement()) + "/5");
        lblTagPrix.setText("💰 " + formatPrice(hebergement.getPrix_nuit_hebergement()).replace(" ", "" ) + "/nuit");
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

    private String safeText(String value) {
        return (value == null || value.isBlank()) ? "Non spécifié" : value;
    }

    private String safeTypeLabel(String type) {
        return (type == null || type.isBlank()) ? "NON SPÉCIFIÉ" : type.toUpperCase();
    }

    private String formatPrice(Double price) {
        return (price == null || price == 0.0) ? "-" : String.format("%.2f €", price);
    }

    private String formatNote(Double note) {
        return (note == null || note == 0.0) ? "-" : String.format("%.1f", note);
    }

    private String formatDestination(Destination destination) {
        if (destination == null) return "Non spécifié";

        String nom = safeText(destination.getNom_destination());
        String pays = safeText(destination.getPays_destination());
        if ("Non spécifié".equals(nom) && "Non spécifié".equals(pays)) {
            return "Non spécifié";
        }
        if ("Non spécifié".equals(nom)) {
            return pays;
        }
        if ("Non spécifié".equals(pays)) {
            return nom;
        }
        return nom + ", " + pays;
    }

    private void handleVoirDestination() {
        if (hebergement == null || hebergement.getDestination() == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherDestinationFront.fxml"));
            Parent root = loader.load();

            AfficherDestinationfrontController controller = loader.getController();
            controller.setDestination(hebergement.getDestination());

            Stage stage = new Stage();
            stage.setTitle("Destination - " + hebergement.getDestination().getNom_destination());
            double width = Screen.getPrimary().getVisualBounds().getWidth();
            double height = Screen.getPrimary().getVisualBounds().getHeight();
            stage.setScene(new Scene(root, width, height));
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.initOwner(btnClose.getScene().getWindow());
            stage.setResizable(true);
            stage.setMaximized(true);
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