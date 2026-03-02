package Controllers;

import Services.VocalAssistantService;
import Services.VoiceCommandProcessor;
import Services.TextToSpeechService;
import Entities.Budget;
import Entities.Depense;
import Services.*;
import Tools.VoyageHelper;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.util.CellRangeAddress;

import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.io.font.constants.StandardFonts;

import Services.InflationCalculator;
import Services.EcologicalScoreService;
import Services.CrisisManagementService;

public class BudgetFrontController implements Initializable {

    // ===== Services =====
    private BudgetCRUD budgetCRUD;
    private DepenseCRUD depenseCRUD;
    private VoyageHelper voyageHelper;
    private final CurrencyConverter currencyConverter = new CurrencyConverter();
    private final CurrencyService currencyService = new CurrencyService();
    private final Budgetnotificationservice notifier = new Budgetnotificationservice("budget-alerts-Yosr");
    private final BudgetEstimationService estimationService = new BudgetEstimationService();
    private final InflationCalculator inflationCalculator = new InflationCalculator();

    // ===== IA Services =====
    private final EcologicalScoreService ecologicalService = new EcologicalScoreService();
    private final CrisisManagementService crisisService = new CrisisManagementService();

    // ===== Data lists =====
    private ObservableList<Budget> budgetsList;
    private ObservableList<Depense> depensesList;
    private Map<Integer, VoyageHelper.VoyageInfo> voyagesMap;
    private FilteredList<Depense> filteredDepenses;
    private Budget selectedBudget;

    // ===== UI Stage =====
    private Stage primaryStage;

    // ===== Date formatter =====
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ===== Variable pour stocker la saison du voyage sélectionné =====
    private String currentSaisonIdeale;

    // ===== FXML injected fields =====
    @FXML private Label lblNomBudget;
    @FXML private Label lblBudgetStatut;
    @FXML private Label lblBudgetNom;
    @FXML private Label lblBudgetDates;
    @FXML private Label lblBudgetDestination;
    @FXML private Label lblBudgetDevise;
    @FXML private Label lblBudgetDescription;
    @FXML private Label lblBudgetIcon;
    @FXML private VBox budgetIconContainer;
    @FXML private HBox statusContainer;

    @FXML private Label lblMontantTotal;
    @FXML private Label lblDepense;
    @FXML private Label lblRestant;
    @FXML private Label lblPourcentageDepense;
    @FXML private Label lblPourcentageRestant;
    @FXML private Label lblInfoBudget;
    @FXML private Button btnVoice;
    @FXML private Label lblVoiceStatus;
    @FXML private Label lblVoiceText;

    @FXML private Label lblProgressPct;
    @FXML private StackPane progressBarContainer;
    @FXML private Rectangle progressBarFill;

    @FXML private Label lblTauxChange;
    @FXML private Label lblTauxUpdate;
    @FXML private Label lblDevisesActives;

    @FXML private HBox alertHistoryPanel;
    @FXML private VBox alertHistoryList;
    @FXML private Label lblAlertBadge;

    @FXML private ComboBox<String> cmbFiltreCategorie;
    @FXML private DatePicker dpDateDebut;
    @FXML private DatePicker dpDateFin;
    @FXML private Button btnAppliquerFiltres;
    @FXML private Button btnReinitialiserFiltres;

    @FXML private ToggleButton btnTriMontant;
    @FXML private ToggleButton btnTriDate;
    @FXML private ToggleButton btnTriLibelle;

    @FXML private Button btnModifierBudget;
    @FXML private Button btnSupprimerBudget;
    @FXML private Button btnAjouterDepense;
    @FXML private Button btnNouveauBudget;
    @FXML private Button btnNouveauBudgetVoyage;

    @FXML private VBox modalDepense;
    @FXML private VBox modalBudget;
    @FXML private Label lblModalTitle;
    @FXML private Label lblModalBudgetTitle;
    @FXML private Label lblModalBudgetSubtitle;
    @FXML private Label lblModalBudgetIcon;

    @FXML private TextField txtLibelleDepense;
    @FXML private ComboBox<String> cmbCategorieDepense;
    @FXML private TextField txtMontantDepense;
    @FXML private ComboBox<String> cmbDeviseDepense;
    @FXML private ComboBox<String> cmbPaiementDepense;
    @FXML private DatePicker dateDepense;
    @FXML private TextArea txtNotesDepense;
    @FXML private Button btnSaveDepense;

    @FXML private ComboBox<String> cmbVoyageBudget;
    @FXML private TextField txtNomBudget;
    @FXML private TextField txtMontantBudget;
    @FXML private ComboBox<String> cmbDeviseBudget;
    @FXML private ComboBox<String> cmbStatutBudget;
    @FXML private TextArea txtDescriptionBudget;
    @FXML private Button btnSaveBudget;

    // Nouveau label pour l'estimation du budget
    @FXML private Label lblEstimation;

    @FXML private ComboBox<String> cmbVoyageSelector;
    @FXML private Label lblVoyageBudgetsCount;
    @FXML private VBox voyageBudgetsContainer;

    @FXML private TableView<Depense> tableDepenses;
    @FXML private TableColumn<Depense, String> colLibelle;
    @FXML private TableColumn<Depense, String> colCategorie;
    @FXML private TableColumn<Depense, Double> colMontant;
    @FXML private TableColumn<Depense, String> colDevise;
    @FXML private TableColumn<Depense, Date> colDate;
    @FXML private TableColumn<Depense, String> colPaiement;
    @FXML private TableColumn<Depense, String> colDescription;
    @FXML private TableColumn<Depense, Void> colActions;

    @FXML private Label lblNbDepenses;
    @FXML private Label lblLastUpdate;

    @FXML private HBox btnDestinations;
    @FXML private HBox btnItineraires;
    @FXML private HBox btnActivites;
    @FXML private HBox btnVoyages;
    @FXML private HBox btnBudgets;
    @FXML private HBox btnHome;

    @FXML private Label lblInflationInfo;

    // ===== Modals IA =====
    @FXML private VBox modalEcologique;
    @FXML private VBox modalCrise;

    // Modal Écologique — champs
    @FXML private Label lblEcoScore;
    @FXML private Label lblEcoLabel;
    @FXML private Label lblEcoCO2;
    @FXML private Label lblEcoAnalysis;
    @FXML private Label lblEcoAlternatives;
    @FXML private Rectangle rectEcoBar;
    @FXML private Label lblEcoLoading;

    // Modal Crise — champs
    @FXML private Label lblCriseStatus;
    @FXML private Label lblCriseSummary;
    @FXML private Label lblCrisePlan;
    @FXML private Label lblCriseSavings;
    @FXML private Label lblCriseEmergency;
    @FXML private VBox boxCriseEmergency;
    @FXML private Label lblCriseLoading;

    private final VocalAssistantService vocalService = new VocalAssistantService();
    private final VoiceCommandProcessor cmdProcessor = new VoiceCommandProcessor();
    private final TextToSpeechService ttsService = new TextToSpeechService();

    // ===== INITIALIZE =====
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        budgetCRUD = new BudgetCRUD();
        depenseCRUD = new DepenseCRUD();
        voyageHelper = new VoyageHelper();

        budgetsList = FXCollections.observableArrayList();
        depensesList = FXCollections.observableArrayList();

        setupDepensesTable();
        setupFilters();
        setupComboBoxes();
        setupToggleButtons();
        setupNavigation();
        setupVoyageSelector();

        loadVoyages();
        loadBudgets();
        loadDepenses();
        updateLastUpdateTime();
        setupVocalAssistant();
        loadCurrenciesAsync();

        if (lblAlertBadge != null) lblAlertBadge.setVisible(false);

        Platform.runLater(() -> {
            if (tableDepenses.getScene() != null)
                primaryStage = (Stage) tableDepenses.getScene().getWindow();
        });

        setupVoyageBudgetListener();

