package Services;

import Utils.Country;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class CountryService {

    private static List<Country> cachedCountries = null;

    public static List<Country> getAllCountries() throws Exception {

        // ✅ If already fetched, return cache
        if (cachedCountries != null) {
            return cachedCountries;
        }

        String url = "https://restcountries.com/v3.1/all?fields=name,cca2,idd,flags";

        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(
                new InputStreamReader(conn.getInputStream()));

        StringBuilder response = new StringBuilder();
        String line;

        while ((line = in.readLine()) != null) {
            response.append(line);
        }

        in.close();

        JSONArray array = new JSONArray(response.toString());

        List<Country> countries = new ArrayList<>();

        for (int i = 0; i < array.length(); i++) {

            JSONObject obj = array.getJSONObject(i);

            if (!obj.has("idd")) continue;

            JSONObject idd = obj.getJSONObject("idd");

            if (!idd.has("root") || !idd.has("suffixes")) continue;

            String root = idd.optString("root");
            JSONArray suffixes = idd.optJSONArray("suffixes");

            if (root == null || suffixes == null || suffixes.length() == 0) continue;

            String dial = root + suffixes.getString(0);

            String name = obj.getJSONObject("name").getString("common");
            String iso = obj.getString("cca2");
            String flag = obj.getJSONObject("flags").getString("png");

            countries.add(new Country(name, iso, dial, flag));
        }

        cachedCountries = countries; // ✅ Cache in memory

        return cachedCountries;
    }
}