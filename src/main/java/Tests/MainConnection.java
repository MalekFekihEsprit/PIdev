/*package Tests;

import Entities.Participation;
import Entities.Voyage;
import Services.ParticipationCRUD;
import Services.VoyageCRUD;
import Utils.MyBD;

import java.sql.SQLException;
import java.util.Date;

public class MainConnection {
    public static void main(String[] args) {

        MyBD myBD = MyBD.getInstance();
        //MyBD myBD2 = MyBD.getInstance();
        //MyBD myBD3 = MyBD.getInstance();

        Participation p1 = new Participation(1875, "organisateur", 20);
        //Participation p2 = new Participation(3445, "participant", 4);

        //Participation p3 = new Participation(1314, "organisateur", 28);


        ParticipationCRUD pc = new ParticipationCRUD();
         try {
        pc.ajouter(p1);

        //pc.ajouter(p2);
        //pc.ajouter(p3);

        } catch (SQLException e) {
           e.printStackTrace();
        }
        try {
            System.out.println(pc.afficher());
        } catch (SQLException s) {
            s.printStackTrace();
        }

        //test modifier et supprimer
        try {
            pc.supprimer(10);
            pc.supprimer(12);
            pc.supprimer(9);
            System.out.println(pc.afficher());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            Participation updatedPart = new Participation();
            updatedPart.setId_participation(1);
            updatedPart.setId(20052001);
            updatedPart.setRole_participation("MINYAR");
            updatedPart.setId_voyage(90);
            pc.modifier(updatedPart);
            System.out.println(pc.afficher());
        } catch (SQLException e) {
            e.printStackTrace();
        }



        Voyage v1 = new Voyage(
                "Voyage Rome",
                new Date(),
                new Date(),
                "En cours",
                1
        );

        Voyage v2 = new Voyage("Voyage Paris", new Date(), new Date(),  "Planifié", 2);
        Voyage v3 = new Voyage("Voyage Berlin", new Date(), new Date(),  "Terminé", 3);

        VoyageCRUD vc = new VoyageCRUD();

        try {
            vc.ajouter(v1);
            vc.ajouter(v2);
            vc.ajouter(v3);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            System.out.println(vc.afficher());
        } catch (SQLException s) {
            s.printStackTrace();
        }
    }
}*/
