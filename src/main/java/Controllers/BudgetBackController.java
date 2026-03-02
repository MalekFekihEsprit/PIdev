package Controllers;

import Entities.Budget;
import Entities.Depense;
import Services.BudgetCRUD;
import Services.DepenseCRUD;
import Utils.UserSession;
import Utils.VoyageHelper;
import Utils.VoyageHelper.VoyageInfo;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.Date;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class BudgetBackController implements Initializable {

    // ══════════════════════════════════════════════════
    //  FXML — Navigation & Header
    // ══════════════════════════════════════════════════
    @FXML private ComboBox<String> cmbVoyageSelector;
    @FXML private Label lblUpdateTime;
    @FXML private Label lblLastUpdate;

    // User Profile elements
    @FXML private HBox userProfileBox;
    @FXML private Label lblUserName;
    @FXML private Label lblUserRole;

    // ══════════════════════════════════════════════════
    //  FXML — Sidebar stats
    // ══════════════════════════════════════════════════
    @FXML private Label lblTotalBudgetsCard;
    @FXML private Label lblActifsCard;
    @FXML private Label lblGlobalCard;
    @FXML private Label lblSidebarBudgetCount;

    // ══════════════════════════════════════════════════
    //  FXML — KPI cards (hero)
    // ══════════════════════════════════════════════════
    @FXML private Label lblTotalBudgetsKpi;
    @FXML private Label lblActifsKpi;
    @FXML private Label lblGlobalKpi;
    @FXML private Label lblBudgetCount;

    // ══════════════════════════════════════════════════
    //  FXML — Budgets table
    // ══════════════════════════════════════════════════
    @FXML private TableView<Budget>          tableBudgets;
    @FXML private TableColumn<Budget, Integer> colIdBudget;
    @FXML private TableColumn<Budget, String>  colLibelleBudget;
    @FXML private TableColumn<Budget, Double>  colMontantTotal;
    @FXML private TableColumn<Budget, String>  colDeviseBudget;
    @FXML private TableColumn<Budget, String>  colStatutBudget;
    @FXML private TableColumn<Budget, String>  colVoyageAssocie;
    @FXML private TableColumn<Budget, String>  colDestination;
    @FXML private TableColumn<Budget, Void>    colActionsBudget;

    // ══════════════════════════════════════════════════
    //  FXML — Analyse Financière
    // ══════════════════════════════════════════════════
    @FXML private Label lblAnalyseDate;
    @FXML private Label lblAnalyseTotalDepense;
    @FXML private Label lblAnalyseTotalDepenseSub;
    @FXML private Label lblAnalyseBudgetTotal;
    @FXML private Label lblAnalyseBudgetNb;
    @FXML private Label lblAnalyseSolde;
    @FXML private Label lblAnalyseSoldePct;
    @FXML private Label lblAnalyseMoyenne;
    @FXML private Label lblAnalyseMoyenneSub;

    // ── 4 nouveaux indicateurs ──
    @FXML private Label lblMoyenneParJour;
    @FXML private Label lblMoyenneParJourSub;
    @FXML private Label lblJourPlusDep;
    @FXML private Label lblJourPlusDepMontant;
    @FXML private Label lblProjection;
    @FXML private Label lblProjectionSub;
    @FXML private Label lblTauxUtilisation;
    @FXML private Label lblTauxUtilisationEtat;
    @FXML private Label lblTauxUtilisationSub;
    @FXML private Rectangle rectTauxBar;

    @FXML private TableView<BudgetStat>            tableAnalyse;
    @FXML private TableColumn<BudgetStat, String>  colAnalyseNom;
    @FXML private TableColumn<BudgetStat, Double>  colAnalyseAlloue;
    @FXML private TableColumn<BudgetStat, Double>  colAnalyseDepense;
    @FXML private TableColumn<BudgetStat, Double>  colAnalyseSolde;
    @FXML private TableColumn<BudgetStat, Double>  colAnalyseTaux;
    @FXML private TableColumn<BudgetStat, String>  colAnalyseStatut;

    // ══════════════════════════════════════════════════
    //  FXML — Pie Chart
    // ══════════════════════════════════════════════════
    @FXML private PieChart pieChartDepenses;

    // ══════════════════════════════════════════════════
    //  FXML — Dépenses table & filtres
    // ══════════════════════════════════════════════════
    @FXML private TableView<Depense>          tableDepenses;
    @FXML private TableColumn<Depense, String> colDepenseLibelle;
    @FXML private TableColumn<Depense, String> colDepenseCategorie;
    @FXML private TableColumn<Depense, Double> colDepenseMontant;
    @FXML private TableColumn<Depense, String> colDepenseDevise;
    @FXML private TableColumn<Depense, Date>   colDepenseDate;
    @FXML private TableColumn<Depense, String> colDepensePaiement;
    @FXML private TableColumn<Depense, String> colDepenseDescription;

    @FXML private Label        lblNbDepenses;
    @FXML private ComboBox<String> cmbFiltreCategorie;
    @FXML private DatePicker   dpDateDebut;
    @FXML private DatePicker   dpDateFin;
    @FXML private Button       btnAppliquerFiltres;
    @FXML private Button       btnReinitialiserFiltres;
    @FXML private ToggleButton btnTriMontant;
    @FXML private ToggleButton btnTriDate;
    @FXML private ToggleButton btnTriLibelle;

    // ══════════════════════════════════════════════════
    //  FXML — Sidebar navigation (Updated)
    // ══════════════════════════════════════════════════
    @FXML private HBox btnDestinations;
    @FXML private HBox btnActivites;
    @FXML private HBox btnHebergements;
    @FXML private HBox btnVoyages;
    @FXML private HBox btnBudgets;
    @FXML private HBox btnUtilisateurs;
    @FXML private HBox btnStatistiques;

    // ══════════════════════════════════════════════════
    //  Services & data
    // ══════════════════════════════════════════════════
    private final BudgetCRUD  budgetCRUD  = new BudgetCRUD();
    private final DepenseCRUD depenseCRUD = new DepenseCRUD();
    private final VoyageHelper voyageHelper = new VoyageHelper();

    private final ObservableList<Budget>  budgets  = FXCollections.observableArrayList();
    private final ObservableList<Depense> depenses = FXCollections.observableArrayList();
    private FilteredList<Depense> filteredDepenses;
    private Map<Integer, VoyageInfo> voyagesMap;
    // depenses totales par budget : budgetId -> total dépensé
    private final Map<Integer, Double> depensesParBudget = new HashMap<>();

    private final DecimalFormat df = new DecimalFormat("#,##0.00");
    private final DateTimeFormatter dtFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ══════════════════════════════════════════════════
    //  Inner class — BudgetStat (table analyse)
    // ══════════════════════════════════════════════════
    public static class BudgetStat {
        private final String nom;
        private final double alloue;
        private final double depense;
        private final double solde;
        private final double taux;
        private final String statut;

        public BudgetStat(String nom, double alloue, double depense, String statut) {
            this.nom     = nom;
            this.alloue  = alloue;
            this.depense = depense;
            this.solde   = alloue - depense;
            this.taux    = alloue > 0 ? (depense / alloue) * 100 : 0;
            this.statut  = statut;
        }
        public String getNom()     { return nom; }
        public double getAlloue()  { return alloue; }
        public double getDepense() { return depense; }
        public double getSolde()   { return solde; }
        public double getTaux()    { return taux; }
        public String getStatut()  { return statut; }
    }

    // ══════════════════════════════════════════════════
    //  Initialize
    // ══════════════════════════════════════════════════
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupUserProfile();
        loadVoyages();
        setupTables();
        setupFiltersAndSorts();
        setupSelectionListener();
        // La table démarre VIDE — chargée seulement après choix d'un voyage
        showEmptyState();
        updateLastUpdate();
        setupNavigation();
    }

    private void setupUserProfile() {
        var currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser != null) {
            lblUserName.setText(currentUser.getPrenom() + " " + currentUser.getNom());
            lblUserRole.setText(currentUser.getRole());
        } else {
            lblUserName.setText("Utilisateur");
            lblUserRole.setText("Non connecté");
        }

        if (userProfileBox != null) {
            userProfileBox.setOnMouseClicked(event -> navigateToProfile());
            userProfileBox.setOnMouseEntered(event ->
                    userProfileBox.setStyle("-fx-background-color: #1e2749; -fx-background-radius: 25; -fx-padding: 6 16 6 6; -fx-cursor: hand;"));
            userProfileBox.setOnMouseExited(event ->
                    userProfileBox.setStyle("-fx-background-color: #1e2749; -fx-background-radius: 25; -fx-padding: 6 16 6 6; -fx-cursor: hand;"));
        }
    }

    private void navigateToProfile() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/Profile.fxml"));
            Stage stage = (Stage) userProfileBox.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TravelMate - Profil");
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir le profil: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /** Remet tout à zéro quand aucun voyage n'est sélectionné */
    private void showEmptyState() {
        tableBudgets.setItems(FXCollections.observableArrayList());
        tableDepenses.setItems(FXCollections.observableArrayList());
        tableAnalyse.setItems(FXCollections.observableArrayList());
        pieChartDepenses.setData(FXCollections.observableArrayList(
                new PieChart.Data("Sélectionnez un voyage", 1)));
        depensesParBudget.clear();
        budgets.clear();
        resetKpiCards();
        resetAnalyseFinanciere();
    }

    /** KPI à zéro */
    private void resetKpiCards() {
        set(lblTotalBudgetsKpi,  "0");
        set(lblActifsKpi,        "0");
        set(lblGlobalKpi,        "0,00 €");
        set(lblTotalBudgetsCard, "0");
        set(lblActifsCard,       "0");
        set(lblGlobalCard,       "0,00 €");
        set(lblBudgetCount,      "0 budget");
        set(lblSidebarBudgetCount, "0");
    }

    /** Analyse à zéro */
    private void resetAnalyseFinanciere() {
        set(lblAnalyseTotalDepense,    "0,00 €");
        set(lblAnalyseTotalDepenseSub, "—");
        set(lblAnalyseBudgetTotal,     "0,00 €");
        set(lblAnalyseBudgetNb,        "—");
        set(lblAnalyseSolde,           "0,00 €");
        set(lblAnalyseSoldePct,        "—");
        set(lblAnalyseMoyenne,         "0,00 €");
        set(lblAnalyseMoyenneSub,      "—");
        set(lblAnalyseDate,            "Mis à jour : —");
        // reset 4 nouveaux indicateurs
        set(lblMoyenneParJour,       "— €/j");
        set(lblMoyenneParJourSub,    "durée du voyage : — jours");
        set(lblJourPlusDep,          "—");
        set(lblJourPlusDepMontant,   "—");
        set(lblProjection,           "—");
        set(lblProjectionSub,        "basé sur le rythme actuel");
        set(lblTauxUtilisation,      "0%");
        set(lblTauxUtilisationEtat,  "—");
        set(lblTauxUtilisationSub,   "du budget total consommé");
        if (rectTauxBar != null) rectTauxBar.setWidth(0);
    }

    // ══════════════════════════════════════════════════
    //  Load voyages
    // ══════════════════════════════════════════════════
    private void loadVoyages() {
        try {
            voyagesMap = voyageHelper.getAllVoyagesInfo();
            ObservableList<String> names = FXCollections.observableArrayList();
            for (VoyageInfo v : voyagesMap.values())
                names.add(v.getIdVoyage() + " - " + v.getTitre() + " (" + v.getDatesFormatted() + ")");
            cmbVoyageSelector.setItems(names);
            cmbVoyageSelector.setPromptText("🔍 Choisir un voyage...");
            cmbVoyageSelector.setValue(null);
            cmbVoyageSelector.setOnAction(e -> filtrerParVoyage());
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les voyages");
        }
    }

    // ══════════════════════════════════════════════════
    //  Charge les budgets d'UN voyage précis
    // ══════════════════════════════════════════════════
    private void loadBudgetsForVoyage(int voyageId) {
        try {
            // Charger TOUS les budgets puis filtrer par voyageId
            List<Budget> all = budgetCRUD.afficher();
            List<Budget> filtered = all.stream()
                    .filter(b -> b.getIdVoyage() == voyageId)
                    .collect(Collectors.toList());
            budgets.setAll(filtered);

            // Pré-charger les dépenses pour chaque budget de ce voyage
            depensesParBudget.clear();
            for (Budget b : budgets) {
                try {
                    double total = depenseCRUD.getDepensesByBudgetId(b.getIdBudget())
                            .stream().mapToDouble(Depense::getMontantDepense).sum();
                    depensesParBudget.put(b.getIdBudget(), total);
                } catch (SQLException ex) {
                    depensesParBudget.put(b.getIdBudget(), 0.0);
                }
            }

            tableBudgets.setItems(budgets);
            updateKpiCards();
            updateAnalyseFinanciere();

            if (!budgets.isEmpty()) {
                tableBudgets.getSelectionModel().select(0);
            } else {
                // Aucun budget pour ce voyage
                tableDepenses.setItems(FXCollections.observableArrayList());
                pieChartDepenses.setData(FXCollections.observableArrayList(
                        new PieChart.Data("Aucun budget pour ce voyage", 1)));
                set(lblNbDepenses, "0 dépense");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les budgets");
        }
    }

    // ══════════════════════════════════════════════════
    //  Load depenses for selected budget
    // ══════════════════════════════════════════════════
    private void loadDepensesForBudget(int budgetId) {
        try {
            List<Depense> list = depenseCRUD.getDepensesByBudgetId(budgetId);
            depenses.setAll(list);
            filteredDepenses = new FilteredList<>(depenses, p -> true);
            SortedList<Depense> sorted = new SortedList<>(filteredDepenses);
            sorted.comparatorProperty().bind(tableDepenses.comparatorProperty());
            tableDepenses.setItems(sorted);
            updateNbDepenses();
            updatePieChart(list);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les dépenses");
        }
    }

    // ══════════════════════════════════════════════════
    //  Pie chart — labels forcés en BLANC après rendu
    // ══════════════════════════════════════════════════
    private void updatePieChart(List<Depense> list) {
        Map<String, Double> map = list.stream().collect(
                Collectors.groupingBy(Depense::getCategorieDepense,
                        Collectors.summingDouble(Depense::getMontantDepense)));

        double total = map.values().stream().mapToDouble(Double::doubleValue).sum();
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        for (Map.Entry<String, Double> e : map.entrySet()) {
            double pct = total > 0 ? (e.getValue() / total) * 100 : 0;
            pieData.add(new PieChart.Data(
                    String.format("%s: %s€ (%.1f%%)", e.getKey(), df.format(e.getValue()), pct),
                    e.getValue()));
        }
        if (pieData.isEmpty()) pieData.add(new PieChart.Data("Aucune dépense", 1));
        pieChartDepenses.setData(pieData);

        // JavaFX ignore le CSS externe sur les labels du PieChart.
        // Solution : styler en Java apres que le scenegraph les a crees.
        javafx.application.Platform.runLater(this::applyPieChartWhiteLabels);
    }

    /**
     * Force tous les labels et la legende du PieChart en blanc.
     * Doit etre appele via Platform.runLater pour que les noeuds existent.
     */
    private void applyPieChartWhiteLabels() {
        // Labels flottants au-dessus de chaque tranche
        pieChartDepenses.lookupAll(".chart-pie-label").forEach(node -> {
            if (node instanceof javafx.scene.text.Text t) {
                t.setFill(javafx.scene.paint.Color.WHITE);
                t.setStyle("-fx-font-size:11px; -fx-font-weight:bold;");
            }
        });
        // Lignes de renvoi
        pieChartDepenses.lookupAll(".chart-pie-label-line").forEach(node -> {
            if (node instanceof javafx.scene.shape.Line line) {
                line.setStroke(javafx.scene.paint.Color.web("#94a3b8"));
            }
        });
        // Texte de la legende en bas
        pieChartDepenses.lookupAll(".chart-legend-item").forEach(node ->
                node.lookupAll(".label").forEach(lbl -> {
                    if (lbl instanceof javafx.scene.control.Label l) {
                        l.setTextFill(javafx.scene.paint.Color.WHITE);
                        l.setStyle("-fx-font-size:12px;");
                    }
                })
        );
    }

    // ══════════════════════════════════════════════════
    //  KPI Cards (top 3 cards + sidebar)
    // ══════════════════════════════════════════════════
    private void updateKpiCards() {
        int    total   = budgets.size();
        long   actifs  = budgets.stream().filter(b -> "ACTIF".equals(b.getStatutBudget())).count();
        double global  = budgets.stream().mapToDouble(Budget::getMontantTotal).sum();

        String totalS  = String.valueOf(total);
        String actifsS = String.valueOf(actifs);
        String globalS = df.format(global) + " €";

        set(lblTotalBudgetsKpi,  totalS);
        set(lblActifsKpi,        actifsS);
        set(lblGlobalKpi,        globalS);
        set(lblTotalBudgetsCard, totalS);
        set(lblActifsCard,       actifsS);
        set(lblGlobalCard,       globalS);
        set(lblBudgetCount,      total + " budget" + (total > 1 ? "s" : ""));
        set(lblSidebarBudgetCount, totalS);
    }

    // ══════════════════════════════════════════════════
    //  ANALYSE FINANCIÈRE COMPLÈTE
    // ══════════════════════════════════════════════════
    private void updateAnalyseFinanciere() {
        if (budgets.isEmpty()) return;

        double budgetAlloueTotal = budgets.stream().mapToDouble(Budget::getMontantTotal).sum();
        double depenseTotal      = depensesParBudget.values().stream().mapToDouble(Double::doubleValue).sum();
        double soldeTotal        = budgetAlloueTotal - depenseTotal;
        double soldePct          = budgetAlloueTotal > 0 ? (soldeTotal / budgetAlloueTotal) * 100 : 0;
        double moyenneBudget     = budgets.size() > 0 ? depenseTotal / budgets.size() : 0;
        int    nbActifs          = (int) budgets.stream().filter(b -> "ACTIF".equals(b.getStatutBudget())).count();

        // ── KPI financiers (4 cartes du haut) ──
        set(lblAnalyseTotalDepense,    df.format(depenseTotal) + " €");
        set(lblAnalyseTotalDepenseSub, "sur " + budgets.size() + " budget(s)");
        set(lblAnalyseBudgetTotal,     df.format(budgetAlloueTotal) + " €");
        set(lblAnalyseBudgetNb,        nbActifs + " budget(s) actif(s)");
        set(lblAnalyseSolde,           df.format(soldeTotal) + " €");
        set(lblAnalyseSoldePct,        String.format("%.1f%% disponible", soldePct));
        set(lblAnalyseMoyenne,         df.format(moyenneBudget) + " €");
        set(lblAnalyseMoyenneSub,      "dépense moyenne / budget");
        set(lblAnalyseDate,            "Mis à jour : " + LocalDateTime.now().format(dateFmt));

        // ════════════════════════════════════════════════════════
        // ── 1. DÉPENSE MOYENNE PAR JOUR — durée réelle du voyage
        //    On utilise les dates du voyage (dateDebut / dateFin)
        //    récupérées via voyagesMap. Si indisponibles, on utilise
        //    la période couverte par les dépenses enregistrées.
        // ════════════════════════════════════════════════════════
        List<Depense> toutesDepenses = new ArrayList<>();
        for (Budget b : budgets) {
            try { toutesDepenses.addAll(depenseCRUD.getDepensesByBudgetId(b.getIdBudget())); }
            catch (SQLException ignored) {}
        }

        // Obtenir l'id voyage depuis le premier budget (tous appartiennent au même voyage)
        int voyageId = budgets.isEmpty() ? 0 : budgets.get(0).getIdVoyage();
        long dureeJours = 1;

        if (voyageId > 0 && voyagesMap != null && voyagesMap.containsKey(voyageId)) {
            VoyageInfo vi = voyagesMap.get(voyageId);
            try {
                java.time.LocalDate debut = vi.getDateDebut() != null
                        ? vi.getDateDebut().toLocalDate() : null;
                java.time.LocalDate fin   = vi.getDateFin()   != null
                        ? vi.getDateFin().toLocalDate()   : null;
                if (debut != null && fin != null) {
                    dureeJours = Math.max(1,
                            java.time.temporal.ChronoUnit.DAYS.between(debut, fin) + 1);
                }
            } catch (Exception ignored) {
                // Fallback : durée calculée depuis les dépenses
            }
        }

        // Fallback si voyage sans dates : période couverte par les dépenses
        if (dureeJours == 1 && !toutesDepenses.isEmpty()) {
            Date dMin = toutesDepenses.stream()
                    .map(Depense::getDateCreation).min(Comparator.naturalOrder()).orElse(null);
            Date dMax = toutesDepenses.stream()
                    .map(Depense::getDateCreation).max(Comparator.naturalOrder()).orElse(null);
            if (dMin != null && dMax != null) {
                dureeJours = Math.max(1,
                        java.time.temporal.ChronoUnit.DAYS.between(
                                dMin.toLocalDate(), dMax.toLocalDate()) + 1);
            }
        }

        double moyenneJour = dureeJours > 0 ? depenseTotal / dureeJours : 0;
        set(lblMoyenneParJour,    df.format(moyenneJour) + " €/j");
        set(lblMoyenneParJourSub, "durée du voyage : " + dureeJours + " jour" + (dureeJours > 1 ? "s" : ""));

        // ── 2. JOUR LE PLUS DÉPENSIER ──
        if (!toutesDepenses.isEmpty()) {
            Map<java.time.LocalDate, Double> parJour = toutesDepenses.stream()
                    .collect(Collectors.groupingBy(
                            d -> d.getDateCreation().toLocalDate(),
                            Collectors.summingDouble(Depense::getMontantDepense)));

            java.time.LocalDate jourMax = parJour.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey).orElse(null);

            if (jourMax != null) {
                double montantJour = parJour.get(jourMax);
                long   nbDepJour   = toutesDepenses.stream()
                        .filter(d -> d.getDateCreation().toLocalDate().equals(jourMax)).count();
                set(lblJourPlusDep,        jourMax.format(dateFmt));
                set(lblJourPlusDepMontant, df.format(montantJour) + " € — " + nbDepJour + " dépense(s)");
            }
        } else {
            set(lblJourPlusDep,        "Aucune donnée");
            set(lblJourPlusDepMontant, "—");
        }

        // ── 3. PROJECTION DÉPASSEMENT ──
        if (!toutesDepenses.isEmpty() && moyenneJour > 0) {
            if (soldeTotal <= 0) {
                set(lblProjection,    "🚨 Budget dépassé !");
                set(lblProjectionSub, "Dépassement de " + df.format(Math.abs(soldeTotal)) + " €");
                if (lblProjection != null) lblProjection.setStyle("-fx-font-size:16;-fx-font-weight:bold;-fx-text-fill:#f87171;");
            } else {
                long joursAvant = (long) (soldeTotal / moyenneJour);
                if (joursAvant == 0) {
                    set(lblProjection,    "⚠ Dépassement imminent !");
                    set(lblProjectionSub, "Le solde est quasi épuisé");
                    if (lblProjection != null) lblProjection.setStyle("-fx-font-size:16;-fx-font-weight:bold;-fx-text-fill:#f87171;");
                } else if (joursAvant <= 3) {
                    set(lblProjection,    "🔴 Dans " + joursAvant + " jour(s)");
                    set(lblProjectionSub, "À ce rythme, budget dépassé très bientôt");
                    if (lblProjection != null) lblProjection.setStyle("-fx-font-size:16;-fx-font-weight:bold;-fx-text-fill:#f87171;");
                } else if (joursAvant <= 7) {
                    set(lblProjection,    "⚡ Dans " + joursAvant + " jours");
                    set(lblProjectionSub, "Rythme élevé — surveiller les dépenses");
                    if (lblProjection != null) lblProjection.setStyle("-fx-font-size:16;-fx-font-weight:bold;-fx-text-fill:#f59e0b;");
                } else {
                    set(lblProjection,    "✅ Dans " + joursAvant + " jours");
                    set(lblProjectionSub, "Rythme maîtrisé — budget sous contrôle");
                    if (lblProjection != null) lblProjection.setStyle("-fx-font-size:16;-fx-font-weight:bold;-fx-text-fill:#34d399;");
                }
            }
        } else {
            set(lblProjection,    "— Aucune donnée");
            set(lblProjectionSub, "Enregistrez des dépenses pour voir la projection");
            if (lblProjection != null) lblProjection.setStyle("-fx-font-size:16;-fx-font-weight:bold;-fx-text-fill:#64748b;");
        }

        // ── 4. TAUX D'UTILISATION GLOBAL avec barre colorée ──
        double tauxUtil = budgetAlloueTotal > 0 ? Math.min((depenseTotal / budgetAlloueTotal) * 100, 100) : 0;
        set(lblTauxUtilisation,    String.format("%.1f%%", tauxUtil));
        set(lblTauxUtilisationSub, "du budget total consommé");

        String tauxColor, tauxEtat, tauxEtatStyle;
        if (tauxUtil < 50) {
            tauxColor = "#34d399"; tauxEtat = "✅ Maîtrisé";
            tauxEtatStyle = "-fx-background-color:rgba(52,211,153,0.15);-fx-text-fill:#34d399;-fx-background-radius:20;-fx-padding:3 10;-fx-font-size:11;-fx-font-weight:700;";
        } else if (tauxUtil < 80) {
            tauxColor = "#f59e0b"; tauxEtat = "⚡ Vigilance";
            tauxEtatStyle = "-fx-background-color:rgba(245,158,11,0.15);-fx-text-fill:#f59e0b;-fx-background-radius:20;-fx-padding:3 10;-fx-font-size:11;-fx-font-weight:700;";
        } else {
            tauxColor = "#f87171"; tauxEtat = "🔴 Critique";
            tauxEtatStyle = "-fx-background-color:rgba(248,113,113,0.15);-fx-text-fill:#f87171;-fx-background-radius:20;-fx-padding:3 10;-fx-font-size:11;-fx-font-weight:700;";
        }
        if (lblTauxUtilisation    != null) lblTauxUtilisation.setStyle("-fx-font-size:24;-fx-font-weight:bold;-fx-text-fill:" + tauxColor + ";");
        if (lblTauxUtilisationEtat != null) { lblTauxUtilisationEtat.setText(tauxEtat); lblTauxUtilisationEtat.setStyle(tauxEtatStyle); }
        if (rectTauxBar != null) {
            final double tw = tauxUtil; final String tc = tauxColor;
            javafx.application.Platform.runLater(() -> {
                double pw = rectTauxBar.getParent() instanceof javafx.scene.layout.StackPane sp
                        ? sp.getWidth() : 250;
                if (pw < 10) pw = 250;
                rectTauxBar.setWidth((tw / 100.0) * pw);
                rectTauxBar.setStyle("-fx-fill:" + tc + ";");
            });
        }

        // ── Table résumé par budget ──
        List<BudgetStat> stats = budgets.stream()
                .map(b -> new BudgetStat(
                        b.getLibelleBudget(),
                        b.getMontantTotal(),
                        depensesParBudget.getOrDefault(b.getIdBudget(), 0.0),
                        b.getStatutBudget()))
                .sorted(Comparator.comparingDouble(BudgetStat::getTaux).reversed())
                .collect(Collectors.toList());
        if (tableAnalyse != null) tableAnalyse.setItems(FXCollections.observableArrayList(stats));
    }

    // ══════════════════════════════════════════════════
    //  Update nb depenses label
    // ══════════════════════════════════════════════════
    private void updateNbDepenses() {
        int sz = depenses.size();
        set(lblNbDepenses, sz + (sz > 1 ? " dépenses" : " dépense"));
    }

    // ══════════════════════════════════════════════════
    //  Setup Tables
    // ══════════════════════════════════════════════════
    private void setupTables() {
        // — Budgets —
        colIdBudget.setCellValueFactory(new PropertyValueFactory<>("idBudget"));
        colLibelleBudget.setCellValueFactory(new PropertyValueFactory<>("libelleBudget"));
        colMontantTotal.setCellValueFactory(new PropertyValueFactory<>("montantTotal"));
        colDeviseBudget.setCellValueFactory(new PropertyValueFactory<>("deviseBudget"));
        colStatutBudget.setCellValueFactory(new PropertyValueFactory<>("statutBudget"));

        colVoyageAssocie.setCellValueFactory(cd -> {
            Budget b = cd.getValue();
            String nom = "Aucun";
            if (b.getIdVoyage() != 0 && voyagesMap != null) {
                VoyageInfo v = voyagesMap.get(b.getIdVoyage());
                nom = v != null ? v.getTitre() : "Inconnu";
            }
            return new javafx.beans.property.SimpleStringProperty(nom);
        });

        colDestination.setCellValueFactory(cd -> {
            Budget b = cd.getValue();
            String dest = "—";
            if (b.getIdVoyage() != 0 && voyagesMap != null) {
                VoyageInfo v = voyagesMap.get(b.getIdVoyage());
                dest = v != null ? v.getNomDestination() : "—";
            }
            return new javafx.beans.property.SimpleStringProperty(dest);
        });

        colMontantTotal.setCellFactory(col -> new TableCell<Budget, Double>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null
                        : df.format(item) + " " + getTableView().getItems().get(getIndex()).getDeviseBudget());
            }
        });

        colStatutBudget.setCellFactory(col -> new TableCell<Budget, String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                setStyle("ACTIF".equals(item)
                        ? "-fx-text-fill:#34d399;-fx-font-weight:700;"
                        : "-fx-text-fill:#f87171;-fx-font-weight:700;");
            }
        });

        colActionsBudget.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("👁 Voir");
            { btn.setStyle("-fx-background-color:#ff8c42;-fx-text-fill:white;-fx-background-radius:8;-fx-padding:5 10;-fx-cursor:hand;-fx-font-size:11;");
                btn.setOnAction(e -> tableBudgets.getSelectionModel().select(getTableView().getItems().get(getIndex()))); }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty); setGraphic(empty ? null : btn); }
        });

        tableBudgets.setItems(budgets);

        // — Dépenses —
        colDepenseLibelle.setCellValueFactory(new PropertyValueFactory<>("libelleDepense"));
        colDepenseCategorie.setCellValueFactory(new PropertyValueFactory<>("categorieDepense"));
        colDepenseMontant.setCellValueFactory(new PropertyValueFactory<>("montantDepense"));
        colDepenseDevise.setCellValueFactory(new PropertyValueFactory<>("deviseDepense"));
        colDepenseDate.setCellValueFactory(new PropertyValueFactory<>("dateCreation"));
        colDepensePaiement.setCellValueFactory(new PropertyValueFactory<>("typePaiement"));
        colDepenseDescription.setCellValueFactory(new PropertyValueFactory<>("descriptionDepense"));

        colDepenseMontant.setCellFactory(col -> new TableCell<Depense, Double>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null
                        : df.format(item) + " " + getTableView().getItems().get(getIndex()).getDeviseDepense());
            }
        });

        colDepenseDate.setCellFactory(col -> new TableCell<Depense, Date>() {
            @Override protected void updateItem(Date item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.toLocalDate().format(dateFmt));
            }
        });

        // — Analyse table —
        colAnalyseNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colAnalyseAlloue.setCellValueFactory(new PropertyValueFactory<>("alloue"));
        colAnalyseDepense.setCellValueFactory(new PropertyValueFactory<>("depense"));
        colAnalyseSolde.setCellValueFactory(new PropertyValueFactory<>("solde"));
        colAnalyseTaux.setCellValueFactory(new PropertyValueFactory<>("taux"));
        colAnalyseStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

        colAnalyseAlloue.setCellFactory(col -> new TableCell<BudgetStat, Double>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : df.format(item) + " €");
            }
        });
        colAnalyseDepense.setCellFactory(col -> new TableCell<BudgetStat, Double>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(df.format(item) + " €");
                setStyle("-fx-text-fill:#f87171;-fx-font-weight:600;");
            }
        });
        colAnalyseSolde.setCellFactory(col -> new TableCell<BudgetStat, Double>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(df.format(item) + " €");
                setStyle(item >= 0 ? "-fx-text-fill:#34d399;-fx-font-weight:600;"
                        : "-fx-text-fill:#f87171;-fx-font-weight:700;");
            }
        });
        colAnalyseTaux.setCellFactory(col -> new TableCell<BudgetStat, Double>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(String.format("%.1f%%", item));
                String color = item > 100 ? "#f87171" : item > 75 ? "#f59e0b" : "#34d399";
                setStyle("-fx-text-fill:" + color + ";-fx-font-weight:700;");
            }
        });
        colAnalyseStatut.setCellFactory(col -> new TableCell<BudgetStat, String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                setStyle("ACTIF".equals(item) ? "-fx-text-fill:#34d399;-fx-font-weight:700;"
                        : "-fx-text-fill:#94a3b8;");
            }
        });
    }

    // ══════════════════════════════════════════════════
    //  Setup filters & sorts
    // ══════════════════════════════════════════════════
    private void setupFiltersAndSorts() {
        cmbFiltreCategorie.setItems(FXCollections.observableArrayList(
                "Toutes les catégories","Hébergement","Transport",
                "Restauration","Activités","Shopping","Autre"));
        cmbFiltreCategorie.setValue("Toutes les catégories");

        ToggleGroup tg = new ToggleGroup();
        btnTriMontant.setToggleGroup(tg);
        btnTriDate.setToggleGroup(tg);
        btnTriLibelle.setToggleGroup(tg);
    }

    // ══════════════════════════════════════════════════
    //  Selection listener
    // ══════════════════════════════════════════════════
    private void setupSelectionListener() {
        tableBudgets.getSelectionModel().selectedItemProperty().addListener((obs, old, nv) -> {
            if (nv != null) loadDepensesForBudget(nv.getIdBudget());
        });
    }

    // ══════════════════════════════════════════════════
    //  FXML handlers
    // ══════════════════════════════════════════════════
    @FXML
    private void filtrerParVoyage() {
        String sel = cmbVoyageSelector.getValue();
        if (sel == null || sel.isBlank()) {
            showEmptyState();
            return;
        }
        try {
            int voyageId = Integer.parseInt(sel.split(" - ")[0].trim());
            loadBudgetsForVoyage(voyageId);
        } catch (Exception e) {
            showAlert("Erreur", "Format de voyage invalide");
        }
    }

    @FXML
    private void handleAppliquerFiltres() {
        if (filteredDepenses == null) return;
        filteredDepenses.setPredicate(d -> {
            String cat = cmbFiltreCategorie.getValue();
            if (cat != null && !cat.equals("Toutes les catégories") && !d.getCategorieDepense().equals(cat)) return false;
            if (dpDateDebut.getValue() != null && d.getDateCreation().before(Date.valueOf(dpDateDebut.getValue()))) return false;
            if (dpDateFin.getValue()   != null && d.getDateCreation().after(Date.valueOf(dpDateFin.getValue())))   return false;
            return true;
        });
    }

    @FXML
    private void handleReinitialiserFiltres() {
        cmbFiltreCategorie.setValue("Toutes les catégories");
        dpDateDebut.setValue(null);
        dpDateFin.setValue(null);
        if (filteredDepenses != null) filteredDepenses.setPredicate(null);
    }

    @FXML private void handleTriMontant() {
        if (btnTriMontant.isSelected()) {
            tableDepenses.getSortOrder().setAll(colDepenseMontant);
            colDepenseMontant.setSortType(TableColumn.SortType.DESCENDING);
        }
    }
    @FXML private void handleTriDate() {
        if (btnTriDate.isSelected()) {
            tableDepenses.getSortOrder().setAll(colDepenseDate);
            colDepenseDate.setSortType(TableColumn.SortType.DESCENDING);
        }
    }
    @FXML private void handleTriLibelle() {
        if (btnTriLibelle.isSelected()) {
            tableDepenses.getSortOrder().setAll(colDepenseLibelle);
            colDepenseLibelle.setSortType(TableColumn.SortType.ASCENDING);
        }
    }

    // ══════════════════════════════════════════════════
    //  Navigation (Updated)
    // ══════════════════════════════════════════════════
    private void setupNavigation() {
        navigateToDestinations(btnDestinations, "Destinations");
        navigateToActivites(btnActivites, "Activités");
        navigateToHebergements(btnHebergements, "Hébergements");
        navigateToVoyages(btnVoyages, "Voyages");
        navigateToBudgets(btnBudgets, "Budgets");
        navigateToUtilisateurs(btnUtilisateurs, "Utilisateurs");
        navigateToStatistiques(btnStatistiques, "Statistiques");
    }

    private void navigateToDestinations(HBox btn, String page) {
        if (btn != null) btn.setOnMouseClicked(e -> {
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/DestinationBack.fxml"));
                Stage stage = (Stage) btn.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("TravelMate - Destinations (Admin)");
                stage.setMaximized(true);
            } catch (IOException ex) {
                showAlert("Erreur", "Impossible de charger la page: " + ex.getMessage());
                ex.printStackTrace();
            }
        });
    }

    private void navigateToActivites(HBox btn, String page) {
        if (btn != null) btn.setOnMouseClicked(e -> {
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/activitesback.fxml"));
                Stage stage = (Stage) btn.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("TravelMate - Activités (Admin)");
                stage.setMaximized(true);
            } catch (IOException ex) {
                showAlert("Erreur", "Impossible de charger la page: " + ex.getMessage());
                ex.printStackTrace();
            }
        });
    }

    private void navigateToHebergements(HBox btn, String page) {
        if (btn != null) btn.setOnMouseClicked(e -> {
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/HebergementBack.fxml"));
                Stage stage = (Stage) btn.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("TravelMate - Hébergements (Admin)");
                stage.setMaximized(true);
            } catch (IOException ex) {
                showAlert("Erreur", "Impossible de charger la page: " + ex.getMessage());
                ex.printStackTrace();
            }
        });
    }

    private void navigateToVoyages(HBox btn, String page) {
        if (btn != null) btn.setOnMouseClicked(e -> {
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/VoyageBack.fxml"));
                Stage stage = (Stage) btn.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("TravelMate - Voyages (Admin)");
                stage.setMaximized(true);
            } catch (IOException ex) {
                showAlert("Erreur", "Impossible de charger la page: " + ex.getMessage());
                ex.printStackTrace();
            }
        });
    }

    private void navigateToBudgets(HBox btn, String page) {
        if (btn != null) btn.setOnMouseClicked(e -> {
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/BudgetDepenseBack.fxml"));
                Stage stage = (Stage) btn.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("TravelMate - Budgets (Admin)");
                stage.setMaximized(true);
            } catch (IOException ex) {
                showAlert("Erreur", "Impossible de charger la page: " + ex.getMessage());
                ex.printStackTrace();
            }
        });
    }

    private void navigateToUtilisateurs(HBox btn, String page) {
        if (btn != null) btn.setOnMouseClicked(e -> {
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/UserBack.fxml"));
                Stage stage = (Stage) btn.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("TravelMate - Utilisateurs (Admin)");
                stage.setMaximized(true);
            } catch (IOException ex) {
                showAlert("Erreur", "Impossible de charger la page: " + ex.getMessage());
                ex.printStackTrace();
            }
        });
    }

    private void navigateToStatistiques(HBox btn, String page) {
        if (btn != null) btn.setOnMouseClicked(e -> {
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/StatistiquesBack.fxml"));
                Stage stage = (Stage) btn.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("TravelMate - Statistiques (Admin)");
                stage.setMaximized(true);
            } catch (IOException ex) {
                showAlert("Erreur", "Impossible de charger la page: " + ex.getMessage());
                ex.printStackTrace();
            }
        });
    }

    // ══════════════════════════════════════════════════
    //  Utilities
    // ══════════════════════════════════════════════════
    private void updateLastUpdate() {
        String now = LocalDateTime.now().format(dtFmt);
        set(lblUpdateTime, "Mise à jour : " + now);
        set(lblLastUpdate,  "Dernière mise à jour : " + now);
    }

    /** Null-safe label setter */
    private void set(Label lbl, String text) {
        if (lbl != null) lbl.setText(text);
    }

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}