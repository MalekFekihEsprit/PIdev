package Services;

import Entities.Participation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ParticipationCRUDTest {

    static ParticipationCRUD pc;
    static VoyageCRUD vc;

    // IDs des utilisateurs existants dans votre table user
    static int idUser1 = 1; // minyar ghannem
    static int idUser2 = 2; // ghfjkghjkl vghjkl

    // IDs des voyages existants dans votre table voyage (d'après la capture)
    static int idVoyage7 = 7;  // DOUDOUZO
    static int idVoyage8 = 8;  // lara
    static int idVoyage3 = 3;  // (ID 3 existe)
    static int idVoyage19 = 19; // Voyage Test
    static int idVoyage17 = 17; // Voyage Test (Modifié)

    @BeforeAll
    static void setUp() {
        pc = new ParticipationCRUD();
        vc = new VoyageCRUD();
        System.out.println("Test ParticipationCRUD initialisé");
    }

    @Test
    @Order(1)
    void ajouter() {
        // Utiliser une combinaison (user, voyage) qui n'existe pas encore
        // D'après la table participation, il n'y a pas de participation avec user=2 et voyage=8
        String roleUnique = "Test Ajout " + System.currentTimeMillis();

        Participation p = new Participation(
                idUser2,      // user 2
                roleUnique,
                idVoyage8     // voyage 8 (lara) - pas de participation avec user 2
        );

        try {
            pc.ajouter(p);
            List<Participation> data = pc.afficher();

            assertFalse(data.isEmpty(), "La liste des participations est vide");

            boolean trouve = data.stream()
                    .anyMatch(r -> r.getId() == idUser2 &&
                            r.getId_voyage() == idVoyage8 &&
                            r.getRole_participation().equals(roleUnique));

            assertTrue(trouve, "Participation inexistante dans la liste");

            System.out.println("Test ajouter réussi !");
            System.out.println("  - User ID: " + idUser2 +
                    ", Voyage ID: " + idVoyage8 +
                    ", Rôle: " + roleUnique);

        } catch (SQLException e) {
            System.out.println("Erreur lors du test d'ajout: " + e.getMessage());
            e.printStackTrace();
            fail("Exception lors de l'ajout: " + e.getMessage());
        }
    }

    @Test
    @Order(2)
    void afficher() {
        try {
            List<Participation> data = pc.afficher();
            assertNotNull(data, "La liste retournée est null");

            System.out.println("Test afficher réussi !");
            System.out.println("Nombre de participations: " + data.size());

            if (!data.isEmpty()) {
                System.out.println("Liste des participations:");
                for (Participation p : data) {
                    System.out.println("  - ID Participation: " + p.getId_participation() +
                            ", ID User: " + p.getId() +
                            ", Rôle: " + p.getRole_participation() +
                            ", ID Voyage: " + p.getId_voyage());
                }
            }

        } catch (SQLException e) {
            System.out.println("Erreur lors du test d'affichage: " + e.getMessage());
            fail("Exception lors de l'affichage: " + e.getMessage());
        }
    }

    @Test
    @Order(3)
    void modifier() {
        try {
            // Modifier la participation ID 16 (user 2, voyage 7) qui existe déjà
            List<Participation> data = pc.afficher();

            if (data.isEmpty()) {
                System.out.println("Aucune participation à modifier, test ignoré");
                return;
            }

            // Chercher une participation existante à modifier (par exemple ID 16)
            Participation p = null;
            for (Participation part : data) {
                if (part.getId_participation() == 16) {
                    p = part;
                    break;
                }
            }

            if (p == null) {
                // Si pas trouvé, prendre la première
                p = data.get(0);
            }

            int idParticipation = p.getId_participation();
            String ancienRole = p.getRole_participation();
            String nouveauRole = ancienRole + " (Modifié Test)";

            // Modifier le rôle
            p.setRole_participation(nouveauRole);
            pc.modifier(p);

            // Vérifier la modification
            final int finalIdParticipation = idParticipation;
            final String finalNouveauRole = nouveauRole;

            List<Participation> newData = pc.afficher();
            boolean trouve = newData.stream()
                    .anyMatch(r -> r.getId_participation() == finalIdParticipation &&
                            r.getRole_participation().equals(finalNouveauRole));

            assertTrue(trouve, "La modification n'a pas été effectuée correctement");

            System.out.println("Test modifier réussi !");
            System.out.println("  - ID Participation: " + idParticipation +
                    ", Ancien rôle: " + ancienRole +
                    ", Nouveau rôle: " + nouveauRole);

        } catch (SQLException e) {
            System.out.println("Erreur lors du test de modification: " + e.getMessage());
            fail("Exception lors de la modification: " + e.getMessage());
        }
    }

    @Test
    @Order(4)
    void supprimer() {
        try {
            // Ajouter d'abord une nouvelle participation à supprimer
            // Utiliser user 1 avec voyage 17 (pas de participation avec cette combinaison)
            String roleASupprimer = "À supprimer " + System.currentTimeMillis();

            Participation p = new Participation(
                    idUser1,      // user 1
                    roleASupprimer,
                    idVoyage17    // voyage 17 - pas de participation avec user 1
            );

            pc.ajouter(p);
            System.out.println("Participation ajoutée pour suppression");

            // Récupérer l'ID de la participation ajoutée
            List<Participation> data = pc.afficher();
            int idASupprimer = -1;

            for (Participation participation : data) {
                if (participation.getRole_participation().equals(roleASupprimer) &&
                        participation.getId() == idUser1 &&
                        participation.getId_voyage() == idVoyage17) {
                    idASupprimer = participation.getId_participation();
                    break;
                }
            }

            assertNotEquals(-1, idASupprimer, "Participation à supprimer non trouvée");

            // Sauvegarder l'ID dans une variable finale pour la lambda
            final int finalIdASupprimer = idASupprimer;

            // Supprimer la participation
            pc.supprimer(finalIdASupprimer);

            // Vérifier la suppression
            List<Participation> newData = pc.afficher();
            boolean trouve = newData.stream()
                    .anyMatch(r -> r.getId_participation() == finalIdASupprimer);

            assertFalse(trouve, "La participation n'a pas été supprimée correctement");

            System.out.println("Test supprimer réussi !");
            System.out.println("  - ID supprimé: " + idASupprimer +
                    ", Rôle: " + roleASupprimer);

        } catch (SQLException e) {
            System.out.println(" Erreur lors du test de suppression: " + e.getMessage());
            e.printStackTrace();
            fail("Exception lors de la suppression: " + e.getMessage());
        }
    }
}