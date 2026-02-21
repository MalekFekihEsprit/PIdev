package Controllers;

import Entites.etape;
import Services.MapAPIService;
import Services.etapeCRUD;
import Utils.AlertUtil;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import javafx.scene.layout.Priority;

public class PageEtape {

    @FXML private Label jourIcon;
    @FXML private Label titreJour;
    @FXML private Label sousTitreJour;
    @FXML private Label lblTotalEtapes;
    @FXML private Label lblDureeTotale;
    @FXML private VBox etapesContainer;
    @FXML private VBox emptyPlaceholder;
    @FXML private ComboBox<String> triCombo;
    @FXML private ComboBox<String> filtreTypeCombo;

    // Composants pour la carte
    @FXML private WebView mapWebView;
    @FXML private ProgressIndicator mapLoadingIndicator;
    @FXML private Label mapErrorLabel;
    @FXML private VBox mapContainer;
    @FXML private ToggleButton toggleMapButton;
    @FXML private Label mapDebugLabel;
    @FXML private Label geoInfoLabel;

    private int numeroJour;
    private int idItineraire;
    private String nomItineraire;
    private etapeCRUD etapeCRUD = new etapeCRUD();
    private MapAPIService mapService = new MapAPIService();
    private List<etape> etapes;
    private List<etape> etapesOriginales;
    private MainLayoutController mainLayoutController;

    // Pour la carte
    private WebEngine webEngine;
    private List<MarkerInfo> markers = new ArrayList<>();
    private boolean mapVisible = true;

    @FXML
    public void initialize() {
        diagnosticComplet(); // Vérification du FXML

        System.out.println("=== PageEtape INITIALIZATION ===");
        System.out.println("1. Début de l'initialisation");

        mainLayoutController = MainLayoutController.getInstance();
        System.out.println("2. MainLayoutController: " + (mainLayoutController != null ? "OK" : "NULL"));

        // Vérification des composants FXML
        verifyFXMLComponents();

        // Initialiser les options de tri
        initTriCombo();

        // Initialiser le filtre par type d'activité
        initFiltreCombo();

        // Initialiser la carte
        initMap();

        // Bouton pour afficher/masquer la carte
        if (toggleMapButton != null) {
            toggleMapButton.setOnAction(e -> toggleMap());
            System.out.println("6. ToggleMapButton configuré");
        }

        // FORCER l'affichage de la carte APRÈS l'initialisation
        javafx.application.Platform.runLater(() -> {
            if (mapContainer != null) {
                mapContainer.setVisible(true);
                mapContainer.setManaged(true);
            }
            if (toggleMapButton != null) {
                toggleMapButton.setSelected(true);
                toggleMapButton.setText("🗺️ Masquer la carte");
            }
            mapVisible = true;

            // Diagnostic après initialisation
            diagnosticCarteVisuel();
        });

        System.out.println("=== FIN INITIALIZATION ===\n");
    }

