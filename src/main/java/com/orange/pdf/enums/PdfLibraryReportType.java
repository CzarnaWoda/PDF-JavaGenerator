package com.orange.pdf.enums;

/**
 * Typy raportów bibliotecznych
 */
public enum PdfLibraryReportType {
    /**
     * Raport inwentaryzacyjny wszystkich książek
     */
    INVENTORY,

    /**
     * Raport książek popularnych
     */
    POPULARITY,

    /**
     * Raport książek filtrowany
     */
    FILTERED,

    /**
     * Raport zalegających użytkowników
     */
    OVERDUE

}