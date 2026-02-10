package Tests;

import Entities.Budget;
import Entities.Depense;
import Services.BudgetCRUD;
import Services.DepenseCRUD;
import Tools.MyBD;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class MainConnection {

    public static void main(String[] args) {

        // 🔹 Test DB connection
        MyBD myBD = MyBD.getInstance();
        System.out.println("Connexion BD réussie ✅");

        BudgetCRUD budgetCRUD = new BudgetCRUD();
        DepenseCRUD depenseCRUD = new DepenseCRUD();

        try {
            // 🔹 CREATE Budgets (BigDecimal + tous les champs sauf id_voyage)
            Budget b1 = new Budget(
                    null,
                    new BigDecimal("5556.10"),
                    "TND",
                    "ACTIF",
                    "Budget principal pour le voyage"
            );

            Budget b2 = new Budget(
                    null,
                    new BigDecimal("223.50"),
                    "EUR",
                    "ACTIF",
                    "Budget secondaire"
            );

            budgetCRUD.ajouter(b1);
            budgetCRUD.ajouter(b2);

            // 🔹 READ Budgets
            System.out.println("📌 Liste des budgets :");
            List<Budget> budgets = budgetCRUD.afficher();
            budgets.forEach(System.out::println);

            // 🔹 CREATE Depenses (utiliser id_budget existant dans Budget)
            // Ici, on prend le premier budget créé pour associer les dépenses
            Long idBudget = budgets.get(0).getIdBudget().longValue();

            Depense d1 = new Depense(
                    null,
                    "Alimentation",
                    new BigDecimal("10.50"),
                    "Nourriture",
                    "Petit-déjeuner et snacks",
                    "TND",
                    "Espèces",
                    LocalDate.now(),
                    idBudget
            );

            Depense d2 = new Depense(
                    null,
                    "Transport",
                    new BigDecimal("12.30"),
                    "Taxi",
                    "Navette aéroport",
                    "TND",
                    "Carte",
                    LocalDate.now(),
                    idBudget
            );

            depenseCRUD.ajouter(d1);
            depenseCRUD.ajouter(d2);

            // 🔹 READ Depenses
            System.out.println("📌 Liste des dépenses :");
            List<Depense> depenses = depenseCRUD.afficher();
            depenses.forEach(System.out::println);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
