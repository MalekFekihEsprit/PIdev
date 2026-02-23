package Controllers;

import Entities.Voyage;
import Services.VoyageCRUD;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Predicate;

public class VoyageController {

    public static VoyageController instance;

    @FXML
    private Button btnAnnulerModification;
    @FXML
    private Button btnConfirmerModification;
    @FXML
    private Button btnAjouter;
    @FXML
    private Button btnModifierFormulaire;

    @FXML
    private DatePicker fxdated;
    @FXML
    private DatePicker fxdatef;
    @FXML
    private TextField fxdestination;
    @FXML
    private ComboBox<String> fxstatut;
    @FXML
    private TextField fxtitre;

    // Nouveaux éléments pour la recherche
    @FXML
    private TextField tfSearchTitre;
    @FXML
    private ComboBox<String> cbFilterStatut;
    @FXML
    private Button btnSearch;
    @FXML
    private Button btnReset;
    @FXML
    private Label lblResultatsRecherche;
    @FXML
    private Label lblVoyagesActifsFooter;

    @FXML
    private Label lblVoyagesCount;
    @FXML
    private VBox voyagesContainer;

    private int voyageEnCoursDeModification = -1;
    private VoyageCRUD voyageCRUD = new VoyageCRUD();
    private ObservableList<Voyage> voyageList = FXCollections.observableArrayList();
    private FilteredList<Voyage> filteredData;

    @FXML
    public void initialize() {
        instance = this;

        // Initialiser la ComboBox des statuts
        fxstatut.getItems().addAll("a venir", "En cours", "Terminé", "Annulé");
        fxstatut.setValue("a venir");

        // Initialiser la ComboBox de filtre
        cbFilterStatut.getItems().addAll("Tous", "a venir", "En cours", "Terminé", "Annulé");
        cbFilterStatut.setValue("Tous");

        // Configurer les validateurs de dates
        configurerDatePickers();

        // Configurer les boutons de recherche
        btnSearch.setOnAction(this::rechercherVoyages);
        btnReset.setOnAction(this::resetRecherche);

        // Charger les voyages
        chargerVoyages();
    }

