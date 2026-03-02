package Services;

import javafx.stage.Stage;

/**
 * Services/Budgetnotificationservice.java — version mise à jour
 * Envoie à la fois un toast JavaFX ET une notification mobile Ntfy.
 */
public class Budgetnotificationservice {

    private static final double WARN_THRESHOLD = 80.0;
    private static final double CRIT_THRESHOLD = 100.0;

    private final NtfyApi ntfyApi;
    private double lastNotifiedPct = 0.0;

    public Budgetnotificationservice(String topic) {
        this.ntfyApi = new NtfyApi(topic);
    }

    /** Appelée depuis updateKPI() avec toast + ntfy */
    public void checkBudgetWithToast(double totalSpent, double totalBudget,
                                     String budgetName, Stage stage) {
        if (totalBudget <= 0) return;

        double pct = (totalSpent / totalBudget) * 100.0;

        // Réinitialise si dépenses baissent sous 80%
        if (pct < WARN_THRESHOLD && lastNotifiedPct >= WARN_THRESHOLD) {
            lastNotifiedPct = 0.0;
        }

        if (pct >= CRIT_THRESHOLD && lastNotifiedPct < CRIT_THRESHOLD) {
            String title   = "Budget Depasse !";
            String message = String.format(
                    "Vous avez depense %.2f sur %.2f (%.1f%%) pour \"%s\"",
                    totalSpent, totalBudget, pct, budgetName);

            // Toast JavaFX
            if (stage != null)
                ToastNotification.showDanger(stage, "🚨 " + title, message);

            // Notification mobile
            ntfyApi.sendCritical(title, message);
            lastNotifiedPct = pct;

        } else if (pct >= WARN_THRESHOLD && lastNotifiedPct < WARN_THRESHOLD) {
            String title   = "Alerte 80% Budget";
            String message = String.format(
                    "Vous avez utilise %.1f%% de \"%s\". Depense : %.2f / %.2f",
                    pct, budgetName, totalSpent, totalBudget);

            // Toast JavaFX
            if (stage != null)
                ToastNotification.showWarning(stage, "⚠️ " + title, message);

            // Notification mobile
            ntfyApi.sendWarning(title, message);
            lastNotifiedPct = pct;
        }
    }

    /** Version sans toast (compatibilité ancienne) */
    public void checkBudget(double totalSpent, double totalBudget) {
        checkBudgetWithToast(totalSpent, totalBudget, "Budget", null);
    }

    public void resetForNewPeriod() {
        lastNotifiedPct = 0.0;
        System.out.println("[BudgetNotificationService] Alertes reinitialisees.");
    }
}