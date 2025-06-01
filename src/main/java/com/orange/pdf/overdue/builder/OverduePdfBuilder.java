package com.orange.pdf.overdue.builder;

import com.orange.pdf.builder.PdfBuilder;
import com.orange.pdf.builder.data.GenreSummary;
import com.orange.pdf.builder.data.PublisherSummary;
import com.orange.pdf.enums.PdfType;
import com.orange.pdf.exception.PDPageContentStreamException;
import com.orange.pdf.overdue.data.OverdueCategorySummary;
import com.orange.pdf.overdue.data.OverduePdfTableItem;
import lombok.Getter;
import org.apache.pdfbox.pdmodel.PDPageContentStream;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Builder do tworzenia raportów zalegających użytkowników PDF
 * Rozszerza PdfBuilder i używa jego funkcjonalności
 */
@Getter
public class OverduePdfBuilder extends PdfBuilder {

    private static final String REPORT_TITLE = "Raport zalegających użytkowników";
    private static final String REPORT_AUTHOR = "System zarządzania biblioteką";
    private static final float SECTION_SPACING = 25f; // Standardowy odstęp między sekcjami

    private OverduePdfBuilder(PdfType pdfType) {
        super(pdfType, REPORT_TITLE, REPORT_AUTHOR);
    }

    /**
     * Tworzy nową instancję buildera do raportu zalegających użytkowników
     */
    public static OverduePdfBuilder createOverdueReport() {
        return new OverduePdfBuilder(PdfType.A4);
    }

    /**
     * Buduje raport zalegających użytkowników
     */
    public OverduePdfBuilder buildOverdueReport(
            String libraryName,
            String libraryDesc,
            String address,
            String city,
            String reportNumber,
            LocalDate reportDate,
            List<OverduePdfTableItem> overdueLoans,
            List<OverdueCategorySummary> categorySummaries,
            List<GenreSummary> genreSummaries,
            List<PublisherSummary> publisherSummaries,
            String generatedBy) {

        try {
            float tableWidth = getWidth();
            float headerHeight = 120f;
            float leftWidth = tableWidth * 0.5f;
            float rightWidth = tableWidth - leftWidth;
            float margin = getMargin();
            float pageHeight = getHeight();
            float minBottomMargin = 120f;

            float currentY = getStartY();
            float rightStartX = margin + leftWidth;

            // Rysuj nagłówek używając metody z PdfBuilder
            drawOverdueReportHeader(libraryName, libraryDesc, address, city, reportNumber,
                    reportDate, rightStartX, currentY, headerHeight, leftWidth, rightWidth);

            currentY = currentY - headerHeight - SECTION_SPACING;

            // Tabela zalegających z wielostronicowością
            currentY = drawOverdueTable(overdueLoans, margin, currentY, tableWidth, pageHeight,
                    minBottomMargin, libraryName, reportNumber, reportDate);

            // Standardowy odstęp po tabeli głównej
            currentY -= SECTION_SPACING;

            // Sekcje podsumowań ze standardowymi odstępami
            if (categorySummaries != null && !categorySummaries.isEmpty()) {
                final float finalCurrentY1 = currentY;
                currentY = drawSectionIfNeeded(currentY, minBottomMargin,
                        () -> drawOverdueCategorySummary(categorySummaries, margin, finalCurrentY1, tableWidth),
                        () -> calculateSummaryHeight(categorySummaries.size()),
                        true);
            }

            if (genreSummaries != null && !genreSummaries.isEmpty()) {
                final float finalCurrentY2 = currentY;
                currentY = drawSectionIfNeeded(currentY, minBottomMargin,
                        () -> drawGenreSummarySection(genreSummaries, margin, finalCurrentY2, tableWidth),
                        () -> calculateSummaryHeight(genreSummaries.size()),
                        true);
            }

            if (publisherSummaries != null && !publisherSummaries.isEmpty()) {
                final float finalCurrentY3 = currentY;
                currentY = drawSectionIfNeeded(currentY, minBottomMargin,
                        () -> drawPublisherSummarySection(publisherSummaries, margin, finalCurrentY3, tableWidth),
                        () -> calculateSummaryHeight(publisherSummaries.size()),
                        true);
            }

            // Podpis na samym końcu z większym marginesem
            float signatureHeight = 80f;
            float minSpaceForSignature = 150f; // Większy margines dla podpisu

            // Sprawdź czy jest wystarczająco miejsca na podpis
            if (currentY - signatureHeight < minSpaceForSignature) {
                addNewPage();
                currentY = getStartY(); // Zacznij od samej góry nowej strony
            }

            // Rysuj podpis
            drawOverdueSignatureSection(margin, currentY - signatureHeight, tableWidth, generatedBy, reportDate);

        } catch (IOException e) {
            safeEndText(getContentStream());
            throw new PDPageContentStreamException("Nie udało się zbudować raportu zalegających: " + e.getMessage());
        }

        return this;
    }

