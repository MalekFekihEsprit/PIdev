package Services;

import Entities.Itineraire;
import Utils.MyBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Toutes les méthodes appellent MyBD.getInstance().getConn() directement,
 * sans stocker la connexion — même principe que etapeCRUD corrigé.
 */
public class itineraireCRUD implements interfaceCRUDItine<Itineraire> {

    public itineraireCRUD() { }

    /** Raccourci pour obtenir la connexion active. */
    private Connection conn() {
        return MyBD.getInstance().getConn();
    }

    // ════════════════════════════════════════════════════════════════════════
    //  CRUD DE BASE
    // ════════════════════════════════════════════════════════════════════════

    @Override
    public void ajouter(Itineraire itin) throws SQLException {
        if (itin.getNom_itineraire().length() > 10)
            throw new SQLException("Le nom de l'itinéraire ne doit pas dépasser 10 caractères");

        String check = "SELECT COUNT(*) FROM itineraire WHERE nom_itineraire=? AND id_voyage=?";
        try (PreparedStatement pstCheck = conn().prepareStatement(check)) {
            pstCheck.setString(1, itin.getNom_itineraire());
            pstCheck.setInt(2, itin.getId_voyage());
            try (ResultSet rs = pstCheck.executeQuery()) {
                if (rs.next() && rs.getInt(1) == 0) {
                    String req = "INSERT INTO itineraire (nom_itineraire, description_itineraire, id_voyage) VALUES (?, ?, ?)";
                    try (PreparedStatement pstInsert = conn().prepareStatement(req)) {
                        pstInsert.setString(1, itin.getNom_itineraire());
                        pstInsert.setString(2, itin.getDescription_itineraire());
                        pstInsert.setInt(3, itin.getId_voyage());
                        pstInsert.executeUpdate();
                        System.out.println("Itinéraire ajouté !");
                    }
                } else {
                    System.out.println("Itinéraire déjà existant pour ce voyage, ajout ignoré !");
                }
            }
        }
    }

    @Override
    public void modifier(Itineraire itin) throws SQLException {
        if (itin.getNom_itineraire().length() > 10)
            throw new SQLException("Le nom de l'itinéraire ne doit pas dépasser 10 caractères");

        String req = "UPDATE itineraire SET nom_itineraire=?, description_itineraire=?, id_voyage=? WHERE id_itineraire=?";
        try (PreparedStatement pst = conn().prepareStatement(req)) {
            pst.setString(1, itin.getNom_itineraire());
            pst.setString(2, itin.getDescription_itineraire());
            pst.setInt(3, itin.getId_voyage());
            pst.setInt(4, itin.getId_itineraire());
            pst.executeUpdate();
            System.out.println("Itinéraire modifié");
        }
    }

    @Override
    public void supprimer(int id_itineraire) throws SQLException {
        try (PreparedStatement pst = conn().prepareStatement(
                "DELETE FROM itineraire WHERE id_itineraire=?")) {
            pst.setInt(1, id_itineraire);
            pst.executeUpdate();
            System.out.println("Itinéraire supprimé");
        }
    }

