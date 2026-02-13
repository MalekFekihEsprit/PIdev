package Controllers;

import Entities.User;
import Services.UserCRUD;
import Utils.ValidationUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;

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

    private UserCRUD userCRUD = new UserCRUD();

    @FXML
    private void handleSignup() {
        // Récupération des valeurs
        String nom = nomField.getText().trim();
        String prenom = prenomField.getText().trim();
        String email = emailField.getText().trim();
        String telephone = telephoneField.getText().trim();
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
        if (!ValidationUtils.isValidPhone(telephone) && !telephone.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation", "Le téléphone doit contenir 8 à 15 chiffres.");
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
            // Rediriger vers login
            goToLogin();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la création du compte : " + e.getMessage());
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