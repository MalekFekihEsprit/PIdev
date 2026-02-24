package test;

import services.DestinationCRUD;
import services.HebergementCRUD;
import entities.Destination;
import entities.Hebergement;

import java.sql.SQLException;
import java.util.List;

public class MainCRUDs {

    public static void main(String[] args) {

        DestinationCRUD destinationCRUD = new DestinationCRUD();
        HebergementCRUD hebergementCRUD = new HebergementCRUD();

        try {
            Destination d = new Destination(
                    "nom",
                    "pays",
                    "description",
                    "climat",
                    "saision"
            );
            destinationCRUD.ajouter(d);

            List<Destination> destinations = destinationCRUD.afficher();
            for (Destination dest : destinations) {
                System.out.println(dest);
            }


            d.setClimat_destination("test modification");
            d.setSaison_destination("Printemps");
            destinationCRUD.modifier(d);

            destinations = destinationCRUD.afficher();
            for (Destination dest : destinations) {
                System.out.println(dest);
            }

            Hebergement h = new Hebergement(
                    "test",
                    "test",
                    0,
                    "Via Roma 21",
                    0,
                    0,
                    0,
                    0,
                    d
            );
            hebergementCRUD.ajouter(h);

            List<Hebergement> hebergements = hebergementCRUD.afficher();
            for (Hebergement heb : hebergements) {
                System.out.println(
                        heb.getNom_hebergement() + " | "
                                + heb.getType_hebergement() + " | "
                                + heb.getPrixNuit_hebergement() + " | Destination = "
                                + (heb.getDestination() != null ? heb.getDestination().getNom_destination() : "null")
                );
            }


            h.setPrixNuit_hebergement(210.0);
            h.setNote_hebergement(4.8);
            hebergementCRUD.modifier(h);

            hebergements = hebergementCRUD.afficher();
            for (Hebergement heb : hebergements) {
                System.out.println(
                        heb.getNom_hebergement() + " | "
                                + heb.getPrixNuit_hebergement() + " | "
                                + heb.getNote_hebergement() + " | Destination = "
                                + (heb.getDestination() != null ? heb.getDestination().getNom_destination() : "null")
                );
            }
            h.setId_hebergement(5);
            hebergementCRUD.supprimer(h);
            d.setId_destination(7);
            destinationCRUD.supprimer(d);


        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
