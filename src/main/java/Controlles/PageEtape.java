package Controlles;

import Entites.etape;
import Services.etapeCRUD;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class PageEtape {

    @FXML private Label jourIcon;
    @FXML private Label titreJour;
    @FXML private Label sousTitreJour;
    @FXML private Label breadcrumbJour;
    @FXML private Label lblTotalEtapes;
    @FXML private Label lblDureeTotale;
    @FXML private VBox etapesContainer;
    @FXML private VBox emptyPlaceholder;
    @FXML private BorderPane mainBorderPane;

    private int numeroJour;
    private int idItineraire;
    private String nomItineraire;
    private etapeCRUD etapeCRUD = new etapeCRUD();
    private List<etape> etapes;

    public void setJourInfo(int numeroJour, int idItineraire, String nomItineraire) {
        this.numeroJour = numeroJour;
        this.idItineraire = idItineraire;
        this.nomItineraire = nomItineraire;

        titreJour.setText("Jour " + numeroJour);
        jourIcon.setText(String.valueOf(numeroJour));
        breadcrumbJour.setText("Jour " + numeroJour + " - " + nomItineraire);
        sousTitreJour.setText("Planification des activités pour le jour " + numeroJour);

        chargerEtapes();
    }

    @FXML
    public void initialize() {
        // Initialisation
    }

    private void chargerEtapes() {
        try {
            // Récupérer toutes les étapes de l'itinéraire
            etapes = etapeCRUD.getEtapesByItineraire(idItineraire);

            // Filtrer par jour si vous avez un champ jour dans votre table
            // Pour l'instant, on prend toutes les étapes
            updateEtapesDisplay();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les étapes: " + e.getMessage());
        }
    }

    private void updateEtapesDisplay() {
        if (etapes == null || etapes.isEmpty()) {
            emptyPlaceholder.setVisible(true);
            emptyPlaceholder.setManaged(true);
            etapesContainer.setVisible(false);
            etapesContainer.setManaged(false);
            lblTotalEtapes.setText("Total: 0 étape");
            lblDureeTotale.setText("Durée totale: 0h");
        } else {
            emptyPlaceholder.setVisible(false);
            emptyPlaceholder.setManaged(false);
            etapesContainer.setVisible(true);
            etapesContainer.setManaged(true);

            lblTotalEtapes.setText("Total: " + etapes.size() + " étape" + (etapes.size() > 1 ? "s" : ""));

            // Calculer la durée totale (à implémenter selon votre logique)
            int dureeTotale = calculerDureeTotale();
            lblDureeTotale.setText("Durée totale: " + dureeTotale + "h");

            generateEtapeCards();
        }
    }

    private int calculerDureeTotale() {
        // À implémenter selon votre logique de durée
        // Pour l'instant, retourne une valeur par défaut
        return etapes.size() * 2; // Exemple: 2h par étape
    }

    private void generateEtapeCards() {
        etapesContainer.getChildren().clear();

        for (etape etape : etapes) {
            VBox card = createEtapeCard(etape);
            etapesContainer.getChildren().add(card);
        }
    }

    private VBox createEtapeCard(etape etape) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-border-radius: 12; -fx-padding: 16; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.02), 4, 0, 0, 1);");

        // En-tête avec heure et lieu
        HBox header = new HBox(12);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // Heure
        VBox timeBox = new VBox();
        timeBox.setAlignment(javafx.geometry.Pos.CENTER);
        timeBox.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 8; -fx-min-width: 60; -fx-min-height: 40;");
        String heure = etape.getHeure() != null ? etape.getHeure().toString().substring(0, 5) : "--:--";
        Label timeLabel = new Label(heure);
        timeLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: #0f172a; -fx-font-size: 14;");
        timeBox.getChildren().add(timeLabel);

        // Lieu
        VBox lieuBox = new VBox(2);
        Label lieuLabel = new Label(etape.getLieu() != null ? etape.getLieu() : "Lieu non défini");
        lieuLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: #0f172a; -fx-font-size: 14;");

        HBox lieuIconBox = new HBox(4);
        lieuIconBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label iconLieu = new Label("📍");
        iconLieu.setStyle("-fx-font-size: 10;");
        Label lieuDetail = new Label("Adresse");
        lieuDetail.setStyle("-fx-text-fill: #64748b; -fx-font-size: 10;");
        lieuIconBox.getChildren().addAll(iconLieu, lieuDetail);

        lieuBox.getChildren().addAll(lieuLabel, lieuIconBox);

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        // Boutons d'action
        HBox actionButtons = new HBox(8);

        Button editButton = new Button("✏️");
        editButton.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #475569; -fx-background-radius: 6; -fx-padding: 4 8; -fx-font-size: 12; -fx-cursor: hand;");
        editButton.setTooltip(new Tooltip("Modifier"));
        editButton.setOnAction(e -> handleModifierEtape(etape));

        Button deleteButton = new Button("🗑️");
        deleteButton.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #ef4444; -fx-background-radius: 6; -fx-padding: 4 8; -fx-font-size: 12; -fx-cursor: hand;");
        deleteButton.setTooltip(new Tooltip("Supprimer"));
        deleteButton.setOnAction(e -> handleSupprimerEtape(etape));

        actionButtons.getChildren().addAll(editButton, deleteButton);

        header.getChildren().addAll(timeBox, lieuBox, spacer, actionButtons);

        // Description
        Label descLabel = new Label(etape.getDescription_etape() != null ?
                etape.getDescription_etape() : "Aucune description");
        descLabel.setStyle("-fx-text-fill: #475569; -fx-font-size: 11;");
        descLabel.setWrapText(true);
        VBox.setMargin(descLabel, new Insets(4, 0, 0, 0));

        // Métadonnées
        HBox metaBox = new HBox(15);
        metaBox.setStyle("-fx-padding: 8 0 0 0;");

        if (etape.getId_activite() > 0) {
            Label activiteLabel = new Label("🎯 Activité #" + etape.getId_activite());
            activiteLabel.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #475569; -fx-background-radius: 12; -fx-padding: 2 8; -fx-font-size: 10;");
            metaBox.getChildren().add(activiteLabel);
        }

        card.getChildren().addAll(header, descLabel, metaBox);

        return card;
    }

    private void handleModifierEtape(etape etape) {
        // À implémenter - ouvrir formulaire de modification
        showAlert("Modifier", "Fonctionnalité à implémenter");
    }

    private void handleSupprimerEtape(etape etape) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer l'étape");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer cette étape ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                etapeCRUD.supprimer(etape.getId_etape());
                chargerEtapes(); // Recharger la liste
                showAlert("Succès", "Étape supprimée avec succès !");
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Erreur", "Impossible de supprimer l'étape: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleAjouterEtape() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/PageAjoutEtape.fxml"));
            DialogPane dialogPane = loader.load();

            PageAjoutEtape ajoutController = loader.getController();
            ajoutController.setJourInfo(numeroJour, idItineraire);
            ajoutController.setOnEtapeAjoutee(() -> {
                chargerEtapes(); // Recharger la liste après ajout
            });

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Ajouter une étape - Jour " + numeroJour);

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // L'ajout est déjà géré par le contrôleur
            }

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le formulaire d'ajout: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

}