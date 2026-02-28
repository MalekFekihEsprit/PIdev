package Services;

import Utils.Config;
import Utils.ConfigV;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;

import java.io.IOException;

public class StripeService {

    private static final String STRIPE_API_URL = "https://api.stripe.com/v1";
    private final OkHttpClient client = new OkHttpClient();

    public JsonObject createPaymentIntent(
            Long amount,
            String currency,
            String description,
            String email
    ) throws IOException {

        FormBody.Builder formBuilder = new FormBody.Builder()
                .add("amount", String.valueOf(amount))
                .add("currency", currency.toLowerCase())
                .add("description", description)
                .add("receipt_email", email)
                .add("automatic_payment_methods[enabled]", "true");

        RequestBody body = formBuilder.build();

        Request request = new Request.Builder()
                .url(STRIPE_API_URL + "/payment_intents")
                .post(body)
                .addHeader("Authorization", "Bearer " + ConfigV.STRIPE_SECRET_KEY)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Erreur Stripe: " + response.code() + " - " + response.body().string());
            }

            String responseBody = response.body().string();
            return JsonParser.parseString(responseBody).getAsJsonObject();
        }
    }

    // ✅ MÉTHODE refundPayment avec 1 paramètre
    public JsonObject refundPayment(String paymentIntentId) throws IOException {
        FormBody body = new FormBody.Builder()
                .add("payment_intent", paymentIntentId)
                .build();

        Request request = new Request.Builder()
                .url(STRIPE_API_URL + "/refunds")
                .post(body)
                .addHeader("Authorization", "Bearer " + ConfigV.STRIPE_SECRET_KEY)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Erreur Stripe: " + response.code() + " - " + response.body().string());
            }

            String responseBody = response.body().string();
            return JsonParser.parseString(responseBody).getAsJsonObject();
        }
    }

    // ✅ MÉTHODE refundPayment avec 2 paramètres (optionnelle)
    public JsonObject refundPayment(String paymentIntentId, Long amount) throws IOException {
        FormBody body = new FormBody.Builder()
                .add("payment_intent", paymentIntentId)
                .add("amount", String.valueOf(amount))
                .build();

        Request request = new Request.Builder()
                .url(STRIPE_API_URL + "/refunds")
                .post(body)
                .addHeader("Authorization", "Bearer " + ConfigV.STRIPE_SECRET_KEY)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Erreur Stripe: " + response.code() + " - " + response.body().string());
            }

            String responseBody = response.body().string();
            return JsonParser.parseString(responseBody).getAsJsonObject();
        }
    }
}