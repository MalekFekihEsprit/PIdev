package Services;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class CulturalAdviceService {

    // Chargement de la clé depuis .env pour Cerebras
    private static final String CEREBRAS_API_KEY = EnvLoader.get("CEREBRAS_API_KEY");
    private static final String CEREBRAS_URL = "https://api.cerebras.ai/v1/chat/completions";
    private static final String DEFAULT_MODEL = "llama-3.3-70b"; // Modèle Cerebras

    private final OkHttpClient client;
    private Map<String, CulturalInfo> cache = new HashMap<>();

    // Gestion des limites de quota
    private static final int MAX_REQUESTS_PER_MINUTE = 50;
    private static final long MIN_TIME_BETWEEN_REQUESTS_MS = 1200; // 1.2 secondes
    private AtomicLong lastRequestTime = new AtomicLong(0);

    // Prompt système optimisé pour les conseils culturels
    private static final String SYSTEM_PROMPT =
            "Tu es un expert mondialement reconnu en étiquette culturelle et conseiller en voyage. " +
                    "Pour la destination suivante, fournis des conseils pratiques et précis.\n\n" +

                    "Format de réponse OBLIGATOIRE (respecte strictement cette structure):\n\n" +

                    "**SALUER**\n" +
                    "• [Comment saluer correctement: poignée de main, bise, inclinaison, contact visuel]\n" +
                    "• [Différences selon le genre ou l'âge si applicables]\n" +
                    "• [Mots ou expressions à utiliser]\n\n" +

                    "**S'HABILLER**\n" +
                    "• [Code vestimentaire général dans les lieux publics]\n" +
                    "• [Règles pour les lieux religieux (mosquées, églises, temples)]\n" +
                    "• [Ce qu'il faut absolument éviter de porter]\n" +
                    "• [Tenue recommandée pour les restaurants ou soirées]\n\n" +

                    "**TABOUS À ÉVITER**\n" +
                    "• [Gestes ou comportements considérés comme impolis]\n" +
                    "• [Sujets de conversation à ne pas aborder]\n" +
                    "• [Règles spécifiques importantes à connaître]\n\n" +

                    "**RÈGLES DE POLITESSE**\n" +
                    "• [Comment dire merci, s'il vous plaît, pardon]\n" +
                    "• [Règles à table (comment manger, pourboires)]\n" +
                    "• [Offrir des cadeaux ou accepter une invitation]\n" +
                    "• [Comportement dans les transports en commun]\n\n" +

                    "**CONSEILS SUPPLÉMENTAIRES**\n" +
                    "• [Informations utiles: jours fériés, horaires, particularités]\n" +
                    "• [Ce qu'il faut absolument savoir avant d'arriver]\n\n" +

                    "Règles importantes:\n" +
                    "- Réponds TOUJOURS en français\n" +
                    "- Utilise UNIQUEMENT des listes à puces (•)\n" +
                    "- Sois précis et concret, donne des exemples\n" +
                    "- Maximum 8 points par section\n" +
                    "- Si tu n'es pas sûr pour un point, ne l'inclus pas\n\n" +

                    "Destination: %s";

    public CulturalAdviceService() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .build();

        // Vérification au démarrage
        if (!isApiConfigured()) {
            System.out.println("⚠️⚠️⚠️ ATTENTION: Clé API Cerebras non configurée!");
            System.out.println("📝 Créez un fichier .env avec CEREBRAS_API_KEY=votre_cle");
        }
    }

    /**
     * Récupère les conseils culturels pour une destination
     * @param destination Le nom de la destination (ville ou pays)
     * @param country Le pays (optionnel, peut être null)
     * @return CulturalInfo contenant tous les conseils
     */
    public CulturalInfo getCulturalAdvice(String destination, String country) {
        // Créer une clé de cache unique
        String cacheKey = destination.toLowerCase().trim() +
                (country != null ? "_" + country.toLowerCase().trim() : "");

        // Vérifier le cache
        if (cache.containsKey(cacheKey)) {
            CulturalInfo cached = cache.get(cacheKey);
            System.out.println("✅ Utilisation du cache pour: " + destination +
                    (cached.isFromDefaultRules() ? " (règles par défaut)" : " (Cerebras)"));
            return cached;
        }

        // Déterminer la requête à envoyer (priorité au pays si fourni)
        String query = (country != null && !country.isEmpty()) ? country : destination;
        System.out.println("🔍 Recherche de conseils pour: " + query);

        // Commencer avec les règles par défaut (affichage immédiat)
        CulturalInfo info = getDefaultAdvice(destination, country);
        System.out.println("📋 AFFICHAGE INITIAL: Règles par défaut pour " + destination);

        // Essayer d'obtenir de meilleures données avec Cerebras en arrière-plan
        if (isApiConfigured()) {
            System.out.println("🔄 Tentative asynchrone de Cerebras pour " + query);
            tryToFetchFromCerebrasAsync(query, cacheKey);
        } else {
            System.out.println("⚠️ API Cerebras non configurée - Utilisation des règles par défaut uniquement");
        }

        // Mettre en cache et retourner les règles par défaut (sera mis à jour plus tard si Cerebras répond)
        cache.put(cacheKey, info);
        return info;
    }

    /**
     * Vérifie si l'API Cerebras est configurée
     */
    private boolean isApiConfigured() {
        return EnvLoader.hasValidKey("CEREBRAS_API_KEY");
    }

    /**
     * Tente de récupérer les données Cerebras en arrière-plan
     */
    private void tryToFetchFromCerebrasAsync(String query, String cacheKey) {
        new Thread(() -> {
            try {
                // Respecter le délai entre les requêtes pour éviter le rate limiting
                long now = System.currentTimeMillis();
                long last = lastRequestTime.get();
                if (now - last < MIN_TIME_BETWEEN_REQUESTS_MS) {
                    Thread.sleep(MIN_TIME_BETWEEN_REQUESTS_MS - (now - last));
                }

                System.out.println("⏳ Appel Cerebras en cours pour: " + query);
                CulturalInfo cerebrasInfo = fetchFromCerebras(query);

                if (cerebrasInfo != null && cerebrasInfo.hasContent()) {
                    lastRequestTime.set(System.currentTimeMillis());

                    // Mettre à jour le cache avec les vraies données
                    cache.put(cacheKey, cerebrasInfo);
                    System.out.println("✅✅ MISE À JOUR CACHE: Cerebras a répondu pour " + query);
                } else {
                    System.out.println("❌ Cerebras n'a pas retourné de contenu pour " + query);
                }
            } catch (Exception e) {
                System.out.println("❌ Erreur Cerebras asynchrone: " + e.getMessage());
            }
        }).start();
    }

    /**
     * Interroge l'API Cerebras
     */
    private CulturalInfo fetchFromCerebras(String query) throws Exception {
        String prompt = String.format(SYSTEM_PROMPT, query);

        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", DEFAULT_MODEL);
        requestBody.addProperty("temperature", 0.2);
        requestBody.addProperty("max_tokens", 1024);
        requestBody.addProperty("top_p", 0.8);

        // Construction des messages pour Cerebras (format OpenAI-compatible)
        JsonArray messages = new JsonArray();

        JsonObject systemMessage = new JsonObject();
        systemMessage.addProperty("role", "system");
        systemMessage.addProperty("content", "Tu es un expert en étiquette culturelle qui répond en français avec des listes à puces.");
        messages.add(systemMessage);

        JsonObject userMessage = new JsonObject();
        userMessage.addProperty("role", "user");
        userMessage.addProperty("content", prompt);
        messages.add(userMessage);

        requestBody.add("messages", messages);

        Request request = new Request.Builder()
                .url(CEREBRAS_URL)
                .header("Authorization", "Bearer " + CEREBRAS_API_KEY)
                .header("Content-Type", "application/json")
                .post(RequestBody.create(
                        requestBody.toString(),
                        MediaType.parse("application/json")
                ))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "";
                System.out.println("❌ Erreur Cerebras " + response.code() + ": " + errorBody);

                // Gestion spécifique des erreurs
                if (response.code() == 429) {
                    System.out.println("⚠️ Quota Cerebras dépassé. Réessayez plus tard.");
                }
                if (response.code() == 401 || response.code() == 403) {
                    System.out.println("🔑 Clé API Cerebras invalide ou non configurée!");
                }
                return null;
            }

            String jsonData = response.body().string();
            return parseCerebrasResponse(jsonData, query);
        }
    }

    /**
     * Parse la réponse de Cerebras
     */
    private CulturalInfo parseCerebrasResponse(String jsonData, String query) {
        CulturalInfo info = new CulturalInfo();
        info.setDestination(query);
        info.setFromDefaultRules(false);

        try {
            JsonObject json = JsonParser.parseString(jsonData).getAsJsonObject();

            if (json.has("choices") && json.getAsJsonArray("choices").size() > 0) {
                JsonObject choice = json.getAsJsonArray("choices").get(0).getAsJsonObject();

                if (choice.has("message")) {
                    JsonObject message = choice.getAsJsonObject("message");
                    if (message.has("content") && !message.get("content").isJsonNull()) {
                        String text = message.get("content").getAsString();
                        info.setRespectSection(text);
                        return info;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("❌ Erreur parsing Cerebras: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Règles par défaut (fallback si Cerebras ne répond pas)
     */
    private CulturalInfo getDefaultAdvice(String destination, String country) {
        CulturalInfo info = new CulturalInfo();
        info.setDestination(destination);
        info.setFromDefaultRules(true);

        String destLower = destination.toLowerCase();
        String countryLower = country != null ? country.toLowerCase() : "";

        // 🌍 TUNISIE ET PAYS DU MAGHREB
        if (destLower.contains("tunis") || destLower.contains("tunisie") ||
                countryLower.contains("tunis") || destLower.contains("djerba") ||
                destLower.contains("sousse") || destLower.contains("zaghouan") ||
                destLower.contains("maroc") || destLower.contains("algérie") ||
                destLower.contains("algerie") || destLower.contains("casablanca") ||
                destLower.contains("egypte") || destLower.contains("le caire")) {

            info.setRespectSection(
                    "**🇹🇳 BIENVENUE EN TUNISIE / MAGHREB**\n\n" +

                            "**SALUER**\n" +
                            "• Poignée de main ferme avec la main droite uniquement\n" +
                            "• Entre hommes, on peut se faire la bise (2 ou 3 bises)\n" +
                            "• Dire 'Salam Aleikom' (que la paix soit sur vous) est très apprécié\n" +
                            "• Pour les femmes, attendre qu'elles tendent la main en premier\n" +
                            "• Les hommes ne serrent généralement pas la main des femmes par respect religieux\n\n" +

                            "**S'HABILLER**\n" +
                            "• Vêtements modestes recommandés (couvrir épaules et genoux)\n" +
                            "• Pour les mosquées, femmes doivent couvrir leurs cheveux et porter des vêtements longs\n" +
                            "• Évitez les shorts trop courts, débardeurs et vêtements moulants dans les souks\n" +
                            "• Maillots de bain uniquement sur les plages privées ou piscines d'hôtel\n" +
                            "• En ville, optez pour des tenues décontractées mais couvrantes\n\n" +

                            "**TABOUS À ÉVITER**\n" +
                            "• Ne pas manger, boire ou fumer en public pendant le Ramadan (du lever au coucher du soleil)\n" +
                            "• Évitez de montrer la plante de vos pieds (considéré comme impoli)\n" +
                            "• N'utilisez jamais la main gauche pour manger, donner ou recevoir\n" +
                            "• Pas de démonstrations d'affection en public (embrasser, se toucher)\n" +
                            "• Ne photographiez pas les militaires, police ou bâtiments officiels\n" +
                            "• Évitez de critiquer le gouvernement ou la religion\n\n" +

                            "**RÈGLES DE POLITESSE**\n" +
                            "• Enlevez vos chaussures avant d'entrer dans une maison ou une mosquée\n" +
                            "• Refusez poliment 2-3 fois quand on vous offre quelque chose, puis acceptez\n" +
                            "• Le thé à la menthe se boit en trois fois, c'est un rituel d'hospitalité\n" +
                            "• Demandez toujours la permission avant de photographier quelqu'un\n" +
                            "• Les pourboires sont appréciés (10% au restaurant, 1-2 TND pour les services)\n" +
                            "• La négociation est normale dans les souks - faites-vous plaisir !\n\n" +

                            "**CONSEILS SUPPLÉMENTAIRES**\n" +
                            "• Le vendredi est un jour saint (certains magasins ferment le midi pour la prière)\n" +
                            "• Le français est largement parlé dans les villes, l'arabe est un plus\n" +
                            "• L'eau du robinet n'est pas toujours potable - préférez l'eau en bouteille\n" +
                            "• Les horaires peuvent être flexibles, c'est normal (concept du 'inchallah')\n" +
                            "• Évitez les tenues militaires ou camouflage\n" +
                            "• En été, il fait très chaud - prévoyez protection solaire et eau"
            );
        }

        // 🇦🇪 ÉMIRATS ARABES UNIS / DUBAÏ
        else if (destLower.contains("dubai") || destLower.contains("dubaï") ||
                destLower.contains("émirats") || countryLower.contains("émirats") ||
                destLower.contains("abou dabi") || destLower.contains("dubai")) {

            info.setRespectSection(
                    "**🇦🇪 BIENVENUE AUX ÉMIRATS ARABES UNIS**\n\n" +

                            "**SALUER**\n" +
                            "• Poignée de main légère, uniquement avec la main droite\n" +
                            "• Entre hommes, on peut se toucher le nez en signe de respect (rare pour les touristes)\n" +
                            "• Ne serrez pas la main d'une femme sauf si elle tend la main d'abord\n" +
                            "• Utilisez les titres 'Cheikh' pour les personnes âgées ou importantes\n" +
                            "• Dites 'As-salamu alaykum' pour saluer\n\n" +

                            "**S'HABILLER**\n" +
                            "• Tenue modeste obligatoire dans les lieux publics (couvrir épaules et genoux)\n" +
                            "• Évitez les vêtements transparents, trop courts ou trop moulants\n" +
                            "• Maillots de bain uniquement à la plage ou à la piscine de l'hôtel\n" +
                            "• Pour les mosquées (ex: Sheikh Zayed), femmes doivent couvrir cheveux, bras et jambes\n" +
                            "• Dans les centres commerciaux, évitez les tenues de plage\n" +
                            "• Pour les hommes, pas de torse nu en dehors des plages\n\n" +

                            "**TABOUS À ÉVITER**\n" +
                            "• Pas d'alcool en public ou en état d'ivresse dans la rue\n" +
                            "• Évitez les jurons et gestes obscènes (strictement interdits)\n" +
                            "• Pas de démonstrations d'affection en public (embrasser, caresses)\n" +
                            "• Ne photographiez pas les Émiratis sans permission explicite\n" +
                            "• Évitez de pointer du doigt les personnes\n" +
                            "• Ne pas critiquer la famille royale ou l'Islam\n" +
                            "• Attention pendant le Ramadan: pas de nourriture, boisson ou cigarette en public\n\n" +

                            "**RÈGLES DE POLITESSE**\n" +
                            "• Acceptez toujours le café arabe (gahwa) ou le thé offert - c'est un signe d'hospitalité\n" +
                            "• Utilisez uniquement la main droite pour manger et boire\n" +
                            "• Enlevez vos chaussures avant d'entrer dans une maison traditionnelle\n" +
                            "• Les vendredis sont sacrés (jour de prière) - matinée calme\n" +
                            "• Les pourboires sont attendus (10-15% au restaurant, 10-20 dirhams pour les services)\n" +
                            "• Demandez la permission avant de prendre des photos dans les souks\n\n" +

                            "**CONSEILS SUPPLÉMENTAIRES**\n" +
                            "• Le week-end est vendredi-samedi\n" +
                            "• Pendant le Ramadan, ne mangez pas en public avant le coucher du soleil\n" +
                            "• Les lois sont extrêmement strictes concernant les drogues (tolérance zéro)\n" +
                            "• L'application WhatsApp pour les appels est bloquée - utilisez FaceTime ou Skype\n" +
                            "• Le vendredi matin tout est fermé jusqu'à la prière (vers 13h)\n" +
                            "• L'habillement dans les hôtels est plus libéral qu'en ville\n" +
                            "• Le métro de Dubaï a des wagons réservés aux femmes"
            );
        }

        // 🇫🇷 FRANCE
        else if (destLower.contains("france") || destLower.contains("paris") ||
                destLower.contains("lyon") || destLower.contains("marseille") ||
                countryLower.contains("france")) {

            info.setRespectSection(
                    "**🇫🇷 BIENVENUE EN FRANCE**\n\n" +

                            "**SALUER**\n" +
                            "• Dites toujours 'Bonjour' avant d'entamer une conversation - c'est la règle d'or !\n" +
                            "• La bise (2 à 4 selon les régions) entre personnes qui se connaissent\n" +
                            "• Poignée de main pour les premières rencontres professionnelles\n" +
                            "• Utilisez 'vous' avec les inconnus, passez au 'tu' seulement quand on vous y invite\n" +
                            "• Au téléphone, dites 'Allô' et donnez votre prénom\n\n" +

                            "**S'HABILLER**\n" +
                            "• Tenue élégante et soignée dans les restaurants et lieux culturels\n" +
                            "• Évitez les vêtements de sport (survêtements, jogging) en ville\n" +
                            "• Pour les églises et cathédrales, tenue respectueuse (épaules couvertes)\n" +
                            "• Le noir est toujours chic et passe-partout\n" +
                            "• À Paris, on fait attention à son apparence - évitez les tenues trop décontractées\n" +
                            "• Les baskets sont acceptées mais préférez des chaussures élégantes pour le soir\n\n" +

                            "**TABOUS À ÉVITER**\n" +
                            "• Ne parlez pas trop fort dans les lieux publics (restaurants, transports)\n" +
                            "• Évitez les sujets controversés au premier rendez-vous (argent, religion, politique)\n" +
                            "• Ne coupez pas le pain avec un couteau à table - déchirez-le avec les mains\n" +
                            "• Ne mettez pas les coudes sur la table pendant le repas\n" +
                            "• Ne demandez pas le salaire des gens (très impoli)\n" +
                            "• Évitez de faire des compliments trop personnels\n\n" +

                            "**RÈGLES DE POLITESSE**\n" +
                            "• Dites 'merci', 's'il vous plaît', 'excusez-moi' systématiquement\n" +
                            "• Attendez que tout le monde soit servi avant de commencer à manger\n" +
                            "• Gardez vos mains visiblement posées sur la table (pas sur les genoux)\n" +
                            "• Offrez des fleurs, du vin ou des chocolats si invité chez quelqu'un\n" +
                            "• Le pain se pose directement sur la table, pas dans l'assiette\n" +
                            "• Finissez ce qu'il y a dans votre assiette (gâcher serait impoli)\n" +
                            "• Dans les magasins, dites 'bonjour' en entrant et 'au revoir' en sortant\n\n" +

                            "**CONSEILS SUPPLÉMENTAIRES**\n" +
                            "• Les magasins ferment souvent le dimanche, surtout en province\n" +
                            "• Le déjeuner est un repas important entre 12h et 14h\n" +
                            "• Les grèves peuvent perturber les transports - vérifiez avant de voyager\n" +
                            "• L'eau du robinet est potable partout\n" +
                            "• Faire la queue (faire la file) est obligatoire - ne passez pas devant\n" +
                            "• Dans les cafés, on paie souvent plus cher si on s'assoit en terrasse\n" +
                            "• Le pourboire n'est pas obligatoire mais apprécié (laisser la monnaie)"
            );
        }

        // 🇩🇪 ALLEMAGNE
        else if (destLower.contains("allemagne") || destLower.contains("berlin") ||
                destLower.contains("munich") || destLower.contains("francfort") ||
                countryLower.contains("allemagne")) {

            info.setRespectSection(
                    "**🇩🇪 BIENVENUE EN ALLEMAGNE**\n\n" +

                            "**SALUER**\n" +
                            "• Poignée de main ferme et franche avec contact visuel direct\n" +
                            "• Utilisez 'Herr' (Monsieur) et 'Frau' (Madame) suivi du nom de famille\n" +
                            "• La ponctualité est cruciale - ne soyez jamais en retard (5 min max)\n" +
                            "• Au téléphone, annoncez toujours votre nom d'abord\n" +
                            "• Entre collègues, on reste formel jusqu'à invitation contraire\n\n" +

                            "**S'HABILLER**\n" +
                            "• Tenue soignée et professionnelle, même pour le quotidien\n" +
                            "• Évitez les tenues trop décontractées ou négligées\n" +
                            "• Pour les sorties le soir, plutôt élégant\n" +
                            "• En entreprise, formel jusqu'à preuve du contraire\n" +
                            "• Les vêtements de marque sont appréciés mais discrets\n\n" +

                            "**TABOUS À ÉVITER**\n" +
                            "• Évitez les bruits forts le dimanche (Ruhetag - jour de repos sacré)\n" +
                            "• Pas de bruit entre 13h et 15h (Mittagsruhe - sieste)\n" +
                            "• Évitez de parler de la Seconde Guerre mondiale ou du nazisme\n" +
                            "• Ne regardez pas fixement les gens (considéré comme intrusif)\n" +
                            "• Ne faites pas de blagues sur les Allemands ou l'histoire\n" +
                            "• Évitez de jurer ou parler fort dans les transports\n\n" +

                            "**RÈGLES DE POLITESSE**\n" +
                            "• Soyez absolument à l'heure - le retard est une insulte\n" +
                            "• Triez vos déchets correctement (c'est très important et surveillé)\n" +
                            "• Respectez les pistes cyclables - les cyclistes ont la priorité\n" +
                            "• Traversez uniquement aux passages piétons et au feu vert\n" +
                            "• Quand on trinque (Prost!), regardez-vous dans les yeux\n" +
                            "• Dites 'Gesundheit' si quelqu'un éternue\n" +
                            "• Dans les restaurants, on dit 'Mahlzeit' avant de manger\n\n" +

                            "**CONSEILS SUPPLÉMENTAIRES**\n" +
                            "• Les magasins ferment tôt le samedi et sont fermés le dimanche\n" +
                            "• L'argent liquide (cash) est roi, surtout dans les petits commerces\n" +
                            "• Faites valider votre ticket dans les transports (contrôles fréquents)\n" +
                            "• La bière se boit à toute heure, même au déjeuner\n" +
                            "• Le pain allemand est varié et délicieux\n" +
                            "• L'eau du robinet est potable\n" +
                            "• Les pourboires se donnent en main (10% ou arrondir au supérieur)"
            );
        }

        // 🇯🇵 JAPON
        else if (destLower.contains("japon") || destLower.contains("tokyo") ||
                destLower.contains("kyoto") || destLower.contains("osaka") ||
                countryLower.contains("japon")) {

            info.setRespectSection(
                    "**🇯🇵 BIENVENUE AU JAPON**\n\n" +

                            "**SALUER**\n" +
                            "• Inclinez-vous (ojigi) pour saluer - plus la personne est importante, plus l'inclinaison est profonde\n" +
                            "• Pas de poignée de main traditionnelle, mais acceptez si un Japonais vous tend la main\n" +
                            "• Utilisez les suffixes honorifiques: -san (M./Mme), -sama (très respectueux)\n" +
                            "• Évitez de serrer la main trop fort, préférez une inclinaison légère\n" +
                            "• Ne faites pas de bruit en reniflant (moucher son nez en public est impoli)\n\n" +

                            "**S'HABILLER**\n" +
                            "• Tenue élégante et soignée en toutes circonstances\n" +
                            "• Pour les temples et sanctuaires, tenue modeste (épaules couvertes)\n" +
                            "• Évitez les vêtements trop décontractés ou négligés\n" +
                            "• Les chaussures s'enlèvent souvent - portez des chaussettes propres sans trous\n" +
                            "• Pas de décolleté ou tenues trop courtes\n\n" +

                            "**TABOUS À ÉVITER**\n" +
                            "• Ne donnez pas de pourboire - c'est considéré comme insultant\n" +
                            "• Évitez de manger en marchant dans la rue\n" +
                            "• Ne vous mouchez pas en public (très mal vu)\n" +
                            "• Ne montrez pas du doigt\n" +
                            "• Évitez de parler fort au téléphone dans les transports\n" +
                            "• Ne plantez pas vos baguettes verticalement dans le riz (comme un rituel funéraire)\n" +
                            "• Ne passez pas de nourriture de baguettes à baguettes (associé aux funérailles)\n\n" +

                            "**RÈGLES DE POLITESSE**\n" +
                            "• Dites 'Itadakimasu' avant de manger et 'Gochisosama' après\n" +
                            "• Enlevez vos chaussures avant d'entrer dans une maison, ryokan, ou certains restaurants\n" +
                            "• Slurpez vos nouilles - c'est un compliment pour le cuisinier\n" +
                            "• Utilisez des serviettes humides (oshibori) pour vous laver les mains avant de manger\n" +
                            "• Ramenez un petit cadeau (omiyage) si invité chez quelqu'un\n" +
                            "• Dans les onsens (sources chaudes), lavez-vous avant d'entrer dans l'eau\n\n" +

                            "**CONSEILS SUPPLÉMENTAIRES**\n" +
                            "• Le métro a des wagons réservés aux femmes aux heures de pointe\n" +
                            "• Il y a très peu de poubelles dans les rues - gardez vos déchets\n" +
                            "• Les cartes de visite (meishi) se donnent et se reçoivent à deux mains\n" +
                            "• L'alcool est socialement accepté, mais l'ivresse publique est mal vue\n" +
                            "• Les règles de tri des déchets sont très strictes\n" +
                            "• Apprenez quelques mots: 'Arigato' (merci), 'Sumimasen' (excusez-moi)"
            );
        }

        // 🇬🇧 ROYAUME-UNI / ANGLETERRE
        else if (destLower.contains("royaume-uni") || destLower.contains("londres") ||
                destLower.contains("angleterre") || destLower.contains("manchester") ||
                destLower.contains("uk") || destLower.contains("britain")) {

            info.setRespectSection(
                    "**🇬🇧 BIENVENUE AU ROYAUME-UNI**\n\n" +

                            "**SALUER**\n" +
                            "• Poignée de main ferme mais pas trop forte, avec contact visuel\n" +
                            "• Entre amis, on peut se faire la bise (une seule, en Écosse c'est moins courant)\n" +
                            "• Utilisez 'Mr', 'Mrs', 'Ms' + nom de famille au début\n" +
                            "• Dites 'Please', 'Thank you', 'Sorry' constamment - on ne dit jamais assez merci !\n" +
                            "• Les Britanniques adorent parler de la météo pour commencer une conversation\n\n" +

                            "**S'HABILLER**\n" +
                            "• Tenue élégante-décontractée selon le contexte\n" +
                            "• Pour les pubs, décontracté mais propre\n" +
                            "• Pour les occasions formelles, plutôt chic\n" +
                            "• Les Britanniques sont réputés pour leur style vestimentaire soigné\n" +
                            "• Prévoyez toujours un parapluie et une veste imperméable\n\n" +

                            "**TABOUS À ÉVITER**\n" +
                            "• Évitez de faire la queue (queue) - c'est sacré, ne passez jamais devant\n" +
                            "• Ne parlez pas de politique ou de la monarchie avec des inconnus\n" +
                            "• Évitez de demander combien gagnent les gens\n" +
                            "• Ne critiquez pas le thé britannique !\n" +
                            "• Évitez les comparaisons avec d'autres pays\n\n" +

                            "**RÈGLES DE POLITESSE**\n" +
                            "• Faites la queue patiemment - c'est très important\n" +
                            "• Dites 'sorry' même si ce n'est pas votre faute\n" +
                            "• Le thé se boit à toute heure - acceptez si on vous en offre\n" +
                            "• Tenez la porte à la personne derrière vous\n" +
                            "• Dans les pubs, commandez au bar (pas de service à table)\n" +
                            "• L'humour britannique est souvent ironique - ne le prenez pas mal\n\n" +

                            "**CONSEILS SUPPLÉMENTAIRES**\n" +
                            "• Conduisez à gauche si vous louez une voiture\n" +
                            "• Les pourboires sont inclus dans l'addition dans les pubs\n" +
                            "• Au restaurant, 10-12% de pourboire si service non inclus\n" +
                            "• La météo change rapidement - soyez prêt à tout\n" +
                            "• L'eau du robinet est potable\n" +
                            "• Les prises électriques sont différentes (adaptateur nécessaire)"
            );
        }

        // 🇪🇸 ESPAGNE
        else if (destLower.contains("espagne") || destLower.contains("barcelone") ||
                destLower.contains("madrid") || destLower.contains("valence") ||
                destLower.contains("sevilla") || countryLower.contains("espagne")) {

            info.setRespectSection(
                    "**🇪🇸 BIENVENUE EN ESPAGNE**\n\n" +

                            "**SALUER**\n" +
                            "• Deux bises (droite puis gauche) entre personnes qui se connaissent\n" +
                            "• Poignée de main pour les rencontres professionnelles\n" +
                            "• Les Espagnols sont chaleureux et tactiles\n" +
                            "• Dites 'Hola' pour bonjour, 'Adiós' pour au revoir\n" +
                            "• Utilisez 'tú' (informel) assez rapidement\n\n" +

                            "**S'HABILLER**\n" +
                            "• Tenue élégante pour sortir le soir\n" +
                            "• Décontracté mais soigné en journée\n" +
                            "• Pour les églises, tenue respectueuse (épaules couvertes)\n" +
                            "• À la plage, maillots acceptés mais pas dans la rue\n" +
                            "• Les Espagnols sont élégants, surtout dans les grandes villes\n\n" +

                            "**TABOUS À ÉVITER**\n" +
                            "• Ne parlez pas de la corrida ou du séparatisme catalan\n" +
                            "• Évitez de manger à des heures 'françaises' - le dîner est tard (21h-22h)\n" +
                            "• Ne vous attendez pas à ce que tout soit ouvert pendant la sieste\n" +
                            "• Évitez de critiquer la nourriture locale\n" +
                            "• Ne confondez pas les Catalans avec les Espagnols à Barcelone\n\n" +

                            "**RÈGLES DE POLITESSE**\n" +
                            "• La vie sociale est importante - acceptez les invitations\n" +
                            "• Partager des tapas est normal\n" +
                            "• Dites 'por favor' et 'gracias'\n" +
                            "• Les horaires de repas sont très différents: déjeuner 14h, dîner 22h\n" +
                            "• La sieste l'après-midi est encore pratiquée\n\n" +

                            "**CONSEILS SUPPLÉMENTAIRES**\n" +
                            "• La vie nocturne commence tard (22h-23h)\n" +
                            "• Les pourboires sont modestes (laisser la monnaie)\n" +
                            "• Attention aux pickpockets dans les grandes villes\n" +
                            "• La chaleur peut être intense en été - hydratez-vous\n" +
                            "• Apprenez quelques mots d'espagnol, très appréciés"
            );
        }

        // 🇮🇹 ITALIE
        else if (destLower.contains("italie") || destLower.contains("rome") ||
                destLower.contains("milan") || destLower.contains("venise") ||
                destLower.contains("florence") || destLower.contains("naples") ||
                countryLower.contains("italie")) {

            info.setRespectSection(
                    "**🇮🇹 BIENVENUE EN ITALIE**\n\n" +

                            "**SALUER**\n" +
                            "• Deux bises (gauche puis droite) entre amis\n" +
                            "• Poignée de main chaleureuse pour les rencontres\n" +
                            "• Utilisez 'Buongiorno' le matin, 'Buonasera' le soir\n" +
                            "• Les Italiens sont expressifs et utilisent leurs mains en parlant\n" +
                            "• Attendez qu'on vous propose le 'tu' avant de l'utiliser\n\n" +

                            "**S'HABILLER**\n" +
                            "• Tenue élégante, les Italiens sont réputés pour leur style\n" +
                            "• Pour les églises et le Vatican, tenue très respectueuse (épaules et genoux couverts)\n" +
                            "• Évitez les shorts dans les centres-villes historiques\n" +
                            "• Prévoyez des vêtements sophistiqués pour le soir\n" +
                            "• Les chaussures sont importantes dans le look italien\n\n" +

                            "**TABOUS À ÉVITER**\n" +
                            "• Ne commandez pas de cappuccino après 11h du matin (réservé au petit-déjeuner)\n" +
                            "• Évitez de toucher les monuments historiques\n" +
                            "• Ne vous asseyez pas par terre dans les lieux publics\n" +
                            "• Évitez de critiquer la cuisine italienne ou les pizzas\n" +
                            "• Ne demandez pas de parmesan sur les pizzas ou fruits de mer\n\n" +

                            "**RÈGLES DE POLITESSE**\n" +
                            "• Attendez qu'on vous invite avant de vous asseoir au café\n" +
                            "• Le pain ne se mange pas avec de l'huile d'olive en entrée (c'est pour la fin)\n" +
                            "• Les pâtes sont un premier plat, pas un accompagnement\n" +
                            "• Dites 'per favore' et 'grazie'\n" +
                            "• À table, gardez vos mains visibles mais pas les coudes\n" +
                            "• Offrez des fleurs ou des chocolats si invité chez quelqu'un\n\n" +

                            "**CONSEILS SUPPLÉMENTAIRES**\n" +
                            "• La 'passeggiata' est une promenade du soir importante\n" +
                            "• Attention aux pickpockets dans les zones très touristiques\n" +
                            "• Vérifiez les horaires d'ouverture des musées qui varient\n" +
                            "• Les magasins ferment souvent pour la pause déjeuner\n" +
                            "• Le café se boit au comptoir, moins cher qu'à table\n" +
                            "• Le pourboire n'est pas obligatoire mais apprécié"
            );
        }

        // 🌏 RÈGLE PAR DÉFAUT POUR TOUTES LES AUTRES DESTINATIONS
        else {
            info.setRespectSection(
                    "**🌍 BIENVENUE À " + destination.toUpperCase() + "**\n\n" +

                            "**CONSEILS GÉNÉRAUX**\n" +
                            "• Renseignez-vous sur les coutumes locales avant votre voyage\n" +
                            "• Observez comment les locaux se comportent et adaptez-vous\n" +
                            "• Souriez et soyez respectueux, ça ouvre toutes les portes\n\n" +

                            "**SALUER**\n" +
                            "• Renseignez-vous sur la forme de salutation locale\n" +
                            "• Dans le doute, une inclinaison légère ou un sourire suffit\n" +
                            "• Apprenez à dire 'bonjour' et 'merci' dans la langue locale\n\n" +

                            "**S'HABILLER**\n" +
                            "• Habillez-vous modestement, surtout dans les lieux religieux\n" +
                            "• Prévoyez de quoi couvrir vos épaules et genoux\n" +
                            "• Évitez les tenues trop décontractées dans les lieux formels\n\n" +

                            "**TABOUS À ÉVITER**\n" +
                            "• Évitez de critiquer la culture ou les traditions locales\n" +
                            "• Ne photographiez pas sans demander la permission\n" +
                            "• Évitez les sujets politiques sensibles\n" +
                            "• Ne montrez pas d'affection en public si ce n'est pas la coutume\n\n" +

                            "**RÈGLES DE POLITESSE**\n" +
                            "• Apprenez quelques mots de base (bonjour, merci, s'il vous plaît)\n" +
                            "• Demandez toujours la permission avant de photographier quelqu'un\n" +
                            "• Respectez les files d'attente\n" +
                            "• Soyez patient, ouvert d'esprit et curieux\n\n" +

                            "**CONSEILS PRATIQUES**\n" +
                            "• Consultez un guide de voyage récent pour plus de détails\n" +
                            "• Demandez conseil à la réception de votre hôtel\n" +
                            "• Faites preuve de bon sens et de respect\n" +
                            "• L'eau du robinet est-elle potable? Renseignez-vous\n" +
                            "• Quels sont les numéros d'urgence locaux?\n\n" +

                            "**⚠️ NOTE**\n" +
                            "• Ces conseils sont génériques - pour des informations spécifiques à " + destination + ", " +
                            "activez l'API Cerebras avec une clé valide ou consultez un guide spécialisé."
            );
        }

        return info;
    }

    /**
     * Classe pour stocker les informations culturelles
     */
    public static class CulturalInfo {
        private String destination;
        private String respectSection;
        private boolean fromDefaultRules = true;

        public String getDestination() { return destination; }
        public void setDestination(String destination) { this.destination = destination; }

        public String getRespectSection() { return respectSection; }
        public void setRespectSection(String respectSection) { this.respectSection = respectSection; }

        public boolean isFromDefaultRules() { return fromDefaultRules; }
        public void setFromDefaultRules(boolean fromDefaultRules) { this.fromDefaultRules = fromDefaultRules; }

        public boolean hasContent() {
            return respectSection != null && !respectSection.isEmpty();
        }

        public String getFormattedAdvice() {
            return respectSection != null ? respectSection : "Aucun conseil disponible pour cette destination.";
        }
    }
}