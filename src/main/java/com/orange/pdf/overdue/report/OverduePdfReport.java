package com.orange.pdf.overdue.report;

import com.orange.pdf.builder.data.GenreSummary;
import com.orange.pdf.builder.data.PublisherSummary;
import com.orange.pdf.callback.PdfCallback;
import com.orange.pdf.overdue.builder.OverduePdfBuilder;
import com.orange.pdf.overdue.data.OverdueCategorySummary;
import com.orange.pdf.overdue.data.OverduePdfTableItem;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Klasa obsługująca generowanie raportów zalegających użytkowników
 */
public class OverduePdfReport {

    private static final String DEFAULT_LIBRARY_NAME = "Biblioteka Miejska";
    private static final String DEFAULT_LIBRARY_DESC = "System Zarządzania Księgozbiorem";
    private static final String DEFAULT_ADDRESS = "ul. Akademicka 16";
    private static final String DEFAULT_CITY = "44-100 Gliwice";

    /**
     * Generuje raport zalegających użytkowników
     */
    public void generateOverdueReport(
            List<OverduePdfTableItem> overdueLoans,
            LocalDate startDate,
            LocalDate endDate,
            String genre,
            String publisher,
            String outputPath,
            String generatedBy) {

        // Filtrowanie wypożyczeń według podanych parametrów
        List<OverduePdfTableItem> filteredLoans = overdueLoans.stream()
                .filter(loan -> loan.isOverdue()) // Tylko zalegające
                .filter(loan -> (genre == null || genre.isEmpty() || loan.getGenre().equalsIgnoreCase(genre)))
                .filter(loan -> (publisher == null || publisher.isEmpty() || loan.getPublisher().equalsIgnoreCase(publisher)))
                .filter(loan -> {
                    if (startDate == null) return true;
                    LocalDate borrowedDate = loan.getBorrowedAt().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                    return !borrowedDate.isBefore(startDate);
                })
                .filter(loan -> {
                    if (endDate == null) return true;
                    LocalDate borrowedDate = loan.getBorrowedAt().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                    return !borrowedDate.isAfter(endDate);
                })
                .collect(Collectors.toList());

        // Sortowanie według liczby dni zaległości (od największej do najmniejszej)
        filteredLoans.sort(Comparator.<OverduePdfTableItem, Long>comparing(
                OverduePdfTableItem::getOverdueDays
        ).reversed());

        // Przygotowanie podsumowań dla kategorii zaległości
        Map<String, List<OverduePdfTableItem>> categoryGroups = filteredLoans.stream()
                .collect(Collectors.groupingBy(OverduePdfTableItem::getOverdueCategory));

        List<OverdueCategorySummary> categorySummaries = categoryGroups.entrySet().stream()
                .map(entry -> {
                    String category = entry.getKey();
                    List<OverduePdfTableItem> loans = entry.getValue();
                    int count = loans.size();
                    long totalDays = loans.stream().mapToLong(OverduePdfTableItem::getOverdueDays).sum();
                    return new OverdueCategorySummary(category, count, totalDays);
                })
                .sorted(Comparator.comparing(summary -> getCategoryOrder(summary.getCategory())))
                .collect(Collectors.toList());

        // Przygotowanie podsumowań dla gatunków
        Map<String, Integer> genreCounts = new HashMap<>();
        for (OverduePdfTableItem loan : filteredLoans) {
            String loanGenre = loan.getGenre() != null ? loan.getGenre() : "Nieznany";
            genreCounts.put(loanGenre, genreCounts.getOrDefault(loanGenre, 0) + 1);
        }

        // Przygotowanie podsumowań dla wydawców
        Map<String, Integer> publisherCounts = new HashMap<>();
        for (OverduePdfTableItem loan : filteredLoans) {
            String loanPublisher = loan.getPublisher() != null ? loan.getPublisher() : "Nieznany";
            publisherCounts.put(loanPublisher, publisherCounts.getOrDefault(loanPublisher, 0) + 1);
        }

        // Budowanie tytułu raportu z uwzględnieniem filtrów
        StringBuilder reportTitleBuilder = new StringBuilder("Raport zalegających użytkowników");
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
        List<GenreSummary> genreSummaries = genreCounts.entrySet().stream()
                .map(entry -> new GenreSummary(entry.getKey(), entry.getValue()))
                .sorted(Comparator.<GenreSummary, Integer>comparing(GenreSummary::getCount).reversed())
                .collect(Collectors.toList());

        List<PublisherSummary> publisherSummaries = publisherCounts.entrySet().stream()
                .map(entry -> new PublisherSummary(entry.getKey(), entry.getValue()))
                .sorted(Comparator.<PublisherSummary, Integer>comparing(PublisherSummary::getCount).reversed())
                .collect(Collectors.toList());

        // Generowanie raportu używając OverduePdfBuilder
        try {
            OverduePdfBuilder.createOverdueReport()
                    .buildOverdueReport(
                            DEFAULT_LIBRARY_NAME,
                            reportTitleBuilder.toString(),
                            DEFAULT_ADDRESS,
                            DEFAULT_CITY,
                            generateReportNumber("OVR"),
                            LocalDate.now(),
                            filteredLoans,
                            categorySummaries,
                            genreSummaries,
                            publisherSummaries,
                            generatedBy
                    )
                    .save(outputPath, new PdfCallback<>() {
                        @Override
                        public void success(PDDocument document) {
                            System.out.println("Raport zalegających użytkowników został wygenerowany pomyślnie: " + outputPath);
                        }

                        @Override
                        public void error(PDDocument document) {
                            System.err.println("Błąd podczas generowania raportu zalegających użytkowników!");
                        }
                    });
        } catch (Exception e) {
            throw new RuntimeException("Błąd podczas generowania raportu zalegających użytkowników: " + e.getMessage(), e);
        }
    }

    /**
     * Określa kolejność kategorii zaległości do sortowania
     */
    private int getCategoryOrder(String category) {
        return switch (category) {
            case "Powyżej 30 dni" -> 1;
            case "15-30 dni" -> 2;
            case "8-14 dni" -> 3;
            case "Do 7 dni" -> 4;
            default -> 5;
        };
    }

    /**
     * Generuje numer raportu z określonym prefiksem
     */
    private String generateReportNumber(String prefix) {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String uniqueNumber = String.format("%03d", (int) (Math.random() * 1000));
        return prefix + "-" + date + "-" + uniqueNumber;
    }
}