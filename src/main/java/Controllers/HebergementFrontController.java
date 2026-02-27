package Controllers;

import Entities.Destination;
import Entities.Hebergement;
import Services.HebergementCRUD;
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

public class HebergementFrontController implements Initializable {

    // ============== FXML INJECTED ELEMENTS ==============

    // Top Navigation
    @FXML private HBox btnDestinations;
    @FXML private HBox btnHebergement;
    @FXML private HBox btnItineraires;
    @FXML private HBox btnActivites;
    @FXML private HBox btnVoyages;
    @FXML private HBox btnBudgets;
    @FXML private HBox btnCategories;
    @FXML private HBox btnHome;
    @FXML private HBox userProfileBox;
    @FXML private Label lblUserName;
    @FXML private Label lblUserRole;

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
    @FXML private HBox btnRefresh;
    @FXML private HBox btnSearch;
    @FXML private HBox btnFilter;

    // Scroll navigation
    @FXML private ScrollPane navScrollPane;
    @FXML private HBox navLinksContainer;

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

        // Setup user profile click
        setupUserProfile();
        updateUserInfo();
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

    private void setupNavigationButtons() {
        // Destinations button
        if (btnDestinations != null) {
            btnDestinations.setOnMouseClicked(event -> navigateToDestinations());
        }

        // Hébergement button (current page, no action needed)

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

    private void navigateToDestinations() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DestinationFront.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) btnDestinations.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("TravelMate - Destinations");
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de navigation", "Impossible de charger les destinations: " + e.getMessage());
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
        if (lblTotalHebergements != null) lblTotalHebergements.setText(String.valueOf(total));
        if (lblHebergementCount != null) lblHebergementCount.setText(total + " hébergement" + (total > 1 ? "s" : ""));
        if (lblStatut != null) lblStatut.setText("● " + total + " hébergement" + (total > 1 ? "s" : ""));

        // Count unique types
        long typesCount = allHebergements.stream()
                .map(Hebergement::getType_hebergement)
                .filter(Objects::nonNull)
                .distinct()
                .count();
        if (lblTypesCount != null) lblTypesCount.setText(typesCount + " type" + (typesCount > 1 ? "s" : "") + " différents");

        // Count unique destinations
        long destsCount = allHebergements.stream()
                .map(Hebergement::getDestination)
                .filter(Objects::nonNull)
                .map(Destination::getId_destination)
                .distinct()
                .count();
        if (lblDestinationsCount != null) lblDestinationsCount.setText(destsCount + " destination" + (destsCount > 1 ? "s" : ""));
        if (lblTotalDestinationsLiees != null) lblTotalDestinationsLiees.setText("Dans " + destsCount + " destination" + (destsCount > 1 ? "s" : ""));

        // Average price
        double avgPrice = allHebergements.stream()
                .mapToDouble(Hebergement::getPrixNuit_hebergement)
                .average()
                .orElse(0.0);
        if (lblPrixMoyen != null) lblPrixMoyen.setText(String.format("%.2f €", avgPrice));

        // Average note
        double avgNote = allHebergements.stream()
                .mapToDouble(Hebergement::getNote_hebergement)
                .average()
                .orElse(0.0);
        if (lblNoteMoyenne != null) lblNoteMoyenne.setText(String.format("Note moyenne: %.1f ⭐", avgNote));
        if (lblNoteMoyenneKPI != null) lblNoteMoyenneKPI.setText(String.format("%.1f", avgNote));
        if (progressNote != null) progressNote.setProgress(avgNote / 5.0);
    }

    private void refreshData() {
        loadHebergements();
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
                    if (lblHebergementCount != null) {
                        lblHebergementCount.setText(searchResults.size() + " résultat" + (searchResults.size() > 1 ? "s" : ""));
                    }
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
                if (lblHebergementCount != null) {
                    lblHebergementCount.setText(allHebergements.size() + " hébergement" + (allHebergements.size() > 1 ? "s" : ""));
                }
            } else {
                List<Hebergement> filteredResults = allHebergements.stream()
                        .filter(h -> type.equals(h.getType_hebergement()))
                        .collect(Collectors.toList());

                if (filteredResults.isEmpty()) {
                    showAlert(Alert.AlertType.INFORMATION, "Résultat", "Aucun hébergement trouvé pour ce type");
                } else {
                    hebergementList.setAll(filteredResults);
                    tableHebergements.setItems(hebergementList);
                    if (lblHebergementCount != null) {
                        lblHebergementCount.setText(filteredResults.size() + " résultat" + (filteredResults.size() > 1 ? "s" : ""));
                    }
                }
            }
        });
    }

    private void handleConsulter(Hebergement hebergement) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherHebergementFront.fxml"));
            Parent root = loader.load();

            AfficherHebergementFrontController controller = loader.getController();
            controller.setHebergement(hebergement);

            Stage stage = new Stage();
            stage.setTitle("Détails - " + hebergement.getNom_hebergement());
            stage.setScene(new Scene(root));
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.initOwner(tableHebergements.getScene().getWindow());
            stage.setResizable(false);
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

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void filterByDestination(Destination destination) {
        if (destination == null) return;

        List<Hebergement> filtered = allHebergements.stream()
                .filter(h -> h.getDestination() != null &&
                        h.getDestination().getId_destination() == destination.getId_destination())
                .collect(Collectors.toList());

        hebergementList.setAll(filtered);
        tableHebergements.setItems(hebergementList);
        if (lblHebergementCount != null) {
            lblHebergementCount.setText(filtered.size() + " hébergement" + (filtered.size() > 1 ? "s" : "") +
                    " à " + destination.getNom_destination());
        }
        if (lblStatut != null) {
            lblStatut.setText("● " + filtered.size() + " hébergement" + (filtered.size() > 1 ? "s" : "") +
                    " • " + destination.getNom_destination());
        }
    }

    public void clearFilter() {
        hebergementList.setAll(allHebergements);
        tableHebergements.setItems(hebergementList);
        if (lblHebergementCount != null) {
            lblHebergementCount.setText(allHebergements.size() + " hébergement" + (allHebergements.size() > 1 ? "s" : ""));
        }
        updateStats();
    }
}