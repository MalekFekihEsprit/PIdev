package Utils;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class PhoneCodeUtil {

    public static String getCountryCodeFromIP() throws Exception {

        URL url = new URL("http://ip-api.com/json");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(
                new InputStreamReader(conn.getInputStream()));

        StringBuilder response = new StringBuilder();
        String line;

        while ((line = in.readLine()) != null) {
            response.append(line);
        }

        in.close();

        JSONObject obj = new JSONObject(response.toString());
        return obj.getString("countryCode");
    }
}