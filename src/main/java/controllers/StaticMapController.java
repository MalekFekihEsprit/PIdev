package controllers;

import entities.Destination;
import services.StaticMapService;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class StaticMapController {

    private final List<Destination> destinations;
    private Stage stage;
    private ImageView mapImageView;
    private ProgressIndicator loadingIndicator;
    private Label statusLabel;

    public StaticMapController(List<Destination> destinations) {
        this.destinations = destinations;
    }

    public void show() {
        stage = new Stage();
        stage.setTitle("TravelMate - Carte des Destinations");
        stage.setMinWidth(900);
        stage.setMinHeight(700);

        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        stage.setX((screenBounds.getWidth() - 1000) / 2);
        stage.setY((screenBounds.getHeight() - 800) / 2);
        stage.setWidth(1000);
        stage.setHeight(800);

        // Create UI components
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #0a0e27;");

        // Top bar with info
        HBox topBar = createTopBar();
        root.setTop(topBar);

        // Center - map image with scroll pane
        VBox centerBox = new VBox(10);
        centerBox.setStyle("-fx-padding: 20; -fx-alignment: center;");

        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setVisible(true);
        loadingIndicator.setPrefSize(50, 50);

        mapImageView = new ImageView();
        mapImageView.setPreserveRatio(true);
        mapImageView.setSmooth(true);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(mapImageView);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-border-color: #ff8c42; -fx-border-width: 2; -fx-border-radius: 10;");

        statusLabel = new Label("Chargement de la carte...");
        statusLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 14;");

        centerBox.getChildren().addAll(loadingIndicator, statusLabel, scrollPane);
        root.setCenter(centerBox);

        // Bottom bar with count
        HBox bottomBar = createBottomBar();
        root.setBottom(bottomBar);

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();

        // Load map asynchronously
        loadMapAsync();
    }

    private HBox createTopBar() {
        HBox topBar = new HBox(10);
        topBar.setStyle("-fx-background-color: #111633; -fx-padding: 15 20; -fx-border-color: #1e2749; -fx-border-width: 0 0 1 0;");

        Label titleLabel = new Label("🗺️ Carte des Destinations");
        titleLabel.setStyle("-fx-font-family: 'Clash Display'; -fx-font-size: 20; -fx-font-weight: bold; -fx-text-fill: #ffffff;");

        // Count valid destinations
        long validCount = destinations.stream()
                .filter(d -> d.getLatitude_destination() != 0.0 || d.getLongitude_destination() != 0.0)
                .count();

        Label countLabel = new Label(validCount + " destinations");
        countLabel.setStyle("-fx-background-color: rgba(255,140,66,0.15); -fx-text-fill: #ff8c42; -fx-background-radius: 20; -fx-padding: 4 12; -fx-font-weight: 600;");

        topBar.getChildren().addAll(titleLabel, countLabel);
        return topBar;
    }

    private HBox createBottomBar() {
        HBox bottomBar = new HBox(20);
        bottomBar.setStyle("-fx-background-color: #111633; -fx-padding: 10 20; -fx-border-color: #1e2749; -fx-border-width: 1 0 0 0;");

        Label legend1 = new Label("🟠 Destination");
        legend1.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12;");

        Label legend2 = new Label("📍 Carte statique © OpenStreetMap");
        legend2.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12;");

        bottomBar.getChildren().addAll(legend1, legend2);
        return bottomBar;
    }

    private void loadMapAsync() {
        CompletableFuture.supplyAsync(() -> {
            try {
                // Build the static map URL
                String mapUrl = buildStaticMapUrl();
                System.out.println("Loading map from: " + mapUrl);

                // Download image directly as JavaFX Image
                URL url = new URL(mapUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("User-Agent", "TravelMate-App/1.0");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                if (conn.getResponseCode() == 200) {
                    InputStream inputStream = conn.getInputStream();
                    return new Image(inputStream);
                }
                return null;

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }).thenAccept(image -> {
            javafx.application.Platform.runLater(() -> {
                loadingIndicator.setVisible(false);
                if (image != null && !image.isError()) {
                    mapImageView.setImage(image);
                    statusLabel.setVisible(false);
                } else {
                    statusLabel.setText("❌ Erreur de chargement de la carte");
                    statusLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 14;");
                }
            });
        });
    }

    private String buildStaticMapUrl() {
        StringBuilder url = new StringBuilder("https://staticmap.openstreetmap.de/staticmap.php?size=1800x1200&maptype=mapnik&format=png");

        // Add markers for each destination
        StringBuilder markers = new StringBuilder();
        int markerIndex = 1;

        for (Destination d : destinations) {
            if (d.getLatitude_destination() != 0.0 || d.getLongitude_destination() != 0.0) {
                if (markers.length() > 0) {
                    markers.append("|");
                }
                // Add marker with label
                markers.append(String.format("%.6f,%.6f,%s",
                        d.getLatitude_destination(),
                        d.getLongitude_destination(),
                        markerIndex));
                markerIndex++;
            }
        }

        if (markers.length() > 0) {
            url.append("&markers=").append(markers.toString());
        } else {
            // Default center if no markers
            url.append("&center=46.0,2.0&zoom=4");
        }

        return url.toString();
    }

    public void close() {
        if (stage != null) {
            stage.close();
        }
    }
}