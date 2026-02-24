package Controllers;

import Entities.User;
import Services.UserCRUD;
import Utils.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.mail.MessagingException;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Random;

public class ProfileController {

    @FXML private Label userNameLabel, fullNameLabel, emailLabel;
    @FXML private ImageView profileImageView;
    @FXML private TextField nomField, prenomField, emailField, telephoneField, photoUrlField;
    @FXML private DatePicker dateNaissancePicker;
    @FXML private PasswordField newPasswordField, confirmPasswordField;
    @FXML private VBox passwordSection;
    @FXML private HBox editButtons;
    @FXML private Button editModeButton, saveButton, cancelButton, deleteButton, uploadPhotoButton;
    @FXML private Hyperlink logoutLink;
    @FXML private Label statsVoyages, statsDepenses;

    private File selectedImageFile;
    private UserCRUD userCRUD = new UserCRUD();
    private User currentUser;
    private boolean editMode = false;

    @FXML
    public void initialize() {
        currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser == null) {
            goToLogin();
            return;
        }
        loadUserData();
        loadAvatar();
        statsVoyages.setText("0");
        statsDepenses.setText("0 €");
    }

    public void setUser(User user) {
        this.currentUser = user;
        if (user != null) {
            loadUserData();
            loadAvatar();
            statsVoyages.setText("0");
            statsDepenses.setText("0 €");
        }
    }

    private void loadUserData() {
        fullNameLabel.setText(currentUser.getPrenom() + " " + currentUser.getNom());
        emailLabel.setText(currentUser.getEmail());
        userNameLabel.setText(currentUser.getPrenom() + " " + currentUser.getNom());

        nomField.setText(currentUser.getNom());
        prenomField.setText(currentUser.getPrenom());
        emailField.setText(currentUser.getEmail());
        telephoneField.setText(currentUser.getTelephone() != null ? currentUser.getTelephone() : "");
        dateNaissancePicker.setValue(currentUser.getDateNaissance());
        photoUrlField.setText(currentUser.getPhotoUrl() != null ? currentUser.getPhotoUrl() : "");
    }

    private void loadAvatar() {
        Image image = null;

        // 1. Si l'utilisateur a uploadé une photo locale
        if (currentUser.getPhotoFileName() != null) {
            File file = FileUtil.getImageFile(currentUser.getPhotoFileName());
            if (file != null && file.exists()) {
                image = new Image(file.toURI().toString(), 64, 64, true, true);
            }
        }

        // 2. Sinon, si une URL est fournie
        if (image == null && currentUser.getPhotoUrl() != null && !currentUser.getPhotoUrl().isEmpty()) {
            try {
                image = new Image(currentUser.getPhotoUrl(), 64, 64, true, true);
            } catch (Exception e) {
                System.err.println("Erreur chargement URL : " + e.getMessage());
            }
        }

        // 3. Sinon, essayer Gravatar
        if (image == null) {
            String gravatarUrl = GravatarUtil.getGravatarUrl(currentUser.getEmail(), 64);
            try {
                image = new Image(gravatarUrl, 64, 64, true, true);
            } catch (Exception e) {
                System.err.println("Erreur Gravatar : " + e.getMessage());
            }
        }

        // 4. Si tout échoue, on laisse l'ImageView vide (on pourrait mettre un emoji, mais l'ImageView ne gère pas le texte)
        // Pour l'emoji, il faudrait un Label à côté, mais on peut se contenter d'une image par défaut.
        // On utilise une image locale si disponible, sinon l'ImageView reste vide.
        if (image != null && !image.isError()) {
            profileImageView.setImage(image);
        } else {
            // Option : afficher un placeholder (ex: icône par défaut)
            profileImageView.setImage(null); // ou une image par défaut si tu en as une
        }
    }

    @FXML
    private void handleUploadPhoto() {
        Stage stage = (Stage) uploadPhotoButton.getScene().getWindow();
        File file = FileUtil.selectImageFile(stage);
        if (file != null) {
            Image preview = new Image(file.toURI().toString(), 64, 64, true, true);
            profileImageView.setImage(preview);
            selectedImageFile = file;
        }
    }

    @FXML
    private void toggleEditMode() {
        editMode = !editMode;
        setEditable(editMode);
        editModeButton.setText(editMode ? "🔍 Mode consultation" : "✏️ Modifier");
        passwordSection.setVisible(editMode);
        passwordSection.setManaged(editMode);
        editButtons.setVisible(editMode);
        editButtons.setManaged(editMode);
    }

    private void setEditable(boolean editable) {
        nomField.setEditable(editable);
        prenomField.setEditable(editable);
        telephoneField.setEditable(editable);
        dateNaissancePicker.setEditable(editable);
        photoUrlField.setEditable(editable);
        // email non modifiable
    }

    @FXML
    private void handleSave() {
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

        // Gestion de l'upload de photo
        if (selectedImageFile != null) {
            try {
                if (currentUser.getPhotoFileName() != null) {
                    File oldFile = FileUtil.getImageFile(currentUser.getPhotoFileName());
                    if (oldFile != null && oldFile.exists()) oldFile.delete();
                }
                String fileName = FileUtil.saveImageToUploads(selectedImageFile, currentUser.getId());
                currentUser.setPhotoFileName(fileName);
                currentUser.setPhotoUrl(null); // on abandonne l'URL
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de sauvegarder l'image : " + e.getMessage());
                return;
            }
        } else if (!photoUrl.isEmpty()) {
            // Si l'utilisateur a saisi une URL, on l'utilise
            currentUser.setPhotoUrl(photoUrl);
            currentUser.setPhotoFileName(null);
        } else {
            // Pas de nouvelle photo, on garde l'existante
        }

        // Mise à jour des champs texte
        currentUser.setNom(nom);
        currentUser.setPrenom(prenom);
        currentUser.setTelephone(telephone.isEmpty() ? null : telephone);
        currentUser.setDateNaissance(dateNaissance);

        try {
            if (!newPassword.isEmpty()) {
                if (!ValidationUtils.isPasswordValid(newPassword)) {
                    showAlert(Alert.AlertType.ERROR, "Validation", "Le mot de passe doit contenir au moins 6 caractères.");
                    return;
                }
                if (!ValidationUtils.passwordsMatch(newPassword, confirm)) {
                    showAlert(Alert.AlertType.ERROR, "Validation", "Les mots de passe ne correspondent pas.");
                    return;
                }
                currentUser.setMotDePasse(newPassword);
            }

            userCRUD.modifier(currentUser);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Profil mis à jour.");

            // Recharger l'affichage
            loadUserData();
            loadAvatar();
            toggleEditMode();
            newPasswordField.clear();
            confirmPasswordField.clear();
            selectedImageFile = null;
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur base de données : " + e.getMessage());
        }
    }

    @FXML
    private void cancelEdit() {
        loadUserData();
        loadAvatar();
        newPasswordField.clear();
        confirmPasswordField.clear();
        if (editMode) toggleEditMode();
        selectedImageFile = null;
    }

    @FXML
    private void handleDeleteAccount() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Suppression du compte");
        confirm.setHeaderText("Êtes-vous sûr de vouloir supprimer votre compte ?");
        confirm.setContentText("Cette action est irréversible.");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    if (currentUser.getPhotoFileName() != null) {
                        File photo = FileUtil.getImageFile(currentUser.getPhotoFileName());
                        if (photo != null && photo.exists()) photo.delete();
                    }
                    userCRUD.supprimer(currentUser.getId());
                    UserSession.getInstance().clear();
                    goToLogin();
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la suppression.");
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