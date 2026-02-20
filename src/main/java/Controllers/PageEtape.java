package Controllers;

import Entites.etape;
import Services.etapeCRUD;
import Utils.AlertUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
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
    @FXML private Label lblTotalEtapes;
    @FXML private Label lblDureeTotale;
    @FXML private VBox etapesContainer;
    @FXML private VBox emptyPlaceholder;
    @FXML private ComboBox<String> triCombo; // NOUVEAU: ComboBox pour le tri
    @FXML private ComboBox<String> filtreTypeCombo; // NOUVEAU: Filtre par type d'activité

    private int numeroJour;
    private int idItineraire;
    private String nomItineraire;
    private etapeCRUD etapeCRUD = new etapeCRUD();
    private List<etape> etapes;
    private List<etape> etapesOriginales;
    private MainLayoutController mainLayoutController;

    @FXML
    public void initialize() {
        System.out.println("PageEtape initialisée");
        mainLayoutController = MainLayoutController.getInstance();

        // NOUVEAU: Initialiser les options de tri
        if (triCombo != null) {
            triCombo.getItems().addAll(
                    "Par heure (croissante)",
                    "Par heure (décroissante)",
                    "Par activité (A → Z)",
                    "Par activité (Z → A)",
                    "Par durée (croissante)",
                    "Par durée (décroissante)"
            );
            triCombo.getSelectionModel().selectFirst();
            triCombo.setOnAction(e -> appliquerTri());
        }

        // NOUVEAU: Initialiser le filtre par type d'activité
        if (filtreTypeCombo != null) {
            filtreTypeCombo.getItems().add("Tous les types");
            filtreTypeCombo.getSelectionModel().selectFirst();
            filtreTypeCombo.setOnAction(e -> appliquerFiltre());
        }
    }

    public void setJourInfo(int numeroJour, int idItineraire, String nomItineraire) {
        this.numeroJour = numeroJour;
        this.idItineraire = idItineraire;
        this.nomItineraire = nomItineraire;

        if (titreJour != null) {
            titreJour.setText("Jour " + numeroJour);
        }
        if (jourIcon != null) {
            jourIcon.setText(String.valueOf(numeroJour));
        }
        if (sousTitreJour != null) {
            sousTitreJour.setText("Planification des activités pour le jour " + numeroJour);
        }

        chargerEtapes();
    }

    private void chargerEtapes() {
        try {
            etapes = etapeCRUD.getEtapesByItineraire(idItineraire);
            etapesOriginales = etapes; // Conserver une copie originale

            // NOUVEAU: Charger les types d'activités pour le filtre
            if (filtreTypeCombo != null && etapes != null) {
                filtreTypeCombo.getItems().clear();
                filtreTypeCombo.getItems().add("Tous les types");
                etapes.stream()
                        .map(e -> e.getNomActivite())
                        .filter(nom -> nom != null && !nom.isEmpty())
                        .distinct()
                        .sorted()
                        .forEach(nom -> filtreTypeCombo.getItems().add(nom));
                filtreTypeCombo.getSelectionModel().selectFirst();
            }

            updateEtapesDisplay();
        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtil.showError("Erreur", "Impossible de charger les étapes: " + e.getMessage());
        }
    }

    // NOUVELLE MÉTHODE: Appliquer le filtre par type d'activité
    private void appliquerFiltre() {
        if (etapesOriginales == null || filtreTypeCombo == null) return;

        String selectedType = filtreTypeCombo.getSelectionModel().getSelectedItem();
        if (selectedType == null || selectedType.equals("Tous les types")) {
            etapes = etapesOriginales;
        } else {
            etapes = etapesOriginales.stream()
                    .filter(e -> selectedType.equals(e.getNomActivite()))
                    .toList();
        }

        appliquerTri(); // Réappliquer le tri après le filtre
    }

    // NOUVELLE MÉTHODE: Appliquer le tri sélectionné
    private void appliquerTri() {
        if (etapes == null || etapes.isEmpty() || triCombo == null) return;

        String selectedTri = triCombo.getSelectionModel().getSelectedItem();
        if (selectedTri == null) return;

        try {
            switch (selectedTri) {
                case "Par heure (croissante)":
                    etapes = etapeCRUD.trierParHeure(etapes, true);
                    break;
                case "Par heure (décroissante)":
                    etapes = etapeCRUD.trierParHeure(etapes, false);
                    break;
                case "Par activité (A → Z)":
                    etapes = etapeCRUD.trierParTypeActivite(etapes, true);
                    break;
                case "Par activité (Z → A)":
                    etapes = etapeCRUD.trierParTypeActivite(etapes, false);
                    break;
                case "Par durée (croissante)":
                    etapes = etapeCRUD.trierParDuree(etapes, true);
                    break;
                case "Par durée (décroissante)":
                    etapes = etapeCRUD.trierParDuree(etapes, false);
                    break;
                default:
                    break;
            }
            updateEtapesDisplay();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateEtapesDisplay() {
        if (etapes == null || etapes.isEmpty()) {
            if (emptyPlaceholder != null) {
                emptyPlaceholder.setVisible(true);
                emptyPlaceholder.setManaged(true);
            }
            if (etapesContainer != null) {
                etapesContainer.setVisible(false);
                etapesContainer.setManaged(false);
            }
            if (lblTotalEtapes != null) {
                lblTotalEtapes.setText("Total: 0 étape");
            }
            if (lblDureeTotale != null) {
                lblDureeTotale.setText("Durée totale: 0h");
            }
        } else {
            if (emptyPlaceholder != null) {
                emptyPlaceholder.setVisible(false);
                emptyPlaceholder.setManaged(false);
            }
            if (etapesContainer != null) {
                etapesContainer.setVisible(true);
                etapesContainer.setManaged(true);
            }

            if (lblTotalEtapes != null) {
                lblTotalEtapes.setText("Total: " + etapes.size() + " étape" + (etapes.size() > 1 ? "s" : ""));
            }

            int dureeTotale = calculerDureeTotale();
            if (lblDureeTotale != null) {
                lblDureeTotale.setText("Durée totale: " + dureeTotale + "h");
            }

            generateEtapeCards();
        }
    }

    private int calculerDureeTotale() {
        int total = 0;
        for (etape e : etapes) {
            if (e.getDureeActivite() != null) {
                total += e.getDureeActivite();
            }
        }
        return total;
    }

    private void generateEtapeCards() {
        if (etapesContainer == null) return;

        etapesContainer.getChildren().clear();
        for (etape etape : etapes) {
            VBox card = createEtapeCard(etape);
            etapesContainer.getChildren().add(card);
        }
    }

    private VBox createEtapeCard(etape etape) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-border-radius: 12; -fx-padding: 16; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.02), 4, 0, 0, 1);");

        HBox header = new HBox(12);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        VBox timeBox = new VBox();
        timeBox.setAlignment(javafx.geometry.Pos.CENTER);
        timeBox.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 8; -fx-min-width: 60; -fx-min-height: 40;");

        String heure = etape.getHeure() != null ? etape.getHeure().toString().substring(0, 5) : "--:--";
        Label timeLabel = new Label(heure);
        timeLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: #0f172a; -fx-font-size: 14;");
        timeBox.getChildren().add(timeLabel);

        VBox activiteBox = new VBox(2);

        String nomActivite = etape.getNomActivite() != null ? etape.getNomActivite() : "Activité inconnue";
        Label nomActiviteLabel = new Label(nomActivite);
        nomActiviteLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: #0f172a; -fx-font-size: 14;");

        HBox infoBox = new HBox(10);

        HBox lieuBox = new HBox(4);
        lieuBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label iconLieu = new Label("📍");
        iconLieu.setStyle("-fx-font-size: 10;");
        String lieu = etape.getLieuActivite() != null ? etape.getLieuActivite() : "Lieu non défini";
        Label lieuLabel = new Label(lieu);
        lieuLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11;");
        lieuBox.getChildren().addAll(iconLieu, lieuLabel);
        infoBox.getChildren().add(lieuBox);

        if (etape.getDureeActivite() != null && etape.getDureeActivite() > 0) {
            HBox dureeBox = new HBox(4);
            dureeBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            Label iconDuree = new Label("⏱️");
            iconDuree.setStyle("-fx-font-size: 10;");
            Label dureeLabel = new Label(String.format("%.1fh", etape.getDureeActivite()));
            dureeLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11;");
            dureeBox.getChildren().addAll(iconDuree, dureeLabel);
            infoBox.getChildren().add(dureeBox);
        }

        // NOUVEAU: Ajouter le niveau de difficulté
        if (etape.getNiveauDifficulteActivite() != null) {
            HBox niveauBox = new HBox(4);
            niveauBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            Label iconNiveau = new Label("📊");
            iconNiveau.setStyle("-fx-font-size: 10;");
            Label niveauLabel = new Label(etape.getNiveauTexte());
            niveauLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11;");
            niveauBox.getChildren().addAll(iconNiveau, niveauLabel);
            infoBox.getChildren().add(niveauBox);
        }

        activiteBox.getChildren().addAll(nomActiviteLabel, infoBox);

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

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

        header.getChildren().addAll(timeBox, activiteBox, spacer, actionButtons);

        Label descLabel = new Label(etape.getDescription_etape() != null && !etape.getDescription_etape().isEmpty() ?
                etape.getDescription_etape() : "Aucune description");
        descLabel.setStyle("-fx-text-fill: #475569; -fx-font-size: 11;");
        descLabel.setWrapText(true);
        VBox.setMargin(descLabel, new Insets(4, 0, 0, 0));

        card.getChildren().addAll(header, descLabel);

        return card;
    }

    private void handleModifierEtape(etape etape) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/PageAjoutEtape.fxml"));
            DialogPane dialogPane = loader.load();

            PageAjoutEtape modifierController = loader.getController();
            modifierController.setDialogPane(dialogPane);
            modifierController.setEtapeAModifier(etape);
            modifierController.setOnEtapeAjoutee(this::chargerEtapes);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Modifier l'étape - Jour " + numeroJour);
            dialog.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Erreur", "Impossible d'ouvrir le formulaire de modification: " + e.getMessage());
        }
    }

    private void handleSupprimerEtape(etape etape) {
        if (AlertUtil.showConfirmation("Confirmation de suppression",
                "Êtes-vous sûr de vouloir supprimer cette étape ?")) {
            try {
                etapeCRUD.supprimer(etape.getId_etape());
                chargerEtapes();
                AlertUtil.showInfo("Succès", "Étape supprimée avec succès !");
            } catch (SQLException e) {
                e.printStackTrace();
                AlertUtil.showError("Erreur", "Impossible de supprimer l'étape: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleAjouterEtape() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/PageAjoutEtape.fxml"));
            DialogPane dialogPane = loader.load();

            PageAjoutEtape ajoutController = loader.getController();
            ajoutController.setDialogPane(dialogPane);
            ajoutController.setJourInfo(numeroJour, idItineraire);
            ajoutController.setOnEtapeAjoutee(this::chargerEtapes);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Ajouter une étape - Jour " + numeroJour);
            dialog.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Erreur", "Impossible d'ouvrir le formulaire d'ajout: " + e.getMessage());
        }
    }

    @FXML
    private void handleRetour() {
        if (mainLayoutController != null) {
            mainLayoutController.loadPage("/PageItineraire.fxml");
        }
    }
}