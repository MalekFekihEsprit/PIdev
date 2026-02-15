package Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

public class PageAccueilController {

    @FXML
    private Button btnFrontOffice;

    @FXML
    private Button btnBackOffice;

    @FXML
    public void initialize() {
        // Configuration des événements
        btnFrontOffice.setOnAction(this::openFrontOffice);
        btnBackOffice.setOnAction(this::openBackOffice);
    }

    private void openFrontOffice(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/PageVoyage.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) btnFrontOffice.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TravelMate - Front Office");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur lors de l'ouverture du Front Office");
        }
    }

    private void openBackOffice(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/PageVoyageBack.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) btnBackOffice.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TravelMate - Back Office");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur lors de l'ouverture du Back Office");
        }
    }

    private void showError(String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}