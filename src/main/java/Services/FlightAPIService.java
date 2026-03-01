package Services;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class FlightAPIService {

    // Chargement de la clé depuis .env
    private static final String AVIATIONSTACK_API_KEY = EnvLoader.get("AVIATIONSTACK_API_KEY");
    private static final String AVIATIONSTACK_URL = "http://api.aviationstack.com/v1";

    private final OkHttpClient client;

    public FlightAPIService() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build();

        if (!isApiConfigured()) {
            System.out.println("⚠️⚠️⚠️ ATTENTION: Clé API AviationStack non configurée!");
            System.out.println("📝 Créez un fichier .env avec AVIATIONSTACK_API_KEY=votre_cle");
        }
    }

    /**
     * Vérifie si l'API est configurée
     */
    private boolean isApiConfigured() {
        return EnvLoader.hasValidKey("AVIATIONSTACK_API_KEY");
    }

    /**
     * Recherche des vols pour une destination à une date donnée
     * Utilise l'endpoint /schedules (disponible dans le plan gratuit)
     */
    public List<FlightInfo> searchFlights(String destination, LocalDate date) throws IOException {
        List<FlightInfo> flights = new ArrayList<>();

        // Si l'API n'est pas configurée, utiliser directement les données mock
        if (!isApiConfigured()) {
            System.out.println("🔧 Utilisation des données de démonstration (API AviationStack non configurée)");
            return getMockFlights(destination, date);
        }

        String airportCode = getAirportCode(destination);
        String dateStr = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        // Utiliser l'endpoint /schedules qui est disponible dans le plan gratuit
        String url = String.format("%s/schedules?access_key=%s&arr_iata=%s&date=%s&limit=10",
                AVIATIONSTACK_URL, AVIATIONSTACK_API_KEY, airportCode, dateStr);

        System.out.println("URL appelée: " + url);

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No body";
                System.out.println("Erreur API - Code: " + response.code() + ", Body: " + errorBody);

                // Gestion des erreurs d'authentification
                if (response.code() == 401 || response.code() == 403) {
                    System.out.println("🔑 Clé API AviationStack invalide ou non configurée!");
                }

                // Si l'API ne fonctionne pas, utiliser des données de démonstration
                return getMockFlights(destination, date);
            }

            String jsonData = response.body().string();
            JsonObject jsonObject = JsonParser.parseString(jsonData).getAsJsonObject();

            if (jsonObject.has("data")) {
                JsonArray data = jsonObject.getAsJsonArray("data");

                for (int i = 0; i < data.size(); i++) {
                    JsonObject schedule = data.get(i).getAsJsonObject();
                    flights.add(parseSchedule(schedule));
                }
            }

            // Si aucun vol trouvé, utiliser des données de démonstration
            if (flights.isEmpty()) {
                return getMockFlights(destination, date);
            }
        }

        return flights;
    }

    /**
     * Parse un objet schedule en FlightInfo
     */
    private FlightInfo parseSchedule(JsonObject schedule) {
        FlightInfo info = new FlightInfo();

        // Informations de vol
        if (schedule.has("flight") && !schedule.get("flight").isJsonNull()) {
            JsonObject flight = schedule.getAsJsonObject("flight");
            info.setFlightNumber(getJsonString(flight, "number"));
            info.setAirline(getJsonString(flight, "iata"));
        }

        // Départ
        if (schedule.has("departure") && !schedule.get("departure").isJsonNull()) {
            JsonObject dep = schedule.getAsJsonObject("departure");
            info.setDepartureAirport(getJsonString(dep, "airport"));
            info.setDepartureTime(getJsonString(dep, "scheduled"));
        }

        // Arrivée
        if (schedule.has("arrival") && !schedule.get("arrival").isJsonNull()) {
            JsonObject arr = schedule.getAsJsonObject("arrival");
            info.setArrivalAirport(getJsonString(arr, "airport"));
            info.setArrivalTime(getJsonString(arr, "scheduled"));
        }

        info.setStatus("Planifié");

        return info;
    }

    /**
     * Récupère une valeur String d'un JsonObject en gérant les nulls
     */
    private String getJsonString(JsonObject obj, String key) {
        if (obj.has(key) && !obj.get(key).isJsonNull()) {
            return obj.get(key).getAsString();
        }
        return "N/A";
    }

    /**
     * Génère des données de démonstration pour tester l'interface
     */
    private List<FlightInfo> getMockFlights(String destination, LocalDate date) {
        List<FlightInfo> flights = new ArrayList<>();

        String[][] mockData = {
                {"Tunisair", "TU712", "CDG", getAirportCode(destination), "08:30", "11:45", "À l'heure"},
                {"Air France", "AF1284", "LHR", getAirportCode(destination), "10:15", "13:30", "À l'heure"},
                {"Lufthansa", "LH432", "FRA", getAirportCode(destination), "12:45", "16:00", "Retard 15min"},
                {"Emirates", "EK248", "DXB", getAirportCode(destination), "15:20", "18:35", "À l'heure"},
                {"Turkish Airlines", "TK864", "IST", getAirportCode(destination), "18:10", "21:25", "À l'heure"}
        };

        for (String[] data : mockData) {
            FlightInfo flight = new FlightInfo();
            flight.setAirline(data[0]);
            flight.setFlightNumber(data[1]);
            flight.setDepartureAirport(data[2]);
            flight.setArrivalAirport(data[3]);

            String dateStr = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            flight.setDepartureTime(dateStr + "T" + data[4] + ":00");
            flight.setArrivalTime(dateStr + "T" + data[5] + ":00");
            flight.setStatus(data[6]);

            flights.add(flight);
        }

        return flights;
    }

    /**
     * Convertit un nom de destination en code IATA d'aéroport
     */
    private String getAirportCode(String destination) {
        if (destination == null) return "TUN";

        switch (destination.toLowerCase().trim()) {
            case "zaghouan":
            case "tunis":
            case "tunisie":
                return "TUN";
            case "paris":
            case "france":
                return "CDG";
            case "londres":
            case "angleterre":
            case "royaume-uni":
                return "LHR";
            case "new york":
            case "etats-unis":
            case "usa":
                return "JFK";
            case "dubai":
            case "dubaï":
            case "emirats":
                return "DXB";
            case "rome":
            case "italie":
                return "FCO";
            case "barcelone":
            case "espagne":
                return "BCN";
            case "madrid":
                return "MAD";
            case "berlin":
            case "allemagne":
                return "BER";
            case "istanbul":
            case "turquie":
                return "IST";
            case "cairo":
            case "le caire":
            case "egypte":
                return "CAI";
            case "casablanca":
            case "maroc":
                return "CMN";
            default:
                return "TUN";
        }
    }

    /**
     * Classe pour les informations de vol
     */
    public static class FlightInfo {
        private String flightNumber;
        private String airline;
        private String departureAirport;
        private String arrivalAirport;
        private String departureTime;
        private String arrivalTime;
        private String status;

        public String getFlightNumber() { return flightNumber; }
        public void setFlightNumber(String flightNumber) { this.flightNumber = flightNumber; }

        public String getAirline() { return airline; }
        public void setAirline(String airline) { this.airline = airline; }

        public String getDepartureAirport() { return departureAirport; }
        public void setDepartureAirport(String departureAirport) { this.departureAirport = departureAirport; }

        public String getArrivalAirport() { return arrivalAirport; }
        public void setArrivalAirport(String arrivalAirport) { this.arrivalAirport = arrivalAirport; }

        public String getDepartureTime() { return departureTime; }
        public void setDepartureTime(String departureTime) { this.departureTime = departureTime; }

        public String getArrivalTime() { return arrivalTime; }
        public void setArrivalTime(String arrivalTime) { this.arrivalTime = arrivalTime; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getFormattedDepartureTime() {
            if (departureTime != null && departureTime.length() >= 16) {
                return departureTime.substring(11, 16);
            }
            return "--:--";
        }

        public String getFormattedArrivalTime() {
            if (arrivalTime != null && arrivalTime.length() >= 16) {
                return arrivalTime.substring(11, 16);
            }
            return "--:--";
        }

        @Override
        public String toString() {
            return String.format("%s - %s: %s → %s",
                    airline, flightNumber, departureAirport, arrivalAirport);
        }
    }
}