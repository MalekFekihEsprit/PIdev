package Controllers;

import Entities.User;
import Services.UserCRUD;
import Utils.UserSession;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static Controllers.FaceCaptureDialog.showAlert;

public class AdminStatsController {

    @FXML private PieChart rolePieChart;
    @FXML private BarChart<String, Number> inscriptionsBarChart;
    @FXML private Label totalUsersStat, totalAdminsStat, totalUsersOnlyStat;
    @FXML private Label lblLastUpdate;
    @FXML private HBox btnDestinations, btnHebergement, btnUsers, btnItineraires, btnActivites, btnVoyages, btnBudgets,btnCategories;
    @FXML private HBox userProfileBox;
    @FXML private Label lblUserName, lblUserRole;

    private UserCRUD userCRUD = new UserCRUD();

    @FXML
    public void initialize() {
        loadStats();
        setupNavigationButtons();
        setupUserProfile();
        updateUserInfo();
        updateLastUpdateTime();
    }

    private void loadStats() {
        try {
            List<User> users = userCRUD.afficherAll();

            totalUsersStat.setText(String.valueOf(users.size()));
            long adminCount = users.stream().filter(u -> "ADMIN".equals(u.getRole())).count();
            long userCount = users.stream().filter(u -> "USER".equals(u.getRole())).count();
            totalAdminsStat.setText(String.valueOf(adminCount));
            totalUsersOnlyStat.setText(String.valueOf(userCount));

            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
                    new PieChart.Data("Administrateurs", adminCount),
                    new PieChart.Data("Utilisateurs", userCount)
            );
            rolePieChart.setData(pieData);
            rolePieChart.setTitle("Rôles");

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Inscriptions");

            java.util.Map<String, Integer> dailyCount = new java.util.TreeMap<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            for (User u : users) {
                if (u.getCreatedAt() != null) {
                    String day = u.getCreatedAt().format(formatter);
                    dailyCount.put(day, dailyCount.getOrDefault(day, 0) + 1);
                }
            }

            for (java.util.Map.Entry<String, Integer> entry : dailyCount.entrySet()) {
                series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
            }

            inscriptionsBarChart.getData().clear();
            inscriptionsBarChart.getData().add(series);
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
        if (btnDestinations != null) btnDestinations.setOnMouseClicked(event -> navigateTo("/DestinationBack.fxml", "Gestion des Destinations"));

        setupSidebarButtonHover(btnHebergement, "🏨", "Hébergement");
        if (btnHebergement != null) btnHebergement.setOnMouseClicked(event -> navigateTo("/HebergementBack.fxml", "Gestion des Hébergements"));

        setupSidebarButtonHover(btnUsers, "👥", "Utilisateurs");
        if (btnUsers != null) btnUsers.setOnMouseClicked(event -> navigateTo("/fxml/admin_users.fxml", "Gestion des Utilisateurs"));

        setupSidebarButtonHover(btnItineraires, "🗺️", "Itinéraires");
        if (btnItineraires != null) btnItineraires.setOnMouseClicked(event -> navigateTo("/ItineraireEtEtape/PageGestionItineraires.fxml", "Itineraire"));


        setupSidebarButtonHover(btnCategories, "📑", "Catégories");
        if (btnCategories != null) btnCategories.setOnMouseClicked(event -> navigateTo("/categoriesback.fxml", "Gestion des Catégories"));

        setupSidebarButtonHover(btnActivites, "🏄", "Activités");
        if (btnActivites != null) btnActivites.setOnMouseClicked(event -> navigateTo("/activitesback.fxml", "Gestion des Activités"));

        setupSidebarButtonHover(btnVoyages, "✈️", "Voyages");
        if (btnVoyages != null) btnVoyages.setOnMouseClicked(event -> navigateTo("/PageVoyageBack.fxml", "Gestion des Voyages"));

        setupSidebarButtonHover(btnBudgets, "💰", "Budgets");
        if (btnBudgets != null) btnBudgets.setOnMouseClicked(event -> navigateTo("/BudgetDepenseBack.fxml", "Gestion des Budgets"));
    }

    private void navigateTo(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) btnDestinations.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TravelMate - " + title);
            stage.setMaximized(true);
        } catch (IOException e) {

            e.printStackTrace();
        }
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
            e.printStackTrace();
        }
    }

    private void navigateToUsers() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin_users.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnUsers.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TravelMate - Gestion des Utilisateurs");
            stage.setMaximized(true);
        } catch (IOException e) {
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
            e.printStackTrace();
        }
    }

    @FXML
    private void goToUsers() {
        navigateToUsers();
    }

    private void showInfoAlert(String title, String message) {
        // Can be implemented if needed
    }
}