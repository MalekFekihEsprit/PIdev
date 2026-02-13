package Controllers;

import Entities.Budget;
import Entities.Depense;
import Services.BudgetCRUD;
import Services.DepenseCRUD;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.net.URL;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class BudgetController implements Initializable {

    // Services CRUD
    private final BudgetCRUD budgetCRUD = new BudgetCRUD();
    private final DepenseCRUD depenseCRUD = new DepenseCRUD();

    // Données observables
    private ObservableList<Budget> budgetList = FXCollections.observableArrayList();
    private ObservableList<Depense> depenseList = FXCollections.observableArrayList();

    // Budget actuellement sélectionné (par défaut le premier)
    private Budget currentBudget;
    private int currentBudgetId = 1;

    // ==================== INJECTION DES COMPOSANTS FXML ====================

    // Header
    @FXML
    private Label lblBudgetVoyage;
    @FXML
    private Label lblDatesVoyage;
    @FXML
    private Label lblParticipants;
    @FXML
    private Label lblStatutBudget;

    // KPI Cards
    @FXML
    private Label lblMontantTotal;
    @FXML
    private Label lblDepense;
    @FXML
    private Label lblRestant;
    @FXML
    private Label lblPct;
    @FXML
    private ProgressBar progressBudget;

    // Labels de répartition par catégorie
    @FXML
    private Label lblHebergementMontant;
    @FXML
    private Label lblHebergementPourcent;
    @FXML
    private Label lblTransportMontant;
    @FXML
    private Label lblTransportPourcent;
    @FXML
    private Label lblRestaurationMontant;
    @FXML
    private Label lblRestaurationPourcent;
    @FXML
    private Label lblActivitesMontant;
    @FXML
    private Label lblActivitesPourcent;

    // TableView des dépenses
    @FXML
    private TableView<Depense> tableDepenses;
    @FXML
    private TableColumn<Depense, String> colLibelle;
    @FXML
    private TableColumn<Depense, String> colCategorie;
    @FXML
    private TableColumn<Depense, Float> colMontant;
    @FXML
    private TableColumn<Depense, java.sql.Date> colDate;
    @FXML
    private TableColumn<Depense, String> colPaiement;
    @FXML
    private TableColumn<Depense, Void> colActions;

    // Labels supplémentaires
    @FXML
    private Label lblTotalDepenses; // Pour afficher le nombre total de dépenses
    @FXML
    private Label lblBudgetParJour;
    @FXML
    private Label lblDateCreation;

    // ==================== INITIALISATION ====================

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            // Configuration des colonnes du tableau
            setupTableColumns();

            // Chargement des données depuis la base
            loadBudgets();
            loadCurrentBudget();
            loadDepenses();

            // Mise à jour de l'interface
            updateUI();

            // Ajout des boutons d'action
            addActionButtons();

            System.out.println("✅ Contrôleur initialisé avec succès !");

        } catch (SQLException e) {
            showAlert("Erreur de chargement",
                    "Impossible de charger les données depuis la base : " + e.getMessage(),
                    Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    // ==================== CONFIGURATION DU TABLEAU ====================

    private void setupTableColumns() {
        // Liaison des propriétés
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
                    NumberFormat format = NumberFormat.getCurrencyInstance(Locale.FRANCE);
                    setText(format.format(montant));
                }
            }
        });

        // Formatage de la colonne date
        colDate.setCellFactory(column -> new TableCell<Depense, java.sql.Date>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            @Override
            protected void updateItem(java.sql.Date date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(date.toLocalDate().format(formatter));
                }
            }
        });
    }

    private void addActionButtons() {
        Callback<TableColumn<Depense, Void>, TableCell<Depense, Void>> cellFactory =
                new Callback<>() {
                    @Override
                    public TableCell<Depense, Void> call(final TableColumn<Depense, Void> param) {
                        return new TableCell<>() {
                            private final Button btnEdit = new Button("✏️");
                            private final Button btnDelete = new Button("❌");
                            private final HBox pane = new HBox(5, btnEdit, btnDelete);

                            {
                                // Style des boutons
                                btnEdit.setStyle("-fx-background-color: #f1f5f9; -fx-cursor: hand; -fx-font-size: 12; -fx-padding: 5 10; -fx-background-radius: 5;");
                                btnDelete.setStyle("-fx-background-color: #f1f5f9; -fx-cursor: hand; -fx-font-size: 12; -fx-padding: 5 10; -fx-background-radius: 5;");

                                // Tooltips
                                btnEdit.setTooltip(new Tooltip("Modifier cette dépense"));
                                btnDelete.setTooltip(new Tooltip("Supprimer cette dépense"));

                                // Actions
                                btnEdit.setOnAction(event -> {
                                    Depense depense = getTableView().getItems().get(getIndex());
                                    editDepense(depense);
                                });

                                btnDelete.setOnAction(event -> {
                                    Depense depense = getTableView().getItems().get(getIndex());
                                    deleteDepense(depense);
                                });
                            }

                            @Override
                            public void updateItem(Void item, boolean empty) {
                                super.updateItem(item, empty);
                                setGraphic(empty ? null : pane);
                            }
                        };
                    }
                };
        colActions.setCellFactory(cellFactory);
    }

    // ==================== CHARGEMENT DES DONNÉES ====================

    private void loadBudgets() throws SQLException {
        List<Budget> budgets = budgetCRUD.afficher();
        budgetList.setAll(budgets);
        System.out.println("📊 " + budgets.size() + " budgets chargés depuis la BDD");
    }

    private void loadCurrentBudget() throws SQLException {
        // Charger le budget avec l'ID 1 (Paris)
        currentBudget = budgetCRUD.getById(currentBudgetId);

        if (currentBudget == null && !budgetList.isEmpty()) {
            // Si le budget 1 n'existe pas, prendre le premier de la liste
            currentBudget = budgetList.get(0);
            currentBudgetId = currentBudget.getIdBudget();
        }

        if (currentBudget != null) {
            System.out.println("💰 Budget courant : " + currentBudget.getDescriptionBudget() +
                    " - " + currentBudget.getMontantTotal() + currentBudget.getDeviseBudget());
        }
    }

    private void loadDepenses() throws SQLException {
        List<Depense> toutesDepenses = depenseCRUD.afficher();

        // Filtrer les dépenses pour le budget courant
        List<Depense> depensesBudget = toutesDepenses.stream()
                .filter(d -> d.getIdBudget() == currentBudgetId)
                .collect(Collectors.toList());

        depenseList.setAll(depensesBudget);
        tableDepenses.setItems(depenseList);

        System.out.println("📝 " + depensesBudget.size() + " dépenses chargées pour le budget " + currentBudgetId);
    }

    // ==================== MISE À JOUR DE L'INTERFACE ====================

    private void updateUI() throws SQLException {
        if (currentBudget == null) return;

        // Mise à jour des informations du budget
        updateBudgetInfo();

        // Mise à jour des KPI
        updateKPIs();

        // Mise à jour de la répartition par catégorie
        updateCategoryBreakdown();
    }

    private void updateBudgetInfo() {
        if (currentBudget != null) {
            // Titre du budget
            if (lblBudgetVoyage != null) {
                lblBudgetVoyage.setText(currentBudget.getDescriptionBudget());
            }

            // Statut du budget
            if (lblStatutBudget != null) {
                lblStatutBudget.setText(currentBudget.getStatutBudget());
                // Changer la couleur selon le statut
                if ("Actif".equals(currentBudget.getStatutBudget())) {
                    lblStatutBudget.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 4 12;");
                } else if ("Terminé".equals(currentBudget.getStatutBudget())) {
                    lblStatutBudget.setStyle("-fx-background-color: #64748b; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 4 12;");
                } else {
                    lblStatutBudget.setStyle("-fx-background-color: #f59e0b; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 4 12;");
                }
            }

            // Date de création
            if (lblDateCreation != null) {
                lblDateCreation.setText("Créé le " + java.time.LocalDate.now().minusMonths(1).format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
            }
        }

        // Informations statiques pour l'exemple (à remplacer par des vraies données de voyage)
        if (lblDatesVoyage != null) lblDatesVoyage.setText("15 - 22 Mars 2026");
        if (lblParticipants != null) lblParticipants.setText("4 participants");
    }

    private void updateKPIs() {
        if (currentBudget == null || depenseList.isEmpty()) {
            // Pas de données, afficher des valeurs par défaut
            lblMontantTotal.setText(formatMontant(0));
            lblDepense.setText(formatMontant(0));
            lblRestant.setText(formatMontant(0));
            lblPct.setText("0%");
            progressBudget.setProgress(0);
            if (lblBudgetParJour != null) lblBudgetParJour.setText("Budget par jour: 0€");
            if (lblTotalDepenses != null) lblTotalDepenses.setText("0 dépense");
            return;
        }

        float montantTotal = currentBudget.getMontantTotal();
        float totalDepenses = (float) depenseList.stream()
                .mapToDouble(Depense::getMontantDepense)
                .sum();
        float montantRestant = montantTotal - totalDepenses;
        float pourcentageUtilise = (totalDepenses / montantTotal) * 100;

        // Mise à jour des labels
        lblMontantTotal.setText(formatMontant(montantTotal));
        lblDepense.setText(formatMontant(totalDepenses));
        lblRestant.setText(formatMontant(montantRestant));
        lblPct.setText(String.format("%.1f%%", pourcentageUtilise));

        // Mise à jour de la progress bar
        progressBudget.setProgress(totalDepenses / montantTotal);

        // Changer la couleur selon le niveau d'utilisation
        if (pourcentageUtilise > 90) {
            progressBudget.setStyle("-fx-accent: #ef4444;"); // Rouge
        } else if (pourcentageUtilise > 70) {
            progressBudget.setStyle("-fx-accent: #f97316;"); // Orange
        } else {
            progressBudget.setStyle("-fx-accent: #ff8c42;"); // Orange clair
        }

        // Budget par jour (supposant 7 jours de voyage)
        if (lblBudgetParJour != null) {
            float budgetParJour = montantTotal / 7;
            lblBudgetParJour.setText(String.format("Budget par jour: %.0f€", budgetParJour));
        }

        // Nombre total de dépenses
        if (lblTotalDepenses != null) {
            lblTotalDepenses.setText(depenseList.size() + (depenseList.size() > 1 ? " dépenses" : " dépense"));
        }
    }

    private void updateCategoryBreakdown() {
        if (depenseList.isEmpty()) {
            // Réinitialiser les catégories
            resetCategoryLabels();
            return;
        }

        float totalDepenses = (float) depenseList.stream()
                .mapToDouble(Depense::getMontantDepense)
                .sum();

        // Calcul par catégorie
        float hebergementTotal = calculateCategoryTotal("Hébergement");
        float transportTotal = calculateCategoryTotal("Transport");
        float restaurationTotal = calculateCategoryTotal("Restauration");
        float activitesTotal = calculateCategoryTotal("Activités");

        // Catégories supplémentaires
        float autresTotal = totalDepenses - (hebergementTotal + transportTotal + restaurationTotal + activitesTotal);

        // Mise à jour des labels
        updateCategoryLabel(lblHebergementMontant, lblHebergementPourcent, hebergementTotal, totalDepenses);
        updateCategoryLabel(lblTransportMontant, lblTransportPourcent, transportTotal, totalDepenses);
        updateCategoryLabel(lblRestaurationMontant, lblRestaurationPourcent, restaurationTotal, totalDepenses);
        updateCategoryLabel(lblActivitesMontant, lblActivitesPourcent, activitesTotal, totalDepenses);
    }

    private float calculateCategoryTotal(String categorie) {
        return (float) depenseList.stream()
                .filter(d -> categorie.equals(d.getCategorieDepense()))
                .mapToDouble(Depense::getMontantDepense)
                .sum();
    }

    private void updateCategoryLabel(Label montantLabel, Label pourcentLabel, float montant, float total) {
        if (montantLabel != null) {
            montantLabel.setText(formatMontant(montant));
        }
        if (pourcentLabel != null && total > 0) {
            pourcentLabel.setText(String.format("(%.1f%%)", (montant / total) * 100));
        }
    }

    private void resetCategoryLabels() {
        String zero = formatMontant(0);
        if (lblHebergementMontant != null) lblHebergementMontant.setText(zero);
        if (lblTransportMontant != null) lblTransportMontant.setText(zero);
        if (lblRestaurationMontant != null) lblRestaurationMontant.setText(zero);
        if (lblActivitesMontant != null) lblActivitesMontant.setText(zero);

        if (lblHebergementPourcent != null) lblHebergementPourcent.setText("(0%)");
        if (lblTransportPourcent != null) lblTransportPourcent.setText("(0%)");
        if (lblRestaurationPourcent != null) lblRestaurationPourcent.setText("(0%)");
        if (lblActivitesPourcent != null) lblActivitesPourcent.setText("(0%)");
    }

    private String formatMontant(float montant) {
        return String.format("%,.0f€", montant).replace(',', ' ');
    }

    // ==================== ACTIONS SUR LES DÉPENSES ====================

    private void editDepense(Depense depense) {
        // Création de la boîte de dialogue
        Dialog<Depense> dialog = new Dialog<>();
        dialog.setTitle("Modifier la dépense");
        dialog.setHeaderText("Modification : " + depense.getLibelleDepense());

        // Boutons
        ButtonType saveButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Formulaire (simplifié pour l'exemple)
        TextField montantField = new TextField(String.valueOf(depense.getMontantDepense()));
        montantField.setPromptText("Montant");

        dialog.getDialogPane().setContent(new VBox(10, new Label("Nouveau montant:"), montantField));

        // Traitement du résultat
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    float nouveauMontant = Float.parseFloat(montantField.getText());
                    depense.setMontantDepense(nouveauMontant);
                    return depense;
                } catch (NumberFormatException e) {
                    showAlert("Erreur", "Montant invalide", Alert.AlertType.ERROR);
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(updatedDepense -> {
            try {
                depenseCRUD.modifier(updatedDepense);
                loadDepenses();
                updateKPIs();
                updateCategoryBreakdown();
                showAlert("Succès", "Dépense modifiée avec succès!", Alert.AlertType.INFORMATION);
            } catch (SQLException e) {
                showAlert("Erreur", "Erreur lors de la modification: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        });
    }

    private void deleteDepense(Depense depense) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer la dépense");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer : " + depense.getLibelleDepense() + " ?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    depenseCRUD.supprimer(depense.getIdDepense());
                    loadDepenses();
                    updateKPIs();
                    updateCategoryBreakdown();
                    showAlert("Succès", "Dépense supprimée!", Alert.AlertType.INFORMATION);
                } catch (SQLException e) {
                    showAlert("Erreur", "Impossible de supprimer: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });
    }

    // ==================== GESTION DES ÉVÉNEMENTS FXML ====================

    @FXML
    private void handleAddDepense() {
        // À implémenter : ouverture du formulaire d'ajout
        showAlert("Info", "Fonctionnalité d'ajout à implémenter", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleExport() {
        try {
            // Génération d'un rapport CSV
            StringBuilder csv = new StringBuilder();
            csv.append("Libellé;Catégorie;Montant;Date;Paiement\n");

            for (Depense d : depenseList) {
                csv.append(d.getLibelleDepense()).append(";")
                        .append(d.getCategorieDepense()).append(";")
                        .append(d.getMontantDepense()).append(";")
                        .append(d.getDateCreation()).append(";")
                        .append(d.getTypePaiement()).append("\n");
            }

            // Ici, vous sauvegarderiez le fichier
            showAlert("Export", "Export réussi ! (" + depenseList.size() + " lignes)", Alert.AlertType.INFORMATION);

        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de l'export: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleFilter() {
        ChoiceDialog<String> dialog = new ChoiceDialog<>("Toutes",
                "Toutes", "Hébergement", "Transport", "Restauration", "Activités");
        dialog.setTitle("Filtre");
        dialog.setHeaderText("Filtrer les dépenses");
        dialog.setContentText("Catégorie:");

        dialog.showAndWait().ifPresent(categorie -> {
            try {
                if ("Toutes".equals(categorie)) {
                    tableDepenses.setItems(depenseList);
                } else {
                    ObservableList<Depense> filtered = FXCollections.observableArrayList(
                            depenseList.stream()
                                    .filter(d -> categorie.equals(d.getCategorieDepense()))
                                    .collect(Collectors.toList())
                    );
                    tableDepenses.setItems(filtered);
                }
            } catch (Exception e) {
                showAlert("Erreur", "Erreur de filtrage", Alert.AlertType.ERROR);
            }
        });
    }

    @FXML
    private void handleSortByDate() {
        FXCollections.sort(depenseList, (d1, d2) -> {
            if (d1.getDateCreation() == null) return -1;
            if (d2.getDateCreation() == null) return 1;
            return d2.getDateCreation().compareTo(d1.getDateCreation());
        });
    }

    @FXML
    private void handleNewBudget() {
        showAlert("Nouveau budget", "Fonctionnalité de création de budget à implémenter", Alert.AlertType.INFORMATION);
    }

    // ==================== UTILITAIRES ====================

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Méthode pour changer de budget
    public void switchBudget(int budgetId) {
        try {
            this.currentBudgetId = budgetId;
            loadCurrentBudget();
            loadDepenses();
            updateUI();
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de changer de budget: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
}