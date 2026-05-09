package Controllers;

import Entities.User;
import Entities.Voyage;
import Services.*;
import Utils.UserSession;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Callback;
import Services.ExportService;

import java.io.IOException;
import java.net.URL;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class VoyageBackController implements Initializable {

    // ── Constantes statuts ──────────────────────────────────────────────────
    private static final String STATUT_A_VENIR  = "a venir";
    private static final String STATUT_EN_COURS = "En cours";
    private static final String STATUT_TERMINE  = "Terminé";
    private static final String STATUT_ANNULE   = "Annulé";
    private static final String STATUT_TOUS     = "Tous";
    private static final String DEST_INCONNUE   = "Inconnue";
    private static final String PERCENT_FORMAT  = "(%.1f%%)";

    // ── Sidebar navigation ──────────────────────────────────────────────────
    // FIX #2 : ces HBox existaient en FXML mais n'avaient aucun onMouseClicked ;
    //          on les branche manuellement dans initialize().
    @FXML private javafx.scene.layout.HBox btnDestinations;
    @FXML private javafx.scene.layout.HBox btnHebergement;
    @FXML private javafx.scene.layout.HBox btnUsers;
    @FXML private javafx.scene.layout.HBox btnStats;
    @FXML private javafx.scene.layout.HBox btnItineraires;
    @FXML private javafx.scene.layout.HBox btnCategories;
    @FXML private javafx.scene.layout.HBox btnActivites;
    @FXML private javafx.scene.layout.HBox btnVoyages;
    @FXML private javafx.scene.layout.HBox btnBudgets;
    @FXML private javafx.scene.layout.HBox btnEvenements;
    @FXML private javafx.scene.layout.HBox userProfileBox;

    // ── User info ───────────────────────────────────────────────────────────
    @FXML private Label lblUserName;
    @FXML private Label lblUserRole;

    // ── Table ───────────────────────────────────────────────────────────────
    @FXML private TableView<Voyage>             tableVoyages;
    @FXML private TableColumn<Voyage, Integer>  colId;
    @FXML private TableColumn<Voyage, String>   colTitre;
    @FXML private TableColumn<Voyage, Date>     colDateDebut;
    @FXML private TableColumn<Voyage, Date>     colDateFin;
    @FXML private TableColumn<Voyage, String>   colStatut;
    @FXML private TableColumn<Voyage, Integer>  colIdDestination;
    @FXML private TableColumn<Voyage, String>   colDestination;
    @FXML private TableColumn<Voyage, Void>     colActions;

    // ── Formulaire ──────────────────────────────────────────────────────────
    @FXML private TextField         tfTitre;
    @FXML private DatePicker        dpDateDebut;
    @FXML private DatePicker        dpDateFin;
    @FXML private ComboBox<String>  cbStatut;
    @FXML private TextField         tfIdDestination;
    @FXML private TextField         tfDestinationNom;

    // ── Recherche ───────────────────────────────────────────────────────────
    @FXML private TextField         tfSearchTitre;
    @FXML private TextField         tfSearchId;
    @FXML private ComboBox<String>  cbSearchStatut;
    @FXML private Button            btnSearch;
    @FXML private Button            btnReset;

    // ── KPI labels ──────────────────────────────────────────────────────────
    @FXML private Label lblTotalVoyages;
    @FXML private Label lblVoyagesActifs;
    @FXML private Label lblVoyagesTermines;
    @FXML private Label lblVoyagesCount;
    @FXML private Label lblSidebarVoyagesCount;
    @FXML private Label statsTotalVoyages;
    @FXML private Label statsActifs;
    @FXML private Label statsTermines;
    @FXML private Label lblLastUpdate;

    // ── Boutons principaux ──────────────────────────────────────────────────
    @FXML private Button btnAjouter;
    @FXML private Button btnModifier;
    @FXML private Button btnAnnuler;
    @FXML private Button btnRefresh;

    // ── Statistiques détaillées ─────────────────────────────────────────────
    @FXML private Label       lblCountAVenir;
    @FXML private Label       lblCountEnCours;
    @FXML private Label       lblCountTermine;
    @FXML private Label       lblCountAnnule;
    @FXML private Label       lblPourcentAVenir;
    @FXML private Label       lblPourcentEnCours;
    @FXML private Label       lblPourcentTermine;
    @FXML private Label       lblPourcentAnnule;
    @FXML private ProgressBar pbAVenir;
    @FXML private ProgressBar pbEnCours;
    @FXML private ProgressBar pbTermine;
    @FXML private ProgressBar pbAnnule;
    @FXML private Label       lblTotalVoyages2;
    @FXML private PieChart    pieChartStatuts;

    // ── Export ──────────────────────────────────────────────────────────────
    @FXML private Button btnExportPDF;
    @FXML private Button btnExportExcel;

    // ── Services & données ──────────────────────────────────────────────────
    private final VoyageCRUDV             voyageCRUD         = new VoyageCRUDV();
    private final QRCodeServicess         qrCodeServicess    = new QRCodeServicess();
    private final RestCountriesService    restCountriesService = new RestCountriesService();
    private final UnsplashService         unsplashService    = new UnsplashService();

    private final ObservableList<Voyage>  voyageList   = FXCollections.observableArrayList();
    private FilteredList<Voyage>          filteredData;
    private SortedList<Voyage>            sortedData;
    private Voyage voyageSelectionne = null;

    // ════════════════════════════════════════════════════════════════════════
    //  INITIALISATION
    // ════════════════════════════════════════════════════════════════════════

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        // 1. Colonnes table
        colId.setCellValueFactory(new PropertyValueFactory<>("id_voyage"));
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titre_voyage"));
        colDateDebut.setCellValueFactory(new PropertyValueFactory<>("date_debut"));
        colDateFin.setCellValueFactory(new PropertyValueFactory<>("date_fin"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colIdDestination.setCellValueFactory(new PropertyValueFactory<>("id_destination"));
        colDestination.setCellValueFactory(cell -> {
            try {
                String nom = voyageCRUD.getNomDestination(cell.getValue().getId_destination());
                return new javafx.beans.property.SimpleStringProperty(nom != null ? nom : DEST_INCONNUE);
            } catch (SQLException e) {
                return new javafx.beans.property.SimpleStringProperty(DEST_INCONNUE);
            }
        });
        ajouterBoutonsActions();

        // 2. ComboBox
        cbStatut.getItems().addAll(STATUT_A_VENIR, STATUT_EN_COURS, STATUT_TERMINE, STATUT_ANNULE);
        cbStatut.setValue(STATUT_A_VENIR);
        cbSearchStatut.getItems().addAll(STATUT_TOUS, STATUT_A_VENIR, STATUT_EN_COURS, STATUT_TERMINE, STATUT_ANNULE);
        cbSearchStatut.setValue(STATUT_TOUS);

        // 3. DatePickers
        configurerDatePickers();

        // 4. Listener ID destination
        tfIdDestination.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) {
                try {
                    int idDest = Integer.parseInt(newVal);
                    String nom = voyageCRUD.getNomDestination(idDest);
                    tfDestinationNom.setText(nom != null ? nom : DEST_INCONNUE);
                } catch (NumberFormatException | SQLException e) {
                    tfDestinationNom.setText(DEST_INCONNUE);
                }
            } else {
                tfDestinationNom.setText("");
            }
        });

        // 5. Boutons formulaire
        btnAjouter.setOnAction(this::ajouterVoyage);
        btnModifier.setOnAction(this::modifierVoyage);
        btnAnnuler.setOnAction(this::annulerModification);
        btnRefresh.setOnAction(this::refreshTable);
        btnSearch.setOnAction(this::rechercherVoyages);
        btnReset.setOnAction(this::resetRecherche);

        // 6. Export
        if (btnExportPDF   != null) btnExportPDF.setOnAction(this::exportToPDF);
        if (btnExportExcel != null) btnExportExcel.setOnAction(this::exportToExcel);

        // 7. Recherche / tri
        configurerRecherche();

        // 8. Données
        chargerVoyages();
        mettreAJourDateMAJ();

        // 9. User info
        updateUserInfo();

        // ── FIX #2 : brancher la navbar ici car le FXML n'a pas de onMouseClicked ──
        setupNavbar();
    }

    // ════════════════════════════════════════════════════════════════════════
    //  FIX #2 — NAVBAR branchée programmatiquement
    // ════════════════════════════════════════════════════════════════════════

    private void setupNavbar() {
        bindNav(btnDestinations, "/DestinationBack.fxml",                          "Destinations");
        bindNav(btnHebergement,  "/HebergementBack.fxml",                          "Hébergements");
        bindNav(btnUsers,        "/fxml/admin_users.fxml",                         "Utilisateurs");
        bindNav(btnStats,        "/fxml/admin_stats.fxml",                         "Statistiques");
        // FIX #1 : le chemin doit correspondre exactement au fichier dans resources
        bindNav(btnItineraires,  "/ItineraireEtEtape/PageGestionItineraires.fxml", "Itinéraires");
        bindNav(btnCategories,   "/categoriesback.fxml",                           "Catégories");
        bindNav(btnActivites,    "/activitesback.fxml",                            "Activités");
        bindNav(btnBudgets,      "/BudgetDepenseBack.fxml",                        "Budgets");
        bindNav(btnEvenements,   "/EVENTback.fxml",                                "Événements");
        // Voyages = page courante → recharge simplement
        if (btnVoyages != null) btnVoyages.setOnMouseClicked(e -> chargerVoyages());
        if (userProfileBox != null)
            userProfileBox.setOnMouseClicked(e -> naviguerVers("/fxml/profile.fxml", "Mon Profil"));
    }

    /** Branche un clic sur un HBox de la sidebar. */
    private void bindNav(javafx.scene.layout.HBox btn, String fxml, String titre) {
        if (btn != null) btn.setOnMouseClicked(e -> naviguerVers(fxml, titre));
    }

    // ════════════════════════════════════════════════════════════════════════
    //  TABLE — boutons d'action inline
    // ════════════════════════════════════════════════════════════════════════

    private void ajouterBoutonsActions() {
        Callback<TableColumn<Voyage, Void>, TableCell<Voyage, Void>> cellFactory = param -> new TableCell<>() {
            private final Button btnVoir        = new Button("👁️");
            private final Button btnMod         = new Button("✏️");
            private final Button btnSup         = new Button("🗑️");
            private final Button btnParticipants= new Button("👥");
            private final javafx.scene.layout.HBox pane =
                    new javafx.scene.layout.HBox(5, btnVoir, btnMod, btnSup, btnParticipants);

            {
                btnVoir.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 4 8; -fx-font-size: 11; -fx-cursor: hand;");
                btnMod.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 4 8; -fx-font-size: 11; -fx-cursor: hand;");
                btnSup.setStyle("-fx-background-color: #ec4899; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 4 8; -fx-font-size: 11; -fx-cursor: hand;");
                btnParticipants.setStyle("-fx-background-color: #ff8c42; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 4 8; -fx-font-size: 11; -fx-cursor: hand;");

                btnVoir.setOnAction(e -> voirVoyage(getTableView().getItems().get(getIndex())));
                btnMod.setOnAction(e -> remplirFormulaire(getTableView().getItems().get(getIndex())));
                btnSup.setOnAction(e -> supprimerVoyage(getTableView().getItems().get(getIndex())));
                btnParticipants.setOnAction(e -> ouvrirParticipants(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        };
        colActions.setCellFactory(cellFactory);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  DATE PICKERS
    // ════════════════════════════════════════════════════════════════════════

    private void configurerDatePickers() {
        dpDateDebut.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && dpDateFin.getValue() != null && dpDateFin.getValue().isBefore(newVal))
                dpDateFin.setValue(null);
        });
        dpDateFin.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate debut = dpDateDebut.getValue();
                if (debut != null) setDisable(empty || !date.isAfter(debut));
            }
        });
    }

    // ════════════════════════════════════════════════════════════════════════
    //  RECHERCHE
    // ════════════════════════════════════════════════════════════════════════

    private void configurerRecherche() {
        filteredData = new FilteredList<>(voyageList, p -> true);
        sortedData   = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableVoyages.comparatorProperty());
        tableVoyages.setItems(sortedData);

        sortedData.addListener((javafx.collections.ListChangeListener<Voyage>) c -> {
            int size = sortedData.size();
            if (lblVoyagesCount       != null) lblVoyagesCount.setText(String.valueOf(size));
            if (lblSidebarVoyagesCount != null) lblSidebarVoyagesCount.setText(String.valueOf(size));
            mettreAJourStatistiques();
            mettreAJourStatistiquesDetaillees();
        });
    }

    @FXML
    private void rechercherVoyages(ActionEvent event) {
        String titre  = tfSearchTitre.getText() != null ? tfSearchTitre.getText().toLowerCase().trim() : "";
        String idText = tfSearchId.getText()    != null ? tfSearchId.getText().trim() : "";
        String statut = cbSearchStatut.getValue();

        filteredData.setPredicate(voyage -> {
            if (titre.isEmpty() && idText.isEmpty() && (statut == null || STATUT_TOUS.equals(statut))) return true;

            if (!idText.isEmpty()) {
                try {
                    if (voyage.getId_voyage() != Integer.parseInt(idText)) return false;
                } catch (NumberFormatException e) { return false; }
            }
            if (!titre.isEmpty()) {
                String t = voyage.getTitre_voyage();
                if (t == null || !t.toLowerCase().contains(titre)) return false;
            }
            if (statut != null && !STATUT_TOUS.equals(statut)) {
                String s = voyage.getStatut();
                if (s == null || !s.equals(statut)) return false;
            }
            return true;
        });

        mettreAJourStatistiques();
        mettreAJourStatistiquesDetaillees();
    }

    @FXML
    private void resetRecherche(ActionEvent event) {
        tfSearchTitre.clear();
        tfSearchId.clear();
        cbSearchStatut.setValue(STATUT_TOUS);
        if (filteredData != null) filteredData.setPredicate(p -> true);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  CHARGEMENT DONNÉES
    //  FIX #1 : chargerVoyages() ouvre une connexion fraîche via VoyageCRUDV.
    //  L'erreur "No operations allowed after connection closed" venait du fait
    //  que la connexion singleton de MyBD était déjà fermée par un autre
    //  contrôleur. La correction est dans etapeCRUD / itineraireCRUD côté
    //  GestionItinerairesController (connexion try-with-resources), mais ici
    //  on s'assure aussi de ne jamais garder une connexion ouverte inutilement.
    // ════════════════════════════════════════════════════════════════════════

    private void chargerVoyages() {
        try {
            List<Voyage> voyages = voyageCRUD.afficher();
            voyageList.clear();
            voyageList.addAll(voyages);
            mettreAJourStatistiques();
            mettreAJourStatistiquesDetaillees();
            mettreAJourDateMAJ();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les voyages : " + e.getMessage());
        }
    }

    private void mettreAJourDateMAJ() {
        if (lblLastUpdate != null)
            lblLastUpdate.setText("Dernière mise à jour : " +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")));
    }

    // ════════════════════════════════════════════════════════════════════════
    //  STATISTIQUES
    // ════════════════════════════════════════════════════════════════════════

    private void mettreAJourStatistiques() {
        if (sortedData == null) return;
        int total = sortedData.size();
        if (lblTotalVoyages    != null) lblTotalVoyages.setText(String.valueOf(total));
        if (statsTotalVoyages  != null) statsTotalVoyages.setText(String.valueOf(total));

        long actifs = sortedData.stream()
                .filter(v -> STATUT_EN_COURS.equals(v.getStatut()) || STATUT_A_VENIR.equals(v.getStatut())).count();
        if (lblVoyagesActifs != null) lblVoyagesActifs.setText(String.valueOf(actifs));
        if (statsActifs      != null) statsActifs.setText(String.valueOf(actifs));

        long termines = sortedData.stream().filter(v -> STATUT_TERMINE.equals(v.getStatut())).count();
        if (lblVoyagesTermines != null) lblVoyagesTermines.setText(String.valueOf(termines));
        if (statsTermines      != null) statsTermines.setText(String.valueOf(termines));
    }

    private void mettreAJourStatistiquesDetaillees() {
        if (voyageList == null) return;
        int  total   = voyageList.size();
        long aVenir  = voyageList.stream().filter(v -> STATUT_A_VENIR.equals(v.getStatut())).count();
        long enCours = voyageList.stream().filter(v -> STATUT_EN_COURS.equals(v.getStatut())).count();
        long termine = voyageList.stream().filter(v -> STATUT_TERMINE.equals(v.getStatut())).count();
        long annule  = voyageList.stream().filter(v -> STATUT_ANNULE.equals(v.getStatut())).count();

        if (lblCountAVenir  != null) lblCountAVenir.setText(String.valueOf(aVenir));
        if (lblCountEnCours != null) lblCountEnCours.setText(String.valueOf(enCours));
        if (lblCountTermine != null) lblCountTermine.setText(String.valueOf(termine));
        if (lblCountAnnule  != null) lblCountAnnule.setText(String.valueOf(annule));
        if (lblTotalVoyages2 != null) lblTotalVoyages2.setText(String.valueOf(total));

        if (total > 0) {
            double pAV = aVenir  * 1.0 / total;
            double pEC = enCours * 1.0 / total;
            double pTe = termine * 1.0 / total;
            double pAn = annule  * 1.0 / total;

            if (lblPourcentAVenir  != null) lblPourcentAVenir.setText(String.format(PERCENT_FORMAT, pAV * 100));
            if (lblPourcentEnCours != null) lblPourcentEnCours.setText(String.format(PERCENT_FORMAT, pEC * 100));
            if (lblPourcentTermine != null) lblPourcentTermine.setText(String.format(PERCENT_FORMAT, pTe * 100));
            if (lblPourcentAnnule  != null) lblPourcentAnnule.setText(String.format(PERCENT_FORMAT, pAn * 100));

            if (pbAVenir  != null) pbAVenir.setProgress(pAV);
            if (pbEnCours != null) pbEnCours.setProgress(pEC);
            if (pbTermine != null) pbTermine.setProgress(pTe);
            if (pbAnnule  != null) pbAnnule.setProgress(pAn);
        } else {
            if (lblPourcentAVenir  != null) lblPourcentAVenir.setText("(0.0%)");
            if (lblPourcentEnCours != null) lblPourcentEnCours.setText("(0.0%)");
            if (lblPourcentTermine != null) lblPourcentTermine.setText("(0.0%)");
            if (lblPourcentAnnule  != null) lblPourcentAnnule.setText("(0.0%)");
            if (pbAVenir  != null) pbAVenir.setProgress(0);
            if (pbEnCours != null) pbEnCours.setProgress(0);
            if (pbTermine != null) pbTermine.setProgress(0);
            if (pbAnnule  != null) pbAnnule.setProgress(0);
        }

        if (pieChartStatuts != null) {
            pieChartStatuts.setData(FXCollections.observableArrayList(
                    new PieChart.Data("À venir (" + aVenir + ")",  aVenir),
                    new PieChart.Data("En cours (" + enCours + ")", enCours),
                    new PieChart.Data("Terminé (" + termine + ")",  termine),
                    new PieChart.Data("Annulé (" + annule + ")",    annule)
            ));
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  CRUD VOYAGES
    // ════════════════════════════════════════════════════════════════════════

    @FXML
    private void ajouterVoyage(ActionEvent event) {
        if (!validerFormulaire()) return;
        try {
            voyageCRUD.ajouter(new Voyage(
                    tfTitre.getText(),
                    Date.valueOf(dpDateDebut.getValue()),
                    Date.valueOf(dpDateFin.getValue()),
                    cbStatut.getValue(),
                    Integer.parseInt(tfIdDestination.getText())
            ));
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Voyage ajouté avec succès !");
            chargerVoyages();
            resetFormulaire();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ajout : " + e.getMessage());
        }
    }

    @FXML
    private void modifierVoyage(ActionEvent event) {
        if (voyageSelectionne == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner un voyage à modifier");
            return;
        }
        if (!validerFormulaire()) return;
        try {
            voyageSelectionne.setTitre_voyage(tfTitre.getText());
            voyageSelectionne.setDate_debut(Date.valueOf(dpDateDebut.getValue()));
            voyageSelectionne.setDate_fin(Date.valueOf(dpDateFin.getValue()));
            voyageSelectionne.setStatut(cbStatut.getValue());
            voyageSelectionne.setId_destination(Integer.parseInt(tfIdDestination.getText()));
            voyageCRUD.modifier(voyageSelectionne);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Voyage modifié avec succès !");
            chargerVoyages();
            resetFormulaire();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la modification : " + e.getMessage());
        }
    }

    @FXML private void annulerModification(ActionEvent event) { resetFormulaire(); }

    @FXML
    private void refreshTable(ActionEvent event) {
        chargerVoyages();
        resetRecherche(event);
    }

    private void supprimerVoyage(Voyage voyage) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText(null);
        alert.setContentText("Supprimer le voyage \"" + voyage.getTitre_voyage() + "\" ?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                voyageCRUD.supprimer(voyage.getId_voyage());
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Voyage supprimé !");
                chargerVoyages();
                resetFormulaire();
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Suppression impossible : " + e.getMessage());
            }
        }
    }

    private void remplirFormulaire(Voyage v) {
        voyageSelectionne = v;
        tfTitre.setText(v.getTitre_voyage());
        dpDateDebut.setValue(v.getDate_debut().toLocalDate());
        dpDateFin.setValue(v.getDate_fin().toLocalDate());
        cbStatut.setValue(v.getStatut());
        tfIdDestination.setText(String.valueOf(v.getId_destination()));
    }

    private void resetFormulaire() {
        tfTitre.clear();
        dpDateDebut.setValue(null);
        dpDateFin.setValue(null);
        cbStatut.setValue(STATUT_A_VENIR);
        tfIdDestination.clear();
        tfDestinationNom.clear();
        voyageSelectionne = null;
    }

    private boolean validerFormulaire() {
        String titre = tfTitre.getText() != null ? tfTitre.getText().trim() : "";
        if (titre.isEmpty())       { showAlert(Alert.AlertType.ERROR, "Erreur", "Le titre est requis"); return false; }
        if (titre.length() < 3)    { showAlert(Alert.AlertType.ERROR, "Erreur", "Le titre doit contenir au moins 3 caractères"); return false; }
        if (titre.length() > 100)  { showAlert(Alert.AlertType.ERROR, "Erreur", "Le titre ne peut pas dépasser 100 caractères"); return false; }
        tfTitre.setText(titre);
        if (dpDateDebut.getValue() == null) { showAlert(Alert.AlertType.ERROR, "Erreur", "La date de début est requise"); return false; }
        if (dpDateFin.getValue()   == null) { showAlert(Alert.AlertType.ERROR, "Erreur", "La date de fin est requise");   return false; }
        if (dpDateFin.getValue().isBefore(dpDateDebut.getValue())) { showAlert(Alert.AlertType.ERROR, "Erreur", "La date de fin doit être après la date de début"); return false; }
        if (dpDateFin.getValue().isEqual(dpDateDebut.getValue()))  { showAlert(Alert.AlertType.ERROR, "Erreur", "Les deux dates doivent être différentes"); return false; }
        String idStr = tfIdDestination.getText() != null ? tfIdDestination.getText().trim() : "";
        if (idStr.isEmpty()) { showAlert(Alert.AlertType.ERROR, "Erreur", "L'ID de destination est requis"); return false; }
        try {
            int id = Integer.parseInt(idStr);
            if (id <= 0) { showAlert(Alert.AlertType.ERROR, "Erreur", "L'ID de destination doit être positif"); return false; }
        } catch (NumberFormatException e) { showAlert(Alert.AlertType.ERROR, "Erreur", "L'ID de destination doit être un nombre"); return false; }
        return true;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  POPUP VOIR VOYAGE
    // ════════════════════════════════════════════════════════════════════════

    private void voirVoyage(Voyage voyage) {
        String nomDest = DEST_INCONNUE;
        try { nomDest = voyageCRUD.getNomDestination(voyage.getId_destination()); }
        catch (SQLException e) { e.printStackTrace(); }

        long durationDays = java.time.temporal.ChronoUnit.DAYS.between(
                voyage.getDate_debut().toLocalDate(), voyage.getDate_fin().toLocalDate());
        final String destination = nomDest;

        Stage popup = new Stage();
        popup.setTitle("📋 " + voyage.getTitre_voyage());

        javafx.scene.layout.VBox root = new javafx.scene.layout.VBox(0);
        root.setStyle("-fx-background-color: #0a0e27;");

        javafx.scene.layout.VBox header = new javafx.scene.layout.VBox(4);
        header.setPadding(new Insets(20, 24, 16, 24));
        header.setStyle("-fx-background-color: linear-gradient(to right, #ff8c42, #ff6b4a);");
        Label titleLbl    = new Label(voyage.getTitre_voyage());
        titleLbl.setStyle("-fx-font-size: 22; -fx-font-weight: bold; -fx-text-fill: white;");
        Label subtitleLbl = new Label("📍 " + destination + "  •  " + durationDays + " jour" + (durationDays > 1 ? "s" : ""));
        subtitleLbl.setStyle("-fx-font-size: 12; -fx-text-fill: rgba(255,255,255,0.85);");
        header.getChildren().addAll(titleLbl, subtitleLbl);
        root.getChildren().add(header);

        javafx.scene.layout.HBox statsRow = new javafx.scene.layout.HBox(12);
        statsRow.setPadding(new Insets(14, 24, 14, 24));
        statsRow.setStyle("-fx-background-color: #111633;");
        statsRow.setAlignment(Pos.CENTER);
        statsRow.getChildren().addAll(
                buildStatCard("📅", "Début",  voyage.getDate_debut().toLocalDate().toString()),
                buildStatCard("🏁", "Fin",    voyage.getDate_fin().toLocalDate().toString()),
                buildStatCard("📊", "Statut", voyage.getStatut()),
                buildStatCard("🔢", "ID",     String.valueOf(voyage.getId_voyage()))
        );
        root.getChildren().add(statsRow);

        javafx.scene.layout.VBox countrySection = new javafx.scene.layout.VBox(8);
        countrySection.setPadding(new Insets(16, 24, 8, 24));
        Label sectionTitle = new Label("🌍 Informations sur le pays");
        sectionTitle.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #ff8c42;");
        Label loadingLbl = new Label("⏳ Chargement des données...");
        loadingLbl.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11;");
        countrySection.getChildren().addAll(sectionTitle, loadingLbl);
        root.getChildren().add(countrySection);

        javafx.scene.layout.VBox qrSection = new javafx.scene.layout.VBox(8);
        qrSection.setPadding(new Insets(8, 24, 8, 24));
        qrSection.setAlignment(Pos.CENTER);
        Label qrTitle   = new Label("📱 QR Code");
        qrTitle.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #ff8c42;");
        Label qrLoading = new Label("⏳ Génération...");
        qrLoading.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11;");
        qrSection.getChildren().addAll(qrTitle, qrLoading);
        root.getChildren().add(qrSection);

        Label footer = new Label("TravelMate — Back Office • APIs : REST Countries • ZXing • Unsplash");
        footer.setStyle("-fx-font-size: 9; -fx-text-fill: #4a5568; -fx-padding: 12 24;");
        root.getChildren().add(footer);

        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #0a0e27; -fx-background-color: #0a0e27;");
        popup.setScene(new Scene(scrollPane, 560, 650));
        popup.show();

        new Thread(() -> {
            RestCountriesService.CountryInfo countryInfo = restCountriesService.getInfosPays(destination);
            Image qrImage = null;
            try { qrImage = qrCodeServicess.genererQRCodeVoyage(voyage, destination); }
            catch (Exception e) { System.out.println("⚠️ QR : " + e.getMessage()); }
            UnsplashService.PhotoInfo photoInfo = unsplashService.rechercherPhotoInfo(destination);
            final Image finalQr = qrImage;

            javafx.application.Platform.runLater(() -> {
                countrySection.getChildren().remove(loadingLbl);
                if (countryInfo != null) {
                    if (!countryInfo.drapeauUrl.isEmpty()) {
                        try {
                            ImageView flag = new ImageView(new Image(countryInfo.drapeauUrl, 50, 30, true, true));
                            javafx.scene.layout.HBox flagRow = new javafx.scene.layout.HBox(8);
                            flagRow.setAlignment(Pos.CENTER_LEFT);
                            Label countryName = new Label(countryInfo.nomOfficiel);
                            countryName.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 13; -fx-font-weight: 600;");
                            flagRow.getChildren().addAll(flag, countryName);
                            countrySection.getChildren().add(flagRow);
                        } catch (Exception ignored) {}
                    }
                    countrySection.getChildren().addAll(
                            buildInfoLabel("🏛️ Capitale",   countryInfo.capitale),
                            buildInfoLabel("👥 Population", RestCountriesService.formatPopulation(countryInfo.population)),
                            buildInfoLabel("🌍 Continent",  countryInfo.continent),
                            buildInfoLabel("🗣️ Langues",   countryInfo.langues),
                            buildInfoLabel("💱 Devises",    countryInfo.devises),
                            buildInfoLabel("🕐 Fuseau",     countryInfo.fuseauxHoraires)
                    );
                } else {
                    Label noData = new Label("❌ Données pays non disponibles");
                    noData.setStyle("-fx-text-fill: #ec4899; -fx-font-size: 11;");
                    countrySection.getChildren().add(noData);
                }
                qrSection.getChildren().remove(qrLoading);
                if (finalQr != null) {
                    ImageView qrView = new ImageView(finalQr);
                    qrView.setFitWidth(180); qrView.setFitHeight(180); qrView.setPreserveRatio(true);
                    qrSection.getChildren().add(qrView);
                }
                if (photoInfo != null && !photoInfo.urlRegular.isEmpty()) {
                    try {
                        javafx.scene.layout.VBox photoSection = new javafx.scene.layout.VBox(8);
                        photoSection.setPadding(new Insets(8, 24, 8, 24));
                        Label photoTitle = new Label("📸 Photo de la destination");
                        photoTitle.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #ff8c42;");
                        ImageView photoView = new ImageView(new Image(photoInfo.urlSmall, 500, 200, true, true, false));
                        photoView.setFitWidth(500); photoView.setPreserveRatio(true);
                        photoView.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 10, 0, 0, 2);");
                        Label credit = new Label(photoInfo.getCredit());
                        credit.setStyle("-fx-font-size: 9; -fx-text-fill: #64748b;");
                        photoSection.getChildren().addAll(photoTitle, photoView, credit);
                        root.getChildren().add(root.getChildren().indexOf(footer), photoSection);
                    } catch (Exception e) { System.out.println("⚠️ Photo : " + e.getMessage()); }
                }
            });
        }).start();
    }

    private javafx.scene.layout.VBox buildStatCard(String icon, String label, String value) {
        javafx.scene.layout.VBox card = new javafx.scene.layout.VBox(2);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(8, 14, 8, 14));
        card.setStyle("-fx-background-color: #1e2749; -fx-background-radius: 10;");
        Label iconLbl  = new Label(icon);  iconLbl.setStyle("-fx-font-size: 16;");
        Label labelLbl = new Label(label); labelLbl.setStyle("-fx-font-size: 9; -fx-text-fill: #94a3b8; -fx-font-weight: 600;");
        Label valueLbl = new Label(value != null ? value : "N/A"); valueLbl.setStyle("-fx-font-size: 11; -fx-text-fill: #ffffff; -fx-font-weight: 500;");
        card.getChildren().addAll(iconLbl, labelLbl, valueLbl);
        return card;
    }

    private javafx.scene.layout.HBox buildInfoLabel(String label, String value) {
        javafx.scene.layout.HBox row = new javafx.scene.layout.HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(3, 10, 3, 10));
        row.setStyle("-fx-background-color: #1e2749; -fx-background-radius: 8;");
        Label lbl = new Label(label); lbl.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11; -fx-min-width: 120;");
        Label val = new Label(value != null && !value.isEmpty() ? value : "N/A");
        val.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 11;"); val.setWrapText(true);
        row.getChildren().addAll(lbl, val);
        return row;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  PARTICIPANTS
    // ════════════════════════════════════════════════════════════════════════

    private void ouvrirParticipants(Voyage voyage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/PageParticipationBack.fxml"));
            Parent root = loader.load();
            ParticipationBackController ctrl = loader.getController();
            String nomDest = DEST_INCONNUE;
            try { nomDest = voyageCRUD.getNomDestination(voyage.getId_destination()); }
            catch (SQLException e) { e.printStackTrace(); }
            ctrl.initData(voyage.getId_voyage(), voyage.getTitre_voyage(), nomDest,
                    voyage.getDate_debut().toLocalDate() + " - " + voyage.getDate_fin().toLocalDate());
            Stage stage = (Stage) tableVoyages.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir les participants : " + e.getMessage());
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  EXPORT
    // ════════════════════════════════════════════════════════════════════════

    @FXML
    private void exportToPDF(ActionEvent event) {
        ObservableList<Voyage> data = (filteredData != null && !filteredData.isEmpty())
                ? FXCollections.observableArrayList(filteredData) : voyageList;
        if (data.isEmpty()) { showAlert(Alert.AlertType.WARNING, "Attention", "Aucune donnée à exporter"); return; }
        try {
            ExportService.exportToPDF(data, (Stage) tableVoyages.getScene().getWindow(), "Liste des voyages - TravelMate");
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Export PDF terminé !");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Export PDF impossible : " + e.getMessage());
        }
    }

    @FXML
    private void exportToExcel(ActionEvent event) {
        ObservableList<Voyage> data = (filteredData != null && !filteredData.isEmpty())
                ? FXCollections.observableArrayList(filteredData) : voyageList;
        if (data.isEmpty()) { showAlert(Alert.AlertType.WARNING, "Attention", "Aucune donnée à exporter"); return; }
        try {
            ExportService.exportToExcel(data, (Stage) tableVoyages.getScene().getWindow());
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Export Excel terminé !");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Export Excel impossible : " + e.getMessage());
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  NAVIGATION
    // ════════════════════════════════════════════════════════════════════════

    private void naviguerVers(String fxmlPath, String titre) {
        try {
            URL resourceUrl = getClass().getResource(fxmlPath);
            if (resourceUrl == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Fichier introuvable : " + fxmlPath);
                return;
            }
            FXMLLoader loader = new FXMLLoader(resourceUrl);
            Parent root = loader.load();
            Stage stage = (Stage) tableVoyages.getScene().getWindow();
            stage.setScene(new Scene(root,
                    javafx.stage.Screen.getPrimary().getVisualBounds().getWidth(),
                    javafx.stage.Screen.getPrimary().getVisualBounds().getHeight()));
            stage.setTitle("TravelMate - " + titre);
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de naviguer vers " + titre + " :\n" + e.getMessage());
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  UTILISATEUR
    // ════════════════════════════════════════════════════════════════════════

    private void updateUserInfo() {
        try {
            User u = UserSession.getInstance().getCurrentUser();
            if (u != null) {
                if (lblUserName != null) lblUserName.setText(u.getPrenom() + " " + u.getNom());
                if (lblUserRole != null) lblUserRole.setText(u.getRole());
            } else {
                if (lblUserName != null) lblUserName.setText("Utilisateur");
                if (lblUserRole != null) lblUserRole.setText("Administrateur");
            }
        } catch (Exception e) {
            if (lblUserName != null) lblUserName.setText("Utilisateur");
            if (lblUserRole != null) lblUserRole.setText("Administrateur");
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  UTILITAIRE
    // ════════════════════════════════════════════════════════════════════════

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}