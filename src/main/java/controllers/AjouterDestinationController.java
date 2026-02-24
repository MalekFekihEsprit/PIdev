package controllers;

import entities.Destination;
import services.*;
import services.CityService.CityCoordinates;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.ArrayList;

import io.github.cdimascio.dotenv.Dotenv;

public class AjouterDestinationController implements Initializable {

    @FXML private TextField tfNom; // City name (user types here)
    @FXML private TextField tfPays; // Country name
    @FXML private Button btnSearchCity; // Manual search button
    @FXML private ComboBox<CityService.CitySuggestion> cbCitySuggestions; // Hidden dropdown for suggestions
    @FXML private Label lblCityValidation; // Validation message for city
    @FXML private TextArea taDescription;
    @FXML private Button btnGenerateDescription; // AI Generate button
    @FXML private ComboBox<String> cbClimat;
    @FXML private ComboBox<String> cbSaison;
    @FXML private Button btnDetectSeason; // Season detection button
    @FXML private TextField tfScore; // Optional score input
    @FXML private Label lblScoreValidation; // Score validation message
    @FXML private Label lblNomCounter;
    @FXML private Label lblPaysCounter;
    @FXML private Label lblDescCounter;
    @FXML private Label lblUniquenessWarning;
    @FXML private Button btnSave;
    @FXML private Button btnCancel;

    private DestinationCRUD destinationCRUD;
    private DestinationBackController parentController;
    private List<Destination> existingDestinations;

    private CityService cityService;
    private CountryCodeService countryCodeService;
    private LocalCityFallbackService fallbackService;
    private SeasonService seasonService;
    private AIService aiService;
    private String currentCountryCode;
    private CityCoordinates validatedCityCoordinates;

    private boolean hasSearched = false;
    private long lastApiCall = 0;
    private static final long MIN_TIME_BETWEEN_CALLS = 1000;

    Dotenv dotenv = Dotenv.load();
    String citiesApiKey = dotenv.get("CITIES_API_KEY");
    String openRouterKey = dotenv.get("OPENROUTER_API_KEY");

    private final String[] climats = {"Méditerranéen", "Tropical", "Continental", "Désertique", "Montagnard", "Océanique", "Polaire"};
    private final String[] saisons = {"Printemps", "Été", "Automne", "Hiver", "Toute l'année"};

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        destinationCRUD = new DestinationCRUD();
        cityService = new CityService(citiesApiKey);
        countryCodeService = new CountryCodeService();
        fallbackService = new LocalCityFallbackService();
        seasonService = new SeasonService();

        // Initialize AI service with key from .env
        try {
            if (openRouterKey != null && !openRouterKey.isEmpty()) {
                aiService = new AIService(openRouterKey);
                System.out.println("AI Service initialized successfully");
            } else {
                System.err.println("OPENROUTER_API_KEY not found in .env file");
                aiService = null;
            }
        } catch (IllegalArgumentException e) {
            System.err.println("AI Service initialization failed: " + e.getMessage());
            aiService = null;
        }

        // Load existing destinations for uniqueness check
        loadExistingDestinations();

        // Initialize combo boxes
        cbClimat.getItems().addAll(climats);
        cbSaison.getItems().addAll(saisons);

        // Set white text for ComboBoxes
        setComboBoxTextWhite(cbClimat);
        setComboBoxTextWhite(cbSaison);

        // Setup city suggestions
        setupCitySuggestions();
        setupSeasonDetection();
        setupAIDescriptionGenerator();
        setupScoreValidation();

        setupValidation();
        setupCounters();

        btnSave.setOnAction(event -> handleSave());
        btnCancel.setOnAction(event -> closeWindow());
        btnSearchCity.setOnAction(event -> handleManualSearch());

        btnSave.setDisable(true);
        if (lblUniquenessWarning != null) {
            lblUniquenessWarning.setVisible(false);
        }

        // Initially hide the suggestions ComboBox
        cbCitySuggestions.setVisible(false);
        cbCitySuggestions.setManaged(false);

        // Disable search button initially
        btnSearchCity.setDisable(true);

        // Disable season detection button initially
        if (btnDetectSeason != null) {
            btnDetectSeason.setDisable(true);
        }

