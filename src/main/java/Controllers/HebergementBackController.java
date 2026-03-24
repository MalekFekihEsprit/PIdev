package Controllers;

import Entities.Destination;
import Entities.Hebergement;
import Entities.DeleteNotification;
import Entities.User;
import Services.DestinationCRUD;
import Services.HebergementCRUD;
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
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class HebergementBackController implements Initializable {

    // ============== FXML INJECTED ELEMENTS ==============

    // Bottom Status
    @FXML private Label lblLastUpdate;

    // Sidebar Stats
    @FXML private Label lblSidebarHebergementCount;
    @FXML private Label lblTotalHebergements;
    @FXML private Label lblTypesCount;
    @FXML private Label lblDestinationsCount;
    @FXML private Label lblPrixMoyen;

    // KPI Cards
    @FXML private Label lblHebergementsTotal;
    @FXML private Label lblTypesTotal;
    @FXML private Label lblDestinationsAssociees;
    @FXML private Label lblPrixMoyenKPI;

    // Table Section
    @FXML private Label lblHebergementCount;
    @FXML private TableView<Hebergement> tableHebergements;
    @FXML private TableColumn<Hebergement, Integer> colId;
    @FXML private TableColumn<Hebergement, String> colNom;
    @FXML private TableColumn<Hebergement, String> colType;
    @FXML private TableColumn<Hebergement, Double> colPrix;
    @FXML private TableColumn<Hebergement, String> colAdresse;
    @FXML private TableColumn<Hebergement, Double> colNote;
    @FXML private TableColumn<Hebergement, String> colDestination;
    @FXML private TableColumn<Hebergement, String> colAddedBy;
    @FXML private TableColumn<Hebergement, Void> colActions;

    // Buttons
    @FXML private Button btnAjouter;
    @FXML private Button btnSupprimer;
    @FXML private HBox btnSearch;
    @FXML private HBox btnFilter;
    @FXML private HBox btnDestinations;
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

    private HebergementCRUD hebergementCRUD;
    private DestinationCRUD destinationCRUD;
    private DeleteNotificationCRUD notificationCRUD;
    private ObservableList<Hebergement> hebergementList = FXCollections.observableArrayList();
    private List<Hebergement> allHebergements = new ArrayList<>();
    private int currentPage = 1;
    private final int rowsPerPage = 10;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        hebergementCRUD = new HebergementCRUD();
        destinationCRUD = new DestinationCRUD();
        notificationCRUD = new DeleteNotificationCRUD();

        setupTableColumns();
        setupTableProperties();
        setupActionsColumn();
        loadHebergements();
        setupButtonActions();
        setupNavigationButtons();
        setupUserProfile();
        updateLastUpdateTime();
        updateUserInfo();
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id_hebergement"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom_hebergement"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type_hebergement"));
        colPrix.setCellValueFactory(new PropertyValueFactory<>("prixNuit_hebergement"));
        colAdresse.setCellValueFactory(new PropertyValueFactory<>("adresse_hebergement"));
        colNote.setCellValueFactory(new PropertyValueFactory<>("note_hebergement"));

        // FIXED: Properly set up the added_by column to show the user's full name
        colAddedBy.setCellValueFactory(new PropertyValueFactory<>("added_by_name"));

        colDestination.setCellValueFactory(cellData -> {
            Hebergement h = cellData.getValue();
            Destination dest = h.getDestination();
            String destDisplay = (dest != null) ? dest.getNom_destination() + ", " + dest.getPays_destination() : "-";
            return new javafx.beans.property.SimpleStringProperty(destDisplay);
        });

        colPrix.setCellFactory(col -> new TableCell<Hebergement, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty) {
                    setText(null);
                } else if (price == null || price == 0.0) {
                    setText("-");
                } else {
                    setText(String.format("%.2f €", price));
                }
            }
        });

        colNote.setCellFactory(col -> new TableCell<Hebergement, Double>() {
            @Override
            protected void updateItem(Double note, boolean empty) {
                super.updateItem(note, empty);
                if (empty) {
                    setText(null);
                } else if (note == null || note == 0.0) {
                    setText("-");
                } else {
                    setText(String.format("%.1f ⭐", note));
                }
            }
        });

        // Format null/empty values for added_by
        colAddedBy.setCellFactory(col -> new TableCell<Hebergement, String>() {
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

    private void setupTableProperties() {
        tableHebergements.setFixedCellSize(35);
        tableHebergements.setPrefHeight(380);
        tableHebergements.setMaxHeight(380);
        tableHebergements.setMinHeight(380);
        tableHebergements.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    private void setupActionsColumn() {
        colActions.setCellFactory(new Callback<>() {
            @Override
            public TableCell<Hebergement, Void> call(final TableColumn<Hebergement, Void> param) {
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
                            Hebergement hebergement = getTableView().getItems().get(getIndex());
                            btnConsulter.setOnMouseClicked(event -> handleConsulter(hebergement));
                            btnModifier.setOnMouseClicked(event -> handleModifier(hebergement));
                            btnSupprimer.setOnMouseClicked(event -> handleDeleteSingle(hebergement));
                            setGraphic(actionBox);
                        }
                    }
                };
            }
        });
    }

    private void setupButtonActions() {
        if (btnAjouter != null) btnAjouter.setOnAction(event -> handleAjouter());
        if (btnSupprimer != null) btnSupprimer.setOnAction(event -> handleBulkDelete());
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
        setupSidebarButtonHover(btnDestinations, "🌍", "Destinations");
        if (btnDestinations != null) btnDestinations.setOnMouseClicked(event -> navigateToDestinations());

        setupSidebarButtonHover(btnUsers, "👥", "Utilisateurs");
        if (btnUsers != null) btnUsers.setOnMouseClicked(event -> navigateToUsers());

        setupSidebarButtonHover(btnStats, "📊", "Statistiques");
        if (btnStats != null) btnStats.setOnMouseClicked(event -> navigateToStats());

        setupSidebarButtonHover(btnItineraires, "🗺️", "Itinéraires");
        if (btnItineraires != null) btnItineraires.setOnMouseClicked(event -> navigateToItineraires());

        setupSidebarButtonHover(btnActivites, "🏄", "Activités");
        if (btnActivites != null) btnActivites.setOnMouseClicked(event -> navigateToActivites());

        setupSidebarButtonHover(btnVoyages, "✈️", "Voyages");
        if (btnVoyages != null) btnVoyages.setOnMouseClicked(event -> navigateToVoyages());

        setupSidebarButtonHover(btnBudgets, "💰", "Budgets");
        if (btnBudgets != null) btnBudgets.setOnMouseClicked(event -> navigateToBudgets());
    }

    private void navigateToBudgets() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/BudgetDepenseBack.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnDestinations.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TravelMate - Gestion des budgets");
            stage.setMaximized(true);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la gestion des hébergements: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void navigateToVoyages() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/PageVoyageBack.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnDestinations.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TravelMate - Gestion des Voyages");
            stage.setMaximized(true);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la gestion des hébergements: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void navigateToActivites() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/activitesback.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnDestinations.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TravelMate - Gestion des activites");
            stage.setMaximized(true);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la gestion: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupSidebarButtonHover(HBox button, String icon, String text) {
        if (button == null) return;

        button.setOnMouseEntered(event -> {
            button.setStyle("-fx-background-color: rgba(16,185,129,0.15); -fx-background-radius: 12; -fx-padding: 12 16; -fx-cursor: hand; -fx-border-color: #10b981; -fx-border-width: 1; -fx-border-radius: 12;");
            button.lookupAll(".label").forEach(label -> {
                if (label instanceof Label) {
                    Label lbl = (Label) label;
                    if (lbl.getText().equals(icon)) {
                        lbl.setStyle("-fx-font-size: 16;");
                    } else {
                        lbl.setStyle("-fx-text-fill: #10b981; -fx-font-weight: 600; -fx-font-size: 14;");
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

    private void navigateToDestinations() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DestinationBack.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnDestinations.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TravelMate - Gestion des Destinations");
            stage.setMaximized(true);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir les destinations: " + e.getMessage());
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterHebergement.fxml"));
            Parent root = loader.load();
            AjouterHebergementController controller = loader.getController();
            controller.setParentController(this);

            Stage stage = new Stage();
            stage.setTitle("Ajouter un hébergement");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage());
            e.printStackTrace();
        }
    }

    void handleModifier(Hebergement hebergement) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierHebergement.fxml"));
            Parent root = loader.load();
            ModifierHebergementController controller = loader.getController();
            controller.setHebergement(hebergement);
            controller.setParentController(this);

            Stage stage = new Stage();
            stage.setTitle("Modifier - " + hebergement.getNom_hebergement());
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleDeleteSingle(Hebergement hebergement) {
        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Utilisateur non connecté");
            return;
        }

        // Check if this hebergement was added by someone else
        if (hebergement.getAdded_by() != currentUser.getId()) {
            showDeleteReasonDialog(hebergement);
        } else {
            showSimpleDeleteConfirmation(hebergement);
        }
    }

    private void showDeleteReasonDialog(Hebergement hebergement) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/HebergementDeleteReasonDialog.fxml"));
            GridPane root = loader.load();

            HebergementDeleteReasonController controller = loader.getController();
            controller.setHebergement(hebergement);
            controller.setOnConfirm(result -> {
                performDeletionWithReason(hebergement, result);
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

    private void showSimpleDeleteConfirmation(Hebergement hebergement) {
        String details = String.format(
                "Êtes-vous sûr de vouloir supprimer cet hébergement ?\n\n" +
                        "🏨 Nom: %s\n" +
                        "📋 Type: %s\n" +
                        "📍 Adresse: %s\n" +
                        "💰 Prix: %.2f €\n" +
                        "⭐ Note: %.1f/5\n" +
                        "🌍 Destination: %s",
                hebergement.getNom_hebergement(),
                hebergement.getType_hebergement(),
                hebergement.getAdresse_hebergement(),
                hebergement.getPrixNuit_hebergement(),
                hebergement.getNote_hebergement(),
                hebergement.getDestination() != null ?
                        hebergement.getDestination().getNom_destination() + ", " + hebergement.getDestination().getPays_destination() :
                        "Non spécifié"
        );

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation de suppression");
        confirm.setHeaderText("Supprimer l'hébergement");
        confirm.setContentText(details);

        DialogPane dialogPane = confirm.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #111633;");
        dialogPane.lookup(".content.label").setStyle("-fx-text-fill: white; -fx-font-size: 13;");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            performDeletion(hebergement, null, null);
        }
    }

    private void performDeletionWithReason(Hebergement hebergement, HebergementDeleteReasonController.DeleteReasonResult result) {
        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser == null) return;

        try {
            // Create notification for the user who added this hebergement
            if (hebergement.getAdded_by() > 0) {
                DeleteNotification notification = new DeleteNotification();
                notification.setUser_id(hebergement.getAdded_by());
                notification.setUser_name(hebergement.getAdded_by_name());
                notification.setAdmin_id(currentUser.getId());
                notification.setAdmin_name(currentUser.getPrenom() + " " + currentUser.getNom());
                notification.setItem_type("Hébergement");
                notification.setItem_id(hebergement.getId_hebergement());
                notification.setItem_name(hebergement.getNom_hebergement() + " (" + hebergement.getType_hebergement() + ")");
                notification.setReason(result.getReason());
                notification.setCustom_reason(result.getCustomReason());

                notificationCRUD.ajouter(notification);
            }

            // Then perform the deletion
            performDeletion(hebergement, result.getReason(), result.getCustomReason());

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible de créer la notification: " + e.getMessage());
        }
    }

    private void performDeletion(Hebergement hebergement, String reason, String customReason) {
        try {
            String nomHebergement = hebergement.getNom_hebergement();
            String typeHebergement = hebergement.getType_hebergement();

            hebergementCRUD.supprimer(hebergement);
            allHebergements.remove(hebergement);
            updateTableData();
            updateStats();

            if (reason != null) {
                String fullReason = reason.equals("Autre") ? customReason : reason;
                showAlert(Alert.AlertType.INFORMATION, "Succès",
                        "Hébergement supprimé avec succès !\n\n" +
                                "🏨 " + nomHebergement + "\n" +
                                "📋 Type: " + typeHebergement + "\n" +
                                "Raison: " + fullReason);
            } else {
                showAlert(Alert.AlertType.INFORMATION, "Succès",
                        "Hébergement supprimé avec succès !\n\n" +
                                "🏨 " + nomHebergement + "\n" +
                                "📋 Type: " + typeHebergement);
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible de supprimer l'hébergement :\n\n" +
                            "🏨 " + hebergement.getNom_hebergement() + "\n" +
                            "Raison: " + e.getMessage());
        }
    }

    private void handleBulkDelete() {
        ObservableList<Hebergement> selectedItems = tableHebergements.getSelectionModel().getSelectedItems();
        if (selectedItems.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner au moins un hébergement à supprimer");
            return;
        }

        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Utilisateur non connecté");
            return;
        }

        // Check if any selected items were added by others
        boolean hasOthersItems = selectedItems.stream()
                .anyMatch(h -> h.getAdded_by() != currentUser.getId());

        if (hasOthersItems) {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Raison de suppression");
            dialog.setHeaderText("Suppression de " + selectedItems.size() + " hébergement(s)");
            dialog.setContentText("Raison de la suppression (s'affichera pour les créateurs):");

            Optional<String> result = dialog.showAndWait();
            result.ifPresent(reason -> {
                performBulkDeletion(selectedItems, reason, currentUser);
            });
        } else {
            StringBuilder namesList = new StringBuilder();
            for (Hebergement h : selectedItems) {
                namesList.append("• ").append(h.getNom_hebergement())
                        .append(" (").append(h.getType_hebergement()).append(")")
                        .append(" - ").append(String.format("%.2f €", h.getPrixNuit_hebergement())).append("\n");
            }

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmation de suppression");
            confirm.setHeaderText("Supprimer " + selectedItems.size() + " hébergement(s)");
            confirm.setContentText("Hébergements à supprimer :\n\n" + namesList.toString() +
                    "\nÊtes-vous sûr de vouloir continuer ?");

            DialogPane dialogPane = confirm.getDialogPane();
            dialogPane.setStyle("-fx-background-color: #111633;");
            dialogPane.lookup(".content.label").setStyle("-fx-text-fill: white; -fx-font-size: 13;");

            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                performBulkDeletion(selectedItems, null, currentUser);
            }
        }
    }

    private void performBulkDeletion(ObservableList<Hebergement> selectedItems, String globalReason, User currentUser) {
        int successCount = 0;
        int failCount = 0;
        List<String> successfulNames = new ArrayList<>();
        List<String> failedNames = new ArrayList<>();

        for (Hebergement hebergement : new ArrayList<>(selectedItems)) {
            try {
                // Create notification if this item belongs to someone else
                if (hebergement.getAdded_by() != currentUser.getId() && globalReason != null && !globalReason.isEmpty()) {
                    DeleteNotification notification = new DeleteNotification();
                    notification.setUser_id(hebergement.getAdded_by());
                    notification.setUser_name(hebergement.getAdded_by_name());
                    notification.setAdmin_id(currentUser.getId());
                    notification.setAdmin_name(currentUser.getPrenom() + " " + currentUser.getNom());
                    notification.setItem_type("Hébergement");
                    notification.setItem_id(hebergement.getId_hebergement());
                    notification.setItem_name(hebergement.getNom_hebergement() + " (" + hebergement.getType_hebergement() + ")");
                    notification.setReason("Suppression groupée");
                    notification.setCustom_reason(globalReason);

                    notificationCRUD.ajouter(notification);
                }

                hebergementCRUD.supprimer(hebergement);
                allHebergements.remove(hebergement);
                successCount++;
                successfulNames.add(hebergement.getNom_hebergement() + " (" + hebergement.getType_hebergement() + ")");

            } catch (SQLException e) {
                failCount++;
                failedNames.add(hebergement.getNom_hebergement() + " (" + hebergement.getType_hebergement() + ")");
                System.err.println("Erreur lors de la suppression de " + hebergement.getNom_hebergement() + ": " + e.getMessage());
            }
        }

        updateTableData();
        updateStats();

        if (failCount == 0) {
            StringBuilder successList = new StringBuilder();
            for (String name : successfulNames) successList.append("✓ ").append(name).append("\n");
            showAlert(Alert.AlertType.INFORMATION, "Succès",
                    successCount + " hébergement(s) supprimé(s) avec succès !\n\n" + successList.toString());
        } else {
            StringBuilder successList = new StringBuilder();
            for (String name : successfulNames) successList.append("✓ ").append(name).append("\n");
            StringBuilder failList = new StringBuilder();
            for (String name : failedNames) failList.append("✗ ").append(name).append("\n");
            showAlert(Alert.AlertType.WARNING, "Suppression partielle",
                    successCount + " supprimé(s), " + failCount + " échec(s)\n\n" +
                            "✅ Réussis :\n" + successList.toString() + "\n" +
                            "❌ Échecs :\n" + failList.toString());
        }
    }

    private void handleConsulter(Hebergement hebergement) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherHebergementBack.fxml"));
            Parent root = loader.load();
            AfficherHebergementBackController controller = loader.getController();
            controller.setHebergement(hebergement);
            controller.setParentController(this);

            Stage stage = new Stage();
            stage.setTitle("Détails - " + hebergement.getNom_hebergement());
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir les détails: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadHebergements() {
        try {
            allHebergements = hebergementCRUD.afficher();
            hebergementList.setAll(allHebergements);
            tableHebergements.setItems(hebergementList);
            updateStats();
            updateTableData();
            System.out.println("Loaded " + allHebergements.size() + " hébergements");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les hébergements: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateStats() {
        int total = allHebergements.size();

        lblHebergementsTotal.setText(String.valueOf(total));
        lblTotalHebergements.setText(String.valueOf(total));
        lblSidebarHebergementCount.setText(String.valueOf(total));
        lblHebergementCount.setText(total + " hébergement" + (total > 1 ? "s" : ""));

        long typesCount = allHebergements.stream()
                .map(Hebergement::getType_hebergement).filter(Objects::nonNull).distinct().count();
        lblTypesTotal.setText(String.valueOf(typesCount));
        lblTypesCount.setText(String.valueOf(typesCount));

        long destsCount = allHebergements.stream()
                .map(Hebergement::getDestination).filter(Objects::nonNull)
                .map(Destination::getId_destination).distinct().count();
        lblDestinationsAssociees.setText(String.valueOf(destsCount));
        lblDestinationsCount.setText(String.valueOf(destsCount));

        double avgPrice = allHebergements.stream()
                .mapToDouble(Hebergement::getPrixNuit_hebergement).average().orElse(0.0);
        String avgPriceStr = String.format("%.2f €", avgPrice);
        lblPrixMoyenKPI.setText(avgPriceStr);
        lblPrixMoyen.setText(avgPriceStr);
    }

    private void updateTableData() {
        int total = allHebergements.size();

        if (total == 0) {
            hebergementList.clear();
            tableHebergements.setItems(hebergementList);
            updatePagination();
            return;
        }

        int fromIndex = (currentPage - 1) * rowsPerPage;
        int toIndex = Math.min(fromIndex + rowsPerPage, total);

        if (fromIndex < total) {
            List<Hebergement> pageData = allHebergements.subList(fromIndex, toIndex);
            hebergementList.setAll(pageData);
            tableHebergements.setItems(hebergementList);
        } else {
            currentPage = Math.max(1, (int) Math.ceil((double) total / rowsPerPage));
            fromIndex = (currentPage - 1) * rowsPerPage;
            toIndex = Math.min(fromIndex + rowsPerPage, total);
            List<Hebergement> pageData = allHebergements.subList(fromIndex, toIndex);
            hebergementList.setAll(pageData);
            tableHebergements.setItems(hebergementList);
        }

        updatePagination();
    }

    private void updatePagination() {
        int total = allHebergements.size();
        int start = total == 0 ? 0 : (currentPage - 1) * rowsPerPage + 1;
        int end = Math.min(currentPage * rowsPerPage, total);

        lblPaginationInfo.setText(total == 0 ? "0-0 sur 0 hébergements" : start + "-" + end + " sur " + total + " hébergements");

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
            label.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-background-radius: 8; -fx-min-width: 32; -fx-min-height: 32; -fx-alignment: center; -fx-font-size: 12; -fx-cursor: hand;");
        } else {
            label.setStyle("-fx-background-color: #1e2749; -fx-text-fill: #94a3b8; -fx-background-radius: 8; -fx-min-width: 32; -fx-min-height: 32; -fx-alignment: center; -fx-font-size: 12; -fx-cursor: hand;");
        }
        return label;
    }

    public void refreshAfterModification() {
        loadHebergements();
        updateLastUpdateTime();
    }

    private void updateLastUpdateTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");
        lblLastUpdate.setText("Dernière mise à jour: " + LocalDateTime.now().format(formatter));
    }

    private void handleSearch() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Recherche");
        dialog.setHeaderText("Rechercher un hébergement");
        dialog.setContentText("Nom de l'hébergement:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(searchTerm -> {
            if (searchTerm.trim().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez entrer un terme de recherche");
                return;
            }

            List<Hebergement> searchResults = allHebergements.stream()
                    .filter(h -> h.getNom_hebergement().toLowerCase().contains(searchTerm.toLowerCase()) ||
                            h.getAdresse_hebergement().toLowerCase().contains(searchTerm.toLowerCase()))
                    .collect(Collectors.toList());

            if (searchResults.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "Résultat", "Aucun hébergement trouvé");
            } else {
                hebergementList.setAll(searchResults);
                tableHebergements.setItems(hebergementList);
                lblHebergementCount.setText(searchResults.size() + " résultat(s)");
            }
        });
    }

    private void handleFilter() {
        List<String> types = allHebergements.stream()
                .map(Hebergement::getType_hebergement).filter(Objects::nonNull).distinct()
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
                lblHebergementCount.setText(allHebergements.size() + " hébergement(s)");
            } else {
                List<Hebergement> filteredResults = allHebergements.stream()
                        .filter(h -> type.equals(h.getType_hebergement()))
                        .collect(Collectors.toList());

                if (filteredResults.isEmpty()) {
                    showAlert(Alert.AlertType.INFORMATION, "Résultat", "Aucun hébergement trouvé pour ce type");
                } else {
                    hebergementList.setAll(filteredResults);
                    tableHebergements.setItems(hebergementList);
                    lblHebergementCount.setText(filteredResults.size() + " résultat(s)");
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

    public void filterByDestination(Destination destination) {
        if (destination == null) return;

        try {
            allHebergements = hebergementCRUD.afficher();
            List<Hebergement> filtered = allHebergements.stream()
                    .filter(h -> h.getDestination() != null &&
                            h.getDestination().getId_destination() == destination.getId_destination())
                    .collect(Collectors.toList());

            hebergementList.setAll(filtered);
            tableHebergements.setItems(hebergementList);
            lblHebergementCount.setText(filtered.size() + " hébergement" + (filtered.size() > 1 ? "s" : "") +
                    " à " + destination.getNom_destination());
            currentPage = 1;
            updatePagination();
            System.out.println("Filtered to " + filtered.size() + " hébergements for destination: " + destination.getNom_destination());
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de filtrer les hébergements: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void clearFilter() {
        try {
            allHebergements = hebergementCRUD.afficher();
            hebergementList.setAll(allHebergements);
            tableHebergements.setItems(hebergementList);
            lblHebergementCount.setText(allHebergements.size() + " hébergement" + (allHebergements.size() > 1 ? "s" : ""));
            currentPage = 1;
            updatePagination();
            updateStats();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de réinitialiser le filtre: " + e.getMessage());
            e.printStackTrace();
        }
    }
}