package com.orange.pdf.builder;

import com.orange.pdf.builder.data.*;
import com.orange.pdf.callback.PdfCallback;
import com.orange.pdf.enums.PdfLibraryReportType;
import com.orange.pdf.enums.PdfType;
import com.orange.pdf.exception.PDPageContentStreamException;
import lombok.Getter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

import java.io.IOException;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Builder do tworzenia raportów bibliotecznych PDF
 */
@Getter
public class LibraryPdfBuilder extends PdfBuilder {

    private static final String REPORT_TITLE = "Raport biblioteczny";
    private static final String REPORT_AUTHOR = "System zarządzania biblioteką";
    private PdfLibraryReportType reportType;

    private LibraryPdfBuilder(PdfType pdfType, PdfLibraryReportType reportType) {
        super(pdfType, REPORT_TITLE, REPORT_AUTHOR);
        this.reportType = reportType;
    }

    /**
     * Tworzy nową instancję buildera do raportu bibliotecznego
     *
     * @param reportType typ raportu bibliotecznego
     * @return nowa instancja LibraryPdfBuilder
     */
    public static LibraryPdfBuilder createLibraryReport(PdfLibraryReportType reportType) {
        return new LibraryPdfBuilder(PdfType.A4, reportType);
    }

    /**
     * Buduje raport inwentaryzacyjny biblioteki z obsługą wielu stron
     *
     * @param libraryName nazwa biblioteki
     * @param libraryDesc opis biblioteki
     * @param address adres biblioteki
     * @param city miasto
     * @param reportNumber numer raportu
     * @param reportDate data raportu
     * @param books lista książek
     * @param statusSummaries lista podsumowań statusów
     * @param genreSummaries lista podsumowań gatunków
     * @param publisherSummaries lista podsumowań wydawców
     * @param generatedBy osoba generująca raport
     * @return builder z gotowym raportem
     */
    public LibraryPdfBuilder buildLibraryInventoryReport(
            String libraryName,
            String libraryDesc,
            String address,
            String city,
            String reportNumber,
            LocalDate reportDate,
            List<LibraryPdfTableItem> books,
            List<BookStatusSummary> statusSummaries,
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
            float minBottomMargin = 50f; // Minimalny margines dolny

            float currentY = getStartY();
            float rightStartX = margin + leftWidth;

            // Nagłówek dokumentu
            drawReportHeader(libraryName, libraryDesc, address, city, reportNumber,
                    reportDate, rightStartX, currentY, headerHeight, leftWidth, rightWidth);

            currentY = currentY - headerHeight - 20;

            // Rysowanie tabeli książek z obsługą wielu stron
            currentY = drawBooksTableWithPaging(books, margin, currentY, tableWidth, pageHeight, minBottomMargin,
                    libraryName, libraryDesc, address, city, reportNumber, reportDate);

            // Sprawdź czy jest wystarczająco miejsca na podsumowanie statusów
            float statusSummaryHeight = calculateStatusSummaryHeight(statusSummaries);
            if (currentY - statusSummaryHeight - 40 < minBottomMargin) {
                // Dodaj nową stronę
                addNewPage();
                currentY = getStartY();
            }

            // Sekcja podsumowania statusów
            float statusSummaryY = currentY - 40;
            drawStatusSummary(statusSummaries, margin, statusSummaryY, tableWidth);
            currentY = statusSummaryY - statusSummaryHeight;

            // Sprawdź czy jest wystarczająco miejsca na podsumowanie gatunków
            float genreSummaryHeight = calculateGenreSummaryHeight(genreSummaries);
            if (currentY - genreSummaryHeight - 40 < minBottomMargin) {
                // Dodaj nową stronę
                addNewPage();
                currentY = getStartY();
            }

            // Sekcja podsumowania gatunków
            float genreSummaryY = currentY - 40;
            drawGenreSummary(genreSummaries, margin, genreSummaryY, tableWidth);
            currentY = genreSummaryY - genreSummaryHeight;

            // Sprawdź czy jest wystarczająco miejsca na podsumowanie wydawców
            float publisherSummaryHeight = calculatePublisherSummaryHeight(publisherSummaries);
            if (currentY - publisherSummaryHeight - 40 < minBottomMargin) {
                // Dodaj nową stronę
                addNewPage();
                currentY = getStartY();
            }

            // Sekcja podsumowania wydawców
            float publisherSummaryY = currentY - 40;
            drawPublisherSummary(publisherSummaries, margin, publisherSummaryY, tableWidth);
            currentY = publisherSummaryY - publisherSummaryHeight;

            // Sprawdź czy jest wystarczająco miejsca na sekcję podpisów
            if (currentY - 80 < minBottomMargin) {
                // Dodaj nową stronę
                addNewPage();
                currentY = getStartY();
            }

            // Sekcja podpisów
            float signaturesY = currentY - 80;
            drawSignatureSection(margin, signaturesY, tableWidth, generatedBy, reportDate);

        } catch (IOException e) {
            safeEndText(getContentStream());
            throw new PDPageContentStreamException("Nie udało się zbudować raportu bibliotecznego: " + e.getMessage());
        }

