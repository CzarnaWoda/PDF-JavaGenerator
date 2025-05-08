package com.orange.pdf.builder;

import com.orange.pdf.builder.data.PdfTableItem;
import com.orange.pdf.callback.PdfCallback;
import com.orange.pdf.enums.PdfType;
import com.orange.pdf.exception.InvalidPdfTypeException;
import com.orange.pdf.exception.PDPageContentStreamException;
import lombok.Getter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Getter
public class PdfBuilder {

    private final PDDocument document;
    private final PDPageContentStream contentStream;
    private PDFont regularFont;
    private PDFont boldFont;
    private PDFont italicFont;
    private float fontSize = 10;
    private float margin = 30;
    private final float width;
    private float startY;
    private final float tableStartY;
    private final PDPage page;

    public PdfBuilder(PdfType type, String title, String author) {
        document = new PDDocument();

        document.getDocumentInformation().setAuthor(author);

        document.getDocumentInformation().setTitle(title);

        switch (type) {
            case A4 -> page = new PDPage(PDRectangle.A4);
            case A5 -> page = new PDPage(PDRectangle.A5);
            default -> throw new InvalidPdfTypeException("Type of PdfFile cannot be found!");
        }
        document.addPage(page);

        try {
            contentStream = new PDPageContentStream(document, page);

            try {
                loadFontsFromResources();
            } catch (IOException e) {
                System.err.println("Nie udało się załadować fontów z resources: " + e.getMessage());
            }

        } catch (IOException e) {
            throw new PDPageContentStreamException("Nie udało się utworzyć dokumentu PDF: " + e.getMessage());
        }

        this.width = page.getMediaBox().getWidth() - 2 * margin;
        this.startY = page.getMediaBox().getHeight() - margin;
        this.tableStartY = startY - 90;
    }

    private void loadFontsFromResources() throws IOException {
        try (
                InputStream regularIs = getClass().getResourceAsStream("/fonts/LiberationSans-Regular.ttf");
                InputStream boldIs = getClass().getResourceAsStream("/fonts/LiberationSans-Bold.ttf");
                InputStream italicIs = getClass().getResourceAsStream("/fonts/LiberationSans-Italic.ttf");
        ) {
            if (regularIs == null) {
                throw new IOException("Nie znaleziono pliku fontu regularnego w zasobach");
            }
            if (boldIs == null) {
                throw new IOException("Nie znaleziono pliku fontu pogrubionego w zasobach");
            }
            if (italicIs == null) {
                throw new IOException("Nie znaleziono pliku fontu kursywy w zasobach");
            }

            regularFont = PDType0Font.load(document, regularIs);
            boldFont = PDType0Font.load(document, boldIs);
            italicFont = PDType0Font.load(document, italicIs);
        }
    }

    public PdfBuilder setRegularFont(PDFont font) {
        regularFont = font;
        return this;
    }

    public PdfBuilder setBoldFont(PDFont font) {
        boldFont = font;
        return this;
    }

    public PdfBuilder setItalicFont(PDFont font) {
        italicFont = font;
        return this;
    }

    public PdfBuilder setFontSize(float fontSize) {
        this.fontSize = fontSize;
        return this;
    }

    public PdfBuilder setMargin(float margin) {
        this.margin = margin;
        return this;
    }

    public PdfBuilder loadFontsFromResources(String regularFontPath, String boldFontPath, String italicFontPath) {
        try (
                InputStream regularIs = getClass().getResourceAsStream(regularFontPath);
                InputStream boldIs = getClass().getResourceAsStream(boldFontPath);
                InputStream italicIs = getClass().getResourceAsStream(italicFontPath)
        ) {
            if (regularIs == null) {
                throw new IOException("Nie znaleziono pliku: " + regularFontPath);
            }
            if (boldIs == null) {
                throw new IOException("Nie znaleziono pliku: " + boldFontPath);
            }
            if (italicIs == null) {
                throw new IOException("Nie znaleziono pliku: " + italicFontPath);
            }

            regularFont = PDType0Font.load(document, regularIs);
            boldFont = PDType0Font.load(document, boldIs);
            italicFont = PDType0Font.load(document, italicIs);
        } catch (IOException e) {
            throw new PDPageContentStreamException("Nie udało się załadować fontów z zasobów: " + e.getMessage());
        }
        return this;
    }

    private void drawLine(float xStart, float yStart, float xEnd, float yEnd) throws IOException {
        contentStream.moveTo(xStart, yStart);
        contentStream.lineTo(xEnd, yEnd);
        contentStream.stroke();
    }

