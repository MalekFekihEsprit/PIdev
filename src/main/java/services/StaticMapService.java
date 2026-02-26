package services;

import entities.Destination;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

public class StaticMapService {

    private static final String STATIC_MAP_API = "https://staticmap.openstreetmap.de/staticmap.php";

    /**
     * Generates a static map image URL for all destinations
     */
    public String getStaticMapUrl(List<Destination> destinations, int width, int height) {
        if (destinations == null || destinations.isEmpty()) {
            return getDefaultMapUrl(width, height);
        }

        try {
            // Calculate center and zoom level based on all destinations
            double minLat = 90, maxLat = -90, minLon = 180, maxLon = -180;
            int validCount = 0;

            for (Destination d : destinations) {
                if (d.getLatitude_destination() != 0 || d.getLongitude_destination() != 0) {
                    double lat = d.getLatitude_destination();
                    double lon = d.getLongitude_destination();
                    minLat = Math.min(minLat, lat);
                    maxLat = Math.max(maxLat, lat);
                    minLon = Math.min(minLon, lon);
                    maxLon = Math.max(maxLon, lon);
                    validCount++;
                }
            }

            if (validCount == 0) {
                return getDefaultMapUrl(width, height);
            }

            // Calculate center
            double centerLat = (minLat + maxLat) / 2;
            double centerLon = (minLon + maxLon) / 2;

            // Calculate zoom level (simplified)
            double latDiff = maxLat - minLat;
            double lonDiff = maxLon - minLon;
            int zoom = calculateZoom(latDiff, lonDiff, width, height);

            // Build markers string
            StringBuilder markers = new StringBuilder();
            for (Destination d : destinations) {
                if (d.getLatitude_destination() != 0 || d.getLongitude_destination() != 0) {
                    if (markers.length() > 0) markers.append("|");
                    String name = d.getNom_destination() + ", " + d.getPays_destination();
                    markers.append(String.format("%.6f,%.6f,%s",
                            d.getLatitude_destination(),
                            d.getLongitude_destination(),
                            URLEncoder.encode(name, "UTF-8")));
                }
            }

            // Build URL
            return String.format("%s?center=%.6f,%.6f&zoom=%d&size=%dx%d&maptype=mapnik&markers=%s",
                    STATIC_MAP_API, centerLat, centerLon, zoom, width, height,
                    markers.toString());

        } catch (Exception e) {
            e.printStackTrace();
            return getDefaultMapUrl(width, height);
        }
    }

    private int calculateZoom(double latDiff, double lonDiff, int width, int height) {
        // Simplified zoom calculation
        double maxDiff = Math.max(latDiff, lonDiff);
        if (maxDiff > 45) return 3;
        if (maxDiff > 20) return 4;
        if (maxDiff > 10) return 5;
        if (maxDiff > 5) return 6;
        if (maxDiff > 2) return 7;
        if (maxDiff > 1) return 8;
        return 9;
    }

    private String getDefaultMapUrl(int width, int height) {
        return String.format("%s?center=46.0,2.0&zoom=4&size=%dx%d&maptype=mapnik",
                STATIC_MAP_API, width, height);
    }

    /**
     * Downloads the static map image (optional)
     */
    public BufferedImage downloadMapImage(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("User-Agent", "TravelMate-App/1.0");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);

        if (conn.getResponseCode() == 200) {
            return ImageIO.read(conn.getInputStream());
        }
        return null;
    }
}