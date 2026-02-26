package Controlles;

import Entites.Activites;
import Services.GeocodingService;
import Services.NearbyPlacesAPI;
import Services.PlaceSuggestionService;
import Services.WeatherService;
import Services.WeatherService.WeatherForecast;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.layout.TilePane;  // Essentiel pour TilePane
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ActivityDetailController implements Initializable {

    @FXML private Label lblDate;
    @FXML private Button btnBack;
    @FXML private Button btnTranslate;
    @FXML private HBox btnVersCategories;
    @FXML private HBox btnVersActivites;
    @FXML private Label lblNomBreadcrumb;

    // Détails de l'activité
    @FXML private Label lblNom;
    @FXML private Label lblDescription;
    @FXML private Label lblBudget;
    @FXML private Label lblDuree;
    @FXML private Label lblDifficulte;
    @FXML private Label lblAgeMin;
    @FXML private Label lblStatut;
    @FXML private Label lblCategorie;
    @FXML private Label lblAdresseComplete;
    @FXML private ImageView imageView;

    // Localisation
    @FXML private WebView webViewMap;
    @FXML private Label lblVille;
    @FXML private Label lblPays;
    @FXML private Label lblCodePostal;
    @FXML private Label lblRue;
    @FXML private Label lblCoordonnees;
    @FXML private ProgressIndicator mapLoadingIndicator;

    // Météo
    @FXML private VBox weatherContainer;
    @FXML private Label lblDatePrevue;
    @FXML private Label lblWeatherIcon;
    @FXML private Label lblWeatherDescription;
    @FXML private Label lblWeatherTemp;
    @FXML private Label lblWeatherPrecip;
    @FXML private Label lblWeatherWind;
    @FXML private Label lblWeatherAdvice;

    // Suggestions - Version TilePane
    @FXML private TilePane suggestionsTilePane;
    @FXML private ScrollPane suggestionsScrollPane;
    @FXML private Label suggestionsTitle;

    private Activites activite;
    private WebEngine webEngine;
    private GeocodingService.LocationResult locationResult;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("🟢 ActivityDetailController initialisé");

        if (lblDate != null) {
            lblDate.setText("📅 " + LocalDate.now().format(
                    DateTimeFormatter.ofPattern("MMMM yyyy", Locale.FRENCH)));
        }

        if (webViewMap != null) {
            webEngine = webViewMap.getEngine();
            System.out.println("✅ WebEngine initialisé");
            webEngine.setJavaScriptEnabled(true);
        }
    }

    public void setActivite(Activites activite) {
        System.out.println("🟢 Activité reçue: " + activite.getNom());
        this.activite = activite;

        if (lblNomBreadcrumb != null) {
            lblNomBreadcrumb.setText(activite.getNom());
        }

        loadActivityDetails();
        loadLocation();
    }

    private void loadActivityDetails() {
        if (activite == null) return;

        lblNom.setText(activite.getNom() != null ? activite.getNom() : "");
        lblDescription.setText(activite.getDescription() != null ? activite.getDescription() : "");
        lblBudget.setText(activite.getBudget() + " €");

        if (activite.getDuree() > 0) {
            lblDuree.setText(activite.getDuree() + " heures");
        } else {
            lblDuree.setText("Non spécifié");
        }

        lblDifficulte.setText(activite.getNiveaudifficulte() != null ?
                activite.getNiveaudifficulte() : "Non spécifié");

        if (activite.getAgemin() > 0) {
            lblAgeMin.setText(activite.getAgemin() + " ans et plus");
        } else {
            lblAgeMin.setText("Tous âges");
        }

        lblStatut.setText(activite.getStatut() != null ? activite.getStatut() : "Non spécifié");

        if (activite.getCategorie() != null) {
            String catText = activite.getCategorie().getNom();
            if (activite.getCategorie().getType() != null) {
                catText += " (" + activite.getCategorie().getType() + ")";
            }
            lblCategorie.setText(catText);
        } else {
            lblCategorie.setText("Non catégorisé");
        }

        lblAdresseComplete.setText(activite.getLieu() != null ? activite.getLieu() : "");

        // Charger l'image
        if (activite.getImagePath() != null && !activite.getImagePath().isEmpty()) {
            try {
                File file = new File(activite.getImagePath());
                if (file.exists()) {
                    Image image = new Image(file.toURI().toString(), 600, 300, true, true);
                    imageView.setImage(image);
                }
            } catch (Exception e) {
                System.err.println("Erreur chargement image: " + e.getMessage());
            }
        }
    }

    private void loadLocation() {
        if (activite == null || activite.getLieu() == null || activite.getLieu().isEmpty()) {
            showMapWithMessage("Adresse non disponible");
            return;
        }

        mapLoadingIndicator.setVisible(true);

        new Thread(() -> {
            GeocodingService.LocationResult result = GeocodingService.geocode(activite.getLieu());

            javafx.application.Platform.runLater(() -> {
                mapLoadingIndicator.setVisible(false);

                if (result != null) {
                    locationResult = result;

                    lblVille.setText(result.getCity().isEmpty() ? "Non spécifié" : result.getCity());
                    lblPays.setText(result.getCountry().isEmpty() ? "Non spécifié" : result.getCountry());
                    lblCodePostal.setText(result.getPostcode().isEmpty() ? "Non spécifié" : result.getPostcode());
                    lblRue.setText(result.getStreet().isEmpty() ? "Non spécifié" : result.getStreet());

                    String coords = String.format("%.4f, %.4f", result.getLatitude(), result.getLongitude());
                    lblCoordonnees.setText(coords);

                    displayMap(result);
                    loadWeatherForActivity();

                    // Charger les suggestions à proximité
                    loadSuggestions();

                } else {
                    showMapWithMessage("Impossible de localiser cette adresse");

                    lblVille.setText("Non trouvé");
                    lblPays.setText("Non trouvé");
                    lblCodePostal.setText("Non trouvé");
                    lblRue.setText("Non trouvé");
                    lblCoordonnees.setText("Non trouvé");
                }
            });
        }).start();
    }

    // ==================== SUGGESTIONS À PROXIMITÉ ====================

    private void loadSuggestions() {
        if (suggestionsTilePane == null || activite == null) return;

        // Afficher le chargement
        javafx.application.Platform.runLater(() -> {
            suggestionsTilePane.getChildren().clear();
            for (int i = 0; i < 4; i++) {
                VBox skeleton = new VBox();
                skeleton.setPrefWidth(200);
                skeleton.setPrefHeight(150);
                skeleton.setStyle("-fx-background-color: linear-gradient(to right, #f0f0f0 0%, #e0e0e0 50%, #f0f0f0 100%); -fx-background-radius: 12;");
                suggestionsTilePane.getChildren().add(skeleton);
            }
        });

        // Utiliser le service dans un thread séparé
        new Thread(() -> {
            List<Map<String, Object>> places = PlaceSuggestionService.getSuggestionsForActivity(activite);

            javafx.application.Platform.runLater(() -> {
                suggestionsTilePane.getChildren().clear();

                if (places.isEmpty()) {
                    showNoSuggestions();
                } else {
                    displaySuggestions(places);
                }
            });
        }).start();
    }

    private void displaySuggestions(List<Map<String, Object>> places) {
        suggestionsTilePane.getChildren().clear();

        if (suggestionsTitle != null && activite != null) {
            suggestionsTitle.setText("À proximité de " +
                    (activite.getLieu() != null ? activite.getLieu() : "cette activité"));
        }

        for (Map<String, Object> place : places) {
            VBox card = createSuggestionCard(place);
            suggestionsTilePane.getChildren().add(card);
        }
    }

    private VBox createSuggestionCard(Map<String, Object> place) {
        VBox card = new VBox(8);
        card.setPrefWidth(200);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 12; " +
                "-fx-border-color: #f0f0f0; -fx-border-radius: 12; -fx-cursor: hand;");
        card.setAlignment(javafx.geometry.Pos.TOP_CENTER);

        String name = (String) place.get("name");
        String type = (String) place.get("type");
        double distance = (double) place.get("distance");

        // Icône selon le type
        String icon = switch(type) {
            case "museum" -> "🏛️";
            case "restaurant" -> "🍽️";
            case "cafe" -> "☕";
            case "park" -> "🌳";
            case "monument" -> "🗿";
            case "theatre" -> "🎭";
            case "cinema" -> "🎬";
            case "hotel" -> "🏨";
            case "shop" -> "🛍️";
            default -> "📍";
        };

        HBox header = new HBox(8);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 24;");

        String typeDisplay = switch(type) {
            case "museum" -> "Musée";
            case "restaurant" -> "Restaurant";
            case "cafe" -> "Café";
            case "park" -> "Parc";
            case "monument" -> "Monument";
            case "theatre" -> "Théâtre";
            case "cinema" -> "Cinéma";
            case "hotel" -> "Hôtel";
            case "shop" -> "Boutique";
            default -> "Lieu d'intérêt";
        };

        Label typeLabel = new Label(typeDisplay);
        typeLabel.setStyle("-fx-background-color: #fff3e8; -fx-text-fill: #ff6b00; " +
                "-fx-background-radius: 10; -fx-padding: 4 10; -fx-font-size: 11; -fx-font-weight: bold;");

        header.getChildren().addAll(iconLabel, typeLabel);

        Label nameLabel = new Label(name);
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14; -fx-text-fill: #1a1a1a;");
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(180);

        String distanceText = distance < 1 ?
                String.format("📍 %.0f m", distance * 1000) :
                String.format("📍 %.1f km", distance);
        Label distanceLabel = new Label(distanceText);
        distanceLabel.setStyle("-fx-text-fill: #888; -fx-font-size: 11;");

        Label viewButton = new Label("Voir sur la carte →");
        viewButton.setStyle("-fx-text-fill: #ff6b00; -fx-font-size: 11; -fx-font-weight: bold; " +
                "-fx-cursor: hand; -fx-padding: 8 0 0 0;");
        viewButton.setOnMouseClicked(e -> centerMapOnPlace(place));

        card.getChildren().addAll(header, nameLabel, distanceLabel, viewButton);

        // Effet de survol
        card.setOnMouseEntered(e -> {
            card.setEffect(new DropShadow(12, Color.web("#ff6b0055")));
            card.setScaleX(1.02);
            card.setScaleY(1.02);
        });
        card.setOnMouseExited(e -> {
            card.setEffect(null);
            card.setScaleX(1.0);
            card.setScaleY(1.0);
        });

        return card;
    }

    private void centerMapOnPlace(Map<String, Object> place) {
        double lat = (double) place.get("lat");
        double lon = (double) place.get("lon");

        String html = "<!DOCTYPE html><html><head><meta charset='UTF-8'><style>"
                + "body { margin:0; padding:0; }"
                + "iframe { width:100%; height:100%; border:none; }"
                + "</style></head><body>"
                + "<iframe src='https://www.openstreetmap.org/export/embed.html?bbox="
                + (lon - 0.005) + "%2C" + (lat - 0.005) + "%2C"
                + (lon + 0.005) + "%2C" + (lat + 0.005)
                + "&amp;layer=mapnik&amp;marker=" + lat + "%2C" + lon + "'></iframe>"
                + "</body></html>";

        webEngine.loadContent(html);

        // Mettre à jour les coordonnées affichées
        lblCoordonnees.setText(String.format("%.4f, %.4f", lat, lon));
    }

    private void showNoSuggestions() {
        VBox messageBox = new VBox(10);
        messageBox.setAlignment(javafx.geometry.Pos.CENTER);
        messageBox.setPrefWidth(800);
        messageBox.setStyle("-fx-padding: 30;");

        Label icon = new Label("📍");
        icon.setStyle("-fx-font-size: 48;");

        Label message = new Label("Aucun lieu à proximité trouvé pour le moment");
        message.setStyle("-fx-text-fill: #888; -fx-font-size: 14;");

        Label subMessage = new Label("Explorez d'autres activités ou élargissez votre recherche");
        subMessage.setStyle("-fx-text-fill: #aaa; -fx-font-size: 12;");

        messageBox.getChildren().addAll(icon, message, subMessage);
        suggestionsTilePane.getChildren().add(messageBox);
    }

    private void displayMap(GeocodingService.LocationResult result) {
        if (webEngine == null) return;

        String html = "<!DOCTYPE html><html><head><meta charset='UTF-8'><style>"
                + "body { margin:0; padding:0; }"
                + "iframe { width:100%; height:100%; border:none; }"
                + "</style></head><body>"
                + "<iframe src='https://www.openstreetmap.org/export/embed.html?bbox="
                + (result.getLongitude() - 0.01) + "%2C" + (result.getLatitude() - 0.01) + "%2C"
                + (result.getLongitude() + 0.01) + "%2C" + (result.getLatitude() + 0.01)
                + "&amp;layer=mapnik&amp;marker=" + result.getLatitude() + "%2C" + result.getLongitude() + "'></iframe>"
                + "</body></html>";

        webEngine.loadContent(html);
    }

    private void showMapWithMessage(String message) {
        if (webEngine == null) return;

        String html = "<!DOCTYPE html><html><head><style>"
                + "body { margin:0; padding:0; display:flex; justify-content:center; align-items:center; height:100%; font-family:Arial; color:#666; background:#f5f5f5; }"
                + "</style></head><body>"
                + "<div style='text-align:center;'><div style='font-size:48px; margin-bottom:20px;'>📍</div>"
                + "<div style='font-size:16px;'>" + message + "</div></div>"
                + "</body></html>";

        webEngine.loadContent(html);
    }

    private void loadWeatherForActivity() {
        if (activite.getDatePrevue() == null || locationResult == null) {
            weatherContainer.setVisible(false);
            return;
        }

        String dateFormatted = activite.getDatePrevue().format(
                DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.FRENCH));
        lblDatePrevue.setText("Prévisions pour le " + dateFormatted);

        weatherContainer.setVisible(true);
        lblWeatherIcon.setText("⏳");
        lblWeatherDescription.setText("Chargement...");

        new Thread(() -> {
            WeatherForecast forecast = WeatherService.getForecast(
                    locationResult.getLatitude(),
                    locationResult.getLongitude(),
                    activite.getDatePrevue()
            );

            javafx.application.Platform.runLater(() -> {
                if (forecast != null) {
                    displayWeather(forecast);
                } else {
                    showWeatherUnavailable();
                }
            });
        }).start();
    }

    private void displayWeather(WeatherForecast forecast) {
        lblWeatherIcon.setText(forecast.getWeatherIcon());
        lblWeatherDescription.setText(forecast.getWeatherDescription());
        lblWeatherTemp.setText(forecast.getFormattedTemp());
        lblWeatherPrecip.setText(String.format("💧 %.1f mm", forecast.getPrecipitation()));
        lblWeatherWind.setText(String.format("💨 %.0f km/h", forecast.getWindSpeed()));

        String advice = getWeatherAdvice(forecast);
        lblWeatherAdvice.setText(advice);
    }

    private void showWeatherUnavailable() {
        lblWeatherIcon.setText("🌡️");
        lblWeatherDescription.setText("Prévisions non disponibles");
        lblWeatherTemp.setText("—");
        lblWeatherPrecip.setText("—");
        lblWeatherWind.setText("—");
        lblWeatherAdvice.setText("ℹ️ Météo non disponible pour cette date");
    }

    private String getWeatherAdvice(WeatherForecast forecast) {
        if (forecast.getPrecipitation() > 5) {
            return "☔ Pluie prévue - Prévoyez un parapluie !";
        } else if (forecast.getTemperatureMax() > 30) {
            return "☀️ Forte chaleur - Hydratez-vous !";
        } else if (forecast.getTemperatureMin() < 5) {
            return "❄️ Temps froid - Habillez-vous chaudement !";
        } else {
            return "🌤️ Conditions idéales pour cette activité !";
        }
    }

    // ==================== NAVIGATION ====================

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/activitesfront.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) btnBack.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("TravelMate - Activités");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleVersCategories() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/categoriesfront.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) btnVersCategories.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("TravelMate - Catégories");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleVersActivites() {
        handleBack();
    }

    @FXML
    private void handleTranslate() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Traduction");
        alert.setHeaderText(null);
        alert.setContentText("Fonctionnalité de traduction à implémenter");
        alert.showAndWait();
    }
}