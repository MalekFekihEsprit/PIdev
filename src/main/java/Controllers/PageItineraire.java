package Controllers;

import Services.itineraireCRUD;
import Entites.Itineraire;
import Utils.MyBD;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.io.IOException;
import java.sql.*;
import java.util.List;
import java.util.Optional;

public class PageItineraire {
    @FXML private ComboBox<String> comboDestination;
    @FXML private DatePicker dateDebut;
    @FXML private DatePicker dateFin;
    @FXML private Button btnRechercher;
    @FXML private Label lblTotalItinerairesTable;
    @FXML private VBox emptyPlaceholder;
    @FXML private Button btnCreerPremierItineraire;
    @FXML private VBox itinerariesContainer;

    private itineraireCRUD itineraireCRUD;
    private List<Itineraire> itineraires;
    private MainLayoutController mainLayoutController;

    @FXML
    public void initialize() {
        itineraireCRUD = new itineraireCRUD();
        refreshItineraries();
        // Récupérer le MainLayoutController
        mainLayoutController = MainLayoutController.getInstance();
        System.out.println("PageItineraire initialisée avec mainLayoutController: " + (mainLayoutController != null));
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
            lblTotalItinerairesTable.setText("0 itinéraire");
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
            try {
                VBox card = createItineraryCard(itineraire);
                itinerariesContainer.getChildren().add(card);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private VBox createItineraryCard(Itineraire itineraire) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ItineraryCard.fxml"));
            VBox card = loader.load();

            ItineraryCardController controller = loader.getController();

            if (controller == null) {
                System.err.println("Erreur: ItineraryCardController est null");
                return createSimpleCard(itineraire);
            }

            VoyageInfo voyageInfo = getVoyageInfoById(itineraire.getId_voyage());
            String affichage = voyageInfo.titre;
            if (voyageInfo.destination != null && !voyageInfo.destination.isEmpty()) {
                affichage += " - " + voyageInfo.destination;
            }

            controller.setData(
                    itineraire,
                    affichage,
                    voyageInfo.nbJours,
                    getEmojiForItinerary(itineraire),
                    this::handleModifierItineraire,
                    this::handleSupprimerItineraire,
                    this::handleJourClick
            );

            return card;

        } catch (IOException e) {
            e.printStackTrace();
            return createSimpleCard(itineraire);
        }
    }

    private VBox createSimpleCard(Itineraire itineraire) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-border-radius: 16; -fx-padding: 16;");

        Label nameLabel = new Label(itineraire.getNom_itineraire());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");

        Label descLabel = new Label(itineraire.getDescription_itineraire());
        descLabel.setWrapText(true);

        card.getChildren().addAll(nameLabel, descLabel);
        return card;
    }

    private void handleModifierItineraire(Itineraire itineraire) {
        try {
            System.out.println("Tentative de modification de l'itinéraire: " + itineraire.getNom_itineraire());

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/PageModifierItineraire.fxml"));
            Parent modifierView = loader.load();

            PageModifierItineraire modifierController = loader.getController();
            modifierController.setItineraire(itineraire);
            modifierController.setParentController(this);

            if (mainLayoutController != null) {
                System.out.println("Chargement de la page de modification");
                mainLayoutController.loadPageDirect(modifierView);
            } else {
                System.err.println("mainLayoutController est null!");
            }

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
        String titre = "Voyage";
        String destination = "";
        int nbJours = 5;

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
            System.out.println("Création d'un nouvel itinéraire");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/PageAjoutItineraire.fxml"));

            if (loader.getLocation() == null) {
                showAlert("Erreur", "Fichier FXML non trouvé: /PageAjoutItineraire.fxml");
                return;
            }

            Parent ajoutView = loader.load();

            PageAjoutItineraire ajoutController = loader.getController();
            ajoutController.setParentController(this);

            if (mainLayoutController != null) {
                System.out.println("Chargement de la page d'ajout");
                mainLayoutController.loadPageDirect(ajoutView);
            } else {
                System.err.println("mainLayoutController est null!");
            }

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
    private void handleTrierParDate() {
        // À implémenter
    }

    @FXML
    private void handleExporter() {
        // À implémenter
    }

    private void handleJourClick(int numeroJour, int idItineraire, String nomItineraire) {
        try {
            System.out.println("Clic sur le jour " + numeroJour + " de l'itinéraire: " + nomItineraire);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/PageEtape.fxml"));
            Parent detailsJourView = loader.load();

            PageEtape detailsJourController = loader.getController();
            detailsJourController.setJourInfo(numeroJour, idItineraire, nomItineraire);

            if (mainLayoutController != null) {
                System.out.println("Chargement de la page des étapes");
                mainLayoutController.loadPageDirect(detailsJourView);
            } else {
                System.err.println("mainLayoutController est null!");
                // Tentative de récupération
                mainLayoutController = MainLayoutController.getInstance();
                if (mainLayoutController != null) {
                    mainLayoutController.loadPageDirect(detailsJourView);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir les détails du jour: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleOuvrirGestion() {
        if (mainLayoutController != null) {
            mainLayoutController.loadPage("/PageGestionItineraires.fxml");
        }
    }
}