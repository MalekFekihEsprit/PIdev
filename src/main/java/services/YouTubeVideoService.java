package services;

import com.google.gson.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class YouTubeVideoService {

    private static final String YOUTUBE_SEARCH_URL = "https://www.googleapis.com/youtube/v3/search";
    private static final String YOUTUBE_VIDEO_URL = "https://www.youtube.com/watch?v=";
    private static final String YOUTUBE_VIDEO_DETAILS_URL = "https://www.googleapis.com/youtube/v3/videos";

    private static final int MIN_DURATION_SECONDS = 30; // Minimum 30 seconds
    private static final int MAX_DURATION_SECONDS = 240; // Keep max 4 minutes

    private final String apiKey;
    private final Gson gson;
    private final Connection dbConnection;
    private static final Logger LOGGER = Logger.getLogger(YouTubeVideoService.class.getName());

    public YouTubeVideoService(String apiKey, Connection dbConnection) {
        this.apiKey = apiKey;
        this.dbConnection = dbConnection;
        this.gson = new GsonBuilder().create();
    }

    // Async method (kept for compatibility)
    public CompletableFuture<String> fetchAndSaveVideo(String city, String country) {
        return CompletableFuture.supplyAsync(() -> fetchVideoUrl(city, country));
    }

    // Synchronous method that returns video URL directly using city AND country
    public String fetchVideoUrl(String city, String country) {
        try {
            String destination = city + ", " + country;
            LOGGER.info("Searching video for: " + destination);

            String videoId = searchVideo(city, country);

            if (videoId == null) {
                LOGGER.warning("No video found for: " + destination);
                return null;
            }

            // Check video duration
            int durationSeconds = getVideoDuration(videoId);

            if (durationSeconds < MIN_DURATION_SECONDS) {
                LOGGER.info("Video too short (" + durationSeconds + "s) for: " + destination);
                return null;
            }

            if (durationSeconds > MAX_DURATION_SECONDS) {
                LOGGER.info("Video too long (" + durationSeconds + "s) for: " + destination);
                return null;
            }

            String videoUrl = YOUTUBE_VIDEO_URL + videoId;
            LOGGER.info("Found video for " + destination + ": " + videoUrl + " (duration: " + durationSeconds + "s)");
            return videoUrl;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error fetching video for: " + city + ", " + country, e);
            return null;
        }
    }

    // Overloaded method for backward compatibility
    public String fetchVideoUrl(String destination) {
        String[] parts = destination.split(",");
        if (parts.length == 2) {
            return fetchVideoUrl(parts[0].trim(), parts[1].trim());
        }
        return null;
    }

    private String searchVideo(String city, String country) throws Exception {
        // Create multiple search queries for better results
        String[] queries = {
                city + " " + country + " travel guide",
                city + " tourism video",
                "visit " + city + " " + country,
                city + " attractions",
                city + " " + country + " drone 4k"
        };

        // Try each query until we find a video with acceptable duration
        for (String query : queries) {
            LOGGER.info("Searching with query: " + query);
            String videoId = searchWithQuery(query);
            if (videoId != null) {
                // Check duration before accepting
                int duration = getVideoDuration(videoId);
                if (duration >= MIN_DURATION_SECONDS && duration <= MAX_DURATION_SECONDS) {
                    LOGGER.info("Found acceptable video with query: " + query + " (duration: " + duration + "s)");
                    return videoId;
                } else {
                    LOGGER.info("Video from query '" + query + "' has unsuitable duration: " + duration + "s");
                }
            }
        }

        return null;
    }

    private String searchWithQuery(String query) throws Exception {
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.toString());

        String urlString = YOUTUBE_SEARCH_URL +
                "?part=snippet" +
                "&q=" + encodedQuery +
                "&type=video" +
                "&videoDuration=short" + // YouTube's built-in short filter (under 20 min)
                "&maxResults=10" + // Get more results to check durations
                "&relevanceLanguage=fr" +
                "&key=" + apiKey;

        String response = makeApiCall(urlString);
        return parseSearchResponse(response);
    }

    private int getVideoDuration(String videoId) throws Exception {
        String urlString = YOUTUBE_VIDEO_DETAILS_URL +
                "?part=contentDetails" +
                "&id=" + videoId +
                "&key=" + apiKey;

        String response = makeApiCall(urlString);
        return parseDurationResponse(response);
    }

    private int parseDurationResponse(String jsonResponse) {
        try {
            JsonObject root = JsonParser.parseString(jsonResponse).getAsJsonObject();

            if (root.has("items") && root.get("items").isJsonArray()) {
                JsonArray items = root.getAsJsonArray("items");

                if (items.size() > 0) {
                    JsonObject firstItem = items.get(0).getAsJsonObject();
                    JsonObject contentDetails = firstItem.getAsJsonObject("contentDetails");

                    if (contentDetails.has("duration")) {
                        String durationIso = contentDetails.get("duration").getAsString();
                        return convertIsoDurationToSeconds(durationIso);
                    }
                }
            }

            return 0;

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error parsing duration response", e);
            return 0;
        }
    }

    /**
     * Convert ISO 8601 duration (PT1H2M3S) to seconds
     */
    private int convertIsoDurationToSeconds(String isoDuration) {
        int seconds = 0;

        // Remove "PT" prefix
        String duration = isoDuration.substring(2);

        StringBuilder number = new StringBuilder();
        for (int i = 0; i < duration.length(); i++) {
            char c = duration.charAt(i);
            if (Character.isDigit(c)) {
                number.append(c);
            } else {
                if (number.length() > 0) {
                    int value = Integer.parseInt(number.toString());
                    switch (c) {
                        case 'H':
                            seconds += value * 3600;
                            break;
                        case 'M':
                            seconds += value * 60;
                            break;
                        case 'S':
                            seconds += value;
                            break;
                    }
                    number = new StringBuilder();
                }
            }
        }

        return seconds;
    }

    private String makeApiCall(String urlString) throws Exception {
        HttpURLConnection conn = null;

        try {
            URL url = new URL(urlString);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            int responseCode = conn.getResponseCode();

            if (responseCode == 200) {
                return readResponse(conn.getInputStream());
            } else {
                String error = readResponse(conn.getErrorStream());
                throw new Exception("YouTube API error " + responseCode + ": " + error);
            }

        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    private String readResponse(InputStream stream) throws IOException {
        if (stream == null) return "";

        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }
        return response.toString();
    }

    private String parseSearchResponse(String jsonResponse) {
        try {
            JsonObject root = JsonParser.parseString(jsonResponse).getAsJsonObject();

            if (root.has("items") && root.get("items").isJsonArray()) {
                JsonArray items = root.getAsJsonArray("items");

                if (items.size() > 0) {
                    // Just return the first video ID - we'll check duration later
                    JsonObject firstItem = items.get(0).getAsJsonObject();
                    JsonObject id = firstItem.getAsJsonObject("id");

                    if (id.has("videoId")) {
                        return id.get("videoId").getAsString();
                    }
                }
            }

            return null;

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error parsing search response", e);
            return null;
        }
    }
}