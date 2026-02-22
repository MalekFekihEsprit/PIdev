package Controllers;

import Entities.Budget;
import Entities.TimeCapsule;
import Services.BudgetCRUD;
import Services.TimeCapsuleCRUD;
import Services.FREDInflationService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.net.URL;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class TimeCapsuleController implements Initializable {

    @FXML private VBox capsuleContainer;
    @FXML private ListView<HBox> capsuleListView;
    @FXML private Label lblCapsuleCount;
    @FXML private ComboBox<Budget> cmbBudgetSelection;
    @FXML private TextField txtCapsuleNom;
    @FXML private TextField txtEmailNotification;
    @FXML private ComboBox<String> cmbPaysDestination;
    @FXML private Button btnCreerCapsule;

    private TimeCapsuleCRUD capsuleCRUD = new TimeCapsuleCRUD();
    private BudgetCRUD budgetCRUD = new BudgetCRUD();
    private FREDInflationService inflationService = new FREDInflationService();
    private DecimalFormat df = new DecimalFormat("#,##0.00");
    private int currentUserId = 1; // À remplacer par l'utilisateur connecté

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        chargerBudgets();
        chargerPaysSupportes();
        chargerCapsules();
    }

    /**
     * Charger les budgets de l'utilisateur
     */
    private void chargerBudgets() {
        try {
            List<Budget> budgets = budgetCRUD.getBudgetsByUserId(currentUserId);
            ObservableList<Budget> budgetItems = FXCollections.observableArrayList(budgets);
            cmbBudgetSelection.setItems(budgetItems);

            // Format d'affichage
            cmbBudgetSelection.setCellFactory(param -> new ListCell<Budget>() {
                @Override
                protected void updateItem(Budget budget, boolean empty) {
                    super.updateItem(budget, empty);
                    if (empty || budget == null) {
                        setText(null);
                    } else {
                        setText(budget.getLibelleBudget() + " - " +
                                df.format(budget.getMontantTotal()) + " " +
                                budget.getDeviseBudget());
                    }
                }
            });

            cmbBudgetSelection.setButtonCell(new ListCell<Budget>() {
                @Override
                protected void updateItem(Budget budget, boolean empty) {
                    super.updateItem(budget, empty);
                    if (empty || budget == null) {
                        setText(null);
                    } else {
                        setText(budget.getLibelleBudget());
                    }
                }
            });

        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les budgets: " + e.getMessage());
        }
    }

    /**
     * Charger la liste des pays supportés
     */
    private void chargerPaysSupportes() {
        ObservableList<String> pays = FXCollections.observableArrayList();

        // Ajouter les pays manuellement ou depuis FREDInflationService
        pays.addAll(
                "France (FR)", "États-Unis (US)", "Royaume-Uni (GB)",
                "Allemagne (DE)", "Italie (IT)", "Espagne (ES)",
                "Japon (JP)", "Canada (CA)", "Australie (AU)",
                "Tunisie (TN)", "Maroc (MA)", "Afrique du Sud (ZA)",
                "Chine (CN)", "Inde (IN)", "Brésil (BR)"
        );

        cmbPaysDestination.setItems(pays);
        cmbPaysDestination.getSelectionModel().selectFirst();
    }

    /**
     * Charger les capsules existantes
     */
    private void chargerCapsules() {
        try {
            List<TimeCapsule> capsules = capsuleCRUD.getCapsulesByUser(currentUserId);
            lblCapsuleCount.setText(capsules.size() + " capsule(s)");

            capsuleListView.getItems().clear();

            for (TimeCapsule capsule : capsules) {
                HBox capsuleItem = creerItemCapsule(capsule);
                capsuleListView.getItems().add(capsuleItem);
            }

        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les capsules: " + e.getMessage());
        }
    }

    /**
     * Créer un élément graphique pour une capsule
     */
    private HBox creerItemCapsule(TimeCapsule capsule) {
        HBox box = new HBox(15);
        box.setStyle("-fx-padding: 15; -fx-background-color: #f8fafc; " +
                "-fx-background-radius: 12; -fx-border-color: #e2e8f0; " +
                "-fx-border-radius: 12;");

        // Icône
        Label iconLabel = new Label(capsule.isEstReouverte() ? "🔓" : "⏳");
        iconLabel.setStyle("-fx-font-size: 28;");

        // Détails
        VBox details = new VBox(5);
        Label titreLabel = new Label(capsule.getLibelleCapsule());
        titreLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");

        Label destinationLabel = new Label("📍 " + capsule.getDestination());
        destinationLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12;");

        String dateReouverture = capsule.getDateReouverture().toLocalDate()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        Label dateLabel = new Label("📅 Réouverture: " + dateReouverture);
        dateLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11;");

        Label montantLabel = new Label(String.format("💰 %.2f %s",
                capsule.getMontantInitial(), capsule.getDevise()));
        montantLabel.setStyle("-fx-font-size: 13; -fx-font-weight: 600;");

        details.getChildren().addAll(titreLabel, destinationLabel, dateLabel, montantLabel);

        // Bouton d'action
        Button actionButton = new Button();

        if (capsule.isEstReouverte()) {
            actionButton.setText("✓ Ouvert");
            actionButton.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; " +
                    "-fx-background-radius: 12; -fx-padding: 8 16;");
            actionButton.setDisable(true);

            // Ajouter la valeur ajustée
            Label valeurLabel = new Label(String.format("Vaut aujourd'hui: %.2f %s",
                    capsule.getMontantAjuste(), capsule.getDevise()));
            valeurLabel.setStyle("-fx-text-fill: #10b981; -fx-font-size: 11; -fx-font-weight: 600;");
            details.getChildren().add(valeurLabel);

        } else if (capsule.getDateReouverture().toLocalDate().isBefore(LocalDate.now())) {
            actionButton.setText("🔓 Ouvrir");
            actionButton.setStyle("-fx-background-color: #ff8c42; -fx-text-fill: white; " +
                    "-fx-background-radius: 12; -fx-padding: 8 16;");
            actionButton.setOnAction(e -> ouvrirCapsule(capsule));
        } else {
            actionButton.setText("⏳ En attente");
            actionButton.setStyle("-fx-background-color: #94a3b8; -fx-text-fill: white; " +
                    "-fx-background-radius: 12; -fx-padding: 8 16;");
            actionButton.setDisable(true);
        }

        Region spacer = new Region();
        spacer.setPrefWidth(20);

        box.getChildren().addAll(iconLabel, details, spacer, actionButton);

        return box;
    }

    /**
     * Créer une nouvelle capsule
     */
    @FXML
    private void handleCreerCapsule() {
        // Validation
        if (cmbBudgetSelection.getValue() == null) {
            showAlert("Erreur", "Veuillez sélectionner un budget");
            return;
        }

        if (txtCapsuleNom.getText().trim().isEmpty()) {
            showAlert("Erreur", "Veuillez donner un nom à la capsule");
            return;
        }

        if (txtEmailNotification.getText().trim().isEmpty()) {
            showAlert("Erreur", "Veuillez saisir un email");
            return;
        }

        if (cmbPaysDestination.getValue() == null) {
            showAlert("Erreur", "Veuillez sélectionner une destination");
            return;
        }

        try {
            Budget budget = cmbBudgetSelection.getValue();
            String paysComplet = cmbPaysDestination.getValue();
            String paysCode = paysComplet.substring(paysComplet.indexOf("(") + 1,
                    paysComplet.indexOf(")"));

            // Créer la capsule
            TimeCapsule capsule = new TimeCapsule(
                    budget.getIdBudget(),
                    txtCapsuleNom.getText().trim(),
                    budget.getMontantTotal(),
                    budget.getDeviseBudget(),
                    paysComplet.split(" \\(")[0], // Nom de la destination
                    paysCode,
                    txtEmailNotification.getText().trim()
            );

            capsuleCRUD.creerCapsule(capsule);

            showAlert("Succès", "Capsule créée avec succès ! Vous serez notifié dans 1 an.");

            // Réinitialiser le formulaire
            txtCapsuleNom.clear();
            txtEmailNotification.clear();
            cmbBudgetSelection.getSelectionModel().clearSelection();

            // Recharger la liste
            chargerCapsules();

        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de créer la capsule: " + e.getMessage());
        }
    }

    /**
     * Ouvrir une capsule
     */
    private void ouvrirCapsule(TimeCapsule capsule) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Ouverture de capsule");
        confirm.setHeaderText("Ouvrir la capsule \"" + capsule.getLibelleCapsule() + "\" ?");
        confirm.setContentText("Vous allez découvrir la valeur ajustée avec l'inflation.");

        if (confirm.showAndWait().get() == ButtonType.OK) {
            try {
                capsuleCRUD.ouvrirCapsule(capsule.getIdCapsule());

                // Recharger la liste
                chargerCapsules();

                // Afficher le résultat
                TimeCapsule capsuleMaj = capsuleCRUD.getCapsuleById(capsule.getIdCapsule());
                afficherResultatCapsule(capsuleMaj);

            } catch (SQLException e) {
                showAlert("Erreur", "Impossible d'ouvrir la capsule: " + e.getMessage());
            }
        }
    }

    /**
     * Afficher le résultat de l'ouverture
     */
    private void afficherResultatCapsule(TimeCapsule capsule) {
        double difference = capsule.getMontantAjuste() - capsule.getMontantInitial();
        String signe = difference > 0 ? "plus" : "moins";

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("🎉 Capsule ouverte !");
        alert.setHeaderText("Il y a un an, vous avez dépensé " +
                df.format(capsule.getMontantInitial()) + " " + capsule.getDevise() +
                " à " + capsule.getDestination());

        String message = String.format(
                "Aujourd'hui, avec l'inflation, cette somme vaudrait:\n\n" +
                        "💰 %.2f %s\n\n" +
                        "Soit %.2f %s de %s qu'à l'époque (%.1f%% d'inflation).\n\n" +
                        "✨ Les souvenirs n'ont pas de prix, mais ils prennent de la valeur !",
                capsule.getMontantAjuste(), capsule.getDevise(),
                Math.abs(difference), capsule.getDevise(), signe,
                capsule.getTauxInflationCalcule()
        );

        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}