package Controllers;

import Entities.Voyage;
import Services.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;

public class CarteVoyageController {

    @FXML
    private Label iconLabel;
    @FXML
    private Label titreVoyage;
    @FXML
    private Label statutVoyage;
    @FXML
    private Label datesVoyage;
    @FXML
    private Label destinationVoyage;
    @FXML
    private Label prixVoyage;
    @FXML
    private Label participantsVoyage;
    @FXML
    private Label activitesVoyage;
    @FXML
    private Label descriptionVoyage;
    @FXML
    private Label btnModifier;
    @FXML
    private Label btnPaiement;
    @FXML
    private Label btnParticipants;
    @FXML
    private Label btnSupprimer;
    @FXML
    private Label btnGenererDescription;
    @FXML
    private Label btnQRCode;
    @FXML
    private Label btnInfosPays;

    private Voyage voyage;
    private String nomDestination;
    private VoyageDescriptionService descriptionService;
    private final VoyageCRUDV voyageCRUD = new VoyageCRUDV();
    private final QRCodeService qrCodeService = new QRCodeService();
    private final RestCountriesService restCountriesService = new RestCountriesService();
    private final UnsplashService unsplashService = new UnsplashService();

    @FXML
    public void initialize() {
        descriptionService = new VoyageDescriptionService();
    }

    public void setDonnees(Voyage voyage, String nomDestination) {
        this.voyage = voyage;
        this.nomDestination = nomDestination;

        titreVoyage.setText(voyage.getTitre_voyage());
        destinationVoyage.setText(nomDestination);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.FRENCH);
        String dateDebutStr = voyage.getDate_debut().toLocalDate().format(formatter);
        String dateFinStr = voyage.getDate_fin().toLocalDate().format(formatter);
        datesVoyage.setText(dateDebutStr + " - " + dateFinStr);

        String statutTexte = voyage.getStatut() != null ? voyage.getStatut() : "a venir";
        statutVoyage.setText("● " + statutTexte);

        String statutCouleur;
        switch (statutTexte.toLowerCase()) {
            case "terminé":
                statutCouleur = "#64748b";
                break;
            case "en cours":
                statutCouleur = "#f59e0b";
                break;
            case "annulé":
                statutCouleur = "#ef4444";
                break;
            default:
                statutCouleur = "#10b981";
                break;
        }

        statutVoyage.setStyle("-fx-background-color: " + statutCouleur + "; -fx-text-fill: white; " +
                "-fx-background-radius: 20; -fx-padding: 3 10; -fx-font-size: 10; -fx-font-weight: 600;");

        iconLabel.setText(getIconePourDestination(nomDestination));

        prixVoyage.setText("150 €");
        participantsVoyage.setText("0 participants");
        activitesVoyage.setText("0 activités");

        // Initialiser la description
        descriptionVoyage.setText("Cliquez sur ✨ pour générer une description");

