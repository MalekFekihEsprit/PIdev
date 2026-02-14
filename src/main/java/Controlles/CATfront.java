package Controlles;

import Entites.Categories;
import Services.CategoriesCRUD;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class CATfront implements Initializable {

    @FXML private GridPane categoriesGrid;
    @FXML private Label lblTotalCategories;
    @FXML private Button btnBackOffice;
    @FXML private HBox btnVersActivites;
    @FXML private ComboBox<String> filterTypeCombo;
    @FXML private ComboBox<String> filterSaisonCombo;
    @FXML private ComboBox<String> filterIntensiteCombo;

    private CategoriesCRUD categoriesCRUD;
    private ObservableList<Categories> categoriesList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        categoriesCRUD = new CategoriesCRUD();
        categoriesList = FXCollections.observableArrayList();

        // Initialiser les filtres
        setupFilters();

        // Charger les catégories
        loadCategories();

        // Mettre à jour le compteur
        updateTotalCount();
    }

    private void setupFilters() {
        // Filtres par type
        filterTypeCombo.getItems().addAll("Tous les types", "Aventure", "Détente", "Culturel", "Sportif", "Gastronomique", "Famille", "Nature");
        filterTypeCombo.setValue("Tous les types");
        filterTypeCombo.setOnAction(event -> applyFilters());

        // Filtres par saison
        filterSaisonCombo.getItems().addAll("Toutes saisons", "Printemps", "Été", "Automne", "Hiver");
        filterSaisonCombo.setValue("Toutes saisons");
        filterSaisonCombo.setOnAction(event -> applyFilters());

        // Filtres par intensité
        filterIntensiteCombo.getItems().addAll("Tous niveaux", "Faible", "Moyen", "Élevé");
        filterIntensiteCombo.setValue("Tous niveaux");
        filterIntensiteCombo.setOnAction(event -> applyFilters());
    }

    private void loadCategories() {
        try {
            List<Categories> liste = categoriesCRUD.afficher();
            categoriesList.clear();
            categoriesList.addAll(liste);
            displayCategories(categoriesList);
        } catch (SQLException e) {
            showError("Erreur de chargement", "Impossible de charger les catégories: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void applyFilters() {
        String selectedType = filterTypeCombo.getValue();
        String selectedSaison = filterSaisonCombo.getValue();
        String selectedIntensite = filterIntensiteCombo.getValue();

        ObservableList<Categories> filteredList = FXCollections.observableArrayList();

        for (Categories categorie : categoriesList) {
            boolean matchType = selectedType.equals("Tous les types") ||
                    (categorie.getType() != null && categorie.getType().equals(selectedType));

            boolean matchSaison = selectedSaison.equals("Toutes saisons") ||
                    (categorie.getSaison() != null && categorie.getSaison().equals(selectedSaison));

            boolean matchIntensite = selectedIntensite.equals("Tous niveaux") ||
                    (categorie.getNiveauintensite() != null && categorie.getNiveauintensite().equals(selectedIntensite));

            if (matchType && matchSaison && matchIntensite) {
                filteredList.add(categorie);
            }
        }

        displayCategories(filteredList);
        updateTotalCount(filteredList.size());
    }

    private void displayCategories(ObservableList<Categories> categories) {
        categoriesGrid.getChildren().clear();
        int column = 0;
        int row = 0;

        for (Categories categorie : categories) {
            VBox card = createCategoryCard(categorie);
            categoriesGrid.add(card, column, row);

            column++;
            if (column >= 3) {
                column = 0;
                row++;
            }
        }
    }

    private VBox createCategoryCard(Categories categorie) {
        VBox card = new VBox(12);
        card.setStyle("-fx-background-color: #1e2749; -fx-background-radius: 15; -fx-padding: 15; -fx-border-color: #ff8c42; -fx-border-width: 2; -fx-border-radius: 15;");
        card.setPrefWidth(350);
        card.setPrefHeight(250);

        // En-tête avec icône et titre
        HBox header = new HBox(10);
        header.setStyle("-fx-alignment: CENTER_LEFT;");

        Label iconLabel = new Label("📑");
        iconLabel.setStyle("-fx-font-size: 24;");

        Label titleLabel = new Label(categorie.getNom());
        titleLabel.setStyle("-fx-font-family: 'Clash Display'; -fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #ffffff;");
        titleLabel.setWrapText(true);

        header.getChildren().addAll(iconLabel, titleLabel);

        // Description
        Label descriptionLabel = new Label(categorie.getDescription());
        descriptionLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 13;");
        descriptionLabel.setWrapText(true);
        descriptionLabel.setPrefHeight(60);

        // Informations
        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(10);
        infoGrid.setVgap(8);

        // Type
        Label typeIcon = new Label("🏷️");
        Label typeLabel = new Label(categorie.getType() != null ? categorie.getType() : "Non spécifié");
        typeLabel.setStyle("-fx-text-fill: #60a5fa; -fx-font-size: 12; -fx-font-weight: bold;");
        infoGrid.add(typeIcon, 0, 0);
        infoGrid.add(typeLabel, 1, 0);

        // Saison
        Label saisonIcon = new Label("🌤️");
        Label saisonLabel = new Label(categorie.getSaison() != null ? categorie.getSaison() : "Toutes saisons");
        saisonLabel.setStyle(getSaisonStyle(categorie.getSaison()));
        infoGrid.add(saisonIcon, 0, 1);
        infoGrid.add(saisonLabel, 1, 1);

        // Niveau d'intensité
        Label intensiteIcon = new Label("📊");
        Label intensiteLabel = new Label(categorie.getNiveauintensite() != null ? categorie.getNiveauintensite() : "Non spécifié");
        intensiteLabel.setStyle(getIntensiteStyle(categorie.getNiveauintensite()));
        infoGrid.add(intensiteIcon, 0, 2);
        infoGrid.add(intensiteLabel, 1, 2);

        // Public cible
        Label publicIcon = new Label("👥");
        Label publicLabel = new Label(categorie.getPubliccible() != null ? categorie.getPubliccible() : "Tous publics");
        publicLabel.setStyle("-fx-text-fill: #e2e8f0; -fx-font-size: 12;");
        infoGrid.add(publicIcon, 0, 3);
        infoGrid.add(publicLabel, 1, 3);

        card.getChildren().addAll(header, descriptionLabel, infoGrid);

        return card;
    }

    private String getSaisonStyle(String saison) {
        if (saison == null) return "-fx-text-fill: #e2e8f0; -fx-font-size: 12;";

        switch (saison.toLowerCase()) {
            case "printemps":
                return "-fx-text-fill: #34d399; -fx-font-size: 12; -fx-font-weight: bold;";
            case "été":
                return "-fx-text-fill: #fbbf24; -fx-font-size: 12; -fx-font-weight: bold;";
            case "automne":
                return "-fx-text-fill: #ff8c42; -fx-font-size: 12; -fx-font-weight: bold;";
            case "hiver":
                return "-fx-text-fill: #60a5fa; -fx-font-size: 12; -fx-font-weight: bold;";
            default:
                return "-fx-text-fill: #e2e8f0; -fx-font-size: 12;";
        }
    }

    private String getIntensiteStyle(String intensite) {
        if (intensite == null) return "-fx-text-fill: #e2e8f0; -fx-font-size: 12;";

        switch (intensite.toLowerCase()) {
            case "faible":
                return "-fx-text-fill: #34d399; -fx-font-size: 12; -fx-font-weight: bold;";
            case "moyen":
                return "-fx-text-fill: #fbbf24; -fx-font-size: 12; -fx-font-weight: bold;";
            case "élevé":
                return "-fx-text-fill: #ef4444; -fx-font-size: 12; -fx-font-weight: bold;";
            default:
                return "-fx-text-fill: #e2e8f0; -fx-font-size: 12;";
        }
    }

    private void updateTotalCount() {
        updateTotalCount(categoriesList.size());
    }

    private void updateTotalCount(int count) {
        lblTotalCategories.setText(String.valueOf(count));
    }

    @FXML
    private void handleBackOffice() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/categoriesback.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) btnBackOffice.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("TravelMate - Back Office Catégories");
            stage.show();
        } catch (IOException e) {
            showError("Erreur de navigation", "Impossible de charger le back office: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML // ← Ajout de l'annotation @FXML ici
    private void handleVersActivites() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/activitesfront.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) btnVersActivites.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("TravelMate - Front Office Activités");
            stage.show();
        } catch (IOException e) {
            showError("Erreur de navigation", "Impossible de charger les activités: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleResetFilters() {
        filterTypeCombo.setValue("Tous les types");
        filterSaisonCombo.setValue("Toutes saisons");
        filterIntensiteCombo.setValue("Tous niveaux");
        displayCategories(categoriesList);
        updateTotalCount();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}