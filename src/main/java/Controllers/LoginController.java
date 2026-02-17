package Controllers;

import Entities.User;
import Services.UserCRUD;
import Utils.UserSession;
import Utils.ValidationUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Hyperlink forgotPasswordLink;
    @FXML private Hyperlink signupLink;
    @FXML private Button loginButton;

    private UserCRUD userCRUD = new UserCRUD();

    @FXML
    private void handleLogin() { // Gérer la connexion de l'utilisateur après validation des champs
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        // Validation basique
        if (!ValidationUtils.isNotEmpty(email) || !ValidationUtils.isNotEmpty(password)) {
            showAlert(Alert.AlertType.ERROR, "Champs requis", "Veuillez remplir tous les champs.");
            return;
        }
        // Validation si l'email et le mot de passe correspondent à un utilisateur existant
        if (!userCRUD.validateEmailAndPassword(email, password)) {
            showAlert(Alert.AlertType.ERROR, "Mot de passe ou email incorrect", "Le mot de passe ou email incorrect.");
            return;
        }
        
        if (!ValidationUtils.isValidEmail(email)) {
            showAlert(Alert.AlertType.ERROR, "Email invalide", "L'adresse email n'est pas valide.");
            return;
        }

        try {
            User user = userCRUD.getUserByEmailAndPassword(email, password);
            if (user != null) {
                UserSession.getInstance().setCurrentUser(user);
                if ("ADMIN".equals(user.getRole())) { // Si l'utilisateur est admin, rediriger vers admin_users
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Bienvenue " + user.getPrenom() + " (Admin) !");
                    // rediriger vers admin_users
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin_users.fxml"));
                    Parent root = loader.load();
                    Stage stage = (Stage) loginButton.getScene().getWindow();
                    stage.setScene(new Scene(root));
                } else { // Sinon, rediriger vers profile
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Bienvenue " + user.getPrenom() + " !");
                    // rediriger vers profile
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/profile.fxml"));
                    Parent root = loader.load();
                    ProfileController profileController = loader.getController();
                    if (profileController != null) {
                        profileController.setUser(user);
                    }
                    Stage stage = (Stage) loginButton.getScene().getWindow();
                    stage.setScene(new Scene(root));

                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de base de données : " + e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
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