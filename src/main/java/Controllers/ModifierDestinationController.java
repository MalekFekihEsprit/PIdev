package Controllers;

import Entities.Destination;
import Services.*;
import Services.CityService.CityCoordinates;
import io.github.cdimascio.dotenv.Dotenv;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

public class ModifierDestinationController implements Initializable {

    @FXML private Label lblDestinationName;
    @FXML private Label lblDestinationId;
    @FXML private Label lblLastModified;
    @FXML private TextField tfNom;
    @FXML private TextField tfPays;
    @FXML private Button btnSearchCity;
    @FXML private ComboBox<CityService.CitySuggestion> cbCitySuggestions;
    @FXML private Label lblCityValidation;
    @FXML private TextArea taDescription;
    @FXML private Button btnGenerateDescription;
    @FXML private ComboBox<String> cbClimat;
    @FXML private ComboBox<String> cbSaison;
    @FXML private Button btnDetectSeason;
    @FXML private TextField tfScore;
    @FXML private Label lblScoreValidation;
    @FXML private TextField tfVideoUrl;
    @FXML private Button btnFetchVideo;
    @FXML private Label lblVideoStatus;
    @FXML private Label lblNomCounter;
    @FXML private Label lblPaysCounter;
    @FXML private Label lblDescCounter;
    @FXML private Button btnUpdate;
    @FXML private Button btnCancel;

    private DestinationCRUD destinationCRUD;
    private DestinationBackController parentController;
    private Destination destinationToEdit;
    private List<Destination> existingDestinations;

    private CityService cityService;
    private CountryCodeService countryCodeService;
    private LocalCityFallbackService fallbackService;
    private SeasonService seasonService;
    private AIService aiService;
    private YouTubeVideoService youTubeVideoService;
    private String currentCountryCode;
    private CityCoordinates validatedCityCoordinates;
    private String fetchedVideoUrl;

    private boolean hasSearched = false;
    private long lastApiCall = 0;
    private static final long MIN_TIME_BETWEEN_CALLS = 1000;

    Dotenv dotenv = Dotenv.load();
    String citiesApiKey = dotenv.get("CITIES_API_KEY");
    String openRouterKey = dotenv.get("OPENROUTER_API_KEY");
    String youtubeApiKey = dotenv.get("YOUTUBE_API_KEY");

    private final String[] climats = {"Méditerranéen", "Tropical", "Continental", "Désertique", "Montagnard", "Océanique", "Polaire"};
    private final String[] saisons = {"Printemps", "Été", "Automne", "Hiver", "Toute l'année"};

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        destinationCRUD = new DestinationCRUD();
        cityService = new CityService(citiesApiKey);
        countryCodeService = new CountryCodeService();
        fallbackService = new LocalCityFallbackService();
        seasonService = new SeasonService();

        // Initialize AI service
        try {
            if (openRouterKey != null && !openRouterKey.trim().isEmpty()) {
                aiService = new AIService(openRouterKey.trim());
                System.out.println("✅ AI Service initialized successfully");
            } else {
                System.err.println("❌ OPENROUTER_API_KEY not found in .env file");
                aiService = null;
            }
        } catch (IllegalArgumentException e) {
            System.err.println("❌ AI Service initialization failed: " + e.getMessage());
            aiService = null;
        }

        // Initialize YouTube service
        try {
            if (youtubeApiKey != null && !youtubeApiKey.trim().isEmpty()) {
                youTubeVideoService = new YouTubeVideoService(youtubeApiKey.trim(), destinationCRUD.getConnection());
                System.out.println("✅ YouTube Service initialized successfully");
            } else {
                System.err.println("❌ YOUTUBE_API_KEY not found in .env file");
                youTubeVideoService = null;
            }
        } catch (Exception e) {
            System.err.println("❌ YouTube Service initialization failed: " + e.getMessage());
            youTubeVideoService = null;
        }

        // Load existing destinations for uniqueness check
        loadExistingDestinations();

        // Initialize combo boxes
        cbClimat.getItems().addAll(climats);
        cbSaison.getItems().addAll(saisons);

        // Set white text for ComboBoxes
        setComboBoxTextWhite(cbClimat);
        setComboBoxTextWhite(cbSaison);

        // Setup all features
        setupCitySuggestions();
        setupSeasonDetection();
        setupAIDescriptionGenerator();
        setupScoreValidation();
        setupVideoFetching();

        setupValidation();
        setupCounters();

