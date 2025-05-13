package com.orange.pdf.report;

import com.orange.pdf.builder.LibraryPdfBuilder;
import com.orange.pdf.builder.data.*;
import com.orange.pdf.callback.PdfCallback;
import com.orange.pdf.enums.PdfLibraryReportType;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Klasa obsługująca generowanie raportów popularności książek
 */
public class PopularityPdfReport {

    private static final String DEFAULT_LIBRARY_NAME = "Biblioteka Miejska";
    private static final String DEFAULT_LIBRARY_DESC = "System Zarządzania Księgozbiorem";
    private static final String DEFAULT_ADDRESS = "ul. Akademicka 16";
    private static final String DEFAULT_CITY = "44-100 Gliwice";

    /**
     * Generuje raport popularności książek
     *
     * @param books Lista wszystkich książek
     * @param loanCountMap Mapa zawierająca liczby wypożyczeń dla poszczególnych książek (klucz: ID książki)
     * @param genre Filtr gatunku (null lub pusty string, jeśli bez filtrowania)
     * @param publisher Filtr wydawcy (null lub pusty string, jeśli bez filtrowania)
     * @param startDate Data początkowa okresu (może być null)
     * @param endDate Data końcowa okresu (może być null)
     * @param outputPath Ścieżka do zapisania pliku PDF
     * @param generatedBy Osoba/użytkownik generujący raport
     */
    public void generatePopularityReport(
            List<LibraryPdfTableItem> books,
            Map<String, Integer> loanCountMap,
            String genre,
            String publisher,
            LocalDate startDate,
            LocalDate endDate,
            String outputPath,
            String generatedBy) {

        // Filtrowanie książek według podanych parametrów
        List<LibraryPdfTableItem> filteredBooks = books.stream()
                .filter(book -> (genre == null || genre.isEmpty() || book.getGenre().equalsIgnoreCase(genre)))
                .filter(book -> (publisher == null || publisher.isEmpty() || book.getPublisher().equalsIgnoreCase(publisher)))
                .collect(Collectors.toList());

        // Sortowanie książek według liczby wypożyczeń (od największej do najmniejszej)
        List<LibraryPdfTableItem> sortedBooks = new ArrayList<>(filteredBooks);
        sortedBooks.sort(Comparator.<LibraryPdfTableItem, Integer>comparing(
                book -> loanCountMap.getOrDefault(book.getBookId(), 0)
        ).reversed());

        // Konwersja na listę PopularityPdfTableItem
        List<PopularityPdfTableItem> popularityBooks = new ArrayList<>();
        for (int i = 0; i < sortedBooks.size(); i++) {
            LibraryPdfTableItem book = sortedBooks.get(i);
            int loanCount = loanCountMap.getOrDefault(book.getBookId(), 0);
            int rank = i + 1;

            popularityBooks.add(PopularityPdfTableItem.fromLibraryPdfTableItem(book, loanCount, rank));
        }

        // Przygotowanie podsumowań dla gatunków
        Map<String, Integer> genreCounts = new HashMap<>();
        for (PopularityPdfTableItem book : popularityBooks) {
            int loanCount = book.getLoanCount();
            String bookGenre = book.getGenre() != null ? book.getGenre() : "Nieznany";
            genreCounts.put(bookGenre, genreCounts.getOrDefault(bookGenre, 0) + loanCount);
        }

        // Przygotowanie podsumowań dla wydawców
        Map<String, Integer> publisherCounts = new HashMap<>();
        for (PopularityPdfTableItem book : popularityBooks) {
            int loanCount = book.getLoanCount();
            String bookPublisher = book.getPublisher() != null ? book.getPublisher() : "Nieznany";
            publisherCounts.put(bookPublisher, publisherCounts.getOrDefault(bookPublisher, 0) + loanCount);
        }

        // Przygotowanie podsumowań dla statusów
        Map<String, Integer> statusCounts = new HashMap<>();
        for (PopularityPdfTableItem book : popularityBooks) {
            String bookStatus = book.getStatus() != null ? book.getStatus() : "Nieznany";
            statusCounts.put(bookStatus, statusCounts.getOrDefault(bookStatus, 0) + 1);
        }

        // Budowanie tytułu raportu z uwzględnieniem filtrów
        StringBuilder reportTitleBuilder = new StringBuilder("Raport popularności książek");
        if (genre != null && !genre.isEmpty()) {
            reportTitleBuilder.append(" - Gatunek: ").append(genre);
        }
        if (publisher != null && !publisher.isEmpty()) {
            reportTitleBuilder.append(" - Wydawca: ").append(publisher);
        }
        if (startDate != null) {
            reportTitleBuilder.append(" - Od: ").append(startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        }
        if (endDate != null) {
            reportTitleBuilder.append(" - Do: ").append(endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        }

        // Konwersja map na listy podsumowań
        List<BookStatusSummary> statusSummaries = statusCounts.entrySet().stream()
                .map(entry -> new BookStatusSummary(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        List<GenreSummary> genreSummaries = genreCounts.entrySet().stream()
                .map(entry -> new GenreSummary(entry.getKey(), entry.getValue()))
                .sorted(Comparator.<GenreSummary, Integer>comparing(GenreSummary::getCount).reversed())
                .collect(Collectors.toList());

        List<PublisherSummary> publisherSummaries = publisherCounts.entrySet().stream()
                .map(entry -> new PublisherSummary(entry.getKey(), entry.getValue()))
                .sorted(Comparator.<PublisherSummary, Integer>comparing(PublisherSummary::getCount).reversed())
                .collect(Collectors.toList());

        // Generowanie raportu używając LibraryPdfBuilder
        try {
            LibraryPdfBuilder.createLibraryReport(PdfLibraryReportType.POPULARITY)
                    .buildPopularityReport(
                            DEFAULT_LIBRARY_NAME,
                            reportTitleBuilder.toString(),
                            DEFAULT_ADDRESS,
                            DEFAULT_CITY,
                            generateReportNumber("POP"),
                            LocalDate.now(),
                            new ArrayList<>(popularityBooks),
                            statusSummaries,
                            genreSummaries,
                            publisherSummaries,
                            generatedBy
                    )
                    .save(outputPath, new PdfCallback<>() {
                        @Override
                        public void success(PDDocument document) {
                            System.out.println("Raport popularności został wygenerowany pomyślnie: " + outputPath);
                        }

                        @Override
                        public void error(PDDocument document) {
                            System.err.println("Błąd podczas generowania raportu popularności!");
                        }
                    });
        } catch (Exception e) {
            throw new RuntimeException("Błąd podczas generowania raportu popularności: " + e.getMessage(), e);
        }
    }

    /**
     * Generuje numer raportu z określonym prefiksem
     *
     * @param prefix prefiks numeru raportu
     * @return numer raportu w formacie PREFIX-YYYYMMDD-XXX
     */
    private String generateReportNumber(String prefix) {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String uniqueNumber = String.format("%03d", (int) (Math.random() * 1000));
        return prefix + "-" + date + "-" + uniqueNumber;
    }
}