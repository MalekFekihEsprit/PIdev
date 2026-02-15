package Services;

import Entities.Voyage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class VoyageCRUDTest {

    static VoyageCRUD vc;

    @BeforeAll
    static void setUp() {
        vc = new VoyageCRUD();
    }

    @Test
    void ajouter() {
        // Création d'un voyage de test
        Voyage v = new Voyage(
                "Voyage Test",
                Date.valueOf("2026-06-01"),
                Date.valueOf("2026-06-10"),
                "a venir",
                1
        );

        try {
            // Ajout du voyage
            vc.ajouter(v);

            // Récupération de tous les voyages
            List<Voyage> data = vc.afficher();

            // Vérifications
            assertFalse(data.isEmpty(), "La liste des voyages est vide");
            assertTrue(data.stream().anyMatch(r -> r.getTitre_voyage().equals("Voyage Test")),
                    "Voyage inexistant dans la liste");

            System.out.println("Test ajouter réussi !");

        } catch (SQLException e) {
            System.out.println("Erreur lors du test d'ajout: " + e.getMessage());
            fail("Exception lors de l'ajout: " + e.getMessage());
        }
    }

    @Test
    void afficher() {
        try {
            List<Voyage> data = vc.afficher();

            // Vérification que l'affichage fonctionne (peut être vide)
            assertNotNull(data, "La liste retournée est null");

            System.out.println("Test afficher réussi !");
            System.out.println("Nombre de voyages: " + data.size());

            // Afficher les voyages (optionnel)
            if (!data.isEmpty()) {
                System.out.println("Liste des voyages:");
                for (Voyage voyage : data) {
                    System.out.println("  - " + voyage.getTitre_voyage() + " (ID: " + voyage.getId_voyage() + ")");
                }
            }

        } catch (SQLException e) {
            System.out.println(" Erreur lors du test d'affichage: " + e.getMessage());
            fail("Exception lors de l'affichage: " + e.getMessage());
        }
    }

    @Test
    void modifier() {
        try {
            // Récupérer le dernier voyage ajouté pour le modifier
            List<Voyage> data = vc.afficher();

            if (data.isEmpty()) {
                System.out.println("Aucun voyage à modifier, test ignoré");
                return;
            }

            // Prendre le premier voyage de la liste
            Voyage v = data.get(0);
            String ancienTitre = v.getTitre_voyage();

            // Modifier le titre
            v.setTitre_voyage(ancienTitre + " (Modifié)");
            vc.modifier(v);

            // Vérifier la modification
            int idModifie = v.getId_voyage(); // Variable effectively final
            String nouveauTitre = v.getTitre_voyage(); // Variable effectively final

            List<Voyage> newData = vc.afficher();
            boolean trouve = newData.stream()
                    .anyMatch(r -> r.getId_voyage() == idModifie &&
                            r.getTitre_voyage().equals(nouveauTitre));

            assertTrue(trouve, "La modification n'a pas été effectuée correctement");

            System.out.println("Test modifier réussi !");

        } catch (SQLException e) {
            System.out.println("Erreur lors du test de modification: " + e.getMessage());
            fail("Exception lors de la modification: " + e.getMessage());
        }
    }

    @Test
    void supprimer() {
        try {
            // Ajouter un voyage spécifique pour le supprimer
            Voyage v = new Voyage(
                    "Voyage à supprimer",
                    Date.valueOf("2026-07-01"),
                    Date.valueOf("2026-07-10"),
                    "a venir",
                    1
            );

            vc.ajouter(v);

            // Récupérer l'ID du dernier voyage ajouté
            List<Voyage> data = vc.afficher();
            int idASupprimer = -1;
            String titreASupprimer = "Voyage à supprimer";

            for (Voyage voyage : data) {
                if (voyage.getTitre_voyage().equals(titreASupprimer)) {
                    idASupprimer = voyage.getId_voyage();
                    break;
                }
            }

            assertNotEquals(-1, idASupprimer, "Voyage à supprimer non trouvé");

            // Sauvegarder l'ID dans une variable finale pour la lambda
            final int finalIdASupprimer = idASupprimer;

            // Supprimer le voyage
            vc.supprimer(finalIdASupprimer);

            // Vérifier la suppression
            List<Voyage> newData = vc.afficher();
            boolean trouve = newData.stream()
                    .anyMatch(r -> r.getId_voyage() == finalIdASupprimer);

            assertFalse(trouve, "Le voyage n'a pas été supprimé correctement");

            System.out.println(" Test supprimer réussi !");

        } catch (SQLException e) {
            System.out.println("Erreur lors du test de suppression: " + e.getMessage());
            fail("Exception lors de la suppression: " + e.getMessage());
        }
    }
}