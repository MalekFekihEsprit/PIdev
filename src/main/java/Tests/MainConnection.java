package Tests;

import Entites.Activites;
import Entites.Categories;
import Services.CategoriesCRUD;
import Utils.MyBD;

import java.sql.SQLException;

public class MainConnection {

    public static void main(String[] args) {

        MyBD myBD = MyBD.getInstance();

        //MyBD myBD2 = MyBD.getInstance();
        //MyBD myBD3 = MyBD.getInstance();

        Categories c1 = new Categories( 22 ,"ab","aaaaa","c","hh","hed","ggggg");
        Categories c2 = new Categories( 24 ,"abc","aaaaa","c","hh","hed","ggggg");



        CategoriesCRUD c = new CategoriesCRUD();
        try {
            c.ajouter(c1);
            c.ajouter(c2);


        } catch (SQLException e) {
           e.printStackTrace();
        }
        try {
            System.out.println(c.afficher());
        } catch (SQLException s) {
            s.printStackTrace();
        }
        try {
            c.supprimer(24);
        }catch (SQLException e){
            e.printStackTrace();
        }


    }
}