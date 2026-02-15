package Controlles;

import Services.itineraireCRUD;
import Entites.Itineraire;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.util.List;
import java.util.Optional;
import Utils.MyBD;

public class PageItineraire {
    @FXML private ComboBox<String> comboDestination;
    @FXML private DatePicker dateDebut;
    @FXML private DatePicker dateFin;
    @FXML private Button btnRechercher;
    @FXML private Label lblTotalItinerairesTable;
    @FXML private VBox emptyPlaceholder;
    @FXML private Button btnCreerPremierItineraire;
    @FXML private VBox itinerariesContainer;
    @FXML private BorderPane mainBorderPane;

    private itineraireCRUD itineraireCRUD;
    private List<Itineraire> itineraires;

    @FXML
    public void initialize() {
        itineraireCRUD = new itineraireCRUD();
        refreshItineraries();
    }

    public void refreshItineraries() {
        try {
            itineraires = itineraireCRUD.afficher();
            updateItineraryDisplay();
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les itinéraires: " + e.getMessage());
        }
    }

    private void updateItineraryDisplay() {
        if (itineraires == null || itineraires.isEmpty()) {
            emptyPlaceholder.setVisible(true);
            emptyPlaceholder.setManaged(true);
            if (itinerariesContainer != null) {
                itinerariesContainer.setVisible(false);
                itinerariesContainer.setManaged(false);
            }
        } else {
            emptyPlaceholder.setVisible(false);
            emptyPlaceholder.setManaged(false);
            if (itinerariesContainer != null) {
                itinerariesContainer.setVisible(true);
                itinerariesContainer.setManaged(true);

                lblTotalItinerairesTable.setText(itineraires.size() + " itinéraire" + (itineraires.size() > 1 ? "s" : ""));
                generateItineraryCards();
            }
        }
    }

    private void generateItineraryCards() {
        itinerariesContainer.getChildren().clear();

        for (Itineraire itineraire : itineraires) {
            VBox card = createItineraryCard(itineraire);
            itinerariesContainer.getChildren().add(card);
        }
    }

    private VBox createItineraryCard(Itineraire itineraire) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-border-radius: 16; -fx-padding: 16;");

        // En-tête avec les boutons
        HBox header = new HBox(12);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // Icône
        VBox iconBox = new VBox();
        iconBox.setAlignment(javafx.geometry.Pos.CENTER);
        iconBox.setStyle("-fx-background-color: #fff7ed; -fx-background-radius: 12; -fx-min-width: 48; -fx-min-height: 48;");
        Label iconLabel = new Label(getEmojiForItinerary(itineraire));
        iconLabel.setStyle("-fx-font-size: 24;");
        iconBox.getChildren().add(iconLabel);

        // Titre et destination
        VBox titleBox = new VBox(2);
        Label nameLabel = new Label(itineraire.getNom_itineraire());
        nameLabel.setStyle("-fx-font-weight: 700; -fx-text-fill: #0f172a; -fx-font-size: 16;");

        HBox infoBox = new HBox(12);
        HBox destBox = new HBox(4);
        destBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label destIcon = new Label("📍");
        destIcon.setStyle("-fx-font-size: 10;");

        VoyageInfo voyageInfo = getVoyageInfoById(itineraire.getId_voyage());
        String affichage = voyageInfo.titre;
        if (voyageInfo.destination != null && !voyageInfo.destination.isEmpty()) {
            affichage += " - " + voyageInfo.destination;
        }

        Label destLabel = new Label(affichage);
        destLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11;");
        destBox.getChildren().addAll(destIcon, destLabel);

        infoBox.getChildren().add(destBox);
        titleBox.getChildren().addAll(nameLabel, infoBox);

        // Espaceur
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        // Boutons d'action
        HBox actionButtons = new HBox(8);
        actionButtons.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

        Button editButton = new Button("✏️");
        editButton.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #475569; -fx-background-radius: 8; -fx-padding: 6 10; -fx-font-size: 12; -fx-cursor: hand;");
        editButton.setTooltip(new Tooltip("Modifier"));
        editButton.setOnAction(e -> handleModifierItineraire(itineraire));

