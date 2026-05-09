package Utils;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Utilitaire centralisé pour la navigation entre les écrans.
 * Garantit que chaque écran s'affiche en plein écran sans marge ni flash.
 *
 * RÈGLE : créer la Scene avec les dimensions de l'écran, puis setMaximized(true) AVANT show().
 */
public class NavigationUtil {

    /**
     * Retourne les dimensions de l'écran principal.
     */
    private static Rectangle2D getScreenBounds() {
        return Screen.getPrimary().getVisualBounds();
    }

    /**
     * Crée une Scene avec les dimensions de l'écran principal.
     * À utiliser pour tous les écrans principaux (plein écran).
     */
    public static Scene makeFullScene(Parent root) {
        Rectangle2D bounds = getScreenBounds();
        return new Scene(root, bounds.getWidth(), bounds.getHeight());
    }

    /**
     * Navigue vers un écran principal (plein écran).
     */
    public static void navigateTo(Stage currentStage, String fxmlPath, String title) throws IOException {
        FXMLLoader loader = new FXMLLoader(NavigationUtil.class.getResource(fxmlPath));
        Parent root = loader.load();
        navigateTo(currentStage, root, title);
    }

    /**
     * Navigue vers un écran principal (plein écran) avec un root déjà chargé.
     * La scène est créée avec les dimensions de l'écran pour éviter toute marge.
     */
    public static void navigateTo(Stage currentStage, Parent root, String title) {
        Rectangle2D bounds = getScreenBounds();
        Scene scene = new Scene(root, bounds.getWidth(), bounds.getHeight());
        currentStage.setScene(scene);
        currentStage.setTitle(title);
        currentStage.setMaximized(true);
        currentStage.show();
    }

    /**
     * Navigue vers un écran d'authentification (taille normale, non maximisé).
     */
    public static void navigateToAuth(Stage currentStage, String fxmlPath, String title) throws IOException {
        FXMLLoader loader = new FXMLLoader(NavigationUtil.class.getResource(fxmlPath));
        Parent root = loader.load();
        navigateToAuth(currentStage, root, title);
    }

    /**
     * Navigue vers un écran d'authentification avec un root déjà chargé.
     */
    public static void navigateToAuth(Stage currentStage, Parent root, String title) {
        Scene scene = new Scene(root);
        currentStage.setScene(scene);
        currentStage.setTitle(title);
        currentStage.setMaximized(false);
        currentStage.show();
    }
}
