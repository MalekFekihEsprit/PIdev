package Controllers;

import Entities.Destination;
import Entities.Hebergement;
import Entities.DeleteNotification;
import Entities.User;
import Services.HebergementCRUD;
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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
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

public class HebergementFrontController implements Initializable {

    // ============== FXML INJECTED ELEMENTS ==============

    // Top Navigation
    @FXML private HBox btnDestinations;
    @FXML private HBox btnHebergement;
    @FXML private HBox btnItineraires;
    @FXML private HBox btnActivites;
    @FXML private HBox btnVoyages;
    @FXML private HBox btnBudgets;
    @FXML private HBox btnCategories; // Added
    @FXML private HBox btnHome;
    @FXML private HBox userProfileBox;
    @FXML private HBox btnNotifications;

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
    @FXML private TableColumn<Hebergement, String> colAddedBy;
    @FXML private TableColumn<Hebergement, Void> colActions;

    // Buttons
    @FXML private Button btnAjouter;
    @FXML private HBox btnRefresh;
    @FXML private HBox btnSearch;
    @FXML private HBox btnFilter;
    @FXML private Label lblUserName;
    @FXML private Label lblUserRole;

    // Notification badge
    @FXML private Label lblNotificationBadge;

    // ============== CLASS VARIABLES ==============

    private HebergementCRUD hebergementCRUD;
    private DestinationCRUD destinationCRUD;
    private DeleteNotificationCRUD notificationCRUD;
    private ObservableList<Hebergement> hebergementList = FXCollections.observableArrayList();
    private List<Hebergement> allHebergements = new ArrayList<>();
    private User currentUser;
    private List<DeleteNotification> unreadNotifications = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        hebergementCRUD = new HebergementCRUD();
        destinationCRUD = new DestinationCRUD();
        notificationCRUD = new DeleteNotificationCRUD();

        currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Utilisateur non connecté");
            return;
        }

        setupTableColumns();
        setupTableHeight();
        loadHebergements();
        setupButtonActions();
        updateLastUpdateTime();
        setupNavigationButtons();
        setupUserProfile();
        updateUserInfo();
        setupNotifications();
        loadUnreadNotifications();
    }

    private void setupTableColumns() {
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom_hebergement"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type_hebergement"));
        colPrix.setCellValueFactory(new PropertyValueFactory<>("prixNuit_hebergement"));
        colAdresse.setCellValueFactory(new PropertyValueFactory<>("adresse_hebergement"));
        colNote.setCellValueFactory(new PropertyValueFactory<>("note_hebergement"));
        colAddedBy.setCellValueFactory(new PropertyValueFactory<>("added_by_name"));

        colDestination.setCellValueFactory(cellData -> {
            Hebergement h = cellData.getValue();
            Destination dest = h.getDestination();
            String destDisplay = (dest != null) ?
                    dest.getNom_destination() + ", " + dest.getPays_destination() : "-";
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

        setupActionsColumn();
    }

    private void setupTableHeight() {
        tableHebergements.setFixedCellSize(35);
        tableHebergements.setPrefHeight(380);
        tableHebergements.setMaxHeight(380);
        tableHebergements.setMinHeight(380);
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

                        actionBox.setPadding(new Insets(4, 0, 4, 0));
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            Hebergement hebergement = getTableView().getItems().get(getIndex());

                            actionBox.getChildren().clear();

                            btnConsulter.setOnMouseClicked(event -> handleConsulter(hebergement));
                            actionBox.getChildren().add(btnConsulter);

                            if (hebergement.getAdded_by() == currentUser.getId()) {
                                btnModifier.setOnMouseClicked(event -> handleModifier(hebergement));
                                btnSupprimer.setOnMouseClicked(event -> handleDeleteSingle(hebergement));
                                actionBox.getChildren().addAll(btnModifier, btnSupprimer);
                            }

                            setGraphic(actionBox);
                        }
                    }
                };
            }
        });
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

    private void updateUserInfo() {
        if (currentUser != null) {
            lblUserName.setText(currentUser.getPrenom() + " " + currentUser.getNom());
            lblUserRole.setText(currentUser.getRole());
        } else {
            lblUserName.setText("Utilisateur");
            lblUserRole.setText("Non connecté");
        }
    }

    private void setupButtonActions() {
        if (btnAjouter != null) {
            btnAjouter.setOnAction(event -> handleAjouter());
        }
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

    private void setupUserProfile() {
        if (userProfileBox != null) {
            userProfileBox.setOnMouseClicked(event -> navigateToProfile());

            userProfileBox.setOnMouseEntered(event -> {
                userProfileBox.setStyle("-fx-background-color: #e2e8f0; -fx-background-radius: 25; -fx-padding: 6 16 6 6; -fx-cursor: hand;");
            });

            userProfileBox.setOnMouseExited(event -> {
                userProfileBox.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 25; -fx-padding: 6 16 6 6; -fx-cursor: hand;");
            });
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

    private void setupNavigationButtons() {
        // Home button
        if (btnHome != null) {
            btnHome.setOnMouseClicked(event -> navigateToHome());

            btnHome.setOnMouseEntered(event -> {
                btnHome.setStyle("-fx-background-color: #10b981; -fx-background-radius: 12; -fx-min-width: 40; -fx-min-height: 40; -fx-cursor: hand;");
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
        setupNavButtonHover(btnDestinations, "🌍", "Destinations");
        if (btnDestinations != null) {
            btnDestinations.setOnMouseClicked(event -> navigateTo("/DestinationFront.fxml", "Destinations"));
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

        // Remove Itinéraires from navigation
        if (btnItineraires != null) {
            btnItineraires.setVisible(false);
            btnItineraires.setManaged(false);
        }
    }

    private void navigateTo(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = (Stage) btnHebergement.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TravelMate - " + title);
            stage.setMaximized(true);

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir " + title + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupNavButtonHover(HBox button, String icon, String text) {
        if (button == null) return;

        button.setOnMouseEntered(event -> {
            button.setStyle("-fx-background-color: rgba(16,185,129,0.1); -fx-background-radius: 12; -fx-padding: 8 16; -fx-cursor: hand; -fx-border-color: #10b981; -fx-border-width: 1; -fx-border-radius: 12;");
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

        long typesCount = allHebergements.stream()
                .map(Hebergement::getType_hebergement)
                .filter(Objects::nonNull)
                .distinct()
                .count();
        lblTypesCount.setText(typesCount + " type" + (typesCount > 1 ? "s" : "") + " différents");

        long destsCount = allHebergements.stream()
                .map(Hebergement::getDestination)
                .filter(Objects::nonNull)
                .map(Destination::getId_destination)
                .distinct()
                .count();
        lblDestinationsCount.setText(destsCount + " destination" + (destsCount > 1 ? "s" : ""));
        lblTotalDestinationsLiees.setText("Dans " + destsCount + " destination" + (destsCount > 1 ? "s" : ""));

        double avgPrice = allHebergements.stream()
                .mapToDouble(Hebergement::getPrixNuit_hebergement)
                .average()
                .orElse(0.0);
        lblPrixMoyen.setText(String.format("%.2f €", avgPrice));

        double avgNote = allHebergements.stream()
                .mapToDouble(Hebergement::getNote_hebergement)
                .average()
                .orElse(0.0);
        lblNoteMoyenne.setText(String.format("Note moyenne: %.1f ⭐", avgNote));
        lblNoteMoyenneKPI.setText(String.format("%.1f", avgNote));
        progressNote.setProgress(avgNote / 5.0);
    }

    private void refreshData() {
        loadHebergements();
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
                    lblHebergementCount.setText(searchResults.size() + " résultat(s)");
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

    private void handleAjouter() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterHebergement.fxml"));
            Parent root = loader.load();
            AjouterHebergementController controller = loader.getController();
            controller.setParentController(null);

            Stage stage = new Stage();
            stage.setTitle("Ajouter un hébergement");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.showAndWait();

            refreshData();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleModifier(Hebergement hebergement) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierHebergement.fxml"));
            Parent root = loader.load();
            ModifierHebergementController controller = loader.getController();
            controller.setHebergement(hebergement);
            controller.setParentController(null);

            Stage stage = new Stage();
            stage.setTitle("Modifier - " + hebergement.getNom_hebergement());
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.showAndWait();

            refreshData();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleDeleteSingle(Hebergement hebergement) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer l'hébergement");
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer " + hebergement.getNom_hebergement() + " ?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                hebergementCRUD.supprimer(hebergement);
                allHebergements.remove(hebergement);
                hebergementList.setAll(allHebergements);
                tableHebergements.setItems(hebergementList);
                updateStats();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Hébergement supprimé avec succès!");
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer: " + e.getMessage());
            }
        }
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

    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
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
        lblHebergementCount.setText(filtered.size() + " hébergement" + (filtered.size() > 1 ? "s" : "") +
                " à " + destination.getNom_destination());
        lblStatut.setText("● " + filtered.size() + " hébergement" + (filtered.size() > 1 ? "s" : "") +
                " • " + destination.getNom_destination());
    }

    public void clearFilter() {
        hebergementList.setAll(allHebergements);
        tableHebergements.setItems(hebergementList);
        lblHebergementCount.setText(allHebergements.size() + " hébergement" + (allHebergements.size() > 1 ? "s" : ""));
        updateStats();
    }
}