package Utils;

import java.sql.*;

public class MyBD {

    private Connection conn;

    private final String URL = "jdbc:mysql://localhost:3306/esprit3a9";
    private final String USER = "root";
    private final String PASS = "";
    private static MyBD instance; // variable statique pour l'instance unique
    /*
        le singleton empêche l'instanciation directe de la classe en rendant le constructeur privé.
        On fournit une méthode statique getInstance() qui crée une nouvelle instance seulement si elle n'existe pas déjà.
        Les etapes pour implementer le singleton sont:
        1- rendre le constructeur privé
        2- créer une variable statique pour stocker l'instance unique de la classe
        3- fournir une méthode statique qui retourne l'instance unique, en la créant si nécessaire
    */

    private MyBD() { // constructeur privé
        try {
            conn = DriverManager.getConnection(URL, USER, PASS);
            System.out.println("Connected to database.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static MyBD getInstance() { // méthode statique pour obtenir l'instance unique
        if (instance == null) {
            instance = new MyBD();
        }
        return instance;
    }

    public Connection getConn() {
        return conn;
    }
    public void setConn(Connection conn) {
        this.conn = conn;
    }
}
