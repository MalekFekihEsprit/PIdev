package Utils;

import Entites.Itineraire;
import Entites.etape;
import Services.etapeCRUD;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ExportUtil {

    public static void exporterItineraire(Itineraire itineraire, List<etape> etapes, Window ownerWindow) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter l'itinéraire");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Fichiers CSV", "*.csv"),
                new FileChooser.ExtensionFilter("Fichiers TXT", "*.txt"),
                new FileChooser.ExtensionFilter("Tous les fichiers", "*.*")
        );

        String defaultName = itineraire.getNom_itineraire().replaceAll("\\s+", "_") + "_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        fileChooser.setInitialFileName(defaultName + ".csv");

        File file = fileChooser.showSaveDialog(ownerWindow);

        if (file != null) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                // En-tête
                writer.println("=== EXPORT ITINÉRAIRE ===");
                writer.println("Date d'export: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
                writer.println();

                // Informations de l'itinéraire
                writer.println("INFORMATIONS GÉNÉRALES:");
                writer.println("Nom: " + itineraire.getNom_itineraire());
                writer.println("Description: " + (itineraire.getDescription_itineraire() != null ?
                        itineraire.getDescription_itineraire() : "Non spécifiée"));
                writer.println();

                // Étapes
                writer.println("ÉTAPES:");
                if (etapes != null && !etapes.isEmpty()) {
                    writer.println("Heure;Activité;Lieu;Durée;Description");
                    for (etape e : etapes) {
                        String heure = e.getHeure() != null ? e.getHeure().toString().substring(0, 5) : "--:--";
                        String activite = e.getNomActivite() != null ? e.getNomActivite() : "Inconnue";
                        String lieu = e.getLieuActivite() != null ? e.getLieuActivite() : "Non défini";
                        String duree = e.getDureeActivite() != null ? e.getDureeActivite() + "h" : "Non spécifiée";
                        String desc = e.getDescription_etape() != null ? e.getDescription_etape().replace("\n", " ") : "";

                        writer.println(heure + ";" + activite + ";" + lieu + ";" + duree + ";" + desc);
                    }

                    // Statistiques
                    writer.println();
                    writer.println("STATISTIQUES:");
                    writer.println("Nombre total d'étapes: " + etapes.size());

                    float dureeTotale = etapes.stream()
                            .map(e -> e.getDureeActivite() != null ? e.getDureeActivite() : 0f)
                            .reduce(0f, Float::sum);
                    writer.println("Durée totale: " + dureeTotale + "h");

                } else {
                    writer.println("Aucune étape planifiée pour cet itinéraire.");
                }

                writer.println();
                writer.println("=== FIN DE L'EXPORT ===");

                AlertUtil.showInfo("Export réussi", "L'itinéraire a été exporté avec succès vers:\n" + file.getAbsolutePath());

            } catch (Exception e) {
                e.printStackTrace();
                AlertUtil.showError("Erreur d'export", "Impossible d'exporter l'itinéraire: " + e.getMessage());
            }
        }
    }

    public static void exporterTousItineraires(List<Itineraire> itineraires, etapeCRUD etapeCRUD, Window ownerWindow) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter tous les itinéraires");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers CSV", "*.csv"));

        String defaultName = "tous_itineraires_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        fileChooser.setInitialFileName(defaultName + ".csv");

        File file = fileChooser.showSaveDialog(ownerWindow);

        if (file != null) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                writer.println("=== EXPORT DE TOUS LES ITINÉRAIRES ===");
                writer.println("Date d'export: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
                writer.println();

                for (Itineraire itineraire : itineraires) {
                    writer.println("------------------------------------------------");
                    writer.println("ITINÉRAIRE: " + itineraire.getNom_itineraire());
                    writer.println("Description: " + (itineraire.getDescription_itineraire() != null ?
                            itineraire.getDescription_itineraire() : "Non spécifiée"));

                    try {
                        List<etape> etapes = etapeCRUD.getEtapesByItineraire(itineraire.getId_itineraire());
                        writer.println("Nombre d'étapes: " + (etapes != null ? etapes.size() : 0));

                        if (etapes != null && !etapes.isEmpty()) {
                            writer.println("Heure;Activité;Lieu");
                            for (etape e : etapes) {
                                String heure = e.getHeure() != null ? e.getHeure().toString().substring(0, 5) : "--:--";
                                String activite = e.getNomActivite() != null ? e.getNomActivite() : "Inconnue";
                                String lieu = e.getLieuActivite() != null ? e.getLieuActivite() : "Non défini";
                                writer.println(heure + ";" + activite + ";" + lieu);
                            }
                        }
                    } catch (SQLException e) {
                        writer.println("Erreur lors du chargement des étapes");
                    }

                    writer.println();
                }

                writer.println("=== FIN DE L'EXPORT ===");
                writer.println("Total itinéraires exportés: " + itineraires.size());

                AlertUtil.showInfo("Export réussi", "Tous les itinéraires ont été exportés avec succès vers:\n" + file.getAbsolutePath());

            } catch (Exception e) {
                e.printStackTrace();
                AlertUtil.showError("Erreur d'export", "Impossible d'exporter les itinéraires: " + e.getMessage());
            }
        }
    }
}