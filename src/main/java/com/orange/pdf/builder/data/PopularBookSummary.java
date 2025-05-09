package com.orange.pdf.builder.data;

/**
 * Element rankingu najpopularniejszych książek
 */
public class PopularBookSummary {
    private String bookId;
    private String title;
    private String author;
    private int loanCount;

    public PopularBookSummary(String bookId, String title, String author, int loanCount) {
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.loanCount = loanCount;
    }

    // Gettery
    public String getBookId() { return bookId; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public int getLoanCount() { return loanCount; }
}