package Services;

import Entities.Budget;
import Entities.Depense;
import org.junit.jupiter.api.*;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DepenseCRUDTest {

    static DepenseCRUD depenseCRUD;
    static BudgetCRUD budgetCRUD;
    static int idDepenseTest;
    static int idBudgetTest;
    static final int TEST_USER_ID = 1;
    static final int TEST_VOYAGE_ID = 15;

    @BeforeAll
    static void setup() {
        try {
            depenseCRUD = new DepenseCRUD();
            budgetCRUD = new BudgetCRUD();

            // 1. Vérifier si le budget de test existe déjà
            List<Budget> budgetsExistants = budgetCRUD.getBudgetsByUserId(TEST_USER_ID);
            Optional<Budget> budgetExistant = budgetsExistants.stream()
                    .filter(b -> "Budget Test Dépenses".equals(b.getLibelleBudget()))
                    .findFirst();

            if (budgetExistant.isPresent()) {
                // Budget existe déjà
                idBudgetTest = budgetExistant.get().getIdBudget();
                System.out.println("Budget existant récupéré avec ID: " + idBudgetTest);
            } else {
                // Créer un nouveau budget
                Budget budget = new Budget(
                        "Budget Test Dépenses",
                        5000.00,
                        "EUR",
                        "ACTIF",
                        "Budget pour tests de dépenses",
                        TEST_USER_ID,
                        TEST_VOYAGE_ID
                );
                budgetCRUD.ajouter(budget);
                idBudgetTest = budget.getIdBudget();
                System.out.println("Nouveau budget créé avec ID: " + idBudgetTest);
            }

            System.out.println("=== Début des tests DepenseCRUD ===");
            System.out.println("Budget de test avec ID: " + idBudgetTest);

        } catch (SQLException e) {
            e.printStackTrace();
            fail("Erreur lors de l'initialisation: " + e.getMessage());
        }
    }

    @Test
    @Order(1)
    void testAjouter() throws SQLException {
        System.out.println("Test 1: Ajout d'une dépense");

        // Nettoyer les anciennes dépenses de test
        nettoyerDepensesTest();

        Depense depense = new Depense(
                150.00,
                "Dépense Test",
                "Restauration",
                "Description test",
                "EUR",
                "Carte bancaire",
                Date.valueOf(LocalDate.now()),
                idBudgetTest
        );

        depenseCRUD.ajouter(depense);
        assertTrue(depense.getIdDepense() > 0, "L'ID de la dépense doit être généré");

        List<Depense> depenses = depenseCRUD.getDepensesByBudgetId(idBudgetTest);
        assertFalse(depenses.isEmpty(), "La liste des dépenses ne doit pas être vide");

        Optional<Depense> found = depenses.stream()
                .filter(d -> "Dépense Test".equals(d.getLibelleDepense()))
                .findFirst();

        assertTrue(found.isPresent(), "La dépense ajoutée doit être trouvée");
        idDepenseTest = found.get().getIdDepense();
        System.out.println("Dépense créée avec ID: " + idDepenseTest);
    }

    @Test
    @Order(2)
    void testAjouterDepenseSansBudget() {
        System.out.println("Test 2: Ajout sans budget");

        Depense depense = new Depense(
                100.00,
                "Dépense Sans Budget",
                "Autre",
                "Test",
                "EUR",
                "Espèces",
                Date.valueOf(LocalDate.now()),
                99999
        );

        Exception exception = assertThrows(SQLException.class, () -> {
            depenseCRUD.ajouter(depense);
        });

        assertTrue(exception.getMessage().toLowerCase().contains("budget"),
                "Message devrait mentionner le budget");
    }

    @Test
    @Order(3)
    void testAjouterDepenseDepasseBudget() throws SQLException {
        System.out.println("Test 3: Dépense qui dépasse le budget");

        double budgetTotal = budgetCRUD.getMontantTotalByBudgetId(idBudgetTest);
        double depensesTotal = depenseCRUD.getTotalDepensesByBudgetId(idBudgetTest);
        double montantRestant = budgetTotal - depensesTotal;

        Depense depense = new Depense(
                montantRestant + 100.00,
                "Dépense Trop Chère",
                "Hébergement",
                "Dépasse le budget",
                "EUR",
                "Carte bancaire",
                Date.valueOf(LocalDate.now()),
                idBudgetTest
        );

        Exception exception = assertThrows(SQLException.class, () -> {
            depenseCRUD.ajouter(depense);
        });

        assertTrue(exception.getMessage().toLowerCase().contains("dépasse"),
                "Message devrait mentionner le dépassement");
    }

    @Test
    @Order(4)
    void testAjouterDepenseDoublon() {
        System.out.println("Test 4: Dépense en double");

        Depense depense = new Depense(
                75.00,
                "Dépense Test",
                "Transport",
                "Test doublon",
                "EUR",
                "Espèces",
                Date.valueOf(LocalDate.now()),
                idBudgetTest
        );

        Exception exception = assertThrows(SQLException.class, () -> {
            depenseCRUD.ajouter(depense);
        });

        assertTrue(exception.getMessage().toLowerCase().contains("existe"),
                "Message devrait mentionner l'existence");
    }

    @Test
    @Order(5)
    void testGetById() throws SQLException {
        System.out.println("Test 5: Récupération par ID");

        assertTrue(idDepenseTest > 0);
        Depense depense = depenseCRUD.getById(idDepenseTest);

        assertNotNull(depense);
        assertEquals(idDepenseTest, depense.getIdDepense());
        assertEquals("Dépense Test", depense.getLibelleDepense());
        assertEquals(150.00, depense.getMontantDepense());
    }

    @Test
    @Order(6)
    void testGetByIdInexistant() throws SQLException {
        System.out.println("Test 6: ID inexistant");

        Depense depense = depenseCRUD.getById(99999);
        assertNull(depense);
    }

    @Test
    @Order(7)
    void testModifier() throws SQLException {
        System.out.println("Test 7: Modification");

        assertTrue(idDepenseTest > 0);
        Depense depense = depenseCRUD.getById(idDepenseTest);
        assertNotNull(depense);

        depense.setLibelleDepense("Dépense Test Modifiée");
        depense.setMontantDepense(200.00);
        depense.setCategorieDepense("Transport");
        depense.setDeviseDepense("USD");
        depense.setTypePaiement("Espèces");

        depenseCRUD.modifier(depense);

        Depense depenseModifiee = depenseCRUD.getById(idDepenseTest);
        assertEquals("Dépense Test Modifiée", depenseModifiee.getLibelleDepense());
        assertEquals(200.00, depenseModifiee.getMontantDepense());
        assertEquals("Transport", depenseModifiee.getCategorieDepense());
        assertEquals("USD", depenseModifiee.getDeviseDepense());
    }

    @Test
    @Order(8)
    void testModifierDepenseInexistante() {
        System.out.println("Test 8: Modification inexistante");

        Depense depense = new Depense(
                99999,
                100.00,
                "Inexistante",
                "Test",
                "Desc",
                "EUR",
                "Carte",
                Date.valueOf(LocalDate.now()),
                idBudgetTest
        );

        Exception exception = assertThrows(SQLException.class, () -> {
            depenseCRUD.modifier(depense);
        });

        assertTrue(exception.getMessage().toLowerCase().contains("introuvable") ||
                exception.getMessage().toLowerCase().contains("existe pas"));
    }

    @Test
    @Order(9)
    void testGetDepensesByBudgetId() throws SQLException {
        System.out.println("Test 9: Dépenses par budget");

        List<Depense> depenses = depenseCRUD.getDepensesByBudgetId(idBudgetTest);
        assertNotNull(depenses);
        assertFalse(depenses.isEmpty());

        boolean found = depenses.stream()
                .anyMatch(d -> d.getIdDepense() == idDepenseTest);
        assertTrue(found);
    }

    @Test
    @Order(10)
    void testGetAllDepenses() throws SQLException {
        System.out.println("Test 10: Toutes les dépenses");

        List<Depense> depenses = depenseCRUD.getAllDepenses();
        assertNotNull(depenses);
        assertTrue(depenses.size() > 0);
    }

    @Test
    @Order(11)
    void testGetTotalDepensesByBudgetId() throws SQLException {
        System.out.println("Test 11: Total des dépenses");

        double total = depenseCRUD.getTotalDepensesByBudgetId(idBudgetTest);
        assertTrue(total >= 200.00);
    }

    @Test
    @Order(12)
    void testDepenseExists() throws SQLException {
        System.out.println("Test 12: Existence");

        assertTrue(depenseCRUD.depenseExists(idDepenseTest));
        assertFalse(depenseCRUD.depenseExists(99999));
    }

    @Test
    @Order(13)
    void testDepenseExistePourBudget() throws SQLException {
        System.out.println("Test 13: Existence par libellé");

        boolean existe = depenseCRUD.depenseExistePourBudget(
                "Dépense Test Modifiée", idBudgetTest);
        assertTrue(existe);

        boolean existePas = depenseCRUD.depenseExistePourBudget(
                "Inexistant", idBudgetTest);
        assertFalse(existePas);
    }

    @Test
    @Order(14)
    void testAfficher() throws SQLException {
        System.out.println("Test 14: Affichage");

        List<Depense> depenses = depenseCRUD.afficher();
        assertNotNull(depenses);
        assertFalse(depenses.isEmpty());
    }

    @Test
    @Order(15)
    void testSupprimer() throws SQLException {
        System.out.println("Test 15: Suppression");

        assertTrue(idDepenseTest > 0);
        depenseCRUD.supprimer(idDepenseTest);

        Depense depense = depenseCRUD.getById(idDepenseTest);
        assertNull(depense);
        assertFalse(depenseCRUD.depenseExists(idDepenseTest));
    }

    @Test
    @Order(16)
    void testSupprimerInexistant() {
        System.out.println("Test 16: Suppression inexistante");

        Exception exception = assertThrows(SQLException.class, () -> {
            depenseCRUD.supprimer(99999);
        });

        assertTrue(exception.getMessage().toLowerCase().contains("introuvable") ||
                exception.getMessage().toLowerCase().contains("existe pas"));
    }

    private void nettoyerDepensesTest() throws SQLException {
        List<Depense> depenses = depenseCRUD.getDepensesByBudgetId(idBudgetTest);
        for (Depense d : depenses) {
            if (d.getLibelleDepense().contains("Test")) {
                try {
                    depenseCRUD.supprimer(d.getIdDepense());
                } catch (SQLException e) {
                    // Ignorer
                }
            }
        }
    }

    @AfterEach
    void cleanUpAfterTest() {
        try {
            nettoyerDepensesTest();
        } catch (SQLException e) {
            System.err.println("Erreur nettoyage: " + e.getMessage());
        }
    }

    @AfterAll
    static void tearDown() {
        System.out.println("=== Fin des tests ===");
        try {
            if (idBudgetTest > 0) {
                // Supprimer toutes les dépenses liées
                List<Depense> depenses = depenseCRUD.getDepensesByBudgetId(idBudgetTest);
                for (Depense d : depenses) {
                    try {
                        depenseCRUD.supprimer(d.getIdDepense());
                    } catch (SQLException e) {
                        // Ignorer
                    }
                }
                // Supprimer le budget
                budgetCRUD.supprimer(idBudgetTest);
            }
        } catch (SQLException e) {
            System.err.println("Erreur nettoyage final: " + e.getMessage());
        }
    }
}