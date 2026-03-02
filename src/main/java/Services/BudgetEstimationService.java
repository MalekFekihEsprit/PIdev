package Services;

import java.util.concurrent.CompletableFuture;

/**
 * Service d'estimation de budget basé sur destination, durée et saison.
 */
public class BudgetEstimationService {

    /**
     * Calcule une estimation de budget selon la destination, la durée et la saison.
     *
     * @param destination Nom de la destination (ex: "Paris")
     * @param dureeJours  Nombre de jours
     * @param saison      Saison idéale (été, hiver, printemps, automne) ou null
     * @return Estimation en double (arrondie à 2 décimales)
     */
    public double estimateBudget(String destination, int dureeJours, String saison) {
        double coutJournalier = getCoutJournalier(destination);
        double coeffSaison = getCoefficientSaison(saison);
        double transport = getCoutTransport(destination);
        double total = (coutJournalier * dureeJours * coeffSaison) + transport;
        total = total * (0.9 + Math.random() * 0.2); // variation aléatoire
        return Math.round(total * 100) / 100.0;
    }

    public CompletableFuture<Double> estimateBudgetAsync(String destination, int dureeJours, String saison) {
        return CompletableFuture.supplyAsync(() -> estimateBudget(destination, dureeJours, saison));
    }

    private double getCoutJournalier(String destination) {
        if (destination == null) return 100;
        switch (destination.toLowerCase()) {
            case "paris": return 120;
            case "marrakech": return 80;
            case "tokyo": return 150;
            case "rome": return 110;
            case "istanbul": return 90;
            case "bali": return 100;
            default: return 100;
        }
    }

    private double getCoefficientSaison(String saison) {
        if (saison == null) return 1.0;
        switch (saison.toLowerCase()) {
            case "été": case "hiver": return 1.3;
            case "printemps": case "automne": return 1.1;
            default: return 1.0;
        }
    }

    private double getCoutTransport(String destination) {
        if (destination == null) return 300;
        switch (destination.toLowerCase()) {
            case "paris": return 200;
            case "marrakech": return 250;
            case "tokyo": return 800;
            case "rome": return 220;
            case "istanbul": return 280;
            case "bali": return 900;
            default: return 300;
        }
    }
}