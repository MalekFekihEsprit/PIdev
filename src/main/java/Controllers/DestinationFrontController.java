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
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
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
    @FXML private HBox btnHebergements;
    @FXML private HBox btnItineraires;
    @FXML private HBox btnActivites;
    @FXML private HBox btnVoyages;
    @FXML private HBox btnBudgets;
    @FXML private HBox btnCategories; // Added
    @FXML private HBox btnEvenements;
    @FXML private HBox btnHome;
    @FXML private HBox userProfileBox;
    @FXML private HBox btnNotifications;

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

    // Destinations Section
    @FXML private Label lblDestinationCount;
    @FXML private VBox containerDestinations;
    @FXML private Label lblNoDestinations;

    // Buttons
    @FXML private Button btnAjouter;
    @FXML private Button btnSuggestHotels;
    @FXML private Button btnShowMap;
    @FXML private HBox btnRefresh;
    @FXML private HBox btnSearch;
    @FXML private HBox btnFilter;
    @FXML private Label lblUserName;
    @FXML private Label lblUserRole;

    // Notification badge
    @FXML private Label lblNotificationBadge;

    // ============== CLASS VARIABLES ==============

    private DestinationCRUD destinationCRUD;
    private DeleteNotificationCRUD notificationCRUD;
    private ObservableList<Destination> destinationList = FXCollections.observableArrayList();
    private List<Destination> allDestinations = new ArrayList<>();
    private User currentUser;
    private List<DeleteNotification> unreadNotifications = new ArrayList<>();
    private Destination selectedDestination;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        destinationCRUD = new DestinationCRUD();
        notificationCRUD = new DeleteNotificationCRUD();

        // Get current user
        currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Utilisateur non connecté");
            return;
        }

        loadDestinationsCards();
        setupButtonActions();
        setupUserProfile();
        updateUserInfo();

        updateLastUpdateTime();
        setupNavigationButtons();
        setupNotifications();
        loadUnreadNotifications();
    }

    private void setupButtonActions() {
        if (btnAjouter != null) btnAjouter.setOnAction(event -> handleAjouter());
        if (btnSuggestHotels != null) btnSuggestHotels.setOnAction(event -> handleSuggestHotels());
        if (btnShowMap != null) btnShowMap.setOnAction(event -> handleShowMap());
        if (btnRefresh != null) btnRefresh.setOnMouseClicked(event -> refreshData());
        if (btnSearch != null) btnSearch.setOnMouseClicked(event -> handleSearch());
        if (btnFilter != null) btnFilter.setOnMouseClicked(event -> handleFilter());
    }

    private void setupUserProfile() {
        if (userProfileBox == null) return;

        userProfileBox.setOnMouseClicked(event -> navigateToProfile());
        userProfileBox.setOnMouseEntered(event ->
                userProfileBox.setStyle("-fx-background-color: #e2e8f0; -fx-background-radius: 25; -fx-padding: 6 16 6 6; -fx-cursor: hand;"));
        userProfileBox.setOnMouseExited(event ->
                userProfileBox.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 25; -fx-padding: 6 16 6 6; -fx-cursor: hand;"));
    }

    private void updateUserInfo() {
        if (currentUser != null) {
            lblUserName.setText(currentUser.getPrenom() + " " + currentUser.getNom());
            lblUserRole.setText(currentUser.getRole());
        } else {
            lblUserName.setText("Utilisateur");
            lblUserRole.setText("Non connecté");
        }
    }

    private void handleShowMap() {
        try {
            allDestinations = destinationCRUD.afficher();

            if (allDestinations.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "Information", "Aucune destination à afficher sur la carte.");
                return;
            }

            List<Destination> validDestinations = allDestinations.stream()
                    .filter(d -> d.getLatitude_destination() != 0.0 || d.getLongitude_destination() != 0.0)
                    .collect(Collectors.toList());

            if (validDestinations.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "Information", "Aucune destination avec des coordonnées valides.");
                return;
            }

            DestinationMapController mapController = new DestinationMapController(validDestinations);
            mapController.show();

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les destinations: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la carte: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleSuggestHotels() {
        if (selectedDestination == null) {
            showAlert(Alert.AlertType.WARNING, "Attention",
                    "Veuillez sélectionner une destination (cliquer sur une card) pour suggérer des hôtels.");
            return;
        }

        if (selectedDestination.getLatitude_destination() == 0.0 && selectedDestination.getLongitude_destination() == 0.0) {
            Alert warning = new Alert(Alert.AlertType.WARNING);
            warning.setTitle("Coordonnées manquantes");
            warning.setHeaderText("Cette destination n'a pas de coordonnées GPS");
            warning.setContentText("Pour suggérer des hôtels, la destination doit avoir des coordonnées valides.\n\n" +
                    "Veuillez modifier la destination et ajouter des coordonnées.");
            warning.showAndWait();
            return;
        }

        try {
            String fxmlPath = "/HotelSuggestionFront.fxml";
            URL fxmlUrl = getClass().getResource(fxmlPath);

            if (fxmlUrl == null) {
                fxmlPath = "/fxml/HotelSuggestionFront.fxml";
                fxmlUrl = getClass().getResource(fxmlPath);
            }

            if (fxmlUrl == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur",
                        "Fichier HotelSuggestionFront.fxml introuvable. Vérifiez que le fichier est dans le dossier resources.");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            HotelSuggestionFrontController controller = loader.getController();
            controller.setDestination(selectedDestination);
            controller.setParentController(this);

            Stage stage = new Stage();
            stage.setTitle("Suggérer des hôtels - " + selectedDestination.getNom_destination());
            stage.setScene(new Scene(root, javafx.stage.Screen.getPrimary().getVisualBounds().getWidth(), javafx.stage.Screen.getPrimary().getVisualBounds().getHeight()));
            stage.setResizable(false);
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.showAndWait();

            refreshData();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la suggestion d'hôtels: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupNotifications() {
        if (btnNotifications != null) {
            btnNotifications.setOnMouseClicked(event -> showNotificationsDialog());

            btnNotifications.setOnMouseEntered(event -> {
                btnNotifications.setStyle("-fx-background-color: #e2e8f0; -fx-background-radius: 50%; -fx-min-width: 40; -fx-min-height: 40; -fx-cursor: hand;");
            });

            btnNotifications.setOnMouseExited(event -> {
                btnNotifications.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 50%; -fx-min-width: 40; -fx-min-height: 40; -fx-cursor: hand;");
            });
        }
    }

    private void loadUnreadNotifications() {
        try {
            unreadNotifications = notificationCRUD.getUnreadNotificationsForUser(currentUser.getId());
            updateNotificationBadge();
        } catch (SQLException e) {
            System.err.println("Error loading notifications: " + e.getMessage());
        }
    }

    private void updateNotificationBadge() {
        if (lblNotificationBadge != null) {
            int count = unreadNotifications.size();
            if (count > 0) {
                lblNotificationBadge.setText(String.valueOf(count));
                lblNotificationBadge.setVisible(true);
            } else {
                lblNotificationBadge.setVisible(false);
            }
        }
    }

    private void showNotificationsDialog() {
        try {
            loadUnreadNotifications();

            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Notifications");
            dialog.setHeaderText("Suppressions de vos contenus");

            DialogPane dialogPane = dialog.getDialogPane();
            dialogPane.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-border-radius: 8;");
            dialogPane.setPrefWidth(600);
            dialogPane.setPrefHeight(500);

            VBox content = new VBox(15);
            content.setPadding(new Insets(20));

            if (unreadNotifications.isEmpty()) {
                VBox emptyBox = new VBox(10);
                emptyBox.setAlignment(javafx.geometry.Pos.CENTER);
                emptyBox.setPrefHeight(300);

                Label emptyIcon = new Label("🔔");
                emptyIcon.setStyle("-fx-font-size: 48;");

                Label noNotifLabel = new Label("Aucune nouvelle notification");
                noNotifLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 14;");

                emptyBox.getChildren().addAll(emptyIcon, noNotifLabel);
                content.getChildren().add(emptyBox);
            } else {
                HBox headerBox = new HBox(10);
                headerBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                Label countLabel = new Label(unreadNotifications.size() + " notification(s) non lue(s)");
                countLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: #0f172a; -fx-font-size: 14;");

                Region spacer = new Region();
                HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

                Button markAllRead = new Button("Tout marquer comme lu");
                markAllRead.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: 600; -fx-font-size: 12; -fx-padding: 8 16; -fx-background-radius: 8; -fx-cursor: hand;");
                markAllRead.setOnAction(e -> {
                    try {
                        notificationCRUD.markAllAsReadForUser(currentUser.getId());
                        loadUnreadNotifications();
                        showNotificationsDialog();
                    } catch (SQLException ex) {
                        showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de marquer les notifications comme lues");
                    }
                });

                headerBox.getChildren().addAll(countLabel, spacer, markAllRead);
                content.getChildren().add(headerBox);

                ListView<DeleteNotification> listView = new ListView<>();
                listView.setPrefHeight(350);
                listView.setCellFactory(param -> new ListCell<DeleteNotification>() {
                    @Override
                    protected void updateItem(DeleteNotification notification, boolean empty) {
                        super.updateItem(notification, empty);
                        if (empty || notification == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            VBox cellContent = new VBox(8);
                            cellContent.setPadding(new Insets(12));
                            cellContent.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 8; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-border-radius: 8;");

                            HBox header = new HBox(10);
                            header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                            Label iconLabel = new Label("🗑️");
                            iconLabel.setStyle("-fx-font-size: 20;");

                            Label titleLabel = new Label(notification.getItem_type() + " supprimé");
                            titleLabel.setStyle("-fx-font-weight: 700; -fx-text-fill: #ef4444; -fx-font-size: 14;");

                            Region spacer1 = new Region();
                            HBox.setHgrow(spacer1, javafx.scene.layout.Priority.ALWAYS);

                            Label dateLabel = new Label(notification.getDeleted_at().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                            dateLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11;");

                            header.getChildren().addAll(iconLabel, titleLabel, spacer1, dateLabel);

                            Label itemLabel = new Label("📌 " + notification.getItem_name());
                            itemLabel.setStyle("-fx-text-fill: #0f172a; -fx-font-size: 13; -fx-font-weight: 500;");

                            Label adminLabel = new Label("Supprimé par: " + notification.getAdmin_name());
                            adminLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 12;");

                            Label reasonLabel = new Label("Raison: " + notification.getFullReason());
                            reasonLabel.setStyle("-fx-text-fill: #475569; -fx-font-size: 12;");
                            reasonLabel.setWrapText(true);

                            Button markReadBtn = new Button("✓ Marquer comme lu");
                            markReadBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-size: 11; -fx-padding: 6 12; -fx-background-radius: 6; -fx-cursor: hand;");
                            markReadBtn.setOnAction(e -> {
                                try {
                                    notificationCRUD.markAsRead(notification.getId_notification());
                                    loadUnreadNotifications();
                                    showNotificationsDialog();
                                } catch (SQLException ex) {
                                    showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de marquer la notification comme lue");
                                }
                            });

                            HBox buttonBox = new HBox();
                            buttonBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
                            buttonBox.getChildren().add(markReadBtn);

                            cellContent.getChildren().addAll(header, itemLabel, adminLabel, reasonLabel, buttonBox);
                            setGraphic(cellContent);
                        }
                    }
                });

                ObservableList<DeleteNotification> items = FXCollections.observableArrayList(unreadNotifications);
                listView.setItems(items);
                content.getChildren().add(listView);
            }

            dialogPane.setContent(content);
            dialogPane.getButtonTypes().add(ButtonType.CLOSE);

            Button closeButton = (Button) dialogPane.lookupButton(ButtonType.CLOSE);
            closeButton.setStyle("-fx-background-color: #64748b; -fx-text-fill: white; -fx-font-weight: 600; -fx-padding: 8 16; -fx-background-radius: 8; -fx-cursor: hand;");

            dialog.showAndWait();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir les notifications: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadDestinationsCards() {
        List<Destination> destinations;
        try {
            destinations = destinationCRUD.afficher();
        } catch (SQLException e) {
            lblNoDestinations.setText("Erreur de chargement des destinations");
            lblNoDestinations.setVisible(true);
            return;
        }

        allDestinations = destinations == null ? new ArrayList<>() : new ArrayList<>(destinations);
        destinationList.setAll(allDestinations);
        renderDestinationCards(allDestinations, true);
        updateStats();
    }

    private VBox createDestinationCard(Destination d) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(16));
        card.setPrefWidth(390);
        card.setMinWidth(320);
        card.setMaxWidth(Double.MAX_VALUE);
        applyCardStyle(card, false);
        card.setUserData(d);

        Label nom = new Label(d.getNom_destination());
        nom.setStyle("-fx-font-size: 20; -fx-font-weight: bold; -fx-text-fill: #0f172a;");
        Label pays = new Label("Pays : " + d.getPays_destination());
        pays.setStyle("-fx-text-fill: #475569; -fx-font-size: 14;");
        Label region = new Label("Région : " + (d.getRegion_destination() == null || d.getRegion_destination().isEmpty() ? "-" : d.getRegion_destination()));
        region.setStyle("-fx-text-fill: #64748b; -fx-font-size: 13;");
        Label description = new Label("Description : " + d.getDescription_destination());
        description.setStyle("-fx-text-fill: #334155; -fx-font-size: 13;");
        description.setWrapText(true);
        Label climat = new Label("Climat : " + d.getClimat_destination());
        climat.setStyle("-fx-text-fill: #10b981; -fx-font-size: 13;");
        Label saison = new Label("Saison idéale : " + d.getSaison_destination());
        saison.setStyle("-fx-text-fill: #ff8c42; -fx-font-size: 13;");
        Label addedBy = new Label("Ajouté par : " + (d.getAdded_by_name() == null ? "-" : d.getAdded_by_name()));
        addedBy.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12;");

        HBox actions = new HBox(8);
        actions.setPadding(new Insets(8, 0, 0, 0));
        Button btnView = new Button("👁 Voir");
        btnView.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-background-radius: 8; -fx-font-size: 13; -fx-cursor: hand;");
        btnView.setOnAction(e -> handleConsulter(d));
        actions.getChildren().add(btnView);

        if (currentUser != null && d.getAdded_by() == currentUser.getId()) {
            Button btnEdit = new Button("✏");
            btnEdit.setStyle("-fx-background-color: #f59e0b; -fx-text-fill: white; -fx-background-radius: 8; -fx-font-size: 13; -fx-cursor: hand;");
            btnEdit.setOnAction(e -> handleModifier(d));

            Button btnDelete = new Button("🗑");
            btnDelete.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-background-radius: 8; -fx-font-size: 13; -fx-cursor: hand;");
            btnDelete.setOnAction(e -> handleDeleteSingle(d));

            actions.getChildren().addAll(btnEdit, btnDelete);
        }

        card.setOnMouseClicked(event -> {
            selectedDestination = d;
            highlightSelectedCard(card);
        });

        card.getChildren().addAll(nom, pays, region, description, climat, saison, addedBy, actions);
        return card;
    }

    private void renderDestinationCards(List<Destination> destinations, boolean updateCountText) {
        containerDestinations.getChildren().clear();

        if (destinations == null || destinations.isEmpty()) {
            lblNoDestinations.setText("Aucune destination à afficher");
            lblNoDestinations.setVisible(true);
            if (updateCountText) {
                lblDestinationCount.setText("0 destinations");
            }
            selectedDestination = null;
            return;
        }

        lblNoDestinations.setVisible(false);
        for (int i = 0; i < destinations.size(); i += 3) {
            HBox row = new HBox(18);

            for (int j = i; j < Math.min(i + 3, destinations.size()); j++) {
                VBox card = createDestinationCard(destinations.get(j));
                HBox.setHgrow(card, javafx.scene.layout.Priority.ALWAYS);
                row.getChildren().add(card);
            }

            // Keep visual balance for the last incomplete row.
            int missingSlots = 3 - row.getChildren().size();
            for (int k = 0; k < missingSlots; k++) {
                Region spacer = new Region();
                spacer.setMinWidth(0);
                HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
                row.getChildren().add(spacer);
            }

            containerDestinations.getChildren().add(row);
        }

        if (updateCountText) {
            int total = destinations.size();
            lblDestinationCount.setText(total + " destination" + (total > 1 ? "s" : ""));
        }

        if (selectedDestination == null || !destinations.contains(selectedDestination)) {
            selectedDestination = destinations.get(0);
        }
        reapplySelectionHighlight();
    }

    private void highlightSelectedCard(VBox selectedCard) {
        for (javafx.scene.Node rowNode : containerDestinations.getChildren()) {
            if (!(rowNode instanceof HBox row)) {
                continue;
            }

            for (javafx.scene.Node cardNode : row.getChildren()) {
                if (cardNode instanceof VBox cardBox) {
                    applyCardStyle(cardBox, cardBox == selectedCard);
                }
            }
        }
    }

    private void reapplySelectionHighlight() {
        for (javafx.scene.Node rowNode : containerDestinations.getChildren()) {
            if (!(rowNode instanceof HBox row)) {
                continue;
            }

            for (javafx.scene.Node cardNode : row.getChildren()) {
                if (cardNode instanceof VBox cardBox) {
                    boolean isSelected = selectedDestination != null && selectedDestination.equals(cardBox.getUserData());
                    applyCardStyle(cardBox, isSelected);
                }
            }
        }
    }

    private void applyCardStyle(VBox card, boolean selected) {
        if (selected) {
            card.setStyle("-fx-background-color: white; -fx-background-radius: 16; -fx-effect: dropshadow(three-pass-box, rgba(59,130,246,0.2), 12, 0, 0, 4); -fx-border-color: #3b82f6; -fx-border-width: 2; -fx-border-radius: 16;");
        } else {
            card.setStyle("-fx-background-color: white; -fx-background-radius: 16; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.04), 8, 0, 0, 2); -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-border-radius: 16;");
        }
    }

    private void handleConsulter(Destination destination) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherDestinationFront.fxml"));
            Parent root = loader.load();

            AfficherDestinationfrontController controller = loader.getController();
            controller.setDestination(destination);

            Stage stage = new Stage();
            stage.setTitle("Détails - " + destination.getNom_destination());
            stage.setScene(new Scene(root,
                    Screen.getPrimary().getVisualBounds().getWidth(),
                    Screen.getPrimary().getVisualBounds().getHeight()));
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir les détails: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void navigateToProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/profile.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) userProfileBox.getScene().getWindow();
            stage.setScene(new Scene(root, javafx.stage.Screen.getPrimary().getVisualBounds().getWidth(), javafx.stage.Screen.getPrimary().getVisualBounds().getHeight()));
            stage.setTitle("TravelMate - Mon Profil");
            stage.setMaximized(true);
            stage.show();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le profil: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupNavigationButtons() {
        // Home button
        if (btnHome != null) {
            btnHome.setOnMouseClicked(event -> navigateToHome());

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

        // Navigation buttons
        setupNavButtonHover(btnHebergements, "🏨", "Hébergements");
        if (btnHebergements != null) {
            btnHebergements.setOnMouseClicked(event -> navigateTo("/HebergementFront.fxml", "Hébergements"));
        }

        setupNavButtonHover(btnCategories, "📑", "Catégories");
        if (btnCategories != null) {
            btnCategories.setOnMouseClicked(event -> navigateTo("/categoriesfront.fxml", "Catégories"));
        }

        setupNavButtonHover(btnActivites, "🏄", "Activités");
        if (btnActivites != null) {
            btnActivites.setOnMouseClicked(event -> navigateTo("/activitesfront.fxml", "Activités"));
        }

        setupNavButtonHover(btnVoyages, "✈️", "Voyages");
        if (btnVoyages != null) {
            btnVoyages.setOnMouseClicked(event -> navigateTo("/PageVoyage.fxml", "Voyages"));
        }

        setupNavButtonHover(btnBudgets, "💰", "Budgets");
        if (btnBudgets != null) {
            btnBudgets.setOnMouseClicked(event -> navigateTo("/BudgetDepenseFront.fxml", "Budgets"));
        }

        setupNavButtonHover(btnEvenements, "🎉", "Événements");
        if (btnEvenements != null) {
            btnEvenements.setOnMouseClicked(event -> navigateTo("/Evenementsfront.fxml", "Événements"));
        }

        // Remove Itinéraires from navigation (keep it but disabled or hidden)
        if (btnItineraires != null) {
            btnItineraires.setVisible(false);
            btnItineraires.setManaged(false);
        }
    }

    private void navigateTo(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = getStage();
            if (stage == null) return;
            stage.setScene(new Scene(root, javafx.stage.Screen.getPrimary().getVisualBounds().getWidth(), javafx.stage.Screen.getPrimary().getVisualBounds().getHeight()));
            stage.setTitle("TravelMate - " + title);
            stage.setMaximized(true);
            stage.show();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir " + title + ": " + e.getMessage());
            e.printStackTrace();
        }
    }


    private Stage getStage() {
        if (btnActivites != null && btnActivites.getScene() != null)
            return (Stage) btnActivites.getScene().getWindow();
        if (btnVoyages != null && btnVoyages.getScene() != null)
            return (Stage) btnVoyages.getScene().getWindow();
        if (btnBudgets != null && btnBudgets.getScene() != null)
            return (Stage) btnBudgets.getScene().getWindow();
        if (containerDestinations != null && containerDestinations.getScene() != null)
            return (Stage) containerDestinations.getScene().getWindow();
        return null;
    }

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


    private void updateStats() {
        int total = allDestinations.size();
        lblTotalDestinations.setText(String.valueOf(total));
        lblDestinationCount.setText(total + " destination" + (total > 1 ? "s" : ""));
        lblStatut.setText("● " + total + " destination" + (total > 1 ? "s" : ""));

        long paysCount = allDestinations.stream()
                .map(Destination::getPays_destination)
                .filter(Objects::nonNull)
                .distinct()
                .count();
        lblPaysCount.setText(paysCount + " pays représentés");
        lblTotalPays.setText("Dans " + paysCount + " pays différents");

        long climatesCount = allDestinations.stream()
                .map(Destination::getClimat_destination)
                .filter(Objects::nonNull)
                .distinct()
                .count();
        lblClimatsCount.setText(String.valueOf(climatesCount));
        lblClimatDiversite.setText(climatesCount + " climats différents");

        long seasonsCount = allDestinations.stream()
                .map(Destination::getSaison_destination)
                .filter(Objects::nonNull)
                .distinct()
                .count();
        lblSaisonsCount.setText(String.valueOf(seasonsCount));
        if (seasonsCount > 0) {
            progressSaisons.setProgress(Math.min(seasonsCount / 4.0, 1.0));
        }

        lblDestinationsActives.setText(String.valueOf(total));
        double pourcentage = total > 0 ? 100.0 : 0;
        lblActivesPourcentage.setText(String.format("%.0f%% des destinations", pourcentage));
    }

    void refreshData() {
        loadDestinationsCards();
        loadUnreadNotifications();
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
                                d.getPays_destination().toLowerCase().contains(searchTerm.toLowerCase()) ||
                                (d.getRegion_destination() != null && d.getRegion_destination().toLowerCase().contains(searchTerm.toLowerCase())))
                        .collect(Collectors.toList());

                if (searchResults.isEmpty()) {
                    lblNoDestinations.setText("Aucune destination trouvée");
                    renderDestinationCards(Collections.emptyList(), false);
                    lblDestinationCount.setText("0 résultat");
                } else {
                    renderDestinationCards(searchResults, false);
                    lblDestinationCount.setText(searchResults.size() + " résultat(s)");
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
                renderDestinationCards(allDestinations, false);
                lblDestinationCount.setText(allDestinations.size() + " destination(s)");
            } else {
                List<Destination> filteredResults = allDestinations.stream()
                        .filter(d -> climate.equals(d.getClimat_destination()))
                        .collect(Collectors.toList());

                if (filteredResults.isEmpty()) {
                    lblNoDestinations.setText("Aucune destination trouvée pour ce climat");
                    renderDestinationCards(Collections.emptyList(), false);
                    lblDestinationCount.setText("0 résultat");
                } else {
                    renderDestinationCards(filteredResults, false);
                    lblDestinationCount.setText(filteredResults.size() + " résultat(s)");
                }
            }
        });
    }

    private void handleAjouter() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterDestinationFront.fxml"));
            Parent root = loader.load();
            AjouterDestinationController controller = loader.getController();
            controller.setParentController(null);

            Stage stage = new Stage();
            stage.setTitle("Ajouter une destination");
            stage.setScene(new Scene(root, javafx.stage.Screen.getPrimary().getVisualBounds().getWidth(), javafx.stage.Screen.getPrimary().getVisualBounds().getHeight()));
            stage.setResizable(false);
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.showAndWait();

            refreshData();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage());
        }
    }

    private void handleModifier(Destination destination) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierDestinationFront.fxml"));
            Parent root = loader.load();
            ModifierDestinationController controller = loader.getController();
            controller.setDestination(destination);
            controller.setParentController(null);

            Stage stage = new Stage();
            stage.setTitle("Modifier - " + destination.getNom_destination());
            stage.setScene(new Scene(root, javafx.stage.Screen.getPrimary().getVisualBounds().getWidth(), javafx.stage.Screen.getPrimary().getVisualBounds().getHeight()));
            stage.setResizable(false);
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.showAndWait();

            refreshData();

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
                loadDestinationsCards();
                updateStats();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Destination supprimée avec succès!");
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer: " + e.getMessage());
            }
        }
    }


    private void navigateToHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/HomePage.fxml"));
            Parent root = loader.load();

            Stage stage = getStage();
            if (stage == null) return;
            stage.setScene(new Scene(root, javafx.stage.Screen.getPrimary().getVisualBounds().getWidth(), javafx.stage.Screen.getPrimary().getVisualBounds().getHeight()));
            stage.setTitle("TravelMate - Accueil");
            stage.setMaximized(true);
            stage.show();

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

    @FXML
    private Button btnTranslate;

    @FXML
    private void handleTranslate() {
        Utils.TranslationManager.createTranslationButton(() ->
            Utils.TranslationManager.translateInterface(
                btnTranslate.getScene().getRoot(),
                Utils.TranslationManager.getCurrentLanguage())
        );
        Utils.TranslationManager.translateInterface(
            btnTranslate.getScene().getRoot(),
            Utils.TranslationManager.getCurrentLanguage());
    }
}