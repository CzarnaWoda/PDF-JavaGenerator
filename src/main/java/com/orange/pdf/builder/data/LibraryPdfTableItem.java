package com.orange.pdf.builder.data;

import lombok.Getter;

/**
 * Klasa reprezentująca pozycję książki w raporcie bibliotecznym
 */
@Getter
public class LibraryPdfTableItem {

    private String bookId;
    private String title;
    private String authors;
    private String publisher;
    private String status;
    private String genre;
    private String description;

    /**
     * Konstruktor pozycji książki w raporcie
     *
     * @param bookId ID książki
     * @param title tytuł książki
     * @param authors autorzy książki (jako string)
     * @param publisher wydawca książki
     * @param status status książki (np. dostępna, wypożyczona)
     * @param genre gatunek książki
     * @param description opis książki
     */
    public LibraryPdfTableItem(String bookId, String title, String authors, String publisher,
                               String status, String genre, String description) {
        this.bookId = bookId;
        this.title = title;
        this.authors = authors;
        this.publisher = publisher;
        this.status = status;
        this.genre = genre;
        this.description = description;
    }

    /**
     * Konstruktor pozycji książki w raporcie (bez opisu - dla kompatybilności)
     *
     * @param bookId ID książki
     * @param title tytuł książki
     * @param authors autorzy książki (jako string)
     * @param publisher wydawca książki
     * @param status status książki (np. dostępna, wypożyczona)
     * @param genre gatunek książki
     */
    public LibraryPdfTableItem(String bookId, String title, String authors, String publisher,
                               String status, String genre) {
        this(bookId, title, authors, publisher, status, genre, "");
    }
}