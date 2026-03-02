package Services;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class InflationCalculator {

    private final InflationService inflationService;

    // Mapping entre les pays (tels qu'utilisés dans l'application) et les codes de série FRED.
    // Pour une liste complète, voir https://fred.stlouisfed.org/release?rid=19
    private static final Map<String, String> COUNTRY_SERIES_MAP = Map.ofEntries(
            Map.entry("France", "CPALTT01FRM657N"),
            Map.entry("Italie", "CPALTT01ITM657N"),
            Map.entry("Japon", "CPALTT01JPM657N"),
            Map.entry("États-Unis", "CPIAUCSL"),
            Map.entry("USA", "CPIAUCSL"),
            Map.entry("Royaume-Uni", "CPALTT01GBM657N"),
            Map.entry("Allemagne", "CPALTT01DEM657N"),
            Map.entry("Espagne", "CPALTT01ESM657N"),
            Map.entry("Canada", "CPALTT01CAM657N"),
            Map.entry("Turquie", "CPALTT01TRM657N"),
            Map.entry("Indonésie", "CPALTT01IDM657N") // Pour Bali
    );

    public InflationCalculator() {
        this.inflationService = new InflationService();
    }

    /**
     * Calcule le montant équivalent dans une année cible en fonction de l'inflation.
     *
     * @param montantInitial Montant d'origine
     * @param pays           Nom du pays (doit correspondre à une clé dans COUNTRY_SERIES_MAP)
     * @param anneeDepart    Année du montant initial
     * @param anneeCible     Année pour laquelle on veut l'équivalent
     * @return CompletableFuture contenant le montant ajusté, ou le montant initial si indisponible
     */
    public CompletableFuture<Double> adjustForInflation(double montantInitial, String pays, int anneeDepart, int anneeCible) {
        String seriesId = COUNTRY_SERIES_MAP.get(pays);
        if (seriesId == null) {
            System.err.println("[Inflation] Pays non supporté : " + pays);
            return CompletableFuture.completedFuture(montantInitial);
        }

        return tryFetchWithFallback(montantInitial, seriesId, pays, anneeDepart, anneeCible, 0);
    }

    private CompletableFuture<Double> tryFetchWithFallback(double montantInitial, String seriesId, String pays,
                                                           int anneeDepart, int anneeCible, int attempt) {
        if (attempt > 5) { // limite à 5 ans de recul
            return CompletableFuture.completedFuture(montantInitial);
        }

        CompletableFuture<Double> cpiDepart = inflationService.getAnnualCPI(seriesId, anneeDepart - attempt);
        CompletableFuture<Double> cpiCible = inflationService.getAnnualCPI(seriesId, anneeCible);

        return cpiDepart.thenCombine(cpiCible, (cpiDep, cpiCib) -> {
            if (cpiDep > 0 && cpiCib > 0) {
                return montantInitial * (cpiCib / cpiDep);
            } else if (cpiDep == 0 && attempt < 5) {
                // récursion avec l'année précédente
                return tryFetchWithFallback(montantInitial, seriesId, pays, anneeDepart, anneeCible, attempt + 1).join();
            } else {
                System.err.println("[Inflation] IPC non disponible pour " + pays + " (" + (anneeDepart - attempt) + " ou " + anneeCible + ")");
                return montantInitial;
            }
        });
    }
}