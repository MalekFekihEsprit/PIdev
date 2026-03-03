package Controllers.ItineraireEtEtape;

import Entities.Itineraire;
import Entities.etape;
import Services.etapeCRUD;
import Services.itineraireCRUD;
import Utils.MyBD;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class GestionItinerairesController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> comboDestination;
    @FXML private DatePicker dateDebut;
    @FXML private DatePicker dateFin;
    @FXML private Button btnFiltrer;
    @FXML private Button btnReinitialiser;
    @FXML private Button btnNouvelItineraire;
    @FXML private Button btnNouvelleEtape;

    // Labels pour les statistiques
    @FXML private Label lblTotalItineraires;
    @FXML private Label lblTotalEtapes;
    @FXML private Label statsTotalItineraires;
    @FXML private Label statsTotalEtapes;
    @FXML private Label statsTotalDestinations;
    @FXML private Label kpiItineraires;
    @FXML private Label kpiEtapes;
    @FXML private Label kpiDestinations;

    // Table Itinéraires
    @FXML private TableView<Itineraire> tableItineraires;
    @FXML private TableColumn<Itineraire, Integer> colId;
    @FXML private TableColumn<Itineraire, String> colNom;
    @FXML private TableColumn<Itineraire, String> colDescription;
    @FXML private TableColumn<Itineraire, String> colVoyage;
    @FXML private TableColumn<Itineraire, String> colDestination;
    @FXML private TableColumn<Itineraire, Integer> colNbEtapes;
    @FXML private TableColumn<Itineraire, Void> colActions;

    // Table Étapes
    @FXML private TableView<EtapeTableModel> tableEtapes;
    @FXML private TableColumn<EtapeTableModel, Integer> colEtapeId;
    @FXML private TableColumn<EtapeTableModel, String> colEtapeHeure;
    @FXML private TableColumn<EtapeTableModel, String> colEtapeDescription;
    @FXML private TableColumn<EtapeTableModel, String> colEtapeActivite;
    @FXML private TableColumn<EtapeTableModel, String> colEtapeItineraire;
    @FXML private TableColumn<EtapeTableModel, String> colEtapeLieu;
    @FXML private TableColumn<EtapeTableModel, Float> colEtapeDuree;
    @FXML private TableColumn<EtapeTableModel, Void> colEtapeActions;

    private itineraireCRUD itineraireCRUD = new itineraireCRUD();
    private etapeCRUD etapeCRUD = new etapeCRUD();

    private ObservableList<Itineraire> itinerairesList = FXCollections.observableArrayList();
    private ObservableList<EtapeTableModel> etapesList = FXCollections.observableArrayList();
    private FilteredList<Itineraire> filteredItineraires;
    private FilteredList<EtapeTableModel> filteredEtapes;

    @FXML
    public void initialize() {
        System.out.println("Initialisation du contrôleur de gestion");

        // Vérification que tous les composants FXML sont injectés
        verifyFXMLComponents();

        setupTables();
        loadDestinations();
        loadItineraires();
        loadEtapes();
        setupSearchAndFilters();
        setupButtons();
        updateStats();
    }

    private void verifyFXMLComponents() {
        System.out.println("Vérification des composants FXML:");
        System.out.println("lblTotalItineraires: " + (lblTotalItineraires != null ? "OK" : "null"));
        System.out.println("lblTotalEtapes: " + (lblTotalEtapes != null ? "OK" : "null"));
        System.out.println("statsTotalItineraires: " + (statsTotalItineraires != null ? "OK" : "null"));
        System.out.println("statsTotalEtapes: " + (statsTotalEtapes != null ? "OK" : "null"));
        System.out.println("statsTotalDestinations: " + (statsTotalDestinations != null ? "OK" : "null"));
        System.out.println("kpiItineraires: " + (kpiItineraires != null ? "OK" : "null"));
        System.out.println("kpiEtapes: " + (kpiEtapes != null ? "OK" : "null"));
        System.out.println("kpiDestinations: " + (kpiDestinations != null ? "OK" : "null"));
        System.out.println("btnNouvelItineraire: " + (btnNouvelItineraire != null ? "OK" : "null"));
        System.out.println("btnNouvelleEtape: " + (btnNouvelleEtape != null ? "OK" : "null"));
        System.out.println("btnFiltrer: " + (btnFiltrer != null ? "OK" : "null"));
        System.out.println("btnReinitialiser: " + (btnReinitialiser != null ? "OK" : "null"));
    }

    private void setupTables() {
        try {
            // Configuration table itinéraires
            if (colId != null) colId.setCellValueFactory(new PropertyValueFactory<>("id_itineraire"));
            if (colNom != null) colNom.setCellValueFactory(new PropertyValueFactory<>("nom_itineraire"));
            if (colDescription != null) colDescription.setCellValueFactory(new PropertyValueFactory<>("description_itineraire"));

            if (colVoyage != null) {
                colVoyage.setCellValueFactory(cellData -> {
                    String voyage = getVoyageNameById(cellData.getValue().getId_voyage());
                    return new SimpleStringProperty(voyage);
                });
            }

            if (colDestination != null) {
                colDestination.setCellValueFactory(cellData -> {
                    String destination = getDestinationByVoyageId(cellData.getValue().getId_voyage());
                    return new SimpleStringProperty(destination);
                });
            }

            if (colNbEtapes != null) {
                colNbEtapes.setCellValueFactory(cellData -> {
                    try {
                        int count = etapeCRUD.getEtapesByItineraire(cellData.getValue().getId_itineraire()).size();
                        return new SimpleIntegerProperty(count).asObject();
                    } catch (SQLException e) {
                        e.printStackTrace();
                        return new SimpleIntegerProperty(0).asObject();
                    }
                });
            }

            if (colActions != null) colActions.setCellFactory(createItineraryActionButtons());

            // Configuration table étapes
            if (colEtapeId != null) colEtapeId.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getId()).asObject());
            if (colEtapeHeure != null) colEtapeHeure.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getHeure()));
            if (colEtapeDescription != null) colEtapeDescription.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDescription()));
            if (colEtapeActivite != null) colEtapeActivite.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getActivite()));
            if (colEtapeItineraire != null) colEtapeItineraire.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getItineraire()));
            if (colEtapeLieu != null) colEtapeLieu.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getLieu()));
            if (colEtapeDuree != null) colEtapeDuree.setCellValueFactory(cellData -> new javafx.beans.property.SimpleFloatProperty(cellData.getValue().getDuree()).asObject());

            if (colEtapeActions != null) colEtapeActions.setCellFactory(createEtapeActionButtons());
        } catch (Exception e) {
            System.err.println("Erreur dans setupTables: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Callback<TableColumn<Itineraire, Void>, TableCell<Itineraire, Void>> createItineraryActionButtons() {
        return new Callback<>() {
            @Override
            public TableCell<Itineraire, Void> call(final TableColumn<Itineraire, Void> param) {
                return new TableCell<>() {
                    private final Button btnEdit = new Button("✏️");
                    private final Button btnDelete = new Button("🗑️");
                    private final HBox pane = new HBox(5, btnEdit, btnDelete);

                    {
                        btnEdit.setStyle("-fx-background-color: #ff8c42; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand;");
                        btnDelete.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand;");

                        btnEdit.setTooltip(new Tooltip("Modifier"));
                        btnDelete.setTooltip(new Tooltip("Supprimer"));

                        btnEdit.setOnAction(event -> {
                            Itineraire itineraire = getTableView().getItems().get(getIndex());
                            modifierItineraire(itineraire);
                        });

                        btnDelete.setOnAction(event -> {
                            Itineraire itineraire = getTableView().getItems().get(getIndex());
                            supprimerItineraire(itineraire);
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
    }
    // ============== MÉTHODES DE NAVIGATION ==============

    @FXML
    private void handleDashboardClick() {
        // Naviguer vers le dashboard (à implémenter selon votre structure)
        showInfoAlert("Dashboard", "Fonctionnalité à venir");
    }

    @FXML
    private void handleDestinationsClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DestinationBack.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) tableItineraires.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TravelMate - Gestion des Destinations");
            stage.setMaximized(true);
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir les destinations: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleHebergementClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/HebergementBack.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) tableItineraires.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TravelMate - Gestion des Hébergements");
            stage.setMaximized(true);
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir les hébergements: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleActivitesClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/activitesback.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) tableItineraires.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TravelMate - Gestion des Activités");
            stage.setMaximized(true);
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir les activités: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCategoriesClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/categoriesback.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) tableItineraires.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TravelMate - Gestion des Catégories");
            stage.setMaximized(true);
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir les catégories: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleVoyagesClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/PageVoyageBack.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) tableItineraires.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TravelMate - Gestion des Voyages");
            stage.setMaximized(true);
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir les voyages: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBudgetsClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/BudgetDepenseBack.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) tableItineraires.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TravelMate - Gestion des Budgets");
            stage.setMaximized(true);
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir la gestion des budgets: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleUsersClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin_users.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) tableItineraires.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TravelMate - Gestion des Utilisateurs");
            stage.setMaximized(true);
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir la gestion des utilisateurs: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleStatsClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin_stats.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) tableItineraires.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TravelMate - Statistiques");
            stage.setMaximized(true);
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir les statistiques: " + e.getMessage());
            e.printStackTrace();
        }
    }
    public void refreshAfterModification() {
        loadItineraires();
        loadEtapes();
        updateStats();
        System.out.println("✅ Données rafraîchies après modification");
    }

    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private Callback<TableColumn<EtapeTableModel, Void>, TableCell<EtapeTableModel, Void>> createEtapeActionButtons() {
        return new Callback<>() {
            @Override
            public TableCell<EtapeTableModel, Void> call(final TableColumn<EtapeTableModel, Void> param) {
                return new TableCell<>() {
                    private final Button btnEdit = new Button("✏️");
                    private final Button btnDelete = new Button("🗑️");
                    private final HBox pane = new HBox(5, btnEdit, btnDelete);

                    {
                        btnEdit.setStyle("-fx-background-color: #ff8c42; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand;");
                        btnDelete.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand;");

                        btnEdit.setTooltip(new Tooltip("Modifier"));
                        btnDelete.setTooltip(new Tooltip("Supprimer"));

                        btnEdit.setOnAction(event -> {
                            EtapeTableModel etape = getTableView().getItems().get(getIndex());
                            modifierEtape(etape);
                        });

                        btnDelete.setOnAction(event -> {
                            EtapeTableModel etape = getTableView().getItems().get(getIndex());
                            supprimerEtape(etape);
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
    }

    private void setupButtons() {
        try {
            if (btnNouvelItineraire != null) {
                btnNouvelItineraire.setOnAction(e -> ouvrirFormulaireItineraire(null));
            }
            if (btnNouvelleEtape != null) {
                btnNouvelleEtape.setOnAction(e -> ouvrirFormulaireEtape(null));
            }
            if (btnFiltrer != null) {
                btnFiltrer.setOnAction(e -> filtrer());
            }
            if (btnReinitialiser != null) {
                btnReinitialiser.setOnAction(e -> reinitialiserFiltres());
            }
        } catch (Exception e) {
            System.err.println("Erreur dans setupButtons: " + e.getMessage());
        }
    }

    private void setupSearchAndFilters() {
        if (itinerairesList == null || etapesList == null) return;

        filteredItineraires = new FilteredList<>(itinerairesList, p -> true);
        filteredEtapes = new FilteredList<>(etapesList, p -> true);

        if (tableItineraires != null) {
            SortedList<Itineraire> sortedItineraires = new SortedList<>(filteredItineraires);
            sortedItineraires.comparatorProperty().bind(tableItineraires.comparatorProperty());
            tableItineraires.setItems(sortedItineraires);
        }

        if (tableEtapes != null) {
            SortedList<EtapeTableModel> sortedEtapes = new SortedList<>(filteredEtapes);
            sortedEtapes.comparatorProperty().bind(tableEtapes.comparatorProperty());
            tableEtapes.setItems(sortedEtapes);
        }

        if (searchField != null) {
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (filteredItineraires != null) {
                    filteredItineraires.setPredicate(itineraire -> {
                        if (newValue == null || newValue.isEmpty()) return true;
                        String lowerCaseFilter = newValue.toLowerCase();
                        return itineraire.getNom_itineraire().toLowerCase().contains(lowerCaseFilter) ||
                                (itineraire.getDescription_itineraire() != null &&
                                        itineraire.getDescription_itineraire().toLowerCase().contains(lowerCaseFilter));
                    });
                }

                if (filteredEtapes != null) {
                    filteredEtapes.setPredicate(etape -> {
                        if (newValue == null || newValue.isEmpty()) return true;
                        String lowerCaseFilter = newValue.toLowerCase();
                        return etape.getDescription().toLowerCase().contains(lowerCaseFilter) ||
                                etape.getActivite().toLowerCase().contains(lowerCaseFilter);
                    });
                }
            });
        }
    }

    private void loadDestinations() {
        if (comboDestination == null) return;

        comboDestination.getItems().clear();
        comboDestination.getItems().add("Toutes les destinations");

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = MyBD.getInstance().getConn();
            stmt = conn.prepareStatement("SELECT DISTINCT nom_destination FROM destination");
            rs = stmt.executeQuery();

            while (rs.next()) {
                comboDestination.getItems().add(rs.getString("nom_destination"));
            }

            comboDestination.getSelectionModel().selectFirst();

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadItineraires() {
        try {
            itinerairesList.clear();
            itinerairesList.addAll(itineraireCRUD.afficher());
            updateStats();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les itinéraires: " + e.getMessage());
        }
    }

    private void loadEtapes() {
        try {
            etapesList.clear();
            List<etape> etapes = etapeCRUD.afficher();
            for (etape e : etapes) {
                etapesList.add(new EtapeTableModel(e));
            }
            updateStats();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les étapes: " + e.getMessage());
        }
    }

    private void updateStats() {
        try {
            int totalItineraires = itinerairesList != null ? itinerairesList.size() : 0;
            int totalEtapes = etapesList != null ? etapesList.size() : 0;
            int totalDestinations = countDestinations();

            if (lblTotalItineraires != null) {
                lblTotalItineraires.setText("Total itinéraires: " + totalItineraires);
            }
            if (lblTotalEtapes != null) {
                lblTotalEtapes.setText("Total étapes: " + totalEtapes);
            }
            if (statsTotalItineraires != null) {
                statsTotalItineraires.setText(String.valueOf(totalItineraires));
            }
            if (statsTotalEtapes != null) {
                statsTotalEtapes.setText(String.valueOf(totalEtapes));
            }
            if (statsTotalDestinations != null) {
                statsTotalDestinations.setText(String.valueOf(totalDestinations));
            }
            if (kpiItineraires != null) {
                kpiItineraires.setText(String.valueOf(totalItineraires));
            }
            if (kpiEtapes != null) {
                kpiEtapes.setText(String.valueOf(totalEtapes));
            }
            if (kpiDestinations != null) {
                kpiDestinations.setText(String.valueOf(totalDestinations));
            }
        } catch (Exception e) {
            System.err.println("Erreur dans updateStats: " + e.getMessage());
        }
    }

    private int countDestinations() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = MyBD.getInstance().getConn();
            stmt = conn.prepareStatement("SELECT COUNT(*) as count FROM destination");
            rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    private void filtrer() {
        if (comboDestination == null || filteredItineraires == null) return;

        String destination = comboDestination.getValue();

        filteredItineraires.setPredicate(itineraire -> {
            if (destination != null && !destination.equals("Toutes les destinations")) {
                String itineraireDestination = getDestinationByVoyageId(itineraire.getId_voyage());
                if (!destination.equals(itineraireDestination)) return false;
            }
            return true;
        });
    }

    private void reinitialiserFiltres() {
        if (comboDestination != null) comboDestination.getSelectionModel().selectFirst();
        if (dateDebut != null) dateDebut.setValue(null);
        if (dateFin != null) dateFin.setValue(null);
        if (searchField != null) searchField.clear();
        if (filteredItineraires != null) filteredItineraires.setPredicate(p -> true);
        if (filteredEtapes != null) filteredEtapes.setPredicate(p -> true);
    }

    private void ouvrirFormulaireItineraire(Itineraire itineraire) {
        try {
            String fxmlPath = itineraire == null ?
                    "/ItineraireEtEtape/AjoutItineraireBack.fxml" :
                    "/ItineraireEtEtape/ModifierItineraireBack.fxml";

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            if (itineraire == null) {
                // Ajout
                AjoutItineraireBack controller = loader.getController();
                controller.setParentController(this);
            } else {
                // Modification
                ModifierItineraireBack controller = loader.getController();
                controller.setItineraire(itineraire);
                controller.setParentController(this);
            }

            Stage stage = new Stage();
            stage.setTitle(itineraire == null ? "Ajouter un itinéraire" : "Modifier l'itinéraire");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Le rafraîchissement se fait via le callback dans les contrôleurs

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage());
        }
    }

    private void ouvrirFormulaireEtape(EtapeTableModel etapeModel) {
        try {
            // Utiliser le nouveau formulaire backend
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ItineraireEtEtape/PageAjoutEtapeBack.fxml"));
            DialogPane dialogPane = loader.load();

            PageAjoutEtapeBack controller = loader.getController();
            controller.setDialogPane(dialogPane);

            controller.setOnEtapeAjoutee(() -> {
                loadEtapes();
                loadItineraires();
            });

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Ajouter une étape (Back Office)");
            dialog.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage());
        }
    }

    private void modifierItineraire(Itineraire itineraire) {
        ouvrirFormulaireItineraire(itineraire);
    }

    private void supprimerItineraire(Itineraire itineraire) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer l'itinéraire");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer l'itinéraire \"" + itineraire.getNom_itineraire() + "\" ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                itineraireCRUD.supprimer(itineraire.getId_itineraire());
                loadItineraires();
                loadEtapes();
                showAlert("Succès", "Itinéraire supprimé avec succès !");
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Erreur", "Impossible de supprimer l'itinéraire: " + e.getMessage());
            }
        }
    }

    private void modifierEtape(EtapeTableModel etape) {
        ouvrirFormulaireEtape(etape);
    }

    private void supprimerEtape(EtapeTableModel etapeModel) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer l'étape");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer cette étape ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                etapeCRUD.supprimer(etapeModel.getId());
                loadEtapes();
                showAlert("Succès", "Étape supprimée avec succès !");
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Erreur", "Impossible de supprimer l'étape: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleRetourVersItineraire() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ItineraireEtEtape/PageItineraire.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) statsTotalItineraires.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TravelMate - Itinéraires");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de revenir à la page précédente");
        }
    }

    private String getVoyageNameById(int idVoyage) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = MyBD.getInstance().getConn();
            stmt = conn.prepareStatement("SELECT titre_voyage FROM voyage WHERE id_voyage = ?");
            stmt.setInt(1, idVoyage);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("titre_voyage");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return "Voyage " + idVoyage;
    }

    private String getDestinationByVoyageId(int idVoyage) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = MyBD.getInstance().getConn();
            stmt = conn.prepareStatement(
                    "SELECT d.nom_destination FROM voyage v " +
                            "LEFT JOIN destination d ON v.id_destination = d.id_destination " +
                            "WHERE v.id_voyage = ?"
            );
            stmt.setInt(1, idVoyage);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("nom_destination") != null ? rs.getString("nom_destination") : "Non définie";
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return "Non définie";
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Classe modèle pour les étapes dans la table
    public static class EtapeTableModel {
        private final int id;
        private final String heure;
        private final String description;
        private final String activite;
        private final String itineraire;
        private final String lieu;
        private final float duree;

        public EtapeTableModel(etape e) {
            this.id = e.getId_etape();
            this.heure = e.getHeure() != null ? e.getHeure().toString().substring(0, 5) : "--:--";
            this.description = e.getDescription_etape() != null ? e.getDescription_etape() : "";
            this.activite = e.getNomActivite() != null ? e.getNomActivite() : "Activité inconnue";
            this.itineraire = e.getNomItineraire() != null ? e.getNomItineraire() : "Itinéraire inconnu";
            this.lieu = e.getLieuActivite() != null ? e.getLieuActivite() : "Lieu non défini";
            Float dureeObj = e.getDureeActivite();
            this.duree = dureeObj != null ? dureeObj : 0;
        }

        public int getId() { return id; }
        public String getHeure() { return heure; }
        public String getDescription() { return description; }
        public String getActivite() { return activite; }
        public String getItineraire() { return itineraire; }
        public String getLieu() { return lieu; }
        public float getDuree() { return duree; }
    }
}