    /**
     * Rysuje tabelę zalegających z obsługą wielu stron
     */
    private float drawOverdueTable(List<OverduePdfTableItem> loans, float x, float y, float tableWidth,
                                   float pageHeight, float minBottomMargin, String libraryName,
                                   String reportNumber, LocalDate reportDate) throws IOException {

        float rowHeight = 25f;
        float currentY = y;
        int rowIndex = 0;

        // Szerokości kolumn
        float[] colWidths = {20f, 45f, 100f, 80f, 70f, 120f, 50f, 20f};
        colWidths[7] = tableWidth - (colWidths[0] + colWidths[1] + colWidths[2] + colWidths[3] + colWidths[4] + colWidths[5] + colWidths[6]);

        // Nagłówek na pierwszej stronie
        drawOverdueTableHeader(x, currentY, tableWidth, rowHeight, colWidths);
        currentY -= rowHeight;

        // Wiersze z danymi
        while (rowIndex < loans.size()) {
            if (currentY - rowHeight < minBottomMargin) {
                addNewPage();
                currentY = getStartY(); // Od samej góry nowej strony
                drawSimpleOverdueHeader(libraryName, "Kontynuacja - strona " + getDocument().getNumberOfPages(),
                        reportNumber, reportDate, getMargin(), currentY, 50f, tableWidth);
                currentY -= 70f;
                drawOverdueTableHeader(x, currentY, tableWidth, rowHeight, colWidths);
                currentY -= rowHeight;
            }

            drawOverdueRow(loans.get(rowIndex), rowIndex + 1, x, currentY, colWidths);
            currentY -= rowHeight;
            rowIndex++;
        }

        return currentY;
    }

    /**
     * Rysuje sekcję jeśli jest miejsce, inaczej dodaje nową stronę
     */
    private float drawSectionIfNeeded(float currentY, float minBottomMargin,
                                      SectionDrawer drawer, HeightCalculator heightCalc, boolean hasData) throws IOException {
        if (!hasData) {
            return currentY; // Nie rysuj pustej sekcji
        }

        float sectionHeight = heightCalc.calculate();

        // Sprawdź czy jest miejsce
        if (currentY - sectionHeight - SECTION_SPACING < minBottomMargin) {
            addNewPage();
            currentY = getStartY(); // Od samej góry nowej strony
        }

        drawer.draw();
        return currentY - sectionHeight - SECTION_SPACING; // Standardowy odstęp po sekcji
    }

