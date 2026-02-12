package Controllers;

import Entities.Voyage;
import Services.VoyageCRUD;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javax.swing.*;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

public class VoyageController {
    public static VoyageController instance;

    @FXML
    private Button btnAnnulerModification;

    @FXML
    private Button btnConfirmerModification;

    @FXML
    private Button btnModifierFormulaire;

    private int voyageEnCoursDeModification = -1;

    @FXML
    private Label activitesVoyage;
    @FXML
    private Button btnAjouter;
    @FXML
    private Label btnModifier;
    @FXML
    private Label btnParticipants;
    @FXML
    private Label btnSupprimer;
    @FXML
    private Label datesVoyage;
    @FXML
    private Label destinationVoyage;
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
    private Label participantsVoyage;
    @FXML
    private Label statutVoyage;
    @FXML
    private Label titreVoyage;
    @FXML
    private VBox voyagesContainer;

    @FXML
    public void initialize() {
        instance = this;
        chargerVoyages();

        fxdated.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate aujourdHui = LocalDate.now();
                setDisable(empty || date.isBefore(aujourdHui));
            }
        });

        fxdatef.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate dateDebut = fxdated.getValue();
                if (dateDebut != null) {
                    setDisable(empty || date.isBefore(dateDebut) || date.isEqual(dateDebut));
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
                        setDisable(empty || date.isBefore(dateDebut) || date.isEqual(dateDebut));
                    }
                }
            });
        });
    }

    @FXML
    void saveVoyage(ActionEvent event) {
        if (fxtitre.getText().isEmpty() || fxdestination.getText().isEmpty() ||
                fxdated.getValue() == null || fxdatef.getValue() == null) {

            JOptionPane.showMessageDialog(
                    JOptionPane.getRootFrame(),
                    "Veuillez remplir tous les champs!",
                    "Erreur de validation",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        try {
            LocalDate dateDebutLocal = fxdated.getValue();
            LocalDate dateFinLocal = fxdatef.getValue();
            LocalDate aujourdHui = LocalDate.now();

            if (dateDebutLocal.isBefore(aujourdHui)) {
                JOptionPane.showMessageDialog(
                        JOptionPane.getRootFrame(),
                        "La date de début doit être aujourd'hui ou une date future!",
                        "Erreur de validation",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            if (!dateFinLocal.isAfter(dateDebutLocal)) {
                JOptionPane.showMessageDialog(
                        JOptionPane.getRootFrame(),
                        "La date de fin doit être postérieure à la date de début!",
                        "Erreur de validation",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            Date dateDebut = Date.from(dateDebutLocal.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date dateFin = Date.from(dateFinLocal.atStartOfDay(ZoneId.systemDefault()).toInstant());

            Voyage v = new Voyage(
                    fxtitre.getText(),
                    dateDebut,
                    dateFin,
                    "À venir",
                    Integer.parseInt(fxdestination.getText())
            );

            VoyageCRUD vc = new VoyageCRUD();
            vc.ajouter(v);

            JOptionPane.showMessageDialog(
                    JOptionPane.getRootFrame(),
                    "Voyage ajouté avec succès!",
                    "Succès",
                    JOptionPane.INFORMATION_MESSAGE
            );

            chargerVoyages();
            resetForm();

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(
                    JOptionPane.getRootFrame(),
                    "Erreur lors de l'ajout du voyage: " + e.getMessage(),
                    "Erreur BD",
                    JOptionPane.ERROR_MESSAGE
            );
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(
                    JOptionPane.getRootFrame(),
                    "L'ID de destination doit être un nombre!",
                    "Erreur de format",
                    JOptionPane.ERROR_MESSAGE
            );
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
            return;
        }

        if (fxtitre.getText().isEmpty() || fxdestination.getText().isEmpty() ||
                fxdated.getValue() == null || fxdatef.getValue() == null) {

            JOptionPane.showMessageDialog(
                    JOptionPane.getRootFrame(),
                    "Veuillez remplir tous les champs!",
                    "Erreur de validation",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        try {
            LocalDate dateDebutLocal = fxdated.getValue();
            LocalDate dateFinLocal = fxdatef.getValue();
            LocalDate aujourdHui = LocalDate.now();

            if (dateDebutLocal.isBefore(aujourdHui)) {
                JOptionPane.showMessageDialog(
                        JOptionPane.getRootFrame(),
                        "La date de début doit être aujourd'hui ou une date future!",
                        "Erreur de validation",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            if (!dateFinLocal.isAfter(dateDebutLocal)) {
                JOptionPane.showMessageDialog(
                        JOptionPane.getRootFrame(),
                        "La date de fin doit être postérieure à la date de début!",
                        "Erreur de validation",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            Date dateDebut = Date.from(dateDebutLocal.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date dateFin = Date.from(dateFinLocal.atStartOfDay(ZoneId.systemDefault()).toInstant());

            Voyage v = new Voyage(
                    voyageEnCoursDeModification,
                    fxtitre.getText(),
                    dateDebut,
                    dateFin,
                    fxstatut.getValue() != null ? fxstatut.getValue() : "À venir",
                    Integer.parseInt(fxdestination.getText())
            );

            VoyageCRUD vc = new VoyageCRUD();
            vc.modifier(v);

            JOptionPane.showMessageDialog(
                    JOptionPane.getRootFrame(),
                    "Voyage modifié avec succès!",
                    "Succès",
                    JOptionPane.INFORMATION_MESSAGE
            );

            resetForm();
            chargerVoyages();

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(
                    JOptionPane.getRootFrame(),
                    "Erreur lors de la modification: " + e.getMessage(),
                    "Erreur BD",
                    JOptionPane.ERROR_MESSAGE
            );
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(
                    JOptionPane.getRootFrame(),
                    "L'ID de destination doit être un nombre!",
                    "Erreur de format",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    @FXML
    void ouvrirFormulaireModification(ActionEvent event) {
        agrandirInterface();
        preparerFormulaireModification();
    }

    private void agrandirInterface() {
        VBox parentVBox = (VBox) voyagesContainer.getParent();
        parentVBox.setPrefHeight(900);
        parentVBox.setSpacing(32);

        VBox formulaireBox = (VBox) btnAjouter.getParent().getParent();
        formulaireBox.setStyle("-fx-background-color: white; -fx-background-radius: 20; -fx-padding: 24; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 15, 0, 0, 5);");
        formulaireBox.setPrefHeight(300);

        fxtitre.setPrefHeight(35);
        fxtitre.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 12; -fx-padding: 10; -fx-font-size: 13;");

        fxdestination.setPrefHeight(35);
        fxdestination.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 12; -fx-padding: 10; -fx-font-size: 13;");

        fxdated.setPrefHeight(35);
        fxdated.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 12; -fx-padding: 8; -fx-font-size: 13;");

        fxdatef.setPrefHeight(35);
        fxdatef.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 12; -fx-padding: 8; -fx-font-size: 13;");

        fxstatut.setPrefHeight(35);
        fxstatut.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 12; -fx-padding: 8; -fx-font-size: 13;");

        btnModifierFormulaire.setVisible(true);
        btnModifierFormulaire.setManaged(true);

        System.out.println("Interface agrandie pour modification");
    }

    private void preparerFormulaireModification() {
        resetForm();

        Label infoLabel = new Label("Sélectionnez un voyage à modifier dans la liste ci-dessous");
        infoLabel.setStyle("-fx-text-fill: #10b981; -fx-font-size: 12; -fx-font-weight: bold; -fx-padding: 10 0 0 0;");

        VBox formulaireBox = (VBox) btnAjouter.getParent().getParent();
        boolean labelExiste = false;
        for (javafx.scene.Node node : formulaireBox.getChildren()) {
            if (node instanceof Label && ((Label) node).getText().contains("Sélectionnez un voyage")) {
                labelExiste = true;
                break;
            }
        }

        if (!labelExiste) {
            formulaireBox.getChildren().add(infoLabel);
        }
    }

    private void restaurerInterface() {
        VBox parentVBox = (VBox) voyagesContainer.getParent();
        parentVBox.setPrefHeight(750);
        parentVBox.setSpacing(24);

        VBox formulaireBox = (VBox) btnAjouter.getParent().getParent();
        formulaireBox.setStyle("-fx-background-color: white; -fx-background-radius: 16; -fx-padding: 16; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.02), 10, 0, 0, 2);");
        formulaireBox.setPrefHeight(250);

        fxtitre.setPrefHeight(30);
        fxtitre.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 10; -fx-padding: 8; -fx-font-size: 12;");

        fxdestination.setPrefHeight(30);
        fxdestination.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 10; -fx-padding: 8; -fx-font-size: 12;");

        fxdated.setPrefHeight(30);
        fxdated.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 10; -fx-padding: 8; -fx-font-size: 12;");

        fxdatef.setPrefHeight(30);
        fxdatef.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 10; -fx-padding: 8; -fx-font-size: 12;");

        fxstatut.setPrefHeight(30);
        fxstatut.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 10; -fx-padding: 8; -fx-font-size: 12;");

        VBox formulaireBox2 = (VBox) btnAjouter.getParent().getParent();
        formulaireBox2.getChildren().removeIf(node ->
                node instanceof Label && ((Label) node).getText().contains("Sélectionnez un voyage"));
    }

    private void resetForm() {
        fxtitre.clear();
        fxdestination.clear();
        fxdated.setValue(null);
        fxdatef.setValue(null);
        fxstatut.setValue(null);

        voyageEnCoursDeModification = -1;

        btnAjouter.setVisible(true);
        btnAjouter.setManaged(true);
        btnAjouter.setText("Ajouter Voyage");
        btnAjouter.setOnAction(this::saveVoyage);

        btnModifierFormulaire.setVisible(false);
        btnModifierFormulaire.setManaged(false);

        btnConfirmerModification.setVisible(false);
        btnConfirmerModification.setManaged(false);

        btnAnnulerModification.setVisible(false);
        btnAnnulerModification.setManaged(false);

        restaurerInterface();

        fxtitre.requestFocus();
    }

    public void chargerVoyages() {
        try {
            System.out.println("=== RAFRAÎCHISSEMENT DES VOYAGES ===");
            voyagesContainer.getChildren().clear();

            VoyageCRUD vc = new VoyageCRUD();
            List<Voyage> voyages = vc.afficher();

            lblVoyagesCount.setText(voyages.size() + " voyage" + (voyages.size() > 1 ? "s" : ""));

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

            System.out.println("=== RAFFRAÎCHISSEMENT TERMINÉ: " + voyages.size() + " voyages ===");

        } catch (SQLException | IOException e) {
            System.err.println("ERREUR chargement voyages: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void chargerVoyagePourModification(Voyage voyage) {
        this.voyageEnCoursDeModification = voyage.getId_voyage();

        fxtitre.setText(voyage.getTitre_voyage());
        fxdestination.setText(String.valueOf(voyage.getId_destination()));

        LocalDate dateDebut = voyage.getDate_debut().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        LocalDate dateFin = voyage.getDate_fin().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        fxdated.setValue(dateDebut);
        fxdatef.setValue(dateFin);
        fxstatut.setValue(voyage.getStatut());

        btnAjouter.setVisible(false);
        btnAjouter.setManaged(false);

        btnModifierFormulaire.setVisible(false);
        btnModifierFormulaire.setManaged(false);

        btnConfirmerModification.setVisible(true);
        btnConfirmerModification.setManaged(true);

        btnAnnulerModification.setVisible(true);
        btnAnnulerModification.setManaged(true);

        fxtitre.requestFocus();

        System.out.println("Voyage chargé pour modification ID: " + voyage.getId_voyage());
    }
}