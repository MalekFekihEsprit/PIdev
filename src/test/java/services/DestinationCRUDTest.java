package services;

import entities.Destination;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DestinationCRUDTest {
    static DestinationCRUD dc;

    @BeforeAll
    static void setUp() {
        dc = new DestinationCRUD();
    }

    @AfterEach
    void tearDown() {
        // Clean up any test data
        try {
            List<Destination> data = dc.afficher();
            // Delete any destinations with "TEST_" in the name
            for (Destination d : data) {
                if (d.getNom_destination().contains("TEST_")) {
                    dc.supprimer(d);
                }
            }
        } catch (SQLException e) {
            System.out.println("Cleanup error: " + e.getMessage());
        }
    }

    @Test
    void ajouter() {
        // Create a test destination (only nom_destination and pays_destination are required now)
        Destination d = new Destination();
        d.setNom_destination("TEST_Paris");
        d.setPays_destination("France");
        // Other fields are optional now

        try {
            dc.ajouter(d);
            List<Destination> data = dc.afficher();
            assertFalse(data.isEmpty(), "liste vide");
            assertTrue(data.stream()
                    .anyMatch(r -> r.getNom_destination().equals("TEST_Paris")), "destination non trouvée");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void modifier() {
        // First add a test destination
        Destination d = new Destination();
        d.setNom_destination("TEST_Lyon");
        d.setPays_destination("France");

        try {
            dc.ajouter(d);

            // Get the added destination
            List<Destination> data = dc.afficher();
            Destination toModify = data.stream()
                    .filter(r -> r.getNom_destination().equals("TEST_Lyon"))
                    .findFirst()
                    .orElse(null);

            assertNotNull(toModify, "destination à modifier non trouvée");

            // Modify it
            toModify.setNom_destination("TEST_Lyon Modifié");
            toModify.setPays_destination("France");
            toModify.setDescription_destination("Capitale des Gaules");
            toModify.setClimat_destination("Tempéré");
            toModify.setSaison_destination("Toutes");

            dc.modifier(toModify);

            // Check modification
            List<Destination> newData = dc.afficher();
            assertTrue(newData.stream()
                    .anyMatch(r -> r.getNom_destination().equals("TEST_Lyon Modifié")), "modification non trouvée");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void supprimer() {
        // First add a test destination
        Destination d = new Destination();
        d.setNom_destination("TEST_Bordeaux");
        d.setPays_destination("France");

        try {
            dc.ajouter(d);

            // Get the added destination
            List<Destination> data = dc.afficher();
            Destination toDelete = data.stream()
                    .filter(r -> r.getNom_destination().equals("TEST_Bordeaux"))
                    .findFirst()
                    .orElse(null);

            assertNotNull(toDelete, "destination à supprimer non trouvée");
            int id = toDelete.getId_destination();

            // Delete it
            dc.supprimer(toDelete);

            // Check deletion
            List<Destination> newData = dc.afficher();
            assertTrue(newData.stream()
                    .noneMatch(r -> r.getId_destination() == id), "suppression non effectuée");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void afficher() {
        try {
            List<Destination> data = dc.afficher();
            assertFalse(data.isEmpty(), "liste vide");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}