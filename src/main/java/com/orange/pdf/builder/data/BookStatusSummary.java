package com.orange.pdf.builder.data;

import lombok.Getter;

/**
 * Klasa reprezentująca podsumowanie statusów książek w bibliotece
 */
@Getter
public class BookStatusSummary {

    private String status;
    private int count;

    /**
     * Konstruktor podsumowania statusu
     *
     * @param status nazwa statusu książki
     * @param count liczba książek o danym statusie
     */
    public BookStatusSummary(String status, int count) {
        this.status = status;
        this.count = count;
    }
}