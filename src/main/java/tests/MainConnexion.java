package tests;
import entities.Destination;
import services.DestinationCRUD;
import tools.MyBD;

import java.util.ArrayList;
import java.util.List;

public class MainConnexion {
    public static void main(String[] args) {
        MyBD myBD = MyBD.getInstance();
        DestinationCRUD pc = new DestinationCRUD();
        List<Destination> destinations = new ArrayList<Destination>();
        Destination test = new Destination(1,"esm","bled","desc","climat","saison");
        /*
        try
        {
            pc.ajouter(test);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

         */
        /*
        try

        {
            destinations=pc.afficher();
            System.out.println(destinations);
        }
        catch (Exception e) {
            e.printStackTrace();
        } */
        /*try
        {
            pc.supprimer(test);
        }
        catch (Exception e) {
            e.printStackTrace();
        }*/
        /*test.setNom_destination("modif");
        try
        {
            pc.modifier(test);
        }
        catch (Exception e) {
            e.printStackTrace();
        }*/

    }
}