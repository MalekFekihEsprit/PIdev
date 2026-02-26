package Services;

import java.sql.SQLException;
import java.util.List;

public interface InterfaceCRUD <T> {
    void ajouter(T t) throws SQLException;
    boolean supprimer(int id) throws SQLException;
    List<T> afficherById(int id) throws SQLException;
    List<T> afficherAll() throws SQLException;
}