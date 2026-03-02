package Services;

import java.util.function.Consumer;

/**
 * Services/VoiceCommandProcessor.java
 * Analyse le texte reconnu et détecte la commande vocale correspondante.
 *
 * Commandes supportées (français) :
 *   "ajoute une dépense de 200 euros pour l'hôtel"
 *   "quel est mon budget restant"
 *   "montre les dépenses de transport"
 *   "convertis 500 dollars en euros"
 *   "exporte en pdf" / "exporte en excel"
 *   "filtre par hébergement"
 *   "réinitialise les filtres"
 */
public class VoiceCommandProcessor {

    // ── Types de commandes ────────────────────────────────────
    public enum CommandType {
        ADD_EXPENSE,        // ajouter une dépense
        CHECK_BUDGET,       // consulter le budget
        FILTER_EXPENSES,    // filtrer les dépenses
        CONVERT_CURRENCY,   // conversion devise
        EXPORT_PDF,         // export PDF
        EXPORT_EXCEL,       // export Excel
        RESET_FILTERS,      // réinitialiser filtres
        UNKNOWN             // commande non reconnue
    }

    // ── Résultat d'une commande ───────────────────────────────
    public static class VoiceCommand {
        public final CommandType type;
        public final String      rawText;
        public final double      amount;       // pour ADD_EXPENSE / CONVERT_CURRENCY
        public final String      category;     // pour ADD_EXPENSE / FILTER_EXPENSES
        public final String      label;        // libellé de la dépense
        public final String      fromCurrency; // pour CONVERT_CURRENCY
        public final String      toCurrency;   // pour CONVERT_CURRENCY

        public VoiceCommand(CommandType type, String rawText,
                            double amount, String category, String label,
                            String fromCurrency, String toCurrency) {
            this.type         = type;
            this.rawText      = rawText;
            this.amount       = amount;
            this.category     = category;
            this.label        = label;
            this.fromCurrency = fromCurrency;
            this.toCurrency   = toCurrency;
        }

        // Constructeur simplifié
        public VoiceCommand(CommandType type, String rawText) {
            this(type, rawText, 0, null, null, null, null);
        }
    }

    // ── Callback quand une commande est détectée ──────────────
    private Consumer<VoiceCommand> onCommand;

    public void setOnCommand(Consumer<VoiceCommand> callback) {
        this.onCommand = callback;
    }

    // ─────────────────────────────────────────────────────────
    //  Analyse principale
    // ─────────────────────────────────────────────────────────

    /**
     * Analyse un texte reconnu et retourne la commande correspondante.
     */
    public VoiceCommand process(String text) {
        if (text == null || text.isBlank()) return null;

        String t = text.toLowerCase().trim();
        System.out.println("[VoiceCommand] Analyse: " + t);

        VoiceCommand cmd;

        if (isAddExpenseCommand(t))       cmd = parseAddExpense(t);
        else if (isCheckBudgetCommand(t)) cmd = new VoiceCommand(CommandType.CHECK_BUDGET, text);
        else if (isExportPDFCommand(t))   cmd = new VoiceCommand(CommandType.EXPORT_PDF, text);
        else if (isExportExcelCommand(t)) cmd = new VoiceCommand(CommandType.EXPORT_EXCEL, text);
        else if (isConvertCommand(t))     cmd = parseConvert(t);
        else if (isFilterCommand(t))      cmd = parseFilter(t);
        else if (isResetCommand(t))       cmd = new VoiceCommand(CommandType.RESET_FILTERS, text);
        else                              cmd = new VoiceCommand(CommandType.UNKNOWN, text);

        System.out.println("[VoiceCommand] → " + cmd.type + " (montant=" + cmd.amount + ", catégorie=" + cmd.category + ")");

        if (onCommand != null) onCommand.accept(cmd);
        return cmd;
    }

    // ─────────────────────────────────────────────────────────
    //  Détection des commandes
    // ─────────────────────────────────────────────────────────

    private boolean isAddExpenseCommand(String t) {
        return t.contains("ajoute") || t.contains("ajouter") ||
                t.contains("nouvelle dépense") || t.contains("dépense de");
    }

    private boolean isCheckBudgetCommand(String t) {
        return t.contains("budget restant") || t.contains("combien reste") ||
                t.contains("solde") || t.contains("budget disponible") ||
                (t.contains("budget") && (t.contains("reste") || t.contains("restant")));
    }

    private boolean isExportPDFCommand(String t) {
        return (t.contains("exporte") || t.contains("exporter")) && t.contains("pdf");
    }

    private boolean isExportExcelCommand(String t) {
        return (t.contains("exporte") || t.contains("exporter")) &&
                (t.contains("excel") || t.contains("tableur"));
    }

    private boolean isConvertCommand(String t) {
        return t.contains("converti") || t.contains("convertis") ||
                t.contains("conversion") || t.contains("en euros") ||
                t.contains("en dollars") || t.contains("en livres");
    }

