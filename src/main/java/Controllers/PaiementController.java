package Controllers;

import Entities.Paiement;
import Entities.Voyage;
import Services.EmailService;
import Services.PaiementCRUDV;
import Services.StripeService;
import Services.VoyageCRUDV;
import Utils.ConfigV;
import com.google.gson.JsonObject;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class PaiementController implements Initializable {

    @FXML
    private Label lblTitreVoyage;
    @FXML
    private Label lblDestinationVoyage;
    @FXML
    private Label lblPrixVoyage;
    @FXML
    private Label lblDatesVoyage;
    @FXML
    private TextField tfMontant;
    @FXML
    private ComboBox<String> cbDevise;
    @FXML
    private TextField tfEmail;
    @FXML
    private TextField tfDescription;
    @FXML
    private Button btnStripe;
    @FXML
    private Button btnRetour;
    @FXML
    private TableView<Paiement> tablePaiements;
    @FXML
    private TableColumn<Paiement, Integer> colId;
    @FXML
    private TableColumn<Paiement, String> colMethode;
    @FXML
    private TableColumn<Paiement, Double> colMontant;
    @FXML
    private TableColumn<Paiement, String> colDevise;
    @FXML
    private TableColumn<Paiement, String> colStatut;
    @FXML
    private TableColumn<Paiement, String> colTransaction;
    @FXML
    private TableColumn<Paiement, Timestamp> colDate;
    @FXML
    private TableColumn<Paiement, String> colEmail;
    @FXML
    private TableColumn<Paiement, Void> colActions;
    @FXML
    private Label lblTotalPaiements;
    @FXML
    private Label lblMontantTotal;

    private int idVoyage;
    private Voyage voyage;
    private VoyageCRUDV voyageCRUD = new VoyageCRUDV();
    private PaiementCRUDV paiementCRUD = new PaiementCRUDV();
    private StripeService stripeService = new StripeService();
    private EmailService emailService = EmailService.getInstance();
    private ObservableList<Paiement> paiementList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        colId.setCellValueFactory(new PropertyValueFactory<>("id_paiement"));
        colMethode.setCellValueFactory(new PropertyValueFactory<>("methode"));
        colMontant.setCellValueFactory(new PropertyValueFactory<>("montant"));
        colDevise.setCellValueFactory(new PropertyValueFactory<>("devise"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colTransaction.setCellValueFactory(new PropertyValueFactory<>("transactionId"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date_paiement"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email_payeur"));

        ajouterBoutonsActions();

        cbDevise.getItems().addAll("EUR", "USD", "GBP");
        cbDevise.setValue("EUR");

        btnStripe.setOnAction(this::payerAvecStripe);
        btnRetour.setOnAction(this::retourVoyages);

        tfMontant.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*")) {
                tfMontant.setText(oldVal);
            }
        });
    }

    private void ajouterBoutonsActions() {
        Callback<TableColumn<Paiement, Void>, TableCell<Paiement, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<Paiement, Void> call(final TableColumn<Paiement, Void> param) {
                return new TableCell<>() {
                    private final Button btnRembourser = new Button("↩️ Rembourser");
                    private final Button btnDetails = new Button("👁️ Détails");
                    private final HBox pane = new HBox(5, btnDetails, btnRembourser);

                    {
                        btnDetails.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 4 8; -fx-font-size: 11; -fx-cursor: hand;");
                        btnRembourser.setStyle("-fx-background-color: #ec4899; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 4 8; -fx-font-size: 11; -fx-cursor: hand;");

                        btnDetails.setOnAction(event -> {
                            Paiement paiement = getTableView().getItems().get(getIndex());
                            voirDetailsPaiement(paiement);
                        });

                        btnRembourser.setOnAction(event -> {
                            Paiement paiement = getTableView().getItems().get(getIndex());
                            rembourserPaiement(paiement);
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

    public void initData(int idVoyage) {
        this.idVoyage = idVoyage;

        try {
            List<Voyage> voyages = voyageCRUD.afficher();
            for (Voyage v : voyages) {
                if (v.getId_voyage() == idVoyage) {
                    this.voyage = v;
                    break;
                }
            }

            if (voyage != null) {
                String nomDestination = voyageCRUD.getNomDestination(voyage.getId_destination());

                lblTitreVoyage.setText(voyage.getTitre_voyage());
                lblDestinationVoyage.setText(nomDestination);
                lblDatesVoyage.setText(voyage.getDate_debut() + " - " + voyage.getDate_fin());

                double prix = 150.0;
                lblPrixVoyage.setText(prix + " €");
                tfMontant.setText(String.valueOf(prix));
                tfDescription.setText("Paiement pour le voyage : " + voyage.getTitre_voyage());
                tfEmail.setText("client@example.com");
            }

            chargerPaiements();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les données: " + e.getMessage());
        }
    }

    private void chargerPaiements() {
        try {
            List<Paiement> paiements = paiementCRUD.getPaiementsByVoyage(idVoyage);
            paiementList.clear();
            paiementList.addAll(paiements);
            tablePaiements.setItems(paiementList);

            double montantTotal = paiements.stream()
                    .filter(p -> "COMPLETE".equals(p.getStatut()))
                    .mapToDouble(Paiement::getMontant)
                    .sum();

            lblTotalPaiements.setText(String.valueOf(paiements.size()));
            lblMontantTotal.setText(String.format("%.2f €", montantTotal));

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les paiements: " + e.getMessage());
        }
    }

    @FXML
    private void payerAvecStripe(ActionEvent event) {
        if (!validerFormulaire()) return;

        try {
            double montant = Double.parseDouble(tfMontant.getText());
            long montantCentimes = (long) (montant * 100);
            String devise = cbDevise.getValue().toLowerCase();
            String description = tfDescription.getText();
            String email = tfEmail.getText();

            if (!ConfigV.areStripeKeysConfigured()) {
                showAlert(Alert.AlertType.ERROR, "Configuration manquante",
                        "Les clés API Stripe ne sont pas configurées.\n" +
                                "Veuillez les configurer dans le fichier Config.java");
                return;
            }

            JsonObject paymentIntent = stripeService.createPaymentIntent(
                    montantCentimes,
                    devise,
                    description,
                    email
            );

            String paymentIntentId = paymentIntent.get("id").getAsString();
            String clientSecret = paymentIntent.get("client_secret").getAsString();

            Paiement paiement = new Paiement(
                    idVoyage,
                    1,
                    montant,
                    "STRIPE",
                    description,
                    email
            );
            paiement.setTransactionId(paymentIntentId);
            paiementCRUD.ajouter(paiement);

            showAlert(Alert.AlertType.INFORMATION, "Paiement Stripe",
                    "PaymentIntent créé avec succès !\n" +
                            "ID: " + paymentIntentId + "\n" +
                            "Client Secret: " + clientSecret);

            completerPaiement(paymentIntentId);

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Montant invalide");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du paiement Stripe: " + e.getMessage());
        }
    }

    private void completerPaiement(String transactionId) {
        try {
            Paiement paiement = paiementCRUD.getPaiementByTransactionId(transactionId);
            if (paiement != null) {
                paiement.setStatut("COMPLETE");
                paiementCRUD.modifier(paiement);
                chargerPaiements();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Paiement complété avec succès !");

                // Envoyer l'email de confirmation
                envoyerEmailConfirmation(paiement);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void envoyerEmailConfirmation(Paiement paiement) {
        if (paiement.getEmail_payeur() == null || paiement.getEmail_payeur().isEmpty()) return;

        new Thread(() -> {
            try {
                String nomDestination = voyage != null ? voyageCRUD.getNomDestination(voyage.getId_destination()) : "N/A";
                String dates = voyage != null ? (voyage.getDate_debut() + " - " + voyage.getDate_fin()) : "N/A";
                String nomVoyage = voyage != null ? voyage.getTitre_voyage() : "N/A";

                emailService.envoyerConfirmationPaiement(
                        paiement.getEmail_payeur(),
                        nomVoyage,
                        nomDestination,
                        dates,
                        paiement.getMontant(),
                        paiement.getDevise(),
                        paiement.getTransactionId(),
                        paiement.getMethode()
                );
            } catch (Exception e) {
                System.out.println("⚠️ Erreur envoi email confirmation: " + e.getMessage());
            }
        }).start();
    }

    private void rembourserPaiement(Paiement paiement) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de remboursement");
        alert.setHeaderText(null);
        alert.setContentText("Voulez-vous vraiment rembourser ce paiement de " +
                paiement.getMontant() + " " + paiement.getDevise() + " ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                if ("STRIPE".equals(paiement.getMethode())) {
                    // ✅ CORRECTION : Appel avec 1 paramètre au lieu de 2
                    JsonObject refundResponse = stripeService.refundPayment(paiement.getTransactionId());
                    String refundId = refundResponse.get("id").getAsString();
                    showAlert(Alert.AlertType.INFORMATION, "Info",
                            "Remboursement Stripe effectué: " + refundId);
                }

                paiement.setStatut("REMBOURSE");
                paiementCRUD.modifier(paiement);
                chargerPaiements();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Remboursement effectué avec succès !");

                // Envoyer l'email de remboursement
                if (paiement.getEmail_payeur() != null && !paiement.getEmail_payeur().isEmpty()) {
                    Paiement refundPaiement = paiement;
                    new Thread(() -> {
                        try {
                            String nomVoyage = voyage != null ? voyage.getTitre_voyage() : "N/A";
                            emailService.envoyerConfirmationRemboursement(
                                    refundPaiement.getEmail_payeur(),
                                    nomVoyage,
                                    refundPaiement.getMontant(),
                                    refundPaiement.getDevise(),
                                    refundPaiement.getTransactionId()
                            );
                        } catch (Exception ex) {
                            System.out.println("⚠️ Erreur envoi email remboursement: " + ex.getMessage());
                        }
                    }).start();
                }

            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du remboursement: " + e.getMessage());
            }
        }
    }

    private void voirDetailsPaiement(Paiement paiement) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails du paiement");
        alert.setHeaderText("Paiement #" + paiement.getId_paiement());

        String details = String.format(
                "ID Transaction: %s\nMéthode: %s\nMontant: %.2f %s\nStatut: %s\nDate: %s\nEmail: %s\nDescription: %s",
                paiement.getTransactionId() != null ? paiement.getTransactionId() : "N/A",
                paiement.getMethode(),
                paiement.getMontant(),
                paiement.getDevise(),
                paiement.getStatut(),
                paiement.getDate_paiement() != null ? paiement.getDate_paiement().toString() : "N/A",
                paiement.getEmail_payeur() != null ? paiement.getEmail_payeur() : "N/A",
                paiement.getDescription() != null ? paiement.getDescription() : "N/A"
        );

        alert.setContentText(details);
        alert.showAndWait();
    }

    private boolean validerFormulaire() {
        if (tfMontant.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez saisir un montant");
            return false;
        }
        if (tfEmail.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez saisir un email");
            return false;
        }
        if (!tfEmail.getText().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Email invalide");
            return false;
        }
        try {
            double montant = Double.parseDouble(tfMontant.getText());
            if (montant <= 0) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Le montant doit être supérieur à 0");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Montant invalide");
            return false;
        }
        return true;
    }

    @FXML
    private void retourVoyages(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/PageVoyage.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnRetour.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de retourner à la page des voyages");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}