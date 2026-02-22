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

import java.net.URL;
import java.sql.Date;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class BudgetBackController implements Initializable {

    // ===== KPI LABELS =====
    @FXML private Label lblTotalBudgets;
    @FXML private Label lblBudgetsActifs;
    @FXML private Label lblMontantGlobal;
    @FXML private Label lblLastUpdate;
    @FXML private Label lblMenuBudgetCount;

    // ===== STATISTIQUES =====
    @FXML private PieChart pieChartRepartition;
    @FXML private TableView<CategorieStat> tableTopCategories;
    @FXML private TableColumn<CategorieStat, String> colCategorieNom;
    @FXML private TableColumn<CategorieStat, Double> colCategorieMontant;
    @FXML private TableColumn<CategorieStat, Double> colCategoriePourcentage;

    // ===== TABLE DES BUDGETS =====
    @FXML private TableView<Budget> tableBudgets;
    @FXML private TableColumn<Budget, Integer> colIdBudget;
    @FXML private TableColumn<Budget, String> colLibelleBudget;
    @FXML private TableColumn<Budget, Double> colMontantTotal;
    @FXML private TableColumn<Budget, String> colDeviseBudget;
    @FXML private TableColumn<Budget, String> colStatutBudget;
    @FXML private TableColumn<Budget, String> colVoyageAssocie;
    @FXML private TableColumn<Budget, String> colDestination;
    @FXML private TableColumn<Budget, Void> colActions;

    // ===== DÉTAIL BUDGET =====
    @FXML private Label lblBudgetNom;
    @FXML private Label lblMontantTotal;
    @FXML private Label lblDepense;
    @FXML private Label lblRestant;

    // ===== TABLE DES DÉPENSES =====
    @FXML private TableView<Depense> tableDepenses;
    @FXML private TableColumn<Depense, String> colLibelle;
    @FXML private TableColumn<Depense, String> colCategorie;
    @FXML private TableColumn<Depense, Double> colMontant;
    @FXML private TableColumn<Depense, String> colDevise;
    @FXML private TableColumn<Depense, Date> colDate;
    @FXML private TableColumn<Depense, String> colPaiement;

    private BudgetCRUD budgetCRUD = new BudgetCRUD();
    private DepenseCRUD depenseCRUD = new DepenseCRUD();
    private VoyageHelper voyageHelper = new VoyageHelper();

    private ObservableList<Budget> budgetList = FXCollections.observableArrayList();
    private ObservableList<Depense> depenseList = FXCollections.observableArrayList();
    private Map<Integer, VoyageInfo> voyagesInfoMap;
    private DecimalFormat df = new DecimalFormat("#,##0.00");
    private Budget currentBudget = null;

    // ===== CLASSE INTERNE POUR LES STATISTIQUES =====
    public static class CategorieStat {
        private String nom;
        private double montant;
        private double pourcentage;

        public CategorieStat(String nom, double montant, double pourcentage) {
            this.nom = nom;
            this.montant = montant;
            this.pourcentage = pourcentage;
        }

        public String getNom() { return nom; }
        public double getMontant() { return montant; }
        public double getPourcentage() { return pourcentage; }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadVoyages();
        loadBudgets();
        setupBudgetTable();
        setupDepenseTable();
        setupSelectionListener();
        loadStatistics();
        updateLastUpdate();
    }

    private void loadVoyages() {
        try {
            voyagesInfoMap = voyageHelper.getAllVoyagesInfo();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadBudgets() {
        try {
            List<Budget> budgets = budgetCRUD.getBudgetsByUserId(1);
            budgetList.clear();
            budgetList.addAll(budgets);
            tableBudgets.setItems(budgetList);
            lblMenuBudgetCount.setText(String.valueOf(budgets.size()));
            updateStatistics();

            if (!budgets.isEmpty() && currentBudget == null) {
                tableBudgets.getSelectionModel().select(0);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les budgets");
        }
    }

    private void setupBudgetTable() {
        colIdBudget.setCellValueFactory(new PropertyValueFactory<>("idBudget"));
        colLibelleBudget.setCellValueFactory(new PropertyValueFactory<>("libelleBudget"));
        colMontantTotal.setCellValueFactory(new PropertyValueFactory<>("montantTotal"));
        colDeviseBudget.setCellValueFactory(new PropertyValueFactory<>("deviseBudget"));
        colStatutBudget.setCellValueFactory(new PropertyValueFactory<>("statutBudget"));

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

        colMontantTotal.setCellFactory(column -> new TableCell<Budget, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(df.format(item));
                    setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-weight: 600;");
                }
            }
        });

        colStatutBudget.setCellFactory(column -> new TableCell<Budget, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    if ("ACTIF".equals(item)) {
                        setStyle("-fx-background-color: #d1fae5; -fx-text-fill: #065f46; -fx-font-weight: 600; -fx-background-radius: 12; -fx-padding: 4 8;");
                    } else {
                        setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #475569; -fx-background-radius: 12; -fx-padding: 4 8;");
                    }
                }
            }
        });

        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnVoir = new Button("Voir");
            private final HBox pane = new HBox(btnVoir);
            {
                btnVoir.setOnAction(event -> {
                    Budget budget = getTableView().getItems().get(getIndex());
                    afficherDetailsBudget(budget);
                    tableBudgets.getSelectionModel().select(budget);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void setupDepenseTable() {
        colLibelle.setCellValueFactory(new PropertyValueFactory<>("libelleDepense"));
        colCategorie.setCellValueFactory(new PropertyValueFactory<>("categorieDepense"));
        colMontant.setCellValueFactory(new PropertyValueFactory<>("montantDepense"));
        colDevise.setCellValueFactory(new PropertyValueFactory<>("deviseDepense"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateCreation"));
        colPaiement.setCellValueFactory(new PropertyValueFactory<>("typePaiement"));

        colMontant.setCellFactory(column -> new TableCell<Depense, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(df.format(item));
                    setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-weight: 600;");
                }
            }
        });

        colDate.setCellFactory(column -> new TableCell<Depense, Date>() {
            @Override
            protected void updateItem(Date item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                    setStyle("-fx-alignment: CENTER;");
                }
            }
        });
    }

    private void loadStatistics() {
        try {
            List<Depense> allDepenses = depenseCRUD.getAllDepenses();

            // Mettre à jour le PieChart
            Map<String, Double> depensesParCategorie = allDepenses.stream()
                    .collect(Collectors.groupingBy(
                            Depense::getCategorieDepense,
                            Collectors.summingDouble(Depense::getMontantDepense)
                    ));

            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
            for (Map.Entry<String, Double> entry : depensesParCategorie.entrySet()) {
                if (entry.getValue() > 0) {
                    pieData.add(new PieChart.Data(entry.getKey() + " (" + df.format(entry.getValue()) + "€)", entry.getValue()));
                }
            }

            if (pieData.isEmpty()) {
                pieData.add(new PieChart.Data("Aucune dépense", 1));
            }

            pieChartRepartition.setData(pieData);

            // Mettre à jour le Top 5
            double totalGeneral = allDepenses.stream().mapToDouble(Depense::getMontantDepense).sum();

            List<CategorieStat> stats = new ArrayList<>();
            for (Map.Entry<String, Double> entry : depensesParCategorie.entrySet()) {
                double pourcentage = totalGeneral > 0 ? (entry.getValue() / totalGeneral) * 100 : 0;
                stats.add(new CategorieStat(entry.getKey(), entry.getValue(), pourcentage));
            }

            List<CategorieStat> top5 = stats.stream()
                    .sorted((s1, s2) -> Double.compare(s2.getMontant(), s1.getMontant()))
                    .limit(5)
                    .collect(Collectors.toList());

            // Configuration des colonnes
            colCategorieNom.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty(cellData.getValue().getNom()));
            colCategorieMontant.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getMontant()).asObject());
            colCategoriePourcentage.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getPourcentage()).asObject());

            // Formatage des cellules
            colCategorieMontant.setCellFactory(column -> new TableCell<CategorieStat, Double>() {
                @Override
                protected void updateItem(Double item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(df.format(item) + " €");
                        setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-weight: 600;");
                    }
                }
            });

            colCategoriePourcentage.setCellFactory(column -> new TableCell<CategorieStat, Double>() {
                @Override
                protected void updateItem(Double item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(String.format("%.1f%%", item));
                        setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-weight: 600;");
                    }
                }
            });

            tableTopCategories.setItems(FXCollections.observableArrayList(top5));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateStatistics() {
        int total = budgetList.size();
        long actifs = budgetList.stream().filter(b -> "ACTIF".equals(b.getStatutBudget())).count();
        double montantGlobal = budgetList.stream().mapToDouble(Budget::getMontantTotal).sum();

        lblTotalBudgets.setText(String.valueOf(total));
        lblBudgetsActifs.setText(String.valueOf(actifs));
        lblMontantGlobal.setText(df.format(montantGlobal) + " €");
    }

    private void setupSelectionListener() {
        tableBudgets.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                afficherDetailsBudget(newVal);
            }
        });
    }

    private void afficherDetailsBudget(Budget budget) {
        currentBudget = budget;
        lblBudgetNom.setText(budget.getLibelleBudget());
        lblMontantTotal.setText(df.format(budget.getMontantTotal()) + " " + budget.getDeviseBudget());

        try {
            List<Depense> depenses = depenseCRUD.getDepensesByBudgetId(budget.getIdBudget());
            depenseList.clear();
            depenseList.addAll(depenses);
            tableDepenses.setItems(depenseList);

            double totalDepense = depenseList.stream().mapToDouble(Depense::getMontantDepense).sum();
            double restant = budget.getMontantTotal() - totalDepense;

            lblDepense.setText(df.format(totalDepense) + " " + budget.getDeviseBudget());
            lblRestant.setText(df.format(restant) + " " + budget.getDeviseBudget());

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateLastUpdate() {
        lblLastUpdate.setText("Dernière mise à jour : " +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}