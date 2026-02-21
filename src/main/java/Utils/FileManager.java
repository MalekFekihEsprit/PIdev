package Utils;

import javafx.stage.FileChooser;
import javafx.stage.Window;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileManager {

    private static final String UPLOAD_DIR = "uploads/activites/";

    static {
        // Créer le répertoire d'upload s'il n'existe pas
        File directory = new File(UPLOAD_DIR);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    /**
     * Ouvre un sélecteur de fichier pour choisir une image
     */
    public static File chooseImage(Window ownerWindow) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");

        // Filtrer pour n'afficher que les images
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"),
                new FileChooser.ExtensionFilter("Tous les fichiers", "*.*")
        );

        return fileChooser.showOpenDialog(ownerWindow);
    }

    /**
     * Copie le fichier sélectionné dans le répertoire d'upload
     * et retourne le chemin relatif
     */
    public static String saveImage(File sourceFile) throws IOException {
        if (sourceFile == null) return null;

        // Générer un nom de fichier unique avec timestamp
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String originalFileName = sourceFile.getName();
        String extension = "";
        int dotIndex = originalFileName.lastIndexOf(".");
        if (dotIndex > 0) {
            extension = originalFileName.substring(dotIndex);
        } else {
            extension = ".jpg"; // extension par défaut
        }

        String newFileName = "act_" + timestamp + extension;

        // Chemin complet de destination
        Path targetPath = Paths.get(UPLOAD_DIR + newFileName);

        // Copier le fichier
        Files.copy(sourceFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        // Retourner le chemin relatif à stocker dans la BD
        return targetPath.toString();
    }

    /**
     * Supprime un fichier image
     */
    public static boolean deleteImage(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) return false;

        try {
            File file = new File(imagePath);
            return file.delete();
        } catch (Exception e) {
            System.err.println("Erreur lors de la suppression de l'image: " + e.getMessage());
            return false;
        }
    }

    /**
     * Récupère le nom du fichier à partir du chemin complet
     */
    public static String getFileName(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) return "";
        File file = new File(imagePath);
        return file.getName();
    }
}