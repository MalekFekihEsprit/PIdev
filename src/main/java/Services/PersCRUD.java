package Services;

import Entities.Pers;
import Utils.MyBD;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PersCRUD implements InterfaceCRUD<Pers> {

    Connection conn;

    public PersCRUD() {
        conn = MyBD.getInstance().getConn();

    }
    @Override
    public void ajouter(Pers pers) throws SQLException {
        String req = "INSERT INTO pers (nom, prenom, age) VALUES ('"+ pers.getNom() + "', '" + pers.getPrenom() + "', " + pers.getAge() + ");"; // requete SQL avec valeurs de l'objet pers
        Statement st = conn.createStatement(); // création d'un objet Statement pour exécuter la requête
        st.executeUpdate(req); // exécution de la requête
        // conn.createStatement().executeUpdate(req); // exécution de la requête en une seule ligne
        System.out.println("Personne ajoutée !");

    }


    public void modifier(Pers pers) throws SQLException {
        String req = "UPDATE pers SET nom = '" + pers.getNom() + "', prenom = '" + pers.getPrenom() + "', age = " + pers.getAge() + " WHERE id = " + pers.getId() + ";"; // requete SQL avec valeurs de l'objet pers
        Statement st = conn.createStatement(); // création d'un objet Statement pour exécuter la requête
        st.executeUpdate(req); // exécution de la requête
        System.out.println("Personne modifiée !");
    }

    @Override
    public boolean supprimer(int id) throws SQLException {
        String req = "DELETE FROM pers WHERE id = " + id + ";"; // requete SQL avec id
        Statement st = conn.createStatement(); // création d'un objet Statement pour exécuter la requête
        st.executeUpdate(req); // exécution de la requête
        System.out.println("Personne supprimée !");
        return false;
    }

    @Override
    public List<Pers> afficherById(int id) throws SQLException {
        String req = "SELECT * FROM pers WHERE id = " + id + ";"; // requete SQL avec id
        Statement st = conn.createStatement(); // création d'un objet Statement pour exécuter la requête
        ResultSet rs = st.executeQuery(req); // exécution de la requête et récupération du résultat
        List<Pers> persList = new ArrayList<>();
        while (rs.next()) {
            Pers p = new Pers();
            p.setId(rs.getInt("id"));
            p.setNom(rs.getString("nom"));
            p.setPrenom(rs.getString("prenom"));
            p.setAge(rs.getInt("age"));
            persList.add(p);
        }
        System.out.println("Personne affichée !");
        return persList;
    }

    @Override
    public List<Pers> afficherAll() throws SQLException {
        String req = "SELECT * FROM pers;"; // requete SQL pour récupérer toutes les personnes
        Statement st = conn.createStatement(); // création d'un objet Statement pour exécuter la requête
        ResultSet rs = st.executeQuery(req); // exécution de la requête et récupération du résultat
        List<Pers> persList = new ArrayList<>();
        while (rs.next()) {
            Pers p = new Pers();
            p.setId(rs.getInt("id"));
            p.setNom(rs.getString("nom"));
            p.setPrenom(rs.getString("prenom"));
            p.setAge(rs.getInt("age"));
            persList.add(p);
        }
        System.out.println("Toutes les personnes affichées !");
        return persList;
    }
}

/*
    @Override
    public void ajouter(Pers pers) throws SQLException {
        String req = "INSERT INTO pers (nom, prenom, age) VALUES (?, ?, ?)";
        PreparedStatement ps = conn.prepareStatement(req);
        ps.setString(1, pers.getNom());
        ps.setString(2, pers.getPrenom());
        ps.setInt(3, pers.getAge());
        ps.executeUpdate();

    }

    @Override
    public void modifier(Pers pers) throws SQLException {
        String req = "UPDATE pers SET nom = ?, prenom = ?, age = ? WHERE id = ?";
        PreparedStatement ps = conn.prepareStatement(req);
        ps.setString(1, pers.getNom());
        ps.setString(2, pers.getPrenom());
        ps.setInt(3, pers.getAge());
        ps.setInt(4, pers.getId());
        ps.executeUpdate();

    }

    @Override
    public void supprimer(int id) throws SQLException {
        String req = "DELETE FROM pers WHERE id = ?";
        PreparedStatement ps = conn.prepareStatement(req);
        ps.setInt(1, id);
        ps.executeUpdate();

    }

    @Override
    public List<Pers> afficherById(int id) throws SQLException {
        String req = "SELECT * FROM pers WHERE id = ?";
        PreparedStatement ps = conn.prepareStatement(req);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        List<Pers> persList = new ArrayList<>();
        while (rs.next()) {
            Pers pers = new Pers();
            pers.setId(rs.getInt("id"));
            pers.setNom(rs.getString("nom"));
            pers.setPrenom(rs.getString("prenom"));
            pers.setAge(rs.getInt("age"));
            persList.add(pers);
        }
        return persList;
    }
*/