    private void drawDottedLine(float xStart, float yStart, float xEnd, float yEnd) throws IOException {
        contentStream.setLineDashPattern(new float[]{3.0f}, 0);
        drawLine(xStart, yStart, xEnd, yEnd);
        contentStream.setLineDashPattern(new float[]{}, 0);
    }

    public PdfBuilder buildWarehouseReceipt(String companyName, String address, String city, String nip,
                                            String documentNumber, String referenceNumber, LocalDate documentDate,
                                            String recipient, List<PdfTableItem> items, String receivedBy) {
        try {
            float tableWidth = width;
            float headerHeight = 120f;
            float leftWidth = tableWidth * 0.5f;
            float rightWidth = tableWidth - leftWidth;

            float currentY = startY;
            float rightStartX = margin + leftWidth;

            drawLine(margin, currentY, margin + tableWidth, currentY);
            drawLine(margin, currentY - headerHeight, margin + tableWidth, currentY - headerHeight);
            drawLine(margin, currentY, margin, currentY - headerHeight);
            drawLine(margin + tableWidth, currentY, margin + tableWidth, currentY - headerHeight);

            contentStream.beginText();
            contentStream.setFont(regularFont, 9);
            contentStream.newLineAtOffset(margin + 5, currentY - 15);
            contentStream.showText(companyName);
            contentStream.newLineAtOffset(0, -12);
            contentStream.showText(address);
            contentStream.newLineAtOffset(0, -12);
            contentStream.showText(city);
            contentStream.newLineAtOffset(0, -12);
            contentStream.showText("NIP " + nip);
            contentStream.endText();

            drawLine(rightStartX, currentY, rightStartX, currentY - headerHeight);


            float labelWidth = 80f;
            float docNumWidth = 80f;
            float refNumWidth = rightWidth - labelWidth - docNumWidth;

            drawLine(rightStartX + labelWidth, currentY, rightStartX + labelWidth, currentY - 30f);
            drawLine(rightStartX + labelWidth + docNumWidth, currentY, rightStartX + labelWidth + docNumWidth, currentY - 30f);

            contentStream.beginText();
            contentStream.setFont(regularFont, 8);
            contentStream.newLineAtOffset(rightStartX + 5, currentY - 15);
            contentStream.showText("Nr. dokumentu:");
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(regularFont, 9);
            contentStream.newLineAtOffset(rightStartX + labelWidth + 5, currentY - 15);
            contentStream.showText(documentNumber);
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(regularFont, 9);
            contentStream.newLineAtOffset(rightStartX + labelWidth + docNumWidth + 5, currentY - 15);
            contentStream.showText(referenceNumber);
            contentStream.endText();

            float rowY1 = currentY - 30f;
            drawLine(rightStartX, rowY1, margin + tableWidth, rowY1);

            float pzColWidth = 80f;
            drawLine(rightStartX + pzColWidth, rowY1, rightStartX + pzColWidth, rowY1 - 30f);

            contentStream.beginText();
            contentStream.setFont(boldFont, 14);
            contentStream.newLineAtOffset(rightStartX + pzColWidth/2 - 10, rowY1 - 20);
            contentStream.showText("PZ");
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(regularFont, 10);
            contentStream.newLineAtOffset(rightStartX + pzColWidth + 10, rowY1 - 20);
            contentStream.showText("Przyjęcie na magazyn");
            contentStream.endText();

            float rowY2 = rowY1 - 30f;
            drawLine(rightStartX, rowY2, margin + tableWidth, rowY2);

            contentStream.beginText();
            contentStream.setFont(regularFont, 8);
            contentStream.newLineAtOffset(rightStartX + 5, rowY2 - 20);
            contentStream.showText("Data:");
            contentStream.endText();

            String formattedDate = documentDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            contentStream.beginText();
            contentStream.setFont(regularFont, 9);
            contentStream.newLineAtOffset(rightStartX + 100, rowY2 - 20);
            contentStream.showText(formattedDate);
            contentStream.endText();

            float rowY3 = rowY2 - 30f;
            drawLine(rightStartX, rowY3, margin + tableWidth, rowY3);

            contentStream.beginText();
            contentStream.setFont(regularFont, 8);
            contentStream.newLineAtOffset(rightStartX + 5, rowY3 - 20);
            contentStream.showText("Nazwisko/Nazwa:");
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(boldFont, 9);
            contentStream.newLineAtOffset(rightStartX + 100, rowY3 - 20);
            contentStream.showText(recipient);
            contentStream.endText();

            currentY = currentY - headerHeight - 20;

            drawItemsTable(items, margin, currentY, tableWidth);

            float tableHeight = (items.size() + 1) * 25f; // Header + rows
            float summaryRowHeight = 25f;

            float signaturesY = currentY - tableHeight - summaryRowHeight - 120f;

            drawSignatureSection(margin, signaturesY, tableWidth, receivedBy, documentDate);
            startY = signaturesY - 50f;

        } catch (IOException e) {
            throw new PDPageContentStreamException("Failed to build document header: " + e.getMessage());
        }
        return this;
    }

