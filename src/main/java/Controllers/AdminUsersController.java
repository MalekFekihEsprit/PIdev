package Controllers;

import Entities.User;
import Services.UserCRUD;
import Utils.ValidationUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;

public class AdminUsersController {

    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, Integer> colId;
    @FXML private TableColumn<User, String> colNom, colPrenom, colEmail, colRole, colTelephone;
    @FXML private TableColumn<User, LocalDate> colDateNaissance;
    @FXML private TableColumn<User, Void> colActions;

    @FXML private TextField searchField;
    @FXML private TextField nomField, prenomField, emailField, telephoneField, photoUrlField;
    @FXML private PasswordField passwordField, confirmPasswordField;
    @FXML private DatePicker dateNaissancePicker;
    @FXML private ComboBox<String> roleCombo;
    @FXML private Label formTitle;
    @FXML private Button saveButton, updateButton, deleteButton, clearButton;
    @FXML private Label totalUsersLabel, sidebarTotalLabel, statsAdmins, statsUsers;

    private UserCRUD userCRUD = new UserCRUD();
    private ObservableList<User> userList = FXCollections.observableArrayList();
    private FilteredList<User> filteredData;
    private User selectedUser = null; // pour l'édition

    @FXML
    public void initialize() {
        // Configurer les colonnes
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colDateNaissance.setCellValueFactory(new PropertyValueFactory<>("dateNaissance"));
        colTelephone.setCellValueFactory(new PropertyValueFactory<>("telephone"));

        // Colonne Actions avec boutons
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button("✏️");
            private final Button deleteButton = new Button("🗑");
            private final HBox pane = new HBox(5, editButton, deleteButton);

            {
                editButton.setStyle("-fx-background-color: #ff8c42; -fx-text-fill: white; -fx-background-radius: 8; -fx-cursor: hand;");
                deleteButton.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-background-radius: 8; -fx-cursor: hand;");
                editButton.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    loadUserForEdit(user);
                });
                deleteButton.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    handleDeleteUser(user);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(pane);
                }
            }
        });

        // Remplir la combo des rôles
        roleCombo.getItems().addAll("USER", "ADMIN");
        roleCombo.setValue("USER");

        // Charger les données
        loadUsers();

        // Mettre en place le filtre de recherche
        filteredData = new FilteredList<>(userList, p -> true);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(user -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lowerCaseFilter = newValue.toLowerCase();
                return user.getNom().toLowerCase().contains(lowerCaseFilter)
                        || user.getPrenom().toLowerCase().contains(lowerCaseFilter)
                        || user.getEmail().toLowerCase().contains(lowerCaseFilter)
                        || (user.getTelephone() != null && user.getTelephone().contains(lowerCaseFilter));
            });
        });

        SortedList<User> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(userTable.comparatorProperty());
        userTable.setItems(sortedData);

        // Mise à jour des compteurs
        updateStats();
    }

    private void loadUsers() {
        try {
            userList.setAll(userCRUD.afficherAll());
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les utilisateurs : " + e.getMessage());
        }
    }

    private void updateStats() {
        int total = userList.size();
        totalUsersLabel.setText(total + " utilisateurs");
        sidebarTotalLabel.setText(String.valueOf(total));
        long adminCount = userList.stream().filter(u -> "ADMIN".equals(u.getRole())).count();
        long userCount = userList.stream().filter(u -> "USER".equals(u.getRole())).count();
        statsAdmins.setText(String.valueOf(adminCount));
        statsUsers.setText(String.valueOf(userCount));
    }

    @FXML
    private void handleAddNew() {
        clearForm();
        selectedUser = null;
        formTitle.setText("Ajouter un utilisateur");
        saveButton.setDisable(false);
        updateButton.setDisable(true);
        deleteButton.setDisable(true);
        passwordField.setDisable(false);
        confirmPasswordField.setDisable(false);
    }

    private void loadUserForEdit(User user) {
        selectedUser = user;
        formTitle.setText("Modifier l'utilisateur #" + user.getId());
        nomField.setText(user.getNom());
        prenomField.setText(user.getPrenom());
        emailField.setText(user.getEmail());
        telephoneField.setText(user.getTelephone() != null ? user.getTelephone() : "");
        dateNaissancePicker.setValue(user.getDateNaissance());
        roleCombo.setValue(user.getRole());
        photoUrlField.setText(user.getPhotoUrl() != null ? user.getPhotoUrl() : "");
        // Pour la modification, on désactive le mot de passe (on peut le changer via un champ dédié, mais on laisse vide)
        passwordField.clear();
        confirmPasswordField.clear();
        passwordField.setDisable(false); // on peut permettre de changer le mot de passe
        confirmPasswordField.setDisable(false);
        saveButton.setDisable(true);
        updateButton.setDisable(false);
        deleteButton.setDisable(false);
    }

    @FXML
    private void handleSave() {
        if (!validateInputs(true)) return;

        User user = new User();
        user.setNom(nomField.getText().trim());
        user.setPrenom(prenomField.getText().trim());
        user.setEmail(emailField.getText().trim());
        user.setTelephone(telephoneField.getText().trim().isEmpty() ? null : telephoneField.getText().trim());
        user.setDateNaissance(dateNaissancePicker.getValue());
        user.setMotDePasse(passwordField.getText()); // À hasher
        user.setRole(roleCombo.getValue());
        user.setPhotoUrl(photoUrlField.getText().trim().isEmpty() ? null : photoUrlField.getText().trim());

        try {
            // Vérifier si l'email existe déjà
            if (userCRUD.emailExists(user.getEmail())) {
                showAlert(Alert.AlertType.ERROR, "Email existant", "Cet email est déjà utilisé.");
                return;
            }
            userCRUD.ajouter(user);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Utilisateur ajouté avec succès.");
            loadUsers();
            updateStats();
            clearForm();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de l'ajout : " + e.getMessage());
        }
    }

    @FXML
    private void handleUpdate() {
        if (selectedUser == null) return;
        if (!validateInputs(false)) return;

        selectedUser.setNom(nomField.getText().trim());
        selectedUser.setPrenom(prenomField.getText().trim());
        selectedUser.setEmail(emailField.getText().trim());
        selectedUser.setTelephone(telephoneField.getText().trim().isEmpty() ? null : telephoneField.getText().trim());
        selectedUser.setDateNaissance(dateNaissancePicker.getValue());
        selectedUser.setRole(roleCombo.getValue());
        selectedUser.setPhotoUrl(photoUrlField.getText().trim().isEmpty() ? null : photoUrlField.getText().trim());

        // Si un nouveau mot de passe est fourni, le mettre à jour
        String newPwd = passwordField.getText();
        if (!newPwd.isEmpty()) {
            if (!ValidationUtils.isPasswordValid(newPwd)) {
                showAlert(Alert.AlertType.ERROR, "Validation", "Le mot de passe doit contenir au moins 6 caractères.");
                return;
            }
            if (!newPwd.equals(confirmPasswordField.getText())) {
                showAlert(Alert.AlertType.ERROR, "Validation", "Les mots de passe ne correspondent pas.");
                return;
            }
            selectedUser.setMotDePasse(newPwd);
        }

        try {
            userCRUD.modifier(selectedUser);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Utilisateur modifié avec succès.");
            loadUsers();
            updateStats();
            clearForm();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la modification : " + e.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        if (selectedUser == null) return;
        handleDeleteUser(selectedUser);
    }

    private void handleDeleteUser(User user) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer l'utilisateur " + user.getEmail() + " ?");
        confirm.setContentText("Cette action est irréversible.");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    userCRUD.supprimer(user.getId());
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Utilisateur supprimé.");
                    loadUsers();
                    updateStats();
                    clearForm();
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la suppression : " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleClear() {
        clearForm();
    }

    private void clearForm() {
        nomField.clear();
        prenomField.clear();
        emailField.clear();
        telephoneField.clear();
        dateNaissancePicker.setValue(null);
        passwordField.clear();
        confirmPasswordField.clear();
        photoUrlField.clear();
        roleCombo.setValue("USER");
        selectedUser = null;
        formTitle.setText("Ajouter un utilisateur");
        saveButton.setDisable(false);
        updateButton.setDisable(true);
        deleteButton.setDisable(true);
    }

    private boolean validateInputs(boolean isNew) {
        String nom = nomField.getText().trim();
        String prenom = prenomField.getText().trim();
        String email = emailField.getText().trim();
        LocalDate date = dateNaissancePicker.getValue();

        if (!ValidationUtils.isNotEmpty(nom)) {
            showAlert(Alert.AlertType.ERROR, "Validation", "Le nom est obligatoire.");
            return false;
        }
        if (!ValidationUtils.isNotEmpty(prenom)) {
            showAlert(Alert.AlertType.ERROR, "Validation", "Le prénom est obligatoire.");
            return false;
        }
        if (!ValidationUtils.isValidEmail(email)) {
            showAlert(Alert.AlertType.ERROR, "Validation", "L'email n'est pas valide.");
            return false;
        }
        if (date == null) {
            showAlert(Alert.AlertType.ERROR, "Validation", "La date de naissance est obligatoire.");
            return false;
        }
        if (!ValidationUtils.isAdult(date)) {
            showAlert(Alert.AlertType.ERROR, "Validation", "L'utilisateur doit avoir au moins 18 ans.");
            return false;
        }
        String tel = telephoneField.getText().trim();
        if (!tel.isEmpty() && !ValidationUtils.isValidPhone(tel)) {
            showAlert(Alert.AlertType.ERROR, "Validation", "Le téléphone doit contenir 8 à 15 chiffres.");
            return false;
        }
        if (isNew) {
            String pwd = passwordField.getText();
            if (!ValidationUtils.isPasswordValid(pwd)) {
                showAlert(Alert.AlertType.ERROR, "Validation", "Le mot de passe doit contenir au moins 6 caractères.");
                return false;
            }
            if (!pwd.equals(confirmPasswordField.getText())) {
                showAlert(Alert.AlertType.ERROR, "Validation", "Les mots de passe ne correspondent pas.");
                return false;
            }
        }
        return true;
    }

    @FXML
    private void goToStats() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/admin_stats.fxml"));
            Stage stage = (Stage) searchField.getScene().getWindow(); // n'importe quel composant
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