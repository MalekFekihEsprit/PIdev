package controllers;

import entities.Destination;
import entities.HotelSuggestion;
import entities.Hebergement;
import javafx.scene.layout.GridPane;
import services.HotelSuggestionService;
import services.HebergementCRUD;
import io.github.cdimascio.dotenv.Dotenv;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class HotelSuggestionController implements Initializable {

    @FXML private Label lblDestinationInfo;
    @FXML private TextField tfRadius;
    @FXML private TextField tfLimit;
    @FXML private Label lblRadiusValidation;
    @FXML private Label lblLimitValidation;
    @FXML private Label lblResultsCount;
    @FXML private VBox hotelListContainer;
    @FXML private VBox noResultsContainer;
    @FXML private Button btnSearch;
    @FXML private Button btnAcceptSelected;
    @FXML private Button btnCancel;

    private Destination destination;
    private HotelSuggestionService hotelService;
    private HebergementCRUD hebergementCRUD;
    private List<HotelSuggestion> currentSuggestions = new ArrayList<>();
    private DestinationBackController parentController;

    Dotenv dotenv = Dotenv.load();
    String geoapifyKey = dotenv.get("GEOAPIFY_API_KEY");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        hotelService = new HotelSuggestionService(geoapifyKey);
        hebergementCRUD = new HebergementCRUD();

        setupValidation();
        setupButtons();
    }

    public void setDestination(Destination destination) {
        this.destination = destination;
        lblDestinationInfo.setText("Hôtels pour " + destination.getNom_destination() + ", " + destination.getPays_destination());
    }

    public void setParentController(DestinationBackController controller) {
        this.parentController = controller;
    }

    private void setupValidation() {
        // Radius validation
        tfRadius.textProperty().addListener((obs, old, newVal) -> {
            validateRadius();
        });

        // Limit validation
        tfLimit.textProperty().addListener((obs, old, newVal) -> {
            validateLimit();
        });
    }

    private boolean validateRadius() {
        String text = tfRadius.getText().trim();
        if (text.isEmpty()) {
            lblRadiusValidation.setText("⚠️ Rayon requis");
            lblRadiusValidation.setStyle("-fx-text-fill: #ef4444;");
            return false;
        }

        try {
            int radius = Integer.parseInt(text);
            if (radius < 100) {
                lblRadiusValidation.setText("⚠️ Minimum 100 mètres");
                lblRadiusValidation.setStyle("-fx-text-fill: #ef4444;");
                return false;
            } else if (radius > 50000) {
                lblRadiusValidation.setText("⚠️ Maximum 50000 mètres");
                lblRadiusValidation.setStyle("-fx-text-fill: #ef4444;");
                return false;
            } else {
                lblRadiusValidation.setText("✓ Valide");
                lblRadiusValidation.setStyle("-fx-text-fill: #10b981;");
                return true;
            }
        } catch (NumberFormatException e) {
            lblRadiusValidation.setText("⚠️ Entrez un nombre valide");
            lblRadiusValidation.setStyle("-fx-text-fill: #ef4444;");
            return false;
        }
    }

    private boolean validateLimit() {
        String text = tfLimit.getText().trim();
        if (text.isEmpty()) {
            lblLimitValidation.setText("⚠️ Limite requise");
            lblLimitValidation.setStyle("-fx-text-fill: #ef4444;");
            return false;
        }

        try {
            int limit = Integer.parseInt(text);
            if (limit < 1) {
                lblLimitValidation.setText("⚠️ Minimum 1");
                lblLimitValidation.setStyle("-fx-text-fill: #ef4444;");
                return false;
            } else if (limit > 50) {
                lblLimitValidation.setText("⚠️ Maximum 50");
                lblLimitValidation.setStyle("-fx-text-fill: #ef4444;");
                return false;
            } else {
                lblLimitValidation.setText("✓ Valide");
                lblLimitValidation.setStyle("-fx-text-fill: #10b981;");
                return true;
            }
        } catch (NumberFormatException e) {
            lblLimitValidation.setText("⚠️ Entrez un nombre valide");
            lblLimitValidation.setStyle("-fx-text-fill: #ef4444;");
            return false;
        }
    }

    private void setupButtons() {
        btnSearch.setOnAction(event -> handleSearch());
        btnAcceptSelected.setOnAction(event -> handleAcceptSelected());
        btnCancel.setOnAction(event -> closeWindow());
    }

    private void handleSearch() {
        if (!validateRadius() || !validateLimit()) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Veuillez corriger les erreurs de saisie");
            return;
        }

        int radius = Integer.parseInt(tfRadius.getText().trim());
        int limit = Integer.parseInt(tfLimit.getText().trim());

        btnSearch.setDisable(true);
        btnSearch.setText("🔍 Recherche en cours...");

        javafx.concurrent.Task<List<HotelSuggestion>> searchTask = new javafx.concurrent.Task<>() {
            @Override
            protected List<HotelSuggestion> call() throws Exception {
                return hotelService.getHotelSuggestions(
                        destination.getLatitude_destination(),
                        destination.getLongitude_destination(),
                        radius,
                        limit
                );
            }
        };

        searchTask.setOnSucceeded(event -> {
            currentSuggestions = searchTask.getValue();
            displayResults(currentSuggestions);
            btnSearch.setDisable(false);
            btnSearch.setText("🔍 Rechercher des hôtels");
        });

        searchTask.setOnFailed(event -> {
            Throwable error = searchTask.getException();
            showAlert(Alert.AlertType.ERROR, "Erreur API",
                    "Impossible de récupérer les hôtels: " + error.getMessage());
            btnSearch.setDisable(false);
            btnSearch.setText("🔍 Rechercher des hôtels");
        });

        new Thread(searchTask).start();
    }

    private void displayResults(List<HotelSuggestion> suggestions) {
        hotelListContainer.getChildren().clear();

        if (suggestions.isEmpty()) {
            noResultsContainer.setManaged(true);
            noResultsContainer.setVisible(true);
            lblResultsCount.setText("0 hôtels");
            return;
        }

        noResultsContainer.setManaged(false);
        noResultsContainer.setVisible(false);
        lblResultsCount.setText(suggestions.size() + " hôtels trouvés");

        for (int i = 0; i < suggestions.size(); i++) {
            HotelSuggestion hotel = suggestions.get(i);
            VBox hotelCard = createHotelCard(hotel, i);
            hotelListContainer.getChildren().add(hotelCard);
        }
    }

    private VBox createHotelCard(HotelSuggestion hotel, int index) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: #1e2749; -fx-background-radius: 16; -fx-padding: 16; -fx-border-color: #2d3a5f; -fx-border-width: 1; -fx-border-radius: 16;");

        // Header with checkbox and name
        HBox header = new HBox(10);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        CheckBox checkBox = new CheckBox();
        checkBox.setStyle("-fx-text-fill: white;");

        Label nameLabel = new Label((index + 1) + ". " + hotel.getNom());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14; -fx-text-fill: white;");

        header.getChildren().addAll(checkBox, nameLabel);

        // Details grid
        GridPane details = new GridPane();
        details.setHgap(10);
        details.setVgap(5);
        details.setPadding(new Insets(5, 0, 0, 25));

        // Address
        Label addressIcon = new Label("📍");
        addressIcon.setStyle("-fx-font-size: 14;");
        Label addressLabel = new Label(hotel.getAdresse());
        addressLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12;");
        addressLabel.setWrapText(true);
        details.add(addressIcon, 0, 0);
        details.add(addressLabel, 1, 0);

        // Rating
        Label ratingIcon = new Label("⭐");
        ratingIcon.setStyle("-fx-font-size: 14;");
        Label ratingLabel;
        if (hotel.getRating() != null) {
            ratingLabel = new Label(String.format("%.1f / 5", hotel.getRating()));
            ratingLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12;");
        } else {
            ratingLabel = new Label("No available rating for the moment");
            ratingLabel.setStyle("-fx-text-fill: #f59e0b; -fx-font-size: 12; -fx-font-style: italic;");
        }
        details.add(ratingIcon, 0, 1);
        details.add(ratingLabel, 1, 1);

        // Price
        Label priceIcon = new Label("💰");
        priceIcon.setStyle("-fx-font-size: 14;");
        Label priceLabel;
        if (hotel.getPrixEstime() != null) {
            priceLabel = new Label(String.format("%.2f TND / nuit (estimé)", hotel.getPrixEstime()));
            priceLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12;");
        } else {
            priceLabel = new Label("No available price for the moment");
            priceLabel.setStyle("-fx-text-fill: #f59e0b; -fx-font-size: 12; -fx-font-style: italic;");
        }
        details.add(priceIcon, 0, 2);
        details.add(priceLabel, 1, 2);

        // Checkbox action
        checkBox.selectedProperty().addListener((obs, old, newVal) -> {
            hotel.setAccepted(newVal);
        });

        card.getChildren().addAll(header, details);
        return card;
    }

    private void handleAcceptSelected() {
        List<HotelSuggestion> accepted = new ArrayList<>();
        for (HotelSuggestion hotel : currentSuggestions) {
            if (hotel.isAccepted()) {
                accepted.add(hotel);
            }
        }

        if (accepted.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Attention",
                    "Aucun hôtel sélectionné. Veuillez cocher les hôtels à ajouter.");
            return;
        }

        // Confirm insertion
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Ajouter " + accepted.size() + " hôtel(s)");
        confirm.setContentText("Voulez-vous ajouter ces hôtels à la base de données ?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            int successCount = 0;
            int failCount = 0;

            // Insert each accepted hotel using HebergementCRUD
            for (HotelSuggestion hotel : accepted) {
                try {
                    // Create Hebergement object from HotelSuggestion
                    Hebergement hebergement = new Hebergement();
                    hebergement.setNom_hebergement(hotel.getNom());
                    hebergement.setType_hebergement("Hôtel");

                    // Handle nullable price
                    if (hotel.getPrixEstime() != null) {
                        hebergement.setPrixNuit_hebergement(hotel.getPrixEstime());
                    } else {
                        hebergement.setPrixNuit_hebergement(0.0); // Default value
                    }

                    hebergement.setAdresse_hebergement(hotel.getAdresse());

                    // Handle nullable rating
                    if (hotel.getRating() != null) {
                        hebergement.setNote_hebergement(hotel.getRating());
                    } else {
                        hebergement.setNote_hebergement(0.0); // Default value
                    }

                    hebergement.setLatitude_hebergement(hotel.getLatitude());
                    hebergement.setLongitude_hebergement(hotel.getLongitude());
                    hebergement.setScore_hebergement(0.0);
                    hebergement.setDestination(destination);

                    hebergementCRUD.ajouter(hebergement);
                    successCount++;

                } catch (SQLException e) {
                    failCount++;
                    System.err.println("Error inserting hotel: " + hotel.getNom() + " - " + e.getMessage());
                    e.printStackTrace();
                }
            }

            if (successCount > 0) {
                showSuccessAndNavigate(successCount, failCount);
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur",
                        "Aucun hôtel n'a pu être ajouté. Vérifiez les logs pour plus de détails.");
            }
        }
    }

    private void showSuccessAndNavigate(int successCount, int failCount) {
        String message = successCount + " hôtel(s) ajouté(s) avec succès !";
        if (failCount > 0) {
            message += "\n" + failCount + " échec(s).";
        }

        Alert success = new Alert(Alert.AlertType.INFORMATION);
        success.setTitle("Succès");
        success.setHeaderText(message);
        success.setContentText("Voulez-vous voir les hébergements pour cette destination ?");

        ButtonType voirButton = new ButtonType("Voir les hébergements");
        ButtonType plusTardButton = new ButtonType("Plus tard", ButtonBar.ButtonData.CANCEL_CLOSE);

        success.getButtonTypes().setAll(voirButton, plusTardButton);

        Optional<ButtonType> result = success.showAndWait();
        if (result.isPresent() && result.get() == voirButton) {
            navigateToHebergementFront();
        } else {
            closeWindow();
            if (parentController != null) {
                parentController.refreshAfterModification();
            }
        }
    }

    private void navigateToHebergementFront() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/HebergementFront.fxml"));
            Parent root = loader.load();

            // You'll need to implement this controller
            // HebergementFrontController controller = loader.getController();
            // controller.filterByDestination(destination);

            Stage stage = (Stage) btnCancel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TravelMate - Hébergements à " + destination.getNom_destination());
            stage.setMaximized(true);

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible d'ouvrir les hébergements: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void closeWindow() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }
}