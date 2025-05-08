package com.orange;

import com.orange.pdf.builder.PdfBuilder;
import com.orange.pdf.builder.data.PdfTableItem;
import com.orange.pdf.callback.PdfCallback;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        // Domyślne wartości
        String companyName = "Projektowanie i Wdrażanie";
        String systemsDesc = "Systemów Informatycznych";
        String address = "44-100 Gliwice";
        String street = "ul. Orlat Śląskich";
        String nip = "631-132-20-90";
        String documentNumber = "Pz 1/2006";
        String referenceNumber = "123/06";
        LocalDate documentDate = LocalDate.of(2006, 2, 28);
        String recipient = "HURTOWNIA WIERTELKO";
        String receivedBy = "Jacek Krywult";
        String outputPath = "receipt.pdf";
        List<PdfTableItem> items = new ArrayList<>();

        // Domyślne pozycje dokumentu
        items.add(new PdfTableItem("2002/BPP/BT024", "PODNOŚNIK HYDRAULICZNY  OKB 15 T", 2000, "szt.", 2000.00));
        items.add(new PdfTableItem("2004/BPP/W119E", "ZAWIESIE WĘŻOWE 127 / 3M", 1000, "szt.", 1000.00));

        // Parsowanie argumentów
        if (args.length > 0) {
            try {
                // Sprawdzanie flag pomocy
                if (args[0].equalsIgnoreCase("--help") || args[0].equalsIgnoreCase("-h")) {
                    printHelp();
                    return;
                }

                // Parsowanie argumentów wiersza poleceń
                for (int i = 0; i < args.length; i++) {
                    switch (args[i]) {
                        case "--company", "-c" -> {
                            if (i + 1 < args.length) companyName = args[++i];
                            else {
                                System.err.println("Błąd: Brak wartości dla parametru --company");
                                printHelp();
                                return;
                            }
                        }
                        case "--desc", "-d" -> {
                            if (i + 1 < args.length) systemsDesc = args[++i];
                            else {
                                System.err.println("Błąd: Brak wartości dla parametru --desc");
                                printHelp();
                                return;
                            }
                        }
                        case "--address", "-a" -> {
                            if (i + 1 < args.length) address = args[++i];
                            else {
                                System.err.println("Błąd: Brak wartości dla parametru --address");
                                printHelp();
                                return;
                            }
                        }
                        case "--street", "-s" -> {
                            if (i + 1 < args.length) street = args[++i];
                            else {
                                System.err.println("Błąd: Brak wartości dla parametru --street");
                                printHelp();
                                return;
                            }
                        }
                        case "--nip", "-n" -> {
                            if (i + 1 < args.length) nip = args[++i];
                            else {
                                System.err.println("Błąd: Brak wartości dla parametru --nip");
                                printHelp();
                                return;
                            }
                        }
                        case "--document", "-dn" -> {
                            if (i + 1 < args.length) documentNumber = args[++i];
                            else {
                                System.err.println("Błąd: Brak wartości dla parametru --document");
                                printHelp();
                                return;
                            }
                        }
                        case "--reference", "-r" -> {
                            if (i + 1 < args.length) referenceNumber = args[++i];
                            else {
                                System.err.println("Błąd: Brak wartości dla parametru --reference");
                                printHelp();
                                return;
                            }
                        }
                        case "--date", "-dt" -> {
                            if (i + 1 < args.length) {
                                try {
                                    documentDate = LocalDate.parse(args[++i], DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                                } catch (DateTimeParseException e) {
                                    System.err.println("Błąd: Nieprawidłowy format daty. Użyj formatu: yyyy-MM-dd");
                                    printHelp();
                                    return;
                                }
                            } else {
                                System.err.println("Błąd: Brak wartości dla parametru --date");
                                printHelp();
                                return;
                            }
                        }
                        case "--recipient", "-rc" -> {
                            if (i + 1 < args.length) recipient = args[++i];
                            else {
                                System.err.println("Błąd: Brak wartości dla parametru --recipient");
                                printHelp();
                                return;
                            }
                        }
                        case "--receivedby", "-rb" -> {
                            if (i + 1 < args.length) receivedBy = args[++i];
                            else {
                                System.err.println("Błąd: Brak wartości dla parametru --receivedby");
                                printHelp();
                                return;
                            }
                        }
                        case "--output", "-o" -> {
                            if (i + 1 < args.length) outputPath = args[++i];
                            else {
                                System.err.println("Błąd: Brak wartości dla parametru --output");
                                printHelp();
                                return;
                            }
                        }
                        case "--item", "-i" -> {
                            if (i + 5 < args.length) {
                                try {
                                    String itemIndex = args[++i];
                                    String itemName = args[++i];
                                    int quantity = Integer.parseInt(args[++i]);
                                    String unit = args[++i];
                                    double value = Double.parseDouble(args[++i].replace(',', '.'));

                                    // Jeśli dodajemy pierwszy element za pomocą parametrów, wyczyść domyślne elementy
                                    if (items.size() == 2 && items.get(0).getIndex().equals("2002/BPP/BT024")) {
                                        items.clear();
                                    }

                                    items.add(new PdfTableItem(itemIndex, itemName, quantity, unit, value));
                                } catch (NumberFormatException e) {
                                    System.err.println("Błąd: Nieprawidłowy format liczby w parametrze --item");
                                    printHelp();
                                    return;
                                }
                            } else {
                                System.err.println("Błąd: Niewystarczająca liczba wartości dla parametru --item");
                                printHelp();
                                return;
                            }
                        }
                        default -> {
                            System.err.println("Błąd: Nieznany parametr: " + args[i]);
                            printHelp();
                            return;
                        }
                    }
                }

            } catch (Exception e) {
                System.err.println("Wystąpił błąd: " + e.getMessage());
                printHelp();
                return;
            }
        } else {
            System.out.println("Używam domyślnych parametrów. Użyj --help aby zobaczyć dostępne opcje.");
        }

        // Tworzenie dokumentu
        try {
            System.out.println("Generowanie dokumentu PDF...");
            PdfBuilder.createWarehouseReceipt()
                    .buildWarehouseReceipt(
                            companyName,
                            systemsDesc,
                            address + street,
                            nip,
                            documentNumber,
                            referenceNumber,
                            documentDate,
                            recipient,
                            items,
                            receivedBy
                    )
                    .save(outputPath, new PdfCallback<>() {
                        @Override
                        public void success(PDDocument success) {
                            System.out.println("Dokument utworzony pomyślnie:");
                            if (success.getDocumentInformation() != null && success.getDocumentInformation().getTitle() != null) {
                                System.out.println("Tytuł dokumentu: " + success.getDocumentInformation().getTitle());
                            }
                        }

                        @Override
                        public void error(PDDocument error) {
                            System.err.println("Błąd podczas tworzenia dokumentu!");
                            if (error != null && error.getDocumentInformation() != null && error.getDocumentInformation().getTitle() != null) {
                                System.err.println("Tytuł dokumentu: " + error.getDocumentInformation().getTitle());
                            }
                        }
                    });
        } catch (Exception e) {
            System.err.println("Wystąpił błąd podczas generowania dokumentu: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Wyświetla pomoc dotyczącą używania programu
     */
    private static void printHelp() {
        System.out.println("Generowanie dokumentu magazynowego (Przyjęcie na magazyn - Pz)");
        System.out.println();
        System.out.println("Użycie: java -jar app.jar [opcje]");
        System.out.println();
        System.out.println("Opcje:");
        System.out.println("  --help, -h              Wyświetla tę pomoc");
        System.out.println("  --company, -c <tekst>   Nazwa firmy (domyślnie: Projektowanie i Wdrażanie)");
        System.out.println("  --desc, -d <tekst>      Opis firmy (domyślnie: Systemów Informatycznych)");
        System.out.println("  --address, -a <tekst>   Adres firmy - kod i miasto (domyślnie: 44-100 Gliwice)");
        System.out.println("  --street, -s <tekst>    Ulica (domyślnie: ul. Orlat Śląskich)");
        System.out.println("  --nip, -n <tekst>       NIP firmy (domyślnie: 631-132-20-90)");
        System.out.println("  --document, -dn <tekst> Numer dokumentu (domyślnie: Pz 1/2006)");
        System.out.println("  --reference, -r <tekst> Numer referencyjny (domyślnie: 123/06)");
        System.out.println("  --date, -dt <data>      Data dokumentu w formacie yyyy-MM-dd (domyślnie: 2006-02-28)");
        System.out.println("  --recipient, -rc <tekst> Nazwa odbiorcy (domyślnie: HURTOWNIA WIERTELKO)");
        System.out.println("  --receivedby, -rb <tekst> Osoba przyjmująca (domyślnie: Jacek Krywult)");
        System.out.println("  --output, -o <ścieżka>  Ścieżka wyjściowa pliku (domyślnie: receipt.pdf)");
        System.out.println("  --item, -i <indeks> <nazwa> <ilość> <jednostka> <wartość>");
        System.out.println("                         Dodaje pozycję do dokumentu. Można użyć wielokrotnie.");
        System.out.println("                         Ilość musi być liczbą całkowitą, wartość liczbą dziesiętną.");
        System.out.println();
        System.out.println("Przykłady:");
        System.out.println("  java -jar app.jar --output moj_dokument.pdf");
        System.out.println("  java -jar app.jar -dt 2025-01-15 -rc \"SKLEP ABC\" -o dokument.pdf");
        System.out.println("  java -jar app.jar -i \"ABC123\" \"WIERTARKA 500W\" 1 \"szt.\" 1200,50");
        System.out.println();
        System.out.println("Uwagi:");
        System.out.println("- Jeśli nie podano argumentów, użyte zostaną wartości domyślne");
        System.out.println("- Aby użyć wartości z spacjami, należy je ująć w cudzysłowy");
        System.out.println("- Wartości liczbowe mogą używać przecinka lub kropki jako separatora dziesiętnego");
    }
}