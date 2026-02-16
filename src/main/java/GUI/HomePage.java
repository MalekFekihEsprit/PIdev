package GUI;

import Controllers.MainLayoutController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HomePage extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            System.out.println("Démarrage de l'application...");

            // Charger le layout principal (qui contient la navbar)
            FXMLLoader mainLoader = new FXMLLoader(getClass().getResource("/MainLayout.fxml"));
            Parent root = mainLoader.load();

            // Récupérer le contrôleur principal
            MainLayoutController mainController = mainLoader.getController();

            // Charger la page Itinéraire dans le contentArea
            FXMLLoader pageLoader = new FXMLLoader(getClass().getResource("/PageItineraire.fxml"));
            Parent pageItineraire = pageLoader.load();

            // Afficher la page dans le contentArea
            mainController.loadPageDirect(pageItineraire);

            Scene scene = new Scene(root, 1200, 800);
            primaryStage.setTitle("TravelMate - Gestion d'itinéraires");
            primaryStage.setScene(scene);
            primaryStage.show();

            System.out.println("Application démarrée avec succès");

        } catch (Exception e) {
            System.err.println("Erreur au démarrage : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}