    private void drawItemsTable(List<PdfTableItem> items, float x, float y, float tableWidth) throws IOException {
        float rowHeight = 25f;

        float col1Width = 30f;  // Lp.
        float col2Width = 100f; // Indeks
        float col3Width = tableWidth - col1Width - col2Width - 150f; // Nazwa narzędzia, wymiar
        float col4Width = 50f;  // Ilość
        float col5Width = 50f;  // jm
        float col6Width = 50f;  // Wartość ISO

        float currentY = y;

        contentStream.setFont(boldFont, 9);

        drawLine(x, currentY, x + tableWidth, currentY);
        drawLine(x, currentY - rowHeight, x + tableWidth, currentY - rowHeight);
        drawLine(x, currentY, x, currentY - rowHeight);

        float nextX = x;

        nextX += col1Width;
        drawLine(nextX, currentY, nextX, currentY - rowHeight);

        nextX += col2Width;
        drawLine(nextX, currentY, nextX, currentY - rowHeight);

        nextX += col3Width;
        drawLine(nextX, currentY, nextX, currentY - rowHeight);

        nextX += col4Width;
        drawLine(nextX, currentY, nextX, currentY - rowHeight);

        nextX += col5Width;
        drawLine(nextX, currentY, nextX, currentY - rowHeight);

        drawLine(x + tableWidth, currentY, x + tableWidth, currentY - rowHeight);

        contentStream.beginText();
        contentStream.setFont(boldFont, 8);
        contentStream.newLineAtOffset(x + 5, currentY - 15);
        contentStream.showText("Lp.");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(boldFont, 8);
        contentStream.newLineAtOffset(x + col1Width + 5, currentY - 15);
        contentStream.showText("Indeks");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(boldFont, 8);
        contentStream.newLineAtOffset(x + col1Width + col2Width + 5, currentY - 15);
        contentStream.showText("Nazwa narzędzia, wymiar");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(boldFont, 8);
        contentStream.newLineAtOffset(x + col1Width + col2Width + col3Width + 5, currentY - 15);
        contentStream.showText("Ilość");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(boldFont, 8);
        contentStream.newLineAtOffset(x + col1Width + col2Width + col3Width + col4Width + 5, currentY - 15);
        contentStream.showText("jm");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(boldFont, 7);
        contentStream.newLineAtOffset(x + col1Width + col2Width + col3Width + col4Width + col5Width + 2, currentY - 15);
        contentStream.showText("Wartość ISO");
        contentStream.endText();

        currentY -= rowHeight;

        for (int i = 0; i < items.size(); i++) {
            PdfTableItem item = items.get(i);

            drawLine(x, currentY - rowHeight, x + tableWidth, currentY - rowHeight);
            drawLine(x, currentY, x, currentY - rowHeight);

            nextX = x;

            nextX += col1Width;
            drawLine(nextX, currentY, nextX, currentY - rowHeight);

            nextX += col2Width;
            drawLine(nextX, currentY, nextX, currentY - rowHeight);

            nextX += col3Width;
            drawLine(nextX, currentY, nextX, currentY - rowHeight);

            nextX += col4Width;
            drawLine(nextX, currentY, nextX, currentY - rowHeight);

            nextX += col5Width;
            drawLine(nextX, currentY, nextX, currentY - rowHeight);

            drawLine(x + tableWidth, currentY, x + tableWidth, currentY - rowHeight);

            // Row content
            contentStream.beginText();
            contentStream.setFont(regularFont, 8);
            contentStream.newLineAtOffset(x + 5, currentY - 15);
            contentStream.showText(String.valueOf(i + 1));
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(regularFont, 8);
            contentStream.newLineAtOffset(x + col1Width + 5, currentY - 15);
            contentStream.showText(item.getIndex());
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(regularFont, 8);
            contentStream.newLineAtOffset(x + col1Width + col2Width + 5, currentY - 15);
            contentStream.showText(item.getName());
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(regularFont, 8);
            contentStream.newLineAtOffset(x + col1Width + col2Width + col3Width + 5, currentY - 15);
            contentStream.showText(String.valueOf(item.getQuantity()));
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(regularFont, 8);
            contentStream.newLineAtOffset(x + col1Width + col2Width + col3Width + col4Width + 5, currentY - 15);
            contentStream.showText(item.getUnit());
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(regularFont, 8);
            contentStream.newLineAtOffset(x + col1Width + col2Width + col3Width + col4Width + col5Width + 5, currentY - 15);
            contentStream.showText(String.format(Locale.forLanguageTag("pl-PL"), "%.1f", item.getValue()));
            contentStream.endText();

            currentY -= rowHeight;
        }

        float total = 0;
        for (PdfTableItem item : items) {
            total += (float) item.getValue();
        }

        float totalStartX = x + col1Width + col2Width + col3Width;

        drawLine(totalStartX, currentY, x + tableWidth, currentY);
        drawLine(totalStartX, currentY - rowHeight, x + tableWidth, currentY - rowHeight);
        drawLine(totalStartX, currentY, totalStartX, currentY - rowHeight);
        drawLine(totalStartX + col4Width + col5Width, currentY, totalStartX + col4Width + col5Width, currentY - rowHeight);
        drawLine(x + tableWidth, currentY, x + tableWidth, currentY - rowHeight);

        contentStream.beginText();
        contentStream.setFont(boldFont, 8);
        contentStream.newLineAtOffset(totalStartX + 5, currentY - 15);
        contentStream.showText("Razem dokument");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(boldFont, 8);
        contentStream.newLineAtOffset(totalStartX + col4Width + col5Width + 5, currentY - 15);
        contentStream.showText(String.format(Locale.forLanguageTag("pl-PL"), "%.2f", total));
        contentStream.endText();
    }

