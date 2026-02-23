package Controlles;

import Entites.Activites;
import Entites.Categories;
import Services.ActivitesCRUD;
import Services.CategoriesCRUD;
import Utils.FileManager;
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
import javafx.scene.control.ListCell;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
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
    // Sidebar : badge compteur sur l'item actif du menu
    @FXML private Label lblTotalActivites;
    // KPI Cards (zone centrale)
    @FXML private Label lblKpiTotal;
    @FXML private Label lblKpiActives;
    @FXML private Label lblKpiInactives;
    @FXML private Label lblKpiBudget;
    // Statistiques sidebar
    @FXML private Label lblStatActivites;
    @FXML private Label lblStatActives;
    @FXML private Label lblStatInactives;
    // Badge liste
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
        // Initialiser le service CRUD
        activitesCRUD = new ActivitesCRUD();
        activitesData = FXCollections.observableArrayList();

        // Configurer les colonnes du TableView
        setupTableColumns();

        // Charger les données
        loadActivites();

        // Activer/désactiver les boutons selon la sélection
        tableActivites.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    boolean isSelected = newSelection != null;
                    btnModifier.setDisable(!isSelected);
                    btnSupprimer.setDisable(!isSelected);
                }
        );

        // Recherche en temps réel
        setupSearch();
    }

    /**
     * Configure les colonnes du TableView
     */
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

        // Colonne catégorie
        colCategorie.setCellValueFactory(cellData -> {
            Activites activite = cellData.getValue();
            if (activite.getCategorie() != null) {
                return new javafx.beans.property.SimpleStringProperty(activite.getCategorie().getNom());
            } else {
                return new javafx.beans.property.SimpleStringProperty("");
            }
        });

        // Colonne pour l'image
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

        // Style personnalisé pour la colonne statut
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

        // Style pour le budget
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

    /**
     * Configure la recherche en temps réel
     */
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

    /**
     * Charge toutes les activités depuis la base de données
     */
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

            // Sidebar badge (item actif menu)
            if (lblTotalActivites != null) lblTotalActivites.setText(String.valueOf(total));
            // KPI Cards
            if (lblKpiTotal != null) lblKpiTotal.setText(String.valueOf(total));
            if (lblKpiActives != null) lblKpiActives.setText(String.valueOf(actives));
            if (lblKpiInactives != null) lblKpiInactives.setText(String.valueOf(inactives));
            if (lblKpiBudget != null) lblKpiBudget.setText(String.format("%.0f", budgetMoyen));
            // Statistiques sidebar
            if (lblStatActivites != null) lblStatActivites.setText(String.valueOf(total));
            if (lblStatActives != null) lblStatActives.setText(String.valueOf(actives));
            if (lblStatInactives != null) lblStatInactives.setText(String.valueOf(inactives));
            // Badge liste
            if (lblCountBadge != null) lblCountBadge.setText(total + " activité" + (total > 1 ? "s" : ""));

        } catch (SQLException e) {
            showError("Erreur de chargement",
                    "Impossible de charger les activités : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Crée un label d'erreur
     */
    private Label createErrorLabel() {
        Label label = new Label();
        label.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11; -fx-wrap-text: true;");
        label.setPrefWidth(300);
        return label;
    }

    /**
     * Valide le champ nom
     */
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

    /**
     * Valide le champ description
     */
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

    /**
     * Valide le champ budget
     */
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

    /**
     * Valide le champ durée
     */
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

    /**
     * Valide le champ difficulté
     */
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

    /**
     * Valide le champ lieu
     */
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

    /**
     * Valide le champ âge minimum
     */
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

    /**
     * Crée la section de sélection d'image pour les formulaires
     */
    private VBox createImageSection(ImageView imageView, Button chooseButton, Label imageNameLabel, String currentImage) {
        VBox imageSection = new VBox(10);
        imageSection.setStyle("-fx-padding: 10; -fx-background-color: #1e2749; -fx-background-radius: 10; -fx-max-width: 400;");

        Label imageLabel = new Label("Image de l'activité :");
        imageLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-weight: bold;");

        // Image preview
        imageView.setFitWidth(150);
        imageView.setFitHeight(150);
        imageView.setPreserveRatio(true);
        imageView.setStyle("-fx-background-color: #0a0e27; -fx-background-radius: 8; -fx-padding: 5;");

        // Charger l'image actuelle si elle existe
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

    /**
     * Gère la sélection d'image
     */
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

    /**
     * Crée un formulaire avec ScrollPane
     */
    private ScrollPane createFormScrollPane(GridPane grid) {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(grid);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setPrefHeight(500);
        scrollPane.setPrefWidth(700);
        scrollPane.setStyle("-fx-background: #1e2749; -fx-background-color: #1e2749; -fx-border-color: #ff8c42; -fx-border-radius: 10;");
        return scrollPane;
    }

    /**
     * Ajouter une nouvelle activité avec validation et image
     */
    @FXML
    private void handleAjouter() {
        Dialog<Activites> dialog = new Dialog<>();
        dialog.setTitle("Nouvelle Activité");
        dialog.setHeaderText("Ajouter une nouvelle activité");

        // Créer le formulaire
        GridPane grid = createFormGrid();

        // Champs du formulaire
        TextField nomField = new TextField();
        nomField.setPromptText("Nom de l'activité (min " + NOM_MIN_LENGTH + " caractères)");

        TextArea descriptionField = new TextArea();
        descriptionField.setPromptText("Description (min " + DESCRIPTION_MIN_LENGTH + " caractères)");
        descriptionField.setPrefRowCount(3);

        TextField budgetField = new TextField();
        budgetField.setPromptText("Budget en € (min " + BUDGET_MIN + " €)");

        TextField dureeField = new TextField();
        dureeField.setPromptText("Durée en heures (min " + DUREE_MIN + " h)");

        ComboBox<String> difficulteCombo = new ComboBox<>();
        difficulteCombo.getItems().addAll("Facile", "Moyen", "Difficile", "Expert");
        difficulteCombo.setPromptText("Niveau de difficulté");

        TextField lieuField = new TextField();
        lieuField.setPromptText("Lieu (min " + LIEU_MIN_LENGTH + " caractères)");

        TextField ageMinField = new TextField();
        ageMinField.setPromptText("Âge minimum (entre " + AGE_MIN + " et " + AGE_MAX + ")");

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
        Label errorDescription = createErrorLabel();
        Label errorBudget = createErrorLabel();
        Label errorDuree = createErrorLabel();
        Label errorDifficulte = createErrorLabel();
        Label errorLieu = createErrorLabel();
        Label errorAgeMin = createErrorLabel();

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

        // Ajouter les champs avec leurs labels d'erreur
        int row = 0;
        grid.add(new Label("Nom:*"), 0, row);
        VBox vboxNom = new VBox(3);
        vboxNom.getChildren().addAll(nomField, errorNom);
        grid.add(vboxNom, 1, row++);

        grid.add(new Label("Description:*"), 0, row);
        VBox vboxDescription = new VBox(3);
        vboxDescription.getChildren().addAll(descriptionField, errorDescription);
        grid.add(vboxDescription, 1, row++);

        grid.add(new Label("Budget (€):*"), 0, row);
        VBox vboxBudget = new VBox(3);
        vboxBudget.getChildren().addAll(budgetField, errorBudget);
        grid.add(vboxBudget, 1, row++);

        grid.add(new Label("Durée (h):*"), 0, row);
        VBox vboxDuree = new VBox(3);
        vboxDuree.getChildren().addAll(dureeField, errorDuree);
        grid.add(vboxDuree, 1, row++);

        grid.add(new Label("Difficulté:*"), 0, row);
        VBox vboxDifficulte = new VBox(3);
        vboxDifficulte.getChildren().addAll(difficulteCombo, errorDifficulte);
        grid.add(vboxDifficulte, 1, row++);

        grid.add(new Label("Lieu:*"), 0, row);
        VBox vboxLieu = new VBox(3);
        vboxLieu.getChildren().addAll(lieuField, errorLieu);
        grid.add(vboxLieu, 1, row++);

        grid.add(new Label("Âge minimum:*"), 0, row);
        VBox vboxAgeMin = new VBox(3);
        vboxAgeMin.getChildren().addAll(ageMinField, errorAgeMin);
        grid.add(vboxAgeMin, 1, row++);

        grid.add(new Label("Statut:*"), 0, row);
        VBox vboxStatut = new VBox(3);
        vboxStatut.getChildren().addAll(statutCombo, new Label());
        grid.add(vboxStatut, 1, row++);

        grid.add(new Label("Catégorie:"), 0, row);
        VBox vboxCategorie = new VBox(3);
        vboxCategorie.getChildren().addAll(categorieCombo, new Label());
        grid.add(vboxCategorie, 1, row++);

        // Ajouter la section image sur une nouvelle ligne
        grid.add(new Label("Image:"), 0, row);
        grid.add(imageSection, 1, row++);

        // Créer le ScrollPane et y mettre le GridPane
        ScrollPane scrollPane = createFormScrollPane(grid);

        // Créer un VBox pour contenir le ScrollPane
        VBox contentBox = new VBox(10, scrollPane);
        contentBox.setPadding(new Insets(10));

        dialog.getDialogPane().setContent(contentBox);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().setPrefWidth(750);
        dialog.getDialogPane().setPrefHeight(600);

        // Validation avant de fermer le dialogue
        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            boolean isValid = validateNom(nomField, errorNom)
                    && validateDescription(descriptionField, errorDescription)
                    && validateBudget(budgetField, errorBudget)
                    && validateDuree(dureeField, errorDuree)
                    && validateDifficulte(difficulteCombo, errorDifficulte)
                    && validateLieu(lieuField, errorLieu)
                    && validateAgeMin(ageMinField, errorAgeMin);

            if (!isValid) {
                event.consume(); // Empêche la fermeture du dialogue
            }
        });

        // Convertir le résultat
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

                    Categories selectedCategorie = categorieCombo.getValue();
                    if (selectedCategorie != null) {
                        activite.setCategorieId(selectedCategorie.getId());
                    } else {
                        activite.setCategorieId(0);
                    }

                    // Gestion de l'image
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
                selectedImageFile = null; // Réinitialiser
            } catch (SQLException e) {
                showError("Erreur d'ajout",
                        "Impossible d'ajouter l'activité : " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * Modifier l'activité sélectionnée avec validation et image
     */
    @FXML
    private void handleModifier() {
        Activites selectedActivite = tableActivites.getSelectionModel().getSelectedItem();

        if (selectedActivite == null) {
            showWarning("Aucune sélection", "Veuillez sélectionner une activité à modifier.");
            return;
        }

        // Réinitialiser les variables d'image
        selectedImageFile = null;
        currentImagePath = selectedActivite.getImagePath();

        Dialog<Activites> dialog = new Dialog<>();
        dialog.setTitle("Modifier Activité");
        dialog.setHeaderText("Modifier l'activité: " + selectedActivite.getNom());

        // Créer le formulaire pré-rempli
        GridPane grid = createFormGrid();

        TextField nomField = new TextField(selectedActivite.getNom());
        TextArea descriptionField = new TextArea(selectedActivite.getDescription());
        descriptionField.setPrefRowCount(3);
        TextField budgetField = new TextField(String.valueOf(selectedActivite.getBudget()));
        TextField dureeField = new TextField(String.valueOf(selectedActivite.getDuree()));

        ComboBox<String> difficulteCombo = new ComboBox<>();
        difficulteCombo.getItems().addAll("Facile", "Moyen", "Difficile", "Expert");
        difficulteCombo.setValue(selectedActivite.getNiveaudifficulte());

        TextField lieuField = new TextField(selectedActivite.getLieu());
        TextField ageMinField = new TextField(String.valueOf(selectedActivite.getAgemin()));

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
        Label errorDescription = createErrorLabel();
        Label errorBudget = createErrorLabel();
        Label errorDuree = createErrorLabel();
        Label errorDifficulte = createErrorLabel();
        Label errorLieu = createErrorLabel();
        Label errorAgeMin = createErrorLabel();

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

        // Ajouter les champs avec leurs labels d'erreur
        int row = 0;
        grid.add(new Label("Nom:*"), 0, row);
        VBox vboxNom = new VBox(3);
        vboxNom.getChildren().addAll(nomField, errorNom);
        grid.add(vboxNom, 1, row++);

        grid.add(new Label("Description:*"), 0, row);
        VBox vboxDescription = new VBox(3);
        vboxDescription.getChildren().addAll(descriptionField, errorDescription);
        grid.add(vboxDescription, 1, row++);

        grid.add(new Label("Budget (€):*"), 0, row);
        VBox vboxBudget = new VBox(3);
        vboxBudget.getChildren().addAll(budgetField, errorBudget);
        grid.add(vboxBudget, 1, row++);

        grid.add(new Label("Durée (h):*"), 0, row);
        VBox vboxDuree = new VBox(3);
        vboxDuree.getChildren().addAll(dureeField, errorDuree);
        grid.add(vboxDuree, 1, row++);

        grid.add(new Label("Difficulté:*"), 0, row);
        VBox vboxDifficulte = new VBox(3);
        vboxDifficulte.getChildren().addAll(difficulteCombo, errorDifficulte);
        grid.add(vboxDifficulte, 1, row++);

        grid.add(new Label("Lieu:*"), 0, row);
        VBox vboxLieu = new VBox(3);
        vboxLieu.getChildren().addAll(lieuField, errorLieu);
        grid.add(vboxLieu, 1, row++);

        grid.add(new Label("Âge minimum:*"), 0, row);
        VBox vboxAgeMin = new VBox(3);
        vboxAgeMin.getChildren().addAll(ageMinField, errorAgeMin);
        grid.add(vboxAgeMin, 1, row++);

        grid.add(new Label("Statut:*"), 0, row);
        VBox vboxStatut = new VBox(3);
        vboxStatut.getChildren().addAll(statutCombo, new Label());
        grid.add(vboxStatut, 1, row++);

        grid.add(new Label("Catégorie:"), 0, row);
        VBox vboxCategorie = new VBox(3);
        vboxCategorie.getChildren().addAll(categorieCombo, new Label());
        grid.add(vboxCategorie, 1, row++);

        // Ajouter la section image sur une nouvelle ligne
        grid.add(new Label("Image:"), 0, row);
        grid.add(imageSection, 1, row++);

        // Créer le ScrollPane et y mettre le GridPane
        ScrollPane scrollPane = createFormScrollPane(grid);

        // Créer un VBox pour contenir le ScrollPane
        VBox contentBox = new VBox(10, scrollPane);
        contentBox.setPadding(new Insets(10));

        dialog.getDialogPane().setContent(contentBox);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().setPrefWidth(750);
        dialog.getDialogPane().setPrefHeight(600);

        // Validation avant de fermer le dialogue
        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            boolean isValid = validateNom(nomField, errorNom)
                    && validateDescription(descriptionField, errorDescription)
                    && validateBudget(budgetField, errorBudget)
                    && validateDuree(dureeField, errorDuree)
                    && validateDifficulte(difficulteCombo, errorDifficulte)
                    && validateLieu(lieuField, errorLieu)
                    && validateAgeMin(ageMinField, errorAgeMin);

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

                    Categories selectedCategorie = categorieCombo.getValue();
                    if (selectedCategorie != null) {
                        selectedActivite.setCategorieId(selectedCategorie.getId());
                    } else {
                        selectedActivite.setCategorieId(0);
                    }

                    // Gestion de l'image
                    if (selectedImageFile != null) {
                        // Supprimer l'ancienne image si elle existe
                        if (currentImagePath != null && !currentImagePath.isEmpty()) {
                            FileManager.deleteImage(currentImagePath);
                        }
                        // Sauvegarder la nouvelle image
                        try {
                            String newImagePath = FileManager.saveImage(selectedImageFile);
                            selectedActivite.setImagePath(newImagePath);
                        } catch (IOException e) {
                            showError("Erreur", "Impossible de sauvegarder l'image : " + e.getMessage());
                            return null;
                        }
                    } else {
                        // Conserver l'image existante
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
                showError("Erreur de modification",
                        "Impossible de modifier l'activité : " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * Supprimer l'activité sélectionnée
     */
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
                // La suppression de l'image est gérée dans le CRUD
                activitesCRUD.supprimer(selectedActivite.getId());
                showInfo("Succès", "Activité supprimée avec succès !");
                loadActivites();
            } catch (SQLException e) {
                showError("Erreur de suppression",
                        "Impossible de supprimer l'activité : " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Actualiser la liste des activités
     */
    @FXML
    private void handleActualiser() {
        loadActivites();
        showInfo("Actualisation", "Liste des activités actualisée !");
    }

    /**
     * Naviguer vers l'interface des catégories
     */
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
            showError("Erreur de navigation",
                    "Impossible de charger l'interface des catégories : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Naviguer vers l'interface front office
     */
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
            showError("Erreur de navigation",
                    "Impossible de charger le front office : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Crée une grille de formulaire stylée
     */
    private GridPane createFormGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 20, 20));
        grid.setStyle("-fx-background-color: #1e2749; -fx-background-radius: 10;");
        return grid;
    }

    // ==================== MÉTHODES UTILITAIRES ====================

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