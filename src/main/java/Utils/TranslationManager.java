package Utils;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import javafx.application.Platform;

public class TranslationManager {

    private static String currentLanguage = "fr";
    private static Map<String, String> languageMap = new LinkedHashMap<>();
    private static Map<String, String> translationCache = new HashMap<>();
    private static Button translateButton;

    static {
        // Langues supportées
        languageMap.put("fr", "Français");
        languageMap.put("en", "English");
        languageMap.put("ar", "العربية");
        languageMap.put("es", "Español");
        languageMap.put("de", "Deutsch");
        languageMap.put("it", "Italiano");
    }

    public static Map<String, String> getSupportedLanguages() {
        return languageMap;
    }

    public static String getCurrentLanguage() {
        return currentLanguage;
    }

    public static void setCurrentLanguage(String langCode) {
        currentLanguage = langCode;
        translationCache.clear();
    }

    /**
     * Décode les séquences Unicode (\\uXXXX)
     */
    private static String decodeUnicode(String text) {
        if (text == null || !text.contains("\\u")) {
            return text;
        }

        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < text.length()) {
            if (text.charAt(i) == '\\' && i + 5 < text.length() && text.charAt(i + 1) == 'u') {
                try {
                    String hex = text.substring(i + 2, i + 6);
                    int code = Integer.parseInt(hex, 16);
                    sb.append((char) code);
                    i += 6;
                } catch (NumberFormatException e) {
                    sb.append(text.charAt(i));
                    i++;
                }
            } else {
                sb.append(text.charAt(i));
                i++;
            }
        }
        return sb.toString();
    }

    /**
     * Traduction via API MyMemory avec décodage Unicode
     */
    public static CompletableFuture<String> translateText(String text, String targetLanguage) {
        CompletableFuture<String> future = new CompletableFuture<>();

        if (targetLanguage.equals("fr") || text == null || text.trim().isEmpty()) {
            future.complete(text);
            return future;
        }

        String cacheKey = text + "|" + targetLanguage;
        if (translationCache.containsKey(cacheKey)) {
            future.complete(translationCache.get(cacheKey));
            return future;
        }

        // Ne pas traduire les textes avec des emojis ou symboles
        if (text.contains("📅") || text.contains("🔍") || text.contains("🏠") ||
                text.contains("📑") || text.contains("🎯") || text.contains("💰") ||
                text.contains("🔔") || text.contains("📍") || text.contains("↺") ||
                text.contains("⚙") || text.contains("🌐") || text.contains("✈") ||
                text.contains("🗺") || text.startsWith("+") || text.startsWith(">")) {
            future.complete(text);
            return future;
        }

        CompletableFuture.runAsync(() -> {
            try {
                String textEncoded = URLEncoder.encode(text, "UTF-8");
                URL url = new URL("https://api.mymemory.translated.net/get?q=" + textEncoded +
                        "&langpair=fr|" + targetLanguage + "&mt=1");

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("User-Agent", "Mozilla/5.0");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                if (conn.getResponseCode() == 200) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = in.readLine()) != null) {
                        response.append(line);
                    }
                    in.close();

                    String translated = extractTranslation(response.toString());

                    if (translated != null && !translated.isEmpty() && !translated.equals(text)) {
                        // DÉCODAGE UNICODE IMPORTANT !
                        translated = decodeUnicode(translated);
                        translationCache.put(cacheKey, translated);
                        future.complete(translated);
                    } else {
                        future.complete(text);
                    }
                } else {
                    future.complete(text);
                }

                conn.disconnect();

            } catch (Exception e) {
                System.err.println("Erreur de traduction: " + e.getMessage());
                future.complete(text);
            }
        });

        return future;
    }

    private static String extractTranslation(String json) {
        try {
            String key = "\"translatedText\":\"";
            int start = json.indexOf(key);
            if (start != -1) {
                start += key.length();
                int end = json.indexOf("\"", start);
                if (end != -1) {
                    return json.substring(start, end);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Traduit toute l'interface
     */
    public static void translateInterface(Node root, String targetLanguage) {
        setCurrentLanguage(targetLanguage);

        // Traduction du titre de la fenêtre
        if (root.getScene() != null && root.getScene().getWindow() instanceof javafx.stage.Stage) {
            javafx.stage.Stage stage = (javafx.stage.Stage) root.getScene().getWindow();
            String title = stage.getTitle();
            if (title != null && !title.isEmpty()) {
                translateText(title, targetLanguage).thenAccept(translated ->
                        Platform.runLater(() -> stage.setTitle(translated))
                );
            }
        }

        // Traduction récursive
        translateNode(root, targetLanguage);
    }

    /**
     * Traduit un nœud spécifique
     */
    private static void translateNode(Node node, String targetLanguage) {
        if (node == null) return;

        // Ignorer les Text nodes
        if (node instanceof Text) {
            return;
        }

        if (node instanceof Label) {
            Label label = (Label) node;
            String text = label.getText();
            if (text != null && !text.isEmpty() && !text.startsWith("📅") && !text.startsWith("🔍") && !text.startsWith("🏠")) {
                translateText(text, targetLanguage).thenAccept(translated ->
                        Platform.runLater(() -> {
                            if (!text.equals(translated)) {
                                label.setText(translated);
                            }
                        })
                );
            }
        }

        else if (node instanceof Button && node != translateButton) {
            Button button = (Button) node;
            String text = button.getText();
            if (text != null && !text.isEmpty() && !text.startsWith("+") && !text.startsWith("↺") && !text.startsWith("⚙") && !text.startsWith("🌐")) {
                translateText(text, targetLanguage).thenAccept(translated ->
                        Platform.runLater(() -> {
                            if (!text.equals(translated)) {
                                button.setText(translated);
                            }
                        })
                );
            }
        }

        else if (node instanceof TextField) {
            TextField field = (TextField) node;
            String prompt = field.getPromptText();
            if (prompt != null && !prompt.isEmpty()) {
                translateText(prompt, targetLanguage).thenAccept(translated ->
                        Platform.runLater(() -> field.setPromptText(translated))
                );
            }
        }

        else if (node instanceof ComboBox) {
            ComboBox<?> combo = (ComboBox<?>) node;
            String prompt = combo.getPromptText();
            if (prompt != null && !prompt.isEmpty()) {
                translateText(prompt, targetLanguage).thenAccept(translated ->
                        Platform.runLater(() -> combo.setPromptText(translated))
                );
            }
        }

        else if (node instanceof TitledPane) {
            TitledPane pane = (TitledPane) node;
            String text = pane.getText();
            if (text != null && !text.isEmpty()) {
                translateText(text, targetLanguage).thenAccept(translated ->
                        Platform.runLater(() -> {
                            if (!text.equals(translated)) {
                                pane.setText(translated);
                            }
                        })
                );
            }
        }

        // Traduction des enfants
        if (node instanceof Parent) {
            Parent parent = (Parent) node;
            for (Node child : parent.getChildrenUnmodifiable()) {
                translateNode(child, targetLanguage);
            }
        }
    }

    /**
     * Crée un bouton de traduction
     */
    public static Button createTranslationButton(Runnable onLanguageChange) {
        translateButton = new Button("🌐 Traduire");
        translateButton.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: 600; " +
                "-fx-background-radius: 20; -fx-padding: 8 16; -fx-cursor: hand; -fx-font-size: 12;");

        ContextMenu menu = new ContextMenu();

        for (Map.Entry<String, String> entry : languageMap.entrySet()) {
            MenuItem item = new MenuItem(entry.getValue());
            item.setOnAction(e -> {
                currentLanguage = entry.getKey();
                translateButton.setText("🌐 " + entry.getValue());
                if (onLanguageChange != null) {
                    onLanguageChange.run();
                }
            });
            menu.getItems().add(item);
        }

        translateButton.setOnMouseClicked(e -> {
            menu.show(translateButton, e.getScreenX(), e.getScreenY());
        });

        return translateButton;
    }
}