        btnModifier.setOnMouseClicked(e -> handleModifier());
        btnPaiement.setOnMouseClicked(e -> handlePaiement());
        btnParticipants.setOnMouseClicked(e -> handleParticipants());
        btnSupprimer.setOnMouseClicked(e -> handleSupprimer());
        btnGenererDescription.setOnMouseClicked(e -> genererDescription());
        btnQRCode.setOnMouseClicked(e -> afficherQRCode());
        btnInfosPays.setOnMouseClicked(e -> afficherInfosPays());
    }

    private String getIconePourDestination(String nomDestination) {
        if (nomDestination == null) return "🏖️";
        String dest = nomDestination.toLowerCase();
        if (dest.contains("paris")) return "🗼";
        if (dest.contains("rome") || dest.contains("itali")) return "🏛️";
        if (dest.contains("londres") || dest.contains("london")) return "🇬🇧";
        if (dest.contains("new york")) return "🗽";
        if (dest.contains("tokyo") || dest.contains("japon")) return "🗾";
        if (dest.contains("egypte") || dest.contains("caire")) return "🏺";
        if (dest.contains("maroc") || dest.contains("marrakech")) return "🕌";
        if (dest.contains("tunis")) return "🇹🇳";
        if (dest.contains("dubai") || dest.contains("abu dhabi")) return "🏙️";
        if (dest.contains("bali") || dest.contains("maldiv")) return "🌴";
        if (dest.contains("suisse") || dest.contains("alpes")) return "🏔️";
        if (dest.contains("espagne") || dest.contains("barcelon")) return "🇪🇸";
        if (dest.contains("grèce") || dest.contains("athèn")) return "🏛️";
        if (dest.contains("canada") || dest.contains("montréal")) return "🍁";
        if (dest.contains("australi") || dest.contains("sydney")) return "🦘";
        return "🏖️";
    }

    private void handleModifier() {
        System.out.println("Modifier voyage ID: " + voyage.getId_voyage());
        if (VoyageController.instance != null) {
            VoyageController.instance.chargerVoyagePourModification(voyage);
        } else {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'accéder au contrôleur principal");
        }
    }

    private void handlePaiement() {
        try {
            System.out.println("=== CHARGEMENT PAGE PAIEMENT ===");

            URL resource = getClass().getResource("/PagePaiement.fxml");

            if (resource == null) {
                resource = Thread.currentThread().getContextClassLoader().getResource("PagePaiement.fxml");
            }

            if (resource == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur",
                        "Fichier PagePaiement.fxml non trouvé!\n" +
                                "Vérifiez qu'il est bien dans src/main/resources/");
                return;
            }

            System.out.println("Fichier trouvé: " + resource);

            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();

            PaiementController paiementController = loader.getController();
            paiementController.initData(voyage.getId_voyage());

            Stage stage = (Stage) btnPaiement.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement: " + e.getMessage());
        }
    }

    private void handleParticipants() {
        try {
            URL resource = getClass().getResource("/PageParticipation.fxml");
            if (resource == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Fichier PageParticipation.fxml non trouvé!");
                return;
            }

            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();

            ParticipationController participationController = loader.getController();
            String dates = voyage.getDate_debut().toLocalDate() + " - " + voyage.getDate_fin().toLocalDate();
            participationController.initData(
                    voyage.getId_voyage(),
                    voyage.getTitre_voyage(),
                    nomDestination,
                    dates
            );

            Stage stage = (Stage) btnParticipants.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la page des participants: " + e.getMessage());
        }
    }

    private void handleSupprimer() {
        try {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation de suppression");
            alert.setHeaderText(null);
            alert.setContentText("Voulez-vous vraiment supprimer le voyage \"" + voyage.getTitre_voyage() + "\" ?");

            Optional<ButtonType> result = alert.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.OK) {
                voyageCRUD.supprimer(voyage.getId_voyage());

                showAlert(Alert.AlertType.INFORMATION, "Succès", "Voyage supprimé avec succès!");

                if (VoyageController.instance != null) {
                    VoyageController.instance.chargerVoyages();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la suppression: " + e.getMessage());
        }
    }

    private void genererDescription() {
        if (voyage == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Aucun voyage sélectionné");
            return;
        }

        String description = descriptionService.genererDescription(voyage, nomDestination);
        descriptionVoyage.setText(description);
    }

    private void afficherQRCode() {
        if (voyage == null) return;

        new Thread(() -> {
            try {
                Image qrImage = QRCodeServicess.genererQRCodeVoyage(voyage, nomDestination);

                javafx.application.Platform.runLater(() -> {
                    Stage popup = new Stage();
                    popup.setTitle("QR Code — " + voyage.getTitre_voyage());

                    VBox root = new VBox(16);
                    root.setAlignment(Pos.CENTER);
                    root.setPadding(new Insets(24));
                    root.setStyle("-fx-background-color: #0a0e27;");

                    Label titre = new Label("📱 QR Code du Voyage");
                    titre.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #ff8c42;");

                    ImageView imageView = new ImageView(qrImage);
                    imageView.setFitWidth(280);
                    imageView.setFitHeight(280);
                    imageView.setPreserveRatio(true);
                    imageView.setStyle("-fx-effect: dropshadow(gaussian, rgba(255,140,66,0.3), 20, 0, 0, 0);");

                    Label nomVoyage = new Label(voyage.getTitre_voyage());
                    nomVoyage.setStyle("-fx-font-size: 14; -fx-font-weight: 600; -fx-text-fill: #ffffff;");

                    Label dest = new Label("📍 " + nomDestination);
                    dest.setStyle("-fx-font-size: 12; -fx-text-fill: #94a3b8;");

                    Label instruction = new Label("Scannez ce QR code pour voir les détails du voyage");
                    instruction.setStyle("-fx-font-size: 10; -fx-text-fill: #64748b; -fx-font-style: italic;");

                    Label btnSave = new Label("💾 Sauvegarder en PNG");
                    btnSave.setStyle("-fx-background-color: #ff8c42; -fx-text-fill: white; -fx-background-radius: 16; " +
                            "-fx-padding: 8 20; -fx-font-size: 12; -fx-font-weight: 600; -fx-cursor: hand;");
                    btnSave.setOnMouseClicked(e -> sauvegarderQRCode(popup));

                    root.getChildren().addAll(titre, imageView, nomVoyage, dest, instruction, btnSave);

                    popup.setScene(new Scene(root, 380, 520));
                    popup.show();
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() ->
                        showAlert(Alert.AlertType.ERROR, "Erreur QR", "Impossible de générer le QR code: " + e.getMessage()));
            }
        }).start();
    }

    private void sauvegarderQRCode(Stage popup) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Sauvegarder le QR Code");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image PNG", "*.png"));
        fc.setInitialFileName("qr_" + voyage.getTitre_voyage().replaceAll("[^a-zA-Z0-9]", "_") + ".png");

        File file = fc.showSaveDialog(popup);
        if (file != null) {
            try {
                QRCodeServicess.sauvegarderQRCode(voyage, nomDestination, file);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "QR code sauvegardé !");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de sauvegarder: " + e.getMessage());
            }
        }
    }

    private void afficherInfosPays() {
        if (nomDestination == null || nomDestination.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Aucune destination définie pour ce voyage");
            return;
        }

        btnInfosPays.setDisable(true);

        new Thread(() -> {
            String paysRecherche = extrairePays(nomDestination);
            RestCountriesService.CountryInfo countryInfo = restCountriesService.getInfosPays(paysRecherche);
            UnsplashService.PhotoInfo photoInfo = unsplashService.rechercherPhotoInfo(nomDestination);

            javafx.application.Platform.runLater(() -> {
                btnInfosPays.setDisable(false);

                Stage popup = new Stage();
                popup.setTitle("🌍 " + nomDestination + " — Infos Destination");

                VBox root = new VBox(0);
                root.setStyle("-fx-background-color: #0a0e27;");

                if (photoInfo != null && !photoInfo.urlRegular.isEmpty()) {
                    try {
                        ImageView imageView = new ImageView(new Image(photoInfo.urlRegular, 500, 250, true, true, false));
                        imageView.setFitWidth(500);
                        imageView.setFitHeight(200);
                        imageView.setPreserveRatio(false);
                        root.getChildren().add(imageView);

                        Label credit = new Label(photoInfo.getCredit());
                        credit.setStyle("-fx-font-size: 9; -fx-text-fill: #64748b; -fx-padding: 4 16;");
                        root.getChildren().add(credit);
                    } catch (Exception e) {
                        System.out.println("⚠️ Image non chargée: " + e.getMessage());
                    }
                }

                VBox titleBox = new VBox(4);
                titleBox.setPadding(new Insets(16, 20, 12, 20));
                titleBox.setStyle("-fx-background-color: #111633;");

                Label titleLbl = new Label("🌍 " + nomDestination);
                titleLbl.setStyle("-fx-font-size: 20; -fx-font-weight: bold; -fx-text-fill: #ff8c42;");

                Label subtitleLbl = new Label("Informations sur la destination");
                subtitleLbl.setStyle("-fx-font-size: 11; -fx-text-fill: #94a3b8;");

                titleBox.getChildren().addAll(titleLbl, subtitleLbl);
                root.getChildren().add(titleBox);

                if (countryInfo != null) {
                    VBox cardsBox = new VBox(8);
                    cardsBox.setPadding(new Insets(16, 20, 16, 20));

                    cardsBox.getChildren().addAll(
                            buildInfoRow("🏳️", "Pays officiel", countryInfo.nomOfficiel),
                            buildInfoRow("🏛️", "Capitale", countryInfo.capitale),
                            buildInfoRow("👥", "Population", RestCountriesService.formatPopulation(countryInfo.population)),
                            buildInfoRow("🌍", "Continent", countryInfo.continent),
                            buildInfoRow("📍", "Région", countryInfo.sousRegion),
                            buildInfoRow("🗣️", "Langues", countryInfo.langues),
                            buildInfoRow("💱", "Devises", countryInfo.devises),
                            buildInfoRow("🕐", "Fuseau horaire", countryInfo.fuseauxHoraires)
                    );

                    if (!countryInfo.drapeauUrl.isEmpty()) {
                        try {
                            ImageView flag = new ImageView(new Image(countryInfo.drapeauUrl, 60, 40, true, true));
                            HBox flagBox = new HBox(8);
                            flagBox.setAlignment(Pos.CENTER_LEFT);
                            Label flagLbl = new Label("Drapeau");
                            flagLbl.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11;");
                            flagBox.getChildren().addAll(flag, flagLbl);
                            flagBox.setPadding(new Insets(8, 0, 0, 0));
                            cardsBox.getChildren().add(flagBox);
                        } catch (Exception ignored) {}
                    }

                    root.getChildren().add(cardsBox);
                } else {
                    Label noData = new Label("❌ Aucune information trouvée pour ce pays");
                    noData.setStyle("-fx-text-fill: #ec4899; -fx-font-size: 12; -fx-padding: 20;");
                    root.getChildren().add(noData);
                }

                Label footer = new Label("Données : REST Countries API" +
                        (photoInfo != null ? " • Photo : Unsplash" : ""));
                footer.setStyle("-fx-font-size: 9; -fx-text-fill: #4a5568; -fx-padding: 12 20;");
                root.getChildren().add(footer);

                ScrollPane scrollPane = new ScrollPane(root);
                scrollPane.setFitToWidth(true);
                scrollPane.setStyle("-fx-background: #0a0e27; -fx-background-color: #0a0e27;");

                popup.setScene(new Scene(scrollPane, 500, 600));
                popup.show();
            });
        }).start();
    }

    private String extrairePays(String destination) {
        if (destination == null) return "";
        String dest = destination.toLowerCase();
        if (dest.contains("paris")) return "France";
        if (dest.contains("rome") || dest.contains("itali")) return "Italy";
        if (dest.contains("londres") || dest.contains("london")) return "United Kingdom";
        if (dest.contains("new york") || dest.contains("miami")) return "United States";
        if (dest.contains("tokyo") || dest.contains("japon")) return "Japan";
        if (dest.contains("caire") || dest.contains("egypte")) return "Egypt";
        if (dest.contains("marrakech") || dest.contains("maroc")) return "Morocco";
        if (dest.contains("tunis")) return "Tunisia";
        if (dest.contains("dubai") || dest.contains("abu dhabi")) return "United Arab Emirates";
        if (dest.contains("bali")) return "Indonesia";
        if (dest.contains("suisse") || dest.contains("zurich") || dest.contains("genève")) return "Switzerland";
        if (dest.contains("espagne") || dest.contains("barcelon") || dest.contains("madrid")) return "Spain";
        if (dest.contains("grèce") || dest.contains("athèn")) return "Greece";
        if (dest.contains("canada") || dest.contains("montréal") || dest.contains("toronto")) return "Canada";
        if (dest.contains("australi") || dest.contains("sydney")) return "Australia";
        if (dest.contains("brésil") || dest.contains("rio")) return "Brazil";
        if (dest.contains("thaïlande") || dest.contains("bangkok")) return "Thailand";
        if (dest.contains("mexique") || dest.contains("cancun")) return "Mexico";
        if (dest.contains("turquie") || dest.contains("istanbul")) return "Turkey";
        if (dest.contains("portugal") || dest.contains("lisbonne")) return "Portugal";
        return destination;
    }

    private HBox buildInfoRow(String icon, String label, String value) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(6, 12, 6, 12));
        row.setStyle("-fx-background-color: #1e2749; -fx-background-radius: 10;");

        Label iconLbl = new Label(icon);
        iconLbl.setStyle("-fx-font-size: 16;");
        iconLbl.setMinWidth(24);

        VBox textBox = new VBox(1);
        Label labelLbl = new Label(label);
        labelLbl.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 10; -fx-font-weight: 600;");
        Label valueLbl = new Label(value != null && !value.isEmpty() ? value : "N/A");
        valueLbl.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 12; -fx-font-weight: 500;");
        valueLbl.setWrapText(true);
        textBox.getChildren().addAll(labelLbl, valueLbl);

        row.getChildren().addAll(iconLbl, textBox);
        return row;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}