        // Disable AI button if service not available
        if (aiService == null && btnGenerateDescription != null) {
            btnGenerateDescription.setDisable(true);
            btnGenerateDescription.setText("🤖 Clé API manquante");
        }
    }

    /**
     * Forces white text in ComboBox dropdown and selected value
     */
    private void setComboBoxTextWhite(ComboBox<String> comboBox) {
        // Set cell factory for dropdown items
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

        // Set button cell for selected value
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

    private void setupCitySuggestions() {
        // Configure how to display city names in the combobox - just the name
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

        // When user selects a suggestion from dropdown
        cbCitySuggestions.setOnAction(event -> {
            CityService.CitySuggestion selected = cbCitySuggestions.getValue();
            if (selected != null) {
                // Fill the text field with selected city
                tfNom.setText(selected.getName());
                // Validate and store coordinates
                validateSelectedCity(selected);
                // Enable season detection button
                if (btnDetectSeason != null) {
                    btnDetectSeason.setDisable(false);
                }
                // Hide suggestions
                cbCitySuggestions.setVisible(false);
                cbCitySuggestions.setManaged(false);
                validateForm();
            }
        });

        // Hide suggestions when focus is lost
        tfNom.focusedProperty().addListener((obs, old, newVal) -> {
            if (!newVal) { // Focus lost
                // Small delay to allow selection to register
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

        // Disable if AI service not available
        if (aiService == null) {
            btnGenerateDescription.setDisable(true);
            btnGenerateDescription.setText("🤖 Service indisponible");
            return;
        }

        btnGenerateDescription.setOnAction(event -> {
            String city = tfNom.getText().trim();
            String country = tfPays.getText().trim();

            if (city.isEmpty()) {
                showValidationAlert("Veuillez d'abord saisir une ville");
                return;
            }

            if (country.isEmpty()) {
                showValidationAlert("Veuillez d'abord saisir un pays");
                return;
            }

            // Disable button during generation
            btnGenerateDescription.setDisable(true);
            String originalText = btnGenerateDescription.getText();
            btnGenerateDescription.setText("🤖 Génération...");

            // Run in background thread
            javafx.concurrent.Task<String> generateTask = new javafx.concurrent.Task<>() {
                @Override
                protected String call() throws Exception {
                    return aiService.generateDescription(city, country);
                }
            };

            generateTask.setOnSucceeded(e -> {
                String description = generateTask.getValue();
                taDescription.setText(description);
                // Force text color to be visible
                taDescription.setStyle("-fx-text-fill: black; -fx-background-color: white;");
                btnGenerateDescription.setText(originalText);
                btnGenerateDescription.setDisable(false);

                if (lblDescCounter != null) {
                    lblDescCounter.setText(description.length() + "/1000");
                }
            });

            generateTask.setOnFailed(e -> {
                Throwable error = generateTask.getException();
                if (error instanceof AIService.AIException) {
                    showErrorAlert("Erreur IA", error.getMessage());
                } else {
                    showErrorAlert("Erreur", "Impossible de générer la description: " + error.getMessage());
                }
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
                showValidationAlert("Veuillez d'abord sélectionner une ville valide");
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

        // Show loading indicator
        lblCityValidation.setText("🔍 Recherche en cours...");
        lblCityValidation.setStyle("-fx-text-fill: #f59e0b;");
        btnSearchCity.setDisable(true);

        // Perform search in background
        javafx.concurrent.Task<List<CityService.CitySuggestion>> searchTask = new javafx.concurrent.Task<>() {
            @Override
            protected List<CityService.CitySuggestion> call() throws Exception {
                List<CityService.CitySuggestion> allSuggestions = new ArrayList<>();

                // Strategy 1: Try API first
                try {
                    allSuggestions.addAll(cityService.suggestCitiesIncludeDeleted(
                            currentCountryCode, cityPrefix, 10));
                } catch (Exception e) {
                    System.err.println("API search failed: " + e.getMessage());
                }

                // Strategy 2: If API returns nothing, use fallback JSON
                if (allSuggestions.isEmpty()) {
                    List<LocalCityFallbackService.CitySuggestion> fallbackResults =
                            fallbackService.getSuggestions(currentCountryCode, cityPrefix);

                    // Convert fallback suggestions to CityService.CitySuggestion type
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
            // Enable season detection button
            if (btnDetectSeason != null) {
                btnDetectSeason.setDisable(false);
            }
        }
    }

    private void validateCurrentCityInput() {
        String cityName = tfNom.getText().trim();

        if (cityName.isEmpty() || currentCountryCode == null) {
            return;
        }

        // Try API first
        CityCoordinates coords = cityService.validateCity(currentCountryCode, cityName);
        String source = "API";

        // If API fails, try fallback JSON
        if (coords == null) {
            LocalCityFallbackService.CityCoordinates fbCoords =
                    fallbackService.validateCity(currentCountryCode, cityName);

            if (fbCoords != null) {
                coords = new CityCoordinates(fbCoords.getLatitude(), fbCoords.getLongitude());
                source = "base locale";
            }
        }

        if (coords != null) {
            validatedCityCoordinates = coords;
            if (source.equals("API")) {
                lblCityValidation.setText("✓ Ville valide: " + cityName);
            } else {
                lblCityValidation.setText("✓ Ville valide (base locale): " + cityName);
            }
            lblCityValidation.setStyle("-fx-text-fill: #10b981;");
            // Enable season detection button
            if (btnDetectSeason != null) {
                btnDetectSeason.setDisable(false);
            }
        } else {
            validatedCityCoordinates = null;
            lblCityValidation.setText("⚠️ Ville non trouvée: " + cityName);
            lblCityValidation.setStyle("-fx-text-fill: #ef4444;");
            // Disable season detection button
            if (btnDetectSeason != null) {
                btnDetectSeason.setDisable(true);
            }
        }

        validateForm();
    }

    private void loadExistingDestinations() {
        try {
            existingDestinations = destinationCRUD.afficher();
        } catch (SQLException e) {
            existingDestinations = List.of();
            System.err.println("Could not load existing destinations: " + e.getMessage());
        }
    }

    public void setParentController(DestinationBackController controller) {
        this.parentController = controller;
    }

    private void setupValidation() {
        // Add listeners to all fields to validate form and check uniqueness
        tfNom.textProperty().addListener((obs, old, newVal) -> {
            validateForm();
            checkUniqueness();
            // Enable search button if we have text and valid country
            btnSearchCity.setDisable(newVal.length() < 2 || currentCountryCode == null);
            // Clear validation when typing new text
            if (!newVal.equals(old) && validatedCityCoordinates != null) {
                validatedCityCoordinates = null;
                lblCityValidation.setText("");
                // Disable season detection button
                if (btnDetectSeason != null) {
                    btnDetectSeason.setDisable(true);
                }
            }
        });

        tfPays.textProperty().addListener((obs, old, newVal) -> {
            // When country changes, fetch country code
            if (newVal != null && newVal.length() >= 2) {
                fetchCountryCode(newVal);
            } else {
                currentCountryCode = null;
                lblCityValidation.setText("");
                // Disable season detection button
                if (btnDetectSeason != null) {
                    btnDetectSeason.setDisable(true);
                }
            }
            // Update search button state
            btnSearchCity.setDisable(tfNom.getText().length() < 2 || currentCountryCode == null);
            validateForm();
            checkUniqueness();
        });

        cbClimat.valueProperty().addListener((obs, old, newVal) -> validateForm());
        cbSaison.valueProperty().addListener((obs, old, newVal) -> validateForm());
        taDescription.textProperty().addListener((obs, old, newVal) -> validateForm());

        // Add listener for city validation
        cbCitySuggestions.valueProperty().addListener((obs, old, newVal) -> validateForm());
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
            System.err.println("Error fetching country code: " + e.getMessage());
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
                    lblDescCounter.setText(newVal.length() + "/1000"));
        }
    }

    private void checkUniqueness() {
        String nom = tfNom.getText().trim();
        String pays = tfPays.getText().trim();

        if (nom.isEmpty() || pays.isEmpty() || existingDestinations == null) {
            if (lblUniquenessWarning != null) {
                lblUniquenessWarning.setVisible(false);
            }
            return;
        }

        boolean exists = existingDestinations.stream()
                .anyMatch(d -> d.getNom_destination().equalsIgnoreCase(nom)
                        && d.getPays_destination().equalsIgnoreCase(pays));

        if (exists) {
            lblUniquenessWarning.setText("⚠️ Une destination avec ce nom et ce pays existe déjà!");
            lblUniquenessWarning.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11; -fx-font-weight: 600;");
            lblUniquenessWarning.setVisible(true);
        } else {
            lblUniquenessWarning.setVisible(false);
        }

        validateForm();
    }

    private void validateForm() {
        String nom = tfNom.getText().trim();
        String pays = tfPays.getText().trim();

        boolean isDuplicate = existingDestinations != null &&
                !nom.isEmpty() && !pays.isEmpty() &&
                existingDestinations.stream()
                        .anyMatch(d -> d.getNom_destination().equalsIgnoreCase(nom)
                                && d.getPays_destination().equalsIgnoreCase(pays));

        // Validate score if not empty
        boolean scoreValid = true;
        String scoreText = tfScore.getText().trim();
        if (!scoreText.isEmpty()) {
            try {
                double score = Double.parseDouble(scoreText);
                if (score < 0 || score > 10) {
                    scoreValid = false;
                }
            } catch (NumberFormatException e) {
                scoreValid = false;
            }
        }

        // Country must be valid, city validation is optional (coordinates default to 0,0)
        boolean isValid = !nom.isEmpty() && nom.length() <= 30 &&
                !pays.isEmpty() && pays.length() <= 30 &&
                currentCountryCode != null && // Country must be valid
                cbClimat.getValue() != null &&
                cbSaison.getValue() != null &&
                taDescription.getText().length() <= 1000 &&
                !isDuplicate &&
                scoreValid;

        btnSave.setDisable(!isValid);
    }

    private void handleSave() {
        if (!validateAllFields()) return;

        String nom = tfNom.getText().trim();
        String pays = tfPays.getText().trim();

        // Double-check uniqueness before saving
        if (existingDestinations != null) {
            boolean exists = existingDestinations.stream()
                    .anyMatch(d -> d.getNom_destination().equalsIgnoreCase(nom)
                            && d.getPays_destination().equalsIgnoreCase(pays));

            if (exists) {
                showValidationAlert("Une destination avec ce nom et ce pays existe déjà!");
                return;
            }
        }

        // Parse score (optional)
        double score = 0.0;
        String scoreText = tfScore.getText().trim();
        if (!scoreText.isEmpty()) {
            try {
                score = Double.parseDouble(scoreText);
            } catch (NumberFormatException e) {
                // Should not happen due to validation
                score = 0.0;
            }
        }

        // Get coordinates
        double latitude = 0.0;
        double longitude = 0.0;
        if (validatedCityCoordinates != null) {
            latitude = validatedCityCoordinates.getLatitude();
            longitude = validatedCityCoordinates.getLongitude();
        }

        // Try to detect best season if coordinates available
        String bestSeason = null;
        if (latitude != 0.0 || longitude != 0.0) {
            try {
                bestSeason = seasonService.getBestSeason(latitude, longitude);
                System.out.println("Best season for " + nom + ": " + bestSeason);
            } catch (Exception e) {
                System.err.println("Could not determine best season: " + e.getMessage());
            }
        }

        // Fetch country information from API
        CountryService.CountryInfo countryInfo = null;
        try {
            CountryService countryService = new CountryService();
            countryInfo = countryService.getCountryInfo(pays);

        } catch (CountryService.CountryNotFoundException e) {
            showValidationAlert("Pays non trouvé dans l'API. Vérifiez le nom du pays.");
            return;
        } catch (CountryService.ApiException e) {
            showErrorAlert("Erreur API", "Impossible de récupérer les informations du pays: " + e.getMessage());
            return;
        }

        try {
            // Create destination object with all fields
            Destination newDestination = new Destination();
            newDestination.setNom_destination(nom);
            newDestination.setPays_destination(pays);
            newDestination.setDescription_destination(taDescription.getText().trim());

            // Use detected best season for climat if available, otherwise use user selection
            if (bestSeason != null && !bestSeason.isEmpty()) {
                newDestination.setClimat_destination(bestSeason);
            } else {
                newDestination.setClimat_destination(cbClimat.getValue());
            }

            // Keep user's saison selection
            newDestination.setSaison_destination(cbSaison.getValue());

            // Set coordinates
            newDestination.setLatitude_destination(latitude);
            newDestination.setLongitude_destination(longitude);

            // Set score (user-provided or default 0.0)
            newDestination.setScore_destination(score);

            // Set the country information from API
            newDestination.setCurrency_destination(countryInfo.getCurrency());
            newDestination.setFlag_destination(countryInfo.getFlagUrl());
            newDestination.setLanguages_destination(countryInfo.getLanguages());

            // Use the CRUD's ajouter method
            destinationCRUD.ajouter(newDestination);

            // Show success message with only city and country
            String scoreMsg = score > 0 ? " (score: " + score + ")" : "";
            showSuccessAlert("Destination ajoutée avec succès!\n" + nom + ", " + pays);

            if (parentController != null) parentController.refreshAfterModification();
            closeWindow();

        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate") || e.getMessage().contains("duplicate")) {
                showValidationAlert("Cette destination existe déjà dans la base de données!");
            } else {
                showErrorAlert("Erreur lors de l'ajout", e.getMessage());
            }
        }
    }

    private boolean validateAllFields() {
        String nom = tfNom.getText().trim();
        String pays = tfPays.getText().trim();

        if (nom.length() < 2) {
            showValidationAlert("Le nom de la ville doit contenir au moins 2 caractères");
            return false;
        }
        if (pays.length() < 2) {
            showValidationAlert("Le pays doit contenir au moins 2 caractères");
            return false;
        }
        if (currentCountryCode == null) {
            showValidationAlert("Pays non valide. Vérifiez le nom du pays.");
            return false;
        }
        if (taDescription.getText().length() > 1000) {
            showValidationAlert("La description ne peut pas dépasser 1000 caractères");
            return false;
        }

        // Validate score if provided
        String scoreText = tfScore.getText().trim();
        if (!scoreText.isEmpty()) {
            try {
                double score = Double.parseDouble(scoreText);
                if (score < 0 || score > 10) {
                    showValidationAlert("Le score doit être entre 0 et 10");
                    return false;
                }
            } catch (NumberFormatException e) {
                showValidationAlert("Le score doit être un nombre valide");
                return false;
            }
        }

        return true;
    }

    private void showValidationAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Erreur de validation");
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