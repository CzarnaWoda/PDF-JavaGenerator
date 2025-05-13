package com.orange.pdf.service;

import com.orange.pdf.builder.LibraryPdfBuilder;
import com.orange.pdf.builder.data.*;
import com.orange.pdf.callback.PdfCallback;
import com.orange.pdf.enums.PdfLibraryReportType;
import com.orange.pdf.report.PopularityPdfReport;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Serwis do generowania raportów PDF dla biblioteki
 */
public class LibraryPdfService {

    private static final String DEFAULT_LIBRARY_NAME = "Biblioteka Miejska";
    private static final String DEFAULT_LIBRARY_DESC = "System Zarządzania Księgozbiorem";
    private static final String DEFAULT_ADDRESS = "ul. Akademicka 16";
    private static final String DEFAULT_CITY = "44-100 Gliwice";

    /**
     * Generuje raport inwentaryzacyjny biblioteki z domyślnymi danymi instytucji
     *
     * @param books lista książek do umieszczenia w raporcie
     * @param statusCounts mapa statusów i ich liczebności
     * @param outputPath ścieżka do zapisania pliku PDF
     * @param generatedBy osoba/użytkownik generujący raport
     */
    public void generateInventoryReport(
            List<LibraryPdfTableItem> books,
            Map<String, Integer> statusCounts,
            String outputPath,
            String generatedBy) {

        // Przygotuj mapy gatunków i wydawców z dostępnych książek
        Map<String, Integer> genreCounts = countBooksByGenre(books);
        Map<String, Integer> publisherCounts = countBooksByPublisher(books);

        generateInventoryReport(
                DEFAULT_LIBRARY_NAME,
                DEFAULT_LIBRARY_DESC,
                DEFAULT_ADDRESS,
                DEFAULT_CITY,
                generateReportNumber(),
                LocalDate.now(),
                books,
                statusCounts,
                genreCounts,
                publisherCounts,
                outputPath,
                generatedBy
        );
    }