        Button deleteButton = new Button("🗑️");
        deleteButton.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #ef4444; -fx-background-radius: 8; -fx-padding: 6 10; -fx-font-size: 12; -fx-cursor: hand;");
        deleteButton.setTooltip(new Tooltip("Supprimer"));
        deleteButton.setOnAction(e -> handleSupprimerItineraire(itineraire));

        actionButtons.getChildren().addAll(editButton, deleteButton);

        // Assembler l'en-tête
        header.getChildren().addAll(iconBox, titleBox, spacer, actionButtons);

        // Description
        Label descLabel = new Label(itineraire.getDescription_itineraire() != null ?
                itineraire.getDescription_itineraire() : "Aucune description");
        descLabel.setStyle("-fx-text-fill: #475569; -fx-font-size: 11; -fx-padding: 0 0 4 0;");
        descLabel.setWrapText(true);

        // Séparateur
        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: #e2e8f0;");

        // Jours du voyage (CERCLES CLIQUABLES)
        VBox joursBox = new VBox(4);
        joursBox.setStyle("-fx-padding: 8 0 0 0;");

        Label titreJours = new Label("Jours du voyage :");
        titreJours.setStyle("-fx-font-weight: 600; -fx-text-fill: #0f172a; -fx-font-size: 11; -fx-padding: 0 0 4 0;");
        joursBox.getChildren().add(titreJours);

        if (voyageInfo.nbJours > 0) {
            HBox ligne1 = new HBox(15);
            HBox ligne2 = new HBox(15);
            ligne1.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            ligne2.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

            for (int i = 1; i <= voyageInfo.nbJours; i++) {
                // Créer le cercle pour chaque jour
                VBox jourCircle = new VBox();
                jourCircle.setAlignment(javafx.geometry.Pos.CENTER);
                jourCircle.setStyle("-fx-background-color: #fff7ed; -fx-background-radius: 50%; -fx-min-width: 40; -fx-min-height: 40; -fx-border-color: #ff8c42; -fx-border-width: 1; -fx-border-radius: 50%; -fx-cursor: hand;");

                Label jourLabel = new Label("J" + i);
                jourLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: #ff8c42; -fx-font-size: 12;");

                // EFFET DE SURVOL
                jourCircle.setOnMouseEntered(event -> {
                    jourCircle.setStyle("-fx-background-color: #ff8c42; -fx-background-radius: 50%; -fx-min-width: 40; -fx-min-height: 40; -fx-border-color: #ff8c42; -fx-border-width: 1; -fx-border-radius: 50%; -fx-cursor: hand;");
                    jourLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: white; -fx-font-size: 12;");
                });

                jourCircle.setOnMouseExited(event -> {
                    jourCircle.setStyle("-fx-background-color: #fff7ed; -fx-background-radius: 50%; -fx-min-width: 40; -fx-min-height: 40; -fx-border-color: #ff8c42; -fx-border-width: 1; -fx-border-radius: 50%; -fx-cursor: hand;");
                    jourLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: #ff8c42; -fx-font-size: 12;");
                });

                // ACTION AU CLIC - Ouvre la page des détails du jour
                final int jourNum = i;
                jourCircle.setOnMouseClicked(e -> handleJourClick(jourNum, itineraire.getId_itineraire(), itineraire.getNom_itineraire()));

                jourCircle.getChildren().add(jourLabel);

                // Répartir sur deux lignes (5 premiers sur ligne1, les suivants sur ligne2)
                if (i <= 5) {
                    ligne1.getChildren().add(jourCircle);
                } else {
                    ligne2.getChildren().add(jourCircle);
                }
            }

            joursBox.getChildren().add(ligne1);
            if (!ligne2.getChildren().isEmpty()) {
                joursBox.getChildren().add(ligne2);
            }
        } else {
            Label pasJours = new Label("  Dates non définies");
            pasJours.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 10; -fx-font-style: italic;");
            joursBox.getChildren().add(pasJours);
        }

        // Assembler la carte
        card.getChildren().addAll(header, descLabel, separator, joursBox);

