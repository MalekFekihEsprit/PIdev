package Controllers;

import Entities.User;
import Services.CountryService;
import Services.UserCRUD;
import Utils.Country;
import Utils.EmailSender;
import Utils.PhoneCodeUtil;
import Utils.ValidationUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import javax.mail.MessagingException;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class SignupController {

    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private TextField telephoneField;
    @FXML private DatePicker dateNaissancePicker;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField photoUrlField;
    @FXML private Button signupButton;
    @FXML private Hyperlink loginLink;
    @FXML private ComboBox<Country> countryCodeCombo;
    private final Map<String, Image> flagCache = new HashMap<>();
    private UserCRUD userCRUD = new UserCRUD();
// make the ComboBox unclickable and uneditable
    @FXML
    public void initialize() {

        try {
            // 1️⃣ Load countries (this fills the ComboBox)
            countryCodeCombo.getItems().addAll(CountryService.getAllCountries());
            for (Country c : CountryService.getAllCountries()) {
                countryCodeCombo.getItems().add(c);

                // Preload flag images asynchronously
                new Thread(() -> {
                    try {
                        Image img = new Image(c.getFlagUrl(), 24, 16, true, true, true);
                        flagCache.put(c.getIsoCode(), img);
                    } catch (Exception e) {
                        // ignore failed flags
                    }
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

                        Image cached = flagCache.get(country.getIsoCode());
                        imageView.setImage(cached);
                        setText(country.getName() + " " + country.getDialCode());
                        setGraphic(imageView);
                    }
                }
            });
            // 2️⃣ Auto-detect country from IP
            String iso = PhoneCodeUtil.getCountryCodeFromIP();

            for (Country c : countryCodeCombo.getItems()) {
                if (c.getIsoCode().equalsIgnoreCase(iso)) {
                    countryCodeCombo.setValue(c);
                    break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        // 3️⃣ Set flag rendering (this part stays as before)
        countryCodeCombo.setCellFactory(listView -> new ListCell<Country>() {
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

                    Image cached = flagCache.get(country.getIsoCode());
                    imageView.setImage(cached);
                    setText(country.getName() + " " + country.getDialCode());
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

                    Image cached = flagCache.get(country.getIsoCode());
                    imageView.setImage(cached);
                    setText(country.getName() + " " + country.getDialCode());
                    setGraphic(imageView);
                }
            }
        });
    }

    @FXML
    private void handleSignup() {
        // Récupération des valeurs
        String nom = nomField.getText().trim();
        String prenom = prenomField.getText().trim();
        String email = emailField.getText().trim();

        Country selectedCountry = countryCodeCombo.getValue();
        String localPhone = telephoneField.getText().trim();

        String telephone = "";

        if (selectedCountry != null && !localPhone.isEmpty()) {
            telephone = selectedCountry.getDialCode() + localPhone;
        }

        LocalDate dateNaissance = dateNaissancePicker.getValue();
        String password = passwordField.getText();
        String confirm = confirmPasswordField.getText();
        String photoUrl = photoUrlField.getText().trim();

        // Validations
        if (!ValidationUtils.isNotEmpty(nom)) {
            showAlert(Alert.AlertType.ERROR, "Validation", "Le nom est obligatoire.");
            return;
        }
        if (!ValidationUtils.isNotEmpty(prenom)) {
            showAlert(Alert.AlertType.ERROR, "Validation", "Le prénom est obligatoire.");
            return;
        }
        if (!ValidationUtils.isValidEmail(email)) {
            showAlert(Alert.AlertType.ERROR, "Validation", "L'email n'est pas valide.");
            return;
        }
        if (!localPhone.isEmpty() && !ValidationUtils.isValidPhone(localPhone)) {
            showAlert(Alert.AlertType.ERROR, "Validation",
                    "Le téléphone doit contenir uniquement des chiffres (8 à 15).");
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
        if (!ValidationUtils.isPasswordValid(password)) {
            showAlert(Alert.AlertType.ERROR, "Validation", "Le mot de passe doit contenir au moins 6 caractères.");
            return;
        }
        if (!ValidationUtils.passwordsMatch(password, confirm)) {
            showAlert(Alert.AlertType.ERROR, "Validation", "Les mots de passe ne correspondent pas.");
            return;
        }

        // Vérifier si l'email existe déjà
        try {
            if (userCRUD.emailExists(email)) {
                showAlert(Alert.AlertType.ERROR, "Email déjà utilisé", "Un compte avec cet email existe déjà.");
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de base de données : " + e.getMessage());
            return;
        }

        // Créer l'utilisateur
        User newUser = new User();
        newUser.setNom(nom);
        newUser.setPrenom(prenom);
        newUser.setEmail(email);
        newUser.setTelephone(telephone.isEmpty() ? null : telephone);
        newUser.setDateNaissance(dateNaissance);
        newUser.setMotDePasse(password); // À hasher plus tard
        newUser.setRole("USER");
        newUser.setPhotoUrl(photoUrl.isEmpty() ? null : photoUrl);

        try {
            userCRUD.ajouter(newUser);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Compte créé avec succès ! Vous pouvez maintenant vous connecter.");

            // Après avoir créé l'utilisateur et récupéré son ID (ou après l'ajout)
            User created = userCRUD.getUserByEmail(email); // assure-toi que cette méthode existe
            if (created != null) {
                String code = String.format("%06d", new Random().nextInt(999999));
                userCRUD.saveVerificationCode(created.getId(), code);
                try {
                    EmailSender.sendVerificationEmail(email, code);
                } catch (MessagingException e) {
                    throw new RuntimeException(e);
                }

                showAlert(Alert.AlertType.INFORMATION, "Inscription réussie",
                        "Un code de vérification a été envoyé à votre adresse email.");

                // Rediriger vers la page de vérification
                goToVerifyEmail(email);
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de récupérer l'utilisateur créé.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la création du compte : " + e.getMessage());
        }
    }

    private void goToVerifyEmail(String email) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/verify_email.fxml"));
            Parent root = loader.load();
            VerifyEmailController controller = loader.getController();
            if (controller != null) {
                controller.setEmail(email);
            }
            Stage stage = (Stage) signupButton.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToLogin() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
            Stage stage = (Stage) loginLink.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}