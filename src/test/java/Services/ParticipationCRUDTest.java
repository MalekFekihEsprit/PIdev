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

    // IDs des utilisateurs existants
    static int idUser1 = 1; // minyar ghanem
    static int idUser2 = 2; // ghfjkghjkl vghjkl

    // IDs des voyages qui existent
    static int idVoyage7 = 7;
    static int idVoyage8 = 8;
    static int idVoyage3 = 3;
    static int idVoyage17 = 17;

    @BeforeAll
    static void setUp() {
        pc = new ParticipationCRUD();
        vc = new VoyageCRUD();
        System.out.println("Test ParticipationCRUD initialisé");
    }

    @Test
    @Order(1)
    void ajouter() {
        try {
            // Récupérer toutes les participations existantes
            List<Participation> existingData = pc.afficher();

            // Utiliser user2 + voyage3 qui n'existe pas
            final int finalUserId = idUser2;
            final int finalVoyageId = idVoyage3;

            String roleUnique = "Test Ajout " + System.currentTimeMillis();

            Participation p = new Participation(
                    finalUserId,
                    roleUnique,
                    finalVoyageId
            );

            pc.ajouter(p);
            List<Participation> data = pc.afficher();

            assertFalse(data.isEmpty(), "La liste des participations est vide");

            final int checkUserId = finalUserId;
            final int checkVoyageId = finalVoyageId;
            final String checkRole = roleUnique;

            boolean trouve = data.stream()
                    .anyMatch(r -> r.getId() == checkUserId &&
                            r.getId_voyage() == checkVoyageId &&
                            r.getRole_participation().equals(checkRole));

            assertTrue(trouve, "Participation inexistante dans la liste");

            System.out.println("Test ajouter réussi !");
            System.out.println("  - User ID: " + finalUserId +
                    ", Voyage ID: " + finalVoyageId +
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
            List<Participation> data = pc.afficher();

            if (data.isEmpty()) {
                System.out.println("Aucune participation à modifier, test ignoré");
                return;
            }

            // Chercher une participation existante à modifier (ID 16 qui existe)
            Participation p = null;
            for (Participation part : data) {
                if (part.getId_participation() == 16) {
                    p = part;
                    break;
                }
            }

            if (p == null) {
                p = data.get(0);
            }

            int idParticipation = p.getId_participation();
            String ancienRole = p.getRole_participation();
            String nouveauRole = ancienRole + " (Modifié Test)";

            // Modifier le rôle
            p.setRole_participation(nouveauRole);
            pc.modifier(p);

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
            // Utiliser une combinaison différente de celle du test ajouter()
            // Ici on utilise user1 + voyage17 (qui n'existe pas encore)
            final int finalUser1 = idUser1;
            final int finalVoyage17 = idVoyage17;

            // Vérifier d'abord si cette combinaison existe déjà
            List<Participation> existingData = pc.afficher();
            boolean existeDeja = existingData.stream()
                    .anyMatch(r -> r.getId() == finalUser1 && r.getId_voyage() == finalVoyage17);

            if (existeDeja) {
                System.out.println("La combinaison user1+v17 existe déjà, test supprimer ignoré");
                return;
            }

            String roleASupprimer = "À supprimer " + System.currentTimeMillis();

            Participation p = new Participation(
                    finalUser1,
                    roleASupprimer,
                    finalVoyage17
            );

            pc.ajouter(p);
            System.out.println("Participation ajoutée pour suppression");

            List<Participation> data = pc.afficher();
            int idASupprimer = -1;

            for (Participation participation : data) {
                if (participation.getRole_participation().equals(roleASupprimer) &&
                        participation.getId() == finalUser1 &&
                        participation.getId_voyage() == finalVoyage17) {
                    idASupprimer = participation.getId_participation();
                    break;
                }
            }

            assertNotEquals(-1, idASupprimer, "Participation à supprimer non trouvée");

            final int finalIdASupprimer = idASupprimer;
            pc.supprimer(finalIdASupprimer);

            List<Participation> newData = pc.afficher();
            boolean trouve = newData.stream()
                    .anyMatch(r -> r.getId_participation() == finalIdASupprimer);

            assertFalse(trouve, "La participation n'a pas été supprimée correctement");

            System.out.println("Test supprimer réussi !");
            System.out.println("  - ID supprimé: " + idASupprimer +
                    ", Rôle: " + roleASupprimer);

        } catch (SQLException e) {
            System.out.println("Erreur lors du test de suppression: " + e.getMessage());
            e.printStackTrace();
            fail("Exception lors de la suppression: " + e.getMessage());
        }
    }
}