    /**
     * Diagnostique le chargement du FXML
     */
    private void diagnosticComplet() {
        System.out.println("\n=== DIAGNOSTIC COMPLET ===");

        // Vérifier l'URL du FXML
        java.net.URL fxmlUrl = getClass().getResource("/PageEtape.fxml");
        System.out.println("URL du FXML: " + fxmlUrl);

        if (fxmlUrl != null) {
            try {
                // Lire le contenu du fichier pour vérifier
                java.nio.file.Path path = java.nio.file.Paths.get(fxmlUrl.toURI());
                String content = java.nio.file.Files.readString(path);
                System.out.println("Taille du fichier: " + content.length() + " caractères");

                // Vérifier si les IDs sont présents
                System.out.println("Recherche des IDs dans le FXML:");
                System.out.println("  mapWebView: " + content.contains("fx:id=\"mapWebView\""));
                System.out.println("  mapContainer: " + content.contains("fx:id=\"mapContainer\""));
                System.out.println("  toggleMapButton: " + content.contains("fx:id=\"toggleMapButton\""));
                System.out.println("  mapLoadingIndicator: " + content.contains("fx:id=\"mapLoadingIndicator\""));
                System.out.println("  mapErrorLabel: " + content.contains("fx:id=\"mapErrorLabel\""));
                System.out.println("  mapDebugLabel: " + content.contains("fx:id=\"mapDebugLabel\""));
                System.out.println("  geoInfoLabel: " + content.contains("fx:id=\"geoInfoLabel\""));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Vérifier le loader qui a créé ce contrôleur
        System.out.println("Contrôleur: " + this.getClass().getName());
        System.out.println("=== FIN DIAGNOSTIC ===\n");
    }

    private void verifyFXMLComponents() {
        System.out.println("\n3. VÉRIFICATION DES COMPOSANTS FXML:");
        System.out.println("   mapWebView: " + (mapWebView != null ? "✓ PRÉSENT" : "✗ NULL"));
        System.out.println("   mapLoadingIndicator: " + (mapLoadingIndicator != null ? "✓ PRÉSENT" : "✗ NULL"));
        System.out.println("   mapErrorLabel: " + (mapErrorLabel != null ? "✓ PRÉSENT" : "✗ NULL"));
        System.out.println("   mapContainer: " + (mapContainer != null ? "✓ PRÉSENT" : "✗ NULL"));
        System.out.println("   toggleMapButton: " + (toggleMapButton != null ? "✓ PRÉSENT" : "✗ NULL"));
        System.out.println("   mapDebugLabel: " + (mapDebugLabel != null ? "✓ PRÉSENT" : "✗ NULL"));
        System.out.println("   geoInfoLabel: " + (geoInfoLabel != null ? "✓ PRÉSENT" : "✗ NULL"));
    }

    private void diagnosticCarteVisuel() {
        System.out.println("\n=== DIAGNOSTIC VISUEL CARTE ===");
        System.out.println("mapContainer visible: " + (mapContainer != null ? mapContainer.isVisible() : "N/A"));
        System.out.println("mapContainer managed: " + (mapContainer != null ? mapContainer.isManaged() : "N/A"));
        System.out.println("mapWebView visible: " + (mapWebView != null ? mapWebView.isVisible() : "N/A"));
        System.out.println("mapWebView height: " + (mapWebView != null ? mapWebView.getHeight() : "N/A"));
        System.out.println("mapVisible variable: " + mapVisible);
        System.out.println("toggleMapButton text: " + (toggleMapButton != null ? toggleMapButton.getText() : "N/A"));
        System.out.println("toggleMapButton selected: " + (toggleMapButton != null ? toggleMapButton.isSelected() : "N/A"));
        System.out.println("webEngine: " + (webEngine != null ? "initialisé" : "null"));
        System.out.println("markers size: " + markers.size());
        System.out.println("=== FIN DIAGNOSTIC ===\n");
    }

    private void initTriCombo() {
        if (triCombo != null) {
            triCombo.getItems().addAll(
                    "Par heure (croissante)",
                    "Par heure (décroissante)",
                    "Par activité (A → Z)",
                    "Par activité (Z → A)",
                    "Par durée (croissante)",
                    "Par durée (décroissante)"
            );
            triCombo.getSelectionModel().selectFirst();
            triCombo.setOnAction(e -> appliquerTri());
            System.out.println("4. TriCombo initialisé");
        } else {
            System.out.println("4. TriCombo est NULL");
        }
    }

    private void initFiltreCombo() {
        if (filtreTypeCombo != null) {
            filtreTypeCombo.getItems().add("Tous les types");
            filtreTypeCombo.getSelectionModel().selectFirst();
            filtreTypeCombo.setOnAction(e -> appliquerFiltre());
            System.out.println("5. FiltreCombo initialisé");
        } else {
            System.out.println("5. FiltreCombo est NULL");
        }
    }

    private void initMap() {
        System.out.println("\n=== INITIALISATION DE LA CARTE ===");

        if (mapWebView == null) {
            System.err.println("❌ ERREUR CRITIQUE: mapWebView est NULL!");
            return;
        }

        try {
            webEngine = mapWebView.getEngine();
            webEngine.setJavaScriptEnabled(true);

            // Forcer la visibilité
            mapWebView.setVisible(true);
            if (mapContainer != null) {
                mapContainer.setVisible(true);
                mapContainer.setManaged(true);
            }

            // Gérer le chargement
            webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                System.out.println("WebEngine state: " + newState);

                if (newState == Worker.State.SUCCEEDED) {
                    System.out.println("✅ Carte chargée avec succès!");
                    mapLoadingIndicator.setVisible(false);

                    // Exécuter un script de test
                    webEngine.executeScript("console.log('Carte prête');");

                    if (!markers.isEmpty()) {
                        ajouterMarqueurs();
                    }
                } else if (newState == Worker.State.FAILED) {
                    Throwable exception = webEngine.getLoadWorker().getException();
                    System.err.println("❌ Échec du chargement de la carte: " +
                            (exception != null ? exception.getMessage() : "inconnue"));
                    mapLoadingIndicator.setVisible(false);
                    mapErrorLabel.setVisible(true);
                    mapErrorLabel.setText("Échec du chargement de la carte");
                }
            });

            // Charger la carte HTML
            chargerCarte();

        } catch (Exception e) {
            System.err.println("❌ Exception lors de l'initialisation de la carte:");
            e.printStackTrace();
        }
    }

