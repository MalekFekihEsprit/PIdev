package controllers;

import entities.Destination;
import services.DestinationCRUD;
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
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class DestinationBackController implements Initializable {

    // ============== FXML INJECTED ELEMENTS ==============

    // Top Navigation
    @FXML private Label lblSearchPlaceholder;

    // Bottom Status
    @FXML private Label lblLastUpdate;

    // Sidebar Stats
    @FXML private Label lblSidebarDestinationCount;
    @FXML private Label lblTotalDestinations;
    @FXML private Label lblClimatsCount;
    @FXML private Label lblSaisonsCount;
    @FXML private Label lblPaysCount;

    // KPI Cards
    @FXML private Label lblDestinationsTotal;
    @FXML private Label lblClimatsTotal;
    @FXML private Label lblSaisonsTotal;
    @FXML private Label lblPaysTotal;


    // Table Section
    @FXML private Label lblDestinationCount;
    @FXML private TableView<Destination> tableDestinations;
    @FXML private TableColumn<Destination, Integer> colId;
    @FXML private TableColumn<Destination, String> colNom;
    @FXML private TableColumn<Destination, String> colPays;
    @FXML private TableColumn<Destination, String> colDescription;
    @FXML private TableColumn<Destination, String> colClimat;
    @FXML private TableColumn<Destination, String> colSaison;
    @FXML private TableColumn<Destination, Void> colActions;

    // Buttons
    @FXML private Button btnAjouter;
    @FXML private Button btnSupprimer;
    @FXML private HBox btnExport;
    @FXML private HBox btnSearch;
    @FXML private HBox btnFilter;
    @FXML private HBox btnHome;

    // Pagination
    @FXML private Label lblPaginationInfo;
    @FXML private HBox paginationContainer;
    @FXML private HBox btnHebergement;

    // ============== CLASS VARIABLES ==============

    private DestinationCRUD destinationCRUD;
    private ObservableList<Destination> destinationList = FXCollections.observableArrayList();
    private List<Destination> allDestinations = new ArrayList<>();
    private int currentPage = 1;
    private final int rowsPerPage = 10;
    private boolean isCurrentPageHebergement = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        destinationCRUD = new DestinationCRUD();

        // Initialize table columns
        setupTableColumns();

        // Set table properties
        setupTableProperties();

        // Setup actions column with icons
        setupActionsColumn();

        // Load data from database
        loadDestinations();

        // Setup button actions
        setupButtonActions();

        // Update last update time
        updateLastUpdateTime();

        setupHebergementButton();
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id_destination"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom_destination"));
        colPays.setCellValueFactory(new PropertyValueFactory<>("pays_destination"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description_destination"));
        colClimat.setCellValueFactory(new PropertyValueFactory<>("climat_destination"));
        colSaison.setCellValueFactory(new PropertyValueFactory<>("saison_destination"));
    }

    private void setupTableProperties() {
        tableDestinations.setFixedCellSize(35);
        tableDestinations.setPrefHeight(380);
        tableDestinations.setMaxHeight(380);
        tableDestinations.setMinHeight(380);

        // Enable multiple selection for bulk delete
        tableDestinations.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }
    private void navigateToHebergement() {
        try {
            // Load the HebergementBack FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/HebergementBack.fxml"));
            Parent root = loader.load();

            // Get the current stage and set the new scene
            Stage stage = (Stage) btnHebergement.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TravelMate - Gestion des Hébergements");
            stage.setMaximized(true);

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la gestion des hébergements: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupHebergementButton() {
        if (btnHebergement == null) return;

        // Click handler
        btnHebergement.setOnMouseClicked(event -> navigateToHebergement());

        // Hover effects
        btnHebergement.setOnMouseEntered(event -> {
            if (!isCurrentPageHebergement) {
                btnHebergement.setStyle("-fx-background-color: rgba(255,140,66,0.15); -fx-background-radius: 12; -fx-padding: 12 16; -fx-cursor: hand;");
                // Change text color of both labels
                btnHebergement.lookupAll(".label").forEach(label -> {
                    if (label instanceof Label) {
                        ((Label) label).setStyle("-fx-text-fill: #ff8c42; -fx-font-weight: 600; -fx-font-size: 14;");
                    }
                });
            }
        });

        btnHebergement.setOnMouseExited(event -> {
            if (!isCurrentPageHebergement) {
                btnHebergement.setStyle("-fx-background-color: transparent; -fx-background-radius: 12; -fx-padding: 12 16; -fx-cursor: hand;");
                // Reset text color
                btnHebergement.lookupAll(".label").forEach(label -> {
                    if (label instanceof Label) {
                        Label lbl = (Label) label;
                        if (lbl.getText().equals("🏨")) {
                            lbl.setStyle("-fx-font-size: 16;");
                        } else {
                            lbl.setStyle("-fx-text-fill: #94a3b8; -fx-font-weight: 500; -fx-font-size: 14;");
                        }
                    }
                });
            }
        });
    }

    private void setupActionsColumn() {
        colActions.setCellFactory(new Callback<>() {
            @Override
            public TableCell<Destination, Void> call(final TableColumn<Destination, Void> param) {
                return new TableCell<>() {
                    private final HBox actionBox = new HBox(8);
                    private final HBox btnConsulter = new HBox();
                    private final HBox btnModifier = new HBox();
                    private final HBox btnSupprimer = new HBox();

                    {
                        // Style for action buttons
                        actionBox.setAlignment(javafx.geometry.Pos.CENTER);

                        // Consulter button (blue)
                        btnConsulter.setAlignment(javafx.geometry.Pos.CENTER);
                        btnConsulter.setStyle("-fx-background-color: #3b82f6; -fx-background-radius: 8; -fx-min-width: 32; -fx-min-height: 32; -fx-cursor: hand;");
                        Label consulterIcon = new Label("👁️");
                        consulterIcon.setStyle("-fx-font-size: 16; -fx-text-fill: white;");
                        btnConsulter.getChildren().add(consulterIcon);
                        Tooltip.install(btnConsulter, new Tooltip("Consulter"));

                        // Modifier button (amber)
                        btnModifier.setAlignment(javafx.geometry.Pos.CENTER);
                        btnModifier.setStyle("-fx-background-color: #f59e0b; -fx-background-radius: 8; -fx-min-width: 32; -fx-min-height: 32; -fx-cursor: hand;");
                        Label modifierIcon = new Label("✏️");
                        modifierIcon.setStyle("-fx-font-size: 16; -fx-text-fill: white;");
                        btnModifier.getChildren().add(modifierIcon);
                        Tooltip.install(btnModifier, new Tooltip("Modifier"));

                        // Supprimer button (red)
                        btnSupprimer.setAlignment(javafx.geometry.Pos.CENTER);
                        btnSupprimer.setStyle("-fx-background-color: #ef4444; -fx-background-radius: 8; -fx-min-width: 32; -fx-min-height: 32; -fx-cursor: hand;");
                        Label supprimerIcon = new Label("🗑️");
                        supprimerIcon.setStyle("-fx-font-size: 16; -fx-text-fill: white;");
                        btnSupprimer.getChildren().add(supprimerIcon);
                        Tooltip.install(btnSupprimer, new Tooltip("Supprimer"));

                        actionBox.getChildren().addAll(btnConsulter, btnModifier, btnSupprimer);
                        actionBox.setPadding(new Insets(4, 0, 4, 0));
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            Destination destination = getTableView().getItems().get(getIndex());

                            btnConsulter.setOnMouseClicked(event -> handleConsulter(destination));
                            btnModifier.setOnMouseClicked(event -> handleModifier(destination));
                            btnSupprimer.setOnMouseClicked(event -> handleDeleteSingle(destination));

                            setGraphic(actionBox);
                        }
                    }
                };
            }
        });
    }

    private void setupButtonActions() {
        // Ajouter button
        if (btnAjouter != null) {
            btnAjouter.setOnAction(event -> handleAjouter());
        }

        // Supprimer button (bulk delete)
        if (btnSupprimer != null) {
            btnSupprimer.setOnAction(event -> handleBulkDelete());
        }

        // Export button
        if (btnExport != null) {
            btnExport.setOnMouseClicked(event -> handleExport());
        }

        // Search button
        if (btnSearch != null) {
            btnSearch.setOnMouseClicked(event -> handleSearch());
        }

        // Filter button
        if (btnFilter != null) {
            btnFilter.setOnMouseClicked(event -> handleFilter());
        }

        // Home
        if (btnHome != null) {
            btnHome.setOnMouseClicked(event -> navigateToHome());
        }

    }

    // ============== CRUD OPERATIONS ==============

    private void handleAjouter() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterDestination.fxml"));
            Parent root = loader.load();

            AjouterDestinationController controller = loader.getController();
            controller.setParentController(this);

            Stage stage = new Stage();
            stage.setTitle("Ajouter une destination");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace(); // ADD THIS LINE
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage());
        }
    }

    public void handleModifier(Destination destination) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierDestination.fxml"));
            Parent root = loader.load();

            ModifierDestinationController controller = loader.getController();
            controller.setDestination(destination);
            controller.setParentController(this);

            Stage stage = new Stage();
            stage.setTitle("Modifier - " + destination.getNom_destination());
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage());
        }
    }

    private void handleDeleteSingle(Destination destination) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer la destination");
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer " + destination.getNom_destination() + " ?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                destinationCRUD.supprimer(destination);
                allDestinations.remove(destination);
                updateTableData();
                updateStats();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Destination supprimée avec succès!");
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer: " + e.getMessage());
            }
        }
    }

    private void handleBulkDelete() {
        ObservableList<Destination> selectedItems = tableDestinations.getSelectionModel().getSelectedItems();

        if (selectedItems.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner au moins une destination à supprimer");
            return;
        }

        // Build a string with all destination names
        StringBuilder namesList = new StringBuilder();
        for (Destination d : selectedItems) {
            namesList.append("• ").append(d.getNom_destination())
                    .append(" (").append(d.getPays_destination()).append(")\n");
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation de suppression");
        confirm.setHeaderText("Supprimer " + selectedItems.size() + " destination(s)");
        confirm.setContentText("Destinations à supprimer :\n\n" + namesList.toString() +
                "\nÊtes-vous sûr de vouloir continuer ?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            int successCount = 0;
            int failCount = 0;
            List<String> failedNames = new ArrayList<>();

            for (Destination destination : new ArrayList<>(selectedItems)) {
                try {
                    destinationCRUD.supprimer(destination);
                    allDestinations.remove(destination);
                    successCount++;
                } catch (SQLException e) {
                    failCount++;
                    failedNames.add(destination.getNom_destination() + " (" + destination.getPays_destination() + ")");
                    System.err.println("Erreur lors de la suppression de " + destination.getNom_destination() + ": " + e.getMessage());
                }
            }

            updateTableData();
            updateStats();

            if (failCount == 0) {
                showAlert(Alert.AlertType.INFORMATION, "Succès",
                        successCount + " destination(s) supprimée(s) avec succès !\n\n" + namesList.toString());
            } else {
                StringBuilder failedList = new StringBuilder();
                for (String name : failedNames) {
                    failedList.append("• ").append(name).append("\n");
                }
                showAlert(Alert.AlertType.WARNING, "Suppression partielle",
                        successCount + " destination(s) supprimée(s) avec succès.\n" +
                                failCount + " échec(s) :\n\n" + failedList.toString());
            }
        }
    }

    private void handleConsulter(Destination destination) {
        try {
            // Load the AfficherDestinationBack FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherDestinationBack.fxml"));
            Parent root = loader.load();

            // Get the controller and pass the destination
            AfficherDestinationBackController controller = loader.getController();
            controller.setDestination(destination);
            controller.setParentController(this);

            // Create a new stage for the details window
            Stage stage = new Stage();
            stage.setTitle("Détails - " + destination.getNom_destination());
            stage.setScene(new Scene(root));

            // Make it modal (blocks interaction with parent window)
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);

            // Center on parent window
            stage.initOwner(tableDestinations.getScene().getWindow());
            stage.setResizable(false);

            // Show and wait (makes it modal)
            stage.showAndWait();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir les détails: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ============== DATA LOADING AND UPDATES ==============

    private void loadDestinations() {
        try {
            allDestinations = destinationCRUD.afficher();
            destinationList.setAll(allDestinations);
            tableDestinations.setItems(destinationList);

            updateStats();
            updateTableData();

            System.out.println("Loaded " + allDestinations.size() + " destinations");

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les destinations: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateStats() {
        int total = allDestinations.size();

        // Update all total labels
        lblDestinationsTotal.setText(String.valueOf(total));
        lblTotalDestinations.setText(String.valueOf(total));
        lblSidebarDestinationCount.setText(String.valueOf(total));
        lblDestinationCount.setText(total + " destination" + (total > 1 ? "s" : ""));

        // Count unique countries
        long paysCount = allDestinations.stream()
                .map(Destination::getPays_destination)
                .filter(Objects::nonNull)
                .distinct()
                .count();
        lblPaysCount.setText(String.valueOf(paysCount));
        lblPaysTotal.setText(String.valueOf(paysCount));


        // Count unique climates
        long climatesCount = allDestinations.stream()
                .map(Destination::getClimat_destination)
                .filter(Objects::nonNull)
                .distinct()
                .count();
        lblClimatsCount.setText(String.valueOf(climatesCount));
        lblClimatsTotal.setText(String.valueOf(climatesCount));

        // Count unique seasons
        long seasonsCount = allDestinations.stream()
                .map(Destination::getSaison_destination)
                .filter(Objects::nonNull)
                .distinct()
                .count();
        lblSaisonsCount.setText(String.valueOf(seasonsCount));
        lblSaisonsTotal.setText(String.valueOf(seasonsCount));
    }

    private void updateTableData() {
        int total = allDestinations.size();

        if (total == 0) {
            destinationList.clear();
            tableDestinations.setItems(destinationList);
            updatePagination();
            return;
        }

        // Calculate pagination
        int fromIndex = (currentPage - 1) * rowsPerPage;
        int toIndex = Math.min(fromIndex + rowsPerPage, total);

        if (fromIndex < total) {
            List<Destination> pageData = allDestinations.subList(fromIndex, toIndex);
            destinationList.setAll(pageData);
            tableDestinations.setItems(destinationList);
        } else {
            currentPage = Math.max(1, (int) Math.ceil((double) total / rowsPerPage));
            fromIndex = (currentPage - 1) * rowsPerPage;
            toIndex = Math.min(fromIndex + rowsPerPage, total);
            List<Destination> pageData = allDestinations.subList(fromIndex, toIndex);
            destinationList.setAll(pageData);
            tableDestinations.setItems(destinationList);
        }

        updatePagination();
    }

    private void updatePagination() {
        int total = allDestinations.size();
        int start = total == 0 ? 0 : (currentPage - 1) * rowsPerPage + 1;
        int end = Math.min(currentPage * rowsPerPage, total);

        lblPaginationInfo.setText(total == 0 ? "0-0 sur 0 destinations" : start + "-" + end + " sur " + total + " destinations");

        // Update pagination buttons
        paginationContainer.getChildren().clear();
        int totalPages = (int) Math.ceil((double) total / rowsPerPage);

        if (totalPages <= 1) return;

        // Previous button
        if (currentPage > 1) {
            Label prevLabel = createPageLabel("◀", false);
            prevLabel.setOnMouseClicked(event -> {
                currentPage--;
                updateTableData();
            });
            paginationContainer.getChildren().add(prevLabel);
        }

        // Page numbers
        int startPage = Math.max(1, currentPage - 2);
        int endPage = Math.min(totalPages, startPage + 4);

        for (int i = startPage; i <= endPage; i++) {
            int pageNum = i;
            Label pageLabel = createPageLabel(String.valueOf(i), i == currentPage);
            pageLabel.setOnMouseClicked(event -> {
                currentPage = pageNum;
                updateTableData();
            });
            paginationContainer.getChildren().add(pageLabel);
        }

        // Next button
        if (currentPage < totalPages) {
            Label nextLabel = createPageLabel("▶", false);
            nextLabel.setOnMouseClicked(event -> {
                currentPage++;
                updateTableData();
            });
            paginationContainer.getChildren().add(nextLabel);
        }
    }

    private Label createPageLabel(String text, boolean active) {
        Label label = new Label(text);
        if (active) {
            label.setStyle("-fx-background-color: #ff8c42; -fx-text-fill: white; -fx-background-radius: 8; -fx-min-width: 32; -fx-min-height: 32; -fx-alignment: center; -fx-font-size: 12; -fx-cursor: hand;");
        } else {
            label.setStyle("-fx-background-color: #1e2749; -fx-text-fill: #94a3b8; -fx-background-radius: 8; -fx-min-width: 32; -fx-min-height: 32; -fx-alignment: center; -fx-font-size: 12; -fx-cursor: hand;");
        }
        return label;
    }

    public void refreshAfterModification() {
        loadDestinations();
        updateLastUpdateTime();
    }

    private void updateLastUpdateTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");
        lblLastUpdate.setText("Dernière mise à jour: " + LocalDateTime.now().format(formatter));
    }

    // ============== SEARCH AND FILTER ==============

    private void handleSearch() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Recherche");
        dialog.setHeaderText("Rechercher une destination");
        dialog.setContentText("Nom de la destination:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(searchTerm -> {
            if (searchTerm.trim().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez entrer un terme de recherche");
                return;
            }

            List<Destination> searchResults = allDestinations.stream()
                    .filter(d -> d.getNom_destination().toLowerCase().contains(searchTerm.toLowerCase()) ||
                            d.getPays_destination().toLowerCase().contains(searchTerm.toLowerCase()))
                    .collect(Collectors.toList());

            if (searchResults.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "Résultat", "Aucune destination trouvée");
            } else {
                destinationList.setAll(searchResults);
                tableDestinations.setItems(destinationList);
                lblDestinationCount.setText(searchResults.size() + " résultat(s)");
            }
        });
    }

    private void handleFilter() {
        List<String> climates = allDestinations.stream()
                .map(Destination::getClimat_destination)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        if (climates.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "Information", "Aucun climat disponible pour le filtrage");
            return;
        }

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
                lblDestinationCount.setText(allDestinations.size() + " destination(s)");
            } else {
                List<Destination> filteredResults = allDestinations.stream()
                        .filter(d -> climate.equals(d.getClimat_destination()))
                        .collect(Collectors.toList());

                if (filteredResults.isEmpty()) {
                    showAlert(Alert.AlertType.INFORMATION, "Résultat", "Aucune destination trouvée pour ce climat");
                } else {
                    destinationList.setAll(filteredResults);
                    tableDestinations.setItems(destinationList);
                    lblDestinationCount.setText(filteredResults.size() + " résultat(s)");
                }
            }
        });
    }

    private void handleExport() {
        // TODO: Implement export functionality
        showAlert(Alert.AlertType.INFORMATION, "Export", "Fonctionnalité d'export à implémenter");
    }

    // ============== UTILITY METHODS ==============

    void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
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
}