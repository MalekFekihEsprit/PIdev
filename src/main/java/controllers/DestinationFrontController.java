package controllers;

import entities.Destination;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import services.DestinationCRUD;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class DestinationFrontController implements Initializable {

    // ============== FXML INJECTED ELEMENTS ==============

    // Top Navigation
    @FXML private Label lblSearchPlaceholder;
    @FXML private HBox btnHebergement;
    @FXML private HBox btnItineraires;
    @FXML private HBox btnActivites;
    @FXML private HBox btnVoyages;
    @FXML private HBox btnBudgets;

    // Bottom Status
    @FXML private Label lblLastUpdate;

    // Header Stats
    @FXML private Label lblStatut;
    @FXML private Label lblClimatDiversite;
    @FXML private Label lblSaisons;
    @FXML private Label lblPaysCount;

    // KPI Cards
    @FXML private Label lblTotalDestinations;
    @FXML private Label lblTotalPays;
    @FXML private Label lblDestinationsActives;
    @FXML private Label lblActivesPourcentage;
    @FXML private Label lblClimatsCount;
    @FXML private Label lblSaisonsCount;
    @FXML private ProgressBar progressSaisons;

    // Table Section
    @FXML private Label lblDestinationCount;
    @FXML private TableView<Destination> tableDestinations;
    @FXML private TableColumn<Destination, String> colNom;
    @FXML private TableColumn<Destination, String> colPays;
    @FXML private TableColumn<Destination, String> colDescription;
    @FXML private TableColumn<Destination, String> colClimat;
    @FXML private TableColumn<Destination, String> colSaison;
    @FXML private TableColumn<Destination, Void> colActions;

    // Buttons
    @FXML private HBox btnRefresh;
    @FXML private HBox btnSearch;
    @FXML private HBox btnFilter;
    @FXML private HBox btnHome;

    // ============== CLASS VARIABLES ==============

    private DestinationCRUD destinationCRUD;
    private ObservableList<Destination> destinationList = FXCollections.observableArrayList();
    private List<Destination> allDestinations = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        destinationCRUD = new DestinationCRUD();

        // Initialize table columns
        setupTableColumns();

        // Set table to show exactly 10 rows with scrolling for more
        setupTableHeight();

        // Load data from database
        loadDestinations();

        // Setup button actions
        setupButtonActions();

        // Update last update time
        updateLastUpdateTime();

        // Setup navigation buttons
        setupNavigationButtons();
    }

    private void setupTableColumns() {
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom_destination"));
        colPays.setCellValueFactory(new PropertyValueFactory<>("pays_destination"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description_destination"));
        colClimat.setCellValueFactory(new PropertyValueFactory<>("climat_destination"));
        colSaison.setCellValueFactory(new PropertyValueFactory<>("saison_destination"));

        // Setup actions column with ONLY consulter button
        setupActionsColumn();
    }

    private void setupTableHeight() {
        // Fixed row height of 35 pixels
        tableDestinations.setFixedCellSize(35);
        // Height for exactly 10 rows + header (approximately 35*10 + 30 for header)
        tableDestinations.setPrefHeight(380);
        tableDestinations.setMaxHeight(380);
        tableDestinations.setMinHeight(380);
    }

    private void setupActionsColumn() {
        colActions.setCellFactory(col -> new TableCell<>() {
            private final HBox btnConsulter = new HBox();

            {
                // Style for consulter button
                btnConsulter.setAlignment(javafx.geometry.Pos.CENTER);
                btnConsulter.setStyle("-fx-background-color: #3b82f6; -fx-background-radius: 8; -fx-min-width: 32; -fx-min-height: 32; -fx-cursor: hand;");
                Label consulterIcon = new Label("👁️");
                consulterIcon.setStyle("-fx-font-size: 16; -fx-text-fill: white;");
                btnConsulter.getChildren().add(consulterIcon);
                btnConsulter.setPadding(new Insets(4, 0, 4, 0));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Destination destination = getTableView().getItems().get(getIndex());
                    btnConsulter.setOnMouseClicked(event -> handleConsulter(destination));
                    setGraphic(btnConsulter);
                }
            }
        });
    }

    private void setupButtonActions() {
        if (btnRefresh != null) {
            btnRefresh.setOnMouseClicked(event -> refreshData());
        }
        if (btnSearch != null) {
            btnSearch.setOnMouseClicked(event -> handleSearch());
        }
        if (btnFilter != null) {
            btnFilter.setOnMouseClicked(event -> handleFilter());
        }
    }

    private void setupNavigationButtons() {
        // Home button
        if (btnHome != null) {
            btnHome.setOnMouseClicked(event -> navigateToHome());

            // Hover effect
            btnHome.setOnMouseEntered(event -> {
                btnHome.setStyle("-fx-background-color: #ff8c42; -fx-background-radius: 12; -fx-min-width: 40; -fx-min-height: 40; -fx-cursor: hand;");
                btnHome.lookupAll(".label").forEach(label -> {
                    if (label instanceof Label) {
                        ((Label) label).setStyle("-fx-text-fill: white; -fx-font-size: 18;");
                    }
                });
            });

            btnHome.setOnMouseExited(event -> {
                btnHome.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 12; -fx-min-width: 40; -fx-min-height: 40; -fx-cursor: hand;");
                btnHome.lookupAll(".label").forEach(label -> {
                    if (label instanceof Label) {
                        ((Label) label).setStyle("-fx-font-size: 18; -fx-text-fill: #475569;");
                    }
                });
            });
        }

        // Hébergement button
        setupNavButtonHover(btnHebergement, "🏨", "Hébergement");
        if (btnHebergement != null) {
            btnHebergement.setOnMouseClicked(event -> navigateToHebergement());
        }

        // Itinéraires button (commented implementation)
        setupNavButtonHover(btnItineraires, "🗺️", "Itinéraires");
        if (btnItineraires != null) {
            btnItineraires.setOnMouseClicked(event -> {
                // TODO: Implement navigation to Itinéraires when ready
                showInfoAlert("Itinéraires", "Cette fonctionnalité sera bientôt disponible");
            });
        }

        // Activités button (commented implementation)
        setupNavButtonHover(btnActivites, "🏄", "Activités");
        if (btnActivites != null) {
            btnActivites.setOnMouseClicked(event -> {
                // TODO: Implement navigation to Activités when ready
                showInfoAlert("Activités", "Cette fonctionnalité sera bientôt disponible");
            });
        }

        // Voyages button (commented implementation)
        setupNavButtonHover(btnVoyages, "✈️", "Voyages");
        if (btnVoyages != null) {
            btnVoyages.setOnMouseClicked(event -> {
                // TODO: Implement navigation to Voyages when ready
                showInfoAlert("Voyages", "Cette fonctionnalité sera bientôt disponible");
            });
        }

        // Budgets button (commented implementation)
        setupNavButtonHover(btnBudgets, "💰", "Budgets");
        if (btnBudgets != null) {
            btnBudgets.setOnMouseClicked(event -> {
                // TODO: Implement navigation to Budgets when ready
                showInfoAlert("Budgets", "Cette fonctionnalité sera bientôt disponible");
            });
        }
    }

    /**
     * Helper method to setup hover effects for navigation buttons
     */
    private void setupNavButtonHover(HBox button, String icon, String text) {
        if (button == null) return;

        button.setOnMouseEntered(event -> {
            button.setStyle("-fx-background-color: rgba(255,140,66,0.1); -fx-background-radius: 12; -fx-padding: 8 16; -fx-cursor: hand; -fx-border-color: #ff8c42; -fx-border-width: 1; -fx-border-radius: 12;");
            button.lookupAll(".label").forEach(label -> {
                if (label instanceof Label) {
                    Label lbl = (Label) label;
                    if (lbl.getText().equals(icon)) {
                        lbl.setStyle("-fx-font-size: 16;");
                    } else {
                        lbl.setStyle("-fx-text-fill: #ff8c42; -fx-font-weight: 600; -fx-font-size: 14;");
                    }
                }
            });
        });

        button.setOnMouseExited(event -> {
            button.setStyle("-fx-background-color: transparent; -fx-background-radius: 12; -fx-padding: 8 16; -fx-cursor: hand;");
            button.lookupAll(".label").forEach(label -> {
                if (label instanceof Label) {
                    Label lbl = (Label) label;
                    if (lbl.getText().equals(icon)) {
                        lbl.setStyle("-fx-font-size: 16;");
                    } else {
                        lbl.setStyle("-fx-text-fill: #475569; -fx-font-weight: 500; -fx-font-size: 14;");
                    }
                }
            });
        });
    }

    private void loadDestinations() {
        try {
            allDestinations = destinationCRUD.afficher();
            destinationList.setAll(allDestinations);
            tableDestinations.setItems(destinationList);

            updateStats();

            System.out.println("Loaded " + allDestinations.size() + " destinations");

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les destinations: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateStats() {
        int total = allDestinations.size();
        lblTotalDestinations.setText(String.valueOf(total));
        lblDestinationCount.setText(total + " destination" + (total > 1 ? "s" : ""));
        lblStatut.setText("● " + total + " destination" + (total > 1 ? "s" : ""));

        // Count unique countries
        long paysCount = allDestinations.stream()
                .map(Destination::getPays_destination)
                .filter(Objects::nonNull)
                .distinct()
                .count();
        lblPaysCount.setText(paysCount + " pays représentés");
        lblTotalPays.setText("Dans " + paysCount + " pays différents");

        // Count unique climates
        long climatesCount = allDestinations.stream()
                .map(Destination::getClimat_destination)
                .filter(Objects::nonNull)
                .distinct()
                .count();
        lblClimatsCount.setText(String.valueOf(climatesCount));
        lblClimatDiversite.setText(climatesCount + " climats différents");

        // Count unique seasons
        long seasonsCount = allDestinations.stream()
                .map(Destination::getSaison_destination)
                .filter(Objects::nonNull)
                .distinct()
                .count();
        lblSaisonsCount.setText(String.valueOf(seasonsCount));
        if (seasonsCount > 0) {
            progressSaisons.setProgress(Math.min(seasonsCount / 4.0, 1.0));
        }

        // Active destinations (consider all as active for now)
        lblDestinationsActives.setText(String.valueOf(total));
        double pourcentage = total > 0 ? 100.0 : 0;
        lblActivesPourcentage.setText(String.format("%.0f%% des destinations", pourcentage));
    }

    private void refreshData() {
        loadDestinations();
        updateLastUpdateTime();
        showAlert(Alert.AlertType.INFORMATION, "Succès", "Données rafraîchies avec succès!");
    }

    private void updateLastUpdateTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");
        lblLastUpdate.setText("Dernière mise à jour: " + LocalDateTime.now().format(formatter));
    }

    private void handleSearch() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Recherche");
        dialog.setHeaderText("Rechercher une destination");
        dialog.setContentText("Nom de la destination:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(searchTerm -> {
            if (!searchTerm.trim().isEmpty()) {
                List<Destination> searchResults = allDestinations.stream()
                        .filter(d -> d.getNom_destination().toLowerCase().contains(searchTerm.toLowerCase()) ||
                                d.getPays_destination().toLowerCase().contains(searchTerm.toLowerCase()))
                        .collect(Collectors.toList());

                if (searchResults.isEmpty()) {
                    showAlert(Alert.AlertType.INFORMATION, "Résultat", "Aucune destination trouvée");
                } else {
                    destinationList.setAll(searchResults);
                    tableDestinations.setItems(destinationList);
                    lblDestinationCount.setText(searchResults.size() + " destination" + (searchResults.size() > 1 ? "s" : ""));
                }
            }
        });
    }

    private void handleFilter() {
        List<String> climates = allDestinations.stream()
                .map(Destination::getClimat_destination)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        climates.add(0, "Tous");

        ChoiceDialog<String> dialog = new ChoiceDialog<>("Tous", climates);
        dialog.setTitle("Filtre");
        dialog.setHeaderText("Filtrer par climat");
        dialog.setContentText("Choisissez un climat:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(climate -> {
            if ("Tous".equals(climate)) {
                destinationList.setAll(allDestinations);
                tableDestinations.setItems(destinationList);
                lblDestinationCount.setText(allDestinations.size() + " destination" + (allDestinations.size() > 1 ? "s" : ""));
            } else {
                List<Destination> filteredResults = allDestinations.stream()
                        .filter(d -> climate.equals(d.getClimat_destination()))
                        .collect(Collectors.toList());

                if (filteredResults.isEmpty()) {
                    showAlert(Alert.AlertType.INFORMATION, "Résultat", "Aucune destination trouvée pour ce climat");
                } else {
                    destinationList.setAll(filteredResults);
                    tableDestinations.setItems(destinationList);
                    lblDestinationCount.setText(filteredResults.size() + " destination" + (filteredResults.size() > 1 ? "s" : ""));
                }
            }
        });
    }

    private void handleConsulter(Destination destination) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherDestinationFront.fxml"));
            Parent root = loader.load();

            AfficherDestinationfrontController controller = loader.getController();
            controller.setDestination(destination);

            Stage stage = new Stage();
            stage.setTitle("Détails - " + destination.getNom_destination());
            stage.setScene(new Scene(root));
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir les détails: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void navigateToHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/HomePage.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) tableDestinations.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TravelMate - Accueil");
            stage.setMaximized(true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Navigates to the Hébergement front interface
     */
    private void navigateToHebergement() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/HebergementFront.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) btnHebergement.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TravelMate - Hébergements");
            stage.setMaximized(true);

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la page des hébergements: " + e.getMessage());
            e.printStackTrace();
        }
    }
}