    private void drawSignatureSection(float x, float y, float tableWidth, String receivedBy, LocalDate documentDate) throws IOException {
        float signatureWidth = tableWidth / 3;

        contentStream.beginText();
        contentStream.setFont(boldFont, 8);
        contentStream.newLineAtOffset(x + tableWidth - signatureWidth/2 - 60, y + 40);
        contentStream.showText(receivedBy);
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(regularFont, 8);
        String formattedDate = documentDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        contentStream.newLineAtOffset(x + tableWidth - signatureWidth/2 - 70, y + 25);
        contentStream.showText("      Przyjął dnia " + formattedDate);
        contentStream.endText();

        // Draw dotted signature lines
        drawDottedLine(x + signatureWidth/2 - 50, y, x + signatureWidth/2 + 50, y);
        drawDottedLine(x + tableWidth/2 - 50, y, x + tableWidth/2 + 50, y);
        drawDottedLine(x + tableWidth - signatureWidth/2 - 50, y, x + tableWidth - signatureWidth/2 + 50, y);

        // Signature labels
        contentStream.beginText();
        contentStream.setFont(regularFont, 8);
        contentStream.newLineAtOffset(x + signatureWidth/2 - 15, y - 15);
        contentStream.showText("Przyjął");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(regularFont, 8);
        contentStream.newLineAtOffset(x + tableWidth/2 - 15, y - 15);
        contentStream.showText("podpis");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(regularFont, 8);
        contentStream.newLineAtOffset(x + tableWidth - signatureWidth/2 - 15, y - 15);
        contentStream.showText("podpis*");
        contentStream.endText();
    }

    public void save(String filePath, PdfCallback<PDDocument> callback) {
        try {
            contentStream.close();
            document.save(filePath);
            document.close();
            callback.success(document);

        } catch (IOException e) {

            callback.error(document);
            throw new PDPageContentStreamException("Failed to save PDF: " + e.getMessage());
        }
    }

    public static PdfBuilder createWarehouseReceipt() {
        return new PdfBuilder(PdfType.A4, "Warehouse receipt", "Orange");
    }

    public static PdfBuilder createWarehouseReceiptWithFonts(String regularFontPath, String boldFontPath, String italicFontPath) {
        return new PdfBuilder(PdfType.A4, "Warehouse receipt", "Orange")
                .loadFontsFromResources(regularFontPath,boldFontPath,italicFontPath);
    }
}