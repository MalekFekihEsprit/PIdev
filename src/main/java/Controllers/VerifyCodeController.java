package Controllers;

import Utils.EmailSender;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import javax.mail.MessagingException;
import java.io.IOException;
import java.security.SecureRandom;

public class VerifyCodeController {

    @FXML private TextField codeField;
    @FXML private Button verifyButton;
    @FXML private Hyperlink resendLink;
    @FXML private Hyperlink backToLoginLink;

    private String email;
    private String generatedCode;
    private long codeTimestamp; // optionnel pour expiration

    // À appeler depuis ForgotPasswordController avant d'afficher cette vue
    public void setEmailAndCode(String email, String code) {
        this.email = email;
        this.generatedCode = code;
        this.codeTimestamp = System.currentTimeMillis();
    }

    @FXML
    private void handleVerify() {
        String enteredCode = codeField.getText().trim();

        // Vérifier expiration (ex: 5 minutes)
        long now = System.currentTimeMillis();
        if (now - codeTimestamp > 5 * 60 * 1000) {
            showAlert(Alert.AlertType.ERROR, "Code expiré", "Le code a expiré. Veuillez en demander un nouveau.");
            return;
        }

        if (enteredCode.equals(generatedCode)) {
            // Code correct → aller vers reset_password
            goToResetPassword();
        } else {
            showAlert(Alert.AlertType.ERROR, "Code incorrect", "Le code saisi ne correspond pas. Veuillez réessayer.");
        }
    }

    @FXML
    private void handleResend() {
        // Générer un nouveau code
        String newCode = generateCode();
        // Envoyer par email
        try {
            EmailSender.sendResetCode(email, newCode);
            this.generatedCode = newCode;
            this.codeTimestamp = System.currentTimeMillis();
            showAlert(Alert.AlertType.INFORMATION, "Code renvoyé", "Un nouveau code a été envoyé à votre adresse.");
        } catch (MessagingException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de l'envoi du code : " + e.getMessage());
        }
    }

    private void goToResetPassword() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/reset_password.fxml"));
            Parent root = loader.load();
            ResetPasswordController controller = loader.getController();
            controller.setEmail(email); // transmettre l'email pour la réinitialisation
            Stage stage = (Stage) verifyButton.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
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

    // Génère un code aléatoire à 4 chiffres
    public static String generateCode() {
        SecureRandom random = new SecureRandom();
        int code = 1000 + random.nextInt(9000); // entre 1000 et 9999
        return String.valueOf(code);
    }
}