        return this;
    }

    /**
     * Buduje raport inwentaryzacyjny biblioteki
     * Wersja zachowana dla kompatybilności wstecznej
     *
     * @param libraryName nazwa biblioteki
     * @param libraryDesc opis biblioteki
     * @param address adres biblioteki
     * @param city miasto
     * @param reportNumber numer raportu
     * @param reportDate data raportu
     * @param books lista książek
     * @param statusSummaries lista podsumowań statusów
     * @param generatedBy osoba generująca raport
     * @return builder z gotowym raportem
     */
    public LibraryPdfBuilder buildLibraryInventoryReport(
            String libraryName,
            String libraryDesc,
            String address,
            String city,
            String reportNumber,
            LocalDate reportDate,
            List<LibraryPdfTableItem> books,
            List<BookStatusSummary> statusSummaries,
            String generatedBy) {

        // Tworzymy puste listy dla gatunków i wydawców
        List<GenreSummary> emptyGenreSummaries = new ArrayList<>();
        List<PublisherSummary> emptyPublisherSummaries = new ArrayList<>();

        // Delegujemy wywołanie do pełnej wersji metody
        return buildLibraryInventoryReport(
                libraryName,
                libraryDesc,
                address,
                city,
                reportNumber,
                reportDate,
                books,
                statusSummaries,
                emptyGenreSummaries,
                emptyPublisherSummaries,
                generatedBy
        );
    }



    /**
     * Rysuje tabelę z książkami z obsługą wielu stron
     *
     * @return aktualna pozycja Y po narysowaniu tabeli
     */
    private float drawBooksTableWithPaging(List<LibraryPdfTableItem> books, float x, float y, float tableWidth,
                                           float pageHeight, float minBottomMargin,
                                           String libraryName, String libraryDesc, String address, String city,
                                           String reportNumber, LocalDate reportDate) throws IOException {

        float rowHeight = 25f;
        float headerRowHeight = 25f;
        float margin = getMargin();

        // Definiuje szerokości kolumn
        float col1Width = 30f;        // Lp.
        float col2Width = 60f;        // ID
        float col3Width = 160f;       // Tytuł
        float col4Width = 120f;       // Autor(zy)
        float col5Width = 90f;        // Wydawca
        float col6Width = tableWidth - col1Width - col2Width - col3Width - col4Width - col5Width; // Status

        float currentY = y;
        int currentRowIndex = 0;

        // Rysuj nagłówek tabeli na pierwszej stronie
        drawTableHeader(x, currentY, tableWidth, headerRowHeight, col1Width, col2Width, col3Width, col4Width, col5Width);
        currentY -= headerRowHeight;

        // Przetwarzanie każdego wiersza z książkami
        while (currentRowIndex < books.size()) {
            // Sprawdź czy jest miejsce na kolejny wiersz
            if (currentY - rowHeight < minBottomMargin) {
                // Zakończ aktualną stronę i rozpocznij nową
                addNewPage();

                // Na nowej stronie dodaj krótki nagłówek
                float headerHeight = 50f;

                currentY = getStartY();

                // Nagłówek dokumentu - uproszczona wersja na kolejnych stronach
                drawSimpleHeader(libraryName, "Kontynuacja raportu - strona " + getDocument().getNumberOfPages(),
                        reportNumber, reportDate, margin, currentY, headerHeight, tableWidth);

                currentY = currentY - headerHeight - 20;

                // Rysuj nagłówek tabeli na nowej stronie
                drawTableHeader(x, currentY, tableWidth, headerRowHeight, col1Width, col2Width, col3Width, col4Width, col5Width);
                currentY -= headerRowHeight;
            }

            // Rysuj wiersz z książką
            LibraryPdfTableItem book = books.get(currentRowIndex);
            drawBookRow(book, currentRowIndex + 1, x, currentY, tableWidth, rowHeight,
                    col1Width, col2Width, col3Width, col4Width, col5Width);

            currentY -= rowHeight;
            currentRowIndex++;
        }

        return currentY;
    }

    /**
     * Rysuje uproszczony nagłówek na kolejnych stronach raportu
     */
    private void drawSimpleHeader(String title, String subtitle, String reportNumber, LocalDate reportDate,
                                  float x, float y, float headerHeight, float tableWidth) throws IOException {

        PDPageContentStream contentStream = getContentStream();

        // Ramka nagłówka
        drawLine(x, y, x + tableWidth, y);
        drawLine(x, y - headerHeight, x + tableWidth, y - headerHeight);
        drawLine(x, y, x, y - headerHeight);
        drawLine(x + tableWidth, y, x + tableWidth, y - headerHeight);

        // Tytuł
        contentStream.beginText();
        contentStream.setFont(getBoldFont(), 10);
        contentStream.newLineAtOffset(x + 10, y - 20);
        contentStream.showText(title);
        contentStream.endText();

        // Podtytuł
        contentStream.beginText();
        contentStream.setFont(getRegularFont(), 9);
        contentStream.newLineAtOffset(x + 10, y - 35);
        contentStream.showText(subtitle);
        contentStream.endText();

        // Numer raportu
        contentStream.beginText();
        contentStream.setFont(getRegularFont(), 8);
        contentStream.newLineAtOffset(x + tableWidth - 150, y - 20);
        contentStream.showText("Nr raportu: " + reportNumber);
        contentStream.endText();

        // Data raportu
        contentStream.beginText();
        contentStream.setFont(getRegularFont(), 8);
        contentStream.newLineAtOffset(x + tableWidth - 150, y - 35);
        String formattedDate = reportDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        contentStream.showText("Data: " + formattedDate);
        contentStream.endText();
    }



    /**
     * Rysuje tabelę z książkami
     */
    private void drawBooksTable(List<LibraryPdfTableItem> books, float x, float y, float tableWidth) throws IOException {
        PDPageContentStream contentStream = getContentStream();
        float rowHeight = 25f;

        // Definiuje szerokości kolumn
        float col1Width = 30f;        // Lp.
        float col2Width = 60f;        // ID
        float col3Width = 160f;       // Tytuł
        float col4Width = 120f;       // Autor(zy)
        float col5Width = 90f;        // Wydawca
        float col6Width = tableWidth - col1Width - col2Width - col3Width - col4Width - col5Width; // Status

        float currentY = y;

        // Nagłówek tabeli
        drawTableHeader(x, currentY, tableWidth, rowHeight, col1Width, col2Width, col3Width, col4Width, col5Width);
        currentY -= rowHeight;

        // Zawartość tabeli
        for (int i = 0; i < books.size(); i++) {
            drawBookRow(books.get(i), i + 1, x, currentY, tableWidth, rowHeight,
                    col1Width, col2Width, col3Width, col4Width, col5Width);
            currentY -= rowHeight;
        }
    }

    /**
     * Rysuje nagłówek tabeli książek
     */
    private void drawTableHeader(float x, float y, float tableWidth, float rowHeight,
                                 float col1Width, float col2Width, float col3Width,
                                 float col4Width, float col5Width) throws IOException {

        PDPageContentStream contentStream = getContentStream();

        // Linie nagłówka
        drawLine(x, y, x + tableWidth, y);
        drawLine(x, y - rowHeight, x + tableWidth, y - rowHeight);
        drawLine(x, y, x, y - rowHeight);

        float nextX = x;

        nextX += col1Width;
        drawLine(nextX, y, nextX, y - rowHeight);

        nextX += col2Width;
        drawLine(nextX, y, nextX, y - rowHeight);

        nextX += col3Width;
        drawLine(nextX, y, nextX, y - rowHeight);

        nextX += col4Width;
        drawLine(nextX, y, nextX, y - rowHeight);

        nextX += col5Width;
        drawLine(nextX, y, nextX, y - rowHeight);

        drawLine(x + tableWidth, y, x + tableWidth, y - rowHeight);

        // Etykiety kolumn
        contentStream.beginText();
        contentStream.setFont(getBoldFont(), 8);
        contentStream.newLineAtOffset(x + 5, y - 15);
        contentStream.showText("Lp.");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(getBoldFont(), 8);
        contentStream.newLineAtOffset(x + col1Width + 5, y - 15);
        contentStream.showText("ID");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(getBoldFont(), 8);
        contentStream.newLineAtOffset(x + col1Width + col2Width + 5, y - 15);
        contentStream.showText("Tytuł");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(getBoldFont(), 8);
        contentStream.newLineAtOffset(x + col1Width + col2Width + col3Width + 5, y - 15);
        contentStream.showText("Autor(zy)");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(getBoldFont(), 8);
        contentStream.newLineAtOffset(x + col1Width + col2Width + col3Width + col4Width + 5, y - 15);
        contentStream.showText("Wydawca");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(getBoldFont(), 8);
        contentStream.newLineAtOffset(x + col1Width + col2Width + col3Width + col4Width + col5Width + 5, y - 15);
        contentStream.showText("Status");
        contentStream.endText();
    }

    /**
     * Rysuje wiersz tabeli książek
     */
    private void drawBookRow(LibraryPdfTableItem book, int rowNum, float x, float y, float tableWidth,
                             float rowHeight, float col1Width, float col2Width, float col3Width,
                             float col4Width, float col5Width) throws IOException {

        PDPageContentStream contentStream = getContentStream();

        // Linie wiersza
        drawLine(x, y - rowHeight, x + tableWidth, y - rowHeight);
        drawLine(x, y, x, y - rowHeight);

        float nextX = x;

        nextX += col1Width;
        drawLine(nextX, y, nextX, y - rowHeight);

        nextX += col2Width;
        drawLine(nextX, y, nextX, y - rowHeight);

        nextX += col3Width;
        drawLine(nextX, y, nextX, y - rowHeight);

        nextX += col4Width;
        drawLine(nextX, y, nextX, y - rowHeight);

        nextX += col5Width;
        drawLine(nextX, y, nextX, y - rowHeight);

        drawLine(x + tableWidth, y, x + tableWidth, y - rowHeight);

        // Zawartość wiersza
        contentStream.beginText();
        contentStream.setFont(getRegularFont(), 8);
        contentStream.newLineAtOffset(x + 5, y - 15);
        contentStream.showText(String.valueOf(rowNum));
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(getRegularFont(), 8);
        contentStream.newLineAtOffset(x + col1Width + 5, y - 15);
        contentStream.showText(book.getBookId());
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(getRegularFont(), 8);
        contentStream.newLineAtOffset(x + col1Width + col2Width + 5, y - 15);
        contentStream.showText(truncateText(book.getTitle(), 30));
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(getRegularFont(), 8);
        contentStream.newLineAtOffset(x + col1Width + col2Width + col3Width + 5, y - 15);
        contentStream.showText(truncateText(book.getAuthors(), 25));
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(getRegularFont(), 8);
        contentStream.newLineAtOffset(x + col1Width + col2Width + col3Width + col4Width + 5, y - 15);
        contentStream.showText(truncateText(book.getPublisher(), 20));
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(getRegularFont(), 8);
        contentStream.newLineAtOffset(x + col1Width + col2Width + col3Width + col4Width + col5Width + 5, y - 15);
        contentStream.showText(book.getStatus());
        contentStream.endText();
    }

    /**
     * Rysuje sekcję z podsumowaniem statusów książek
     */
    private void drawStatusSummary(List<BookStatusSummary> summaries, float x, float y, float tableWidth) throws IOException {
        if (summaries == null || summaries.isEmpty()) {
            return;
        }

        PDPageContentStream contentStream = getContentStream();
        float rowHeight = 25f;

        // Nagłówek sekcji
        contentStream.beginText();
        contentStream.setFont(getBoldFont(), 10);
        contentStream.newLineAtOffset(x, y + 15);
        contentStream.showText("Podsumowanie statusów książek:");
        contentStream.endText();

        // Definiuje szerokości kolumn
        float col1Width = 200f;       // Status
        float col2Width = tableWidth - col1Width;  // Ilość

        float currentY = y - 15;

        // Nagłówek tabeli statusów
        drawLine(x, currentY, x + tableWidth, currentY);
        drawLine(x, currentY - rowHeight, x + tableWidth, currentY - rowHeight);
        drawLine(x, currentY, x, currentY - rowHeight);
        drawLine(x + col1Width, currentY, x + col1Width, currentY - rowHeight);
        drawLine(x + tableWidth, currentY, x + tableWidth, currentY - rowHeight);

        contentStream.beginText();
        contentStream.setFont(getBoldFont(), 8);
        contentStream.newLineAtOffset(x + 5, currentY - 15);
        contentStream.showText("Status");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(getBoldFont(), 8);
        contentStream.newLineAtOffset(x + col1Width + 5, currentY - 15);
        contentStream.showText("Ilość");
        contentStream.endText();

        currentY -= rowHeight;

        // Wiersze statusów
        for (BookStatusSummary summary : summaries) {
            drawLine(x, currentY - rowHeight, x + tableWidth, currentY - rowHeight);
            drawLine(x, currentY, x, currentY - rowHeight);
            drawLine(x + col1Width, currentY, x + col1Width, currentY - rowHeight);
            drawLine(x + tableWidth, currentY, x + tableWidth, currentY - rowHeight);

            contentStream.beginText();
            contentStream.setFont(getRegularFont(), 8);
            contentStream.newLineAtOffset(x + 5, currentY - 15);
            contentStream.showText(summary.getStatus());
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(getRegularFont(), 8);
            contentStream.newLineAtOffset(x + col1Width + 5, currentY - 15);
            contentStream.showText(String.valueOf(summary.getCount()));
            contentStream.endText();

            currentY -= rowHeight;
        }

        // Wiersz z sumą książek
        int totalBooks = summaries.stream().mapToInt(BookStatusSummary::getCount).sum();

        drawLine(x, currentY - rowHeight, x + tableWidth, currentY - rowHeight);
        drawLine(x, currentY, x, currentY - rowHeight);
        drawLine(x + col1Width, currentY, x + col1Width, currentY - rowHeight);
        drawLine(x + tableWidth, currentY, x + tableWidth, currentY - rowHeight);

        contentStream.beginText();
        contentStream.setFont(getBoldFont(), 8);
        contentStream.newLineAtOffset(x + 5, currentY - 15);
        contentStream.showText("RAZEM");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(getBoldFont(), 8);
        contentStream.newLineAtOffset(x + col1Width + 5, currentY - 15);
        contentStream.showText(String.valueOf(totalBooks));
        contentStream.endText();
    }

    /**
     * Rysuje sekcję z podsumowaniem gatunków książek
     */
    private void drawGenreSummary(List<GenreSummary> summaries, float x, float y, float tableWidth) throws IOException {
        if (summaries == null || summaries.isEmpty()) {
            return;
        }

        PDPageContentStream contentStream = getContentStream();
        float rowHeight = 25f;

        // Nagłówek sekcji
        contentStream.beginText();
        contentStream.setFont(getBoldFont(), 10);
        contentStream.newLineAtOffset(x, y + 15);
        contentStream.showText("Podsumowanie gatunków książek:");
        contentStream.endText();

        // Definiuje szerokości kolumn
        float col1Width = 200f;       // Gatunek
        float col2Width = tableWidth - col1Width;  // Ilość

        float currentY = y - 15;

        // Nagłówek tabeli gatunków
        drawLine(x, currentY, x + tableWidth, currentY);
        drawLine(x, currentY - rowHeight, x + tableWidth, currentY - rowHeight);
        drawLine(x, currentY, x, currentY - rowHeight);
        drawLine(x + col1Width, currentY, x + col1Width, currentY - rowHeight);
        drawLine(x + tableWidth, currentY, x + tableWidth, currentY - rowHeight);

        contentStream.beginText();
        contentStream.setFont(getBoldFont(), 8);
        contentStream.newLineAtOffset(x + 5, currentY - 15);
        contentStream.showText("Gatunek");
        contentStream.endText();

        // POPRAWKA: Usunięcie dodatkowego beginText przed showText("Ilość")
        contentStream.beginText();
        contentStream.setFont(getBoldFont(), 8);
        contentStream.newLineAtOffset(x + col1Width + 5, currentY - 15);
        contentStream.showText("Ilość");
        contentStream.endText();

        currentY -= rowHeight;

        // Wiersze gatunków
        for (GenreSummary summary : summaries) {
            drawLine(x, currentY - rowHeight, x + tableWidth, currentY - rowHeight);
            drawLine(x, currentY, x, currentY - rowHeight);
            drawLine(x + col1Width, currentY, x + col1Width, currentY - rowHeight);
            drawLine(x + tableWidth, currentY, x + tableWidth, currentY - rowHeight);

            contentStream.beginText();
            contentStream.setFont(getRegularFont(), 8);
            contentStream.newLineAtOffset(x + 5, currentY - 15);
            contentStream.showText(summary.getGenre());
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(getRegularFont(), 8);
            contentStream.newLineAtOffset(x + col1Width + 5, currentY - 15);
            contentStream.showText(String.valueOf(summary.getCount()));
            contentStream.endText();

            currentY -= rowHeight;
        }

        // Wiersz z sumą książek
        int totalBooks = summaries.stream().mapToInt(GenreSummary::getCount).sum();

        drawLine(x, currentY - rowHeight, x + tableWidth, currentY - rowHeight);
        drawLine(x, currentY, x, currentY - rowHeight);
        drawLine(x + col1Width, currentY, x + col1Width, currentY - rowHeight);
        drawLine(x + tableWidth, currentY, x + tableWidth, currentY - rowHeight);

        contentStream.beginText();
        contentStream.setFont(getBoldFont(), 8);
        contentStream.newLineAtOffset(x + 5, currentY - 15);
        contentStream.showText("RAZEM");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(getBoldFont(), 8);
        contentStream.newLineAtOffset(x + col1Width + 5, currentY - 15);
        contentStream.showText(String.valueOf(totalBooks));
        contentStream.endText();
    }

    /**
     * Rysuje sekcję z podsumowaniem wydawców książek
     */
    private void drawPublisherSummary(List<PublisherSummary> summaries, float x, float y, float tableWidth) throws IOException {
        if (summaries == null || summaries.isEmpty()) {
            return;
        }

        PDPageContentStream contentStream = getContentStream();
        float rowHeight = 25f;

        // Nagłówek sekcji
        contentStream.beginText();
        contentStream.setFont(getBoldFont(), 10);
        contentStream.newLineAtOffset(x, y + 15);
        contentStream.showText("Podsumowanie wydawców:");
        contentStream.endText();

        // Definiuje szerokości kolumn
        float col1Width = 200f;       // Wydawca
        float col2Width = tableWidth - col1Width;  // Ilość

        float currentY = y - 15;

        // Nagłówek tabeli wydawców
        drawLine(x, currentY, x + tableWidth, currentY);
        drawLine(x, currentY - rowHeight, x + tableWidth, currentY - rowHeight);
        drawLine(x, currentY, x, currentY - rowHeight);
        drawLine(x + col1Width, currentY, x + col1Width, currentY - rowHeight);
        drawLine(x + tableWidth, currentY, x + tableWidth, currentY - rowHeight);

        contentStream.beginText();
        contentStream.setFont(getBoldFont(), 8);
        contentStream.newLineAtOffset(x + 5, currentY - 15);
        contentStream.showText("Wydawca");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(getBoldFont(), 8);
        contentStream.newLineAtOffset(x + col1Width + 5, currentY - 15);
        contentStream.showText("Ilość");
        contentStream.endText();

        currentY -= rowHeight;

        // Wiersze wydawców
        for (PublisherSummary summary : summaries) {
            drawLine(x, currentY - rowHeight, x + tableWidth, currentY - rowHeight);
            drawLine(x, currentY, x, currentY - rowHeight);
            drawLine(x + col1Width, currentY, x + col1Width, currentY - rowHeight);
            drawLine(x + tableWidth, currentY, x + tableWidth, currentY - rowHeight);

            contentStream.beginText();
            contentStream.setFont(getRegularFont(), 8);
            contentStream.newLineAtOffset(x + 5, currentY - 15);
            contentStream.showText(summary.getPublisher());
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(getRegularFont(), 8);
            contentStream.newLineAtOffset(x + col1Width + 5, currentY - 15);
            contentStream.showText(String.valueOf(summary.getCount()));
            contentStream.endText();

            currentY -= rowHeight;
        }

        // Wiersz z sumą książek
        int totalBooks = summaries.stream().mapToInt(PublisherSummary::getCount).sum();

        drawLine(x, currentY - rowHeight, x + tableWidth, currentY - rowHeight);
        drawLine(x, currentY, x, currentY - rowHeight);
        drawLine(x + col1Width, currentY, x + col1Width, currentY - rowHeight);
        drawLine(x + tableWidth, currentY, x + tableWidth, currentY - rowHeight);

        contentStream.beginText();
        contentStream.setFont(getBoldFont(), 8);
        contentStream.newLineAtOffset(x + 5, currentY - 15);
        contentStream.showText("RAZEM");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(getBoldFont(), 8);
        contentStream.newLineAtOffset(x + col1Width + 5, currentY - 15);
        contentStream.showText(String.valueOf(totalBooks));
        contentStream.endText();
    }

    /**
     * Rysuje sekcję podpisów
     */
    protected void drawSignatureSection(float x, float y, float tableWidth, String generatedBy, LocalDate date) throws IOException {
        PDPageContentStream contentStream = getContentStream();
        float signatureWidth = tableWidth / 3;

        contentStream.beginText();
        contentStream.setFont(getBoldFont(), 8);
        contentStream.newLineAtOffset(x + tableWidth - signatureWidth/2 - 60, y + 40);
        contentStream.showText(generatedBy);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(getRegularFont(), 8);
        String formattedDate = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        contentStream.newLineAtOffset(x + tableWidth - signatureWidth/2 - 70, y + 25);
        contentStream.showText("      Wygenerowano dnia " + formattedDate);
        contentStream.endText();

        // Podpisy linią przerywaną
        drawDottedLine(x + tableWidth - signatureWidth/2 - 50, y, x + tableWidth - signatureWidth/2 + 50, y);

        // Etykiety podpisów
        contentStream.beginText();
        contentStream.setFont(getRegularFont(), 8);
        contentStream.newLineAtOffset(x + tableWidth - signatureWidth/2 - 15, y - 15);
        contentStream.showText("podpis");
        contentStream.endText();
    }

    /**
     * Oblicza wysokość tabeli książek
     */
    private float calculateTableHeight(List<LibraryPdfTableItem> books) {
        return (books.size() + 1) * 25f; // Wysokość wiersza (25f) * (liczba książek + nagłówek)
    }

    /**
     * Oblicza wysokość tabeli podsumowania statusów
     */
    private float calculateSummaryHeight(List<BookStatusSummary> summaries) {
        if (summaries == null || summaries.isEmpty()) {
            return 0f;
        }
        return (summaries.size() + 2) * 25f; // Wysokość wiersza (25f) * (liczba statusów + nagłówek + suma)
    }

    /**
     * Oblicza wysokość tabeli podsumowania statusów
     */
    private float calculateStatusSummaryHeight(List<BookStatusSummary> summaries) {
        if (summaries == null || summaries.isEmpty()) {
            return 0f;
        }
        return (summaries.size() + 2) * 25f; // Wysokość wiersza (25f) * (liczba statusów + nagłówek + suma)
    }

    /**
     * Oblicza wysokość tabeli podsumowania gatunków
     */
    private float calculateGenreSummaryHeight(List<GenreSummary> summaries) {
        if (summaries == null || summaries.isEmpty()) {
            return 0f;
        }
        return (summaries.size() + 2) * 25f; // Wysokość wiersza (25f) * (liczba gatunków + nagłówek + suma)
    }

    /**
     * Oblicza wysokość tabeli podsumowania wydawców
     */
    private float calculatePublisherSummaryHeight(List<PublisherSummary> summaries) {
        if (summaries == null || summaries.isEmpty()) {
            return 0f;
        }
        return (summaries.size() + 2) * 25f; // Wysokość wiersza (25f) * (liczba wydawców + nagłówek + suma)
    }

    /**
     * Skraca tekst do określonej długości, dodając "..." na końcu jeśli został skrócony
     */
    private String truncateText(String text, int maxLength) {
        if (text == null) {
            return "";
        }

        if (text.length() <= maxLength) {
            return text;
        }

        return text.substring(0, maxLength - 3) + "...";
    }
    public LibraryPdfBuilder buildPopularityReport(
            String libraryName,
            String libraryDesc,
            String address,
            String city,
            String reportNumber,
            LocalDate reportDate,
            List<LibraryPdfTableItem> books,
            List<BookStatusSummary> statusSummaries,
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
            float minBottomMargin = 50f; // Minimalny margines dolny

            float currentY = getStartY();
            float rightStartX = margin + leftWidth;

            // Nagłówek dokumentu
            drawReportHeader(libraryName, libraryDesc, address, city, reportNumber,
                    reportDate, rightStartX, currentY, headerHeight, leftWidth, rightWidth);

            currentY = currentY - headerHeight - 20;

            // Rysowanie tabeli książek z obsługą wielu stron i dodatkową kolumną popularności
            currentY = drawPopularityBooksTableWithPaging(books, margin, currentY, tableWidth, pageHeight, minBottomMargin,
                    libraryName, libraryDesc, address, city, reportNumber, reportDate);

            // Sprawdź czy jest wystarczająco miejsca na podsumowanie gatunków
            float genreSummaryHeight = calculateGenreSummaryHeight(genreSummaries);
            if (currentY - genreSummaryHeight - 40 < minBottomMargin) {
                // Dodaj nową stronę
                addNewPage();
                currentY = getStartY();
            }

            // Sekcja podsumowania gatunków (sortowana wg popularności)
            float genreSummaryY = currentY - 40;
            drawGenreSummary(genreSummaries, margin, genreSummaryY, tableWidth);
            currentY = genreSummaryY - genreSummaryHeight;

            // Sprawdź czy jest wystarczająco miejsca na podsumowanie wydawców
            float publisherSummaryHeight = calculatePublisherSummaryHeight(publisherSummaries);
            if (currentY - publisherSummaryHeight - 40 < minBottomMargin) {
                // Dodaj nową stronę
                addNewPage();
                currentY = getStartY();
            }

            // Sekcja podsumowania wydawców (sortowana wg popularności)
            float publisherSummaryY = currentY - 40;
            drawPublisherSummary(publisherSummaries, margin, publisherSummaryY, tableWidth);
            currentY = publisherSummaryY - publisherSummaryHeight;

            // Sprawdź czy jest wystarczająco miejsca na sekcję podpisów
            if (currentY - 80 < minBottomMargin) {
                // Dodaj nową stronę
                addNewPage();
                currentY = getStartY();
            }

            // Sekcja podpisów
            float signaturesY = currentY - 80;
            drawSignatureSection(margin, signaturesY, tableWidth, generatedBy, reportDate);

        } catch (IOException e) {
            safeEndText(getContentStream());
            throw new PDPageContentStreamException("Nie udało się zbudować raportu popularności: " + e.getMessage());
        }

        return this;
    }

    /**
     * Rysuje tabelę książek z obsługą wielu stron i dodatkową kolumną popularności
     *
     * @return aktualna pozycja Y po narysowaniu tabeli
     */
    private float drawPopularityBooksTableWithPaging(List<LibraryPdfTableItem> books, float x, float y, float tableWidth,
                                                     float pageHeight, float minBottomMargin,
                                                     String libraryName, String libraryDesc, String address, String city,
                                                     String reportNumber, LocalDate reportDate) throws IOException {

        float rowHeight = 25f;
        float headerRowHeight = 25f;
        float margin = getMargin();

        // Definiuje szerokości kolumn z uwzględnieniem dwóch dodatkowych kolumn (ranking i liczba wypożyczeń)
        float col1Width = 20f;        // Lp.
        float col2Width = 25f;        // Ranking
        float col3Width = 50f;        // ID
        float col4Width = 140f;       // Tytuł
        float col5Width = 110f;       // Autor(zy)
        float col6Width = 80f;        // Wydawca
        float col7Width = 50f;        // Gatunek
        float col8Width = tableWidth - col1Width - col2Width - col3Width - col4Width - col5Width - col6Width - col7Width; // Wypożyczenia

        float currentY = y;
        int currentRowIndex = 0;

        // Rysuj nagłówek tabeli na pierwszej stronie
        drawPopularityTableHeader(x, currentY, tableWidth, headerRowHeight, col1Width, col2Width, col3Width, col4Width, col5Width, col6Width, col7Width);
        currentY -= headerRowHeight;

        // Przetwarzanie każdego wiersza z książkami
        while (currentRowIndex < books.size()) {
            // Sprawdź czy jest miejsce na kolejny wiersz
            if (currentY - rowHeight < minBottomMargin) {
                // Zakończ aktualną stronę i rozpocznij nową
                addNewPage();

                // Na nowej stronie dodaj krótki nagłówek
                float headerHeight = 50f;

                currentY = getStartY();

                // Nagłówek dokumentu - uproszczona wersja na kolejnych stronach
                drawSimpleHeader(libraryName, "Kontynuacja raportu popularności - strona " + getDocument().getNumberOfPages(),
                        reportNumber, reportDate, margin, currentY, headerHeight, tableWidth);

                currentY = currentY - headerHeight - 20;

                // Rysuj nagłówek tabeli na nowej stronie
                drawPopularityTableHeader(x, currentY, tableWidth, headerRowHeight, col1Width, col2Width, col3Width, col4Width, col5Width, col6Width, col7Width);
                currentY -= headerRowHeight;
            }

            // Rysuj wiersz z książką
            LibraryPdfTableItem book = books.get(currentRowIndex);
            // Sprawdź czy książka jest instancją PopularityPdfTableItem i pobierz dane o popularności
            int rank = currentRowIndex + 1; // Domyślnie użyj indeksu jako rangi
            int loanCount = 0;              // Domyślna liczba wypożyczeń

            if (book instanceof PopularityPdfTableItem) {
                PopularityPdfTableItem popularityBook = (PopularityPdfTableItem) book;
                rank = popularityBook.getRank();
                loanCount = popularityBook.getLoanCount();
            }

            drawPopularityBookRow(book, currentRowIndex + 1, rank, loanCount, x, currentY, tableWidth, rowHeight,
                    col1Width, col2Width, col3Width, col4Width, col5Width, col6Width, col7Width);

            currentY -= rowHeight;
            currentRowIndex++;
        }

        return currentY;
    }

    /**
     * Rysuje nagłówek tabeli książek dla raportu popularności
     */
    private void drawPopularityTableHeader(float x, float y, float tableWidth, float rowHeight,
                                           float col1Width, float col2Width, float col3Width,
                                           float col4Width, float col5Width, float col6Width, float col7Width) throws IOException {

        PDPageContentStream contentStream = getContentStream();

        // Linie nagłówka
        drawLine(x, y, x + tableWidth, y);
        drawLine(x, y - rowHeight, x + tableWidth, y - rowHeight);
        drawLine(x, y, x, y - rowHeight);

        float nextX = x;

        nextX += col1Width;
        drawLine(nextX, y, nextX, y - rowHeight);

        nextX += col2Width;
        drawLine(nextX, y, nextX, y - rowHeight);

        nextX += col3Width;
        drawLine(nextX, y, nextX, y - rowHeight);

        nextX += col4Width;
        drawLine(nextX, y, nextX, y - rowHeight);

        nextX += col5Width;
        drawLine(nextX, y, nextX, y - rowHeight);

        nextX += col6Width;
        drawLine(nextX, y, nextX, y - rowHeight);

        nextX += col7Width;
        drawLine(nextX, y, nextX, y - rowHeight);

        drawLine(x + tableWidth, y, x + tableWidth, y - rowHeight);

        // Etykiety kolumn
        contentStream.beginText();
        contentStream.setFont(getBoldFont(), 8);
        contentStream.newLineAtOffset(x + 5, y - 15);
        contentStream.showText("Lp.");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(getBoldFont(), 8);
        contentStream.newLineAtOffset(x + col1Width + 5, y - 15);
        contentStream.showText("Rank");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(getBoldFont(), 8);
        contentStream.newLineAtOffset(x + col1Width + col2Width + 5, y - 15);
        contentStream.showText("ID");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(getBoldFont(), 8);
        contentStream.newLineAtOffset(x + col1Width + col2Width + col3Width + 5, y - 15);
        contentStream.showText("Tytuł");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(getBoldFont(), 8);
        contentStream.newLineAtOffset(x + col1Width + col2Width + col3Width + col4Width + 5, y - 15);
        contentStream.showText("Autor(zy)");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(getBoldFont(), 8);
        contentStream.newLineAtOffset(x + col1Width + col2Width + col3Width + col4Width + col5Width + 5, y - 15);
        contentStream.showText("Wydawca");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(getBoldFont(), 8);
        contentStream.newLineAtOffset(x + col1Width + col2Width + col3Width + col4Width + col5Width + col6Width + 5, y - 15);
        contentStream.showText("Gatunek");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(getBoldFont(), 8);
        contentStream.newLineAtOffset(x + col1Width + col2Width + col3Width + col4Width + col5Width + col6Width + col7Width + 5, y - 15);
        contentStream.showText("Wypożyczeń");
        contentStream.endText();
    }

    /**
     * Rysuje wiersz tabeli książek dla raportu popularności
     */
    private void drawPopularityBookRow(LibraryPdfTableItem book, int rowNum, int rank, int loanCount, float x, float y, float tableWidth,
                                       float rowHeight, float col1Width, float col2Width, float col3Width,
                                       float col4Width, float col5Width, float col6Width, float col7Width) throws IOException {

        PDPageContentStream contentStream = getContentStream();

        // Linie wiersza
        drawLine(x, y - rowHeight, x + tableWidth, y - rowHeight);
        drawLine(x, y, x, y - rowHeight);

        float nextX = x;

        nextX += col1Width;
        drawLine(nextX, y, nextX, y - rowHeight);

        nextX += col2Width;
        drawLine(nextX, y, nextX, y - rowHeight);

        nextX += col3Width;
        drawLine(nextX, y, nextX, y - rowHeight);

        nextX += col4Width;
        drawLine(nextX, y, nextX, y - rowHeight);

        nextX += col5Width;
        drawLine(nextX, y, nextX, y - rowHeight);

        nextX += col6Width;
        drawLine(nextX, y, nextX, y - rowHeight);

        nextX += col7Width;
        drawLine(nextX, y, nextX, y - rowHeight);

        drawLine(x + tableWidth, y, x + tableWidth, y - rowHeight);

        // Zawartość wiersza
        contentStream.beginText();
        contentStream.setFont(getRegularFont(), 8);
        contentStream.newLineAtOffset(x + 5, y - 15);
        contentStream.showText(String.valueOf(rowNum));
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(getRegularFont(), 8);
        contentStream.newLineAtOffset(x + col1Width + 5, y - 15);
        contentStream.showText(String.valueOf(rank));
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(getRegularFont(), 8);
        contentStream.newLineAtOffset(x + col1Width + col2Width + 5, y - 15);
        contentStream.showText(book.getBookId());
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(getRegularFont(), 8);
        contentStream.newLineAtOffset(x + col1Width + col2Width + col3Width + 5, y - 15);
        contentStream.showText(truncateText(book.getTitle(), 25));
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(getRegularFont(), 8);
        contentStream.newLineAtOffset(x + col1Width + col2Width + col3Width + col4Width + 5, y - 15);
        contentStream.showText(truncateText(book.getAuthors(), 20));
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(getRegularFont(), 8);
        contentStream.newLineAtOffset(x + col1Width + col2Width + col3Width + col4Width + col5Width + 5, y - 15);
        contentStream.showText(truncateText(book.getPublisher(), 15));
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(getRegularFont(), 8);
        contentStream.newLineAtOffset(x + col1Width + col2Width + col3Width + col4Width + col5Width + col6Width + 5, y - 15);
        contentStream.showText(truncateText(book.getGenre(), 10));
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(getRegularFont(), 8);
        contentStream.newLineAtOffset(x + col1Width + col2Width + col3Width + col4Width + col5Width + col6Width + col7Width + 5, y - 15);
        contentStream.showText(String.valueOf(loanCount));
        contentStream.endText();
    }








}