    /**
     * Generuje raport inwentaryzacyjny biblioteki
     *
     * @param libraryName nazwa biblioteki/instytucji
     * @param libraryDesc opis biblioteki/instytucji
     * @param address adres - ulica
     * @param city miasto i kod pocztowy
     * @param reportNumber numer raportu
     * @param reportDate data raportu
     * @param books lista książek do umieszczenia w raporcie
     * @param statusCounts mapa statusów i ich liczebności
     * @param genreCounts mapa gatunków i ich liczebności
     * @param publisherCounts mapa wydawców i ich liczebności
     * @param outputPath ścieżka do zapisania pliku PDF
     * @param generatedBy osoba/użytkownik generujący raport
     */
    public void generateInventoryReport(
            String libraryName,
            String libraryDesc,
            String address,
            String city,
            String reportNumber,
            LocalDate reportDate,
            List<LibraryPdfTableItem> books,
            Map<String, Integer> statusCounts,
            Map<String, Integer> genreCounts,
            Map<String, Integer> publisherCounts,
            String outputPath,
            String generatedBy) {

        // Konwersja map na listy BookStatusSummary, GenreSummary i PublisherSummary
        List<BookStatusSummary> statusSummaries = statusCounts.entrySet().stream()
                .map(entry -> new BookStatusSummary(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        List<GenreSummary> genreSummaries = genreCounts.entrySet().stream()
                .map(entry -> new GenreSummary(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        List<PublisherSummary> publisherSummaries = publisherCounts.entrySet().stream()
                .map(entry -> new PublisherSummary(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        // Wywołaj buildLibraryInventoryReport z odpowiednimi parametrami
        try {
            LibraryPdfBuilder.createLibraryReport(PdfLibraryReportType.INVENTORY)
                    .buildLibraryInventoryReport(
                            libraryName,
                            libraryDesc,
                            address,
                            city,
                            reportNumber,
                            reportDate,
                            books,
                            statusSummaries,
                            genreSummaries,
                            publisherSummaries,
                            generatedBy
                    )
                    .save(outputPath, new PdfCallback<>() {
                        @Override
                        public void success(PDDocument document) {
                            System.out.println("Raport biblioteczny został wygenerowany pomyślnie: " + outputPath);
                        }

                        @Override
                        public void error(PDDocument document) {
                            System.err.println("Błąd podczas generowania raportu bibliotecznego!");
                        }
                    });
        } catch (Exception e) {
            throw new RuntimeException("Błąd podczas generowania raportu: " + e.getMessage(), e);
        }
    }

    /**
     * Generuje raport książek wypożyczonych
     *
     * @param books lista wypożyczonych książek
     * @param outputPath ścieżka do zapisania pliku PDF
     * @param generatedBy osoba/użytkownik generujący raport
     */
    public void generateBorrowedBooksReport(
            List<LibraryPdfTableItem> books,
            String outputPath,
            String generatedBy) {

        // Filtrowanie tylko książek wypożyczonych
        List<LibraryPdfTableItem> borrowedBooks = books.stream()
                .filter(book -> "Wypożyczona".equalsIgnoreCase(book.getStatus()))
                .collect(Collectors.toList());

        // Tworzenie mapy statusów - w tym przypadku tylko jeden status
        Map<String, Integer> statusCounts = Map.of("Wypożyczona", borrowedBooks.size());

        // Przygotuj mapy gatunków i wydawców
        Map<String, Integer> genreCounts = countBooksByGenre(borrowedBooks);
        Map<String, Integer> publisherCounts = countBooksByPublisher(borrowedBooks);

        generateInventoryReport(
                DEFAULT_LIBRARY_NAME,
                DEFAULT_LIBRARY_DESC + " - Raport książek wypożyczonych",
                DEFAULT_ADDRESS,
                DEFAULT_CITY,
                generateReportNumber("BR"),
                LocalDate.now(),
                borrowedBooks,
                statusCounts,
                genreCounts,
                publisherCounts,
                outputPath,
                generatedBy
        );
    }

    /**
     * Generuje raport książek filtrowany według podanych parametrów
     *
     * @param books lista wszystkich książek
     * @param genre filtr gatunku (null lub pusty string, jeśli bez filtrowania)
     * @param status filtr statusu (null lub pusty string, jeśli bez filtrowania)
     * @param publisher filtr wydawcy (null lub pusty string, jeśli bez filtrowania)
     * @param outputPath ścieżka do zapisania pliku PDF
     * @param generatedBy osoba/użytkownik generujący raport
     */
    public void generateFilteredReport(
            List<LibraryPdfTableItem> books,
            String genre,
            String status,
            String publisher,
            String outputPath,
            String generatedBy) {

        // Filtrowanie książek według podanych parametrów
        List<LibraryPdfTableItem> filteredBooks = books.stream()
                .filter(book -> (genre == null || genre.isEmpty() || book.getGenre().equalsIgnoreCase(genre)))
                .filter(book -> (status == null || status.isEmpty() || book.getStatus().equalsIgnoreCase(status)))
                .filter(book -> (publisher == null || publisher.isEmpty() || book.getPublisher().equalsIgnoreCase(publisher)))
                .collect(Collectors.toList());

        // Przygotuj mapy statusów, gatunków i wydawców
        Map<String, Integer> statusCounts = countBooksByStatus(filteredBooks);
        Map<String, Integer> genreCounts = countBooksByGenre(filteredBooks);
        Map<String, Integer> publisherCounts = countBooksByPublisher(filteredBooks);

        // Zastosowane filtry do tytułu raportu
        StringBuilder reportTitle = new StringBuilder(DEFAULT_LIBRARY_DESC);
        if (genre != null && !genre.isEmpty()) {
            reportTitle.append(" - Gatunek: ").append(genre);
        }
        if (status != null && !status.isEmpty()) {
            reportTitle.append(" - Status: ").append(status);
        }
        if (publisher != null && !publisher.isEmpty()) {
            reportTitle.append(" - Wydawca: ").append(publisher);
        }

        generateInventoryReport(
                DEFAULT_LIBRARY_NAME,
                reportTitle.toString(),
                DEFAULT_ADDRESS,
                DEFAULT_CITY,
                generateReportNumber("FR"),
                LocalDate.now(),
                filteredBooks,
                statusCounts,
                genreCounts,
                publisherCounts,
                outputPath,
                generatedBy
        );
    }

    /**
     * Generuje domyślny numer raportu
     *
     * @return numer raportu w formacie INV-YYYYMMDD-XXX
     */
    private String generateReportNumber() {
        return generateReportNumber("INV");
    }

    /**
     * Generuje numer raportu z określonym prefiksem
     *
     * @param prefix prefiks numeru raportu
     * @return numer raportu w formacie PREFIX-YYYYMMDD-XXX
     */
    private String generateReportNumber(String prefix) {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        // Tutaj możnaby zaimplementować pobieranie ostatniego numeru z bazy danych
        // dla większego stopnia unikalności
        String uniqueNumber = String.format("%03d", (int) (Math.random() * 1000));
        return prefix + "-" + date + "-" + uniqueNumber;
    }

    /**
     * Zlicza książki według statusów
     */
    private Map<String, Integer> countBooksByStatus(List<LibraryPdfTableItem> books) {
        Map<String, Integer> counts = new HashMap<>();
        for (LibraryPdfTableItem book : books) {
            String status = book.getStatus() != null ? book.getStatus() : "Nieznany";
            counts.put(status, counts.getOrDefault(status, 0) + 1);
        }
        return counts;
    }

    /**
     * Zlicza książki według gatunków
     */
    private Map<String, Integer> countBooksByGenre(List<LibraryPdfTableItem> books) {
        Map<String, Integer> counts = new HashMap<>();
        for (LibraryPdfTableItem book : books) {
            String genre = book.getGenre() != null ? book.getGenre() : "Nieznany";
            counts.put(genre, counts.getOrDefault(genre, 0) + 1);
        }
        return counts;
    }

    /**
     * Zlicza książki według wydawców
     */
    private Map<String, Integer> countBooksByPublisher(List<LibraryPdfTableItem> books) {
        Map<String, Integer> counts = new HashMap<>();
        for (LibraryPdfTableItem book : books) {
            String publisher = book.getPublisher() != null ? book.getPublisher() : "Nieznany";
            counts.put(publisher, counts.getOrDefault(publisher, 0) + 1);
        }
        return counts;
    }

    /**
     * Generuje raport popularności książek z określonymi liczbami wypożyczeń
     *
     * @param books lista książek
     * @param loanCountMap mapa zawierająca liczby wypożyczeń dla poszczególnych książek (klucz: ID książki)
     * @param genre filtr gatunku (null lub pusty string, jeśli bez filtrowania)
     * @param publisher filtr wydawcy (null lub pusty string, jeśli bez filtrowania)
     * @param startDate data początkowa okresu (może być null)
     * @param endDate data końcowa okresu (może być null)
     * @param outputPath ścieżka do zapisania pliku PDF
     * @param generatedBy osoba/użytkownik generujący raport
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

        // Użycie klasy PopularityPdfReport do generowania raportu
        PopularityPdfReport popularityReport = new PopularityPdfReport();
        popularityReport.generatePopularityReport(
                books,
                loanCountMap,
                genre,
                publisher,
                startDate,
                endDate,
                outputPath,
                generatedBy
        );
    }
}