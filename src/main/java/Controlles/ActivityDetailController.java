package Controlles;

import Entites.Activites;
import Services.ActivitesCRUD;
import Services.GeocodingService;
import Services.WeatherService;
import Services.WeatherService.WeatherForecast;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.ResourceBundle;

public class ActivityDetailController implements Initializable {

    @FXML private Label lblDate;
    @FXML private Button btnBack;
    @FXML private Button btnTranslate;
    @FXML private HBox btnVersCategories;
    @FXML private HBox btnVersActivites;

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

    private Activites activite;
    private WebEngine webEngine;
    private GeocodingService.LocationResult locationResult;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("🟢 ActivityDetailController initialisé");

        // Date du jour
        if (lblDate != null) {
            lblDate.setText("📅 " + LocalDate.now().format(
                    DateTimeFormatter.ofPattern("MMMM yyyy", Locale.FRENCH)));
        }

        // Initialiser WebEngine
        if (webViewMap != null) {
            webEngine = webViewMap.getEngine();
            System.out.println("✅ WebEngine initialisé");
            webEngine.setJavaScriptEnabled(true);
        }
    }

    public void setActivite(Activites activite) {
        System.out.println("🟢 Activité reçue: " + activite.getNom());
        this.activite = activite;
        loadActivityDetails();
        loadLocation();
    }

    private void loadActivityDetails() {
        if (activite == null) {
            System.out.println("❌ activite est null");
            return;
        }

        System.out.println("📝 Chargement des détails pour: " + activite.getNom());
        System.out.println("   Date prévue: " + activite.getDatePrevue());

        // Mettre à jour les labels
        lblNom.setText(activite.getNom() != null ? activite.getNom() : "");
        lblDescription.setText(activite.getDescription() != null ? activite.getDescription() : "");
        lblBudget.setText(activite.getBudget() + " €");

        if (activite.getDuree() > 0) {
            lblDuree.setText(activite.getDuree() + " heures");
        } else {
            lblDuree.setText("Non spécifié");
        }

        if (activite.getNiveaudifficulte() != null && !activite.getNiveaudifficulte().isEmpty()) {
            lblDifficulte.setText(activite.getNiveaudifficulte());
        } else {
            lblDifficulte.setText("Non spécifié");
        }

        if (activite.getAgemin() > 0) {
            lblAgeMin.setText(activite.getAgemin() + " ans et plus");
        } else {
            lblAgeMin.setText("Tous âges");
        }

        if (activite.getStatut() != null && !activite.getStatut().isEmpty()) {
            lblStatut.setText(activite.getStatut());
        } else {
            lblStatut.setText("Non spécifié");
        }

        // Afficher la catégorie
        if (activite.getCategorie() != null) {
            String catText = activite.getCategorie().getNom();
            if (activite.getCategorie().getType() != null && !activite.getCategorie().getType().isEmpty()) {
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
                    Image image = new Image(file.toURI().toString(), 400, 250, true, true);
                    imageView.setImage(image);
                    System.out.println("🖼️ Image chargée: " + activite.getImagePath());
                } else {
                    System.out.println("⚠️ Fichier image non trouvé: " + activite.getImagePath());
                }
            } catch (Exception e) {
                System.err.println("Erreur chargement image: " + e.getMessage());
            }
        }
    }

    private void loadLocation() {
        if (activite == null || activite.getLieu() == null || activite.getLieu().isEmpty()) {
            System.out.println("❌ Pas d'adresse pour cette activité");
            showMapWithMessage("Adresse non disponible");
            return;
        }

        System.out.println("🗺️ Début du géocodage pour: " + activite.getLieu());
        mapLoadingIndicator.setVisible(true);

        // Lancer le géocodage dans un thread séparé
        new Thread(() -> {
            System.out.println("🔄 Thread géocodage démarré");
            GeocodingService.LocationResult result = GeocodingService.geocode(activite.getLieu());

            javafx.application.Platform.runLater(() -> {
                System.out.println("🔄 Retour sur le thread JavaFX");
                mapLoadingIndicator.setVisible(false);

                if (result != null) {
                    System.out.println("✅ Résultat géocodage trouvé!");
                    System.out.println("   Lat: " + result.getLatitude());
                    System.out.println("   Lon: " + result.getLongitude());

                    locationResult = result; // Stocker pour la météo

                    // Mettre à jour les labels
                    lblVille.setText(result.getCity() != null && !result.getCity().isEmpty() ?
                            result.getCity() : "Non spécifié");
                    lblPays.setText(result.getCountry() != null && !result.getCountry().isEmpty() ?
                            result.getCountry() : "Non spécifié");
                    lblCodePostal.setText(result.getPostcode() != null && !result.getPostcode().isEmpty() ?
                            result.getPostcode() : "Non spécifié");
                    lblRue.setText(result.getStreet() != null && !result.getStreet().isEmpty() ?
                            result.getStreet() : "Non spécifié");

                    String coords = String.format("%.4f, %.4f",
                            result.getLatitude(), result.getLongitude());
                    lblCoordonnees.setText(coords);

                    System.out.println("✅ Labels mis à jour");

                    displayMap(result);

                    // Charger la météo
                    loadWeatherForActivity();

                } else {
                    System.out.println("❌ Aucun résultat de géocodage");
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

    private void displayMap(GeocodingService.LocationResult result) {
        System.out.println("🗺️ Affichage de la carte");

        if (webEngine == null) {
            System.err.println("❌ WebEngine non initialisé!");
            return;
        }

        String html = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <style>\n" +
                "        * { margin: 0; padding: 0; box-sizing: border-box; }\n" +
                "        html, body { width: 100%; height: 100%; overflow: hidden; }\n" +
                "        iframe { width: 100%; height: 100%; border: none; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <iframe \n" +
                "        src='https://www.openstreetmap.org/export/embed.html?bbox=" + (result.getLongitude() - 0.01) + "%2C" + (result.getLatitude() - 0.01) + "%2C" + (result.getLongitude() + 0.01) + "%2C" + (result.getLatitude() + 0.01) + "&amp;layer=mapnik&amp;marker=" + result.getLatitude() + "%2C" + result.getLongitude() + "'\n" +
                "        style='border: 1px solid black'>\n" +
                "    </iframe>\n" +
                "</body>\n" +
                "</html>";

        webEngine.loadContent(html);
        System.out.println("✅ Carte chargée en iframe");
    }

    private void showMapWithMessage(String message) {
        System.out.println("ℹ️ Message carte: " + message);

        if (webEngine == null) {
            System.err.println("❌ WebEngine non initialisé!");
            return;
        }

        String html = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <style>\n" +
                "        body { \n" +
                "            margin: 0; \n" +
                "            padding: 0; \n" +
                "            display: flex;\n" +
                "            justify-content: center;\n" +
                "            align-items: center;\n" +
                "            height: 100%;\n" +
                "            font-family: Arial, sans-serif;\n" +
                "            color: #666;\n" +
                "            background: #f5f5f5;\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div style=\"text-align: center;\">\n" +
                "        <div style=\"font-size: 48px; margin-bottom: 20px;\">📍</div>\n" +
                "        <div style=\"font-size: 16px;\">" + message + "</div>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";

        webEngine.loadContent(html);
    }

    private void loadWeatherForActivity() {
        System.out.println("🔍 Tentative de chargement météo");
        System.out.println("   Date activité: " + activite.getDatePrevue());
        System.out.println("   Location result: " + (locationResult != null ? "OK" : "NULL"));

        if (activite.getDatePrevue() == null) {
            System.out.println("⚠️ Pas de date prévue pour cette activité");
            weatherContainer.setVisible(false);
            return;
        }

        if (locationResult == null) {
            System.out.println("⚠️ Pas de localisation disponible");
            showWeatherUnavailable("Localisation inconnue");
            return;
        }

        // Afficher la date
        String dateFormatted = activite.getDatePrevue().format(
                DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.FRENCH));
        lblDatePrevue.setText("Prévisions pour le " + dateFormatted);

        // Afficher le chargement
        weatherContainer.setVisible(true);
        lblWeatherIcon.setText("⏳");
        lblWeatherDescription.setText("Chargement météo...");
        lblWeatherTemp.setText("—");
        lblWeatherPrecip.setText("—");
        lblWeatherWind.setText("—");

        // Lancer dans un thread séparé
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
                    showWeatherUnavailable("Prévisions non disponibles (date trop lointaine?)");
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

        // Ajouter un conseil selon la météo
        String advice = getWeatherAdvice(forecast);
        lblWeatherAdvice.setText(advice);

        // Colorer selon les conditions
        if (forecast.getPrecipitation() > 5) {
            lblWeatherPrecip.setStyle("-fx-text-fill: #3b82f6; -fx-font-weight: bold;");
        } else {
            lblWeatherPrecip.setStyle("-fx-text-fill: #555;");
        }

        if (forecast.getWindSpeed() > 30) {
            lblWeatherWind.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
        } else {
            lblWeatherWind.setStyle("-fx-text-fill: #555;");
        }
    }

    private void showWeatherUnavailable(String message) {
        lblWeatherIcon.setText("🌡️");
        lblWeatherDescription.setText(message);
        lblWeatherTemp.setText("—");
        lblWeatherPrecip.setText("—");
        lblWeatherWind.setText("—");
        lblWeatherAdvice.setText("ℹ️ " + message);
    }

    private String getWeatherAdvice(WeatherForecast forecast) {
        if (forecast.getPrecipitation() > 10) {
            return "☔ Fortes pluies prévues - Prévoyez un équipement imperméable !";
        } else if (forecast.getPrecipitation() > 2) {
            return "🌦️ Quelques averses possibles - Un parapluie peut être utile";
        } else if (forecast.getWindSpeed() > 40) {
            return "💨 Vents forts - Attention aux activités en extérieur";
        } else if (forecast.getTemperatureMax() > 30) {
            return "☀️ Forte chaleur - Pensez à vous hydrater et vous protéger du soleil";
        } else if (forecast.getTemperatureMin() < 5) {
            return "❄️ Températures froides - Habillez-vous chaudement !";
        } else if (forecast.getWeatherCode() == 0 || forecast.getWeatherCode() == 1) {
            return "☀️ Conditions idéales pour profiter de cette activité !";
        } else {
            return "🌤️ Conditions météo acceptables pour l'activité";
        }
    }

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