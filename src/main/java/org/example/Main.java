package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        System.out.println("Démarrage de l'application...");

        Parent root = FXMLLoader.load(getClass().getResource("/MainLayout.fxml"));

        Scene scene = new Scene(root);
        primaryStage.setTitle("TravelMate - Gestion d'itinéraires");
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();

        System.out.println("Application démarrée avec succès");
    }

    public static void main(String[] args) {
        launch(args);
    }
}