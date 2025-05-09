package com.orange.pdf.builder.data;

/**
 * Podsumowanie statusów wypożyczeń w raporcie PDF
 */
public class LoanStatusSummary {
    private String status;
    private int count;

    public LoanStatusSummary(String status, int count) {
        this.status = status;
        this.count = count;
    }

    // Gettery
    public String getStatus() { return status; }
    public int getCount() { return count; }
}