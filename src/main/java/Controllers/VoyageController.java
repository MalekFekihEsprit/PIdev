package Controllers;

import Services.ItineraryGeneratorService;
import Services.itineraireCRUD;
import Entities.Itineraire;
import Entities.Voyage;
import Services.VoyageCRUDV;
import Services.ActiviteVoyageService;
import Utils.MyBD;
import Utils.UserSession;
import Entities.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javafx.application.Platform;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class VoyageController {

    public static VoyageController instance;

    // ============== FXML INJECTED ELEMENTS ==============

    // Boutons de navigation
    @FXML private HBox btnDestinations;
    @FXML private HBox btnHebergement;
    @FXML private HBox btnActivites;
    @FXML private HBox btnVoyages;      // Bouton actuel
    @FXML private HBox btnBudgets;
    @FXML private HBox btnCategories;
    @FXML private HBox userProfileBox;
    @FXML private Label lblUserName;
    @FXML private Label lblUserRole;
    private ItineraryGeneratorService itineraryGeneratorService = new ItineraryGeneratorService();
    private itineraireCRUD itineraireCRUD = new itineraireCRUD();

    // Boutons existants
    @FXML
    private Button btnAnnulerModification;
    @FXML
    private Button btnConfirmerModification;
    @FXML
    private Button btnAjouter;
    @FXML
    private Button btnModifierFormulaire;

    @FXML
    private DatePicker fxdated;
    @FXML
    private DatePicker fxdatef;
    @FXML
    private ComboBox<DestinationItem> cbDestination;
    @FXML
    private ComboBox<String> fxstatut;
    @FXML
    private TextField fxtitre;

    // Nouveaux éléments pour la sélection d'activités
    @FXML
    private ComboBox<String> cbLieuActivite;
    @FXML
    private ComboBox<String> cbActivite;
    @FXML
    private ListView<String> lvActivitesSelectionnees;
    @FXML
    private Button btnAjouterActivite;
    @FXML
    private Button btnRetirerActivite;
    @FXML
    private Label lblActivitesCount;

    // Nouveaux éléments pour la recherche
    @FXML
    private TextField tfSearchTitre;
    @FXML
    private ComboBox<String> cbFilterStatut;
    @FXML
    private Button btnSearch;
    @FXML
    private Button btnReset;
    @FXML
    private Label lblResultatsRecherche;
    @FXML
    private Label lblVoyagesActifsFooter;

    @FXML
    private Label lblVoyagesCount;
    @FXML
    private VBox voyagesContainer;

    // Variables de classe
    private int voyageEnCoursDeModification = -1;
    private VoyageCRUDV voyageCRUD = new VoyageCRUDV();
    private ActiviteVoyageService activiteVoyageService = new ActiviteVoyageService();
    private ObservableList<Voyage> voyageList = FXCollections.observableArrayList();
    private FilteredList<Voyage> filteredData;

    // Listes pour les activités
    private ObservableList<String> activitesDisponibles = FXCollections.observableArrayList();
    private ObservableList<String> activitesSelectionnees = FXCollections.observableArrayList();
    private Map<String, Integer> activiteIdMap = new HashMap<>();

    // Mapping des villes/régions vers les pays
    private Map<String, String> mappingVillePays = new HashMap<>();

    // Classe interne pour représenter une destination dans le ComboBox
    public static class DestinationItem {
        private int id;
        private String pays;
        private String nom;

        public DestinationItem(int id, String pays, String nom) {
            this.id = id;
            this.pays = pays;
            this.nom = nom;
        }

        public int getId() { return id; }
        public String getPays() { return pays; }
        public String getNom() { return nom; }

        @Override
        public String toString() {
            return pays + " - " + nom;
        }
    }

    @FXML
    public void initialize() {
        instance = this;

        // Initialiser le mapping des villes vers les pays
        initialiserMappingVillePays();

        // Initialiser la ComboBox des statuts
        fxstatut.getItems().addAll("a venir", "En cours", "Terminé", "Annulé");
        fxstatut.setValue("a venir");

        // Initialiser la ComboBox de filtre
        cbFilterStatut.getItems().addAll("Tous", "a venir", "En cours", "Terminé", "Annulé");
        cbFilterStatut.setValue("Tous");

        // Configurer les validateurs de dates
        configurerDatePickers();

        // Configurer la ComboBox de destination
        configurerComboBoxDestination();

        // Configurer la ComboBox de lieu d'activité
        cbLieuActivite.setPromptText("Choisir un lieu");
        cbActivite.setPromptText("Choisir une activité");

        // Configurer la ListView
        configurerListView();

        // Configurer les boutons de recherche
        btnSearch.setOnAction(this::rechercherVoyages);
        btnReset.setOnAction(this::resetRecherche);

        // Configurer les boutons d'activités
        btnAjouterActivite.setOnAction(this::ajouterActivite);
        btnRetirerActivite.setOnAction(this::retirerActivite);

        // Configurer les boutons de navigation
        configurerNavigation();

        // Configurer le profil utilisateur
        configurerUserProfile();
        updateUserInfo();

        // Listener sur le changement de destination
        cbDestination.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                chargerLieuxParPays(newValue.getPays());
            } else {
                cbLieuActivite.getItems().clear();
                cbActivite.getItems().clear();
            }
        });

        // Listener sur le changement de lieu
        cbLieuActivite.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                chargerActivitesParLieu(newValue);
            } else {
                cbActivite.getItems().clear();
            }
        });

        // Charger les voyages
        chargerVoyages();
    }

    /**
     * Initialise le mapping entre les villes/régions et leurs pays
     */
    private void initialiserMappingVillePays() {
        mappingVillePays.put("Paris", "France");
        mappingVillePays.put("Alpes", "France");
        mappingVillePays.put("Milan", "Italie");
        mappingVillePays.put("Marrakech", "Maroc");
        mappingVillePays.put("Tokyo", "Japon");
        System.out.println("Mapping ville->pays initialisé: " + mappingVillePays);
    }

    /**
     * Configure les boutons de navigation
     */
    private void configurerNavigation() {
        // Bouton Destinations
        if (btnDestinations != null) {
            btnDestinations.setOnMouseClicked(event -> navigateToDestinations());
        }

        // Bouton Hébergement (NOUVEAU)
        if (btnHebergement != null) {
            btnHebergement.setOnMouseClicked(event -> navigateToHebergement());
        }

        // Bouton Activités
        if (btnActivites != null) {
            btnActivites.setOnMouseClicked(event -> navigateToActivites());
        }

        // Bouton Catégories
        if (btnCategories != null) {
            btnCategories.setOnMouseClicked(event -> navigateToCategories());
        }

        // Bouton Budgets
        if (btnBudgets != null) {
            btnBudgets.setOnMouseClicked(event -> showNotImplementedAlert("Budgets"));
        }
        if (btnVoyages != null) {
            btnVoyages.setOnMouseClicked(event -> navigateToVoyages());
        }
    }

    /**
     * Configure le profil utilisateur
     */
    private void configurerUserProfile() {
        if (userProfileBox != null) {
            userProfileBox.setOnMouseClicked(event -> navigateToProfile());

            // Effet de survol
            userProfileBox.setOnMouseEntered(event -> {
                userProfileBox.setStyle("-fx-background-color: #e2e8f0; -fx-background-radius: 25; -fx-padding: 5 14 5 5; -fx-cursor: hand; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-border-radius: 25;");
            });

            userProfileBox.setOnMouseExited(event -> {
                userProfileBox.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 25; -fx-padding: 5 14 5 5; -fx-cursor: hand; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-border-radius: 25;");
            });
        }
    }

    /**
     * Met à jour les informations utilisateur
     */
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

    /**
     * Navigue vers la page des destinations
     */
    private void navigateToDestinations() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DestinationFront.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnDestinations.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TravelMate - Destinations");
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les destinations: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private void navigateToVoyages() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/BudgetDepenseFront.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnDestinations.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TravelMate - Budgets");
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les Budgets: " + e.getMessage());
            e.printStackTrace();
        }
    }


    /**
     * Navigue vers la page des hébergements (NOUVEAU)
     */
    private void navigateToHebergement() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/HebergementFront.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnHebergement.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TravelMate - Hébergements");
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les hébergements: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Navigue vers la page des activités
     */
    private void navigateToActivites() {
        try {
            // Réinitialiser le filtre de catégorie si nécessaire
            CategorieContext.categorieFiltre = null;

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/activitesfront.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnActivites.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TravelMate - Activités");
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les activités: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Navigue vers la page des catégories
     */
    private void navigateToCategories() {
        try {
            // Réinitialiser le filtre de catégorie
            CategorieContext.categorieFiltre = null;

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/categoriesfront.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnCategories.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TravelMate - Catégories");
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les catégories: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Navigue vers le profil utilisateur
     */
    private void navigateToProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/profile.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) userProfileBox.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TravelMate - Mon Profil");
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le profil: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Affiche une alerte pour les fonctionnalités non implémentées
     */
    private void showNotImplementedAlert(String feature) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Fonctionnalité à venir");
        alert.setHeaderText(null);
        alert.setContentText("La fonctionnalité \"" + feature + "\" sera bientôt disponible !");
        alert.showAndWait();
    }

    private void configurerComboBoxDestination() {
        // Charger les destinations depuis la base de données
        ObservableList<DestinationItem> destinations = FXCollections.observableArrayList();

        String query = "SELECT id_destination, pays_destination, nom_destination FROM destination ORDER BY pays_destination, nom_destination";

        try (Connection conn = MyBD.getInstance().getConn();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                int id = rs.getInt("id_destination");
                String pays = rs.getString("pays_destination");
                String nom = rs.getString("nom_destination");
                destinations.add(new DestinationItem(id, pays, nom));
            }

            cbDestination.setItems(destinations);

            // Configurer l'affichage du ComboBox
            cbDestination.setConverter(new StringConverter<DestinationItem>() {
                @Override
                public String toString(DestinationItem item) {
                    return item == null ? "" : item.getPays() + " - " + item.getNom();
                }

                @Override
                public DestinationItem fromString(String string) {
                    return null; // Non utilisé
                }
            });

            // Sélectionner le premier élément par défaut si disponible
            if (!destinations.isEmpty()) {
                cbDestination.setValue(destinations.get(0));
            }

        } catch (SQLException e) {
            System.err.println("Erreur chargement destinations: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les destinations: " + e.getMessage());
        }
    }

    private void configurerListView() {
        lvActivitesSelectionnees.setItems(activitesSelectionnees);

        // Mettre à jour le compteur quand la sélection change
        activitesSelectionnees.addListener((javafx.collections.ListChangeListener.Change<? extends String> c) -> {
            mettreAJourCompteurActivites();
        });
    }

    private void mettreAJourCompteurActivites() {
        int count = activitesSelectionnees.size();
        lblActivitesCount.setText(count + " activité(s) sélectionnée(s)");
    }

    /**
     * Charge les lieux d'activités qui correspondent au pays sélectionné
     */
    private void chargerLieuxParPays(String pays) {
        List<String> tousLesLieux = new ArrayList<>();
        List<String> lieuxFiltres = new ArrayList<>();

        System.out.println("=== CHARGEMENT DES LIEUX POUR LE PAYS: " + pays + " ===");

        // Récupérer tous les lieux distincts de la table activites
        String query = "SELECT DISTINCT lieu FROM activites WHERE lieu IS NOT NULL AND lieu != '' ORDER BY lieu";

        try (Connection conn = MyBD.getInstance().getConn();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                tousLesLieux.add(rs.getString("lieu"));
            }

            System.out.println("Tous les lieux disponibles dans la base: " + tousLesLieux);

            // Filtrer les lieux qui appartiennent au pays sélectionné via le mapping
            for (String lieu : tousLesLieux) {
                // Chercher le pays correspondant à ce lieu dans le mapping
                for (Map.Entry<String, String> entry : mappingVillePays.entrySet()) {
                    if (entry.getKey().equalsIgnoreCase(lieu) && entry.getValue().equalsIgnoreCase(pays)) {
                        lieuxFiltres.add(lieu);
                        System.out.println("✓ Lieu correspondant: " + lieu + " -> " + entry.getValue());
                        break;
                    }
                }
            }

            // Si aucun lieu trouvé avec le mapping, afficher un message
            if (lieuxFiltres.isEmpty()) {
                System.out.println("⚠️ Aucun lieu trouvé pour " + pays + " avec le mapping actuel");
                System.out.println("Mapping actuel: " + mappingVillePays);

                // Option: Afficher un message à l'utilisateur
                String message = "Aucune activité trouvée pour " + pays + ".\n" +
                        "Villes disponibles dans la base : " + tousLesLieux + "\n" +
                        "Mapping actuel : " + mappingVillePays;

                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.WARNING, "Information", message);
                });
            }

            // Mettre à jour la ComboBox des lieux
            cbLieuActivite.getItems().clear();
            cbLieuActivite.getItems().addAll(lieuxFiltres);

            if (!lieuxFiltres.isEmpty()) {
                cbLieuActivite.setValue(lieuxFiltres.get(0));
                System.out.println("Premier lieu sélectionné: " + lieuxFiltres.get(0));
            } else {
                cbLieuActivite.setValue(null);
                cbActivite.getItems().clear();
            }

        } catch (SQLException e) {
            System.err.println("Erreur SQL: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les lieux: " + e.getMessage());
        }
    }

    /**
     * Charge les activités pour un lieu spécifique
     */
    private void chargerActivitesParLieu(String lieu) {
        activitesDisponibles.clear();
        activiteIdMap.clear();

        System.out.println("=== CHARGEMENT DES ACTIVITÉS POUR LE LIEU: " + lieu + " ===");

        String query = "SELECT id, nom, duree, budget FROM activites WHERE lieu = ? ORDER BY nom";

        try (Connection conn = MyBD.getInstance().getConn();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, lieu);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String nom = rs.getString("nom");
                float duree = rs.getFloat("duree");
                float budget = rs.getFloat("budget");

                String affichage = nom + " (" + duree + "h - " + budget + "€)";
                activitesDisponibles.add(affichage);
                activiteIdMap.put(affichage, id);

                System.out.println("✓ Activité chargée: '" + affichage + "' -> ID: " + id);
            }

            System.out.println("Map des activités: " + activiteIdMap);

            cbActivite.setItems(activitesDisponibles);
            if (!activitesDisponibles.isEmpty()) {
                cbActivite.setValue(activitesDisponibles.get(0));
                System.out.println("Première activité sélectionnée: " + activitesDisponibles.get(0));
            } else {
                cbActivite.setValue(null);
                System.out.println("Aucune activité trouvée pour ce lieu");
            }

        } catch (SQLException e) {
            System.err.println("Erreur chargement activités: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les activités: " + e.getMessage());
        }
    }

    @FXML
    private void ajouterActivite(ActionEvent event) {
        String selected = cbActivite.getValue();
        if (selected != null && !selected.isEmpty()) {
            // Vérifier que l'ID existe dans la map
            Integer id = activiteIdMap.get(selected);
            if (id == null) {
                System.err.println("ERREUR: ID non trouvé pour l'activité: " + selected);
                showAlert(Alert.AlertType.ERROR, "Erreur",
                        "Impossible d'ajouter cette activité: ID non trouvé");
                return;
            }

            if (!activitesSelectionnees.contains(selected)) {
                activitesSelectionnees.add(selected);
                System.out.println("Activité ajoutée: '" + selected + "' (ID: " + id + ")");
            } else {
                System.out.println("Activité déjà sélectionnée: " + selected);
            }
        }
    }

    @FXML
    private void retirerActivite(ActionEvent event) {
        String selected = lvActivitesSelectionnees.getSelectionModel().getSelectedItem();
        if (selected != null) {
            activitesSelectionnees.remove(selected);
            System.out.println("Activité retirée: " + selected);
        }
    }

    private void configurerDatePickers() {
        // Date début: ne peut pas être avant aujourd'hui
        fxdated.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate aujourdHui = LocalDate.now();
                setDisable(empty || date.isBefore(aujourdHui));
            }
        });

        // Date fin: doit être après date début
        fxdatef.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate dateDebut = fxdated.getValue();
                if (dateDebut != null) {
                    setDisable(empty || !date.isAfter(dateDebut));
                }
            }
        });

        fxdated.valueProperty().addListener((observable, oldValue, newValue) -> {
            fxdatef.setValue(null);
            fxdatef.setDayCellFactory(picker -> new DateCell() {
                @Override
                public void updateItem(LocalDate date, boolean empty) {
                    super.updateItem(date, empty);
                    LocalDate dateDebut = fxdated.getValue();
                    if (dateDebut != null) {
                        setDisable(empty || !date.isAfter(dateDebut));
                    }
                }
            });
        });
    }

    @FXML
    private void rechercherVoyages(ActionEvent event) {
        String titreRecherche = tfSearchTitre.getText().toLowerCase();
        String statutFiltre = cbFilterStatut.getValue();

        // Créer un prédicat pour filtrer les voyages
        Predicate<Voyage> predicate = voyage -> {
            boolean matchTitre = true;
            boolean matchStatut = true;

            // Filtre par titre
            if (titreRecherche != null && !titreRecherche.isEmpty()) {
                matchTitre = voyage.getTitre_voyage().toLowerCase().contains(titreRecherche);
            }

            // Filtre par statut
            if (statutFiltre != null && !statutFiltre.equals("Tous")) {
                matchStatut = voyage.getStatut().equals(statutFiltre);
            }

            return matchTitre && matchStatut;
        };

        // Appliquer le filtre
        filteredData = new FilteredList<>(voyageList, predicate);

        // Afficher les résultats
        afficherVoyagesFiltres();

        // Mettre à jour le label de résultat
        int resultCount = filteredData.size();
        if (resultCount == 0) {
            lblResultatsRecherche.setText("Aucun résultat trouvé");
        } else {
            lblResultatsRecherche.setText(resultCount + " résultat(s) trouvé(s)");
        }
    }

    @FXML
    private void resetRecherche(ActionEvent event) {
        tfSearchTitre.clear();
        cbFilterStatut.setValue("Tous");
        filteredData = null;
        lblResultatsRecherche.setText("");
        chargerVoyages();
    }

    private void afficherVoyagesFiltres() {
        try {
            voyagesContainer.getChildren().clear();

            List<Voyage> voyagesAfficher;
            if (filteredData != null) {
                voyagesAfficher = filteredData;
            } else {
                voyagesAfficher = voyageList;
            }

            int nbVoyages = voyagesAfficher.size();
            lblVoyagesCount.setText(nbVoyages + " voyage" + (nbVoyages > 1 ? "s" : ""));

            // Compter les voyages actifs (En cours ou à venir)
            long actifs = voyagesAfficher.stream()
                    .filter(v -> "En cours".equals(v.getStatut()) || "a venir".equals(v.getStatut()))
                    .count();
            lblVoyagesActifsFooter.setText(actifs + " voyage" + (actifs > 1 ? "s" : "") + " actif" + (actifs > 1 ? "s" : ""));

            if (voyagesAfficher.isEmpty()) {
                Label aucunVoyage = new Label("Aucun voyage trouvé");
                aucunVoyage.setStyle("-fx-text-fill: #64748b; -fx-font-size: 14; -fx-padding: 20;");
                voyagesContainer.getChildren().add(aucunVoyage);
                return;
            }

            for (Voyage voyage : voyagesAfficher) {
                String nomDestination = voyageCRUD.getNomDestination(voyage.getId_destination());

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/CarteVoyage.fxml"));
                VBox carte = loader.load();

                CarteVoyageController carteController = loader.getController();
                carteController.setDonnees(voyage, nomDestination);

                voyagesContainer.getChildren().add(carte);
            }

        } catch (SQLException | IOException e) {
            System.err.println("ERREUR affichage voyages: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'afficher les voyages: " + e.getMessage());
        }
    }

    @FXML
    void saveVoyage(ActionEvent event) {
        if (!validerFormulaire()) return;

        try {
            Voyage v = construireVoyageFromFormulaire();

            // Ajouter le voyage et récupérer son ID
            int idVoyage = voyageCRUD.ajouterEtRetournerId(v);
            v.setId_voyage(idVoyage);

            // Ajouter les activités sélectionnées
            if (!activitesSelectionnees.isEmpty()) {
                List<Integer> idsActivites = new ArrayList<>();
                for (String activite : activitesSelectionnees) {
                    Integer id = activiteIdMap.get(activite);
                    if (id != null) {
                        idsActivites.add(id);
                    }
                }
                if (!idsActivites.isEmpty()) {
                    activiteVoyageService.associerActivitesAVoyage(idVoyage, idsActivites);
                }
            }

            // ===== GÉNÉRATION AUTOMATIQUE D'ITINÉRAIRE =====
            String nomDestination = cbDestination.getValue().getNom();
            String pays = cbDestination.getValue().getPays();

            // Appeler la méthode du CRUD pour générer l'itinéraire
            voyageCRUD.genererItinerairePourVoyage(v, nomDestination, pays);

            showAlert(Alert.AlertType.INFORMATION, "Succès",
                    "Voyage ajouté avec succès!\n" +
                            activitesSelectionnees.size() + " activité(s) sélectionnée(s)." +
                            "\nUn itinéraire a été généré automatiquement.");

            chargerVoyages();
            resetForm();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur BD", "Erreur lors de l'ajout: " + e.getMessage());
        }
    }

    @FXML
    void annulerModification(ActionEvent event) {
        resetForm();
        voyageEnCoursDeModification = -1;
        System.out.println("Modification annulée");
    }

    @FXML
    void confirmerModification(ActionEvent event) {
        if (voyageEnCoursDeModification == -1) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Aucun voyage sélectionné pour modification");
            return;
        }

        if (!validerFormulaire()) return;

        try {
            Voyage v = construireVoyageFromFormulaire();
            v.setId_voyage(voyageEnCoursDeModification);

            voyageCRUD.modifier(v);

            // Mettre à jour les activités associées
            // D'abord supprimer les anciennes associations
            activiteVoyageService.supprimerAssociationsParVoyage(voyageEnCoursDeModification);

            // Puis ajouter les nouvelles
            if (!activitesSelectionnees.isEmpty()) {
                List<Integer> idsActivites = activitesSelectionnees.stream()
                        .map(activite -> activiteIdMap.get(activite))
                        .collect(Collectors.toList());

                activiteVoyageService.associerActivitesAVoyage(voyageEnCoursDeModification, idsActivites);

                System.out.println(idsActivites.size() + " activité(s) associée(s) au voyage " + voyageEnCoursDeModification);
            }

            showAlert(Alert.AlertType.INFORMATION, "Succès",
                    "Voyage modifié avec succès!\n" +
                            activitesSelectionnees.size() + " activité(s) associée(s).");

            resetForm();
            chargerVoyages();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur BD", "Erreur lors de la modification: " + e.getMessage());
        }
    }

    private boolean validerFormulaire() {
        String titre = fxtitre.getText() != null ? fxtitre.getText().trim() : "";
        DestinationItem destination = cbDestination.getValue();

        if (titre.isEmpty() || destination == null ||
                fxdated.getValue() == null || fxdatef.getValue() == null) {

            showAlert(Alert.AlertType.ERROR, "Erreur de validation",
                    "Veuillez remplir tous les champs (titre, destination, dates)");
            return false;
        }

        if (titre.length() < 3) {
            showAlert(Alert.AlertType.ERROR, "Erreur de validation",
                    "Le titre doit contenir au moins 3 caractères");
            return false;
        }
        if (titre.length() > 100) {
            showAlert(Alert.AlertType.ERROR, "Erreur de validation",
                    "Le titre ne peut pas dépasser 100 caractères");
            return false;
        }
        // Apply trimmed values back
        fxtitre.setText(titre);

        LocalDate dateDebutLocal = fxdated.getValue();
        LocalDate dateFinLocal = fxdatef.getValue();
        LocalDate aujourdHui = LocalDate.now();

        // Only validate past dates for new voyages (not for modifications)
        if (voyageEnCoursDeModification == -1 && dateDebutLocal.isBefore(aujourdHui)) {
            showAlert(Alert.AlertType.ERROR, "Erreur de validation",
                    "La date de début doit être aujourd'hui ou une date future");
            return false;
        }

        if (!dateFinLocal.isAfter(dateDebutLocal)) {
            showAlert(Alert.AlertType.ERROR, "Erreur de validation",
                    "La date de fin doit être postérieure à la date de début");
            return false;
        }

        return true;
    }

    private Voyage construireVoyageFromFormulaire() {
        LocalDate dateDebutLocal = fxdated.getValue();
        LocalDate dateFinLocal = fxdatef.getValue();
        DestinationItem destination = cbDestination.getValue();

        Date dateDebut = Date.valueOf(dateDebutLocal);
        Date dateFin = Date.valueOf(dateFinLocal);

        return new Voyage(
                fxtitre.getText(),
                dateDebut,
                dateFin,
                fxstatut.getValue() != null ? fxstatut.getValue() : "a venir",
                destination.getId()
        );
    }

    private void resetForm() {
        fxtitre.clear();
        // Garder la destination sélectionnée par défaut
        if (cbDestination.getItems() != null && !cbDestination.getItems().isEmpty() && cbDestination.getValue() == null) {
            cbDestination.setValue(cbDestination.getItems().get(0));
        }
        fxdated.setValue(null);
        fxdatef.setValue(null);
        fxstatut.setValue("a venir");

        // Réinitialiser les listes d'activités
        cbLieuActivite.getItems().clear();
        cbLieuActivite.setValue(null);
        cbActivite.getItems().clear();
        cbActivite.setValue(null);
        activitesSelectionnees.clear();
        activitesDisponibles.clear();
        activiteIdMap.clear();
        lblActivitesCount.setText("0 activité(s) sélectionnée(s)");

        voyageEnCoursDeModification = -1;

        btnAjouter.setVisible(true);
        btnAjouter.setManaged(true);
        btnAjouter.setOnAction(this::saveVoyage);

        btnConfirmerModification.setVisible(false);
        btnConfirmerModification.setManaged(false);

        btnAnnulerModification.setVisible(false);
        btnAnnulerModification.setManaged(false);

        btnModifierFormulaire.setVisible(false);
        btnModifierFormulaire.setManaged(false);

        fxtitre.requestFocus();
    }

    public void chargerVoyages() {
        try {
            System.out.println("=== CHARGEMENT DES VOYAGES ===");

            // Tentative avec gestion de reconnexion
            List<Voyage> voyages = null;
            try {
                voyages = voyageCRUD.afficher();
            } catch (SQLException e) {
                System.err.println("Erreur lors du chargement: " + e.getMessage());

                if (e.getMessage().contains("Connection closed") ||
                        e.getMessage().contains("closed connection") ||
                        e.getMessage().contains("No operations allowed")) {

                    System.out.println("Connexion fermée, tentative de reconnexion...");

                    // Réinitialiser complètement la connexion
                    try {
                        MyBD.getInstance().setConn(null);
                        // Attendre un peu
                        Thread.sleep(500);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }

                    // Réessayer
                    voyages = voyageCRUD.afficher();
                } else {
                    throw e;
                }
            }

            if (voyages != null) {
                voyageList.clear();
                voyageList.addAll(voyages);

                filteredData = null;
                lblResultatsRecherche.setText("");
                afficherVoyagesFiltres();

                System.out.println("=== CHARGEMENT TERMINÉ: " + voyages.size() + " voyages ===");
            }

        } catch (SQLException e) {
            System.err.println("ERREUR chargement voyages: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les voyages: " + e.getMessage());
        }
    }

    public void chargerVoyagePourModification(Voyage voyage) {
        this.voyageEnCoursDeModification = voyage.getId_voyage();

        fxtitre.setText(voyage.getTitre_voyage());

        // Sélectionner la destination correspondante dans le ComboBox
        int idDestination = voyage.getId_destination();
        for (DestinationItem item : cbDestination.getItems()) {
            if (item.getId() == idDestination) {
                cbDestination.setValue(item);
                break;
            }
        }

        LocalDate dateDebut = voyage.getDate_debut().toLocalDate();
        LocalDate dateFin = voyage.getDate_fin().toLocalDate();

        fxdated.setValue(dateDebut);
        fxdatef.setValue(dateFin);
        fxstatut.setValue(voyage.getStatut());

        // Charger les activités déjà associées à ce voyage (optionnel)
        try {
            List<Integer> idsActivites = activiteVoyageService.getActivitesIdsParVoyage(voyage.getId_voyage());
            System.out.println("Activités déjà associées: " + idsActivites.size());
        } catch (SQLException e) {
            System.err.println("Erreur chargement activités associées: " + e.getMessage());
        }

        btnAjouter.setVisible(false);
        btnAjouter.setManaged(false);

        btnConfirmerModification.setVisible(true);
        btnConfirmerModification.setManaged(true);

        btnAnnulerModification.setVisible(true);
        btnAnnulerModification.setManaged(true);

        btnModifierFormulaire.setVisible(false);
        btnModifierFormulaire.setManaged(false);

        fxtitre.requestFocus();

        System.out.println("Voyage chargé pour modification ID: " + voyage.getId_voyage());
    }

    @FXML
    void ouvrirFormulaireModification(ActionEvent event) {
        showAlert(Alert.AlertType.INFORMATION, "Info",
                "Sélectionnez un voyage à modifier en cliquant sur le bouton 'Modifier' dans sa carte");
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void ouvrirPaiementPourVoyage(int idVoyage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/PagePaiement.fxml"));
            Parent root = loader.load();

            PaiementController paiementController = loader.getController();
            paiementController.initData(idVoyage);

            Stage stage = (Stage) voyagesContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la page de paiement: " + e.getMessage());
        }
    }
}