    private void chargerCarte() {
        String htmlContent = getLeafletMapHTML();
        System.out.println("HTML chargé, longueur: " + htmlContent.length());
        webEngine.loadContent(htmlContent);
    }

    private String getLeafletMapHTML() {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8" />
                <meta name="viewport" content="width=device-width, initial-scale=1.0" />
                <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
                <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
                <style>
                    * { margin: 0; padding: 0; }
                    body { font-family: 'Segoe UI', sans-serif; }
                    #map { width: 100%; height: 100vh; background-color: #e0e0e0; }
                    .marker-number {
                        background-color: #ff8c42;
                        color: white;
                        border-radius: 50%;
                        width: 24px;
                        height: 24px;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        font-weight: bold;
                        font-size: 12px;
                        border: 2px solid white;
                        box-shadow: 0 2px 5px rgba(0,0,0,0.3);
                    }
                    .test-marker {
                        background-color: #3b82f6;
                    }
                </style>
            </head>
            <body>
                <div id="map"></div>
                <script>
                    var map = L.map('map').setView([34.0, 9.0], 6);
                    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                        attribution: '© OpenStreetMap'
                    }).addTo(map);
                    
                    // Marqueur de test pour vérifier que la carte fonctionne
                    L.marker([36.3994751, 10.1446349]).addTo(map)
                        .bindPopup('Marqueur de test - Zaghouan')
                        .openPopup();
                    
                    var markers = [];
                    
                    function addMarker(index, lat, lon, lieu, heure, activite, description) {
                        var markerIcon = L.divIcon({
                            className: 'custom-div-icon',
                            html: '<div class="marker-number">' + index + '</div>',
                            iconSize: [24, 24],
                            popupAnchor: [0, -12]
                        });
                        
                        var marker = L.marker([lat, lon], {icon: markerIcon}).addTo(map);
                        
                        var popupContent = '<b>Étape ' + index + '</b><br>' +
                            '🕒 ' + heure + '<br>' +
                            '🎯 ' + activite + '<br>' +
                            '📍 ' + lieu;
                        
                        if (description && description.trim() !== '') {
                            popupContent += '<br><i>' + description + '</i>';
                        }
                        
                        marker.bindPopup(popupContent);
                        markers.push(marker);
                        return marker;
                    }
                    
                    function fitMapToMarkers() {
                        if (markers.length > 0) {
                            var group = L.featureGroup(markers);
                            map.fitBounds(group.getBounds().pad(0.2));
                        }
                    }
                    
                    function clearMarkers() {
                        markers.forEach(m => map.removeLayer(m));
                        markers = [];
                    }
                    
                    function addMarkersFromJava(markersData) {
                        clearMarkers();
                        if (markersData && markersData.length > 0) {
                            markersData.forEach(data => {
                                addMarker(
                                    data.index, data.lat, data.lon,
                                    data.lieu, data.heure,
                                    data.activite, data.description
                                );
                            });
                            fitMapToMarkers();
                        }
                    }
                </script>
            </body>
            </html>
            """;
    }

    public void setJourInfo(int numeroJour, int idItineraire, String nomItineraire) {
        this.numeroJour = numeroJour;
        this.idItineraire = idItineraire;
        this.nomItineraire = nomItineraire;

        if (titreJour != null) {
            titreJour.setText("Jour " + numeroJour);
        }
        if (jourIcon != null) {
            jourIcon.setText(String.valueOf(numeroJour));
        }
        if (sousTitreJour != null) {
            sousTitreJour.setText("Planification des activités pour le jour " + numeroJour);
        }

        chargerEtapes();

        // Diagnostic après chargement
        javafx.application.Platform.runLater(() -> {
            diagnosticCarteVisuel();
        });
    }

    private void chargerEtapes() {
        try {
            etapes = etapeCRUD.getEtapesByItineraire(idItineraire);
            etapesOriginales = new ArrayList<>(etapes);

            System.out.println("Étapes chargées: " + etapes.size() + " étapes");

            // Charger les types d'activités pour le filtre
            if (filtreTypeCombo != null && etapes != null && !etapes.isEmpty()) {
                filtreTypeCombo.getItems().clear();
                filtreTypeCombo.getItems().add("Tous les types");
                etapes.stream()
                        .map(e -> e.getNomActivite())
                        .filter(nom -> nom != null && !nom.isEmpty())
                        .distinct()
                        .sorted()
                        .forEach(nom -> filtreTypeCombo.getItems().add(nom));
                filtreTypeCombo.getSelectionModel().selectFirst();
            }

            updateEtapesDisplay();
            geocoderEtapes();

        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtil.showError("Erreur", "Impossible de charger les étapes: " + e.getMessage());
        }
    }

    private void geocoderEtapes() {
        if (mapWebView == null) {
            System.err.println("❌ geocoderEtapes: mapWebView est null!");
            return;
        }

        System.out.println("🔄 Début du géocodage pour " + etapes.size() + " étapes");
        mapLoadingIndicator.setVisible(true);
        markers.clear();

        CompletableFuture.runAsync(() -> {
            int index = 1;
            int successCount = 0;

            for (etape e : etapes) {
                String lieu = e.getLieuActivite();
                System.out.println("   Traitement étape " + index + ": " + lieu);

                if (lieu != null && !lieu.isEmpty()) {
                    try {
                        MapAPIService.GeoPoint point = mapService.geocode(lieu);
                        if (point != null) {
                            markers.add(new MarkerInfo(
                                    index,
                                    point.getLatitude(),
                                    point.getLongitude(),
                                    lieu,
                                    e.getHeureFormatted(),
                                    e.getDescription_etape(),
                                    e.getNomActivite() != null ? e.getNomActivite() : "Activité"
                            ));
                            successCount++;
                            System.out.println("      ✅ Géocodé: " + point.getLatitude() + ", " + point.getLongitude());
                        } else {
                            System.out.println("      ❌ Échec géocodage pour: " + lieu);
                        }
                        Thread.sleep(1000); // Respect de la limite API
                    } catch (Exception ex) {
                        System.err.println("      ❌ Erreur: " + ex.getMessage());
                    }
                } else {
                    System.out.println("      ⚠️ Lieu vide pour étape " + index);
                }
                index++;
            }

            final int finalSuccessCount = successCount;
            System.out.println("📊 Géocodage terminé: " + finalSuccessCount + "/" + etapes.size() + " réussis");

            javafx.application.Platform.runLater(() -> {
                mapLoadingIndicator.setVisible(false);
                if (!markers.isEmpty()) {
                    System.out.println("✅ " + markers.size() + " marqueurs à afficher");
                    ajouterMarqueurs();
                    mapErrorLabel.setVisible(false);
                    if (geoInfoLabel != null) {
                        geoInfoLabel.setText("📍 " + markers.size() + " lieu(x) localisé(s)");
                        geoInfoLabel.setVisible(true);
                    }
                } else {
                    System.out.println("❌ Aucun marqueur à afficher");
                    mapErrorLabel.setVisible(true);
                    mapErrorLabel.setText("⚠️ Aucune localisation trouvée");
                }
            });
        });
    }

    private void ajouterMarqueurs() {
        if (webEngine == null) {
            System.err.println("❌ ajouterMarqueurs: webEngine est null!");
            return;
        }

        if (markers.isEmpty()) {
            System.out.println("⚠️ ajouterMarqueurs: aucun marqueur à ajouter");
            return;
        }

        try {
            StringBuilder markersJson = new StringBuilder("[");
            for (int i = 0; i < markers.size(); i++) {
                MarkerInfo m = markers.get(i);
                if (i > 0) markersJson.append(",");
                markersJson.append("{")
                        .append("\"index\":").append(m.index).append(",")
                        .append("\"lat\":").append(m.lat).append(",")
                        .append("\"lon\":").append(m.lon).append(",")
                        .append("\"lieu\":\"").append(escapeJS(m.lieu)).append("\",")
                        .append("\"heure\":\"").append(escapeJS(m.heure)).append("\",")
                        .append("\"activite\":\"").append(escapeJS(m.activite)).append("\",")
                        .append("\"description\":\"").append(escapeJS(m.description != null ? m.description : "")).append("\"")
                        .append("}");
            }
            markersJson.append("]");

            String script = "addMarkersFromJava(" + markersJson.toString() + ");";
            System.out.println("Exécution du script JavaScript avec " + markers.size() + " marqueurs");
            webEngine.executeScript(script);

            // Forcer le recentrage
            webEngine.executeScript("fitMapToMarkers();");

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'ajout des marqueurs:");
            e.printStackTrace();
        }
    }

    private String escapeJS(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                .replace("'", "\\'")
                .replace("\"", "\\\"")
                .replace("\n", " ")
                .replace("\r", " ");
    }

    private void appliquerFiltre() {
        if (etapesOriginales == null) return;
        String selectedType = filtreTypeCombo.getSelectionModel().getSelectedItem();
        if (selectedType == null || selectedType.equals("Tous les types")) {
            etapes = new ArrayList<>(etapesOriginales);
        } else {
            etapes = etapesOriginales.stream()
                    .filter(e -> selectedType.equals(e.getNomActivite()))
                    .collect(Collectors.toList());
        }
        appliquerTri();
    }

    private void appliquerTri() {
        if (etapes == null || etapes.isEmpty()) return;
        String selectedTri = triCombo.getSelectionModel().getSelectedItem();
        if (selectedTri == null) return;

        try {
            switch (selectedTri) {
                case "Par heure (croissante)":
                    etapes = etapeCRUD.trierParHeure(etapes, true);
                    break;
                case "Par heure (décroissante)":
                    etapes = etapeCRUD.trierParHeure(etapes, false);
                    break;
                case "Par activité (A → Z)":
                    etapes = etapeCRUD.trierParTypeActivite(etapes, true);
                    break;
                case "Par activité (Z → A)":
                    etapes = etapeCRUD.trierParTypeActivite(etapes, false);
                    break;
                case "Par durée (croissante)":
                    etapes = etapeCRUD.trierParDuree(etapes, true);
                    break;
                case "Par durée (décroissante)":
                    etapes = etapeCRUD.trierParDuree(etapes, false);
                    break;
            }
            updateEtapesDisplay();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateEtapesDisplay() {
        if (etapes == null || etapes.isEmpty()) {
            emptyPlaceholder.setVisible(true);
            etapesContainer.setVisible(false);
            lblTotalEtapes.setText("Total: 0 étape");
            lblDureeTotale.setText("Durée: 0h");
        } else {
            emptyPlaceholder.setVisible(false);
            etapesContainer.setVisible(true);
            lblTotalEtapes.setText("Total: " + etapes.size() + " étape" + (etapes.size() > 1 ? "s" : ""));
            lblDureeTotale.setText("Durée: " + calculerDureeTotale() + "h");
            generateEtapeCards();
        }
    }

    private int calculerDureeTotale() {
        int total = 0;
        for (etape e : etapes) {
            if (e.getDureeActivite() != null) total += e.getDureeActivite();
        }
        return total;
    }

    private void generateEtapeCards() {
        etapesContainer.getChildren().clear();
        int index = 1;
        for (etape etape : etapes) {
            etapesContainer.getChildren().add(createEtapeCard(etape, index++));
        }
    }

    private VBox createEtapeCard(etape etape, int index) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-border-radius: 10; -fx-padding: 12;");

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label numberLabel = new Label(String.valueOf(index));
        numberLabel.setStyle("-fx-background-color: #ff8c42; -fx-text-fill: white; -fx-background-radius: 50%; -fx-min-width: 28; -fx-min-height: 28; -fx-alignment: center; -fx-font-weight: bold; -fx-font-size: 12;");

        Label timeLabel = new Label(etape.getHeureFormatted());
        timeLabel.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 6; -fx-padding: 4 8; -fx-font-weight: 600; -fx-text-fill: #0f172a; -fx-font-size: 12;");

        VBox contentBox = new VBox(4);
        Label nomLabel = new Label(etape.getNomActivite());
        nomLabel.setStyle("-fx-font-weight: 700; -fx-text-fill: #0f172a; -fx-font-size: 14;");

        HBox lieuBox = new HBox(6);
        lieuBox.setAlignment(Pos.CENTER_LEFT);
        Label lieuIcon = new Label("📍");
        lieuIcon.setStyle("-fx-font-size: 12;");
        Label lieuText = new Label(etape.getLieuActivite() != null ? etape.getLieuActivite() : "Lieu non défini");
        lieuText.setStyle("-fx-text-fill: #3b82f6; -fx-font-size: 12; -fx-font-weight: 500;");
        lieuBox.getChildren().addAll(lieuIcon, lieuText);

        HBox infoBox = new HBox(12);
        if (etape.getDureeActivite() != null && etape.getDureeActivite() > 0) {
            HBox dureeBox = new HBox(4);
            dureeBox.setAlignment(Pos.CENTER_LEFT);
            dureeBox.getChildren().addAll(new Label("⏱️"), new Label(String.format("%.1fh", etape.getDureeActivite())));
            infoBox.getChildren().add(dureeBox);
        }
        if (etape.getBudgetActivite() != null && etape.getBudgetActivite() > 0) {
            HBox budgetBox = new HBox(4);
            budgetBox.setAlignment(Pos.CENTER_LEFT);
            budgetBox.getChildren().addAll(new Label("💰"), new Label(String.format("%.2f TND", etape.getBudgetActivite())));
            infoBox.getChildren().add(budgetBox);
        }

        contentBox.getChildren().addAll(nomLabel, lieuBox, infoBox);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox actionButtons = new HBox(5);
        Button editButton = new Button("✏️");
        editButton.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 4; -fx-padding: 4 6; -fx-cursor: hand;");
        editButton.setOnAction(e -> handleModifierEtape(etape));

        Button deleteButton = new Button("🗑️");
        deleteButton.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #ef4444; -fx-background-radius: 4; -fx-padding: 4 6; -fx-cursor: hand;");
        deleteButton.setOnAction(e -> handleSupprimerEtape(etape));

        actionButtons.getChildren().addAll(editButton, deleteButton);

        header.getChildren().addAll(numberLabel, timeLabel, contentBox, spacer, actionButtons);
        card.getChildren().add(header);

        if (etape.getDescription_etape() != null && !etape.getDescription_etape().isEmpty()) {
            Label descLabel = new Label("📝 " + etape.getDescription_etape());
            descLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11; -fx-padding: 8 0 0 0;");
            descLabel.setWrapText(true);
            card.getChildren().add(descLabel);
        }

        return card;
    }

    private void handleModifierEtape(etape etape) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/PageAjoutEtape.fxml"));
            DialogPane dialogPane = loader.load();
            PageAjoutEtape modifierController = loader.getController();
            modifierController.setDialogPane(dialogPane);
            modifierController.setEtapeAModifier(etape);
            modifierController.setOnEtapeAjoutee(this::chargerEtapes);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Modifier l'étape");
            dialog.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleSupprimerEtape(etape etape) {
        if (AlertUtil.showConfirmation("Confirmation", "Supprimer cette étape ?")) {
            try {
                etapeCRUD.supprimer(etape.getId_etape());
                chargerEtapes();
                AlertUtil.showInfo("Succès", "Étape supprimée");
            } catch (SQLException e) {
                e.printStackTrace();
                AlertUtil.showError("Erreur", "Impossible de supprimer: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleAjouterEtape() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/PageAjoutEtape.fxml"));
            DialogPane dialogPane = loader.load();
            PageAjoutEtape ajoutController = loader.getController();
            ajoutController.setDialogPane(dialogPane);
            ajoutController.setJourInfo(numeroJour, idItineraire);
            ajoutController.setOnEtapeAjoutee(this::chargerEtapes);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Ajouter une étape - Jour " + numeroJour);
            dialog.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCentrerCarte() {
        if (webEngine != null && !markers.isEmpty()) {
            webEngine.executeScript("fitMapToMarkers();");
        }
    }

    private void toggleMap() {
        if (mapContainer == null) return;
        mapVisible = !mapVisible;
        mapContainer.setVisible(mapVisible);
        mapContainer.setManaged(mapVisible);
        if (toggleMapButton != null) {
            toggleMapButton.setText(mapVisible ? "🗺️ Masquer la carte" : "🗺️ Afficher la carte");
        }
        System.out.println("Toggle map: " + (mapVisible ? "visible" : "masqué"));
    }

    @FXML
    private void handleRetour() {
        if (mainLayoutController != null) {
            mainLayoutController.loadPage("/PageItineraire.fxml");
        }
    }

    // Classe interne pour les informations des marqueurs
    private static class MarkerInfo {
        int index;
        double lat;
        double lon;
        String lieu;
        String heure;
        String description;
        String activite;

        MarkerInfo(int i, double la, double lo, String l, String h, String d, String a) {
            index = i;
            lat = la;
            lon = lo;
            lieu = l;
            heure = h;
            description = d;
            activite = a;
        }
    }
}