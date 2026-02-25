package Services;

import org.junit.jupiter.api.*;

import Entities.User;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserCRUDTest {

    private static UserCRUD userCRUD;
    private static User testUser;
    private static String uniqueEmail;

    @BeforeAll
    static void setup() {
        userCRUD = new UserCRUD();
        uniqueEmail = "test_" + UUID.randomUUID() + "@example.com";
        testUser = new User();
        testUser.setNom("TestNom");
        testUser.setPrenom("TestPrenom");
        testUser.setEmail(uniqueEmail);
        testUser.setMotDePasse("password123");
        testUser.setDateNaissance(LocalDate.of(1990, 1, 1));
        testUser.setTelephone("12345678");
        testUser.setRole("USER");
        testUser.setPhotoUrl(null);
    }

    @AfterEach
    void cleanUp() throws SQLException {
        // Supprimer le testUser s'il existe après chaque test pour éviter les interférences
        // Mais on le fait dans les tests eux-mêmes ou on gère via une méthode dédiée
    }

    @Test
    @Order(1)
    void testAjouter() throws SQLException {
        // Vérifier que l'email n'existe pas déjà
        assertFalse(userCRUD.emailExists(uniqueEmail), "L'email ne devrait pas exister avant l'ajout");

        // Ajouter l'utilisateur
        userCRUD.ajouter(testUser);

        // Vérifier que l'email existe maintenant
        assertTrue(userCRUD.emailExists(uniqueEmail), "L'email devrait exister après ajout");

        // Récupérer par email pour vérifier les données
        User retrieved = userCRUD.getUserByEmailAndPassword(uniqueEmail, "password123");
        assertNotNull(retrieved, "L'utilisateur devrait être récupérable");
        assertEquals(testUser.getNom(), retrieved.getNom());
        assertEquals(testUser.getPrenom(), retrieved.getPrenom());
        assertEquals(testUser.getEmail(), retrieved.getEmail());
        assertEquals(testUser.getTelephone(), retrieved.getTelephone());
        assertEquals(testUser.getDateNaissance(), retrieved.getDateNaissance());
        assertEquals(testUser.getRole(), retrieved.getRole());

        // Sauvegarder l'ID pour les tests suivants
        testUser.setId(retrieved.getId());
    }

    @Test
    @Order(2)
    void testAfficherAll() throws SQLException {
        List<User> users = userCRUD.afficherAll();
        assertNotNull(users, "La liste ne devrait pas être null");
        assertFalse(users.isEmpty(), "La liste ne devrait pas être vide (au moins l'utilisateur de test)");

        // Vérifier que notre utilisateur de test est présent
        boolean found = users.stream().anyMatch(u -> u.getEmail().equals(uniqueEmail));
        assertTrue(found, "L'utilisateur de test devrait être dans la liste");
    }

    @Test
    @Order(3)
    void testAfficherById() throws SQLException {
        assertTrue(testUser.getId() > 0, "L'ID de test devrait être défini");

        List<User> users = userCRUD.afficherById(testUser.getId());
        assertNotNull(users);
        assertFalse(users.isEmpty());
        assertEquals(1, users.size());
        User retrieved = users.get(0);
        assertEquals(testUser.getId(), retrieved.getId());
        assertEquals(testUser.getEmail(), retrieved.getEmail());
    }

    @Test
    @Order(4)
    void testModifier() throws SQLException {
        // Modifier quelques champs
        testUser.setNom("NouveauNom");
        testUser.setPrenom("NouveauPrenom");
        testUser.setTelephone("87654321");

        userCRUD.modifier(testUser, false);

        // Vérifier les modifications
        User retrieved = userCRUD.getUserByEmailAndPassword(uniqueEmail, "password123");
        assertNotNull(retrieved);
        assertEquals("NouveauNom", retrieved.getNom());
        assertEquals("NouveauPrenom", retrieved.getPrenom());
        assertEquals("87654321", retrieved.getTelephone());
    }

    @Test
    @Order(5)
    void testUpdatePassword() throws SQLException {
        String newPassword = "newPass123";
        boolean updated = userCRUD.updatePassword(uniqueEmail, newPassword);
        assertTrue(updated, "Le mot de passe devrait être mis à jour");

        // Vérifier avec l'ancien mot de passe (ne doit pas fonctionner)
        User withOldPwd = userCRUD.getUserByEmailAndPassword(uniqueEmail, "password123");
        assertNull(withOldPwd, "L'ancien mot de passe ne devrait plus fonctionner");

        // Vérifier avec le nouveau
        User withNewPwd = userCRUD.getUserByEmailAndPassword(uniqueEmail, newPassword);
        assertNotNull(withNewPwd, "Le nouveau mot de passe devrait fonctionner");
    }

    @Test
    @Order(6)
    void testEmailExists() throws SQLException {
        assertTrue(userCRUD.emailExists(uniqueEmail), "L'email devrait exister");
        assertFalse(userCRUD.emailExists("nonexistent_" + UUID.randomUUID() + "@test.com"), "Un email inexistant ne devrait pas exister");
    }

    @Test
    @Order(7)
    void testGetUserByEmailAndPassword_NotFound() throws SQLException {
        User notFound = userCRUD.getUserByEmailAndPassword("inconnu@test.com", "pwd");
        assertNull(notFound, "Doit retourner null pour un utilisateur inexistant");
    }

//    @Test
//    @Order(8)
//    void testSupprimer() throws SQLException {
//        // Supprimer l'utilisateur
//        boolean deleted = userCRUD.supprimer(testUser.getId());
//        assertTrue(deleted, "La suppression devrait réussir");
//
//        // Vérifier qu'il n'existe plus
//        assertFalse(userCRUD.emailExists(uniqueEmail), "L'email ne devrait plus exister après suppression");
//
//        List<User> users = userCRUD.afficherById(testUser.getId());
//        assertTrue(users.isEmpty(), "La recherche par ID ne devrait rien retourner");
//    }

    @Test
    @Order(8)
    void getPublicIpgetPublicIp() throws SQLException {
        String ip = null;
        try {
            ip = userCRUD.getPublicIp();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        assertNotNull(ip, "L'adresse IP publique ne devrait pas être null");
        assertFalse(ip.isEmpty(), "L'adresse IP publique ne devrait pas être vide");
        System.out.println("Adresse IP publique : " + ip);
    }

    @Test
    @Order(9)
    void getLocationFromIp() throws SQLException {
        String ip = null;
        try {
            ip = userCRUD.getPublicIp();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        String location = null;
        try {
            location = userCRUD.getLocationFromIp(ip);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        assertNotNull(location, "La localisation ne devrait pas être null");
        assertFalse(location.isEmpty(), "La localisation ne devrait pas être vide");
        System.out.println("Localisation pour l'IP " + ip + " : " + location);
    }

}