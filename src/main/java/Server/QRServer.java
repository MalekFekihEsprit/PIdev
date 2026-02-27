package Server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import Utils.DeepLinkHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.BindException;

public class QRServer {

    private static HttpServer server;
    private static int currentPort = 8080;
    private static final int MAX_PORT_ATTEMPTS = 10;

    /**
     * Démarre le serveur HTTP avec recherche de port automatique
     */
    public static void startServer() {
        for (int attempt = 0; attempt < MAX_PORT_ATTEMPTS; attempt++) {
            int portToTry = 8080 + attempt;
            try {
                server = HttpServer.create(new InetSocketAddress(portToTry), 0);
                server.createContext("/activity", new ActivityHandler());
                server.setExecutor(null);
                server.start();

                currentPort = portToTry;
                System.out.println("==========================================");
                System.out.println("🌐 Serveur QR DÉMARRÉ sur le port " + currentPort);
                System.out.println("📱 Accessible depuis votre téléphone à:");
                System.out.println("   http://" + getLocalIp() + ":" + currentPort + "/activity?id=XXX");
                System.out.println("==========================================");
                return; // Serveur démarré avec succès

            } catch (BindException e) {
                System.out.println("⚠️ Port " + portToTry + " déjà utilisé, essai du port " + (portToTry + 1) + "...");
            } catch (IOException e) {
                System.err.println("❌ Erreur démarrage serveur: " + e.getMessage());
                break;
            }
        }

        System.err.println("❌ Impossible de démarrer le serveur après " + MAX_PORT_ATTEMPTS + " tentatives");
    }

    private static String getLocalIp() {
        try {
            return java.net.InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return "192.168.1.9";
        }
    }

    /**
     * Arrête le serveur
     */
    public static void stopServer() {
        if (server != null) {
            server.stop(0);
            System.out.println("🛑 Serveur QR arrêté (port " + currentPort + ")");
        }
    }

    /**
     * Retourne le port actuel
     */
    public static int getCurrentPort() {
        return currentPort;
    }

    static class ActivityHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            String clientIp = exchange.getRemoteAddress().getAddress().getHostAddress();

            System.out.println("📱 Requête reçue de: " + clientIp);

            String response;
            int statusCode;

