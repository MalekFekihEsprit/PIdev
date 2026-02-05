package Services;

import Entities.Participation;
import Entities.Voyage;
import Utils.MyBD;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class VoyageCRUD implements InterfaceCRUD<Voyage>{
    Connection conn;
    public VoyageCRUD() {
        conn= MyBD.getInstance().getConn();
    }
    @Override
    public void ajouter(Voyage voyage) throws SQLException {

    }

    @Override
    public void modifier(Voyage voyage) throws SQLException {

    }

    @Override
    public void supprimer(int id) throws SQLException {

    }

    @Override
    public List<Voyage> afficher() throws SQLException {
        return List.of();
    }
}
