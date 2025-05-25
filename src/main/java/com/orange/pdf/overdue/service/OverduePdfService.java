package com.orange.pdf.overdue.service;

import com.orange.pdf.overdue.data.OverduePdfTableItem;
import com.orange.pdf.overdue.report.OverduePdfReport;

import java.time.LocalDate;
import java.util.List;

/**
 * Serwis do generowania raportów zalegających użytkowników PDF
 */
public class OverduePdfService {

    /**
     * Generuje raport zalegających użytkowników z określonymi filtrami
     */
    public void generateOverdueReport(
            List<OverduePdfTableItem> overdueLoans,
            LocalDate startDate,
            LocalDate endDate,
            String genre,
            String publisher,
            String outputPath,
            String generatedBy) {

        // Użycie klasy OverduePdfReport do generowania raportu
        OverduePdfReport overdueReport = new OverduePdfReport();
        overdueReport.generateOverdueReport(
                overdueLoans,
                startDate,
                endDate,
                genre,
                publisher,
                outputPath,
                generatedBy
        );
    }

    /**
     * Generuje raport zalegających użytkowników z domyślnymi parametrami
     */
    public void generateOverdueReport(
            List<OverduePdfTableItem> overdueLoans,
            String outputPath,
            String generatedBy) {

        generateOverdueReport(
                overdueLoans,
                null, // brak filtra daty początkowej
                null, // brak filtra daty końcowej
                null, // brak filtra gatunku
                null, // brak filtra wydawcy
                outputPath,
                generatedBy
        );
    }
}