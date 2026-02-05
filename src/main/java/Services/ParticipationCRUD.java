package Services;

import Entities.Participation;
import Utils.MyBD;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class ParticipationCRUD implements InterfaceCRUD<Participation>{
    Connection conn;

    public ParticipationCRUD() {
        conn= MyBD.getInstance().getConn();
    }
    @Override
    public void ajouter(Participation participation) throws SQLException {
        String req=
                "insert into partcipation (id, role_participation,id_voyage) " +
                        "values('" + participation.getId() + "','"
                        + participation.getRole_participation() + "'"
                        +  "," + participation.getId_voyage() + ")";

        Statement st=conn.createStatement();
        st.executeUpdate(req);
        System.out.println("Participation ajoutée !");

    }

    @Override
    public void modifier(Participation participation) throws SQLException {

    }

    @Override
    public void supprimer(int id) throws SQLException {

    }

    @Override
    public List<Participation> afficher() throws SQLException {
        return List.of();
    }
}
