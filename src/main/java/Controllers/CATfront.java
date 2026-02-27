package Controllers;

import Entities.Categories;
import Services.CategoriesCRUD;
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
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class CATfront implements Initializable {

    // ─── FXML ─────────────────────────────────────────────────────────
    @FXML private GridPane categoriesGrid;
    @FXML private Label lblTotalCategories;
    @FXML private Button btnToggleOffice;
    @FXML private HBox btnVersActivites;
    @FXML private ComboBox<String> filterTypeCombo;
    @FXML private ComboBox<String> filterSaisonCombo;
    @FXML private ComboBox<String> filterIntensiteCombo;
    @FXML private PieChart pieChartSaison;
    @FXML private PieChart pieChartType;
    @FXML private Label lblDate;
    @FXML private Label lblTopCategorie;
    @FXML private Label lblTopType;
    @FXML private Label lblTopSaison;
    @FXML private Label lblSaisonCount;
    @FXML private Label lblSaisonIcon;
    @FXML private Label lblTopPublic;
    @FXML private Label lblSelectedNom;
    @FXML private Label lblSelectedInfo;
    @FXML private TextField searchField;
    @FXML private Button btnTranslate;

    // Navigation buttons (navbar)
    @FXML private HBox btnDestinations;
    @FXML private HBox btnHebergements;
    @FXML private HBox btnItineraires;
    @FXML private HBox btnVoyages;
    @FXML private HBox btnBudgets;
    @FXML private HBox userProfileBox;
    @FXML private Label lblUserName;
    @FXML private Label lblUserRole;

    // Nouveau bouton pour voir toutes les activités
    @FXML private Button btnVoirActivites;

    // Scroll navigation (plus utilisée mais conservée pour compatibilité)
    @FXML private ScrollPane navScrollPane;
    @FXML private HBox navLinksContainer;

    // ─── State ────────────────────────────────────────────────────────
    private CategoriesCRUD categoriesCRUD;
    private ObservableList<Categories> categoriesList;
    private FilteredList<Categories> filteredData;

    private static final String[] SAISON_COLORS = {"#34d399","#fbbf24","#ff6b00","#60a5fa"};
    private static final String[] TYPE_COLORS   = {"#ef4444","#34d399","#60a5fa","#f97316",
            "#fbbf24","#a78bfa","#86efac","#f5a623"};

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        categoriesCRUD = new CategoriesCRUD();
        categoriesList = FXCollections.observableArrayList();

        safeSet(lblDate, "📅 " + LocalDate.now()
                .format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.FRENCH)));

        setupFilters();
        setupSearch();
        setupNavigationButtons();
        updateUserInfo();
        loadCategories();
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

        // Voyages button (pas encore implémenté)
        if (btnVoyages != null) {
            btnVoyages.setOnMouseClicked(event -> showNotImplementedAlert("Voyages"));
        }

        // Budgets button (pas encore implémenté)
        if (btnBudgets != null) {
            btnBudgets.setOnMouseClicked(event -> showNotImplementedAlert("Budgets"));
        }

        // User profile
        if (userProfileBox != null) {
            userProfileBox.setOnMouseClicked(event -> navigateToProfile());
        }

        // Bouton Voir toutes les activités
        if (btnVoirActivites != null) {
            btnVoirActivites.setOnAction(event -> navigateToActivites());
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
            e.printStackTrace();
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
            e.printStackTrace();
        }
    }

    @FXML
    private void navigateToActivites() {
        CategorieContext.categorieFiltre = null;
        try {
            System.out.println("Tentative de chargement: /activitesfront.fxml");
            URL resource = getClass().getResource("/activitesfront.fxml");
            if (resource == null) {
                showError("Erreur", "Fichier activitesfront.fxml non trouvé dans le classpath");
                return;
            }

            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) btnVoirActivites.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("TravelMate - Activités");
            stage.show();
        } catch (IOException e) {
            showError("Erreur de navigation", "Impossible de charger les activités: " + e.getMessage());
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
            e.printStackTrace();
        }
    }

    private void showNotImplementedAlert(String feature) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Fonctionnalité à venir");
        alert.setHeaderText(null);
        alert.setContentText("La fonctionnalité \"" + feature + "\" sera bientôt disponible !");
        alert.showAndWait();
    }

    // Méthodes de défilement conservées mais plus utilisées
    @FXML
    private void scrollNavLeft() {
        // Méthode conservée pour compatibilité
    }

    @FXML
    private void scrollNavRight() {
        // Méthode conservée pour compatibilité
    }

    private void setupSearch() {
        if (searchField != null) {
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                applyFilters();
            });
            searchField.setPromptText("Rechercher une catégorie...");
        }
    }

    private void setupFilters() {
        filterTypeCombo.getItems().addAll("Tous les types","Aventure","Détente","Culturel",
                "Sportif","Gastronomique","Famille","Nature");
        filterTypeCombo.setValue("Tous les types");
        filterTypeCombo.setOnAction(e -> applyFilters());

        filterSaisonCombo.getItems().addAll("Toutes saisons","Printemps","Été","Automne","Hiver");
        filterSaisonCombo.setValue("Toutes saisons");
        filterSaisonCombo.setOnAction(e -> applyFilters());

        filterIntensiteCombo.getItems().addAll("Tous niveaux","Faible","Moyen","Élevé");
        filterIntensiteCombo.setValue("Tous niveaux");
        filterIntensiteCombo.setOnAction(e -> applyFilters());
    }

    private void applyFilters() {
        if (filteredData == null) return;

        String st = filterTypeCombo.getValue();
        String ss = filterSaisonCombo.getValue();
        String si = filterIntensiteCombo.getValue();
        String searchText = searchField != null ? searchField.getText().toLowerCase() : "";

        filteredData.setPredicate(c -> {
            boolean mt = st.equals("Tous les types") || (c.getType() != null && c.getType().equals(st));
            boolean ms = ss.equals("Toutes saisons") || (c.getSaison() != null && c.getSaison().equals(ss));
            boolean mi = si.equals("Tous niveaux") || (c.getNiveauintensite() != null && c.getNiveauintensite().equals(si));

            boolean matchSearch = searchText.isEmpty() ||
                    (c.getNom() != null && c.getNom().toLowerCase().contains(searchText)) ||
                    (c.getDescription() != null && c.getDescription().toLowerCase().contains(searchText)) ||
                    (c.getType() != null && c.getType().toLowerCase().contains(searchText)) ||
                    (c.getPubliccible() != null && c.getPubliccible().toLowerCase().contains(searchText));

            return mt && ms && mi && matchSearch;
        });

        refreshDisplay(filteredData);
    }

    @FXML
    private void handleResetFilters() {
        filterTypeCombo.setValue("Tous les types");
        filterSaisonCombo.setValue("Toutes saisons");
        filterIntensiteCombo.setValue("Tous niveaux");
        if (searchField != null) {
            searchField.clear();
        }
        refreshDisplay(categoriesList);
    }

    private void loadCategories() {
        try {
            List<Categories> liste = categoriesCRUD.afficher();
            categoriesList.clear();
            categoriesList.addAll(liste);
            filteredData = new FilteredList<>(categoriesList, p -> true);
            refreshDisplay(filteredData);
        } catch (SQLException e) {
            showError("Erreur de chargement", "Impossible de charger les catégories : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void refreshDisplay(ObservableList<Categories> list) {
        displayCategories(list);
        updateStats(list);
        updatePieCharts(list);
    }

    private void displayCategories(ObservableList<Categories> categories) {
        categoriesGrid.getChildren().clear();
        categoriesGrid.getColumnConstraints().clear();
        for (int i = 0; i < 3; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setHgrow(Priority.ALWAYS);
            cc.setPercentWidth(33.33);
            categoriesGrid.getColumnConstraints().add(cc);
        }
        int col = 0, row = 0;
        for (Categories cat : categories) {
            categoriesGrid.add(createCard(cat), col, row);
            if (++col >= 3) { col = 0; row++; }
        }
        lblTotalCategories.setText(String.valueOf(categories.size()));
    }

    private VBox createCard(Categories cat) {
        VBox card = new VBox(0);
        card.getStyleClass().add("category-card");
        card.setPrefWidth(300);

        HBox banner = new HBox(10);
        banner.setAlignment(Pos.CENTER_LEFT);
        banner.setPrefHeight(65);
        banner.setPadding(new Insets(12, 16, 12, 16));
        banner.setStyle("-fx-background-color:" + getTypeBannerColor(cat.getType())
                + ";-fx-background-radius:16 16 0 0;");

        Label iconLbl = new Label(getTypeIcon(cat.getType()));
        iconLbl.setStyle("-fx-font-size:28;");

        VBox bannerText = new VBox(2);
        Label bannerNom = new Label(cat.getNom() != null ? cat.getNom() : "");
        bannerNom.setStyle("-fx-font-size:15;-fx-font-weight:bold;-fx-text-fill:white;");
        bannerNom.setWrapText(true);
        Label bannerType = new Label(cat.getType() != null ? cat.getType() : "");
        bannerType.setStyle("-fx-font-size:11;-fx-text-fill:rgba(255,255,255,0.8);");
        bannerText.getChildren().addAll(bannerNom, bannerType);
        banner.getChildren().addAll(iconLbl, bannerText);

        VBox body = new VBox(10);
        body.setPadding(new Insets(14, 16, 14, 16));
        body.setStyle("-fx-background-color:white;-fx-background-radius:0 0 16 16;");

        if (cat.getDescription() != null && !cat.getDescription().isEmpty()) {
            Label desc = new Label(cat.getDescription());
            desc.setStyle("-fx-text-fill:#777;-fx-font-size:12;");
            desc.setWrapText(true);
            desc.setMaxHeight(40);
            body.getChildren().add(desc);
        }

        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 0;");
        body.getChildren().add(separator);

        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(8);
        infoGrid.setVgap(8);

        addInfoRow(infoGrid, 0, "🌤", "Saison",
                cat.getSaison() != null ? cat.getSaison() : "Toutes saisons",
                getSaisonStyle(cat.getSaison()));
        addInfoRow(infoGrid, 1, "📊", "Intensité",
                cat.getNiveauintensite() != null ? cat.getNiveauintensite() : "—",
                getIntensiteStyle(cat.getNiveauintensite()));
        addInfoRow(infoGrid, 2, "👥", "Public",
                cat.getPubliccible() != null ? cat.getPubliccible() : "Tous publics",
                "-fx-text-fill:#555;-fx-font-size:12;");
        body.getChildren().add(infoGrid);

        Button btnAct = new Button("🎯  Voir les activités de cette catégorie");
        btnAct.setStyle("-fx-background-color:#fff3e8;-fx-text-fill:#ff6b00;"
                + "-fx-border-color:#ff6b00;-fx-border-radius:20;-fx-background-radius:20;"
                + "-fx-padding:7 14;-fx-font-size:11;-fx-font-weight:bold;-fx-cursor:hand;-fx-max-width:Infinity;");
        btnAct.setMaxWidth(Double.MAX_VALUE);
        btnAct.setOnAction(e -> navigateToActivitesForCategorie(cat));

        btnAct.setOnMouseEntered(e -> {
            btnAct.setStyle("-fx-background-color:#ff6b00;-fx-text-fill:white;"
                    + "-fx-border-color:#ff6b00;-fx-border-radius:20;-fx-background-radius:20;"
                    + "-fx-padding:7 14;-fx-font-size:11;-fx-font-weight:bold;-fx-cursor:hand;-fx-max-width:Infinity;");
        });
        btnAct.setOnMouseExited(e -> {
            btnAct.setStyle("-fx-background-color:#fff3e8;-fx-text-fill:#ff6b00;"
                    + "-fx-border-color:#ff6b00;-fx-border-radius:20;-fx-background-radius:20;"
                    + "-fx-padding:7 14;-fx-font-size:11;-fx-font-weight:bold;-fx-cursor:hand;-fx-max-width:Infinity;");
        });

        body.getChildren().add(btnAct);
        card.getChildren().addAll(banner, body);

        card.setOnMouseEntered(e -> {
            card.setEffect(new DropShadow(18, Color.web("#f5a62340")));
            card.setScaleX(1.02);
            card.setScaleY(1.02);
        });
        card.setOnMouseExited(e -> {
            card.setEffect(new DropShadow(10, Color.web("#00000018")));
            card.setScaleX(1.0);
            card.setScaleY(1.0);
        });
        card.setOnMouseClicked(e -> showDetail(cat));

        return card;
    }

    private void addInfoRow(GridPane g, int row, String icon, String label, String value, String style) {
        Label ic  = new Label(icon);  ic.setStyle("-fx-font-size:13;");
        Label lbl = new Label(label + " :"); lbl.setStyle("-fx-text-fill:#aaa;-fx-font-size:12;");
        Label val = new Label(value); val.setStyle(style);
        g.add(ic, 0, row);
        g.add(lbl, 1, row);
        g.add(val, 2, row);
    }

    private void showDetail(Categories cat) {
        safeSet(lblSelectedNom, cat.getNom() != null ? cat.getNom() : "—");
        safeSet(lblSelectedInfo, (cat.getType() != null ? cat.getType() : "") + " · "
                + (cat.getSaison() != null ? cat.getSaison() : "") + " · "
                + (cat.getNiveauintensite() != null ? cat.getNiveauintensite() : ""));
    }

    private void updateStats(ObservableList<Categories> list) {
        lblTotalCategories.setText(String.valueOf(list.size()));
        if (list.isEmpty()) {
            safeSet(lblTopCategorie,"—");
            safeSet(lblTopType,"—");
            safeSet(lblTopSaison,"—");
            safeSet(lblSaisonCount,"0 catégorie");
            safeSet(lblTopPublic,"—");
            return;
        }
        safeSet(lblTopCategorie, list.get(0).getNom() != null ? list.get(0).getNom() : "—");
        safeSet(lblTopType, list.get(0).getType() != null ? list.get(0).getType() : "—");

        Map<String,Long> bySaison = list.stream().filter(c -> c.getSaison() != null)
                .collect(Collectors.groupingBy(Categories::getSaison, Collectors.counting()));
        if (!bySaison.isEmpty()) {
            String dom = Collections.max(bySaison.entrySet(), Map.Entry.comparingByValue()).getKey();
            long cnt = bySaison.get(dom);
            safeSet(lblTopSaison, dom);
            safeSet(lblSaisonCount, cnt + " catégorie" + (cnt > 1 ? "s" : ""));
            safeSet(lblSaisonIcon, getSaisonIcon(dom));
        }

        Map<String,Long> byPublic = list.stream().filter(c -> c.getPubliccible() != null)
                .collect(Collectors.groupingBy(Categories::getPubliccible, Collectors.counting()));
        if (!byPublic.isEmpty()) {
            safeSet(lblTopPublic, Collections.max(byPublic.entrySet(), Map.Entry.comparingByValue()).getKey());
        }
    }

    private void updatePieCharts(ObservableList<Categories> list) {
        fillPie(pieChartSaison, list.stream().filter(c -> c.getSaison() != null)
                .collect(Collectors.groupingBy(Categories::getSaison, Collectors.counting())), SAISON_COLORS);
        fillPie(pieChartType, list.stream().filter(c -> c.getType() != null)
                .collect(Collectors.groupingBy(Categories::getType, Collectors.counting())), TYPE_COLORS);
    }

    private void fillPie(PieChart chart, Map<String, Long> data, String[] colors) {
        if (chart == null) return;
        chart.getData().clear();
        int i = 0;
        for (Map.Entry<String, Long> e : data.entrySet()) {
            PieChart.Data d = new PieChart.Data(e.getKey(), e.getValue());
            chart.getData().add(d);
            final String col = colors[i++ % colors.length];
            d.nodeProperty().addListener((obs, o, n) -> { if (n != null) n.setStyle("-fx-pie-color:" + col + ";"); });
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

    private void navigateToActivitesForCategorie(Categories cat) {
        CategorieContext.categorieFiltre = cat.getNom();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/activitesfront.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) btnVersActivites.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("TravelMate - Activités : " + cat.getNom());
            stage.show();
        } catch (IOException e) {
            showError("Erreur de navigation", "Impossible de charger les activités: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleVersActivites() {
        CategorieContext.categorieFiltre = null;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/activitesfront.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) btnVersActivites.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("TravelMate - Activités");
            stage.show();
        } catch (IOException e) {
            showError("Erreur de navigation", "Impossible de charger les activités: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleToggleOffice() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/categoriesback.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) btnToggleOffice.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("TravelMate - Back Office Catégories");
            stage.show();
        } catch (IOException e) {
            showError("Erreur de navigation", "Impossible de charger le back office: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String getTypeBannerColor(String type) {
        if (type == null) return "linear-gradient(to right,#f5a623,#ff6b00)";
        switch (type.toLowerCase()) {
            case "aventure":      return "linear-gradient(to right,#ef4444,#dc2626)";
            case "détente":       return "linear-gradient(to right,#34d399,#059669)";
            case "culturel":      return "linear-gradient(to right,#60a5fa,#2563eb)";
            case "sportif":       return "linear-gradient(to right,#f97316,#ea580c)";
            case "gastronomique": return "linear-gradient(to right,#fbbf24,#d97706)";
            case "famille":       return "linear-gradient(to right,#a78bfa,#7c3aed)";
            case "nature":        return "linear-gradient(to right,#86efac,#16a34a)";
            default:              return "linear-gradient(to right,#f5a623,#ff6b00)";
        }
    }

    private String getTypeIcon(String type) {
        if (type == null) return "📑";
        switch (type.toLowerCase()) {
            case "aventure": return "🏔";
            case "détente": return "🧘";
            case "culturel": return "🏛";
            case "sportif": return "⚽";
            case "gastronomique": return "🍽";
            case "famille": return "👨‍👩‍👧";
            case "nature": return "🌿";
            default: return "📑";
        }
    }

    private String getSaisonIcon(String s) {
        if (s == null) return "🌤";
        switch (s.toLowerCase()) {
            case "printemps": return "🌸";
            case "été": return "☀";
            case "automne": return "🍂";
            case "hiver": return "❄";
            default: return "🌤";
        }
    }

    private String getSaisonStyle(String s) {
        if (s == null) return "-fx-text-fill:#555;-fx-font-size:12;";
        switch (s.toLowerCase()) {
            case "printemps": return "-fx-text-fill:#059669;-fx-font-size:12;-fx-font-weight:bold;";
            case "été":       return "-fx-text-fill:#d97706;-fx-font-size:12;-fx-font-weight:bold;";
            case "automne":   return "-fx-text-fill:#ff6b00;-fx-font-size:12;-fx-font-weight:bold;";
            case "hiver":     return "-fx-text-fill:#2563eb;-fx-font-size:12;-fx-font-weight:bold;";
            default:          return "-fx-text-fill:#555;-fx-font-size:12;";
        }
    }

    private String getIntensiteStyle(String i) {
        if (i == null) return "-fx-text-fill:#555;-fx-font-size:12;";
        switch (i.toLowerCase()) {
            case "faible": return "-fx-text-fill:#059669;-fx-font-size:12;-fx-font-weight:bold;";
            case "moyen":  return "-fx-text-fill:#d97706;-fx-font-size:12;-fx-font-weight:bold;";
            case "élevé":  return "-fx-text-fill:#ef4444;-fx-font-size:12;-fx-font-weight:bold;";
            default:       return "-fx-text-fill:#555;-fx-font-size:12;";
        }
    }

    private void safeSet(Label lbl, String text) {
        if (lbl != null) lbl.setText(text);
    }

    private void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}