    private void configurerDatePickers() {
        // Date début: ne peut pas être avant aujourd'hui
        fxdated.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate aujourdHui = LocalDate.now();
                setDisable(empty || date.isBefore(aujourdHui));
            }
        });

        // Date fin: doit être après date début
        fxdatef.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate dateDebut = fxdated.getValue();
                if (dateDebut != null) {
                    setDisable(empty || !date.isAfter(dateDebut));
                }
            }
        });

        fxdated.valueProperty().addListener((observable, oldValue, newValue) -> {
            fxdatef.setValue(null);
            fxdatef.setDayCellFactory(picker -> new DateCell() {
                @Override
                public void updateItem(LocalDate date, boolean empty) {
                    super.updateItem(date, empty);
                    LocalDate dateDebut = fxdated.getValue();
                    if (dateDebut != null) {
                        setDisable(empty || !date.isAfter(dateDebut));
                    }
                }
            });
        });
    }

    @FXML
    private void rechercherVoyages(ActionEvent event) {
        String titreRecherche = tfSearchTitre.getText().toLowerCase();
        String statutFiltre = cbFilterStatut.getValue();

        // Créer un prédicat pour filtrer les voyages
        Predicate<Voyage> predicate = voyage -> {
            boolean matchTitre = true;
            boolean matchStatut = true;

            // Filtre par titre
            if (titreRecherche != null && !titreRecherche.isEmpty()) {
                matchTitre = voyage.getTitre_voyage().toLowerCase().contains(titreRecherche);
            }

            // Filtre par statut
            if (statutFiltre != null && !statutFiltre.equals("Tous")) {
                matchStatut = voyage.getStatut().equals(statutFiltre);
            }

            return matchTitre && matchStatut;
        };

        // Appliquer le filtre
        filteredData = new FilteredList<>(voyageList, predicate);

        // Afficher les résultats
        afficherVoyagesFiltres();

        // Mettre à jour le label de résultat
        int resultCount = filteredData.size();
        if (resultCount == 0) {
            lblResultatsRecherche.setText("Aucun résultat trouvé");
        } else {
            lblResultatsRecherche.setText(resultCount + " résultat(s) trouvé(s)");
        }
    }

    @FXML
    private void resetRecherche(ActionEvent event) {
        tfSearchTitre.clear();
        cbFilterStatut.setValue("Tous");
        filteredData = null;
        lblResultatsRecherche.setText("");
        chargerVoyages();
    }

    private void afficherVoyagesFiltres() {
        try {
            voyagesContainer.getChildren().clear();

            List<Voyage> voyagesAfficher;
            if (filteredData != null) {
                voyagesAfficher = filteredData;
            } else {
                voyagesAfficher = voyageList;
            }

            int nbVoyages = voyagesAfficher.size();
            lblVoyagesCount.setText(nbVoyages + " voyage" + (nbVoyages > 1 ? "s" : ""));

            // Compter les voyages actifs (En cours ou à venir)
            long actifs = voyagesAfficher.stream()
                    .filter(v -> "En cours".equals(v.getStatut()) || "a venir".equals(v.getStatut()))
                    .count();
            lblVoyagesActifsFooter.setText(actifs + " voyage" + (actifs > 1 ? "s" : "") + " actif" + (actifs > 1 ? "s" : ""));

            if (voyagesAfficher.isEmpty()) {
                Label aucunVoyage = new Label("Aucun voyage trouvé");
                aucunVoyage.setStyle("-fx-text-fill: #64748b; -fx-font-size: 14; -fx-padding: 20;");
                voyagesContainer.getChildren().add(aucunVoyage);
                return;
            }

            for (Voyage voyage : voyagesAfficher) {
                String nomDestination = voyageCRUD.getNomDestination(voyage.getId_destination());

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/CarteVoyage.fxml"));
                VBox carte = loader.load();

                CarteVoyageController carteController = loader.getController();
                carteController.setDonnees(voyage, nomDestination);

                voyagesContainer.getChildren().add(carte);
            }

        } catch (SQLException | IOException e) {
            System.err.println("ERREUR affichage voyages: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'afficher les voyages: " + e.getMessage());
        }
    }

    @FXML
    void saveVoyage(ActionEvent event) {
        if (!validerFormulaire()) return;

        try {
            Voyage v = construireVoyageFromFormulaire();
            voyageCRUD.ajouter(v);

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Voyage ajouté avec succès!");
            chargerVoyages();
            resetForm();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur BD", "Erreur lors de l'ajout: " + e.getMessage());
        }
    }

    @FXML
    void annulerModification(ActionEvent event) {
        resetForm();
        voyageEnCoursDeModification = -1;
        System.out.println("Modification annulée");
    }

    @FXML
    void confirmerModification(ActionEvent event) {
        if (voyageEnCoursDeModification == -1) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Aucun voyage sélectionné pour modification");
            return;
        }

        if (!validerFormulaire()) return;

        try {
            Voyage v = construireVoyageFromFormulaire();
            v.setId_voyage(voyageEnCoursDeModification);

            voyageCRUD.modifier(v);

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Voyage modifié avec succès!");
            resetForm();
            chargerVoyages();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur BD", "Erreur lors de la modification: " + e.getMessage());
        }
    }

    private boolean validerFormulaire() {
        if (fxtitre.getText().isEmpty() || fxdestination.getText().isEmpty() ||
                fxdated.getValue() == null || fxdatef.getValue() == null) {

            showAlert(Alert.AlertType.ERROR, "Erreur de validation",
                    "Veuillez remplir tous les champs (titre, destination, dates)");
            return false;
        }

        LocalDate dateDebutLocal = fxdated.getValue();
        LocalDate dateFinLocal = fxdatef.getValue();
        LocalDate aujourdHui = LocalDate.now();

        if (dateDebutLocal.isBefore(aujourdHui)) {
            showAlert(Alert.AlertType.ERROR, "Erreur de validation",
                    "La date de début doit être aujourd'hui ou une date future");
            return false;
        }

        if (!dateFinLocal.isAfter(dateDebutLocal)) {
            showAlert(Alert.AlertType.ERROR, "Erreur de validation",
                    "La date de fin doit être postérieure à la date de début");
            return false;
        }

        try {
            Integer.parseInt(fxdestination.getText());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de format",
                    "L'ID de destination doit être un nombre");
            return false;
        }

        return true;
    }

    private Voyage construireVoyageFromFormulaire() {
        LocalDate dateDebutLocal = fxdated.getValue();
        LocalDate dateFinLocal = fxdatef.getValue();

        Date dateDebut = Date.valueOf(dateDebutLocal);
        Date dateFin = Date.valueOf(dateFinLocal);

        return new Voyage(
                fxtitre.getText(),
                dateDebut,
                dateFin,
                fxstatut.getValue() != null ? fxstatut.getValue() : "a venir",
                Integer.parseInt(fxdestination.getText())
        );
    }

    private void resetForm() {
        fxtitre.clear();
        fxdestination.clear();
        fxdated.setValue(null);
        fxdatef.setValue(null);
        fxstatut.setValue("a venir");

        voyageEnCoursDeModification = -1;

        btnAjouter.setVisible(true);
        btnAjouter.setManaged(true);
        btnAjouter.setOnAction(this::saveVoyage);

        btnConfirmerModification.setVisible(false);
        btnConfirmerModification.setManaged(false);

        btnAnnulerModification.setVisible(false);
        btnAnnulerModification.setManaged(false);

        btnModifierFormulaire.setVisible(false);
        btnModifierFormulaire.setManaged(false);

        fxtitre.requestFocus();
    }

    public void chargerVoyages() {
        try {
            System.out.println("=== CHARGEMENT DES VOYAGES ===");

            List<Voyage> voyages = voyageCRUD.afficher();
            voyageList.clear();
            voyageList.addAll(voyages);

            filteredData = null;
            lblResultatsRecherche.setText("");
            afficherVoyagesFiltres();

            System.out.println("=== CHARGEMENT TERMINÉ: " + voyages.size() + " voyages ===");

        } catch (SQLException e) {
            System.err.println("ERREUR chargement voyages: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les voyages: " + e.getMessage());
        }
    }

    public void chargerVoyagePourModification(Voyage voyage) {
        this.voyageEnCoursDeModification = voyage.getId_voyage();

        fxtitre.setText(voyage.getTitre_voyage());
        fxdestination.setText(String.valueOf(voyage.getId_destination()));

        LocalDate dateDebut = voyage.getDate_debut().toLocalDate();
        LocalDate dateFin = voyage.getDate_fin().toLocalDate();

        fxdated.setValue(dateDebut);
        fxdatef.setValue(dateFin);
        fxstatut.setValue(voyage.getStatut());

        btnAjouter.setVisible(false);
        btnAjouter.setManaged(false);

        btnConfirmerModification.setVisible(true);
        btnConfirmerModification.setManaged(true);

        btnAnnulerModification.setVisible(true);
        btnAnnulerModification.setManaged(true);

        btnModifierFormulaire.setVisible(false);
        btnModifierFormulaire.setManaged(false);

        fxtitre.requestFocus();

        System.out.println("Voyage chargé pour modification ID: " + voyage.getId_voyage());
    }

    @FXML
    void ouvrirFormulaireModification(ActionEvent event) {
        showAlert(Alert.AlertType.INFORMATION, "Info",
                "Sélectionnez un voyage à modifier en cliquant sur le bouton 'Modifier' dans sa carte");
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}