package com.function.repository;

import com.function.config.DatabaseConfig;
import com.function.models.Book;
import com.function.models.Book.BookStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Repositorio para operaciones CRUD de libros.
 * Gestiona la persistencia de libros en la base de datos Oracle.
 */
public class BookRepository {
    private static final Logger logger = Logger.getLogger(BookRepository.class.getName());

    /**
     * Obtiene todos los libros del catálogo.
     * 
     * @return lista de todos los libros ordenados por ID
     */
    public List<Book> findAll() {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT id, title, author, publisher, publication_year, language, isbn, genre, description, status FROM books ORDER BY id";

        try (Connection conn = DatabaseConfig.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                books.add(mapResultSetToBook(rs));
            }
            logger.info("Found " + books.size() + " books");
        } catch (SQLException e) {
            logger.severe("Error fetching all books: " + e.getMessage());
            throw new RuntimeException("Error fetching books", e);
        }

        return books;
    }

    /**
     * Busca un libro por su ID.
     * 
     * @param id identificador del libro
     * @return Optional con el libro si existe, vacío en caso contrario
     */
    public Optional<Book> findById(String id) {
        String sql = "SELECT id, title, author, publisher, publication_year, language, isbn, genre, description, status FROM books WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToBook(rs));
            }
        } catch (SQLException e) {
            logger.severe("Error fetching book by id: " + e.getMessage());
            throw new RuntimeException("Error fetching book by id", e);
        }

        return Optional.empty();
    }

    /**
     * Guarda un libro en la base de datos.
     * Si el libro tiene ID y existe, lo actualiza; en caso contrario, crea uno
     * nuevo.
     * 
     * @param book libro a guardar
     * @return libro guardado con su ID asignado
     */
    public Book save(Book book) {
        if (book.getId() != null && !book.getId().isEmpty() && findById(book.getId()).isPresent()) {
            // Update existing book
            String sql = "UPDATE books SET title = ?, author = ?, publisher = ?, publication_year = ?, " +
                    "language = ?, isbn = ?, genre = ?, description = ?, status = ? WHERE id = ?";

            try (Connection conn = DatabaseConfig.getConnection();
                    PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, book.getTitle());
                pstmt.setString(2, book.getAuthor());
                pstmt.setString(3, book.getPublisher());
                pstmt.setInt(4, book.getPublicationYear());
                pstmt.setString(5, book.getLanguage());
                pstmt.setString(6, book.getIsbn());
                pstmt.setString(7, book.getGenre());
                pstmt.setString(8, book.getDescription());
                pstmt.setString(9, book.getStatus().name());
                pstmt.setString(10, book.getId());

                int rowsAffected = pstmt.executeUpdate();
                logger.info("Updated book with id: " + book.getId() + " (" + rowsAffected + " rows)");
            } catch (SQLException e) {
                logger.severe("Error updating book: " + e.getMessage());
                throw new RuntimeException("Error updating book", e);
            }
        } else {
            String sql = "INSERT INTO books (id, title, author, publisher, publication_year, language, isbn, genre, description, status) "
                    +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            try (Connection conn = DatabaseConfig.getConnection();
                    PreparedStatement pstmt = conn.prepareStatement(sql)) {

                if (book.getId() == null || book.getId().isEmpty()) {
                    book.setId(getNextId());
                }

                pstmt.setString(1, book.getId());
                pstmt.setString(2, book.getTitle());
                pstmt.setString(3, book.getAuthor());
                pstmt.setString(4, book.getPublisher());
                pstmt.setInt(5, book.getPublicationYear());
                pstmt.setString(6, book.getLanguage());
                pstmt.setString(7, book.getIsbn());
                pstmt.setString(8, book.getGenre());
                pstmt.setString(9, book.getDescription());
                pstmt.setString(10, book.getStatus().name());

                pstmt.executeUpdate();
                logger.info("Created new book with id: " + book.getId());
            } catch (SQLException e) {
                logger.severe("Error creating book: " + e.getMessage());
                throw new RuntimeException("Error creating book", e);
            }
        }

        return book;
    }

    /**
     * Elimina un libro por su ID.
     * 
     * @param id identificador del libro a eliminar
     * @return true si se eliminó, false si no se encontró
     */
    public boolean deleteById(String id) {
        String sql = "DELETE FROM books WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);
            int rowsAffected = pstmt.executeUpdate();
            logger.info("Deleted book with id: " + id + " (" + rowsAffected + " rows)");
            return rowsAffected > 0;

        } catch (SQLException e) {
            logger.severe("Error deleting book: " + e.getMessage());
            throw new RuntimeException("Error deleting book", e);
        }
    }

    /**
     * Obtiene todos los libros disponibles para préstamo.
     * 
     * @return lista de libros con estado AVAILABLE ordenados por título
     */
    public List<Book> findAvailableBooks() {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT id, title, author, publisher, publication_year, language, isbn, genre, description, status "
                +
                "FROM books WHERE status = 'AVAILABLE' ORDER BY title";

        try (Connection conn = DatabaseConfig.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                books.add(mapResultSetToBook(rs));
            }
            logger.info("Found " + books.size() + " available books");
        } catch (SQLException e) {
            logger.severe("Error fetching available books: " + e.getMessage());
            throw new RuntimeException("Error fetching available books", e);
        }

        return books;
    }

    /**
     * Actualiza el estado de disponibilidad de un libro.
     * 
     * @param id     identificador del libro
     * @param status nuevo estado del libro
     * @return true si se actualizó, false si no se encontró el libro
     */
    public boolean updateStatus(String id, BookStatus status) {
        String sql = "UPDATE books SET status = ? WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status.name());
            pstmt.setString(2, id);

            int rowsAffected = pstmt.executeUpdate();
            logger.info("Updated book status for id: " + id + " to " + status + " (" + rowsAffected + " rows)");
            return rowsAffected > 0;

        } catch (SQLException e) {
            logger.severe("Error updating book status: " + e.getMessage());
            throw new RuntimeException("Error updating book status", e);
        }
    }

    /**
     * Obtiene el siguiente ID disponible para un nuevo libro.
     * 
     * @return siguiente ID como String
     */
    public String getNextId() {
        String sql = "SELECT NVL(MAX(TO_NUMBER(id)), 0) + 1 as next_id FROM books";

        try (Connection conn = DatabaseConfig.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return String.valueOf(rs.getInt("next_id"));
            }
        } catch (SQLException e) {
            logger.severe("Error getting next book id: " + e.getMessage());
            throw new RuntimeException("Error getting next book id", e);
        }

        return "1";
    }

    /**
     * Mapea un ResultSet a un objeto Book.
     * 
     * @param rs ResultSet con los datos del libro
     * @return objeto Book poblado con los datos
     * @throws SQLException si hay error al leer el ResultSet
     */
    private Book mapResultSetToBook(ResultSet rs) throws SQLException {
        String statusStr = rs.getString("status");
        BookStatus status = BookStatus.valueOf(statusStr);

        return new Book(
                rs.getString("id"),
                rs.getString("title"),
                rs.getString("author"),
                rs.getString("publisher"),
                rs.getInt("publication_year"),
                rs.getString("language"),
                rs.getString("isbn"),
                rs.getString("genre"),
                rs.getString("description"),
                status);
    }
}
