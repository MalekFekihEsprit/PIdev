package Controllers;

import Entities.Evenement;
import Services.Evenementcrud;
import Utils.UserSession;
import Utils.FileManager;
import Entities.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class EVENTfront implements Initializable {

    // ===== Navigation navbar =====
    @FXML private HBox btnDestinations;
    @FXML private HBox btnHebergements;
    @FXML private HBox btnCategories;
    @FXML private HBox btnActivites;
    @FXML private HBox btnEvenements;
    @FXML private HBox btnVoyages;
    @FXML private HBox btnBudgets;
    @FXML private HBox btnHome;
    @FXML private HBox btnNotifications;
    @FXML private HBox userProfileBox;

    // ===== User info =====
    @FXML private Label lblUserName;
    @FXML private Label lblUserRole;
    @FXML private Label lblDate;

    // ===== Stats labels =====
    @FXML private Label lblTotalEvenements;
    @FXML private Label lblDateProchain;
    @FXML private Label lblNomProchain;
    @FXML private Label lblTopLieu;
    @FXML private Label lblTopPlaces;
    @FXML private Label lblTopNom;
    @FXML private Label titleLabel;

    // ===== Selected event detail =====
    @FXML private Label lblSelectedNom;
    @FXML private Label lblSelectedDate;
    @FXML private Label lblSelectedLieu;
    @FXML private Label lblSelectedPlaces;

    // ===== Filters =====
    @FXML private ComboBox<String> filterLieuCombo;
    @FXML private ComboBox<String> filterMoisCombo;

    // ===== Grid & chart =====
    @FXML private GridPane evenementsGrid;
    @FXML private PieChart pieChart;
    @FXML private ProgressBar progressTop;
    @FXML private ScrollPane gridScrollPane;
    @FXML private ScrollPane rightScrollPane;

    // ===== Back Office button =====
    @FXML private Button btnBackOffice;
    @FXML private Button btnAllEvenements;

    private Evenementcrud evenementCRUD;
    private ObservableList<Evenement> allEvenements;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        evenementCRUD = new Evenementcrud();
        allEvenements = FXCollections.observableArrayList();

        setupNavigationButtons();
        setupUserProfile();
        updateUserInfo();
        updateDateLabel();
        loadEvenements();
        setupFilters();

        if (gridScrollPane != null) {
            gridScrollPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    bindScrollHeights();
                }
            });
            // Recalculer les 3 colonnes si la fenêtre est redimensionnée
            gridScrollPane.widthProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal.doubleValue() > 0 && !allEvenements.isEmpty()) {
                    displayEvenements(allEvenements);
                }
            });
        }
    }

    private void bindScrollHeights() {
        if (gridScrollPane != null) {
            gridScrollPane.setMinHeight(200);
        }
    }

    // ==================== DATA ====================

    private void loadEvenements() {
        try {
            List<Evenement> liste = evenementCRUD.afficher();
            allEvenements.setAll(liste);
            updateStats();
            updatePieChart();
            displayEvenements(allEvenements);
        } catch (SQLException e) {
            showError("Erreur", "Impossible de charger les événements : " + e.getMessage());
        }
    }

    private void updateStats() {
        int total = allEvenements.size();
        set(lblTotalEvenements, String.valueOf(total));

        allEvenements.stream()
                .filter(e -> e.getDate() != null)
                .min((a, b) -> a.getDate().compareTo(b.getDate()))
                .ifPresentOrElse(e -> {
                    set(lblNomProchain, e.getTitre());
                    set(lblDateProchain, e.getDate().toString());
                }, () -> {
                    set(lblNomProchain, "—");
                    set(lblDateProchain, "—");
                });

        allEvenements.stream()
                .filter(e -> e.getLieu() != null)
                .max((a, b) -> Integer.compare(a.getNbPlaces(), b.getNbPlaces()))
                .ifPresentOrElse(e -> {
                    set(lblTopLieu, e.getLieu());
                    set(lblTopNom, e.getTitre());
                    set(lblTopPlaces, String.valueOf(e.getNbPlaces()));
                    if (progressTop != null && total > 0)
                        progressTop.setProgress((double) e.getNbPlaces() / total);
                }, () -> {
                    set(lblTopLieu, "—");
                    set(lblTopNom, "—");
                    set(lblTopPlaces, "—");
                });
    }

    private void updatePieChart() {
        if (pieChart == null) return;
        Map<String, Long> parLieu = allEvenements.stream()
                .filter(e -> e.getLieu() != null)
                .collect(Collectors.groupingBy(Evenement::getLieu, Collectors.counting()));

        ObservableList<PieChart.Data> data = FXCollections.observableArrayList();
        parLieu.forEach((lieu, count) -> data.add(new PieChart.Data(lieu + " (" + count + ")", count)));
        if (data.isEmpty()) data.add(new PieChart.Data("Aucun événement", 1));
        pieChart.setData(data);
    }

    private void displayEvenements(List<Evenement> liste) {
        if (evenementsGrid == null) return;
        evenementsGrid.getChildren().clear();
        evenementsGrid.getColumnConstraints().clear();

        // 3 colonnes avec largeur forcée dynamiquement (% ne fonctionne pas dans ScrollPane)
        for (int i = 0; i < 3; i++) {
            javafx.scene.layout.ColumnConstraints cc = new javafx.scene.layout.ColumnConstraints();
            cc.setHgrow(Priority.ALWAYS);
            cc.setFillWidth(true);
            evenementsGrid.getColumnConstraints().add(cc);
        }

        // Calcule et applique la largeur de chaque colonne = 1/3 du ScrollPane
        Runnable applyWidths = () -> {
            if (gridScrollPane == null) return;
            double scrollWidth = gridScrollPane.getWidth();
            if (scrollWidth <= 0) return;
            double cardWidth = (scrollWidth - evenementsGrid.getHgap() * 2 - 16) / 3.0;
            for (javafx.scene.layout.ColumnConstraints cc : evenementsGrid.getColumnConstraints()) {
                cc.setMinWidth(cardWidth);
                cc.setPrefWidth(cardWidth);
                cc.setMaxWidth(cardWidth);
            }
        };

        if (gridScrollPane.getWidth() > 0) {
            applyWidths.run();
        } else {
            gridScrollPane.widthProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal.doubleValue() > 0) applyWidths.run();
            });
        }

        int col = 0, row = 0;
        for (Evenement e : liste) {
            VBox card = buildCard(e);
            card.setMaxWidth(Double.MAX_VALUE);
            GridPane.setHgrow(card, Priority.ALWAYS);
            GridPane.setFillWidth(card, true);
            evenementsGrid.add(card, col, row);
            col++;
            if (col >= 3) { col = 0; row++; }
        }
    }

    private VBox buildCard(Evenement e) {
        VBox card = new VBox(0);
        card.setMaxWidth(Double.MAX_VALUE);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 14; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 8, 0, 0, 2); -fx-cursor: hand;");

        // ── Image banner ──────────────────────────────────────────────
        StackPane imageContainer = new StackPane();
        imageContainer.setPrefHeight(150);
        imageContainer.setMinHeight(150);
        imageContainer.setMaxWidth(Double.MAX_VALUE);
        imageContainer.setStyle("-fx-background-radius: 14 14 0 0;");

        ImageView imageView = loadEventImage(e);
        if (imageView != null) {
            // ✅ CORRECTION : lier la largeur de l'ImageView au StackPane parent
            // au lieu de setFitWidth(Double.MAX_VALUE) qui rendait l'image invisible
            imageView.fitWidthProperty().bind(imageContainer.widthProperty());
            imageView.setFitHeight(150);
            imageView.setPreserveRatio(false);
            imageView.setSmooth(true);
            imageView.setStyle("-fx-background-radius: 14 14 0 0;");
            imageContainer.getChildren().add(imageView);
        } else {
            // Fallback gradient placeholder
            javafx.scene.layout.Pane placeholder = new javafx.scene.layout.Pane();
            placeholder.setPrefSize(Double.MAX_VALUE, 150);
            placeholder.setStyle("-fx-background-color: linear-gradient(to bottom right, #ff8c42, #ff6b00); " +
                    "-fx-background-radius: 14 14 0 0;");
            Label icon = new Label("🎉");
            icon.setStyle("-fx-font-size: 48;");
            StackPane.setAlignment(icon, Pos.CENTER);
            imageContainer.getChildren().addAll(placeholder, icon);
        }

        // ── Text info ─────────────────────────────────────────────────
        VBox infoBox = new VBox(6);
        infoBox.setStyle("-fx-padding: 12 14 14 14; -fx-background-color: white; " +
                "-fx-background-radius: 0 0 14 14;");

        Label titre = new Label(e.getTitre() != null ? e.getTitre() : "—");
        titre.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #1a1a1a; -fx-wrap-text: true;");
        titre.setMaxWidth(Double.MAX_VALUE);

        Label date = new Label("📅 " + (e.getDate() != null ? e.getDate().toString() : "—"));
        date.setStyle("-fx-font-size: 12; -fx-text-fill: #666;");

        Label lieu = new Label("📍 " + (e.getLieu() != null ? e.getLieu() : "—"));
        lieu.setStyle("-fx-font-size: 12; -fx-text-fill: #888;");

        Label places = new Label("🎟️ " + e.getNbPlaces() + " places");
        places.setStyle("-fx-font-size: 12; -fx-text-fill: #ff6b00; -fx-font-weight: bold;");

        infoBox.getChildren().addAll(titre, date, lieu, places);
        card.getChildren().addAll(imageContainer, infoBox);

        card.setOnMouseClicked(ev -> {
            set(lblSelectedNom, e.getTitre());
            set(lblSelectedDate, e.getDate() != null ? e.getDate().toString() : "—");
            set(lblSelectedLieu, e.getLieu());
            set(lblSelectedPlaces, e.getNbPlaces() + " places");
        });

        card.setOnMouseEntered(ev ->
                card.setStyle("-fx-background-color: white; -fx-background-radius: 14; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(255,107,0,0.20), 14, 0, 0, 4); -fx-cursor: hand;"));
        card.setOnMouseExited(ev ->
                card.setStyle("-fx-background-color: white; -fx-background-radius: 14; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 8, 0, 0, 2); -fx-cursor: hand;"));

        return card;
    }

    /**
     * Charge l'image d'un événement via FileManager.resolveImageFile()
     * qui gère tous les formats de chemins (absolu, relatif, nom seul, legacy).
     *
     * ✅ CORRECTION : l'Image est chargée sans largeur fixe (requestedWidth=0)
     * pour laisser le bind() de fitWidthProperty gérer le redimensionnement.
     */
    private ImageView loadEventImage(Evenement e) {
        String storedPath = e.getImagePath();
        if (storedPath == null || storedPath.trim().isEmpty()) return null;

        // Normaliser les séparateurs Windows → Unix
        storedPath = storedPath.trim().replace("\\", "/");

        // Utiliser FileManager pour résoudre le chemin de manière robuste
        File imageFile = FileManager.resolveImageFile(storedPath);

        if (imageFile != null) {
            try {
                // ✅ CORRECTION : requestedWidth=0 → JavaFX charge à la taille réelle
                // Le redimensionnement est géré par fitWidthProperty().bind() dans buildCard
                Image image = new Image(imageFile.toURI().toString(), 0, 150, true, true);
                if (!image.isError()) {
                    return new ImageView(image);
                } else {
                    System.err.println("[EVENTfront] Erreur interne image : " + imageFile.getAbsolutePath());
                }
            } catch (Exception ex) {
                System.err.println("[EVENTfront] Erreur chargement image ["
                        + imageFile.getAbsolutePath() + "] : " + ex.getMessage());
            }
        } else {
            System.err.println("[EVENTfront] Image introuvable pour le chemin stocké : " + storedPath);
        }

        return null;
    }

    private void setupFilters() {
        if (filterLieuCombo != null) {
            ObservableList<String> lieux = FXCollections.observableArrayList("Tous");
            allEvenements.stream()
                    .map(Evenement::getLieu)
                    .filter(l -> l != null && !l.isEmpty())
                    .distinct()
                    .forEach(lieux::add);
            filterLieuCombo.setItems(lieux);
            filterLieuCombo.setValue("Tous");
            filterLieuCombo.setOnAction(e -> applyFilters());
        }

        if (filterMoisCombo != null) {
            filterMoisCombo.setItems(FXCollections.observableArrayList(
                    "Tous", "Janvier", "Février", "Mars", "Avril", "Mai", "Juin",
                    "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"));
            filterMoisCombo.setValue("Tous");
            filterMoisCombo.setOnAction(e -> applyFilters());
        }
    }

    private void applyFilters() {
        String lieu = filterLieuCombo != null ? filterLieuCombo.getValue() : "Tous";
        String mois = filterMoisCombo != null ? filterMoisCombo.getValue() : "Tous";

        List<Evenement> filtered = allEvenements.stream()
                .filter(e -> "Tous".equals(lieu) || (e.getLieu() != null && e.getLieu().equals(lieu)))
                .filter(e -> {
                    if ("Tous".equals(mois) || e.getDate() == null) return true;
                    int moisNum = List.of("", "Janvier", "Février", "Mars", "Avril", "Mai", "Juin",
                            "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre").indexOf(mois);
                    return e.getDate().getMonthValue() == moisNum;
                })
                .collect(Collectors.toList());

        displayEvenements(filtered);

        if (btnAllEvenements != null) {
            boolean isFiltered = !"Tous".equals(lieu) || !"Tous".equals(mois);
            btnAllEvenements.setVisible(isFiltered);
            btnAllEvenements.setManaged(isFiltered);
        }
    }

    // ==================== FXML HANDLERS ====================

    @FXML
    private void handleResetFilters() {
        if (filterLieuCombo != null) filterLieuCombo.setValue("Tous");
        if (filterMoisCombo != null) filterMoisCombo.setValue("Tous");
        displayEvenements(allEvenements);
        if (btnAllEvenements != null) {
            btnAllEvenements.setVisible(false);
            btnAllEvenements.setManaged(false);
        }
    }

    @FXML
    private void handleShowAllEvenements() {
        handleResetFilters();
    }

    @FXML
    private void handleVersActivites() {
        navigateTo("/activitesfront.fxml", "Activités");
    }

    @FXML
    private void handleBackOffice() {
        navigateTo("/Evenementsback.fxml", "Événements (Back Office)");
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

    // ==================== NAVIGATION ====================

    private void setupNavigationButtons() {
        if (btnDestinations != null) {
            btnDestinations.setOnMouseClicked(e -> navigateTo("/DestinationFront.fxml", "Destinations"));
            setupNavButtonHover(btnDestinations);
        }
        if (btnHebergements != null) {
            btnHebergements.setOnMouseClicked(e -> navigateTo("/HebergementFront.fxml", "Hébergements"));
            setupNavButtonHover(btnHebergements);
        }
        if (btnCategories != null) {
            btnCategories.setOnMouseClicked(e -> navigateTo("/categoriesfront.fxml", "Catégories"));
            setupNavButtonHover(btnCategories);
        }
        if (btnActivites != null) {
            btnActivites.setOnMouseClicked(e -> navigateTo("/activitesfront.fxml", "Activités"));
            setupNavButtonHover(btnActivites);
        }
        if (btnEvenements != null) {
            btnEvenements.setOnMouseClicked(e -> loadEvenements());
        }
        if (btnVoyages != null) {
            btnVoyages.setOnMouseClicked(e -> navigateTo("/PageVoyage.fxml", "Voyages"));
            setupNavButtonHover(btnVoyages);
        }
        if (btnBudgets != null) {
            btnBudgets.setOnMouseClicked(e -> navigateTo("/BudgetDepenseFront.fxml", "Budgets"));
            setupNavButtonHover(btnBudgets);
        }
        if (btnHome != null) {
            btnHome.setOnMouseClicked(e -> navigateTo("/HomePage.fxml", "Accueil"));
            btnHome.setOnMouseEntered(ev ->
                    btnHome.setStyle("-fx-background-color: #ff8c42; -fx-background-radius: 12; -fx-min-width: 40; -fx-min-height: 40; -fx-cursor: hand;"));
            btnHome.setOnMouseExited(ev ->
                    btnHome.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 12; -fx-min-width: 40; -fx-min-height: 40; -fx-cursor: hand;"));
        }
        if (btnNotifications != null) {
            btnNotifications.setOnMouseClicked(e ->
                    showInfo("Notifications", "Fonctionnalité à venir."));
        }
    }

    private void navigateTo(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = getStage();
            if (stage == null) return;
            stage.setScene(new Scene(root,
                    javafx.stage.Screen.getPrimary().getVisualBounds().getWidth(),
                    javafx.stage.Screen.getPrimary().getVisualBounds().getHeight()));
            stage.setTitle("TravelMate - " + title);
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            showError("Erreur de navigation", "Impossible d'ouvrir : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Stage getStage() {
        if (btnEvenements != null && btnEvenements.getScene() != null)
            return (Stage) btnEvenements.getScene().getWindow();
        if (evenementsGrid != null && evenementsGrid.getScene() != null)
            return (Stage) evenementsGrid.getScene().getWindow();
        if (userProfileBox != null && userProfileBox.getScene() != null)
            return (Stage) userProfileBox.getScene().getWindow();
        return null;
    }

    private void setupNavButtonHover(HBox btn) {
        if (btn == null) return;
        btn.setOnMouseEntered(e ->
                btn.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 12; -fx-padding: 6 10; -fx-cursor: hand;"));
        btn.setOnMouseExited(e ->
                btn.setStyle("-fx-background-color: transparent; -fx-background-radius: 12; -fx-padding: 6 10; -fx-cursor: hand;"));
    }

    private void setupUserProfile() {
        if (userProfileBox != null) {
            userProfileBox.setOnMouseClicked(e -> navigateTo("/fxml/profile.fxml", "Mon Profil"));
            userProfileBox.setOnMouseEntered(e ->
                    userProfileBox.setStyle("-fx-background-color: #e2e8f0; -fx-background-radius: 25; -fx-padding: 6 12 6 6; -fx-cursor: hand;"));
            userProfileBox.setOnMouseExited(e ->
                    userProfileBox.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 25; -fx-padding: 6 12 6 6; -fx-cursor: hand;"));
        }
    }

    private void updateUserInfo() {
        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser != null) {
            set(lblUserName, currentUser.getPrenom() + " " + currentUser.getNom());
            set(lblUserRole, currentUser.getRole());
        } else {
            set(lblUserName, "Utilisateur");
            set(lblUserRole, "Non connecté");
        }
    }

    private void updateDateLabel() {
        set(lblDate, LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy",
                java.util.Locale.FRENCH)));
    }

    // ==================== UTILITIES ====================

    private void set(Label lbl, String text) {
        if (lbl != null) lbl.setText(text);
    }

    private void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }

    private void showInfo(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}