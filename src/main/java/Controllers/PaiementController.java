package Controllers;

import Entities.Paiement;
import Entities.Voyage;
import Entities.User;
import Services.EmailService;
import Services.PaiementCRUDV;
import Services.StripeService;
import Services.VoyageCRUDV;
import Utils.ConfigV;
import Utils.UserSession;
import com.google.gson.JsonObject;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class PaiementController implements Initializable {

    // ============== FXML INJECTED ELEMENTS ==============

    // Top Navigation
    @FXML private HBox btnDestinations;
    @FXML private HBox btnHebergement;
    @FXML private HBox btnCategories;
    @FXML private HBox btnActivites;
    @FXML private HBox btnVoyages;
    @FXML private HBox btnBudgets;
    @FXML private HBox btnHome;
    @FXML private HBox userProfileBox;
    @FXML private HBox btnNotifications;
    @FXML private HBox btnRefresh;
    @FXML private Label lblNotificationBadge;
    @FXML private Label lblUserName;
    @FXML private Label lblUserRole;

    // Bottom Status
    @FXML private Label lblLastUpdate; // You might need to add this to your FXML

    // Voyage Header
    @FXML private Label lblTitreVoyage;
    @FXML private Label lblDestinationVoyage;
    @FXML private Label lblPrixVoyage;
    @FXML private Label lblDatesVoyage;

    // Payment Form
    @FXML private TextField tfMontant;
    @FXML private ComboBox<String> cbDevise;
    @FXML private TextField tfEmail;
    @FXML private TextField tfDescription;
    @FXML private Button btnStripe;
    @FXML private Button btnRetour;

    // Payment History
    @FXML private TableView<Paiement> tablePaiements;
    @FXML private TableColumn<Paiement, Integer> colId;
    @FXML private TableColumn<Paiement, String> colMethode;
    @FXML private TableColumn<Paiement, Double> colMontant;
    @FXML private TableColumn<Paiement, String> colDevise;
    @FXML private TableColumn<Paiement, String> colStatut;
    @FXML private TableColumn<Paiement, String> colTransaction;
    @FXML private TableColumn<Paiement, Timestamp> colDate;
    @FXML private TableColumn<Paiement, String> colEmail;
    @FXML private TableColumn<Paiement, Void> colActions;
    @FXML private Label lblTotalPaiements;
    @FXML private Label lblMontantTotal;

    // ============== CLASS VARIABLES ==============

    private int idVoyage;
    private Voyage voyage;
    private VoyageCRUDV voyageCRUD = new VoyageCRUDV();
    private PaiementCRUDV paiementCRUD = new PaiementCRUDV();
    private StripeService stripeService = new StripeService();
    private EmailService emailService = EmailService.getInstance();
    private ObservableList<Paiement> paiementList = FXCollections.observableArrayList();
    private User currentUser;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Get current user
        currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Utilisateur non connecté");
            return;
        }

        // Setup table columns
        setupTableColumns();

        // Setup button actions
        setupButtonActions();

        // Setup navigation
        setupNavigationButtons();

        // Setup user profile
        setupUserProfile();
        updateUserInfo();

        // Setup currency combo box
        cbDevise.getItems().addAll("EUR", "USD", "GBP");
        cbDevise.setValue("EUR");

        // Setup montant field validation
        tfMontant.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*")) {
                tfMontant.setText(oldVal);
            }
        });

        // Update last update time
        updateLastUpdateTime();
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id_paiement"));
        colMethode.setCellValueFactory(new PropertyValueFactory<>("methode"));
        colMontant.setCellValueFactory(new PropertyValueFactory<>("montant"));
        colDevise.setCellValueFactory(new PropertyValueFactory<>("devise"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colTransaction.setCellValueFactory(new PropertyValueFactory<>("transactionId"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date_paiement"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email_payeur"));

        // Format montant column
        colMontant.setCellFactory(col -> new TableCell<Paiement, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f", item));
                }
            }
        });

        // Format date column
        colDate.setCellFactory(col -> new TableCell<Paiement, Timestamp>() {
            @Override
            protected void updateItem(Timestamp item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toLocalDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                }
            }
        });

        // Setup actions column
        setupActionsColumn();
    }

    private void setupActionsColumn() {
        Callback<TableColumn<Paiement, Void>, TableCell<Paiement, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<Paiement, Void> call(final TableColumn<Paiement, Void> param) {
                return new TableCell<>() {
                    private final Button btnRembourser = new Button("↩️ Rembourser");
                    private final Button btnDetails = new Button("👁️ Détails");
                    private final HBox pane = new HBox(5);

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
                            Paiement paiement = getTableView().getItems().get(getIndex());
                            pane.getChildren().clear();
                            pane.getChildren().addAll(btnDetails);

                            // Only show rembourser button for completed payments
                            if ("COMPLETE".equals(paiement.getStatut())) {
                                pane.getChildren().add(btnRembourser);
                            }

                            setGraphic(pane);
                        }
                    }
                };
            }
        };
        colActions.setCellFactory(cellFactory);
    }

    private void setupButtonActions() {
        btnStripe.setOnAction(this::payerAvecStripe);
        btnRetour.setOnAction(this::retourVoyages);
    }

    private void setupUserProfile() {
        if (userProfileBox != null) {
            userProfileBox.setOnMouseClicked(event -> navigateToProfile());

            userProfileBox.setOnMouseEntered(event -> {
                userProfileBox.setStyle("-fx-background-color: #e2e8f0; -fx-background-radius: 25; -fx-padding: 6 16 6 6; -fx-cursor: hand;");
            });

            userProfileBox.setOnMouseExited(event -> {
                userProfileBox.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 25; -fx-padding: 6 16 6 6; -fx-cursor: hand;");
            });
        }
    }

    private void updateUserInfo() {
        if (currentUser != null) {
            lblUserName.setText(currentUser.getPrenom() + " " + currentUser.getNom());
            lblUserRole.setText(currentUser.getRole());
        } else {
            lblUserName.setText("Utilisateur");
            lblUserRole.setText("Non connecté");
        }
    }

    private void setupNavigationButtons() {
        // Home button
        if (btnHome != null) {
            btnHome.setOnMouseClicked(event -> navigateToHome());

            btnHome.setOnMouseEntered(event -> {
                btnHome.setStyle("-fx-background-color: #ff8c42; -fx-background-radius: 12; -fx-min-width: 40; -fx-min-height: 40; -fx-cursor: hand;");
                btnHome.lookupAll(".label").forEach(label -> {
                    if (label instanceof Label) {
                        ((Label) label).setStyle("-fx-text-fill: white; -fx-font-size: 18;");
                    }
                });
            });

            btnHome.setOnMouseExited(event -> {
                btnHome.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 12; -fx-min-width: 40; -fx-min-height: 40; -fx-cursor: hand;");
                btnHome.lookupAll(".label").forEach(label -> {
                    if (label instanceof Label) {
                        ((Label) label).setStyle("-fx-font-size: 18; -fx-text-fill: #475569;");
                    }
                });
            });
        }

        // Navigation buttons
        setupNavButtonHover(btnDestinations, "🌍", "Destinations");
        if (btnDestinations != null) {
            btnDestinations.setOnMouseClicked(event -> navigateTo("/DestinationFront.fxml", "Destinations"));
        }

        setupNavButtonHover(btnHebergement, "🏨", "Hébergements");
        if (btnHebergement != null) {
            btnHebergement.setOnMouseClicked(event -> navigateTo("/HebergementFront.fxml", "Hébergements"));
        }

        setupNavButtonHover(btnCategories, "📑", "Catégories");
        if (btnCategories != null) {
            btnCategories.setOnMouseClicked(event -> navigateTo("/categoriesfront.fxml", "Catégories"));
        }

        setupNavButtonHover(btnActivites, "🏄", "Activités");
        if (btnActivites != null) {
            btnActivites.setOnMouseClicked(event -> navigateTo("/activitesfront.fxml", "Activités"));
        }

        setupNavButtonHover(btnVoyages, "✈️", "Voyages");
        if (btnVoyages != null) {
            btnVoyages.setOnMouseClicked(event -> navigateTo("/PageVoyage.fxml", "Voyages"));
        }

        setupNavButtonHover(btnBudgets, "💰", "Budgets");
        if (btnBudgets != null) {
            btnBudgets.setOnMouseClicked(event -> navigateTo("/BudgetDepenseFront.fxml", "Budgets"));
        }

        // Refresh button
        if (btnRefresh != null) {
            btnRefresh.setOnMouseClicked(event -> refreshData());
        }
    }

    private void setupNavButtonHover(HBox button, String icon, String text) {
        if (button == null) return;

        button.setOnMouseEntered(event -> {
            button.setStyle("-fx-background-color: rgba(255,140,66,0.1); -fx-background-radius: 12; -fx-padding: 8 16; -fx-cursor: hand; -fx-border-color: #ff8c42; -fx-border-width: 1; -fx-border-radius: 12;");
            button.lookupAll(".label").forEach(label -> {
                if (label instanceof Label) {
                    Label lbl = (Label) label;
                    if (lbl.getText().equals(icon)) {
                        lbl.setStyle("-fx-font-size: 16;");
                    } else {
                        lbl.setStyle("-fx-text-fill: #ff8c42; -fx-font-weight: 600; -fx-font-size: 14;");
                    }
                }
            });
        });

        button.setOnMouseExited(event -> {
            button.setStyle("-fx-background-color: transparent; -fx-background-radius: 12; -fx-padding: 8 16; -fx-cursor: hand;");
            button.lookupAll(".label").forEach(label -> {
                if (label instanceof Label) {
                    Label lbl = (Label) label;
                    if (lbl.getText().equals(icon)) {
                        lbl.setStyle("-fx-font-size: 16;");
                    } else {
                        lbl.setStyle("-fx-text-fill: #475569; -fx-font-weight: 500; -fx-font-size: 14;");
                    }
                }
            });
        });
    }

    private void navigateTo(String fxmlPath, String title) {
        try {
            // Try multiple possible paths
            URL fxmlUrl = getClass().getResource(fxmlPath);
            if (fxmlUrl == null) {
                fxmlUrl = getClass().getResource("/fxml" + fxmlPath);
            }
            if (fxmlUrl == null) {
                fxmlUrl = getClass().getResource("/views" + fxmlPath);
            }

            if (fxmlUrl == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur",
                        "Fichier FXML non trouvé: " + fxmlPath);
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            Stage stage = (Stage) btnRetour.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TravelMate - " + title);
            stage.setMaximized(true);

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible d'ouvrir " + title + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void navigateToHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/HomePage.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) btnRetour.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TravelMate - Accueil");
            stage.setMaximized(true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void navigateToProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/profile.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) userProfileBox.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TravelMate - Mon Profil");
            stage.setMaximized(true);

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible d'ouvrir le profil: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateLastUpdateTime() {
        if (lblLastUpdate != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");
            lblLastUpdate.setText("Dernière mise à jour: " + LocalDateTime.now().format(formatter));
        }
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

                double prix = 150.0; // You should get this from voyage
                lblPrixVoyage.setText(prix + " €");
                tfMontant.setText(String.valueOf(prix));
                tfDescription.setText("Paiement pour le voyage : " + voyage.getTitre_voyage());

                // Set email from current user if available
                if (currentUser != null) {
                    tfEmail.setText(currentUser.getEmail());
                } else {
                    tfEmail.setText("client@example.com");
                }
            }

            chargerPaiements();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible de charger les données: " + e.getMessage());
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

            updateLastUpdateTime();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible de charger les paiements: " + e.getMessage());
        }
    }

    private void refreshData() {
        chargerPaiements();
        showAlert(Alert.AlertType.INFORMATION, "Succès", "Données rafraîchies avec succès!");
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
                    currentUser != null ? currentUser.getId() : 1,
                    montant,
                    "STRIPE",
                    description,
                    email
            );
            paiement.setTransactionId(paymentIntentId);
            paiementCRUD.ajouter(paiement);

            showAlert(Alert.AlertType.INFORMATION, "Paiement Stripe",
                    "PaymentIntent créé avec succès !\n" +
                            "ID: " + paymentIntentId);

            completerPaiement(paymentIntentId);

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Montant invalide");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Erreur lors du paiement Stripe: " + e.getMessage());
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
                String nomDestination = voyage != null ?
                        voyageCRUD.getNomDestination(voyage.getId_destination()) : "N/A";
                String dates = voyage != null ?
                        (voyage.getDate_debut() + " - " + voyage.getDate_fin()) : "N/A";
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
                    new Thread(() -> {
                        try {
                            String nomVoyage = voyage != null ? voyage.getTitre_voyage() : "N/A";
                            emailService.envoyerConfirmationRemboursement(
                                    paiement.getEmail_payeur(),
                                    nomVoyage,
                                    paiement.getMontant(),
                                    paiement.getDevise(),
                                    paiement.getTransactionId()
                            );
                        } catch (Exception ex) {
                            System.out.println("⚠️ Erreur envoi email remboursement: " + ex.getMessage());
                        }
                    }).start();
                }

            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur",
                        "Erreur lors du remboursement: " + e.getMessage());
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
                paiement.getDate_paiement() != null ?
                        paiement.getDate_paiement().toLocalDateTime()
                                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "N/A",
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
        navigateTo("/PageVoyage.fxml", "Voyages");
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}