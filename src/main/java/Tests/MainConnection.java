package Tests;

import Entites.Itineraire;
import Entites.etape;
import Services.etapeCRUD;
import Services.itineraireCRUD;
import Utils.MyBD;

import java.sql.Date;
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

        etape e1 = new etape(1, Date.valueOf("2026-02-10"), "Tunis", "Visite de la médina", 25, 1);
        etape e2 = new etape(2, Date.valueOf("2026-02-11"), "Sousse", "Visite du ribat", 2, 1);
        etape e3 = new etape(3, Date.valueOf("2026-02-12"), "Kairouan", "Grande mosquée", 20, 2);
        etapeCRUD ec = new etapeCRUD();

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

        System.out.println("----____----____----____----____----____----____----____----____----____----____----____----____");

        try {
            ec.ajouter(e1);
            ec.ajouter(e2);
            ec.ajouter(e3);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            System.out.println(ec.afficher());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        /*
        try {
            ec.supprimer(3);
            System.out.println(ec.afficher());
        } catch (SQLException e) {
            e.printStackTrace();
        } */

        try {
            etape updatedEtape = new etape();
            updatedEtape.setId_etape(2);
            updatedEtape.setJour(4);
            updatedEtape.setHeure(Date.valueOf("2026-02-15"));
            updatedEtape.setLieu("Monastir");
            updatedEtape.setDescription_etape("Visite du mausolée");
            updatedEtape.setId_activite(5);
            updatedEtape.setId_itineraire(3);

            ec.modifier(updatedEtape);
            System.out.println(ec.afficher());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            System.out.println("Etapes triées par jour :");
            System.out.println(ec.trierParJour());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}