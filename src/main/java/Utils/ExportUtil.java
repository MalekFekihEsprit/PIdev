package Utils;

import Entities.Itineraire;
import Entities.etape;
import Services.etapeCRUD;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.util.CellRangeAddress;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ExportUtil {

    private static final String[] HEADER_COLORS = {
            "#FF8C42", "#FF6B4A", "#10B981", "#3B82F6", "#8B5CF6", "#6366F1"
    };

    // ==================== MÉTHODES D'EXPORT EXCEL ====================

    public static void exporterItineraireExcel(Itineraire itineraire, List<etape> etapes, Window ownerWindow) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter l'itinéraire en Excel");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Fichiers Excel", "*.xlsx"),
                new FileChooser.ExtensionFilter("Fichiers Excel (ancien)", "*.xls")
        );

        String defaultName = itineraire.getNom_itineraire().replaceAll("\\s+", "_") + "_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        fileChooser.setInitialFileName(defaultName + ".xlsx");

        File file = fileChooser.showSaveDialog(ownerWindow);

        if (file != null) {
            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Itinéraire");

                // Styles
                CellStyle titleStyle = createTitleStyle(workbook);
                CellStyle headerStyle = createHeaderStyle(workbook);
                CellStyle subHeaderStyle = createSubHeaderStyle(workbook);
                CellStyle cellStyle = createCellStyle(workbook);
                CellStyle dateCellStyle = createDateCellStyle(workbook);
                CellStyle highlightStyle = createHighlightStyle(workbook);

                int rowNum = 0;

                // Titre principal
                Row titleRow = sheet.createRow(rowNum++);
                Cell titleCell = titleRow.createCell(0);
                titleCell.setCellValue("EXPORT ITINÉRAIRE : " + itineraire.getNom_itineraire().toUpperCase());
                titleCell.setCellStyle(titleStyle);
                sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 0, 4));

                rowNum++; // Espace

                // Date d'export
                Row dateRow = sheet.createRow(rowNum++);
                Cell dateLabelCell = dateRow.createCell(0);
                dateLabelCell.setCellValue("Date d'export :");
                dateLabelCell.setCellStyle(subHeaderStyle);

                Cell dateValueCell = dateRow.createCell(1);
                dateValueCell.setCellValue(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
                dateValueCell.setCellStyle(dateCellStyle);

                rowNum++; // Espace

                // Informations générales
                Row infoHeaderRow = sheet.createRow(rowNum++);
                Cell infoHeaderCell = infoHeaderRow.createCell(0);
                infoHeaderCell.setCellValue("INFORMATIONS GÉNÉRALES");
                infoHeaderCell.setCellStyle(headerStyle);
                sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 0, 4));

                // Nom
                Row nomRow = sheet.createRow(rowNum++);
                Cell nomLabelCell = nomRow.createCell(0);
                nomLabelCell.setCellValue("Nom :");
                nomLabelCell.setCellStyle(subHeaderStyle);

                Cell nomValueCell = nomRow.createCell(1);
                nomValueCell.setCellValue(itineraire.getNom_itineraire());
                nomValueCell.setCellStyle(cellStyle);

                // Description
                Row descRow = sheet.createRow(rowNum++);
                Cell descLabelCell = descRow.createCell(0);
                descLabelCell.setCellValue("Description :");
                descLabelCell.setCellStyle(subHeaderStyle);

                Cell descValueCell = descRow.createCell(1);
                descValueCell.setCellValue(itineraire.getDescription_itineraire() != null ?
                        itineraire.getDescription_itineraire() : "Non spécifiée");
                descValueCell.setCellStyle(cellStyle);

                rowNum++; // Espace

                // Étapes
                Row etapesHeaderRow = sheet.createRow(rowNum++);
                Cell etapesHeaderCell = etapesHeaderRow.createCell(0);
                etapesHeaderCell.setCellValue("ÉTAPES");
                etapesHeaderCell.setCellStyle(headerStyle);
                sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 0, 4));

                if (etapes != null && !etapes.isEmpty()) {
                    // En-têtes des colonnes
                    String[] columns = {"Heure", "Activité", "Lieu", "Durée (h)", "Description"};
                    Row headerRow = sheet.createRow(rowNum++);
                    for (int i = 0; i < columns.length; i++) {
                        Cell cell = headerRow.createCell(i);
                        cell.setCellValue(columns[i]);
                        cell.setCellStyle(createColoredHeaderStyle(workbook, i));
                    }

                    // Données des étapes
                    float dureeTotale = 0;
                    for (etape e : etapes) {
                        Row row = sheet.createRow(rowNum++);

                        // Heure
                        Cell heureCell = row.createCell(0);
                        heureCell.setCellValue(e.getHeure() != null ? e.getHeure().toString() : "--:--");
                        heureCell.setCellStyle(dateCellStyle);

                        // Activité
                        Cell activiteCell = row.createCell(1);
                        activiteCell.setCellValue(e.getNomActivite() != null ? e.getNomActivite() : "Inconnue");
                        activiteCell.setCellStyle(cellStyle);

                        // Lieu
                        Cell lieuCell = row.createCell(2);
                        lieuCell.setCellValue(e.getLieuActivite() != null ? e.getLieuActivite() : "Non défini");
                        lieuCell.setCellStyle(cellStyle);

                        // Durée
                        float duree = e.getDureeActivite() != null ? e.getDureeActivite() : 0f;
                        dureeTotale += duree;
                        Cell dureeCell = row.createCell(3);
                        dureeCell.setCellValue(duree);
                        dureeCell.setCellStyle(createNumberCellStyle(workbook));

                        // Description
                        Cell descCell = row.createCell(4);
                        descCell.setCellValue(e.getDescription_etape() != null ? e.getDescription_etape() : "");
                        descCell.setCellStyle(cellStyle);
                    }

                    rowNum++; // Espace

                    // Statistiques
                    Row statsHeaderRow = sheet.createRow(rowNum++);
                    Cell statsHeaderCell = statsHeaderRow.createCell(0);
                    statsHeaderCell.setCellValue("STATISTIQUES");
                    statsHeaderCell.setCellStyle(headerStyle);
                    sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 0, 4));

                    // Nombre d'étapes
                    Row nbRow = sheet.createRow(rowNum++);
                    Cell nbLabelCell = nbRow.createCell(0);
                    nbLabelCell.setCellValue("Nombre total d'étapes :");
                    nbLabelCell.setCellStyle(subHeaderStyle);

                    Cell nbValueCell = nbRow.createCell(1);
                    nbValueCell.setCellValue(etapes.size());
                    nbValueCell.setCellStyle(highlightStyle);

                    // Durée totale
                    Row dureeTotaleRow = sheet.createRow(rowNum++);
                    Cell dureeTotaleLabelCell = dureeTotaleRow.createCell(0);
                    dureeTotaleLabelCell.setCellValue("Durée totale :");
                    dureeTotaleLabelCell.setCellStyle(subHeaderStyle);

                    Cell dureeTotaleValueCell = dureeTotaleRow.createCell(1);
                    dureeTotaleValueCell.setCellValue(dureeTotale + " heures");
                    dureeTotaleValueCell.setCellStyle(highlightStyle);

                } else {
                    Row noEtapesRow = sheet.createRow(rowNum++);
                    Cell noEtapesCell = noEtapesRow.createCell(0);
                    noEtapesCell.setCellValue("Aucune étape planifiée pour cet itinéraire.");
                    noEtapesCell.setCellStyle(createWarningStyle(workbook));
                }

                // Ajuster la largeur des colonnes
                for (int i = 0; i < 5; i++) {
                    sheet.autoSizeColumn(i);
                }

                // Sauvegarder le fichier
                try (FileOutputStream fileOut = new FileOutputStream(file)) {
                    workbook.write(fileOut);
                }

                AlertUtil.showInfo("Export réussi", "L'itinéraire a été exporté avec succès vers :\n" + file.getAbsolutePath());

                // Ouvrir automatiquement le fichier
                openFile(file);

            } catch (Exception e) {
                e.printStackTrace();
                AlertUtil.showError("Erreur d'export", "Impossible d'exporter l'itinéraire : " + e.getMessage());
            }
        }
    }

    public static void exporterTousItinerairesExcel(List<Itineraire> itineraires, etapeCRUD etapeCRUD, Window ownerWindow) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter tous les itinéraires en Excel");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers Excel", "*.xlsx"));

        String defaultName = "tous_itineraires_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        fileChooser.setInitialFileName(defaultName + ".xlsx");

        File file = fileChooser.showSaveDialog(ownerWindow);

        if (file != null) {
            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet summarySheet = workbook.createSheet("Résumé");

                // Styles
                CellStyle titleStyle = createTitleStyle(workbook);
                CellStyle headerStyle = createHeaderStyle(workbook);
                CellStyle subHeaderStyle = createSubHeaderStyle(workbook);
                CellStyle cellStyle = createCellStyle(workbook);
                CellStyle dateCellStyle = createDateCellStyle(workbook);

                int rowNum = 0;

                // Titre principal
                Row titleRow = summarySheet.createRow(rowNum++);
                Cell titleCell = titleRow.createCell(0);
                titleCell.setCellValue("EXPORT DE TOUS LES ITINÉRAIRES");
                titleCell.setCellStyle(titleStyle);
                summarySheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 0, 5));

                rowNum++; // Espace

                // Date d'export
                Row dateRow = summarySheet.createRow(rowNum++);
                Cell dateLabelCell = dateRow.createCell(0);
                dateLabelCell.setCellValue("Date d'export :");
                dateLabelCell.setCellStyle(subHeaderStyle);

                Cell dateValueCell = dateRow.createCell(1);
                dateValueCell.setCellValue(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
                dateValueCell.setCellStyle(dateCellStyle);

                rowNum++; // Espace

                // En-têtes du tableau récapitulatif
                String[] columns = {"N°", "Nom Itinéraire", "Description", "Nombre d'étapes", "Durée totale (h)", "Statut"};
                Row headerRow = summarySheet.createRow(rowNum++);
                for (int i = 0; i < columns.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(columns[i]);
                    cell.setCellStyle(createColoredHeaderStyle(workbook, i));
                }

                int totalEtapes = 0;
                float totalDuree = 0;

                // Remplir le tableau récapitulatif
                for (int i = 0; i < itineraires.size(); i++) {
                    Itineraire itineraire = itineraires.get(i);
                    Row row = summarySheet.createRow(rowNum++);

                    // N°
                    Cell numCell = row.createCell(0);
                    numCell.setCellValue(i + 1);
                    numCell.setCellStyle(cellStyle);

                    // Nom
                    Cell nomCell = row.createCell(1);
                    nomCell.setCellValue(itineraire.getNom_itineraire());
                    nomCell.setCellStyle(cellStyle);

                    // Description
                    Cell descCell = row.createCell(2);
                    descCell.setCellValue(itineraire.getDescription_itineraire() != null ?
                            itineraire.getDescription_itineraire() : "Non spécifiée");
                    descCell.setCellStyle(cellStyle);

                    try {
                        List<etape> etapes = etapeCRUD.getEtapesByItineraire(itineraire.getId_itineraire());
                        int nbEtapes = etapes != null ? etapes.size() : 0;
                        totalEtapes += nbEtapes;

                        // Nombre d'étapes
                        Cell nbCell = row.createCell(3);
                        nbCell.setCellValue(nbEtapes);
                        nbCell.setCellStyle(createNumberCellStyle(workbook));

                        // Durée totale
                        float dureeItineraire = 0;
                        if (etapes != null) {
                            dureeItineraire = etapes.stream()
                                    .map(e -> e.getDureeActivite() != null ? e.getDureeActivite() : 0f)
                                    .reduce(0f, Float::sum);
                            totalDuree += dureeItineraire;
                        }

                        Cell dureeCell = row.createCell(4);
                        dureeCell.setCellValue(dureeItineraire);
                        dureeCell.setCellStyle(createNumberCellStyle(workbook));

                        // Statut
                        Cell statutCell = row.createCell(5);
                        statutCell.setCellValue(nbEtapes > 0 ? "Actif" : "Sans étape");
                        statutCell.setCellStyle(cellStyle);

                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }

                rowNum++; // Espace

                // Totaux
                Row totalRow = summarySheet.createRow(rowNum++);
                Cell totalLabelCell = totalRow.createCell(2);
                totalLabelCell.setCellValue("TOTAUX :");
                totalLabelCell.setCellStyle(subHeaderStyle);

                Cell totalEtapesCell = totalRow.createCell(3);
                totalEtapesCell.setCellValue(totalEtapes);
                totalEtapesCell.setCellStyle(createHighlightStyle(workbook));

                Cell totalDureeCell = totalRow.createCell(4);
                totalDureeCell.setCellValue(totalDuree);
                totalDureeCell.setCellStyle(createHighlightStyle(workbook));

                // Ajuster la largeur des colonnes
                for (int i = 0; i < 6; i++) {
                    summarySheet.autoSizeColumn(i);
                }

                // Créer une feuille détaillée pour chaque itinéraire
                for (Itineraire itineraire : itineraires) {
                    try {
                        List<etape> etapes = etapeCRUD.getEtapesByItineraire(itineraire.getId_itineraire());
                        String sheetName = itineraire.getNom_itineraire();
                        if (sheetName.length() > 31) {
                            sheetName = sheetName.substring(0, 28) + "...";
                        }
                        Sheet detailSheet = workbook.createSheet(sheetName);
                        createDetailSheet(detailSheet, itineraire, etapes, workbook);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }

                // Sauvegarder le fichier
                try (FileOutputStream fileOut = new FileOutputStream(file)) {
                    workbook.write(fileOut);
                }

                AlertUtil.showInfo("Export réussi", "Tous les itinéraires ont été exportés avec succès vers :\n" + file.getAbsolutePath());

                // Ouvrir automatiquement le fichier
                openFile(file);

            } catch (Exception e) {
                e.printStackTrace();
                AlertUtil.showError("Erreur d'export", "Impossible d'exporter les itinéraires : " + e.getMessage());
            }
        }
    }

    private static void createDetailSheet(Sheet sheet, Itineraire itineraire, List<etape> etapes, Workbook workbook) {
        CellStyle titleStyle = createTitleStyle(workbook);
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle cellStyle = createCellStyle(workbook);
        CellStyle dateCellStyle = createDateCellStyle(workbook);

        int rowNum = 0;

        // Titre
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("DÉTAIL : " + itineraire.getNom_itineraire().toUpperCase());
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 0, 4));

        rowNum++; // Espace

        if (etapes != null && !etapes.isEmpty()) {
            // En-têtes
            String[] columns = {"Heure", "Activité", "Lieu", "Durée (h)", "Description"};
            Row headerRow = sheet.createRow(rowNum++);
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(createColoredHeaderStyle(workbook, i));
            }

            // Données
            for (etape e : etapes) {
                Row row = sheet.createRow(rowNum++);

                Cell heureCell = row.createCell(0);
                heureCell.setCellValue(e.getHeure() != null ? e.getHeure().toString() : "--:--");
                heureCell.setCellStyle(dateCellStyle);

                Cell activiteCell = row.createCell(1);
                activiteCell.setCellValue(e.getNomActivite() != null ? e.getNomActivite() : "Inconnue");
                activiteCell.setCellStyle(cellStyle);

                Cell lieuCell = row.createCell(2);
                lieuCell.setCellValue(e.getLieuActivite() != null ? e.getLieuActivite() : "Non défini");
                lieuCell.setCellStyle(cellStyle);

                Cell dureeCell = row.createCell(3);
                dureeCell.setCellValue(e.getDureeActivite() != null ? e.getDureeActivite() : 0f);
                dureeCell.setCellStyle(createNumberCellStyle(workbook));

                Cell descCell = row.createCell(4);
                descCell.setCellValue(e.getDescription_etape() != null ? e.getDescription_etape() : "");
                descCell.setCellStyle(cellStyle);
            }

            // Ajuster la largeur des colonnes
            for (int i = 0; i < 5; i++) {
                sheet.autoSizeColumn(i);
            }
        }
    }

    // ==================== MÉTHODES D'EXPORT CSV (ANCIENNES) ====================

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

    // ==================== MÉTHODES DE STYLE POUR EXCEL ====================

    private static CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 16);
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.ORANGE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private static CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 14);
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private static CellStyle createSubHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 11);
        font.setBold(true);
        font.setColor(IndexedColors.BLACK.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private static CellStyle createCellStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setWrapText(true);
        return style;
    }

    private static CellStyle createDateCellStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        CreationHelper createHelper = workbook.getCreationHelper();
        style.setDataFormat(createHelper.createDataFormat().getFormat("HH:mm"));
        return style;
    }

    private static CellStyle createNumberCellStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.RIGHT);
        CreationHelper createHelper = workbook.getCreationHelper();
        style.setDataFormat(createHelper.createDataFormat().getFormat("#,##0.00"));
        return style;
    }

    private static CellStyle createHighlightStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 12);
        font.setBold(true);
        font.setColor(IndexedColors.RED.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private static CellStyle createWarningStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 11);
        font.setItalic(true);
        font.setColor(IndexedColors.DARK_RED.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LIGHT_ORANGE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private static CellStyle createColoredHeaderStyle(Workbook workbook, int colorIndex) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 11);
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);

        // Couleurs alternées pour les en-têtes
        short[] colors = {
                IndexedColors.LIGHT_ORANGE.getIndex(),
                IndexedColors.ORANGE.getIndex(),
                IndexedColors.GREEN.getIndex(),
                IndexedColors.BLUE.getIndex(),
                IndexedColors.VIOLET.getIndex(),
                IndexedColors.INDIGO.getIndex()
        };

        style.setFillForegroundColor(colors[colorIndex % colors.length]);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.MEDIUM);
        style.setBorderTop(BorderStyle.MEDIUM);
        style.setBorderLeft(BorderStyle.MEDIUM);
        style.setBorderRight(BorderStyle.MEDIUM);
        return style;
    }

    private static void openFile(File file) {
        try {
            java.awt.Desktop.getDesktop().open(file);
        } catch (Exception e) {
            // Silently fail if cannot open
        }
    }
}