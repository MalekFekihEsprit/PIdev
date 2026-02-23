package Services;

import Entities.Participation;
import Utils.MyBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ParticipationCRUD implements InterfaceCRUD<Participation>{
    Connection conn;

    public ParticipationCRUD() {
        conn= MyBD.getInstance().getConn();
    }
    @Override
    public void ajouter(Participation participation) throws SQLException {
        String check = "SELECT COUNT(*) FROM participation WHERE id=? AND id_voyage=?";
        PreparedStatement pstCheck = conn.prepareStatement(check);
        pstCheck.setInt(1, participation.getId());
        pstCheck.setInt(2, participation.getId_voyage());

        ResultSet rs = pstCheck.executeQuery();

        if (rs.next() && rs.getInt(1) == 0) {
            String req = "INSERT INTO participation (id, role_participation, id_voyage) VALUES (?, ?, ?)";
            PreparedStatement pstInsert = conn.prepareStatement(req);
            pstInsert.setInt(1, participation.getId());
            pstInsert.setString(2, participation.getRole_participation());
            pstInsert.setInt(3, participation.getId_voyage());
            pstInsert.executeUpdate();

            System.out.println("participation ajoutée !");
        } else {
            System.out.println("participation déjà existante, ajout ignoré !");
        }



        /*String req=
                "insert into participation (id, role_participation,id_voyage) " +
                        "values('" + participation.getId() + "','"
                        + participation.getRole_participation() + "'"
                        +  "," + participation.getId_voyage() + ")";

        Statement st=conn.createStatement();
        st.executeUpdate(req);
        System.out.println("Participation ajoutée !");*/

    }

    @Override
    public void modifier(Participation participation) throws SQLException {
        String req="update participation set id=?,role_participation=?,id_voyage=? where id_participation=?";

        PreparedStatement pst=conn.prepareStatement(req);
        pst.setInt(1, participation.getId());
        pst.setString(2, participation.getRole_participation());
        pst.setInt(3, participation.getId_voyage());
        pst.setInt(4, participation.getId_participation());
        pst.executeUpdate();
        System.out.println("Participation modifiée");

    }

    @Override
    public void supprimer(int id) throws SQLException {
        String req="delete from participation where id_participation=?";
        PreparedStatement pst=conn.prepareStatement(req);
        pst.setInt(1, id);
        pst.executeUpdate();
        System.out.println("Participation supprimer");

    }

    @Override
    public List<Participation> afficher() throws SQLException {
        String req ="select * from participation";
        Statement st=conn.createStatement();
        ResultSet rs=st.executeQuery(req);
        List<Participation> listepersonnes=new ArrayList<Participation>();

        while(rs.next()){
            Participation p = new Participation();
            p.setId_participation(rs.getInt("id_participation"));
            p.setId(rs.getInt("id"));
            p.setRole_participation(rs.getString("role_participation"));
            p.setId_voyage(rs.getInt("id_voyage"));

            listepersonnes.add(p);
        }
        return listepersonnes;
    }

    // Ajoutez ces méthodes dans ParticipationCRUD.java

    public List<Participation> rechercherParIdParticipation(int id) throws SQLException {
        List<Participation> liste = new ArrayList<>();
        String req = "SELECT * FROM participation WHERE id_participation = ?";
        PreparedStatement pst = conn.prepareStatement(req);
        pst.setInt(1, id);
        ResultSet rs = pst.executeQuery();

        while (rs.next()) {
            Participation p = new Participation();
            p.setId_participation(rs.getInt("id_participation"));
            p.setId(rs.getInt("id"));
            p.setRole_participation(rs.getString("role_participation"));
            p.setId_voyage(rs.getInt("id_voyage"));
            liste.add(p);
        }
        return liste;
    }

    public List<Participation> rechercherParRole(String role) throws SQLException {
        List<Participation> liste = new ArrayList<>();
        String req = "SELECT * FROM participation WHERE role_participation = ?";
        PreparedStatement pst = conn.prepareStatement(req);
        pst.setString(1, role);
        ResultSet rs = pst.executeQuery();

        while (rs.next()) {
            Participation p = new Participation();
            p.setId_participation(rs.getInt("id_participation"));
            p.setId(rs.getInt("id"));
            p.setRole_participation(rs.getString("role_participation"));
            p.setId_voyage(rs.getInt("id_voyage"));
            liste.add(p);
        }
        return liste;
    }
}
