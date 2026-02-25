package Controllers;

import Entities.User;
import Services.UserCRUD;
import Utils.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Hyperlink forgotPasswordLink;
    @FXML private Hyperlink signupLink;
    @FXML private Button loginButton;
    @FXML private ComboBox<Country> countryCodeCombo;

    private UserCRUD userCRUD = new UserCRUD();
private Map<String, Integer> failedAttempts = new HashMap<>();

@FXML
private void handleLogin() { // Gérer la connexion de l'utilisateur après validation des champs
    String email = emailField.getText().trim();
    String password = passwordField.getText();

    // Validation basique
    if (!ValidationUtils.isNotEmpty(email) || !ValidationUtils.isNotEmpty(password)) {
        showAlert(Alert.AlertType.ERROR, "Champs requis", "Veuillez remplir tous les champs.");
        return;
    }
    if (!ValidationUtils.isValidEmail(email)) {
        showAlert(Alert.AlertType.ERROR, "Email invalide", "L'adresse email n'est pas valide.");
        return;
    }

    try {
        // Vérifier si l'email existe (pour savoir si on compte les tentatives)
        boolean emailExists = userCRUD.emailExists(email);
        if (!emailExists) {
            // On ne veut pas donner d'indice sur l'existence de l'email, donc on simule un échec
            showAlert(Alert.AlertType.ERROR, "Mot de passe ou email incorrect", "Le mot de passe ou email incorrect.");
            return;
        }

        // Vérifier le mot de passe avec BCrypt
        boolean passwordCorrect = userCRUD.checkPassword(email, password);
        if (passwordCorrect) {
            // Réinitialiser les tentatives
            failedAttempts.remove(email);

            // Vérifier si l'email est vérifié
            if (!userCRUD.isEmailVerified(email)) {
                showAlert(Alert.AlertType.WARNING, "Email non vérifié", "Veuillez vérifier votre email avant de vous connecter.");
                goToVerifyEmail(email);
                return;
            }

            User user = userCRUD.getUserByEmail(email);
            UserSession.getInstance().setCurrentUser(user);

            if ("ADMIN".equals(user.getRole())) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Bienvenue " + user.getPrenom() + " (Admin) !");
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin_users.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) loginButton.getScene().getWindow();
                stage.setScene(new Scene(root));
            } else {
                // Enregistrer la dernière connexion (IP, localisation)
                try {
                    String ip = userCRUD.getPublicIp() != null ? userCRUD.getPublicIp() : "IP non disponible";
                    String location = userCRUD.getLocationFromIp(ip) != null ? userCRUD.getLocationFromIp(ip) : "Localisation non disponible";
                    userCRUD.updateLastLogin(user.getId(), ip, location);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Bienvenue " + user.getPrenom() + " !");
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/profile.fxml"));
                Parent root = loader.load();
                ProfileController profileController = loader.getController();
                if (profileController != null) {
                    profileController.setUser(user);
                }
                Stage stage = (Stage) loginButton.getScene().getWindow();
                stage.setScene(new Scene(root));
            }
        } else {
            // Mot de passe incorrect
            int attempts = failedAttempts.getOrDefault(email, 0) + 1;
            failedAttempts.put(email, attempts);

            if (attempts >= 3) {
                // Capture photo et envoi email
                try {
                    File photo = WebcamUtil.captureImage(email);
                    // Envoyer l'alerte
                    EmailSender.sendWarningEmailWithAttachment(email, photo);
                    showAlert(Alert.AlertType.WARNING, "Alerte de sécurité",
                            "Trois tentatives échouées. Une photo a été prise et un email d'alerte a été envoyé au propriétaire du compte.");
                    // Supprimer le fichier temporaire après envoi
                    photo.delete();
                } catch (Exception e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de capturer la photo ou d'envoyer l'email : " + e.getMessage());
                }
                // Réinitialiser le compteur pour cet email pour éviter de spammer
                failedAttempts.remove(email);
            } else {
                showAlert(Alert.AlertType.ERROR, "Mot de passe ou email incorrect",
                        "Le mot de passe ou email incorrect. Tentative " + attempts + "/3.");
            }
        }
    } catch (SQLException | IOException e) {
        e.printStackTrace();
        showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de base de données : " + e.getMessage());
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
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }

    @FXML
    private void redirectToHome(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/home.fxml"));
            Parent root = loader.load();
            HomeController homeController = loader.getController();
            homeController.setUser(user); // pour afficher le nom
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void redirectToProfile(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/profile.fxml"));
            Parent root = loader.load();
            // controller initialize() will run automatically and pick up the user from session,
            // but we can also pass it explicitly in case we want to support that.
            ProfileController profileController = loader.getController();
            if (profileController != null) {
                profileController.setUser(user);
            }
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToSignup() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/signup.fxml"));
            Stage stage = (Stage) signupLink.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToForgotPassword() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/forgot_password.fxml"));
            Stage stage = (Stage) forgotPasswordLink.getScene().getWindow();
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