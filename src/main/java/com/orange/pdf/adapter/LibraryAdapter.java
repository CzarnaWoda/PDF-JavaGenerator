package com.orange.pdf.adapter;

import com.orange.pdf.builder.data.LibraryPdfTableItem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Adapter do konwertowania modeli książek z aplikacji do obiektów używanych przez bibliotekę generowania PDF.
 * Wykorzystuje generyki i interfejsy funkcyjne, aby uniknąć bezpośrednich zależności od konkretnych klas aplikacji.
 */
public class LibraryAdapter {

    /**
     * Konwertuje książkę z modelu aplikacji do modelu biblioteki PDF
     *
     * @param <B> typ obiektu książki w aplikacji
     * @param <A> typ obiektu autora w aplikacji
     * @param <P> typ obiektu wydawcy w aplikacji
     * @param book obiekt książki do konwersji
     * @param idExtractor ekstraktor ID książki
     * @param titleExtractor ekstraktor tytułu książki
     * @param authorsExtractor ekstraktor listy autorów książki
     * @param publisherExtractor ekstraktor wydawcy książki
     * @param statusExtractor ekstraktor statusu książki
     * @param genreExtractor ekstraktor gatunku książki
     * @param authorNameExtractor ekstraktor imienia i nazwiska autora
     * @param publisherNameExtractor ekstraktor nazwy wydawcy
     * @return obiekt LibraryPdfTableItem
     */
    public static <B, A, P> LibraryPdfTableItem convertToPdfItem(
            B book,
            IdExtractor<B> idExtractor,
            TitleExtractor<B> titleExtractor,
            AuthorsExtractor<B, A> authorsExtractor,
            PublisherExtractor<B, P> publisherExtractor,
            StatusExtractor<B> statusExtractor,
            GenreExtractor<B> genreExtractor,
            AuthorNameExtractor<A> authorNameExtractor,
            PublisherNameExtractor<P> publisherNameExtractor) {

        // Pobieranie danych z obiektu książki
        String id = idExtractor.extractId(book);
        String title = titleExtractor.extractTitle(book);
        List<A> authors = authorsExtractor.extractAuthors(book);
        P publisher = publisherExtractor.extractPublisher(book);
        String status = statusExtractor.extractStatus(book);
        String genre = genreExtractor.extractGenre(book);

        // Konwersja listy autorów do stringa
        String authorsString = authors.stream()
                .map(authorNameExtractor::extractName)
                .collect(Collectors.joining(", "));

        // Pobranie nazwy wydawcy (z obsługą null)
        String publisherName = publisher != null ?
                publisherNameExtractor.extractName(publisher) : "";

        return new LibraryPdfTableItem(id, title, authorsString, publisherName, status, genre);
    }

    /**
     * Zlicza książki według statusów
     *
     * @param <T> typ obiektu książki w aplikacji
     * @param books lista książek
     * @param statusExtractor ekstraktor statusu książki
     * @return mapa statusów i ich liczebności
     */
    public static <T> Map<String, Integer> countBooksByStatus(
            List<T> books,
            StatusExtractor<T> statusExtractor) {

        Map<String, Integer> counts = new HashMap<>();

        for (T book : books) {
            String status = statusExtractor.extractStatus(book);
            counts.put(status, counts.getOrDefault(status, 0) + 1);
        }

        return counts;
    }

    /**
     * Interfejs funkcyjny do pobierania ID książki
     *
     * @param <T> typ obiektu książki w aplikacji
     */
    @FunctionalInterface
    public interface IdExtractor<T> {
        String extractId(T book);
    }

    /**
     * Interfejs funkcyjny do pobierania tytułu książki
     *
     * @param <T> typ obiektu książki w aplikacji
     */
    @FunctionalInterface
    public interface TitleExtractor<T> {
        String extractTitle(T book);
    }

    /**
     * Interfejs funkcyjny do pobierania listy autorów książki
     *
     * @param <B> typ obiektu książki w aplikacji
     * @param <A> typ obiektu autora w aplikacji
     */
    @FunctionalInterface
    public interface AuthorsExtractor<B, A> {
        List<A> extractAuthors(B book);
    }

    /**
     * Interfejs funkcyjny do pobierania wydawcy książki
     *
     * @param <B> typ obiektu książki w aplikacji
     * @param <P> typ obiektu wydawcy w aplikacji
     */
    @FunctionalInterface
    public interface PublisherExtractor<B, P> {
        P extractPublisher(B book);
    }

    /**
     * Interfejs funkcyjny do pobierania statusu książki
     *
     * @param <T> typ obiektu książki w aplikacji
     */
    @FunctionalInterface
    public interface StatusExtractor<T> {
        String extractStatus(T book);
    }

    /**
     * Interfejs funkcyjny do pobierania gatunku książki
     *
     * @param <T> typ obiektu książki w aplikacji
     */
    @FunctionalInterface
    public interface GenreExtractor<T> {
        String extractGenre(T book);
    }

    /**
     * Interfejs funkcyjny do pobierania imienia i nazwiska autora
     *
     * @param <T> typ obiektu autora w aplikacji
     */
    @FunctionalInterface
    public interface AuthorNameExtractor<T> {
        String extractName(T author);
    }

    /**
     * Interfejs funkcyjny do pobierania nazwy wydawcy
     *
     * @param <T> typ obiektu wydawcy w aplikacji
     */
    @FunctionalInterface
    public interface PublisherNameExtractor<T> {
        String extractName(T publisher);
    }
}