package com.orange.pdf.builder.data;

/**
 * Podsumowanie wypożyczeń obsłużonych przez bibliotekarza
 */
public class LibrarianSummary {
    private String librarianId;
    private int count;

    public LibrarianSummary(String librarianId, int count) {
        this.librarianId = librarianId;
        this.count = count;
    }

    // Gettery
    public String getLibrarianId() { return librarianId; }
    public int getCount() { return count; }
}