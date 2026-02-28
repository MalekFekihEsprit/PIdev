package Services;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.util.concurrent.TimeUnit;

/**
 * Service d'informations pays via l'API REST Countries (https://restcountries.com).
 * Entièrement gratuite, aucune clé API requise.
 */
public class RestCountriesService {

    private static final String BASE_URL = "https://restcountries.com/v3.1";
    private final OkHttpClient client;

    public RestCountriesService() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build();
    }

    /**
     * Récupère les informations d'un pays par son nom (nom complet ou partiel).
     *
     * @param nomPays le nom du pays (ex: "France", "Tunisie", "Italy")
     * @return un objet CountryInfo ou null si non trouvé
     */
    public CountryInfo getInfosPays(String nomPays) {
        if (nomPays == null || nomPays.trim().isEmpty()) return null;

        try {
            // Essayer d'abord par nom complet
            String url = BASE_URL + "/name/" + nomPays.trim().replace(" ", "%20")
                    + "?fields=name,capital,population,region,subregion,languages,currencies,flags,timezones,continents";

            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Accept", "application/json")
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful() || response.body() == null) {
                    return null;
                }

                String body = response.body().string();
                JsonArray array = JsonParser.parseString(body).getAsJsonArray();

                if (array.isEmpty()) return null;

                JsonObject pays = array.get(0).getAsJsonObject();
                return parseCountryInfo(pays);
            }
        } catch (Exception e) {
            System.out.println("⚠️ REST Countries API erreur: " + e.getMessage());
            return null;
        }
    }

    /**
     * Parse la réponse JSON en objet CountryInfo.
     */
    private CountryInfo parseCountryInfo(JsonObject json) {
        CountryInfo info = new CountryInfo();

        // Nom officiel
        if (json.has("name")) {
            JsonObject nameObj = json.getAsJsonObject("name");
            info.nomOfficiel = nameObj.has("official") ? nameObj.get("official").getAsString() : "";
            info.nomCommun = nameObj.has("common") ? nameObj.get("common").getAsString() : "";
        }

        // Capitale
        if (json.has("capital") && json.get("capital").isJsonArray()) {
            JsonArray caps = json.getAsJsonArray("capital");
            if (!caps.isEmpty()) {
                info.capitale = caps.get(0).getAsString();
            }
        }

        // Population
        if (json.has("population")) {
            info.population = json.get("population").getAsLong();
        }

        // Région / Sous-région
        if (json.has("region")) info.region = json.get("region").getAsString();
        if (json.has("subregion")) info.sousRegion = json.get("subregion").getAsString();

        // Continent
        if (json.has("continents") && json.get("continents").isJsonArray()) {
            JsonArray conts = json.getAsJsonArray("continents");
            if (!conts.isEmpty()) {
                info.continent = conts.get(0).getAsString();
            }
        }

        // Langues
        if (json.has("languages") && json.get("languages").isJsonObject()) {
            JsonObject langs = json.getAsJsonObject("languages");
            StringBuilder sb = new StringBuilder();
            for (String key : langs.keySet()) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(langs.get(key).getAsString());
            }
            info.langues = sb.toString();
        }

        // Devises
        if (json.has("currencies") && json.get("currencies").isJsonObject()) {
            JsonObject currencies = json.getAsJsonObject("currencies");
            StringBuilder sb = new StringBuilder();
            for (String key : currencies.keySet()) {
                JsonObject curr = currencies.getAsJsonObject(key);
                if (sb.length() > 0) sb.append(", ");
                String nom = curr.has("name") ? curr.get("name").getAsString() : key;
                String symbole = curr.has("symbol") ? curr.get("symbol").getAsString() : "";
                sb.append(nom);
                if (!symbole.isEmpty()) sb.append(" (").append(symbole).append(")");
            }
            info.devises = sb.toString();
        }

        // Drapeau (URL SVG)
        if (json.has("flags") && json.get("flags").isJsonObject()) {
            JsonObject flags = json.getAsJsonObject("flags");
            info.drapeauUrl = flags.has("png") ? flags.get("png").getAsString() : "";
            info.drapeauEmoji = flags.has("alt") ? flags.get("alt").getAsString() : "";
        }

        // Fuseaux horaires
        if (json.has("timezones") && json.get("timezones").isJsonArray()) {
            JsonArray tz = json.getAsJsonArray("timezones");
            StringBuilder sb = new StringBuilder();
            for (JsonElement el : tz) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(el.getAsString());
            }
            info.fuseauxHoraires = sb.toString();
        }

        return info;
    }

    /**
     * Formate un nombre de population en format lisible (ex: 67 390 000 → 67.4M).
     */
    public static String formatPopulation(long population) {
        if (population >= 1_000_000_000) {
            return String.format("%.1fMd", population / 1_000_000_000.0);
        } else if (population >= 1_000_000) {
            return String.format("%.1fM", population / 1_000_000.0);
        } else if (population >= 1_000) {
            return String.format("%.0fK", population / 1_000.0);
        }
        return String.valueOf(population);
    }

    /**
     * Classe interne contenant les informations d'un pays.
     */
    public static class CountryInfo {
        public String nomOfficiel = "";
        public String nomCommun = "";
        public String capitale = "";
        public long population = 0;
        public String region = "";
        public String sousRegion = "";
        public String continent = "";
        public String langues = "";
        public String devises = "";
        public String drapeauUrl = "";
        public String drapeauEmoji = "";
        public String fuseauxHoraires = "";

        /**
         * Construit un résumé formaté pour l'affichage.
         */
        public String toDisplayString() {
            StringBuilder sb = new StringBuilder();
            sb.append("🏳️ ").append(nomOfficiel).append("\n");
            if (!capitale.isEmpty()) sb.append("🏛️ Capitale : ").append(capitale).append("\n");
            sb.append("👥 Population : ").append(formatPopulation(population)).append("\n");
            if (!continent.isEmpty()) sb.append("🌍 Continent : ").append(continent).append("\n");
            if (!sousRegion.isEmpty()) sb.append("📍 Région : ").append(sousRegion).append("\n");
            if (!langues.isEmpty()) sb.append("🗣️ Langues : ").append(langues).append("\n");
            if (!devises.isEmpty()) sb.append("💱 Devises : ").append(devises).append("\n");
            if (!fuseauxHoraires.isEmpty()) sb.append("🕐 Fuseau : ").append(fuseauxHoraires);
            return sb.toString().trim();
        }
    }
}
