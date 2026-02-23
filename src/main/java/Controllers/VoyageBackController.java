package Controllers;

import Entities.Voyage;
import Services.VoyageCRUD;
import Services.ExportService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class VoyageBackController implements Initializable {

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

    // Nouveaux champs pour la recherche
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

    @FXML
    private Button btnAjouter;
    @FXML
    private Button btnModifier;
    @FXML
    private Button btnAnnuler;
    @FXML
    private Button btnRefresh;

    // Statistiques
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

    // Boutons d'export
    @FXML
    private Button btnExportPDF;
    @FXML
    private Button btnExportExcel;

    private VoyageCRUD voyageCRUD = new VoyageCRUD();
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
                return new javafx.beans.property.SimpleStringProperty(nomDest);
            } catch (SQLException e) {
                return new javafx.beans.property.SimpleStringProperty("Inconnue");
            }
        });

        // Ajouter les boutons d'action
        ajouterBoutonsActions();

        // Initialiser les ComboBox des statuts
        cbStatut.getItems().addAll("a venir", "En cours", "Terminé", "Annulé");
        cbStatut.setValue("a venir");

        cbSearchStatut.getItems().addAll("Tous", "a venir", "En cours", "Terminé", "Annulé");
        cbSearchStatut.setValue("Tous");

        // Charger les données
        chargerVoyages();

        // Configurer le tri des colonnes
        configurerTri();

        // Configurer la recherche
        configurerRecherche();

        // Configurer l'écouteur de changement d'ID destination
        tfIdDestination.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) {
                try {
                    int idDest = Integer.parseInt(newVal);
                    String nomDest = voyageCRUD.getNomDestination(idDest);
                    tfDestinationNom.setText(nomDest);
                } catch (NumberFormatException | SQLException e) {
                    tfDestinationNom.setText("Destination inconnue");
                }
            } else {
                tfDestinationNom.setText("");
            }
        });

        // Configurer les boutons
        btnAjouter.setOnAction(this::ajouterVoyage);
        btnModifier.setOnAction(this::modifierVoyage);
        btnAnnuler.setOnAction(this::annulerModification);
        btnRefresh.setOnAction(this::refreshTable);
        btnSearch.setOnAction(this::rechercherVoyages);
        btnReset.setOnAction(this::resetRecherche);

        // Configurer les boutons d'export (vérifier qu'ils ne sont pas null)
        if (btnExportPDF != null) {
            btnExportPDF.setOnAction(this::exportToPDF);
        }
        if (btnExportExcel != null) {
            btnExportExcel.setOnAction(this::exportToExcel);
        }

        // Initialiser les statistiques
        mettreAJourStatistiquesDetaillees();
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

    // Configuration du tri des colonnes
    private void configurerTri() {
        // Permettre le tri multiple
        tableVoyages.getSortOrder().addListener((javafx.collections.ListChangeListener.Change<? extends TableColumn<Voyage, ?>> c) -> {
            while (c.next()) {
                if (c.wasAdded() && sortedData != null) {
                    sortedData.comparatorProperty().bind(tableVoyages.comparatorProperty());
                }
            }
        });

        // Tri par défaut sur date_debut (descendant)
        colDateDebut.setSortType(TableColumn.SortType.DESCENDING);
        tableVoyages.getSortOrder().add(colDateDebut);
    }

    // Configuration de la recherche
    private void configurerRecherche() {
        // Créer une liste filtrée
        filteredData = new FilteredList<>(voyageList, p -> true);

        // Lier la liste triée à la liste filtrée
        sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableVoyages.comparatorProperty());

        // Ajouter les données triées au tableau
        tableVoyages.setItems(sortedData);

        // Mettre à jour le compteur quand les données changent
        sortedData.addListener((javafx.collections.ListChangeListener<Voyage>) c -> {
            lblVoyagesCount.setText(String.valueOf(sortedData.size()));
            mettreAJourStatistiques();
            mettreAJourStatistiquesDetaillees();
        });
    }

    // Méthode de recherche
    @FXML
    private void rechercherVoyages(ActionEvent event) {
        String titre = tfSearchTitre.getText().toLowerCase();
        String idText = tfSearchId.getText();
        String statut = cbSearchStatut.getValue();

        filteredData.setPredicate(voyage -> {
            // Si tous les champs sont vides, afficher tout
            if ((titre == null || titre.isEmpty()) &&
                    (idText == null || idText.isEmpty()) &&
                    (statut == null || statut.equals("Tous"))) {
                return true;
            }

            // Filtre par ID
            if (idText != null && !idText.isEmpty()) {
                try {
                    int id = Integer.parseInt(idText);
                    if (voyage.getId_voyage() != id) {
                        return false;
                    }
                } catch (NumberFormatException e) {
                    return false;
                }
            }

            // Filtre par titre
            if (titre != null && !titre.isEmpty()) {
                if (!voyage.getTitre_voyage().toLowerCase().contains(titre)) {
                    return false;
                }
            }

            // Filtre par statut
            if (statut != null && !statut.equals("Tous")) {
                if (!voyage.getStatut().equals(statut)) {
                    return false;
                }
            }

            return true;
        });

        // Mettre à jour les statistiques
        mettreAJourStatistiques();
        mettreAJourStatistiquesDetaillees();
    }

    // Réinitialiser la recherche
    @FXML
    private void resetRecherche(ActionEvent event) {
        tfSearchTitre.clear();
        tfSearchId.clear();
        cbSearchStatut.setValue("Tous");

        // Réinitialiser le filtre
        filteredData.setPredicate(null);

        // Recharger les données
        chargerVoyages();
    }

    // Mettre à jour les statistiques en fonction des données filtrées
    private void mettreAJourStatistiques() {
        if (sortedData != null) {
            lblTotalVoyages.setText(String.valueOf(sortedData.size()));

            long actifs = sortedData.stream()
                    .filter(v -> "En cours".equals(v.getStatut()) || "a venir".equals(v.getStatut()))
                    .count();
            lblVoyagesActifs.setText(String.valueOf(actifs));

            long termines = sortedData.stream()
                    .filter(v -> "Terminé".equals(v.getStatut()))
                    .count();
            lblVoyagesTermines.setText(String.valueOf(termines));
        }
    }

    // Charger les voyages
    private void chargerVoyages() {
        try {
            List<Voyage> voyages = voyageCRUD.afficher();
            voyageList.clear();
            voyageList.addAll(voyages);

            // Mettre à jour les statistiques
            lblTotalVoyages.setText(String.valueOf(voyages.size()));
            lblVoyagesCount.setText(String.valueOf(voyages.size()));

            long actifs = voyages.stream()
                    .filter(v -> "En cours".equals(v.getStatut()) || "a venir".equals(v.getStatut()))
                    .count();
            lblVoyagesActifs.setText(String.valueOf(actifs));

            long termines = voyages.stream()
                    .filter(v -> "Terminé".equals(v.getStatut()))
                    .count();
            lblVoyagesTermines.setText(String.valueOf(termines));

            // Mettre à jour les statistiques détaillées
            mettreAJourStatistiquesDetaillees();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les voyages: " + e.getMessage());
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

    private void voirVoyage(Voyage voyage) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails du voyage");
        alert.setHeaderText(voyage.getTitre_voyage());

        String nomDest = "Inconnue";
        try {
            nomDest = voyageCRUD.getNomDestination(voyage.getId_destination());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        alert.setContentText(
                "ID: " + voyage.getId_voyage() + "\n" +
                        "Titre: " + voyage.getTitre_voyage() + "\n" +
                        "Date début: " + voyage.getDate_debut() + "\n" +
                        "Date fin: " + voyage.getDate_fin() + "\n" +
                        "Statut: " + voyage.getStatut() + "\n" +
                        "Destination: " + nomDest + " (ID: " + voyage.getId_destination() + ")"
        );
        alert.showAndWait();
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

            String nomDest = "Inconnue";
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
        if (tfTitre.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Le titre est requis");
            return false;
        }
        if (dpDateDebut.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "La date de début est requise");
            return false;
        }
        if (dpDateFin.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "La date de fin est requise");
            return false;
        }
        if (tfIdDestination.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "L'ID de destination est requis");
            return false;
        }

        if (dpDateFin.getValue().isBefore(dpDateDebut.getValue())) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "La date de fin doit être après la date de début");
            return false;
        }

        try {
            Integer.parseInt(tfIdDestination.getText());
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
        cbStatut.setValue("a venir");
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
            long aVenir = voyageList.stream().filter(v -> "a venir".equals(v.getStatut())).count();
            long enCours = voyageList.stream().filter(v -> "En cours".equals(v.getStatut())).count();
            long termine = voyageList.stream().filter(v -> "Terminé".equals(v.getStatut())).count();
            long annule = voyageList.stream().filter(v -> "Annulé".equals(v.getStatut())).count();

            // Mettre à jour les labels
            lblCountAVenir.setText(String.valueOf(aVenir));
            lblCountEnCours.setText(String.valueOf(enCours));
            lblCountTermine.setText(String.valueOf(termine));
            lblCountAnnule.setText(String.valueOf(annule));
            lblTotalVoyages2.setText(String.valueOf(total));

            // Calculer et afficher les pourcentages
            if (total > 0) {
                lblPourcentAVenir.setText(String.format("(%.1f%%)", (aVenir * 100.0 / total)));
                lblPourcentEnCours.setText(String.format("(%.1f%%)", (enCours * 100.0 / total)));
                lblPourcentTermine.setText(String.format("(%.1f%%)", (termine * 100.0 / total)));
                lblPourcentAnnule.setText(String.format("(%.1f%%)", (annule * 100.0 / total)));
            }

            // Mettre à jour le PieChart
            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                    new PieChart.Data("À venir (" + aVenir + ")", aVenir),
                    new PieChart.Data("En cours (" + enCours + ")", enCours),
                    new PieChart.Data("Terminé (" + termine + ")", termine),
                    new PieChart.Data("Annulé (" + annule + ")", annule)
            );

            pieChartStatuts.setData(pieChartData);

            // Personnaliser les couleurs du PieChart
            pieChartData.forEach(data -> {
                if (data.getName().startsWith("À venir")) {
                    data.getNode().setStyle("-fx-pie-color: #ff8c42;");
                } else if (data.getName().startsWith("En cours")) {
                    data.getNode().setStyle("-fx-pie-color: #10b981;");
                } else if (data.getName().startsWith("Terminé")) {
                    data.getNode().setStyle("-fx-pie-color: #64748b;");
                } else if (data.getName().startsWith("Annulé")) {
                    data.getNode().setStyle("-fx-pie-color: #ec4899;");
                }
            });
        }
    }

    // Export PDF
    @FXML
    private void exportToPDF(ActionEvent event) {
        try {
            ObservableList<Voyage> voyagesToExport;
            if (filteredData != null && !filteredData.isEmpty()) {
                voyagesToExport = FXCollections.observableArrayList(filteredData);
            } else {
                voyagesToExport = voyageList;
            }

            if (!voyagesToExport.isEmpty()) {
                ExportService.exportToPDF(
                        voyagesToExport,
                        (Stage) tableVoyages.getScene().getWindow(),
                        "Liste des voyages - TravelMate"
                );
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Export PDF terminé avec succès!");
            } else {
                showAlert(Alert.AlertType.WARNING, "Attention", "Aucune donnée à exporter");
            }

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'export PDF: " + e.getMessage());
        }
    }

    // Export Excel
    @FXML
    private void exportToExcel(ActionEvent event) {
        try {
            ObservableList<Voyage> voyagesToExport;
            if (filteredData != null && !filteredData.isEmpty()) {
                voyagesToExport = FXCollections.observableArrayList(filteredData);
            } else {
                voyagesToExport = voyageList;
            }

            if (!voyagesToExport.isEmpty()) {
                ExportService.exportToExcel(
                        voyagesToExport,
                        (Stage) tableVoyages.getScene().getWindow()
                );
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Export Excel terminé avec succès!");
            } else {
                showAlert(Alert.AlertType.WARNING, "Attention", "Aucune donnée à exporter");
            }

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'export Excel: " + e.getMessage());
        }
    }
}