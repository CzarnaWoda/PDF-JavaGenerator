package com.orange.pdf.exception;

import com.orange.pdf.callback.PdfCallback;
import org.apache.pdfbox.pdmodel.PDDocument;

public class InvalidPdfTypeException extends RuntimeException {
    public InvalidPdfTypeException(String message) {
        super(message);
    }
}
