package Services;

import Server.QRServer;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class QRCodeService {

    private static final String QR_API = "https://api.qrserver.com/v1/create-qr-code/?size=100x100&data=";

    // IP détectée automatiquement
    private static String localIp = getLocalIp();

    /**
     * Détecte automatiquement l'IP locale du PC
     */
    private static String getLocalIp() {
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            String ip = localHost.getHostAddress();

            if (!ip.startsWith("127.") && !ip.startsWith("0.")) {
                System.out.println("🌐 IP détectée: " + ip);
                return ip;
            }

            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                Enumeration<InetAddress> addresses = ni.getInetAddresses();

                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    String hostAddr = addr.getHostAddress();

                    if (!addr.isLoopbackAddress() && !addr.isLinkLocalAddress() &&
                            !addr.isMulticastAddress() && hostAddr.contains(".")) {

                        if (hostAddr.startsWith("192.168.") || hostAddr.startsWith("10.")) {
                            System.out.println("🌐 IP réseau local détectée: " + hostAddr);
                            return hostAddr;
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ Impossible de détecter l'IP: " + e.getMessage());
        }

        return "192.168.1.9"; // IP par défaut
    }

    /**
     * Génère un QR code avec le port dynamique
     */
    public static ImageView generateQRCode(int activityId, String activityName) {
        try {
            // Récupérer le port actuel du serveur
            int port = QRServer.getCurrentPort();

            // URL accessible depuis le téléphone
            String url = "http://" + localIp + ":" + port + "/activity?id=" + activityId;

            System.out.println("📱 QR code URL: " + url);

            String encodedUrl = URLEncoder.encode(url, StandardCharsets.UTF_8.toString());
            String qrUrl = QR_API + encodedUrl;

            Image qrImage = new Image(qrUrl, 80, 80, true, true);

            if (qrImage.isError()) {
                System.err.println("❌ Erreur de chargement de l'image QR code");
                return null;
            }

            ImageView qrView = new ImageView(qrImage);
            qrView.setFitWidth(60);
            qrView.setFitHeight(60);
            qrView.setPreserveRatio(true);
            qrView.setStyle("-fx-cursor: hand;");

            return qrView;

        } catch (Exception e) {
            System.err.println("❌ Erreur génération QR code: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}