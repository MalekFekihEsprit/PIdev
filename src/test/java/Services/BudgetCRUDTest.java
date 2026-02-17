package Services;

import Entities.Budget;
import org.junit.jupiter.api.*;
import java.sql.SQLException;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BudgetCRUDTest {

    static BudgetCRUD budgetCRUD;
    static int idBudgetTest;
    static final int TEST_USER_ID = 1;
    static final int TEST_VOYAGE_ID = 15; // ParisTrip

    @BeforeAll
    static void setup() {
        budgetCRUD = new BudgetCRUD();
        System.out.println("=== Début des tests BudgetCRUD ===");
    }

    @Test
    @Order(1)
    void testAjouter() throws SQLException {
        System.out.println("Test 1: Ajout d'un budget");

        Budget budget = new Budget(
                "Budget Test Unitaire",
                2000.00,
                "EUR",
                "ACTIF",
                "Budget pour les tests unitaires",
                TEST_USER_ID,
                TEST_VOYAGE_ID
        );

        budgetCRUD.ajouter(budget);

        assertTrue(budget.getIdBudget() > 0, "L'ID du budget doit être généré");

        List<Budget> budgets = budgetCRUD.getBudgetsByUserId(TEST_USER_ID);
        assertFalse(budgets.isEmpty(), "La liste des budgets ne doit pas être vide");

        boolean trouve = budgets.stream()
                .anyMatch(b -> b.getLibelleBudget().equals("Budget Test Unitaire"));
        assertTrue(trouve, "Le budget ajouté doit être trouvé dans la liste");

        idBudgetTest = budgets.stream()
                .filter(b -> b.getLibelleBudget().equals("Budget Test Unitaire"))
                .findFirst()
                .get()
                .getIdBudget();

        System.out.println("Budget créé avec ID: " + idBudgetTest);
    }

    @Test
    @Order(2)
    void testAjouterBudgetExistant() {
        System.out.println("Test 1b: Ajout d'un budget avec le même libellé");

        Budget budget = new Budget(
                "Budget Test Unitaire",
                3000.00,
                "USD",
                "ACTIF",
                "Test doublon",
                TEST_USER_ID,
                TEST_VOYAGE_ID
        );

        Exception exception = assertThrows(SQLException.class, () -> {
            budgetCRUD.ajouter(budget);
        });

        assertTrue(exception.getMessage().contains("existe déjà"),
                "Une exception doit être levée pour un budget en double");
    }

    @Test
    @Order(3)
    void testGetById() throws SQLException {
        System.out.println("Test 2: Récupération par ID");

        assertTrue(idBudgetTest > 0, "L'ID du budget doit être défini");

        Budget budget = budgetCRUD.getById(idBudgetTest);
        assertNotNull(budget, "Le budget récupéré ne doit pas être null");
        assertEquals(idBudgetTest, budget.getIdBudget(), "L'ID doit correspondre");
        assertEquals("Budget Test Unitaire", budget.getLibelleBudget());
        assertEquals(2000.00, budget.getMontantTotal());
    }

    @Test
    @Order(4)
    void testGetByIdInexistant() throws SQLException {
        System.out.println("Test 2b: Récupération par ID inexistant");

        Budget budget = budgetCRUD.getById(99999);
        assertNull(budget, "Un budget inexistant doit retourner null");
    }

    @Test
    @Order(5)
    void testModifier() throws SQLException {
        System.out.println("Test 3: Modification d'un budget");

        assertTrue(idBudgetTest > 0, "L'ID du budget doit être défini");

        Budget budget = budgetCRUD.getById(idBudgetTest);
        assertNotNull(budget);

        budget.setLibelleBudget("Budget Test Modifié");
        budget.setMontantTotal(2500.00);
        budget.setDeviseBudget("USD");
        budget.setStatutBudget("INACTIF");
        budget.setDescriptionBudget("Description modifiée");

        budgetCRUD.modifier(budget);

        Budget budgetModifie = budgetCRUD.getById(idBudgetTest);
        assertEquals("Budget Test Modifié", budgetModifie.getLibelleBudget());
        assertEquals(2500.00, budgetModifie.getMontantTotal());
        assertEquals("USD", budgetModifie.getDeviseBudget());
        assertEquals("INACTIF", budgetModifie.getStatutBudget());
    }

    @Test
    @Order(6)
    void testGetBudgetsByUserId() throws SQLException {
        System.out.println("Test 4: Récupération des budgets par utilisateur");

        List<Budget> budgets = budgetCRUD.getBudgetsByUserId(TEST_USER_ID);
        assertNotNull(budgets, "La liste ne doit pas être null");
        assertTrue(budgets.size() > 0, "La liste doit contenir au moins un budget");
    }

    @Test
    @Order(7)
    void testGetBudgetsByVoyageId() throws SQLException {
        System.out.println("Test 5: Récupération des budgets par voyage");

        List<Budget> budgets = budgetCRUD.getBudgetsByVoyageId(TEST_VOYAGE_ID);
        assertNotNull(budgets, "La liste ne doit pas être null");
    }

    @Test
    @Order(8)
    void testBudgetExists() throws SQLException {
        System.out.println("Test 6: Vérification d'existence d'un budget");

        assertTrue(budgetCRUD.budgetExists(idBudgetTest),
                "Le budget de test doit exister");
        assertFalse(budgetCRUD.budgetExists(99999),
                "Un budget avec ID 99999 ne doit pas exister");
    }

    @Test
    @Order(9)
    void testBudgetExiste() throws SQLException {
        System.out.println("Test 7: Vérification d'existence par libellé");

        boolean existe = budgetCRUD.budgetExiste(
                "Budget Test Modifié",
                TEST_VOYAGE_ID,
                TEST_USER_ID
        );
        assertTrue(existe, "Le budget doit être trouvé");
    }

    @Test
    @Order(10)
    void testBudgetExisteExclusion() throws SQLException {
        System.out.println("Test 8: Vérification d'existence avec exclusion");

        boolean existeAvecExclusion = budgetCRUD.budgetExisteExclusion(
                "Budget Test Modifié",
                TEST_VOYAGE_ID,
                TEST_USER_ID,
                idBudgetTest
        );
        assertFalse(existeAvecExclusion,
                "Avec exclusion, il ne doit pas être trouvé");
    }

    @Test
    @Order(11)
    void testGetMontantTotalByBudgetId() throws SQLException {
        System.out.println("Test 9: Récupération du montant total");

        double montant = budgetCRUD.getMontantTotalByBudgetId(idBudgetTest);
        assertEquals(2500.00, montant, "Le montant doit correspondre");
    }

    @Test
    @Order(12)
    void testAfficher() throws SQLException {
        System.out.println("Test 10: Affichage de tous les budgets");

        List<Budget> budgets = budgetCRUD.afficher();
        assertNotNull(budgets, "La liste ne doit pas être null");
        assertTrue(budgets.size() > 0, "La liste doit contenir des budgets");
    }

    @Test
    @Order(13)
    void testVoyageADejaUnBudget() throws SQLException {
        System.out.println("Test 11: Vérification si un voyage a déjà un budget");

        boolean aDeja = budgetCRUD.voyageADejaUnBudget(TEST_VOYAGE_ID);
        System.out.println("Le voyage " + TEST_VOYAGE_ID + " a déjà un budget ? " + aDeja);
    }

    @Test
    @Order(14)
    void testSupprimer() throws SQLException {
        System.out.println("Test 12: Suppression d'un budget");

        assertTrue(idBudgetTest > 0, "L'ID du budget doit être défini");

        budgetCRUD.supprimer(idBudgetTest);

        Budget budget = budgetCRUD.getById(idBudgetTest);
        assertNull(budget, "Le budget supprimé ne doit plus exister");
    }

    @Test
    @Order(15)
    void testSupprimerInexistant() {
        System.out.println("Test 12b: Suppression d'un budget inexistant");

        Exception exception = assertThrows(SQLException.class, () -> {
            budgetCRUD.supprimer(99999);
        });

        assertTrue(exception.getMessage().contains("introuvable"),
                "Une exception doit être levée pour un budget inexistant");
    }

    @AfterAll
    static void tearDown() {
        System.out.println("=== Fin des tests BudgetCRUD ===");
    }
}