package Utils;

import Controllers.ActivityDetailController;
import Entities.Activites;
import Services.ActivitesCRUD;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class DeepLinkHandler {

    private static DeepLinkHandler instance;
    private Stage primaryStage;
    private ActivitesCRUD activitesCRUD;

    private DeepLinkHandler() {
        activitesCRUD = new ActivitesCRUD();
    }

    public static DeepLinkHandler getInstance() {
        if (instance == null) {
            instance = new DeepLinkHandler();
        }
        return instance;
    }

    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    /**
     * Gère un deep link reçu
     */
    public void handleDeepLink(String url) {
        System.out.println("🔗 Deep link reçu: " + url);

        if (url != null && url.contains("id=")) {
            int activityId = extractActivityId(url);
            if (activityId > 0) {
                openActivityDetail(activityId);
            }
        }
    }

    /**
     * Extrait l'ID de l'activité depuis l'URL
     */
    private int extractActivityId(String url) {
        try {
            if (url.contains("id=")) {
                String idPart = url.split("id=")[1];
                if (idPart.contains("&")) {
                    idPart = idPart.split("&")[0];
                }
                if (idPart.contains("?")) {
                    idPart = idPart.split("\\?")[0];
                }
                return Integer.parseInt(idPart);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Ouvre la page de détail d'une activité
     */
    private void openActivityDetail(int activityId) {
        Platform.runLater(() -> {
            try {
                Activites activite = activitesCRUD.getOne(activityId);

                if (activite != null && primaryStage != null) {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/activitydetail.fxml"));
                    Parent root = loader.load();

                    ActivityDetailController controller = loader.getController();
                    controller.setActivite(activite);

                    Scene scene = new Scene(root);
                    primaryStage.setScene(scene);
                    primaryStage.setTitle("TravelMate - Détail de l'activité");
                    primaryStage.show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}