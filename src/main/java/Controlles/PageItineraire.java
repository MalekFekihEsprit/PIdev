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
import java.util.List;
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
        card.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-border-radius: 16; -fx-padding: 16; -fx-cursor: hand;");

        card.setOnMouseClicked(event -> {
            showAlert("Itinéraire", "Détails de: " + itineraire.getNom_itineraire());
        });

        HBox header = new HBox(12);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        VBox iconBox = new VBox();
        iconBox.setAlignment(javafx.geometry.Pos.CENTER);
        iconBox.setStyle("-fx-background-color: #fff7ed; -fx-background-radius: 12; -fx-min-width: 48; -fx-min-height: 48;");
        Label iconLabel = new Label(getEmojiForItinerary(itineraire));
        iconLabel.setStyle("-fx-font-size: 24;");
        iconBox.getChildren().add(iconLabel);

        VBox titleBox = new VBox(2);
        Label nameLabel = new Label(itineraire.getNom_itineraire());
        nameLabel.setStyle("-fx-font-weight: 700; -fx-text-fill: #0f172a; -fx-font-size: 16;");

        HBox infoBox = new HBox(12);
        HBox destBox = new HBox(4);
        destBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label destIcon = new Label("📍");
        destIcon.setStyle("-fx-font-size: 10;");

        String voyageTitre = getVoyageTitreById(itineraire.getId_voyage());
        Label destLabel = new Label(voyageTitre != null ? voyageTitre : "Voyage #" + itineraire.getId_voyage());
        destLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11;");
        destBox.getChildren().addAll(destIcon, destLabel);

        infoBox.getChildren().add(destBox);
        titleBox.getChildren().addAll(nameLabel, infoBox);

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        header.getChildren().addAll(iconBox, titleBox, spacer);

        Label descLabel = new Label(itineraire.getDescription_itineraire() != null ?
                itineraire.getDescription_itineraire() : "Aucune description");
        descLabel.setStyle("-fx-text-fill: #475569; -fx-font-size: 11; -fx-padding: 0 0 4 0;");
        descLabel.setWrapText(true);

        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: #e2e8f0;");

        card.getChildren().addAll(header, descLabel, separator);

        return card;
    }

    private String getVoyageTitreById(int idVoyage) {
        String titre = null;
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = MyBD.getInstance().getConn();
            String query = "SELECT titre_voyage FROM voyage WHERE id_voyage = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, idVoyage);
            rs = stmt.executeQuery();

            if (rs.next()) {
                titre = rs.getString("titre_voyage");
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

        return titre;
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
}