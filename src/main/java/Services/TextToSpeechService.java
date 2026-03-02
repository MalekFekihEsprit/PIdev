package Services;

/**
 * Services/TextToSpeechService.java — version corrigée
 * ZÉRO dépendance externe — utilise uniquement les outils natifs du système.
 *
 * Windows : PowerShell + SAPI (intégré à Windows, aucune installation)
 * macOS   : commande 'say' (native macOS)
 * Linux   : espeak (sudo apt install espeak)
 */
public class TextToSpeechService {

    public enum Engine { WINDOWS, MAC, LINUX, UNAVAILABLE }

    private final Engine  engine;
    private       boolean available = false;

    public TextToSpeechService() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            engine    = Engine.WINDOWS;
            available = true;
            System.out.println("[TTS] ✅ Windows PowerShell SAPI prêt.");
        } else if (os.contains("mac")) {
            engine    = Engine.MAC;
            available = true;
            System.out.println("[TTS] ✅ macOS 'say' prêt.");
        } else if (os.contains("nix") || os.contains("nux")) {
            engine    = Engine.LINUX;
            available = isEspeakAvailable();
            System.out.println("[TTS] " + (available
                    ? "✅ espeak disponible."
                    : "⚠ espeak absent — installez avec: sudo apt install espeak"));
        } else {
            engine = Engine.UNAVAILABLE;
        }
    }

    // ─────────────────────────────────────────────────────────
    //  API publique
    // ─────────────────────────────────────────────────────────

    /** Prononce un texte en arrière-plan (ne bloque pas l'UI JavaFX). */
    public void speak(String text) {
        if (text == null || text.isBlank()) return;
        String clean = cleanText(text);
        System.out.println("[TTS] → " + clean);
        if (!available) return;

        Thread t = new Thread(() -> {
            try {
                switch (engine) {
                    case WINDOWS: speakWindows(clean); break;
                    case MAC:     speakMac(clean);     break;
                    case LINUX:   speakLinux(clean);   break;
                    default: break;
                }
            } catch (Exception e) {
                System.err.println("[TTS] Erreur: " + e.getMessage());
            }
        });
        t.setDaemon(true);
        t.start();
    }

    /** Répond vocalement à une commande vocale reconnue. */
    public void respondToCommand(VoiceCommandProcessor.VoiceCommand cmd,
                                 double budgetRestant, String deviseBudget) {
        String response;
        switch (cmd.type) {
            case ADD_EXPENSE:
                response = cmd.amount > 0
                        ? String.format("Depense de %.0f %s ajoutee pour %s",
                        cmd.amount,
                        cmd.fromCurrency != null ? cmd.fromCurrency : "euros",
                        cmd.label != null ? cmd.label : cmd.category)
                        : "Formulaire de depense ouvert";
                break;
            case CHECK_BUDGET:
                response = budgetRestant >= 0
                        ? String.format("Il vous reste %.2f %s", budgetRestant, deviseBudget)
                        : String.format("Budget depasse de %.2f %s", Math.abs(budgetRestant), deviseBudget);
                break;
            case FILTER_EXPENSES:
                response = "Filtre : " + (cmd.category != null ? cmd.category : "toutes categories");
                break;
            case CONVERT_CURRENCY:
                response = "Conversion en cours";
                break;
            case EXPORT_PDF:   response = "Export PDF lance";    break;
            case EXPORT_EXCEL: response = "Export Excel lance";  break;
            case RESET_FILTERS: response = "Filtres reinitialises"; break;
            default:           response = "Commande non reconnue"; break;
        }
        speak(response);
    }

    public boolean isAvailable() { return available; }
    public void close() { /* rien à fermer */ }

    // ─────────────────────────────────────────────────────────
    //  Moteurs natifs
    // ─────────────────────────────────────────────────────────

    /**
     * Windows — PowerShell + System.Speech.Synthesis (intégré depuis Windows 7).
     * Voix françaises disponibles si le pack de langue FR est installé.
     */
    private void speakWindows(String text) throws Exception {
        String safe = text.replace("'", " ").replace("\"", " ");
        String[] cmd = {
                "powershell", "-NoProfile", "-Command",
                "Add-Type -AssemblyName System.Speech; " +
                        "$synth = New-Object System.Speech.Synthesis.SpeechSynthesizer; " +
                        "$synth.Rate = 0; " +
                        "$synth.Speak('" + safe + "');"
        };
        Process p = Runtime.getRuntime().exec(cmd);
        p.waitFor();
    }

    /** macOS — 'say' avec voix Thomas (française). */
    private void speakMac(String text) throws Exception {
        String safe = text.replace("'", " ");
        Process p = Runtime.getRuntime().exec(new String[]{"say", "-v", "Thomas", safe});
        p.waitFor();
    }

    /** Linux — espeak avec voix française. */
    private void speakLinux(String text) throws Exception {
        String safe = text.replace("'", " ").replace("\"", " ");
        Process p = Runtime.getRuntime().exec(
                new String[]{"espeak", "-v", "fr", "-s", "140", safe});
        p.waitFor();
    }

    private boolean isEspeakAvailable() {
        try {
            return Runtime.getRuntime().exec("espeak --version").waitFor() == 0;
        } catch (Exception e) { return false; }
    }

    /** Supprime les emojis qui bloquent la synthèse vocale. */
    private String cleanText(String text) {
        return text.replaceAll("[^\\x00-\\x7FÀ-ÿ0-9 .,%€$]", "").trim();
    }
}