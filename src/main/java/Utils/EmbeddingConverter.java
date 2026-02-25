package Utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.List;

public class EmbeddingConverter {
    private static final Gson gson = new Gson();
    private static final Type listType = new TypeToken<List<Double>>(){}.getType();

    public static String toJson(List<Double> embedding) {
        return gson.toJson(embedding);
    }

    public static List<Double> fromJson(String json) {
        if (json == null || json.isEmpty()) return null;
        return gson.fromJson(json, listType);
    }
}