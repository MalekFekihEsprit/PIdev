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
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class AdminStatsController {

    // ========== FXML BINDINGS (Sidebar, user profile, etc.) ==========
    @FXML private PieChart rolePieChart;
    @FXML private PieChart verificationPieChart;          // new
    @FXML private BarChart<String, Number> inscriptionsBarChart;
    @FXML private BarChart<String, Number> ageDistributionBarChart;   // new
    @FXML private BarChart<String, Number> birthYearsBarChart;        // new

    // Labels for KPI cards
    @FXML private Label totalUsersStat, totalAdminsStat, totalUsersOnlyStat;
    @FXML private Label totalUsersCard, adminsCard, usersCard;
    @FXML private Label averageAgeLabel, youngestAgeLabel, oldestAgeLabel;  // new
    @FXML private Label lblLastUpdate;

    // Navigation buttons (unchanged)
    @FXML private HBox btnDestinations, btnHebergement, btnUsers, btnStats, btnItineraires;
    @FXML private HBox btnActivites, btnVoyages, btnBudgets, btnCategories, btnEvenements;
    @FXML private HBox userProfileBox;
    @FXML private Label lblUserName, lblUserRole;

    private UserCRUD userCRUD = new UserCRUD();
    private int currentOffset = 0;   // for 7-day window navigation

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

            // ---- Basic counts ----
            int total = users.size();
            long adminCount = users.stream().filter(u -> "ADMIN".equals(u.getRole())).count();
            long userCount = users.stream().filter(u -> "USER".equals(u.getRole())).count();
            long verifiedCount = users.stream().filter(User::isVerified).count();
            long unverifiedCount = total - verifiedCount;

            // Update sidebar & KPI cards
            totalUsersStat.setText(String.valueOf(total));
            totalAdminsStat.setText(String.valueOf(adminCount));
            totalUsersOnlyStat.setText(String.valueOf(userCount));
            totalUsersCard.setText(String.valueOf(total));
            adminsCard.setText(String.valueOf(adminCount));
            usersCard.setText(String.valueOf(userCount));

            // ---- Pie chart: roles ----
            ObservableList<PieChart.Data> roleData = FXCollections.observableArrayList(
                    new PieChart.Data("Administrateurs", adminCount),
                    new PieChart.Data("Utilisateurs", userCount)
            );
            rolePieChart.setData(roleData);
            rolePieChart.setTitle("Rôles");

            // ---- Pie chart: verification ----
            ObservableList<PieChart.Data> verifData = FXCollections.observableArrayList(
                    new PieChart.Data("Vérifiés", verifiedCount),
                    new PieChart.Data("Non vérifiés", unverifiedCount)
            );
            verificationPieChart.setData(verifData);
            verificationPieChart.setTitle("Vérification");

            // ---- Age statistics ----
            double avgAge = users.stream()
                    .filter(u -> u.getDateNaissance() != null)
                    .mapToInt(u -> computeAge(u.getDateNaissance()))
                    .average()
                    .orElse(0);
            int youngest = users.stream()
                    .filter(u -> u.getDateNaissance() != null)
                    .mapToInt(u -> computeAge(u.getDateNaissance()))
                    .min()
                    .orElse(0);
            int oldest = users.stream()
                    .filter(u -> u.getDateNaissance() != null)
                    .mapToInt(u -> computeAge(u.getDateNaissance()))
                    .max()
                    .orElse(0);

            averageAgeLabel.setText(String.format("%.1f ans", avgAge));
            youngestAgeLabel.setText(youngest + " ans");
            oldestAgeLabel.setText(oldest + " ans");

            // ---- Age distribution (bar chart) ----
            Map<String, Integer> ageGroups = new LinkedHashMap<>();
            ageGroups.put("Moins de 18 ans", 0);
            ageGroups.put("18-25 ans", 0);
            ageGroups.put("26-35 ans", 0);
            ageGroups.put("36-50 ans", 0);
            ageGroups.put("50+ ans", 0);

            for (User u : users) {
                if (u.getDateNaissance() == null) continue;
                int age = computeAge(u.getDateNaissance());
                if (age < 18) ageGroups.put("Moins de 18 ans", ageGroups.get("Moins de 18 ans") + 1);
                else if (age <= 25) ageGroups.put("18-25 ans", ageGroups.get("18-25 ans") + 1);
                else if (age <= 35) ageGroups.put("26-35 ans", ageGroups.get("26-35 ans") + 1);
                else if (age <= 50) ageGroups.put("36-50 ans", ageGroups.get("36-50 ans") + 1);
                else ageGroups.put("50+ ans", ageGroups.get("50+ ans") + 1);
            }

            XYChart.Series<String, Number> ageSeries = new XYChart.Series<>();
            ageSeries.setName("Utilisateurs");
            for (Map.Entry<String, Integer> entry : ageGroups.entrySet()) {
                ageSeries.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
            }
            ageDistributionBarChart.getData().clear();
            ageDistributionBarChart.getData().add(ageSeries);
            ageDistributionBarChart.setTitle("Répartition par tranche d'âge");

            // ---- Birth years distribution (bar chart) ----
            Map<Integer, Integer> birthYearCounts = users.stream()
                    .filter(u -> u.getDateNaissance() != null)
                    .collect(Collectors.groupingBy(
                            u -> u.getDateNaissance().getYear(),
                            Collectors.summingInt(e -> 1)
                    ));
            // Sort by year
            TreeMap<Integer, Integer> sortedYears = new TreeMap<>(birthYearCounts);

            XYChart.Series<String, Number> yearSeries = new XYChart.Series<>();
            yearSeries.setName("Utilisateurs");
            for (Map.Entry<Integer, Integer> entry : sortedYears.entrySet()) {
                yearSeries.getData().add(new XYChart.Data<>(String.valueOf(entry.getKey()), entry.getValue()));
            }
            birthYearsBarChart.getData().clear();
            birthYearsBarChart.getData().add(yearSeries);
            birthYearsBarChart.setTitle("Par année de naissance");

            // ---- 7-day registrations window (with offset navigation) ----
            updateWindowedRegistrations(users);

        } catch (SQLException e) {
            e.printStackTrace();
            showErrorAlert("Erreur", "Impossible de charger les statistiques : " + e.getMessage());
        }
    }

    private void updateWindowedRegistrations(List<User> users) {
        // Calculate date range based on currentOffset (0 = current week, 7 = previous week, -7 = next week)
        LocalDate endDate = LocalDate.now().minusDays(currentOffset);
        LocalDate startDate = endDate.minusDays(6);

        Map<String, Integer> dailyCount = new TreeMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");

        for (User u : users) {
            if (u.getCreatedAt() == null) continue;
            LocalDate createdDate = u.getCreatedAt().toLocalDate();
            if ((createdDate.isEqual(startDate) || createdDate.isAfter(startDate)) &&
                    (createdDate.isEqual(endDate) || createdDate.isBefore(endDate))) {
                String label = createdDate.format(formatter);
                dailyCount.put(label, dailyCount.getOrDefault(label, 0) + 1);
            }
        }

        // Fill missing days
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            String label = current.format(formatter);
            dailyCount.putIfAbsent(label, 0);
            current = current.plusDays(1);
        }

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Inscriptions");
        for (Map.Entry<String, Integer> entry : dailyCount.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        inscriptionsBarChart.getData().clear();
        inscriptionsBarChart.getData().add(series);
        inscriptionsBarChart.setTitle("Inscriptions du " + startDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                + " au " + endDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
    }

    private int computeAge(LocalDate birthDate) {
        return (int) ChronoUnit.YEARS.between(birthDate, LocalDate.now());
    }

    // ========== Navigation (unchanged from your original) ==========
    private void setupNavigationButtons() {
        // ... keep your existing navigation setup ...
        // (I'm omitting for brevity, but you already have it)
        // Just make sure to call the correct methods for each button.
    }

    private void navigateTo(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) btnDestinations.getScene().getWindow();
            stage.setScene(new Scene(root, Screen.getPrimary().getVisualBounds().getWidth(), Screen.getPrimary().getVisualBounds().getHeight()));
            stage.setTitle("TravelMate - " + title);
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Example for navigation buttons (adapt to your existing ones)
    private void setupSidebarButtonHover(HBox button, String icon, String text) { /* keep your implementation */ }
    private void setupUserProfile() { /* keep your implementation */ }
    private void updateUserInfo() { /* keep your implementation */ }
    private void updateLastUpdateTime() { /* keep your implementation */ }
    private void showErrorAlert(String title, String message) { /* keep your implementation */ }

    // Button actions for week navigation
    @FXML
    private void goPrevWeek() {
        currentOffset += 7;
        try {
            List<User> users = userCRUD.afficherAll();
            updateWindowedRegistrations(users);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goNextWeek() {
        if (currentOffset >= 7) {
            currentOffset -= 7;
            try {
                List<User> users = userCRUD.afficherAll();
                updateWindowedRegistrations(users);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}