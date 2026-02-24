package Controlles;

import Entites.Activites;
import Entites.Categories;
import Services.ActivitesCRUD;
import Services.AIService;
import Services.CategoriesCRUD;
import Utils.FileManager;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.geometry.Insets;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

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

    // Patterns de validation
    private static final Pattern NOM_PATTERN = Pattern.compile("^[a-zA-ZÀ-ÿ\\s\\-']+$");
    private static final Pattern LIEU_PATTERN = Pattern.compile("^[a-zA-ZÀ-ÿ\\s\\-',0-9]+$");

    // Constantes de validation
    private static final int NOM_MIN_LENGTH = 3;
    private static final int NOM_MAX_LENGTH = 100;
    private static final int DESCRIPTION_MIN_LENGTH = 15;
    private static final int DESCRIPTION_MAX_LENGTH = 500;
    private static final int LIEU_MIN_LENGTH = 3;
    private static final int LIEU_MAX_LENGTH = 100;
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
                boolean isConnected = AIService.testConnection();
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

    /**
     * Configure la génération automatique de description par IA
     * Déclenchement uniquement à la perte de focus du dernier champ
     */
    private void setupAutoDescriptionGeneration(TextField nomField, ComboBox<String> difficulteCombo,
                                                TextField lieuField, TextArea descriptionField) {

        final boolean[] isGenerating = {false};

        // Écouteur pour le champ lieu (dernier champ des 3 essentiels)
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

        // Aussi déclencher quand l'utilisateur appuie sur Entrée dans le champ lieu
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

    /**
     * Génère une description attrayante et professionnelle
     */
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
                String description = AIService.genererDescriptionAttrayante(nom, difficulte, lieu);

                Platform.runLater(() -> {
                    descriptionField.setText(description);
                    descriptionField.setDisable(false);
                    descriptionField.setPromptText("Description (min " + DESCRIPTION_MIN_LENGTH + " caractères)");
                    isGenerating[0] = false;

                    descriptionField.setStyle("-fx-border-color: #34d399; -fx-border-width: 2;");
                    new Thread(() -> {
                        try { Thread.sleep(1000); } catch (InterruptedException e) {}
                        Platform.runLater(() ->
                                descriptionField.setStyle("-fx-border-color: #2d3a5f; -fx-border-width: 1;")
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

                    descriptionField.setStyle("-fx-border-color: #f59e0b; -fx-border-width: 2;");
                });
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Description de secours attrayante (quand l'API ne répond pas)
     */
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

    private Label createErrorLabel() {
        Label label = new Label();
        label.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11; -fx-wrap-text: true;");
        label.setPrefWidth(300);
        return label;
    }

    private boolean validateNom(TextField champ, Label errorLabel) {
        String texte = champ.getText();
        champ.setStyle("-fx-border-color: #2d3a5f; -fx-border-width: 1;");
        errorLabel.setText("");

        if (texte == null || texte.trim().isEmpty()) {
            champ.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2;");
            errorLabel.setText("Nom : Ce champ est obligatoire");
            return false;
        }

        String trimmed = texte.trim();
        if (trimmed.length() < NOM_MIN_LENGTH) {
            champ.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2;");
            errorLabel.setText("Nom : doit contenir au moins " + NOM_MIN_LENGTH + " caractères");
            return false;
        }

        if (trimmed.length() > NOM_MAX_LENGTH) {
            champ.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2;");
            errorLabel.setText("Nom : ne doit pas dépasser " + NOM_MAX_LENGTH + " caractères");
            return false;
        }

        if (!NOM_PATTERN.matcher(trimmed).matches()) {
            champ.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2;");
            errorLabel.setText("Nom : ne doit contenir que des lettres, espaces et tirets");
            return false;
        }

        return true;
    }

    private boolean validateDescription(TextArea champ, Label errorLabel) {
        String texte = champ.getText();
        champ.setStyle("-fx-border-color: #2d3a5f; -fx-border-width: 1;");
        errorLabel.setText("");

        if (texte == null || texte.trim().isEmpty()) {
            champ.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2;");
            errorLabel.setText("Description : Ce champ est obligatoire");
            return false;
        }

        String trimmed = texte.trim();
        if (trimmed.length() < DESCRIPTION_MIN_LENGTH) {
            champ.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2;");
            errorLabel.setText("Description : doit contenir au moins " + DESCRIPTION_MIN_LENGTH + " caractères");
            return false;
        }

        if (trimmed.length() > DESCRIPTION_MAX_LENGTH) {
            champ.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2;");
            errorLabel.setText("Description : ne doit pas dépasser " + DESCRIPTION_MAX_LENGTH + " caractères");
            return false;
        }

        return true;
    }

    private boolean validateBudget(TextField champ, Label errorLabel) {
        String texte = champ.getText();
        champ.setStyle("-fx-border-color: #2d3a5f; -fx-border-width: 1;");
        errorLabel.setText("");

        if (texte == null || texte.trim().isEmpty()) {
            champ.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2;");
            errorLabel.setText("Budget : Ce champ est obligatoire");
            return false;
        }

        try {
            int valeur = Integer.parseInt(texte.trim());
            if (valeur < BUDGET_MIN) {
                champ.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2;");
                errorLabel.setText("Budget : doit être au moins " + BUDGET_MIN + " €");
                return false;
            }
            if (valeur > BUDGET_MAX) {
                champ.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2;");
                errorLabel.setText("Budget : ne peut pas dépasser " + BUDGET_MAX + " €");
                return false;
            }
        } catch (NumberFormatException e) {
            champ.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2;");
            errorLabel.setText("Budget : veuillez entrer un nombre valide");
            return false;
        }

        return true;
    }

    private boolean validateDuree(TextField champ, Label errorLabel) {
        String texte = champ.getText();
        champ.setStyle("-fx-border-color: #2d3a5f; -fx-border-width: 1;");
        errorLabel.setText("");

        if (texte == null || texte.trim().isEmpty()) {
            champ.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2;");
            errorLabel.setText("Durée : Ce champ est obligatoire");
            return false;
        }

        try {
            int valeur = Integer.parseInt(texte.trim());
            if (valeur < DUREE_MIN) {
                champ.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2;");
                errorLabel.setText("Durée : doit être au moins " + DUREE_MIN + " heure");
                return false;
            }
            if (valeur > DUREE_MAX) {
                champ.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2;");
                errorLabel.setText("Durée : ne peut pas dépasser " + DUREE_MAX + " heures (1 an)");
                return false;
            }
        } catch (NumberFormatException e) {
            champ.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2;");
            errorLabel.setText("Durée : veuillez entrer un nombre valide");
            return false;
        }

        return true;
    }

    private boolean validateDifficulte(ComboBox<String> combo, Label errorLabel) {
        combo.setStyle("-fx-border-color: #2d3a5f; -fx-border-width: 1;");
        errorLabel.setText("");

        if (combo.getValue() == null) {
            combo.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2;");
            errorLabel.setText("Difficulté : veuillez faire une sélection");
            return false;
        }

        return true;
    }

    private boolean validateLieu(TextField champ, Label errorLabel) {
        String texte = champ.getText();
        champ.setStyle("-fx-border-color: #2d3a5f; -fx-border-width: 1;");
        errorLabel.setText("");

        if (texte == null || texte.trim().isEmpty()) {
            champ.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2;");
            errorLabel.setText("Lieu : Ce champ est obligatoire");
            return false;
        }

        String trimmed = texte.trim();
        if (trimmed.length() < LIEU_MIN_LENGTH) {
            champ.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2;");
            errorLabel.setText("Lieu : doit contenir au moins " + LIEU_MIN_LENGTH + " caractères");
            return false;
        }

        if (trimmed.length() > LIEU_MAX_LENGTH) {
            champ.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2;");
            errorLabel.setText("Lieu : ne doit pas dépasser " + LIEU_MAX_LENGTH + " caractères");
            return false;
        }

        if (!LIEU_PATTERN.matcher(trimmed).matches()) {
            champ.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2;");
            errorLabel.setText("Lieu : format invalide (lettres, chiffres, espaces et tirets uniquement)");
            return false;
        }

        return true;
    }

    private boolean validateAgeMin(TextField champ, Label errorLabel) {
        String texte = champ.getText();
        champ.setStyle("-fx-border-color: #2d3a5f; -fx-border-width: 1;");
        errorLabel.setText("");

        if (texte == null || texte.trim().isEmpty()) {
            champ.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2;");
            errorLabel.setText("Âge minimum : Ce champ est obligatoire");
            return false;
        }

        try {
            int valeur = Integer.parseInt(texte.trim());
            if (valeur < AGE_MIN) {
                champ.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2;");
                errorLabel.setText("Âge minimum : doit être supérieur ou égal à " + AGE_MIN);
                return false;
            }
            if (valeur > AGE_MAX) {
                champ.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2;");
                errorLabel.setText("Âge minimum : doit être inférieur ou égal à " + AGE_MAX + " ans");
                return false;
            }
        } catch (NumberFormatException e) {
            champ.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2;");
            errorLabel.setText("Âge minimum : veuillez entrer un nombre valide");
            return false;
        }

        return true;
    }

    private VBox createImageSection(ImageView imageView, Button chooseButton, Label imageNameLabel, String currentImage) {
        VBox imageSection = new VBox(10);
        imageSection.setStyle("-fx-padding: 10; -fx-background-color: #1e2749; -fx-background-radius: 10; -fx-max-width: 400;");

        Label imageLabel = new Label("Image de l'activité :");
        imageLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-weight: bold;");

        imageView.setFitWidth(150);
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
        chooseButton.setStyle("-fx-background-color: #ff8c42; -fx-text-fill: white; -fx-font-weight: 600; -fx-background-radius: 10; -fx-padding: 8 15; -fx-cursor: hand;");

        imageNameLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11; -fx-wrap-text: true;");

        HBox buttonBox = new HBox(10, chooseButton);
        buttonBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

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

    private ScrollPane createFormScrollPane(GridPane grid) {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(grid);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setPrefHeight(500);
        scrollPane.setPrefWidth(750);
        scrollPane.setStyle("-fx-background: #1e2749; -fx-background-color: #1e2749; -fx-border-color: #ff8c42; -fx-border-radius: 10;");
        return scrollPane;
    }

    private GridPane createFormGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 20, 20));
        grid.setStyle("-fx-background-color: #1e2749; -fx-background-radius: 10;");
        return grid;
    }

    @FXML
    private void handleAjouter() {
        Dialog<Activites> dialog = new Dialog<>();
        dialog.setTitle("Nouvelle Activité");
        dialog.setHeaderText("Ajouter une nouvelle activité");

        GridPane grid = createFormGrid();

        // Champs du formulaire
        TextField nomField = new TextField();
        nomField.setPromptText("Nom de l'activité (min " + NOM_MIN_LENGTH + " caractères)");

        ComboBox<String> difficulteCombo = new ComboBox<>();
        difficulteCombo.getItems().addAll("Facile", "Moyen", "Difficile", "Expert");
        difficulteCombo.setPromptText("Niveau de difficulté");

        TextField lieuField = new TextField();
        lieuField.setPromptText("Lieu (min " + LIEU_MIN_LENGTH + " caractères)");

        TextArea descriptionField = new TextArea();
        descriptionField.setPromptText("Description (min " + DESCRIPTION_MIN_LENGTH + " caractères)");
        descriptionField.setPrefRowCount(3);

        TextField budgetField = new TextField();
        budgetField.setPromptText("Budget en € (min " + BUDGET_MIN + " €)");

        TextField dureeField = new TextField();
        dureeField.setPromptText("Durée en heures (min " + DUREE_MIN + " h)");

        TextField ageMinField = new TextField();
        ageMinField.setPromptText("Âge minimum (entre " + AGE_MIN + " et " + AGE_MAX + ")");

        // NOUVEAU : DatePicker pour la date prévue
        DatePicker datePicker = new DatePicker();
        datePicker.setPromptText("Date prévue de l'activité");
        datePicker.setValue(LocalDate.now().plusDays(7)); // Par défaut dans 7 jours

        ComboBox<String> statutCombo = new ComboBox<>();
        statutCombo.getItems().addAll("Active", "Inactive", "En attente");
        statutCombo.setValue("Active");

        ComboBox<Categories> categorieCombo = new ComboBox<>();

        // Section image
        ImageView imagePreview = new ImageView();
        Button chooseImageButton = new Button();
        Label imageNameLabel = new Label();
        VBox imageSection = createImageSection(imagePreview, chooseImageButton, imageNameLabel, null);

        chooseImageButton.setOnAction(e -> handleChooseImage(imagePreview, imageNameLabel));

        // Labels d'erreur
        Label errorNom = createErrorLabel();
        Label errorDifficulte = createErrorLabel();
        Label errorLieu = createErrorLabel();
        Label errorDescription = createErrorLabel();
        Label errorBudget = createErrorLabel();
        Label errorDuree = createErrorLabel();
        Label errorAgeMin = createErrorLabel();
        Label errorDate = createErrorLabel(); // NOUVEAU

        // Charger les catégories
        try {
            CategoriesCRUD categoriesCRUD = new CategoriesCRUD();
            List<Categories> categoriesList = categoriesCRUD.afficher();
            ObservableList<Categories> categoriesObservable = FXCollections.observableArrayList(categoriesList);
            categorieCombo.setItems(categoriesObservable);
            categorieCombo.setPromptText("Sélectionner une catégorie");

            categorieCombo.setCellFactory(param -> new ListCell<Categories>() {
                @Override
                protected void updateItem(Categories item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getNom());
                    }
                }
            });

            categorieCombo.setButtonCell(new ListCell<Categories>() {
                @Override
                protected void updateItem(Categories item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getNom());
                    }
                }
            });
        } catch (SQLException e) {
            showError("Erreur", "Impossible de charger les catégories : " + e.getMessage());
            e.printStackTrace();
        }

        // Configuration de la génération automatique par IA
        setupAutoDescriptionGeneration(nomField, difficulteCombo, lieuField, descriptionField);

        // Bouton de génération manuelle
        Button generateAIBtn = createAIGenerateButton(nomField, difficulteCombo, lieuField, descriptionField);

        // Ajout des champs dans l'ordre LOGIQUE
        int row = 0;

        // 1. NOM
        grid.add(new Label("Nom:*"), 0, row);
        VBox vboxNom = new VBox(3);
        vboxNom.getChildren().addAll(nomField, errorNom);
        grid.add(vboxNom, 1, row++);

        // 2. DIFFICULTÉ
        grid.add(new Label("Difficulté:*"), 0, row);
        VBox vboxDifficulte = new VBox(3);
        vboxDifficulte.getChildren().addAll(difficulteCombo, errorDifficulte);
        grid.add(vboxDifficulte, 1, row++);

        // 3. LIEU
        grid.add(new Label("Lieu:*"), 0, row);
        VBox vboxLieu = new VBox(3);
        vboxLieu.getChildren().addAll(lieuField, errorLieu);
        grid.add(vboxLieu, 1, row++);

        // 4. DESCRIPTION
        grid.add(new Label("Description:*"), 0, row);
        VBox vboxDescription = new VBox(3);
        vboxDescription.getChildren().addAll(descriptionField, errorDescription);
        grid.add(vboxDescription, 1, row++);

        // 5. BUDGET
        grid.add(new Label("Budget (€):*"), 0, row);
        VBox vboxBudget = new VBox(3);
        vboxBudget.getChildren().addAll(budgetField, errorBudget);
        grid.add(vboxBudget, 1, row++);

        // 6. DURÉE
        grid.add(new Label("Durée (h):*"), 0, row);
        VBox vboxDuree = new VBox(3);
        vboxDuree.getChildren().addAll(dureeField, errorDuree);
        grid.add(vboxDuree, 1, row++);

        // 7. ÂGE MINIMUM
        grid.add(new Label("Âge minimum:*"), 0, row);
        VBox vboxAgeMin = new VBox(3);
        vboxAgeMin.getChildren().addAll(ageMinField, errorAgeMin);
        grid.add(vboxAgeMin, 1, row++);

        // 8. DATE PREVUE (NOUVEAU)
        grid.add(new Label("Date prévue:*"), 0, row);
        VBox vboxDate = new VBox(3);
        vboxDate.getChildren().addAll(datePicker, errorDate);
        grid.add(vboxDate, 1, row++);

        // 9. STATUT
        grid.add(new Label("Statut:*"), 0, row);
        VBox vboxStatut = new VBox(3);
        vboxStatut.getChildren().addAll(statutCombo, new Label());
        grid.add(vboxStatut, 1, row++);

        // 10. CATÉGORIE
        grid.add(new Label("Catégorie:"), 0, row);
        VBox vboxCategorie = new VBox(3);
        vboxCategorie.getChildren().addAll(categorieCombo, new Label());
        grid.add(vboxCategorie, 1, row++);

        // 11. IA
        grid.add(new Label("IA:"), 0, row);
        VBox vboxIA = new VBox(10);
        Label iaHint = new Label("✨ La description sera générée automatiquement quand vous aurez rempli les 3 champs (nom, difficulté, lieu) et quitté le champ lieu");
        iaHint.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11; -fx-wrap-text: true;");
        vboxIA.getChildren().addAll(generateAIBtn, iaHint);
        grid.add(vboxIA, 1, row++);

        // 12. IMAGE
        grid.add(new Label("Image:"), 0, row);
        grid.add(imageSection, 1, row++);

        ScrollPane scrollPane = createFormScrollPane(grid);
        VBox contentBox = new VBox(10, scrollPane);
        contentBox.setPadding(new Insets(10));

        dialog.getDialogPane().setContent(contentBox);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().setPrefWidth(800);
        dialog.getDialogPane().setPrefHeight(700); // Légèrement plus haut pour la date

        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            // Validation de la date
            boolean isDateValid = datePicker.getValue() != null;
            if (!isDateValid) {
                datePicker.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2;");
                errorDate.setText("Date : Ce champ est obligatoire");
            } else {
                datePicker.setStyle("-fx-border-color: #2d3a5f; -fx-border-width: 1;");
                errorDate.setText("");
            }

            boolean isValid = validateNom(nomField, errorNom)
                    && validateDifficulte(difficulteCombo, errorDifficulte)
                    && validateLieu(lieuField, errorLieu)
                    && validateDescription(descriptionField, errorDescription)
                    && validateBudget(budgetField, errorBudget)
                    && validateDuree(dureeField, errorDuree)
                    && validateAgeMin(ageMinField, errorAgeMin)
                    && isDateValid;

            if (!isValid) {
                event.consume();
            }
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
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

                    // NOUVEAU : Ajouter la date
                    activite.setDatePrevue(datePicker.getValue());

                    Categories selectedCategorie = categorieCombo.getValue();
                    if (selectedCategorie != null) {
                        activite.setCategorieId(selectedCategorie.getId());
                    } else {
                        activite.setCategorieId(0);
                    }

                    if (selectedImageFile != null) {
                        try {
                            String imagePath = FileManager.saveImage(selectedImageFile);
                            activite.setImagePath(imagePath);
                        } catch (IOException e) {
                            showError("Erreur", "Impossible de sauvegarder l'image : " + e.getMessage());
                            return null;
                        }
                    }

                    return activite;
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        });

        Optional<Activites> result = dialog.showAndWait();

        result.ifPresent(activite -> {
            try {
                activitesCRUD.ajouter(activite);
                showInfo("Succès", "Activité ajoutée avec succès !");
                loadActivites();
                selectedImageFile = null;
            } catch (SQLException e) {
                showError("Erreur d'ajout", "Impossible d'ajouter l'activité : " + e.getMessage());
                e.printStackTrace();
            }
        });
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

        GridPane grid = createFormGrid();

        // Champs pré-remplis
        TextField nomField = new TextField(selectedActivite.getNom());

        ComboBox<String> difficulteCombo = new ComboBox<>();
        difficulteCombo.getItems().addAll("Facile", "Moyen", "Difficile", "Expert");
        difficulteCombo.setValue(selectedActivite.getNiveaudifficulte());

        TextField lieuField = new TextField(selectedActivite.getLieu());

        TextArea descriptionField = new TextArea(selectedActivite.getDescription());
        descriptionField.setPrefRowCount(3);

        TextField budgetField = new TextField(String.valueOf(selectedActivite.getBudget()));
        TextField dureeField = new TextField(String.valueOf(selectedActivite.getDuree()));
        TextField ageMinField = new TextField(String.valueOf(selectedActivite.getAgemin()));

        // NOUVEAU : DatePicker avec la date existante
        DatePicker datePicker = new DatePicker();
        datePicker.setValue(selectedActivite.getDatePrevue());
        datePicker.setPromptText("Date prévue de l'activité");

        ComboBox<String> statutCombo = new ComboBox<>();
        statutCombo.getItems().addAll("Active", "Inactive", "En attente");
        statutCombo.setValue(selectedActivite.getStatut());

        // Section image
        ImageView imagePreview = new ImageView();
        Button chooseImageButton = new Button();
        Label imageNameLabel = new Label();
        VBox imageSection = createImageSection(imagePreview, chooseImageButton, imageNameLabel, currentImagePath);

        chooseImageButton.setOnAction(e -> handleChooseImage(imagePreview, imageNameLabel));

        // Labels d'erreur
        Label errorNom = createErrorLabel();
        Label errorDifficulte = createErrorLabel();
        Label errorLieu = createErrorLabel();
        Label errorDescription = createErrorLabel();
        Label errorBudget = createErrorLabel();
        Label errorDuree = createErrorLabel();
        Label errorAgeMin = createErrorLabel();
        Label errorDate = createErrorLabel(); // NOUVEAU

        ComboBox<Categories> categorieCombo = new ComboBox<>();
        try {
            CategoriesCRUD categoriesCRUD = new CategoriesCRUD();
            List<Categories> categoriesList = categoriesCRUD.afficher();
            ObservableList<Categories> categoriesObservable = FXCollections.observableArrayList(categoriesList);
            categorieCombo.setItems(categoriesObservable);

            categorieCombo.setCellFactory(param -> new ListCell<Categories>() {
                @Override
                protected void updateItem(Categories item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getNom());
                    }
                }
            });

            categorieCombo.setButtonCell(new ListCell<Categories>() {
                @Override
                protected void updateItem(Categories item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getNom());
                    }
                }
            });

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

        // Configuration de la génération automatique par IA
        setupAutoDescriptionGeneration(nomField, difficulteCombo, lieuField, descriptionField);

        // Bouton de génération manuelle
        Button generateAIBtn = createAIGenerateButton(nomField, difficulteCombo, lieuField, descriptionField);

        // Ajout des champs dans l'ordre LOGIQUE
        int row = 0;

        // 1. NOM
        grid.add(new Label("Nom:*"), 0, row);
        VBox vboxNom = new VBox(3);
        vboxNom.getChildren().addAll(nomField, errorNom);
        grid.add(vboxNom, 1, row++);

        // 2. DIFFICULTÉ
        grid.add(new Label("Difficulté:*"), 0, row);
        VBox vboxDifficulte = new VBox(3);
        vboxDifficulte.getChildren().addAll(difficulteCombo, errorDifficulte);
        grid.add(vboxDifficulte, 1, row++);

        // 3. LIEU
        grid.add(new Label("Lieu:*"), 0, row);
        VBox vboxLieu = new VBox(3);
        vboxLieu.getChildren().addAll(lieuField, errorLieu);
        grid.add(vboxLieu, 1, row++);

        // 4. DESCRIPTION
        grid.add(new Label("Description:*"), 0, row);
        VBox vboxDescription = new VBox(3);
        vboxDescription.getChildren().addAll(descriptionField, errorDescription);
        grid.add(vboxDescription, 1, row++);

        // 5. BUDGET
        grid.add(new Label("Budget (€):*"), 0, row);
        VBox vboxBudget = new VBox(3);
        vboxBudget.getChildren().addAll(budgetField, errorBudget);
        grid.add(vboxBudget, 1, row++);

        // 6. DURÉE
        grid.add(new Label("Durée (h):*"), 0, row);
        VBox vboxDuree = new VBox(3);
        vboxDuree.getChildren().addAll(dureeField, errorDuree);
        grid.add(vboxDuree, 1, row++);

        // 7. ÂGE MINIMUM
        grid.add(new Label("Âge minimum:*"), 0, row);
        VBox vboxAgeMin = new VBox(3);
        vboxAgeMin.getChildren().addAll(ageMinField, errorAgeMin);
        grid.add(vboxAgeMin, 1, row++);

        // 8. DATE PREVUE (NOUVEAU)
        grid.add(new Label("Date prévue:*"), 0, row);
        VBox vboxDate = new VBox(3);
        vboxDate.getChildren().addAll(datePicker, errorDate);
        grid.add(vboxDate, 1, row++);

        // 9. STATUT
        grid.add(new Label("Statut:*"), 0, row);
        VBox vboxStatut = new VBox(3);
        vboxStatut.getChildren().addAll(statutCombo, new Label());
        grid.add(vboxStatut, 1, row++);

        // 10. CATÉGORIE
        grid.add(new Label("Catégorie:"), 0, row);
        VBox vboxCategorie = new VBox(3);
        vboxCategorie.getChildren().addAll(categorieCombo, new Label());
        grid.add(vboxCategorie, 1, row++);

        // 11. IA
        grid.add(new Label("IA:"), 0, row);
        VBox vboxIA = new VBox(10);
        Label iaHint = new Label("✨ La description sera régénérée automatiquement si vous modifiez les champs et quittez le champ lieu");
        iaHint.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11; -fx-wrap-text: true;");
        vboxIA.getChildren().addAll(generateAIBtn, iaHint);
        grid.add(vboxIA, 1, row++);

        // 12. IMAGE
        grid.add(new Label("Image:"), 0, row);
        grid.add(imageSection, 1, row++);

        ScrollPane scrollPane = createFormScrollPane(grid);
        VBox contentBox = new VBox(10, scrollPane);
        contentBox.setPadding(new Insets(10));

        dialog.getDialogPane().setContent(contentBox);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().setPrefWidth(800);
        dialog.getDialogPane().setPrefHeight(700); // Légèrement plus haut pour la date

        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            // Validation de la date
            boolean isDateValid = datePicker.getValue() != null;
            if (!isDateValid) {
                datePicker.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2;");
                errorDate.setText("Date : Ce champ est obligatoire");
            } else {
                datePicker.setStyle("-fx-border-color: #2d3a5f; -fx-border-width: 1;");
                errorDate.setText("");
            }

            boolean isValid = validateNom(nomField, errorNom)
                    && validateDifficulte(difficulteCombo, errorDifficulte)
                    && validateLieu(lieuField, errorLieu)
                    && validateDescription(descriptionField, errorDescription)
                    && validateBudget(budgetField, errorBudget)
                    && validateDuree(dureeField, errorDuree)
                    && validateAgeMin(ageMinField, errorAgeMin)
                    && isDateValid;

            if (!isValid) {
                event.consume();
            }
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                try {
                    selectedActivite.setNom(nomField.getText().trim());
                    selectedActivite.setDescription(descriptionField.getText().trim());
                    selectedActivite.setBudget(Integer.parseInt(budgetField.getText().trim()));
                    selectedActivite.setDuree(Integer.parseInt(dureeField.getText().trim()));
                    selectedActivite.setNiveaudifficulte(difficulteCombo.getValue());
                    selectedActivite.setLieu(lieuField.getText().trim());
                    selectedActivite.setAgemin(Integer.parseInt(ageMinField.getText().trim()));
                    selectedActivite.setStatut(statutCombo.getValue());

                    // NOUVEAU : Mettre à jour la date
                    selectedActivite.setDatePrevue(datePicker.getValue());

                    Categories selectedCategorie = categorieCombo.getValue();
                    if (selectedCategorie != null) {
                        selectedActivite.setCategorieId(selectedCategorie.getId());
                    } else {
                        selectedActivite.setCategorieId(0);
                    }

                    if (selectedImageFile != null) {
                        if (currentImagePath != null && !currentImagePath.isEmpty()) {
                            FileManager.deleteImage(currentImagePath);
                        }
                        try {
                            String newImagePath = FileManager.saveImage(selectedImageFile);
                            selectedActivite.setImagePath(newImagePath);
                        } catch (IOException e) {
                            showError("Erreur", "Impossible de sauvegarder l'image : " + e.getMessage());
                            return null;
                        }
                    } else {
                        selectedActivite.setImagePath(currentImagePath);
                    }

                    return selectedActivite;
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        });

        Optional<Activites> result = dialog.showAndWait();

        result.ifPresent(activite -> {
            try {
                activitesCRUD.modifier(activite);
                showInfo("Succès", "Activité modifiée avec succès !");
                loadActivites();
                selectedImageFile = null;
            } catch (SQLException e) {
                showError("Erreur de modification", "Impossible de modifier l'activité : " + e.getMessage());
                e.printStackTrace();
            }
        });
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
                showInfo("Succès", "Activité supprimée avec succès !");
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