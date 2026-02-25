package Utils;

import com.github.sarxos.webcam.Webcam;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class WebcamUtil {
    public static File captureImage(String email) throws IOException {
        Webcam webcam = Webcam.getDefault();
        if (webcam == null) {
            throw new IOException("Aucune webcam détectée");
        }
        webcam.open();
        BufferedImage image = webcam.getImage();
        webcam.close();

        File tempFile = File.createTempFile("intruder_" + email + "_", ".jpg");
        ImageIO.write(image, "JPG", tempFile);
        return tempFile;
    }
}