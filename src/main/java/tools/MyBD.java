package tools;
import java.sql.*;
public class MyBD {


    Connection conn;
    final private String URL = "jdbc:mysql://localhost:3306/pi_dev";
    final private String USER = "root";
    final private String PASS = "";
    private static MyBD myBD;
    private MyBD(){
        try {
            conn=DriverManager.getConnection(URL,USER,PASS);
            System.out.println("Connected to database successfully");
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    };
    public static MyBD getInstance(){
        if (myBD==null){
            myBD = new MyBD();
        }
        return myBD;
    }

    public Connection getConn() {
        return conn;
    }

    public void setConn(Connection conn) {
        this.conn = conn;
    }
}
