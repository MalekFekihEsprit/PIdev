package GUI;

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
            // Charger le fichier FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/BudgetDepenseFront.fxml"));
            Parent root = loader.load();

            // Créer la scène avec les dimensions de votre interface
            Scene scene = new Scene(root, 1400, 900);

            // Configurer la fenêtre
            primaryStage.setTitle("TravelMate - Gestion de Budget");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(1200); // Taille minimum
            primaryStage.setMinHeight(700);  // Taille minimum
            primaryStage.show();

            System.out.println("✅ Application démarrée avec succès !");

        } catch (IOException e) {
            System.err.println("❌ Erreur lors du chargement du FXML : " + e.getMessage());
            e.printStackTrace();
            showErrorAlert(e);
        }
    }

    private void showErrorAlert(IOException e) {
        // Optionnel : Afficher une alerte graphique
        javafx.scene.control.Alert alert =
                new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("Erreur de démarrage");
        alert.setHeaderText("Impossible de charger l'interface");
        alert.setContentText("Fichier FXML introuvable : " + e.getMessage());
        alert.showAndWait();
    }
}