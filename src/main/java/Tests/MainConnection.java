package Tests;

import Entities.Pers;
import Services.PersCRUD;
import Utils.MyBD;

import java.sql.SQLException;
import java.util.List;

public class MainConnection {

    public static void main(String[] args) throws SQLException {
        MyBD myBD = MyBD.getInstance();
        PersCRUD pc = new PersCRUD();
        Pers p1 = new Pers("Doe", "John", 30);
        Pers p2 = new Pers("Smith", "Jane", 25);
        Pers p3 = new Pers("Brown", "Mike", 40);

        try {
            pc.ajouter(p1);
            pc.ajouter(p2);
            pc.ajouter(p3);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        try {
            pc.modifier(new Pers(1, "Doe", "Johnny", 31));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        try {
            pc.supprimer(2);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        List<Pers> list;
        list = pc.afficherById(1);
        for (Pers p : list) {
            System.out.println(p.getId() + " " + p.getNom() + " " + p.getPrenom() + " " + p.getAge());
        }

        list = pc.afficherAll();
        for (Pers p : list) {
            System.out.println(p.getId() + " " + p.getNom() + " " + p.getPrenom() + " " + p.getAge());
        }

    }
}
