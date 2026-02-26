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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Hyperlink forgotPasswordLink;
    @FXML private Hyperlink signupLink;
    @FXML private Button loginButton;
    @FXML private ComboBox<Country> countryCodeCombo;
    @FXML private Button faceLoginButton;

    private UserCRUD userCRUD = new UserCRUD();
    private Map<String, Integer> failedAttempts = new HashMap<>();
    private FaceRecognitionClient faceClient = new FaceRecognitionClient();

    @FXML
    private void handleFaceLogin() {
        // 1. Capturer le visage
        File faceImage = FaceCaptureDialog.captureFace((Stage) loginButton.getScene().getWindow());
        if (faceImage == null) return;

        try {
            // 2. Extraire l'embedding du visage capturé
            List<Double> currentEmbedding = faceClient.extractEmbedding(faceImage);
            if (currentEmbedding == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Aucun visage détecté");
                faceImage.delete();
                return;
            }

            // 3. Récupérer tous les utilisateurs avec embeddings
            List<User> users = userCRUD.afficherAll();
            User bestMatch = null;
            double bestSimilarity = 0.6; // Seuil minimum de similarité

            for (User user : users) {
                if (user.getFaceEmbedding() != null) {
                    List<Double> storedEmbedding = EmbeddingConverter.fromJson(user.getFaceEmbedding());
                    boolean match = faceClient.compareEmbeddings(currentEmbedding, storedEmbedding);
                    if (match) {
                        bestMatch = user;
                        break;
                    }
                }
            }

            faceImage.delete();

            if (bestMatch != null) {
                // 4. Mettre à jour la dernière connexion (IP, localisation)
                try {
                    String ip = userCRUD.getPublicIp() != null ? userCRUD.getPublicIp() : "IP non disponible";
                    String location = userCRUD.getLocationFromIp(ip) != null ? userCRUD.getLocationFromIp(ip) : "Localisation non disponible";
                    userCRUD.updateLastLogin(bestMatch.getId(), ip, location);
                } catch (Exception e) {
                    e.printStackTrace(); // Ne pas bloquer la connexion
                }

                // 5. Stocker l'utilisateur en session
                UserSession.getInstance().setCurrentUser(bestMatch);

                // 6. Apprentissage incrémental
                trainIncremental(bestMatch, currentEmbedding);

                // 7. Redirection selon le rôle
                redirectUser(bestMatch);
            } else {
                showAlert(Alert.AlertType.ERROR, "Échec", "Visage non reconnu");
            }

        } catch (IOException | SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur communication service IA: " + e.getMessage());
        }
    }

    /**
     * Redirige l'utilisateur selon son rôle
     * ADMIN → DestinationBack.fxml
     * USER → DestinationFront.fxml
     */
    private void redirectUser(User user) throws IOException {
        if ("ADMIN".equals(user.getRole())) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DestinationBack.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Bienvenue " + user.getPrenom() + " (Administrateur) !");
        } else {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DestinationFront.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Bienvenue " + user.getPrenom() + " !");
        }
    }

    /**
     * Apprentissage incrémental : moyenne mobile des embeddings
     */
    private void trainIncremental(User user, List<Double> newEmbedding) {
        try {
            String updatedEmbedding;
            if (user.getFaceEmbedding() == null) {
                updatedEmbedding = EmbeddingConverter.toJson(newEmbedding);
            } else {
                List<Double> oldEmbedding = EmbeddingConverter.fromJson(user.getFaceEmbedding());
                List<Double> updated = new ArrayList<>();
                for (int i = 0; i < oldEmbedding.size(); i++) {
                    updated.add(0.7 * oldEmbedding.get(i) + 0.3 * newEmbedding.get(i));
                }
                updatedEmbedding = EmbeddingConverter.toJson(updated);
            }
            userCRUD.updateFaceEmbedding(user.getId(), updatedEmbedding);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogin() {
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
            // Vérifier si l'email existe
            boolean emailExists = userCRUD.emailExists(email);
            if (!emailExists) {
                showAlert(Alert.AlertType.ERROR, "Email ou mot de passe incorrect",
                        "Email ou mot de passe incorrect.");
                return;
            }

            // Vérifier le mot de passe avec BCrypt
            boolean passwordCorrect = userCRUD.checkPassword(email, password);
            if (passwordCorrect) {
                // Réinitialiser les tentatives
                failedAttempts.remove(email);

                // Vérifier si l'email est vérifié
                if (!userCRUD.isEmailVerified(email)) {
                    showAlert(Alert.AlertType.WARNING, "Email non vérifié",
                            "Veuillez vérifier votre email avant de vous connecter.");
                    goToVerifyEmail(email);
                    return;
                }

                User user = userCRUD.getUserByEmail(email);
                UserSession.getInstance().setCurrentUser(user);

                // Enregistrer la dernière connexion (IP, localisation)
                try {
                    String ip = userCRUD.getPublicIp() != null ? userCRUD.getPublicIp() : "IP non disponible";
                    String location = userCRUD.getLocationFromIp(ip) != null ? userCRUD.getLocationFromIp(ip) : "Localisation non disponible";
                    userCRUD.updateLastLogin(user.getId(), ip, location);
                } catch (Exception e) {
                    e.printStackTrace(); // Ne pas bloquer la connexion
                }

                // Redirection selon le rôle
                redirectUser(user);

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
                        if (photo != null && photo.exists()) {
                            photo.delete();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        showAlert(Alert.AlertType.ERROR, "Erreur",
                                "Impossible de capturer la photo ou d'envoyer l'email : " + e.getMessage());
                    }
                    // Réinitialiser le compteur pour cet email pour éviter de spammer
                    failedAttempts.remove(email);
                } else {
                    showAlert(Alert.AlertType.ERROR, "Email ou mot de passe incorrect",
                            "Email ou mot de passe incorrect. Tentative " + attempts + "/3.");
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