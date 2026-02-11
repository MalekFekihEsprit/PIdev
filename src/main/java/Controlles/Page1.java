package Controlles;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class Page1 {

    @FXML
    private VBox stepJour1;
    @FXML
    private VBox stepJour2;
    @FXML
    private VBox stepJour3;
    @FXML
    private VBox stepJour4;

    @FXML
    private void initialize() {
        setStepClickHandler(stepJour1);
        setStepClickHandler(stepJour2);
        setStepClickHandler(stepJour3);
        setStepClickHandler(stepJour4);
    }

    private void setStepClickHandler(VBox step) {
        if (step == null) return;
        step.setOnMouseClicked(event -> ouvrirPageEtape());
    }

    private void ouvrirPageEtape() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/PageEtape.fxml"));
            Scene scene = new Scene(root);
            String css = getClass().getResource("/style.css").toExternalForm();
            if (css != null) {
                scene.getStylesheets().add(css);
            }
            Stage stage = (Stage) stepJour1.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
