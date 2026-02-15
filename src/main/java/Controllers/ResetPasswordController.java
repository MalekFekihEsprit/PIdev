package Controllers;

import Services.UserCRUD;
import Utils.ValidationUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class ResetPasswordController {

    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button resetButton;
    @FXML private Hyperlink backToLoginLink;

    private UserCRUD userCRUD = new UserCRUD();
    private String email; // à passer depuis ForgotPasswordController

    public void setEmail(String email) {
        this.email = email;
    }

    @FXML
    private void handleReset() {
        String newPwd = newPasswordField.getText();
        String confirm = confirmPasswordField.getText();

        if (!ValidationUtils.isPasswordValid(newPwd)) {
            showAlert(Alert.AlertType.ERROR, "Validation", "Le mot de passe doit contenir au moins 6 caractères.");
            return;
        }
        if (!ValidationUtils.passwordsMatch(newPwd, confirm)) {
            showAlert(Alert.AlertType.ERROR, "Validation", "Les mots de passe ne correspondent pas.");
            return;
        }

        try {
            // Mettre à jour le mot de passe dans la base
            boolean updated = userCRUD.updatePassword(email, newPwd);
            if (updated) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Votre mot de passe a été mis à jour. Vous pouvez maintenant vous connecter.");
                goToLogin();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "La mise à jour a échoué. Veuillez réessayer.");
            }
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

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}