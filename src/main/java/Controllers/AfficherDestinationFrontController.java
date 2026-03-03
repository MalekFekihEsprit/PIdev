package Controllers;

import Entities.Destination;
import Services.HebergementCRUD;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AfficherDestinationFrontController implements Initializable {

    @FXML private Label lblBreadcrumbDestination;
    @FXML private Label lblDestinationIcon;
    @FXML private Label lblDestinationName;
    @FXML private Label lblDestinationCountry;
    @FXML private Label lblDestinationScore;
    @FXML private Label lblDestinationId;
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
    @FXML private Label lblCurrency;
    @FXML private Label lblLanguages;
    @FXML private Label lblRegion;
    @FXML private ProgressBar scoreProgress;
    @FXML private HBox btnClose;
    @FXML private Button btnClose2;
    @FXML private Button btnVoirHebergements;

    // Flag and video elements
    @FXML private ImageView flagImageView;
    @FXML private WebView videoWebView;
    @FXML private Label lblNoVideo;
    @FXML private VBox videoContainer;

    private Destination destination;
    private HebergementCRUD hebergementCRUD;
    private WebEngine webEngine;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        hebergementCRUD = new HebergementCRUD();

        // Setup close buttons
        btnClose.setOnMouseClicked(event -> closeWindow());
        btnClose2.setOnAction(event -> closeWindow());

        // Setup voir hébergements button
        if (btnVoirHebergements != null) {
            btnVoirHebergements.setOnAction(event -> handleVoirHebergements());
        }

        // Initialize WebView for video
        if (videoWebView != null) {
            webEngine = videoWebView.getEngine();
            webEngine.setJavaScriptEnabled(true);
            webEngine.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");

            // Add error handling
            webEngine.getLoadWorker().exceptionProperty().addListener((obs, oldErr, newErr) -> {
                if (newErr != null) {
                    System.err.println("WebView error: " + newErr.getMessage());
                }
            });
        }

        // Make TextArea read-only and ensure wrapping
        if (taDescription != null) {
            taDescription.setEditable(false);
            taDescription.setWrapText(true);
            taDescription.setStyle("-fx-font-size: 14;");
        }
    }

    public void setDestination(Destination destination) {
        this.destination = destination;
        populateFields();
        loadFlagImage();
        loadVideo();
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

        // Region
        String region = destination.getRegion_destination() != null ? destination.getRegion_destination() : "Non spécifiée";
        if (lblRegion != null) {
            lblRegion.setText(region);
        }

        // Climate and season
        String climate = destination.getClimat_destination() != null ? destination.getClimat_destination() : "Non spécifié";
        String season = destination.getSaison_destination() != null ? destination.getSaison_destination() : "Non spécifié";

        lblClimate.setText(climate);
        lblSeason.setText(season);
        lblStatClimate.setText(climate);
        lblStatSeason.setText(season);
        lblStatCountry.setText(destination.getPays_destination());
        lblStatId.setText("#" + destination.getId_destination());

        // Currency and Languages
        String currency = destination.getCurrency_destination() != null ? destination.getCurrency_destination() : "Non disponible";
        String languages = destination.getLanguages_destination() != null ? destination.getLanguages_destination() : "Non disponible";

        if (lblCurrency != null) lblCurrency.setText(currency);
        if (lblLanguages != null) lblLanguages.setText(languages);

        // Description
        String desc = destination.getDescription_destination() != null ?
                destination.getDescription_destination() : "Aucune description disponible.";
        taDescription.setText(desc);

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

        // Progress bar
        double progress = Math.min(score / 5.0, 1.0);
        scoreProgress.setProgress(progress);

        // Tags
        lblTagCountry.setText("🇫🇷 " + destination.getPays_destination());
        lblTagClimate.setText("🌡️ " + climate);
        lblTagSeason.setText("🗓️ " + season);
        lblTagScore.setText("⭐ " + scoreText);
    }

    private void loadFlagImage() {
        if (flagImageView == null) return;

        String flagUrl = destination.getFlag_destination();
        if (flagUrl != null && !flagUrl.isEmpty()) {
            try {
                Image flagImage = new Image(flagUrl, 45, 30, true, true);
                flagImageView.setImage(flagImage);
                flagImageView.setVisible(true);
            } catch (Exception e) {
                System.err.println("Erreur chargement drapeau: " + e.getMessage());
                flagImageView.setVisible(false);
            }
        } else {
            flagImageView.setVisible(false);
        }
    }

    private void loadVideo() {
        if (videoWebView == null || videoContainer == null || lblNoVideo == null) return;

        String videoUrl = destination.getVideo_url();

        if (videoUrl != null && !videoUrl.isEmpty()) {
            System.out.println("📹 Loading video from URL: " + videoUrl);

            String videoId = extractVideoId(videoUrl);

            if (videoId != null) {
                System.out.println("✅ Extracted video ID: " + videoId);

                // HTML that makes video fill the entire container
                String embedHtml = String.format(
                        "<!DOCTYPE html>" +
                                "<html>" +
                                "<head>" +
                                "<style>" +
                                "html, body { margin: 0; padding: 0; width: 100%%; height: 100%%; overflow: hidden; background: #f8fafc; }" +
                                ".video-container { position: relative; width: 100%%; height: 100%%; }" +
                                "iframe { position: absolute; top: 0; left: 0; width: 100%%; height: 100%%; border: 0; border-radius: 12px; }" +
                                "</style>" +
                                "</head>" +
                                "<body>" +
                                "<div class='video-container'>" +
                                "<iframe src='https://www.youtube.com/embed/%s?autoplay=0&rel=0&showinfo=0&modestbranding=1&playsinline=1&controls=1' " +
                                "allow='accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture' " +
                                "allowfullscreen>" +
                                "</iframe>" +
                                "</div>" +
                                "</body></html>",
                        videoId
                );

                // Ensure WebView fills its container
                videoWebView.setPrefHeight(400);
                videoWebView.setMinHeight(400);
                videoWebView.setMaxHeight(Double.MAX_VALUE);

                // Make sure the container expands properly
                videoContainer.setPrefHeight(450);
                videoContainer.setMinHeight(450);
                videoContainer.setMaxHeight(Double.MAX_VALUE);
                videoContainer.setStyle("-fx-background-color: white; -fx-background-radius: 20; -fx-padding: 20; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.02), 10, 0, 0, 2);");

                // Load the HTML
                webEngine.loadContent(embedHtml, "text/html");

                // Make container visible
                videoContainer.setVisible(true);
                videoContainer.setManaged(true);
                lblNoVideo.setVisible(false);
                lblNoVideo.setManaged(false);

                // Force layout pass
                javafx.application.Platform.runLater(() -> {
                    videoWebView.requestLayout();
                    videoContainer.requestLayout();
                });

                System.out.println("✅ Video should now fill the container");

            } else {
                System.err.println("❌ Could not extract video ID from: " + videoUrl);
                showNoVideo();
            }
        } else {
            System.out.println("ℹ️ No video URL provided");
            showNoVideo();
        }
    }

    private void showNoVideo() {
        if (videoContainer != null && lblNoVideo != null) {
            videoContainer.setVisible(false);
            videoContainer.setManaged(false);
            lblNoVideo.setVisible(true);
            lblNoVideo.setManaged(true);
        }
    }

    private String extractVideoId(String videoUrl) {
        if (videoUrl == null || videoUrl.isEmpty()) return null;

        String videoId = null;

        try {
            // Format: https://www.youtube.com/watch?v=VIDEO_ID
            if (videoUrl.contains("youtube.com/watch")) {
                String[] parts = videoUrl.split("[?&]v=");
                if (parts.length > 1) {
                    videoId = parts[1].split("&")[0];
                }
            }
            // Format: https://youtu.be/VIDEO_ID
            else if (videoUrl.contains("youtu.be/")) {
                String[] parts = videoUrl.split("youtu.be/");
                if (parts.length > 1) {
                    videoId = parts[1].split("\\?")[0];
                }
            }
            // Format: https://www.youtube.com/embed/VIDEO_ID
            else if (videoUrl.contains("youtube.com/embed/")) {
                String[] parts = videoUrl.split("embed/");
                if (parts.length > 1) {
                    videoId = parts[1].split("\\?")[0];
                }
            }
            // Format: Just the video ID
            else if (videoUrl.matches("^[a-zA-Z0-9_-]{11}$")) {
                videoId = videoUrl;
            }

            // Validate
            if (videoId != null && videoId.length() == 11) {
                return videoId;
            }
        } catch (Exception e) {
            System.err.println("Error extracting video ID: " + e.getMessage());
        }

        return null;
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

    private void handleVoirHebergements() {
        if (destination == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Aucune destination sélectionnée");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/HebergementFront.fxml"));
            Parent root = loader.load();

            HebergementFrontController controller = loader.getController();
            controller.filterByDestination(destination);

            Stage stage = (Stage) btnVoirHebergements.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TravelMate - Hébergements à " + destination.getNom_destination());
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