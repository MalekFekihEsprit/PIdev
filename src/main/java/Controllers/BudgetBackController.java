package Controllers;

import Entities.Budget;
import Entities.Depense;
import Services.BudgetCRUD;
import Services.DepenseCRUD;
import Tools.VoyageHelper;
import Tools.VoyageHelper.VoyageInfo;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.sql.Date;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class BudgetBackController implements Initializable {

    // ===== DASHBOARD LEFT =====
    @FXML private VBox dashboardLeft;
    @FXML private Label lblAdminName;
    @FXML private Label lblAdminRole;
    @FXML private Label lblDate;

    // 5 ÉLÉMENTS DU DASHBOARD
    @FXML private Label lblTotalDestinations;
    @FXML private Label lblTotalBudgetsDash;
    @FXML private Label lblTotalItineraires;
    @FXML private Label lblTotalActivites;
    @FXML private Label lblTotalDepensesDash;

    @FXML private ListView<String> recentActivitiesList;
    @FXML private PieChart pieChartCategories;

    // ===== TABLE DES DÉPENSES =====
    @FXML private TableColumn<Depense, String> colCategorie;
    @FXML private TableColumn<Depense, Date> colDate;
    @FXML private TableColumn<Depense, String> colDevise;
    @FXML private TableColumn<Depense, String> colLibelle;
    @FXML private TableColumn<Depense, Double> colMontant;
    @FXML private TableColumn<Depense, String> colPaiement;
    @FXML private TableColumn<Depense, String> colDescription;
    @FXML private TableView<Depense> tableDepenses;

    // ===== TABLE DES BUDGETS =====
    @FXML private TableView<Budget> tableBudgets;
    @FXML private TableColumn<Budget, Integer> colIdBudget;
    @FXML private TableColumn<Budget, String> colLibelleBudget;
    @FXML private TableColumn<Budget, Double> colMontantTotal;
    @FXML private TableColumn<Budget, String> colDeviseBudget;
    @FXML private TableColumn<Budget, String> colStatutBudget;
    @FXML private TableColumn<Budget, String> colVoyageAssocie;
    @FXML private TableColumn<Budget, String> colDestination;
    @FXML private TableColumn<Budget, String> colDatesVoyage;
    @FXML private TableColumn<Budget, Void> colActions;

    // ===== LABELS STATISTIQUES =====
    @FXML private Label lblBudgetNom;
    @FXML private Label lblDepense;
    @FXML private Label lblMontantTotal;
    @FXML private Label lblRestant;
    @FXML private Label lblTotalBudgets;
    @FXML private Label lblBudgetsActifs;
    @FXML private Label lblMontantGlobal;
    @FXML private Label lblMoyenneBudget;
    @FXML private Label lblLastUpdate;

    // ===== FILTRES =====
    @FXML private ComboBox<String> cmbFiltreCategorie;
    @FXML private DatePicker dpDateDebut;
    @FXML private DatePicker dpDateFin;
    @FXML private Button btnAppliquerFiltres;
    @FXML private Button btnReinitialiserFiltres;
    @FXML private Label lblFiltresActifs;

    private BudgetCRUD budgetCRUD = new BudgetCRUD();
    private DepenseCRUD depenseCRUD = new DepenseCRUD();
    private VoyageHelper voyageHelper = new VoyageHelper();

    private ObservableList<Budget> budgetList = FXCollections.observableArrayList();
    private ObservableList<Depense> depenseList = FXCollections.observableArrayList();
    private ObservableList<Depense> filteredDepenseList = FXCollections.observableArrayList();
    private ObservableList<String> activitiesList = FXCollections.observableArrayList();
    private Map<Integer, VoyageInfo> voyagesInfoMap;
    private DecimalFormat df = new DecimalFormat("#,##0.00");
    private Budget currentBudget = null;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupDashboard();
        setupFilters();
        loadVoyages();
        loadBudgets();
        setupBudgetTable();
        setupDepenseTable();
        setupSelectionListener();
        updateLastUpdate();
        loadRecentActivities();
        updateCharts();
        loadDashboardStats();
    }

    /**
     * Configuration du dashboard gauche avec les 5 éléments
     */
    private void setupDashboard() {
        lblAdminName.setText("Yosr Boutaieb");
        lblAdminRole.setText("Administrateur");

        // Date du jour
        LocalDate today = LocalDate.now();
        lblDate.setText(today.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        // Configuration de la liste des activités récentes
        recentActivitiesList.setItems(activitiesList);
        recentActivitiesList.setCellFactory(list -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: #94a3b8; -fx-padding: 5;");
                }
            }
        });
    }

    /**
     * Charge les statistiques pour les 5 éléments du dashboard
     */
    private void loadDashboardStats() {
        // 1. Destinations
        try {
            Map<Integer, VoyageInfo> destinations = voyageHelper.getAllVoyagesInfo();
            lblTotalDestinations.setText(String.valueOf(destinations.size()));
        } catch (SQLException e) {
            lblTotalDestinations.setText("0");
        }

        // 2. Budgets
        lblTotalBudgetsDash.setText(String.valueOf(budgetList.size()));

        // 3. Itinéraires (simulé - à adapter selon votre logique métier)
        lblTotalItineraires.setText("8"); // Exemple

        // 4. Activités (simulé - à adapter)
        long totalActivites = depenseList.stream()
                .filter(d -> "Activités".equals(d.getCategorieDepense()))
                .count();
        lblTotalActivites.setText(String.valueOf(totalActivites));

        // 5. Dépenses
        double totalDepenses = budgetList.stream()
                .flatMap(b -> {
                    try {
                        return depenseCRUD.getDepensesByBudgetId(b.getIdBudget()).stream();
                    } catch (SQLException e) {
                        return java.util.stream.Stream.empty();
                    }
                })
                .mapToDouble(Depense::getMontantDepense)
                .sum();
        lblTotalDepensesDash.setText(df.format(totalDepenses) + " €");
    }

    /**
     * Charge les activités récentes
     */
    private void loadRecentActivities() {
        activitiesList.clear();
        activitiesList.add("✓ Nouvelle destination ajoutée");
        activitiesList.add("✓ Budget 'Paris 2026' créé");
        activitiesList.add("✓ Itinéraire 'Rome' modifié");
        activitiesList.add("✓ Activité 'Visite Louvre' ajoutée");
        activitiesList.add("✓ Dépense de 120€ enregistrée");
    }

    /**
     * Met à jour les graphiques
     */
    private void updateCharts() {
        // Calculer les totaux par catégorie pour tous les budgets
        Map<String, Double> totalsByCategory = depenseList.stream()
                .collect(Collectors.groupingBy(
                        Depense::getCategorieDepense,
                        Collectors.summingDouble(Depense::getMontantDepense)
                ));

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();

        for (Map.Entry<String, Double> entry : totalsByCategory.entrySet()) {
            if (entry.getValue() > 0) {
                PieChart.Data slice = new PieChart.Data(entry.getKey() + " (" + df.format(entry.getValue()) + "€)", entry.getValue());
                pieData.add(slice);
            }
        }

        // Si pas de données, ajouter des données par défaut
        if (pieData.isEmpty()) {
            pieData.add(new PieChart.Data("Aucune donnée", 1));
        }

        pieChartCategories.setData(pieData);
        pieChartCategories.setTitle("Répartition des dépenses");
        pieChartCategories.setLabelsVisible(true);
        pieChartCategories.setLegendVisible(false);
        pieChartCategories.setStyle("-fx-background-color: transparent;");
    }

    /**
     * Configuration des filtres
     */
    private void setupFilters() {
        cmbFiltreCategorie.setItems(FXCollections.observableArrayList(
                "Toutes les catégories", "Hébergement", "Transport", "Restauration",
                "Activités", "Shopping", "Autre"
        ));
        cmbFiltreCategorie.getSelectionModel().selectFirst();

        btnAppliquerFiltres.setOnAction(e -> appliquerFiltres());
        btnReinitialiserFiltres.setOnAction(e -> reinitialiserFiltres());
    }

    /**
     * Application des filtres
     */
    private void appliquerFiltres() {
        if (currentBudget == null) return;

        String categorie = cmbFiltreCategorie.getValue();
        LocalDate dateDebut = dpDateDebut.getValue();
        LocalDate dateFin = dpDateFin.getValue();

        List<Depense> filtered = depenseList.stream()
                .filter(d -> categorie.equals("Toutes les catégories") || d.getCategorieDepense().equals(categorie))
                .filter(d -> dateDebut == null || !d.getDateCreation().toLocalDate().isBefore(dateDebut))
                .filter(d -> dateFin == null || !d.getDateCreation().toLocalDate().isAfter(dateFin))
                .collect(Collectors.toList());

        filteredDepenseList.setAll(filtered);
        tableDepenses.setItems(filteredDepenseList);

        int nbFiltres = (categorie.equals("Toutes les catégories") ? 0 : 1) +
                (dateDebut != null ? 1 : 0) +
                (dateFin != null ? 1 : 0);
        lblFiltresActifs.setText(nbFiltres + " filtre(s) actif(s)");
        lblFiltresActifs.setVisible(nbFiltres > 0);
    }

    /**
     * Réinitialisation des filtres
     */
    private void reinitialiserFiltres() {
        cmbFiltreCategorie.getSelectionModel().selectFirst();
        dpDateDebut.setValue(null);
        dpDateFin.setValue(null);
        if (currentBudget != null) {
            filteredDepenseList.setAll(depenseList);
            tableDepenses.setItems(filteredDepenseList);
        }
        lblFiltresActifs.setVisible(false);
    }

    /**
     * Charge les informations des voyages
     */
    private void loadVoyages() {
        try {
            voyagesInfoMap = voyageHelper.getAllVoyagesInfo();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les voyages: " + e.getMessage());
        }
    }

    /**
     * Charge tous les budgets
     */
    private void loadBudgets() {
        try {
            List<Budget> budgets = budgetCRUD.getBudgetsByUserId(1); // ID admin par défaut
            budgetList.clear();
            budgetList.addAll(budgets);
            tableBudgets.setItems(budgetList);
            updateStatistics();
            loadDashboardStats();

            if (!budgets.isEmpty()) {
                tableBudgets.getSelectionModel().select(0);
                afficherDetailsBudget(budgets.get(0));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les budgets: " + e.getMessage());
        }
    }

    /**
     * Configuration de la table des budgets
     */
    private void setupBudgetTable() {
        colIdBudget.setCellValueFactory(new PropertyValueFactory<>("idBudget"));
        colLibelleBudget.setCellValueFactory(new PropertyValueFactory<>("libelleBudget"));
        colMontantTotal.setCellValueFactory(new PropertyValueFactory<>("montantTotal"));
        colDeviseBudget.setCellValueFactory(new PropertyValueFactory<>("deviseBudget"));
        colStatutBudget.setCellValueFactory(new PropertyValueFactory<>("statutBudget"));

        // Colonnes calculées (voyage, destination, dates)
        colVoyageAssocie.setCellValueFactory(cellData -> {
            Budget budget = cellData.getValue();
            String voyageNom = "Aucun";
            if (budget.getIdVoyage() != 0 && voyagesInfoMap != null) {
                VoyageInfo info = voyagesInfoMap.get(budget.getIdVoyage());
                voyageNom = info != null ? info.getTitre() : "Inconnu";
            }
            return new javafx.beans.property.SimpleStringProperty(voyageNom);
        });

        colDestination.setCellValueFactory(cellData -> {
            Budget budget = cellData.getValue();
            String destination = "—";
            if (budget.getIdVoyage() != 0 && voyagesInfoMap != null) {
                VoyageInfo info = voyagesInfoMap.get(budget.getIdVoyage());
                destination = info != null ? info.getNomDestination() : "—";
            }
            return new javafx.beans.property.SimpleStringProperty(destination);
        });

        colDatesVoyage.setCellValueFactory(cellData -> {
            Budget budget = cellData.getValue();
            String dates = "—";
            if (budget.getIdVoyage() != 0 && voyagesInfoMap != null) {
                VoyageInfo info = voyagesInfoMap.get(budget.getIdVoyage());
                dates = info != null ? info.getDatesFormatted() : "—";
            }
            return new javafx.beans.property.SimpleStringProperty(dates);
        });

        // Formatage de la colonne montant
        colMontantTotal.setCellFactory(column -> new TableCell<Budget, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(df.format(item));
                    setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-weight: bold; -fx-text-fill: #ff8c42;");
                }
            }
        });

        // Style pour la colonne statut
        colStatutBudget.setCellFactory(column -> new TableCell<Budget, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("ACTIF".equals(item)) {
                        setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #6b7280;");
                    }
                }
            }
        });

        // Colonne d'actions
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnVoir = new Button("👁");
            private final Button btnExport = new Button("📊");
            private final HBox pane = new HBox(5, btnVoir, btnExport);

            {
                btnVoir.setStyle("-fx-background-color: #2d3a5f; -fx-text-fill: white; -fx-cursor: hand; -fx-font-size: 12; -fx-background-radius: 5;");
                btnExport.setStyle("-fx-background-color: #2d3a5f; -fx-text-fill: white; -fx-cursor: hand; -fx-font-size: 12; -fx-background-radius: 5;");

                btnVoir.setOnAction(event -> {
                    Budget budget = getTableView().getItems().get(getIndex());
                    afficherDetailsBudget(budget);
                    tableBudgets.getSelectionModel().select(budget);
                });

                btnExport.setOnAction(event -> {
                    Budget budget = getTableView().getItems().get(getIndex());
                    exporterBudget(budget);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });

        tableBudgets.setItems(budgetList);
    }

    /**
     * Configuration de la table des dépenses
     */
    private void setupDepenseTable() {
        colLibelle.setCellValueFactory(new PropertyValueFactory<>("libelleDepense"));
        colCategorie.setCellValueFactory(new PropertyValueFactory<>("categorieDepense"));
        colMontant.setCellValueFactory(new PropertyValueFactory<>("montantDepense"));
        colDevise.setCellValueFactory(new PropertyValueFactory<>("deviseDepense"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateCreation"));
        colPaiement.setCellValueFactory(new PropertyValueFactory<>("typePaiement"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("descriptionDepense"));

        // Formatage de la colonne montant
        colMontant.setCellFactory(column -> new TableCell<Depense, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(df.format(item));
                    setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-weight: bold;");
                }
            }
        });

        // Formatage de la colonne date
        colDate.setCellFactory(column -> new TableCell<Depense, Date>() {
            @Override
            protected void updateItem(Date item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toString());
                    setStyle("-fx-alignment: CENTER;");
                }
            }
        });

        tableDepenses.setItems(filteredDepenseList);
    }

    /**
     * Met à jour les statistiques globales
     */
    private void updateStatistics() {
        int totalBudgets = budgetList.size();
        long actifs = budgetList.stream().filter(b -> "ACTIF".equals(b.getStatutBudget())).count();
        double montantGlobal = budgetList.stream().mapToDouble(Budget::getMontantTotal).sum();
        double moyenne = totalBudgets > 0 ? montantGlobal / totalBudgets : 0;

        lblTotalBudgets.setText(String.valueOf(totalBudgets));
        lblBudgetsActifs.setText(String.valueOf(actifs));
        lblMontantGlobal.setText(df.format(montantGlobal) + " €");
        lblMoyenneBudget.setText(df.format(moyenne) + " €");
    }

    /**
     * Configure le listener de sélection sur la table des budgets
     */
    private void setupSelectionListener() {
        tableBudgets.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                afficherDetailsBudget(newVal);
            }
        });
    }

    /**
     * Affiche les détails d'un budget (dépenses et infos)
     */
    private void afficherDetailsBudget(Budget budget) {
        currentBudget = budget;
        lblBudgetNom.setText(budget.getLibelleBudget());
        lblMontantTotal.setText(df.format(budget.getMontantTotal()) + " " + budget.getDeviseBudget());

        try {
            List<Depense> depenses = depenseCRUD.getDepensesByBudgetId(budget.getIdBudget());
            depenseList.clear();
            depenseList.addAll(depenses);
            filteredDepenseList.setAll(depenses);

            double totalDepense = depenseList.stream().mapToDouble(Depense::getMontantDepense).sum();
            double restant = budget.getMontantTotal() - totalDepense;

            lblDepense.setText(df.format(totalDepense) + " " + budget.getDeviseBudget());
            lblRestant.setText(df.format(restant) + " " + budget.getDeviseBudget());

            // Réinitialiser les filtres
            reinitialiserFiltres();

            // Mettre à jour le graphique
            updateCharts();

            // Mettre à jour le compteur d'activités
            long totalActivites = depenses.stream()
                    .filter(d -> "Activités".equals(d.getCategorieDepense()))
                    .count();
            lblTotalActivites.setText(String.valueOf(totalActivites));

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les dépenses: " + e.getMessage());
        }
    }

    /**
     * Exporte les données d'un budget (simulé)
     */
    private void exporterBudget(Budget budget) {
        showAlert(Alert.AlertType.INFORMATION, "Export",
                "Export du budget \"" + budget.getLibelleBudget() + "\" en cours...\n" +
                        "Total dépenses: " + df.format(depenseList.stream().mapToDouble(Depense::getMontantDepense).sum()) + " " + budget.getDeviseBudget());
    }

    /**
     * Met à jour le label de dernière mise à jour
     */
    private void updateLastUpdate() {
        lblLastUpdate.setText("Dernière mise à jour : " +
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
    }

    /**
     * Affiche une alerte
     */
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}