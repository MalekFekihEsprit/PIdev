package Controllers;

import Entities.Participation;
import Services.EmailService;
import Services.ParticipationCRUDV;
import Utils.MyBD;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ParticipationBackController implements Initializable {

    private static final String PERCENT_FORMAT = "(%.1f%%)";

    // En-tête du voyage
    @FXML
    private Label lblTitreVoyage;
    @FXML
    private Label lblDestinationVoyage;
    @FXML
    private Label lblDatesVoyage;

    // Formulaire d'ajout
    @FXML
    private TextField tfEmail;
    @FXML
    private ComboBox<String> cbRole;
    @FXML
    private Button btnAjouterParticipation;
    @FXML
    private Button btnRetour;

    // Tableau
    @FXML
    private TableView<Participation> tableParticipations;
    @FXML
    private TableColumn<Participation, Integer> colIdParticipation;
    @FXML
    private TableColumn<Participation, Integer> colIdUser;
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

    // Statistiques par rôle (cards)
    @FXML
    private Label lblCountVIP;
    @FXML
    private Label lblCountOrganisateur;
    @FXML
    private Label lblCountParticipant;
    @FXML
    private Label lblCountGuide;
    @FXML
    private Label lblCountChauffeur;

    @FXML
    private Label lblPourcentVIP;
    @FXML
    private Label lblPourcentOrganisateur;
    @FXML
    private Label lblPourcentParticipant;
    @FXML
    private Label lblPourcentGuide;
    @FXML
    private Label lblPourcentChauffeur;

    // Total participants
    @FXML
    private Label lblTotalParticipants;

    // Compteurs
    @FXML
    private Label lblParticipantsCount;
    @FXML
    private Label lblTotalParticipantsFooter;

    // Sidebar labels
    @FXML
    private Label lblSidebarParticipantsCount;
    @FXML
    private Label statsTotalParticipants;
    @FXML
    private Label statsVIPCount;
    @FXML
    private Label statsOrganisateursCount;
    @FXML
    private Label statsParticipantsCount;
    @FXML
    private Label lblLastUpdate;

    // Champs de recherche
    @FXML
    private TextField tfSearchIdParticipation;
    @FXML
    private ComboBox<String> cbSearchRole;
    @FXML
    private TextField tfSearchEmail;
    @FXML
    private Button btnSearchParticipation;
    @FXML
    private Button btnResetParticipation;

    private int idVoyage;
    private String titreVoyage;
    private String destinationVoyage;
    private String datesVoyage;

    private ParticipationCRUDV participationCRUD = new ParticipationCRUDV();
    private EmailService emailService = EmailService.getInstance();
    private ObservableList<Participation> participationList = FXCollections.observableArrayList();
    private FilteredList<Participation> filteredData;
    private SortedList<Participation> sortedData;
    private Connection conn;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        conn = MyBD.getInstance().getConn();

        // Initialiser les colonnes du tableau
        colIdParticipation.setCellValueFactory(new PropertyValueFactory<>("id_participation"));
        colIdUser.setCellValueFactory(new PropertyValueFactory<>("id"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role_participation"));

        // Ajouter les boutons d'action
        ajouterBoutonsActions();

        // Remplir les comboBox des rôles
        List<String> roles = Arrays.asList("VIP", "Organisateur", "Participant", "Guide", "Chauffeur");
        cbRole.getItems().addAll(roles);
        cbRole.setValue("Participant");

        cbSearchRole.getItems().addAll("Tous");
        cbSearchRole.getItems().addAll(roles);
        cbSearchRole.setValue("Tous");

        // Configurer les boutons
        btnAjouterParticipation.setOnAction(this::ajouterParticipation);
        btnRetour.setOnAction(this::retourVoyages);
        btnSearchParticipation.setOnAction(this::rechercherParticipations);
        btnResetParticipation.setOnAction(this::resetRecherche);

        // Configurer la recherche
        configurerRecherche();

        // Mettre à jour la date
        mettreAJourDateMAJ();
    }

    private void configurerRecherche() {
        filteredData = new FilteredList<>(participationList, p -> true);
        sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableParticipations.comparatorProperty());
        tableParticipations.setItems(sortedData);

        sortedData.addListener((javafx.collections.ListChangeListener<Participation>) c -> {
            mettreAJourStatistiquesRoles();
            mettreAJourCompteurs();
        });
    }

    @FXML
    private void rechercherParticipations(ActionEvent event) {
        String idText = tfSearchIdParticipation.getText() != null ? tfSearchIdParticipation.getText().trim() : "";
        String email = tfSearchEmail.getText() != null ? tfSearchEmail.getText().toLowerCase().trim() : "";
        String role = cbSearchRole.getValue();

        filteredData.setPredicate(participation -> {
            if (idText.isEmpty() && email.isEmpty() && (role == null || "Tous".equals(role))) {
                return true;
            }

            if (!idText.isEmpty()) {
                try {
                    int id = Integer.parseInt(idText);
                    if (participation.getId_participation() != id) {
                        return false;
                    }
                } catch (NumberFormatException e) {
                    return false;
                }
            }

            if (!email.isEmpty()) {
                String participationEmail = participation.getEmail();
                if (participationEmail == null || !participationEmail.toLowerCase().contains(email)) {
                    return false;
                }
            }

            if (role != null && !"Tous".equals(role)) {
                String participationRole = participation.getRole_participation();
                if (participationRole == null || !participationRole.equals(role)) {
                    return false;
                }
            }

            return true;
        });
    }

    @FXML
    private void resetRecherche(ActionEvent event) {
        tfSearchIdParticipation.clear();
        tfSearchEmail.clear();
        cbSearchRole.setValue("Tous");

        if (filteredData != null) {
            filteredData.setPredicate(p -> true);
        }
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
                        btnModifier.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 4 8; -fx-font-size: 11; -fx-cursor: hand;");
                        btnSupprimer.setStyle("-fx-background-color: #ec4899; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 4 8; -fx-font-size: 11; -fx-cursor: hand;");

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

            for (Participation p : toutesParticipations) {
                if (p.getId_voyage() == idVoyage) {
                    Map<String, String> userInfos = getUserInfos(p.getId());
                    if (userInfos != null) {
                        p.setEmail(userInfos.get("email"));
                        p.setNom(userInfos.get("nom"));
                        p.setPrenom(userInfos.get("prenom"));
                    }
                    participationList.add(p);
                }
            }

            mettreAJourStatistiquesRoles();
            mettreAJourCompteurs();
            mettreAJourDateMAJ();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les participations: " + e.getMessage());
        }
    }

    private void mettreAJourStatistiquesRoles() {
        int total = participationList.size();

        long vip = participationList.stream().filter(p -> "VIP".equals(p.getRole_participation())).count();
        long organisateur = participationList.stream().filter(p -> "Organisateur".equals(p.getRole_participation())).count();
        long participant = participationList.stream().filter(p -> "Participant".equals(p.getRole_participation())).count();
        long guide = participationList.stream().filter(p -> "Guide".equals(p.getRole_participation())).count();
        long chauffeur = participationList.stream().filter(p -> "Chauffeur".equals(p.getRole_participation())).count();

        // Mettre à jour les labels des cartes
        if (lblCountVIP != null) lblCountVIP.setText(String.valueOf(vip));
        if (lblCountOrganisateur != null) lblCountOrganisateur.setText(String.valueOf(organisateur));
        if (lblCountParticipant != null) lblCountParticipant.setText(String.valueOf(participant));
        if (lblCountGuide != null) lblCountGuide.setText(String.valueOf(guide));
        if (lblCountChauffeur != null) lblCountChauffeur.setText(String.valueOf(chauffeur));

        if (lblTotalParticipants != null) lblTotalParticipants.setText(String.valueOf(total));

        // Mettre à jour les statistiques sidebar
        if (statsTotalParticipants != null) statsTotalParticipants.setText(String.valueOf(total));
        if (statsVIPCount != null) statsVIPCount.setText(String.valueOf(vip));
        if (statsOrganisateursCount != null) statsOrganisateursCount.setText(String.valueOf(organisateur));
        if (statsParticipantsCount != null) statsParticipantsCount.setText(String.valueOf(participant));

        // Calculer et afficher les pourcentages
        if (total > 0) {
            if (lblPourcentVIP != null) lblPourcentVIP.setText(String.format(PERCENT_FORMAT, (vip * 100.0 / total)));
            if (lblPourcentOrganisateur != null) lblPourcentOrganisateur.setText(String.format(PERCENT_FORMAT, (organisateur * 100.0 / total)));
            if (lblPourcentParticipant != null) lblPourcentParticipant.setText(String.format(PERCENT_FORMAT, (participant * 100.0 / total)));
            if (lblPourcentGuide != null) lblPourcentGuide.setText(String.format(PERCENT_FORMAT, (guide * 100.0 / total)));
            if (lblPourcentChauffeur != null) lblPourcentChauffeur.setText(String.format(PERCENT_FORMAT, (chauffeur * 100.0 / total)));
        }
    }

    private void mettreAJourCompteurs() {
        int size = (filteredData != null) ? filteredData.size() : participationList.size();

        if (lblParticipantsCount != null) {
            lblParticipantsCount.setText(size + " participant" + (size > 1 ? "s" : ""));
        }

        if (lblTotalParticipantsFooter != null) {
            lblTotalParticipantsFooter.setText(size + " participant" + (size > 1 ? "s" : ""));
        }

        if (lblSidebarParticipantsCount != null) {
            lblSidebarParticipantsCount.setText(String.valueOf(size));
        }
    }

    private void mettreAJourDateMAJ() {
        if (lblLastUpdate != null) {
            lblLastUpdate.setText("Dernière mise à jour: " +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")));
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
    private void ajouterParticipation(ActionEvent event) {
        String email = tfEmail.getText().trim();

        if (email.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez saisir un email");
            return;
        }

        if (cbRole.getValue() == null || cbRole.getValue().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez sélectionner un rôle");
            return;
        }

        int idUser = getUserIdByEmail(email);

        if (idUser == -1) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Aucun utilisateur trouvé avec cet email");
            return;
        }

        try {
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

            // Envoyer email d'information
            String participantEmail = email;
            String participantRole = cbRole.getValue();
            Map<String, String> infos = getUserInfos(idUser);
            String nomComplet = (infos != null) ? (infos.get("prenom") + " " + infos.get("nom")) : email;

            new Thread(() -> {
                try {
                    emailService.envoyerNotificationAjoutParAdmin(
                            participantEmail, nomComplet, titreVoyage,
                            destinationVoyage, datesVoyage, participantRole
                    );
                } catch (Exception ex) {
                    System.out.println("⚠️ Erreur envoi email participation: " + ex.getMessage());
                }
            }).start();

            tfEmail.clear();
            cbRole.setValue("Participant");
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
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Modifier la participation");
        dialog.setHeaderText("Modifier la participation de " + participation.getEmail());

        ComboBox<String> cbRoleModif = new ComboBox<>();
        cbRoleModif.getItems().addAll("VIP", "Organisateur", "Participant", "Guide", "Chauffeur");
        cbRoleModif.setValue(participation.getRole_participation());

        TextField tfEmailModif = new TextField(participation.getEmail());
        tfEmailModif.setEditable(false);
        tfEmailModif.setStyle("-fx-background-color: #2d3a5f; -fx-text-fill: #94a3b8;");

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        VBox vbox = new VBox(10);
        vbox.getChildren().addAll(
                new Label("Email:"),
                tfEmailModif,
                new Label("Rôle:"),
                cbRoleModif
        );
        vbox.setStyle("-fx-padding: 20; -fx-background-color: #1e2749;");

        dialog.getDialogPane().setContent(vbox);
        dialog.getDialogPane().setStyle("-fx-background-color: #1e2749;");

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
    private void retourVoyages(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/PageVoyageBack.fxml"));
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