    @Override
    public List<Itineraire> afficher() throws SQLException {
        List<Itineraire> liste = new ArrayList<>();
        try (Statement st = conn().createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM itineraire")) {
            while (rs.next()) liste.add(mapRow(rs));
        }
        return liste;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  REQUÊTES SPÉCIFIQUES
    // ════════════════════════════════════════════════════════════════════════

    public List<Itineraire> getItinerairesByVoyage(int id_voyage) throws SQLException {
        List<Itineraire> liste = new ArrayList<>();
        try (PreparedStatement pst = conn().prepareStatement(
                "SELECT * FROM itineraire WHERE id_voyage = ?")) {
            pst.setInt(1, id_voyage);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) liste.add(mapRow(rs));
            }
        }
        return liste;
    }

    public List<String> getEtapesParItineraire(int id_itineraire) throws SQLException {
        List<String> etapes = new ArrayList<>();
        try (PreparedStatement pst = conn().prepareStatement(
                "SELECT lieu, heure FROM etape WHERE id_itineraire = ? ORDER BY heure ASC")) {
            pst.setInt(1, id_itineraire);
            try (ResultSet rs = pst.executeQuery()) {
                int compteur = 1;
                while (rs.next()) {
                    String lieu  = rs.getString("lieu");
                    Time   heure = rs.getTime("heure");
                    String etape = "J" + compteur + " - " + lieu;
                    if (heure != null) etape += " (" + heure.toString().substring(0, 5) + ")";
                    etapes.add(etape);
                    compteur++;
                }
            }
        }
        return etapes;
    }

    public VoyageDates getVoyageDatesForItineraire(int idItineraire) throws SQLException {
        String req = "SELECT v.date_debut, v.date_fin FROM itineraire i " +
                "JOIN voyage v ON i.id_voyage = v.id_voyage " +
                "WHERE i.id_itineraire = ?";
        try (PreparedStatement pst = conn().prepareStatement(req)) {
            pst.setInt(1, idItineraire);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next())
                    return new VoyageDates(rs.getDate("date_debut"), rs.getDate("date_fin"));
            }
        }
        return new VoyageDates(null, null);
    }

    public List<Itineraire> rechercherItineraires(String searchTerm) throws SQLException {
        if (searchTerm == null || searchTerm.trim().isEmpty()) return afficher();
        List<Itineraire> resultats = new ArrayList<>();
        String req = "SELECT i.* FROM itineraire i " +
                "LEFT JOIN voyage v ON i.id_voyage = v.id_voyage " +
                "WHERE i.nom_itineraire LIKE ? OR i.description_itineraire LIKE ? OR v.titre_voyage LIKE ?";
        try (PreparedStatement pst = conn().prepareStatement(req)) {
            String p = "%" + searchTerm + "%";
            pst.setString(1, p); pst.setString(2, p); pst.setString(3, p);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) resultats.add(mapRow(rs));
            }
        }
        return resultats;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  TRI
    // ════════════════════════════════════════════════════════════════════════

    public List<Itineraire> trierParDate(List<Itineraire> itineraires, boolean ascendant) {
        return itineraires.stream().sorted((i1, i2) -> {
            try {
                VoyageDates d1 = getVoyageDatesForItineraire(i1.getId_itineraire());
                VoyageDates d2 = getVoyageDatesForItineraire(i2.getId_itineraire());
                if (!d1.hasDates() && !d2.hasDates()) return 0;
                if (!d1.hasDates()) return ascendant ? 1 : -1;
                if (!d2.hasDates()) return ascendant ? -1 : 1;
                int cmp = d1.getDateDebut().compareTo(d2.getDateDebut());
                return ascendant ? cmp : -cmp;
            } catch (SQLException e) { e.printStackTrace(); return 0; }
        }).collect(Collectors.toList());
    }

    public List<Itineraire> trierParNom(List<Itineraire> itineraires, boolean ascendant) {
        Comparator<Itineraire> cmp = Comparator.comparing(Itineraire::getNom_itineraire);
        if (!ascendant) cmp = cmp.reversed();
        return itineraires.stream().sorted(cmp).collect(Collectors.toList());
    }

    public List<Itineraire> trierParNombreJours(List<Itineraire> itineraires, boolean ascendant) {
        return itineraires.stream().sorted((i1, i2) -> {
            try {
                int j1 = getVoyageDatesForItineraire(i1.getId_itineraire()).getNombreJours();
                int j2 = getVoyageDatesForItineraire(i2.getId_itineraire()).getNombreJours();
                int cmp = Integer.compare(j1, j2);
                return ascendant ? cmp : -cmp;
            } catch (SQLException e) { e.printStackTrace(); return 0; }
        }).collect(Collectors.toList());
    }

    // ════════════════════════════════════════════════════════════════════════
    //  HELPERS
    // ════════════════════════════════════════════════════════════════════════

    private Itineraire mapRow(ResultSet rs) throws SQLException {
        Itineraire it = new Itineraire();
        it.setId_itineraire(rs.getInt("id_itineraire"));
        it.setNom_itineraire(rs.getString("nom_itineraire"));
        it.setDescription_itineraire(rs.getString("description_itineraire"));
        it.setId_voyage(rs.getInt("id_voyage"));
        return it;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  INNER CLASS VoyageDates
    // ════════════════════════════════════════════════════════════════════════

    public static class VoyageDates {
        private final Date dateDebut;
        private final Date dateFin;

        public VoyageDates(Date dateDebut, Date dateFin) {
            this.dateDebut = dateDebut;
            this.dateFin   = dateFin;
        }

        public Date    getDateDebut()  { return dateDebut; }
        public Date    getDateFin()    { return dateFin; }
        public boolean hasDates()      { return dateDebut != null && dateFin != null; }

        public int getNombreJours() {
            if (!hasDates()) return 0;
            long diff = dateFin.getTime() - dateDebut.getTime();
            return (int) (diff / (1000 * 60 * 60 * 24)) + 1;
        }
    }
}