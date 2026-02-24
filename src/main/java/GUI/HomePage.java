package GUI;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HomePage extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Start with the Home Page
            Parent root = FXMLLoader.load(getClass().getResource("/HomePage.fxml"));

            Scene scene = new Scene(root);
            primaryStage.setTitle("TravelMate - Plateforme de Voyage");
            primaryStage.setScene(scene);
            primaryStage.setMaximized(true);
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}