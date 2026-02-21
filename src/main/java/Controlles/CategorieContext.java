package Controlles;

/**
 * Contexte partagé entre CATfront et ACTfront.
 * Permet de transmettre le filtre de catégorie lors de la navigation.
 *
 * Usage :
 *   CATfront → met categorieFiltre = "Nom"  → navigate vers activitesfront.fxml
 *   ACTfront → lit categorieFiltre au démarrage, filtre, puis reset à null
 */
public class CategorieContext {

    /** Nom de la catégorie à filtrer. null = afficher toutes les activités. */
    public static String categorieFiltre = null;

    private CategorieContext() {}
}
