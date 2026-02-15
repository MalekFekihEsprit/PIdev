package Controllers;

import Entities.Voyage;
import Services.VoyageCRUD;
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

public class VoyageController {

    public static VoyageController instance;

    @FXML
    private Button btnAnnulerModification;
    @FXML
    private Button btnConfirmerModification;
    @FXML
    private Button btnAjouter;

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

    @FXML
    private Label lblVoyagesCount;
    @FXML
    private VBox voyagesContainer;

    private int voyageEnCoursDeModification = -1;

    @FXML
    public void initialize() {
        instance = this;

        // Initialiser la ComboBox des statuts avec la valeur par défaut de la BD
        fxstatut.getItems().addAll("a venir", "En cours", "Terminé", "Annulé");
        fxstatut.setValue("a venir"); // Valeur par défaut

        // Configurer les validateurs de dates
        configurerDatePickers();

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
    void saveVoyage(ActionEvent event) {
        if (!validerFormulaire()) return;

        try {
            Voyage v = construireVoyageFromFormulaire();
            VoyageCRUD vc = new VoyageCRUD();
            vc.ajouter(v);

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

            VoyageCRUD vc = new VoyageCRUD();
            vc.modifier(v);

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

        // Convertir LocalDate en java.sql.Date
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
        fxstatut.setValue("a venir"); // Remettre la valeur par défaut

        voyageEnCoursDeModification = -1;

        // Réinitialiser les boutons
        btnAjouter.setVisible(true);
        btnAjouter.setManaged(true);
        btnAjouter.setText("Ajouter Voyage");
        btnAjouter.setOnAction(this::saveVoyage);

        btnConfirmerModification.setVisible(false);
        btnConfirmerModification.setManaged(false);

        btnAnnulerModification.setVisible(false);
        btnAnnulerModification.setManaged(false);

        fxtitre.requestFocus();
    }

    public void chargerVoyages() {
        try {
            System.out.println("=== CHARGEMENT DES VOYAGES ===");
            voyagesContainer.getChildren().clear();

            VoyageCRUD vc = new VoyageCRUD();
            List<Voyage> voyages = vc.afficher();

            int nbVoyages = voyages.size();
            lblVoyagesCount.setText(nbVoyages + " voyage" + (nbVoyages > 1 ? "s" : ""));

            if (voyages.isEmpty()) {
                Label aucunVoyage = new Label("Aucun voyage trouvé");
                aucunVoyage.setStyle("-fx-text-fill: #64748b; -fx-font-size: 14; -fx-padding: 20;");
                voyagesContainer.getChildren().add(aucunVoyage);
                return;
            }

            for (Voyage voyage : voyages) {
                String nomDestination = vc.getNomDestination(voyage.getId_destination());

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/CarteVoyage.fxml"));
                VBox carte = loader.load();

                CarteVoyageController carteController = loader.getController();
                carteController.setDonnees(voyage, nomDestination);

                voyagesContainer.getChildren().add(carte);
            }

            System.out.println("=== CHARGEMENT TERMINÉ: " + voyages.size() + " voyages ===");

        } catch (SQLException | IOException e) {
            System.err.println("ERREUR chargement voyages: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les voyages: " + e.getMessage());
        }
    }

    public void chargerVoyagePourModification(Voyage voyage) {
        this.voyageEnCoursDeModification = voyage.getId_voyage();

        // Remplir le formulaire
        fxtitre.setText(voyage.getTitre_voyage());
        fxdestination.setText(String.valueOf(voyage.getId_destination()));

        // Convertir java.sql.Date en LocalDate
        LocalDate dateDebut = voyage.getDate_debut().toLocalDate();
        LocalDate dateFin = voyage.getDate_fin().toLocalDate();

        fxdated.setValue(dateDebut);
        fxdatef.setValue(dateFin);
        fxstatut.setValue(voyage.getStatut());

        // Afficher les boutons de modification
        btnAjouter.setVisible(false);
        btnAjouter.setManaged(false);

        btnConfirmerModification.setVisible(true);
        btnConfirmerModification.setManaged(true);

        btnAnnulerModification.setVisible(true);
        btnAnnulerModification.setManaged(true);

        fxtitre.requestFocus();

        System.out.println("Voyage chargé pour modification ID: " + voyage.getId_voyage());
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    @FXML
    void ouvrirFormulaireModification(ActionEvent event) {
        // Cette méthode est appelée quand on clique sur "✏️ Modifier un voyage"
        showAlert(Alert.AlertType.INFORMATION, "Info",
                "Sélectionnez un voyage à modifier en cliquant sur le bouton 'Modifier' dans sa carte");
    }
}