package Controllers;

import Services.GeocodingService;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;

import java.net.URL;
import java.util.ResourceBundle;

public class LocationPickerController implements Initializable {

    @FXML private WebView webViewMap;
    @FXML private TextField searchField;
    @FXML private Button btnSearch;
    @FXML private Button btnConfirm;
    @FXML private Button btnCancel;
    @FXML private Label lblSelectedLocation;
    @FXML private ProgressIndicator loadingIndicator;

    private WebEngine webEngine;
    private double selectedLat = 0;
    private double selectedLon = 0;
    private String selectedAddress = "";
    private Runnable onLocationSelected;

    private static final String MAP_HTML = """
    <!DOCTYPE html>
    <html>
    <head>
        <meta charset="UTF-8">
        <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
        <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
        <style>
            * { margin: 0; padding: 0; box-sizing: border-box; }
            html, body { width: 100%; height: 100%; overflow: hidden; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; }
            #map { width: 100%; height: 100%; position: absolute; top: 0; left: 0; z-index: 1; }
            .custom-marker {
                background: #ff8c42;
                border: 3px solid white;
                border-radius: 50%;
                width: 20px !important;
                height: 20px !important;
                box-shadow: 0 0 15px rgba(255,140,66,0.5);
            }
            .custom-marker.pulse {
                animation: pulse 1.5s infinite;
            }
            @keyframes pulse {
                0% { box-shadow: 0 0 0 0 rgba(255,140,66,0.7); }
                70% { box-shadow: 0 0 0 15px rgba(255,140,66,0); }
                100% { box-shadow: 0 0 0 0 rgba(255,140,66,0); }
            }
            .coordinates-info {
                position: absolute;
                bottom: 20px;
                right: 20px;
                background: rgba(0,0,0,0.8);
                color: white;
                padding: 8px 15px;
                border-radius: 20px;
                font-size: 12px;
                border-left: 4px solid #ff8c42;
                z-index: 1000;
                font-family: monospace;
            }
        </style>
    </head>
    <body>
        <div id="map"></div>
        <div id="coords" class="coordinates-info">Cliquez sur la carte</div>

        <script>
            let map;
            let marker;
            let currentLat = 36.8065; // Tunis par défaut
            let currentLon = 10.1815;

            function initMap() {
                map = L.map('map').setView([currentLat, currentLon], 12);

                L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                    attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>',
                    maxZoom: 19
                }).addTo(map);

                marker = L.marker([currentLat, currentLon], {
                    draggable: true,
                    icon: L.divIcon({
                        className: 'custom-marker pulse',
                        iconSize: [20, 20]
                    })
                }).addTo(map);

                updateCoordinates(currentLat, currentLon);
                reverseGeocode(currentLat, currentLon);

                marker.on('dragend', function(e) {
                    const pos = e.target.getLatLng();
                    updateCoordinates(pos.lat, pos.lng);
                    reverseGeocode(pos.lat, pos.lng);
                });

                map.on('click', function(e) {
                    marker.setLatLng(e.latlng);
                    updateCoordinates(e.latlng.lat, e.latlng.lng);
                    reverseGeocode(e.latlng.lat, e.latlng.lng);
                });
            }

            function updateCoordinates(lat, lon) {
                document.getElementById('coords').innerHTML = 
                    `📍 ${lat.toFixed(6)}, ${lon.toFixed(6)}`;
                
                // Notifier Java
                if (window.javaConnector) {
                    window.javaConnector.onLocationChanged(lat, lon);
                }
            }

            function reverseGeocode(lat, lon) {
                const url = `https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lon}&addressdetails=1&accept-language=fr`;
                
                fetch(url, {
                    headers: {
                        'User-Agent': 'TravelMate/1.0'
                    }
                })
                .then(response => response.json())
                .then(data => {
                    if (data.display_name) {
                        // Mettre à jour le champ de recherche JavaFX via le connector
                        if (window.javaConnector) {
                            window.javaConnector.onAddressFound(data.display_name);
                        }
                    }
                })
                .catch(error => {
                    console.error('Erreur reverse geocoding:', error);
                });
            }

            function searchAddress(query) {
                if (!query) return;

                const url = `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(query)}&limit=1&addressdetails=1&accept-language=fr`;
                
                fetch(url, {
                    headers: {
                        'User-Agent': 'TravelMate/1.0'
                    }
                })
                .then(response => response.json())
                .then(data => {
                    if (data && data.length > 0) {
                        const result = data[0];
                        const lat = parseFloat(result.lat);
                        const lon = parseFloat(result.lon);
                        
                        map.setView([lat, lon], 15);
                        marker.setLatLng([lat, lon]);
                        updateCoordinates(lat, lon);
                        
                        if (result.display_name && window.javaConnector) {
                            window.javaConnector.onAddressFound(result.display_name);
                        }
                    } else {
                        alert('Aucune adresse trouvée');
                    }
                })
                .catch(error => {
                    console.error('Erreur recherche:', error);
                    alert('Erreur lors de la recherche');
                });
            }

            // Fonction appelée depuis Java pour effectuer une recherche
            window.performSearch = function(query) {
                searchAddress(query);
            };

            // Initialisation au chargement
            window.onload = function() {
                initMap();
            };
        </script>
    </body>
    </html>
    """;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        webEngine = webViewMap.getEngine();
        webEngine.setJavaScriptEnabled(true);

