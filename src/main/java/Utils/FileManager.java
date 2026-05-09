package Utils;

import javafx.stage.FileChooser;
import javafx.stage.Window;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FileManager {

    // Chemin RELATIF stocké en base de données
    private static final String RELATIVE_UPLOAD_DIR          = "uploads/activites/";
    private static final String RELATIVE_UPLOAD_DIR_EVENEMENTS = "uploads/evenements/";

    // ─── Racines candidates pour résoudre les chemins ───────────────────────
    // On construit la liste une seule fois au chargement de la classe.
    private static final List<File> PROJECT_ROOTS = buildProjectRoots();

    private static List<File> buildProjectRoots() {
        List<File> roots = new ArrayList<>();

        // 1. Répertoire de travail courant (user.dir)
        roots.add(new File(System.getProperty("user.dir")));

        // 2. Répertoire du fichier .jar / .class en cours d'exécution
        try {
            File jarLocation = new File(
                    FileManager.class.getProtectionDomain()
                            .getCodeSource()
                            .getLocation()
                            .toURI()
            );
            // Si c'est un .jar → son parent ; si c'est un dossier classes → remonter jusqu'au projet
            File jarDir = jarLocation.isDirectory() ? jarLocation : jarLocation.getParentFile();
            // Remonter de 1 à 3 niveaux pour trouver la racine du projet (target/, out/, …)
            File candidate = jarDir;
            for (int i = 0; i < 3; i++) {
                if (candidate != null && !roots.contains(candidate)) {
                    roots.add(candidate);
                }
                if (candidate != null) candidate = candidate.getParentFile();
            }
        } catch (URISyntaxException | SecurityException ignored) { }

        // 3. Répertoire parent de user.dir (au cas où l'IDE lance depuis un sous-module)
        File userDirParent = new File(System.getProperty("user.dir")).getParentFile();
        if (userDirParent != null && !roots.contains(userDirParent)) {
            roots.add(userDirParent);
        }

        System.out.println("[FileManager] Racines de recherche d'images :");
        for (File r : roots) System.out.println("  → " + r.getAbsolutePath());

        return roots;
    }

    // ─── Chemins absolus pour l'upload (basé sur user.dir) ──────────────────
    private static String uploadDirEvenements() {
        return System.getProperty("user.dir") + "/uploads/evenements/";
    }

    private static String uploadDirActivites() {
        return System.getProperty("user.dir") + "/uploads/activites/";
    }

    static {
        // Créer les répertoires d'upload s'ils n'existent pas
        new File(uploadDirActivites()).mkdirs();
        new File(uploadDirEvenements()).mkdirs();
    }

    // ════════════════════════════════════════════════════════════════════════
    //  API publique
    // ════════════════════════════════════════════════════════════════════════

    /** Ouvre un sélecteur de fichier pour choisir une image. */
    public static File chooseImage(Window ownerWindow) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"),
                new FileChooser.ExtensionFilter("Tous les fichiers", "*.*")
        );
        return fileChooser.showOpenDialog(ownerWindow);
    }

    /**
     * Copie le fichier dans le dossier uploads/activites/ et retourne
     * le chemin RELATIF (ex: "uploads/activites/act_20260509_120000.jpg").
     */
    public static String saveImage(File sourceFile) throws IOException {
        if (sourceFile == null) return null;
        String newFileName = "act_" + timestamp() + extension(sourceFile);
        Path targetPath = Paths.get(uploadDirActivites() + newFileName);
        Files.copy(sourceFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        return RELATIVE_UPLOAD_DIR + newFileName;
    }

    /**
     * Copie le fichier dans le dossier uploads/evenements/ et retourne
     * le chemin RELATIF (ex: "uploads/evenements/evt_20260509_120000.jpg").
     */
    public static String saveImageEvenement(File sourceFile) throws IOException {
        if (sourceFile == null) return null;
        String newFileName = "evt_" + timestamp() + extension(sourceFile);
        Path targetPath = Paths.get(uploadDirEvenements() + newFileName);
        Files.copy(sourceFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        return RELATIVE_UPLOAD_DIR_EVENEMENTS + newFileName;
    }

    /**
     * Résout un chemin stocké en BD vers un {@link File} absolu existant.
     *
     * Stratégies (dans l'ordre) :
     *  1. Chemin absolu tel quel
     *  2. Chemin relatif depuis chaque racine candidate (user.dir, jar, …)
     *  3. Nom de fichier seul dans uploads/evenements/ depuis chaque racine (legacy)
     *  4. Nom de fichier seul dans uploads/activites/ depuis chaque racine (legacy)
     */
    public static File resolveImageFile(String storedPath) {
        if (storedPath == null || storedPath.trim().isEmpty()) return null;

        storedPath = storedPath.trim().replace("\\", "/");
        String fileName = new File(storedPath).getName();

        // 1. Chemin absolu tel quel
        File absolute = new File(storedPath);
        if (absolute.isAbsolute() && absolute.exists() && absolute.isFile()) {
            return absolute;
        }

        // 2 + 3 + 4 : parcourir toutes les racines candidates
        for (File root : PROJECT_ROOTS) {

            // 2. Chemin relatif depuis cette racine
            File f2 = new File(root, storedPath);
            if (f2.exists() && f2.isFile()) {
                System.out.println("[FileManager] Image trouvée (relatif) : " + f2.getAbsolutePath());
                return f2;
            }

            // 3. Nom seul → uploads/evenements/ (données legacy)
            File f3 = new File(root, RELATIVE_UPLOAD_DIR_EVENEMENTS + fileName);
            if (f3.exists() && f3.isFile()) {
                System.out.println("[FileManager] Image trouvée (legacy evenements) : " + f3.getAbsolutePath());
                return f3;
            }

            // 4. Nom seul → uploads/activites/ (données legacy)
            File f4 = new File(root, RELATIVE_UPLOAD_DIR + fileName);
            if (f4.exists() && f4.isFile()) {
                System.out.println("[FileManager] Image trouvée (legacy activites) : " + f4.getAbsolutePath());
                return f4;
            }
        }

        // Aucun fichier trouvé : log détaillé pour faciliter le débogage
        System.err.println("[FileManager] Image introuvable pour : \"" + storedPath + "\"");
        System.err.println("  Chemins testés :");
        for (File root : PROJECT_ROOTS) {
            System.err.println("    " + new File(root, storedPath).getAbsolutePath());
            System.err.println("    " + new File(root, RELATIVE_UPLOAD_DIR_EVENEMENTS + fileName).getAbsolutePath());
            System.err.println("    " + new File(root, RELATIVE_UPLOAD_DIR + fileName).getAbsolutePath());
        }
        return null;
    }

    /** Supprime un fichier image à partir de son chemin relatif ou absolu. */
    public static boolean deleteImage(String storedPath) {
        if (storedPath == null || storedPath.isEmpty()) return false;
        try {
            File file = resolveImageFile(storedPath);
            if (file != null && file.exists()) return file.delete();
            return new File(storedPath).delete();
        } catch (Exception e) {
            System.err.println("[FileManager] Erreur suppression image : " + e.getMessage());
            return false;
        }
    }

    /** Retourne le nom du fichier depuis un chemin complet. */
    public static String getFileName(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) return "";
        return new File(imagePath).getName();
    }

    // ════════════════════════════════════════════════════════════════════════
    //  Helpers privés
    // ════════════════════════════════════════════════════════════════════════

    private static String timestamp() {
        return new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    }

    private static String extension(File f) {
        String name = f.getName();
        int dot = name.lastIndexOf('.');
        return dot > 0 ? name.substring(dot) : ".jpg";
    }
}