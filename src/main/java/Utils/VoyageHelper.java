package Utils;

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
     * Récupère tous les détails d'un voyage, y compris la destination et sa saison idéale.
     */
    public VoyageDetails getVoyageDetails(int idVoyage) throws SQLException {
        String sql = "SELECT v.id_voyage, v.titre_voyage, v.date_debut, v.date_fin, " +
                "d.id_destination, d.nom_destination, d.saison_ideale " +
                "FROM voyage v " +
                "LEFT JOIN destination d ON v.id_destination = d.id_destination " +
                "WHERE v.id_voyage = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idVoyage);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new VoyageDetails(
                            rs.getInt("id_voyage"),
                            rs.getString("titre_voyage"),
                            rs.getDate("date_debut"),
                            rs.getDate("date_fin"),
                            rs.getInt("id_destination"),
                            rs.getString("nom_destination"),
                            rs.getString("saison_ideale")
                    );
                }
            }
        }
        return null;
    }

    // ===== Classes internes =====
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
            if (dateDebut == null || dateFin == null) return "Dates non définies";
            return dateDebut.toString() + " → " + dateFin.toString();
        }
    }

    public static class VoyageDetails {
        private int idVoyage;
        private String titre;
        private Date dateDebut;
        private Date dateFin;
        private int idDestination;
        private String nomDestination;
        private String saisonIdeale;

        public VoyageDetails(int idVoyage, String titre, Date dateDebut, Date dateFin,
                             int idDestination, String nomDestination, String saisonIdeale) {
            this.idVoyage = idVoyage;
            this.titre = titre;
            this.dateDebut = dateDebut;
            this.dateFin = dateFin;
            this.idDestination = idDestination;
            this.nomDestination = nomDestination;
            this.saisonIdeale = saisonIdeale;
        }

        public int getIdVoyage() { return idVoyage; }
        public String getTitre() { return titre; }
        public Date getDateDebut() { return dateDebut; }
        public Date getDateFin() { return dateFin; }
        public int getIdDestination() { return idDestination; }
        public String getNomDestination() { return nomDestination; }
        public String getSaisonIdeale() { return saisonIdeale; }

        public long getDureeJours() {
            if (dateDebut == null || dateFin == null) return 0;
            long diff = dateFin.getTime() - dateDebut.getTime();
            return diff / (1000 * 60 * 60 * 24);
        }
    }
}