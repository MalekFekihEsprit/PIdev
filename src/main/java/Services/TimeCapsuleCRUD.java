package Services;

import Entities.TimeCapsule;
import Tools.MyBD;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TimeCapsuleCRUD {

    private Connection conn = MyBD.getInstance().getConn();
    private FREDInflationService inflationService = new FREDInflationService();
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Créer une nouvelle capsule temporelle
     */
    public void creerCapsule(TimeCapsule capsule) throws SQLException {
        // Étape 1: Calculer la valeur future avec l'inflation
        String dateDebut = capsule.getDateCreation().toLocalDate().format(DATE_FORMAT);

        double valeurFuture = inflationService.calculerInflationUnAn(
                capsule.getMontantInitial(),
                capsule.getPaysCode(),
                dateDebut
        );

        capsule.setMontantAjuste(valeurFuture);

        // Calculer le taux d'inflation implicite
        double tauxInflation = (valeurFuture / capsule.getMontantInitial() - 1) * 100;
        capsule.setTauxInflationCalcule(tauxInflation);

        // Étape 2: Insérer en base de données
        String sql = "INSERT INTO time_capsule (id_budget, libelle_capsule, montant_initial, " +
                "montant_ajuste, devise, destination, pays_code, date_creation, " +
                "date_reouverture, est_reouverte, email_notification, taux_inflation_calcule) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, capsule.getIdBudget());
            ps.setString(2, capsule.getLibelleCapsule());
            ps.setDouble(3, capsule.getMontantInitial());
            ps.setDouble(4, capsule.getMontantAjuste());
            ps.setString(5, capsule.getDevise());
            ps.setString(6, capsule.getDestination());
            ps.setString(7, capsule.getPaysCode());
            ps.setDate(8, capsule.getDateCreation());
            ps.setDate(9, capsule.getDateReouverture());
            ps.setBoolean(10, capsule.isEstReouverte());
            ps.setString(11, capsule.getEmailNotification());
            ps.setDouble(12, capsule.getTauxInflationCalcule());

            ps.executeUpdate();

            // Récupérer l'ID généré
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    capsule.setIdCapsule(rs.getInt(1));
                }
            }
        }
    }

    /**
     * Ouvrir une capsule (appelé après 1 an)
     */
    public void ouvrirCapsule(int idCapsule) throws SQLException {
        TimeCapsule capsule = getCapsuleById(idCapsule);

        if (capsule != null && !capsule.isEstReouverte()) {
            // Vérifier si la date est dépassée
            if (capsule.getDateReouverture().toLocalDate().isAfter(LocalDate.now())) {
                throw new SQLException("La capsule n'est pas encore prête à être ouverte !");
            }

            // Recalculer avec l'inflation réelle
            String dateDebut = capsule.getDateCreation().toLocalDate().format(DATE_FORMAT);
            String dateFin = LocalDate.now().format(DATE_FORMAT);

            double valeurReelle = inflationService.calculerValeurAjustee(
                    capsule.getMontantInitial(),
                    capsule.getPaysCode(),
                    dateDebut,
                    dateFin
            );

            // Mettre à jour la capsule
            String sql = "UPDATE time_capsule SET est_reouverte = true, montant_ajuste = ? " +
                    "WHERE id_capsule = ?";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setDouble(1, valeurReelle);
                ps.setInt(2, idCapsule);
                ps.executeUpdate();
            }

            // Envoyer l'email de notification
            envoyerEmailNotification(capsule, valeurReelle);
        }
    }

    /**
     * Récupérer toutes les capsules d'un utilisateur
     */
    public List<TimeCapsule> getCapsulesByUser(int userId) throws SQLException {
        List<TimeCapsule> capsules = new ArrayList<>();

        String sql = "SELECT tc.* FROM time_capsule tc " +
                "JOIN budget b ON tc.id_budget = b.id_budget " +
                "WHERE b.id = ? ORDER BY tc.date_reouverture ASC";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    capsules.add(extractCapsuleFromResultSet(rs));
                }
            }
        }

        return capsules;
    }

    /**
     * Récupérer une capsule par son ID
     */
    public TimeCapsule getCapsuleById(int idCapsule) throws SQLException {
        String sql = "SELECT * FROM time_capsule WHERE id_capsule = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idCapsule);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractCapsuleFromResultSet(rs);
                }
            }
        }

        return null;
    }

    /**
     * Supprimer une capsule
     */
    public void supprimerCapsule(int idCapsule) throws SQLException {
        String sql = "DELETE FROM time_capsule WHERE id_capsule = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idCapsule);
            ps.executeUpdate();
        }
    }

    /**
     * Envoyer une notification par email
     */
    private void envoyerEmailNotification(TimeCapsule capsule, double valeurReelle) {
        String sujet = String.format("⏳ Votre capsule temporelle pour %s est prête !",
                capsule.getDestination());

        double difference = valeurReelle - capsule.getMontantInitial();
        String signe = difference > 0 ? "plus" : "moins";
        String emoji = difference > 0 ? "📈" : "📉";

        String message = String.format(
                "Bonjour,\n\n" +
                        "Il y a un an, vous avez scellé un budget de %.2f %s pour votre voyage à %s.\n\n" +
                        "%s Aujourd'hui, avec l'inflation, cette somme vaudrait %.2f %s\n" +
                        "Soit %.2f %s de %s qu'à l'époque (%.1f%% d'inflation).\n\n" +
                        "Les souvenirs n'ont pas de prix, mais ils prennent de la valeur !\n" +
                        "Prêt(e) pour de nouvelles aventures ?\n\n" +
                        "---\n" +
                        "TravelMate - Votre compagnon de voyage",
                capsule.getMontantInitial(), capsule.getDevise(),
                capsule.getDestination(),
                emoji, valeurReelle, capsule.getDevise(),
                Math.abs(difference), capsule.getDevise(), signe,
                capsule.getTauxInflationCalcule()
        );

        // Ici, vous intégreriez votre service d'envoi d'email
        System.out.println("📧 Email envoyé à: " + capsule.getEmailNotification());
        System.out.println("Sujet: " + sujet);
        System.out.println("Message: " + message);

        // TODO: Implémenter l'envoi réel avec JavaMail
    }

    /**
     * Extraire une capsule depuis un ResultSet
     */
    private TimeCapsule extractCapsuleFromResultSet(ResultSet rs) throws SQLException {
        TimeCapsule capsule = new TimeCapsule(
                rs.getInt("id_budget"),
                rs.getString("libelle_capsule"),
                rs.getDouble("montant_initial"),
                rs.getString("devise"),
                rs.getString("destination"),
                rs.getString("pays_code"),
                rs.getString("email_notification")
        );

        capsule.setIdCapsule(rs.getInt("id_capsule"));
        capsule.setMontantAjuste(rs.getDouble("montant_ajuste"));
        capsule.setDateCreation(rs.getDate("date_creation"));
        capsule.setDateReouverture(rs.getDate("date_reouverture"));
        capsule.setEstReouverte(rs.getBoolean("est_reouverte"));
        capsule.setTauxInflationCalcule(rs.getDouble("taux_inflation_calcule"));

        return capsule;
    }
}