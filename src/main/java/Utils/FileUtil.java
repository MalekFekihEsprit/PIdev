package Utils;

import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;

public class FileUtil {
    public static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/profiles/";

    public static void ensureUploadDirExists() {
        try {
            Files.createDirectories(Paths.get(UPLOAD_DIR));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static File selectImageFile(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une photo de profil");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"),
                new FileChooser.ExtensionFilter("Tous les fichiers", "*.*")
        );
        return fileChooser.showOpenDialog(stage);
    }

    public static String saveImageToUploads(File sourceFile, int userId) throws IOException {
        // Générer un nom de fichier unique : user_15_1677234567.jpg
        String extension = getFileExtension(sourceFile.getName());
        String fileName = "user_" + userId + "_" + Instant.now().getEpochSecond() + "." + extension;
        Path destination = Paths.get(UPLOAD_DIR, fileName);
        Files.copy(sourceFile.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);
        return fileName; // Retourne uniquement le nom du fichier
    }

    public static String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex > 0) ? fileName.substring(dotIndex + 1) : "jpg";
    }

    public static File getImageFile(String fileName) {
        if (fileName == null || fileName.isEmpty()) return null;
        return new File(UPLOAD_DIR + fileName);
    }
}