package Controllers;

import Entities.User;
import Services.UserCRUD;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class AdminStatsController {

    @FXML private PieChart rolePieChart;
    @FXML private BarChart<String, Number> inscriptionsBarChart;
    @FXML private Label totalUsersStat, totalAdminsStat, totalUsersOnlyStat;

    private UserCRUD userCRUD = new UserCRUD();

    @FXML
    public void initialize() {
        loadStats();
    }

    private void loadStats() {
        try {
            List<User> users = userCRUD.afficherAll();

            // Totaux (inchangé)
            totalUsersStat.setText(String.valueOf(users.size()));
            long adminCount = users.stream().filter(u -> "ADMIN".equals(u.getRole())).count();
            long userCount = users.stream().filter(u -> "USER".equals(u.getRole())).count();
            totalAdminsStat.setText(String.valueOf(adminCount));
            totalUsersOnlyStat.setText(String.valueOf(userCount));

            // Pie chart des rôles (inchangé)
            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
                    new PieChart.Data("Administrateurs", adminCount),
                    new PieChart.Data("Utilisateurs", userCount)
            );
            rolePieChart.setData(pieData);
            rolePieChart.setTitle("Rôles");

            // Bar chart dynamique des inscriptions par jour (30 derniers jours ou toutes les dates)
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Inscriptions");

            // Grouper par date (sans l'heure)
            java.util.Map<String, Integer> dailyCount = new java.util.TreeMap<>(); // TreeMap pour tri automatique
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");

            for (User u : users) {
                if (u.getCreatedAt() != null) {
                    String day = u.getCreatedAt().format(formatter);
                    dailyCount.put(day, dailyCount.getOrDefault(day, 0) + 1);
                }
            }

            // Ajouter les données dans l'ordre (déjà trié par TreeMap)
            for (java.util.Map.Entry<String, Integer> entry : dailyCount.entrySet()) {
                series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
            }

            inscriptionsBarChart.getData().clear();
            inscriptionsBarChart.getData().add(series);


        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToUsers() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/admin_users.fxml"));
            Stage stage = (Stage) totalUsersStat.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}