    /**
     * Rysuje podsumowanie kategorii zaległości
     */
    private void drawOverdueCategorySummary(List<OverdueCategorySummary> summaries, float x, float y, float tableWidth) throws IOException {
        if (summaries == null || summaries.isEmpty()) return;

        float currentY = y;
        drawSectionHeader("Podsumowanie zaległości:", x, currentY);
        currentY -= 20f;

        String[] headers = {"Kategoria", "Liczba", "Łączne dni", "Średnia"};
        float[] colWidths = {120f, 80f, 100f, tableWidth - 300f};

        drawTableFrame(x, currentY, tableWidth, 25f, colWidths);
        drawRowHeaders(headers, x, currentY, colWidths);
        currentY -= 25f;

        // Wiersze danych
        for (OverdueCategorySummary summary : summaries) {
            String[] rowData = {
                    summary.getCategory(),
                    String.valueOf(summary.getCount()),
                    String.valueOf(summary.getTotalOverdueDays()),
                    String.format("%.1f", summary.getAverageOverdueDays())
            };
            drawTableFrame(x, currentY, tableWidth, 25f, colWidths);
            drawRowData(rowData, x, currentY, colWidths);
            currentY -= 25f;
        }

        // Wiersz podsumowania
        int totalCount = summaries.stream().mapToInt(OverdueCategorySummary::getCount).sum();
        long totalDays = summaries.stream().mapToLong(OverdueCategorySummary::getTotalOverdueDays).sum();
        double avgDays = totalCount > 0 ? (double) totalDays / totalCount : 0.0;

        String[] totalRow = {"RAZEM", String.valueOf(totalCount), String.valueOf(totalDays), String.format("%.1f", avgDays)};
        drawTableFrame(x, currentY, tableWidth, 25f, colWidths);
        drawRowData(totalRow, x, currentY, colWidths, true);
    }

    private void drawGenreSummarySection(List<GenreSummary> summaries, float x, float y, float tableWidth) throws IOException {
        if (summaries == null || summaries.isEmpty()) return;

        float currentY = y;
        drawSectionHeader("Podsumowanie gatunków:", x, currentY);
        currentY -= 20f;

        drawSimpleSummaryTable(summaries, x, currentY, tableWidth,
                s -> s.getGenre(), s -> s.getCount());
    }

    private void drawPublisherSummarySection(List<PublisherSummary> summaries, float x, float y, float tableWidth) throws IOException {
        if (summaries == null || summaries.isEmpty()) return;

        float currentY = y;
        drawSectionHeader("Podsumowanie wydawców:", x, currentY);
        currentY -= 20f;

        drawSimpleSummaryTable(summaries, x, currentY, tableWidth,
                s -> s.getPublisher(), s -> s.getCount());
    }

    /**
     * Rysuje nagłówek tabeli zalegających
     */
    private void drawOverdueTableHeader(float x, float y, float tableWidth, float rowHeight, float[] colWidths) throws IOException {
        String[] headers = {"Lp.", "ID wyp.", "Tytuł", "Autor", "Użytkownik", "Email", "Termin", "Dni zaleg."};

        // Ramka nagłówka
        drawTableFrame(x, y, tableWidth, rowHeight, colWidths);

        // Teksty nagłówków
        float currentX = x;
        for (int i = 0; i < headers.length; i++) {
            drawCellText(headers[i], currentX + 5, y - 15, getBoldFont(), 8);
            currentX += colWidths[i];
        }
    }

    /**
     * Rysuje wiersz z danymi zalegającego
     */
    private void drawOverdueRow(OverduePdfTableItem loan, int rowNum, float x, float y, float[] colWidths) throws IOException {
        // Ramka wiersza
        drawTableFrame(x, y, getWidth(), 25f, colWidths);

        // Dane wiersza
        String[] rowData = {
                String.valueOf(rowNum),
                loan.getLoanId(),
                truncateText(loan.getTitle(), 20),
                truncateText(loan.getAuthors(), 15),
                truncateText(loan.getUserName(), 12),
                truncateText(loan.getUserEmail() != null ? loan.getUserEmail() : "", 30),
                loan.getDueDate().atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                        .format(DateTimeFormatter.ofPattern("MM-dd")),
                String.valueOf(loan.getOverdueDays())
        };

        float currentX = x;
        for (int i = 0; i < rowData.length; i++) {
            // Wyróżnij duże zaległości
            boolean isBold = i == rowData.length - 1 && loan.getOverdueDays() > 30;
            drawCellText(rowData[i], currentX + 5, y - 15,
                    isBold ? getBoldFont() : getRegularFont(), 8);
            currentX += colWidths[i];
        }
    }

