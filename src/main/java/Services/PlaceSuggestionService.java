package Services;

import Entites.Activites;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

public class PlaceSuggestionService {

    public static List<Map<String, Object>> getSuggestionsForActivity(Activites activity) {
        if (activity == null || activity.getLieu() == null || activity.getLieu().isEmpty()) {
            System.out.println("❌ Activité sans lieu, impossible de suggérer");
            return List.of();
        }

        // Géocoder le lieu pour obtenir ses coordonnées
        GeocodingService.LocationResult location = GeocodingService.geocode(activity.getLieu());

        if (location == null) {
            System.out.println("❌ Impossible de géocoder: " + activity.getLieu());
            return getLocalMockPlaces(activity.getLieu());
        }

        System.out.println("📍 Recherche de lieux près de: " + activity.getLieu());
        System.out.println("   Coordonnées: " + location.getLatitude() + ", " + location.getLongitude());
        System.out.println("   Pays: " + location.getCountry());

        // Essayer avec différents rayons
        List<Map<String, Object>> places = new ArrayList<>();

        // Essayer 2km
        places = NearbyPlacesAPI.findNearbyPlaces(
                location.getLatitude(),
                location.getLongitude(),
                2000
        );

        // Si pas de résultats, essayer 5km
        if (places.isEmpty()) {
            System.out.println("⚠️ Aucun résultat à 2km, essai à 5km");
            places = NearbyPlacesAPI.findNearbyPlaces(
                    location.getLatitude(),
                    location.getLongitude(),
                    5000
            );
        }

        // Si toujours pas de résultats, essayer 10km
        if (places.isEmpty()) {
            System.out.println("⚠️ Aucun résultat à 5km, essai à 10km");
            places = NearbyPlacesAPI.findNearbyPlaces(
                    location.getLatitude(),
                    location.getLongitude(),
                    10000
            );
        }

        // Si TOUJOURS pas de résultats, utiliser les données simulées
        if (places.isEmpty()) {
            System.out.println("ℹ️ Utilisation des données simulées pour: " + activity.getLieu());
            places = getLocalMockPlaces(activity.getLieu());
        }

        // Filtrer par distance raisonnable (max 15km)
        final double MAX_DISTANCE = 15.0;
        places = places.stream()
                .filter(p -> {
                    Object distObj = p.get("distance");
                    if (distObj instanceof Double) {
                        return (Double) distObj < MAX_DISTANCE;
                    }
                    return true;
                })
                .collect(Collectors.toList());

        System.out.println("✅ " + places.size() + " suggestions trouvées");
        return places.stream().limit(8).collect(Collectors.toList());
    }

    private static List<Map<String, Object>> getLocalMockPlaces(String lieu) {
        List<Map<String, Object>> places = new ArrayList<>();

        String lieuLower = lieu.toLowerCase();
        System.out.println("📍 Génération de données simulées pour: " + lieuLower);

        // Données pour Tunis et ses environs
        if (lieuLower.contains("tunis") || lieuLower.contains("tunisie") ||
                lieuLower.contains("montplaisir") || lieuLower.contains("khereiddine") ||
                lieuLower.contains("rue 8001")) {

            places.add(createMockPlace("Parc du Belvédère", "park", 36.8256, 10.1772, 0.9));
            places.add(createMockPlace("Restaurant Le Golfe", "restaurant", 36.8300, 10.1900, 0.7));
            places.add(createMockPlace("Café de la Jeunesse", "cafe", 36.8225, 10.1850, 0.3));
            places.add(createMockPlace("Centre Commercial Montplaisir", "shop", 36.8230, 10.1830, 0.4));
            places.add(createMockPlace("Médina de Tunis", "attraction", 36.7997, 10.1658, 2.5));
            places.add(createMockPlace("Musée National du Bardo", "museum", 36.8095, 10.1345, 3.8));
            places.add(createMockPlace("Restaurant Dar El Jeld", "restaurant", 36.7983, 10.1686, 2.4));
            places.add(createMockPlace("Théâtre Municipal", "theatre", 36.8008, 10.1775, 1.5));
        }
        // Données pour Paris
        else if (lieuLower.contains("paris") || lieuLower.contains("france")) {
            places.add(createMockPlace("Musée du Louvre", "museum", 48.8606, 2.3376, 1.2));
            places.add(createMockPlace("Tour Eiffel", "monument", 48.8584, 2.2945, 2.5));
            places.add(createMockPlace("Café de Flore", "cafe", 48.8539, 2.3327, 0.8));
            places.add(createMockPlace("Jardin du Luxembourg", "park", 48.8462, 2.3372, 1.5));
            places.add(createMockPlace("Notre-Dame de Paris", "monument", 48.8530, 2.3499, 1.0));
            places.add(createMockPlace("Le Marais", "attraction", 48.8599, 2.3622, 1.8));
        }
        // Données génériques pour toute autre ville
        else {
            places.add(createMockPlace("Centre-ville historique", "attraction", 0, 0, 1.0));
            places.add(createMockPlace("Musée Municipal", "museum", 0, 0, 1.5));
            places.add(createMockPlace("Restaurant Le Central", "restaurant", 0, 0, 0.5));
            places.add(createMockPlace("Parc de la Ville", "park", 0, 0, 2.0));
            places.add(createMockPlace("Café du Commerce", "cafe", 0, 0, 0.3));
            places.add(createMockPlace("Théâtre Municipal", "theatre", 0, 0, 1.2));
        }

        return places;
    }

    private static Map<String, Object> createMockPlace(String name, String type, double lat, double lon, double dist) {
        Map<String, Object> place = new HashMap<>();
        place.put("name", name);
        place.put("type", type);
        place.put("lat", lat);
        place.put("lon", lon);
        place.put("distance", dist);
        return place;
    }

    public static boolean hasSuggestions(Activites activity) {
        return !getSuggestionsForActivity(activity).isEmpty();
    }
}