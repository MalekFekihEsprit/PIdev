package Controllers;

import Entities.Destination;
import Services.HebergementCRUD;
import Services.NoteDestinationCRUD;
import Utils.UserSession;
import Entities.User;
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
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class AfficherDestinationfrontController implements Initializable {

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

    // Image banner
    @FXML private javafx.scene.layout.HBox imageBannerBox;
    @FXML private javafx.scene.image.ImageView imageBannerView;

    // Rating elements
    @FXML private HBox starsContainer;
    @FXML private Label lblRatingPrompt;
    @FXML private Label lblRatingConfirm;
    @FXML private Label lblRatingAvg;

    private Destination destination;
    private HebergementCRUD hebergementCRUD;
    private NoteDestinationCRUD noteCRUD;
    private WebEngine webEngine;
    private User currentUser;
    private int currentUserRating = 0; // 0 = not rated yet

    // The 5 star labels built programmatically
    private final Label[] starLabels = new Label[5];

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        hebergementCRUD = new HebergementCRUD();
        noteCRUD = new NoteDestinationCRUD();
        currentUser = UserSession.getInstance().getCurrentUser();

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
            webEngine.getLoadWorker().exceptionProperty().addListener((obs, oldErr, newErr) -> {
                if (newErr != null) System.err.println("WebView error: " + newErr.getMessage());
            });
        }

        // Make TextArea read-only
        if (taDescription != null) {
            taDescription.setEditable(false);
            taDescription.setWrapText(true);
            taDescription.setStyle("-fx-font-size: 14;");
        }

        // Build the 5 interactive star labels
        buildStarRating();
    }

    // ── Rating ────────────────────────────────────────────────────────────────

    private void buildStarRating() {
        if (starsContainer == null) return;
        starsContainer.getChildren().clear();

        for (int i = 0; i < 5; i++) {
            final int starValue = i + 1; // 1-based
            Label star = new Label("☆");
            star.setStyle("-fx-font-size: 36; -fx-cursor: hand; -fx-text-fill: #cbd5e1;");

            // Hover: highlight up to this star
            star.setOnMouseEntered(e -> paintStars(starValue, true));
            star.setOnMouseExited(e -> paintStars(currentUserRating, false));

            // Click: save rating
            star.setOnMouseClicked(e -> submitRating(starValue));

            starLabels[i] = star;
            starsContainer.getChildren().add(star);
        }
    }

    /** Colours stars 1..value in gold, the rest grey. hover=true uses a lighter gold. */
    private void paintStars(int value, boolean hover) {
        String filledColor = hover ? "#fbbf24" : "#f59e0b";
        for (int i = 0; i < 5; i++) {
            if (starLabels[i] == null) continue;
            if (i < value) {
                starLabels[i].setText("★");
                starLabels[i].setStyle("-fx-font-size: 36; -fx-cursor: hand; -fx-text-fill: " + filledColor + ";");
            } else {
                starLabels[i].setText("☆");
                starLabels[i].setStyle("-fx-font-size: 36; -fx-cursor: hand; -fx-text-fill: #cbd5e1;");
            }
        }
    }

    private void submitRating(int stars) {
        if (currentUser == null || destination == null) return;
        try {
            noteCRUD.saveRating(currentUser.getId(), destination.getId_destination(), stars);
            noteCRUD.refreshDestinationScore(destination.getId_destination());
            currentUserRating = stars;
            paintStars(stars, false);

            // Update confirmation label
            if (lblRatingConfirm != null) {
                lblRatingConfirm.setText("✓ Vous avez noté cette destination " + stars + "/5");
                lblRatingConfirm.setVisible(true);
                lblRatingConfirm.setManaged(true);
            }
            if (lblRatingPrompt != null) {
                lblRatingPrompt.setText("Modifiez votre note en cliquant sur une autre étoile");
            }

            // Refresh the displayed average score
            loadAverageScore();

        } catch (SQLException e) {
            System.err.println("Could not save rating: " + e.getMessage());
        }
    }

    private void loadUserRatingAndAverage() {
        if (currentUser == null || destination == null) return;
        try {
            currentUserRating = noteCRUD.getUserRating(currentUser.getId(), destination.getId_destination());
            paintStars(currentUserRating, false);

            if (currentUserRating > 0 && lblRatingPrompt != null) {
                lblRatingPrompt.setText("Modifiez votre note en cliquant sur une autre étoile");
                if (lblRatingConfirm != null) {
                    lblRatingConfirm.setText("✓ Vous avez noté cette destination " + currentUserRating + "/5");
                    lblRatingConfirm.setVisible(true);
                    lblRatingConfirm.setManaged(true);
                }
            }

            loadAverageScore();

        } catch (SQLException e) {
            System.err.println("Could not load user rating: " + e.getMessage());
        }
    }

    private void loadAverageScore() throws SQLException {
        // Re-read the updated score from the destination table
        Services.DestinationCRUD destCRUD = new Services.DestinationCRUD();
        Destination updated = destCRUD.getDestinationById(destination.getId_destination());
        if (updated != null && lblRatingAvg != null) {
            double avg = updated.getScore_destination();
            lblRatingAvg.setText(String.format("Moyenne : %.1f / 5", avg));
            // Also refresh the score labels in the header
            String scoreText = String.format("%.1f/5", avg);
            if (lblDestinationScore != null) lblDestinationScore.setText(scoreText);
            if (lblScoreValue != null) lblScoreValue.setText(scoreText);
            if (lblTagScore != null) lblTagScore.setText("⭐ " + scoreText);
            if (scoreProgress != null) scoreProgress.setProgress(Math.min(avg / 5.0, 1.0));
        }
    }

    // ── Destination data ──────────────────────────────────────────────────────

    public void setDestination(Destination destination) {
        this.destination = destination;
        populateFields();
        loadFlagImage();
        loadVideo();
        loadImageBanner();
        loadUserRatingAndAverage();
    }

    private void loadImageBanner() {
        if (imageBannerBox == null || imageBannerView == null || destination == null) return;
        String imageName = destination.getImage_name();
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
        if (destination == null) return;

        lblDestinationIcon.setText(getIconForDestination(destination));
        lblDestinationName.setText(destination.getNom_destination());
        lblDestinationCountry.setText(destination.getPays_destination());
        lblDestinationId.setText("ID: " + destination.getId_destination());
        lblBreadcrumbDestination.setText(destination.getNom_destination());

        String region = destination.getRegion_destination() != null ? destination.getRegion_destination() : "Non spécifiée";
        if (lblRegion != null) lblRegion.setText(region);

        String climate = destination.getClimat_destination() != null ? destination.getClimat_destination() : "Non spécifié";
        String season  = destination.getSaison_destination()  != null ? destination.getSaison_destination()  : "Non spécifié";

        lblClimate.setText(climate);
        lblSeason.setText(season);
        lblStatClimate.setText(climate);
        lblStatSeason.setText(season);
        lblStatCountry.setText(destination.getPays_destination());
        lblStatId.setText("#" + destination.getId_destination());

        if (lblCurrency  != null) lblCurrency.setText(destination.getCurrency_destination()  != null ? destination.getCurrency_destination()  : "Non disponible");
        if (lblLanguages != null) lblLanguages.setText(destination.getLanguages_destination() != null ? destination.getLanguages_destination() : "Non disponible");

        taDescription.setText(destination.getDescription_destination() != null ? destination.getDescription_destination() : "Aucune description disponible.");

        double lat = destination.getLatitude_destination();
        double lon = destination.getLongitude_destination();
        lblLatitude.setText(String.format("%.4f° %s", Math.abs(lat), lat >= 0 ? "N" : "S"));
        lblLongitude.setText(String.format("%.4f° %s", Math.abs(lon), lon >= 0 ? "E" : "W"));

        double score = destination.getScore_destination();
        String scoreText = String.format("%.1f/5", score);
        lblDestinationScore.setText(scoreText);
        lblScoreValue.setText(scoreText);
        scoreProgress.setProgress(Math.min(score / 5.0, 1.0));

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
                flagImageView.setImage(new Image(flagUrl, 45, 30, true, true));
                flagImageView.setVisible(true);
            } catch (Exception e) {
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
            String videoId = extractVideoId(videoUrl);
            if (videoId != null) {
                String embedHtml = String.format(
                    "<!DOCTYPE html><html><head><style>" +
                    "html,body{margin:0;padding:0;width:100%%;height:100%%;overflow:hidden;background:#f8fafc;}" +
                    ".vc{position:relative;width:100%%;height:100%%;}" +
                    "iframe{position:absolute;top:0;left:0;width:100%%;height:100%%;border:0;border-radius:12px;}" +
                    "</style></head><body><div class='vc'>" +
                    "<iframe src='https://www.youtube.com/embed/%s?autoplay=0&rel=0&modestbranding=1&controls=1' allowfullscreen></iframe>" +
                    "</div></body></html>", videoId);

                videoWebView.setPrefHeight(400);
                videoWebView.setMinHeight(400);
                videoContainer.setPrefHeight(450);
                videoContainer.setMinHeight(450);
                videoContainer.setStyle("-fx-background-color: white; -fx-background-radius: 20; -fx-padding: 20;");
                webEngine.loadContent(embedHtml, "text/html");
                videoContainer.setVisible(true);
                videoContainer.setManaged(true);
                lblNoVideo.setVisible(false);
                lblNoVideo.setManaged(false);
                javafx.application.Platform.runLater(() -> { videoWebView.requestLayout(); videoContainer.requestLayout(); });
            } else {
                showNoVideo();
            }
        } else {
            showNoVideo();
        }
    }

    private void showNoVideo() {
        if (videoContainer != null) { videoContainer.setVisible(false); videoContainer.setManaged(false); }
        if (lblNoVideo != null)     { lblNoVideo.setVisible(true);      lblNoVideo.setManaged(true); }
    }

    private String extractVideoId(String url) {
        if (url == null || url.isEmpty()) return null;
        try {
            String id = null;
            if (url.contains("youtube.com/watch"))        id = url.split("[?&]v=")[1].split("&")[0];
            else if (url.contains("youtu.be/"))           id = url.split("youtu.be/")[1].split("\\?")[0];
            else if (url.contains("youtube.com/embed/"))  id = url.split("embed/")[1].split("\\?")[0];
            else if (url.matches("^[a-zA-Z0-9_-]{11}$")) id = url;
            return (id != null && id.length() == 11) ? id : null;
        } catch (Exception e) { return null; }
    }

    private String getIconForDestination(Destination d) {
        String c = d.getPays_destination().toLowerCase();
        String n = d.getNom_destination().toLowerCase();
        if (c.contains("france")      || n.contains("paris"))      return "🗼";
        if (c.contains("italie")      || n.contains("rome"))       return "🏛️";
        if (c.contains("tunisie")     || n.contains("djerba"))     return "🏖️";
        if (c.contains("suisse")      || n.contains("chamonix"))   return "🏔️";
        if (c.contains("indonésie")   || n.contains("bali"))       return "🏝️";
        if (c.contains("grèce")       || n.contains("athènes"))    return "🏛️";
        if (c.contains("espagne")     || n.contains("barcelone"))  return "💃";
        if (c.contains("japon")       || n.contains("tokyo"))      return "🗾";
        if (c.contains("egypte")      || n.contains("le caire"))   return "🐫";
        if (c.contains("maroc")       || n.contains("marrakech"))  return "🕌";
        if (c.contains("royaume-uni") || n.contains("london"))     return "🇬🇧";
        if (c.contains("états-unis")  || n.contains("new york"))   return "🗽";
        return "🌍";
    }

    private void handleVoirHebergements() {
        if (destination == null) { showAlert(Alert.AlertType.WARNING, "Attention", "Aucune destination sélectionnée"); return; }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/HebergementFront.fxml"));
            Parent root = loader.load();
            HebergementFrontController controller = loader.getController();
            controller.filterByDestination(destination);
            Stage stage = (Stage) btnVoirHebergements.getScene().getWindow();
            stage.setScene(new Scene(root, Screen.getPrimary().getVisualBounds().getWidth(), Screen.getPrimary().getVisualBounds().getHeight()));
            stage.setTitle("TravelMate - Hébergements à " + destination.getNom_destination());
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir les hébergements: " + e.getMessage());
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

