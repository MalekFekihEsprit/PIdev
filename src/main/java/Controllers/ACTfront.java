package Controllers;

import Entities.Activites;
import Services.ActivitesCRUD;
import Services.ModerationService;
import Services.QRCodeService;
import Utils.DeepLinkHandler;
import Utils.TranslationManager;
import Utils.UserSession;
import Entities.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class ACTfront implements Initializable {

    // ─── FXML Injections ───────────────────────────────────────────────
    @FXML private GridPane activitesGrid;
    @FXML private Label lblTotalActivites;
    @FXML private Button btnBackOffice;
    @FXML private Button btnModifier;
    @FXML private Button btnSupprimer;
    @FXML private Button btnFavoris;
    @FXML private HBox btnVersCategories;
    @FXML private ComboBox<String> filterTypeCombo;
    @FXML private ComboBox<String> filterSaisonCombo;
    @FXML private ComboBox<String> filterDifficulteCombo;
    @FXML private PieChart pieChart;
    @FXML private Label lblDateMoyenne;
    @FXML private Label lblNomMoyenne;
    @FXML private Label lblTopLieu;
    @FXML private Label lblTopBudget;
    @FXML private Label lblTopNom;
    @FXML private ProgressBar progressTop;
    @FXML private Label lblSelectedDate;
    @FXML private Label lblSelectedBudget;
    @FXML private Label lblSelectedNom;
    @FXML private Circle dotColor;
    @FXML private Label lblDate;
    @FXML private TextField searchField;
    @FXML private Button btnTranslate;
    @FXML private Label titleLabel;
    @FXML private Button btnAllActivities;
    @FXML private Button btnSimulateQR;

    // Navigation buttons (navbar)
    @FXML private HBox btnDestinations;
    @FXML private HBox btnHebergements;
    @FXML private HBox btnItineraires;
    @FXML private HBox btnVoyages;
    @FXML private HBox btnBudgets;
    @FXML private HBox userProfileBox;
    @FXML private Label lblUserName;
    @FXML private Label lblUserRole;

    // Scroll navigation
    @FXML private ScrollPane navScrollPane;
    @FXML private HBox navLinksContainer;

    // ─── State ─────────────────────────────────────────────────────────
    private ActivitesCRUD activitesCRUD;
    private ObservableList<Activites> activitesList;
    private FilteredList<Activites> filteredData;
    private Activites selectedActivite = null;
    private String currentCategorieFiltre = null;

    // Cache pour les résultats de modération
    private Map<Integer, ModerationService.ModerationResult> moderationCache = new HashMap<>();

    // ─── Couleurs pour le PieChart ──────────────────────────────────────
    private static final String[] PIE_COLORS = {
            "#ff6b00", "#f5a623", "#ef4444", "#34d399", "#60a5fa", "#a78bfa", "#fbbf24", "#f87171"
    };

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        activitesCRUD = new ActivitesCRUD();
        activitesList = FXCollections.observableArrayList();

        // Date du jour dans la navbar
        if (lblDate != null) {
            lblDate.setText("📅 " + LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.FRENCH)));
        }

        setupFilters();
        setupSearch();
        setupNavigationButtons();
        updateUserInfo();

        // Vérifier si on vient d'une catégorie
        currentCategorieFiltre = CategorieContext.categorieFiltre;
        if (currentCategorieFiltre != null && !currentCategorieFiltre.isEmpty()) {
            System.out.println("🔍 Filtrage par catégorie: " + currentCategorieFiltre);
            if (titleLabel != null) {
                titleLabel.setText("Activités de la catégorie : " + currentCategorieFiltre);
            }
            if (searchField != null) {
                searchField.setPromptText("Rechercher dans " + currentCategorieFiltre + "...");
            }
            if (btnAllActivities != null) {
                btnAllActivities.setVisible(true);
                btnAllActivities.setManaged(true);
            }
        } else {
            if (btnAllActivities != null) {
                btnAllActivities.setVisible(false);
                btnAllActivities.setManaged(false);
            }
        }

        loadActivitesWithFilter();
    }

    private void setupNavigationButtons() {
        // Destinations button
        if (btnDestinations != null) {
            btnDestinations.setOnMouseClicked(event -> navigateToDestinations());
        }

        // Hébergements button
        if (btnHebergements != null) {
            btnHebergements.setOnMouseClicked(event -> navigateToHebergements());
        }

        // Itinéraires button (pas encore implémenté)
        if (btnItineraires != null) {
            btnItineraires.setOnMouseClicked(event -> showNotImplementedAlert("Itinéraires"));
        }

        // Voyages button - Navigue vers PageVoyage.fxml
        if (btnVoyages != null) {
            btnVoyages.setOnMouseClicked(event -> navigateToVoyages());
        }

        // Budgets button (pas encore implémenté)
        if (btnBudgets != null) {
            btnBudgets.setOnMouseClicked(event -> showNotImplementedAlert("Budgets"));
        }

        // User profile
        if (userProfileBox != null) {
            userProfileBox.setOnMouseClicked(event -> navigateToProfile());
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
            showError("Erreur de navigation", "Impossible de charger les destinations: " + e.getMessage());
        }
    }

    private void navigateToHebergements() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/HebergementFront.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) btnHebergements.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("TravelMate - Hébergements");
            stage.show();
        } catch (IOException e) {
            showError("Erreur de navigation", "Impossible de charger les hébergements: " + e.getMessage());
        }
    }

    /**
     * Navigue vers la page PageVoyage.fxml
     */
    private void navigateToVoyages() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/PageVoyage.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) btnVoyages.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("TravelMate - Gestion des Voyages");
            stage.show();
        } catch (IOException e) {
            showError("Erreur de navigation", "Impossible de charger la page des voyages: " + e.getMessage());
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
            showError("Erreur", "Impossible d'ouvrir le profil: " + e.getMessage());
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

    private void setupSearch() {
        if (searchField != null) {
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                applyFilters();
            });
            searchField.setPromptText("Rechercher une activité...");
        }
    }

    private void setupFilters() {
        filterTypeCombo.getItems().addAll("Tous les types", "Aventure", "Détente", "Culturel", "Sportif", "Gastronomique", "Famille", "Nature");
        filterTypeCombo.setValue("Tous les types");
        filterTypeCombo.setOnAction(e -> applyFilters());

        filterSaisonCombo.getItems().addAll("Toutes saisons", "Printemps", "Été", "Automne", "Hiver");
        filterSaisonCombo.setValue("Toutes saisons");
        filterSaisonCombo.setOnAction(e -> applyFilters());

        filterDifficulteCombo.getItems().addAll("Tous niveaux", "Facile", "Moyen", "Difficile", "Expert");
        filterDifficulteCombo.setValue("Tous niveaux");
        filterDifficulteCombo.setOnAction(e -> applyFilters());
    }

    private void loadActivitesWithFilter() {
        try {
            List<Activites> liste;
            if (currentCategorieFiltre != null && !currentCategorieFiltre.isEmpty()) {
                liste = activitesCRUD.afficherParCategorie(currentCategorieFiltre);
                System.out.println("📊 " + liste.size() + " activités trouvées pour la catégorie: " + currentCategorieFiltre);
            } else {
                liste = activitesCRUD.afficher();
            }

            activitesList.clear();
            activitesList.addAll(liste);
            filteredData = new FilteredList<>(activitesList, p -> true);

            moderationCache.clear();
            for (Activites activite : activitesList) {
                ModerationService.ModerationResult result = ModerationService.analyserActivite(
                        activite.getNom(),
                        activite.getDescription(),
                        activite.getImagePath()
                );
                moderationCache.put(activite.getId(), result);
            }

            applyFilters();

        } catch (SQLException e) {
            showError("Erreur de chargement", "Impossible de charger les activités: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void applyFilters() {
        if (filteredData == null) return;

        String selectedType = filterTypeCombo.getValue();
        String selectedSaison = filterSaisonCombo.getValue();
        String selectedDifficulte = filterDifficulteCombo.getValue();
        String searchText = searchField != null ? searchField.getText().toLowerCase() : "";

        filteredData.setPredicate(activite -> {
            boolean matchType = selectedType.equals("Tous les types") ||
                    (activite.getCategorie() != null && activite.getCategorie().getType() != null
                            && activite.getCategorie().getType().equals(selectedType));

            boolean matchSaison = selectedSaison.equals("Toutes saisons") ||
                    (activite.getCategorie() != null && activite.getCategorie().getSaison() != null
                            && activite.getCategorie().getSaison().equals(selectedSaison));

            boolean matchDifficulte = selectedDifficulte.equals("Tous niveaux") ||
                    (activite.getNiveaudifficulte() != null
                            && activite.getNiveaudifficulte().equalsIgnoreCase(selectedDifficulte));

            boolean matchSearch = searchText.isEmpty() ||
                    (activite.getNom() != null && activite.getNom().toLowerCase().contains(searchText)) ||
                    (activite.getDescription() != null && activite.getDescription().toLowerCase().contains(searchText)) ||
                    (activite.getLieu() != null && activite.getLieu().toLowerCase().contains(searchText)) ||
                    (activite.getCategorie() != null && activite.getCategorie().getNom() != null
                            && activite.getCategorie().getNom().toLowerCase().contains(searchText));

            return matchType && matchSaison && matchDifficulte && matchSearch;
        });

        displayActivites(filteredData);
        updateStats(filteredData);
        updatePieChart(filteredData);
    }

    private void displayActivites(ObservableList<Activites> activites) {
        activitesGrid.getChildren().clear();
        activitesGrid.getColumnConstraints().clear();

        for (int i = 0; i < 4; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setHgrow(Priority.ALWAYS);
            cc.setPercentWidth(25);
            activitesGrid.getColumnConstraints().add(cc);
        }

        int column = 0;
        int row = 0;

        for (Activites activite : activites) {
            VBox card;

            ModerationService.ModerationResult modResult = moderationCache.get(activite.getId());
            if (modResult == null) {
                modResult = ModerationService.analyserActivite(
                        activite.getNom(),
                        activite.getDescription(),
                        activite.getImagePath()
                );
                moderationCache.put(activite.getId(), modResult);
            }

            if (modResult != null && modResult.estSuspect) {
                card = createModeratedCard(activite, modResult);
            } else {
                card = createImageCard(activite);
            }

            activitesGrid.add(card, column, row);
            column++;
            if (column >= 4) {
                column = 0;
                row++;
            }
        }

        lblTotalActivites.setText(String.valueOf(activites.size()));
    }

    private VBox createImageCard(Activites activite) {
        VBox card = new VBox(0);
        card.getStyleClass().add("activity-card");
        card.setPrefWidth(200);
        card.setMinWidth(160);

        VBox cardContent = createBaseCardContent(activite, true);
        card.getChildren().add(cardContent);

        card.setOnMouseClicked(event -> {
            selectedActivite = activite;
            updateSelectedDetail(activite);
        });

        card.setOnMouseEntered(event -> {
            card.setEffect(new DropShadow(16, Color.web("#f5a62355")));
            card.setScaleX(1.02);
            card.setScaleY(1.02);
        });
        card.setOnMouseExited(event -> {
            card.setEffect(new DropShadow(10, Color.web("#00000019")));
            card.setScaleX(1.0);
            card.setScaleY(1.0);
        });

        return card;
    }

    private VBox createModeratedCard(Activites activite, ModerationService.ModerationResult modResult) {
        VBox card = new VBox(0);
        card.getStyleClass().add("activity-card");
        card.setPrefWidth(200);
        card.setMinWidth(160);

        StackPane cardContainer = new StackPane();
        VBox cardContent = createBaseCardContent(activite, true);

        GaussianBlur blurEffect = new GaussianBlur();
        blurEffect.setRadius(8);
        cardContent.setEffect(blurEffect);
        cardContent.setOpacity(0.7);

        VBox warningOverlay = createWarningOverlay(activite, modResult, card, cardContent);
        cardContainer.getChildren().addAll(cardContent, warningOverlay);
        card.getChildren().add(cardContainer);

        return card;
    }

    private VBox createBaseCardContent(Activites activite, boolean includeQR) {
        VBox content = new VBox(0);

        StackPane imageContainer = new StackPane();
        imageContainer.setPrefHeight(140);
        imageContainer.setMinHeight(140);

        ImageView imageView = tryLoadImage(activite);

        if (imageView != null) {
            imageView.setFitWidth(200);
            imageView.setFitHeight(140);
            imageView.setPreserveRatio(false);
            imageView.setSmooth(true);
            imageContainer.getChildren().add(imageView);
        } else {
            Pane placeholder = new Pane();
            placeholder.setPrefSize(200, 140);
            placeholder.setStyle("-fx-background-color: " + getCardGradient(activite) + "; -fx-background-radius: 10 10 0 0;");

            Label icon = new Label(getCategoryIcon(activite));
            icon.setStyle("-fx-font-size: 40;");
            StackPane.setAlignment(icon, Pos.CENTER);

            imageContainer.getChildren().addAll(placeholder, icon);
        }

        Label badgeDiff = new Label(activite.getNiveaudifficulte() != null ? activite.getNiveaudifficulte() : "");
        badgeDiff.setStyle("-fx-background-color: " + getDifficultyBgColor(activite.getNiveaudifficulte()) +
                "; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 2 8; -fx-font-size: 10; -fx-font-weight: bold;");
        StackPane.setAlignment(badgeDiff, Pos.TOP_LEFT);
        StackPane.setMargin(badgeDiff, new Insets(8, 0, 0, 8));
        imageContainer.getChildren().add(badgeDiff);

        if (includeQR) {
            try {
                ImageView qrCode = QRCodeService.generateQRCode(activite.getId(), activite.getNom());
                if (qrCode != null) {
                    qrCode.setFitWidth(40);
                    qrCode.setFitHeight(40);
                    qrCode.getStyleClass().add("qr-image-card");

                    Tooltip tooltip = new Tooltip("📱 Scanner pour voir sur mobile");
                    tooltip.setStyle("-fx-background-color: #ff6b00; -fx-text-fill: white; -fx-font-size: 10;");
                    Tooltip.install(qrCode, tooltip);

                    qrCode.setOnMouseClicked(event -> {
                        event.consume();
                        openActivityDetail(activite);
                    });

                    StackPane.setAlignment(qrCode, Pos.TOP_RIGHT);
                    StackPane.setMargin(qrCode, new Insets(8, 8, 0, 0));
                    imageContainer.getChildren().add(qrCode);
                }
            } catch (Exception ex) {
                // Silencieux
            }
        }

        VBox infoBox = new VBox(4);
        infoBox.setStyle("-fx-padding: 10 12 12 12; -fx-background-color: white;" +
                "-fx-background-radius: 0 0 14 14;");

        Label nameLabel = new Label(activite.getNom() != null ? activite.getNom() : "");
        nameLabel.getStyleClass().add("card-title");
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(180);

        HBox lieuBox = new HBox(4);
        lieuBox.setAlignment(Pos.CENTER_LEFT);
        Label lieuIcon = new Label("📍");
        lieuIcon.setStyle("-fx-font-size: 11;");
        Label lieuLabel = new Label(activite.getLieu() != null ? activite.getLieu() : "");
        lieuLabel.getStyleClass().add("card-location");
        lieuLabel.setWrapText(true);
        lieuBox.getChildren().addAll(lieuIcon, lieuLabel);

        HBox bottomRow = new HBox(8);
        bottomRow.setAlignment(Pos.CENTER_LEFT);
        Label budgetLabel = new Label("💰 " + activite.getBudget() + " €");
        budgetLabel.getStyleClass().add("card-budget");

        Label statutLabel = new Label(activite.getStatut() != null ? activite.getStatut() : "");
        if ("active".equalsIgnoreCase(activite.getStatut())) {
            statutLabel.getStyleClass().add("card-statut-active");
        } else {
            statutLabel.getStyleClass().add("card-statut-inactive");
        }
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        bottomRow.getChildren().addAll(budgetLabel, spacer, statutLabel);

        infoBox.getChildren().addAll(nameLabel, lieuBox, bottomRow);
        content.getChildren().addAll(imageContainer, infoBox);

        return content;
    }

    private VBox createWarningOverlay(Activites activite, ModerationService.ModerationResult modResult,
                                      VBox card, VBox cardContent) {
        VBox overlay = new VBox(10);
        overlay.setAlignment(Pos.CENTER);
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.8); -fx-background-radius: 14; -fx-padding: 15;");
        overlay.setPrefSize(200, 300);

        Label warningIcon = new Label("⚠️");
        warningIcon.setStyle("-fx-font-size: 36;");

        Label warningTitle = new Label("Contenu suspect");
        warningTitle.setStyle("-fx-text-fill: #ff6b00; -fx-font-weight: bold; -fx-font-size: 14;");

        Label warningMessage = new Label(modResult.raison);
        warningMessage.setStyle("-fx-text-fill: white; -fx-font-size: 11; -fx-wrap-text: true;");
        warningMessage.setAlignment(Pos.CENTER);
        warningMessage.setMaxWidth(180);
        warningMessage.setMinHeight(60);

        Label levelLabel = new Label("Niveau: " + modResult.niveauAlerte);
        String levelColor = modResult.niveauAlerte.equals("HIGH") ? "#ef4444" :
                (modResult.niveauAlerte.equals("MEDIUM") ? "#ff8c42" : "#f5a623");
        levelLabel.setStyle("-fx-text-fill: " + levelColor + "; -fx-font-weight: bold; -fx-font-size: 11;");

        Button revealButton = new Button("👁️ Afficher quand même");
        revealButton.setStyle("-fx-background-color: #ff6b00; -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-background-radius: 20; -fx-padding: 8 12; -fx-cursor: hand; -fx-font-size: 10; -fx-max-width: Infinity;");
        revealButton.setMaxWidth(Double.MAX_VALUE);
        revealButton.setOnAction(e -> {
            StackPane cardContainer = (StackPane) overlay.getParent();
            cardContainer.getChildren().remove(overlay);
            cardContent.setEffect(null);
            cardContent.setOpacity(1.0);
            card.setOnMouseClicked(event -> {
                selectedActivite = activite;
                updateSelectedDetail(activite);
            });
        });

        Button hideButton = new Button("✖️ Masquer cette activité");
        hideButton.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-background-radius: 20; -fx-padding: 8 12; -fx-cursor: hand; -fx-font-size: 10; -fx-max-width: Infinity;");
        hideButton.setMaxWidth(Double.MAX_VALUE);
        hideButton.setOnAction(e -> {
            GridPane grid = (GridPane) card.getParent();
            grid.getChildren().remove(card);
        });

        overlay.getChildren().addAll(warningIcon, warningTitle, warningMessage, levelLabel, revealButton, hideButton);
        return overlay;
    }

    private void openActivityDetail(Activites activite) {
        try {
            selectedActivite = activite;
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/activitydetail.fxml"));
            Parent root = loader.load();
            ActivityDetailController controller = loader.getController();
            controller.setActivite(activite);
            Scene scene = new Scene(root);
            Stage stage = (Stage) activitesGrid.getScene().getWindow();
            DeepLinkHandler.getInstance().setPrimaryStage(stage);
            stage.setScene(scene);
            stage.setTitle("TravelMate - Détail de l'activité");
            stage.show();
        } catch (IOException e) {
            showError("Erreur de navigation", "Impossible d'ouvrir les détails de l'activité: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateSelectedDetail(Activites activite) {
        if (activite != null) {
            lblSelectedNom.setText(activite.getNom());
            lblSelectedBudget.setText(activite.getBudget() + " €");
            lblSelectedDate.setText("ID: " + activite.getId() + " · " +
                    (activite.getCategorie() != null ? activite.getCategorie().getNom() : "Sans catégorie"));
        }
    }

    private ImageView tryLoadImage(Activites activite) {
        if (activite.getImagePath() != null && !activite.getImagePath().isEmpty()) {
            try {
                File f = new File(activite.getImagePath());
                if (f.exists()) {
                    return new ImageView(new Image(f.toURI().toString(), 200, 140, false, true));
                }
            } catch (Exception ignored) {}
        }

        if (activite.getNom() != null) {
            String[] exts = {".jpg", ".jpeg", ".png", ".webp"};
            String baseName = activite.getNom().toLowerCase().replaceAll("\\s+", "_").replaceAll("[^a-z0-9_]", "");
            for (String ext : exts) {
                URL url = getClass().getResource("/images/" + baseName + ext);
                if (url != null) {
                    try {
                        return new ImageView(new Image(url.toExternalForm(), 200, 140, false, true));
                    } catch (Exception ignored) {}
                }
            }
            for (String ext : exts) {
                URL url = getClass().getResource("/images/activite_" + activite.getId() + ext);
                if (url != null) {
                    try {
                        return new ImageView(new Image(url.toExternalForm(), 200, 140, false, true));
                    } catch (Exception ignored) {}
                }
            }
        }
        return null;
    }

    private void updateStats(ObservableList<Activites> list) {
        lblTotalActivites.setText(String.valueOf(list.size()));

        if (list.isEmpty()) {
            lblNomMoyenne.setText("Aucune activité");
            lblDateMoyenne.setText("—");
            lblTopLieu.setText("—");
            lblTopBudget.setText("—");
            lblTopNom.setText("—");
            if (progressTop != null) progressTop.setProgress(0);
            return;
        }

        double avgBudget = list.stream().mapToInt(Activites::getBudget).average().orElse(0);
        lblDateMoyenne.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        lblNomMoyenne.setText(String.format("Budget moyen : %.0f €", avgBudget));

        Activites top = list.stream()
                .max(Comparator.comparingInt(Activites::getBudget))
                .orElse(list.get(0));

        lblTopLieu.setText(top.getLieu() != null ? top.getLieu() : "—");
        lblTopBudget.setText(top.getBudget() + " €");
        lblTopNom.setText(top.getNom() != null ? top.getNom() : "—");

        int maxBudget = list.stream().mapToInt(Activites::getBudget).max().orElse(1);
        if (progressTop != null && maxBudget > 0) {
            progressTop.setProgress((double) top.getBudget() / maxBudget);
        }
    }

    private void updatePieChart(ObservableList<Activites> list) {
        pieChart.getData().clear();

        if (list.isEmpty()) {
            return;
        }

        Map<String, Long> countByType = list.stream()
                .collect(Collectors.groupingBy(a -> {
                    if (a.getCategorie() != null && a.getCategorie().getType() != null) {
                        return a.getCategorie().getType();
                    }
                    return "Autres";
                }, Collectors.counting()));

        long total = list.size();
        int colorIdx = 0;

        for (Map.Entry<String, Long> entry : countByType.entrySet()) {
            double percentage = (entry.getValue() * 100.0) / total;
            // Format du label avec le nom et le pourcentage
            String label = String.format("%s (%.1f%%)", entry.getKey(), percentage);

            PieChart.Data slice = new PieChart.Data(label, entry.getValue());
            pieChart.getData().add(slice);

            String color = PIE_COLORS[colorIdx % PIE_COLORS.length];
            final String col = color;

            slice.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    newNode.setStyle("-fx-pie-color: " + col + ";");
                }
            });
            colorIdx++;
        }
    }

    @FXML
    private void handleTranslate() {
        Button translateBtn = TranslationManager.createTranslationButton(() -> {
            TranslationManager.translateInterface(btnTranslate.getScene().getRoot(),
                    TranslationManager.getCurrentLanguage());
        });

        if (btnTranslate.getParent() instanceof HBox) {
            HBox parent = (HBox) btnTranslate.getParent();
            int index = parent.getChildren().indexOf(btnTranslate);
            parent.getChildren().set(index, translateBtn);
            btnTranslate = translateBtn;
        }
    }

    @FXML
    private void handleBackOffice() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/activitesback.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) btnBackOffice.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("TravelMate - Back Office Activités");
            stage.show();
        } catch (IOException e) {
            showError("Erreur de navigation", "Impossible de charger le back office: " + e.getMessage());
        }
    }

    @FXML
    private void handleVersCategories() {
        CategorieContext.categorieFiltre = null;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/categoriesfront.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) btnVersCategories.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("TravelMate - Front Office Catégories");
            stage.show();
        } catch (IOException e) {
            showError("Erreur de navigation", "Impossible de charger les catégories: " + e.getMessage());
        }
    }

    @FXML
    private void handleResetFilters() {
        filterTypeCombo.setValue("Tous les types");
        filterSaisonCombo.setValue("Toutes saisons");
        filterDifficulteCombo.setValue("Tous niveaux");
        if (searchField != null) {
            searchField.clear();
        }
        applyFilters();
    }

    @FXML
    private void handleShowAllActivities() {
        CategorieContext.categorieFiltre = null;
        currentCategorieFiltre = null;

        if (titleLabel != null) {
            titleLabel.setText("Explorez les Activités");
        }
        if (searchField != null) {
            searchField.setPromptText("Rechercher une activité...");
        }
        if (btnAllActivities != null) {
            btnAllActivities.setVisible(false);
            btnAllActivities.setManaged(false);
        }
        loadActivitesWithFilter();
    }

    @FXML
    private void handleSimulateQR() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Simulateur QR Code");
        dialog.setHeaderText("📱 Scanner un QR Code");
        dialog.setContentText("Entrez l'ID de l'activité:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(input -> {
            try {
                int activityId = Integer.parseInt(input);
                Optional<Activites> activite = activitesList.stream()
                        .filter(a -> a.getId() == activityId)
                        .findFirst();

                if (activite.isPresent()) {
                    openActivityDetail(activite.get());
                } else {
                    try {
                        Activites a = activitesCRUD.getOne(activityId);
                        if (a != null) {
                            openActivityDetail(a);
                        } else {
                            showError("Activité non trouvée", "Aucune activité avec l'ID: " + activityId);
                        }
                    } catch (SQLException e) {
                        showError("Erreur", "Impossible de charger l'activité: " + e.getMessage());
                    }
                }
            } catch (NumberFormatException e) {
                showError("Erreur", "Veuillez entrer un ID valide");
            }
        });
    }

    private String getCardGradient(Activites activite) {
        if (activite.getCategorie() == null) return "linear-gradient(to bottom right, #f5a623, #ff6b00)";
        String type = activite.getCategorie().getType();
        if (type == null) return "linear-gradient(to bottom right, #f5a623, #ff6b00)";
        switch (type.toLowerCase()) {
            case "aventure":    return "linear-gradient(to bottom right, #ef4444, #dc2626)";
            case "détente":     return "linear-gradient(to bottom right, #34d399, #059669)";
            case "culturel":    return "linear-gradient(to bottom right, #60a5fa, #2563eb)";
            case "sportif":     return "linear-gradient(to bottom right, #f97316, #ea580c)";
            case "gastronomique": return "linear-gradient(to bottom right, #fbbf24, #d97706)";
            case "famille":     return "linear-gradient(to bottom right, #a78bfa, #7c3aed)";
            case "nature":      return "linear-gradient(to bottom right, #86efac, #16a34a)";
            default:            return "linear-gradient(to bottom right, #f5a623, #ff6b00)";
        }
    }

    private String getCategoryIcon(Activites activite) {
        if (activite.getCategorie() == null) return "🎯";
        String type = activite.getCategorie().getType();
        if (type == null) return "🎯";
        switch (type.toLowerCase()) {
            case "aventure":    return "🏔️";
            case "détente":     return "🧘";
            case "culturel":    return "🏛️";
            case "sportif":     return "⚽";
            case "gastronomique": return "🍽️";
            case "famille":     return "👨‍👩‍👧‍👦";
            case "nature":      return "🌿";
            default:            return "🎯";
        }
    }

    private String getDifficultyBgColor(String difficulte) {
        if (difficulte == null) return "#888888";
        switch (difficulte.toLowerCase()) {
            case "facile":  return "#34d399";
            case "moyen":   return "#f5a623";
            case "difficile": return "#ef4444";
            case "expert":  return "#7c3aed";
            default:        return "#888888";
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}