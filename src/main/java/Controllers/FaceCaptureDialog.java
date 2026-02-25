package Controllers;

import com.github.sarxos.webcam.Webcam;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class FaceCaptureDialog {
    
    public static File captureFace(Stage owner) {
        Webcam webcam = Webcam.getDefault();
        if (webcam == null) {
            showAlert(owner, "Erreur", "Aucune webcam détectée");
            return null;
        }

        // Créer la fenêtre de capture
        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.setTitle("Capture faciale");

        ImageView imageView = new ImageView();
        imageView.setFitWidth(320);
        imageView.setFitHeight(240);

        Button captureBtn = new Button("📸 Capturer");
        Button cancelBtn = new Button("Annuler");

        VBox vbox = new VBox(10, imageView, captureBtn, cancelBtn);
        vbox.setPadding(new Insets(10));
        vbox.setStyle("-fx-alignment: center;");

        Scene scene = new Scene(vbox, 400, 350);
        dialog.setScene(scene);

        // Démarrer la webcam
        webcam.open();
        
        // Thread de mise à jour de l'image
        Thread cameraThread = new Thread(() -> {
            while (dialog.isShowing()) {
                BufferedImage img = webcam.getImage();
                javafx.application.Platform.runLater(() -> 
                    imageView.setImage(SwingFXUtils.toFXImage(img, null))
                );
                try { Thread.sleep(50); } catch (InterruptedException e) {}
            }
        });
        cameraThread.setDaemon(true);
        cameraThread.start();

        final File[] capturedFile = {null};

        captureBtn.setOnAction(e -> {
            try {
                BufferedImage img = webcam.getImage();
                File tempFile = File.createTempFile("face_", ".jpg");
                ImageIO.write(img, "JPG", tempFile);
                capturedFile[0] = tempFile;
                dialog.close();
            } catch (IOException ex) {
                showAlert(dialog, "Erreur", "Échec capture: " + ex.getMessage());
            }
        });

        cancelBtn.setOnAction(e -> dialog.close());
        dialog.setOnHidden(e -> webcam.close());

        dialog.showAndWait();
        return capturedFile[0];
    }

    private static void showAlert(Stage owner, String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(owner);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}