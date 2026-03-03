package Utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import okhttp3.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class FaceRecognitionClient {
    private static final String BASE_URL = "http://127.0.0.1:8000";
    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();

    public List<Double> extractEmbedding(File imageFile) throws IOException {
        RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", imageFile.getName(),
                        RequestBody.create(MediaType.parse("image/jpeg"), imageFile))
                .build();

        Request request = new Request.Builder()
                .url(BASE_URL + "/extract")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body().string();
            System.out.println("Réponse de /extract : " + responseBody);

            if (!response.isSuccessful()) return null;

            Type type = new TypeToken<Map<String, Object>>(){}.getType();
            Map<String, Object> result = gson.fromJson(responseBody, type);

            if (result.get("success") != null && (Boolean) result.get("success")) {
                // L'embedding est une liste de nombres
                return (List<Double>) result.get("embedding");
            }
            return null;
        }
    }

    public boolean compareEmbeddings(List<Double> emb1, List<Double> emb2) throws IOException {
        Map<String, Object> requestBody = Map.of(
                "embedding1", emb1,
                "embedding2", emb2
        );
        String json = gson.toJson(requestBody);
        System.out.println("Envoi à /compare : " + json);

        Request request = new Request.Builder()
                .url(BASE_URL + "/compare")
                .post(RequestBody.create(MediaType.parse("application/json"), json))
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body().string();
            System.out.println("Réponse de /compare : " + responseBody);

            if (!response.isSuccessful()) return false;

            Type type = new TypeToken<Map<String, Object>>(){}.getType();
            Map<String, Object> result = gson.fromJson(responseBody, type);
            return (boolean) result.get("is_match");
        }
    }
}