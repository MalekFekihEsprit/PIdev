package services;

import entities.Destination;
import entities.Hebergement;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HebergementCRUDTest {
    static HebergementCRUD hc;
    static DestinationCRUD dc;

    @BeforeAll
    static void setUp() {
        hc = new HebergementCRUD();
        dc = new DestinationCRUD();
    }

    @AfterEach
    void tearDown() {
        // Clean up any test data
        try {
            List<Hebergement> data = hc.afficher();
            // Delete any hebergements with "TEST_" in the name
            for (Hebergement h : data) {
                if (h.getNom_hebergement().contains("TEST_")) {
                    hc.supprimer(h);
                }
            }

            // Also clean up test destinations
            List<Destination> destData = dc.afficher();
            for (Destination d : destData) {
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
        Destination dest = new Destination();
        dest.setNom_destination("TEST_Paris");
        dest.setPays_destination("France");
        // Other fields are optional now

        try {
            dc.ajouter(dest);

            // Get the added destination
            List<Destination> destData = dc.afficher();
            Destination testDest = destData.stream()
                    .filter(d -> d.getNom_destination().equals("TEST_Paris"))
                    .findFirst()
                    .orElse(null);

            assertNotNull(testDest, "destination de test non trouvée");

            // Create hebergement (only nom_hebergement and destination_hebergement are required now)
            Hebergement h = new Hebergement();
            h.setNom_hebergement("TEST_Hotel Paris");
            h.setDestination(testDest);
            // Other fields are optional now

            hc.ajouter(h);

            List<Hebergement> data = hc.afficher();
            assertFalse(data.isEmpty(), "liste vide");
            assertTrue(data.stream()
                    .anyMatch(r -> r.getNom_hebergement().equals("TEST_Hotel Paris")), "hébergement non trouvé");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void modifier() {
        // Create a test destination
        Destination dest = new Destination();
        dest.setNom_destination("TEST_Lyon");
        dest.setPays_destination("France");

        try {
            dc.ajouter(dest);

            // Get the added destination
            List<Destination> destData = dc.afficher();
            Destination testDest = destData.stream()
                    .filter(d -> d.getNom_destination().equals("TEST_Lyon"))
                    .findFirst()
                    .orElse(null);

            assertNotNull(testDest, "destination de test non trouvée");

            // Create hebergement
            Hebergement h = new Hebergement();
            h.setNom_hebergement("TEST_Hotel Lyon");
            h.setDestination(testDest);

            hc.ajouter(h);

            // Get the added hebergement
            List<Hebergement> data = hc.afficher();
            Hebergement toModify = data.stream()
                    .filter(r -> r.getNom_hebergement().equals("TEST_Hotel Lyon"))
                    .findFirst()
                    .orElse(null);

            assertNotNull(toModify, "hébergement à modifier non trouvé");

            // Modify it
            toModify.setNom_hebergement("TEST_Hotel Lyon Modifié");
            toModify.setType_hebergement("Appartement");
            toModify.setPrixNuit_hebergement(150.0);
            // Destination remains the same
            toModify.setDestination(testDest);

            hc.modifier(toModify);

            // Check modification
            List<Hebergement> newData = hc.afficher();
            assertTrue(newData.stream()
                    .anyMatch(r -> r.getNom_hebergement().equals("TEST_Hotel Lyon Modifié")), "modification non trouvée");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void supprimer() {
        // Create a test destination
        Destination dest = new Destination();
        dest.setNom_destination("TEST_Marseille");
        dest.setPays_destination("France");

        try {
            dc.ajouter(dest);

            // Get the added destination
            List<Destination> destData = dc.afficher();
            Destination testDest = destData.stream()
                    .filter(d -> d.getNom_destination().equals("TEST_Marseille"))
                    .findFirst()
                    .orElse(null);

            assertNotNull(testDest, "destination de test non trouvée");

            // Create hebergement
            Hebergement h = new Hebergement();
            h.setNom_hebergement("TEST_Hotel Marseille");
            h.setDestination(testDest);

            hc.ajouter(h);

            // Get the added hebergement
            List<Hebergement> data = hc.afficher();
            Hebergement toDelete = data.stream()
                    .filter(r -> r.getNom_hebergement().equals("TEST_Hotel Marseille"))
                    .findFirst()
                    .orElse(null);

            assertNotNull(toDelete, "hébergement à supprimer non trouvé");
            int id = toDelete.getId_hebergement();

            // Delete it
            hc.supprimer(toDelete);

            // Check deletion
            List<Hebergement> newData = hc.afficher();
            assertTrue(newData.stream()
                    .noneMatch(r -> r.getId_hebergement() == id), "suppression non effectuée");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void afficher() {
        try {
            List<Hebergement> data = hc.afficher();
            assertFalse(data.isEmpty(), "liste vide");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}