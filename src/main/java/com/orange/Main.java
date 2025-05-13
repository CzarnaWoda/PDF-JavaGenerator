package com.orange;

import com.orange.pdf.builder.data.LibraryPdfTableItem;
import com.orange.pdf.service.LibraryPdfService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Klasa główna do generowania raportów bibliotecznych.
 * Można ją uruchomić z wiersza poleceń, aby wygenerować raport w formacie PDF.
 */
public class Main {
    public static void main(String[] args) {
        // Domyślne wartości
        String libraryName = "Biblioteka Miejska";
        String libraryDesc = "System Zarządzania Księgozbiorem";
        String address = "ul. Akademicka 16";
        String city = "44-100 Gliwice";
        String reportNumber = "INV-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-001";
        LocalDate reportDate = LocalDate.now();
        String generatedBy = "Administrator";
        String outputPath = "library-report.pdf";
        String reportType = "inventory"; // domyślnie raport inwentaryzacyjny
        String genre = null;     // filtr gatunku (domyślnie brak)
        String status = null;    // filtr statusu (domyślnie brak)
        String publisher = null; // filtr wydawcy (domyślnie brak)
        LocalDate startDate = null; // data początkowa zakresu (domyślnie brak)
        LocalDate endDate = null;   // data końcowa zakresu (domyślnie brak)
        boolean useSampleLoanCounts = false; // czy używać próbkowych danych o wypożyczeniach

        List<LibraryPdfTableItem> books = new ArrayList<>();
        Map<String, Integer> statusCounts = new HashMap<>();
        Map<String, Integer> genreCounts = new HashMap<>();
        Map<String, Integer> publisherCounts = new HashMap<>();
        Map<String, Integer> loanCounts = new HashMap<>();

        // Domyślne przykładowe książki z rozszerzonym konstruktorem dla uwzględnienia opisu
        books.add(new LibraryPdfTableItem("1001", "Władca Pierścieni", "J.R.R. Tolkien", "Czytelnik", "Dostępna", "Fantasy", "Klasyka literatury fantasy opowiadająca o wyprawie hobbita Frodo Bagginsa."));
        books.add(new LibraryPdfTableItem("1002", "Harry Potter i Kamień Filozoficzny", "J.K. Rowling", "Media Rodzina", "Wypożyczona", "Fantasy", "Pierwsza część przygód młodego czarodzieja."));
        books.add(new LibraryPdfTableItem("1003", "Wiedźmin: Ostatnie życzenie", "Andrzej Sapkowski", "SuperNowa", "Dostępna", "Fantasy", "Zbiór opowiadań o przygodach wiedźmina Geralta z Rivii."));
        books.add(new LibraryPdfTableItem("1004", "Pan Tadeusz", "Adam Mickiewicz", "Ossolineum", "Dostępna", "Klasyka", "Polska epopeja narodowa z 1834 roku."));
        books.add(new LibraryPdfTableItem("1005", "Lalka", "Bolesław Prus", "PIW", "Wypożyczona", "Klasyka", "Powieść społeczno-obyczajowa z 1890 roku."));
        books.add(new LibraryPdfTableItem("1006", "Gra o Tron", "George R.R. Martin", "Zysk i S-ka", "Wypożyczona", "Fantasy", "Pierwsza część sagi Pieśń Lodu i Ognia."));
        books.add(new LibraryPdfTableItem("1007", "Solaris", "Stanisław Lem", "Wydawnictwo Literackie", "Dostępna", "Sci-Fi", "Powieść science fiction o kontakcie z obcą inteligencją."));
        books.add(new LibraryPdfTableItem("1008", "Duma i uprzedzenie", "Jane Austen", "Prószyński i S-ka", "Dostępna", "Klasyka", "Klasyczna powieść obyczajowa."));
        books.add(new LibraryPdfTableItem("1009", "Hobbit", "J.R.R. Tolkien", "Iskry", "Wypożyczona", "Fantasy", "Powieść fantasy poprzedzająca Władcę Pierścieni."));
        books.add(new LibraryPdfTableItem("1010", "Firma", "John Grisham", "Albatros", "Dostępna", "Thriller", "Thriller prawniczy."));

        // Domyślne liczby statusów
        statusCounts.put("Dostępna", 6);
        statusCounts.put("Wypożyczona", 4);
        statusCounts.put("Zarezerwowana", 0);

        // Domyślne liczby gatunków
        genreCounts.put("Fantasy", 4);
        genreCounts.put("Klasyka", 3);
        genreCounts.put("Sci-Fi", 1);
        genreCounts.put("Thriller", 1);

        // Domyślne liczby wydawców
        publisherCounts.put("Czytelnik", 1);
        publisherCounts.put("Media Rodzina", 1);
        publisherCounts.put("SuperNowa", 1);
        publisherCounts.put("Ossolineum", 1);
        publisherCounts.put("PIW", 1);
        publisherCounts.put("Zysk i S-ka", 1);
        publisherCounts.put("Wydawnictwo Literackie", 1);
        publisherCounts.put("Prószyński i S-ka", 1);
        publisherCounts.put("Iskry", 1);
        publisherCounts.put("Albatros", 1);

        // Domyślne liczby wypożyczeń dla raportu popularności
        loanCounts.put("1001", 45); // Władca Pierścieni
        loanCounts.put("1002", 78); // Harry Potter
        loanCounts.put("1003", 62); // Wiedźmin
        loanCounts.put("1004", 23); // Pan Tadeusz
        loanCounts.put("1005", 18); // Lalka
        loanCounts.put("1006", 89); // Gra o Tron
        loanCounts.put("1007", 31); // Solaris
        loanCounts.put("1008", 27); // Duma i uprzedzenie
        loanCounts.put("1009", 56); // Hobbit
        loanCounts.put("1010", 19); // Firma

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
                        case "--library", "-l" -> {
                            if (i + 1 < args.length) libraryName = args[++i];
                            else {
                                System.err.println("Błąd: Brak wartości dla parametru --library");
                                printHelp();
                                return;
                            }
                        }
                        case "--desc", "-d" -> {
                            if (i + 1 < args.length) libraryDesc = args[++i];
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
                        case "--city", "-c" -> {
                            if (i + 1 < args.length) city = args[++i];
                            else {
                                System.err.println("Błąd: Brak wartości dla parametru --city");
                                printHelp();
                                return;
                            }
                        }
                        case "--report", "-r" -> {
                            if (i + 1 < args.length) reportNumber = args[++i];
                            else {
                                System.err.println("Błąd: Brak wartości dla parametru --report");
                                printHelp();
                                return;
                            }
                        }
                        case "--date", "-dt" -> {
                            if (i + 1 < args.length) {
                                try {
                                    reportDate = LocalDate.parse(args[++i], DateTimeFormatter.ofPattern("yyyy-MM-dd"));
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
                        case "--by", "-b" -> {
                            if (i + 1 < args.length) generatedBy = args[++i];
                            else {
                                System.err.println("Błąd: Brak wartości dla parametru --by");
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
                        case "--type", "-t" -> {
                            if (i + 1 < args.length) {
                                reportType = args[++i].toLowerCase();
                                if (!reportType.equals("inventory") && !reportType.equals("borrowed") &&
                                        !reportType.equals("filtered") && !reportType.equals("popularity")) {
                                    System.err.println("Błąd: Nieznany typ raportu: " + reportType);
                                    printHelp();
                                    return;
                                }
                            } else {
                                System.err.println("Błąd: Brak wartości dla parametru --type");
                                printHelp();
                                return;
                            }
                        }
                        case "--genre", "-g" -> {
                            if (i + 1 < args.length) genre = args[++i];
                            else {
                                System.err.println("Błąd: Brak wartości dla parametru --genre");
                                printHelp();
                                return;
                            }
                        }
                        case "--status", "-st" -> {
                            if (i + 1 < args.length) status = args[++i];
                            else {
                                System.err.println("Błąd: Brak wartości dla parametru --status");
                                printHelp();
                                return;
                            }
                        }
                        case "--publisher", "-p" -> {
                            if (i + 1 < args.length) publisher = args[++i];
                            else {
                                System.err.println("Błąd: Brak wartości dla parametru --publisher");
                                printHelp();
                                return;
                            }
                        }
                        case "--start-date", "-sd" -> {
                            if (i + 1 < args.length) {
                                try {
                                    startDate = LocalDate.parse(args[++i], DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                                } catch (DateTimeParseException e) {
                                    System.err.println("Błąd: Nieprawidłowy format daty początkowej. Użyj formatu: yyyy-MM-dd");
                                    printHelp();
                                    return;
                                }
                            } else {
                                System.err.println("Błąd: Brak wartości dla parametru --start-date");
                                printHelp();
                                return;
                            }
                        }
                        case "--end-date", "-ed" -> {
                            if (i + 1 < args.length) {
                                try {
                                    endDate = LocalDate.parse(args[++i], DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                                } catch (DateTimeParseException e) {
                                    System.err.println("Błąd: Nieprawidłowy format daty końcowej. Użyj formatu: yyyy-MM-dd");
                                    printHelp();
                                    return;
                                }
                            } else {
                                System.err.println("Błąd: Brak wartości dla parametru --end-date");
                                printHelp();
                                return;
                            }
                        }
                        case "--book", "-bk" -> {
                            if (i + 7 < args.length) {
                                String bookId = args[++i];
                                String title = args[++i];
                                String authors = args[++i];
                                String bookPublisher = args[++i];
                                String bookStatus = args[++i];
                                String bookGenre = args[++i];
                                String description = args[++i];

                                // Jeśli dodajemy pierwszą książkę za pomocą parametrów, wyczyść domyślne książki
                                if (!books.isEmpty() && books.get(0).getBookId().equals("1001")) {
                                    books.clear();
                                    statusCounts.clear();
                                    genreCounts.clear();
                                    publisherCounts.clear();
                                    loanCounts.clear();
                                }

                                books.add(new LibraryPdfTableItem(bookId, title, authors, bookPublisher, bookStatus, bookGenre, description));

                                // Aktualizacja liczników
                                statusCounts.put(bookStatus, statusCounts.getOrDefault(bookStatus, 0) + 1);
                                genreCounts.put(bookGenre, genreCounts.getOrDefault(bookGenre, 0) + 1);
                                publisherCounts.put(bookPublisher, publisherCounts.getOrDefault(bookPublisher, 0) + 1);

                                // Dodajemy losową liczbę wypożyczeń dla raportu popularności
                                Random random = new Random();
                                loanCounts.put(bookId, random.nextInt(100));
                            } else {
                                System.err.println("Błąd: Niewystarczająca liczba wartości dla parametru --book");
                                printHelp();
                                return;
                            }
                        }
                        case "--loan-count", "-lc" -> {
                            if (i + 2 < args.length) {
                                String bookId = args[++i];
                                try {
                                    int count = Integer.parseInt(args[++i]);
                                    loanCounts.put(bookId, count);
                                } catch (NumberFormatException e) {
                                    System.err.println("Błąd: Liczba wypożyczeń musi być liczbą całkowitą");
                                    printHelp();
                                    return;
                                }
                            } else {
                                System.err.println("Błąd: Niewystarczająca liczba wartości dla parametru --loan-count");
                                printHelp();
                                return;
                            }
                        }
                        case "--use-sample-data", "-usd" -> {
                            useSampleLoanCounts = true;
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
            System.out.println("Generowanie raportu bibliotecznego PDF...");
            LibraryPdfService pdfService = new LibraryPdfService();

            if (reportType.equals("inventory")) {
                pdfService.generateInventoryReport(
                        libraryName,
                        libraryDesc,
                        address,
                        city,
                        reportNumber,
                        reportDate,
                        books,
                        statusCounts,
                        genreCounts,
                        publisherCounts,
                        outputPath,
                        generatedBy
                );
            } else if (reportType.equals("borrowed")) {
                pdfService.generateBorrowedBooksReport(
                        books,
                        outputPath,
                        generatedBy
                );
            } else if (reportType.equals("filtered")) {
                pdfService.generateFilteredReport(
                        books,
                        genre,
                        status,
                        publisher,
                        outputPath,
                        generatedBy
                );
            } else if (reportType.equals("popularity")) {
                // Używamy metody z przekazaniem danych o wypożyczeniach
                    pdfService.generatePopularityReport(
                            books,
                            loanCounts,
                            genre,
                            publisher,
                            startDate,
                            endDate,
                            outputPath,
                            generatedBy
                    );
            }

            System.out.println("Raport biblioteczny został pomyślnie wygenerowany: " + outputPath);
        } catch (Exception e) {
            System.err.println("Wystąpił błąd podczas generowania raportu: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Wyświetla pomoc dotyczącą używania programu
     */
    private static void printHelp() {
        System.out.println("Generator raportów bibliotecznych PDF");
        System.out.println();
        System.out.println("Użycie: java -jar pdfjava-generator.jar [opcje]");
        System.out.println();
        System.out.println("Opcje:");
        System.out.println("  --help, -h                Wyświetla tę pomoc");
        System.out.println("  --library, -l <tekst>     Nazwa biblioteki");
        System.out.println("  --desc, -d <tekst>        Opis biblioteki");
        System.out.println("  --address, -a <tekst>     Adres - ulica");
        System.out.println("  --city, -c <tekst>        Miasto i kod pocztowy");
        System.out.println("  --report, -r <tekst>      Numer raportu");
        System.out.println("  --date, -dt <data>        Data raportu w formacie yyyy-MM-dd");
        System.out.println("  --by, -b <tekst>          Osoba generująca raport");
        System.out.println("  --output, -o <ścieżka>    Ścieżka wyjściowa pliku PDF");
        System.out.println("  --type, -t <typ>          Typ raportu: 'inventory', 'borrowed', 'filtered' lub 'popularity'");
        System.out.println("  --genre, -g <tekst>       Filtr gatunku (dla typu filtered/popularity)");
        System.out.println("  --status, -st <tekst>     Filtr statusu (dla typu filtered)");
        System.out.println("  --publisher, -p <tekst>   Filtr wydawcy (dla typu filtered/popularity)");
        System.out.println("  --start-date, -sd <data>  Data początkowa zakresu w formacie yyyy-MM-dd (dla typu popularity)");
        System.out.println("  --end-date, -ed <data>    Data końcowa zakresu w formacie yyyy-MM-dd (dla typu popularity)");
        System.out.println("  --book, -bk <id> <tytuł> <autor> <wydawca> <status> <gatunek> <opis>");
        System.out.println("                            Dodaje książkę do raportu. Można użyć wielokrotnie.");
        System.out.println("  --loan-count, -lc <id> <liczba>");
        System.out.println("                            Określa liczbę wypożyczeń dla książki (dla typu popularity).");
        System.out.println("  --use-sample-data, -usd   Używa przykładowych danych o wypożyczeniach generowanych automatycznie");
        System.out.println();
        System.out.println("Przykłady:");
        System.out.println("  java -jar pdfjava-generator.jar --output raport_biblioteka.pdf");
        System.out.println("  java -jar pdfjava-generator.jar -t borrowed -o wypozyczone.pdf");
        System.out.println("  java -jar pdfjava-generator.jar -t filtered -g Fantasy -o fantasy_books.pdf");
        System.out.println("  java -jar pdfjava-generator.jar -t popularity -g Fantasy -sd 2023-01-01 -ed 2023-12-31 -o popularity_report.pdf");
        System.out.println("  java -jar pdfjava-generator.jar -t popularity -usd -o popularity_sample_data.pdf");
        System.out.println("  java -jar pdfjava-generator.jar -bk \"2001\" \"Hobbit\" \"J.R.R. Tolkien\" \"Iskry\" \"Dostępna\" \"Fantasy\" \"Powieść fantasy\" -lc \"2001\" 42");
        System.out.println();
        System.out.println("Uwagi:");
        System.out.println("- Jeśli nie podano argumentów, użyte zostaną wartości domyślne");
        System.out.println("- Aby użyć wartości z spacjami, należy je ująć w cudzysłowy");
    }
}