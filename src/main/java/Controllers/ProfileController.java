package Controllers;

import Entities.User;
import Services.UserCRUD;
import Utils.ValidationUtils;
import Utils.UserSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;

public class ProfileController {

    @FXML private Label userNameLabel, fullNameLabel, emailLabel, avatarLabel;
    @FXML private TextField nomField, prenomField, emailField, telephoneField, photoUrlField;
    @FXML private DatePicker dateNaissancePicker;
    @FXML private PasswordField newPasswordField, confirmPasswordField;
    @FXML private VBox passwordSection;
    @FXML private javafx.scene.layout.HBox editButtons;
    @FXML private Button editModeButton, saveButton, cancelButton, deleteButton;
    @FXML private Hyperlink logoutLink;
    @FXML private Label statsVoyages, statsDepenses;
    @FXML private VBox headerAvatarContainer;
    @FXML private Label headerAvatarLabel;
    @FXML private VBox avatarContainer;

    private UserCRUD userCRUD = new UserCRUD();
    private User currentUser;
    private boolean editMode = false;

    private void loadUserAvatar(User user) {

        avatarContainer.getChildren().clear();

        if (user.getPhotoUrl() != null && !user.getPhotoUrl().isEmpty()) {
            try {

                // Anti-cache
                String imageUrl = user.getPhotoUrl() + "?v=" + System.currentTimeMillis();

                Image image = new Image(imageUrl, 64, 64, true, true, false);
                ImageView imageView = new ImageView(image);
                imageView.setFitWidth(64);
                imageView.setFitHeight(64);
                imageView.setPreserveRatio(true);

                avatarContainer.getChildren().add(imageView);

            } catch (Exception e) {
                avatarLabel.setText("👤");
                avatarContainer.getChildren().add(avatarLabel);
            }
        } else {
            avatarLabel.setText("👤");
            avatarContainer.getChildren().add(avatarLabel);
        }
    }


    
    @FXML
    public void initialize() {
        // Récupérer l'utilisateur connecté depuis la session
        if (currentUser == null) {
            currentUser = UserSession.getInstance().getCurrentUser();
        }
        if (currentUser == null) {
            // Si pas d'utilisateur, rediriger vers login
            goToLogin();
            return;
        }
        loadUserData();
        loadUserAvatar(currentUser);
        // Initialiser les statistiques (pour l'instant à 0)
        statsVoyages.setText("0");
        statsDepenses.setText("0 €");
    }


    private void loadUserData() {
        // Afficher les infos dans les labels
        fullNameLabel.setText(currentUser.getPrenom() + " " + currentUser.getNom());
        emailLabel.setText(currentUser.getEmail());
        userNameLabel.setText(currentUser.getPrenom() + " " + currentUser.getNom());

        // Remplir les champs du formulaire
        nomField.setText(currentUser.getNom());
        prenomField.setText(currentUser.getPrenom());
        emailField.setText(currentUser.getEmail());
        telephoneField.setText(currentUser.getTelephone() != null ? currentUser.getTelephone() : "");
        dateNaissancePicker.setValue(currentUser.getDateNaissance());
        photoUrlField.setText(currentUser.getPhotoUrl() != null ? currentUser.getPhotoUrl() : "");
        
        // Load avatar with user data
        loadUserAvatar(currentUser);
    }

    /**
     * Allows the login/redirection logic to supply the user before initialize() is called.
     * This is optional since the controller already reads from the session on initialize().
     */
    public void setUser(User user) {
        this.currentUser = user;
        if (user != null) {
            // update the UI immediately if initialization already happened
            loadUserData();
            loadUserAvatar(currentUser);
            statsVoyages.setText("0");
            statsDepenses.setText("0 €");
        }
    }

    @FXML
    private void toggleEditMode() { // Basculer entre mode consultation et édition, en affichant ou cachant les champs de mot de passe et les boutons de sauvegarde/annulation
        editMode = !editMode;
        setEditable(editMode);
        editModeButton.setText(editMode ? "🔍 Mode consultation" : "✏️Modifier");
        passwordSection.setVisible(editMode);
        passwordSection.setManaged(editMode);
        editButtons.setVisible(editMode);
        editButtons.setManaged(editMode);
    }

    private void setEditable(boolean editable) {
        nomField.setEditable(editable);
        prenomField.setEditable(editable);
        // email non modifiable (identifiant)
        telephoneField.setEditable(editable);
        dateNaissancePicker.setEditable(editable);
        photoUrlField.setEditable(editable);
    }

    @FXML
    private void handleSave() {
        // Récupérer les nouvelles valeurs
        String nom = nomField.getText().trim();
        String prenom = prenomField.getText().trim();
        String telephone = telephoneField.getText().trim();
        LocalDate dateNaissance = dateNaissancePicker.getValue();
        String photoUrl = photoUrlField.getText().trim();
        String newPassword = newPasswordField.getText();
        String confirm = confirmPasswordField.getText();

        // Validations
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
        if (!telephone.isEmpty() && !ValidationUtils.isValidPhone(telephone)) {
            showAlert(Alert.AlertType.ERROR, "Validation", "Le téléphone doit contenir 8 à 15 chiffres.");
            return;
        }

        // Mise à jour de l'objet utilisateur
        currentUser.setNom(nom);
        currentUser.setPrenom(prenom);
        currentUser.setTelephone(telephone.isEmpty() ? null : telephone);
        currentUser.setDateNaissance(dateNaissance);
        currentUser.setPhotoUrl(photoUrl.isEmpty() ? null : photoUrl);

        try {
            // Si un nouveau mot de passe est fourni, le valider et le mettre à jour
            if (!newPassword.isEmpty()) {
                if (!ValidationUtils.isPasswordValid(newPassword)) {
                    showAlert(Alert.AlertType.ERROR, "Validation", "Le mot de passe doit contenir au moins 6 caractères.");
                    return;
                }
                if (!ValidationUtils.passwordsMatch(newPassword, confirm)) {
                    showAlert(Alert.AlertType.ERROR, "Validation", "Les mots de passe ne correspondent pas.");
                    return;
                }
                currentUser.setMotDePasse(newPassword); // À hasher plus tard
            }

            // Appeler la méthode modifier du CRUD
            userCRUD.modifier(currentUser);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Profil mis à jour avec succès.");

            // Recharger l'affichage et quitter le mode édition
            loadUserData();
            loadUserAvatar(currentUser);
            toggleEditMode(); // repasse en mode consultation
            newPasswordField.clear();
            confirmPasswordField.clear();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de base de données : " + e.getMessage());
        }
    }

    @FXML
    private void cancelEdit() {
        // Remettre les valeurs initiales et quitter le mode édition
        loadUserData();
        loadUserAvatar(currentUser);
        newPasswordField.clear();
        confirmPasswordField.clear();
        if (editMode) toggleEditMode();
    }

    @FXML
    private void handleDeleteAccount() { // Gérer la suppression du compte après confirmation de l'utilisateur, en appelant la méthode supprimer du CRUD et en redirigeant vers login
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Suppression du compte");
        confirm.setHeaderText("Êtes-vous sûr de vouloir supprimer votre compte ?");
        confirm.setContentText("Cette action est irréversible et toutes vos données seront perdues.");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    userCRUD.supprimer(currentUser.getId());
                    showAlert(Alert.AlertType.INFORMATION, "Compte supprimé", "Votre compte a été supprimé. Au revoir.");
                    UserSession.getInstance().clear(); // déconnexion
                    goToLogin();
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la suppression : " + e.getMessage());
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

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}