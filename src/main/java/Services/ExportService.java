package Services;

import Entities.Voyage;
import javafx.collections.ObservableList;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

// Imports pour Apache POI (Excel)
import org.apache.poi.ss.usermodel.*;

// Imports pour iText (PDF) - en utilisant des noms complets dans le code
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ExportService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static void exportToExcel(ObservableList<Voyage> voyages, Stage stage) throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sauvegarder le fichier Excel");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichier Excel", "*.xlsx")
        );
        fileChooser.setInitialFileName("voyages_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx");

        File file = fileChooser.showSaveDialog(stage);
        if (file == null) return;

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Voyages");

            // Style pour l'en-tête
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.ORANGE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Créer l'en-tête
            String[] columns = {"ID", "Titre", "Date Début", "Date Fin", "Statut", "ID Destination"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            // Remplir les données
            int rowNum = 1;
            for (Voyage v : voyages) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(v.getId_voyage());
                row.createCell(1).setCellValue(v.getTitre_voyage());
                row.createCell(2).setCellValue(v.getDate_debut().toLocalDate().format(DATE_FORMATTER));
                row.createCell(3).setCellValue(v.getDate_fin().toLocalDate().format(DATE_FORMATTER));
                row.createCell(4).setCellValue(v.getStatut());
                row.createCell(5).setCellValue(v.getId_destination());
            }

            // Ajuster la largeur des colonnes
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Écrire dans le fichier
            try (FileOutputStream fileOut = new FileOutputStream(file)) {
                workbook.write(fileOut);
            }
        }
    }

    public static void exportToPDF(ObservableList<Voyage> voyages, Stage stage, String titrePage) throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sauvegarder le fichier PDF");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichier PDF", "*.pdf")
        );
        fileChooser.setInitialFileName("voyages_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".pdf");

        File file = fileChooser.showSaveDialog(stage);
        if (file == null) return;

        try (com.itextpdf.kernel.pdf.PdfWriter writer = new com.itextpdf.kernel.pdf.PdfWriter(file.getAbsolutePath());
             com.itextpdf.kernel.pdf.PdfDocument pdf = new com.itextpdf.kernel.pdf.PdfDocument(writer);
             com.itextpdf.layout.Document document = new com.itextpdf.layout.Document(pdf)) {

            // Titre
            com.itextpdf.layout.element.Paragraph title = new com.itextpdf.layout.element.Paragraph(titrePage)
                    .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER)
                    .setBold()
                    .setFontSize(18)
                    .setMarginBottom(20);
            document.add(title);

            // Date d'export
            com.itextpdf.layout.element.Paragraph dateExport = new com.itextpdf.layout.element.Paragraph("Exporté le : " + LocalDate.now().format(DATE_FORMATTER))
                    .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.RIGHT)
                    .setFontSize(8)
                    .setMarginBottom(20);
            document.add(dateExport);

            // Créer le tableau
            com.itextpdf.layout.element.Table table = new com.itextpdf.layout.element.Table(
                    com.itextpdf.layout.properties.UnitValue.createPercentArray(new float[]{5, 25, 15, 15, 15, 10})
            );
            table.setWidth(com.itextpdf.layout.properties.UnitValue.createPercentValue(100));

            // En-têtes
            String[] headers = {"ID", "Titre", "Date Début", "Date Fin", "Statut", "ID Dest"};
            for (String header : headers) {
                table.addHeaderCell(
                        new com.itextpdf.layout.element.Cell()
                                .add(new com.itextpdf.layout.element.Paragraph(header))
                                .setBold()
                                .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER)
                );
            }

            // Données
            for (Voyage v : voyages) {
                table.addCell(
                        new com.itextpdf.layout.element.Cell()
                                .add(new com.itextpdf.layout.element.Paragraph(String.valueOf(v.getId_voyage())))
                                .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER)
                );
                table.addCell(
                        new com.itextpdf.layout.element.Cell()
                                .add(new com.itextpdf.layout.element.Paragraph(v.getTitre_voyage()))
                );
                table.addCell(
                        new com.itextpdf.layout.element.Cell()
                                .add(new com.itextpdf.layout.element.Paragraph(v.getDate_debut().toLocalDate().format(DATE_FORMATTER)))
                                .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER)
                );
                table.addCell(
                        new com.itextpdf.layout.element.Cell()
                                .add(new com.itextpdf.layout.element.Paragraph(v.getDate_fin().toLocalDate().format(DATE_FORMATTER)))
                                .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER)
                );
                table.addCell(
                        new com.itextpdf.layout.element.Cell()
                                .add(new com.itextpdf.layout.element.Paragraph(v.getStatut()))
                                .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER)
                );
                table.addCell(
                        new com.itextpdf.layout.element.Cell()
                                .add(new com.itextpdf.layout.element.Paragraph(String.valueOf(v.getId_destination())))
                                .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER)
                );
            }

            document.add(table);

            // Résumé
            com.itextpdf.layout.element.Paragraph summary = new com.itextpdf.layout.element.Paragraph("\n\nRésumé : Total " + voyages.size() + " voyages")
                    .setMarginTop(20)
                    .setItalic();
            document.add(summary);
        }
    }
}