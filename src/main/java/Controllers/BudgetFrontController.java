package Controllers;

import Entities.Budget;
import Entities.Depense;
import Services.BudgetCRUD;
import Services.DepenseCRUD;
import Tools.VoyageHelper;
import Tools.VoyageHelper.VoyageInfo;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.sql.Date;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class BudgetFrontController implements Initializable {

    // ===== BOUTONS =====
    @FXML private Button btnAjouterDepense;
    @FXML private Button btnAppliquerFiltres;
    @FXML private Button btnModifierBudget;
    @FXML private Button btnNouveauBudget;
    @FXML private Button btnReinitialiserFiltres;
    @FXML private Button btnSaveBudget;
    @FXML private Button btnSaveDepense;
    @FXML private Button btnSupprimerBudget;
    @FXML private Button btnVoirTousBudgets;
    @FXML private Label lblExportPDF; // Ajoutez ce label dans le FXML

    // ===== NOUVEAUX ÉLÉMENTS POUR L'ACCORDÉON =====
    @FXML private VBox contentTousBudgets;
    @FXML private VBox budgetsCardsContainer;
    @FXML private Label lblToggleIcon;
    @FXML private HBox headerTousBudgets;
    @FXML private Label lblNbTotalBudgets;
    private boolean isBudgetsSectionVisible = true;

    // ===== TOGGLE BUTTONS =====
    @FXML private ToggleButton btnTriDate;
    @FXML private ToggleButton btnTriLibelle;
    @FXML private ToggleButton btnTriMontant;
    @FXML private ToggleGroup triGroup;

    // ===== COMBOBOX =====
    @FXML private ComboBox<String> cmbCategorieDepense;
    @FXML private ComboBox<String> cmbDeviseBudget;
    @FXML private ComboBox<String> cmbDeviseDepense;
    @FXML private ComboBox<String> cmbFiltreCategorie;
    @FXML private ComboBox<String> cmbPaiementDepense;
    @FXML private ComboBox<String> cmbStatutBudget;
    @FXML private ComboBox<String> cmbVoyageBudget;

    // ===== TABLE COLUMNS =====
    @FXML private TableColumn<Depense, Void> colActions;
    @FXML private TableColumn<Depense, String> colCategorie;
    @FXML private TableColumn<Depense, Date> colDate;
    @FXML private TableColumn<Depense, String> colDescription;
    @FXML private TableColumn<Depense, String> colDevise;
    @FXML private TableColumn<Depense, String> colLibelle;
    @FXML private TableColumn<Depense, Double> colMontant;
    @FXML private TableColumn<Depense, String> colPaiement;

    // ===== DATEPICKER =====
    @FXML private DatePicker dateDepense;
    @FXML private DatePicker dpDateDebut;
    @FXML private DatePicker dpDateFin;

    // ===== LABELS =====
    @FXML private Label lblBudgetDates;
    @FXML private Label lblBudgetDescription;
    @FXML private Label lblBudgetDestination;
    @FXML private Label lblBudgetDevise;
    @FXML private Label lblBudgetIcon;
    @FXML private Label lblBudgetNom;
    @FXML private Label lblBudgetStatut;
    @FXML private Label lblDepense;
    @FXML private Label lblInfoBudget;
    @FXML private Label lblModalBudgetIcon;
    @FXML private Label lblModalBudgetSubtitle;
    @FXML private Label lblModalBudgetTitle;
    @FXML private Label lblModalTitle;
    @FXML private Label lblMontantTotal;
    @FXML private Label lblNbDepenses;
    @FXML private Label lblNomBudget;
    @FXML private Label lblPourcentageDepense;
    @FXML private Label lblPourcentageRestant;
    @FXML private Label lblRestant;

    // ===== TEXT INPUTS =====
    @FXML private TextField txtLibelleDepense;
    @FXML private TextField txtMontantBudget;
    @FXML private TextField txtMontantDepense;
    @FXML private TextField txtNomBudget;
    @FXML private TextArea txtDescriptionBudget;
    @FXML private TextArea txtNotesDepense;

    // ===== CONTAINERS =====
    @FXML private VBox budgetIconContainer;
    @FXML private VBox modalBudget;
    @FXML private VBox modalDepense;
    @FXML private TableView<Depense> tableDepenses;

    // ===== SERVICES =====
    private BudgetCRUD budgetCRUD = new BudgetCRUD();
    private DepenseCRUD depenseCRUD = new DepenseCRUD();
    private VoyageHelper voyageHelper = new VoyageHelper();

    // ===== DATA =====
    private ObservableList<Depense> depenseList = FXCollections.observableArrayList();
    private FilteredList<Depense> filteredData;
    private SortedList<Depense> sortedData;
    private Budget currentBudget = null;
    private int currentUserId = 1; // À remplacer par l'utilisateur connecté
    private boolean isEditingBudget = false;
    private boolean isEditingDepense = false;
    private Depense editingDepense = null;
    private DecimalFormat df = new DecimalFormat("#,##0.00");

    // Flag pour éviter la confirmation lors des changements programmatiques
    private boolean isProgrammaticChange = false;

    // Maps pour stocker les informations des voyages
    private Map<Integer, VoyageInfo> voyagesInfoMap;
    private Map<Integer, String> voyagesMap;
    private Map<String, Integer> voyagesReverseMap;

    // Map pour stocker les budgets associés aux voyages
    private Map<Integer, List<Budget>> budgetsParVoyage = new HashMap<>();

    // Liste de tous les budgets
    private ObservableList<Budget> allBudgetsList = FXCollections.observableArrayList();

    // État du tri
    private boolean isDateAscending = false;
    private boolean isMontantAscending = false;
    private boolean isLibelleAscending = true;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            loadVoyages();
            loadBudgetsParVoyage();
            setupComboBoxes();
            setupTableColumns();
            setupToggleGroup();
            setupAccordionListener();
            loadDefaultBudget();
            loadAllBudgets();
            setupListeners();
            setupExportHandlers();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les données: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Configure les gestionnaires d'export
     */
    private void setupExportHandlers() {
        // Le gestionnaire est déjà défini dans le FXML
    }

    /**
     * Configure le groupe de boutons de tri
     */
    private void setupToggleGroup() {
        triGroup = new ToggleGroup();
        btnTriDate.setToggleGroup(triGroup);
        btnTriLibelle.setToggleGroup(triGroup);
        btnTriMontant.setToggleGroup(triGroup);

        // Sélectionner le tri par date par défaut
        btnTriDate.setSelected(true);
        updateSortButtonStyles(btnTriDate);
    }

    /**
     * Configure les listeners
     */
    private void setupListeners() {
        triGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && sortedData != null) {
                // Réinitialiser l'état d'ascendance quand on change de type de tri
                if (newVal == btnTriDate) {
                    isDateAscending = false;
                } else if (newVal == btnTriMontant) {
                    isMontantAscending = false;
                } else if (newVal == btnTriLibelle) {
                    isLibelleAscending = true;
                }
                applySort();
            }
        });

        // Double-clic sur les boutons pour inverser le tri
        btnTriDate.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && btnTriDate.isSelected()) {
                isDateAscending = !isDateAscending;
                applySort();
            }
        });

        btnTriMontant.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && btnTriMontant.isSelected()) {
                isMontantAscending = !isMontantAscending;
                applySort();
            }
        });

        btnTriLibelle.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && btnTriLibelle.isSelected()) {
                isLibelleAscending = !isLibelleAscending;
                applySort();
            }
        });
    }

    /**
     * Applique le tri selon le bouton sélectionné
     */
    private void applySort() {
        if (sortedData == null || filteredData == null) return;

        if (btnTriDate.isSelected()) {
            if (isDateAscending) {
                // Tri par date croissant (plus ancien d'abord)
                sortedData.setComparator((d1, d2) -> d1.getDateCreation().compareTo(d2.getDateCreation()));
                btnTriDate.setText("📅 Date ↑");
            } else {
                // Tri par date décroissant (plus récent d'abord)
                sortedData.setComparator((d1, d2) -> d2.getDateCreation().compareTo(d1.getDateCreation()));
                btnTriDate.setText("📅 Date ↓");
            }
            updateSortButtonStyles(btnTriDate);
        } else if (btnTriLibelle.isSelected()) {
            if (isLibelleAscending) {
                // Tri par libellé A-Z
                sortedData.setComparator((d1, d2) -> d1.getLibelleDepense().compareToIgnoreCase(d2.getLibelleDepense()));
                btnTriLibelle.setText("📝 A-Z");
            } else {
                // Tri par libellé Z-A
                sortedData.setComparator((d1, d2) -> d2.getLibelleDepense().compareToIgnoreCase(d1.getLibelleDepense()));
                btnTriLibelle.setText("📝 Z-A");
            }
            updateSortButtonStyles(btnTriLibelle);
        } else if (btnTriMontant.isSelected()) {
            if (isMontantAscending) {
                // Tri par montant croissant
                sortedData.setComparator((d1, d2) -> Double.compare(d1.getMontantDepense(), d2.getMontantDepense()));
                btnTriMontant.setText("💰 Montant ↑");
            } else {
                // Tri par montant décroissant
                sortedData.setComparator((d1, d2) -> Double.compare(d2.getMontantDepense(), d1.getMontantDepense()));
                btnTriMontant.setText("💰 Montant ↓");
            }
            updateSortButtonStyles(btnTriMontant);
        }

        // Forcer le rafraîchissement
        tableDepenses.setItems(sortedData);
    }

    /**
     * Met à jour le style des boutons de tri
     */
    private void updateSortButtonStyles(ToggleButton activeButton) {
        String activeStyle = "-fx-background-color: #ff8c42; -fx-text-fill: white; -fx-border-color: #ff8c42; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 6 12; -fx-font-size: 12; -fx-cursor: hand;";
        String inactiveStyle = "-fx-background-color: #f8fafc; -fx-text-fill: #475569; -fx-border-color: #e2e8f0; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 6 12; -fx-font-size: 12; -fx-cursor: hand;";

        btnTriDate.setStyle(inactiveStyle);
        btnTriLibelle.setStyle(inactiveStyle);
        btnTriMontant.setStyle(inactiveStyle);

        if (activeButton != null) {
            activeButton.setStyle(activeStyle);
        }
    }

    /**
     * Charge tous les voyages avec leurs informations complètes
     */
    private void loadVoyages() throws SQLException {
        voyagesInfoMap = voyageHelper.getAllVoyagesInfo();
        voyagesMap = new HashMap<>();

        for (Map.Entry<Integer, VoyageInfo> entry : voyagesInfoMap.entrySet()) {
            voyagesMap.put(entry.getKey(), entry.getValue().getTitre());
        }

        voyagesReverseMap = new HashMap<>();
        for (Map.Entry<Integer, String> entry : voyagesMap.entrySet()) {
            voyagesReverseMap.put(entry.getValue(), entry.getKey());
        }
    }

    /**
     * Récupère les informations complètes d'un voyage
     */
    private VoyageInfo getVoyageInfo(int idVoyage) {
        return voyagesInfoMap != null ? voyagesInfoMap.get(idVoyage) : null;
    }

    /**
     * Charge tous les budgets et crée l'association voyage -> liste de budgets
     */
    private void loadBudgetsParVoyage() throws SQLException {
        List<Budget> budgets = budgetCRUD.getBudgetsByUserId(currentUserId);
        budgetsParVoyage.clear();

        for (Budget budget : budgets) {
            if (budget.getIdVoyage() != 0) {
                if (!budgetsParVoyage.containsKey(budget.getIdVoyage())) {
                    budgetsParVoyage.put(budget.getIdVoyage(), new ArrayList<>());
                }
                budgetsParVoyage.get(budget.getIdVoyage()).add(budget);
            }
        }
    }

    /**
     * Configure l'accordéon pour afficher/masquer la section des budgets
     */
    private void setupAccordionListener() {
        headerTousBudgets.setOnMouseClicked(event -> {
            isBudgetsSectionVisible = !isBudgetsSectionVisible;
            contentTousBudgets.setVisible(isBudgetsSectionVisible);
            contentTousBudgets.setManaged(isBudgetsSectionVisible);
            lblToggleIcon.setText(isBudgetsSectionVisible ? "▼" : "▶");
        });
    }

    /**
     * Met à jour l'icône du budget en fonction de la destination
     */
    private void updateBudgetIcon(String destination) {
        if (destination == null) return;

        String destinationLower = destination.toLowerCase();

        if (destinationLower.contains("paris") || destinationLower.contains("france")) {
            lblBudgetIcon.setText("🗼");
            budgetIconContainer.setStyle("-fx-background-color: #fff7ed; -fx-background-radius: 24; -fx-min-width: 80; -fx-min-height: 80;");
        } else if (destinationLower.contains("londres") || destinationLower.contains("angleterre")) {
            lblBudgetIcon.setText("🇬🇧");
            budgetIconContainer.setStyle("-fx-background-color: #e6f7ff; -fx-background-radius: 24; -fx-min-width: 80; -fx-min-height: 80;");
        } else if (destinationLower.contains("tunis") || destinationLower.contains("tunisie")) {
            lblBudgetIcon.setText("🇹🇳");
            budgetIconContainer.setStyle("-fx-background-color: #fff1e6; -fx-background-radius: 24; -fx-min-width: 80; -fx-min-height: 80;");
        } else if (destinationLower.contains("rome") || destinationLower.contains("italie")) {
            lblBudgetIcon.setText("🍕");
            budgetIconContainer.setStyle("-fx-background-color: #fef2e6; -fx-background-radius: 24; -fx-min-width: 80; -fx-min-height: 80;");
        } else if (destinationLower.contains("tokyo") || destinationLower.contains("japon")) {
            lblBudgetIcon.setText("🗻");
            budgetIconContainer.setStyle("-fx-background-color: #ffe6f2; -fx-background-radius: 24; -fx-min-width: 80; -fx-min-height: 80;");
        } else if (destinationLower.contains("new york") || destinationLower.contains("usa")) {
            lblBudgetIcon.setText("🗽");
            budgetIconContainer.setStyle("-fx-background-color: #e6f0ff; -fx-background-radius: 24; -fx-min-width: 80; -fx-min-height: 80;");
        } else {
            lblBudgetIcon.setText("💰");
            budgetIconContainer.setStyle("-fx-background-color: #fff7ed; -fx-background-radius: 24; -fx-min-width: 80; -fx-min-height: 80;");
        }
    }

    private void setupComboBoxes() {
        cmbCategorieDepense.setItems(FXCollections.observableArrayList(
                "Hébergement", "Transport", "Restauration", "Activités", "Shopping", "Autre"
        ));

        cmbDeviseBudget.setItems(FXCollections.observableArrayList("EUR", "USD", "GBP", "CHF", "CAD", "TND"));
        cmbDeviseDepense.setItems(FXCollections.observableArrayList("EUR", "USD", "GBP", "CHF", "TND"));

        cmbPaiementDepense.setItems(FXCollections.observableArrayList(
                "Carte bancaire", "Espèces", "Virement", "PayPal", "Autre"
        ));

        cmbStatutBudget.setItems(FXCollections.observableArrayList("ACTIF", "INACTIF"));

        // Le filtre catégorie sera mis à jour dynamiquement
        cmbFiltreCategorie.setItems(FXCollections.observableArrayList("Toutes les catégories"));

        ObservableList<String> voyageNames = FXCollections.observableArrayList();
        voyageNames.add("Sans voyage");

        if (voyagesInfoMap != null) {
            for (Map.Entry<Integer, VoyageInfo> entry : voyagesInfoMap.entrySet()) {
                String displayName = entry.getValue().getTitre();
                List<Budget> budgetsDuVoyage = budgetsParVoyage.getOrDefault(entry.getKey(), new ArrayList<>());
                if (!budgetsDuVoyage.isEmpty()) {
                    displayName += " (" + budgetsDuVoyage.size() + " budget" +
                            (budgetsDuVoyage.size() > 1 ? "s" : "") + ")";
                }
                voyageNames.add(displayName);
            }
        }

        cmbVoyageBudget.setItems(voyageNames);
        cmbVoyageBudget.getSelectionModel().selectFirst();
    }

    private void setupTableColumns() {
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
                    setText(item.toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                    setStyle("-fx-alignment: CENTER;");
                }
            }
        });

        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnEdit = new Button("✏");
            private final Button btnDelete = new Button("🗑");
            private final HBox pane = new HBox(5, btnEdit, btnDelete);

            {
                btnEdit.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #475569; -fx-cursor: hand; -fx-font-size: 12; -fx-min-width: 30; -fx-background-radius: 6;");
                btnDelete.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #ef4444; -fx-cursor: hand; -fx-font-size: 12; -fx-min-width: 30; -fx-background-radius: 6;");

                btnEdit.setOnAction(event -> {
                    Depense depense = getTableView().getItems().get(getIndex());
                    handleEditDepense(depense);
                });

                btnDelete.setOnAction(event -> {
                    Depense depense = getTableView().getItems().get(getIndex());
                    handleDeleteDepense(depense);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void loadDefaultBudget() {
        try {
            List<Budget> budgets = budgetCRUD.getBudgetsByUserId(currentUserId);
            if (!budgets.isEmpty()) {
                currentBudget = budgets.get(0);
                loadBudgetData();
            } else {
                clearBudgetDisplay();
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les budgets: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadBudgetData() {
        if (currentBudget != null) {
            try {
                lblBudgetNom.setText(currentBudget.getLibelleBudget());
                lblNomBudget.setText(currentBudget.getLibelleBudget());
                lblBudgetDevise.setText("Devise: " + currentBudget.getDeviseBudget());
                lblBudgetDescription.setText(currentBudget.getDescriptionBudget() != null ?
                        currentBudget.getDescriptionBudget() : "Aucune description");
                lblBudgetStatut.setText("● " + currentBudget.getStatutBudget());
                lblBudgetStatut.setStyle(currentBudget.getStatutBudget().equals("ACTIF") ?
                        "-fx-background-color: #10b981; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 4 12; -fx-font-size: 11; -fx-font-weight: 600;" :
                        "-fx-background-color: #94a3b8; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 4 12; -fx-font-size: 11; -fx-font-weight: 600;");

                if (currentBudget.getIdVoyage() != 0) {
                    VoyageInfo voyageInfo = voyagesInfoMap.get(currentBudget.getIdVoyage());
                    if (voyageInfo != null) {
                        lblBudgetDestination.setText(voyageInfo.getNomDestination());
                        lblBudgetDates.setText(voyageInfo.getDatesFormatted());
                        updateBudgetIcon(voyageInfo.getNomDestination());
                    } else {
                        lblBudgetDestination.setText("Destination inconnue");
                        lblBudgetDates.setText("Dates non définies");
                    }
                } else {
                    lblBudgetDestination.setText("Aucun voyage associé");
                    lblBudgetDates.setText("Dates non définies");
                    lblBudgetIcon.setText("💰");
                }

                double montantTotal = currentBudget.getMontantTotal();
                String devise = currentBudget.getDeviseBudget();
                lblMontantTotal.setText(String.format("%.2f %s", montantTotal, devise));

                loadDepenses();

            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les informations: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void loadDepenses() {
        if (currentBudget == null) return;

        try {
            List<Depense> depensesFromDB = depenseCRUD.getDepensesByBudgetId(currentBudget.getIdBudget());

            depenseList.clear();
            depenseList.addAll(depensesFromDB);

            filteredData = new FilteredList<>(depenseList, p -> true);
            sortedData = new SortedList<>(filteredData);

            tableDepenses.setItems(sortedData);

            updateStats();
            updateCategoryFilter();

            // Appliquer le tri par défaut
            btnTriDate.setSelected(true);
            updateSortButtonStyles(btnTriDate);
            applySort();

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les dépenses: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Met à jour la liste des catégories disponibles dans le filtre
     */
    private void updateCategoryFilter() {
        Set<String> categories = depenseList.stream()
                .map(Depense::getCategorieDepense)
                .collect(Collectors.toCollection(TreeSet::new));

        ObservableList<String> filterItems = FXCollections.observableArrayList();
        filterItems.add("Toutes les catégories");
        filterItems.addAll(categories);

        cmbFiltreCategorie.setItems(filterItems);
        cmbFiltreCategorie.getSelectionModel().selectFirst();
    }

    private void updateStats() {
        double totalDepense = depenseList.stream().mapToDouble(Depense::getMontantDepense).sum();
        double montantTotal = currentBudget != null ? currentBudget.getMontantTotal() : 0;
        double restant = montantTotal - totalDepense;
        String devise = currentBudget != null ? currentBudget.getDeviseBudget() : "€";

        lblDepense.setText(String.format("%.2f %s", totalDepense, devise));
        lblRestant.setText(String.format("%.2f %s", restant, devise));

        if (montantTotal > 0) {
            double pourcentageDepense = (totalDepense / montantTotal) * 100;
            double pourcentageRestant = (restant / montantTotal) * 100;
            lblPourcentageDepense.setText(String.format("↗ %.1f%% du budget", pourcentageDepense));
            lblPourcentageRestant.setText(String.format("✓ %.1f%% disponible", pourcentageRestant));
        }

        lblNbDepenses.setText(depenseList.size() + " dépense" + (depenseList.size() > 1 ? "s" : ""));
    }

    /**
     * Charge tous les budgets et les affiche dans des cartes
     */
    private void loadAllBudgets() {
        try {
            List<Budget> budgets = budgetCRUD.getBudgetsByUserId(currentUserId);
            allBudgetsList.clear();
            allBudgetsList.addAll(budgets);

            lblNbTotalBudgets.setText(allBudgetsList.size() + " budget" + (allBudgetsList.size() > 1 ? "s" : ""));

            generateBudgetCards();

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les budgets: " + e.getMessage());
        }
    }

    /**
     * Génère les cartes pour chaque budget
     */
    private void generateBudgetCards() {
        budgetsCardsContainer.getChildren().clear();

        if (allBudgetsList.isEmpty()) {
            Label emptyLabel = new Label("Aucun budget trouvé");
            emptyLabel.setStyle("-fx-text-fill: #64748b; -fx-padding: 20;");
            budgetsCardsContainer.getChildren().add(emptyLabel);
            return;
        }

        List<Budget> sortedBudgets = allBudgetsList.stream()
                .sorted((b1, b2) -> {
                    if (b1.getIdVoyage() == 0 && b2.getIdVoyage() != 0) return 1;
                    if (b1.getIdVoyage() != 0 && b2.getIdVoyage() == 0) return -1;
                    return b1.getLibelleBudget().compareTo(b2.getLibelleBudget());
                })
                .collect(Collectors.toList());

        for (Budget budget : sortedBudgets) {
            VBox card = createBudgetCard(budget);
            budgetsCardsContainer.getChildren().add(card);
        }
    }

    /**
     * Crée une carte pour un budget
     */
    private VBox createBudgetCard(Budget budget) {
        VBox card = new VBox(12);
        card.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 16; -fx-padding: 16; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-border-radius: 16;");
        card.setPrefWidth(300);

        HBox header = new HBox(10);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        String iconText = "💰";
        String destinationText = "Aucun voyage";
        String datesText = "";

        if (budget.getIdVoyage() != 0) {
            VoyageInfo info = voyagesInfoMap.get(budget.getIdVoyage());
            if (info != null) {
                destinationText = info.getNomDestination();
                datesText = info.getDatesFormatted();

                String dest = info.getNomDestination().toLowerCase();
                if (dest.contains("paris")) iconText = "🗼";
                else if (dest.contains("londres")) iconText = "🇬🇧";
                else if (dest.contains("tunis")) iconText = "🇹🇳";
                else if (dest.contains("rome")) iconText = "🍕";
                else if (dest.contains("tokyo")) iconText = "🗻";
                else if (dest.contains("new york")) iconText = "🗽";
            }
        }

        Label iconLabel = new Label(iconText);
        iconLabel.setStyle("-fx-font-size: 24;");

        VBox titleBox = new VBox(4);
        Label nomLabel = new Label(budget.getLibelleBudget());
        nomLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16; -fx-text-fill: #0f172a;");

        Label destLabel = new Label(destinationText);
        destLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11;");

        titleBox.getChildren().addAll(nomLabel, destLabel);

        if (!datesText.isEmpty()) {
            Label datesLabel = new Label(datesText);
            datesLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 10;");
            titleBox.getChildren().add(datesLabel);
        }

        Region region = new Region();
        HBox.setHgrow(region, javafx.scene.layout.Priority.ALWAYS);

        Label statutLabel = new Label(budget.getStatutBudget());
        statutLabel.setStyle(budget.getStatutBudget().equals("ACTIF") ?
                "-fx-background-color: #10b981; -fx-text-fill: white; -fx-background-radius: 12; -fx-padding: 2 8; -fx-font-size: 10;" :
                "-fx-background-color: #94a3b8; -fx-text-fill: white; -fx-background-radius: 12; -fx-padding: 2 8; -fx-font-size: 10;");

        header.getChildren().addAll(iconLabel, titleBox, region, statutLabel);

        HBox montantBox = new HBox(8);
        montantBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label montantLabel = new Label(String.format("%.2f", budget.getMontantTotal()));
        montantLabel.setStyle("-fx-font-size: 20; -fx-font-weight: bold; -fx-text-fill: #ff8c42;");

        Label deviseLabel = new Label(budget.getDeviseBudget());
        deviseLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 14; -fx-padding: 4 0 0 0;");

        montantBox.getChildren().addAll(montantLabel, deviseLabel);

        HBox actionsBox = new HBox(8);
        actionsBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

        Button btnChoisir = new Button("Choisir");
        btnChoisir.setStyle("-fx-background-color: #ff8c42; -fx-text-fill: white; -fx-background-radius: 12; -fx-padding: 6 12; -fx-font-size: 11; -fx-cursor: hand; -fx-border-width: 0;");
        btnChoisir.setOnAction(e -> selectBudget(budget));

        Button btnModifier = new Button("✏");
        btnModifier.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #475569; -fx-background-radius: 12; -fx-padding: 6 10; -fx-font-size: 11; -fx-cursor: hand; -fx-border-width: 0;");
        btnModifier.setOnAction(e -> editBudgetFromCard(budget));

        Button btnSupprimer = new Button("🗑");
        btnSupprimer.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #ef4444; -fx-background-radius: 12; -fx-padding: 6 10; -fx-font-size: 11; -fx-cursor: hand; -fx-border-width: 0;");
        btnSupprimer.setOnAction(e -> deleteBudgetFromCard(budget));

        actionsBox.getChildren().addAll(btnChoisir, btnModifier, btnSupprimer);

        card.getChildren().addAll(header, montantBox, actionsBox);

        card.setOnMouseEntered(e ->
                card.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 16; -fx-padding: 16; -fx-border-color: #ff8c42; -fx-border-width: 2; -fx-border-radius: 16; -fx-effect: dropshadow(three-pass-box, rgba(255,140,66,0.2), 10, 0, 0, 2);"));
        card.setOnMouseExited(e ->
                card.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 16; -fx-padding: 16; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-border-radius: 16;"));

        return card;
    }

    private void selectBudget(Budget budget) {
        currentBudget = budget;
        loadBudgetData();

        isBudgetsSectionVisible = false;
        contentTousBudgets.setVisible(false);
        contentTousBudgets.setManaged(false);
        lblToggleIcon.setText("▶");

        showAlert(Alert.AlertType.INFORMATION, "Budget sélectionné",
                "Le budget \"" + budget.getLibelleBudget() + "\" est maintenant actif.");
    }

    private void editBudgetFromCard(Budget budget) {
        currentBudget = budget;
        handleModifierBudget(null);
    }

    private void deleteBudgetFromCard(Budget budget) {
        if (confirmDelete("le budget \"" + budget.getLibelleBudget() + "\"")) {
            try {
                budgetCRUD.supprimer(budget.getIdBudget());

                if (budget.getIdVoyage() != 0) {
                    List<Budget> budgetsVoyage = budgetsParVoyage.get(budget.getIdVoyage());
                    if (budgetsVoyage != null) {
                        budgetsVoyage.remove(budget);
                        if (budgetsVoyage.isEmpty()) {
                            budgetsParVoyage.remove(budget.getIdVoyage());
                        }
                    }
                }

                loadAllBudgets();

                if (currentBudget != null && currentBudget.getIdBudget() == budget.getIdBudget()) {
                    if (!allBudgetsList.isEmpty()) {
                        currentBudget = allBudgetsList.get(0);
                        loadBudgetData();
                    } else {
                        currentBudget = null;
                        clearBudgetDisplay();
                    }
                }

                showAlert(Alert.AlertType.INFORMATION, "Succès", "Budget supprimé avec succès !");
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
            }
        }
    }

    /**
     * Application des filtres
     */
    private void applyFilters() {
        if (filteredData == null || currentBudget == null) return;

        filteredData.setPredicate(depense -> {
            String selectedCategorie = cmbFiltreCategorie.getValue();
            if (selectedCategorie != null && !selectedCategorie.equals("Toutes les catégories")) {
                if (!depense.getCategorieDepense().equals(selectedCategorie)) {
                    return false;
                }
            }

            if (dpDateDebut.getValue() != null) {
                if (depense.getDateCreation().toLocalDate().isBefore(dpDateDebut.getValue())) {
                    return false;
                }
            }

            if (dpDateFin.getValue() != null) {
                if (depense.getDateCreation().toLocalDate().isAfter(dpDateFin.getValue())) {
                    return false;
                }
            }

            return true;
        });

        updateStats();

        int resultCount = filteredData.size();
        String message = resultCount + " dépense" + (resultCount > 1 ? "s" : "") + " trouvée" + (resultCount > 1 ? "s" : "");
        lblNbDepenses.setText(message);
    }

    private void clearFilters() {
        cmbFiltreCategorie.getSelectionModel().selectFirst();
        dpDateDebut.setValue(null);
        dpDateFin.setValue(null);
        if (filteredData != null) {
            filteredData.setPredicate(p -> true);
        }
        updateStats();
        lblNbDepenses.setText(depenseList.size() + " dépense" + (depenseList.size() > 1 ? "s" : ""));
    }

    private void clearBudgetDisplay() {
        lblBudgetNom.setText("Aucun budget");
        lblNomBudget.setText("Gestion des budgets");
        lblMontantTotal.setText("0 €");
        lblDepense.setText("0 €");
        lblRestant.setText("0 €");
        lblPourcentageDepense.setText("↗ 0% du budget");
        lblPourcentageRestant.setText("✓ 0% disponible");
        lblNbDepenses.setText("0 dépense");
        lblBudgetDevise.setText("Devise: Non définie");
        lblBudgetDescription.setText("Aucune description");
        lblBudgetStatut.setText("● En attente");
        lblBudgetDestination.setText("Destination non définie");
        lblBudgetDates.setText("Dates non définies");
        lblBudgetIcon.setText("💰");
        depenseList.clear();
        if (tableDepenses != null) {
            tableDepenses.setItems(depenseList);
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private boolean confirmDelete(String item) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer " + item + " ?");
        alert.setContentText("Cette action est irréversible.");
        return alert.showAndWait().filter(response -> response == ButtonType.OK).isPresent();
    }

    // ===== HANDLERS =====

    @FXML
    void handleNouveauBudget(ActionEvent event) {
        isEditingBudget = false;
        txtNomBudget.clear();
        txtMontantBudget.clear();
        txtDescriptionBudget.clear();
        cmbDeviseBudget.setValue("EUR");
        cmbStatutBudget.setValue("ACTIF");

        // Sélectionner "Sans voyage" sans déclencher le listener
        isProgrammaticChange = true;
        cmbVoyageBudget.getSelectionModel().selectFirst();

        lblModalBudgetTitle.setText("Nouveau Budget");
        lblModalBudgetSubtitle.setText("Créez un nouveau budget pour votre voyage");
        modalBudget.setVisible(true);
    }

    @FXML
    void handleModifierBudget(ActionEvent event) {
        if (currentBudget == null) {
            showAlert(Alert.AlertType.WARNING, "Aucun budget", "Veuillez d'abord créer ou sélectionner un budget.");
            return;
        }

        isEditingBudget = true;
        txtNomBudget.setText(currentBudget.getLibelleBudget());
        txtMontantBudget.setText(String.valueOf(currentBudget.getMontantTotal()));
        txtDescriptionBudget.setText(currentBudget.getDescriptionBudget() != null ?
                currentBudget.getDescriptionBudget() : "");
        cmbDeviseBudget.setValue(currentBudget.getDeviseBudget());
        cmbStatutBudget.setValue(currentBudget.getStatutBudget());

        try {
            if (currentBudget.getIdVoyage() == 0) {
                // Sélectionner "Sans voyage"
                selectVoyageItem("Sans voyage");
            } else {
                VoyageInfo info = voyagesInfoMap.get(currentBudget.getIdVoyage());
                if (info != null) {
                    String voyageName = info.getTitre();
                    boolean selected = false;

                    // Chercher l'élément exact ou commençant par le nom
                    for (String item : cmbVoyageBudget.getItems()) {
                        if (item.equals(voyageName) || item.startsWith(voyageName)) {
                            selectVoyageItem(item);
                            selected = true;
                            break;
                        }
                    }

                    if (!selected) {
                        // Si pas trouvé, sélectionner "Sans voyage"
                        selectVoyageItem("Sans voyage");
                    }
                } else {
                    selectVoyageItem("Sans voyage");
                }
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger le voyage: " + e.getMessage());
            selectVoyageItem("Sans voyage");
        }

        lblModalBudgetTitle.setText("Modifier Budget");
        lblModalBudgetSubtitle.setText("Modifiez les informations de votre budget");
        modalBudget.setVisible(true);
    }

    /**
     * Méthode utilitaire pour sélectionner un item dans la ComboBox sans ajouter de nouvel élément
     */
    private void selectVoyageItem(String itemName) {
        for (String item : cmbVoyageBudget.getItems()) {
            if (item.equals(itemName)) {
                isProgrammaticChange = true;
                cmbVoyageBudget.setValue(item);
                return;
            }
        }
        // Si pas trouvé, sélectionner le premier élément ("Sans voyage")
        isProgrammaticChange = true;
        cmbVoyageBudget.getSelectionModel().selectFirst();
    }

    @FXML
    void handleSaveBudget(ActionEvent event) {
        try {
            String nom = txtNomBudget.getText().trim();
            if (nom.isEmpty()) throw new IllegalArgumentException("Le nom du budget est requis.");

            double montant;
            try {
                montant = Double.parseDouble(txtMontantBudget.getText().trim());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Montant invalide.");
            }

            String devise = cmbDeviseBudget.getValue();
            if (devise == null) throw new IllegalArgumentException("La devise est requise.");

            String statut = cmbStatutBudget.getValue();
            if (statut == null) throw new IllegalArgumentException("Le statut est requis.");

            String description = txtDescriptionBudget.getText();

            String voyageSelection = cmbVoyageBudget.getValue();
            int idVoyage = 0;

            if (voyageSelection != null && !voyageSelection.equals("Sans voyage")) {
                String cleanName = voyageSelection.replaceAll(" \\(\\d+ budget.*\\)", "");
                idVoyage = voyagesReverseMap.get(cleanName);
            }

            // VÉRIFICATION D'UNICITÉ
            if (isEditingBudget && currentBudget != null) {
                // Mode modification : vérifier si un AUTRE budget avec le même libellé existe
                if (budgetCRUD.budgetExisteExclusion(nom, idVoyage, currentUserId, currentBudget.getIdBudget())) {
                    showAlert(Alert.AlertType.ERROR, "Erreur",
                            "Un budget avec le libellé \"" + nom + "\" existe déjà pour ce voyage.");
                    return;
                }
            } else {
                // Mode création : vérifier si un budget avec ce libellé existe déjà
                if (budgetCRUD.budgetExiste(nom, idVoyage, currentUserId)) {
                    showAlert(Alert.AlertType.ERROR, "Erreur",
                            "Un budget avec le libellé \"" + nom + "\" existe déjà pour ce voyage.");
                    return;
                }
            }

            if (isEditingBudget && currentBudget != null) {
                int ancienVoyage = currentBudget.getIdVoyage();

                currentBudget.setLibelleBudget(nom);
                currentBudget.setMontantTotal(montant);
                currentBudget.setDeviseBudget(devise);
                currentBudget.setStatutBudget(statut);
                currentBudget.setDescriptionBudget(description);
                currentBudget.setIdVoyage(idVoyage);

                budgetCRUD.modifier(currentBudget);

                // Mettre à jour les associations
                if (ancienVoyage != 0) {
                    List<Budget> ancienneListe = budgetsParVoyage.get(ancienVoyage);
                    if (ancienneListe != null) {
                        ancienneListe.remove(currentBudget);
                        if (ancienneListe.isEmpty()) {
                            budgetsParVoyage.remove(ancienVoyage);
                        }
                    }
                }

                if (idVoyage != 0) {
                    if (!budgetsParVoyage.containsKey(idVoyage)) {
                        budgetsParVoyage.put(idVoyage, new ArrayList<>());
                    }
                    budgetsParVoyage.get(idVoyage).add(currentBudget);
                }

                showAlert(Alert.AlertType.INFORMATION, "Succès", "Budget modifié avec succès !");
            } else {
                Budget newBudget = new Budget(nom, montant, devise, statut, description, currentUserId, idVoyage);
                budgetCRUD.ajouter(newBudget);
                currentBudget = newBudget;

                if (idVoyage != 0) {
                    if (!budgetsParVoyage.containsKey(idVoyage)) {
                        budgetsParVoyage.put(idVoyage, new ArrayList<>());
                    }
                    budgetsParVoyage.get(idVoyage).add(newBudget);
                }

                showAlert(Alert.AlertType.INFORMATION, "Succès", "Budget créé avec succès !");
            }

            modalBudget.setVisible(false);
            loadBudgetData();
            loadAllBudgets();
            setupComboBoxes();

        } catch (IllegalArgumentException | SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    @FXML
    void handleSupprimerBudget(ActionEvent event) {
        if (currentBudget == null) return;

        if (confirmDelete("le budget \"" + currentBudget.getLibelleBudget() + "\"")) {
            try {
                int idVoyageAssocie = currentBudget.getIdVoyage();

                budgetCRUD.supprimer(currentBudget.getIdBudget());

                if (idVoyageAssocie != 0) {
                    List<Budget> budgetsVoyage = budgetsParVoyage.get(idVoyageAssocie);
                    if (budgetsVoyage != null) {
                        budgetsVoyage.remove(currentBudget);
                        if (budgetsVoyage.isEmpty()) {
                            budgetsParVoyage.remove(idVoyageAssocie);
                        }
                    }
                }

                showAlert(Alert.AlertType.INFORMATION, "Succès", "Budget supprimé avec succès !");
                currentBudget = null;
                clearBudgetDisplay();
                loadAllBudgets();
                setupComboBoxes();

            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
            }
        }
    }

    @FXML
    void handleCloseModalBudget(ActionEvent event) {
        modalBudget.setVisible(false);
    }

    @FXML
    void handleAjouterDepense(ActionEvent event) {
        if (currentBudget == null) {
            showAlert(Alert.AlertType.WARNING, "Aucun budget", "Veuillez d'abord créer ou sélectionner un budget.");
            return;
        }

        isEditingDepense = false;
        editingDepense = null;
        txtLibelleDepense.clear();
        txtMontantDepense.clear();
        txtNotesDepense.clear();
        cmbCategorieDepense.setValue(null);
        cmbDeviseDepense.setValue("EUR");
        cmbPaiementDepense.setValue(null);
        dateDepense.setValue(LocalDate.now());
        lblModalTitle.setText("Nouvelle Dépense");
        modalDepense.setVisible(true);
    }

    @FXML
    void handleSaveDepense(ActionEvent event) {
        if (currentBudget == null) return;

        try {
            String libelle = txtLibelleDepense.getText().trim();
            if (libelle.isEmpty()) throw new IllegalArgumentException("Le libellé est requis.");

            double montant;
            try {
                montant = Double.parseDouble(txtMontantDepense.getText().trim());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Montant invalide.");
            }

            String categorie = cmbCategorieDepense.getValue();
            if (categorie == null) throw new IllegalArgumentException("La catégorie est requise.");

            String devise = cmbDeviseDepense.getValue();
            if (devise == null) throw new IllegalArgumentException("La devise est requise.");

            String paiement = cmbPaiementDepense.getValue();
            if (paiement == null) throw new IllegalArgumentException("Le mode de paiement est requis.");

            LocalDate date = dateDepense.getValue();
            if (date == null) throw new IllegalArgumentException("La date est requise.");

            String notes = txtNotesDepense.getText();

            if (isEditingDepense && editingDepense != null) {
                editingDepense.setLibelleDepense(libelle);
                editingDepense.setMontantDepense(montant);
                editingDepense.setCategorieDepense(categorie);
                editingDepense.setDeviseDepense(devise);
                editingDepense.setTypePaiement(paiement);
                editingDepense.setDateCreation(Date.valueOf(date));
                editingDepense.setDescriptionDepense(notes);

                depenseCRUD.modifier(editingDepense);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Dépense modifiée avec succès !");
            } else {
                Depense newDepense = new Depense(montant, libelle, categorie, notes, devise, paiement,
                        Date.valueOf(date), currentBudget.getIdBudget());
                depenseCRUD.ajouter(newDepense);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Dépense ajoutée avec succès !");
            }

            modalDepense.setVisible(false);
            loadDepenses();

        } catch (IllegalArgumentException | SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleEditDepense(Depense depense) {
        isEditingDepense = true;
        editingDepense = depense;

        txtLibelleDepense.setText(depense.getLibelleDepense());
        txtMontantDepense.setText(String.valueOf(depense.getMontantDepense()));
        txtNotesDepense.setText(depense.getDescriptionDepense() != null ? depense.getDescriptionDepense() : "");
        cmbCategorieDepense.setValue(depense.getCategorieDepense());
        cmbDeviseDepense.setValue(depense.getDeviseDepense());
        cmbPaiementDepense.setValue(depense.getTypePaiement());
        dateDepense.setValue(depense.getDateCreation().toLocalDate());

        lblModalTitle.setText("Modifier Dépense");
        modalDepense.setVisible(true);
    }

    private void handleDeleteDepense(Depense depense) {
        if (confirmDelete("la dépense \"" + depense.getLibelleDepense() + "\"")) {
            try {
                depenseCRUD.supprimer(depense.getIdDepense());
                depenseList.remove(depense);
                updateStats();
                updateCategoryFilter();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Dépense supprimée avec succès !");
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
            }
        }
    }

    @FXML
    void handleCloseModalDepense(ActionEvent event) {
        modalDepense.setVisible(false);
    }

    @FXML
    void handleAppliquerFiltres(ActionEvent event) {
        applyFilters();
    }

    @FXML
    void handleReinitialiserFiltres(ActionEvent event) {
        clearFilters();
    }

    @FXML
    void handleTriDate(ActionEvent event) {
        if (btnTriDate.isSelected()) {
            isDateAscending = !isDateAscending;
            applySort();
        }
    }

    @FXML
    void handleTriLibelle(ActionEvent event) {
        if (btnTriLibelle.isSelected()) {
            isLibelleAscending = !isLibelleAscending;
            applySort();
        }
    }

    @FXML
    void handleTriMontant(ActionEvent event) {
        if (btnTriMontant.isSelected()) {
            isMontantAscending = !isMontantAscending;
            applySort();
        }
    }

    @FXML
    void handleVoirTousBudgets(ActionEvent event) {
        isBudgetsSectionVisible = !isBudgetsSectionVisible;
        contentTousBudgets.setVisible(isBudgetsSectionVisible);
        contentTousBudgets.setManaged(isBudgetsSectionVisible);
        lblToggleIcon.setText(isBudgetsSectionVisible ? "▼" : "▶");

        if (isBudgetsSectionVisible) {
            loadAllBudgets();
        }
    }

    /**
     * Export CSV
     */
    @FXML
    void handleExportCSV(javafx.scene.input.MouseEvent event) {
        if (currentBudget == null) {
            showAlert(Alert.AlertType.WARNING, "Aucun budget", "Veuillez sélectionner un budget d'abord.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter en CSV");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichier CSV", "*.csv")
        );

        String defaultFileName = "Budget_" + currentBudget.getLibelleBudget().replace(" ", "_") +
                "_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        fileChooser.setInitialFileName(defaultFileName);

        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            exportToCSV(file);
        }
    }

    /**
     * Export PDF
     */
    @FXML
    void handleExportPDF(javafx.scene.input.MouseEvent event) {
        if (currentBudget == null) {
            showAlert(Alert.AlertType.WARNING, "Aucun budget", "Veuillez sélectionner un budget d'abord.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter en PDF");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichier PDF", "*.pdf")
        );

        String defaultFileName = "Budget_" + currentBudget.getLibelleBudget().replace(" ", "_") +
                "_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        fileChooser.setInitialFileName(defaultFileName);

        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            exportToPDF(file);
        }
    }

    /**
     * Export vers fichier CSV
     */
    private void exportToCSV(File file) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            // En-tête du fichier
            writer.println("=== HISTORIQUE DES DÉPENSES ===");
            writer.println("Budget : " + currentBudget.getLibelleBudget());
            writer.println("Montant total : " + df.format(currentBudget.getMontantTotal()) + " " + currentBudget.getDeviseBudget());

            double totalDepense = depenseList.stream().mapToDouble(Depense::getMontantDepense).sum();
            writer.println("Total dépensé : " + df.format(totalDepense) + " " + currentBudget.getDeviseBudget());
            writer.println("Date d'export : " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            writer.println();

            // En-têtes des colonnes
            writer.println("Libellé;Catégorie;Montant;Devise;Date;Paiement;Description");

            // Données
            for (Depense d : depenseList) {
                writer.printf("%s;%s;%.2f;%s;%s;%s;%s%n",
                        d.getLibelleDepense(),
                        d.getCategorieDepense(),
                        d.getMontantDepense(),
                        d.getDeviseDepense(),
                        d.getDateCreation().toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                        d.getTypePaiement(),
                        d.getDescriptionDepense() != null ? d.getDescriptionDepense().replace(";", ",") : ""
                );
            }

            // Résumé par catégorie
            writer.println();
            writer.println("=== RÉSUMÉ PAR CATÉGORIE ===");
            Map<String, Double> totalsByCategory = depenseList.stream()
                    .collect(Collectors.groupingBy(
                            Depense::getCategorieDepense,
                            Collectors.summingDouble(Depense::getMontantDepense)
                    ));

            for (Map.Entry<String, Double> entry : totalsByCategory.entrySet()) {
                writer.printf("%s;%.2f %s%n", entry.getKey(), entry.getValue(), currentBudget.getDeviseBudget());
            }

            // Statistiques mensuelles
            writer.println();
            writer.println("=== STATISTIQUES MENSUELLES ===");
            Map<String, Double> monthlyTotals = depenseList.stream()
                    .collect(Collectors.groupingBy(
                            d -> d.getDateCreation().toLocalDate().format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                            Collectors.summingDouble(Depense::getMontantDepense)
                    ));

            for (Map.Entry<String, Double> entry : monthlyTotals.entrySet()) {
                writer.printf("%s;%.2f %s%n", entry.getKey(), entry.getValue(), currentBudget.getDeviseBudget());
            }

            showAlert(Alert.AlertType.INFORMATION, "Succès",
                    "Export CSV réussi !\nFichier : " + file.getName() +
                            "\n" + depenseList.size() + " dépenses exportées.");

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'export CSV : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Export vers fichier PDF
     */
    private void exportToPDF(File file) {
        try {
            // Initialiser le PDF writer
            PdfWriter writer = new PdfWriter(file.getAbsolutePath());
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // Polices
            PdfFont boldFont = PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA_BOLD);
            PdfFont normalFont = PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA);

            // Titre
            Paragraph title = new Paragraph("RAPPORT DE BUDGET")
                    .setFont(boldFont)
                    .setFontSize(20)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(title);

            // Informations générales
            document.add(new Paragraph("Budget : " + currentBudget.getLibelleBudget())
                    .setFont(boldFont).setFontSize(14));
            document.add(new Paragraph("Montant total : " + df.format(currentBudget.getMontantTotal()) + " " + currentBudget.getDeviseBudget())
                    .setFont(normalFont).setFontSize(12));

            double totalDepense = depenseList.stream().mapToDouble(Depense::getMontantDepense).sum();
            document.add(new Paragraph("Total dépensé : " + df.format(totalDepense) + " " + currentBudget.getDeviseBudget())
                    .setFont(normalFont).setFontSize(12));

            double restant = currentBudget.getMontantTotal() - totalDepense;
            document.add(new Paragraph("Reste : " + df.format(restant) + " " + currentBudget.getDeviseBudget())
                    .setFont(normalFont).setFontSize(12));

            document.add(new Paragraph("Date d'export : " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                    .setFont(normalFont).setFontSize(10)
                    .setMarginBottom(20));

            // Tableau des dépenses
            if (!depenseList.isEmpty()) {
                document.add(new Paragraph("DÉTAIL DES DÉPENSES")
                        .setFont(boldFont).setFontSize(14)
                        .setMarginBottom(10));

                float[] columnWidths = {2f, 1.5f, 1f, 0.8f, 1.2f, 1.5f, 3f};
                Table table = new Table(UnitValue.createPercentArray(columnWidths));
                table.setWidth(UnitValue.createPercentValue(100));

                // En-têtes
                String[] headers = {"Libellé", "Catégorie", "Montant", "Devise", "Date", "Paiement", "Description"};
                for (String header : headers) {
                    table.addHeaderCell(new Cell().add(new Paragraph(header).setFont(boldFont).setFontSize(10))
                            .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                            .setTextAlignment(TextAlignment.CENTER));
                }

                // Données
                for (Depense d : depenseList) {
                    table.addCell(new Cell().add(new Paragraph(d.getLibelleDepense()).setFont(normalFont).setFontSize(9)));
                    table.addCell(new Cell().add(new Paragraph(d.getCategorieDepense()).setFont(normalFont).setFontSize(9)));
                    table.addCell(new Cell().add(new Paragraph(df.format(d.getMontantDepense())).setFont(normalFont).setFontSize(9))
                            .setTextAlignment(TextAlignment.RIGHT));
                    table.addCell(new Cell().add(new Paragraph(d.getDeviseDepense()).setFont(normalFont).setFontSize(9))
                            .setTextAlignment(TextAlignment.CENTER));
                    table.addCell(new Cell().add(new Paragraph(d.getDateCreation().toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                                    .setFont(normalFont).setFontSize(9))
                            .setTextAlignment(TextAlignment.CENTER));
                    table.addCell(new Cell().add(new Paragraph(d.getTypePaiement()).setFont(normalFont).setFontSize(9)));
                    table.addCell(new Cell().add(new Paragraph(d.getDescriptionDepense() != null ? d.getDescriptionDepense() : "")
                            .setFont(normalFont).setFontSize(9)));
                }

                document.add(table);
            }

            // Résumé par catégorie
            document.add(new Paragraph("\nRÉSUMÉ PAR CATÉGORIE")
                    .setFont(boldFont).setFontSize(14)
                    .setMarginTop(20).setMarginBottom(10));

            Map<String, Double> totalsByCategory = depenseList.stream()
                    .collect(Collectors.groupingBy(
                            Depense::getCategorieDepense,
                            Collectors.summingDouble(Depense::getMontantDepense)
                    ));

            for (Map.Entry<String, Double> entry : totalsByCategory.entrySet()) {
                document.add(new Paragraph(entry.getKey() + " : " + df.format(entry.getValue()) + " " + currentBudget.getDeviseBudget())
                        .setFont(normalFont).setFontSize(11));
            }

            document.close();

            showAlert(Alert.AlertType.INFORMATION, "Succès",
                    "Export PDF réussi !\nFichier : " + file.getName() +
                            "\n" + depenseList.size() + " dépenses exportées.");

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'export PDF : " + e.getMessage());
            e.printStackTrace();
        }
    }
}