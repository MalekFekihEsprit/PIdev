package Services;

import org.vosk.Model;
import org.vosk.Recognizer;
import javax.sound.sampled.*;
import javafx.application.Platform;
import java.io.IOException;
import java.util.function.Consumer;

/**
 * Services/VocalAssistantService.java
 * Écoute le microphone en temps réel et retourne le texte reconnu.
 * Utilise Vosk — 100% offline, aucune clé API requise.
 *
 * Modèle français : https://alphacephei.com/vosk/models
 * → Téléchargez vosk-model-small-fr-0.22 dans src/main/resources/models/
 */
public class VocalAssistantService {

    private static final String MODEL_PATH = "src/main/resources/models/vosk-model-fr";
    private static final int    SAMPLE_RATE = 16000;

    private Model      model;
    private Recognizer recognizer;
    private TargetDataLine microphone;
    private Thread     listenThread;
    private boolean    isListening = false;
    private boolean    initialized = false;

    // Callback appelé quand un texte est reconnu
    private Consumer<String> onTextRecognized;
    // Callback appelé pour les mises à jour de statut
    private Consumer<String> onStatusUpdate;

    public VocalAssistantService() {}

    // ─────────────────────────────────────────────────────────
    //  Initialisation
    // ─────────────────────────────────────────────────────────

    /**
     * Charge le modèle Vosk. Doit être appelé une seule fois.
     * Prend 2-3 secondes la première fois.
     */
    public synchronized boolean initialize() {
        if (initialized) return true;
        try {
            System.out.println("[VocalAssistant] Chargement du modèle...");
            System.setProperty("jna.library.path", "lib");
            model = new Model(MODEL_PATH);
            recognizer = new Recognizer(model, SAMPLE_RATE);
            initialized = true;
            System.out.println("[VocalAssistant] ✅ Modèle chargé.");
            return true;
        } catch (IOException e) {
            System.err.println("[VocalAssistant] ❌ Modèle introuvable: " + e.getMessage());
            System.err.println("[VocalAssistant] → Téléchargez le modèle sur: https://alphacephei.com/vosk/models");
            return false;
        }
    }

    // ─────────────────────────────────────────────────────────
    //  Écoute
    // ─────────────────────────────────────────────────────────

    /**
     * Démarre l'écoute du microphone en arrière-plan.
     */
    public void startListening() {
        if (isListening) {
            System.out.println("[VocalAssistant] Déjà en écoute.");
            return;
        }
        System.out.println("[VocalAssistant] startListening() appelé.");

        if (!initialized && !initialize()) {
            notifyStatus("❌ Modèle non chargé");
            return;
        }

        try {
            AudioFormat format = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

            System.out.println("[VocalAssistant] Recherche microphone...");
            if (!AudioSystem.isLineSupported(info)) {
                notifyStatus("❌ Microphone non supporté");
                return;
            }

            microphone = (TargetDataLine) AudioSystem.getLine(info);
            microphone.open(format);
            microphone.start();
            System.out.println("[VocalAssistant] Microphone ouvert et démarré.");

            isListening = true;
            notifyStatus("🎙️ Écoute en cours...");

            listenThread = new Thread(this::listenLoop);
            listenThread.setDaemon(true);
            listenThread.start();
            System.out.println("[VocalAssistant] Thread d'écoute lancé.");

        } catch (LineUnavailableException e) {
            System.err.println("[VocalAssistant] ❌ Microphone indisponible: " + e.getMessage());
            notifyStatus("❌ Microphone indisponible");
        }
    }

    /**
     * Arrête l'écoute.
     */
    public void stopListening() {
        if (!isListening) return;
        System.out.println("[VocalAssistant] stopListening() appelé. Stack trace:");
        new Exception().printStackTrace(); // Affiche l'appelant

        isListening = false;
        if (microphone != null) {
            microphone.stop();
            microphone.close();
            System.out.println("[VocalAssistant] Microphone fermé.");
        }
        notifyStatus("⏹️ Écoute arrêtée");
        System.out.println("[VocalAssistant] Écoute arrêtée.");
    }

    /**
     * Toggle écoute on/off.
     */
    public void toggleListening() {
        if (isListening) stopListening();
        else             startListening();
    }

    public boolean isListening() { return isListening; }

    // ─────────────────────────────────────────────────────────
    //  Boucle d'écoute principale
    // ─────────────────────────────────────────────────────────

    private void listenLoop() {
        if (recognizer == null) {
            System.err.println("[VocalAssistant] Recognizer null, abandon.");
            Platform.runLater(this::stopListening);
            return;
        }

        byte[] buffer = new byte[4096];
        System.out.println("[VocalAssistant] Boucle d'écoute démarrée.");
        try {
            while (isListening) {
                int bytesRead = microphone.read(buffer, 0, buffer.length);
                if (bytesRead > 0) {
                    // Log occasionnel pour voir l'activité (toutes les ~5 secondes)
                    if (System.currentTimeMillis() % 5000 < 100) {
                        System.out.println("[VocalAssistant] Données audio reçues...");
                    }
                    if (recognizer.acceptWaveForm(buffer, bytesRead)) {
                        String result = recognizer.getResult();
                        String text   = extractText(result);
                        if (!text.isBlank()) {
                            System.out.println("[VocalAssistant] Reconnu: " + text);
                            Platform.runLater(() -> {
                                if (onTextRecognized != null)
                                    onTextRecognized.accept(text);
                            });
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[VocalAssistant] Erreur dans listenLoop: " + e.getMessage());
            e.printStackTrace();
            Platform.runLater(() -> {
                stopListening();
                notifyStatus("❌ Erreur écoute");
            });
        }
        System.out.println("[VocalAssistant] Boucle d'écoute terminée.");
    }

    /**
     * Extrait le texte du JSON Vosk: {"text": "bonjour"} → "bonjour"
     */
    private String extractText(String json) {
        try {
            int start = json.indexOf("\"text\"");
            if (start == -1) return "";
            int q1 = json.indexOf('"', start + 7);
            int q2 = json.indexOf('"', q1 + 1);
            if (q1 == -1 || q2 == -1) return "";
            return json.substring(q1 + 1, q2).trim();
        } catch (Exception e) {
            return "";
        }
    }

    // ─────────────────────────────────────────────────────────
    //  Callbacks
    // ─────────────────────────────────────────────────────────

    public void setOnTextRecognized(Consumer<String> callback) {
        this.onTextRecognized = callback;
    }

    public void setOnStatusUpdate(Consumer<String> callback) {
        this.onStatusUpdate = callback;
    }

    private void notifyStatus(String status) {
        Platform.runLater(() -> {
            if (onStatusUpdate != null) onStatusUpdate.accept(status);
        });
    }

    // ─────────────────────────────────────────────────────────
    //  Libération ressources
    // ─────────────────────────────────────────────────────────

    public void close() {
        stopListening();
        if (recognizer != null) recognizer.close();
        if (model      != null) model.close();
    }
}