        return card;
    }

    private void handleModifierItineraire(Itineraire itineraire) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/PageModifierItineraire.fxml"));
            BorderPane modifierView = loader.load();

            PageModifierItineraire modifierController = loader.getController();
            modifierController.setItineraire(itineraire);
            modifierController.setParentController(this);

            mainBorderPane.setCenter(modifierView);

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le formulaire de modification: " + e.getMessage());
        }
    }

    private void handleSupprimerItineraire(Itineraire itineraire) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer l'itinéraire");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer l'itinéraire \"" + itineraire.getNom_itineraire() + "\" ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                itineraireCRUD.supprimer(itineraire.getId_itineraire());
                refreshItineraries();
                showAlert("Succès", "Itinéraire supprimé avec succès !");
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Erreur", "Impossible de supprimer l'itinéraire: " + e.getMessage());
            }
        }
    }

    // Classe interne pour stocker les informations du voyage
    private class VoyageInfo {
        String titre;
        String destination;
        int nbJours;

        VoyageInfo(String titre, String destination, int nbJours) {
            this.titre = titre;
            this.destination = destination;
            this.nbJours = nbJours;
        }
    }

    private VoyageInfo getVoyageInfoById(int idVoyage) {
        String titre = null;
        String destination = null;
        int nbJours = 0;

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = MyBD.getInstance().getConn();
            String query = "SELECT v.titre_voyage, v.date_debut, v.date_fin, d.nom_destination " +
                    "FROM voyage v " +
                    "LEFT JOIN destination d ON v.id_destination = d.id_destination " +
                    "WHERE v.id_voyage = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, idVoyage);
            rs = stmt.executeQuery();

            if (rs.next()) {
                titre = rs.getString("titre_voyage");
                destination = rs.getString("nom_destination");
                Date dateDebut = rs.getDate("date_debut");
                Date dateFin = rs.getDate("date_fin");

                if (dateDebut != null && dateFin != null) {
                    long diff = dateFin.getTime() - dateDebut.getTime();
                    nbJours = (int) (diff / (1000 * 60 * 60 * 24)) + 1;
                }
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

        return new VoyageInfo(titre, destination, nbJours);
    }

    private String getEmojiForItinerary(Itineraire itineraire) {
        String nom = itineraire.getNom_itineraire().toLowerCase();
        if (nom.contains("plage") || nom.contains("mer")) return "🏖️";
        if (nom.contains("montagne") || nom.contains("ski")) return "🏔️";
        if (nom.contains("rome") || nom.contains("histoire")) return "🏛️";
        if (nom.contains("paris") || nom.contains("ville")) return "🌆";
        return "🗺️";
    }

    @FXML
    private void handleCreerClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/PageAjoutItineraire.fxml"));

            if (loader.getLocation() == null) {
                showAlert("Erreur", "Fichier FXML non trouvé: /PageAjoutItineraire.fxml");
                return;
            }

            BorderPane ajoutView = loader.load();

            PageAjoutItineraire ajoutController = loader.getController();
            ajoutController.setParentController(this);

            mainBorderPane.setCenter(ajoutView);

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage());
        }
    }

    @FXML
    private void handleRechercherClick() {
        refreshItineraries();
    }

    @FXML
    private void handleVoirTousProchainsDeparts() {
        // À implémenter
    }

    @FXML
    private void handleTrierParDate() {
        // À implémenter
    }

    @FXML
    private void handleExporter() {
        // À implémenter
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void handleJourClick(int numeroJour, int idItineraire, String nomItineraire) {
        try {
            // CHANGER LE NOM DU FICHIER ICI - Utilisez le vrai nom de votre fichier FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/PageEtape.fxml"));

            // Vérifier si le fichier existe
            if (loader.getLocation() == null) {
                showAlert("Erreur", "Fichier FXML non trouvé: /PageEtape.fxml");
                return;
            }

            BorderPane detailsJourView = loader.load();

            // Récupérer le contrôleur (qui doit être PageDetailsJour)
            Object controller = loader.getController();

            // Vérifier le type du contrôleur
            if (controller instanceof PageEtape) {
                PageEtape detailsJourController = (PageEtape) controller;
                detailsJourController.setJourInfo(numeroJour, idItineraire, nomItineraire);
            } else {
                System.out.println("Le contrôleur n'est pas de type PageDetailsJour: " + controller.getClass().getName());
            }

            mainBorderPane.setCenter(detailsJourView);

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir les détails du jour: " + e.getMessage());
        }
    }
}