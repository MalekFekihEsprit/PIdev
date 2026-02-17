package Tests;

import Entities.Budget;
import Entities.Depense;
import Services.BudgetCRUD;
import Services.DepenseCRUD;
import Tools.MyBD;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class MainConnection {

    public static void main(String[] args) {

        try {
            // 🔹 Connexion BD
            Connection conn = MyBD.getInstance().getConn();
            System.out.println("Connexion BD réussie ✅");

            // 🔹 Créer un utilisateur test
            String sqlUser = "INSERT IGNORE INTO user (id, nom, email) VALUES (?, ?, ?)";
            try (PreparedStatement psUser = conn.prepareStatement(sqlUser)) {
                psUser.setInt(1, 1);
                psUser.setString(2, "Alice");
                psUser.setString(3, "alice@test.com");
                psUser.executeUpdate();
            }
            System.out.println("Utilisateur test créé ✅");

            // 🔹 Récupérer un id_voyage existant
            int idVoyage = 0;
            String sqlVoyage = "SELECT id_voyage FROM voyage LIMIT 1";
            try (PreparedStatement ps = conn.prepareStatement(sqlVoyage);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    idVoyage = rs.getInt("id_voyage");
                } else {
                    throw new SQLException("Aucun voyage trouvé dans la base ! Créez-en au moins un.");
                }
            }
            System.out.println("id_voyage récupéré : " + idVoyage);

            // 🔹 Initialiser CRUD
            BudgetCRUD budgetCRUD = new BudgetCRUD();
            DepenseCRUD depenseCRUD = new DepenseCRUD();

            // 🔹 CREATE plusieurs budgets
            Budget[] budgetsToAdd = new Budget[]{
                    new Budget("Budget loisirs", 1500.00, "TND", "ACTIF", "Budget pour sorties et loisirs", 1, idVoyage),
                    new Budget("Budget nourriture", 800.00, "TND", "ACTIF", "Alimentation quotidienne", 1, idVoyage),
                    new Budget("Budget transport", 500.00, "TND", "ACTIF", "Taxi, bus, train", 1, idVoyage)
            };

            for (Budget b : budgetsToAdd) {
                budgetCRUD.ajouter(b);
            }
            System.out.println("Tous les budgets ajoutés ✅");

            // 🔹 READ Budgets
            List<Budget> budgets = budgetCRUD.afficher();
            System.out.println("📌 Liste des budgets :");
            budgets.forEach(System.out::println);

            // 🔹 Récupérer le premier budget
            Budget premierBudget = budgetCRUD.afficher().get(0);
            int idBudget = premierBudget.getIdBudget();

// 🔹 Créer les dépenses
            Depense dep1 = new Depense(10.50, "Petit-déjeuner", "Nourriture", "Café et croissant", "TND", "Espèces", Date.valueOf(LocalDate.now()), idBudget);
            Depense dep2 = new Depense(25.00, "Taxi", "Transport", "Course aéroport", "TND", "Carte", Date.valueOf(LocalDate.now()), idBudget);

// 🔹 Ajouter les dépenses
            depenseCRUD.ajouter(dep1);
            depenseCRUD.ajouter(dep2);

            System.out.println("Dépenses ajoutées ✅");

            // 🔹 READ Depenses
            List<Depense> depenses = depenseCRUD.afficher();
            System.out.println("📌 Liste des dépenses :");
            depenses.forEach(System.out::println);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
