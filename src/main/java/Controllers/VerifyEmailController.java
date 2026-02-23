package Controllers;

import Entities.User;
import Services.UserCRUD;
import Utils.EmailSender;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Random;

public class VerifyEmailController {

    @FXML private Label emailInfoLabel;
    @FXML private TextField codeField;
    @FXML private Button verifyButton;
    @FXML private Hyperlink resendLink;
    @FXML private Hyperlink backToLoginLink;

    private UserCRUD userCRUD = new UserCRUD();
    private String email; // email à vérifier (passé depuis SignupController)

    public void setEmail(String email) {
        this.email = email;
        emailInfoLabel.setText(email);
    }

    @FXML
    private void handleVerify() {
        String code = codeField.getText().trim();

        if (code.isEmpty() || code.length() != 6 || !code.matches("\\d{6}")) {
            showAlert(Alert.AlertType.ERROR, "Code invalide", "Veuillez saisir un code à 6 chiffres.");
            return;
        }

        try {
            // Récupérer l'utilisateur par email
            User user = userCRUD.getUserByEmail(email); // nous devons ajouter cette méthode dans UserCRUD
            if (user == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Utilisateur introuvable.");
                return;
            }

            boolean verified = userCRUD.verifyEmail(user.getId(), code);
            if (verified) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Votre email a été vérifié. Vous pouvez maintenant vous connecter.");
                goToLogin();
            } else {
                showAlert(Alert.AlertType.ERROR, "Code incorrect", "Le code saisi est invalide ou a expiré.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de base de données : " + e.getMessage());
        }
    }

    @FXML
    private void handleResend() {
        try {
            User user = userCRUD.getUserByEmail(email);
            if (user == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Utilisateur introuvable.");
                return;
            }

            // Générer un nouveau code
            String newCode = String.format("%06d", new Random().nextInt(999999));
            userCRUD.saveVerificationCode(user.getId(), newCode);

            // Envoyer l'email
            EmailSender.sendVerificationEmail(email, newCode);

            showAlert(Alert.AlertType.INFORMATION, "Code renvoyé", "Un nouveau code de vérification a été envoyé à votre adresse email.");
        } catch (SQLException | javax.mail.MessagingException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de renvoyer le code : " + e.getMessage());
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

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}