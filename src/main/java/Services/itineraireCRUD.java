package Services;

import Entites.Itineraire;
import Utils.MyBD;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;
import java.util.stream.Collectors;

public class itineraireCRUD implements interfaceCRUD<Itineraire> {

    public itineraireCRUD() {
        // Pas besoin de stocker la connexion ici
    }

    @Override
    public void ajouter(Itineraire itin) throws SQLException {
        if (itin.getNom_itineraire().length() > 10) {
            throw new SQLException("Le nom de l'itinéraire ne doit pas dépasser 10 caractères");
        }

        Connection conn = null;
        PreparedStatement pstCheck = null;
        PreparedStatement pstInsert = null;
        ResultSet rs = null;

        try {
            conn = MyBD.getInstance().getConn();

            String check = "SELECT COUNT(*) FROM itineraire WHERE nom_itineraire=? AND id_voyage=?";
            pstCheck = conn.prepareStatement(check);
            pstCheck.setString(1, itin.getNom_itineraire());
            pstCheck.setInt(2, itin.getId_voyage());
            rs = pstCheck.executeQuery();

            if (rs.next() && rs.getInt(1) == 0) {
                String req = "INSERT INTO itineraire (nom_itineraire, description_itineraire, id_voyage) VALUES (?, ?, ?)";
                pstInsert = conn.prepareStatement(req);
                pstInsert.setString(1, itin.getNom_itineraire());
                pstInsert.setString(2, itin.getDescription_itineraire());
                pstInsert.setInt(3, itin.getId_voyage());
                pstInsert.executeUpdate();

                System.out.println("Itinéraire ajouté !");
            } else {
                System.out.println("Itinéraire déjà existant pour ce voyage, ajout ignoré !");
            }
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (pstCheck != null) try { pstCheck.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (pstInsert != null) try { pstInsert.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    @Override
    public void modifier(Itineraire itin) throws SQLException {
        if (itin.getNom_itineraire().length() > 10) {
            throw new SQLException("Le nom de l'itinéraire ne doit pas dépasser 10 caractères");
        }

        Connection conn = null;
        PreparedStatement pst = null;

        try {
            conn = MyBD.getInstance().getConn();
            String req = "UPDATE itineraire SET nom_itineraire=?, description_itineraire=?, id_voyage=? WHERE id_itineraire=?";
            pst = conn.prepareStatement(req);

            pst.setString(1, itin.getNom_itineraire());
            pst.setString(2, itin.getDescription_itineraire());
            pst.setInt(3, itin.getId_voyage());
            pst.setInt(4, itin.getId_itineraire());

            pst.executeUpdate();
            System.out.println("Itinéraire modifié");
        } finally {
            if (pst != null) try { pst.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    @Override
    public void supprimer(int id_itineraire) throws SQLException {
        Connection conn = null;
        PreparedStatement pst = null;

        try {
            conn = MyBD.getInstance().getConn();
            String req = "DELETE FROM itineraire WHERE id_itineraire=?";
            pst = conn.prepareStatement(req);
            pst.setInt(1, id_itineraire);
            pst.executeUpdate();
            System.out.println("Itinéraire supprimé");
        } finally {
            if (pst != null) try { pst.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    @Override
    public List<Itineraire> afficher() throws SQLException {
        List<Itineraire> listeItineraire = new ArrayList<>();
        Connection conn = null;
        Statement st = null;
        ResultSet rs = null;

        try {
            conn = MyBD.getInstance().getConn();
            String req = "SELECT * FROM itineraire";
            st = conn.createStatement();
            rs = st.executeQuery(req);

            while (rs.next()) {
                Itineraire itineraire = new Itineraire();
                itineraire.setId_itineraire(rs.getInt("id_itineraire"));
                itineraire.setNom_itineraire(rs.getString("nom_itineraire"));
                itineraire.setDescription_itineraire(rs.getString("description_itineraire"));
                itineraire.setId_voyage(rs.getInt("id_voyage"));

                listeItineraire.add(itineraire);
            }
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (st != null) try { st.close(); } catch (SQLException e) { e.printStackTrace(); }
        }

        return listeItineraire;
    }

    public List<Itineraire> getItinerairesByVoyage(int id_voyage) throws SQLException {
        List<Itineraire> listeItineraire = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;

        try {
            conn = MyBD.getInstance().getConn();
            String req = "SELECT * FROM itineraire WHERE id_voyage = ?";
            pst = conn.prepareStatement(req);
            pst.setInt(1, id_voyage);
            rs = pst.executeQuery();

            while (rs.next()) {
                Itineraire itineraire = new Itineraire();
                itineraire.setId_itineraire(rs.getInt("id_itineraire"));
                itineraire.setNom_itineraire(rs.getString("nom_itineraire"));
                itineraire.setDescription_itineraire(rs.getString("description_itineraire"));
                itineraire.setId_voyage(rs.getInt("id_voyage"));
                listeItineraire.add(itineraire);
            }
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (pst != null) try { pst.close(); } catch (SQLException e) { e.printStackTrace(); }
        }

        return listeItineraire;
    }

    public List<String> getEtapesParItineraire(int id_itineraire) throws SQLException {
        List<String> etapes = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;

        try {
            conn = MyBD.getInstance().getConn();
            String req = "SELECT lieu, heure FROM etape WHERE id_itineraire = ? ORDER BY heure ASC";
            pst = conn.prepareStatement(req);
            pst.setInt(1, id_itineraire);
            rs = pst.executeQuery();

            int compteur = 1;
            while (rs.next()) {
                String lieu = rs.getString("lieu");
                Time heure = rs.getTime("heure");
                String etape = "J" + compteur + " - " + lieu;
                if (heure != null) {
                    etape += " (" + heure.toString().substring(0, 5) + ")";
                }
                etapes.add(etape);
                compteur++;
            }
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (pst != null) try { pst.close(); } catch (SQLException e) { e.printStackTrace(); }
        }

        return etapes;
    }

    // NOUVELLE MÉTHODE: Obtenir les dates de voyage pour un itinéraire
    public VoyageDates getVoyageDatesForItineraire(int idItineraire) throws SQLException {
        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;

        try {
            conn = MyBD.getInstance().getConn();
            String req = "SELECT v.date_debut, v.date_fin FROM itineraire i " +
                    "JOIN voyage v ON i.id_voyage = v.id_voyage " +
                    "WHERE i.id_itineraire = ?";
            pst = conn.prepareStatement(req);
            pst.setInt(1, idItineraire);
            rs = pst.executeQuery();

            if (rs.next()) {
                Date dateDebut = rs.getDate("date_debut");
                Date dateFin = rs.getDate("date_fin");
                return new VoyageDates(dateDebut, dateFin);
            }
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (pst != null) try { pst.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
        return new VoyageDates(null, null);
    }

    // Classe interne pour les dates de voyage
    public static class VoyageDates {
        private Date dateDebut;
        private Date dateFin;

        public VoyageDates(Date dateDebut, Date dateFin) {
            this.dateDebut = dateDebut;
            this.dateFin = dateFin;
        }

        public Date getDateDebut() { return dateDebut; }
        public Date getDateFin() { return dateFin; }
        public boolean hasDates() { return dateDebut != null && dateFin != null; }

        public int getNombreJours() {
            if (!hasDates()) return 0;
            long diff = dateFin.getTime() - dateDebut.getTime();
            return (int) (diff / (1000 * 60 * 60 * 24)) + 1;
        }
    }

    // NOUVELLE MÉTHODE: Rechercher des itinéraires
    public List<Itineraire> rechercherItineraires(String searchTerm) throws SQLException {
        List<Itineraire> resultats = new ArrayList<>();
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return afficher();
        }

        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;

        try {
            conn = MyBD.getInstance().getConn();
            String req = "SELECT i.* FROM itineraire i " +
                    "LEFT JOIN voyage v ON i.id_voyage = v.id_voyage " +
                    "WHERE i.nom_itineraire LIKE ? OR i.description_itineraire LIKE ? OR v.titre_voyage LIKE ?";
            pst = conn.prepareStatement(req);
            String searchPattern = "%" + searchTerm + "%";
            pst.setString(1, searchPattern);
            pst.setString(2, searchPattern);
            pst.setString(3, searchPattern);
            rs = pst.executeQuery();

            while (rs.next()) {
                Itineraire itineraire = new Itineraire();
                itineraire.setId_itineraire(rs.getInt("id_itineraire"));
                itineraire.setNom_itineraire(rs.getString("nom_itineraire"));
                itineraire.setDescription_itineraire(rs.getString("description_itineraire"));
                itineraire.setId_voyage(rs.getInt("id_voyage"));
                resultats.add(itineraire);
            }
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (pst != null) try { pst.close(); } catch (SQLException e) { e.printStackTrace(); }
        }

        return resultats;
    }

    // NOUVELLE MÉTHODE: Trier les itinéraires par date
    public List<Itineraire> trierParDate(List<Itineraire> itineraires, boolean ascendant) {
        return itineraires.stream()
                .sorted((i1, i2) -> {
                    try {
                        VoyageDates d1 = getVoyageDatesForItineraire(i1.getId_itineraire());
                        VoyageDates d2 = getVoyageDatesForItineraire(i2.getId_itineraire());

                        if (!d1.hasDates() && !d2.hasDates()) return 0;
                        if (!d1.hasDates()) return ascendant ? 1 : -1;
                        if (!d2.hasDates()) return ascendant ? -1 : 1;

                        int comparison = d1.getDateDebut().compareTo(d2.getDateDebut());
                        return ascendant ? comparison : -comparison;
                    } catch (SQLException e) {
                        e.printStackTrace();
                        return 0;
                    }
                })
                .collect(Collectors.toList());
    }

    // NOUVELLE MÉTHODE: Trier les itinéraires par nom (ordre alphabétique)
    public List<Itineraire> trierParNom(List<Itineraire> itineraires, boolean ascendant) {
        Comparator<Itineraire> comparator = Comparator.comparing(Itineraire::getNom_itineraire);
        if (!ascendant) {
            comparator = comparator.reversed();
        }
        return itineraires.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    // NOUVELLE MÉTHODE: Trier les itinéraires par nombre de jours
    public List<Itineraire> trierParNombreJours(List<Itineraire> itineraires, boolean ascendant) {
        return itineraires.stream()
                .sorted((i1, i2) -> {
                    try {
                        int jours1 = getVoyageDatesForItineraire(i1.getId_itineraire()).getNombreJours();
                        int jours2 = getVoyageDatesForItineraire(i2.getId_itineraire()).getNombreJours();

                        int comparison = Integer.compare(jours1, jours2);
                        return ascendant ? comparison : -comparison;
                    } catch (SQLException e) {
                        e.printStackTrace();
                        return 0;
                    }
                })
                .collect(Collectors.toList());
    }
}