package Services;

import Entities.Voyage;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.LineSeparator;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import javafx.collections.ObservableList;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ExportService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ==================== COULEURS TRAVELMATE ====================
    private static final Color DARK_BG         = new DeviceRgb(10, 14, 39);      // #0a0e27
    private static final Color CARD_BG         = new DeviceRgb(30, 39, 73);      // #1e2749
    private static final Color SIDEBAR_BG      = new DeviceRgb(17, 22, 51);      // #111633
    private static final Color ORANGE_PRIMARY  = new DeviceRgb(255, 140, 66);    // #ff8c42
    private static final Color GREEN_SUCCESS   = new DeviceRgb(16, 185, 129);    // #10b981
    private static final Color PINK_ACCENT     = new DeviceRgb(236, 72, 153);    // #ec4899
    private static final Color BLUE_INFO       = new DeviceRgb(59, 130, 246);    // #3b82f6
    private static final Color GRAY_MUTED      = new DeviceRgb(148, 163, 184);   // #94a3b8
    private static final Color GRAY_COMPLETED  = new DeviceRgb(100, 116, 139);   // #64748b
    private static final Color WHITE           = new DeviceRgb(255, 255, 255);   // #ffffff
    private static final Color ROW_EVEN        = new DeviceRgb(13, 17, 45);      // slightly lighter than #0a0e27
    private static final Color ROW_ODD         = new DeviceRgb(20, 26, 58);      // between dark and sidebar

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
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
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

        // Ouvrir le fichier automatiquement dans l'application par défaut
        openFileInDefaultApp(file);
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

        VoyageCRUDV voyageCRUD = new VoyageCRUDV();

        try (PdfWriter writer = new PdfWriter(file.getAbsolutePath());
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf, PageSize.A4)) {

            document.setMargins(0, 0, 0, 0);

            // ==================== HEADER BANNER (orange) ====================
            Table headerBanner = new Table(UnitValue.createPercentArray(1)).useAllAvailableWidth();
            Cell bannerCell = new Cell()
                    .setBackgroundColor(ORANGE_PRIMARY)
                    .setPadding(30)
                    .setBorder(Border.NO_BORDER);

            bannerCell.add(new Paragraph("✈  TravelMate")
                    .setFontColor(WHITE)
                    .setFontSize(28)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(4));

            bannerCell.add(new Paragraph("Votre compagnon de voyage")
                    .setFontColor(new DeviceRgb(255, 255, 255))
                    .setFontSize(11)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setOpacity(0.85f));

            headerBanner.addCell(bannerCell);
            document.add(headerBanner);

            // ==================== TITLE BAR (dark) ====================
            Table titleBar = new Table(UnitValue.createPercentArray(1)).useAllAvailableWidth();
            Cell titleCell = new Cell()
                    .setBackgroundColor(DARK_BG)
                    .setPadding(20)
                    .setPaddingLeft(40)
                    .setPaddingRight(40)
                    .setBorder(Border.NO_BORDER);

            titleCell.add(new Paragraph(titrePage)
                    .setFontColor(WHITE)
                    .setFontSize(18)
                    .setBold()
                    .setTextAlignment(TextAlignment.LEFT)
                    .setMarginBottom(6));

            titleCell.add(new Paragraph("Exporté le " + LocalDate.now().format(DATE_FORMATTER) + "  |  " + voyages.size() + " voyages")
                    .setFontColor(GRAY_MUTED)
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.LEFT));

            titleBar.addCell(titleCell);
            document.add(titleBar);

            // ==================== STATISTICS RIBBON ====================
            long aVenir = voyages.stream().filter(v -> "a venir".equalsIgnoreCase(v.getStatut())).count();
            long enCours = voyages.stream().filter(v -> "en cours".equalsIgnoreCase(v.getStatut())).count();
            long termines = voyages.stream().filter(v -> "terminé".equalsIgnoreCase(v.getStatut()) || "termine".equalsIgnoreCase(v.getStatut())).count();
            long annules = voyages.stream().filter(v -> "annulé".equalsIgnoreCase(v.getStatut()) || "annule".equalsIgnoreCase(v.getStatut())).count();

            Table statsBar = new Table(UnitValue.createPercentArray(new float[]{25, 25, 25, 25})).useAllAvailableWidth();

            statsBar.addCell(buildStatCell("À venir", String.valueOf(aVenir), BLUE_INFO));
            statsBar.addCell(buildStatCell("En cours", String.valueOf(enCours), ORANGE_PRIMARY));
            statsBar.addCell(buildStatCell("Terminés", String.valueOf(termines), GREEN_SUCCESS));
            statsBar.addCell(buildStatCell("Annulés", String.valueOf(annules), PINK_ACCENT));

            document.add(statsBar);

            // ==================== SPACER ====================
            Table spacer = new Table(UnitValue.createPercentArray(1)).useAllAvailableWidth();
            spacer.addCell(new Cell().setBackgroundColor(DARK_BG).setHeight(15).setBorder(Border.NO_BORDER));
            document.add(spacer);

            // ==================== DATA TABLE ====================
            Table table = new Table(UnitValue.createPercentArray(new float[]{6, 22, 16, 14, 14, 14, 14})).useAllAvailableWidth();

            // Table Header
            String[] headers = {"#", "Titre du voyage", "Destination", "Date Début", "Date Fin", "Statut", "Durée"};
            for (String header : headers) {
                Cell headerCell = new Cell()
                        .setBackgroundColor(CARD_BG)
                        .setBorder(new SolidBorder(new DeviceRgb(40, 50, 90), 0.5f))
                        .setPadding(10)
                        .setPaddingLeft(12)
                        .setPaddingRight(12);
                headerCell.add(new Paragraph(header)
                        .setFontColor(ORANGE_PRIMARY)
                        .setFontSize(9)
                        .setBold()
                        .setTextAlignment(TextAlignment.CENTER));
                table.addHeaderCell(headerCell);
            }

            // Table Rows
            int index = 0;
            for (Voyage v : voyages) {
                Color rowBg = (index % 2 == 0) ? ROW_EVEN : ROW_ODD;
                Border cellBorder = new SolidBorder(new DeviceRgb(30, 38, 70), 0.3f);

                // # column
                table.addCell(buildDataCell(String.valueOf(index + 1), rowBg, cellBorder, GRAY_MUTED, TextAlignment.CENTER));

                // Titre
                table.addCell(buildDataCell(v.getTitre_voyage(), rowBg, cellBorder, WHITE, TextAlignment.LEFT));

                // Destination — resolve name from DB
                String destName;
                try {
                    destName = voyageCRUD.getNomDestination(v.getId_destination());
                    if (destName == null || destName.isEmpty()) destName = "ID: " + v.getId_destination();
                } catch (SQLException e) {
                    destName = "ID: " + v.getId_destination();
                }
                table.addCell(buildDataCell(destName, rowBg, cellBorder, WHITE, TextAlignment.CENTER));

                // Date Début
                String dateDebut = v.getDate_debut() != null ? v.getDate_debut().toLocalDate().format(DATE_FORMATTER) : "—";
                table.addCell(buildDataCell(dateDebut, rowBg, cellBorder, GRAY_MUTED, TextAlignment.CENTER));

                // Date Fin
                String dateFin = v.getDate_fin() != null ? v.getDate_fin().toLocalDate().format(DATE_FORMATTER) : "—";
                table.addCell(buildDataCell(dateFin, rowBg, cellBorder, GRAY_MUTED, TextAlignment.CENTER));

                // Statut — colored badge
                table.addCell(buildStatutCell(v.getStatut(), rowBg, cellBorder));

                // Durée
                String duree = "—";
                if (v.getDate_debut() != null && v.getDate_fin() != null) {
                    long days = java.time.temporal.ChronoUnit.DAYS.between(
                            v.getDate_debut().toLocalDate(), v.getDate_fin().toLocalDate());
                    duree = days + " jour" + (days > 1 ? "s" : "");
                }
                table.addCell(buildDataCell(duree, rowBg, cellBorder, GRAY_MUTED, TextAlignment.CENTER));

                index++;
            }

            document.add(table);

            // ==================== FOOTER ====================
            Table footer = new Table(UnitValue.createPercentArray(1)).useAllAvailableWidth();
            Cell footerCell = new Cell()
                    .setBackgroundColor(DARK_BG)
                    .setPadding(20)
                    .setPaddingLeft(40)
                    .setPaddingRight(40)
                    .setBorder(Border.NO_BORDER);

            // Orange separator line
            SolidLine orangeLine = new SolidLine(2f);
            orangeLine.setColor(ORANGE_PRIMARY);
            footerCell.add(new LineSeparator(orangeLine).setMarginBottom(14));

            footerCell.add(new Paragraph("Récapitulatif")
                    .setFontColor(WHITE)
                    .setFontSize(13)
                    .setBold()
                    .setMarginBottom(8));

            footerCell.add(new Paragraph("Total : " + voyages.size() + " voyages  •  " +
                    aVenir + " à venir  •  " + enCours + " en cours  •  " +
                    termines + " terminés  •  " + annules + " annulés")
                    .setFontColor(GRAY_MUTED)
                    .setFontSize(9)
                    .setMarginBottom(16));

            SolidLine grayLine = new SolidLine(0.5f);
            grayLine.setColor(GRAY_COMPLETED);
            footerCell.add(new LineSeparator(grayLine).setMarginBottom(10));

            footerCell.add(new Paragraph("© 2025 TravelMate — Document généré automatiquement")
                    .setFontColor(GRAY_COMPLETED)
                    .setFontSize(8)
                    .setTextAlignment(TextAlignment.CENTER));

            footer.addCell(footerCell);
            document.add(footer);
        }

        // Ouvrir le fichier automatiquement dans l'application par défaut
        openFileInDefaultApp(file);
    }

    // ==================== HELPER METHODS FOR PDF ====================

    /**
     * Builds a colored stat cell for the statistics ribbon.
     */
    private static Cell buildStatCell(String label, String value, Color accentColor) {
        Cell cell = new Cell()
                .setBackgroundColor(SIDEBAR_BG)
                .setBorder(new SolidBorder(new DeviceRgb(30, 38, 70), 0.5f))
                .setPadding(14)
                .setTextAlignment(TextAlignment.CENTER);

        cell.add(new Paragraph(value)
                .setFontColor(accentColor)
                .setFontSize(22)
                .setBold()
                .setMarginBottom(2));

        cell.add(new Paragraph(label)
                .setFontColor(GRAY_MUTED)
                .setFontSize(9));

        return cell;
    }

    /**
     * Builds a standard data cell for the table.
     */
    private static Cell buildDataCell(String text, Color bgColor, Border border, Color fontColor, TextAlignment align) {
        Cell cell = new Cell()
                .setBackgroundColor(bgColor)
                .setBorder(border)
                .setPadding(8)
                .setPaddingLeft(10)
                .setPaddingRight(10)
                .setVerticalAlignment(VerticalAlignment.MIDDLE);

        cell.add(new Paragraph(text != null ? text : "—")
                .setFontColor(fontColor)
                .setFontSize(9)
                .setTextAlignment(align));

        return cell;
    }

    /**
     * Builds a colored statut badge cell for the table.
     */
    private static Cell buildStatutCell(String statut, Color rowBg, Border border) {
        Color badgeColor;
        String displayText;

        if (statut == null) statut = "";
        switch (statut.toLowerCase()) {
            case "en cours":
                badgeColor = ORANGE_PRIMARY;
                displayText = "● En cours";
                break;
            case "terminé":
            case "termine":
                badgeColor = GREEN_SUCCESS;
                displayText = "✓ Terminé";
                break;
            case "annulé":
            case "annule":
                badgeColor = PINK_ACCENT;
                displayText = "✕ Annulé";
                break;
            case "a venir":
            default:
                badgeColor = BLUE_INFO;
                displayText = "◷ À venir";
                break;
        }

        Cell cell = new Cell()
                .setBackgroundColor(rowBg)
                .setBorder(border)
                .setPadding(8)
                .setVerticalAlignment(VerticalAlignment.MIDDLE);

        cell.add(new Paragraph(displayText)
                .setFontColor(badgeColor)
                .setFontSize(9)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER));

        return cell;
    }

    /**
     * Ouvre un fichier dans l'application par défaut du système (navigateur pour PDF, Excel pour .xlsx)
     */
    private static void openFileInDefaultApp(File file) {
        if (file != null && file.exists()) {
            try {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(file);
                } else {
                    // Fallback pour Windows si Desktop n'est pas supporté
                    Runtime.getRuntime().exec(new String[]{"cmd", "/c", "start", "", file.getAbsolutePath()});
                }
            } catch (IOException e) {
                System.err.println("Impossible d'ouvrir le fichier: " + e.getMessage());
            }
        }
    }
}