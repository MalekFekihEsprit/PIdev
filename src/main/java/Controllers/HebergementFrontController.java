package Controllers;

import Entities.Destination;
import Entities.Hebergement;
import Entities.DeleteNotification;
import Entities.User;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
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
    @FXML private HBox btnDestinations;
    @FXML private HBox btnHebergements;
    @FXML private HBox btnItineraires;
    @FXML private HBox btnActivites;
    @FXML private HBox btnVoyages;
    @FXML private HBox btnBudgets;
    @FXML private HBox btnCategories;
    @FXML private HBox btnEvenements;
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

    // Cards Section
    @FXML private Label lblHebergementCount;
    @FXML private Label lblNoHebergements;
    @FXML private VBox containerHebergements;

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
    private DeleteNotificationCRUD notificationCRUD;
    private ObservableList<Hebergement> hebergementList = FXCollections.observableArrayList();
    private List<Hebergement> allHebergements = new ArrayList<>();
    private User currentUser;
    private List<DeleteNotification> unreadNotifications = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        hebergementCRUD = new HebergementCRUD();
        notificationCRUD = new DeleteNotificationCRUD();

        currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Utilisateur non connecté");
            return;
        }

        loadHebergements();
        setupButtonActions();
        updateLastUpdateTime();
        setupNavigationButtons();
        setupUserProfile();
        updateUserInfo();
        setupNotifications();
        loadUnreadNotifications();
    }

    private VBox createHebergementCard(Hebergement hebergement) {
        VBox card = new VBox(0);
        card.setMinWidth(0);
        card.setMaxWidth(420);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 20; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-border-radius: 20; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.04), 14, 0, 0, 3);");

        // ── TOP HALF: image banner via CSS (never inflates card width) ──────
        javafx.scene.layout.Region imageBanner = new javafx.scene.layout.Region();
        imageBanner.setMinHeight(180);
        imageBanner.setPrefHeight(180);
        imageBanner.setMaxHeight(180);
        imageBanner.setPrefWidth(0);
        imageBanner.setMaxWidth(Double.MAX_VALUE);

        String imageName = hebergement.getImage_name();
        if (imageName != null && !imageName.isBlank()) {
            String imageUrl;
            if (imageName.startsWith("http://") || imageName.startsWith("https://")) {
                imageUrl = imageName;
            } else {
                java.net.URL res = getClass().getResource("/images/" + imageName);
                imageUrl = (res != null) ? res.toExternalForm() : "file:" + imageName;
            }
            imageBanner.setStyle(
                "-fx-background-image: url('" + imageUrl + "');" +
                "-fx-background-size: cover;" +
                "-fx-background-position: center center;" +
                "-fx-background-repeat: no-repeat;" +
                "-fx-background-radius: 20 20 0 0;"
            );
        } else {
            // Gradient placeholder
            String gradientColor = typeToGradient(hebergement.getType_hebergement());
            imageBanner.setStyle(
                "-fx-background-color: " + gradientColor + ";" +
                "-fx-background-radius: 20 20 0 0;"
            );
        }

        // Overlay: placeholder emoji + type badge
        StackPane imageOverlay = new StackPane();
        imageOverlay.setMinHeight(180);
        imageOverlay.setPrefHeight(180);
        imageOverlay.setMaxHeight(180);
        imageOverlay.setPrefWidth(0);
        imageOverlay.setMaxWidth(Double.MAX_VALUE);
        imageOverlay.setStyle("-fx-background-color: transparent;");

        if (imageName == null || imageName.isBlank()) {
            Label placeholder = new Label(typeToEmoji(hebergement.getType_hebergement()));
            placeholder.setStyle("-fx-font-size: 56; -fx-text-fill: white;");
            imageOverlay.getChildren().add(placeholder);
        }

        // Type badge bottom-left
        String typeText = (hebergement.getType_hebergement() == null || hebergement.getType_hebergement().isBlank())
                ? "Hébergement" : hebergement.getType_hebergement();
        Label typeBadge = new Label(typeText);
        typeBadge.setStyle("-fx-background-color: rgba(0,0,0,0.55); -fx-text-fill: white; " +
                           "-fx-background-radius: 20; -fx-padding: 3 10; -fx-font-size: 11; -fx-font-weight: 700;");
        StackPane.setAlignment(typeBadge, javafx.geometry.Pos.BOTTOM_LEFT);
        StackPane.setMargin(typeBadge, new Insets(0, 0, 10, 10));
        imageOverlay.getChildren().add(typeBadge);

        StackPane bannerStack = new StackPane(imageBanner, imageOverlay);
        bannerStack.setMinHeight(180);
        bannerStack.setPrefHeight(180);
        bannerStack.setMaxHeight(180);
        bannerStack.setPrefWidth(0);
        bannerStack.setMaxWidth(Double.MAX_VALUE);

        // ── BOTTOM HALF: card content ────────────────────────────────────────
        VBox content = new VBox(6);
        content.setPadding(new Insets(14, 16, 14, 16));
        content.setMaxWidth(Double.MAX_VALUE);

        Label name = new Label(hebergement.getNom_hebergement());
        name.setStyle("-fx-font-size: 17; -fx-font-weight: bold; -fx-text-fill: #0f172a;");
        name.setWrapText(true);

        Label destination = new Label("📍 " + formatDestination(hebergement.getDestination()));
        destination.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12;");
        destination.setWrapText(true);

        // Star rating row
        double noteVal = hebergement.getNote_hebergement() != null ? hebergement.getNote_hebergement() : 0.0;
        HBox starsRow = new HBox(3);
        starsRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        int fullStars = (int) Math.floor(noteVal);
        boolean halfStar = (noteVal - fullStars) >= 0.5;
        for (int s = 1; s <= 5; s++) {
            Label star = new Label();
            if (s <= fullStars) {
                star.setText("★"); star.setStyle("-fx-text-fill: #f59e0b; -fx-font-size: 15;");
            } else if (s == fullStars + 1 && halfStar) {
                star.setText("½"); star.setStyle("-fx-text-fill: #f59e0b; -fx-font-size: 13;");
            } else {
                star.setText("☆"); star.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 15;");
            }
            starsRow.getChildren().add(star);
        }
        Label noteLabel = new Label(String.format("  %.1f", noteVal));
        noteLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12;");
        starsRow.getChildren().add(noteLabel);

        // Price + address row
        HBox priceRow = new HBox(8);
        priceRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label price = new Label(formatPrice(hebergement.getPrix_nuit_hebergement()) + " / nuit");
        price.setStyle("-fx-text-fill: #10b981; -fx-font-size: 13; -fx-font-weight: 700;");
        priceRow.getChildren().add(price);

        Label adresse = new Label("🏠 " + safeText(hebergement.getAdresse_hebergement()));
        adresse.setStyle("-fx-text-fill: #475569; -fx-font-size: 12;");
        adresse.setWrapText(true);

        Label addedBy = new Label("Ajouté par : " + safeText(hebergement.getAdded_by_name()));
        addedBy.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11;");

        HBox actions = new HBox(8);
        actions.setPadding(new Insets(6, 0, 0, 0));

        Button btnView = new Button("👁 Voir");
        btnView.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-background-radius: 8; -fx-font-size: 12; -fx-cursor: hand; -fx-padding: 6 14;");
        btnView.setOnAction(e -> handleConsulter(hebergement));
        actions.getChildren().add(btnView);

        if (hebergement.getAdded_by() != null && currentUser != null
                && hebergement.getAdded_by().intValue() == currentUser.getId()) {
            Button btnEdit = new Button("✏");
            btnEdit.setStyle("-fx-background-color: #f59e0b; -fx-text-fill: white; -fx-background-radius: 8; -fx-font-size: 12; -fx-cursor: hand; -fx-padding: 6 14;");
            btnEdit.setOnAction(e -> handleModifier(hebergement));

            Button btnDelete = new Button("🗑");
            btnDelete.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-background-radius: 8; -fx-font-size: 12; -fx-cursor: hand; -fx-padding: 6 14;");
            btnDelete.setOnAction(e -> handleDeleteSingle(hebergement));

            actions.getChildren().addAll(btnEdit, btnDelete);
        }

        content.getChildren().addAll(name, destination, starsRow, priceRow, adresse, addedBy, actions);
        card.getChildren().addAll(bannerStack, content);
        return card;
    }

    /** Returns a gradient CSS color string based on accommodation type. */
    private String typeToGradient(String type) {
        if (type == null) return "linear-gradient(from 0% 0% to 100% 100%, #e2e8f0, #cbd5e1)";
        return switch (type.toLowerCase()) {
            case "hôtel", "hotel"   -> "linear-gradient(from 0% 0% to 100% 100%, #1e40af, #3b82f6)";
            case "villa"            -> "linear-gradient(from 0% 0% to 100% 100%, #065f46, #10b981)";
            case "appartement"      -> "linear-gradient(from 0% 0% to 100% 100%, #7c3aed, #a78bfa)";
            case "auberge"          -> "linear-gradient(from 0% 0% to 100% 100%, #92400e, #f59e0b)";
            case "resort"           -> "linear-gradient(from 0% 0% to 100% 100%, #be185d, #f472b6)";
            default                 -> "linear-gradient(from 0% 0% to 100% 100%, #374151, #6b7280)";
        };
    }

    /** Returns an emoji for the accommodation type placeholder. */
    private String typeToEmoji(String type) {
        if (type == null) return "🏠";
        return switch (type.toLowerCase()) {
            case "hôtel", "hotel"   -> "🏨";
            case "villa"            -> "🏡";
            case "appartement"      -> "🏢";
            case "auberge"          -> "🏚️";
            case "resort"           -> "🌴";
            default                 -> "🏠";
        };
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

        setupNavButtonHover(btnEvenements, "🎉", "Événements");
        if (btnEvenements != null) {
            btnEvenements.setOnMouseClicked(event -> navigateTo("/Evenementsfront.fxml", "Événements"));
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
        if (containerHebergements != null && containerHebergements.getScene() != null)
            return (Stage) containerHebergements.getScene().getWindow();
        if (btnDestinations != null && btnDestinations.getScene() != null)
            return (Stage) btnDestinations.getScene().getWindow();
        if (btnActivites != null && btnActivites.getScene() != null)
            return (Stage) btnActivites.getScene().getWindow();
        if (btnVoyages != null && btnVoyages.getScene() != null)
            return (Stage) btnVoyages.getScene().getWindow();
        return null;
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
            renderHebergementCards(hebergementList, true);

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
                .mapToDouble(Hebergement::getPrix_nuit_hebergement)
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
                    renderHebergementCards(hebergementList, false);
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
                renderHebergementCards(hebergementList, false);
                lblHebergementCount.setText(allHebergements.size() + " hébergement(s)");
            } else {
                List<Hebergement> filteredResults = allHebergements.stream()
                        .filter(h -> type.equals(h.getType_hebergement()))
                        .collect(Collectors.toList());

                if (filteredResults.isEmpty()) {
                    showAlert(Alert.AlertType.INFORMATION, "Résultat", "Aucun hébergement trouvé pour ce type");
                } else {
                    hebergementList.setAll(filteredResults);
                    renderHebergementCards(hebergementList, false);
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
            stage.setScene(new Scene(root, javafx.stage.Screen.getPrimary().getVisualBounds().getWidth(), javafx.stage.Screen.getPrimary().getVisualBounds().getHeight()));
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
            stage.setScene(new Scene(root, javafx.stage.Screen.getPrimary().getVisualBounds().getWidth(), javafx.stage.Screen.getPrimary().getVisualBounds().getHeight()));
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
                renderHebergementCards(hebergementList, true);
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
            stage.setScene(new Scene(root, javafx.stage.Screen.getPrimary().getVisualBounds().getWidth(), javafx.stage.Screen.getPrimary().getVisualBounds().getHeight()));
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.initOwner(getStage());
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

            Stage stage = getStage();
            if (stage == null) {
                return;
            }
            stage.setScene(new Scene(root, javafx.stage.Screen.getPrimary().getVisualBounds().getWidth(), javafx.stage.Screen.getPrimary().getVisualBounds().getHeight()));
            stage.setTitle("TravelMate - Accueil");
            stage.setMaximized(true);
            stage.show();

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

    public void filterByDestination(Destination destination) {
        if (destination == null) return;

        List<Hebergement> filtered = allHebergements.stream()
                .filter(h -> h.getDestination() != null &&
                        h.getDestination().getId_destination() == destination.getId_destination())
                .collect(Collectors.toList());

        hebergementList.setAll(filtered);
        renderHebergementCards(hebergementList, false);
        lblHebergementCount.setText(filtered.size() + " hébergement" + (filtered.size() > 1 ? "s" : "") +
                " à " + destination.getNom_destination());
        lblStatut.setText("● " + filtered.size() + " hébergement" + (filtered.size() > 1 ? "s" : "") +
                " • " + destination.getNom_destination());
    }

    public void clearFilter() {
        hebergementList.setAll(allHebergements);
        renderHebergementCards(hebergementList, false);
        lblHebergementCount.setText(allHebergements.size() + " hébergement" + (allHebergements.size() > 1 ? "s" : ""));
        updateStats();
    }

    private void renderHebergementCards(List<Hebergement> hebergements, boolean updateCountText) {
        if (containerHebergements == null) {
            return;
        }

        containerHebergements.getChildren().clear();

        if (hebergements == null || hebergements.isEmpty()) {
            if (lblNoHebergements != null) {
                lblNoHebergements.setVisible(true);
            }
            if (updateCountText && lblHebergementCount != null) {
                lblHebergementCount.setText("0 hébergement");
            }
            return;
        }

        if (lblNoHebergements != null) {
            lblNoHebergements.setVisible(false);
        }

        for (int i = 0; i < hebergements.size(); i += 3) {
            HBox row = new HBox(18);
            row.setMaxWidth(Double.MAX_VALUE);
            row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

            for (int j = i; j < Math.min(i + 3, hebergements.size()); j++) {
                VBox card = createHebergementCard(hebergements.get(j));
                HBox.setHgrow(card, javafx.scene.layout.Priority.ALWAYS);
                row.getChildren().add(card);
            }

            int missingSlots = 3 - row.getChildren().size();
            for (int k = 0; k < missingSlots; k++) {
                Region spacer = new Region();
                spacer.setMinWidth(0);
                HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
                row.getChildren().add(spacer);
            }

            containerHebergements.getChildren().add(row);
        }

        if (updateCountText && lblHebergementCount != null) {
            int total = hebergements.size();
            lblHebergementCount.setText(total + " hébergement" + (total > 1 ? "s" : ""));
        }
    }

    private String safeText(String value) {
        return (value == null || value.isBlank()) ? "-" : value;
    }

    private String formatPrice(Double price) {
        return (price == null || price == 0.0) ? "-" : String.format("%.2f €", price);
    }

    private String formatNote(Double note) {
        return (note == null || note == 0.0) ? "-" : String.format("%.1f ⭐", note);
    }

    private String formatDestination(Destination destination) {
        if (destination == null) {
            return "-";
        }
        String nom = safeText(destination.getNom_destination());
        String pays = safeText(destination.getPays_destination());
        if ("-".equals(nom) && "-".equals(pays)) {
            return "-";
        }
        if ("-".equals(nom)) {
            return pays;
        }
        if ("-".equals(pays)) {
            return nom;
        }
        return nom + ", " + pays;
    }
}