package Services;

import org.junit.jupiter.api.*;
import Entites.etape;

import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class etapeCRUDTest {

    private static etapeCRUD etapeCRUD;
    private static etape testEtape;
    private static int createdId;
    // Utiliser des IDs qui existent dans votre base
    private static int idItineraireTest = 3; // D'après votre base, l'itinéraire 3 existe
    private static int idActiviteTest = 1;   // À vérifier que l'activité 1 existe

    @BeforeAll
    static void setup() {
        etapeCRUD = new etapeCRUD();

        testEtape = new etape();
        testEtape.setHeure(Time.valueOf(LocalTime.of(10, 30)));
        testEtape.setDescription_etape("Description de test pour l'étape");
        testEtape.setId_itineraire(idItineraireTest);
        testEtape.setId_activite(idActiviteTest);

        System.out.println("Test avec id_itineraire = " + idItineraireTest);
    }

    @Test
    @Order(1)
    void ajouter() throws SQLException {
        try {
            // Ajouter l'étape
            etapeCRUD.ajouter(testEtape);

            // Vérifier que l'ID a été généré
            assertTrue(testEtape.getId_etape() > 0, "L'ID devrait être généré après ajout");

            // Sauvegarder l'ID pour les tests suivants
            createdId = testEtape.getId_etape();

            System.out.println("Étape ajoutée avec ID: " + createdId);
        } catch (SQLException e) {
            System.err.println("Erreur SQL: " + e.getMessage());
            fail("L'ajout a échoué: " + e.getMessage());
        }
    }

    @Test
    @Order(2)
    void afficher() throws SQLException {
        // Vérifier d'abord que l'étape a été créée
        assertTrue(createdId > 0, "L'étape devrait avoir été créée dans le test ajouter()");

        // Test afficher() - récupère toutes les étapes
        List<etape> allEtapes = etapeCRUD.afficher();
        assertNotNull(allEtapes, "La liste ne devrait pas être null");
        assertFalse(allEtapes.isEmpty(), "La liste ne devrait pas être vide");

        // Afficher toutes les étapes pour déboguer
        System.out.println("Nombre d'étapes dans la base: " + allEtapes.size());
        for (etape e : allEtapes) {
            System.out.println("ID: " + e.getId_etape() + ", Description: " + e.getDescription_etape());
        }

        // Vérifier que notre étape de test est présente
        boolean found = false;
        for (etape e : allEtapes) {
            if (e.getId_etape() == createdId) {
                found = true;
                assertEquals(testEtape.getDescription_etape(), e.getDescription_etape());
                assertEquals(testEtape.getHeure(), e.getHeure());
                assertEquals(testEtape.getId_itineraire(), e.getId_itineraire());
                assertEquals(testEtape.getId_activite(), e.getId_activite());
                System.out.println("Étape trouvée avec ID: " + createdId);
                break;
            }
        }
        assertTrue(found, "L'étape de test devrait être dans la liste");
    }

    @Test
    @Order(3)
    void testGetEtapesByItineraire() throws SQLException {
        // Test de la méthode spécifique getEtapesByItineraire
        List<etape> etapesByItineraire = etapeCRUD.getEtapesByItineraire(idItineraireTest);
        assertNotNull(etapesByItineraire, "La liste ne devrait pas être null");

        System.out.println("Nombre d'étapes pour l'itinéraire " + idItineraireTest + ": " + etapesByItineraire.size());

        // Vérifier que notre étape est dans la liste de l'itinéraire
        boolean found = false;
        for (etape e : etapesByItineraire) {
            if (e.getId_etape() == createdId) {
                found = true;
                break;
            }
        }
        assertTrue(found, "L'étape devrait être trouvée dans getEtapesByItineraire");
    }

    @Test
    @Order(4)
    void modifier() throws SQLException {
        // Vérifier d'abord que l'étape existe
        assertTrue(createdId > 0, "L'étape devrait avoir été créée");

        // Modifier quelques champs
        testEtape.setHeure(Time.valueOf(LocalTime.of(14, 45)));
        testEtape.setDescription_etape("Description modifiée de l'étape");

        etapeCRUD.modifier(testEtape);
        System.out.println("Étape modifiée avec ID: " + createdId);

        // Vérifier les modifications en récupérant toutes les étapes
        List<etape> allEtapes = etapeCRUD.afficher();
        etape retrieved = null;
        for (etape e : allEtapes) {
            if (e.getId_etape() == createdId) {
                retrieved = e;
                break;
            }
        }

        assertNotNull(retrieved, "L'étape devrait exister après modification");
        assertEquals(Time.valueOf(LocalTime.of(14, 45)), retrieved.getHeure());
        assertEquals("Description modifiée de l'étape", retrieved.getDescription_etape());
    }

    @Test
    @Order(5)
    void supprimer() throws SQLException {
        // Vérifier d'abord que l'étape existe
        assertTrue(createdId > 0, "L'étape devrait avoir été créée");

        // Supprimer l'étape
        etapeCRUD.supprimer(createdId);
        System.out.println("Étape supprimée avec ID: " + createdId);

        // Vérifier qu'elle n'existe plus
        List<etape> allEtapes = etapeCRUD.afficher();
        boolean found = false;
        for (etape e : allEtapes) {
            if (e.getId_etape() == createdId) {
                found = true;
                break;
            }
        }
        assertFalse(found, "L'étape ne devrait plus être dans la liste après suppression");
    }
}