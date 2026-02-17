package Controllers;

import Services.UserCRUD;
import Utils.ValidationUtils;
import Entities.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class ForgotPasswordController {

    @FXML private TextField emailField;
    @FXML private Button sendButton;
    @FXML private Hyperlink backToLoginLink;

    private UserCRUD userCRUD = new UserCRUD();

    @FXML
    private void handleSend() { // Gérer l'envoi du lien de réinitialisation après validation de l'email
        String email = emailField.getText().trim();

        if (!ValidationUtils.isNotEmpty(email)) {
            showAlert(Alert.AlertType.ERROR, "Champ requis", "Veuillez saisir votre email.");
            return;
        }

        if (!ValidationUtils.isValidEmail(email)) {
            showAlert(Alert.AlertType.ERROR, "Email invalide", "L'adresse email n'est pas valide.");
            return;
        }

        try {
            // Vérifier si l'email existe
            if (!userCRUD.emailExists(email)) {
                showAlert(Alert.AlertType.ERROR, "Email inconnu", "Aucun compte n'est associé à cet email.");
                return;
            }

            // Simuler l'envoi d'un lien (ici on redirige directement vers reset_password en passant l'email)
            
            showAlert(Alert.AlertType.INFORMATION, "Email envoyé", "Un lien de réinitialisation a été envoyé à votre adresse. (Simulation : redirection vers la page de réinitialisation)");

            // Rediriger vers reset_password.fxml avec l'email en paramètre
            goToResetPassword(email);

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de base de données : " + e.getMessage());
        }
    }

    @FXML
    private void goToLogin() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
            Stage stage = (Stage) backToLoginLink.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void goToResetPassword(String email) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/reset_password.fxml"));
            Parent root = loader.load();
            ResetPasswordController controller = loader.getController();
            controller.setEmail(email); // passer l'email au contrôleur suivant
            Stage stage = (Stage) sendButton.getScene().getWindow();
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