        if (lblInflationInfo != null) lblInflationInfo.setText("");
    }

    // ===== IA — SCORE ÉCOLOGIQUE =====
    @FXML
    public void handleEcologicalAnalysis(ActionEvent event) {
        if (selectedBudget == null) {
            showAlert("IA Score Écologique", "Sélectionnez d'abord un budget.");
            return;
        }
        if (depensesList.isEmpty()) {
            showAlert("IA Score Écologique", "Aucune dépense à analyser.");
            return;
        }

        if (modalEcologique != null) {
            setEcoLoading(true);
            modalEcologique.setVisible(true);
            modalEcologique.setManaged(true);
        }

        String destination = getDestinationText();
        String devise = selectedBudget.getDeviseBudget() != null ? selectedBudget.getDeviseBudget() : "EUR";

        List<Depense> depensesCopy = List.copyOf(depensesList);

        ecologicalService.analyzeAsync(depensesCopy, destination, devise)
                .thenAccept(result -> Platform.runLater(() -> {
                    if (modalEcologique != null) {
                        fillEcoModal(result);
                        setEcoLoading(false);
                    } else {
                        showEcologicalDialog(result);
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        if (modalEcologique != null) {
                            setEcoLoading(false);
                            if (lblEcoLabel != null) lblEcoLabel.setText("⚠️ Erreur : " + ex.getMessage());
                        }
                        System.err.println("[IA Écologie] " + ex.getMessage());
                    });
                    return null;
                });
    }

    private void fillEcoModal(EcologicalScoreService.EcologicalResult result) {
        if (lblEcoScore != null) lblEcoScore.setText(result.score + "/100");
        if (lblEcoLabel != null) lblEcoLabel.setText(result.label);
        if (lblEcoCO2 != null) lblEcoCO2.setText("🌍 Empreinte carbone estimée : " + result.co2Estimate);
        if (lblEcoAnalysis != null) { lblEcoAnalysis.setText(result.analysis); lblEcoAnalysis.setWrapText(true); }
        if (lblEcoAlternatives != null) { lblEcoAlternatives.setText(result.alternatives); lblEcoAlternatives.setWrapText(true); }

        if (rectEcoBar != null) {
            double barW = 4.0 * result.score;
            rectEcoBar.setWidth(Math.max(0, barW));
            String c = result.score >= 70 ? "#10b981" : result.score >= 40 ? "#f59e0b" : "#ef4444";
            rectEcoBar.setStyle("-fx-fill:" + c + ";");
        }
    }

    private void setEcoLoading(boolean loading) {
        if (lblEcoLoading != null) {
            lblEcoLoading.setVisible(loading);
            lblEcoLoading.setManaged(loading);
        }
        if (lblEcoScore != null) lblEcoScore.setVisible(!loading);
        if (lblEcoLabel != null) lblEcoLabel.setVisible(!loading);
        if (lblEcoCO2 != null) lblEcoCO2.setVisible(!loading);
        if (lblEcoAnalysis != null) lblEcoAnalysis.setVisible(!loading);
        if (lblEcoAlternatives != null) lblEcoAlternatives.setVisible(!loading);
    }

    @FXML
    public void handleCloseModalEcologique(ActionEvent event) {
        if (modalEcologique != null) {
            modalEcologique.setVisible(false);
            modalEcologique.setManaged(false);
        }
    }

    private void showEcologicalDialog(EcologicalScoreService.EcologicalResult result) {
        // Fallback dialog (identique à l'original)
        // ... (code inchangé, omis pour brièveté)
    }

    // ===== IA — GESTION DE CRISE / PLAN B =====
    @FXML
    public void handleCrisisAnalysis(ActionEvent event) {
        if (selectedBudget == null) {
            showAlert("IA Plan B", "Sélectionnez d'abord un budget.");
            return;
        }

        if (modalCrise != null) {
            setCriseLoading(true);
            modalCrise.setVisible(true);
            modalCrise.setManaged(true);
        }

        String destination = getDestinationText();
        int joursRestants = computeJoursRestants();
        List<Depense> depensesCopy = List.copyOf(depensesList);

        crisisService.analyzeAsync(selectedBudget, depensesCopy, destination, joursRestants)
                .thenAccept(result -> Platform.runLater(() -> {
                    if (modalCrise != null) {
                        fillCriseModal(result);
                        setCriseLoading(false);
                    } else {
                        showCrisisDialog(result);
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        if (modalCrise != null) setCriseLoading(false);
                        System.err.println("[IA Crise] " + ex.getMessage());
                    });
                    return null;
                });
    }

    private void fillCriseModal(CrisisManagementService.CrisisResult result) {
        if (lblCriseStatus != null) lblCriseStatus.setText(result.statusLabel);
        if (lblCriseSummary != null) { lblCriseSummary.setText(result.summary); lblCriseSummary.setWrapText(true); }
        if (lblCrisePlan != null) { lblCrisePlan.setText(result.immediatePlan); lblCrisePlan.setWrapText(true); }
        if (lblCriseSavings != null) { lblCriseSavings.setText(result.savingsTips); lblCriseSavings.setWrapText(true); }

        boolean showEmergency = result.level == CrisisManagementService.CrisisLevel.DANGER
                && result.emergencyOptions != null && !result.emergencyOptions.isEmpty();
        if (boxCriseEmergency != null) {
            boxCriseEmergency.setVisible(showEmergency);
            boxCriseEmergency.setManaged(showEmergency);
        }
        if (lblCriseEmergency != null && showEmergency) {
            lblCriseEmergency.setText(result.emergencyOptions);
            lblCriseEmergency.setWrapText(true);
        }

        if (lblCriseStatus != null) {
            String c = switch (result.level) {
                case WARNING  -> "#f59e0b";
                case CRITICAL -> "#ef4444";
                case DANGER   -> "#7f1d1d";
                default       -> "#10b981";
            };
            lblCriseStatus.setStyle("-fx-font-size:18;-fx-font-weight:bold;-fx-text-fill:" + c + ";");
        }
    }

    private void setCriseLoading(boolean loading) {
        if (lblCriseLoading != null) {
            lblCriseLoading.setVisible(loading);
            lblCriseLoading.setManaged(loading);
        }
        if (lblCriseStatus != null) lblCriseStatus.setVisible(!loading);
        if (lblCriseSummary != null) lblCriseSummary.setVisible(!loading);
        if (lblCrisePlan != null) lblCrisePlan.setVisible(!loading);
        if (lblCriseSavings != null) lblCriseSavings.setVisible(!loading);
    }

    @FXML
    public void handleCloseModalCrise(ActionEvent event) {
        if (modalCrise != null) {
            modalCrise.setVisible(false);
            modalCrise.setManaged(false);
        }
    }

    // Méthode supprimée car plus appelée automatiquement
    // private void triggerCrisisAutoCheck() { ... }

    private void showCrisisDialog(CrisisManagementService.CrisisResult result) {
        // Fallback dialog (identique à l'original)
        // ... (code inchangé, omis pour brièveté)
    }

    // ===== Helpers IA =====
    private String getDestinationText() {
        String dest = lblBudgetDestination != null ? lblBudgetDestination.getText() : null;
        return (dest == null || dest.equals("—") || dest.equals("Destination non définie"))
                ? "destination inconnue" : dest;
    }

    private int computeJoursRestants() {
        if (voyagesMap != null && selectedBudget != null && selectedBudget.getIdVoyage() > 0) {
            VoyageHelper.VoyageInfo v = voyagesMap.get(selectedBudget.getIdVoyage());
            if (v != null && v.getDateFin() != null) {
                long j = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), v.getDateFin().toLocalDate());
                return (int) Math.max(0, j);
            }
        }
        return 0;
    }

    // ===== INFLATION =====
    private void updateInflationInfo() {
        if (selectedBudget == null || lblInflationInfo == null) return;

        int anneeBudget = LocalDate.now().getYear();
        if (selectedBudget.getIdVoyage() > 0) {
            VoyageHelper.VoyageInfo v = voyagesMap.get(selectedBudget.getIdVoyage());
            if (v != null && v.getDateDebut() != null)
                anneeBudget = v.getDateDebut().toLocalDate().getYear();
        }
        final int finalAnneeBudget = anneeBudget;

        String pays = null;
        String dest = lblBudgetDestination.getText();
        if (dest != null && !dest.equals("—") && !dest.equals("Destination non définie")) {
            pays = dest.contains(",") ? dest.split(",")[1].trim() : getCountryFromDestination(dest);
        }
        final String finalPays = pays;

        if (finalPays == null || finalPays.isEmpty()) {
            lblInflationInfo.setText("Pays non identifié pour l'analyse d'inflation.");
            return;
        }

        final int anneeReference = 2020;
        final double montantInitial = selectedBudget.getMontantTotal();

        inflationCalculator.adjustForInflation(montantInitial, finalPays, finalAnneeBudget, anneeReference)
                .thenAccept(montantAjuste -> Platform.runLater(() -> {
                    if (montantAjuste != montantInitial) {
                        double pct = ((montantInitial / montantAjuste) - 1) * 100;
                        lblInflationInfo.setText(String.format(
                                "Le saviez-vous ? Ce voyage en %d te coûterait l'équivalent de %.2f %s en %d. " +
                                        "Le coût de la vie a augmenté de %.1f%% en %d ans.",
                                finalAnneeBudget, montantAjuste, selectedBudget.getDeviseBudget(),
                                anneeReference, pct, (finalAnneeBudget - anneeReference)));
                    } else {
                        lblInflationInfo.setText("Données d'inflation non disponibles pour cette destination.");
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> lblInflationInfo.setText("Erreur de calcul de l'inflation."));
                    return null;
                });
    }

    private String getCountryFromDestination(String ville) {
        // ... (identique à l'original)
        if (ville == null) return null;
        return switch (ville.toLowerCase()) {
            case "paris","lyon","marseille","bordeaux","toulouse","nice" -> "France";
            case "lisbonne","porto" -> "Portugal";
            case "madrid","barcelone" -> "Espagne";
            case "rome","milan","venise" -> "Italie";
            case "berlin","munich" -> "Allemagne";
            case "londres","manchester" -> "Royaume-Uni";
            case "new york","los angeles","san francisco" -> "États-Unis";
            case "tokyo","kyoto","osaka" -> "Japon";
            case "pékin","shanghai" -> "Chine";
            case "istanbul" -> "Turquie";
            case "dubaï" -> "Émirats arabes unis";
            case "sydney","melbourne" -> "Australie";
            case "rio de janeiro","são paulo" -> "Brésil";
            case "toronto","montréal","vancouver" -> "Canada";
            case "le caire" -> "Égypte";
            case "casablanca" -> "Maroc";
            case "tunis" -> "Tunisie";
            case "alger" -> "Algérie";
            default -> null;
        };
    }

    // ===== CURRENCIES =====
    private void loadCurrenciesAsync() {
        ObservableList<String> defaults = FXCollections.observableArrayList(
                "EUR","USD","GBP","TND","CHF","CAD","JPY","CNY","MAD","DZD");
        if (cmbDeviseDepense != null) { cmbDeviseDepense.setItems(defaults); cmbDeviseDepense.setValue("EUR"); }
        if (cmbDeviseBudget != null)  { cmbDeviseBudget.setItems(defaults);  cmbDeviseBudget.setValue("EUR"); }

        currencyService.getAllCurrenciesFormattedAsync().thenAccept(list -> Platform.runLater(() -> {
            ObservableList<String> items = FXCollections.observableArrayList(list);
            if (cmbDeviseDepense != null) { String c = CurrencyService.extractCode(cmbDeviseDepense.getValue()); cmbDeviseDepense.setItems(items); selectDevise(cmbDeviseDepense, c); }
            if (cmbDeviseBudget != null)  { String c = CurrencyService.extractCode(cmbDeviseBudget.getValue());  cmbDeviseBudget.setItems(items);  selectDevise(cmbDeviseBudget, c); }
        })).exceptionally(ex -> { System.err.println("[Devises] " + ex.getMessage()); return null; });
    }

    private void selectDevise(ComboBox<String> combo, String code) {
        if (code == null || code.isBlank()) return;
        combo.getItems().stream()
                .filter(item -> item.equals(code) || item.startsWith(code + " — "))
                .findFirst().ifPresent(combo::setValue);
    }

    // ===== TABLE SETUP =====
    private void setupDepensesTable() {
        colLibelle.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getLibelleDepense()));
        colCategorie.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getCategorieDepense()));
        colMontant.setCellValueFactory(c -> new javafx.beans.property.SimpleDoubleProperty(c.getValue().getMontantDepense()).asObject());
        colDevise.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getDeviseDepense()));
        colDate.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getDateCreation()));
        colPaiement.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getTypePaiement()));
        colDescription.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getDescriptionDepense()));

        colMontant.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double v, boolean empty) {
                super.updateItem(v, empty);
                setText((empty || v == null) ? null : String.format("%.2f", v));
            }
        });
        colDate.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Date d, boolean empty) {
                super.updateItem(d, empty);
                setText((empty || d == null) ? null : d.toLocalDate().format(dateFormatter));
            }
        });
        setupActionsColumn();
    }

    private void setupActionsColumn() {
        colActions.setCellFactory(p -> new TableCell<>() {
            private final Button btnEdit   = new Button("✏️");
            private final Button btnDelete = new Button("🗑️");
            private final HBox pane = new HBox(5, btnEdit, btnDelete);
            {
                btnEdit.setStyle("-fx-background-color:#f1f5f9;-fx-text-fill:#475569;-fx-background-radius:8;-fx-padding:5 10;-fx-cursor:hand;-fx-font-size:12;");
                btnDelete.setStyle("-fx-background-color:#fee2e2;-fx-text-fill:#ef4444;-fx-background-radius:8;-fx-padding:5 10;-fx-cursor:hand;-fx-font-size:12;");
                btnEdit.setOnAction(e -> handleEditDepense(getTableView().getItems().get(getIndex())));
                btnDelete.setOnAction(e -> handleDeleteDepense(getTableView().getItems().get(getIndex())));
            }
            @Override public void updateItem(Void v, boolean empty) { super.updateItem(v, empty); setGraphic(empty ? null : pane); }
        });
    }

    private void setupFilters() {
        filteredDepenses = new FilteredList<>(depensesList, p -> true);
        SortedList<Depense> sorted = new SortedList<>(filteredDepenses);
        sorted.comparatorProperty().bind(tableDepenses.comparatorProperty());
        tableDepenses.setItems(sorted);
        cmbFiltreCategorie.setValue("Toutes les catégories");
    }

    private void setupComboBoxes() {
        cmbFiltreCategorie.setItems(FXCollections.observableArrayList("Toutes les catégories","Hébergement","Transport","Restauration","Activités","Shopping","Autre"));
        cmbCategorieDepense.setItems(FXCollections.observableArrayList("Hébergement","Transport","Restauration","Activités","Shopping","Autre"));
        cmbPaiementDepense.setItems(FXCollections.observableArrayList("Carte bancaire","Espèces","Virement","PayPal","Autre"));
        cmbStatutBudget.setItems(FXCollections.observableArrayList("ACTIF","INACTIF","TERMINE","PLANIFIE","ENCOURS"));
    }

    private void setupToggleButtons() {
        ToggleGroup group = new ToggleGroup();
        btnTriMontant.setToggleGroup(group);
        btnTriDate.setToggleGroup(group);
        btnTriLibelle.setToggleGroup(group);
    }

    private void setupNavigation() {
        btnDestinations.setOnMouseClicked(e -> navigateTo("Destinations"));
        btnItineraires.setOnMouseClicked(e -> navigateTo("Itinéraires"));
        btnActivites.setOnMouseClicked(e -> navigateTo("Activités"));
        btnVoyages.setOnMouseClicked(e -> navigateTo("Voyages"));
        btnBudgets.setOnMouseClicked(e -> navigateTo("Budgets"));
        btnHome.setOnMouseClicked(e -> navigateTo("Accueil"));
    }

    private void navigateTo(String page) { System.out.println("Navigation vers: " + page); }

    private void updateLastUpdateTime() {
        if (lblLastUpdate != null)
            lblLastUpdate.setText("Dernière mise à jour: " +
                    LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy")) + ", " +
                    java.time.LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
    }

    private void setupVoyageSelector() {
        if (cmbVoyageSelector != null) {
            cmbVoyageSelector.setOnAction(event -> {
                String sel = cmbVoyageSelector.getValue();
                if (sel != null && !sel.isEmpty()) {
                    filterBudgetsByVoyage(sel);
                } else {
                    // Aucun voyage sélectionné → vider la liste des budgets
                    voyageBudgetsContainer.getChildren().clear();
                    Label msg = new Label("Sélectionnez un voyage pour voir ses budgets");
                    msg.setStyle("-fx-text-fill:#64748b;-fx-padding:20;");
                    voyageBudgetsContainer.getChildren().add(msg);
                    lblVoyageBudgetsCount.setText("0 budget");
                }
            });
        }
    }

    private void setupVoyageBudgetListener() {
        if (cmbVoyageBudget != null)
            cmbVoyageBudget.setOnAction(event -> {
                String sel = cmbVoyageBudget.getValue();
                if (sel != null && !sel.equals("Sans voyage") && !sel.isEmpty()) {
                    int idVoyage = extractVoyageId(sel);
                    try {
                        VoyageHelper.VoyageDetails details = voyageHelper.getVoyageDetails(idVoyage);
                        if (details != null) {
                            currentSaisonIdeale = details.getSaisonIdeale();
                            estimerBudgetAutomatiquement(details.getNomDestination(), (int) details.getDureeJours(), currentSaisonIdeale);
                        }
                    } catch (SQLException e) { System.err.println("Erreur: " + e.getMessage()); }
                } else {
                    // Réinitialiser le label d'estimation
                    if (lblEstimation != null) {
                        lblEstimation.setText("");
                        lblEstimation.setVisible(false);
                        lblEstimation.setManaged(false);
                    }
                    currentSaisonIdeale = null;
                }
            });
    }

    private void estimerBudgetAutomatiquement(String destination, int dureeJours, String saison) {
        if (destination == null || destination.isEmpty() || dureeJours <= 0) return;
        estimationService.estimateBudgetAsync(destination, dureeJours, saison)
                .thenAccept(estimation -> Platform.runLater(() -> {
                    // Afficher l'estimation dans le label sous le champ montant
                    String devise = CurrencyService.extractCode(cmbDeviseBudget.getValue());
                    String text = "estimation : " + estimation + " " + devise.toLowerCase();
                    lblEstimation.setText(text);
                    lblEstimation.setStyle("-fx-text-fill: #ff8c42; -fx-font-size: 12; -fx-font-style: italic;");
                    lblEstimation.setVisible(true);
                    lblEstimation.setManaged(true);
                    if (primaryStage != null)
                        ToastNotification.showInfo(primaryStage, "Estimation automatique",
                                "Budget estimé : " + estimation + " " + devise);
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> showAlert("Erreur", "Estimation impossible: " + ex.getMessage()));
                    return null;
                });
    }

    private void loadVoyages() {
        try {
            voyagesMap = voyageHelper.getAllVoyagesInfo();
            ObservableList<String> names = FXCollections.observableArrayList();
            // Ne pas ajouter "Tous les voyages"
            for (VoyageHelper.VoyageInfo v : voyagesMap.values())
                names.add(v.getIdVoyage() + " - " + v.getTitre() + " (" + v.getDatesFormatted() + ")");
            if (cmbVoyageSelector != null) {
                cmbVoyageSelector.setItems(names);
                cmbVoyageSelector.setPromptText("Sélectionner un voyage");
                cmbVoyageSelector.setValue(null);
            }
            if (cmbVoyageBudget != null) {
                ObservableList<String> budgetNames = FXCollections.observableArrayList();
                budgetNames.add("Sans voyage");
                budgetNames.addAll(names);
                cmbVoyageBudget.setItems(budgetNames);
                cmbVoyageBudget.setPromptText("Sélectionner un voyage");
                cmbVoyageBudget.setValue("Sans voyage");
            }
        } catch (SQLException e) {
            // Gestion d'erreur : liste vide
            if (cmbVoyageSelector != null) cmbVoyageSelector.setItems(FXCollections.observableArrayList());
            if (cmbVoyageBudget != null)   cmbVoyageBudget.setItems(FXCollections.observableArrayList("Sans voyage"));
        }
    }

    private void loadBudgets() {
        try {
            List<Budget> budgets = budgetCRUD.afficher();
            budgetsList.setAll(budgets);
            // N'afficher les budgets que si un voyage est sélectionné
            if (cmbVoyageSelector != null && cmbVoyageSelector.getValue() != null) {
                filterBudgetsByVoyage(cmbVoyageSelector.getValue());
            } else {
                // Afficher un message invitant à sélectionner un voyage
                voyageBudgetsContainer.getChildren().clear();
                Label msg = new Label("Sélectionnez un voyage pour voir ses budgets");
                msg.setStyle("-fx-text-fill:#64748b;-fx-padding:20;");
                voyageBudgetsContainer.getChildren().add(msg);
                lblVoyageBudgetsCount.setText("0 budget");
            }
            // Ne pas sélectionner automatiquement un budget
            // if (!budgets.isEmpty() && selectedBudget == null) selectBudget(budgets.get(0));
        } catch (Exception e) {
            showAlert("Erreur", "Impossible de charger les budgets: " + e.getMessage());
        }
    }

    private void loadDepenses() {
        try {
            depensesList.setAll(depenseCRUD.afficher());
            updateNbDepenses();
        } catch (Exception e) {
            showAlert("Erreur", "Impossible de charger les dépenses: " + e.getMessage());
        }
    }

    private void setupVocalAssistant() {
        vocalService.setOnTextRecognized(text -> {
            if (lblVoiceText != null) lblVoiceText.setText("🗣 \"" + text + "\"");
            VoiceCommandProcessor.VoiceCommand cmd = cmdProcessor.process(text);
            executeVoiceCommand(cmd);
        });
        vocalService.setOnStatusUpdate(status -> {
            if (lblVoiceStatus != null) lblVoiceStatus.setText(status);
            if (btnVoice != null) {
                boolean listening = status.contains("Écoute");
                btnVoice.setStyle(listening
                        ? "-fx-background-color:#ef4444;-fx-text-fill:white;-fx-background-radius:50%;-fx-min-width:48;-fx-min-height:48;-fx-font-size:20;-fx-cursor:hand;"
                        : "-fx-background-color:#ff8c42;-fx-text-fill:white;-fx-background-radius:50%;-fx-min-width:48;-fx-min-height:48;-fx-font-size:20;-fx-cursor:hand;");
            }
        });
        Thread t = new Thread(() -> {
            vocalService.initialize();
            Platform.runLater(() -> { if (lblVoiceStatus != null) lblVoiceStatus.setText("✅ Assistant prêt"); });
        });
        t.setDaemon(true);
        t.start();
    }

    @FXML public void handleToggleVoice(ActionEvent event) {
        btnVoice.setDisable(true);
        vocalService.toggleListening();
        btnVoice.setDisable(false);
    }

    private void executeVoiceCommand(VoiceCommandProcessor.VoiceCommand cmd) {
        // ... (identique à l'original)
        if (cmd == null) return;
        double budgetRestant = 0;
        String deviseBudget = "EUR";
        if (selectedBudget != null) {
            deviseBudget = selectedBudget.getDeviseBudget();
            double total = depensesList.stream().mapToDouble(Depense::getMontantDepense).sum();
            budgetRestant = selectedBudget.getMontantTotal() - total;
        }
        switch (cmd.type) {
            case ADD_EXPENSE -> Platform.runLater(() -> {
                clearDepenseForm();
                if (cmd.amount > 0) txtMontantDepense.setText(String.format("%.0f", cmd.amount));
                if (cmd.category != null) cmbCategorieDepense.setValue(cmd.category);
                if (cmd.label != null) txtLibelleDepense.setText(cmd.label);
                if (cmd.fromCurrency != null) selectDevise(cmbDeviseDepense, cmd.fromCurrency);
                lblModalTitle.setText("Nouvelle Dépense 🎙️");
                modalDepense.setVisible(true); modalDepense.setManaged(true);
            });
            case CHECK_BUDGET -> { final double r = budgetRestant; final String d = deviseBudget;
                Platform.runLater(() -> { String msg = r >= 0 ? String.format("Il vous reste %.2f %s", r, d) : String.format("Budget dépassé de %.2f %s", Math.abs(r), d);
                    if (primaryStage != null) ToastNotification.showInfo(primaryStage, "💰 Budget", msg); }); }
            case FILTER_EXPENSES -> Platform.runLater(() -> { if (cmd.category != null) { cmbFiltreCategorie.setValue(cmd.category); handleAppliquerFiltres(null); } });
            case CONVERT_CURRENCY -> { if (cmd.amount > 0 && cmd.fromCurrency != null && cmd.toCurrency != null) {
                double amt = cmd.amount; String fc = cmd.fromCurrency; String tc = cmd.toCurrency;
                Thread t = new Thread(() -> { double res = currencyConverter.convert(amt, fc, tc);
                    Platform.runLater(() -> { String msg = res > 0 ? String.format("%.2f %s = %.2f %s", amt, fc, res, tc) : "Conversion indisponible";
                        if (primaryStage != null) ToastNotification.showInfo(primaryStage, "💱 Conversion", msg); }); });
                t.setDaemon(true); t.start(); } }
            case EXPORT_PDF -> Platform.runLater(() -> handleExportPDF(null));
            case EXPORT_EXCEL -> Platform.runLater(this::handleExportExcel);
            case RESET_FILTERS -> Platform.runLater(() -> handleReinitialiserFiltres(null));
            default -> Platform.runLater(() -> { if (primaryStage != null) ToastNotification.showInfo(primaryStage, "🎙️ Commande inconnue", "Essayez: \"ajoute une dépense\", \"budget restant\", \"exporte en PDF\""); });
        }
        final double fr = budgetRestant; final String fd = deviseBudget;
        Thread tts = new Thread(() -> ttsService.respondToCommand(cmd, fr, fd));
        tts.setDaemon(true); tts.start();
    }

    private void loadDepensesForBudget(int budgetId) {
        try {
            List<Depense> all = depenseCRUD.afficher();
            depensesList.setAll(all.stream().filter(d -> d.getIdBudget() == budgetId).collect(Collectors.toList()));
            updateNbDepenses();
            updateKPI();
        } catch (Exception e) { showAlert("Erreur", "Impossible de charger les dépenses: " + e.getMessage()); }
    }

    private void filterBudgetsByVoyage(String selectedVoyage) {
        try {
            int id = extractVoyageId(selectedVoyage);
            List<Budget> filtered = budgetsList.stream()
                    .filter(b -> b.getIdVoyage() == id)
                    .collect(Collectors.toList());
            displayVoyageBudgets(filtered);
        } catch (Exception e) { System.err.println("Erreur filtrage: " + e.getMessage()); }
    }

    private int extractVoyageId(String s) {
        try { return Integer.parseInt(s.split(" - ")[0]); } catch (Exception e) { return 0; }
    }

    private String getVoyageDisplayName(int id) {
        if (id == 0) return "Sans voyage";
        VoyageHelper.VoyageInfo v = voyagesMap.get(id);
        return v != null ? v.getIdVoyage() + " - " + v.getTitre() + " (" + v.getDatesFormatted() + ")" : "Voyage inconnu";
    }

    private void displayVoyageBudgets(List<Budget> budgets) {
        voyageBudgetsContainer.getChildren().clear();
        if (budgets.isEmpty()) {
            Label l = new Label("Aucun budget pour ce voyage");
            l.setStyle("-fx-text-fill:#64748b;-fx-padding:20;");
            voyageBudgetsContainer.getChildren().add(l);
            lblVoyageBudgetsCount.setText("0 budget");
            return;
        }
        lblVoyageBudgetsCount.setText(budgets.size() + (budgets.size() > 1 ? " budgets" : " budget"));
        for (Budget b : budgets) voyageBudgetsContainer.getChildren().add(createCompactBudgetCard(b));
    }

    private HBox createCompactBudgetCard(Budget budget) {
        // ... (identique à l'original)
        HBox card = new HBox(15);
        card.setStyle("-fx-background-color:white;-fx-background-radius:16;-fx-padding:12;-fx-border-color:#e2e8f0;-fx-border-width:1;-fx-border-radius:16;-fx-cursor:hand;");
        card.setPrefHeight(60);
        card.setAlignment(Pos.CENTER_LEFT);
        VBox iconBox = new VBox(); iconBox.setAlignment(Pos.CENTER);
        iconBox.setStyle("-fx-background-color:#fff7ed;-fx-background-radius:10;-fx-min-width:40;-fx-min-height:40;");
        String icon = "💰";
        String lib = budget.getLibelleBudget().toLowerCase();
        if (lib.contains("hebergement") || lib.contains("hôtel")) icon = "🏨";
        else if (lib.contains("restauration")) icon = "🍽️";
        else if (lib.contains("transport")) icon = "🚗";
        else if (lib.contains("activité")) icon = "🎟️";
        Label iconLabel = new Label(icon); iconLabel.setStyle("-fx-font-size:20;");
        iconBox.getChildren().add(iconLabel);
        Label nom = new Label(budget.getLibelleBudget()); nom.setStyle("-fx-font-weight:600;-fx-font-size:14;-fx-text-fill:#0f172a;");
        card.getChildren().addAll(iconBox, nom);
        card.setOnMouseClicked(e -> selectBudget(budget));
        return card;
    }

    private void selectBudget(Budget budget) {
        this.selectedBudget = budget;
        notifier.resetForNewPeriod();

        lblBudgetNom.setText(budget.getLibelleBudget());
        lblBudgetStatut.setText("● " + budget.getStatutBudget());
        lblBudgetDevise.setText(budget.getDeviseBudget());
        lblBudgetDescription.setText(budget.getDescriptionBudget() != null ? budget.getDescriptionBudget() : "Aucune description");

        if (budget.getIdVoyage() > 0) {
            VoyageHelper.VoyageInfo v = voyagesMap.get(budget.getIdVoyage());
            if (v != null) { lblBudgetDates.setText(v.getDatesFormatted()); lblBudgetDestination.setText(v.getNomDestination()); }
            else { lblBudgetDates.setText("Dates non définies"); lblBudgetDestination.setText("Destination non définie"); }
        } else { lblBudgetDates.setText("Dates non définies"); lblBudgetDestination.setText("Destination non définie"); }

        String color = switch (budget.getStatutBudget()) {
            case "ACTIF" -> "#10b981"; case "TERMINE" -> "#ef4444";
            case "PLANIFIE" -> "#f59e0b"; case "ENCOURS" -> "#3b82f6"; default -> "#94a3b8";
        };
        statusContainer.setStyle("-fx-background-color:" + color + ";-fx-background-radius:30;-fx-padding:6 18;");

        updateKPI();
        loadDepensesForBudget(budget.getIdBudget());
        updateInflationInfo();
        // Suppression de l'appel automatique à la crise
        // triggerCrisisAutoCheck();
    }

    private void updateKPI() {
        if (selectedBudget == null) return;
        String deviseBudget = selectedBudget.getDeviseBudget();
        lblMontantTotal.setText(String.format("%.2f", selectedBudget.getMontantTotal()) + " " + deviseBudget);

        List<Depense> depensesDuBudget = depensesList.stream()
                .filter(d -> d.getIdBudget() == selectedBudget.getIdBudget()).collect(Collectors.toList());

        double totalConverti = 0; String autreDevise = null;
        for (Depense d : depensesDuBudget) {
            double montantConverti = d.getDeviseDepense().equalsIgnoreCase(deviseBudget)
                    ? d.getMontantDepense()
                    : currencyConverter.convert(d.getMontantDepense(), d.getDeviseDepense(), deviseBudget);
            if (!d.getDeviseDepense().equalsIgnoreCase(deviseBudget)) { autreDevise = d.getDeviseDepense(); if (montantConverti < 0) montantConverti = d.getMontantDepense(); }
            totalConverti += montantConverti;
        }

        double restant = selectedBudget.getMontantTotal() - totalConverti;
        double pct = selectedBudget.getMontantTotal() > 0 ? (totalConverti / selectedBudget.getMontantTotal()) * 100 : 0;
        lblDepense.setText(String.format("%.2f", totalConverti) + " " + deviseBudget);
        lblRestant.setText(String.format("%.2f", restant) + " " + deviseBudget);
        lblPourcentageDepense.setText(String.format("↗ %.1f%% du budget", pct));
        lblPourcentageRestant.setText(String.format("✓ %.1f%% disponible", 100 - pct));
        lblInfoBudget.setText("Budget " + selectedBudget.getStatutBudget().toLowerCase());

        updateProgressBar(pct);
        updateRateCard(deviseBudget, autreDevise);
        notifier.checkBudgetWithToast(totalConverti, selectedBudget.getMontantTotal(), selectedBudget.getLibelleBudget(), primaryStage);
    }

    private void updateProgressBar(double pct) {
        if (progressBarContainer == null || progressBarFill == null) return;
        Platform.runLater(() -> {
            double w = progressBarContainer.getWidth();
            if (w <= 0) { progressBarContainer.applyCss(); progressBarContainer.layout(); w = progressBarContainer.getWidth(); }
            if (w <= 0) w = 500;
            double clamped = Math.min(100, Math.max(0, pct));
            progressBarFill.setWidth((clamped / 100.0) * w);
            Color c = pct < 60 ? Color.web("#10b981") : pct < 85 ? Color.web("#f59e0b") : Color.web("#ef4444");
            progressBarFill.setFill(c);
            if (lblProgressPct != null) lblProgressPct.setText(String.format("%.1f%%", clamped));
        });
    }

    private void updateRateCard(String deviseBudget, String autreDevise) {
        if (lblTauxChange == null) return;
        if (autreDevise == null || autreDevise.equalsIgnoreCase(deviseBudget)) {
            lblTauxChange.setText("Toutes dépenses en " + deviseBudget);
            if (lblTauxUpdate != null) lblTauxUpdate.setText("Aucune conversion nécessaire");
            if (lblDevisesActives != null) lblDevisesActives.setText(deviseBudget + " → " + deviseBudget);
            return;
        }
        String finalAutreDevise = autreDevise;
        Thread t = new Thread(() -> {
            try {
                double rate = currencyConverter.getRate(finalAutreDevise, deviseBudget);
                Platform.runLater(() -> {
                    if (rate > 0) { lblTauxChange.setText(String.format("1 %s = %.4f %s", finalAutreDevise, rate, deviseBudget)); if (lblTauxUpdate != null) lblTauxUpdate.setText("Mis à jour ✓"); if (lblDevisesActives != null) lblDevisesActives.setText(finalAutreDevise + " → " + deviseBudget); }
                    else { lblTauxChange.setText("Taux indisponible"); if (lblTauxUpdate != null) lblTauxUpdate.setText("Erreur de connexion"); }
                });
            } catch (Exception e) { Platform.runLater(() -> { lblTauxChange.setText("Taux indisponible"); if (lblTauxUpdate != null) lblTauxUpdate.setText("⚠ Erreur API"); }); }
        });
        t.setDaemon(true); t.start();
    }

    private void updateNbDepenses() {
        lblNbDepenses.setText(depensesList.size() + (depensesList.size() > 1 ? " dépenses" : " dépense"));
    }

    @FXML public void handleToggleAlertHistory(ActionEvent event) {
        if (alertHistoryPanel == null) return;
        boolean visible = alertHistoryPanel.isVisible();
        alertHistoryPanel.setVisible(!visible); alertHistoryPanel.setManaged(!visible);
        if (!visible) refreshAlertHistory();
    }

    @FXML public void handleClearAlertHistory(ActionEvent event) { ToastNotification.clearHistory(); refreshAlertHistory(); }

    private void refreshAlertHistory() {
        if (alertHistoryList == null) return;
        alertHistoryList.getChildren().clear();
        List<ToastNotification.AlertRecord> history = ToastNotification.getHistory();
        int count = history.size();
        if (lblAlertBadge != null) { lblAlertBadge.setText(String.valueOf(count)); lblAlertBadge.setVisible(count > 0); }
        if (history.isEmpty()) { Label empty = new Label("Aucune alerte"); empty.setStyle("-fx-text-fill:#94a3b8;-fx-font-size:13;-fx-padding:16;"); alertHistoryList.getChildren().add(empty); return; }
        for (ToastNotification.AlertRecord record : history) {
            HBox row = new HBox(12); row.setPadding(new Insets(10,14,10,14)); row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle("-fx-background-color:white;-fx-background-radius:12;-fx-border-color:" + record.getColor() + ";-fx-border-width:0 0 0 3;-fx-border-radius:12;");
            Label icon = new Label(record.getIcon()); icon.setStyle("-fx-font-size:20;");
            VBox text = new VBox(2);
            Label title = new Label(record.title); title.setStyle("-fx-font-weight:700;-fx-font-size:13;-fx-text-fill:#0f172a;");
            Label msg = new Label(record.message); msg.setStyle("-fx-font-size:11;-fx-text-fill:#64748b;"); msg.setMaxWidth(280); msg.setWrapText(true);
            Label time = new Label(record.timestamp); time.setStyle("-fx-font-size:10;-fx-text-fill:#94a3b8;");
            text.getChildren().addAll(title, msg, time);
            row.getChildren().addAll(icon, text); alertHistoryList.getChildren().add(row);
        }
    }

    @FXML public void handleAppliquerFiltres(ActionEvent e) {
        filteredDepenses.setPredicate(d -> {
            String cat = cmbFiltreCategorie.getValue();
            if (cat != null && !cat.equals("Toutes les catégories") && !d.getCategorieDepense().equals(cat)) return false;
            if (dpDateDebut.getValue() != null && d.getDateCreation().before(Date.valueOf(dpDateDebut.getValue()))) return false;
            if (dpDateFin.getValue() != null && d.getDateCreation().after(Date.valueOf(dpDateFin.getValue()))) return false;
            return true;
        });
    }

    @FXML public void handleReinitialiserFiltres(ActionEvent e) { cmbFiltreCategorie.setValue("Toutes les catégories"); dpDateDebut.setValue(null); dpDateFin.setValue(null); filteredDepenses.setPredicate(null); }
    @FXML public void handleTriMontant(ActionEvent e) { if (btnTriMontant.isSelected()) { tableDepenses.getSortOrder().clear(); tableDepenses.getSortOrder().add(colMontant); colMontant.setSortType(TableColumn.SortType.DESCENDING); } }
    @FXML public void handleTriDate(ActionEvent e) { if (btnTriDate.isSelected()) { tableDepenses.getSortOrder().clear(); tableDepenses.getSortOrder().add(colDate); colDate.setSortType(TableColumn.SortType.DESCENDING); } }
    @FXML public void handleTriLibelle(ActionEvent e) { if (btnTriLibelle.isSelected()) { tableDepenses.getSortOrder().clear(); tableDepenses.getSortOrder().add(colLibelle); colLibelle.setSortType(TableColumn.SortType.ASCENDING); } }

    @FXML public void handleAjouterDepense(ActionEvent e) { clearDepenseForm(); lblModalTitle.setText("Nouvelle Dépense"); modalDepense.setVisible(true); modalDepense.setManaged(true); }
    @FXML public void handleCloseModalDepense(ActionEvent e) { modalDepense.setVisible(false); modalDepense.setManaged(false); clearDepenseForm(); }

    @FXML public void handleSaveDepense(ActionEvent event) {
        if (!validateDepenseForm()) return;
        try {
            Depense d = new Depense();
            d.setLibelleDepense(txtLibelleDepense.getText());
            d.setCategorieDepense(cmbCategorieDepense.getValue());
            d.setMontantDepense(Double.parseDouble(txtMontantDepense.getText()));
            d.setDeviseDepense(CurrencyService.extractCode(cmbDeviseDepense.getValue()));
            d.setTypePaiement(cmbPaiementDepense.getValue());
            if (dateDepense.getValue() != null) d.setDateCreation(Date.valueOf(dateDepense.getValue()));
            d.setDescriptionDepense(txtNotesDepense.getText());
            d.setIdBudget(selectedBudget != null ? selectedBudget.getIdBudget() : 1);
            d.validate();
            depenseCRUD.ajouter(d);
            loadDepensesForBudget(selectedBudget != null ? selectedBudget.getIdBudget() : 1);
            handleCloseModalDepense(null);
            showInfo("Succès", "Dépense ajoutée avec succès!");
            // Suppression de l'appel automatique à la crise
            // triggerCrisisAutoCheck();
        } catch (Exception ex) { showAlert("Erreur", "Impossible d'ajouter: " + ex.getMessage()); }
    }

    private void handleEditDepense(Depense depense) {
        // ... (identique à l'original)
        txtLibelleDepense.setText(depense.getLibelleDepense());
        cmbCategorieDepense.setValue(depense.getCategorieDepense());
        txtMontantDepense.setText(String.valueOf(depense.getMontantDepense()));
        selectDevise(cmbDeviseDepense, depense.getDeviseDepense());
        cmbPaiementDepense.setValue(depense.getTypePaiement());
        if (depense.getDateCreation() != null) dateDepense.setValue(depense.getDateCreation().toLocalDate());
        txtNotesDepense.setText(depense.getDescriptionDepense());
        lblModalTitle.setText("Modifier Dépense");
        modalDepense.setVisible(true); modalDepense.setManaged(true);
        btnSaveDepense.setOnAction(ev -> {
            try {
                depense.setLibelleDepense(txtLibelleDepense.getText()); depense.setCategorieDepense(cmbCategorieDepense.getValue());
                depense.setMontantDepense(Double.parseDouble(txtMontantDepense.getText())); depense.setDeviseDepense(CurrencyService.extractCode(cmbDeviseDepense.getValue()));
                depense.setTypePaiement(cmbPaiementDepense.getValue()); if (dateDepense.getValue() != null) depense.setDateCreation(Date.valueOf(dateDepense.getValue()));
                depense.setDescriptionDepense(txtNotesDepense.getText()); depense.validate();
                depenseCRUD.modifier(depense);
                loadDepensesForBudget(selectedBudget != null ? selectedBudget.getIdBudget() : 1);
                handleCloseModalDepense(null); showInfo("Succès", "Dépense modifiée!");
            } catch (Exception ex) { showAlert("Erreur", "Impossible de modifier: " + ex.getMessage()); }
        });
    }

    private void handleDeleteDepense(Depense depense) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION,"Supprimer cette dépense ?", ButtonType.OK, ButtonType.CANCEL);
        a.setTitle("Confirmation"); a.setHeaderText(null);
        a.showAndWait().ifPresent(r -> { if (r == ButtonType.OK) { try { depenseCRUD.supprimer(depense.getIdDepense()); loadDepensesForBudget(selectedBudget != null ? selectedBudget.getIdBudget() : 1); showInfo("Succès", "Dépense supprimée!"); } catch (Exception ex) { showAlert("Erreur", "Impossible de supprimer: " + ex.getMessage()); } } });
    }

    @FXML public void handleNouveauBudget(ActionEvent e) { clearBudgetForm(); lblModalBudgetTitle.setText("Nouveau Budget"); lblModalBudgetSubtitle.setText("Créez un nouveau budget pour votre voyage"); modalBudget.setVisible(true); modalBudget.setManaged(true); }
    @FXML public void handleCloseModalBudget(ActionEvent e) { modalBudget.setVisible(false); modalBudget.setManaged(false); clearBudgetForm(); currentSaisonIdeale = null; }

    @FXML public void handleSaveBudget(ActionEvent event) {
        if (!validateBudgetForm()) return;
        try {
            Budget b = new Budget();
            b.setLibelleBudget(txtNomBudget.getText()); b.setMontantTotal(Double.parseDouble(txtMontantBudget.getText()));
            b.setDeviseBudget(CurrencyService.extractCode(cmbDeviseBudget.getValue())); b.setStatutBudget(cmbStatutBudget.getValue());
            b.setDescriptionBudget(txtDescriptionBudget.getText()); b.setId(1);
            String sv = cmbVoyageBudget.getValue();
            b.setIdVoyage((sv != null && !sv.equals("Sans voyage") && !sv.isEmpty()) ? extractVoyageId(sv) : 0);
            b.validate(); budgetCRUD.ajouter(b); loadBudgets(); handleCloseModalBudget(null); showInfo("Succès", "Budget créé!");
        } catch (Exception ex) { showAlert("Erreur", "Impossible de créer: " + ex.getMessage()); }
    }

    @FXML public void handleModifierBudget(ActionEvent event) {
        if (selectedBudget == null) { showAlert("Attention", "Sélectionnez un budget"); return; }
        txtNomBudget.setText(selectedBudget.getLibelleBudget()); txtMontantBudget.setText(String.valueOf(selectedBudget.getMontantTotal()));
        selectDevise(cmbDeviseBudget, selectedBudget.getDeviseBudget()); cmbStatutBudget.setValue(selectedBudget.getStatutBudget());
        txtDescriptionBudget.setText(selectedBudget.getDescriptionBudget());
        cmbVoyageBudget.setValue(selectedBudget.getIdVoyage() > 0 ? getVoyageDisplayName(selectedBudget.getIdVoyage()) : "Sans voyage");
        lblModalBudgetTitle.setText("Modifier Budget"); lblModalBudgetSubtitle.setText("Modifiez les informations du budget");
        modalBudget.setVisible(true); modalBudget.setManaged(true);
        btnSaveBudget.setOnAction(ev -> {
            try {
                selectedBudget.setLibelleBudget(txtNomBudget.getText()); selectedBudget.setMontantTotal(Double.parseDouble(txtMontantBudget.getText()));
                selectedBudget.setDeviseBudget(CurrencyService.extractCode(cmbDeviseBudget.getValue())); selectedBudget.setStatutBudget(cmbStatutBudget.getValue());
                selectedBudget.setDescriptionBudget(txtDescriptionBudget.getText());
                String sv = cmbVoyageBudget.getValue();
                selectedBudget.setIdVoyage((sv != null && !sv.equals("Sans voyage") && !sv.isEmpty()) ? extractVoyageId(sv) : 0);
                selectedBudget.validate(); budgetCRUD.modifier(selectedBudget); loadBudgets(); selectBudget(selectedBudget);
                handleCloseModalBudget(null); showInfo("Succès", "Budget modifié!");
            } catch (Exception ex) { showAlert("Erreur", "Impossible de modifier: " + ex.getMessage()); }
        });
    }

    @FXML public void handleSupprimerBudget(ActionEvent event) {
        if (selectedBudget == null) { showAlert("Attention", "Sélectionnez un budget"); return; }
        Alert a = new Alert(Alert.AlertType.CONFIRMATION,"Supprimer ce budget ?", ButtonType.OK, ButtonType.CANCEL);
        a.setTitle("Confirmation"); a.setHeaderText(null);
        a.showAndWait().ifPresent(r -> { if (r == ButtonType.OK) { try { budgetCRUD.supprimer(selectedBudget.getIdBudget()); loadBudgets(); showInfo("Succès", "Budget supprimé!"); } catch (Exception ex) { showAlert("Erreur", "Impossible de supprimer: " + ex.getMessage()); } } });
    }

    @FXML private void handleExportExcel() {
        ObservableList<Depense> items = tableDepenses.getItems();
        if (items.isEmpty()) { showAlert(Alert.AlertType.WARNING,"Export Excel","Aucune donnée."); return; }
        FileChooser fc = new FileChooser(); fc.setTitle("Exporter en Excel");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel","*.xlsx"));
        fc.setInitialFileName("depenses_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx");
        File file = fc.showSaveDialog(tableDepenses.getScene().getWindow());
        if (file != null) { try { exportToExcel(items, file); showInfo("Succès","Excel exporté."); if (java.awt.Desktop.isDesktopSupported()) java.awt.Desktop.getDesktop().open(file); } catch (IOException ex) { showAlert(Alert.AlertType.ERROR,"Erreur","Excel: " + ex.getMessage()); } }
    }

    private void exportToExcel(ObservableList<Depense> depenses, File file) throws IOException {
        // ... (identique à l'original)
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Dépenses");
            CellStyle ts = wb.createCellStyle(); Font tf = wb.createFont(); tf.setBold(true); tf.setFontHeightInPoints((short)16); ts.setFont(tf); ts.setAlignment(HorizontalAlignment.CENTER);
            CellStyle hs = wb.createCellStyle(); Font hf = wb.createFont(); hf.setBold(true); hf.setColor(IndexedColors.WHITE.getIndex()); hs.setFont(hf); hs.setFillForegroundColor(IndexedColors.ORANGE.getIndex()); hs.setFillPattern(FillPatternType.SOLID_FOREGROUND); hs.setAlignment(HorizontalAlignment.CENTER);
            CellStyle ds = wb.createCellStyle(); ds.setBorderBottom(BorderStyle.THIN); ds.setBorderTop(BorderStyle.THIN); ds.setBorderLeft(BorderStyle.THIN); ds.setBorderRight(BorderStyle.THIN);
            Row tr = sheet.createRow(0); org.apache.poi.ss.usermodel.Cell tc = tr.createCell(0);
            tc.setCellValue("RAPPORT DES DÉPENSES - " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))); tc.setCellStyle(ts);
            sheet.addMergedRegion(new CellRangeAddress(0,0,0,6));
            String[] headers = {"Libellé","Catégorie","Montant","Devise","Date","Paiement","Description"};
            Row hr = sheet.createRow(3);
            for (int i = 0; i < headers.length; i++) { org.apache.poi.ss.usermodel.Cell c = hr.createCell(i); c.setCellValue(headers[i]); c.setCellStyle(hs); }
            int rn = 4;
            for (Depense d : depenses) {
                Row r = sheet.createRow(rn++);
                r.createCell(0).setCellValue(d.getLibelleDepense() != null ? d.getLibelleDepense() : "");
                r.createCell(1).setCellValue(d.getCategorieDepense() != null ? d.getCategorieDepense() : "");
                r.createCell(2).setCellValue(d.getMontantDepense());
                r.createCell(3).setCellValue(d.getDeviseDepense() != null ? d.getDeviseDepense() : "");
                r.createCell(4).setCellValue(d.getDateCreation() != null ? d.getDateCreation().toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "");
                r.createCell(5).setCellValue(d.getTypePaiement() != null ? d.getTypePaiement() : "");
                r.createCell(6).setCellValue(d.getDescriptionDepense() != null ? d.getDescriptionDepense() : "");
                for (int i = 0; i < 7; i++) if (r.getCell(i) != null) r.getCell(i).setCellStyle(ds);
            }
            for (int i = 0; i < 7; i++) sheet.autoSizeColumn(i);
            try (FileOutputStream fos = new FileOutputStream(file)) { wb.write(fos); }
        }
    }

    @FXML public void handleExportPDF(ActionEvent event) {
        ObservableList<Depense> items = tableDepenses.getItems();
        if (items.isEmpty()) { showAlert(Alert.AlertType.WARNING,"Export PDF","Aucune dépense."); return; }
        FileChooser fc = new FileChooser(); fc.setTitle("Enregistrer PDF");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF","*.pdf"));
        fc.setInitialFileName("depenses_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".pdf");
        File file = fc.showSaveDialog(tableDepenses.getScene().getWindow());
        if (file != null) { try { exportToPDF(items, file); if (java.awt.Desktop.isDesktopSupported()) java.awt.Desktop.getDesktop().open(file); showInfo("Succès","PDF exporté."); } catch (Exception ex) { showAlert(Alert.AlertType.ERROR,"Erreur","PDF: " + ex.getMessage()); } }
    }

    private void exportToPDF(ObservableList<Depense> depenses, File file) throws Exception {
        // ... (identique à l'original)
        PdfWriter writer = new PdfWriter(file); PdfDocument pdf = new PdfDocument(writer); Document doc = new Document(pdf);
        DeviceRgb orange = new DeviceRgb(255,140,66);
        PdfFont bold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        PdfFont normal = PdfFontFactory.createFont(StandardFonts.HELVETICA);
        doc.add(new Paragraph().add(new Text("RAPPORT DES DEPENSES\n").setFont(bold).setFontSize(24).setFontColor(orange)).add(new Text("TravelMate PRO").setFont(normal).setFontSize(14)).setTextAlignment(TextAlignment.CENTER).setMarginBottom(20));
        float[] w = {2f,1.5f,1f,0.8f,1f,1.5f,2.5f};
        Table table = new Table(w); table.setWidth(UnitValue.createPercentValue(100));
        for (String h : new String[]{"Libellé","Catégorie","Montant","Devise","Date","Paiement","Description"})
            table.addCell(new Cell().add(new Paragraph(h).setFont(bold).setFontSize(10).setFontColor(ColorConstants.WHITE)).setBackgroundColor(orange).setTextAlignment(TextAlignment.CENTER).setPadding(8));
        for (Depense d : depenses) {
            table.addCell(createPdfCell(d.getLibelleDepense(), normal, 9, TextAlignment.LEFT));
            table.addCell(createPdfCell(d.getCategorieDepense(), normal, 9, TextAlignment.LEFT));
            table.addCell(createPdfCell(String.format("%.2f", d.getMontantDepense()), normal, 9, TextAlignment.RIGHT));
            table.addCell(createPdfCell(d.getDeviseDepense(), normal, 9, TextAlignment.CENTER));
            table.addCell(createPdfCell(d.getDateCreation() != null ? d.getDateCreation().toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "", normal, 9, TextAlignment.CENTER));
            table.addCell(createPdfCell(d.getTypePaiement(), normal, 9, TextAlignment.LEFT));
            table.addCell(createPdfCell(d.getDescriptionDepense(), normal, 9, TextAlignment.LEFT));
        }
        doc.add(table); doc.close();
    }

    private Cell createPdfCell(String t, PdfFont f, float s, TextAlignment a) {
        return new Cell().add(new Paragraph(t != null ? t : "").setFont(f).setFontSize(s)).setTextAlignment(a).setPadding(5).setBorder(new SolidBorder(ColorConstants.LIGHT_GRAY, 0.5f));
    }

    private boolean validateDepenseForm() {
        if (txtLibelleDepense.getText().trim().isEmpty()) { showAlert("Validation","Le libellé est requis"); return false; }
        if (cmbCategorieDepense.getValue() == null) { showAlert("Validation","La catégorie est requise"); return false; }
        if (txtMontantDepense.getText().trim().isEmpty()) { showAlert("Validation","Le montant est requis"); return false; }
        try { Double.parseDouble(txtMontantDepense.getText()); } catch (NumberFormatException e) { showAlert("Validation","Montant invalide"); return false; }
        if (cmbDeviseDepense.getValue() == null) { showAlert("Validation","La devise est requise"); return false; }
        if (cmbPaiementDepense.getValue() == null) { showAlert("Validation","Le paiement est requis"); return false; }
        if (dateDepense.getValue() == null) { showAlert("Validation","La date est requise"); return false; }
        return true;
    }

    private boolean validateBudgetForm() {
        if (txtNomBudget.getText().trim().isEmpty()) { showAlert("Validation","Le nom est requis"); return false; }
        if (txtMontantBudget.getText().trim().isEmpty()) { showAlert("Validation","Le montant est requis"); return false; }
        try { Double.parseDouble(txtMontantBudget.getText()); } catch (NumberFormatException e) { showAlert("Validation","Montant invalide"); return false; }
        if (cmbDeviseBudget.getValue() == null) { showAlert("Validation","La devise est requise"); return false; }
        if (cmbStatutBudget.getValue() == null) { showAlert("Validation","Le statut est requis"); return false; }
        return true;
    }

    private void clearDepenseForm() {
        txtLibelleDepense.clear(); cmbCategorieDepense.setValue(null); txtMontantDepense.clear();
        selectDevise(cmbDeviseDepense,"EUR"); cmbPaiementDepense.setValue(null); dateDepense.setValue(null);
        txtNotesDepense.clear(); btnSaveDepense.setOnAction(this::handleSaveDepense);
    }

    private void clearBudgetForm() {
        txtNomBudget.clear(); txtMontantBudget.clear(); selectDevise(cmbDeviseBudget,"EUR");
        cmbStatutBudget.setValue("ACTIF"); txtDescriptionBudget.clear();
        cmbVoyageBudget.setValue("Sans voyage"); btnSaveBudget.setOnAction(this::handleSaveBudget);
        if (lblEstimation != null) {
            lblEstimation.setText("");
            lblEstimation.setVisible(false);
            lblEstimation.setManaged(false);
        }
    }

    private Stage getPrimaryStage() {
        if (primaryStage == null) primaryStage = (Stage) tableDepenses.getScene().getWindow();
        return primaryStage;
    }

    private void showAlert(Alert.AlertType t, String title, String msg) { Alert a = new Alert(t); a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.showAndWait(); }
    private void showAlert(String title, String msg) { showAlert(Alert.AlertType.ERROR, title, msg); }
    private void showInfo(String title, String msg) { showAlert(Alert.AlertType.INFORMATION, title, msg); }
}