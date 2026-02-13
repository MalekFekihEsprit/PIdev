package Utils;
import java.sql.*;

public class MyBD {
    private Connection conn;
    final private String URL = "jdbc:mysql://localhost:3306/travelmate";
    final private String USER = "root";
    final private String PASS = "";
    private static MyBD instance;

    private MyBD() {
        // Constructeur vide - la connexion sera créée à la demande
    }

    public static MyBD getInstance() {
        if (instance == null) {
            instance = new MyBD();
        }
        return instance;
    }

    public Connection getConn() {
        try {
            // Vérifier si la connexion est nulle ou fermée
            if (conn == null || conn.isClosed()) {
                conn = DriverManager.getConnection(URL, USER, PASS);
                System.out.println("Nouvelle connexion établie");
            }
        } catch (SQLException e) {
            System.out.println("Erreur de connexion: " + e.getMessage());
            e.printStackTrace();
        }
        return conn;
    }
}