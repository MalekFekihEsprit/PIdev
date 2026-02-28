package Controllers;

import Entities.User;
import Services.CountryService;
import Services.UserCRUD;
import Utils.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileController {

    @FXML private Label userNameLabel, fullNameLabel, emailLabel;
    @FXML private ImageView profileImageView;
    @FXML private TextField nomField, prenomField, emailField, telephoneField, photoUrlField;
    @FXML private DatePicker dateNaissancePicker;
    @FXML private PasswordField newPasswordField, confirmPasswordField;
    @FXML private VBox passwordSection;
    @FXML private HBox editButtons;
    @FXML private Button editModeButton, saveButton, cancelButton, deleteButton, uploadPhotoButton;
    @FXML private Hyperlink logoutLink;
    @FXML private Label statsVoyages, statsDepenses;
    @FXML private ComboBox<Country> countryCodeCombo;
    @FXML private Button enrollFaceButton;

    // Navigation buttons (navbar)
    @FXML private HBox btnDestinations;
    @FXML private HBox btnHebergement;
    @FXML private HBox btnItineraires;
    @FXML private HBox btnActivites;
    @FXML private HBox btnVoyages;
    @FXML private HBox btnBudgets;
    @FXML private HBox btnCategories;
    @FXML private HBox userProfileBox;
    @FXML private Label lblUserName;
    @FXML private Label lblUserRole;
    @FXML private Label lblLastUpdate;

    // Scroll navigation elements
    @FXML private ScrollPane navScrollPane;
    @FXML private HBox navLinksContainer;
    @FXML private HBox leftArrow;
    @FXML private HBox rightArrow;

    private final Map<String, Image> flagCache = new HashMap<>();
    private File selectedImageFile;
    private UserCRUD userCRUD = new UserCRUD();
    private User currentUser;
    private boolean editMode = false;
    private FaceRecognitionClient faceClient = new FaceRecognitionClient();

    @FXML
    public void initialize() {
        currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser == null) {
            goToLogin();
            return;
        }
        loadUserData();
        setupCountryCodeCombo();
        loadAvatar();
        statsVoyages.setText("0");
        statsDepenses.setText("0 €");
        if (currentUser.getFaceEmbedding() != null) {
            enrollFaceButton.setText("🔄 Mettre à jour mon visage");
        } else {
            enrollFaceButton.setText("👤 Enregistrer mon visage");
        }
        setupNavigationButtons();
        setupUserProfile();
        updateUserInfo();
        updateLastUpdateTime();
        setupScrollArrows();
    }

    public void setUser(User user) {
        this.currentUser = user;
        if (user != null) {
            loadUserData();
            loadAvatar();
            statsVoyages.setText("0");
            statsDepenses.setText("0 €");
        }
    }

    private void loadUserData() {
        fullNameLabel.setText(currentUser.getPrenom() + " " + currentUser.getNom());
        emailLabel.setText(currentUser.getEmail());
        userNameLabel.setText(currentUser.getPrenom() + " " + currentUser.getNom());

        nomField.setText(currentUser.getNom());
        prenomField.setText(currentUser.getPrenom());
        emailField.setText(currentUser.getEmail());

        if (currentUser.getTelephone() != null && !currentUser.getTelephone().isEmpty()) {
            String phone = currentUser.getTelephone();
            String dial = phone.replaceAll("\\d+", "");
            String number = phone.substring(dial.length());
            telephoneField.setText(number);

            countryCodeCombo.getItems().stream()
                    .filter(c -> c.getDialCode().equals(dial))
                    .findFirst()
                    .ifPresent(countryCodeCombo::setValue);
        } else {
            telephoneField.setText("");
        }

        dateNaissancePicker.setValue(currentUser.getDateNaissance());
        photoUrlField.setText(currentUser.getPhotoUrl() != null ? currentUser.getPhotoUrl() : "");
    }

    private void updateUserInfo() {
        if (currentUser != null) {
            lblUserName.setText(currentUser.getPrenom() + " " + currentUser.getNom());
            lblUserRole.setText(currentUser.getRole());
        } else {
            lblUserName.setText("Utilisateur");
            lblUserRole.setText("Non connecté");
        }
    }

    private void updateLastUpdateTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");
        lblLastUpdate.setText("Dernière mise à jour: " + LocalDateTime.now().format(formatter));
    }

    private void setupUserProfile() {
        if (userProfileBox != null) {
            userProfileBox.setOnMouseClicked(event -> {
                // Already on profile page, could refresh or do nothing
            });
            userProfileBox.setOnMouseEntered(event ->
                    userProfileBox.setStyle("-fx-background-color: #e2e8f0; -fx-background-radius: 25; -fx-padding: 6 16 6 6; -fx-cursor: hand;"));
            userProfileBox.setOnMouseExited(event ->
                    userProfileBox.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 25; -fx-padding: 6 16 6 6; -fx-cursor: hand;"));
        }
    }

    private void setupScrollArrows() {
        if (navScrollPane != null && leftArrow != null && rightArrow != null) {
            // Masquer les flèches initialement
            leftArrow.setVisible(false);
            leftArrow.setManaged(false);
            rightArrow.setVisible(false);
            rightArrow.setManaged(false);

            // Surveiller les changements de largeur
            navScrollPane.widthProperty().addListener((obs, oldVal, newVal) -> {
                updateArrowVisibility();
            });

            navLinksContainer.widthProperty().addListener((obs, oldVal, newVal) -> {
                updateArrowVisibility();
            });

            // Surveiller le défilement
            navScrollPane.hvalueProperty().addListener((obs, oldVal, newVal) -> {
                updateArrowVisibility();
            });
        }
    }

    private void updateArrowVisibility() {
        if (navScrollPane == null || navLinksContainer == null) return;

        double contentWidth = navLinksContainer.getWidth();
        double viewportWidth = navScrollPane.getViewportBounds().getWidth();
        double hvalue = navScrollPane.getHvalue();

        // Afficher flèche gauche si on n'est pas au début ET si le contenu dépasse
        boolean showLeft = contentWidth > viewportWidth && hvalue > 0.01;
        leftArrow.setVisible(showLeft);
        leftArrow.setManaged(showLeft);

        // Afficher flèche droite si on n'est pas à la fin ET si le contenu dépasse
        boolean showRight = contentWidth > viewportWidth && hvalue < 0.99;
        rightArrow.setVisible(showRight);
        rightArrow.setManaged(showRight);
    }

    @FXML
    private void scrollLeft() {
        if (navScrollPane != null) {
            double newHvalue = navScrollPane.getHvalue() - 0.15;
            navScrollPane.setHvalue(Math.max(0, newHvalue));
        }
    }

    @FXML
    private void scrollRight() {
        if (navScrollPane != null) {
            double newHvalue = navScrollPane.getHvalue() + 0.15;
            navScrollPane.setHvalue(Math.min(1, newHvalue));
        }
    }

    private void setupNavigationButtons() {
        if (btnDestinations != null) {
            btnDestinations.setOnMouseClicked(event -> navigateToDestinations());
            setupNavButtonHover(btnDestinations, "🌍", "Destinations");
        }

        if (btnHebergement != null) {
            btnHebergement.setOnMouseClicked(event -> navigateToHebergement());
            setupNavButtonHover(btnHebergement, "🏨", "Hébergement");
        }

        if (btnItineraires != null) {
            btnItineraires.setOnMouseClicked(event ->
                    showInfoAlert("Itinéraires", "Cette fonctionnalité sera bientôt disponible"));
            setupNavButtonHover(btnItineraires, "🗺️", "Itinéraires");
        }

        // Bouton Activités
        if (btnActivites != null) {
            btnActivites.setOnMouseClicked(event -> navigateToActivitesFront());
            setupNavButtonHover(btnActivites, "🏄", "Activités");
        }

        // Bouton Catégories
        if (btnCategories != null) {
            btnCategories.setOnMouseClicked(event -> navigateToCategoriesFront());
            setupNavButtonHover(btnCategories, "📑", "Catégories");
        }

        // BOUTON VOYAGES - CORRIGÉ
        if (btnVoyages != null) {
            btnVoyages.setOnMouseClicked(event -> navigateToVoyages());
            setupNavButtonHover(btnVoyages, "✈️", "Voyages");
        }

        if (btnBudgets != null) {
            btnBudgets.setOnMouseClicked(event ->
                    showInfoAlert("Budgets", "Cette fonctionnalité sera bientôt disponible"));
            setupNavButtonHover(btnBudgets, "💰", "Budgets");
        }
    }

    private void setupNavButtonHover(HBox button, String icon, String text) {
        if (button == null) return;

        button.setOnMouseEntered(event -> {
            button.setStyle("-fx-background-color: rgba(255,140,66,0.1); -fx-background-radius: 12; -fx-padding: 8 16; -fx-cursor: hand; -fx-border-color: #ff8c42; -fx-border-width: 1; -fx-border-radius: 12;");
            button.lookupAll(".label").forEach(label -> {
                if (label instanceof Label) {
                    Label lbl = (Label) label;
                    if (lbl.getText().equals(icon)) {
                        lbl.setStyle("-fx-font-size: 16;");
                    } else {
                        lbl.setStyle("-fx-text-fill: #ff8c42; -fx-font-weight: 600; -fx-font-size: 14;");
                    }
                }
            });
        });

        button.setOnMouseExited(event -> {
            button.setStyle("-fx-background-color: transparent; -fx-background-radius: 12; -fx-padding: 8 16; -fx-cursor: hand;");
            button.lookupAll(".label").forEach(label -> {
                if (label instanceof Label) {
                    Label lbl = (Label) label;
                    if (lbl.getText().equals(icon)) {
                        lbl.setStyle("-fx-font-size: 16;");
                    } else {
                        lbl.setStyle("-fx-text-fill: #475569; -fx-font-weight: 500; -fx-font-size: 14;");
                    }
                }
            });
        });
    }

    // Navigation vers Activités Front
    private void navigateToActivitesFront() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/activitesfront.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnActivites.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TravelMate - Activités");
            stage.setMaximized(true);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible d'ouvrir les activités: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Navigation vers Catégories Front
    private void navigateToCategoriesFront() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/categoriesfront.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnCategories.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TravelMate - Catégories");
            stage.setMaximized(true);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible d'ouvrir les catégories: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Navigation vers Destinations
    private void navigateToDestinations() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DestinationFront.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnDestinations.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TravelMate - Destinations");
            stage.setMaximized(true);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir les destinations: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Navigation vers Hébergements
    private void navigateToHebergement() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/HebergementFront.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnHebergement.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TravelMate - Hébergements");
            stage.setMaximized(true);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir les hébergements: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // NOUVELLE MÉTHODE : Navigation vers Voyages
    private void navigateToVoyages() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/PageVoyage.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnVoyages.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TravelMate - Gestion des Voyages");
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible d'ouvrir la page des voyages: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleFaceEnrollment() {
        if (currentUser == null) return;

        File faceImage = FaceCaptureDialog.captureFace((Stage) enrollFaceButton.getScene().getWindow());
        if (faceImage == null) return;

        try {
            List<Double> embedding = faceClient.extractEmbedding(faceImage);
            if (embedding == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Aucun visage détecté. Veuillez réessayer.");
                faceImage.delete();
                return;
            }

            String embeddingJson = EmbeddingConverter.toJson(embedding);
            currentUser.setFaceEmbedding(embeddingJson);
            userCRUD.modifier(currentUser, false);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Visage enregistré avec succès !");
            faceImage.delete();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de communication avec le service de reconnaissance faciale.");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la sauvegarde en base.");
        }
    }

    private void setupCountryCodeCombo() {
        List<Country> countries = null;
        try {
            countries = CountryService.getAllCountries();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        for (Country c : countries) {
            countryCodeCombo.getItems().add(c);
            new Thread(() -> {
                try {
                    Image img = new Image(c.getFlagUrl(), 24, 16, true, true, true);
                    flagCache.put(c.getIsoCode(), img);
                } catch (Exception ignored) {}
            }).start();
        }

        countryCodeCombo.setCellFactory(lv -> new ListCell<Country>() {
            private final ImageView imageView = new ImageView();
            @Override
            protected void updateItem(Country country, boolean empty) {
                super.updateItem(country, empty);
                if (empty || country == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    imageView.setFitWidth(24);
                    imageView.setFitHeight(16);
                    imageView.setPreserveRatio(true);
                    imageView.setImage(flagCache.get(country.getIsoCode()));
                    setText(" " + country.getDialCode());
                    setGraphic(imageView);
                }
            }
        });

        countryCodeCombo.setButtonCell(new ListCell<Country>() {
            private final ImageView imageView = new ImageView();
            @Override
            protected void updateItem(Country country, boolean empty) {
                super.updateItem(country, empty);
                if (empty || country == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    imageView.setFitWidth(24);
                    imageView.setFitHeight(16);
                    imageView.setPreserveRatio(true);
                    imageView.setImage(flagCache.get(country.getIsoCode()));
                    setText(" " + country.getDialCode());
                    setGraphic(imageView);
                }
            }
        });

        if (currentUser.getTelephone() != null && !currentUser.getTelephone().isEmpty()) {
            String userDial = currentUser.getTelephone().substring(0, currentUser.getTelephone().indexOf(currentUser.getTelephone().replaceAll("\\D+", "")));
            countries.stream()
                    .filter(c -> c.getDialCode().equals(userDial))
                    .findFirst()
                    .ifPresent(countryCodeCombo::setValue);
        } else {
            try {
                String iso = PhoneCodeUtil.getCountryCodeFromIP();
                countries.stream().filter(c -> c.getIsoCode().equals(iso))
                        .findFirst()
                        .ifPresent(countryCodeCombo::setValue);
            } catch (Exception e) {
                e.printStackTrace();
                countryCodeCombo.setValue(countries.get(0));
            }
        }
    }

    private void loadAvatar() {
        Image image = null;

        if (currentUser.getPhotoFileName() != null) {
            File file = FileUtil.getImageFile(currentUser.getPhotoFileName());
            if (file != null && file.exists()) {
                image = new Image(file.toURI().toString(), 64, 64, true, true);
            }
        }

        if (image == null && currentUser.getPhotoUrl() != null && !currentUser.getPhotoUrl().isEmpty()) {
            try {
                image = new Image(currentUser.getPhotoUrl(), 64, 64, true, true);
            } catch (Exception e) {
                System.err.println("Erreur chargement URL : " + e.getMessage());
            }
        }

        if (image == null) {
            String gravatarUrl = GravatarUtil.getGravatarUrl(currentUser.getEmail(), 64);
            try {
                image = new Image(gravatarUrl, 64, 64, true, true);
            } catch (Exception e) {
                System.err.println("Erreur Gravatar : " + e.getMessage());
            }
        }

        if (image != null && !image.isError()) {
            profileImageView.setImage(image);
        } else {
            profileImageView.setImage(null);
        }
    }

    @FXML
    private void handleUploadPhoto() {
        Stage stage = (Stage) uploadPhotoButton.getScene().getWindow();
        File file = FileUtil.selectImageFile(stage);
        if (file != null) {
            Image preview = new Image(file.toURI().toString(), 64, 64, true, true);
            profileImageView.setImage(preview);
            selectedImageFile = file;
        }
    }

    @FXML
    private void toggleEditMode() {
        editMode = !editMode;
        setEditable(editMode);
        editModeButton.setText(editMode ? "🔍 Mode consultation" : "✏️ Modifier");
        passwordSection.setVisible(editMode);
        passwordSection.setManaged(editMode);
        editButtons.setVisible(editMode);
        editButtons.setManaged(editMode);
    }

    private void setEditable(boolean editable) {
        nomField.setEditable(editable);
        prenomField.setEditable(editable);
        telephoneField.setEditable(editable);
        dateNaissancePicker.setEditable(editable);
        photoUrlField.setEditable(editable);
    }

    @FXML
    private void handleSave() {
        String nom = nomField.getText().trim();
        String prenom = prenomField.getText().trim();

        String dial = countryCodeCombo.getValue() != null ? countryCodeCombo.getValue().getDialCode() : "";
        String phoneNumber = telephoneField.getText().trim();
        String fullPhone = phoneNumber.isEmpty() ? null : dial + phoneNumber;

        LocalDate dateNaissance = dateNaissancePicker.getValue();
        String photoUrl = photoUrlField.getText().trim();
        String newPassword = newPasswordField.getText();
        String confirm = confirmPasswordField.getText();

        if (!ValidationUtils.isNotEmpty(nom)) {
            showAlert(Alert.AlertType.ERROR, "Validation", "Le nom ne peut pas être vide.");
            return;
        }
        if (!ValidationUtils.isNotEmpty(prenom)) {
            showAlert(Alert.AlertType.ERROR, "Validation", "Le prénom ne peut pas être vide.");
            return;
        }
        if (dateNaissance == null) {
            showAlert(Alert.AlertType.ERROR, "Validation", "La date de naissance est obligatoire.");
            return;
        }
        if (!ValidationUtils.isAdult(dateNaissance)) {
            showAlert(Alert.AlertType.ERROR, "Validation", "Vous devez avoir au moins 18 ans.");
            return;
        }
        if (!phoneNumber.isEmpty() && !ValidationUtils.isValidPhone(phoneNumber)) {
            showAlert(Alert.AlertType.ERROR, "Validation", "Le téléphone doit contenir 8 à 15 chiffres.");
            return;
        }

        if (selectedImageFile != null) {
            try {
                if (currentUser.getPhotoFileName() != null) {
                    File oldFile = FileUtil.getImageFile(currentUser.getPhotoFileName());
                    if (oldFile != null && oldFile.exists()) oldFile.delete();
                }
                String fileName = FileUtil.saveImageToUploads(selectedImageFile, currentUser.getId());
                currentUser.setPhotoFileName(fileName);
                currentUser.setPhotoUrl(null);
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de sauvegarder l'image : " + e.getMessage());
                return;
            }
        } else if (!photoUrl.isEmpty()) {
            currentUser.setPhotoUrl(photoUrl);
            currentUser.setPhotoFileName(null);
        }

        currentUser.setNom(nom);
        currentUser.setPrenom(prenom);
        currentUser.setTelephone(fullPhone);
        currentUser.setDateNaissance(dateNaissance);

        boolean passwordChanged = !newPassword.isEmpty();

        try {
            if (passwordChanged) {
                if (!ValidationUtils.isPasswordValid(newPassword)) {
                    showAlert(Alert.AlertType.ERROR, "Validation", "Le mot de passe doit contenir au moins 6 caractères.");
                    return;
                }
                if (!ValidationUtils.passwordsMatch(newPassword, confirm)) {
                    showAlert(Alert.AlertType.ERROR, "Validation", "Les mots de passe ne correspondent pas.");
                    return;
                }
                String hashed = BCrypt.hashpw(newPassword, BCrypt.gensalt());
                currentUser.setMotDePasse(hashed);
            }

            userCRUD.modifier(currentUser, passwordChanged);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Profil mis à jour.");

            loadUserData();
            loadAvatar();
            updateUserInfo();
            toggleEditMode();
            newPasswordField.clear();
            confirmPasswordField.clear();
            selectedImageFile = null;
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur base de données : " + e.getMessage());
        }
    }

    @FXML
    private void cancelEdit() {
        loadUserData();
        loadAvatar();
        newPasswordField.clear();
        confirmPasswordField.clear();
        if (editMode) toggleEditMode();
        selectedImageFile = null;
    }

    @FXML
    private void handleDeleteAccount() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Suppression du compte");
        confirm.setHeaderText("Êtes-vous sûr de vouloir supprimer votre compte ?");
        confirm.setContentText("Cette action est irréversible.");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    if (currentUser.getPhotoFileName() != null) {
                        File photo = FileUtil.getImageFile(currentUser.getPhotoFileName());
                        if (photo != null && photo.exists()) photo.delete();
                    }
                    userCRUD.supprimer(currentUser.getId());
                    UserSession.getInstance().clear();
                    goToLogin();
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la suppression.");
                }
            }
        });
    }

    @FXML
    private void handleLogout() {
        UserSession.getInstance().clear();
        goToLogin();
    }

    private void goToLogin() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
            Stage stage = (Stage) logoutLink.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}