
package Tools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyBD {
    private Connection conn;
    private final String URL = "jdbc:mysql://localhost:3306/travelmate";
    private final String USER = "root";
    private final String PASS = "";
    private static MyBD instance;

    private MyBD() {
        try {
            this.conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/travelmate", "root", "");
            System.out.println("Connected");
        } catch (SQLException s) {
            System.out.println(s.getMessage());
        }

    }

    public static MyBD getInstance() {
        if (instance == null) {
            instance = new MyBD();
        }

        return instance;
    }

    public Connection getConn() {
        return this.conn;
    }
}