        btnUpdate.setOnAction(event -> handleUpdate());
        btnCancel.setOnAction(event -> closeWindow());
        btnSearchCity.setOnAction(event -> handleManualSearch());

        btnUpdate.setDisable(true);

        // Initially hide the suggestions ComboBox
        cbCitySuggestions.setVisible(false);
        cbCitySuggestions.setManaged(false);

        // Disable buttons initially
        btnSearchCity.setDisable(true);
        if (btnDetectSeason != null) btnDetectSeason.setDisable(true);
        if (btnFetchVideo != null) btnFetchVideo.setDisable(true);

        // Disable AI button if service not available
        if (aiService == null && btnGenerateDescription != null) {
            btnGenerateDescription.setDisable(true);
            btnGenerateDescription.setText("🤖 Service indisponible");
        }

        // Disable YouTube button if service not available
        if (youTubeVideoService == null && btnFetchVideo != null) {
            btnFetchVideo.setDisable(true);
            btnFetchVideo.setText("📹 Service indisponible");
        }
    }

    private void setComboBoxTextWhite(ComboBox<String> comboBox) {
        comboBox.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("-fx-background-color: #1e2749;");
                } else {
                    setText(item);
                    setTextFill(Color.WHITE);
                    setStyle("-fx-background-color: #1e2749;");
                }
            }
        });

        comboBox.setButtonCell(new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("-fx-background-color: #1e2749; -fx-text-fill: #94a3b8;");
                } else {
                    setText(item);
                    setTextFill(Color.WHITE);
                    setStyle("-fx-background-color: #1e2749;");
                }
            }
        });
    }

    public void setParentController(DestinationBackController controller) {
        this.parentController = controller;
    }

    public void setDestination(Destination destination) {
        this.destinationToEdit = destination;

        // Display destination name in header
        lblDestinationName.setText(destination.getNom_destination() + ", " + destination.getPays_destination());
        lblDestinationId.setText(String.valueOf(destination.getId_destination()));

        // Pre-fill form
        tfNom.setText(destination.getNom_destination());
        tfPays.setText(destination.getPays_destination());
        taDescription.setText(destination.getDescription_destination());
        cbClimat.setValue(destination.getClimat_destination());
        cbSaison.setValue(destination.getSaison_destination());
        tfScore.setText(destination.getScore_destination() > 0 ? String.valueOf(destination.getScore_destination()) : "");
        tfVideoUrl.setText(destination.getVideo_url());

        // Validate city coordinates if available
        if (destination.getLatitude_destination() != 0.0 || destination.getLongitude_destination() != 0.0) {
            validatedCityCoordinates = new CityCoordinates(
                    destination.getLatitude_destination(),
                    destination.getLongitude_destination()
            );
            if (btnDetectSeason != null) btnDetectSeason.setDisable(false);
            if (btnFetchVideo != null) btnFetchVideo.setDisable(false);
        }

        // Fetch country code for validation
        fetchCountryCode(destination.getPays_destination());

        // Set last modified time
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        lblLastModified.setText("Dernière modification: " + LocalDateTime.now().format(formatter));

        validateForm();
    }

    private void setupCitySuggestions() {
        cbCitySuggestions.setConverter(new StringConverter<CityService.CitySuggestion>() {
            @Override
            public String toString(CityService.CitySuggestion city) {
                return city == null ? "" : city.getName();
            }

            @Override
            public CityService.CitySuggestion fromString(String string) {
                return cbCitySuggestions.getItems().stream()
                        .filter(city -> city.getName().equalsIgnoreCase(string))
                        .findFirst()
                        .orElse(null);
            }
        });

        cbCitySuggestions.setOnAction(event -> {
            CityService.CitySuggestion selected = cbCitySuggestions.getValue();
            if (selected != null) {
                tfNom.setText(selected.getName());
                validateSelectedCity(selected);
                if (btnDetectSeason != null) btnDetectSeason.setDisable(false);
                if (btnFetchVideo != null) btnFetchVideo.setDisable(false);
                cbCitySuggestions.setVisible(false);
                cbCitySuggestions.setManaged(false);
                validateForm();
            }
        });

        tfNom.focusedProperty().addListener((obs, old, newVal) -> {
            if (!newVal) {
                javafx.animation.PauseTransition pause =
                        new javafx.animation.PauseTransition(javafx.util.Duration.millis(200));
                pause.setOnFinished(e -> {
                    cbCitySuggestions.setVisible(false);
                    cbCitySuggestions.setManaged(false);
                });
                pause.play();
            }
        });
    }

    private void setupVideoFetching() {
        if (btnFetchVideo == null) return;

        btnFetchVideo.setOnAction(event -> {
            String city = tfNom.getText().trim();
            String country = tfPays.getText().trim();

            if (city.isEmpty() || country.isEmpty()) {
                showWarning("Veuillez d'abord saisir une ville et un pays");
                return;
            }

            btnFetchVideo.setDisable(true);
            String originalText = btnFetchVideo.getText();
            btnFetchVideo.setText("📹 Recherche...");
            if (lblVideoStatus != null) {
                lblVideoStatus.setText("Recherche d'une vidéo...");
                lblVideoStatus.setStyle("-fx-text-fill: #f59e0b;");
            }

            CompletableFuture<String> future = youTubeVideoService.fetchAndSaveVideo(city, country);

            future.thenAccept(videoUrl -> {
                javafx.application.Platform.runLater(() -> {
                    if (videoUrl != null) {
                        fetchedVideoUrl = videoUrl;
                        tfVideoUrl.setText(videoUrl);
                        if (lblVideoStatus != null) {
                            lblVideoStatus.setText("✓ Vidéo trouvée!");
                            lblVideoStatus.setStyle("-fx-text-fill: #10b981;");
                        }
                    } else {
                        fetchedVideoUrl = null;
                        if (lblVideoStatus != null) {
                            lblVideoStatus.setText("⚠️ Aucune vidéo courte trouvée");
                            lblVideoStatus.setStyle("-fx-text-fill: #ef4444;");
                        }
                    }
                    btnFetchVideo.setText(originalText);
                    btnFetchVideo.setDisable(false);
                });
            }).exceptionally(throwable -> {
                javafx.application.Platform.runLater(() -> {
                    if (lblVideoStatus != null) {
                        lblVideoStatus.setText("⚠️ Erreur: " + throwable.getMessage());
                        lblVideoStatus.setStyle("-fx-text-fill: #ef4444;");
                    }
                    btnFetchVideo.setText(originalText);
                    btnFetchVideo.setDisable(false);
                });
                return null;
            });
        });
    }

    private void setupScoreValidation() {
        tfScore.textProperty().addListener((obs, old, newVal) -> {
            if (newVal == null || newVal.trim().isEmpty()) {
                lblScoreValidation.setText("✓ Vide (0.0 par défaut)");
                lblScoreValidation.setStyle("-fx-text-fill: #10b981;");
                validateForm();
                return;
            }

            try {
                double score = Double.parseDouble(newVal.trim());
                if (score < 0 || score > 10) {
                    lblScoreValidation.setText("⚠️ Doit être entre 0 et 10");
                    lblScoreValidation.setStyle("-fx-text-fill: #ef4444;");
                } else {
                    lblScoreValidation.setText("✓ Score valide");
                    lblScoreValidation.setStyle("-fx-text-fill: #10b981;");
                }
            } catch (NumberFormatException e) {
                lblScoreValidation.setText("⚠️ Entrez un nombre valide");
                lblScoreValidation.setStyle("-fx-text-fill: #ef4444;");
            }
            validateForm();
        });
    }

    private void setupAIDescriptionGenerator() {
        if (btnGenerateDescription == null) return;

        if (aiService == null) {
            btnGenerateDescription.setDisable(true);
            btnGenerateDescription.setText("🤖 Service indisponible");
            return;
        }

        btnGenerateDescription.setOnAction(event -> {
            String city = tfNom.getText().trim();
            String country = tfPays.getText().trim();

            if (city.isEmpty()) {
                showWarning("Veuillez d'abord saisir une ville");
                return;
            }

            if (country.isEmpty()) {
                showWarning("Veuillez d'abord saisir un pays");
                return;
            }

            btnGenerateDescription.setDisable(true);
            String originalText = btnGenerateDescription.getText();
            btnGenerateDescription.setText("🤖 Génération en cours...");

            javafx.concurrent.Task<String> generateTask = new javafx.concurrent.Task<>() {
                @Override
                protected String call() throws Exception {
                    return aiService.generateDescription(city, country);
                }
            };

            generateTask.setOnSucceeded(e -> {
                String content = generateTask.getValue();
                taDescription.setText(content);
                taDescription.setStyle("-fx-text-fill: black; -fx-background-color: white;");
                btnGenerateDescription.setText(originalText);
                btnGenerateDescription.setDisable(false);
                if (lblDescCounter != null) {
                    lblDescCounter.setText(content.length() + " caractères");
                }
                showInfoAlert("Description et itinéraire générés avec succès!");
            });

            generateTask.setOnFailed(e -> {
                Throwable error = generateTask.getException();
                System.err.println("AI Error: " + error.getMessage());
                error.printStackTrace();

                showErrorAlert("Erreur IA", "Impossible de générer le contenu: " + error.getMessage());
                btnGenerateDescription.setText(originalText);
                btnGenerateDescription.setDisable(false);
            });

            new Thread(generateTask).start();
        });
    }

    private void setupSeasonDetection() {
        if (btnDetectSeason == null) return;

        btnDetectSeason.setOnAction(event -> {
            if (validatedCityCoordinates == null) {
                showWarning("Veuillez d'abord sélectionner une ville valide");
                return;
            }

            btnDetectSeason.setDisable(true);
            String originalText = btnDetectSeason.getText();
            btnDetectSeason.setText("🔍");

            javafx.concurrent.Task<String> seasonTask = new javafx.concurrent.Task<>() {
                @Override
                protected String call() throws Exception {
                    return seasonService.getBestSeason(
                            validatedCityCoordinates.getLatitude(),
                            validatedCityCoordinates.getLongitude()
                    );
                }
            };

            seasonTask.setOnSucceeded(e -> {
                String bestSeason = seasonTask.getValue();
                cbSaison.setValue(bestSeason);
                lblCityValidation.setText("✓ Meilleure saison détectée: " + bestSeason);
                lblCityValidation.setStyle("-fx-text-fill: #10b981;");
                btnDetectSeason.setText(originalText);
                btnDetectSeason.setDisable(false);
                validateForm();
            });

            seasonTask.setOnFailed(e -> {
                Throwable error = seasonTask.getException();
                System.err.println("Season detection failed: " + error.getMessage());
                lblCityValidation.setText("⚠️ Utilisez la sélection manuelle");
                lblCityValidation.setStyle("-fx-text-fill: #f59e0b;");
                btnDetectSeason.setText(originalText);
                btnDetectSeason.setDisable(false);
            });

            new Thread(seasonTask).start();
        });
    }

    private void handleManualSearch() {
        String cityPrefix = tfNom.getText().trim();

        if (cityPrefix.length() < 2) {
            lblCityValidation.setText("⚠️ Tapez au moins 2 caractères");
            lblCityValidation.setStyle("-fx-text-fill: #ef4444;");
            return;
        }

        if (currentCountryCode == null) {
            lblCityValidation.setText("⚠️ Sélectionnez d'abord un pays valide");
            lblCityValidation.setStyle("-fx-text-fill: #ef4444;");
            return;
        }

        lblCityValidation.setText("🔍 Recherche en cours...");
        lblCityValidation.setStyle("-fx-text-fill: #f59e0b;");
        btnSearchCity.setDisable(true);

        javafx.concurrent.Task<List<CityService.CitySuggestion>> searchTask = new javafx.concurrent.Task<>() {
            @Override
            protected List<CityService.CitySuggestion> call() throws Exception {
                List<CityService.CitySuggestion> allSuggestions = new ArrayList<>();

                try {
                    allSuggestions.addAll(cityService.suggestCitiesIncludeDeleted(
                            currentCountryCode, cityPrefix, 10));
                } catch (Exception e) {
                    System.err.println("API search failed: " + e.getMessage());
                }

                if (allSuggestions.isEmpty()) {
                    List<LocalCityFallbackService.CitySuggestion> fallbackResults =
                            fallbackService.getSuggestions(currentCountryCode, cityPrefix);

                    for (LocalCityFallbackService.CitySuggestion fb : fallbackResults) {
                        allSuggestions.add(new CityService.CitySuggestion(
                                fb.getName(), "", currentCountryCode,
                                fb.getLatitude(), fb.getLongitude(), 0
                        ));
                    }

                    if (!fallbackResults.isEmpty()) {
                        System.out.println("Using fallback data for: " + cityPrefix);
                    }
                }

                return allSuggestions;
            }
        };

        searchTask.setOnSucceeded(event -> {
            List<CityService.CitySuggestion> suggestions = searchTask.getValue();

            if (!suggestions.isEmpty()) {
                ObservableList<CityService.CitySuggestion> items = FXCollections.observableArrayList(suggestions);
                cbCitySuggestions.setItems(items);
                cbCitySuggestions.setVisible(true);
                cbCitySuggestions.setManaged(true);
                cbCitySuggestions.show();

                hasSearched = true;
                lblCityValidation.setText(suggestions.size() + " suggestion(s) trouvée(s)");
                lblCityValidation.setStyle("-fx-text-fill: #10b981;");
            } else {
                cbCitySuggestions.setVisible(false);
                cbCitySuggestions.setManaged(false);
                lblCityValidation.setText("⚠️ Aucune ville trouvée pour: " + cityPrefix);
                lblCityValidation.setStyle("-fx-text-fill: #ef4444;");
            }

            btnSearchCity.setDisable(false);
        });

        searchTask.setOnFailed(event -> {
            lblCityValidation.setText("⚠️ Erreur de recherche");
            lblCityValidation.setStyle("-fx-text-fill: #ef4444;");
            btnSearchCity.setDisable(false);
        });

        new Thread(searchTask).start();
    }

    private void validateSelectedCity(CityService.CitySuggestion city) {
        if (city != null) {
            validatedCityCoordinates = new CityCoordinates(city.getLatitude(), city.getLongitude());
            lblCityValidation.setText("✓ Ville valide: " + city.getName());
            lblCityValidation.setStyle("-fx-text-fill: #10b981;");
            if (btnDetectSeason != null) btnDetectSeason.setDisable(false);
            if (btnFetchVideo != null) btnFetchVideo.setDisable(false);
        }
    }

    private void loadExistingDestinations() {
        try {
            existingDestinations = destinationCRUD.afficher();
        } catch (SQLException e) {
            existingDestinations = List.of();
            System.err.println("Could not load existing destinations: " + e.getMessage());
        }
    }

    private void setupValidation() {
        tfNom.textProperty().addListener((obs, old, newVal) -> {
            validateForm();
            btnSearchCity.setDisable(newVal.length() < 2 || currentCountryCode == null);
            if (!newVal.equals(old) && validatedCityCoordinates != null) {
                validatedCityCoordinates = null;
                lblCityValidation.setText("");
                if (btnDetectSeason != null) btnDetectSeason.setDisable(true);
                if (btnFetchVideo != null) btnFetchVideo.setDisable(true);
            }
        });

        tfPays.textProperty().addListener((obs, old, newVal) -> {
            if (newVal != null && newVal.length() >= 2) {
                fetchCountryCode(newVal);
            } else {
                currentCountryCode = null;
                lblCityValidation.setText("");
                if (btnDetectSeason != null) btnDetectSeason.setDisable(true);
                if (btnFetchVideo != null) btnFetchVideo.setDisable(true);
            }
            btnSearchCity.setDisable(tfNom.getText().length() < 2 || currentCountryCode == null);
            validateForm();
        });

        cbClimat.valueProperty().addListener((obs, old, newVal) -> validateForm());
        cbSaison.valueProperty().addListener((obs, old, newVal) -> validateForm());
        taDescription.textProperty().addListener((obs, old, newVal) -> validateForm());
        cbCitySuggestions.valueProperty().addListener((obs, old, newVal) -> validateForm());
        tfVideoUrl.textProperty().addListener((obs, old, newVal) -> validateForm());
    }

    private void fetchCountryCode(String countryName) {
        try {
            currentCountryCode = countryCodeService.getCountryCode(countryName);
            if (currentCountryCode == null) {
                lblCityValidation.setText("⚠️ Pays non reconnu: " + countryName);
                lblCityValidation.setStyle("-fx-text-fill: #ef4444;");
            } else {
                lblCityValidation.setText("✓ Pays valide: " + countryName);
                lblCityValidation.setStyle("-fx-text-fill: #10b981;");
            }
        } catch (Exception e) {
            currentCountryCode = null;
            lblCityValidation.setText("⚠️ Erreur de validation du pays");
            lblCityValidation.setStyle("-fx-text-fill: #ef4444;");
        }
    }

    private void setupCounters() {
        if (lblNomCounter != null) {
            tfNom.textProperty().addListener((obs, old, newVal) ->
                    lblNomCounter.setText(newVal.length() + "/30"));
        }

        if (lblPaysCounter != null) {
            tfPays.textProperty().addListener((obs, old, newVal) ->
                    lblPaysCounter.setText(newVal.length() + "/30"));
        }

        if (lblDescCounter != null) {
            taDescription.textProperty().addListener((obs, old, newVal) ->
                    lblDescCounter.setText(newVal.length() + " caractères"));
        }
    }

    private void validateForm() {
        String nom = tfNom.getText().trim();
        String pays = tfPays.getText().trim();

        // Check if this city+country combination already exists (excluding current destination)
        boolean isDuplicate = false;
        if (existingDestinations != null && !nom.isEmpty() && !pays.isEmpty()) {
            isDuplicate = existingDestinations.stream()
                    .anyMatch(d -> d.getId_destination() != destinationToEdit.getId_destination() &&
                            d.getNom_destination().equalsIgnoreCase(nom) &&
                            d.getPays_destination().equalsIgnoreCase(pays));
        }

        boolean scoreValid = true;
        String scoreText = tfScore.getText().trim();
        if (!scoreText.isEmpty()) {
            try {
                double score = Double.parseDouble(scoreText);
                if (score < 0 || score > 10) scoreValid = false;
            } catch (NumberFormatException e) {
                scoreValid = false;
            }
        }

        boolean isValid = !nom.isEmpty() && nom.length() <= 30 &&
                !pays.isEmpty() && pays.length() <= 30 &&
                currentCountryCode != null &&
                cbClimat.getValue() != null &&
                cbSaison.getValue() != null &&
                !isDuplicate &&
                scoreValid;

        btnUpdate.setDisable(!isValid);
    }

    private void handleUpdate() {
        if (!validateAllFields()) return;

        String nom = tfNom.getText().trim();
        String pays = tfPays.getText().trim();

        // Check for duplicates (excluding current destination)
        if (existingDestinations != null) {
            boolean exists = existingDestinations.stream()
                    .anyMatch(d -> d.getId_destination() != destinationToEdit.getId_destination() &&
                            d.getNom_destination().equalsIgnoreCase(nom) &&
                            d.getPays_destination().equalsIgnoreCase(pays));

            if (exists) {
                showWarning("Une autre destination avec ce nom et ce pays existe déjà!");
                return;
            }
        }

        double score = 0.0;
        String scoreText = tfScore.getText().trim();
        if (!scoreText.isEmpty()) {
            try {
                score = Double.parseDouble(scoreText);
            } catch (NumberFormatException e) {
                score = 0.0;
            }
        }

        double latitude = 0.0, longitude = 0.0;
        if (validatedCityCoordinates != null) {
            latitude = validatedCityCoordinates.getLatitude();
            longitude = validatedCityCoordinates.getLongitude();
        }

        String videoUrl = tfVideoUrl.getText().trim();
        if (videoUrl.isEmpty()) {
            videoUrl = null;
        }

        destinationToEdit.setNom_destination(nom);
        destinationToEdit.setPays_destination(pays);
        destinationToEdit.setDescription_destination(taDescription.getText().trim());
        destinationToEdit.setClimat_destination(cbClimat.getValue());
        destinationToEdit.setSaison_destination(cbSaison.getValue());
        destinationToEdit.setLatitude_destination(latitude);
        destinationToEdit.setLongitude_destination(longitude);
        destinationToEdit.setScore_destination(score);
        destinationToEdit.setVideo_url(videoUrl);

        try {
            destinationCRUD.modifier(destinationToEdit);

            String videoMsg = (videoUrl != null && !videoUrl.isEmpty()) ? "\n✓ Vidéo mise à jour" : "";
            showSuccessAlert("Destination modifiée avec succès!\n" + nom + ", " + pays + videoMsg);

            if (parentController != null) parentController.refreshAfterModification();
            closeWindow();

        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate") || e.getMessage().contains("duplicate")) {
                showWarning("Cette destination existe déjà dans la base de données!");
            } else {
                showErrorAlert("Erreur lors de la modification", e.getMessage());
            }
        }
    }

    private boolean validateAllFields() {
        String nom = tfNom.getText().trim();
        String pays = tfPays.getText().trim();

        if (nom.length() < 2) {
            showWarning("Le nom de la ville doit contenir au moins 2 caractères");
            return false;
        }
        if (pays.length() < 2) {
            showWarning("Le pays doit contenir au moins 2 caractères");
            return false;
        }
        if (currentCountryCode == null) {
            showWarning("Pays non valide. Vérifiez le nom du pays.");
            return false;
        }

        String scoreText = tfScore.getText().trim();
        if (!scoreText.isEmpty()) {
            try {
                double score = Double.parseDouble(scoreText);
                if (score < 0 || score > 10) {
                    showWarning("Le score doit être entre 0 et 10");
                    return false;
                }
            } catch (NumberFormatException e) {
                showWarning("Le score doit être un nombre valide");
                return false;
            }
        }

        return true;
    }

    private void showInfoAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Validation");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccessAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void closeWindow() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }
}