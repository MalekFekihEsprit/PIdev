package Controlles;

import Entites.Categories;
import Services.CategoriesCRUD;
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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class CATback implements Initializable {

    // TableView et colonnes
    @FXML private TableView<Categories> tableCategories;
    @FXML private TableColumn<Categories, Integer> colId;
    @FXML private TableColumn<Categories, String> colNom;
    @FXML private TableColumn<Categories, String> colDescription;
    @FXML private TableColumn<Categories, String> colType;
    @FXML private TableColumn<Categories, String> colSaison;
    @FXML private TableColumn<Categories, String> colNiveauIntensite;
    @FXML private TableColumn<Categories, String> colPublicCible;

    // Boutons et champs
    @FXML private Button btnModifier;
    @FXML private Button btnSupprimer;
    @FXML private TextField searchField;
    @FXML private Label lblTotalCategories;
    @FXML private Button btnFrontOffice;

    // Patterns de validation
    private static final Pattern NOM_PATTERN = Pattern.compile("^[a-zA-ZÀ-ÿ\\s\\-']+$");
    private static final Pattern PUBLIC_PATTERN = Pattern.compile("^[a-zA-ZÀ-ÿ\\s\\-',]+$");

    // Constantes de validation
    private static final int NOM_MIN_LENGTH = 3;
    private static final int NOM_MAX_LENGTH = 100;
    private static final int DESCRIPTION_MIN_LENGTH = 15;
    private static final int DESCRIPTION_MAX_LENGTH = 500;
    private static final int PUBLIC_MIN_LENGTH = 3;
    private static final int PUBLIC_MAX_LENGTH = 100;

    // Service CRUD
    private CategoriesCRUD categoriesCRUD;
    private ObservableList<Categories> categoriesData;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialiser le service CRUD
        categoriesCRUD = new CategoriesCRUD();
        categoriesData = FXCollections.observableArrayList();

        // Configurer les colonnes du TableView
        setupTableColumns();

        // Charger les données
        loadCategories();

        // Activer/désactiver les boutons selon la sélection
        tableCategories.getSelectionModel().selectedItemProperty().addListener(
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
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colSaison.setCellValueFactory(new PropertyValueFactory<>("saison"));
        colNiveauIntensite.setCellValueFactory(new PropertyValueFactory<>("niveauintensite"));
        colPublicCible.setCellValueFactory(new PropertyValueFactory<>("publiccible"));

        // Style personnalisé pour le niveau d'intensité
        colNiveauIntensite.setCellFactory(column -> new TableCell<Categories, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item.toLowerCase()) {
                        case "faible":
                            setStyle("-fx-text-fill: #34d399; -fx-font-weight: bold;");
                            break;
                        case "moyen":
                            setStyle("-fx-text-fill: #fbbf24; -fx-font-weight: bold;");
                            break;
                        case "élevé":
                        case "eleve":
                            setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
                            break;
                        default:
                            setStyle("-fx-text-fill: #ff8c42; -fx-font-weight: bold;");
                    }
                }
            }
        });

        // Style pour la saison
        colSaison.setCellFactory(column -> new TableCell<Categories, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: #60a5fa; -fx-font-weight: bold;");
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
                tableCategories.setItems(categoriesData);
            } else {
                ObservableList<Categories> filteredData = FXCollections.observableArrayList();
                String lowerCaseFilter = newValue.toLowerCase();

                for (Categories categorie : categoriesData) {
                    if (categorie.getNom().toLowerCase().contains(lowerCaseFilter) ||
                            categorie.getDescription().toLowerCase().contains(lowerCaseFilter) ||
                            categorie.getType().toLowerCase().contains(lowerCaseFilter) ||
                            categorie.getSaison().toLowerCase().contains(lowerCaseFilter)) {
                        filteredData.add(categorie);
                    }
                }
                tableCategories.setItems(filteredData);
            }
        });
    }

    /**
     * Charge toutes les catégories depuis la base de données
     */
    private void loadCategories() {
        try {
            List<Categories> listeCategories = categoriesCRUD.afficher();
            categoriesData.clear();
            categoriesData.addAll(listeCategories);
            tableCategories.setItems(categoriesData);

            // Mettre à jour le compteur
            lblTotalCategories.setText(String.valueOf(categoriesData.size()));

        } catch (SQLException e) {
            showError("Erreur de chargement",
                    "Impossible de charger les catégories : " + e.getMessage());
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
     * Valide le champ type
     */
    private boolean validateType(ComboBox<String> combo, Label errorLabel) {
        combo.setStyle("-fx-border-color: #2d3a5f; -fx-border-width: 1;");
        errorLabel.setText("");

        if (combo.getValue() == null) {
            combo.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2;");
            errorLabel.setText("Type : veuillez sélectionner un type");
            return false;
        }

        return true;
    }

    /**
     * Valide le champ saison
     */
    private boolean validateSaison(ComboBox<String> combo, Label errorLabel) {
        combo.setStyle("-fx-border-color: #2d3a5f; -fx-border-width: 1;");
        errorLabel.setText("");

        if (combo.getValue() == null) {
            combo.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2;");
            errorLabel.setText("Saison : veuillez sélectionner une saison");
            return false;
        }

        return true;
    }

    /**
     * Valide le champ niveau d'intensité
     */
    private boolean validateIntensite(ComboBox<String> combo, Label errorLabel) {
        combo.setStyle("-fx-border-color: #2d3a5f; -fx-border-width: 1;");
        errorLabel.setText("");

        if (combo.getValue() == null) {
            combo.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2;");
            errorLabel.setText("Niveau d'intensité : veuillez faire une sélection");
            return false;
        }

        return true;
    }

    /**
     * Valide le champ public cible
     */
    private boolean validatePublicCible(TextField champ, Label errorLabel) {
        String texte = champ.getText();
        champ.setStyle("-fx-border-color: #2d3a5f; -fx-border-width: 1;");
        errorLabel.setText("");

        if (texte == null || texte.trim().isEmpty()) {
            champ.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2;");
            errorLabel.setText("Public cible : Ce champ est obligatoire");
            return false;
        }

        String trimmed = texte.trim();
        if (trimmed.length() < PUBLIC_MIN_LENGTH) {
            champ.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2;");
            errorLabel.setText("Public cible : doit contenir au moins " + PUBLIC_MIN_LENGTH + " caractères");
            return false;
        }

        if (trimmed.length() > PUBLIC_MAX_LENGTH) {
            champ.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2;");
            errorLabel.setText("Public cible : ne doit pas dépasser " + PUBLIC_MAX_LENGTH + " caractères");
            return false;
        }

        if (!PUBLIC_PATTERN.matcher(trimmed).matches()) {
            champ.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2;");
            errorLabel.setText("Public cible : format invalide (lettres, espaces et tirets uniquement)");
            return false;
        }

        return true;
    }

    /**
     * Ajouter une nouvelle catégorie avec validation
     */
    @FXML
    private void handleAjouter() {
        Dialog<Categories> dialog = new Dialog<>();
        dialog.setTitle("Nouvelle Catégorie");
        dialog.setHeaderText("Ajouter une nouvelle catégorie");

        // Créer le formulaire
        GridPane grid = createFormGrid();

        TextField nomField = new TextField();
        nomField.setPromptText("Nom de la catégorie (min " + NOM_MIN_LENGTH + " caractères)");

        TextArea descriptionField = new TextArea();
        descriptionField.setPromptText("Description (min " + DESCRIPTION_MIN_LENGTH + " caractères)");
        descriptionField.setPrefRowCount(3);

        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("Aventure", "Détente", "Culturel", "Sportif", "Gastronomique", "Famille", "Nature");
        typeCombo.setPromptText("Type de catégorie");

        ComboBox<String> saisonCombo = new ComboBox<>();
        saisonCombo.getItems().addAll("Printemps", "Été", "Automne", "Hiver", "Toutes saisons");
        saisonCombo.setPromptText("Saison recommandée");

        ComboBox<String> intensiteCombo = new ComboBox<>();
        intensiteCombo.getItems().addAll("Faible", "Moyen", "Élevé");
        intensiteCombo.setPromptText("Niveau d'intensité");

        TextField publicCibleField = new TextField();
        publicCibleField.setPromptText("Public cible (min " + PUBLIC_MIN_LENGTH + " caractères)");

        // Labels d'erreur
        Label errorNom = createErrorLabel();
        Label errorDescription = createErrorLabel();
        Label errorType = createErrorLabel();
        Label errorSaison = createErrorLabel();
        Label errorIntensite = createErrorLabel();
        Label errorPublicCible = createErrorLabel();

        // Ajouter les champs au formulaire avec leurs labels d'erreur
        int row = 0;
        grid.add(new Label("Nom:*"), 0, row);
        VBox vboxNom = new VBox(3);
        vboxNom.getChildren().addAll(nomField, errorNom);
        grid.add(vboxNom, 1, row++);

        grid.add(new Label("Description:*"), 0, row);
        VBox vboxDescription = new VBox(3);
        vboxDescription.getChildren().addAll(descriptionField, errorDescription);
        grid.add(vboxDescription, 1, row++);

        grid.add(new Label("Type:*"), 0, row);
        VBox vboxType = new VBox(3);
        vboxType.getChildren().addAll(typeCombo, errorType);
        grid.add(vboxType, 1, row++);

        grid.add(new Label("Saison:*"), 0, row);
        VBox vboxSaison = new VBox(3);
        vboxSaison.getChildren().addAll(saisonCombo, errorSaison);
        grid.add(vboxSaison, 1, row++);

        grid.add(new Label("Niveau d'intensité:*"), 0, row);
        VBox vboxIntensite = new VBox(3);
        vboxIntensite.getChildren().addAll(intensiteCombo, errorIntensite);
        grid.add(vboxIntensite, 1, row++);

        grid.add(new Label("Public cible:*"), 0, row);
        VBox vboxPublicCible = new VBox(3);
        vboxPublicCible.getChildren().addAll(publicCibleField, errorPublicCible);
        grid.add(vboxPublicCible, 1, row++);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Validation avant de fermer le dialogue
        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            boolean isValid = validateNom(nomField, errorNom)
                    && validateDescription(descriptionField, errorDescription)
                    && validateType(typeCombo, errorType)
                    && validateSaison(saisonCombo, errorSaison)
                    && validateIntensite(intensiteCombo, errorIntensite)
                    && validatePublicCible(publicCibleField, errorPublicCible);

            if (!isValid) {
                event.consume(); // Empêche la fermeture du dialogue
            }
        });

        // Convertir le résultat
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                Categories categorie = new Categories();
                categorie.setNom(nomField.getText().trim());
                categorie.setDescription(descriptionField.getText().trim());
                categorie.setType(typeCombo.getValue());
                categorie.setSaison(saisonCombo.getValue());
                categorie.setNiveauintensite(intensiteCombo.getValue());
                categorie.setPubliccible(publicCibleField.getText().trim());

                return categorie;
            }
            return null;
        });

        Optional<Categories> result = dialog.showAndWait();

        result.ifPresent(categorie -> {
            try {
                categoriesCRUD.ajouter(categorie);
                showInfo("Succès", "Catégorie ajoutée avec succès !");
                loadCategories();
            } catch (SQLException e) {
                showError("Erreur d'ajout",
                        "Impossible d'ajouter la catégorie : " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * Modifier la catégorie sélectionnée avec validation
     */
    @FXML
    private void handleModifier() {
        Categories selectedCategorie = tableCategories.getSelectionModel().getSelectedItem();

        if (selectedCategorie == null) {
            showWarning("Aucune sélection", "Veuillez sélectionner une catégorie à modifier.");
            return;
        }

        Dialog<Categories> dialog = new Dialog<>();
        dialog.setTitle("Modifier Catégorie");
        dialog.setHeaderText("Modifier la catégorie: " + selectedCategorie.getNom());

        // Créer le formulaire pré-rempli
        GridPane grid = createFormGrid();

        TextField nomField = new TextField(selectedCategorie.getNom());
        TextArea descriptionField = new TextArea(selectedCategorie.getDescription());
        descriptionField.setPrefRowCount(3);

        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("Aventure", "Détente", "Culturel", "Sportif", "Gastronomique", "Famille", "Nature");
        typeCombo.setValue(selectedCategorie.getType());

        ComboBox<String> saisonCombo = new ComboBox<>();
        saisonCombo.getItems().addAll("Printemps", "Été", "Automne", "Hiver", "Toutes saisons");
        saisonCombo.setValue(selectedCategorie.getSaison());

        ComboBox<String> intensiteCombo = new ComboBox<>();
        intensiteCombo.getItems().addAll("Faible", "Moyen", "Élevé");
        intensiteCombo.setValue(selectedCategorie.getNiveauintensite());

        TextField publicCibleField = new TextField(selectedCategorie.getPubliccible());

        // Labels d'erreur
        Label errorNom = createErrorLabel();
        Label errorDescription = createErrorLabel();
        Label errorType = createErrorLabel();
        Label errorSaison = createErrorLabel();
        Label errorIntensite = createErrorLabel();
        Label errorPublicCible = createErrorLabel();

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

        grid.add(new Label("Type:*"), 0, row);
        VBox vboxType = new VBox(3);
        vboxType.getChildren().addAll(typeCombo, errorType);
        grid.add(vboxType, 1, row++);

        grid.add(new Label("Saison:*"), 0, row);
        VBox vboxSaison = new VBox(3);
        vboxSaison.getChildren().addAll(saisonCombo, errorSaison);
        grid.add(vboxSaison, 1, row++);

        grid.add(new Label("Niveau d'intensité:*"), 0, row);
        VBox vboxIntensite = new VBox(3);
        vboxIntensite.getChildren().addAll(intensiteCombo, errorIntensite);
        grid.add(vboxIntensite, 1, row++);

        grid.add(new Label("Public cible:*"), 0, row);
        VBox vboxPublicCible = new VBox(3);
        vboxPublicCible.getChildren().addAll(publicCibleField, errorPublicCible);
        grid.add(vboxPublicCible, 1, row++);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Validation avant de fermer le dialogue
        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            boolean isValid = validateNom(nomField, errorNom)
                    && validateDescription(descriptionField, errorDescription)
                    && validateType(typeCombo, errorType)
                    && validateSaison(saisonCombo, errorSaison)
                    && validateIntensite(intensiteCombo, errorIntensite)
                    && validatePublicCible(publicCibleField, errorPublicCible);

            if (!isValid) {
                event.consume();
            }
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                selectedCategorie.setNom(nomField.getText().trim());
                selectedCategorie.setDescription(descriptionField.getText().trim());
                selectedCategorie.setType(typeCombo.getValue());
                selectedCategorie.setSaison(saisonCombo.getValue());
                selectedCategorie.setNiveauintensite(intensiteCombo.getValue());
                selectedCategorie.setPubliccible(publicCibleField.getText().trim());

                return selectedCategorie;
            }
            return null;
        });

        Optional<Categories> result = dialog.showAndWait();

        result.ifPresent(categorie -> {
            try {
                categoriesCRUD.modifier(categorie);
                showInfo("Succès", "Catégorie modifiée avec succès !");
                loadCategories();
            } catch (SQLException e) {
                showError("Erreur de modification",
                        "Impossible de modifier la catégorie : " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * Supprimer la catégorie sélectionnée
     */
    @FXML
    private void handleSupprimer() {
        Categories selectedCategorie = tableCategories.getSelectionModel().getSelectedItem();

        if (selectedCategorie == null) {
            showWarning("Aucune sélection", "Veuillez sélectionner une catégorie à supprimer.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText("Supprimer la catégorie \"" + selectedCategorie.getNom() + "\" ?");
        confirmation.setContentText("Cette action est irréversible.");

        Optional<ButtonType> result = confirmation.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                categoriesCRUD.supprimer(selectedCategorie.getId());
                showInfo("Succès", "Catégorie supprimée avec succès !");
                loadCategories();
            } catch (SQLException e) {
                showError("Erreur de suppression",
                        "Impossible de supprimer la catégorie : " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Actualiser la liste des catégories
     */
    @FXML
    private void handleActualiser() {
        loadCategories();
        showInfo("Actualisation", "Liste des catégories actualisée !");
    }

    /**
     * Naviguer vers l'interface front office des catégories
     */
    @FXML
    private void handleFrontOffice() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/categoriesfront.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) btnFrontOffice.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("TravelMate - Front Office Catégories");
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
        grid.setPadding(new Insets(20, 150, 10, 10));
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