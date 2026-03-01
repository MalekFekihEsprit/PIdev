package Controllers.ItineraireEtEtape;

import Entities.Itineraire;
import Entities.Voyage;
import Services.itineraireCRUD;
import Services.CulturalAdviceService;
import Services.FlightAPIService;
import Utils.AlertUtil;
import Utils.ExportUtil;
import Utils.MyBD;
import Utils.UserSession;
import Entities.User;
import Entities.etape;
import Services.etapeCRUD;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Modality;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PageItineraire {

    // ============== ÉLÉMENTS DE NAVIGATION ==============
    @FXML private HBox btnDestinations;
    @FXML private HBox btnHebergement;
    @FXML private HBox btnActivites;
    @FXML private HBox btnCategories;
    @FXML private HBox btnVoyages;
    @FXML private HBox btnBudgets;
    @FXML private HBox userProfileBox;
    @FXML private Label lblUserName;
    @FXML private Label lblUserRole;
    @FXML private Label lblVoyageContext;
    @FXML private Label lblItinerairesActifsFooter;

    // ============== NOUVEAUX ÉLÉMENTS ==============
    @FXML private Button btnVols;
    @FXML private Button btnCulture;
    @FXML private Label cultureBadge;

    // ============== ÉLÉMENTS EXISTANTS ==============
    @FXML private ComboBox<String> comboDestination;
    @FXML private DatePicker dateDebut;
    @FXML private DatePicker dateFin;
    @FXML private Button btnRechercher;
    @FXML private Button btnReinitialiserFiltres;
    @FXML private Label lblTotalItinerairesTable;
    @FXML private VBox emptyPlaceholder;
    @FXML private Button btnCreerPremierItineraire;
    @FXML private VBox itinerariesContainer;
    @FXML private ComboBox<String> triCombo;
    @FXML private TextField searchField;
    @FXML private Label lblDestinationsCount;
    @FXML private Label lblJoursCount;
    @FXML private Label lblResultCount;
    @FXML private Label lblListeTitre;
    @FXML private BorderPane mainBorderPane;

    private itineraireCRUD itineraireCRUD;
    private etapeCRUD etapeCRUD;
    private List<Itineraire> tousLesItineraires;
    private List<Itineraire> itinerairesAffiches;
    private Voyage voyageActuel;
    private String destinationActuelle;

    // Services pour les fonctionnalités globales
    private CulturalAdviceService culturalService = new CulturalAdviceService();
    private FlightAPIService flightService = new FlightAPIService();
    private CulturalAdviceService.CulturalInfo culturalInfo;

    // Classes pour les informations de voyage
    private class VoyageInfo {
        String titre;
        String destination;
        int nbJours;
        LocalDate dateDebut;
        LocalDate dateFin;

        VoyageInfo(String titre, String destination, int nbJours, LocalDate dateDebut, LocalDate dateFin) {
            this.titre = titre;
            this.destination = destination;
            this.nbJours = nbJours;
            this.dateDebut = dateDebut;
            this.dateFin = dateFin;
        }
    }

    private java.util.Map<Integer, VoyageInfo> voyageInfoCache = new java.util.HashMap<>();

    @FXML
    public void initialize() {
        itineraireCRUD = new itineraireCRUD();
        etapeCRUD = new etapeCRUD();

        // ============== CONFIGURATION DE LA NAVIGATION ==============
        configurerNavigation();
        configurerUserProfile();
        updateUserInfo();

        // ============== CONFIGURATION DES BOUTONS GLOBAUX ==============
        configurerBoutonsGlobaux();

        // Initialiser les options de tri
        if (triCombo != null) {
            triCombo.getItems().addAll(
                    "Par défaut",
                    "Nom (A → Z)",
                    "Nom (Z → A)",
                    "Date (plus récent)",
                    "Date (plus ancien)",
                    "Nombre de jours (croissant)",
                    "Nombre de jours (décroissant)"
            );
            triCombo.getSelectionModel().selectFirst();
            triCombo.setOnAction(e -> appliquerTri());
        }

        // Recherche en temps réel
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> {
                appliquerRechercheEtFiltres();
            });
        }

        // Charger les destinations dans le filtre
        chargerDestinations();

        // Configurer les boutons de filtre
        if (btnRechercher != null) {
            btnRechercher.setOnAction(e -> appliquerRechercheEtFiltres());
        }

        if (btnReinitialiserFiltres != null) {
            btnReinitialiserFiltres.setOnAction(e -> reinitialiserFiltres());
        }

        // Ajouter des listeners pour les DatePicker
        if (dateDebut != null) {
            dateDebut.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && dateFin.getValue() != null && newVal.isAfter(dateFin.getValue())) {
                    AlertUtil.showWarning("Attention", "La date de début ne peut pas être après la date de fin");
                    dateDebut.setValue(oldVal);
                } else {
                    appliquerRechercheEtFiltres();
                }
            });
        }

        if (dateFin != null) {
            dateFin.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && dateDebut.getValue() != null && newVal.isBefore(dateDebut.getValue())) {
                    AlertUtil.showWarning("Attention", "La date de fin ne peut pas être avant la date de début");
                    dateFin.setValue(oldVal);
                } else {
                    appliquerRechercheEtFiltres();
                }
            });
        }

        // Listener pour le combo destination
        if (comboDestination != null) {
            comboDestination.valueProperty().addListener((obs, oldVal, newVal) -> {
                appliquerRechercheEtFiltres();
            });
        }

        // Charger tous les itinéraires
        chargerTousLesItineraires();

        System.out.println("PageItineraire initialisée");
    }

    // ============== CONFIGURATION DES BOUTONS GLOBAUX ==============
    private void configurerBoutonsGlobaux() {
        if (btnVols != null) {
            btnVols.setOnAction(e -> ouvrirPopupVols());
            btnVols.setDisable(true); // Désactivé par défaut
        }

        if (btnCulture != null) {
            btnCulture.setOnAction(e -> ouvrirPopupCulture());
            btnCulture.setDisable(true); // Désactivé par défaut
        }

        if (cultureBadge != null) {
            cultureBadge.setOnMouseClicked(e -> ouvrirPopupCulture());
            cultureBadge.setVisible(false);
            cultureBadge.setManaged(false);
        }
    }

    // ============== MÉTHODES POUR LES POPUPS GLOBAUX ==============
    private void ouvrirPopupVols() {
        if (voyageActuel == null) {
            AlertUtil.showWarning("Information", "Sélectionnez d'abord un voyage pour voir les vols");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ItineraireEtEtape/FlightPopup.fxml"));

            if (loader.getLocation() == null) {
                System.err.println("ERREUR: FlightPopup.fxml non trouvé!");
                AlertUtil.showError("Erreur", "Fichier FlightPopup.fxml non trouvé");
                return;
            }

            DialogPane dialogPane = loader.load();
            FlightPopupController controller = loader.getController();

            String destination = destinationActuelle != null ? destinationActuelle : "Tunis";
            LocalDate firstDayDate = voyageActuel.getDate_debut().toLocalDate();

            controller.setFlightInfo(destination, firstDayDate, voyageActuel.getTitre_voyage());

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Vols pour " + destination);
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Erreur", "Impossible d'ouvrir la fenêtre des vols: " + e.getMessage());
        }
    }

    private void ouvrirPopupCulture() {
        if (voyageActuel == null) {
            AlertUtil.showWarning("Information", "Sélectionnez d'abord un voyage pour voir les conseils culturels");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ItineraireEtEtape/CulturalPopup.fxml"));

            if (loader.getLocation() == null) {
                System.err.println("ERREUR: CulturalPopup.fxml non trouvé!");
                AlertUtil.showError("Erreur", "Fichier CulturalPopup.fxml non trouvé");
                return;
            }

            DialogPane dialogPane = loader.load();
            CulturalPopupController controller = loader.getController();

            String destination = destinationActuelle != null ? destinationActuelle : "Tunis";
            String country = extrairePays(destination);

            if (culturalInfo == null) {
                culturalInfo = culturalService.getCulturalAdvice(destination, country);
            }

            controller.setCulturalInfo(culturalInfo, voyageActuel.getTitre_voyage());

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Conseils culturels - " + destination);
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Erreur", "Impossible d'ouvrir les conseils culturels: " + e.getMessage());
        }
    }

    private String extrairePays(String destination) {
        if (destination == null) return "Tunisie";

        Map<String, String> countryMap = new HashMap<>();
        countryMap.put("zaghouan", "Tunisie");
        countryMap.put("tunis", "Tunisie");
        countryMap.put("paris", "France");
        countryMap.put("londres", "Royaume-Uni");
        countryMap.put("london", "Royaume-Uni");
        countryMap.put("dubai", "Émirats Arabes Unis");
        countryMap.put("dubaï", "Émirats Arabes Unis");
        countryMap.put("berlin", "Allemagne");
        countryMap.put("rome", "Italie");
        countryMap.put("italie", "Italie");
        countryMap.put("barcelone", "Espagne");
        countryMap.put("barcelona", "Espagne");
        countryMap.put("madrid", "Espagne");
        countryMap.put("new york", "États-Unis");
        countryMap.put("istanbul", "Turquie");
        countryMap.put("cairo", "Égypte");
        countryMap.put("le caire", "Égypte");
        countryMap.put("casablanca", "Maroc");
        countryMap.put("marrakech", "Maroc");
        countryMap.put("tokyo", "Japon");
        countryMap.put("japon", "Japon");

        return countryMap.getOrDefault(destination.toLowerCase(), destination);
    }

    private void loadCulturalAdvice() {
        if (voyageActuel == null || destinationActuelle == null) return;

        new Thread(() -> {
            try {
                String country = extrairePays(destinationActuelle);
                culturalInfo = culturalService.getCulturalAdvice(destinationActuelle, country);

                javafx.application.Platform.runLater(() -> {
                    updateCultureBadge();
                    if (btnCulture != null) {
                        btnCulture.setDisable(false);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void updateCultureBadge() {
        if (cultureBadge != null && culturalInfo != null) {
            if (culturalInfo.hasContent()) {
                cultureBadge.setVisible(true);
                cultureBadge.setManaged(true);
                cultureBadge.setTooltip(new Tooltip("Conseils culturels disponibles pour " + destinationActuelle));
            }
        }
    }

    // ============== MÉTHODES DE NAVIGATION ==============
    private void configurerNavigation() {
        if (btnDestinations != null) {
            btnDestinations.setOnMouseClicked(event -> navigateTo("/DestinationFront.fxml", "Destinations"));

            btnDestinations.setOnMouseEntered(event -> {
                btnDestinations.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 12; -fx-padding: 8 14; -fx-cursor: hand;");
            });
            btnDestinations.setOnMouseExited(event -> {
                btnDestinations.setStyle("-fx-background-color: transparent; -fx-background-radius: 12; -fx-padding: 8 14; -fx-cursor: hand;");
            });
        }

        if (btnHebergement != null) {
            btnHebergement.setOnMouseClicked(event -> navigateTo("/HebergementFront.fxml", "Hébergements"));

            btnHebergement.setOnMouseEntered(event -> {
                btnHebergement.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 12; -fx-padding: 8 14; -fx-cursor: hand;");
            });
            btnHebergement.setOnMouseExited(event -> {
                btnHebergement.setStyle("-fx-background-color: transparent; -fx-background-radius: 12; -fx-padding: 8 14; -fx-cursor: hand;");
            });
        }

        if (btnActivites != null) {
            btnActivites.setOnMouseClicked(event -> navigateTo("/activitesfront.fxml", "Activités"));

            btnActivites.setOnMouseEntered(event -> {
                btnActivites.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 12; -fx-padding: 8 14; -fx-cursor: hand;");
            });
            btnActivites.setOnMouseExited(event -> {
                btnActivites.setStyle("-fx-background-color: transparent; -fx-background-radius: 12; -fx-padding: 8 14; -fx-cursor: hand;");
            });
        }

        if (btnCategories != null) {
            btnCategories.setOnMouseClicked(event -> navigateTo("/categoriesfront.fxml", "Catégories"));

            btnCategories.setOnMouseEntered(event -> {
                btnCategories.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 12; -fx-padding: 8 14; -fx-cursor: hand;");
            });
            btnCategories.setOnMouseExited(event -> {
                btnCategories.setStyle("-fx-background-color: transparent; -fx-background-radius: 12; -fx-padding: 8 14; -fx-cursor: hand;");
            });
        }

        if (btnVoyages != null) {
            btnVoyages.setOnMouseClicked(event -> navigateTo("/PageVoyage.fxml", "Voyages"));

            btnVoyages.setOnMouseEntered(event -> {
                btnVoyages.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 12; -fx-padding: 8 14; -fx-cursor: hand;");
            });
            btnVoyages.setOnMouseExited(event -> {
                btnVoyages.setStyle("-fx-background-color: transparent; -fx-background-radius: 12; -fx-padding: 8 14; -fx-cursor: hand;");
            });
        }

        if (btnBudgets != null) {
            btnBudgets.setOnMouseClicked(event -> showNotImplementedAlert("Budgets"));

            btnBudgets.setOnMouseEntered(event -> {
                btnBudgets.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 12; -fx-padding: 8 14; -fx-cursor: hand;");
            });
            btnBudgets.setOnMouseExited(event -> {
                btnBudgets.setStyle("-fx-background-color: transparent; -fx-background-radius: 12; -fx-padding: 8 14; -fx-cursor: hand;");
            });
        }
    }

    private void configurerUserProfile() {
        if (userProfileBox != null) {
            userProfileBox.setOnMouseClicked(event -> navigateTo("/fxml/profile.fxml", "Profil"));

            userProfileBox.setOnMouseEntered(event -> {
                userProfileBox.setStyle("-fx-background-color: #e2e8f0; -fx-background-radius: 25; -fx-padding: 5 14 5 5; -fx-cursor: hand; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-border-radius: 25;");
            });

            userProfileBox.setOnMouseExited(event -> {
                userProfileBox.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 25; -fx-padding: 5 14 5 5; -fx-cursor: hand; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-border-radius: 25;");
            });
        }
    }

    private void updateUserInfo() {
        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser != null) {
            if (lblUserName != null) {
                lblUserName.setText(currentUser.getPrenom() + " " + currentUser.getNom());
            }
            if (lblUserRole != null) {
                lblUserRole.setText(currentUser.getRole());
            }
        } else {
            if (lblUserName != null) {
                lblUserName.setText("Utilisateur");
            }
            if (lblUserRole != null) {
                lblUserRole.setText("Non connecté");
            }
        }
    }

    private void navigateTo(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) btnDestinations.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TravelMate - " + title);
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger " + title + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showNotImplementedAlert(String feature) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Fonctionnalité à venir");
        alert.setHeaderText(null);
        alert.setContentText("La fonctionnalité \"" + feature + "\" sera bientôt disponible !");
        alert.showAndWait();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ============== MÉTHODE POUR RECEVOIR LES DONNÉES DU VOYAGE ==============
    public void initData(Voyage voyage, String nomDestination) {
        this.voyageActuel = voyage;
        this.destinationActuelle = nomDestination;

        if (voyage != null && lblVoyageContext != null) {
            lblVoyageContext.setText("Voyage: " + voyage.getTitre_voyage() + " (" + nomDestination + ")");

            // Désactiver le filtre par destination
            if (comboDestination != null) {
                comboDestination.setDisable(true);
                comboDestination.setValue(nomDestination);
            }

            // Activer les boutons
            if (btnVols != null) {
                btnVols.setDisable(false);
            }
            if (btnCulture != null) {
                btnCulture.setDisable(false);
            }

            // Charger les conseils culturels en arrière-plan
            loadCulturalAdvice();
        }

        // Appliquer le filtrage strict par voyage
        appliquerFiltrageStrict();
    }

    // ============== MÉTHODES DE FILTRAGE ==============
    private void appliquerFiltrageStrict() {
        if (tousLesItineraires == null) {
            chargerTousLesItineraires();
            return;
        }

        if (voyageActuel != null) {
            itinerairesAffiches = tousLesItineraires.stream()
                    .filter(i -> i.getId_voyage() == voyageActuel.getId_voyage())
                    .collect(Collectors.toList());

            System.out.println("Filtrage strict: " + itinerairesAffiches.size() +
                    " itinéraire(s) pour le voyage ID " + voyageActuel.getId_voyage());

            if (lblListeTitre != null) {
                lblListeTitre.setText("Itinéraires de " + voyageActuel.getTitre_voyage());
            }
        } else {
            itinerairesAffiches = new ArrayList<>(tousLesItineraires);
            if (lblListeTitre != null) {
                lblListeTitre.setText("Tous les itinéraires");
            }
        }

        appliquerFiltresSupplementaires();
    }

    private void appliquerFiltresSupplementaires() {
        if (itinerairesAffiches == null) return;

        List<Itineraire> resultats = new ArrayList<>(itinerairesAffiches);

        String searchText = searchField != null ? searchField.getText().toLowerCase().trim() : "";
        if (!searchText.isEmpty()) {
            resultats = resultats.stream()
                    .filter(i -> {
                        VoyageInfo info = getVoyageInfo(i.getId_voyage());
                        return i.getNom_itineraire().toLowerCase().contains(searchText) ||
                                (i.getDescription_itineraire() != null && i.getDescription_itineraire().toLowerCase().contains(searchText)) ||
                                (info.titre != null && info.titre.toLowerCase().contains(searchText));
                    })
                    .collect(Collectors.toList());
        }

        if (voyageActuel == null) {
            String destinationFilter = comboDestination != null ? comboDestination.getValue() : null;
            if (destinationFilter != null && !destinationFilter.equals("Toutes les destinations")) {
                resultats = resultats.stream()
                        .filter(i -> {
                            VoyageInfo info = getVoyageInfo(i.getId_voyage());
                            return destinationFilter.equals(info.destination);
                        })
                        .collect(Collectors.toList());
            }
        }

        LocalDate debut = dateDebut != null ? dateDebut.getValue() : null;
        LocalDate fin = dateFin != null ? dateFin.getValue() : null;

        if (debut != null || fin != null) {
            resultats = resultats.stream()
                    .filter(i -> {
                        VoyageInfo info = getVoyageInfo(i.getId_voyage());
                        if (info.dateDebut == null || info.dateFin == null) {
                            return debut == null && fin == null;
                        }
                        boolean apresDebut = debut == null || !info.dateFin.isBefore(debut);
                        boolean avantFin = fin == null || !info.dateDebut.isAfter(fin);
                        return apresDebut && avantFin;
                    })
                    .collect(Collectors.toList());
        }

        itinerairesAffiches = resultats;
        appliquerTri();
    }

    private void appliquerRechercheEtFiltres() {
        if (tousLesItineraires == null) return;

        if (voyageActuel != null) {
            itinerairesAffiches = tousLesItineraires.stream()
                    .filter(i -> i.getId_voyage() == voyageActuel.getId_voyage())
                    .collect(Collectors.toList());
        } else {
            itinerairesAffiches = new ArrayList<>(tousLesItineraires);
        }

        appliquerFiltresSupplementaires();
    }

    // ============== MÉTHODES EXISTANTES ==============
    private void chargerTousLesItineraires() {
        try {
            tousLesItineraires = itineraireCRUD.afficher();
            System.out.println("Itinéraires chargés: " + tousLesItineraires.size());

            if (voyageActuel != null) {
                itinerairesAffiches = tousLesItineraires.stream()
                        .filter(i -> i.getId_voyage() == voyageActuel.getId_voyage())
                        .collect(Collectors.toList());
                System.out.println("Filtrage par voyage ID " + voyageActuel.getId_voyage() +
                        ": " + itinerairesAffiches.size() + " itinéraires");
            } else {
                itinerairesAffiches = new ArrayList<>(tousLesItineraires);
            }

            mettreAJourStatistiques();
            appliquerFiltresSupplementaires();

        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtil.showError("Erreur", "Impossible de charger les itinéraires: " + e.getMessage());
        }
    }

    private void chargerDestinations() {
        if (comboDestination == null) return;

        ObservableList<String> destinations = FXCollections.observableArrayList();
        destinations.add("Toutes les destinations");

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = MyBD.getInstance().getConn();
            String query = "SELECT DISTINCT d.nom_destination FROM destination d ORDER BY d.nom_destination";
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();

            while (rs.next()) {
                destinations.add(rs.getString("nom_destination"));
            }

            comboDestination.setItems(destinations);
            comboDestination.getSelectionModel().selectFirst();

            if (lblDestinationsCount != null) {
                lblDestinationsCount.setText(destinations.size() - 1 + " destinations");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private VoyageInfo getVoyageInfo(int idVoyage) {
        if (voyageInfoCache.containsKey(idVoyage)) {
            return voyageInfoCache.get(idVoyage);
        }

        String titre = "Voyage";
        String destination = "";
        int nbJours = 5;
        LocalDate dateDebut = null;
        LocalDate dateFin = null;

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = MyBD.getInstance().getConn();
            String query = "SELECT v.titre_voyage, v.date_debut, v.date_fin, d.nom_destination " +
                    "FROM voyage v " +
                    "LEFT JOIN destination d ON v.id_destination = d.id_destination " +
                    "WHERE v.id_voyage = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, idVoyage);
            rs = stmt.executeQuery();

            if (rs.next()) {
                titre = rs.getString("titre_voyage");
                destination = rs.getString("nom_destination");

                Timestamp tsDebut = rs.getTimestamp("date_debut");
                Timestamp tsFin = rs.getTimestamp("date_fin");

                if (tsDebut != null) {
                    dateDebut = tsDebut.toLocalDateTime().toLocalDate();
                }
                if (tsFin != null) {
                    dateFin = tsFin.toLocalDateTime().toLocalDate();
                }

                if (dateDebut != null && dateFin != null) {
                    long diff = java.time.Duration.between(dateDebut.atStartOfDay(), dateFin.atStartOfDay()).toDays();
                    nbJours = (int) diff + 1;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        VoyageInfo info = new VoyageInfo(titre, destination, nbJours, dateDebut, dateFin);
        voyageInfoCache.put(idVoyage, info);
        return info;
    }

    private void appliquerTri() {
        if (itinerairesAffiches == null || itinerairesAffiches.isEmpty() || triCombo == null) {
            updateItineraryDisplay();
            return;
        }

        String selectedTri = triCombo.getSelectionModel().getSelectedItem();
        if (selectedTri == null || selectedTri.equals("Par défaut")) {
            updateItineraryDisplay();
            return;
        }

        try {
            switch (selectedTri) {
                case "Nom (A → Z)":
                    itinerairesAffiches = itineraireCRUD.trierParNom(itinerairesAffiches, true);
                    break;
                case "Nom (Z → A)":
                    itinerairesAffiches = itineraireCRUD.trierParNom(itinerairesAffiches, false);
                    break;
                case "Date (plus récent)":
                    itinerairesAffiches = trierParDate(itinerairesAffiches, false);
                    break;
                case "Date (plus ancien)":
                    itinerairesAffiches = trierParDate(itinerairesAffiches, true);
                    break;
                case "Nombre de jours (croissant)":
                    itinerairesAffiches = trierParNombreJours(itinerairesAffiches, true);
                    break;
                case "Nombre de jours (décroissant)":
                    itinerairesAffiches = trierParNombreJours(itinerairesAffiches, false);
                    break;
                default:
                    break;
            }
            updateItineraryDisplay();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<Itineraire> trierParDate(List<Itineraire> list, boolean ascendant) {
        return list.stream()
                .sorted((i1, i2) -> {
                    VoyageInfo info1 = getVoyageInfo(i1.getId_voyage());
                    VoyageInfo info2 = getVoyageInfo(i2.getId_voyage());

                    if (info1.dateDebut == null && info2.dateDebut == null) return 0;
                    if (info1.dateDebut == null) return ascendant ? 1 : -1;
                    if (info2.dateDebut == null) return ascendant ? -1 : 1;

                    int comparison = info1.dateDebut.compareTo(info2.dateDebut);
                    return ascendant ? comparison : -comparison;
                })
                .collect(Collectors.toList());
    }

    private List<Itineraire> trierParNombreJours(List<Itineraire> list, boolean ascendant) {
        return list.stream()
                .sorted((i1, i2) -> {
                    VoyageInfo info1 = getVoyageInfo(i1.getId_voyage());
                    VoyageInfo info2 = getVoyageInfo(i2.getId_voyage());

                    int comparison = Integer.compare(info1.nbJours, info2.nbJours);
                    return ascendant ? comparison : -comparison;
                })
                .collect(Collectors.toList());
    }

    private void mettreAJourStatistiques() {
        if (tousLesItineraires == null) return;

        int totalJours = 0;
        for (Itineraire itineraire : tousLesItineraires) {
            VoyageInfo info = getVoyageInfo(itineraire.getId_voyage());
            totalJours += info.nbJours;
        }

        if (lblJoursCount != null) {
            lblJoursCount.setText(totalJours + " jours d'aventure");
        }

        if (lblItinerairesActifsFooter != null) {
            long actifs = tousLesItineraires.stream()
                    .filter(i -> {
                        VoyageInfo info = getVoyageInfo(i.getId_voyage());
                        LocalDate now = LocalDate.now();
                        return info.dateDebut != null && !info.dateFin.isBefore(now);
                    })
                    .count();
            lblItinerairesActifsFooter.setText(actifs + " itinéraire" + (actifs > 1 ? "s" : "") + " actif" + (actifs > 1 ? "s" : ""));
        }
    }

    private void updateItineraryDisplay() {
        if (itinerairesAffiches == null || itinerairesAffiches.isEmpty()) {
            emptyPlaceholder.setVisible(true);
            emptyPlaceholder.setManaged(true);
            if (itinerariesContainer != null) {
                itinerariesContainer.setVisible(false);
                itinerariesContainer.setManaged(false);
            }

            if (lblListeTitre != null) {
                if (voyageActuel != null) {
                    lblListeTitre.setText("Aucun itinéraire pour " + voyageActuel.getTitre_voyage());
                } else {
                    String destinationFilter = comboDestination != null ? comboDestination.getValue() : null;
                    if (destinationFilter != null && !destinationFilter.equals("Toutes les destinations")) {
                        lblListeTitre.setText("Aucun itinéraire pour " + destinationFilter);
                    } else {
                        lblListeTitre.setText("Aucun itinéraire trouvé");
                    }
                }
            }

            if (lblTotalItinerairesTable != null) {
                lblTotalItinerairesTable.setText("0 itinéraire");
            }
            if (lblResultCount != null) {
                lblResultCount.setText("0");
            }
        } else {
            emptyPlaceholder.setVisible(false);
            emptyPlaceholder.setManaged(false);
            if (itinerariesContainer != null) {
                itinerariesContainer.setVisible(true);
                itinerariesContainer.setManaged(true);

                if (lblListeTitre != null) {
                    if (voyageActuel != null) {
                        lblListeTitre.setText("Itinéraires de " + voyageActuel.getTitre_voyage());
                    } else {
                        String destinationFilter = comboDestination != null ? comboDestination.getValue() : null;
                        if (destinationFilter != null && !destinationFilter.equals("Toutes les destinations")) {
                            lblListeTitre.setText("Itinéraires à " + destinationFilter);
                        } else {
                            lblListeTitre.setText("Tous les itinéraires");
                        }
                    }
                }

                lblTotalItinerairesTable.setText(itinerairesAffiches.size() + " itinéraire" + (itinerairesAffiches.size() > 1 ? "s" : ""));
                if (lblResultCount != null) {
                    lblResultCount.setText(String.valueOf(itinerairesAffiches.size()));
                }
                generateItineraryCards();
            }
        }
    }

    private void generateItineraryCards() {
        itinerariesContainer.getChildren().clear();
        for (Itineraire itineraire : itinerairesAffiches) {
            try {
                VBox card = createItineraryCard(itineraire);
                itinerariesContainer.getChildren().add(card);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private VBox createItineraryCard(Itineraire itineraire) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ItineraireEtEtape/ItineraryCard.fxml"));
            VBox card = loader.load();

            ItineraryCardController controller = loader.getController();

            if (controller == null) {
                System.err.println("Erreur: ItineraryCardController est null");
                return createSimpleCard(itineraire);
            }

            VoyageInfo voyageInfo = getVoyageInfo(itineraire.getId_voyage());
            String affichage = voyageInfo.titre;
            if (voyageInfo.destination != null && !voyageInfo.destination.isEmpty()) {
                affichage += " - " + voyageInfo.destination;
            }

            int nombreJours = 0;
            if (voyageInfo.dateDebut != null && voyageInfo.dateFin != null) {
                nombreJours = (int) (java.time.temporal.ChronoUnit.DAYS.between(voyageInfo.dateDebut, voyageInfo.dateFin) + 1);
            }

            controller.setData(
                    itineraire,
                    affichage,
                    nombreJours,
                    getEmojiForItinerary(itineraire),
                    this::handleModifierItineraire,
                    this::handleSupprimerItineraire,
                    this::handleJourClick,
                    this::handleExporterItineraire
            );

            return card;

        } catch (IOException e) {
            e.printStackTrace();
            return createSimpleCard(itineraire);
        }
    }

    private VBox createSimpleCard(Itineraire itineraire) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-border-radius: 16; -fx-padding: 16;");

        Label nameLabel = new Label(itineraire.getNom_itineraire());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");

        Label descLabel = new Label(itineraire.getDescription_itineraire());
        descLabel.setWrapText(true);

        card.getChildren().addAll(nameLabel, descLabel);
        return card;
    }

    private String getEmojiForItinerary(Itineraire itineraire) {
        String nom = itineraire.getNom_itineraire().toLowerCase();
        if (nom.contains("plage") || nom.contains("mer")) return "🏖️";
        if (nom.contains("montagne") || nom.contains("ski")) return "🏔️";
        if (nom.contains("rome") || nom.contains("histoire")) return "🏛️";
        if (nom.contains("paris") || nom.contains("ville")) return "🌆";
        return "🗺️";
    }

    private void handleModifierItineraire(Itineraire itineraire) {
        try {
            System.out.println("Modification itinéraire: " + itineraire.getNom_itineraire());

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ItineraireEtEtape/PageModifierItineraire.fxml"));
            Parent modifierView = loader.load();

            PageModifierItineraire modifierController = loader.getController();
            modifierController.setItineraire(itineraire);
            modifierController.setVoyageActuel(this.voyageActuel);

            Stage stage = (Stage) itinerariesContainer.getScene().getWindow();
            stage.setScene(new Scene(modifierView));
            stage.setTitle("TravelMate - Modifier Itinéraire");
            stage.setMaximized(true);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Erreur", "Impossible d'ouvrir le formulaire de modification: " + e.getMessage());
        }
    }

    @FXML
    private void handleCreerClick() {
        try {
            System.out.println("Création d'un nouvel itinéraire");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ItineraireEtEtape/PageAjoutItineraire.fxml"));

            if (loader.getLocation() == null) {
                AlertUtil.showError("Erreur", "Fichier FXML non trouvé: /ItineraireEtEtape/PageAjoutItineraire.fxml");
                return;
            }

            Parent ajoutView = loader.load();

            PageAjoutItineraire ajoutController = loader.getController();
            ajoutController.setVoyageActuel(this.voyageActuel);

            if (voyageActuel != null) {
                ajoutController.preselectionnerVoyage(voyageActuel.getId_voyage());
            }

            Stage stage = (Stage) itinerariesContainer.getScene().getWindow();
            stage.setScene(new Scene(ajoutView));
            stage.setTitle("TravelMate - Nouvel Itinéraire");
            stage.setMaximized(true);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage());
        }
    }

    private void handleSupprimerItineraire(Itineraire itineraire) {
        if (AlertUtil.showConfirmation("Confirmation de suppression",
                "Êtes-vous sûr de vouloir supprimer l'itinéraire \"" + itineraire.getNom_itineraire() + "\" ?")) {
            try {
                itineraireCRUD.supprimer(itineraire.getId_itineraire());
                chargerTousLesItineraires();
                AlertUtil.showInfo("Succès", "Itinéraire supprimé avec succès !");
            } catch (SQLException e) {
                e.printStackTrace();
                AlertUtil.showError("Erreur", "Impossible de supprimer l'itinéraire: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleRechercherClick() {
        appliquerRechercheEtFiltres();
    }

    @FXML
    private void reinitialiserFiltres() {
        if (comboDestination != null && voyageActuel == null) {
            comboDestination.getSelectionModel().selectFirst();
        }
        if (dateDebut != null) {
            dateDebut.setValue(null);
        }
        if (dateFin != null) {
            dateFin.setValue(null);
        }
        if (searchField != null) {
            searchField.clear();
        }
        if (triCombo != null) {
            triCombo.getSelectionModel().selectFirst();
        }

        if (voyageActuel != null) {
            itinerairesAffiches = tousLesItineraires.stream()
                    .filter(i -> i.getId_voyage() == voyageActuel.getId_voyage())
                    .collect(Collectors.toList());
        } else if (tousLesItineraires != null) {
            itinerairesAffiches = new ArrayList<>(tousLesItineraires);
        }

        appliquerTri();
    }

    @FXML
    private void handleExporter() {
        if (itinerairesAffiches != null && !itinerairesAffiches.isEmpty()) {
            // Proposer le choix entre export simple et Excel
            ChoiceDialog<String> dialog = new ChoiceDialog<>("Excel avec mise en forme",
                    "Excel avec mise en forme", "Export simple (CSV)");
            dialog.setTitle("Type d'export");
            dialog.setHeaderText("Choisissez le format d'export");
            dialog.setContentText("Format :");

            dialog.showAndWait().ifPresent(choice -> {
                if (choice.equals("Excel avec mise en forme")) {
                    // Utiliser la méthode Excel
                    ExportUtil.exporterTousItinerairesExcel(itinerairesAffiches, etapeCRUD,
                            itinerariesContainer.getScene().getWindow());
                } else {
                    // Utiliser la méthode CSV simple
                    ExportUtil.exporterTousItineraires(itinerairesAffiches, etapeCRUD,
                            itinerariesContainer.getScene().getWindow());
                }
            });
        } else {
            AlertUtil.showWarning("Aucun itinéraire", "Il n'y a aucun itinéraire à exporter.");
        }
    }

    private void handleExporterItineraire(Itineraire itineraire) {
        try {
            List<etape> etapes = etapeCRUD.getEtapesByItineraire(itineraire.getId_itineraire());

            // Proposer le choix entre export simple et Excel
            ChoiceDialog<String> dialog = new ChoiceDialog<>("Excel avec mise en forme",
                    "Excel avec mise en forme", "Export simple (CSV)");
            dialog.setTitle("Type d'export");
            dialog.setHeaderText("Choisissez le format d'export pour : " + itineraire.getNom_itineraire());
            dialog.setContentText("Format :");

            dialog.showAndWait().ifPresent(choice -> {
                if (choice.equals("Excel avec mise en forme")) {
                    // Utiliser la méthode Excel
                    ExportUtil.exporterItineraireExcel(itineraire, etapes,
                            itinerariesContainer.getScene().getWindow());
                } else {
                    // Utiliser la méthode CSV simple
                    ExportUtil.exporterItineraire(itineraire, etapes,
                            itinerariesContainer.getScene().getWindow());
                }
            });

        } catch (SQLException e) {
            AlertUtil.showError("Erreur", "Impossible de charger les étapes: " + e.getMessage());
        }
    }

    private void handleJourClick(int numeroJour, int idItineraire, String nomItineraire) {
        try {
            System.out.println("Clic sur le jour " + numeroJour + " de l'itinéraire: " + nomItineraire);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ItineraireEtEtape/PageEtape.fxml"));
            Parent detailsJourView = loader.load();

            PageEtape detailsJourController = loader.getController();
            detailsJourController.setJourInfo(numeroJour, idItineraire, nomItineraire);

            Stage stage = (Stage) itinerariesContainer.getScene().getWindow();
            stage.setScene(new Scene(detailsJourView));
            stage.setTitle("TravelMate - Jour " + numeroJour + " - " + nomItineraire);
            stage.setMaximized(true);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Erreur", "Impossible d'ouvrir les détails du jour: " + e.getMessage());
        }
    }

    @FXML
    private void handleRetour() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/PageVoyage.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) itinerariesContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TravelMate - Voyages");
            stage.setMaximized(true);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Erreur", "Impossible de retourner à la page des voyages");
        }
    }

    /**
     * Méthode publique pour exporter un itinéraire individuel (appelée depuis la carte)
     */
    public void exporterItineraire(Itineraire itineraire) {
        handleExporterItineraire(itineraire);
    }
}