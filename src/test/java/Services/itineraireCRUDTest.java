package Services;

import org.junit.jupiter.api.*;
import Entites.Itineraire;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class itineraireCRUDTest {

    private static itineraireCRUD itineraireCRUD;
    private static Itineraire testItineraire;
    private static String uniqueNom;
    private static int createdId; // Note: l'ID n'est pas automatiquement récupéré dans ajouter()
    private static int idVoyageTest = 1; // À remplacer par un ID de voyage valide

    @BeforeAll
    static void setup() {
        itineraireCRUD = new itineraireCRUD();
        uniqueNom = "Test_" + UUID.randomUUID().toString().substring(0, 5); // Max 10 caractères

        testItineraire = new Itineraire();
        testItineraire.setNom_itineraire(uniqueNom);
        testItineraire.setDescription_itineraire("Description de test pour l'itinéraire");
        testItineraire.setId_voyage(idVoyageTest);
    }

    @Test
    @Order(1)
    void ajouter() throws SQLException {
        // Ajouter l'itinéraire
        itineraireCRUD.ajouter(testItineraire);

        // Récupérer tous les itinéraires pour trouver le nôtre (car ajouter() ne retourne pas l'ID)
        List<Itineraire> allItineraires = itineraireCRUD.afficher();
        assertNotNull(allItineraires, "La liste ne devrait pas être null");

        // Trouver notre itinéraire par nom
        Itineraire created = null;
        for (Itineraire i : allItineraires) {
            if (uniqueNom.equals(i.getNom_itineraire())) {
                created = i;
                break;
            }
        }

        assertNotNull(created, "L'itinéraire devrait être trouvé après ajout");

        // Sauvegarder l'ID
        createdId = created.getId_itineraire();
        testItineraire.setId_itineraire(createdId);

        System.out.println("Itinéraire ajouté avec ID: " + createdId);
    }

    @Test
    @Order(2)
    void testAjouterDoublon() throws SQLException {
        // Tenter d'ajouter le même itinéraire (devrait être ignoré sans erreur)
        itineraireCRUD.ajouter(testItineraire);

        // Vérifier qu'il n'y a pas de doublon
        List<Itineraire> allItineraires = itineraireCRUD.afficher();
        int count = 0;
        for (Itineraire i : allItineraires) {
            if (uniqueNom.equals(i.getNom_itineraire())) {
                count++;
            }
        }
        assertEquals(1, count, "Il ne devrait y avoir qu'un seul itinéraire avec ce nom");
    }

    @Test
    @Order(3)
    void afficher() throws SQLException {
        // Test afficher()
        List<Itineraire> allItineraires = itineraireCRUD.afficher();
        assertNotNull(allItineraires, "La liste ne devrait pas être null");
        assertFalse(allItineraires.isEmpty(), "La liste ne devrait pas être vide");

        // Vérifier que notre itinéraire de test est présent
        boolean found = false;
        for (Itineraire i : allItineraires) {
            if (i.getId_itineraire() == createdId) {
                found = true;
                assertEquals(testItineraire.getNom_itineraire(), i.getNom_itineraire());
                assertEquals(testItineraire.getDescription_itineraire(), i.getDescription_itineraire());
                assertEquals(testItineraire.getId_voyage(), i.getId_voyage());
                break;
            }
        }
        assertTrue(found, "L'itinéraire de test devrait être dans la liste");
    }

    @Test
    @Order(4)
    void testGetItinerairesByVoyage() throws SQLException {
        // Test de la méthode spécifique
        List<Itineraire> itinerairesByVoyage = itineraireCRUD.getItinerairesByVoyage(idVoyageTest);
        assertNotNull(itinerairesByVoyage, "La liste ne devrait pas être null");

        // Vérifier que notre itinéraire est dans la liste du voyage
        boolean found = false;
        for (Itineraire i : itinerairesByVoyage) {
            if (i.getId_itineraire() == createdId) {
                found = true;
                break;
            }
        }
        assertTrue(found, "L'itinéraire devrait être trouvé dans getItinerairesByVoyage");
    }

    @Test
    @Order(5)
    void modifier() throws SQLException {
        // Modifier quelques champs
        String newNom = "Modif_" + UUID.randomUUID().toString().substring(0, 4); // Max 10 caractères
        testItineraire.setNom_itineraire(newNom);
        testItineraire.setDescription_itineraire("Nouvelle description modifiée");

        itineraireCRUD.modifier(testItineraire);

        // Vérifier les modifications
        List<Itineraire> allItineraires = itineraireCRUD.afficher();
        Itineraire retrieved = null;
        for (Itineraire i : allItineraires) {
            if (i.getId_itineraire() == createdId) {
                retrieved = i;
                break;
            }
        }

        assertNotNull(retrieved, "L'itinéraire devrait exister après modification");
        assertEquals(newNom, retrieved.getNom_itineraire());
        assertEquals("Nouvelle description modifiée", retrieved.getDescription_itineraire());

        // Mettre à jour le nom unique pour référence
        uniqueNom = newNom;
    }

    @Test
    @Order(6)
    void testNomTropLong() {
        // Tester la validation du nom (max 10 caractères)
        Itineraire itineraireInvalide = new Itineraire();
        itineraireInvalide.setNom_itineraire("NomTropLong123"); // Plus de 10 caractères
        itineraireInvalide.setDescription_itineraire("Test");
        itineraireInvalide.setId_voyage(idVoyageTest);

        SQLException exception = assertThrows(SQLException.class, () -> {
            itineraireCRUD.ajouter(itineraireInvalide);
        });

        assertTrue(exception.getMessage().contains("10 caractères"),
                "Le message d'erreur devrait mentionner la limite de 10 caractères");
    }

    @Test
    @Order(7)
    void supprimer() throws SQLException {
        // Supprimer l'itinéraire
        itineraireCRUD.supprimer(createdId);

        // Vérifier qu'il n'existe plus
        List<Itineraire> allItineraires = itineraireCRUD.afficher();
        boolean found = false;
        for (Itineraire i : allItineraires) {
            if (i.getId_itineraire() == createdId) {
                found = true;
                break;
            }
        }
        assertFalse(found, "L'itinéraire ne devrait plus être dans la liste après suppression");
    }
}