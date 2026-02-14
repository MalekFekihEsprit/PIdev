package Controlles;

import Entites.Activites;
import Services.ActivitesCRUD;
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

public class ACTfront implements Initializable {

    @FXML private GridPane activitesGrid;
    @FXML private Label lblTotalActivites;
    @FXML private Button btnBackOffice;
    @FXML private HBox btnVersCategories;
    @FXML private ComboBox<String> filterTypeCombo;
    @FXML private ComboBox<String> filterSaisonCombo;
    @FXML private ComboBox<String> filterDifficulteCombo;

    private ActivitesCRUD activitesCRUD;
    private ObservableList<Activites> activitesList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        activitesCRUD = new ActivitesCRUD();
        activitesList = FXCollections.observableArrayList();

        // Initialiser les filtres
        setupFilters();

        // Charger les activités
        loadActivites();

        // Mettre à jour le compteur
        updateTotalCount();
    }

    private void setupFilters() {
        // Filtres par type de catégorie
        filterTypeCombo.getItems().addAll("Tous les types", "Aventure", "Détente", "Culturel", "Sportif", "Gastronomique", "Famille", "Nature");
        filterTypeCombo.setValue("Tous les types");
        filterTypeCombo.setOnAction(event -> applyFilters());

        // Filtres par saison
        filterSaisonCombo.getItems().addAll("Toutes saisons", "Printemps", "Été", "Automne", "Hiver");
        filterSaisonCombo.setValue("Toutes saisons");
        filterSaisonCombo.setOnAction(event -> applyFilters());

        // Filtres par difficulté
        filterDifficulteCombo.getItems().addAll("Tous niveaux", "Facile", "Moyen", "Difficile", "Expert");
        filterDifficulteCombo.setValue("Tous niveaux");
        filterDifficulteCombo.setOnAction(event -> applyFilters());
    }

    private void loadActivites() {
        try {
            List<Activites> liste = activitesCRUD.afficher();
            activitesList.clear();
            activitesList.addAll(liste);
            displayActivites(activitesList);
        } catch (SQLException e) {
            showError("Erreur de chargement", "Impossible de charger les activités: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void applyFilters() {
        String selectedType = filterTypeCombo.getValue();
        String selectedSaison = filterSaisonCombo.getValue();
        String selectedDifficulte = filterDifficulteCombo.getValue();

        ObservableList<Activites> filteredList = FXCollections.observableArrayList();

        for (Activites activite : activitesList) {
            boolean matchType = selectedType.equals("Tous les types") ||
                    (activite.getCategorie() != null && activite.getCategorie().getType().equals(selectedType));

            boolean matchSaison = selectedSaison.equals("Toutes saisons") ||
                    (activite.getCategorie() != null && activite.getCategorie().getSaison().equals(selectedSaison));

            boolean matchDifficulte = selectedDifficulte.equals("Tous niveaux") ||
                    activite.getNiveaudifficulte().equals(selectedDifficulte);

            if (matchType && matchSaison && matchDifficulte) {
                filteredList.add(activite);
            }
        }

        displayActivites(filteredList);
        updateTotalCount(filteredList.size());
    }

    private void displayActivites(ObservableList<Activites> activites) {
        activitesGrid.getChildren().clear();
        int column = 0;
        int row = 0;

        for (Activites activite : activites) {
            VBox card = createActivityCard(activite);
            activitesGrid.add(card, column, row);

            column++;
            if (column >= 3) {
                column = 0;
                row++;
            }
        }
    }

    private VBox createActivityCard(Activites activite) {
        VBox card = new VBox(12);
        card.setStyle("-fx-background-color: #1e2749; -fx-background-radius: 15; -fx-padding: 15; -fx-border-color: #ff8c42; -fx-border-width: 2; -fx-border-radius: 15;");
        card.setPrefWidth(350);
        card.setPrefHeight(280);

        // En-tête avec icône et titre
        HBox header = new HBox(10);
        header.setStyle("-fx-alignment: CENTER_LEFT;");

        Label iconLabel = new Label("🎯");
        iconLabel.setStyle("-fx-font-size: 24;");

        Label titleLabel = new Label(activite.getNom());
        titleLabel.setStyle("-fx-font-family: 'Clash Display'; -fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #ffffff;");
        titleLabel.setWrapText(true);

        header.getChildren().addAll(iconLabel, titleLabel);

        // Description
        Label descriptionLabel = new Label(activite.getDescription());
        descriptionLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 13;");
        descriptionLabel.setWrapText(true);
        descriptionLabel.setPrefHeight(60);

        // Informations
        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(10);
        infoGrid.setVgap(8);

        // Lieu
        Label lieuIcon = new Label("📍");
        Label lieuLabel = new Label(activite.getLieu());
        lieuLabel.setStyle("-fx-text-fill: #e2e8f0; -fx-font-size: 12;");
        infoGrid.add(lieuIcon, 0, 0);
        infoGrid.add(lieuLabel, 1, 0);

        // Budget
        Label budgetIcon = new Label("💰");
        Label budgetLabel = new Label(activite.getBudget() + " €");
        budgetLabel.setStyle("-fx-text-fill: #ff8c42; -fx-font-size: 12; -fx-font-weight: bold;");
        infoGrid.add(budgetIcon, 0, 1);
        infoGrid.add(budgetLabel, 1, 1);

        // Durée
        Label dureeIcon = new Label("⏱️");
        Label dureeLabel = new Label(activite.getDuree() + "h");
        dureeLabel.setStyle("-fx-text-fill: #e2e8f0; -fx-font-size: 12;");
        infoGrid.add(dureeIcon, 0, 2);
        infoGrid.add(dureeLabel, 1, 2);

        // Difficulté
        Label difficulteIcon = new Label("📊");
        Label difficulteLabel = new Label(activite.getNiveaudifficulte());
        difficulteLabel.setStyle(getDifficultyStyle(activite.getNiveaudifficulte()));
        infoGrid.add(difficulteIcon, 0, 3);
        infoGrid.add(difficulteLabel, 1, 3);

        // Âge minimum
        Label ageIcon = new Label("👤");
        Label ageLabel = new Label(activite.getAgemin() + "+ ans");
        ageLabel.setStyle("-fx-text-fill: #e2e8f0; -fx-font-size: 12;");
        infoGrid.add(ageIcon, 0, 4);
        infoGrid.add(ageLabel, 1, 4);

        // Catégorie
        if (activite.getCategorie() != null) {
            Label categorieIcon = new Label("📑");
            Label categorieLabel = new Label(activite.getCategorie().getNom());
            categorieLabel.setStyle("-fx-text-fill: #60a5fa; -fx-font-size: 12; -fx-font-weight: bold;");
            infoGrid.add(categorieIcon, 0, 5);
            infoGrid.add(categorieLabel, 1, 5);
        }

        // Statut
        Label statutIcon = new Label("⚡");
        Label statutLabel = new Label(activite.getStatut());
        statutLabel.setStyle(getStatusStyle(activite.getStatut()));
        infoGrid.add(statutIcon, 0, 6);
        infoGrid.add(statutLabel, 1, 6);

        card.getChildren().addAll(header, descriptionLabel, infoGrid);

        return card;
    }

    private String getDifficultyStyle(String difficulte) {
        switch (difficulte.toLowerCase()) {
            case "facile":
                return "-fx-text-fill: #34d399; -fx-font-size: 12; -fx-font-weight: bold;";
            case "moyen":
                return "-fx-text-fill: #fbbf24; -fx-font-size: 12; -fx-font-weight: bold;";
            case "difficile":
                return "-fx-text-fill: #ef4444; -fx-font-size: 12; -fx-font-weight: bold;";
            case "expert":
                return "-fx-text-fill: #ff8c42; -fx-font-size: 12; -fx-font-weight: bold;";
            default:
                return "-fx-text-fill: #e2e8f0; -fx-font-size: 12;";
        }
    }

    private String getStatusStyle(String statut) {
        if (statut.equalsIgnoreCase("active")) {
            return "-fx-text-fill: #34d399; -fx-font-size: 12; -fx-font-weight: bold;";
        } else {
            return "-fx-text-fill: #ef4444; -fx-font-size: 12; -fx-font-weight: bold;";
        }
    }

    private void updateTotalCount() {
        updateTotalCount(activitesList.size());
    }

    private void updateTotalCount(int count) {
        lblTotalActivites.setText(String.valueOf(count));
    }

    @FXML
    private void handleBackOffice() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/activitesback.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) btnBackOffice.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("TravelMate - Back Office Activités");
            stage.show();
        } catch (IOException e) {
            showError("Erreur de navigation", "Impossible de charger le back office: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML // ← Ajout de l'annotation @FXML ici
    private void handleVersCategories() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/categoriesfront.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) btnVersCategories.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("TravelMate - Front Office Catégories");
            stage.show();
        } catch (IOException e) {
            showError("Erreur de navigation", "Impossible de charger les catégories: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleResetFilters() {
        filterTypeCombo.setValue("Tous les types");
        filterSaisonCombo.setValue("Toutes saisons");
        filterDifficulteCombo.setValue("Tous niveaux");
        displayActivites(activitesList);
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
