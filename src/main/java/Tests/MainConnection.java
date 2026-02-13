package Tests;

import Entities.Budget;
import Entities.Depense;
import Services.BudgetCRUD;
import Services.DepenseCRUD;
import Tools.MyBD;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class MainConnection {

    public static void main(String[] args) {

        try {
            // 🔹 Connexion BD
            MyBD myBD = MyBD.getInstance();
            Connection conn = myBD.getConn();
            System.out.println("Connexion BD réussie ✅");

            // 🔹 Créer un utilisateur test pour respecter la FK
            String sqlUser = "INSERT IGNORE INTO user (id, nom, email) VALUES (?, ?, ?)";
            try (PreparedStatement psUser = conn.prepareStatement(sqlUser)) {
                psUser.setInt(1, 1);           // id utilisateur
                psUser.setString(2, "Alice");  // nom
                psUser.setString(3, "alice@test.com"); // email
                psUser.executeUpdate();
            }
            System.out.println("Utilisateur test créé ✅");

            // 🔹 Initialiser CRUD
            BudgetCRUD budgetCRUD = new BudgetCRUD();
            DepenseCRUD depenseCRUD = new DepenseCRUD();

            // 🔹 CREATE Budgets
            Budget b1 = new Budget(
                    5556.10f,
                    "TND",
                    "ACTIF",
                    "Budget principal pour le voyage",
                    1, // FK id_user
                    0  // id_voyage
            );

            Budget b2 = new Budget(
                    223.50f,
                    "EUR",
                    "ACTIF",
                    "Budget secondaire",
                    1, // FK id_user
                    0
            );


            budgetCRUD.ajouter(b1);
            budgetCRUD.ajouter(b2);
            System.out.println("Budgets ajoutés ✅");

            // 🔹 READ Budgets
            List<Budget> budgets = budgetCRUD.afficher();
            System.out.println("📌 Liste des budgets :");
            budgets.forEach(System.out::println);

            // 🔹 CREATE Depenses pour le premier budget
            int idBudget = budgets.get(0).getIdBudget(); // premier budget créé

            Depense d1 = new Depense(
                    10.50f,
                    "Alimentation",
                    "Nourriture",
                    "Petit-déjeuner et snacks",
                    "TND",
                    "Espèces",
                    Date.valueOf(LocalDate.now()),
                    idBudget
            );

            Depense d2 = new Depense(
                    12.30f,
                    "Transport",
                    "Taxi",
                    "Navette aéroport",
                    "TND",
                    "Carte",
                    Date.valueOf(LocalDate.now()),
                    idBudget
            );

            depenseCRUD.ajouter(d1);
            depenseCRUD.ajouter(d2);
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
