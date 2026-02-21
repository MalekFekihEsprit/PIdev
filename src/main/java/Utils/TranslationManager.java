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
import java.util.concurrent.ConcurrentHashMap;
import javafx.application.Platform;

public class TranslationManager {

    private static String currentLanguage = "fr";
    private static Map<String, String> languageMap = new LinkedHashMap<>();
    private static Map<String, String> translationCache = new ConcurrentHashMap<>();
    private static Button translateButton;
    private static CompletableFuture<Void> currentTranslation = CompletableFuture.completedFuture(null);

    // Symboles et textes à NE PAS TRADUIRE
    private static final Set<String> NON_TRANSLATABLE = new HashSet<>(Arrays.asList(
            "📍", "📋", "💰", "📅", "🔍", "🏠", "🗺", "🎯", "📑", "✈", "🔔",
            "▼", "+", "↺", "⚙", "🌐", "🌤", "🌸", "☀", "🍂", "❄", "🏆", "👥", "📊", "👤"
    ));

    static {
        // Langues supportées
        languageMap.put("fr", "Français");
        languageMap.put("en", "English");
        languageMap.put("ar", "العربية");
        languageMap.put("es", "Español");
        languageMap.put("de", "Deutsch");
        languageMap.put("it", "Italiano");
        languageMap.put("pt", "Português");
        languageMap.put("ru", "Русский");
        languageMap.put("zh", "中文");
        languageMap.put("ja", "日本語");
    }

    public static Map<String, String> getSupportedLanguages() {
        return languageMap;
    }

    public static String getCurrentLanguage() {
        return currentLanguage;
    }

    public static void setCurrentLanguage(String langCode) {
        currentLanguage = langCode;
        translationCache.clear(); // Vider le cache à chaque changement
    }

    /**
     * Vérifie si un texte doit être traduit
     */
    private static boolean shouldTranslate(String text) {
        if (text == null || text.trim().isEmpty()) return false;

        // NE PAS TRADUIRE LES DATES
        if (text.matches(".*\\d{4}.*") || text.matches(".*\\d{2}/\\d{2}/\\d{4}.*")) {
            return false;
        }

        // NE PAS TRADUIRE LES TEXTES EN MAJUSCULES (statistiques)
        if (text.equals(text.toUpperCase()) && text.length() > 3) {
            return false;
        }

        // NE PAS TRADUIRE LES CHIFFRES SEULS
        if (text.matches("^\\d+$") || text.matches("^\\d+\\s*€$")) {
            return false;
        }

        // NE PAS TRADUIRE LES SYMBOLES
        if (NON_TRANSLATABLE.contains(text)) {
            return false;
        }

        return true;
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
     * Traduction via API MyMemory
     */
    public static CompletableFuture<String> translateText(String text, String targetLanguage) {
        CompletableFuture<String> future = new CompletableFuture<>();

        if (targetLanguage.equals("fr") || !shouldTranslate(text)) {
            future.complete(text);
            return future;
        }

        String cacheKey = text + "|" + targetLanguage;
        if (translationCache.containsKey(cacheKey)) {
            future.complete(translationCache.get(cacheKey));
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
                System.err.println("Erreur de traduction pour: '" + text + "' -> " + e.getMessage());
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
     * Traduit toute l'interface de façon séquentielle
     */
    public static void translateInterface(Node root, String targetLanguage) {
        if (root == null) return;

        // IMPORTANT: Vider le cache et changer la langue avant de commencer
        setCurrentLanguage(targetLanguage);

        // Exécuter la traduction de façon séquentielle
        currentTranslation = currentTranslation.thenRun(() ->
                performTranslation(root, targetLanguage)
        );
    }

    private static void performTranslation(Node root, String targetLanguage) {
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        // Traduction du titre de la fenêtre
        if (root.getScene() != null && root.getScene().getWindow() instanceof javafx.stage.Stage) {
            javafx.stage.Stage stage = (javafx.stage.Stage) root.getScene().getWindow();
            String title = stage.getTitle();
            if (title != null && !title.isEmpty() && shouldTranslate(title)) {
                futures.add(translateText(title, targetLanguage).thenAccept(translated ->
                        Platform.runLater(() -> stage.setTitle(translated))
                ));
            }
        }

        // Traduction récursive
        collectTranslationFutures(root, targetLanguage, futures);

        // Attendre que toutes les traductions soient finies
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    private static void collectTranslationFutures(Node node, String targetLanguage, List<CompletableFuture<Void>> futures) {
        if (node == null) return;

        // Ignorer les Text nodes
        if (node instanceof Text) {
            return;
        }

        if (node instanceof Label) {
            Label label = (Label) node;
            String text = label.getText();
            if (text != null && !text.isEmpty() && shouldTranslate(text)) {
                futures.add(translateText(text, targetLanguage).thenAccept(translated ->
                        Platform.runLater(() -> {
                            if (!text.equals(translated)) {
                                label.setText(translated);
                            }
                        })
                ));
            }
        }

        else if (node instanceof Button && node != translateButton) {
            Button button = (Button) node;
            String text = button.getText();
            if (text != null && !text.isEmpty() && shouldTranslate(text)) {
                futures.add(translateText(text, targetLanguage).thenAccept(translated ->
                        Platform.runLater(() -> {
                            if (!text.equals(translated)) {
                                button.setText(translated);
                            }
                        })
                ));
            }
        }

        else if (node instanceof TextField) {
            TextField field = (TextField) node;
            String prompt = field.getPromptText();
            if (prompt != null && !prompt.isEmpty() && shouldTranslate(prompt)) {
                futures.add(translateText(prompt, targetLanguage).thenAccept(translated ->
                        Platform.runLater(() -> field.setPromptText(translated))
                ));
            }
        }

        else if (node instanceof ComboBox) {
            ComboBox<?> combo = (ComboBox<?>) node;
            String prompt = combo.getPromptText();
            if (prompt != null && !prompt.isEmpty() && shouldTranslate(prompt)) {
                futures.add(translateText(prompt, targetLanguage).thenAccept(translated ->
                        Platform.runLater(() -> combo.setPromptText(translated))
                ));
            }
        }

        else if (node instanceof TitledPane) {
            TitledPane pane = (TitledPane) node;
            String text = pane.getText();
            if (text != null && !text.isEmpty() && shouldTranslate(text)) {
                futures.add(translateText(text, targetLanguage).thenAccept(translated ->
                        Platform.runLater(() -> {
                            if (!text.equals(translated)) {
                                pane.setText(translated);
                            }
                        })
                ));
            }
        }

        // Traduction des enfants
        if (node instanceof Parent) {
            Parent parent = (Parent) node;
            for (Node child : parent.getChildrenUnmodifiable()) {
                collectTranslationFutures(child, targetLanguage, futures);
            }
        }
    }

    /**
     * Traduit les colonnes d'un TableView
     */
    @SuppressWarnings("rawtypes")
    public static void translateTableColumns(TableView tableView, String targetLanguage) {
        if (tableView == null) return;

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (Object col : tableView.getColumns()) {
            if (col instanceof TableColumn) {
                TableColumn column = (TableColumn) col;
                String text = column.getText();
                if (text != null && !text.isEmpty() && shouldTranslate(text)) {
                    futures.add(translateText(text, targetLanguage).thenAccept(translated ->
                            Platform.runLater(() -> {
                                if (!text.equals(translated)) {
                                    column.setText(translated);
                                }
                            })
                    ));
                }
            }
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
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
                // IMPORTANT: Vider le cache avant de changer de langue
                translationCache.clear();
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