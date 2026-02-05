package Tests;

import Entities.Participation;
import Services.ParticipationCRUD;
import Utils.MyBD;

public class MainConnection {
    public static void main(String[] args) {

        MyBD myBD = MyBD.getInstance();
        //MyBD myBD2 = MyBD.getInstance();
        //MyBD myBD3 = MyBD.getInstance();

        Participation p1 = new Participation("ghannem", "minyar", 20);
        Participation p2 = new Participation("amira", "zm", 4);

        Participation p3 = new Participation("", "bedis", 28);


        ParticipationCRUD pc = new ParticipationCRUD();
         try {
        pc.ajouter(p1);
        pc.ajouter(p2);
        pc.ajouter(p3);

        } catch (SQLException e) {
           e.printStackTrace();
        }
        try {
            System.out.println(pc.afficher());
        } catch (SQLException s) {
            s.printStackTrace();
        }
    }
}
