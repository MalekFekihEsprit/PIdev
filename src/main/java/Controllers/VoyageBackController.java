package Controllers;

import Entities.Voyage;
import Services.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import Services.ExportService;

import java.io.IOException;
import java.net.URL;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class VoyageBackController implements Initializable {

    // Constants for status values
    private static final String STATUT_A_VENIR = "a venir";
    private static final String STATUT_EN_COURS = "En cours";
    private static final String STATUT_TERMINE = "Terminé";
    private static final String STATUT_ANNULE = "Annulé";
    private static final String STATUT_TOUS = "Tous";
    private static final String DEST_INCONNUE = "Inconnue";
    private static final String PERCENT_FORMAT = "(%.1f%%)";

    @FXML
    private TableView<Voyage> tableVoyages;
    @FXML
    private TableColumn<Voyage, Integer> colId;
    @FXML
    private TableColumn<Voyage, String> colTitre;
    @FXML
    private TableColumn<Voyage, Date> colDateDebut;
    @FXML
    private TableColumn<Voyage, Date> colDateFin;
    @FXML
    private TableColumn<Voyage, String> colStatut;
    @FXML
    private TableColumn<Voyage, Integer> colIdDestination;
    @FXML
    private TableColumn<Voyage, String> colDestination;
    @FXML
    private TableColumn<Voyage, Void> colActions;

    @FXML
    private TextField tfTitre;
    @FXML
    private DatePicker dpDateDebut;
    @FXML
    private DatePicker dpDateFin;
    @FXML
    private ComboBox<String> cbStatut;
    @FXML
    private TextField tfIdDestination;
    @FXML
    private TextField tfDestinationNom;

    // Search fields
    @FXML
    private TextField tfSearchTitre;
    @FXML
    private TextField tfSearchId;
    @FXML
    private ComboBox<String> cbSearchStatut;
    @FXML
    private Button btnSearch;
    @FXML
    private Button btnReset;

    @FXML
    private Label lblTotalVoyages;
    @FXML
    private Label lblVoyagesActifs;
    @FXML
    private Label lblVoyagesTermines;
    @FXML
    private Label lblVoyagesCount;

    // Sidebar labels
    @FXML
    private Label lblSidebarVoyagesCount;
    @FXML
    private Label statsTotalVoyages;
    @FXML
    private Label statsActifs;
    @FXML
    private Label statsTermines;
    @FXML
    private Label lblLastUpdate;

    @FXML
    private Button btnAjouter;
    @FXML
    private Button btnModifier;
    @FXML
    private Button btnAnnuler;
    @FXML
    private Button btnRefresh;

    // Statistics
    @FXML
    private Label lblCountAVenir;
    @FXML
    private Label lblCountEnCours;
    @FXML
    private Label lblCountTermine;
    @FXML
    private Label lblCountAnnule;
    @FXML
    private Label lblPourcentAVenir;
    @FXML
    private Label lblPourcentEnCours;
    @FXML
    private Label lblPourcentTermine;
    @FXML
    private Label lblPourcentAnnule;
    @FXML
    private ProgressBar pbAVenir;
    @FXML
    private ProgressBar pbEnCours;
    @FXML
    private ProgressBar pbTermine;
    @FXML
    private ProgressBar pbAnnule;
    @FXML
    private Label lblTotalVoyages2;
    @FXML
    private PieChart pieChartStatuts;

    // Export buttons
    @FXML
    private Button btnExportPDF;
    @FXML
    private Button btnExportExcel;

    private VoyageCRUDV voyageCRUD = new VoyageCRUDV();
    private final QRCodeServicess qrCodeServicess = new QRCodeServicess();
    private final RestCountriesService restCountriesService = new RestCountriesService();
    private final UnsplashService unsplashService = new UnsplashService();
    private ObservableList<Voyage> voyageList = FXCollections.observableArrayList();
    private FilteredList<Voyage> filteredData;
    private SortedList<Voyage> sortedData;
    private Voyage voyageSelectionne = null;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialiser les colonnes du tableau
        colId.setCellValueFactory(new PropertyValueFactory<>("id_voyage"));
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titre_voyage"));
        colDateDebut.setCellValueFactory(new PropertyValueFactory<>("date_debut"));
        colDateFin.setCellValueFactory(new PropertyValueFactory<>("date_fin"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colIdDestination.setCellValueFactory(new PropertyValueFactory<>("id_destination"));

        // Colonne destination avec nom
        colDestination.setCellValueFactory(cellData -> {
            try {
                int idDest = cellData.getValue().getId_destination();
                String nomDest = voyageCRUD.getNomDestination(idDest);
                return new javafx.beans.property.SimpleStringProperty(nomDest != null ? nomDest : DEST_INCONNUE);
            } catch (SQLException e) {
                return new javafx.beans.property.SimpleStringProperty(DEST_INCONNUE);
            }
        });

        // Ajouter les boutons d'action
        ajouterBoutonsActions();

        // Initialiser les ComboBox des statuts
        cbStatut.getItems().addAll(STATUT_A_VENIR, STATUT_EN_COURS, STATUT_TERMINE, STATUT_ANNULE);
        cbStatut.setValue(STATUT_A_VENIR);

        cbSearchStatut.getItems().addAll(STATUT_TOUS, STATUT_A_VENIR, STATUT_EN_COURS, STATUT_TERMINE, STATUT_ANNULE);
        cbSearchStatut.setValue(STATUT_TOUS);

        // Configurer les DatePickers pour empêcher les dates incohérentes
        configurerDatePickers();

        // Configurer l'écouteur de changement d'ID destination
        tfIdDestination.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) {
                try {
                    int idDest = Integer.parseInt(newVal);
                    String nomDest = voyageCRUD.getNomDestination(idDest);
                    tfDestinationNom.setText(nomDest != null ? nomDest : DEST_INCONNUE);
                } catch (NumberFormatException | SQLException e) {
                    tfDestinationNom.setText(DEST_INCONNUE);
                }
            } else {
                tfDestinationNom.setText("");
            }
        });

        // Configurer les boutons PRINCIPAUX (CEUX-CI DOIVENT ÊTRE AVANT chargerVoyages)
        btnAjouter.setOnAction(this::ajouterVoyage);
        btnModifier.setOnAction(this::modifierVoyage);
        btnAnnuler.setOnAction(this::annulerModification);
        btnRefresh.setOnAction(this::refreshTable);
        btnSearch.setOnAction(this::rechercherVoyages);
        btnReset.setOnAction(this::resetRecherche);

        // Configurer les boutons d'export (TRÈS IMPORTANT - à mettre ici)
        if (btnExportPDF != null) {
            btnExportPDF.setOnAction(this::exportToPDF);
            System.out.println("✅ Bouton PDF configuré avec succès");
        } else {
            System.err.println("❌ btnExportPDF est null - vérifiez le fx:id dans le FXML");
        }

        if (btnExportExcel != null) {
            btnExportExcel.setOnAction(this::exportToExcel);
            System.out.println("✅ Bouton Excel configuré avec succès");
        } else {
            System.err.println("❌ btnExportExcel est null - vérifiez le fx:id dans le FXML");
        }

        // Configurer la recherche
        configurerRecherche();

        // Charger les données (APRÈS la configuration des boutons)
        chargerVoyages();

        // Mettre à jour la date de dernière mise à jour
        mettreAJourDateMAJ();
    }

    private void ajouterBoutonsActions() {
        Callback<TableColumn<Voyage, Void>, TableCell<Voyage, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<Voyage, Void> call(final TableColumn<Voyage, Void> param) {
                return new TableCell<>() {
                    private final Button btnVoir = new Button("👁️");
                    private final Button btnModifier = new Button("✏️");
                    private final Button btnSupprimer = new Button("🗑️");
                    private final Button btnParticipants = new Button("👥");
                    private final HBox pane = new HBox(5, btnVoir, btnModifier, btnSupprimer, btnParticipants);

                    {
                        btnVoir.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 4 8; -fx-font-size: 11; -fx-cursor: hand;");
                        btnModifier.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 4 8; -fx-font-size: 11; -fx-cursor: hand;");
                        btnSupprimer.setStyle("-fx-background-color: #ec4899; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 4 8; -fx-font-size: 11; -fx-cursor: hand;");
                        btnParticipants.setStyle("-fx-background-color: #ff8c42; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 4 8; -fx-font-size: 11; -fx-cursor: hand;");

                        btnVoir.setOnAction(event -> {
                            Voyage voyage = getTableView().getItems().get(getIndex());
                            voirVoyage(voyage);
                        });

                        btnModifier.setOnAction(event -> {
                            Voyage voyage = getTableView().getItems().get(getIndex());
                            remplirFormulaire(voyage);
                        });

                        btnSupprimer.setOnAction(event -> {
                            Voyage voyage = getTableView().getItems().get(getIndex());
                            supprimerVoyage(voyage);
                        });

                        btnParticipants.setOnAction(event -> {
                            Voyage voyage = getTableView().getItems().get(getIndex());
                            ouvrirParticipants(voyage);
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(pane);
                        }
                    }
                };
            }
        };
        colActions.setCellFactory(cellFactory);
    }

    // Configuration des DatePickers pour empêcher les dates incohérentes
    private void configurerDatePickers() {
        dpDateDebut.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && dpDateFin.getValue() != null && dpDateFin.getValue().isBefore(newVal)) {
                dpDateFin.setValue(null);
            }
        });

        dpDateFin.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate dateDebut = dpDateDebut.getValue();
                if (dateDebut != null) {
                    setDisable(empty || !date.isAfter(dateDebut));
                }
            }
        });
    }

    // Configuration de la recherche
    private void configurerRecherche() {
        filteredData = new FilteredList<>(voyageList, p -> true);
        sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableVoyages.comparatorProperty());
        tableVoyages.setItems(sortedData);

        sortedData.addListener((javafx.collections.ListChangeListener<Voyage>) c -> {
            int size = sortedData.size();
            lblVoyagesCount.setText(String.valueOf(size));
            if (lblSidebarVoyagesCount != null) {
                lblSidebarVoyagesCount.setText(String.valueOf(size));
            }
            mettreAJourStatistiques();
            mettreAJourStatistiquesDetaillees();
        });

        btnSearch.setOnAction(this::rechercherVoyages);
        btnReset.setOnAction(this::resetRecherche);
    }

    // Méthode de recherche
    @FXML
    private void rechercherVoyages(ActionEvent event) {
        String titre = tfSearchTitre.getText() != null ? tfSearchTitre.getText().toLowerCase().trim() : "";
        String idText = tfSearchId.getText() != null ? tfSearchId.getText().trim() : "";
        String statut = cbSearchStatut.getValue();

        filteredData.setPredicate(voyage -> {
            if (titre.isEmpty() && idText.isEmpty() && (statut == null || STATUT_TOUS.equals(statut))) {
                return true;
            }

            if (!idText.isEmpty()) {
                try {
                    int id = Integer.parseInt(idText);
                    if (voyage.getId_voyage() != id) {
                        return false;
                    }
                } catch (NumberFormatException e) {
                    return false;
                }
            }

            if (!titre.isEmpty()) {
                String voyageTitre = voyage.getTitre_voyage();
                if (voyageTitre == null || !voyageTitre.toLowerCase().contains(titre)) {
                    return false;
                }
            }

            if (statut != null && !STATUT_TOUS.equals(statut)) {
                String voyageStatut = voyage.getStatut();
                if (voyageStatut == null || !voyageStatut.equals(statut)) {
                    return false;
                }
            }

            return true;
        });

        mettreAJourStatistiques();
        mettreAJourStatistiquesDetaillees();
    }

    // Réinitialiser la recherche
    @FXML
    private void resetRecherche(ActionEvent event) {
        tfSearchTitre.clear();
        tfSearchId.clear();
        cbSearchStatut.setValue(STATUT_TOUS);

        if (filteredData != null) {
            filteredData.setPredicate(p -> true);
        }
    }

    // Mettre à jour les statistiques en fonction des données filtrées
    private void mettreAJourStatistiques() {
        if (sortedData != null) {
            int total = sortedData.size();
            lblTotalVoyages.setText(String.valueOf(total));
            if (statsTotalVoyages != null) {
                statsTotalVoyages.setText(String.valueOf(total));
            }

            long actifs = sortedData.stream()
                    .filter(v -> STATUT_EN_COURS.equals(v.getStatut()) || STATUT_A_VENIR.equals(v.getStatut()))
                    .count();
            lblVoyagesActifs.setText(String.valueOf(actifs));
            if (statsActifs != null) {
                statsActifs.setText(String.valueOf(actifs));
            }

            long termines = sortedData.stream()
                    .filter(v -> STATUT_TERMINE.equals(v.getStatut()))
                    .count();
            lblVoyagesTermines.setText(String.valueOf(termines));
            if (statsTermines != null) {
                statsTermines.setText(String.valueOf(termines));
            }
        }
    }

    // Charger les voyages
    private void chargerVoyages() {
        try {
            List<Voyage> voyages = voyageCRUD.afficher();
            voyageList.clear();
            voyageList.addAll(voyages);

            mettreAJourStatistiques();
            mettreAJourStatistiquesDetaillees();
            mettreAJourDateMAJ();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les voyages: " + e.getMessage());
        }
    }

    private void mettreAJourDateMAJ() {
        if (lblLastUpdate != null) {
            lblLastUpdate.setText("Dernière mise à jour: " +
                    java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")));
        }
    }

    @FXML
    private void ajouterVoyage(ActionEvent event) {
        if (!validerFormulaire()) return;

        try {
            Voyage v = new Voyage(
                    tfTitre.getText(),
                    Date.valueOf(dpDateDebut.getValue()),
                    Date.valueOf(dpDateFin.getValue()),
                    cbStatut.getValue(),
                    Integer.parseInt(tfIdDestination.getText())
            );

            voyageCRUD.ajouter(v);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Voyage ajouté avec succès!");
            chargerVoyages();
            resetFormulaire();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ajout: " + e.getMessage());
        }
    }

    @FXML
    private void modifierVoyage(ActionEvent event) {
        if (voyageSelectionne == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner un voyage à modifier");
            return;
        }

        if (!validerFormulaire()) return;

        try {
            voyageSelectionne.setTitre_voyage(tfTitre.getText());
            voyageSelectionne.setDate_debut(Date.valueOf(dpDateDebut.getValue()));
            voyageSelectionne.setDate_fin(Date.valueOf(dpDateFin.getValue()));
            voyageSelectionne.setStatut(cbStatut.getValue());
            voyageSelectionne.setId_destination(Integer.parseInt(tfIdDestination.getText()));

            voyageCRUD.modifier(voyageSelectionne);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Voyage modifié avec succès!");
            chargerVoyages();
            resetFormulaire();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la modification: " + e.getMessage());
        }
    }

    @FXML
    private void annulerModification(ActionEvent event) {
        resetFormulaire();
    }

    @FXML
    private void refreshTable(ActionEvent event) {
        chargerVoyages();
        resetRecherche(event);
    }

    // Navigation methods
    @FXML
    private void handleDashboardClick(MouseEvent event) {
        naviguerVers("/PageDashboardBack.fxml", "Dashboard");
    }

    @FXML
    private void handleDestinationsClick(MouseEvent event) {
        naviguerVers("/PageDestinationBack.fxml", "Destinations");
    }

    @FXML
    private void handleHebergementClick(MouseEvent event) {
        naviguerVers("/HebergementBack.fxml", "Hébergements");
    }

    @FXML
    private void handleItinerairesClick(MouseEvent event) {
        naviguerVers("/ItineraireEtEtape/PageGestionItineraires.fxml", "Itinéraires");
    }

    @FXML
    private void handleActivitesClick(MouseEvent event) {
        naviguerVers("/activitesback.fxml", "Activités");
    }

    @FXML
    private void handleCategoriesClick(MouseEvent event) {
        naviguerVers("/categoriesback.fxml", "Catégories");
    }

    @FXML
    private void handleBudgetsClick(MouseEvent event) {
        naviguerVers("/BudgetDepenseBack.fxml", "Budgets");
    }

    @FXML
    private void handleUsersClick(MouseEvent event) {
        naviguerVers("/fxml/admin_users.fxml", "Utilisateurs");
    }

    @FXML
    private void handleStatsClick(MouseEvent event) {
        naviguerVers("/fxml/admin_stats.fxml", "Statistiques");
    }

    private void naviguerVers(String fxmlPath, String titre) {
        try {
            // Vérifier si le fichier existe avant de charger
            URL resourceUrl = getClass().getResource(fxmlPath);
            if (resourceUrl == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur",
                        "Fichier introuvable: " + fxmlPath + "\nVérifiez que le fichier existe dans le dossier resources.");
                return;
            }

            FXMLLoader loader = new FXMLLoader(resourceUrl);
            Parent root = loader.load();
            Stage stage = (Stage) tableVoyages.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TravelMate - " + titre);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible de naviguer vers " + titre + ":\n" + e.getMessage() +
                            "\nVérifiez que le fichier " + fxmlPath + " existe.");
        }
    }

    private void voirVoyage(Voyage voyage) {
        String nomDest = DEST_INCONNUE;
        try {
            nomDest = voyageCRUD.getNomDestination(voyage.getId_destination());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        long durationDays = java.time.temporal.ChronoUnit.DAYS.between(
                voyage.getDate_debut().toLocalDate(), voyage.getDate_fin().toLocalDate());

        final String destination = nomDest;

        // Build rich popup (code existant...)
        Stage popup = new Stage();
        popup.setTitle("📋 " + voyage.getTitre_voyage());

        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: #0a0e27;");

        // === HEADER ===
        VBox header = new VBox(4);
        header.setPadding(new Insets(20, 24, 16, 24));
        header.setStyle("-fx-background-color: linear-gradient(to right, #ff8c42, #ff6b4a);");

        Label titleLbl = new Label(voyage.getTitre_voyage());
        titleLbl.setStyle("-fx-font-size: 22; -fx-font-weight: bold; -fx-text-fill: white;");
        Label subtitleLbl = new Label("📍 " + destination + "  •  " + durationDays + " jour" + (durationDays > 1 ? "s" : ""));
        subtitleLbl.setStyle("-fx-font-size: 12; -fx-text-fill: rgba(255,255,255,0.85);");
        header.getChildren().addAll(titleLbl, subtitleLbl);
        root.getChildren().add(header);

        // === STATS ROW ===
        HBox statsRow = new HBox(12);
        statsRow.setPadding(new Insets(14, 24, 14, 24));
        statsRow.setStyle("-fx-background-color: #111633;");
        statsRow.setAlignment(Pos.CENTER);

        statsRow.getChildren().addAll(
                buildStatCard("📅", "Début", voyage.getDate_debut().toLocalDate().toString()),
                buildStatCard("🏁", "Fin", voyage.getDate_fin().toLocalDate().toString()),
                buildStatCard("📊", "Statut", voyage.getStatut()),
                buildStatCard("🔢", "ID", String.valueOf(voyage.getId_voyage()))
        );
        root.getChildren().add(statsRow);

        // === COUNTRY INFO SECTION (async load) ===
        VBox countrySection = new VBox(8);
        countrySection.setPadding(new Insets(16, 24, 8, 24));

        Label sectionTitle = new Label("🌍 Informations sur le pays");
        sectionTitle.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #ff8c42;");
        countrySection.getChildren().add(sectionTitle);

        Label loadingLbl = new Label("⏳ Chargement des données...");
        loadingLbl.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11;");
        countrySection.getChildren().add(loadingLbl);
        root.getChildren().add(countrySection);

        // === QR CODE SECTION ===
        VBox qrSection = new VBox(8);
        qrSection.setPadding(new Insets(8, 24, 8, 24));
        qrSection.setAlignment(Pos.CENTER);

        Label qrTitle = new Label("📱 QR Code");
        qrTitle.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #ff8c42;");

        Label qrLoading = new Label("⏳ Génération...");
        qrLoading.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11;");
        qrSection.getChildren().addAll(qrTitle, qrLoading);
        root.getChildren().add(qrSection);

        // === FOOTER ===
        Label footer = new Label("TravelMate — Back Office • APIs : REST Countries • ZXing • Unsplash");
        footer.setStyle("-fx-font-size: 9; -fx-text-fill: #4a5568; -fx-padding: 12 24;");
        root.getChildren().add(footer);

        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #0a0e27; -fx-background-color: #0a0e27;");

        popup.setScene(new Scene(scrollPane, 560, 650));
        popup.show();

        // === ASYNC LOADING ===
        new Thread(() -> {
            RestCountriesService.CountryInfo countryInfo = restCountriesService.getInfosPays(destination);
            Image qrImage = null;
            try {
                qrImage = qrCodeServicess.genererQRCodeVoyage(voyage, destination);
            } catch (Exception e) {
                System.out.println("⚠️ QR generation failed: " + e.getMessage());
            }

            UnsplashService.PhotoInfo photoInfo = unsplashService.rechercherPhotoInfo(destination);
            final Image finalQr = qrImage;

            javafx.application.Platform.runLater(() -> {
                countrySection.getChildren().remove(loadingLbl);
                if (countryInfo != null) {
                    if (!countryInfo.drapeauUrl.isEmpty()) {
                        try {
                            ImageView flag = new ImageView(new Image(countryInfo.drapeauUrl, 50, 30, true, true));
                            HBox flagRow = new HBox(8);
                            flagRow.setAlignment(Pos.CENTER_LEFT);
                            Label countryName = new Label(countryInfo.nomOfficiel);
                            countryName.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 13; -fx-font-weight: 600;");
                            flagRow.getChildren().addAll(flag, countryName);
                            countrySection.getChildren().add(flagRow);
                        } catch (Exception ignored) {}
                    }
                    countrySection.getChildren().addAll(
                            buildInfoLabel("🏛️ Capitale", countryInfo.capitale),
                            buildInfoLabel("👥 Population", RestCountriesService.formatPopulation(countryInfo.population)),
                            buildInfoLabel("🌍 Continent", countryInfo.continent),
                            buildInfoLabel("🗣️ Langues", countryInfo.langues),
                            buildInfoLabel("💱 Devises", countryInfo.devises),
                            buildInfoLabel("🕐 Fuseau", countryInfo.fuseauxHoraires)
                    );
                } else {
                    Label noData = new Label("❌ Données pays non disponibles");
                    noData.setStyle("-fx-text-fill: #ec4899; -fx-font-size: 11;");
                    countrySection.getChildren().add(noData);
                }

                qrSection.getChildren().remove(qrLoading);
                if (finalQr != null) {
                    ImageView qrView = new ImageView(finalQr);
                    qrView.setFitWidth(180);
                    qrView.setFitHeight(180);
                    qrView.setPreserveRatio(true);
                    qrSection.getChildren().add(qrView);
                }

                if (photoInfo != null && !photoInfo.urlRegular.isEmpty()) {
                    try {
                        VBox photoSection = new VBox(8);
                        photoSection.setPadding(new Insets(8, 24, 8, 24));

                        Label photoTitle = new Label("📸 Photo de la destination");
                        photoTitle.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #ff8c42;");

                        ImageView photoView = new ImageView(new Image(photoInfo.urlSmall, 500, 200, true, true, false));
                        photoView.setFitWidth(500);
                        photoView.setPreserveRatio(true);
                        photoView.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 10, 0, 0, 2);");

                        Label credit = new Label(photoInfo.getCredit());
                        credit.setStyle("-fx-font-size: 9; -fx-text-fill: #64748b;");

                        photoSection.getChildren().addAll(photoTitle, photoView, credit);

                        int footerIdx = root.getChildren().indexOf(footer);
                        root.getChildren().add(footerIdx, photoSection);
                    } catch (Exception e) {
                        System.out.println("⚠️ Photo non chargée: " + e.getMessage());
                    }
                }
            });
        }).start();
    }

    private VBox buildStatCard(String icon, String label, String value) {
        VBox card = new VBox(2);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(8, 14, 8, 14));
        card.setStyle("-fx-background-color: #1e2749; -fx-background-radius: 10;");

        Label iconLbl = new Label(icon);
        iconLbl.setStyle("-fx-font-size: 16;");
        Label labelLbl = new Label(label);
        labelLbl.setStyle("-fx-font-size: 9; -fx-text-fill: #94a3b8; -fx-font-weight: 600;");
        Label valueLbl = new Label(value != null ? value : "N/A");
        valueLbl.setStyle("-fx-font-size: 11; -fx-text-fill: #ffffff; -fx-font-weight: 500;");

        card.getChildren().addAll(iconLbl, labelLbl, valueLbl);
        return card;
    }

    private HBox buildInfoLabel(String label, String value) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(3, 10, 3, 10));
        row.setStyle("-fx-background-color: #1e2749; -fx-background-radius: 8;");

        Label lbl = new Label(label);
        lbl.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11; -fx-min-width: 120;");
        Label val = new Label(value != null && !value.isEmpty() ? value : "N/A");
        val.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 11;");
        val.setWrapText(true);

        row.getChildren().addAll(lbl, val);
        return row;
    }

    private void remplirFormulaire(Voyage voyage) {
        this.voyageSelectionne = voyage;
        tfTitre.setText(voyage.getTitre_voyage());
        dpDateDebut.setValue(voyage.getDate_debut().toLocalDate());
        dpDateFin.setValue(voyage.getDate_fin().toLocalDate());
        cbStatut.setValue(voyage.getStatut());
        tfIdDestination.setText(String.valueOf(voyage.getId_destination()));
    }

    private void supprimerVoyage(Voyage voyage) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText(null);
        alert.setContentText("Voulez-vous vraiment supprimer le voyage \"" + voyage.getTitre_voyage() + "\" ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                voyageCRUD.supprimer(voyage.getId_voyage());
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Voyage supprimé avec succès!");
                chargerVoyages();
                resetFormulaire();
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la suppression: " + e.getMessage());
            }
        }
    }

    private void ouvrirParticipants(Voyage voyage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/PageParticipationBack.fxml"));
            Parent root = loader.load();

            ParticipationBackController participationController = loader.getController();

            String nomDest = DEST_INCONNUE;
            try {
                nomDest = voyageCRUD.getNomDestination(voyage.getId_destination());
            } catch (SQLException e) {
                e.printStackTrace();
            }

            String dates = voyage.getDate_debut().toLocalDate() + " - " + voyage.getDate_fin().toLocalDate();

            participationController.initData(
                    voyage.getId_voyage(),
                    voyage.getTitre_voyage(),
                    nomDest,
                    dates
            );

            Stage stage = (Stage) tableVoyages.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la page des participants: " + e.getMessage());
        }
    }

    private boolean validerFormulaire() {
        String titre = tfTitre.getText() != null ? tfTitre.getText().trim() : "";
        if (titre.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Le titre est requis");
            return false;
        }
        if (titre.length() < 3) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Le titre doit contenir au moins 3 caractères");
            return false;
        }
        if (titre.length() > 100) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Le titre ne peut pas dépasser 100 caractères");
            return false;
        }
        tfTitre.setText(titre);

        if (dpDateDebut.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "La date de début est requise");
            return false;
        }
        if (dpDateFin.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "La date de fin est requise");
            return false;
        }
        if (tfIdDestination.getText() == null || tfIdDestination.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "L'ID de destination est requis");
            return false;
        }

        if (dpDateFin.getValue().isBefore(dpDateDebut.getValue())) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "La date de fin doit être après la date de début");
            return false;
        }
        if (dpDateFin.getValue().isEqual(dpDateDebut.getValue())) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "La date de fin doit être différente de la date de début");
            return false;
        }

        try {
            int idDest = Integer.parseInt(tfIdDestination.getText().trim());
            if (idDest <= 0) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "L'ID de destination doit être un nombre positif");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "L'ID de destination doit être un nombre");
            return false;
        }

        return true;
    }

    private void resetFormulaire() {
        tfTitre.clear();
        dpDateDebut.setValue(null);
        dpDateFin.setValue(null);
        cbStatut.setValue(STATUT_A_VENIR);
        tfIdDestination.clear();
        tfDestinationNom.clear();
        voyageSelectionne = null;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Statistiques détaillées
    private void mettreAJourStatistiquesDetaillees() {
        if (voyageList != null) {
            int total = voyageList.size();
            long aVenir = voyageList.stream().filter(v -> STATUT_A_VENIR.equals(v.getStatut())).count();
            long enCours = voyageList.stream().filter(v -> STATUT_EN_COURS.equals(v.getStatut())).count();
            long termine = voyageList.stream().filter(v -> STATUT_TERMINE.equals(v.getStatut())).count();
            long annule = voyageList.stream().filter(v -> STATUT_ANNULE.equals(v.getStatut())).count();

            // Mettre à jour les labels
            if (lblCountAVenir != null) lblCountAVenir.setText(String.valueOf(aVenir));
            if (lblCountEnCours != null) lblCountEnCours.setText(String.valueOf(enCours));
            if (lblCountTermine != null) lblCountTermine.setText(String.valueOf(termine));
            if (lblCountAnnule != null) lblCountAnnule.setText(String.valueOf(annule));
            if (lblTotalVoyages2 != null) lblTotalVoyages2.setText(String.valueOf(total));

            // Calculer et afficher les pourcentages + progress bars
            if (total > 0) {
                double pctAVenir = aVenir * 1.0 / total;
                double pctEnCours = enCours * 1.0 / total;
                double pctTermine = termine * 1.0 / total;
                double pctAnnule = annule * 1.0 / total;

                if (lblPourcentAVenir != null) lblPourcentAVenir.setText(String.format(PERCENT_FORMAT, pctAVenir * 100));
                if (lblPourcentEnCours != null) lblPourcentEnCours.setText(String.format(PERCENT_FORMAT, pctEnCours * 100));
                if (lblPourcentTermine != null) lblPourcentTermine.setText(String.format(PERCENT_FORMAT, pctTermine * 100));
                if (lblPourcentAnnule != null) lblPourcentAnnule.setText(String.format(PERCENT_FORMAT, pctAnnule * 100));

                if (pbAVenir != null) pbAVenir.setProgress(pctAVenir);
                if (pbEnCours != null) pbEnCours.setProgress(pctEnCours);
                if (pbTermine != null) pbTermine.setProgress(pctTermine);
                if (pbAnnule != null) pbAnnule.setProgress(pctAnnule);
            } else {
                if (lblPourcentAVenir != null) lblPourcentAVenir.setText("(0.0%)");
                if (lblPourcentEnCours != null) lblPourcentEnCours.setText("(0.0%)");
                if (lblPourcentTermine != null) lblPourcentTermine.setText("(0.0%)");
                if (lblPourcentAnnule != null) lblPourcentAnnule.setText("(0.0%)");

                if (pbAVenir != null) pbAVenir.setProgress(0);
                if (pbEnCours != null) pbEnCours.setProgress(0);
                if (pbTermine != null) pbTermine.setProgress(0);
                if (pbAnnule != null) pbAnnule.setProgress(0);
            }

            // Mettre à jour le PieChart
            if (pieChartStatuts != null) {
                ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                        new PieChart.Data("À venir (" + aVenir + ")", aVenir),
                        new PieChart.Data("En cours (" + enCours + ")", enCours),
                        new PieChart.Data("Terminé (" + termine + ")", termine),
                        new PieChart.Data("Annulé (" + annule + ")", annule)
                );

                pieChartStatuts.setData(pieChartData);
            }
        }
    }

    @FXML
    private void exportToPDF(ActionEvent event) {
        try {
            System.out.println("=== DÉBUT EXPORT PDF ===");

            ObservableList<Voyage> voyagesToExport;
            if (filteredData != null && !filteredData.isEmpty()) {
                voyagesToExport = FXCollections.observableArrayList(filteredData);
                System.out.println("Export des données filtrées: " + voyagesToExport.size() + " voyages");
            } else {
                voyagesToExport = voyageList;
                System.out.println("Export de toutes les données: " + voyagesToExport.size() + " voyages");
            }

            if (!voyagesToExport.isEmpty()) {
                System.out.println("Appel à ExportService.exportToPDF...");

                ExportService.exportToPDF(
                        voyagesToExport,
                        (Stage) tableVoyages.getScene().getWindow(),
                        "Liste des voyages - TravelMate"
                );

                System.out.println("Export PDF terminé avec succès");
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Export PDF terminé avec succès!");
            } else {
                System.out.println("Aucune donnée à exporter");
                showAlert(Alert.AlertType.WARNING, "Attention", "Aucune donnée à exporter");
            }

        } catch (Exception e) {
            System.err.println("=== ERREUR EXPORT PDF ===");
            System.err.println("Type d'erreur: " + e.getClass().getName());
            System.err.println("Message: " + e.getMessage());
            e.printStackTrace();

            String errorMessage = e.getMessage();
            if (errorMessage == null) {
                errorMessage = "Erreur inconnue";
            }
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'export PDF: " + errorMessage);
        }
    }

    @FXML
    private void exportToExcel(ActionEvent event) {
        try {
            System.out.println("=== DÉBUT EXPORT EXCEL ===");

            ObservableList<Voyage> voyagesToExport;
            if (filteredData != null && !filteredData.isEmpty()) {
                voyagesToExport = FXCollections.observableArrayList(filteredData);
                System.out.println("Export des données filtrées: " + voyagesToExport.size() + " voyages");
            } else {
                voyagesToExport = voyageList;
                System.out.println("Export de toutes les données: " + voyagesToExport.size() + " voyages");
            }

            if (!voyagesToExport.isEmpty()) {
                System.out.println("Appel à ExportService.exportToExcel...");

                ExportService.exportToExcel(
                        voyagesToExport,
                        (Stage) tableVoyages.getScene().getWindow()
                );

                System.out.println("Export Excel terminé avec succès");
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Export Excel terminé avec succès!");
            } else {
                System.out.println("Aucune donnée à exporter");
                showAlert(Alert.AlertType.WARNING, "Attention", "Aucune donnée à exporter");
            }

        } catch (Exception e) {
            System.err.println("=== ERREUR EXPORT EXCEL ===");
            System.err.println("Type d'erreur: " + e.getClass().getName());
            System.err.println("Message: " + e.getMessage());
            e.printStackTrace();

            String errorMessage = e.getMessage();
            if (errorMessage == null) {
                errorMessage = "Erreur inconnue";
            }
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'export Excel: " + errorMessage);
        }
    }
}