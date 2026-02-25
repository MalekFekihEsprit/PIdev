package GUI;

import Server.QRServer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class HomePage extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            // DÉMARRER LE SERVEUR QR CODE
            QRServer.startServer();

            // Lancer le front office des activités
            Parent root = FXMLLoader.load(getClass().getResource("/activitesfront.fxml"));

            Scene scene = new Scene(root);
            primaryStage.setTitle("TravelMate - Front Office Activités");
            primaryStage.setScene(scene);
            primaryStage.show();

            // Ajouter un hook pour arrêter le serveur à la fermeture
            primaryStage.setOnCloseRequest(event -> {
                QRServer.stopServer();
            });

        } catch (IOException e) {
            System.err.println("Erreur lors du chargement du fichier FXML : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void stop() throws Exception {
        // Arrêter le serveur proprement
        QRServer.stopServer();
        super.stop();
    }
}