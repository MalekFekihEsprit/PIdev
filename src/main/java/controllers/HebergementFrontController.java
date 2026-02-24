package controllers;

import entities.Hebergement;
import entities.Destination;
import services.HebergementCRUD;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class HebergementFrontController implements Initializable {

    // ============== FXML INJECTED ELEMENTS ==============

    // Top Navigation
    @FXML private Label lblSearchPlaceholder;
    @FXML private HBox btnDestinations;
    @FXML private HBox btnHome;

    // Bottom Status
    @FXML private Label lblLastUpdate;

    // Header Stats
    @FXML private Label lblStatut;
    @FXML private Label lblTypesCount;
    @FXML private Label lblNoteMoyenne;
    @FXML private Label lblDestinationsCount;

    // KPI Cards
    @FXML private Label lblTotalHebergements;
    @FXML private Label lblTotalDestinationsLiees;
    @FXML private Label lblPrixMoyen;
    @FXML private Label lblNoteMoyenneKPI;
    @FXML private Label lblScoreMoyen;
    @FXML private ProgressBar progressNote;

    // Table Section
    @FXML private Label lblHebergementCount;
    @FXML private TableView<Hebergement> tableHebergements;
    @FXML private TableColumn<Hebergement, String> colNom;
    @FXML private TableColumn<Hebergement, String> colType;
    @FXML private TableColumn<Hebergement, Double> colPrix;
    @FXML private TableColumn<Hebergement, String> colAdresse;
    @FXML private TableColumn<Hebergement, Double> colNote;
    @FXML private TableColumn<Hebergement, String> colDestination;
    @FXML private TableColumn<Hebergement, Void> colActions;

    // Buttons
    @FXML private HBox btnExport;
    @FXML private HBox btnRefresh;
    @FXML private HBox btnSearch;
    @FXML private HBox btnFilter;

    // ============== CLASS VARIABLES ==============

    private HebergementCRUD hebergementCRUD;
    private ObservableList<Hebergement> hebergementList = FXCollections.observableArrayList();
    private List<Hebergement> allHebergements = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        hebergementCRUD = new HebergementCRUD();

        // Initialize table columns
        setupTableColumns();

        // Set table height
        setupTableHeight();

        // Load data from database
        loadHebergements();

        // Setup button actions
        setupButtonActions();

        // Update last update time
        updateLastUpdateTime();

        // Setup navigation buttons
        setupNavigationButtons();
    }

    private void setupTableColumns() {
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom_hebergement"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type_hebergement"));
        colPrix.setCellValueFactory(new PropertyValueFactory<>("prixNuit_hebergement"));
        colAdresse.setCellValueFactory(new PropertyValueFactory<>("adresse_hebergement"));
        colNote.setCellValueFactory(new PropertyValueFactory<>("note_hebergement"));

        // Custom cell factory for destination column to show "nom, pays"
        colDestination.setCellValueFactory(cellData -> {
            Hebergement h = cellData.getValue();
            Destination dest = h.getDestination();
            String destDisplay = (dest != null) ?
                    dest.getNom_destination() + ", " + dest.getPays_destination() : "Non spécifié";
            return new javafx.beans.property.SimpleStringProperty(destDisplay);
        });

        // Format price column
        colPrix.setCellFactory(col -> new TableCell<Hebergement, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f €", price));
                }
            }
        });

        // Format note column
        colNote.setCellFactory(col -> new TableCell<Hebergement, Double>() {
            @Override
            protected void updateItem(Double note, boolean empty) {
                super.updateItem(note, empty);
                if (empty || note == null) {
                    setText(null);
                } else {
                    setText(String.format("%.1f ⭐", note));
                }
            }
        });

        // Setup actions column with consulter button
        setupActionsColumn();
    }

    private void setupTableHeight() {
        tableHebergements.setFixedCellSize(35);
        tableHebergements.setPrefHeight(380);
        tableHebergements.setMaxHeight(380);
        tableHebergements.setMinHeight(380);
    }

    private void setupActionsColumn() {
        colActions.setCellFactory(col -> new TableCell<>() {
            private final HBox btnConsulter = new HBox();

            {
                btnConsulter.setAlignment(javafx.geometry.Pos.CENTER);
                btnConsulter.setStyle("-fx-background-color: #3b82f6; -fx-background-radius: 8; -fx-min-width: 32; -fx-min-height: 32; -fx-cursor: hand;");
                Label consulterIcon = new Label("👁️");
                consulterIcon.setStyle("-fx-font-size: 16; -fx-text-fill: white;");
                btnConsulter.getChildren().add(consulterIcon);
                btnConsulter.setPadding(new Insets(4, 0, 4, 0));

                // Tooltip
                Tooltip.install(btnConsulter, new Tooltip("Voir les détails"));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Hebergement hebergement = getTableView().getItems().get(getIndex());
                    btnConsulter.setOnMouseClicked(event -> handleConsulter(hebergement));
                    setGraphic(btnConsulter);
                }
            }
        });
    }

    private void setupButtonActions() {
        if (btnRefresh != null) {
            btnRefresh.setOnMouseClicked(event -> refreshData());
        }
        if (btnExport != null) {
            btnExport.setOnMouseClicked(event -> handleExport());
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

        // Destinations button
        if (btnDestinations != null) {
            btnDestinations.setOnMouseClicked(event -> navigateToDestinations());

            // Hover effect
            btnDestinations.setOnMouseEntered(event -> {
                btnDestinations.setStyle("-fx-background-color: rgba(255,140,66,0.1); -fx-background-radius: 12; -fx-padding: 8 16; -fx-cursor: hand; -fx-border-color: #ff8c42; -fx-border-width: 1; -fx-border-radius: 12;");
                btnDestinations.lookupAll(".label").forEach(label -> {
                    if (label instanceof Label) {
                        Label lbl = (Label) label;
                        if (lbl.getText().equals("🌍")) {
                            lbl.setStyle("-fx-font-size: 16;");
                        } else {
                            lbl.setStyle("-fx-text-fill: #ff8c42; -fx-font-weight: 600; -fx-font-size: 14;");
                        }
                    }
                });
            });

            btnDestinations.setOnMouseExited(event -> {
                btnDestinations.setStyle("-fx-background-color: transparent; -fx-background-radius: 12; -fx-padding: 8 16; -fx-cursor: hand;");
                btnDestinations.lookupAll(".label").forEach(label -> {
                    if (label instanceof Label) {
                        Label lbl = (Label) label;
                        if (lbl.getText().equals("🌍")) {
                            lbl.setStyle("-fx-font-size: 16;");
                        } else {
                            lbl.setStyle("-fx-text-fill: #475569; -fx-font-weight: 500; -fx-font-size: 14;");
                        }
                    }
                });
            });
        }
    }

    private void loadHebergements() {
        try {
            allHebergements = hebergementCRUD.afficher();
            hebergementList.setAll(allHebergements);
            tableHebergements.setItems(hebergementList);

            updateStats();

            System.out.println("Loaded " + allHebergements.size() + " hébergements");

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les hébergements: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateStats() {
        int total = allHebergements.size();
        lblTotalHebergements.setText(String.valueOf(total));
        lblHebergementCount.setText(total + " hébergement" + (total > 1 ? "s" : ""));
        lblStatut.setText("● " + total + " hébergement" + (total > 1 ? "s" : ""));

        // Count unique types
        long typesCount = allHebergements.stream()
                .map(Hebergement::getType_hebergement)
                .filter(Objects::nonNull)
                .distinct()
                .count();
        lblTypesCount.setText(typesCount + " type" + (typesCount > 1 ? "s" : "") + " différents");

        // Count unique destinations
        long destsCount = allHebergements.stream()
                .map(Hebergement::getDestination)
                .filter(Objects::nonNull)
                .map(Destination::getId_destination)
                .distinct()
                .count();
        lblDestinationsCount.setText(destsCount + " destination" + (destsCount > 1 ? "s" : ""));
        lblTotalDestinationsLiees.setText("Dans " + destsCount + " destination" + (destsCount > 1 ? "s" : ""));

        // Average price
        double avgPrice = allHebergements.stream()
                .mapToDouble(Hebergement::getPrixNuit_hebergement)
                .average()
                .orElse(0.0);
        lblPrixMoyen.setText(String.format("%.2f €", avgPrice));

        // Find minimum price
        double minPrice = allHebergements.stream()
                .mapToDouble(Hebergement::getPrixNuit_hebergement)
                .min()
                .orElse(0.0);

        // Update the "Prix minimum" label (you'll need to add this label in FXML or modify the existing one)
        // For now, we'll just update the text of the label that's there
        if (total > 0) {
            // Find the label that shows "Prix minimum" - in your FXML it's a generic label
            // You might want to add a specific fx:id for this label
        }

        // Average note
        double avgNote = allHebergements.stream()
                .mapToDouble(Hebergement::getNote_hebergement)
                .average()
                .orElse(0.0);
        lblNoteMoyenne.setText(String.format("Note moyenne: %.1f ⭐", avgNote));
        lblNoteMoyenneKPI.setText(String.format("%.1f", avgNote));
        progressNote.setProgress(avgNote / 5.0);

        // Average score
        double avgScore = allHebergements.stream()
                .mapToDouble(Hebergement::getScore_hebergement)
                .average()
                .orElse(0.0);
        lblScoreMoyen.setText(String.format("%.2f", avgScore));
    }

    private void refreshData() {
        loadHebergements();
        updateLastUpdateTime();
        showAlert(Alert.AlertType.INFORMATION, "Succès", "Données rafraîchies avec succès!");
    }

    private void updateLastUpdateTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");
        lblLastUpdate.setText("Dernière mise à jour: " + LocalDateTime.now().format(formatter));
    }

    private void handleExport() {
        showAlert(Alert.AlertType.INFORMATION, "Export", "Fonctionnalité d'export à implémenter");
    }

    private void handleSearch() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Recherche");
        dialog.setHeaderText("Rechercher un hébergement");
        dialog.setContentText("Nom de l'hébergement:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(searchTerm -> {
            if (!searchTerm.trim().isEmpty()) {
                List<Hebergement> searchResults = allHebergements.stream()
                        .filter(h -> h.getNom_hebergement().toLowerCase().contains(searchTerm.toLowerCase()) ||
                                h.getAdresse_hebergement().toLowerCase().contains(searchTerm.toLowerCase()))
                        .collect(Collectors.toList());

                if (searchResults.isEmpty()) {
                    showAlert(Alert.AlertType.INFORMATION, "Résultat", "Aucun hébergement trouvé");
                } else {
                    hebergementList.setAll(searchResults);
                    tableHebergements.setItems(hebergementList);
                    lblHebergementCount.setText(searchResults.size() + " résultat" + (searchResults.size() > 1 ? "s" : ""));
                }
            }
        });
    }

    private void handleFilter() {
        List<String> types = allHebergements.stream()
                .map(Hebergement::getType_hebergement)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        if (types.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "Information", "Aucun type disponible pour le filtrage");
            return;
        }

        types.add(0, "Tous");

        ChoiceDialog<String> dialog = new ChoiceDialog<>("Tous", types);
        dialog.setTitle("Filtre");
        dialog.setHeaderText("Filtrer par type");
        dialog.setContentText("Choisissez un type:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(type -> {
            if ("Tous".equals(type)) {
                hebergementList.setAll(allHebergements);
                tableHebergements.setItems(hebergementList);
                lblHebergementCount.setText(allHebergements.size() + " hébergement" + (allHebergements.size() > 1 ? "s" : ""));
            } else {
                List<Hebergement> filteredResults = allHebergements.stream()
                        .filter(h -> type.equals(h.getType_hebergement()))
                        .collect(Collectors.toList());

                if (filteredResults.isEmpty()) {
                    showAlert(Alert.AlertType.INFORMATION, "Résultat", "Aucun hébergement trouvé pour ce type");
                } else {
                    hebergementList.setAll(filteredResults);
                    tableHebergements.setItems(hebergementList);
                    lblHebergementCount.setText(filteredResults.size() + " résultat" + (filteredResults.size() > 1 ? "s" : ""));
                }
            }
        });
    }

    private void handleConsulter(Hebergement hebergement) {
        try {
            // Load the hebergement details FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherHebergementFront.fxml"));
            Parent root = loader.load();

            // Get the controller and pass the hebergement
            AfficherHebergementFrontController controller = loader.getController();
            controller.setHebergement(hebergement);

            // Create a new stage for the details window
            Stage stage = new Stage();
            stage.setTitle("Détails - " + hebergement.getNom_hebergement());
            stage.setScene(new Scene(root));

            // Make it modal (blocks interaction with parent window)
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);

            // Center on parent window
            stage.initOwner(tableHebergements.getScene().getWindow());
            stage.setResizable(false);

            // Show and wait (makes it modal)
            stage.showAndWait();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir les détails: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void navigateToHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/HomePage.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) tableHebergements.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TravelMate - Accueil");
            stage.setMaximized(true);

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de retourner à l'accueil: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void navigateToDestinations() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DestinationFront.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) btnDestinations.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TravelMate - Destinations");
            stage.setMaximized(true);

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir les destinations: " + e.getMessage());
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

    // Add this method to filter by destination
    public void filterByDestination(Destination destination) {
        if (destination == null) return;

        // Filter the list to show only hébergements for this destination
        List<Hebergement> filtered = allHebergements.stream()
                .filter(h -> h.getDestination() != null &&
                        h.getDestination().getId_destination() == destination.getId_destination())
                .collect(Collectors.toList());

        // Update the table with filtered results
        hebergementList.setAll(filtered);
        tableHebergements.setItems(hebergementList);

        // Update the count label
        lblHebergementCount.setText(filtered.size() + " hébergement" + (filtered.size() > 1 ? "s" : "") +
                " à " + destination.getNom_destination());

        // Optionally update the header to show filter info
        lblStatut.setText("● " + filtered.size() + " hébergement" + (filtered.size() > 1 ? "s" : "") +
                " • " + destination.getNom_destination());

        // Store the original list if we want to reset later
        // You might want to add a "Clear filter" button
    }

    // Optional: Add a method to clear the filter
    public void clearFilter() {
        hebergementList.setAll(allHebergements);
        tableHebergements.setItems(hebergementList);
        lblHebergementCount.setText(allHebergements.size() + " hébergement" + (allHebergements.size() > 1 ? "s" : ""));
        updateStats(); // Reset stats to original values
    }
}