package Utils;

import java.sql.*;

    public class MyBD {

        private Connection conn;
        private final String URL = "jdbc:mysql://localhost:3306/travelmate?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
        private final String USER = "root";
        private final String PASS = "";
        private static MyBD instance;
        /*
                le singleton empêche l'instanciation directe de la classe en rendant le constructeur privé.
                On fournit une méthode statique getInstance() qui crée une nouvelle instance seulement si elle n'existe pas déjà.
                Les etapes pour implementer le singleton sont:
                1- rendre le constructeur privé
                2- créer une variable statique pour stocker l'instance unique de la classe
                3- fournir une méthode statique qui retourne l'instance unique, en la créant si nécessaire
            */
        private MyBD() {
            try {
                // Charger le driver MySQL
                Class.forName("com.mysql.cj.jdbc.Driver");
                conn = DriverManager.getConnection(URL, USER, PASS);
                System.out.println("✓ Connected to database successfully.");
            } catch (ClassNotFoundException e) {
                System.err.println("✗ MySQL Driver not found: " + e.getMessage());
                e.printStackTrace();
            } catch (SQLException e) {
                System.err.println("✗ Database connection failed: " + e.getMessage());
                e.printStackTrace();
            }
        }

        public static MyBD getInstance() {
            if (instance == null) {
                instance = new MyBD();
            }
            return instance;
        }

        public Connection getConn() {
            try {
                if (conn == null || conn.isClosed()) {
                    System.out.println("Connexion fermée, tentative de reconnexion...");
                    Class.forName("com.mysql.cj.jdbc.Driver");
                    conn = DriverManager.getConnection(URL, USER, PASS);
                    System.out.println("✓ Reconnected successfully.");
                }
            } catch (ClassNotFoundException e) {
                System.err.println("✗ MySQL Driver not found during reconnect: " + e.getMessage());
            } catch (SQLException e) {
                System.err.println("✗ Reconnection failed: " + e.getMessage());
                conn = null;
            }
            return conn;
        }

        public void setConn(Connection conn) {
            this.conn = conn;
        }
    }