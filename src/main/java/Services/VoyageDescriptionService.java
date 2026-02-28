package Services;

import Entities.Voyage;

import java.sql.SQLException;

/**
 * Service de génération de descriptions de voyages
 * Utilise des templates locaux variés — aucune API externe requise
 */
public class VoyageDescriptionService {

    private final String[] templates = {
            "Partez à la découverte de %s avec notre voyage '%s'. Une expérience inoubliable vous attend avec des paysages à couper le souffle !",
            "Explorez %s lors d'un voyage exceptionnel '%s'. Des moments magiques et des souvenirs impérissables garantis.",
            "Vivez une aventure unique à %s avec notre voyage '%s'. Laissez-vous surprendre par la beauté de cette destination.",
            "Laissez-vous séduire par %s pendant ce voyage '%s'. Dépaysement total et découvertes fascinantes assurés.",
            "Découvrez les merveilles de %s avec notre voyage '%s'. Une escapade mémorable entre culture et détente.",
            "Embarquez pour %s avec '%s' — des souvenirs plein la tête et des étoiles plein les yeux !",
            "Le voyage de vos rêves à %s vous attend avec '%s'. Ne manquez pas cette chance unique de vous évader.",
            "Cap sur %s avec '%s' ! Entre découvertes culturelles et moments de détente, ce voyage a tout pour plaire.",
            "Offrez-vous une parenthèse enchantée à %s avec '%s'. Un voyage qui éveillera tous vos sens.",
            "Direction %s pour un voyage '%s' inoubliable. Préparez-vous à vivre des expériences extraordinaires !",
            "%s vous ouvre ses portes avec '%s'. Plongez dans un univers de découvertes et de sensations nouvelles.",
            "Évadez-vous vers %s grâce à notre voyage '%s'. Une destination qui ne laisse personne indifférent !"
    };

    public VoyageDescriptionService() {
        // Aucune initialisation externe nécessaire
    }

    /**
     * Génère une description à partir de templates locaux
     */
    public String genererDescription(Voyage voyage, String nomDestination) {
        if (voyage == null) return "Description non disponible";
        String dest = (nomDestination != null && !nomDestination.isEmpty()) ? nomDestination : "cette magnifique destination";
        int index = Math.abs((voyage.getTitre_voyage() + dest).hashCode()) % templates.length;
        return String.format(templates[index], dest, voyage.getTitre_voyage());
    }

    /**
     * Génère une description en allant chercher le nom de la destination
     */
    public String genererDescription(Voyage voyage) {
        try {
            VoyageCRUDV crud = new VoyageCRUDV();
            String nomDestination = crud.getNomDestination(voyage.getId_destination());
            return genererDescription(voyage, nomDestination);
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de la destination: " + e.getMessage());
            return genererDescription(voyage, "cette magnifique destination");
        }
    }

    /**
     * Génère plusieurs variantes de description
     */
    public String[] genererVariantes(Voyage voyage, String nomDestination, int nombreVariantes) {
        if (nombreVariantes <= 0) nombreVariantes = 1;
        String dest = (nomDestination != null && !nomDestination.isEmpty()) ? nomDestination : "cette magnifique destination";
        String[] variantes = new String[nombreVariantes];
        int startIndex = Math.abs((voyage.getTitre_voyage() + dest).hashCode()) % templates.length;
        for (int i = 0; i < nombreVariantes; i++) {
            int idx = (startIndex + i) % templates.length;
            variantes[i] = String.format(templates[idx], dest, voyage.getTitre_voyage());
        }
        return variantes;
    }

    /**
     * Toujours disponible (pas de dépendance externe)
     */
    public boolean isConfigured() {
        return true;
    }

    public String getLastError() {
        return "";
    }

    public boolean testConnexion() {
        return true;
    }
}
