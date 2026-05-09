package Controllers.ItineraireEtEtape;

import Entities.Itineraire;
import Entities.User;
import Entities.etape;
import Services.etapeCRUD;
import Services.itineraireCRUD;
import Utils.MyBD;
import Utils.UserSession;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class GestionItinerairesController {

    // ===== Sidebar navigation =====
    @FXML private HBox btnDestinations;
    @FXML private HBox btnHebergement;
    @FXML private HBox btnUsers;
    @FXML private HBox btnStats;
    @FXML private HBox btnItineraires;
    @FXML private HBox btnCategories;
    @FXML private HBox btnActivites;
    @FXML private HBox btnEvenements;
    @FXML private HBox btnVoyages;
    @FXML private HBox btnBudgets;

    // ===== User profile (top-right) =====
    @FXML private HBox userProfileBox;
    @FXML private Label lblUserName;
    @FXML private Label lblUserRole;

    // ===== Status bar =====
    @FXML private Label lblLastUpdate;

    // ===== Sidebar stats badge =====
    @FXML private Label lblSidebarItinerairesCount;

    // ===== Filtres / recherche =====
    @FXML private TextField searchField;
    @FXML private ComboBox<String> comboDestination;
    @FXML private DatePicker dateDebut;
    @FXML private DatePicker dateFin;
    @FXML private Button btnFiltrer;
    @FXML private Button btnReinitialiser;
    @FXML private Button btnNouvelItineraire;
    @FXML private Button btnNouvelleEtape;

    // ===== Labels statistiques =====
    @FXML private Label lblTotalItineraires;
    @FXML private Label lblTotalEtapes;
    @FXML private Label statsTotalItineraires;
    @FXML private Label statsTotalEtapes;
    @FXML private Label statsTotalDestinations;
    @FXML private Label kpiItineraires;
    @FXML private Label kpiEtapes;
    @FXML private Label kpiDestinations;

    // ===== Table Itinéraires =====
    @FXML private TableView<Itineraire> tableItineraires;
    @FXML private TableColumn<Itineraire, Integer> colId;
    @FXML private TableColumn<Itineraire, String>  colNom;
    @FXML private TableColumn<Itineraire, String>  colDescription;
    @FXML private TableColumn<Itineraire, String>  colVoyage;
    @FXML private TableColumn<Itineraire, String>  colDestination;
    @FXML private TableColumn<Itineraire, Integer> colNbEtapes;
    @FXML private TableColumn<Itineraire, Void>    colActions;

    // ===== Table Étapes =====
    @FXML private TableView<EtapeTableModel> tableEtapes;
    @FXML private TableColumn<EtapeTableModel, Integer> colEtapeId;
    @FXML private TableColumn<EtapeTableModel, String>  colEtapeHeure;
    @FXML private TableColumn<EtapeTableModel, String>  colEtapeDescription;
    @FXML private TableColumn<EtapeTableModel, String>  colEtapeActivite;
    @FXML private TableColumn<EtapeTableModel, String>  colEtapeItineraire;
    @FXML private TableColumn<EtapeTableModel, String>  colEtapeLieu;
    @FXML private TableColumn<EtapeTableModel, Float>   colEtapeDuree;
    @FXML private TableColumn<EtapeTableModel, Void>    colEtapeActions;

    // ===== Services =====
    private final itineraireCRUD itineraireCRUD = new itineraireCRUD();
    private final etapeCRUD      etapeCRUD      = new etapeCRUD();

    // ===== Données =====
    private final ObservableList<Itineraire>      itinerairesList = FXCollections.observableArrayList();
    private final ObservableList<EtapeTableModel> etapesList      = FXCollections.observableArrayList();
    private FilteredList<Itineraire>      filteredItineraires;
    private FilteredList<EtapeTableModel> filteredEtapes;

    // ============================================================
    //  INITIALISATION
    // ============================================================

    @FXML
    public void initialize() {
        System.out.println("Initialisation du contrôleur de gestion");

        verifyFXMLComponents();
        setupTables();
        loadDestinations();
        loadItineraires();
        loadEtapes();
        setupSearchAndFilters();
        setupButtons();
        updateStats();
        updateUserInfo();
        updateLastUpdateTime();
    }

    private void verifyFXMLComponents() {
        System.out.println("Vérification des composants FXML:");
        System.out.println("userProfileBox        : " + (userProfileBox        != null ? "OK" : "null"));
        System.out.println("lblUserName           : " + (lblUserName           != null ? "OK" : "null"));
        System.out.println("lblUserRole           : " + (lblUserRole           != null ? "OK" : "null"));
        System.out.println("lblLastUpdate         : " + (lblLastUpdate         != null ? "OK" : "null"));
        System.out.println("statsTotalItineraires : " + (statsTotalItineraires != null ? "OK" : "null"));
        System.out.println("statsTotalEtapes      : " + (statsTotalEtapes      != null ? "OK" : "null"));
        System.out.println("statsTotalDestinations: " + (statsTotalDestinations != null ? "OK" : "null"));
        System.out.println("kpiItineraires        : " + (kpiItineraires        != null ? "OK" : "null"));
        System.out.println("kpiEtapes             : " + (kpiEtapes             != null ? "OK" : "null"));
        System.out.println("kpiDestinations       : " + (kpiDestinations       != null ? "OK" : "null"));
        System.out.println("btnNouvelItineraire   : " + (btnNouvelItineraire   != null ? "OK" : "null"));
        System.out.println("btnNouvelleEtape      : " + (btnNouvelleEtape      != null ? "OK" : "null"));
        System.out.println("btnFiltrer            : " + (btnFiltrer            != null ? "OK" : "null"));
        System.out.println("btnReinitialiser      : " + (btnReinitialiser      != null ? "OK" : "null"));
    }

    // ============================================================
    //  TABLES
    // ============================================================

    private void setupTables() {
        try {
            // --- Table itinéraires ---
            if (colId != null)          colId.setCellValueFactory(new PropertyValueFactory<>("id_itineraire"));
            if (colNom != null)         colNom.setCellValueFactory(new PropertyValueFactory<>("nom_itineraire"));
            if (colDescription != null) colDescription.setCellValueFactory(new PropertyValueFactory<>("description_itineraire"));

            if (colVoyage != null) {
                colVoyage.setCellValueFactory(cell ->
                        new SimpleStringProperty(getVoyageNameById(cell.getValue().getId_voyage())));
            }
            if (colDestination != null) {
                colDestination.setCellValueFactory(cell ->
                        new SimpleStringProperty(getDestinationByVoyageId(cell.getValue().getId_voyage())));
            }
            if (colNbEtapes != null) {
                colNbEtapes.setCellValueFactory(cell -> {
                    try {
                        int count = etapeCRUD.getEtapesByItineraire(cell.getValue().getId_itineraire()).size();
                        return new SimpleIntegerProperty(count).asObject();
                    } catch (SQLException e) {
                        e.printStackTrace();
                        return new SimpleIntegerProperty(0).asObject();
                    }
                });
            }
            if (colActions != null) colActions.setCellFactory(createItineraryActionButtons());

            // --- Table étapes ---
            if (colEtapeId != null)          colEtapeId.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getId()).asObject());
            if (colEtapeHeure != null)        colEtapeHeure.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getHeure()));
            if (colEtapeDescription != null)  colEtapeDescription.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getDescription()));
            if (colEtapeActivite != null)     colEtapeActivite.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getActivite()));
            if (colEtapeItineraire != null)   colEtapeItineraire.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getItineraire()));
            if (colEtapeLieu != null)         colEtapeLieu.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getLieu()));
            if (colEtapeDuree != null)        colEtapeDuree.setCellValueFactory(cell -> new javafx.beans.property.SimpleFloatProperty(cell.getValue().getDuree()).asObject());
            if (colEtapeActions != null)      colEtapeActions.setCellFactory(createEtapeActionButtons());

        } catch (Exception e) {
            System.err.println("Erreur dans setupTables: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Callback<TableColumn<Itineraire, Void>, TableCell<Itineraire, Void>> createItineraryActionButtons() {
        return param -> new TableCell<>() {
            private final Button btnEdit   = new Button("✏️");
            private final Button btnDelete = new Button("🗑️");
            private final HBox   pane      = new HBox(5, btnEdit, btnDelete);

            {
                btnEdit.setStyle("-fx-background-color: #ff8c42; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand;");
                btnDelete.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand;");
                btnEdit.setTooltip(new Tooltip("Modifier"));
                btnDelete.setTooltip(new Tooltip("Supprimer"));

                btnEdit.setOnAction(e -> modifierItineraire(getTableView().getItems().get(getIndex())));
                btnDelete.setOnAction(e -> supprimerItineraire(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        };
    }

    private Callback<TableColumn<EtapeTableModel, Void>, TableCell<EtapeTableModel, Void>> createEtapeActionButtons() {
        return param -> new TableCell<>() {
            private final Button btnEdit   = new Button("✏️");
            private final Button btnDelete = new Button("🗑️");
            private final HBox   pane      = new HBox(5, btnEdit, btnDelete);

            {
                btnEdit.setStyle("-fx-background-color: #ff8c42; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand;");
                btnDelete.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand;");
                btnEdit.setTooltip(new Tooltip("Modifier"));
                btnDelete.setTooltip(new Tooltip("Supprimer"));

                btnEdit.setOnAction(e -> modifierEtape(getTableView().getItems().get(getIndex())));
                btnDelete.setOnAction(e -> supprimerEtape(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        };
    }

    // ============================================================
    //  NAVIGATION — méthodes appelées depuis le FXML
    // ============================================================

    /** FIX : méthode manquante → "Cannot resolve symbol 'handleEvenementsClick'" */
    @FXML
    private void handleEvenementsClick() {
        navigateTo("/EVENTback.fxml", "Événements");
    }

    @FXML
    private void handleDestinationsClick() {
        navigateTo("/DestinationBack.fxml", "Destinations");
    }

    @FXML
    private void handleHebergementClick() {
        navigateTo("/HebergementBack.fxml", "Hébergements");
    }

    @FXML
    private void handleUsersClick() {
        navigateTo("/fxml/admin_users.fxml", "Utilisateurs");
    }

    @FXML
    private void handleStatsClick() {
        navigateTo("/fxml/admin_stats.fxml", "Statistiques");
    }

    @FXML
    private void handleCategoriesClick() {
        navigateTo("/categoriesback.fxml", "Catégories");
    }

    @FXML
    private void handleActivitesClick() {
        navigateTo("/activitesback.fxml", "Activités");
    }

    @FXML
    private void handleVoyagesClick() {
        navigateTo("/PageVoyageBack.fxml", "Voyages");
    }

    @FXML
    private void handleBudgetsClick() {
        navigateTo("/BudgetDepenseBack.fxml", "Budgets");
    }

    @FXML
    private void handleDashboardClick() {
        showInfoAlert("Dashboard", "Fonctionnalité à venir");
    }

    @FXML
    private void handleRetourVersItineraire() {
        navigateTo("/ItineraireEtEtape/PageItineraire.fxml", "Itinéraires");
    }

    /** Méthode de navigation centralisée — réutilisée par tous les handlers */
    private void navigateTo(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = getStage();
            if (stage == null) return;
            stage.setScene(new Scene(root,
                    javafx.stage.Screen.getPrimary().getVisualBounds().getWidth(),
                    javafx.stage.Screen.getPrimary().getVisualBounds().getHeight()));
            stage.setTitle("TravelMate - " + title);
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            showAlert("Erreur de navigation", "Impossible d'ouvrir : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /** Récupère le Stage courant depuis n'importe quel nœud disponible */
    private Stage getStage() {
        if (tableItineraires != null && tableItineraires.getScene() != null)
            return (Stage) tableItineraires.getScene().getWindow();
        if (statsTotalItineraires != null && statsTotalItineraires.getScene() != null)
            return (Stage) statsTotalItineraires.getScene().getWindow();
        if (userProfileBox != null && userProfileBox.getScene() != null)
            return (Stage) userProfileBox.getScene().getWindow();
        return null;
    }

    // ============================================================
    //  BOUTONS PRINCIPAUX
    // ============================================================

    private void setupButtons() {
        try {
            if (btnNouvelItineraire != null) btnNouvelItineraire.setOnAction(e -> ouvrirFormulaireItineraire(null));
            if (btnNouvelleEtape    != null) btnNouvelleEtape.setOnAction(e -> ouvrirFormulaireEtape(null));
            if (btnFiltrer          != null) btnFiltrer.setOnAction(e -> filtrer());
            if (btnReinitialiser    != null) btnReinitialiser.setOnAction(e -> reinitialiserFiltres());
        } catch (Exception e) {
            System.err.println("Erreur dans setupButtons: " + e.getMessage());
        }
    }

    // ============================================================
    //  RECHERCHE & FILTRES
    // ============================================================

    private void setupSearchAndFilters() {
        filteredItineraires = new FilteredList<>(itinerairesList, p -> true);
        filteredEtapes      = new FilteredList<>(etapesList, p -> true);

        if (tableItineraires != null) {
            SortedList<Itineraire> sorted = new SortedList<>(filteredItineraires);
            sorted.comparatorProperty().bind(tableItineraires.comparatorProperty());
            tableItineraires.setItems(sorted);
        }
        if (tableEtapes != null) {
            SortedList<EtapeTableModel> sorted = new SortedList<>(filteredEtapes);
            sorted.comparatorProperty().bind(tableEtapes.comparatorProperty());
            tableEtapes.setItems(sorted);
        }

        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> {
                filteredItineraires.setPredicate(it -> {
                    if (newVal == null || newVal.isEmpty()) return true;
                    String lc = newVal.toLowerCase();
                    return it.getNom_itineraire().toLowerCase().contains(lc)
                            || (it.getDescription_itineraire() != null
                            && it.getDescription_itineraire().toLowerCase().contains(lc));
                });
                filteredEtapes.setPredicate(et -> {
                    if (newVal == null || newVal.isEmpty()) return true;
                    String lc = newVal.toLowerCase();
                    return et.getDescription().toLowerCase().contains(lc)
                            || et.getActivite().toLowerCase().contains(lc);
                });
            });
        }
    }

    private void filtrer() {
        if (comboDestination == null || filteredItineraires == null) return;
        String destination = comboDestination.getValue();
        filteredItineraires.setPredicate(it -> {
            if (destination != null && !destination.equals("Toutes les destinations")) {
                return destination.equals(getDestinationByVoyageId(it.getId_voyage()));
            }
            return true;
        });
    }

    private void reinitialiserFiltres() {
        if (comboDestination    != null) comboDestination.getSelectionModel().selectFirst();
        if (dateDebut           != null) dateDebut.setValue(null);
        if (dateFin             != null) dateFin.setValue(null);
        if (searchField         != null) searchField.clear();
        if (filteredItineraires != null) filteredItineraires.setPredicate(p -> true);
        if (filteredEtapes      != null) filteredEtapes.setPredicate(p -> true);
    }

    // ============================================================
    //  CHARGEMENT DES DONNÉES
    // ============================================================

    private void loadDestinations() {
        if (comboDestination == null) return;
        comboDestination.getItems().clear();
        comboDestination.getItems().add("Toutes les destinations");
        try (Connection conn = MyBD.getInstance().getConn();
             PreparedStatement stmt = conn.prepareStatement("SELECT DISTINCT nom_destination FROM destination");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                comboDestination.getItems().add(rs.getString("nom_destination"));
            }
            comboDestination.getSelectionModel().selectFirst();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadItineraires() {
        try {
            itinerairesList.clear();
            itinerairesList.addAll(itineraireCRUD.afficher());
            updateStats();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les itinéraires : " + e.getMessage());
        }
    }

    private void loadEtapes() {
        try {
            etapesList.clear();
            List<etape> etapes = etapeCRUD.afficher();
            for (etape e : etapes) etapesList.add(new EtapeTableModel(e));
            updateStats();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les étapes : " + e.getMessage());
        }
    }

    // ============================================================
    //  STATISTIQUES
    // ============================================================

    private void updateStats() {
        try {
            int totalIt   = itinerairesList.size();
            int totalEt   = etapesList.size();
            int totalDest = countDestinations();

            setLabel(lblTotalItineraires,   "Total itinéraires : " + totalIt);
            setLabel(lblTotalEtapes,        "Total étapes : " + totalEt);
            setLabel(statsTotalItineraires, String.valueOf(totalIt));
            setLabel(statsTotalEtapes,      String.valueOf(totalEt));
            setLabel(statsTotalDestinations,String.valueOf(totalDest));
            setLabel(kpiItineraires,        String.valueOf(totalIt));
            setLabel(kpiEtapes,             String.valueOf(totalEt));
            setLabel(kpiDestinations,       String.valueOf(totalDest));
            setLabel(lblSidebarItinerairesCount, String.valueOf(totalIt));
        } catch (Exception e) {
            System.err.println("Erreur dans updateStats: " + e.getMessage());
        }
    }

    private int countDestinations() {
        try (Connection conn = MyBD.getInstance().getConn();
             PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) AS count FROM destination");
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return rs.getInt("count");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // ============================================================
    //  INFORMATIONS UTILISATEUR (FIX fx:id userProfileBox, lblUserName, lblUserRole)
    // ============================================================

    private void updateUserInfo() {
        try {
            User currentUser = UserSession.getInstance().getCurrentUser();
            if (currentUser != null) {
                setLabel(lblUserName, currentUser.getPrenom() + " " + currentUser.getNom());
                setLabel(lblUserRole, currentUser.getRole());
            } else {
                setLabel(lblUserName, "Utilisateur");
                setLabel(lblUserRole, "Administrateur");
            }
        } catch (Exception e) {
            setLabel(lblUserName, "Utilisateur");
            setLabel(lblUserRole, "Administrateur");
        }
    }

    private void updateLastUpdateTime() {
        setLabel(lblLastUpdate, "Dernière mise à jour : " +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")));
    }

    // ============================================================
    //  FORMULAIRES (CRUD)
    // ============================================================

    private void ouvrirFormulaireItineraire(Itineraire itineraire) {
        try {
            String fxmlPath = itineraire == null
                    ? "/ItineraireEtEtape/AjoutItineraireBack.fxml"
                    : "/ItineraireEtEtape/ModifierItineraireBack.fxml";

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            if (itineraire == null) {
                AjoutItineraireBack ctrl = loader.getController();
                ctrl.setParentController(this);
            } else {
                ModifierItineraireBack ctrl = loader.getController();
                ctrl.setItineraire(itineraire);
                ctrl.setParentController(this);
            }

            Stage stage = new Stage();
            stage.setTitle(itineraire == null ? "Ajouter un itinéraire" : "Modifier l'itinéraire");
            stage.setScene(new Scene(root,
                    javafx.stage.Screen.getPrimary().getVisualBounds().getWidth(),
                    javafx.stage.Screen.getPrimary().getVisualBounds().getHeight()));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le formulaire : " + e.getMessage());
        }
    }

    private void ouvrirFormulaireEtape(EtapeTableModel etapeModel) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ItineraireEtEtape/PageAjoutEtapeBack.fxml"));
            DialogPane dialogPane = loader.load();

            PageAjoutEtapeBack ctrl = loader.getController();
            ctrl.setDialogPane(dialogPane);
            ctrl.setOnEtapeAjoutee(() -> {
                loadEtapes();
                loadItineraires();
            });

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Ajouter une étape (Back Office)");
            dialog.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le formulaire : " + e.getMessage());
        }
    }

    private void modifierItineraire(Itineraire it)      { ouvrirFormulaireItineraire(it); }
    private void modifierEtape(EtapeTableModel et)      { ouvrirFormulaireEtape(et); }

    private void supprimerItineraire(Itineraire itineraire) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer l'itinéraire");
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer \"" + itineraire.getNom_itineraire() + "\" ?");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                itineraireCRUD.supprimer(itineraire.getId_itineraire());
                loadItineraires();
                loadEtapes();
                showAlert("Succès", "Itinéraire supprimé avec succès !");
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Erreur", "Impossible de supprimer l'itinéraire : " + e.getMessage());
            }
        }
    }

    private void supprimerEtape(EtapeTableModel etapeModel) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer l'étape");
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer cette étape ?");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                etapeCRUD.supprimer(etapeModel.getId());
                loadEtapes();
                showAlert("Succès", "Étape supprimée avec succès !");
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Erreur", "Impossible de supprimer l'étape : " + e.getMessage());
            }
        }
    }

    // ============================================================
    //  CALLBACK PUBLIC (appelé depuis AjoutItineraireBack, etc.)
    // ============================================================

    public void refreshAfterModification() {
        loadItineraires();
        loadEtapes();
        updateStats();
        System.out.println("✅ Données rafraîchies après modification");
    }

    // ============================================================
    //  HELPERS SQL
    // ============================================================

    private String getVoyageNameById(int idVoyage) {
        try (Connection conn = MyBD.getInstance().getConn();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT titre_voyage FROM voyage WHERE id_voyage = ?")) {
            stmt.setInt(1, idVoyage);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getString("titre_voyage");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Voyage " + idVoyage;
    }

    private String getDestinationByVoyageId(int idVoyage) {
        try (Connection conn = MyBD.getInstance().getConn();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT d.nom_destination FROM voyage v " +
                             "LEFT JOIN destination d ON v.id_destination = d.id_destination " +
                             "WHERE v.id_voyage = ?")) {
            stmt.setInt(1, idVoyage);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String nom = rs.getString("nom_destination");
                    return nom != null ? nom : "Non définie";
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Non définie";
    }

    // ============================================================
    //  UTILITAIRES UI
    // ============================================================

    private void setLabel(Label lbl, String text) {
        if (lbl != null) lbl.setText(text);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfoAlert(String title, String message) {
        showAlert(title, message);
    }

    // ============================================================
    //  INNER CLASS — modèle de ligne pour la table des étapes
    // ============================================================

    public static class EtapeTableModel {
        private final int    id;
        private final String heure;
        private final String description;
        private final String activite;
        private final String itineraire;
        private final String lieu;
        private final float  duree;

        public EtapeTableModel(etape e) {
            this.id          = e.getId_etape();
            this.heure       = e.getHeure() != null ? e.getHeure().toString().substring(0, 5) : "--:--";
            this.description = e.getDescription_etape() != null ? e.getDescription_etape() : "";
            this.activite    = e.getNomActivite()   != null ? e.getNomActivite()   : "Activité inconnue";
            this.itineraire  = e.getNomItineraire() != null ? e.getNomItineraire() : "Itinéraire inconnu";
            this.lieu        = e.getLieuActivite()  != null ? e.getLieuActivite()  : "Lieu non défini";
            Float d = e.getDureeActivite();
            this.duree = d != null ? d : 0f;
        }

        public int    getId()          { return id; }
        public String getHeure()       { return heure; }
        public String getDescription() { return description; }
        public String getActivite()    { return activite; }
        public String getItineraire()  { return itineraire; }
        public String getLieu()        { return lieu; }
        public float  getDuree()       { return duree; }
    }
}