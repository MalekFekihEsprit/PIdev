package Tools;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class VoyageHelper {

    private Connection conn = MyBD.getInstance().getConn();

    /**
     * Récupère tous les voyages avec leurs informations complètes
     */
    public Map<Integer, VoyageInfo> getAllVoyagesInfo() throws SQLException {
        Map<Integer, VoyageInfo> voyages = new HashMap<>();
        String sql = "SELECT v.id_voyage, v.titre_voyage, v.date_debut, v.date_fin, v.statut, v.id_destination, " +
                "d.nom_destination " +
                "FROM voyage v " +
                "LEFT JOIN destination d ON v.id_destination = d.id_destination " +
                "ORDER BY v.date_debut DESC";

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                VoyageInfo info = new VoyageInfo(
                        rs.getInt("id_voyage"),
                        rs.getString("titre_voyage"),
                        rs.getDate("date_debut"),
                        rs.getDate("date_fin"),
                        rs.getString("statut"),
                        rs.getInt("id_destination"),
                        rs.getString("nom_destination")
                );
                voyages.put(rs.getInt("id_voyage"), info);
            }
        }
        return voyages;
    }

    /**
     * Récupère un voyage par son ID
     */
    public VoyageInfo getVoyageById(int idVoyage) throws SQLException {
        String sql = "SELECT v.id_voyage, v.titre_voyage, v.date_debut, v.date_fin, v.statut, v.id_destination, " +
                "d.nom_destination " +
                "FROM voyage v " +
                "LEFT JOIN destination d ON v.id_destination = d.id_destination " +
                "WHERE v.id_voyage = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idVoyage);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new VoyageInfo(
                            rs.getInt("id_voyage"),
                            rs.getString("titre_voyage"),
                            rs.getDate("date_debut"),
                            rs.getDate("date_fin"),
                            rs.getString("statut"),
                            rs.getInt("id_destination"),
                            rs.getString("nom_destination")
                    );
                }
            }
        }
        return null;
    }

    /**
     * Récupère tous les voyages (simplifié)
     */
    public Map<Integer, String> getAllVoyages() throws SQLException {
        Map<Integer, String> voyages = new HashMap<>();
        String sql = "SELECT id_voyage, titre_voyage FROM voyage ORDER BY titre_voyage";

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                voyages.put(rs.getInt("id_voyage"), rs.getString("titre_voyage"));
            }
        }
        return voyages;
    }

    /**
     * Récupère le titre d'un voyage
     */
    public String getVoyageNameById(int idVoyage) throws SQLException {
        if (idVoyage == 0) return "Sans voyage";

        String sql = "SELECT titre_voyage FROM voyage WHERE id_voyage = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idVoyage);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("titre_voyage");
                }
            }
        }
        return "Voyage inconnu";
    }

    /**
     * Récupère le nom de la destination
     */
    public String getDestinationNameById(int idDestination) throws SQLException {
        if (idDestination == 0) return "Destination inconnue";

        String sql = "SELECT nom_destination FROM destination WHERE id_destination = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idDestination);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("nom_destination");
                }
            }
        }
        return "Destination inconnue";
    }

    /**
     * Classe interne pour les informations de voyage
     */
    public static class VoyageInfo {
        private int idVoyage;
        private String titre;
        private Date dateDebut;
        private Date dateFin;
        private String statut;
        private int idDestination;
        private String nomDestination;

        public VoyageInfo(int idVoyage, String titre, Date dateDebut, Date dateFin,
                          String statut, int idDestination, String nomDestination) {
            this.idVoyage = idVoyage;
            this.titre = titre;
            this.dateDebut = dateDebut;
            this.dateFin = dateFin;
            this.statut = statut;
            this.idDestination = idDestination;
            this.nomDestination = nomDestination != null ? nomDestination : "Destination inconnue";
        }

        public int getIdVoyage() { return idVoyage; }
        public String getTitre() { return titre; }
        public Date getDateDebut() { return dateDebut; }
        public Date getDateFin() { return dateFin; }
        public String getStatut() { return statut; }
        public int getIdDestination() { return idDestination; }
        public String getNomDestination() { return nomDestination; }

        public String getDatesFormatted() {
            if (dateDebut == null || dateFin == null) {
                return "Dates non définies";
            }
            return dateDebut.toString() + " → " + dateFin.toString();
        }

        @Override
        public String toString() {
            return titre + " (" + getDatesFormatted() + ")";
        }
    }
}