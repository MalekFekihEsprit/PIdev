package Services;

import Entities.User;
import Utils.MyBD;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.mindrot.jbcrypt.BCrypt;

public class UserCRUD implements InterfaceCRUD<User> {
    private Connection conn;

    public UserCRUD() {
        conn = MyBD.getInstance().getConn();
    }

    // ================= PASSWORD UTILS =================

    private String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));
    }

    private boolean verifyPassword(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }

    @Override
    public void ajouter(User user) throws SQLException {
        String req = "INSERT INTO user (nom, prenom, date_naissance, email, telephone, mot_de_passe, role, photo_url, photo_file_name, face_embedding, verification_code, is_verified, last_login_ip, last_login_location, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement pst = conn.prepareStatement(req);
        pst.setString(1, user.getNom());
        pst.setString(2, user.getPrenom());
        pst.setDate(3, user.getDateNaissance() != null ? Date.valueOf(user.getDateNaissance()) : null);
        pst.setString(4, user.getEmail());
        pst.setString(5, user.getTelephone());
        pst.setString(6, hashPassword(user.getMotDePasse()));
        pst.setString(7, user.getRole());
        pst.setString(8, user.getPhotoUrl());
        pst.setString(9, user.getPhotoFileName());
        pst.setString(10, user.getFaceEmbedding());
        pst.setString(11, null); // verification_code
        pst.setBoolean(12, false); // is_verified
        pst.setString(13, null); // last_login_ip
        pst.setString(14, null); // last_login_location
        pst.setTimestamp(15, Timestamp.valueOf(LocalDateTime.now())); // created_at
        pst.executeUpdate();
    }

    public void modifier(User user, boolean updatePassword) throws SQLException {
    if (updatePassword) {
        String req = "UPDATE user SET nom=?, prenom=?, date_naissance=?, email=?, telephone=?, mot_de_passe=?, role=?, photo_url=?, photo_file_name=?, face_embedding=?, verification_code=?, is_verified=?, last_login_ip=?, last_login_location=? WHERE id=?";
        PreparedStatement pst = conn.prepareStatement(req);
        pst.setString(1, user.getNom());
        pst.setString(2, user.getPrenom());
        pst.setDate(3, user.getDateNaissance() != null ? Date.valueOf(user.getDateNaissance()) : null);
        pst.setString(4, user.getEmail());
        pst.setString(5, user.getTelephone());
        pst.setString(6, user.getMotDePasse()); // déjà haché
        pst.setString(7, user.getRole());
        pst.setString(8, user.getPhotoUrl());
        pst.setString(9, user.getPhotoFileName());
        pst.setString(10, user.getFaceEmbedding());
        pst.setString(11, user.getVerificationCode());
        pst.setBoolean(12, user.isVerified());
        pst.setString(13, user.getLastLoginIp());
        pst.setString(14, user.getLastLoginLocation());
        pst.setInt(15, user.getId());
        pst.executeUpdate();
    } else {
        String req = "UPDATE user SET nom=?, prenom=?, date_naissance=?, email=?, telephone=?, role=?, photo_url=?, photo_file_name=?, face_embedding=?, verification_code=?, is_verified=?, last_login_ip=?, last_login_location=? WHERE id=?";
        PreparedStatement pst = conn.prepareStatement(req);
        pst.setString(1, user.getNom());
        pst.setString(2, user.getPrenom());
        pst.setDate(3, user.getDateNaissance() != null ? Date.valueOf(user.getDateNaissance()) : null);
        pst.setString(4, user.getEmail());
        pst.setString(5, user.getTelephone());
        pst.setString(6, user.getRole());
        pst.setString(7, user.getPhotoUrl());
        pst.setString(8, user.getPhotoFileName());
        pst.setString(9, user.getFaceEmbedding());
        pst.setString(10, user.getVerificationCode());
        pst.setBoolean(11, user.isVerified());
        pst.setString(12, user.getLastLoginIp());
        pst.setString(13, user.getLastLoginLocation());
        pst.setInt(14, user.getId());
        pst.executeUpdate();
    }
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
        u.setPhotoFileName(rs.getString("photo_file_name"));
        u.setFaceEmbedding(rs.getString("face_embedding")); // <-- AJOUT
        u.setVerificationCode(rs.getString("verification_code"));
        u.setVerified(rs.getBoolean("is_verified"));
        u.setLastLoginIp(rs.getString("last_login_ip"));
        u.setLastLoginLocation(rs.getString("last_login_location"));
        u.setCreatedAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null);
        return u;
    }

    // Méthode de vérification du mot de passe
    public boolean checkPassword(String email, String plainPassword) throws SQLException {
        String req = "SELECT mot_de_passe FROM user WHERE email = ?";
        PreparedStatement pst = conn.prepareStatement(req);
        pst.setString(1, email);
        ResultSet rs = pst.executeQuery();
        if (rs.next()) {
            String hashed = rs.getString("mot_de_passe");
            return BCrypt.checkpw(plainPassword, hashed);
        }
        return false;
    }

    public User getUserByEmailAndPassword(String email, String plainPassword) throws SQLException {

        String req = "SELECT * FROM user WHERE email = ?";

        try (PreparedStatement pst = conn.prepareStatement(req)) {

            pst.setString(1, email);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {

                String hashedPassword = rs.getString("mot_de_passe");

                if (BCrypt.checkpw(plainPassword, hashedPassword)) {
                    return mapResultSetToUser(rs);
                }
            }
        }

        return null;
    }

    public User getUserByEmail(String email) throws SQLException {
        String req = "SELECT * FROM user WHERE email = ?";
        PreparedStatement pst = conn.prepareStatement(req);
        pst.setString(1, email);
        ResultSet rs = pst.executeQuery();
        if (rs.next()) {
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

        try (PreparedStatement pst = conn.prepareStatement(req)) {

            pst.setString(1, hashPassword(newPassword));
            pst.setString(2, email);

            return pst.executeUpdate() > 0;
        }
    }

    public boolean validateEmailAndPassword(String email, String password) {
        try {
            return getUserByEmailAndPassword(email, password) != null;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public void saveVerificationCode(int userId, String code) throws SQLException {
    String req = "UPDATE user SET verification_code = ? WHERE id = ?";
    PreparedStatement pst = conn.prepareStatement(req);
    pst.setString(1, code);
    pst.setInt(2, userId);
    pst.executeUpdate();
}

    public boolean verifyEmail(int userId, String code) throws SQLException {
        String req = "SELECT id FROM user WHERE id = ? AND verification_code = ?";
        PreparedStatement pst = conn.prepareStatement(req);
        pst.setInt(1, userId);
        pst.setString(2, code);
        ResultSet rs = pst.executeQuery();
        if (rs.next()) {
            // Marquer comme vérifié et effacer le code
            String update = "UPDATE user SET is_verified = TRUE, verification_code = NULL WHERE id = ?";
            PreparedStatement pst2 = conn.prepareStatement(update);
            pst2.setInt(1, userId);
            pst2.executeUpdate();
            return true;
        }
        return false;
    }

    public boolean isEmailVerified(String email) throws SQLException {
        String req = "SELECT is_verified FROM user WHERE email = ?";
        PreparedStatement pst = conn.prepareStatement(req);
        pst.setString(1, email);
        ResultSet rs = pst.executeQuery();
        return rs.next() && rs.getBoolean("is_verified");
    }

    public static String getPublicIp() throws Exception {
        URL whatismyip = new URL("http://checkip.amazonaws.com");
        BufferedReader in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));
        return in.readLine();
    }

    public static String getLocationFromIp(String ip) throws Exception {
        String url = "http://ip-api.com/json/" + ip + "?fields=status,country,city,district";
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("GET");

        if (conn.getResponseCode() != 200) {
            return "Inconnu";
        }

        BufferedReader in = new BufferedReader(
                new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            response.append(line);
        }
        in.close();

        JSONObject json = new JSONObject(response.toString());

        if ("success".equals(json.optString("status"))) {
            return json.optString("city") + ", " + json.optString("country") + ", " + json.optString("district");
        }

        return "Inconnu";
    }

    public void updateLastLogin(int userId, String ip, String location) throws SQLException {
        String req = "UPDATE user SET last_login_ip = ?, last_login_location = ? WHERE id = ?";
        PreparedStatement pst = conn.prepareStatement(req);
        pst.setString(1, ip);
        pst.setString(2, location);
        pst.setInt(3, userId);
        pst.executeUpdate();
    }

    public void updateFaceEmbedding(int userId, String embeddingJson) throws SQLException {
    String sql = "UPDATE user SET face_embedding = ? WHERE id = ?";
    try (PreparedStatement pst = conn.prepareStatement(sql)) {
        pst.setString(1, embeddingJson);
        pst.setInt(2, userId);
        pst.executeUpdate();
    }
}
}