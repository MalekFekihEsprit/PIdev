package Controllers;

import Entities.Activites;
import Entities.Categories;
import Services.ActivitesCRUD;
import Services.AIServiceActivites;
import Services.CategoriesCRUD;
import Utils.FileManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class ACTback implements Initializable {

    // TableView et colonnes
    @FXML private TableView<Activites> tableActivites;
    @FXML private TableColumn<Activites, Integer> colId;
    @FXML private TableColumn<Activites, String> colNom;
    @FXML private TableColumn<Activites, String> colDescription;
    @FXML private TableColumn<Activites, Integer> colBudget;
    @FXML private TableColumn<Activites, Integer> colDuree;
    @FXML private TableColumn<Activites, String> colNiveauDifficulte;
    @FXML private TableColumn<Activites, String> colLieu;
    @FXML private TableColumn<Activites, Integer> colAgeMin;
    @FXML private TableColumn<Activites, String> colStatut;
    @FXML private TableColumn<Activites, String> colCategorie;
    @FXML private TableColumn<Activites, String> colImage;

    // Boutons et champs
    @FXML private Button btnModifier;
    @FXML private Button btnSupprimer;
    @FXML private TextField searchField;
    @FXML private Label lblTotalActivites;
    @FXML private Label lblKpiTotal;
    @FXML private Label lblKpiActives;
    @FXML private Label lblKpiInactives;
    @FXML private Label lblKpiBudget;
    @FXML private Label lblStatActivites;
    @FXML private Label lblStatActives;
    @FXML private Label lblStatInactives;
    @FXML private Label lblCountBadge;
    @FXML private Button btnVersCategories;
    @FXML private Button btnFrontOffice;

    // Éléments du menu latéral (navigation)
    @FXML private HBox btnDestinations;
    @FXML private HBox btnHebergement;
    @FXML private HBox btnItineraires;
    @FXML private HBox btnVoyages;
    @FXML private HBox btnBudgets;
    @FXML private HBox btnUsers;
    @FXML private HBox btnStats;
    @FXML private HBox btnCategories;
    @FXML private Label lblCategoriesCount;
    @FXML private HBox userProfileBox;

    // Patterns de validation
    private static final Pattern NOM_PATTERN = Pattern.compile("^[a-zA-ZÀ-ÿ\\s\\-']+$");

    // Constantes de validation
    private static final int NOM_MIN_LENGTH = 3;
    private static final int NOM_MAX_LENGTH = 100;
    private static final int DESCRIPTION_MIN_LENGTH = 15;
    private static final int DESCRIPTION_MAX_LENGTH = 500;
    private static final int BUDGET_MIN = 1;
    private static final int BUDGET_MAX = 1000000;
    private static final int DUREE_MIN = 1;
    private static final int DUREE_MAX = 8760;
    private static final int AGE_MIN = 0;
    private static final int AGE_MAX = 120;

    // Service CRUD
    private ActivitesCRUD activitesCRUD;
    private ObservableList<Activites> activitesData;

    // Variables pour la gestion d'images
    private File selectedImageFile;
    private String currentImagePath;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        activitesCRUD = new ActivitesCRUD();
        activitesData = FXCollections.observableArrayList();

        setupTableColumns();
        loadActivites();

        tableActivites.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    boolean isSelected = newSelection != null;
                    btnModifier.setDisable(!isSelected);
                    btnSupprimer.setDisable(!isSelected);
                }
        );

        setupSearch();
        testAIConnection();
        setupNavigationButtons();
        loadCategoriesCount();
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colBudget.setCellValueFactory(new PropertyValueFactory<>("budget"));
        colDuree.setCellValueFactory(new PropertyValueFactory<>("duree"));
        colNiveauDifficulte.setCellValueFactory(new PropertyValueFactory<>("niveaudifficulte"));
        colLieu.setCellValueFactory(new PropertyValueFactory<>("lieu"));
        colAgeMin.setCellValueFactory(new PropertyValueFactory<>("agemin"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

        colCategorie.setCellValueFactory(cellData -> {
            Activites activite = cellData.getValue();
            if (activite.getCategorie() != null) {
                return new javafx.beans.property.SimpleStringProperty(activite.getCategorie().getNom());
            } else {
                return new javafx.beans.property.SimpleStringProperty("");
            }
        });

        colImage.setCellValueFactory(new PropertyValueFactory<>("imagePath"));
        colImage.setCellFactory(column -> new TableCell<Activites, String>() {
            private final ImageView imageView = new ImageView();

            @Override
            protected void updateItem(String imagePath, boolean empty) {
                super.updateItem(imagePath, empty);
                if (empty || imagePath == null || imagePath.isEmpty()) {
                    setGraphic(null);
                    setText("❌");
                } else {
                    try {
                        File file = new File(imagePath);
                        if (file.exists()) {
                            Image image = new Image(file.toURI().toString(), 30, 30, true, true);
                            imageView.setImage(image);
                            setGraphic(imageView);
                            setText(null);
                        } else {
                            setGraphic(null);
                            setText("❌");
                        }
                    } catch (Exception e) {
                        setGraphic(null);
                        setText("❌");
                    }
                }
            }
        });

        colStatut.setCellFactory(column -> new TableCell<Activites, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.equalsIgnoreCase("active")) {
                        setStyle("-fx-text-fill: #34d399; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
                    }
                }
            }
        });

        colBudget.setCellFactory(column -> new TableCell<Activites, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item + " €");
                    setStyle("-fx-text-fill: #ff8c42; -fx-font-weight: bold;");
                }
            }
        });
    }

    private void setupSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) {
                tableActivites.setItems(activitesData);
            } else {
                ObservableList<Activites> filteredData = FXCollections.observableArrayList();
                String lowerCaseFilter = newValue.toLowerCase();

                for (Activites activite : activitesData) {
                    if (activite.getNom().toLowerCase().contains(lowerCaseFilter) ||
                            activite.getLieu().toLowerCase().contains(lowerCaseFilter) ||
                            activite.getDescription().toLowerCase().contains(lowerCaseFilter)) {
                        filteredData.add(activite);
                    }
                }
                tableActivites.setItems(filteredData);
            }
        });
    }

    // ==================== MÉTHODES DE NAVIGATION CORRIGÉES ====================

    private void setupNavigationButtons() {
        // Destinations
        if (btnDestinations != null) {
            btnDestinations.setOnMouseClicked(event -> navigateToDestinationsBack());
            setupSidebarButtonHover(btnDestinations, "🌍", "Destinations");
        }

        // Hébergement
        if (btnHebergement != null) {
            btnHebergement.setOnMouseClicked(event -> navigateToHebergementBack());
            setupSidebarButtonHover(btnHebergement, "🏨", "Hébergement");
        }

        // Itinéraires
        if (btnItineraires != null) {
            btnItineraires.setOnMouseClicked(event ->
                    showInfo("Itinéraires", "Cette fonctionnalité sera bientôt disponible"));
            setupSidebarButtonHover(btnItineraires, "🗺️", "Itinéraires");
        }

        // Voyages
        if (btnVoyages != null) {
            btnVoyages.setOnMouseClicked(event ->
                    showInfo("Voyages", "Cette fonctionnalité sera bientôt disponible"));
            setupSidebarButtonHover(btnVoyages, "✈️", "Voyages");
        }

        // Budgets
        if (btnBudgets != null) {
            btnBudgets.setOnMouseClicked(event ->
                    showInfo("Budgets", "Cette fonctionnalité sera bientôt disponible"));
            setupSidebarButtonHover(btnBudgets, "💰", "Budgets");
        }

        // Utilisateurs
        if (btnUsers != null) {
            btnUsers.setOnMouseClicked(event ->
                    showInfo("Utilisateurs", "Cette fonctionnalité sera bientôt disponible"));
            setupSidebarButtonHover(btnUsers, "👥", "Utilisateurs");
        }

        // Statistiques
        if (btnStats != null) {
            btnStats.setOnMouseClicked(event ->
                    showInfo("Statistiques", "Cette fonctionnalité sera bientôt disponible"));
            setupSidebarButtonHover(btnStats, "📊", "Statistiques");
        }

        // Catégories
        if (btnCategories != null) {
            btnCategories.setOnMouseClicked(event -> navigateToCategoriesBack());
            setupSidebarButtonHover(btnCategories, "📑", "Catégories");
        }

        // Style spécial pour la page active (Activités)
        if (btnActivites != null) {
            btnActivites.setStyle("-fx-background-color: linear-gradient(to right, #ff8c42, #ff6b4a); -fx-background-radius: 12; -fx-padding: 12 16; -fx-cursor: hand;");
            btnActivites.lookupAll(".label").forEach(label -> {
                if (label instanceof Label) {
                    Label lbl = (Label) label;
                    if (lbl.getText().equals("🏄")) {
                        lbl.setStyle("-fx-font-size: 16; -fx-text-fill: white;");
                    } else if (!lbl.getText().matches("\\d+")) {
                        lbl.setStyle("-fx-text-fill: white; -fx-font-weight: 600; -fx-font-size: 14;");
                    }
                }
            });
        }

        // User profile
        if (userProfileBox != null) {
            userProfileBox.setOnMouseClicked(event -> navigateToProfile());
            userProfileBox.setOnMouseEntered(event ->
                    userProfileBox.setStyle("-fx-background-color: #2d3759; -fx-background-radius: 25; -fx-padding: 6 16 6 6; -fx-cursor: hand;"));
            userProfileBox.setOnMouseExited(event ->
                    userProfileBox.setStyle("-fx-background-color: #1e2749; -fx-background-radius: 25; -fx-padding: 6 16 6 6; -fx-cursor: hand;"));
        }
    }

    // Ajout de la référence manquante pour btnActivites
    @FXML private HBox btnActivites;

    private void setupSidebarButtonHover(HBox button, String icon, String text) {
        if (button == null) return;

        button.setOnMouseEntered(event -> {
            button.setStyle("-fx-background-color: rgba(255,140,66,0.15); -fx-background-radius: 12; -fx-padding: 12 16; -fx-cursor: hand; -fx-border-color: #ff8c42; -fx-border-width: 1; -fx-border-radius: 12;");
            button.lookupAll(".label").forEach(label -> {
                if (label instanceof Label) {
                    Label lbl = (Label) label;
                    if (lbl.getText().equals(icon)) {
                        lbl.setStyle("-fx-font-size: 16;");
                    } else if (!lbl.getText().matches("\\d+")) {
                        lbl.setStyle("-fx-text-fill: #ff8c42; -fx-font-weight: 600; -fx-font-size: 14;");
                    }
                }
            });
        });

        button.setOnMouseExited(event -> {
            button.setStyle("-fx-background-color: transparent; -fx-background-radius: 12; -fx-padding: 12 16; -fx-cursor: hand;");
            button.lookupAll(".label").forEach(label -> {
                if (label instanceof Label) {
                    Label lbl = (Label) label;
                    if (lbl.getText().equals(icon)) {
                        lbl.setStyle("-fx-font-size: 16;");
                    } else if (!lbl.getText().matches("\\d+")) {
                        lbl.setStyle("-fx-text-fill: #94a3b8; -fx-font-weight: 500; -fx-font-size: 14;");
                    }
                }
            });
        });
    }

    private void navigateToDestinationsBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DestinationBack.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnDestinations.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TravelMate - Gestion des Destinations");
            stage.setMaximized(true);
        } catch (IOException e) {
            showError("Erreur de navigation", "Impossible d'ouvrir les destinations: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void navigateToHebergementBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/HebergementBack.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnHebergement.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TravelMate - Gestion des Hébergements");
            stage.setMaximized(true);
        } catch (IOException e) {
            showError("Erreur de navigation", "Impossible d'ouvrir les hébergements: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void navigateToCategoriesBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/categoriesback.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnCategories.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TravelMate - Gestion des Catégories");
            stage.setMaximized(true);
        } catch (IOException e) {
            showError("Erreur de navigation", "Impossible d'ouvrir la gestion des catégories: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void navigateToProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/profile.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) userProfileBox.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TravelMate - Mon Profil");
            stage.setMaximized(true);
        } catch (IOException e) {
            showError("Erreur de navigation", "Impossible d'ouvrir le profil: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadCategoriesCount() {
        try {
            CategoriesCRUD crud = new CategoriesCRUD();
            int count = crud.afficher().size();
            if (lblCategoriesCount != null) {
                lblCategoriesCount.setText(String.valueOf(count));
            }
        } catch (SQLException e) {
            if (lblCategoriesCount != null) {
                lblCategoriesCount.setText("0");
            }
        }
    }
    // ==================== MÉTHODES EXISTANTES (inchangées) ====================

    private void loadActivites() {
        try {
            List<Activites> listeActivites = activitesCRUD.afficher();
            activitesData.clear();
            activitesData.addAll(listeActivites);
            tableActivites.setItems(activitesData);

            int total = activitesData.size();
            long actives = activitesData.stream()
                    .filter(a -> "active".equalsIgnoreCase(a.getStatut()))
                    .count();
            long inactives = total - actives;
            double budgetMoyen = activitesData.stream()
                    .mapToInt(Activites::getBudget)
                    .average()
                    .orElse(0);

            if (lblTotalActivites != null) lblTotalActivites.setText(String.valueOf(total));
            if (lblKpiTotal != null) lblKpiTotal.setText(String.valueOf(total));
            if (lblKpiActives != null) lblKpiActives.setText(String.valueOf(actives));
            if (lblKpiInactives != null) lblKpiInactives.setText(String.valueOf(inactives));
            if (lblKpiBudget != null) lblKpiBudget.setText(String.format("%.0f €", budgetMoyen));
            if (lblStatActivites != null) lblStatActivites.setText(String.valueOf(total));
            if (lblStatActives != null) lblStatActives.setText(String.valueOf(actives));
            if (lblStatInactives != null) lblStatInactives.setText(String.valueOf(inactives));
            if (lblCountBadge != null) lblCountBadge.setText(total + " activité" + (total > 1 ? "s" : ""));

        } catch (SQLException e) {
            showError("Erreur de chargement", "Impossible de charger les activités : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void testAIConnection() {
        new Thread(() -> {
            try {
                boolean isConnected = AIServiceActivites.testConnection();
                Platform.runLater(() -> {
                    if (isConnected) {
                        System.out.println("✅ API IA connectée avec succès");
                    } else {
                        System.out.println("⚠️ API IA non disponible (mode dégradé)");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() ->
                        System.out.println("⚠️ API IA non disponible - utilisation manuelle uniquement"));
            }
        }).start();
    }

    private void setupAutoDescriptionGeneration(TextField nomField, ComboBox<String> difficulteCombo,
                                                TextField lieuField, TextArea descriptionField) {

        final boolean[] isGenerating = {false};

        lieuField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal && !isGenerating[0]) {
                if (!nomField.getText().trim().isEmpty() &&
                        difficulteCombo.getValue() != null &&
                        !lieuField.getText().trim().isEmpty() &&
                        descriptionField.getText().trim().isEmpty()) {

                    isGenerating[0] = true;
                    generateAttractiveDescription(nomField, difficulteCombo, lieuField, descriptionField, isGenerating);
                }
            }
        });

        lieuField.setOnAction(event -> {
            if (!isGenerating[0]) {
                if (!nomField.getText().trim().isEmpty() &&
                        difficulteCombo.getValue() != null &&
                        !lieuField.getText().trim().isEmpty() &&
                        descriptionField.getText().trim().isEmpty()) {

                    isGenerating[0] = true;
                    generateAttractiveDescription(nomField, difficulteCombo, lieuField, descriptionField, isGenerating);
                }
            }
        });
    }

    private void generateAttractiveDescription(TextField nomField, ComboBox<String> difficulteCombo,
                                               TextField lieuField, TextArea descriptionField,
                                               boolean[] isGenerating) {

        String nom = nomField.getText().trim();
        String difficulte = difficulteCombo.getValue();
        String lieu = lieuField.getText().trim();

        descriptionField.setDisable(true);
        descriptionField.setPromptText("🤖 Génération d'une description attrayante...");

        new Thread(() -> {
            try {
                String description = AIServiceActivites.genererDescriptionAttrayante(nom, difficulte, lieu);

                Platform.runLater(() -> {
                    descriptionField.setText(description);
                    descriptionField.setDisable(false);
                    descriptionField.setPromptText("Description (min " + DESCRIPTION_MIN_LENGTH + " caractères)");
                    isGenerating[0] = false;

                    descriptionField.setStyle("-fx-border-color: #34d399; -fx-border-width: 2; -fx-border-radius: 8;");
                    new Thread(() -> {
                        try { Thread.sleep(1000); } catch (InterruptedException e) {}
                        Platform.runLater(() ->
                                descriptionField.setStyle("-fx-background-color: #1e2749; -fx-text-fill: white; -fx-border-color: #2d3a5f; -fx-border-radius: 8; -fx-padding: 8; -fx-font-size: 13; -fx-control-inner-background: #1e2749;")
                        );
                    }).start();
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    String fallbackDescription = getFallbackAttractiveDescription(nom, difficulte, lieu);
                    descriptionField.setText(fallbackDescription);
                    descriptionField.setDisable(false);
                    descriptionField.setPromptText("Description (min " + DESCRIPTION_MIN_LENGTH + " caractères)");
                    isGenerating[0] = false;

                    descriptionField.setStyle("-fx-border-color: #f59e0b; -fx-border-width: 2; -fx-border-radius: 8;");
                });
                e.printStackTrace();
            }
        }).start();
    }

    private String getFallbackAttractiveDescription(String nom, String difficulte, String lieu) {
        String[] templates = {
                "✨ Préparez-vous pour une aventure inoubliable ! %s vous attend à %s. Une activité de niveau %s qui vous fera vibrer et créer des souvenirs magiques !",
                "🌟 Vivez une expérience unique avec %s à %s ! Que vous soyez débutant ou expert (niveau %s), cette activité saura vous séduire par son authenticité.",
                "🎉 Découvrez %s comme vous ne l'avez jamais vu ! À %s, cette activité de niveau %s vous promet des moments de pur bonheur et d'émerveillement.",
                "🔥 L'aventure vous appelle ! %s à %s est l'activité parfaite pour les amateurs de sensations (niveau %s). Venez vivre des moments intenses !",
                "💫 Laissez-vous tenter par %s à %s ! Une expérience de niveau %s qui ravira petits et grands. Préparez-vous à être émerveillés !",
                "⭐ Une activité exceptionnelle vous attend : %s à %s ! Niveau %s, cette expérience unique restera gravée dans vos mémoires.",
                "🎯 Envie de découverte ? %s à %s est fait pour vous ! Activité de niveau %s, idéale pour les passionnés d'aventure.",
                "🌈 Plongez dans l'univers magique de %s à %s ! Une activité de niveau %s qui éveillera tous vos sens."
        };

        java.util.Random rand = new java.util.Random();
        String template = templates[rand.nextInt(templates.length)];

        return String.format(template, nom, lieu, difficulte.toLowerCase());
    }

    private Button createAIGenerateButton(TextField nomField, ComboBox<String> difficulteCombo,
                                          TextField lieuField, TextArea descriptionField) {
        Button generateBtn = new Button("🤖 Générer description avec IA");
        generateBtn.setStyle("-fx-background-color: #6366f1; -fx-text-fill: white; -fx-font-weight: 600; " +
                "-fx-background-radius: 10; -fx-padding: 10 20; -fx-cursor: hand; -fx-font-size: 12;");

        final boolean[] isGenerating = {false};

        generateBtn.setOnAction(e -> {
            if (nomField.getText().trim().isEmpty() ||
                    difficulteCombo.getValue() == null ||
                    lieuField.getText().trim().isEmpty()) {
                showWarning("Champs manquants",
                        "Veuillez remplir le nom, la difficulté et le lieu d'abord.");
                return;
            }

            if (!isGenerating[0]) {
                isGenerating[0] = true;
                generateAttractiveDescription(nomField, difficulteCombo, lieuField, descriptionField, isGenerating);
            }
        });

        return generateBtn;
    }

    private Label createErrorLabel() {
        Label label = new Label();
        label.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11; -fx-wrap-text: true;");
        label.setPrefWidth(300);
        return label;
    }

    private boolean validateNom(TextField champ, Label errorLabel) {
        String texte = champ.getText();
        champ.setStyle("-fx-background-color: #1e2749; -fx-text-fill: white; -fx-border-color: #2d3a5f; -fx-border-radius: 8; -fx-padding: 8;");
        errorLabel.setText("");

        if (texte == null || texte.trim().isEmpty()) {
            champ.setStyle("-fx-background-color: #1e2749; -fx-text-fill: white; -fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 8; -fx-padding: 8;");
            errorLabel.setText("Nom : Ce champ est obligatoire");
            return false;
        }

        String trimmed = texte.trim();
        if (trimmed.length() < NOM_MIN_LENGTH) {
            champ.setStyle("-fx-background-color: #1e2749; -fx-text-fill: white; -fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 8; -fx-padding: 8;");
            errorLabel.setText("Nom : doit contenir au moins " + NOM_MIN_LENGTH + " caractères");
            return false;
        }

        if (trimmed.length() > NOM_MAX_LENGTH) {
            champ.setStyle("-fx-background-color: #1e2749; -fx-text-fill: white; -fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 8; -fx-padding: 8;");
            errorLabel.setText("Nom : ne doit pas dépasser " + NOM_MAX_LENGTH + " caractères");
            return false;
        }

        if (!NOM_PATTERN.matcher(trimmed).matches()) {
            champ.setStyle("-fx-background-color: #1e2749; -fx-text-fill: white; -fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 8; -fx-padding: 8;");
            errorLabel.setText("Nom : ne doit contenir que des lettres, espaces et tirets");
            return false;
        }

        return true;
    }

    private boolean validateDescription(TextArea champ, Label errorLabel) {
        String texte = champ.getText();
        champ.setStyle("-fx-background-color: #1e2749; -fx-text-fill: white; -fx-border-color: #2d3a5f; -fx-border-radius: 8; -fx-padding: 8; -fx-control-inner-background: #1e2749;");
        errorLabel.setText("");

        if (texte == null || texte.trim().isEmpty()) {
            champ.setStyle("-fx-background-color: #1e2749; -fx-text-fill: white; -fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 8; -fx-padding: 8; -fx-control-inner-background: #1e2749;");
            errorLabel.setText("Description : Ce champ est obligatoire");
            return false;
        }

        String trimmed = texte.trim();
        if (trimmed.length() < DESCRIPTION_MIN_LENGTH) {
            champ.setStyle("-fx-background-color: #1e2749; -fx-text-fill: white; -fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 8; -fx-padding: 8; -fx-control-inner-background: #1e2749;");
            errorLabel.setText("Description : doit contenir au moins " + DESCRIPTION_MIN_LENGTH + " caractères");
            return false;
        }

        if (trimmed.length() > DESCRIPTION_MAX_LENGTH) {
            champ.setStyle("-fx-background-color: #1e2749; -fx-text-fill: white; -fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 8; -fx-padding: 8; -fx-control-inner-background: #1e2749;");
            errorLabel.setText("Description : ne doit pas dépasser " + DESCRIPTION_MAX_LENGTH + " caractères");
            return false;
        }

        return true;
    }

    private boolean validateBudget(TextField champ, Label errorLabel) {
        String texte = champ.getText();
        champ.setStyle("-fx-background-color: #1e2749; -fx-text-fill: white; -fx-border-color: #2d3a5f; -fx-border-radius: 8; -fx-padding: 8;");
        errorLabel.setText("");

        if (texte == null || texte.trim().isEmpty()) {
            champ.setStyle("-fx-background-color: #1e2749; -fx-text-fill: white; -fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 8; -fx-padding: 8;");
            errorLabel.setText("Budget : Ce champ est obligatoire");
            return false;
        }

        try {
            int valeur = Integer.parseInt(texte.trim());
            if (valeur < BUDGET_MIN) {
                champ.setStyle("-fx-background-color: #1e2749; -fx-text-fill: white; -fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 8; -fx-padding: 8;");
                errorLabel.setText("Budget : doit être au moins " + BUDGET_MIN + " €");
                return false;
            }
            if (valeur > BUDGET_MAX) {
                champ.setStyle("-fx-background-color: #1e2749; -fx-text-fill: white; -fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 8; -fx-padding: 8;");
                errorLabel.setText("Budget : ne peut pas dépasser " + BUDGET_MAX + " €");
                return false;
            }
        } catch (NumberFormatException e) {
            champ.setStyle("-fx-background-color: #1e2749; -fx-text-fill: white; -fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 8; -fx-padding: 8;");
            errorLabel.setText("Budget : veuillez entrer un nombre valide");
            return false;
        }

        return true;
    }

    private boolean validateDuree(TextField champ, Label errorLabel) {
        String texte = champ.getText();
        champ.setStyle("-fx-background-color: #1e2749; -fx-text-fill: white; -fx-border-color: #2d3a5f; -fx-border-radius: 8; -fx-padding: 8;");
        errorLabel.setText("");

        if (texte == null || texte.trim().isEmpty()) {
            champ.setStyle("-fx-background-color: #1e2749; -fx-text-fill: white; -fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 8; -fx-padding: 8;");
            errorLabel.setText("Durée : Ce champ est obligatoire");
            return false;
        }

        try {
            int valeur = Integer.parseInt(texte.trim());
            if (valeur < DUREE_MIN) {
                champ.setStyle("-fx-background-color: #1e2749; -fx-text-fill: white; -fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 8; -fx-padding: 8;");
                errorLabel.setText("Durée : doit être au moins " + DUREE_MIN + " heure");
                return false;
            }
            if (valeur > DUREE_MAX) {
                champ.setStyle("-fx-background-color: #1e2749; -fx-text-fill: white; -fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 8; -fx-padding: 8;");
                errorLabel.setText("Durée : ne peut pas dépasser " + DUREE_MAX + " heures (1 an)");
                return false;
            }
        } catch (NumberFormatException e) {
            champ.setStyle("-fx-background-color: #1e2749; -fx-text-fill: white; -fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 8; -fx-padding: 8;");
            errorLabel.setText("Durée : veuillez entrer un nombre valide");
            return false;
        }

        return true;
    }

    private boolean validateDifficulte(ComboBox<String> combo, Label errorLabel) {
        combo.setStyle("-fx-background-color: #1e2749; -fx-text-fill: white; -fx-border-color: #2d3a5f; -fx-border-radius: 8; -fx-padding: 5;");
        errorLabel.setText("");

        if (combo.getValue() == null) {
            combo.setStyle("-fx-background-color: #1e2749; -fx-text-fill: white; -fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 8; -fx-padding: 5;");
            errorLabel.setText("Difficulté : veuillez faire une sélection");
            return false;
        }

        return true;
    }

    private boolean validateAgeMin(TextField champ, Label errorLabel) {
        String texte = champ.getText();
        champ.setStyle("-fx-background-color: #1e2749; -fx-text-fill: white; -fx-border-color: #2d3a5f; -fx-border-radius: 8; -fx-padding: 8;");
        errorLabel.setText("");

        if (texte == null || texte.trim().isEmpty()) {
            champ.setStyle("-fx-background-color: #1e2749; -fx-text-fill: white; -fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 8; -fx-padding: 8;");
            errorLabel.setText("Âge minimum : Ce champ est obligatoire");
            return false;
        }

        try {
            int valeur = Integer.parseInt(texte.trim());
            if (valeur < AGE_MIN) {
                champ.setStyle("-fx-background-color: #1e2749; -fx-text-fill: white; -fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 8; -fx-padding: 8;");
                errorLabel.setText("Âge minimum : doit être supérieur ou égal à " + AGE_MIN);
                return false;
            }
            if (valeur > AGE_MAX) {
                champ.setStyle("-fx-background-color: #1e2749; -fx-text-fill: white; -fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 8; -fx-padding: 8;");
                errorLabel.setText("Âge minimum : doit être inférieur ou égal à " + AGE_MAX + " ans");
                return false;
            }
        } catch (NumberFormatException e) {
            champ.setStyle("-fx-background-color: #1e2749; -fx-text-fill: white; -fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 8; -fx-padding: 8;");
            errorLabel.setText("Âge minimum : veuillez entrer un nombre valide");
            return false;
        }

        return true;
    }

    private VBox createImageSection(ImageView imageView, Button chooseButton, Label imageNameLabel, String currentImage) {
        VBox imageSection = new VBox(10);
        imageSection.setStyle("-fx-padding: 15; -fx-background-color: #1e2749; -fx-background-radius: 10; -fx-border-color: #2d3a5f; -fx-border-width: 1; -fx-border-radius: 10;");

        Label imageLabel = new Label("Image de l'activité :");
        imageLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-font-size: 13;");

        imageView.setFitWidth(200);
        imageView.setFitHeight(150);
        imageView.setPreserveRatio(true);
        imageView.setStyle("-fx-background-color: #0a0e27; -fx-background-radius: 8; -fx-padding: 5;");

        if (currentImage != null && !currentImage.isEmpty()) {
            try {
                File file = new File(currentImage);
                if (file.exists()) {
                    Image image = new Image(file.toURI().toString());
                    imageView.setImage(image);
                    imageNameLabel.setText("Image actuelle : " + file.getName());
                } else {
                    imageNameLabel.setText("Aucune image");
                }
            } catch (Exception e) {
                imageNameLabel.setText("Aucune image");
            }
        } else {
            imageNameLabel.setText("Aucune image sélectionnée");
        }

        chooseButton.setText("📁 Choisir une image");
        chooseButton.setStyle("-fx-background-color: #ff8c42; -fx-text-fill: white; -fx-font-weight: 600; -fx-background-radius: 10; -fx-padding: 8 20; -fx-cursor: hand;");

        imageNameLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11; -fx-wrap-text: true;");

        HBox buttonBox = new HBox(10, chooseButton);
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        imageSection.getChildren().addAll(imageLabel, imageView, imageNameLabel, buttonBox);

        return imageSection;
    }

    private void handleChooseImage(ImageView imageView, Label imageNameLabel) {
        File file = FileManager.chooseImage(imageView.getScene().getWindow());
        if (file != null) {
            selectedImageFile = file;
            try {
                Image image = new Image(file.toURI().toString());
                imageView.setImage(image);
                imageNameLabel.setText("Image sélectionnée : " + file.getName());
            } catch (Exception e) {
                showError("Erreur", "Impossible de charger l'image : " + e.getMessage());
            }
        }
    }

    // ==================== MÉTHODES UTILITAIRES POUR LE FORMULAIRE ====================

    private VBox createSection(String title) {
        VBox section = new VBox(15);
        section.setPadding(new Insets(20));
        section.setStyle("-fx-background-color: #111633; -fx-background-radius: 15; -fx-border-color: #2d3a5f; -fx-border-width: 1; -fx-border-radius: 15;");
        section.setPrefWidth(800);

        Label sectionTitle = new Label(title);
        sectionTitle.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #ff8c42; -fx-letter-spacing: 0.5;");

        section.getChildren().add(sectionTitle);
        return section;
    }

    private TextField createStyledTextField(String prompt, String tooltip) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setStyle("-fx-background-color: #1e2749; -fx-text-fill: white; -fx-prompt-text-fill: #94a3b8; -fx-border-color: #2d3a5f; -fx-border-radius: 8; -fx-padding: 10; -fx-font-size: 13;");
        field.setPrefHeight(40);
        field.setMaxWidth(600);
        field.setTooltip(new Tooltip(tooltip));
        return field;
    }

    private ComboBox<String> createStyledComboBox() {
        ComboBox<String> combo = new ComboBox<>();
        combo.setStyle("-fx-background-color: #1e2749; -fx-text-fill: white; -fx-border-color: #2d3a5f; -fx-border-radius: 8; -fx-padding: 5; -fx-font-size: 13;");
        combo.setPrefHeight(40);
        combo.setMaxWidth(600);

        // Style pour que le texte sélectionné soit blanc
        combo.setButtonCell(new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: white; -fx-background-color: #1e2749;");
                }
            }
        });

        // Style pour les éléments de la liste déroulante
        combo.setCellFactory(param -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: white; -fx-background-color: #1e2749;");
                }
            }
        });

        return combo;
    }

    private ComboBox<Categories> createStyledCategoryComboBox() {
        ComboBox<Categories> combo = new ComboBox<>();
        combo.setStyle("-fx-background-color: #1e2749; -fx-text-fill: white; -fx-border-color: #2d3a5f; -fx-border-radius: 8; -fx-padding: 5; -fx-font-size: 13;");
        combo.setPrefHeight(40);
        combo.setMaxWidth(600);

        // Style pour que le texte sélectionné soit blanc
        combo.setButtonCell(new ListCell<Categories>() {
            @Override
            protected void updateItem(Categories item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getNom());
                    setStyle("-fx-text-fill: white; -fx-background-color: #1e2749;");
                }
            }
        });

        // Style pour les éléments de la liste déroulante
        combo.setCellFactory(param -> new ListCell<Categories>() {
            @Override
            protected void updateItem(Categories item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getNom() + " (" + item.getType() + ")");
                    setStyle("-fx-text-fill: white; -fx-background-color: #1e2749;");
                }
            }
        });

        return combo;
    }

    private TextArea createStyledTextArea() {
        TextArea area = new TextArea();
        area.setPromptText("Description détaillée de l'activité...");
        area.setPrefRowCount(5);
        area.setPrefWidth(600);
        area.setMaxWidth(600);
        area.setWrapText(true);
        area.setStyle("-fx-background-color: #1e2749; -fx-text-fill: white; -fx-prompt-text-fill: #94a3b8; -fx-border-color: #2d3a5f; -fx-border-radius: 8; -fx-padding: 10; -fx-font-size: 13; -fx-control-inner-background: #1e2749;");
        return area;
    }

    private DatePicker createStyledDatePicker() {
        DatePicker picker = new DatePicker();
        picker.setValue(LocalDate.now().plusDays(7));
        picker.setStyle("-fx-background-color: #1e2749; -fx-text-fill: white; -fx-border-color: #2d3a5f; -fx-border-radius: 8; -fx-padding: 8; -fx-font-size: 13;");
        picker.setPrefHeight(40);
        picker.setMaxWidth(600);
        return picker;
    }

    private Button createStyledAIButton() {
        Button btn = new Button("🤖 Générer avec IA");
        btn.setStyle("-fx-background-color: #6366f1; -fx-text-fill: white; -fx-font-weight: 600; -fx-background-radius: 10; -fx-padding: 12 25; -fx-cursor: hand; -fx-font-size: 13; -fx-effect: dropshadow(gaussian, rgba(99,102,241,0.3), 5, 0, 0, 2);");
        btn.setPrefHeight(40);
        return btn;
    }

    private VBox createFormField(String labelText, Node field, Node errorLabel) {
        VBox container = new VBox(5);
        container.setPadding(new Insets(0, 0, 10, 0));
        container.setMaxWidth(600);

        Label label = new Label(labelText);
        label.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 13; -fx-font-weight: 500;");

        container.getChildren().addAll(label, field);
        if (errorLabel != null) {
            container.getChildren().add(errorLabel);
        }

        return container;
    }

    @FXML
    private void handleAjouter() {
        Dialog<Activites> dialog = new Dialog<>();
        dialog.setTitle("Nouvelle Activité");
        dialog.setHeaderText(null);

        // Style personnalisé pour le dialogue
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #0a0e27;");
        dialogPane.setPrefWidth(900);
        dialogPane.setPrefHeight(800);

        // Création du conteneur principal avec padding
        VBox mainContainer = new VBox(25);
        mainContainer.setPadding(new Insets(30));
        mainContainer.setStyle("-fx-background-color: #0a0e27;");
        mainContainer.setAlignment(Pos.TOP_CENTER);

        // === TITRE DU FORMULAIRE ===
        HBox titleBox = new HBox(15);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        titleBox.setPadding(new Insets(0, 0, 10, 0));
        titleBox.setMaxWidth(800);

        Label iconTitle = new Label("✨");
        iconTitle.setStyle("-fx-font-size: 32;");

        Label textTitle = new Label("Créer une nouvelle activité");
        textTitle.setStyle("-fx-font-family: 'Clash Display'; -fx-font-size: 26; -fx-font-weight: bold; -fx-text-fill: white;");

        Label badgeTitle = new Label("NOUVEAU");
        badgeTitle.setStyle("-fx-background-color: #ff8c42; -fx-text-fill: white; -fx-background-radius: 12; -fx-padding: 4 12; -fx-font-size: 11; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label infoTitle = new Label("Tous les champs * sont obligatoires");
        infoTitle.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11;");

        titleBox.getChildren().addAll(iconTitle, textTitle, badgeTitle, spacer, infoTitle);

        // Conteneur centré pour le titre
        VBox titleContainer = new VBox(titleBox);
        titleContainer.setAlignment(Pos.CENTER);
        mainContainer.getChildren().add(titleContainer);

        // === SECTION 1: INFORMATIONS PRINCIPALES ===
        VBox section1 = createSection("📋 INFORMATIONS PRINCIPALES");
        section1.setAlignment(Pos.CENTER);

        // Conteneur pour aligner les champs à gauche dans la section centrée
        VBox fieldsContainer1 = new VBox(15);
        fieldsContainer1.setPadding(new Insets(10, 0, 0, 0));
        fieldsContainer1.setMaxWidth(600);
        fieldsContainer1.setAlignment(Pos.CENTER_LEFT);

        // Champs du formulaire
        TextField nomField = createStyledTextField("Ex: Randonnée en montagne", "Nom de l'activité");

        ComboBox<String> difficulteCombo = createStyledComboBox();
        difficulteCombo.getItems().addAll("Facile", "Moyen", "Difficile", "Expert");
        difficulteCombo.setPromptText("Sélectionnez un niveau");

        // CHAMP LIEU AMÉLIORÉ
        HBox lieuBox = new HBox(10);
        TextField lieuField = createStyledTextField("Choisissez sur la carte", "Lieu");
        lieuField.setEditable(false);
        lieuField.setPrefWidth(450);
        lieuField.setMaxWidth(450);

        Button btnChoisirLieu = new Button("🗺️ Choisir");
        btnChoisirLieu.setStyle("-fx-background-color: #ff8c42; -fx-text-fill: white; -fx-font-weight: 600; -fx-background-radius: 10; -fx-padding: 8 20; -fx-cursor: hand; -fx-font-size: 12;");
        btnChoisirLieu.setPrefHeight(40);

        // Variables pour stocker les coordonnées
        final double[] selectedLat = {0};
        final double[] selectedLon = {0};
        final String[] selectedAddress = {""};

        btnChoisirLieu.setOnAction(e -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/locationpicker.fxml"));
                Parent root = loader.load();

                LocationPickerController controller = loader.getController();
                controller.setOnLocationSelected(() -> {
                    LocationPickerController.LocationResult result = controller.getSelectedLocation();
                    if (result != null) {
                        String formattedAddress = result.getFormattedAddress();
                        lieuField.setText(formattedAddress);
                        selectedLat[0] = result.getLatitude();
                        selectedLon[0] = result.getLongitude();
                        selectedAddress[0] = formattedAddress;
                        lieuField.setStyle("-fx-background-color: #2d3a5f; -fx-text-fill: #34d399; -fx-font-weight: bold; -fx-border-color: #34d399; -fx-border-radius: 8; -fx-padding: 8;");
                    }
                });

                Scene scene = new Scene(root);
                Stage stage = new Stage();
                stage.setTitle("Choisir la localisation");
                stage.setScene(scene);
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.show();

            } catch (IOException ex) {
                showError("Erreur", "Impossible d'ouvrir le sélecteur de carte : " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        lieuBox.getChildren().addAll(lieuField, btnChoisirLieu);

        // Labels d'erreur
        Label errorNom = createErrorLabel();
        Label errorDifficulte = createErrorLabel();
        Label errorLieu = createErrorLabel();

        // Ajout des champs au conteneur (un en dessous de l'autre)
        fieldsContainer1.getChildren().addAll(
                createFormField("Nom *", nomField, errorNom),
                createFormField("Difficulté *", difficulteCombo, errorDifficulte),
                createFormField("Lieu *", lieuBox, errorLieu)
        );

        section1.getChildren().add(fieldsContainer1);

        // Conteneur centré pour la section
        VBox sectionContainer1 = new VBox(section1);
        sectionContainer1.setAlignment(Pos.CENTER);
        mainContainer.getChildren().add(sectionContainer1);

        // === SECTION 2: DESCRIPTION AVEC IA ===
        VBox section2 = createSection("🤖 DESCRIPTION INTELLIGENTE");
        section2.setAlignment(Pos.CENTER);

        VBox fieldsContainer2 = new VBox(15);
        fieldsContainer2.setPadding(new Insets(10, 0, 0, 0));
        fieldsContainer2.setMaxWidth(600);
        fieldsContainer2.setAlignment(Pos.CENTER_LEFT);

        TextArea descriptionField = createStyledTextArea();

        // Bouton IA à côté de la description
        HBox descriptionButtonBox = new HBox(15);
        descriptionButtonBox.setAlignment(Pos.CENTER_LEFT);

        Button generateAIBtn = createStyledAIButton();
        Label aiHint = new Label("✨ Auto-génération quand les 3 champs sont remplis");
        aiHint.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11; -fx-font-style: italic;");

        descriptionButtonBox.getChildren().addAll(generateAIBtn, aiHint);

        Label errorDescription = createErrorLabel();

        fieldsContainer2.getChildren().addAll(
                createFormField("Description *", descriptionField, errorDescription),
                descriptionButtonBox
        );

        section2.getChildren().add(fieldsContainer2);

        VBox sectionContainer2 = new VBox(section2);
        sectionContainer2.setAlignment(Pos.CENTER);
        mainContainer.getChildren().add(sectionContainer2);

        // === SECTION 3: DÉTAILS & BUDGET ===
        VBox section3 = createSection("💰 DÉTAILS & BUDGET");
        section3.setAlignment(Pos.CENTER);

        VBox fieldsContainer3 = new VBox(15);
        fieldsContainer3.setPadding(new Insets(10, 0, 0, 0));
        fieldsContainer3.setMaxWidth(600);
        fieldsContainer3.setAlignment(Pos.CENTER_LEFT);

        TextField budgetField = createStyledTextField("Ex: 150", "Budget en €");
        TextField dureeField = createStyledTextField("Ex: 3", "Durée en heures");
        TextField ageMinField = createStyledTextField("Ex: 12", "Âge minimum");
        DatePicker datePicker = createStyledDatePicker();
        ComboBox<String> statutCombo = createStyledComboBox();
        statutCombo.getItems().addAll("Active", "Inactive", "En attente");
        statutCombo.setValue("Active");
        statutCombo.setPromptText("Sélectionnez un statut");

        ComboBox<Categories> categorieCombo = createStyledCategoryComboBox();

        // Charger les catégories
        try {
            CategoriesCRUD categoriesCRUD = new CategoriesCRUD();
            List<Categories> categoriesList = categoriesCRUD.afficher();
            ObservableList<Categories> categoriesObservable = FXCollections.observableArrayList(categoriesList);
            categorieCombo.setItems(categoriesObservable);
            categorieCombo.setPromptText("Sélectionner une catégorie");

        } catch (SQLException e) {
            showError("Erreur", "Impossible de charger les catégories : " + e.getMessage());
        }

        Label errorBudget = createErrorLabel();
        Label errorDuree = createErrorLabel();
        Label errorAgeMin = createErrorLabel();
        Label errorDate = createErrorLabel();

        fieldsContainer3.getChildren().addAll(
                createFormField("Budget (€) *", budgetField, errorBudget),
                createFormField("Durée (h) *", dureeField, errorDuree),
                createFormField("Âge minimum *", ageMinField, errorAgeMin),
                createFormField("Date prévue *", datePicker, errorDate),
                createFormField("Statut *", statutCombo, null),
                createFormField("Catégorie", categorieCombo, null)
        );

        section3.getChildren().add(fieldsContainer3);

        VBox sectionContainer3 = new VBox(section3);
        sectionContainer3.setAlignment(Pos.CENTER);
        mainContainer.getChildren().add(sectionContainer3);

        // === SECTION 4: IMAGE ===
        VBox section4 = createSection("🖼️ IMAGE DE L'ACTIVITÉ");
        section4.setAlignment(Pos.CENTER);

        VBox fieldsContainer4 = new VBox(15);
        fieldsContainer4.setPadding(new Insets(10, 0, 0, 0));
        fieldsContainer4.setMaxWidth(600);
        fieldsContainer4.setAlignment(Pos.CENTER_LEFT);

        ImageView imagePreview = new ImageView();
        Button chooseImageButton = new Button("📁 Choisir une image");
        chooseImageButton.setStyle("-fx-background-color: #ff8c42; -fx-text-fill: white; -fx-font-weight: 600; -fx-background-radius: 10; -fx-padding: 10 25; -fx-cursor: hand; -fx-font-size: 13;");

        Label imageNameLabel = new Label("Aucune image sélectionnée");
        imageNameLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11;");

        VBox imageSection = createImageSection(imagePreview, chooseImageButton, imageNameLabel, null);
        chooseImageButton.setOnAction(e -> handleChooseImage(imagePreview, imageNameLabel));

        fieldsContainer4.getChildren().add(imageSection);
        section4.getChildren().add(fieldsContainer4);

        VBox sectionContainer4 = new VBox(section4);
        sectionContainer4.setAlignment(Pos.CENTER);
        mainContainer.getChildren().add(sectionContainer4);

        // === CONFIGURATION IA ===
        setupAutoDescriptionGeneration(nomField, difficulteCombo, lieuField, descriptionField);
        generateAIBtn.setOnAction(e -> {
            if (nomField.getText().trim().isEmpty() || difficulteCombo.getValue() == null || lieuField.getText().trim().isEmpty()) {
                showWarning("Champs manquants", "Veuillez remplir le nom, la difficulté et le lieu d'abord.");
                return;
            }
            final boolean[] isGenerating = {false};
            isGenerating[0] = true;
            generateAttractiveDescription(nomField, difficulteCombo, lieuField, descriptionField, isGenerating);
        });

        // === BOUTONS D'ACTION ===
        HBox buttonBar = new HBox(15);
        buttonBar.setAlignment(Pos.CENTER_RIGHT);
        buttonBar.setPadding(new Insets(20, 0, 0, 0));
        buttonBar.setMaxWidth(800);

        Button cancelButton = new Button("Annuler");
        cancelButton.setStyle("-fx-background-color: #1e2749; -fx-text-fill: #94a3b8; -fx-font-weight: 600; -fx-background-radius: 10; -fx-padding: 12 30; -fx-cursor: hand; -fx-border-color: #2d3a5f; -fx-border-width: 1; -fx-border-radius: 10; -fx-font-size: 14;");
        cancelButton.setOnAction(e -> dialog.close());

        Button saveButton = new Button("✅ Créer l'activité");
        saveButton.setStyle("-fx-background-color: #ff8c42; -fx-text-fill: white; -fx-font-weight: 600; -fx-background-radius: 10; -fx-padding: 12 30; -fx-cursor: hand; -fx-font-size: 14; -fx-effect: dropshadow(gaussian, rgba(255,140,66,0.3), 10, 0, 0, 2);");

        Region buttonSpacer = new Region();
        HBox.setHgrow(buttonSpacer, Priority.ALWAYS);
        buttonBar.getChildren().addAll(buttonSpacer, cancelButton, saveButton);

        // Conteneur centré pour les boutons
        VBox buttonContainer = new VBox(buttonBar);
        buttonContainer.setAlignment(Pos.CENTER);
        mainContainer.getChildren().add(buttonContainer);

        // ScrollPane pour le contenu
        ScrollPane scrollPane = new ScrollPane(mainContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #0a0e27; -fx-background-color: #0a0e27; -fx-border-color: #ff8c42; -fx-border-width: 1; -fx-border-radius: 10;");

        dialogPane.setContent(scrollPane);
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Cacher les boutons par défaut
        Node okButtonNode = dialogPane.lookupButton(ButtonType.OK);
        Node cancelButtonNode = dialogPane.lookupButton(ButtonType.CANCEL);
        if (okButtonNode != null) {
            okButtonNode.setVisible(false);
            okButtonNode.setManaged(false);
        }
        if (cancelButtonNode != null) {
            cancelButtonNode.setVisible(false);
            cancelButtonNode.setManaged(false);
        }

        // Validation avec le bouton personnalisé
        saveButton.setOnAction(event -> {
            boolean isDateValid = datePicker.getValue() != null;
            if (!isDateValid) {
                datePicker.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 8;");
                errorDate.setText("Date : Ce champ est obligatoire");
            } else {
                datePicker.setStyle("-fx-background-color: #1e2749; -fx-text-fill: white; -fx-border-color: #2d3a5f; -fx-border-radius: 8; -fx-padding: 8;");
                errorDate.setText("");
            }

            boolean isLieuValid = !lieuField.getText().trim().isEmpty();
            if (!isLieuValid) {
                lieuField.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 8; -fx-padding: 8;");
                errorLieu.setText("Lieu : Veuillez sélectionner un lieu sur la carte");
            } else {
                lieuField.setStyle("-fx-background-color: #2d3a5f; -fx-text-fill: white; -fx-border-color: #2d3a5f; -fx-border-radius: 8; -fx-padding: 8;");
                errorLieu.setText("");
            }

            boolean isValid = validateNom(nomField, errorNom)
                    && validateDifficulte(difficulteCombo, errorDifficulte)
                    && isLieuValid
                    && validateDescription(descriptionField, errorDescription)
                    && validateBudget(budgetField, errorBudget)
                    && validateDuree(dureeField, errorDuree)
                    && validateAgeMin(ageMinField, errorAgeMin)
                    && isDateValid;

            if (isValid) {
                try {
                    Activites activite = new Activites();
                    activite.setNom(nomField.getText().trim());
                    activite.setDescription(descriptionField.getText().trim());
                    activite.setBudget(Integer.parseInt(budgetField.getText().trim()));
                    activite.setDuree(Integer.parseInt(dureeField.getText().trim()));
                    activite.setNiveaudifficulte(difficulteCombo.getValue());
                    activite.setLieu(lieuField.getText().trim());
                    activite.setAgemin(Integer.parseInt(ageMinField.getText().trim()));
                    activite.setStatut(statutCombo.getValue());
                    activite.setDatePrevue(datePicker.getValue());

                    Categories selectedCategorie = categorieCombo.getValue();
                    activite.setCategorieId(selectedCategorie != null ? selectedCategorie.getId() : 0);

                    if (selectedImageFile != null) {
                        String imagePath = FileManager.saveImage(selectedImageFile);
                        activite.setImagePath(imagePath);
                    }

                    activitesCRUD.ajouter(activite);
                    showInfo("Succès", "✅ Activité ajoutée avec succès !");
                    loadActivites();
                    dialog.close();
                } catch (Exception e) {
                    showError("Erreur", "Impossible d'ajouter l'activité : " + e.getMessage());
                }
            }
        });

        dialog.showAndWait();
    }

    @FXML
    private void handleModifier() {
        Activites selectedActivite = tableActivites.getSelectionModel().getSelectedItem();

        if (selectedActivite == null) {
            showWarning("Aucune sélection", "Veuillez sélectionner une activité à modifier.");
            return;
        }

        selectedImageFile = null;
        currentImagePath = selectedActivite.getImagePath();

        Dialog<Activites> dialog = new Dialog<>();
        dialog.setTitle("Modifier Activité");
        dialog.setHeaderText("Modifier l'activité: " + selectedActivite.getNom());

        // Style personnalisé pour le dialogue
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #0a0e27;");
        dialogPane.setPrefWidth(900);
        dialogPane.setPrefHeight(800);

        // Création du conteneur principal avec padding
        VBox mainContainer = new VBox(25);
        mainContainer.setPadding(new Insets(30));
        mainContainer.setStyle("-fx-background-color: #0a0e27;");
        mainContainer.setAlignment(Pos.TOP_CENTER);

        // === TITRE DU FORMULAIRE ===
        HBox titleBox = new HBox(15);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        titleBox.setPadding(new Insets(0, 0, 10, 0));
        titleBox.setMaxWidth(800);

        Label iconTitle = new Label("✏️");
        iconTitle.setStyle("-fx-font-size: 32;");

        Label textTitle = new Label("Modifier l'activité");
        textTitle.setStyle("-fx-font-family: 'Clash Display'; -fx-font-size: 26; -fx-font-weight: bold; -fx-text-fill: white;");

        Label badgeTitle = new Label("MODIFICATION");
        badgeTitle.setStyle("-fx-background-color: #6366f1; -fx-text-fill: white; -fx-background-radius: 12; -fx-padding: 4 12; -fx-font-size: 11; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label infoTitle = new Label("Tous les champs * sont obligatoires");
        infoTitle.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11;");

        titleBox.getChildren().addAll(iconTitle, textTitle, badgeTitle, spacer, infoTitle);

        VBox titleContainer = new VBox(titleBox);
        titleContainer.setAlignment(Pos.CENTER);
        mainContainer.getChildren().add(titleContainer);

        // === SECTION 1: INFORMATIONS PRINCIPALES ===
        VBox section1 = createSection("📋 INFORMATIONS PRINCIPALES");
        section1.setAlignment(Pos.CENTER);

        VBox fieldsContainer1 = new VBox(15);
        fieldsContainer1.setPadding(new Insets(10, 0, 0, 0));
        fieldsContainer1.setMaxWidth(600);
        fieldsContainer1.setAlignment(Pos.CENTER_LEFT);

        TextField nomField = new TextField(selectedActivite.getNom());
        nomField.setStyle("-fx-background-color: #1e2749; -fx-text-fill: white; -fx-border-color: #2d3a5f; -fx-border-radius: 8; -fx-padding: 10; -fx-font-size: 13;");
        nomField.setPrefHeight(40);
        nomField.setMaxWidth(600);

        ComboBox<String> difficulteCombo = createStyledComboBox();
        difficulteCombo.getItems().addAll("Facile", "Moyen", "Difficile", "Expert");
        difficulteCombo.setValue(selectedActivite.getNiveaudifficulte());

        HBox lieuBox = new HBox(10);
        TextField lieuField = new TextField(selectedActivite.getLieu());
        lieuField.setEditable(false);
        lieuField.setStyle("-fx-background-color: #2d3a5f; -fx-text-fill: white; -fx-font-weight: bold; -fx-border-color: #2d3a5f; -fx-border-radius: 8; -fx-padding: 10; -fx-font-size: 13;");
        lieuField.setPrefWidth(450);
        lieuField.setMaxWidth(450);
        lieuField.setPrefHeight(40);

        Button btnChoisirLieu = new Button("🗺️ Changer");
        btnChoisirLieu.setStyle("-fx-background-color: #ff8c42; -fx-text-fill: white; -fx-font-weight: 600; -fx-background-radius: 10; -fx-padding: 8 20; -fx-cursor: hand; -fx-font-size: 12;");
        btnChoisirLieu.setPrefHeight(40);

        final double[] selectedLat = {0};
        final double[] selectedLon = {0};
        final String[] selectedAddress = {selectedActivite.getLieu()};

        btnChoisirLieu.setOnAction(e -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/locationpicker.fxml"));
                Parent root = loader.load();

                LocationPickerController controller = loader.getController();
                controller.setOnLocationSelected(() -> {
                    LocationPickerController.LocationResult result = controller.getSelectedLocation();
                    if (result != null) {
                        String formattedAddress = result.getFormattedAddress();
                        lieuField.setText(formattedAddress);
                        selectedLat[0] = result.getLatitude();
                        selectedLon[0] = result.getLongitude();
                        selectedAddress[0] = formattedAddress;
                        lieuField.setStyle("-fx-background-color: #2d3a5f; -fx-text-fill: #34d399; -fx-font-weight: bold; -fx-border-color: #34d399; -fx-border-radius: 8; -fx-padding: 10;");
                    }
                });

                Scene scene = new Scene(root);
                Stage stage = new Stage();
                stage.setTitle("Changer la localisation");
                stage.setScene(scene);
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.show();

            } catch (IOException ex) {
                showError("Erreur", "Impossible d'ouvrir le sélecteur de carte : " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        lieuBox.getChildren().addAll(lieuField, btnChoisirLieu);

        Label errorNom = createErrorLabel();
        Label errorDifficulte = createErrorLabel();
        Label errorLieu = createErrorLabel();

        fieldsContainer1.getChildren().addAll(
                createFormField("Nom *", nomField, errorNom),
                createFormField("Difficulté *", difficulteCombo, errorDifficulte),
                createFormField("Lieu *", lieuBox, errorLieu)
        );

        section1.getChildren().add(fieldsContainer1);

        VBox sectionContainer1 = new VBox(section1);
        sectionContainer1.setAlignment(Pos.CENTER);
        mainContainer.getChildren().add(sectionContainer1);

        // === SECTION 2: DESCRIPTION AVEC IA ===
        VBox section2 = createSection("🤖 DESCRIPTION INTELLIGENTE");
        section2.setAlignment(Pos.CENTER);

        VBox fieldsContainer2 = new VBox(15);
        fieldsContainer2.setPadding(new Insets(10, 0, 0, 0));
        fieldsContainer2.setMaxWidth(600);
        fieldsContainer2.setAlignment(Pos.CENTER_LEFT);

        TextArea descriptionField = new TextArea(selectedActivite.getDescription());
        descriptionField.setPrefRowCount(5);
        descriptionField.setPrefWidth(600);
        descriptionField.setMaxWidth(600);
        descriptionField.setWrapText(true);
        descriptionField.setStyle("-fx-background-color: #1e2749; -fx-text-fill: white; -fx-border-color: #2d3a5f; -fx-border-radius: 8; -fx-padding: 10; -fx-font-size: 13; -fx-control-inner-background: #1e2749;");

        HBox descriptionButtonBox = new HBox(15);
        descriptionButtonBox.setAlignment(Pos.CENTER_LEFT);

        Button generateAIBtn = createStyledAIButton();
        Label aiHint = new Label("✨ Auto-génération quand les 3 champs sont remplis");
        aiHint.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11; -fx-font-style: italic;");

        descriptionButtonBox.getChildren().addAll(generateAIBtn, aiHint);

        Label errorDescription = createErrorLabel();

        fieldsContainer2.getChildren().addAll(
                createFormField("Description *", descriptionField, errorDescription),
                descriptionButtonBox
        );

        section2.getChildren().add(fieldsContainer2);

        VBox sectionContainer2 = new VBox(section2);
        sectionContainer2.setAlignment(Pos.CENTER);
        mainContainer.getChildren().add(sectionContainer2);

        // === SECTION 3: DÉTAILS & BUDGET ===
        VBox section3 = createSection("💰 DÉTAILS & BUDGET");
        section3.setAlignment(Pos.CENTER);

        VBox fieldsContainer3 = new VBox(15);
        fieldsContainer3.setPadding(new Insets(10, 0, 0, 0));
        fieldsContainer3.setMaxWidth(600);
        fieldsContainer3.setAlignment(Pos.CENTER_LEFT);

        TextField budgetField = new TextField(String.valueOf(selectedActivite.getBudget()));
        budgetField.setStyle("-fx-background-color: #1e2749; -fx-text-fill: white; -fx-border-color: #2d3a5f; -fx-border-radius: 8; -fx-padding: 10; -fx-font-size: 13;");
        budgetField.setPrefHeight(40);
        budgetField.setMaxWidth(600);

        TextField dureeField = new TextField(String.valueOf(selectedActivite.getDuree()));
        dureeField.setStyle("-fx-background-color: #1e2749; -fx-text-fill: white; -fx-border-color: #2d3a5f; -fx-border-radius: 8; -fx-padding: 10; -fx-font-size: 13;");
        dureeField.setPrefHeight(40);
        dureeField.setMaxWidth(600);

        TextField ageMinField = new TextField(String.valueOf(selectedActivite.getAgemin()));
        ageMinField.setStyle("-fx-background-color: #1e2749; -fx-text-fill: white; -fx-border-color: #2d3a5f; -fx-border-radius: 8; -fx-padding: 10; -fx-font-size: 13;");
        ageMinField.setPrefHeight(40);
        ageMinField.setMaxWidth(600);

        DatePicker datePicker = new DatePicker();
        datePicker.setValue(selectedActivite.getDatePrevue());
        datePicker.setStyle("-fx-background-color: #1e2749; -fx-text-fill: white; -fx-border-color: #2d3a5f; -fx-border-radius: 8; -fx-padding: 8; -fx-font-size: 13;");
        datePicker.setPrefHeight(40);
        datePicker.setMaxWidth(600);

        ComboBox<String> statutCombo = createStyledComboBox();
        statutCombo.getItems().addAll("Active", "Inactive", "En attente");
        statutCombo.setValue(selectedActivite.getStatut());

        ComboBox<Categories> categorieCombo = createStyledCategoryComboBox();

        try {
            CategoriesCRUD categoriesCRUD = new CategoriesCRUD();
            List<Categories> categoriesList = categoriesCRUD.afficher();
            ObservableList<Categories> categoriesObservable = FXCollections.observableArrayList(categoriesList);
            categorieCombo.setItems(categoriesObservable);
            categorieCombo.setPromptText("Sélectionner une catégorie");

            if (selectedActivite.getCategorieId() > 0) {
                for (Categories cat : categoriesList) {
                    if (cat.getId() == selectedActivite.getCategorieId()) {
                        categorieCombo.setValue(cat);
                        break;
                    }
                }
            }
        } catch (SQLException e) {
            showError("Erreur", "Impossible de charger les catégories : " + e.getMessage());
            e.printStackTrace();
        }

        Label errorBudget = createErrorLabel();
        Label errorDuree = createErrorLabel();
        Label errorAgeMin = createErrorLabel();
        Label errorDate = createErrorLabel();

        fieldsContainer3.getChildren().addAll(
                createFormField("Budget (€) *", budgetField, errorBudget),
                createFormField("Durée (h) *", dureeField, errorDuree),
                createFormField("Âge minimum *", ageMinField, errorAgeMin),
                createFormField("Date prévue *", datePicker, errorDate),
                createFormField("Statut *", statutCombo, null),
                createFormField("Catégorie", categorieCombo, null)
        );

        section3.getChildren().add(fieldsContainer3);

        VBox sectionContainer3 = new VBox(section3);
        sectionContainer3.setAlignment(Pos.CENTER);
        mainContainer.getChildren().add(sectionContainer3);

        // === SECTION 4: IMAGE ===
        VBox section4 = createSection("🖼️ IMAGE DE L'ACTIVITÉ");
        section4.setAlignment(Pos.CENTER);

        VBox fieldsContainer4 = new VBox(15);
        fieldsContainer4.setPadding(new Insets(10, 0, 0, 0));
        fieldsContainer4.setMaxWidth(600);
        fieldsContainer4.setAlignment(Pos.CENTER_LEFT);

        ImageView imagePreview = new ImageView();
        Button chooseImageButton = new Button("📁 Choisir une image");
        chooseImageButton.setStyle("-fx-background-color: #ff8c42; -fx-text-fill: white; -fx-font-weight: 600; -fx-background-radius: 10; -fx-padding: 10 25; -fx-cursor: hand; -fx-font-size: 13;");

        Label imageNameLabel = new Label();
        VBox imageSection = createImageSection(imagePreview, chooseImageButton, imageNameLabel, currentImagePath);
        chooseImageButton.setOnAction(e -> handleChooseImage(imagePreview, imageNameLabel));

        fieldsContainer4.getChildren().add(imageSection);
        section4.getChildren().add(fieldsContainer4);

        VBox sectionContainer4 = new VBox(section4);
        sectionContainer4.setAlignment(Pos.CENTER);
        mainContainer.getChildren().add(sectionContainer4);

        // === CONFIGURATION IA ===
        setupAutoDescriptionGeneration(nomField, difficulteCombo, lieuField, descriptionField);
        generateAIBtn.setOnAction(e -> {
            if (nomField.getText().trim().isEmpty() || difficulteCombo.getValue() == null || lieuField.getText().trim().isEmpty()) {
                showWarning("Champs manquants", "Veuillez remplir le nom, la difficulté et le lieu d'abord.");
                return;
            }
            final boolean[] isGenerating = {false};
            isGenerating[0] = true;
            generateAttractiveDescription(nomField, difficulteCombo, lieuField, descriptionField, isGenerating);
        });

        // === BOUTONS D'ACTION ===
        HBox buttonBar = new HBox(15);
        buttonBar.setAlignment(Pos.CENTER_RIGHT);
        buttonBar.setPadding(new Insets(20, 0, 0, 0));
        buttonBar.setMaxWidth(800);

        Button cancelButton = new Button("Annuler");
        cancelButton.setStyle("-fx-background-color: #1e2749; -fx-text-fill: #94a3b8; -fx-font-weight: 600; -fx-background-radius: 10; -fx-padding: 12 30; -fx-cursor: hand; -fx-border-color: #2d3a5f; -fx-border-width: 1; -fx-border-radius: 10; -fx-font-size: 14;");
        cancelButton.setOnAction(e -> dialog.close());

        Button saveButton = new Button("✅ Enregistrer les modifications");
        saveButton.setStyle("-fx-background-color: #ff8c42; -fx-text-fill: white; -fx-font-weight: 600; -fx-background-radius: 10; -fx-padding: 12 30; -fx-cursor: hand; -fx-font-size: 14; -fx-effect: dropshadow(gaussian, rgba(255,140,66,0.3), 10, 0, 0, 2);");

        Region buttonSpacer = new Region();
        HBox.setHgrow(buttonSpacer, Priority.ALWAYS);
        buttonBar.getChildren().addAll(buttonSpacer, cancelButton, saveButton);

        VBox buttonContainer = new VBox(buttonBar);
        buttonContainer.setAlignment(Pos.CENTER);
        mainContainer.getChildren().add(buttonContainer);

        ScrollPane scrollPane = new ScrollPane(mainContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #0a0e27; -fx-background-color: #0a0e27; -fx-border-color: #ff8c42; -fx-border-width: 1; -fx-border-radius: 10;");

        dialogPane.setContent(scrollPane);
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Node okButtonNode = dialogPane.lookupButton(ButtonType.OK);
        Node cancelButtonNode = dialogPane.lookupButton(ButtonType.CANCEL);
        if (okButtonNode != null) {
            okButtonNode.setVisible(false);
            okButtonNode.setManaged(false);
        }
        if (cancelButtonNode != null) {
            cancelButtonNode.setVisible(false);
            cancelButtonNode.setManaged(false);
        }

        saveButton.setOnAction(event -> {
            boolean isDateValid = datePicker.getValue() != null;
            if (!isDateValid) {
                datePicker.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 8;");
                errorDate.setText("Date : Ce champ est obligatoire");
            }

            boolean isLieuValid = !lieuField.getText().trim().isEmpty();
            if (!isLieuValid) {
                lieuField.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 8; -fx-padding: 8;");
                errorLieu.setText("Lieu : Veuillez sélectionner un lieu sur la carte");
            }

            boolean isValid = validateNom(nomField, errorNom)
                    && validateDifficulte(difficulteCombo, errorDifficulte)
                    && isLieuValid
                    && validateDescription(descriptionField, errorDescription)
                    && validateBudget(budgetField, errorBudget)
                    && validateDuree(dureeField, errorDuree)
                    && validateAgeMin(ageMinField, errorAgeMin)
                    && isDateValid;

            if (isValid) {
                try {
                    selectedActivite.setNom(nomField.getText().trim());
                    selectedActivite.setDescription(descriptionField.getText().trim());
                    selectedActivite.setBudget(Integer.parseInt(budgetField.getText().trim()));
                    selectedActivite.setDuree(Integer.parseInt(dureeField.getText().trim()));
                    selectedActivite.setNiveaudifficulte(difficulteCombo.getValue());
                    selectedActivite.setLieu(lieuField.getText().trim());
                    selectedActivite.setAgemin(Integer.parseInt(ageMinField.getText().trim()));
                    selectedActivite.setStatut(statutCombo.getValue());
                    selectedActivite.setDatePrevue(datePicker.getValue());

                    Categories selectedCategorie = categorieCombo.getValue();
                    selectedActivite.setCategorieId(selectedCategorie != null ? selectedCategorie.getId() : 0);

                    if (selectedImageFile != null) {
                        if (currentImagePath != null && !currentImagePath.isEmpty()) {
                            FileManager.deleteImage(currentImagePath);
                        }
                        String newImagePath = FileManager.saveImage(selectedImageFile);
                        selectedActivite.setImagePath(newImagePath);
                    }

                    activitesCRUD.modifier(selectedActivite);
                    showInfo("Succès", "✅ Activité modifiée avec succès !");
                    loadActivites();
                    dialog.close();
                } catch (Exception e) {
                    showError("Erreur", "Impossible de modifier l'activité : " + e.getMessage());
                }
            }
        });

        dialog.showAndWait();
    }

    @FXML
    private void handleSupprimer() {
        Activites selectedActivite = tableActivites.getSelectionModel().getSelectedItem();

        if (selectedActivite == null) {
            showWarning("Aucune sélection", "Veuillez sélectionner une activité à supprimer.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText("Supprimer l'activité \"" + selectedActivite.getNom() + "\" ?");
        confirmation.setContentText("Cette action est irréversible.");

        Optional<ButtonType> result = confirmation.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                activitesCRUD.supprimer(selectedActivite.getId());
                showInfo("Succès", "✅ Activité supprimée avec succès !");
                loadActivites();
            } catch (SQLException e) {
                showError("Erreur de suppression", "Impossible de supprimer l'activité : " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleActualiser() {
        loadActivites();
        showInfo("Actualisation", "Liste des activités actualisée !");
    }

    @FXML
    private void handleVersCategories() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/categoriesback.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) btnVersCategories.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("TravelMate - Gestion des Catégories");
            stage.show();
        } catch (IOException e) {
            showError("Erreur de navigation", "Impossible de charger l'interface des catégories : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleFrontOffice() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/activitesfront.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) btnFrontOffice.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("TravelMate - Front Office Activités");
            stage.show();
        } catch (IOException e) {
            showError("Erreur de navigation", "Impossible de charger le front office : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}