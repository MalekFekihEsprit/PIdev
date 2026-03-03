package Controllers;

import Entities.Destination;
import Entities.DeleteNotification;
import Entities.User;
import Services.DestinationCRUD;
import Services.DeleteNotificationCRUD;
import Utils.UserSession;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
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

    // Region Statistics
    @FXML private PieChart regionPieChart;
    @FXML private VBox regionStatsContainer;
    @FXML private Label lblTotalRegions;
    @FXML private Label lblRegionCoverage;

    // Table Section
    @FXML private Label lblDestinationCount;
    @FXML private TableView<Destination> tableDestinations;
    @FXML private TableColumn<Destination, Integer> colId;
    @FXML private TableColumn<Destination, String> colNom;
    @FXML private TableColumn<Destination, String> colPays;
    @FXML private TableColumn<Destination, String> colRegion;
    @FXML private TableColumn<Destination, String> colDescription;
    @FXML private TableColumn<Destination, String> colClimat;
    @FXML private TableColumn<Destination, String> colSaison;
    @FXML private TableColumn<Destination, String> colAddedBy;
    @FXML private TableColumn<Destination, Void> colActions;

    // Buttons
    @FXML private Button btnAjouter;
    @FXML private Button btnSupprimer;
    @FXML private Button btnSuggestHotels;
    @FXML private Button btnShowMap;
    @FXML private HBox btnSearch;
    @FXML private HBox btnFilter;
    @FXML private HBox btnHebergement;
    @FXML private HBox btnItineraires;
    @FXML private HBox btnActivites;
    @FXML private HBox btnVoyages;
    @FXML private HBox btnBudgets;
    @FXML private HBox btnUsers;
    @FXML private HBox btnStats;
    @FXML private HBox userProfileBox;

    // Pagination
    @FXML private Label lblPaginationInfo;
    @FXML private HBox paginationContainer;
    @FXML private Label lblUserName;
    @FXML private Label lblUserRole;

    // ============== CLASS VARIABLES ==============

    private DestinationCRUD destinationCRUD;
    private DeleteNotificationCRUD notificationCRUD;
    private ObservableList<Destination> destinationList = FXCollections.observableArrayList();
    private List<Destination> allDestinations = new ArrayList<>();
    private int currentPage = 1;
    private final int rowsPerPage = 10;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        destinationCRUD = new DestinationCRUD();
        notificationCRUD = new DeleteNotificationCRUD();

        setupTableColumns();
        setupTableProperties();
        setupActionsColumn();
        loadDestinations();
        setupButtonActions();
        setupNavigationButtons();
        setupUserProfile();
        updateLastUpdateTime();
        updateUserInfo();
        setupRegionChart();
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id_destination"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom_destination"));
        colPays.setCellValueFactory(new PropertyValueFactory<>("pays_destination"));
        colRegion.setCellValueFactory(new PropertyValueFactory<>("region_destination"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description_destination"));
        colClimat.setCellValueFactory(new PropertyValueFactory<>("climat_destination"));
        colSaison.setCellValueFactory(new PropertyValueFactory<>("saison_destination"));

        // FIXED: Properly set up the added_by column to show the user's full name
        colAddedBy.setCellValueFactory(new PropertyValueFactory<>("added_by_name"));

        // Format null/empty values for region
        colRegion.setCellFactory(col -> new TableCell<Destination, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else if (item == null || item.trim().isEmpty()) {
                    setText("-");
                } else {
                    setText(item);
                }
            }
        });

        // Format null/empty values for added_by
        colAddedBy.setCellFactory(col -> new TableCell<Destination, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else if (item == null || item.trim().isEmpty()) {
                    setText("-");
                } else {
                    setText(item);
                }
            }
        });
    }

    private void setupTableProperties() {
        tableDestinations.setFixedCellSize(35);
        tableDestinations.setPrefHeight(380);
        tableDestinations.setMaxHeight(380);
        tableDestinations.setMinHeight(380);
        tableDestinations.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
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
                        actionBox.setAlignment(javafx.geometry.Pos.CENTER);

                        btnConsulter.setAlignment(javafx.geometry.Pos.CENTER);
                        btnConsulter.setStyle("-fx-background-color: #3b82f6; -fx-background-radius: 8; -fx-min-width: 32; -fx-min-height: 32; -fx-cursor: hand;");
                        Label consulterIcon = new Label("👁️");
                        consulterIcon.setStyle("-fx-font-size: 16; -fx-text-fill: white;");
                        btnConsulter.getChildren().add(consulterIcon);
                        Tooltip.install(btnConsulter, new Tooltip("Consulter"));

                        btnModifier.setAlignment(javafx.geometry.Pos.CENTER);
                        btnModifier.setStyle("-fx-background-color: #f59e0b; -fx-background-radius: 8; -fx-min-width: 32; -fx-min-height: 32; -fx-cursor: hand;");
                        Label modifierIcon = new Label("✏️");
                        modifierIcon.setStyle("-fx-font-size: 16; -fx-text-fill: white;");
                        btnModifier.getChildren().add(modifierIcon);
                        Tooltip.install(btnModifier, new Tooltip("Modifier"));

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

    private void updateUserInfo() {
        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser != null) {
            lblUserName.setText(currentUser.getPrenom() + " " + currentUser.getNom());
            lblUserRole.setText(currentUser.getRole());
        } else {
            lblUserName.setText("Utilisateur");
            lblUserRole.setText("Non connecté");
        }
    }

    private void handleSuggestHotels() {
        Destination selectedDestination = tableDestinations.getSelectionModel().getSelectedItem();
        if (selectedDestination == null) {
            showAlert(Alert.AlertType.WARNING, "Attention",
                    "Veuillez sélectionner une destination dans le tableau pour suggérer des hôtels.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/HotelSuggestionDialog.fxml"));
            Parent root = loader.load();
            HotelSuggestionController controller = loader.getController();
            controller.setDestination(selectedDestination);
            controller.setParentController(this);

            Stage stage = new Stage();
            stage.setTitle("Suggérer des hôtels - " + selectedDestination.getNom_destination());
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la suggestion d'hôtels: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleShowMap() {
        try {
            List<Destination> allDestinations = destinationCRUD.afficher();
            if (allDestinations.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "Information", "Aucune destination à afficher sur la carte.");
                return;
            }

            long validDestinations = allDestinations.stream()
                    .filter(d -> d.getLatitude_destination() != 0.0 || d.getLongitude_destination() != 0.0)
                    .count();

            if (validDestinations == 0) {
                showAlert(Alert.AlertType.INFORMATION, "Information", "Aucune destination avec des coordonnées valides.");
                return;
            }

            DestinationMapController mapController = new DestinationMapController(allDestinations);
            mapController.show();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les destinations: " + e.getMessage());
        }
    }

    private void setupButtonActions() {
        if (btnAjouter != null) btnAjouter.setOnAction(event -> handleAjouter());
        if (btnSupprimer != null) btnSupprimer.setOnAction(event -> handleBulkDelete());
        if (btnSuggestHotels != null) btnSuggestHotels.setOnAction(event -> handleSuggestHotels());
        if (btnShowMap != null) btnShowMap.setOnAction(event -> handleShowMap());
        if (btnSearch != null) btnSearch.setOnMouseClicked(event -> handleSearch());
        if (btnFilter != null) btnFilter.setOnMouseClicked(event -> handleFilter());
    }

    private void setupUserProfile() {
        if (userProfileBox != null) {
            userProfileBox.setOnMouseClicked(event -> navigateToProfile());
            userProfileBox.setOnMouseEntered(event ->
                    userProfileBox.setStyle("-fx-background-color: #2d3759; -fx-background-radius: 25; -fx-padding: 6 16 6 6; -fx-cursor: hand;"));
            userProfileBox.setOnMouseExited(event ->
                    userProfileBox.setStyle("-fx-background-color: #1e2749; -fx-background-radius: 25; -fx-padding: 6 16 6 6; -fx-cursor: hand;"));
        }
    }

    private void setupNavigationButtons() {
        setupSidebarButtonHover(btnHebergement, "🏨", "Hébergement");
        if (btnHebergement != null) btnHebergement.setOnMouseClicked(event -> navigateToHebergement());

        setupSidebarButtonHover(btnUsers, "👥", "Utilisateurs");
        if (btnUsers != null) btnUsers.setOnMouseClicked(event -> navigateToUsers());

        setupSidebarButtonHover(btnStats, "📊", "Statistiques");
        if (btnStats != null) btnStats.setOnMouseClicked(event -> navigateToStats());

        setupSidebarButtonHover(btnItineraires, "🗺️", "Itinéraires");
        if (btnItineraires != null) btnItineraires.setOnMouseClicked(event -> navigateToItineraires());

        setupSidebarButtonHover(btnActivites, "🏄", "Activités");
        if (btnActivites != null) btnActivites.setOnMouseClicked(event ->
                showInfoAlert("Activités", "Cette fonctionnalité sera bientôt disponible"));

        setupSidebarButtonHover(btnVoyages, "✈️", "Voyages");
        if (btnVoyages != null) btnVoyages.setOnMouseClicked(event ->
                showInfoAlert("Voyages", "Cette fonctionnalité sera bientôt disponible"));

        setupSidebarButtonHover(btnBudgets, "💰", "Budgets");
        if (btnBudgets != null) btnBudgets.setOnMouseClicked(event ->
                showInfoAlert("Budgets", "Cette fonctionnalité sera bientôt disponible"));
    }

    private void setupSidebarButtonHover(HBox button, String icon, String text) {
        if (button == null) return;

        button.setOnMouseEntered(event -> {
            button.setStyle("-fx-background-color: rgba(255,140,66,0.15); -fx-background-radius: 12; -fx-padding: 12 16; -fx-cursor: hand; -fx-border-color: #ff8c42; -fx-border-width: 1; -fx-border-radius: 12;");
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
            button.setStyle("-fx-background-color: transparent; -fx-background-radius: 12; -fx-padding: 12 16; -fx-cursor: hand;");
            button.lookupAll(".label").forEach(label -> {
                if (label instanceof Label) {
                    Label lbl = (Label) label;
                    if (lbl.getText().equals(icon)) {
                        lbl.setStyle("-fx-font-size: 16;");
                    } else {
                        lbl.setStyle("-fx-text-fill: #94a3b8; -fx-font-weight: 500; -fx-font-size: 14;");
                    }
                }
            });
        });
    }

    private void navigateToHebergement() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/HebergementBack.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnHebergement.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TravelMate - Gestion des Hébergements");
            stage.setMaximized(true);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la gestion des hébergements: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void navigateToUsers() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin_users.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnUsers.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TravelMate - Gestion des Utilisateurs");
            stage.setMaximized(true);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la gestion des utilisateurs: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void navigateToStats() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin_stats.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnStats.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TravelMate - Statistiques");
            stage.setMaximized(true);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir les statistiques: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void navigateToItineraires() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ItineraireEtEtape/PageGestionItineraires.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnItineraires.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TravelMate - Gestion des Itinéraires");
            stage.setMaximized(true);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la gestion des itinéraires: " + e.getMessage());
            e.printStackTrace();
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
            e.printStackTrace();
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
        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Utilisateur non connecté");
            return;
        }

        // Check if this destination was added by someone else
        if (destination.getAdded_by() != currentUser.getId()) {
            showDeleteReasonDialog(destination);
        } else {
            showSimpleDeleteConfirmation(destination);
        }
    }

    private void showDeleteReasonDialog(Destination destination) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DestinationDeleteReasonDialog.fxml"));
            GridPane root = loader.load();

            DestinationDeleteReasonController controller = loader.getController();
            controller.setDestination(destination);
            controller.setOnConfirm(result -> {
                performDeletionWithReason(destination, result);
            });

            Stage stage = new Stage();
            stage.setTitle("Raison de suppression");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la boîte de dialogue: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showSimpleDeleteConfirmation(Destination destination) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer la destination");
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer " + destination.getNom_destination() + " ?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            performDeletion(destination, null, null);
        }
    }

    private void performDeletionWithReason(Destination destination, DestinationDeleteReasonController.DeleteReasonResult result) {
        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser == null) return;

        try {
            // Create notification for the user who added this destination
            if (destination.getAdded_by() > 0) {
                DeleteNotification notification = new DeleteNotification();
                notification.setUser_id(destination.getAdded_by());
                notification.setUser_name(destination.getAdded_by_name());
                notification.setAdmin_id(currentUser.getId());
                notification.setAdmin_name(currentUser.getPrenom() + " " + currentUser.getNom());
                notification.setItem_type("Destination");
                notification.setItem_id(destination.getId_destination());
                notification.setItem_name(destination.getNom_destination() + " (" + destination.getPays_destination() + ")");
                notification.setReason(result.getReason());
                notification.setCustom_reason(result.getCustomReason());

                notificationCRUD.ajouter(notification);
            }

            // Then perform the deletion
            performDeletion(destination, result.getReason(), result.getCustomReason());

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible de créer la notification: " + e.getMessage());
        }
    }

    private void performDeletion(Destination destination, String reason, String customReason) {
        try {
            destinationCRUD.supprimer(destination);
            allDestinations.remove(destination);
            updateTableData();
            updateStats();

            if (reason != null) {
                String fullReason = reason.equals("Autre") ? customReason : reason;
                showAlert(Alert.AlertType.INFORMATION, "Succès",
                        "Destination supprimée avec succès!\n\nRaison: " + fullReason);
            } else {
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Destination supprimée avec succès!");
            }

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer: " + e.getMessage());
        }
    }

    private void handleBulkDelete() {
        ObservableList<Destination> selectedItems = tableDestinations.getSelectionModel().getSelectedItems();
        if (selectedItems.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner au moins une destination à supprimer");
            return;
        }

        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Utilisateur non connecté");
            return;
        }

        // Check if any selected items were added by others
        boolean hasOthersItems = selectedItems.stream()
                .anyMatch(d -> d.getAdded_by() != currentUser.getId());

        if (hasOthersItems) {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Raison de suppression");
            dialog.setHeaderText("Suppression de " + selectedItems.size() + " destination(s)");
            dialog.setContentText("Raison de la suppression (s'affichera pour les créateurs):");

            Optional<String> result = dialog.showAndWait();
            result.ifPresent(reason -> {
                performBulkDeletion(selectedItems, reason, currentUser);
            });
        } else {
            StringBuilder namesList = new StringBuilder();
            for (Destination d : selectedItems) {
                namesList.append("• ").append(d.getNom_destination())
                        .append(" (").append(d.getPays_destination()).append(")\n");
            }

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmation de suppression");
            confirm.setHeaderText("Supprimer " + selectedItems.size() + " destination(s)");
            confirm.setContentText("Destinations à supprimer :\n\n" + namesList.toString() + "\nÊtes-vous sûr de vouloir continuer ?");

            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                performBulkDeletion(selectedItems, null, currentUser);
            }
        }
    }

    private void performBulkDeletion(ObservableList<Destination> selectedItems, String globalReason, User currentUser) {
        int successCount = 0;
        int failCount = 0;
        List<String> successfulNames = new ArrayList<>();
        List<String> failedNames = new ArrayList<>();

        for (Destination destination : new ArrayList<>(selectedItems)) {
            try {
                // Create notification if this item belongs to someone else
                if (destination.getAdded_by() != currentUser.getId() && globalReason != null && !globalReason.isEmpty()) {
                    DeleteNotification notification = new DeleteNotification();
                    notification.setUser_id(destination.getAdded_by());
                    notification.setUser_name(destination.getAdded_by_name());
                    notification.setAdmin_id(currentUser.getId());
                    notification.setAdmin_name(currentUser.getPrenom() + " " + currentUser.getNom());
                    notification.setItem_type("Destination");
                    notification.setItem_id(destination.getId_destination());
                    notification.setItem_name(destination.getNom_destination() + " (" + destination.getPays_destination() + ")");
                    notification.setReason("Suppression groupée");
                    notification.setCustom_reason(globalReason);

                    notificationCRUD.ajouter(notification);
                }

                destinationCRUD.supprimer(destination);
                allDestinations.remove(destination);
                successCount++;
                successfulNames.add(destination.getNom_destination() + " (" + destination.getPays_destination() + ")");

            } catch (SQLException e) {
                failCount++;
                failedNames.add(destination.getNom_destination() + " (" + destination.getPays_destination() + ")");
                System.err.println("Erreur lors de la suppression de " + destination.getNom_destination() + ": " + e.getMessage());
            }
        }

        updateTableData();
        updateStats();

        if (failCount == 0) {
            StringBuilder successList = new StringBuilder();
            for (String name : successfulNames) successList.append("✓ ").append(name).append("\n");
            showAlert(Alert.AlertType.INFORMATION, "Succès",
                    successCount + " destination(s) supprimée(s) avec succès !\n\n" + successList.toString());
        } else {
            StringBuilder successList = new StringBuilder();
            for (String name : successfulNames) successList.append("✓ ").append(name).append("\n");
            StringBuilder failList = new StringBuilder();
            for (String name : failedNames) failList.append("✗ ").append(name).append("\n");
            showAlert(Alert.AlertType.WARNING, "Suppression partielle",
                    successCount + " supprimée(s), " + failCount + " échec(s)\n\n" +
                            "✅ Réussis :\n" + successList.toString() + "\n" +
                            "❌ Échecs :\n" + failList.toString());
        }
    }

    private void handleConsulter(Destination destination) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherDestinationBack.fxml"));
            Parent root = loader.load();
            AfficherDestinationBackController controller = loader.getController();
            controller.setDestination(destination);
            controller.setParentController(this);

            Stage stage = new Stage();
            stage.setTitle("Détails - " + destination.getNom_destination());
            stage.setScene(new Scene(root));
            stage.setWidth(1100);
            stage.setHeight(1000);
            stage.setResizable(true);
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir les détails: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadDestinations() {
        try {
            allDestinations = destinationCRUD.afficher();
            destinationList.setAll(allDestinations);
            tableDestinations.setItems(destinationList);
            updateStats();
            updateTableData();
            updateRegionStats();
            System.out.println("Loaded " + allDestinations.size() + " destinations");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les destinations: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupRegionChart() {
        regionPieChart.setTitle("Répartition par Région");
        regionPieChart.setLabelsVisible(true);
        regionPieChart.setLegendVisible(true);
        regionPieChart.setAnimated(true);
        regionPieChart.setStyle("-fx-text-fill: white;");
    }

    private void updateRegionStats() {
        int total = allDestinations.size();

        // Group destinations by region
        Map<String, Long> regionCounts = allDestinations.stream()
                .filter(d -> d.getRegion_destination() != null && !d.getRegion_destination().trim().isEmpty())
                .collect(Collectors.groupingBy(
                        Destination::getRegion_destination,
                        Collectors.counting()
                ));

        long destinationsWithRegion = regionCounts.values().stream().mapToLong(Long::longValue).sum();
        long destinationsWithoutRegion = total - destinationsWithRegion;

        // Update total regions count
        lblTotalRegions.setText(String.valueOf(regionCounts.size()));

        // Update coverage percentage
        double coverage = total > 0 ? (double) destinationsWithRegion / total * 100 : 0;
        lblRegionCoverage.setText(String.format("%.1f%% des destinations ont une région", coverage));

        // Clear existing chart data
        regionPieChart.getData().clear();

        // Create pie chart data
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

        // Add regions to pie chart
        for (Map.Entry<String, Long> entry : regionCounts.entrySet()) {
            double percentage = (double) entry.getValue() / total * 100;
            String label = String.format("%s (%.1f%%)", entry.getKey(), percentage);
            pieChartData.add(new PieChart.Data(label, entry.getValue()));
        }

        // Add "Sans région" category if there are destinations without region
        if (destinationsWithoutRegion > 0) {
            double percentage = (double) destinationsWithoutRegion / total * 100;
            String label = String.format("Sans région (%.1f%%)", percentage);
            pieChartData.add(new PieChart.Data(label, destinationsWithoutRegion));
        }

        regionPieChart.setData(pieChartData);

        // Style the pie chart slices
        for (PieChart.Data data : pieChartData) {
            data.getNode().setStyle("-fx-pie-color: derive(#ff8c42, " +
                    (pieChartData.indexOf(data) * 20) + "%);");
        }

        // Clear and rebuild region stats container
        regionStatsContainer.getChildren().clear();

        // Add header
        Label headerLabel = new Label("Détail par région");
        headerLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: #ffffff; -fx-font-size: 14; -fx-padding: 0 0 10 0;");
        regionStatsContainer.getChildren().add(headerLabel);

        // Add region statistics in a grid
        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(10);
        statsGrid.setVgap(8);
        statsGrid.setPadding(new Insets(5, 0, 0, 0));

        int row = 0;

        // Add each region with count and percentage
        for (Map.Entry<String, Long> entry : regionCounts.entrySet()) {
            double percentage = (double) entry.getValue() / total * 100;

            Label regionName = new Label(entry.getKey());
            regionName.setStyle("-fx-text-fill: #ff8c42; -fx-font-weight: 600; -fx-font-size: 12;");

            Label countLabel = new Label(entry.getValue() + " dest.");
            countLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11;");

            Label percentLabel = new Label(String.format("%.1f%%", percentage));
            percentLabel.setStyle("-fx-text-fill: #10b981; -fx-font-size: 11; -fx-font-weight: 600;");

            statsGrid.add(regionName, 0, row);
            statsGrid.add(countLabel, 1, row);
            statsGrid.add(percentLabel, 2, row);

            row++;
        }

        // Add "Sans région" if applicable
        if (destinationsWithoutRegion > 0) {
            double percentage = (double) destinationsWithoutRegion / total * 100;

            Label regionName = new Label("Sans région");
            regionName.setStyle("-fx-text-fill: #94a3b8; -fx-font-weight: 600; -fx-font-size: 12; -fx-font-style: italic;");

            Label countLabel = new Label(destinationsWithoutRegion + " dest.");
            countLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11;");

            Label percentLabel = new Label(String.format("%.1f%%", percentage));
            percentLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11; -fx-font-weight: 600;");

            statsGrid.add(regionName, 0, row);
            statsGrid.add(countLabel, 1, row);
            statsGrid.add(percentLabel, 2, row);
        }

        regionStatsContainer.getChildren().add(statsGrid);
    }

    private void updateStats() {
        int total = allDestinations.size();

        lblDestinationsTotal.setText(String.valueOf(total));
        lblTotalDestinations.setText(String.valueOf(total));
        lblSidebarDestinationCount.setText(String.valueOf(total));
        lblDestinationCount.setText(total + " destination" + (total > 1 ? "s" : ""));

        long paysCount = allDestinations.stream()
                .map(Destination::getPays_destination).filter(Objects::nonNull).distinct().count();
        lblPaysCount.setText(String.valueOf(paysCount));
        lblPaysTotal.setText(String.valueOf(paysCount));

        long climatesCount = allDestinations.stream()
                .map(Destination::getClimat_destination).filter(Objects::nonNull).distinct().count();
        lblClimatsCount.setText(String.valueOf(climatesCount));
        lblClimatsTotal.setText(String.valueOf(climatesCount));

        long seasonsCount = allDestinations.stream()
                .map(Destination::getSaison_destination).filter(Objects::nonNull).distinct().count();
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

        paginationContainer.getChildren().clear();
        int totalPages = (int) Math.ceil((double) total / rowsPerPage);

        if (totalPages <= 1) return;

        if (currentPage > 1) {
            Label prevLabel = createPageLabel("◀", false);
            prevLabel.setOnMouseClicked(event -> { currentPage--; updateTableData(); });
            paginationContainer.getChildren().add(prevLabel);
        }

        int startPage = Math.max(1, currentPage - 2);
        int endPage = Math.min(totalPages, startPage + 4);

        for (int i = startPage; i <= endPage; i++) {
            int pageNum = i;
            Label pageLabel = createPageLabel(String.valueOf(i), i == currentPage);
            pageLabel.setOnMouseClicked(event -> { currentPage = pageNum; updateTableData(); });
            paginationContainer.getChildren().add(pageLabel);
        }

        if (currentPage < totalPages) {
            Label nextLabel = createPageLabel("▶", false);
            nextLabel.setOnMouseClicked(event -> { currentPage++; updateTableData(); });
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
                            d.getPays_destination().toLowerCase().contains(searchTerm.toLowerCase()) ||
                            (d.getRegion_destination() != null && d.getRegion_destination().toLowerCase().contains(searchTerm.toLowerCase())))
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
                .map(Destination::getClimat_destination).filter(Objects::nonNull).distinct()
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

    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}