        // Attendre que la page soit chargée
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                // Créer le pont Java <-> JavaScript
                JSObject window = (JSObject) webEngine.executeScript("window");
                window.setMember("javaConnector", new JavaConnector());

                // Forcer le redimensionnement de la carte
                webEngine.executeScript(
                        "setTimeout(function() { " +
                                "   if (map) map.invalidateSize(); " +
                                "}, 200);"
                );
            }
        });

        webEngine.loadContent(MAP_HTML);

        btnConfirm.setDisable(true);
    }

    /**
     * Classe interne pour la communication Java <-> JavaScript
     */
    public class JavaConnector {
        public void onLocationChanged(double lat, double lon) {
            Platform.runLater(() -> {
                selectedLat = lat;
                selectedLon = lon;
                btnConfirm.setDisable(false);
            });
        }

        public void onAddressFound(String address) {
            Platform.runLater(() -> {
                selectedAddress = address;
                searchField.setText(address); // Mettre à jour le champ de recherche
                lblSelectedLocation.setText("📍 " + address);
            });
        }
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText();
        if (query != null && !query.trim().isEmpty()) {
            loadingIndicator.setVisible(true);
            // Appeler la fonction JavaScript de recherche
            webEngine.executeScript("window.performSearch('" + query.replace("'", "\\'") + "')");
            loadingIndicator.setVisible(false);
        }
    }

    @FXML
    private void handleConfirm() {
        if (onLocationSelected != null && selectedLat != 0 && selectedLon != 0) {
            LocationResult result = new LocationResult();
            result.setLatitude(selectedLat);
            result.setLongitude(selectedLon);
            result.setDisplayName(selectedAddress);

            // Obtenir les détails de l'adresse via reverse geocoding
            new Thread(() -> {
                GeocodingService.LocationResult details = GeocodingService.reverseGeocode(selectedLat, selectedLon);
                Platform.runLater(() -> {
                    if (details != null) {
                        result.setCity(details.getCity());
                        result.setCountry(details.getCountry());
                        result.setPostcode(details.getPostcode());
                        result.setStreet(details.getStreet());
                    }
                    onLocationSelected.run();
                    ((Stage) btnConfirm.getScene().getWindow()).close();
                });
            }).start();
        }
    }

    @FXML
    private void handleCancel() {
        ((Stage) btnCancel.getScene().getWindow()).close();
    }

    public void setOnLocationSelected(Runnable callback) {
        this.onLocationSelected = callback;
    }

    public LocationResult getSelectedLocation() {
        LocationResult result = new LocationResult();
        result.setLatitude(selectedLat);
        result.setLongitude(selectedLon);
        result.setDisplayName(selectedAddress);
        return result;
    }

    /**
     * Classe pour stocker le résultat de la localisation
     */
    public static class LocationResult {
        private double latitude;
        private double longitude;
        private String displayName;
        private String city;
        private String country;
        private String postcode;
        private String street;

        public double getLatitude() { return latitude; }
        public void setLatitude(double latitude) { this.latitude = latitude; }

        public double getLongitude() { return longitude; }
        public void setLongitude(double longitude) { this.longitude = longitude; }

        public String getDisplayName() { return displayName != null ? displayName : ""; }
        public void setDisplayName(String displayName) { this.displayName = displayName; }

        public String getCity() { return city != null ? city : ""; }
        public void setCity(String city) { this.city = city; }

        public String getCountry() { return country != null ? country : ""; }
        public void setCountry(String country) { this.country = country; }

        public String getPostcode() { return postcode != null ? postcode : ""; }
        public void setPostcode(String postcode) { this.postcode = postcode; }

        public String getStreet() { return street != null ? street : ""; }
        public void setStreet(String street) { this.street = street; }

        public String getFormattedAddress() {
            StringBuilder sb = new StringBuilder();
            if (street != null && !street.isEmpty()) sb.append(street);
            if (city != null && !city.isEmpty()) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(city);
            }
            if (postcode != null && !postcode.isEmpty()) {
                if (sb.length() > 0 && city != null && !city.isEmpty()) sb.append(" ");
                else if (sb.length() > 0) sb.append(", ");
                sb.append(postcode);
            }
            if (country != null && !country.isEmpty()) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(country);
            }
            return sb.length() > 0 ? sb.toString() : displayName;
        }
    }
}