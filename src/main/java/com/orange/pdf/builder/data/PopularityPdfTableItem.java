package com.orange.pdf.builder.data;

import lombok.Getter;

/**
 * Klasa reprezentująca pozycję książki w raporcie popularności
 * Rozszerza standardową pozycję książki, dodając informacje o popularności
 */
@Getter
public class PopularityPdfTableItem extends LibraryPdfTableItem {

    private int loanCount;    // liczba wypożyczeń
    private int rank;         // ranking popularności (pozycja)

    /**
     * Konstruktor pozycji książki w raporcie popularności
     *
     * @param bookId ID książki
     * @param title tytuł książki
     * @param authors autorzy książki (jako string)
     * @param publisher wydawca książki
     * @param status status książki (np. dostępna, wypożyczona)
     * @param genre gatunek książki
     * @param description opis książki
     * @param loanCount liczba wypożyczeń
     * @param rank ranking popularności (pozycja)
     */
    public PopularityPdfTableItem(
            String bookId,
            String title,
            String authors,
            String publisher,
            String status,
            String genre,
            String description,
            int loanCount,
            int rank) {
        super(bookId, title, authors, publisher, status, genre, description);
        this.loanCount = loanCount;
        this.rank = rank;
    }

    /**
     * Tworzy pozycję raportu popularności na podstawie standardowej pozycji książki
     *
     * @param item Standardowa pozycja książki
     * @param loanCount liczba wypożyczeń
     * @param rank ranking popularności (pozycja)
     * @return Nowa pozycja raportu popularności
     */
    public static PopularityPdfTableItem fromLibraryPdfTableItem(
            LibraryPdfTableItem item,
            int loanCount,
            int rank) {
        return new PopularityPdfTableItem(
                item.getBookId(),
                item.getTitle(),
                item.getAuthors(),
                item.getPublisher(),
                item.getStatus(),
                item.getGenre(),
                item.getDescription(),
                loanCount,
                rank
        );
    }
}