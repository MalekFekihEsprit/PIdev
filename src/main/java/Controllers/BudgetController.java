package Controllers;

import Entities.Budget;
import Entities.Depense;
import Services.BudgetCRUD;
import Services.DepenseCRUD;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.net.URL;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class BudgetController implements Initializable {

    // Services
    private final BudgetCRUD budgetCRUD = new BudgetCRUD();
    private final DepenseCRUD depenseCRUD = new DepenseCRUD();

    // Budget actif
    private Budget budgetActif;
    private ObservableList<Budget> budgetsList = FXCollections.observableArrayList();
    private ObservableList<Depense> depensesList = FXCollections.observableArrayList();

    // FXML Injections
    @FXML
    private ComboBox<Budget> cmbListeBudgets;
    @FXML
    private Button btnModifierBudget;
    @FXML
    private Button btnSupprimerBudget;
    @FXML
    private Label lblBudgetNom;
    @FXML
    private Label lblBudgetStatut;
    @FXML
    private Label lblBudgetDevise;
    @FXML
    private Label lblBudgetVoyage;
    @FXML
    private Label lblModalBudgetTitle;
    @FXML
    private Label lblModalBudgetSubtitle;

    // FXML Injections existantes
    @FXML
    private Button btnAjouterDepense;
    @FXML
    private Button btnNouveauBudget;
    @FXML
    private Button btnSaveBudget;
    @FXML
    private Button btnSaveDepense;
    @FXML
    private ComboBox<String> cmbCategorieDepense;
    @FXML
    private ComboBox<String> cmbDeviseBudget;
    @FXML
    private ComboBox<String> cmbDeviseDepense;
    @FXML
    private ComboBox<String> cmbPaiementDepense;
    @FXML
    private ComboBox<String> cmbStatutBudget;
    @FXML
    private TableColumn<Depense, String> colActions;
    @FXML
    private TableColumn<Depense, String> colCategorie;
    @FXML
    private TableColumn<Depense, Date> colDate;
    @FXML
    private TableColumn<Depense, String> colLibelle;
    @FXML
    private TableColumn<Depense, Float> colMontant;
    @FXML
    private TableColumn<Depense, String> colPaiement;
    @FXML
    private Label lblDepense;
    @FXML
    private Label lblModalTitle;
    @FXML
    private Label lblMontantTotal;
    @FXML
    private Label lblPct;
    @FXML
    private Label lblRestant;
    @FXML
    private Label lblNomBudget;
    @FXML
    private VBox modalBudget;
    @FXML
    private VBox modalDepense;
    @FXML
    private ProgressBar progressBudget;
    @FXML
    private TableView<Depense> tableDepenses;
    @FXML
    private TextField txtMontantBudget;
    @FXML
    private TextField txtMontantDepense;
    @FXML
    private TextField txtNomBudget;
    @FXML
    private TextArea txtNotesDepense;
    @FXML
    private TextField txtLibelleDepense;
    @FXML
    private DatePicker dateDepense;

    // Pour la modification
    private Depense depenseEnCours;
    private boolean isEditingDepense = false;
    private boolean isEditingBudget = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initializeComboBoxes();
        configureTableView();
        configureBudgetComboBox();
        chargerTousLesBudgets();
        chargerPremierBudget();
        chargerDepenses();
        dateDepense.setValue(LocalDate.now());
        setupDateConverters();
    }

    private void initializeComboBoxes() {
        // Devises pour budget
        if (cmbDeviseBudget != null) {
            cmbDeviseBudget.setItems(FXCollections.observableArrayList("EUR", "USD", "GBP", "CHF", "CAD", "TND"));
            cmbDeviseBudget.setValue("EUR");
        }

        // Statut budget
        if (cmbStatutBudget != null) {
            cmbStatutBudget.setItems(FXCollections.observableArrayList("ACTIF", "INACTIF"));
            cmbStatutBudget.setValue("ACTIF");
        }

        // Devises pour dépense
        if (cmbDeviseDepense != null) {
            cmbDeviseDepense.setItems(FXCollections.observableArrayList("EUR", "USD", "GBP", "CHF", "CAD", "TND"));
            cmbDeviseDepense.setValue("EUR");
        }

        // Catégories de dépenses
        if (cmbCategorieDepense != null) {
            cmbCategorieDepense.setItems(FXCollections.observableArrayList(
                    "Hébergement", "Transport", "Restauration", "Activités",
                    "Shopping", "Essence", "Péage", "Parking", "Autre"
            ));
        }

        // Méthodes de paiement
        if (cmbPaiementDepense != null) {
            cmbPaiementDepense.setItems(FXCollections.observableArrayList(
                    "Carte bancaire", "Espèces", "Virement", "PayPal", "Apple Pay", "Google Pay"
            ));
        }
    }

    private void configureBudgetComboBox() {
        if (cmbListeBudgets != null) {
            // Personnaliser l'affichage des budgets dans le ComboBox
            cmbListeBudgets.setConverter(new StringConverter<Budget>() {
                @Override
                public String toString(Budget budget) {
                    return budget != null ? budget.getDescriptionBudget() : "";
                }

                @Override
                public Budget fromString(String string) {
                    return null;
                }
            });

            // Gérer le changement de sélection
            cmbListeBudgets.setOnAction(event -> {
                Budget selected = cmbListeBudgets.getValue();
                if (selected != null) {
                    budgetActif = selected;
                    afficherInfoBudget();
                    chargerDepenses();
                }
            });
        }
    }

    private void configureTableView() {
        colLibelle.setCellValueFactory(new PropertyValueFactory<>("libelleDepense"));
        colCategorie.setCellValueFactory(new PropertyValueFactory<>("categorieDepense"));
        colMontant.setCellValueFactory(new PropertyValueFactory<>("montantDepense"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateCreation"));
        colPaiement.setCellValueFactory(new PropertyValueFactory<>("typePaiement"));

        // Formatage de la colonne montant
        colMontant.setCellFactory(column -> new TableCell<Depense, Float>() {
            @Override
            protected void updateItem(Float montant, boolean empty) {
                super.updateItem(montant, empty);
                if (empty || montant == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f €", montant));
                }
            }
        });

        // Formatage de la colonne date
        colDate.setCellFactory(column -> new TableCell<Depense, Date>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            @Override
            protected void updateItem(Date date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(date.toLocalDate().format(formatter));
                }
            }
        });

        // Colonne des actions
        colActions.setCellFactory(column -> new TableCell<Depense, String>() {
            private final Button btnEdit = new Button("✏️");
            private final Button btnDelete = new Button("❌");

            {
                btnEdit.setStyle("-fx-background-color: #f1f5f9; -fx-cursor: hand; -fx-font-size: 12; -fx-min-width: 30; -fx-background-radius: 8;");
                btnDelete.setStyle("-fx-background-color: #f1f5f9; -fx-cursor: hand; -fx-font-size: 12; -fx-min-width: 30; -fx-background-radius: 8;");

                btnEdit.setOnAction(event -> {
                    Depense depense = getTableView().getItems().get(getIndex());
                    handleEditDepense(depense);
                });

                btnDelete.setOnAction(event -> {
                    Depense depense = getTableView().getItems().get(getIndex());
                    handleDeleteDepense(depense);
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox buttons = new HBox(5, btnEdit, btnDelete);
                    buttons.setAlignment(javafx.geometry.Pos.CENTER);
                    setGraphic(buttons);
                }
            }
        });

        tableDepenses.setItems(depensesList);
    }

    private void setupDateConverters() {
        StringConverter<LocalDate> converter = new StringConverter<LocalDate>() {
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            @Override
            public String toString(LocalDate date) {
                if (date != null) {
                    return dateFormatter.format(date);
                }
                return "";
            }

            @Override
            public LocalDate fromString(String string) {
                if (string != null && !string.isEmpty()) {
                    return LocalDate.parse(string, dateFormatter);
                }
                return null;
            }
        };

        if (dateDepense != null) dateDepense.setConverter(converter);
    }

    private void chargerTousLesBudgets() {
        try {
            List<Budget> budgets = budgetCRUD.afficher();
            budgetsList.clear();
            budgetsList.addAll(budgets);
            if (cmbListeBudgets != null) {
                cmbListeBudgets.setItems(budgetsList);
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les budgets: " + e.getMessage());
        }
    }

    private void chargerPremierBudget() {
        try {
            List<Budget> budgets = budgetCRUD.afficher();
            if (!budgets.isEmpty()) {
                budgetActif = budgets.get(0);
                if (cmbListeBudgets != null) {
                    cmbListeBudgets.setValue(budgetActif);
                }
                afficherInfoBudget();
            } else {
                budgetActif = null;
                resetAffichageBudget();
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les budgets: " + e.getMessage());
        }
    }

    private void resetAffichageBudget() {
        lblMontantTotal.setText("0€");
        lblDepense.setText("0€");
        lblRestant.setText("0€");
        lblPct.setText("0%");
        progressBudget.setProgress(0);

        if (lblNomBudget != null) lblNomBudget.setText("Aucun budget");
        if (lblBudgetNom != null) lblBudgetNom.setText("Aucun budget");
        if (lblBudgetStatut != null) {
            lblBudgetStatut.setText("● Inactif");
            lblBudgetStatut.setStyle("-fx-background-color: #94a3b8; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 4 12; -fx-font-size: 11; -fx-font-weight: 600;");
        }
        if (lblBudgetDevise != null) lblBudgetDevise.setText("Devise: -");
        if (lblBudgetVoyage != null) lblBudgetVoyage.setText("Voyage ID: -");
    }

    private void afficherInfoBudget() {
        if (budgetActif != null) {
            // Mise à jour des labels principaux
            lblMontantTotal.setText(String.format("%.0f %s", budgetActif.getMontantTotal(), budgetActif.getDeviseBudget()));

            if (lblNomBudget != null) lblNomBudget.setText(budgetActif.getDescriptionBudget());
            if (lblBudgetNom != null) lblBudgetNom.setText(budgetActif.getDescriptionBudget());

            // Statut avec couleur
            if (lblBudgetStatut != null) {
                String statut = budgetActif.getStatutBudget();
                lblBudgetStatut.setText("● " + statut);
                if ("ACTIF".equals(statut)) {
                    lblBudgetStatut.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 4 12; -fx-font-size: 11; -fx-font-weight: 600;");
                } else {
                    lblBudgetStatut.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 4 12; -fx-font-size: 11; -fx-font-weight: 600;");
                }
            }

            // Devise
            if (lblBudgetDevise != null) {
                lblBudgetDevise.setText("Devise: " + budgetActif.getDeviseBudget());
            }

            // ID Voyage
            if (lblBudgetVoyage != null) {
                lblBudgetVoyage.setText(budgetActif.getIdVoyage() > 0 ? "Voyage ID: " + budgetActif.getIdVoyage() : "Voyage non associé");
            }
        }
    }

    private void chargerDepenses() {
        if (budgetActif != null) {
            try {
                List<Depense> depenses = depenseCRUD.afficher();
                depensesList.clear();
                for (Depense d : depenses) {
                    if (d.getIdBudget() == budgetActif.getIdBudget()) {
                        depensesList.add(d);
                    }
                }
                mettreAJourStats();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les dépenses: " + e.getMessage());
            }
        }
    }

    private void mettreAJourStats() {
        if (budgetActif == null) return;

        float totalDepenses = 0;
        for (Depense d : depensesList) {
            totalDepenses += d.getMontantDepense();
        }

        float restant = budgetActif.getMontantTotal() - totalDepenses;
        float pourcentage = budgetActif.getMontantTotal() > 0 ? (totalDepenses / budgetActif.getMontantTotal()) * 100 : 0;

        lblDepense.setText(String.format("%.0f %s", totalDepenses, budgetActif.getDeviseBudget()));
        lblRestant.setText(String.format("%.0f %s", restant, budgetActif.getDeviseBudget()));
        lblPct.setText(String.format("%.1f%%", pourcentage));
        progressBudget.setProgress(budgetActif.getMontantTotal() > 0 ? totalDepenses / budgetActif.getMontantTotal() : 0);

        // Changer la couleur selon le pourcentage
        String couleur;
        if (pourcentage > 85) {
            couleur = "#ef4444";
        } else if (pourcentage > 60) {
            couleur = "#f59e0b";
        } else {
            couleur = "#10b981";
        }

        lblPct.setStyle("-fx-text-fill: " + couleur + ";");
        progressBudget.setStyle("-fx-accent: " + couleur + ";");
    }

    // GESTION DES BUDGETS
    @FXML
    void handleNouveauBudget(ActionEvent event) {
        modalBudget.setVisible(true);
        modalBudget.setManaged(true);
        modalDepense.setVisible(false);
        modalDepense.setManaged(false);

        isEditingBudget = false;

        if (lblModalBudgetTitle != null) lblModalBudgetTitle.setText("Nouveau Budget");
        if (lblModalBudgetSubtitle != null) lblModalBudgetSubtitle.setText("Créez un nouveau budget");

        // Mode création - formulaire vide
        txtNomBudget.clear();
        txtMontantBudget.clear();
        cmbDeviseBudget.setValue("EUR");
        cmbStatutBudget.setValue("ACTIF");
    }

    @FXML
    void handleModifierBudget(ActionEvent event) {
        if (budgetActif == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Aucun budget sélectionné");
            return;
        }

        modalBudget.setVisible(true);
        modalBudget.setManaged(true);
        modalDepense.setVisible(false);
        modalDepense.setManaged(false);

        isEditingBudget = true;

        if (lblModalBudgetTitle != null) lblModalBudgetTitle.setText("Modifier le Budget");
        if (lblModalBudgetSubtitle != null) lblModalBudgetSubtitle.setText("Modifiez les informations du budget");

        // Pré-remplir le formulaire
        txtNomBudget.setText(budgetActif.getDescriptionBudget());
        txtMontantBudget.setText(String.valueOf(budgetActif.getMontantTotal()));
        cmbDeviseBudget.setValue(budgetActif.getDeviseBudget());
        cmbStatutBudget.setValue(budgetActif.getStatutBudget());
    }

    @FXML
    void handleSupprimerBudget(ActionEvent event) {
        if (budgetActif == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Aucun budget sélectionné");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer le budget");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer ce budget ? Toutes les dépenses associées seront également supprimées.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // Supprimer d'abord les dépenses associées
                    for (Depense d : depensesList) {
                        depenseCRUD.supprimer(d.getIdDepense());
                    }

                    // Puis supprimer le budget
                    budgetCRUD.supprimer(budgetActif.getIdBudget());

                    // Recharger les listes
                    chargerTousLesBudgets();
                    chargerPremierBudget();

                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Budget supprimé avec succès !");
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer: " + e.getMessage());
                }
            }
        });
    }

    // GESTION DES DÉPENSES
    @FXML
    void handleAjouterDepense(ActionEvent event) {
        if (budgetActif == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez d'abord sélectionner un budget");
            return;
        }

        modalDepense.setVisible(true);
        modalDepense.setManaged(true);
        modalBudget.setVisible(false);
        modalBudget.setManaged(false);

        isEditingDepense = false;
        depenseEnCours = null;
        lblModalTitle.setText("Nouvelle Dépense");
        txtLibelleDepense.clear();
        txtMontantDepense.clear();
        txtNotesDepense.clear();
        cmbCategorieDepense.setValue(null);
        cmbPaiementDepense.setValue(null);
        cmbDeviseDepense.setValue(budgetActif.getDeviseBudget());
        dateDepense.setValue(LocalDate.now());
    }

    private void handleEditDepense(Depense depense) {
        modalDepense.setVisible(true);
        modalDepense.setManaged(true);
        modalBudget.setVisible(false);
        modalBudget.setManaged(false);

        isEditingDepense = true;
        depenseEnCours = depense;
        lblModalTitle.setText("Modifier la Dépense");

        txtLibelleDepense.setText(depense.getLibelleDepense());
        txtMontantDepense.setText(String.valueOf(depense.getMontantDepense()));
        txtNotesDepense.setText(depense.getDescriptionDepense());
        cmbCategorieDepense.setValue(depense.getCategorieDepense());
        cmbPaiementDepense.setValue(depense.getTypePaiement());
        cmbDeviseDepense.setValue(depense.getDeviseDepense() != null ? depense.getDeviseDepense() : budgetActif.getDeviseBudget());
        dateDepense.setValue(depense.getDateCreation().toLocalDate());
    }

    private void handleDeleteDepense(Depense depense) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer la dépense");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer cette dépense ?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    depenseCRUD.supprimer(depense.getIdDepense());
                    depensesList.remove(depense);
                    mettreAJourStats();
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Dépense supprimée avec succès !");
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer: " + e.getMessage());
                }
            }
        });
    }

    // FERMETURE DES MODALES
    @FXML
    void handleCloseModalBudget(ActionEvent event) {
        modalBudget.setVisible(false);
        modalBudget.setManaged(false);
    }

    @FXML
    void handleCloseModalDepense(ActionEvent event) {
        modalDepense.setVisible(false);
        modalDepense.setManaged(false);
    }

    // SAUVEGARDE BUDGET - CORRIGÉ POUR CORRESPONDRE À L'ENTITÉ BUDGET
    @FXML
    void handleSaveBudget(ActionEvent event) {
        if (!validateBudgetForm()) {
            return;
        }

        try {
            Budget budget;
            if (isEditingBudget && budgetActif != null) {
                budget = budgetActif;
            } else {
                budget = new Budget();
            }

            // Remplir les attributs selon l'entité Budget
            budget.setMontantTotal(Float.parseFloat(txtMontantBudget.getText()));
            budget.setDeviseBudget(cmbDeviseBudget.getValue());
            budget.setStatutBudget(cmbStatutBudget.getValue());
            budget.setDescriptionBudget(txtNomBudget.getText());

            // ID de l'utilisateur (à remplacer par l'ID de l'utilisateur connecté)
            budget.setId(1); // ID utilisateur par défaut

            // ID du voyage (0 par défaut si non associé)
            budget.setIdVoyage(0);

            // Valider l'objet avant sauvegarde
            budget.validate();

            if (isEditingBudget && budgetActif != null) {
                budgetCRUD.modifier(budget);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Budget modifié avec succès !");
            } else {
                budgetCRUD.ajouter(budget);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Budget créé avec succès !");
            }

            // Recharger les données
            chargerTousLesBudgets();
            chargerPremierBudget();
            modalBudget.setVisible(false);
            modalBudget.setManaged(false);

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Le montant doit être un nombre valide");
        } catch (IllegalArgumentException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de validation", e.getMessage());
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la sauvegarde: " + e.getMessage());
        }
    }

    // SAUVEGARDE DEPENSE
    @FXML
    void handleSaveDepense(ActionEvent event) {
        if (!validateDepenseForm()) {
            return;
        }

        if (budgetActif == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Aucun budget actif trouvé");
            return;
        }

        try {
            Depense depense;
            if (isEditingDepense && depenseEnCours != null) {
                depense = depenseEnCours;
            } else {
                depense = new Depense();
            }

            depense.setLibelleDepense(txtLibelleDepense.getText());
            depense.setMontantDepense(Float.parseFloat(txtMontantDepense.getText()));
            depense.setCategorieDepense(cmbCategorieDepense.getValue());
            depense.setTypePaiement(cmbPaiementDepense.getValue());
            depense.setDeviseDepense(cmbDeviseDepense.getValue());
            depense.setDescriptionDepense(txtNotesDepense.getText());
            depense.setDateCreation(Date.valueOf(dateDepense.getValue()));
            depense.setIdBudget(budgetActif.getIdBudget());

            if (isEditingDepense) {
                depenseCRUD.modifier(depense);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Dépense modifiée avec succès !");
            } else {
                depenseCRUD.ajouter(depense);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Dépense ajoutée avec succès !");
            }

            chargerDepenses();
            modalDepense.setVisible(false);
            modalDepense.setManaged(false);

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Le montant doit être un nombre valide");
        } catch (IllegalArgumentException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de validation", e.getMessage());
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la sauvegarde: " + e.getMessage());
        }
    }

    // VALIDATION BUDGET
    private boolean validateBudgetForm() {
        if (txtNomBudget.getText() == null || txtNomBudget.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Le nom du budget est requis");
            return false;
        }

        if (txtMontantBudget.getText() == null || txtMontantBudget.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Le montant total est requis");
            return false;
        }

        try {
            float montant = Float.parseFloat(txtMontantBudget.getText());
            if (montant <= 0) {
                showAlert(Alert.AlertType.WARNING, "Validation", "Le montant doit être positif");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Le montant doit être un nombre valide");
            return false;
        }

        if (cmbDeviseBudget.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Validation", "La devise est requise");
            return false;
        }

        if (cmbDeviseBudget.getValue().length() != 3) {
            showAlert(Alert.AlertType.WARNING, "Validation", "La devise doit avoir 3 caractères (ex: EUR, USD)");
            return false;
        }

        if (cmbStatutBudget.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Le statut est requis");
            return false;
        }

        return true;
    }

    // VALIDATION DEPENSE
    private boolean validateDepenseForm() {
        if (txtLibelleDepense.getText() == null || txtLibelleDepense.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Le libellé est requis");
            return false;
        }

        if (txtMontantDepense.getText() == null || txtMontantDepense.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Le montant est requis");
            return false;
        }

        try {
            float montant = Float.parseFloat(txtMontantDepense.getText());
            if (montant <= 0) {
                showAlert(Alert.AlertType.WARNING, "Validation", "Le montant doit être positif");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Le montant doit être un nombre valide");
            return false;
        }

        if (cmbCategorieDepense.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Validation", "La catégorie est requise");
            return false;
        }

        if (cmbPaiementDepense.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Le mode de paiement est requis");
            return false;
        }

        if (dateDepense.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Validation", "La date est requise");
            return false;
        }

        return true;
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}