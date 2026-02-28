package Services;

import Entities.Voyage;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Service de génération de QR Codes pour les voyages TravelMate.
 * Utilise la bibliothèque ZXing (offline, pas besoin de clé API).
 */
public class QRCodeServicess {

    private static final int DEFAULT_SIZE = 300;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Couleurs TravelMate pour le QR code
    private static final int QR_DARK_COLOR  = 0xFF0a0e27;   // #0a0e27 (dark navy)
    private static final int QR_LIGHT_COLOR = 0xFFFFFFFF;    // white background

    /**
     * Génère un QR code contenant les informations du voyage.
     *
     * @param voyage         le voyage
     * @param nomDestination le nom de la destination
     * @return une Image JavaFX du QR code
     */
    public static Image genererQRCodeVoyage(Voyage voyage, String nomDestination) throws WriterException {
        String contenu = construireContenuVoyage(voyage, nomDestination);
        return genererQRCode(contenu, DEFAULT_SIZE);
    }

    /**
     * Génère un QR code à partir d'un texte quelconque.
     */
    public static Image genererQRCode(String contenu, int taille) throws WriterException {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 2);

        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix bitMatrix = writer.encode(contenu, BarcodeFormat.QR_CODE, taille, taille, hints);

        MatrixToImageConfig config = new MatrixToImageConfig(QR_DARK_COLOR, QR_LIGHT_COLOR);
        BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix, config);

        return SwingFXUtils.toFXImage(bufferedImage, null);
    }

    /**
     * Génère et sauvegarde un QR code en fichier PNG.
     */
    public static File sauvegarderQRCode(Voyage voyage, String nomDestination, File fichier)
            throws WriterException, IOException {
        String contenu = construireContenuVoyage(voyage, nomDestination);

        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 2);

        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix bitMatrix = writer.encode(contenu, BarcodeFormat.QR_CODE, DEFAULT_SIZE, DEFAULT_SIZE, hints);

        MatrixToImageConfig config = new MatrixToImageConfig(QR_DARK_COLOR, QR_LIGHT_COLOR);
        BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix, config);

        ImageIO.write(bufferedImage, "PNG", fichier);
        return fichier;
    }

    /**
     * Construit le contenu textuel encodé dans le QR code.
     */
    private static String construireContenuVoyage(Voyage voyage, String nomDestination) {
        StringBuilder sb = new StringBuilder();
        sb.append("╔══ TRAVELMATE ══╗\n");
        sb.append("Voyage: ").append(voyage.getTitre_voyage()).append("\n");
        sb.append("Destination: ").append(nomDestination != null ? nomDestination : "N/A").append("\n");

        if (voyage.getDate_debut() != null) {
            sb.append("Début: ").append(voyage.getDate_debut().toLocalDate().format(DATE_FMT)).append("\n");
        }
        if (voyage.getDate_fin() != null) {
            sb.append("Fin: ").append(voyage.getDate_fin().toLocalDate().format(DATE_FMT)).append("\n");
        }
        sb.append("Statut: ").append(voyage.getStatut() != null ? voyage.getStatut() : "N/A").append("\n");
        sb.append("ID: ").append(voyage.getId_voyage()).append("\n");
        sb.append("╚═════════════════╝");

        return sb.toString();
    }
}