            if (query != null && query.startsWith("id=")) {
                String activityId = query.substring(3);

                // Page HTML responsive
                response = "<!DOCTYPE html>\n" +
                        "<html>\n" +
                        "<head>\n" +
                        "    <meta charset='UTF-8'>\n" +
                        "    <meta name='viewport' content='width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no'>\n" +
                        "    <title>TravelMate - Activité #" + activityId + "</title>\n" +
                        "    <style>\n" +
                        "        * { margin: 0; padding: 0; box-sizing: border-box; }\n" +
                        "        body { \n" +
                        "            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;\n" +
                        "            background: #f0f4f0; \n" +
                        "            min-height: 100vh;\n" +
                        "            display: flex;\n" +
                        "            align-items: center;\n" +
                        "            justify-content: center;\n" +
                        "            padding: 16px;\n" +
                        "        }\n" +
                        "        .card { \n" +
                        "            background: white; \n" +
                        "            border-radius: 28px; \n" +
                        "            padding: 32px 24px; \n" +
                        "            max-width: 400px; \n" +
                        "            width: 100%;\n" +
                        "            box-shadow: 0 20px 40px rgba(0,0,0,0.1);\n" +
                        "            text-align: center;\n" +
                        "        }\n" +
                        "        .icon { font-size: 64px; margin-bottom: 16px; }\n" +
                        "        h1 { \n" +
                        "            color: #1a1a1a; \n" +
                        "            font-size: 28px;\n" +
                        "            font-weight: 700;\n" +
                        "            margin-bottom: 8px;\n" +
                        "        }\n" +
                        "        .subtitle {\n" +
                        "            color: #666;\n" +
                        "            font-size: 16px;\n" +
                        "            margin-bottom: 24px;\n" +
                        "        }\n" +
                        "        .id-badge { \n" +
                        "            background: #fff3e8;\n" +
                        "            padding: 16px;\n" +
                        "            border-radius: 16px;\n" +
                        "            margin: 24px 0;\n" +
                        "        }\n" +
                        "        .id-label {\n" +
                        "            color: #ff6b00;\n" +
                        "            font-size: 14px;\n" +
                        "            font-weight: 600;\n" +
                        "            text-transform: uppercase;\n" +
                        "            letter-spacing: 1px;\n" +
                        "            margin-bottom: 4px;\n" +
                        "        }\n" +
                        "        .id-number { \n" +
                        "            font-size: 48px;\n" +
                        "            font-weight: 800;\n" +
                        "            color: #ff6b00;\n" +
                        "            line-height: 1.2;\n" +
                        "        }\n" +
                        "        .button { \n" +
                        "            background: #ff6b00; \n" +
                        "            color: white; \n" +
                        "            padding: 20px 32px; \n" +
                        "            border-radius: 100px; \n" +
                        "            text-decoration: none; \n" +
                        "            display: block; \n" +
                        "            margin: 24px 0; \n" +
                        "            font-weight: 700;\n" +
                        "            font-size: 18px;\n" +
                        "            border: none;\n" +
                        "            width: 100%;\n" +
                        "            box-shadow: 0 8px 20px rgba(255,107,0,0.3);\n" +
                        "            transition: transform 0.2s;\n" +
                        "        }\n" +
                        "        .button:active {\n" +
                        "            transform: scale(0.98);\n" +
                        "        }\n" +
                        "        .info-box { \n" +
                        "            background: #f8f9fa;\n" +
                        "            border-radius: 16px;\n" +
                        "            padding: 16px;\n" +
                        "            margin-top: 24px;\n" +
                        "            text-align: left;\n" +
                        "        }\n" +
                        "        .info-title {\n" +
                        "            color: #333;\n" +
                        "            font-weight: 600;\n" +
                        "            margin-bottom: 12px;\n" +
                        "            font-size: 14px;\n" +
                        "        }\n" +
                        "        .step {\n" +
                        "            display: flex;\n" +
                        "            align-items: center;\n" +
                        "            gap: 12px;\n" +
                        "            margin-bottom: 12px;\n" +
                        "            color: #555;\n" +
                        "            font-size: 14px;\n" +
                        "        }\n" +
                        "        .step-number {\n" +
                        "            background: #ff6b00;\n" +
                        "            color: white;\n" +
                        "            width: 24px;\n" +
                        "            height: 24px;\n" +
                        "            border-radius: 12px;\n" +
                        "            display: flex;\n" +
                        "            align-items: center;\n" +
                        "            justify-content: center;\n" +
                        "            font-weight: 700;\n" +
                        "            font-size: 12px;\n" +
                        "            flex-shrink: 0;\n" +
                        "        }\n" +
                        "        .note {\n" +
                        "            color: #999;\n" +
                        "            font-size: 12px;\n" +
                        "            margin-top: 16px;\n" +
                        "            padding-top: 16px;\n" +
                        "            border-top: 1px solid #eee;\n" +
                        "        }\n" +
                        "    </style>\n" +
                        "    <script>\n" +
                        "        // Tentative d'ouverture automatique de l'application\n" +
                        "        window.onload = function() {\n" +
                        "            setTimeout(function() {\n" +
                        "                window.location.href = 'travelmate://activity?id=" + activityId + "';\n" +
                        "            }, 500);\n" +
                        "        }\n" +
                        "    </script>\n" +
                        "</head>\n" +
                        "<body>\n" +
                        "    <div class='card'>\n" +
                        "        <div class='icon'>🎯</div>\n" +
                        "        <h1>TravelMate</h1>\n" +
                        "        <div class='subtitle'>QR code scanné avec succès</div>\n" +
                        "        \n" +
                        "        <div class='id-badge'>\n" +
                        "            <div class='id-label'>Activité n°</div>\n" +
                        "            <div class='id-number'>" + activityId + "</div>\n" +
                        "        </div>\n" +
                        "        \n" +
                        "        <a href='travelmate://activity?id=" + activityId + "' class='button'>\n" +
                        "            📱 OUVRIR L'APPLICATION\n" +
                        "        </a>\n" +
                        "        \n" +
                        "        <div class='info-box'>\n" +
                        "            <div class='info-title'>📋 Comment ça marche ?</div>\n" +
                        "            <div class='step'>\n" +
                        "                <span class='step-number'>1</span>\n" +
                        "                <span>Cliquez sur le bouton orange</span>\n" +
                        "            </div>\n" +
                        "            <div class='step'>\n" +
                        "                <span class='step-number'>2</span>\n" +
                        "                <span>L'application TravelMate s'ouvre</span>\n" +
                        "            </div>\n" +
                        "            <div class='step'>\n" +
                        "                <span class='step-number'>3</span>\n" +
                        "                <span>Vous voyez les détails de l'activité</span>\n" +
                        "            </div>\n" +
                        "        </div>\n" +
                        "        \n" +
                        "        <div class='note'>\n" +
                        "            ℹ️ Si l'application ne s'ouvre pas, cliquez sur le bouton ci-dessus\n" +
                        "        </div>\n" +
                        "    </div>\n" +
                        "</body>\n" +
                        "</html>";

                statusCode = 200;

            } else {
                response = "<html><body style='font-family:Arial; text-align:center; padding:50px; background:#f0f4f0;'>" +
                        "<div style='background:white; border-radius:20px; padding:30px; max-width:400px; margin:0 auto;'>" +
                        "<h1 style='color:#ff6b00;'>❌ Erreur</h1>" +
                        "<p style='color:#666;'>ID d'activité manquant</p>" +
                        "<p style='color:#999; font-size:12px; margin-top:20px;'>Utilisez: /activity?id=123</p>" +
                        "</div></body></html>";
                statusCode = 400;
            }

            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(statusCode, response.getBytes().length);

            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}