    /**
     * Rysuje ramkę tabeli
     */
    private void drawTableFrame(float x, float y, float tableWidth, float rowHeight, float[] colWidths) throws IOException {
        // Linie poziome
        drawLine(x, y, x + tableWidth, y);
        drawLine(x, y - rowHeight, x + tableWidth, y - rowHeight);

        // Linie pionowe
        float currentX = x;
        drawLine(currentX, y, currentX, y - rowHeight);
        for (float width : colWidths) {
            currentX += width;
            drawLine(currentX, y, currentX, y - rowHeight);
        }
    }

    /**
     * Rysuje tekst w komórce
     */
    private void drawCellText(String text, float x, float y, org.apache.pdfbox.pdmodel.font.PDFont font, int fontSize) throws IOException {
        PDPageContentStream contentStream = getContentStream();
        contentStream.beginText();
        contentStream.setFont(font, fontSize);
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(text);
        contentStream.endText();
    }

    /**
     * Pomocnicze metody do rysowania
     */
    private void drawSectionHeader(String title, float x, float y) throws IOException {
        drawCellText(title, x, y, getBoldFont(), 10);
    }

    private void drawRowHeaders(String[] headers, float x, float y, float[] colWidths) throws IOException {
        float currentX = x;
        for (int i = 0; i < headers.length; i++) {
            drawCellText(headers[i], currentX + 5, y - 15, getBoldFont(), 8);
            currentX += colWidths[i];
        }
    }

    private void drawRowData(String[] data, float x, float y, float[] colWidths) throws IOException {
        drawRowData(data, x, y, colWidths, false);
    }

    private void drawRowData(String[] data, float x, float y, float[] colWidths, boolean bold) throws IOException {
        float currentX = x;
        for (int i = 0; i < data.length; i++) {
            drawCellText(data[i], currentX + 5, y - 15,
                    bold ? getBoldFont() : getRegularFont(), 8);
            currentX += colWidths[i];
        }
    }

    private <T> void drawSimpleSummaryTable(List<T> summaries, float x, float y, float tableWidth,
                                            java.util.function.Function<T, String> nameExtractor,
                                            java.util.function.Function<T, Integer> countExtractor) throws IOException {
        float[] colWidths = {200f, tableWidth - 200f};
        String[] headers = {"Nazwa", "Ilość"};

        float currentY = y;
        drawTableFrame(x, currentY, tableWidth, 25f, colWidths);
        drawRowHeaders(headers, x, currentY, colWidths);
        currentY -= 25f;

        for (T summary : summaries) {
            String[] rowData = {nameExtractor.apply(summary), String.valueOf(countExtractor.apply(summary))};
            drawTableFrame(x, currentY, tableWidth, 25f, colWidths);
            drawRowData(rowData, x, currentY, colWidths);
            currentY -= 25f;
        }

        // Suma
        int total = summaries.stream().mapToInt(countExtractor::apply).sum();
        String[] totalRow = {"RAZEM", String.valueOf(total)};
        drawTableFrame(x, currentY, tableWidth, 25f, colWidths);
        drawRowData(totalRow, x, currentY, colWidths, true);
    }

    /**
     * Pomocnicze metody
     */
    private float calculateSummaryHeight(int itemCount) {
        if (itemCount == 0) return 0f;
        return 20f + (itemCount + 2) * 25f; // nagłówek + elementy + suma + odstęp na nagłówek sekcji
    }

