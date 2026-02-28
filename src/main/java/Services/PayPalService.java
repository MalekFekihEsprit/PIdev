package Services;

import Utils.Config;
import Utils.ConfigV;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;

import java.io.IOException;
import java.util.Base64;

public class PayPalService {

    private static final String PAYPAL_API_URL = ConfigV.PAYPAL_MODE.equals("live")
            ? "https://api-m.paypal.com"
            : "https://api-m.sandbox.paypal.com";

    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();
    private String accessToken;
    private long tokenExpirationTime;

    /**
     * Obtenir un token d'accès PayPal
     */
    private String getAccessToken() throws IOException {
        // Vérifier si le token est encore valide (moins de 9 heures)
        if (accessToken != null && System.currentTimeMillis() < tokenExpirationTime) {
            return accessToken;
        }

        String auth = ConfigV.PAYPAL_CLIENT_ID + ":" + ConfigV.PAYPAL_CLIENT_SECRET;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());

        Request request = new Request.Builder()
                .url(PAYPAL_API_URL + "/v1/oauth2/token")
                .post(RequestBody.create("grant_type=client_credentials",
                        MediaType.parse("application/x-www-form-urlencoded")))
                .addHeader("Authorization", "Basic " + encodedAuth)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Erreur PayPal: " + response.code());
            }

            String responseBody = response.body().string();
            JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();

            this.accessToken = json.get("access_token").getAsString();
            int expiresIn = json.get("expires_in").getAsInt();
            this.tokenExpirationTime = System.currentTimeMillis() + (expiresIn * 1000L) - 60000; // 1 minute de marge

            return accessToken;
        }
    }

    /**
     * Créer un paiement PayPal
     */
    public JsonObject createPayment(
            Double total,
            String currency,
            String description,
            String cancelUrl,
            String successUrl
    ) throws IOException {

        String accessToken = getAccessToken();

        JsonObject paymentJson = new JsonObject();
        paymentJson.addProperty("intent", "sale");

        // Payer
        JsonObject payer = new JsonObject();
        payer.addProperty("payment_method", "paypal");
        paymentJson.add("payer", payer);

        // Transactions
        JsonObject amount = new JsonObject();
        amount.addProperty("total", String.format("%.2f", total));
        amount.addProperty("currency", currency);

        JsonObject transaction = new JsonObject();
        transaction.add("amount", amount);
        transaction.addProperty("description", description);

        JsonArray transactions = new JsonArray();  // Maintenant reconnu grâce à l'import
        transactions.add(transaction);
        paymentJson.add("transactions", transactions);

        // Redirect URLs
        JsonObject redirectUrls = new JsonObject();
        redirectUrls.addProperty("return_url", successUrl);
        redirectUrls.addProperty("cancel_url", cancelUrl);
        paymentJson.add("redirect_urls", redirectUrls);

        RequestBody body = RequestBody.create(
                gson.toJson(paymentJson),
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(PAYPAL_API_URL + "/v1/payments/payment")
                .post(body)
                .addHeader("Authorization", "Bearer " + accessToken)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Erreur PayPal: " + response.code() + " - " + response.body().string());
            }

            String responseBody = response.body().string();
            return JsonParser.parseString(responseBody).getAsJsonObject();
        }
    }

    /**
     * Exécuter un paiement PayPal
     */
    public JsonObject executePayment(String paymentId, String payerId) throws IOException {
        String accessToken = getAccessToken();

        JsonObject executeJson = new JsonObject();
        executeJson.addProperty("payer_id", payerId);

        RequestBody body = RequestBody.create(
                gson.toJson(executeJson),
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(PAYPAL_API_URL + "/v1/payments/payment/" + paymentId + "/execute")
                .post(body)
                .addHeader("Authorization", "Bearer " + accessToken)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Erreur PayPal: " + response.code() + " - " + response.body().string());
            }

            String responseBody = response.body().string();
            return JsonParser.parseString(responseBody).getAsJsonObject();
        }
    }

    /**
     * Rembourser un paiement PayPal
     */
    public JsonObject refundPayment(String saleId) throws IOException {
        String accessToken = getAccessToken();

        JsonObject refundJson = new JsonObject();
        // Ajouter des paramètres si nécessaire (montant partiel, etc.)

        RequestBody body = RequestBody.create(
                gson.toJson(refundJson),
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(PAYPAL_API_URL + "/v1/payments/sale/" + saleId + "/refund")
                .post(body)
                .addHeader("Authorization", "Bearer " + accessToken)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Erreur PayPal: " + response.code() + " - " + response.body().string());
            }

            String responseBody = response.body().string();
            return JsonParser.parseString(responseBody).getAsJsonObject();
        }
    }

    /**
     * Obtenir les détails d'un paiement
     */
    public JsonObject getPaymentDetails(String paymentId) throws IOException {
        String accessToken = getAccessToken();

        Request request = new Request.Builder()
                .url(PAYPAL_API_URL + "/v1/payments/payment/" + paymentId)
                .get()
                .addHeader("Authorization", "Bearer " + accessToken)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Erreur PayPal: " + response.code() + " - " + response.body().string());
            }

            String responseBody = response.body().string();
            return JsonParser.parseString(responseBody).getAsJsonObject();
        }
    }
}