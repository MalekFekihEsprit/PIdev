package Controllers;

import Entities.User;
import Services.UserCRUD;
import Utils.FileUtil;
import Utils.UserSession;
import Utils.ValidationUtils;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AdminUsersController {

    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, Integer> colId;
    @FXML private TableColumn<User, String> colNom, colPrenom, colEmail, colRole, colTelephone, colPhoto;
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
    @FXML private Button filterAllButton, filterAdminsButton, filterUsersButton;
    @FXML private Label lblLastUpdate;
    @FXML private HBox btnDestinations, btnHebergement, btnStats, btnItineraires, btnActivites, btnVoyages, btnBudgets;
    @FXML private HBox userProfileBox;
    @FXML private Label lblUserName, lblUserRole;

    private UserCRUD userCRUD = new UserCRUD();
    private ObservableList<User> userList = FXCollections.observableArrayList();
    private FilteredList<User> filteredData;
    private User selectedUser = null;

    @FXML
    public void initialize() {
        setupTableColumns();
        setupActionsColumn();
        setupComboBox();
        loadUsers();
        setupSearchFilter();
        updateStats();
        updateFilterButtonAppearance("all");
        setupNavigationButtons();
        setupUserProfile();
        updateUserInfo();
        updateLastUpdateTime();
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colDateNaissance.setCellValueFactory(new PropertyValueFactory<>("dateNaissance"));
        colTelephone.setCellValueFactory(new PropertyValueFactory<>("telephone"));

        colPhoto.setCellFactory(column -> new TableCell<User, String>() {
            private final ImageView imageView = new ImageView();
            @Override
            protected void updateItem(String fileName, boolean empty) {
                super.updateItem(fileName, empty);
                if (empty || fileName == null) {
                    setGraphic(null);
                } else {
                    File file = FileUtil.getImageFile(fileName);
                    if (file != null && file.exists()) {
                        Image image = new Image(file.toURI().toString(), 30, 30, true, true);
                        imageView.setImage(image);
                        setGraphic(imageView);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });
    }

    private void setupActionsColumn() {
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
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void setupComboBox() {
        roleCombo.getItems().addAll("USER", "ADMIN");
        roleCombo.setValue("USER");
    }

    private void loadUsers() {
        try {
            userList.setAll(userCRUD.afficherAll());
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les utilisateurs : " + e.getMessage());
        }
    }

    private void setupSearchFilter() {
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

    private void updateUserInfo() {
        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser != null) {
            lblUserName.setText(currentUser.getPrenom() + " " + currentUser.getNom());
            lblUserRole.setText(currentUser.getRole());
        } else {
            lblUserName.setText("Utilisateur");
            lblUserRole.setText("Non connecté");
        }
    }

    private void updateLastUpdateTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");
        lblLastUpdate.setText("Dernière mise à jour: " + LocalDateTime.now().format(formatter));
    }

    private void setupNavigationButtons() {
        setupSidebarButtonHover(btnDestinations, "🌍", "Destinations");
        if (btnDestinations != null) btnDestinations.setOnMouseClicked(event -> navigateToDestinations());

        setupSidebarButtonHover(btnHebergement, "🏨", "Hébergement");
        if (btnHebergement != null) btnHebergement.setOnMouseClicked(event -> navigateToHebergement());

        setupSidebarButtonHover(btnStats, "📊", "Statistiques");
        if (btnStats != null) btnStats.setOnMouseClicked(event -> navigateToStats());

        setupSidebarButtonHover(btnItineraires, "🗺️", "Itinéraires");
        if (btnItineraires != null) btnItineraires.setOnMouseClicked(event ->
                showInfoAlert("Itinéraires", "Cette fonctionnalité sera bientôt disponible"));

        setupSidebarButtonHover(btnActivites, "🏄", "Activités");
        if (btnActivites != null) btnActivites.setOnMouseClicked(event ->
                showInfoAlert("Activités", "Cette fonctionnalité sera bientôt disponible"));

        setupSidebarButtonHover(btnVoyages, "✈️", "Voyages");
        if (btnVoyages != null) btnVoyages.setOnMouseClicked(event ->
                showInfoAlert("Voyages", "Cette fonctionnalité sera bientôt disponible"));

        setupSidebarButtonHover(btnBudgets, "💰", "Budgets");
        if (btnBudgets != null) btnBudgets.setOnMouseClicked(event ->
                showInfoAlert("Budgets", "Cette fonctionnalité sera bientôt disponible"));
    }

    private void setupSidebarButtonHover(HBox button, String icon, String text) {
        if (button == null) return;

        button.setOnMouseEntered(event -> {
            button.setStyle("-fx-background-color: rgba(255,140,66,0.15); -fx-background-radius: 12; -fx-padding: 12 16; -fx-cursor: hand; -fx-border-color: #ff8c42; -fx-border-width: 1; -fx-border-radius: 12;");
            button.lookupAll(".label").forEach(label -> {
                if (label instanceof Label) {
                    Label lbl = (Label) label;
                    if (lbl.getText().equals(icon)) {
                        lbl.setStyle("-fx-font-size: 16;");
                    } else {
                        lbl.setStyle("-fx-text-fill: #ff8c42; -fx-font-weight: 600; -fx-font-size: 14;");
                    }
                }
            });
        });

        button.setOnMouseExited(event -> {
            button.setStyle("-fx-background-color: transparent; -fx-background-radius: 12; -fx-padding: 12 16; -fx-cursor: hand;");
            button.lookupAll(".label").forEach(label -> {
                if (label instanceof Label) {
                    Label lbl = (Label) label;
                    if (lbl.getText().equals(icon)) {
                        lbl.setStyle("-fx-font-size: 16;");
                    } else {
                        lbl.setStyle("-fx-text-fill: #94a3b8; -fx-font-weight: 500; -fx-font-size: 14;");
                    }
                }
            });
        });
    }

    private void setupUserProfile() {
        if (userProfileBox != null) {
            userProfileBox.setOnMouseClicked(event -> navigateToProfile());
            userProfileBox.setOnMouseEntered(event ->
                    userProfileBox.setStyle("-fx-background-color: #2d3759; -fx-background-radius: 25; -fx-padding: 6 16 6 6; -fx-cursor: hand;"));
            userProfileBox.setOnMouseExited(event ->
                    userProfileBox.setStyle("-fx-background-color: #1e2749; -fx-background-radius: 25; -fx-padding: 6 16 6 6; -fx-cursor: hand;"));
        }
    }

    private void navigateToDestinations() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DestinationBack.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnDestinations.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TravelMate - Gestion des Destinations");
            stage.setMaximized(true);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir les destinations: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void navigateToHebergement() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/HebergementBack.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnHebergement.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TravelMate - Gestion des Hébergements");
            stage.setMaximized(true);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la gestion des hébergements: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void navigateToStats() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin_stats.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnStats.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TravelMate - Statistiques");
            stage.setMaximized(true);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir les statistiques: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void navigateToProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/profile.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) userProfileBox.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TravelMate - Mon Profil");
            stage.setMaximized(true);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le profil: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void filterAll() {
        filteredData.setPredicate(user -> true);
        updateFilterButtonAppearance("all");
    }

    @FXML
    private void filterAdmins() {
        filteredData.setPredicate(user -> "ADMIN".equals(user.getRole()));
        updateFilterButtonAppearance("admins");
    }

    @FXML
    private void filterUsers() {
        filteredData.setPredicate(user -> "USER".equals(user.getRole()));
        updateFilterButtonAppearance("users");
    }

    private void updateFilterButtonAppearance(String active) {
        String activeStyle = "-fx-background-color: rgba(255,140,66,0.15); -fx-text-fill: #ff8c42; -fx-background-radius: 16; -fx-padding: 4 12; -fx-font-size: 11; -fx-cursor: hand; -fx-border-color: #ff8c42; -fx-border-width: 1; -fx-border-radius: 16;";
        String inactiveStyle = "-fx-background-color: #1e2749; -fx-text-fill: #94a3b8; -fx-background-radius: 16; -fx-padding: 4 12; -fx-font-size: 11; -fx-cursor: hand; -fx-border-color: transparent; -fx-border-width: 1; -fx-border-radius: 16;";

        filterAllButton.setStyle("all".equals(active) ? activeStyle : inactiveStyle);
        filterAdminsButton.setStyle("admins".equals(active) ? activeStyle : inactiveStyle);
        filterUsersButton.setStyle("users".equals(active) ? activeStyle : inactiveStyle);
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
        passwordField.clear();
        confirmPasswordField.clear();
        passwordField.setDisable(false);
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
        user.setMotDePasse(passwordField.getText());
        user.setRole(roleCombo.getValue());
        user.setPhotoUrl(photoUrlField.getText().trim().isEmpty() ? null : photoUrlField.getText().trim());

        try {
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
            userCRUD.modifier(selectedUser, false);
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
    private void exportToPDF() {
        try {
            List<User> users = userTable.getItems();
            if (users.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Aucune donnée", "Aucun utilisateur à exporter.");
                return;
            }

            PdfWriter writer = new PdfWriter("utilisateurs.pdf");
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);
            document.add(new Paragraph("Liste des utilisateurs").setFontSize(18).setBold());

            Table table = new Table(new float[]{1, 3, 3, 5, 2});
            table.addHeaderCell("ID");
            table.addHeaderCell("Nom");
            table.addHeaderCell("Prénom");
            table.addHeaderCell("Email");
            table.addHeaderCell("Rôle");

            for (User u : users) {
                table.addCell(String.valueOf(u.getId()));
                table.addCell(u.getNom());
                table.addCell(u.getPrenom());
                table.addCell(u.getEmail());
                table.addCell(u.getRole());
            }
            document.add(table);
            document.close();
            showAlert(Alert.AlertType.INFORMATION, "Export réussi", "PDF généré : utilisateurs.pdf (" + users.size() + " utilisateurs)");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de l'export : " + e.getMessage());
        }
    }

    @FXML
    private void goToStats() {
        navigateToStats();
    }

    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}