    private boolean isFilterCommand(String t) {
        return (t.contains("filtre") || t.contains("montrer") ||
                t.contains("affiche") || t.contains("montre")) &&
                (t.contains("transport") || t.contains("hébergement") ||
                        t.contains("restauration") || t.contains("activité") ||
                        t.contains("shopping"));
    }

    private boolean isResetCommand(String t) {
        return t.contains("réinitialise") || t.contains("efface les filtres") ||
                t.contains("tout afficher") || t.contains("supprimer les filtres");
    }

    // ─────────────────────────────────────────────────────────
    //  Parseurs détaillés
    // ─────────────────────────────────────────────────────────

    /**
     * "ajoute une dépense de 200 euros pour l'hôtel"
     * → amount=200, fromCurrency=EUR, category=Hébergement, label=hôtel
     */
    private VoiceCommand parseAddExpense(String t) {
        double amount   = extractAmount(t);
        String currency = extractCurrency(t);
        String category = extractCategory(t);
        String label    = extractLabel(t);

        return new VoiceCommand(CommandType.ADD_EXPENSE, t,
                amount, category, label, currency, null);
    }

    /**
     * "convertis 500 dollars en euros"
     * → amount=500, fromCurrency=USD, toCurrency=EUR
     */
    private VoiceCommand parseConvert(String t) {
        double amount   = extractAmount(t);
        String from     = extractSourceCurrency(t);
        String to       = extractTargetCurrency(t);

        return new VoiceCommand(CommandType.CONVERT_CURRENCY, t,
                amount, null, null, from, to);
    }

    /**
     * "filtre les dépenses de transport"
     * → category=Transport
     */
    private VoiceCommand parseFilter(String t) {
        String category = extractCategory(t);
        return new VoiceCommand(CommandType.FILTER_EXPENSES, t,
                0, category, null, null, null);
    }

    // ─────────────────────────────────────────────────────────
    //  Extraction d'entités
    // ─────────────────────────────────────────────────────────

    private double extractAmount(String t) {
        // Cherche un nombre dans le texte : "200", "200.50", "deux cents"
        String[] words = t.split("\\s+");
        for (String w : words) {
            try {
                return Double.parseDouble(w.replace(",", "."));
            } catch (NumberFormatException ignored) {}
        }
        // Nombres en lettres simples
        if (t.contains("cent"))     return 100;
        if (t.contains("cinquante")) return 50;
        if (t.contains("vingt"))    return 20;
        if (t.contains("dix"))      return 10;
        return 0;
    }

    private String extractCurrency(String t) {
        if (t.contains("euro"))    return "EUR";
        if (t.contains("dollar"))  return "USD";
        if (t.contains("livre"))   return "GBP";
        if (t.contains("franc"))   return "CHF";
        if (t.contains("dinar"))   return "TND";
        if (t.contains("dirham"))  return "MAD";
        if (t.contains("yen"))     return "JPY";
        return "EUR"; // défaut
    }

    private String extractSourceCurrency(String t) {
        // "convertis 500 DOLLARS en euros" → cherche avant "en"
        int enIdx = t.indexOf(" en ");
        String before = enIdx > 0 ? t.substring(0, enIdx) : t;
        return extractCurrency(before);
    }

    private String extractTargetCurrency(String t) {
        // "convertis 500 dollars en EUROS" → cherche après "en"
        int enIdx = t.indexOf(" en ");
        String after = enIdx > 0 ? t.substring(enIdx + 4) : t;
        return extractCurrency(after);
    }

    private String extractCategory(String t) {
        if (t.contains("hôtel") || t.contains("hotel") ||
                t.contains("hébergement") || t.contains("logement")) return "Hébergement";
        if (t.contains("transport") || t.contains("taxi") ||
                t.contains("avion") || t.contains("train") || t.contains("vol")) return "Transport";
        if (t.contains("restaurant") || t.contains("repas") ||
                t.contains("nourriture") || t.contains("restauration") || t.contains("manger")) return "Restauration";
        if (t.contains("activité") || t.contains("musée") ||
                t.contains("visite") || t.contains("excursion")) return "Activités";
        if (t.contains("shopping") || t.contains("achat") ||
                t.contains("souvenir") || t.contains("boutique")) return "Shopping";
        return "Autre";
    }

    private String extractLabel(String t) {
        // Cherche le mot-clé après "pour" : "dépense de 200 euros pour l'hôtel"
        int pourIdx = t.indexOf("pour ");
        if (pourIdx >= 0) {
            String after = t.substring(pourIdx + 5).replace("l'","").replace("le ","").replace("la ","").trim();
            if (!after.isBlank()) return capitalize(after);
        }
        // Sinon retourne la catégorie comme libellé
        return extractCategory(t);
    }

    private String capitalize(String s) {
        if (s == null || s.isBlank()) return s;
        return s.substring(0,1).toUpperCase() + s.substring(1);
    }
}