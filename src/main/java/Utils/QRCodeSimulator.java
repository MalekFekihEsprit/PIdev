package Utils;

import javafx.scene.control.Alert;
import javafx.scene.control.TextInputDialog;

import java.util.Optional;

public class QRCodeSimulator {

    /**
     * Affiche une boîte de dialogue pour simuler le scan d'un QR code
     */
    public static void showQRSimulator() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Simulateur QR Code");
        dialog.setHeaderText("📱 Scanner un QR Code");
        dialog.setContentText("Entrez l'ID de l'activité ou l'URL complète:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(input -> {
            try {
                if (input.matches("\\d+")) {
                    DeepLinkHandler.getInstance().handleDeepLink("travelmate://activity?id=" + input);
                } else {
                    DeepLinkHandler.getInstance().handleDeepLink(input);
                }
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setHeaderText("QR Code invalide");
                alert.setContentText("Impossible de traiter ce QR code");
                alert.showAndWait();
            }
        });
    }
}
