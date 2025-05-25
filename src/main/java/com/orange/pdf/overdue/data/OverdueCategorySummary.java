package com.orange.pdf.overdue.data;

import lombok.Getter;

/**
 * Klasa reprezentująca podsumowanie kategorii zaległości w bibliotece
 */
@Getter
public class OverdueCategorySummary {

    private String category;
    private int count;
    private long totalOverdueDays;

    /**
     * Konstruktor podsumowania kategorii zaległości
     */
    public OverdueCategorySummary(String category, int count, long totalOverdueDays) {
        this.category = category;
        this.count = count;
        this.totalOverdueDays = totalOverdueDays;
    }

    /**
     * Oblicza średnią liczbę dni zaległości w kategorii
     */
    public double getAverageOverdueDays() {
        if (count == 0) {
            return 0.0;
        }
        return (double) totalOverdueDays / count;
    }
}