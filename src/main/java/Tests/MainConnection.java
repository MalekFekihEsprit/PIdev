package Tests;

import Entites.Itineraire;
import Services.itineraireCRUD;
import Utils.MyBD;

import java.sql.SQLException;

public class MainConnection {
    public static void main(String[] args) {

        MyBD myBD = MyBD.getInstance();
        //MyBD myBD2 = MyBD.getInstance();
        //MyBD myBD3 = MyBD.getInstance();

        Itineraire i1 = new Itineraire("meriem", "olikuytrfghj", 15);
        Itineraire i2 = new Itineraire("nour", "kjuhygtfrgjhk", 43);
        Itineraire i3 = new Itineraire("khadija", "loiuytgrdgfhj", 67);

        itineraireCRUD pc = new itineraireCRUD();
        try {
            pc.ajouter(i1);
            pc.ajouter(i2);
            pc.ajouter(i3);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            System.out.println(pc.afficher());
        } catch (SQLException s) {
            s.printStackTrace();
        }

        /*try {
            pc.supprimer(4);
            System.out.println(pc.afficher());
        } catch (SQLException e) {
            e.printStackTrace();
        }*/

        try {
            Itineraire updatedItin = new Itineraire();
            updatedItin.setId(2);
            updatedItin.setNom("fatma");
            updatedItin.setDescription("mlokijuhygtfrghjlm");
            updatedItin.setVoyage_id(90);
            pc.modifier(updatedItin);
            System.out.println(pc.afficher());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}