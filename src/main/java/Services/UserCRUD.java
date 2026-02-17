package Services;

import Entities.User;
import Utils.MyBD;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserCRUD implements InterfaceCRUD<User> {
    private Connection conn;

    public UserCRUD() {
        conn = MyBD.getInstance().getConn();
    }

    @Override
    public void ajouter(User user) throws SQLException {
        String req = "INSERT INTO user (nom, prenom, date_naissance, email, telephone, mot_de_passe, role, photo_url) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement pst = conn.prepareStatement(req);
        pst.setString(1, user.getNom());
        pst.setString(2, user.getPrenom());
        pst.setDate(3, user.getDateNaissance() != null ? Date.valueOf(user.getDateNaissance()) : null);
        pst.setString(4, user.getEmail());
        pst.setString(5, user.getTelephone());
        pst.setString(6, user.getMotDePasse());
        pst.setString(7, user.getRole());
        pst.setString(8, user.getPhotoUrl());
        pst.executeUpdate();
        System.out.println("Utilisateur ajouté !");
    }

    @Override
    public void modifier(User user) throws SQLException {
        String req = "UPDATE user SET nom=?, prenom=?, date_naissance=?, email=?, telephone=?, mot_de_passe=?, role=?, photo_url=? WHERE id=?";
        PreparedStatement pst = conn.prepareStatement(req);
        pst.setString(1, user.getNom());
        pst.setString(2, user.getPrenom());
        pst.setDate(3, user.getDateNaissance() != null ? Date.valueOf(user.getDateNaissance()) : null);
        pst.setString(4, user.getEmail());
        pst.setString(5, user.getTelephone());
        pst.setString(6, user.getMotDePasse());
        pst.setString(7, user.getRole());
        pst.setString(8, user.getPhotoUrl());
        pst.setInt(9, user.getId());
        pst.executeUpdate();
        System.out.println("Utilisateur modifié !");
    }

    @Override
    public boolean supprimer(int id) throws SQLException {
        String req = "DELETE FROM user WHERE id=?";
        PreparedStatement pst = conn.prepareStatement(req);
        pst.setInt(1, id);
        pst.executeUpdate();
        System.out.println("Utilisateur supprimé !");
        return true;
    }

    @Override
    public List<User> afficherById(int id) throws SQLException {
        List<User> list = new ArrayList<>();
        String req = "SELECT * FROM user WHERE id=?";
        PreparedStatement pst = conn.prepareStatement(req);
        pst.setInt(1, id);
        ResultSet rs = pst.executeQuery();
        while (rs.next()) {
            list.add(mapResultSetToUser(rs));
            System.out.println("Utilisateur trouvé : " + rs.getString("email"));
        }
        return list;
    }

    @Override
    public List<User> afficherAll() throws SQLException {
        List<User> list = new ArrayList<>();
        String req = "SELECT * FROM user";
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(req);
        while (rs.next()) {
            list.add(mapResultSetToUser(rs));
            System.out.println("Utilisateur trouvé : " + rs.getString("email"));
        }
        return list;
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException { 
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setNom(rs.getString("nom"));
        u.setPrenom(rs.getString("prenom"));
        u.setDateNaissance(rs.getDate("date_naissance") != null ? rs.getDate("date_naissance").toLocalDate() : null);
        u.setEmail(rs.getString("email"));
        u.setTelephone(rs.getString("telephone"));
        u.setMotDePasse(rs.getString("mot_de_passe"));
        u.setRole(rs.getString("role"));
        u.setPhotoUrl(rs.getString("photo_url"));
        return u;
    }


    public User getUserByEmailAndPassword(String email, String password) throws SQLException {
        String req = "SELECT * FROM user WHERE email = ? AND mot_de_passe = ?";
        PreparedStatement pst = conn.prepareStatement(req);
        pst.setString(1, email);
        pst.setString(2, password);
        ResultSet rs = pst.executeQuery();
        if (rs.next()) {
            System.out.println("Utilisateur trouvé : " + rs.getString("email"));
            return mapResultSetToUser(rs);
        }
        return null;
    }

    public boolean emailExists(String email) throws SQLException {
        String req = "SELECT id FROM user WHERE email = ?";
        PreparedStatement pst = conn.prepareStatement(req);
        pst.setString(1, email);
        ResultSet rs = pst.executeQuery();
        System.out.println("Vérification de l'email : " + email);
        return rs.next();
    }

    public boolean updatePassword(String email, String newPassword) throws SQLException {
        String req = "UPDATE user SET mot_de_passe = ? WHERE email = ?";
        PreparedStatement pst = conn.prepareStatement(req);
        pst.setString(1, newPassword);
        pst.setString(2, email);
        System.out.println("Mise à jour du mot de passe pour : " + email);
        return pst.executeUpdate() > 0;
    }

    public boolean deleteUser(int id) throws SQLException {
        String req = "DELETE FROM user WHERE id = ?";
        PreparedStatement pst = conn.prepareStatement(req);
        pst.setInt(1, id);
        System.out.println("Suppression de l'utilisateur avec ID : " + id);
        return pst.executeUpdate() > 0;
    }
}