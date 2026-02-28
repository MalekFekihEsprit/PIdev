package Controllers;

import Entities.Participation;
import Services.EmailService;
import Services.ParticipationCRUDV;
import Utils.MyBD;
import Utils.UserSession;
import Entities.User;
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
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class ParticipationController implements Initializable {

    // ============== FXML INJECTED ELEMENTS - NAVIGATION ==============
    @FXML private HBox btnDestinations;
    @FXML private HBox btnHebergement;
    @FXML private HBox btnActivites;
    @FXML private HBox btnCategories;
    @FXML private HBox btnVoyages;
    @FXML private HBox btnBudgets;
    @FXML private HBox userProfileBox;
    @FXML private Label lblUserName;
    @FXML private Label lblUserRole;

    // ============== FXML INJECTED ELEMENTS - EXISTANTS ==============
    @FXML
    private Label lblTitreVoyage;
    @FXML
    private Label lblDestinationVoyage;
    @FXML
    private Label lblDatesVoyage;

    @FXML
    private TextField tfEmail;
    @FXML
    private ComboBox<String> cbRole;
    @FXML
    private Button btnAjouterParticipation;

    @FXML
    private TableView<Participation> tableParticipations;
    @FXML
    private TableColumn<Participation, String> colEmail;
    @FXML
    private TableColumn<Participation, String> colNom;
    @FXML
    private TableColumn<Participation, String> colPrenom;
    @FXML
    private TableColumn<Participation, String> colRole;
    @FXML
    private TableColumn<Participation, Void> colActions;

    @FXML
    private Label lblTotalParticipants;
    @FXML
    private Label lblTotalParticipantsFooter;

    // ============== CLASS VARIABLES ==============
    private int idVoyage;
    private String titreVoyage;
    private String destinationVoyage;
    private String datesVoyage;

    private ParticipationCRUDV participationCRUD = new ParticipationCRUDV();
    private EmailService emailService = EmailService.getInstance();
    private ObservableList<Participation> participationList = FXCollections.observableArrayList();
    private Connection conn;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        conn = MyBD.getInstance().getConn();

        // Initialiser les colonnes du tableau
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role_participation"));

        // Ajouter les boutons d'action
        ajouterBoutonsActions();

        // Remplir la comboBox des rôles
        cbRole.getItems().addAll("VIP", "Organisateur", "Participant", "Guide", "Chauffeur");
        cbRole.setValue("Participant");

        // Configurer les boutons de navigation
        configurerNavigation();

        // Configurer le profil utilisateur
        configurerUserProfile();
        updateUserInfo();
    }

    /**
     * Configure les boutons de navigation
     */
    private void configurerNavigation() {
        // Bouton Destinations
        if (btnDestinations != null) {
            btnDestinations.setOnMouseClicked(event -> navigateToDestinations());
        }

        // Bouton Hébergement
        if (btnHebergement != null) {
            btnHebergement.setOnMouseClicked(event -> navigateToHebergement());
        }

        // Bouton Activités
        if (btnActivites != null) {
            btnActivites.setOnMouseClicked(event -> navigateToActivites());
        }

        // Bouton Catégories
        if (btnCategories != null) {
            btnCategories.setOnMouseClicked(event -> navigateToCategories());
        }

        // Bouton Voyages
        if (btnVoyages != null) {
            btnVoyages.setOnMouseClicked(event -> navigateToVoyages());
        }

        // Bouton Budgets
        if (btnBudgets != null) {
            btnBudgets.setOnMouseClicked(event -> showNotImplementedAlert("Budgets"));
        }
    }

    /**
     * Configure le profil utilisateur
     */
    private void configurerUserProfile() {
        if (userProfileBox != null) {
            userProfileBox.setOnMouseClicked(event -> navigateToProfile());

            // Effet de survol
            userProfileBox.setOnMouseEntered(event -> {
                userProfileBox.setStyle("-fx-background-color: #e2e8f0; -fx-background-radius: 25; -fx-padding: 5 14 5 5; -fx-cursor: hand; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-border-radius: 25;");
            });

            userProfileBox.setOnMouseExited(event -> {
                userProfileBox.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 25; -fx-padding: 5 14 5 5; -fx-cursor: hand; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-border-radius: 25;");
            });
        }
    }

    /**
     * Met à jour les informations utilisateur
     */
    private void updateUserInfo() {
        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser != null) {
            if (lblUserName != null) {
                lblUserName.setText(currentUser.getPrenom() + " " + currentUser.getNom());
            }
            if (lblUserRole != null) {
                lblUserRole.setText(currentUser.getRole());
            }
        } else {
            if (lblUserName != null) {
                lblUserName.setText("Utilisateur");
            }
            if (lblUserRole != null) {
                lblUserRole.setText("Non connecté");
            }
        }
    }

    /**
     * Navigue vers la page des destinations
     */
    private void navigateToDestinations() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DestinationFront.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnDestinations.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TravelMate - Destinations");
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les destinations: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Navigue vers la page des hébergements
     */
    private void navigateToHebergement() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/HebergementFront.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnHebergement.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TravelMate - Hébergements");
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les hébergements: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Navigue vers la page des activités
     */
    private void navigateToActivites() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/activitesfront.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnActivites.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TravelMate - Activités");
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les activités: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Navigue vers la page des catégories
     */
    private void navigateToCategories() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/categoriesfront.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnCategories.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TravelMate - Catégories");
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les catégories: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Navigue vers la page des voyages
     */
    private void navigateToVoyages() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/PageVoyage.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnVoyages.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TravelMate - Gestion des Voyages");
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la page des voyages: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Navigue vers le profil utilisateur
     */
    private void navigateToProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/profile.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) userProfileBox.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TravelMate - Mon Profil");
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le profil: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Affiche une alerte pour les fonctionnalités non implémentées
     */
    private void showNotImplementedAlert(String feature) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Fonctionnalité à venir");
        alert.setHeaderText(null);
        alert.setContentText("La fonctionnalité \"" + feature + "\" sera bientôt disponible !");
        alert.showAndWait();
    }

    private void ajouterBoutonsActions() {
        Callback<TableColumn<Participation, Void>, TableCell<Participation, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<Participation, Void> call(final TableColumn<Participation, Void> param) {
                return new TableCell<>() {
                    private final Button btnModifier = new Button("✏️");
                    private final Button btnSupprimer = new Button("🗑️");
                    private final HBox pane = new HBox(5, btnModifier, btnSupprimer);

                    {
                        btnModifier.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-background-radius: 12; -fx-padding: 4 8; -fx-font-size: 10; -fx-cursor: hand;");
                        btnSupprimer.setStyle("-fx-background-color: #ec4899; -fx-text-fill: white; -fx-background-radius: 12; -fx-padding: 4 8; -fx-font-size: 10; -fx-cursor: hand;");

                        btnModifier.setOnAction(event -> {
                            Participation participation = getTableView().getItems().get(getIndex());
                            handleModifierParticipation(participation);
                        });

                        btnSupprimer.setOnAction(event -> {
                            Participation participation = getTableView().getItems().get(getIndex());
                            handleSupprimerParticipation(participation);
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

    public void initData(int idVoyage, String titre, String destination, String dates) {
        this.idVoyage = idVoyage;
        this.titreVoyage = titre;
        this.destinationVoyage = destination;
        this.datesVoyage = dates;

        lblTitreVoyage.setText(titre);
        lblDestinationVoyage.setText(destination);
        lblDatesVoyage.setText(dates);

        chargerParticipations();
    }

    private void chargerParticipations() {
        try {
            List<Participation> toutesParticipations = participationCRUD.afficher();
            participationList.clear();

            // Filtrer pour n'avoir que les participations de ce voyage
            for (Participation p : toutesParticipations) {
                if (p.getId_voyage() == idVoyage) {
                    // Récupérer les infos de l'utilisateur depuis la BD
                    Map<String, String> userInfos = getUserInfos(p.getId());
                    if (userInfos != null) {
                        p.setEmail(userInfos.get("email"));
                        p.setNom(userInfos.get("nom"));
                        p.setPrenom(userInfos.get("prenom"));
                    }
                    participationList.add(p);
                }
            }

            tableParticipations.setItems(participationList);

            // Mettre à jour les deux labels
            String texte = participationList.size() + " participant" + (participationList.size() > 1 ? "s" : "");
            lblTotalParticipants.setText(texte);
            lblTotalParticipantsFooter.setText(texte);

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les participations: " + e.getMessage());
        }
    }

    private Map<String, String> getUserInfos(int userId) {
        Map<String, String> infos = new HashMap<>();
        String req = "SELECT email, nom, prenom FROM user WHERE id = ?";

        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setInt(1, userId);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                infos.put("email", rs.getString("email"));
                infos.put("nom", rs.getString("nom"));
                infos.put("prenom", rs.getString("prenom"));
                return infos;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private int getUserIdByEmail(String email) {
        String req = "SELECT id FROM user WHERE email = ?";

        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setString(1, email);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @FXML
    void ajouterParticipation(ActionEvent event) {
        String email = tfEmail.getText().trim();

        if (email.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez saisir un email");
            return;
        }

        if (cbRole.getValue() == null || cbRole.getValue().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez sélectionner un rôle");
            return;
        }

        // Vérifier si l'email existe dans la table user
        int idUser = getUserIdByEmail(email);

        if (idUser == -1) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Aucun utilisateur trouvé avec cet email");
            return;
        }

        try {
            // Vérifier si l'utilisateur est déjà participant à ce voyage
            if (isParticipationExistante(idUser, idVoyage)) {
                showAlert(Alert.AlertType.WARNING, "Attention", "Cet utilisateur est déjà participant à ce voyage");
                return;
            }

            Participation p = new Participation(
                    idUser,
                    cbRole.getValue(),
                    idVoyage
            );

            participationCRUD.ajouter(p);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Participation ajoutée avec succès!");

            // Envoyer email de confirmation de participation
            String participantEmail = email;
            String participantRole = cbRole.getValue();
            Map<String, String> infos = getUserInfos(idUser);
            String nomComplet = (infos != null) ? (infos.get("prenom") + " " + infos.get("nom")) : email;
            new Thread(() -> {
                try {
                    emailService.envoyerConfirmationParticipation(
                            participantEmail, nomComplet, titreVoyage,
                            destinationVoyage, datesVoyage, participantRole
                    );
                } catch (Exception ex) {
                    System.out.println("⚠️ Erreur envoi email participation: " + ex.getMessage());
                }
            }).start();

            // Réinitialiser le formulaire
            tfEmail.clear();
            cbRole.setValue("Participant");

            // Recharger la liste
            chargerParticipations();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ajout: " + e.getMessage());
        }
    }

    private boolean isParticipationExistante(int idUser, int idVoyage) {
        try {
            List<Participation> toutes = participationCRUD.afficher();
            for (Participation p : toutes) {
                if (p.getId() == idUser && p.getId_voyage() == idVoyage) {
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void handleModifierParticipation(Participation participation) {
        // Créer un dialogue de modification
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Modifier la participation");
        dialog.setHeaderText("Modifier la participation de " + participation.getEmail());

        // Créer les champs du formulaire
        ComboBox<String> cbRoleModif = new ComboBox<>();
        cbRoleModif.getItems().addAll("VIP", "Organisateur", "Participant", "Guide", "Chauffeur");
        cbRoleModif.setValue(participation.getRole_participation());

        TextField tfEmailModif = new TextField(participation.getEmail());
        tfEmailModif.setPromptText("Email");
        tfEmailModif.setEditable(false);
        tfEmailModif.setStyle("-fx-background-color: #e2e8f0;");

        // Ajouter les boutons
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Layout
        VBox vbox = new VBox(10);
        vbox.getChildren().addAll(
                new Label("Email:"),
                tfEmailModif,
                new Label("Rôle:"),
                cbRoleModif
        );
        vbox.setStyle("-fx-padding: 20;");

        dialog.getDialogPane().setContent(vbox);

        // Traiter la réponse
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                participation.setRole_participation(cbRoleModif.getValue());
                participationCRUD.modifier(participation);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Participation modifiée avec succès!");
                chargerParticipations();
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la modification: " + e.getMessage());
            }
        }
    }

    private void handleSupprimerParticipation(Participation participation) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText(null);
        alert.setContentText("Voulez-vous vraiment supprimer la participation de " + participation.getEmail() + " ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                participationCRUD.supprimer(participation.getId_participation());
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Participation supprimée avec succès!");
                chargerParticipations();
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la suppression: " + e.getMessage());
            }
        }
    }

    @FXML
    void retourVoyages(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/PageVoyage.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) lblTitreVoyage.getScene().getWindow();
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