package com.orange.pdf.builder.data;

import java.time.Instant;

/**
 * Element tabeli wypożyczeń w raporcie PDF
 */
public class LoanPdfTableItem {
    private String loanId;
    private String bookId;
    private String userId;
    private String status;
    private Instant borrowedAt;
    private Instant dueDate;
    private String lendingLibrarianId;

    public LoanPdfTableItem(String loanId, String bookId, String userId, String status,
                            Instant borrowedAt, Instant dueDate, String lendingLibrarianId) {
        this.loanId = loanId;
        this.bookId = bookId;
        this.userId = userId;
        this.status = status;
        this.borrowedAt = borrowedAt;
        this.dueDate = dueDate;
        this.lendingLibrarianId = lendingLibrarianId;
    }

    // Gettery
    public String getLoanId() { return loanId; }
    public String getBookId() { return bookId; }
    public String getUserId() { return userId; }
    public String getStatus() { return status; }
    public Instant getBorrowedAt() { return borrowedAt; }
    public Instant getDueDate() { return dueDate; }
    public String getLendingLibrarianId() { return lendingLibrarianId; }
}