package Controllers.ItineraireEtEtape;

import Entities.Itineraire;
import Entities.Voyage;
import Entities.User;
import Services.itineraireCRUD;
import Utils.MyBD;
import Utils.AlertUtil;
import Utils.UserSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PageAjoutItineraire {

    // ============== ÉLÉMENTS DE NAVIGATION ==============
    @FXML private HBox btnDestinations;
    @FXML private HBox btnHebergement;
    @FXML private HBox btnActivites;
    @FXML private HBox btnCategories;
    @FXML private HBox btnVoyages;
    @FXML private HBox btnBudgets;
    @FXML private HBox userProfileBox;
    @FXML private Label lblUserName;
    @FXML private Label lblUserRole;
    @FXML private Label lblVoyageContext;

    // ============== ÉLÉMENTS DU FORMULAIRE ==============
    @FXML
    private TextField nomField;

    @FXML
    private TextArea descriptionField;

    @FXML
    private ComboBox<VoyageItem> voyageCombo;

    @FXML
    private Label errorLabel;

    private itineraireCRUD itineraireCRUD = new itineraireCRUD();
    private Voyage voyageActuel;

    public static class VoyageItem {
        private int id;
        private String titre;
        private String destination;

        public VoyageItem(int id, String titre, String destination) {
            this.id = id;
            this.titre = titre;
            this.destination = destination;
        }

        public int getId() { return id; }
        public String getTitre() { return titre; }
        public String getDestination() { return destination; }

        @Override
        public String toString() {
            return titre + (destination != null && !destination.isEmpty() ? " - " + destination : "");
        }
    }

    @FXML
    public void initialize() {
        // ============== CONFIGURATION DE LA NAVIGATION ==============
        configurerNavigation();
        configurerUserProfile();
        updateUserInfo();

        // ============== CONFIGURATION DU FORMULAIRE ==============
        chargerVoyages();
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    // ============== MÉTHODES DE NAVIGATION ==============

    private void configurerNavigation() {
        if (btnDestinations != null) {
            btnDestinations.setOnMouseClicked(event -> navigateTo("/DestinationFront.fxml", "Destinations"));

            btnDestinations.setOnMouseEntered(event -> {
                btnDestinations.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 12; -fx-padding: 8 14; -fx-cursor: hand;");
            });
            btnDestinations.setOnMouseExited(event -> {
                btnDestinations.setStyle("-fx-background-color: transparent; -fx-background-radius: 12; -fx-padding: 8 14; -fx-cursor: hand;");
            });
        }

        if (btnHebergement != null) {
            btnHebergement.setOnMouseClicked(event -> navigateTo("/HebergementFront.fxml", "Hébergements"));

            btnHebergement.setOnMouseEntered(event -> {
                btnHebergement.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 12; -fx-padding: 8 14; -fx-cursor: hand;");
            });
            btnHebergement.setOnMouseExited(event -> {
                btnHebergement.setStyle("-fx-background-color: transparent; -fx-background-radius: 12; -fx-padding: 8 14; -fx-cursor: hand;");
            });
        }

        if (btnActivites != null) {
            btnActivites.setOnMouseClicked(event -> navigateTo("/activitesfront.fxml", "Activités"));

            btnActivites.setOnMouseEntered(event -> {
                btnActivites.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 12; -fx-padding: 8 14; -fx-cursor: hand;");
            });
            btnActivites.setOnMouseExited(event -> {
                btnActivites.setStyle("-fx-background-color: transparent; -fx-background-radius: 12; -fx-padding: 8 14; -fx-cursor: hand;");
            });
        }

        if (btnCategories != null) {
            btnCategories.setOnMouseClicked(event -> navigateTo("/categoriesfront.fxml", "Catégories"));

            btnCategories.setOnMouseEntered(event -> {
                btnCategories.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 12; -fx-padding: 8 14; -fx-cursor: hand;");
            });
            btnCategories.setOnMouseExited(event -> {
                btnCategories.setStyle("-fx-background-color: transparent; -fx-background-radius: 12; -fx-padding: 8 14; -fx-cursor: hand;");
            });
        }

        if (btnVoyages != null) {
            btnVoyages.setOnMouseClicked(event -> navigateTo("/PageVoyage.fxml", "Voyages"));

            btnVoyages.setOnMouseEntered(event -> {
                btnVoyages.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 12; -fx-padding: 8 14; -fx-cursor: hand;");
            });
            btnVoyages.setOnMouseExited(event -> {
                btnVoyages.setStyle("-fx-background-color: transparent; -fx-background-radius: 12; -fx-padding: 8 14; -fx-cursor: hand;");
            });
        }

        if (btnBudgets != null) {
            btnBudgets.setOnMouseClicked(event -> showNotImplementedAlert("Budgets"));

            btnBudgets.setOnMouseEntered(event -> {
                btnBudgets.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 12; -fx-padding: 8 14; -fx-cursor: hand;");
            });
            btnBudgets.setOnMouseExited(event -> {
                btnBudgets.setStyle("-fx-background-color: transparent; -fx-background-radius: 12; -fx-padding: 8 14; -fx-cursor: hand;");
            });
        }
    }

    private void configurerUserProfile() {
        if (userProfileBox != null) {
            userProfileBox.setOnMouseClicked(event -> navigateTo("/fxml/profile.fxml", "Profil"));

            userProfileBox.setOnMouseEntered(event -> {
                userProfileBox.setStyle("-fx-background-color: #e2e8f0; -fx-background-radius: 25; -fx-padding: 5 14 5 5; -fx-cursor: hand; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-border-radius: 25;");
            });

            userProfileBox.setOnMouseExited(event -> {
                userProfileBox.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 25; -fx-padding: 5 14 5 5; -fx-cursor: hand; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-border-radius: 25;");
            });
        }
    }

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

    private void navigateTo(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) btnDestinations.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TravelMate - " + title);
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger " + title + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showNotImplementedAlert(String feature) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Fonctionnalité à venir");
        alert.setHeaderText(null);
        alert.setContentText("La fonctionnalité \"" + feature + "\" sera bientôt disponible !");
        alert.showAndWait();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ============== MÉTHODES DU FORMULAIRE ==============

    private void chargerVoyages() {
        List<VoyageItem> voyages = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = MyBD.getInstance().getConn();
            String query = "SELECT v.id_voyage, v.titre_voyage, d.nom_destination " +
                    "FROM voyage v " +
                    "LEFT JOIN destination d ON v.id_destination = d.id_destination " +
                    "ORDER BY v.titre_voyage";
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();

            while (rs.next()) {
                voyages.add(new VoyageItem(
                        rs.getInt("id_voyage"),
                        rs.getString("titre_voyage"),
                        rs.getString("nom_destination")
                ));
            }

            voyageCombo.getItems().addAll(voyages);

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur lors du chargement des voyages: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void setVoyageActuel(Voyage voyage) {
        this.voyageActuel = voyage;
        if (voyage != null && lblVoyageContext != null) {
            String nomDestination = getDestinationName(voyage.getId_destination());
            lblVoyageContext.setText("Création pour: " + voyage.getTitre_voyage() + " (" + nomDestination + ")");
        }
    }

    public void preselectionnerVoyage(int idVoyage) {
        for (VoyageItem item : voyageCombo.getItems()) {
            if (item.getId() == idVoyage) {
                voyageCombo.getSelectionModel().select(item);
                break;
            }
        }
    }

    private String getDestinationName(int idDestination) {
        String nom = "Destination";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = MyBD.getInstance().getConn();
            String query = "SELECT nom_destination FROM destination WHERE id_destination = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, idDestination);
            rs = stmt.executeQuery();

            if (rs.next()) {
                nom = rs.getString("nom_destination");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return nom;
    }

    @FXML
    private void handleCreer() {
        if (nomField.getText() == null || nomField.getText().trim().isEmpty()) {
            showError("Le nom de l'itinéraire est requis");
            return;
        }

        if (nomField.getText().length() > 100) {
            showError("Le nom ne doit pas dépasser 100 caractères");
            return;
        }

        VoyageItem selectedVoyage = voyageCombo.getSelectionModel().getSelectedItem();
        if (selectedVoyage == null) {
            showError("Veuillez sélectionner un voyage");
            return;
        }

        try {
            Itineraire itineraire = new Itineraire();
            itineraire.setNom_itineraire(nomField.getText().trim());
            itineraire.setDescription_itineraire(descriptionField.getText() != null ? descriptionField.getText().trim() : "");
            itineraire.setId_voyage(selectedVoyage.getId());

            itineraireCRUD.ajouter(itineraire);

            AlertUtil.showInfo("Succès", "Itinéraire créé avec succès !");

            // Retour à la liste du MÊME voyage
            retourAListe();

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur lors de la création: " + e.getMessage());
        }
    }

    @FXML
    private void handleAnnuler() {
        retourAListe();
    }

    private void retourAListe() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ItineraireEtEtape/PageItineraire.fxml"));
            Parent root = loader.load();

            PageItineraire controller = loader.getController();

            // Repasser le voyage actuel au contrôleur
            if (voyageActuel != null) {
                String nomDestination = getDestinationName(voyageActuel.getId_destination());
                controller.initData(voyageActuel, nomDestination);
            }

            Stage stage = (Stage) nomField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TravelMate - Itinéraires");
            stage.setMaximized(true);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur lors du retour à la liste");
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: #ef4444;");
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    @FXML
    private void handleRetour() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ItineraireEtEtape/PageItineraire.fxml"));
            Parent root = loader.load();

            PageItineraire controller = loader.getController();

            // Repasser le voyage actuel au contrôleur pour garder le contexte
            if (voyageActuel != null) {
                String nomDestination = getDestinationName(voyageActuel.getId_destination());
                controller.initData(voyageActuel, nomDestination);
            }

            Stage stage = (Stage) nomField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TravelMate - Itinéraires");
            stage.setMaximized(true);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur lors du retour à la liste");
        }
    }
}