    private String truncateText(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) return text != null ? text : "";
        return text.substring(0, maxLength - 3) + "...";
    }

    private void drawOverdueReportHeader(String libraryName, String libraryDesc, String address, String city,
                                         String reportNumber, LocalDate reportDate, float rightStartX, float currentY,
                                         float headerHeight, float leftWidth, float rightWidth) throws IOException {
        // Wykorzystuj metodę z PdfBuilder ale dostosuj tytuł
        float margin = getMargin();
        float tableWidth = getWidth();
        PDPageContentStream contentStream = getContentStream();

        // Ramka
        drawLine(margin, currentY, margin + tableWidth, currentY);
        drawLine(margin, currentY - headerHeight, margin + tableWidth, currentY - headerHeight);
        drawLine(margin, currentY, margin, currentY - headerHeight);
        drawLine(margin + tableWidth, currentY, margin + tableWidth, currentY - headerHeight);
        drawLine(rightStartX, currentY, rightStartX, currentY - headerHeight);

        // Dane biblioteki
        drawCellText(libraryName, margin + 5, currentY - 15, getBoldFont(), 9);
        drawCellText(libraryDesc, margin + 5, currentY - 27, getRegularFont(), 9);
        drawCellText(address, margin + 5, currentY - 39, getRegularFont(), 9);
        drawCellText(city, margin + 5, currentY - 51, getRegularFont(), 9);

        // Prawa strona
        drawCellText("Nr raportu:", rightStartX + 5, currentY - 15, getRegularFont(), 8);
        drawCellText(reportNumber, rightStartX + 80, currentY - 15, getRegularFont(), 9);

        drawLine(rightStartX, currentY - 30f, margin + tableWidth, currentY - 30f);
        drawCellText("RAPORT ZALEGAJĄCYCH", rightStartX + 30, currentY - 50, getBoldFont(), 14);

        drawLine(rightStartX, currentY - 60f, margin + tableWidth, currentY - 60f);
        drawCellText("Data raportu:", rightStartX + 5, currentY - 80, getRegularFont(), 8);
        drawCellText(reportDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), rightStartX + 80, currentY - 80, getRegularFont(), 9);
    }

    private void drawSimpleOverdueHeader(String title, String subtitle, String reportNumber, LocalDate reportDate,
                                         float x, float y, float headerHeight, float tableWidth) throws IOException {
        drawLine(x, y, x + tableWidth, y);
        drawLine(x, y - headerHeight, x + tableWidth, y - headerHeight);
        drawLine(x, y, x, y - headerHeight);
        drawLine(x + tableWidth, y, x + tableWidth, y - headerHeight);

        drawCellText(title, x + 10, y - 20, getBoldFont(), 10);
        drawCellText(subtitle, x + 10, y - 35, getRegularFont(), 9);
        drawCellText("Nr: " + reportNumber, x + tableWidth - 150, y - 20, getRegularFont(), 8);
        drawCellText("Data: " + reportDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                x + tableWidth - 150, y - 35, getRegularFont(), 8);
    }

    private void drawOverdueSignatureSection(float x, float y, float tableWidth, String generatedBy, LocalDate date) throws IOException {
        float signatureWidth = tableWidth / 3;

        drawCellText(generatedBy, x + tableWidth - signatureWidth/2 - 60, y + 40, getBoldFont(), 8);
        drawCellText("Wygenerowano dnia " + date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                x + tableWidth - signatureWidth/2 - 70, y + 25, getRegularFont(), 8);

        drawDottedLine(x + tableWidth - signatureWidth/2 - 50, y, x + tableWidth - signatureWidth/2 + 50, y);
        drawCellText("podpis", x + tableWidth - signatureWidth/2 - 15, y - 15, getRegularFont(), 8);
    }

    // Interfejsy pomocnicze
    @FunctionalInterface
    private interface SectionDrawer {
        void draw() throws IOException;
    }

    @FunctionalInterface
    private interface HeightCalculator {
        float calculate();
    }
}