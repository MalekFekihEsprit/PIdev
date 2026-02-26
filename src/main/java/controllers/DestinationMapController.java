package controllers;

import entities.Destination;
import javafx.concurrent.Worker;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.List;

public class DestinationMapController {

    private final List<Destination> destinations;
    private Stage stage;
    private WebView webView;
    private WebEngine webEngine;

    /**
     * Constructor that takes a list of destinations
     * @param destinations List of destinations to display on the map
     */
    public DestinationMapController(List<Destination> destinations) {
        this.destinations = destinations;
    }

    /**
     * Shows the map window
     */
    public void show() {
        stage = new Stage();
        stage.setTitle("TravelMate - Carte des Destinations");
        stage.setMinWidth(800);
        stage.setMinHeight(600);

        // Center the window on screen
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        stage.setX((screenBounds.getWidth() - 900) / 2);
        stage.setY((screenBounds.getHeight() - 700) / 2);
        stage.setWidth(900);
        stage.setHeight(700);

        // Create WebView
        webView = new WebView();
        webEngine = webView.getEngine();

        // Load the HTML file from resources
        loadHtmlFile();

        // Set up the scene
        Scene scene = new Scene(webView);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Loads the map.html file from resources
     */
    private void loadHtmlFile() {
        try {
            // Get the URL of the HTML file in resources
            java.net.URL url = getClass().getResource("/map.html");
            if (url == null) {
                System.err.println("Erreur: map.html non trouvé dans les ressources!");
                return;
            }

            // Load the HTML file
            webEngine.load(url.toExternalForm());

            // Wait for the page to finish loading before adding markers
            webEngine.getLoadWorker().stateProperty().addListener(
                    (observable, oldValue, newValue) -> {
                        if (newValue == Worker.State.SUCCEEDED) {
                            // Page loaded successfully, now inject markers
                            injectMarkers();
                        }
                    }
            );

        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de map.html: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Injects markers into the JavaScript map
     */
    private void injectMarkers() {
        int markerCount = 0;

        for (Destination dest : destinations) {
            // Only add markers with valid coordinates (non-zero)
            if (dest.getLatitude_destination() != 0.0 || dest.getLongitude_destination() != 0.0) {
                double lat = dest.getLatitude_destination();
                double lng = dest.getLongitude_destination();
                String name = dest.getNom_destination() + ", " + dest.getPays_destination();

                // Escape the name for JavaScript
                String escapedName = escapeJavaScriptString(name);

                // Execute JavaScript to add marker
                try {
                    webEngine.executeScript("addMarker(" + lat + ", " + lng + ", '" + escapedName + "')");
                    markerCount++;
                } catch (Exception e) {
                    System.err.println("Échec d'ajout du marqueur pour: " + name);
                    e.printStackTrace();
                }
            }
        }

        System.out.println("Ajout de " + markerCount + " marqueurs sur la carte");

        // Update window title with count
        if (stage != null) {
            stage.setTitle("TravelMate - Carte des Destinations (" + markerCount + " destinations)");
        }
    }

    /**
     * Escapes a string to be safely used in JavaScript
     * @param input The string to escape
     * @return Escaped string safe for JavaScript
     */
    private String escapeJavaScriptString(String input) {
        if (input == null) return "";

        return input.replace("\\", "\\\\")
                .replace("'", "\\'")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * Closes the map window
     */
    public void close() {
        if (stage != null) {
            stage.close();
        }
    }
}