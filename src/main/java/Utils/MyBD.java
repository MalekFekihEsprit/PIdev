package Utils;

import java.sql.*;

public class MyBD {

    private Connection conn;
    private static MyBD instance;

    private static final String URL  = "jdbc:mysql://172.20.10.9:3306/travelmate?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String USER = "Travelmate";
    private static final String PASS = "Travelmate";

    private MyBD() {
        connect();
    }

    public static MyBD getInstance() {
        if (instance == null) {
            instance = new MyBD();
        }
        return instance;
    }

    /**
     * Retourne TOUJOURS une connexion valide et ouverte.
     * Si la connexion est fermée ou nulle, elle est recréée automatiquement.
     *
     * IMPORTANT : ne jamais appeler conn.close() ni utiliser getConn()
     * dans un try-with-resources — cela fermerait la connexion singleton.
     * Fermez uniquement les PreparedStatement et ResultSet, pas la Connection.
     */
    public Connection getConn() {
        try {
            if (conn == null || conn.isClosed() || !conn.isValid(2)) {
                System.out.println("⚠ Connexion invalide, reconnexion en cours...");
                connect();
            }
        } catch (SQLException e) {
            System.err.println("✗ Erreur lors du test de connexion : " + e.getMessage());
            connect();
        }
        return conn;
    }

    private void connect() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(URL, USER, PASS);
            System.out.println("✓ Connexion à la base de données établie.");
        } catch (ClassNotFoundException e) {
            System.err.println("✗ Driver MySQL introuvable : " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("✗ Connexion échouée : " + e.getMessage());
            conn = null;
        }
    }

    // Gardé pour compatibilité ascendante si utilisé ailleurs
    public void setConn(Connection conn) {
        this.conn = conn;
    }
}