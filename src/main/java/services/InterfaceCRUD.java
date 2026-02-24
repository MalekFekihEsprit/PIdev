package services;

import java.sql.SQLException;
import java.util.List;

public interface InterfaceCRUD <T>{
    void ajouter(T object) throws SQLException;
    void modifier(T object) throws SQLException;
    void supprimer(T object) throws SQLException;
    List<T> afficher() throws SQLException;
}
