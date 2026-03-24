package Utils;

import io.github.cdimascio.dotenv.Dotenv;

public class ConfigV {

    // ==================== CHARGEMENT DES VARIABLES D'ENVIRONNEMENT ====================
    public static final Dotenv dotenv = Dotenv.configure()
            .ignoreIfMissing()
            .load();

    private static String getEnvOrDefault(String key, String defaultValue) {
        // Priorité à la variable d'environnement système
        String envValue = System.getenv(key);
        if (envValue != null && !envValue.isEmpty()) {
            return envValue;
        }

        // Ensuite au fichier .env
        String dotenvValue = dotenv.get(key);
        if (dotenvValue != null && !dotenvValue.isEmpty()) {
            return dotenvValue;
        }

        // Sinon valeur par défaut
        return defaultValue;
    }

    // ==================== PAYPAL CONFIGURATION ====================
    public static final String PAYPAL_CLIENT_ID = getEnvOrDefault("PAYPAL_CLIENT_ID", "");
    public static final String PAYPAL_CLIENT_SECRET = getEnvOrDefault("PAYPAL_CLIENT_SECRET", "");
    public static final String PAYPAL_MODE = getEnvOrDefault("PAYPAL_MODE", "sandbox");

    // ==================== STRIPE CONFIGURATION ====================
    public static final String STRIPE_SECRET_KEY = getEnvOrDefault("STRIPE_SECRET_KEY", "");
    public static final String STRIPE_PUBLISHABLE_KEY = getEnvOrDefault("STRIPE_PUBLISHABLE_KEY", "");

    // ==================== EMAIL SMTP CONFIGURATION ====================
    public static final String SMTP_HOST = getEnvOrDefault("SMTP_HOST", "smtp.gmail.com");
    public static final String SMTP_PORT = getEnvOrDefault("SMTP_PORT", "587");
    public static final String SMTP_EMAIL = getEnvOrDefault("SMTP_EMAIL", "");
    public static final String SMTP_PASSWORD = getEnvOrDefault("SMTP_PASSWORD", "");
    public static final String SMTP_FROM_NAME = getEnvOrDefault("SMTP_FROM_NAME", "TravelMate");

    // ==================== UNSPLASH CONFIGURATION ====================
    public static final String UNSPLASH_ACCESS_KEY = getEnvOrDefault("UNSPLASH_ACCESS_KEY", "");

    // ==================== PARAMÈTRES GÉNÉRAUX ====================
    public static final String DEFAULT_CURRENCY = getEnvOrDefault("DEFAULT_CURRENCY", "EUR");
    public static final String BASE_URL = getEnvOrDefault("BASE_URL", "http://localhost:8080");
    public static final String SUCCESS_URL = BASE_URL + "/payment/success";
    public static final String CANCEL_URL = BASE_URL + "/payment/cancel";

    // ==================== MÉTHODES DE VÉRIFICATION PAYPAL ====================
    public static boolean arePayPalKeysConfigured() {
        return !PAYPAL_CLIENT_ID.isEmpty() && !PAYPAL_CLIENT_SECRET.isEmpty();
    }

    public static boolean isPayPalSandbox() {
        return "sandbox".equalsIgnoreCase(PAYPAL_MODE);
    }

    public static boolean isPayPalLive() {
        return "live".equalsIgnoreCase(PAYPAL_MODE);
    }

    public static String getPayPalMode() {
        if (!arePayPalKeysConfigured()) return "non configuré";
        return PAYPAL_MODE;
    }

    // ==================== MÉTHODES DE VÉRIFICATION STRIPE ====================
    public static boolean areStripeKeysConfigured() {
        return !STRIPE_SECRET_KEY.isEmpty() && !STRIPE_PUBLISHABLE_KEY.isEmpty();
    }

    public static boolean isStripeInTestMode() {
        return STRIPE_SECRET_KEY.startsWith("sk_test_") && STRIPE_PUBLISHABLE_KEY.startsWith("pk_test_");
    }

    public static boolean isStripeInLiveMode() {
        return STRIPE_SECRET_KEY.startsWith("sk_live_") && STRIPE_PUBLISHABLE_KEY.startsWith("pk_live_");
    }

    public static String getStripeMode() {
        if (!areStripeKeysConfigured()) return "non configuré";
        if (isStripeInTestMode()) return "test";
        if (isStripeInLiveMode()) return "live";
        return "mode inconnu";
    }

    // ==================== MÉTHODES DE VÉRIFICATION EMAIL ====================
    public static boolean isEmailConfigured() {
        return !SMTP_EMAIL.isEmpty() && !SMTP_PASSWORD.isEmpty();
    }

    // ==================== MÉTHODES DE VÉRIFICATION UNSPLASH ====================
    public static boolean isUnsplashConfigured() {
        return UNSPLASH_ACCESS_KEY != null && !UNSPLASH_ACCESS_KEY.isEmpty();
    }

    // ==================== MÉTHODES UTILITAIRES ====================
    public static boolean isAnyPaymentConfigured() {
        return arePayPalKeysConfigured() || areStripeKeysConfigured();
    }

    public static String[] getAvailablePaymentMethods() {
        if (arePayPalKeysConfigured() && areStripeKeysConfigured()) {
            return new String[]{"PAYPAL", "STRIPE"};
        } else if (arePayPalKeysConfigured()) {
            return new String[]{"PAYPAL"};
        } else if (areStripeKeysConfigured()) {
            return new String[]{"STRIPE"};
        } else {
            return new String[]{};
        }
    }

    public static String maskKey(String key) {
        if (key == null || key.length() < 10) return "***";
        return key.substring(0, 8) + "..." + key.substring(key.length() - 4);
    }

    public static void printConfigSummary() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("           CONFIGURATION DES SERVICES TRAVELMATE");
        System.out.println("=".repeat(60));

        System.out.println("\n📌 PAYPAL:");
        System.out.println("   Mode: " + getPayPalMode());
        System.out.println("   Client ID: " + (arePayPalKeysConfigured() ? maskKey(PAYPAL_CLIENT_ID) : "❌ Non configuré"));

        System.out.println("\n📌 STRIPE:");
        System.out.println("   Mode: " + getStripeMode());
        System.out.println("   Secret Key: " + (areStripeKeysConfigured() ? maskKey(STRIPE_SECRET_KEY) : "❌ Non configuré"));

        System.out.println("\n📌 EMAIL SMTP:");
        System.out.println("   Hôte: " + SMTP_HOST + ":" + SMTP_PORT);
        System.out.println("   Email: " + (isEmailConfigured() ? maskKey(SMTP_EMAIL) : "❌ Non configuré"));
        System.out.println("   Expéditeur: " + SMTP_FROM_NAME);

        System.out.println("\n📌 PARAMÈTRES GÉNÉRAUX:");
        System.out.println("   Devise par défaut: " + DEFAULT_CURRENCY);
        System.out.println("   Méthodes disponibles: " + String.join(", ", getAvailablePaymentMethods()));

        System.out.println("\n" + "=".repeat(60) + "\n");
    }
}