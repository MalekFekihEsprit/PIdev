package Services;

import Entities.Pers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PersCRUDTest {

    static PersCRUD pc;
    @BeforeAll
    static void setup() {
        pc = new PersCRUD();
    }
    @Test
    void ajouter() {
        Pers p = new Pers("TestNom", "TestPrenom", 30);
        try {
            pc.ajouter(p);
            List<Pers> personnes = pc.afficherAll();
            assertFalse(personnes.isEmpty(), "The list of people should not be empty after adding a person.");
            assertTrue(personnes.stream().anyMatch(person -> person.getNom().equals("TestNom") && person.getPrenom().equals("TestPrenom") && person.getAge() == 30), "The added person should be in the list.");
        } catch (Exception e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }

    @Test
    void afficherAll() {
    }
}