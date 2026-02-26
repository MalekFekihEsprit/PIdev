package Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class HomePageController implements Initializable {

    @FXML private VBox frontOfficeCard;
    @FXML private VBox backOfficeCard;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Setup hover effects
        setupCardEffects();

        // Setup click handlers
        frontOfficeCard.setOnMouseClicked(event -> openFrontOffice());
        backOfficeCard.setOnMouseClicked(event -> openBackOffice());
    }

    private void setupCardEffects() {
        // Front Office card hover effect
        frontOfficeCard.setOnMouseEntered(event ->
                frontOfficeCard.setStyle("-fx-background-color: white; -fx-background-radius: 30; -fx-padding: 40; -fx-effect: dropshadow(three-pass-box, rgba(255,140,66,0.3), 25, 0, 0, 10); -fx-cursor: hand;")
        );
        frontOfficeCard.setOnMouseExited(event ->
                frontOfficeCard.setStyle("-fx-background-color: white; -fx-background-radius: 30; -fx-padding: 40; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 20, 0, 0, 10); -fx-cursor: hand;")
        );

        // Back Office card hover effect
        backOfficeCard.setOnMouseEntered(event ->
                backOfficeCard.setStyle("-fx-background-color: #1e2749; -fx-background-radius: 30; -fx-padding: 40; -fx-effect: dropshadow(three-pass-box, rgba(255,140,66,0.3), 25, 0, 0, 10); -fx-cursor: hand;")
        );
        backOfficeCard.setOnMouseExited(event ->
                backOfficeCard.setStyle("-fx-background-color: #111633; -fx-background-radius: 30; -fx-padding: 40; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 20, 0, 0, 10); -fx-cursor: hand;")
        );
    }

    private void openFrontOffice() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DestinationFront.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) frontOfficeCard.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TravelMate - Front Office");
            stage.setMaximized(true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openBackOffice() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DestinationBack.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) backOfficeCard.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TravelMate - Back Office (Administration)");
            stage.setMaximized(true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}