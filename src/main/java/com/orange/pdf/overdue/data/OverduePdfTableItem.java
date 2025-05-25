package com.orange.pdf.overdue.data;

import lombok.Getter;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Klasa reprezentująca pozycję zalegającego użytkownika w raporcie PDF
 */
@Getter
public class OverduePdfTableItem {

    private String loanId;
    private String bookId;
    private String title;
    private String authors;
    private String publisher;
    private String genre;
    private String userId;
    private String userName;
    private String userEmail;
    private Instant borrowedAt;
    private Instant dueDate;
    private long overdueDays;
    private String librarianId;

    /**
     * Konstruktor pozycji zalegającego użytkownika w raporcie
     */
    public OverduePdfTableItem(String loanId, String bookId, String title, String authors, String publisher,
                               String genre, String userId, String userName, String userEmail,
                               Instant borrowedAt, Instant dueDate, String librarianId) {
        this.loanId = loanId;
        this.bookId = bookId;
        this.title = title;
        this.authors = authors;
        this.publisher = publisher;
        this.genre = genre;
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.borrowedAt = borrowedAt;
        this.dueDate = dueDate;
        this.librarianId = librarianId;

        // Oblicz liczbę dni zaległości
        this.overdueDays = calculateOverdueDays(dueDate);
    }

    /**
     * Oblicza liczbę dni zaległości
     */
    private long calculateOverdueDays(Instant dueDate) {
        LocalDate dueDateLocal = dueDate.atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate today = LocalDate.now();

        if (today.isAfter(dueDateLocal)) {
            return java.time.temporal.ChronoUnit.DAYS.between(dueDateLocal, today);
        }

        return 0; // Jeśli nie ma zaległości
    }

    /**
     * Sprawdza czy użytkownik jest zalegający
     */
    public boolean isOverdue() {
        return overdueDays > 0;
    }

    /**
     * Zwraca kategorię zaległości na podstawie liczby dni
     */
    public String getOverdueCategory() {
        if (overdueDays <= 0) {
            return "Brak zaległości";
        } else if (overdueDays <= 7) {
            return "Do 7 dni";
        } else if (overdueDays <= 14) {
            return "8-14 dni";
        } else if (overdueDays <= 30) {
            return "15-30 dni";
        } else {
            return "Powyżej 30 dni";
        }
    }
}