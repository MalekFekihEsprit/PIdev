package Controllers;

import Entities.Destination;
import Services.DestinationCRUD;
import Utils.UserSession;
import Entities.User;
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
import javafx.stage.Stage;

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
    @FXML private HBox btnDestinations;
    @FXML private HBox btnHebergement;
    @FXML private HBox btnItineraires;
    @FXML private HBox btnActivites;
    @FXML private HBox btnVoyages;
    @FXML private HBox btnBudgets;
    @FXML private HBox btnCategories;
    @FXML private HBox userProfileBox;
    @FXML private Label lblUserName;
    @FXML private Label lblUserRole;

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

    // Scroll navigation
    @FXML private ScrollPane navScrollPane;
    @FXML private HBox navLinksContainer;

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

        // Setup user profile click
        setupUserProfile();
        updateUserInfo();
    }

    private void setupTableColumns() {
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom_destination"));
        colPays.setCellValueFactory(new PropertyValueFactory<>("pays_destination"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description_destination"));
        colClimat.setCellValueFactory(new PropertyValueFactory<>("climat_destination"));
        colSaison.setCellValueFactory(new PropertyValueFactory<>("saison_destination"));

        // Setup actions column with consulter button
        setupActionsColumn();
    }

    private void setupTableHeight() {
        tableDestinations.setFixedCellSize(35);
        tableDestinations.setPrefHeight(380);
        tableDestinations.setMaxHeight(380);
        tableDestinations.setMinHeight(380);
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

                Tooltip.install(btnConsulter, new Tooltip("Voir les détails"));
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

    private void setupNavigationButtons() {
        // Destinations button (current page, no action needed)

        // Hébergement button
        if (btnHebergement != null) {
            btnHebergement.setOnMouseClicked(event -> navigateToHebergement());
        }

        // Itinéraires button (pas encore implémenté)
        if (btnItineraires != null) {
            btnItineraires.setOnMouseClicked(event -> showNotImplementedAlert("Itinéraires"));
        }

        // Activités button
        if (btnActivites != null) {
            btnActivites.setOnMouseClicked(event -> navigateToActivites());
        }

        // Catégories button
        if (btnCategories != null) {
            btnCategories.setOnMouseClicked(event -> navigateToCategories());
        }

        // Voyages button (pas encore implémenté)
        if (btnVoyages != null) {
            btnVoyages.setOnMouseClicked(event -> showNotImplementedAlert("Voyages"));
        }

        // Budgets button (pas encore implémenté)
        if (btnBudgets != null) {
            btnBudgets.setOnMouseClicked(event -> showNotImplementedAlert("Budgets"));
        }
    }

    private void navigateToHebergement() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/HebergementFront.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) btnHebergement.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("TravelMate - Hébergements");
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de navigation", "Impossible de charger les hébergements: " + e.getMessage());
        }
    }

    private void navigateToActivites() {
        CategorieContext.categorieFiltre = null;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/activitesfront.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) btnActivites.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("TravelMate - Activités");
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de navigation", "Impossible de charger les activités: " + e.getMessage());
        }
    }

    private void navigateToCategories() {
        CategorieContext.categorieFiltre = null;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/categoriesfront.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) btnCategories.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("TravelMate - Catégories");
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de navigation", "Impossible de charger les catégories: " + e.getMessage());
        }
    }

    private void showNotImplementedAlert(String feature) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Fonctionnalité à venir");
        alert.setHeaderText(null);
        alert.setContentText("La fonctionnalité \"" + feature + "\" sera bientôt disponible !");
        alert.showAndWait();
    }

    @FXML
    private void scrollNavLeft() {
        if (navScrollPane != null && navLinksContainer != null) {
            double newHvalue = navScrollPane.getHvalue() - 0.2;
            navScrollPane.setHvalue(Math.max(0, newHvalue));
        }
    }

    @FXML
    private void scrollNavRight() {
        if (navScrollPane != null && navLinksContainer != null) {
            double newHvalue = navScrollPane.getHvalue() + 0.2;
            navScrollPane.setHvalue(Math.min(1, newHvalue));
        }
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
    }

    private void setupUserProfile() {
        if (userProfileBox != null) {
            userProfileBox.setOnMouseClicked(event -> navigateToProfile());

            // Hover effect
            userProfileBox.setOnMouseEntered(event -> {
                userProfileBox.setStyle("-fx-background-color: #e2e8f0; -fx-background-radius: 25; -fx-padding: 6 16 6 6; -fx-cursor: hand;");
            });

            userProfileBox.setOnMouseExited(event -> {
                userProfileBox.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 25; -fx-padding: 6 16 6 6; -fx-cursor: hand;");
            });
        }
    }

    private void updateUserInfo() {
        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser != null) {
            if (lblUserName != null) {
                lblUserName.setText(currentUser.getPrenom() + " " + currentUser.getNom());
            }
            if (lblUserRole != null) {
                lblUserRole.setText(currentUser.getRole());
            }
        } else {
            if (lblUserName != null) {
                lblUserName.setText("Utilisateur");
            }
            if (lblUserRole != null) {
                lblUserRole.setText("Non connecté");
            }
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
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le profil: " + e.getMessage());
            e.printStackTrace();
        }
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
        if (lblTotalDestinations != null) lblTotalDestinations.setText(String.valueOf(total));
        if (lblDestinationCount != null) lblDestinationCount.setText(total + " destination" + (total > 1 ? "s" : ""));
        if (lblStatut != null) lblStatut.setText("● " + total + " destination" + (total > 1 ? "s" : ""));

        // Count unique countries
        long paysCount = allDestinations.stream()
                .map(Destination::getPays_destination)
                .filter(Objects::nonNull)
                .distinct()
                .count();
        if (lblPaysCount != null) lblPaysCount.setText(paysCount + " pays représentés");
        if (lblTotalPays != null) lblTotalPays.setText("Dans " + paysCount + " pays différents");

        // Count unique climates
        long climatesCount = allDestinations.stream()
                .map(Destination::getClimat_destination)
                .filter(Objects::nonNull)
                .distinct()
                .count();
        if (lblClimatsCount != null) lblClimatsCount.setText(String.valueOf(climatesCount));
        if (lblClimatDiversite != null) lblClimatDiversite.setText(climatesCount + " climats différents");

        // Count unique seasons
        long seasonsCount = allDestinations.stream()
                .map(Destination::getSaison_destination)
                .filter(Objects::nonNull)
                .distinct()
                .count();
        if (lblSaisonsCount != null) lblSaisonsCount.setText(String.valueOf(seasonsCount));
        if (progressSaisons != null && seasonsCount > 0) {
            progressSaisons.setProgress(Math.min(seasonsCount / 4.0, 1.0));
        }

        // Active destinations (consider all as active for now)
        if (lblDestinationsActives != null) lblDestinationsActives.setText(String.valueOf(total));
        double pourcentage = total > 0 ? 100.0 : 0;
        if (lblActivesPourcentage != null) lblActivesPourcentage.setText(String.format("%.0f%% des destinations", pourcentage));
    }

    private void refreshData() {
        loadDestinations();
        updateLastUpdateTime();
        showAlert(Alert.AlertType.INFORMATION, "Succès", "Données rafraîchies avec succès!");
    }

    private void updateLastUpdateTime() {
        if (lblLastUpdate != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");
            lblLastUpdate.setText("Dernière mise à jour: " + LocalDateTime.now().format(formatter));
        }
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
                    if (lblDestinationCount != null) {
                        lblDestinationCount.setText(searchResults.size() + " destination" + (searchResults.size() > 1 ? "s" : ""));
                    }
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
                if (lblDestinationCount != null) {
                    lblDestinationCount.setText(allDestinations.size() + " destination" + (allDestinations.size() > 1 ? "s" : ""));
                }
            } else {
                List<Destination> filteredResults = allDestinations.stream()
                        .filter(d -> climate.equals(d.getClimat_destination()))
                        .collect(Collectors.toList());

                if (filteredResults.isEmpty()) {
                    showAlert(Alert.AlertType.INFORMATION, "Résultat", "Aucune destination trouvée pour ce climat");
                } else {
                    destinationList.setAll(filteredResults);
                    tableDestinations.setItems(destinationList);
                    if (lblDestinationCount != null) {
                        lblDestinationCount.setText(filteredResults.size() + " destination" + (filteredResults.size() > 1 ? "s" : ""));
                    }
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

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}