package com.orange.pdf.overdue.adapter;

import com.orange.pdf.overdue.data.OverduePdfTableItem;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Adapter do konwertowania modeli zalegających wypożyczeń z aplikacji do obiektów używanych przez bibliotekę generowania PDF.
 * Wykorzystuje generyki i interfejsy funkcyjne, aby uniknąć bezpośrednich zależności od konkretnych klas aplikacji.
 */
public class OverdueAdapter {

    /**
     * Konwertuje zalegające wypożyczenie z modelu aplikacji do modelu biblioteki PDF
     */
    public static <L, B, U, A, P> OverduePdfTableItem convertToOverdueItem(
            L loan, B book, U user,
            LoanIdExtractor<L> loanIdExtractor,
            BookIdExtractor<B> bookIdExtractor,
            TitleExtractor<B> titleExtractor,
            AuthorsExtractor<B, A> authorsExtractor,
            PublisherExtractor<B, P> publisherExtractor,
            GenreExtractor<B> genreExtractor,
            UserIdExtractor<U> userIdExtractor,
            UserNameExtractor<U> userNameExtractor,
            UserEmailExtractor<U> userEmailExtractor,
            UserPhoneExtractor<U> userPhoneExtractor,
            BorrowedAtExtractor<L> borrowedAtExtractor,
            DueDateExtractor<L> dueDateExtractor,
            LibrarianIdExtractor<L> librarianIdExtractor,
            AuthorNameExtractor<A> authorNameExtractor,
            PublisherNameExtractor<P> publisherNameExtractor) {

        // Pobieranie danych z wypożyczenia
        String loanId = loanIdExtractor.extractLoanId(loan);
        Instant borrowedAt = borrowedAtExtractor.extractBorrowedAt(loan);
        Instant dueDate = dueDateExtractor.extractDueDate(loan);
        String librarianId = librarianIdExtractor.extractLibrarianId(loan);

        // Pobieranie danych z książki
        String bookId = bookIdExtractor.extractBookId(book);
        String title = titleExtractor.extractTitle(book);
        List<A> authors = authorsExtractor.extractAuthors(book);
        P publisher = publisherExtractor.extractPublisher(book);
        String genre = genreExtractor.extractGenre(book);

        // Pobieranie danych z użytkownika
        String userId = userIdExtractor.extractUserId(user);
        String userName = userNameExtractor.extractUserName(user);
        String userEmail = userEmailExtractor.extractUserEmail(user);
        String userPhone = userPhoneExtractor.extractUserPhone(user);

        // Konwersja listy autorów do stringa
        String authorsString = authors.stream()
                .map(authorNameExtractor::extractName)
                .collect(Collectors.joining(", "));

        // Pobranie nazwy wydawcy (z obsługą null)
        String publisherName = publisher != null ?
                publisherNameExtractor.extractName(publisher) : "";

        return new OverduePdfTableItem(
                loanId, bookId, title, authorsString, publisherName, genre,
                userId, userName, userEmail,
                borrowedAt, dueDate, librarianId
        );
    }

    // Interfejsy funkcyjne dla ekstraktora danych z wypożyczeń
    @FunctionalInterface
    public interface LoanIdExtractor<T> { String extractLoanId(T loan); }

    @FunctionalInterface
    public interface BorrowedAtExtractor<T> { Instant extractBorrowedAt(T loan); }

    @FunctionalInterface
    public interface DueDateExtractor<T> { Instant extractDueDate(T loan); }

    @FunctionalInterface
    public interface LibrarianIdExtractor<T> { String extractLibrarianId(T loan); }

    // Interfejsy funkcyjne dla ekstraktora danych z książek
    @FunctionalInterface
    public interface BookIdExtractor<T> { String extractBookId(T book); }

    @FunctionalInterface
    public interface TitleExtractor<T> { String extractTitle(T book); }

    @FunctionalInterface
    public interface AuthorsExtractor<B, A> { List<A> extractAuthors(B book); }

    @FunctionalInterface
    public interface PublisherExtractor<B, P> { P extractPublisher(B book); }

    @FunctionalInterface
    public interface GenreExtractor<T> { String extractGenre(T book); }

    // Interfejsy funkcyjne dla ekstraktora danych z użytkowników
    @FunctionalInterface
    public interface UserIdExtractor<T> { String extractUserId(T user); }

    @FunctionalInterface
    public interface UserNameExtractor<T> { String extractUserName(T user); }

    @FunctionalInterface
    public interface UserEmailExtractor<T> { String extractUserEmail(T user); }

    @FunctionalInterface
    public interface UserPhoneExtractor<T> { String extractUserPhone(T user); }

    // Interfejsy pomocnicze
    @FunctionalInterface
    public interface AuthorNameExtractor<T> { String extractName(T author); }

    @FunctionalInterface
    public interface PublisherNameExtractor<T> { String extractName(T publisher); }
}