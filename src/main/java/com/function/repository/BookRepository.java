package com.function.repository;

import com.function.models.Book;
import com.function.models.Book.BookStatus;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class BookRepository {
    private static List<Book> books = new ArrayList<>();

    // Initialize with static data
    static {
        books.add(new Book("1", "One Hundred Years of Solitude", "Gabriel García Márquez",
                "Editorial Sudamericana", 1967, "Spanish", "978-0307474728",
                "Magical Realism", "Masterpiece of magical realism", BookStatus.AVAILABLE));

        books.add(new Book("2", "1984", "George Orwell",
                "Secker & Warburg", 1949, "English", "978-0451524935",
                "Dystopia", "Dystopian novel about totalitarianism", BookStatus.AVAILABLE));

        books.add(new Book("3", "Don Quixote", "Miguel de Cervantes",
                "Francisco de Robles", 1605, "Spanish", "978-8424936464",
                "Novel", "The most outstanding work of Spanish literature", BookStatus.AVAILABLE));

        books.add(new Book("4", "The Little Prince", "Antoine de Saint-Exupéry",
                "Reynal & Hitchcock", 1943, "French", "978-0156012195",
                "Fable", "Short novel about a little prince", BookStatus.BORROWED));

        books.add(new Book("5", "Harry Potter and the Philosopher's Stone", "J.K. Rowling",
                "Bloomsbury", 1997, "English", "978-0439708180",
                "Fantasy", "First installment of the Harry Potter saga", BookStatus.AVAILABLE));
    }

    // Get all books
    public List<Book> findAll() {
        return new ArrayList<>(books);
    }

    // Get book by ID
    public Optional<Book> findById(String id) {
        return books.stream()
                .filter(book -> book.getId().equals(id))
                .findFirst();
    }

    // Create new book
    public Book save(Book book) {
        // Check if book already exists
        Optional<Book> existing = findById(book.getId());
        if (existing.isPresent()) {
            // Update existing book
            books.remove(existing.get());
        }
        books.add(book);
        return book;
    }

    // Delete book
    public boolean deleteById(String id) {
        return books.removeIf(book -> book.getId().equals(id));
    }

    // Get available books
    public List<Book> findAvailableBooks() {
        return books.stream()
                .filter(book -> book.getStatus() == BookStatus.AVAILABLE)
                .collect(Collectors.toList());
    }

    // Update book status
    public boolean updateStatus(String id, BookStatus status) {
        Optional<Book> book = findById(id);
        if (book.isPresent()) {
            book.get().setStatus(status);
            return true;
        }
        return false;
    }

    // Get next ID for new books
    public String getNextId() {
        return String.valueOf(books.size() + 1);
    }
}
