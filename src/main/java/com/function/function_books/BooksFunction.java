package com.function.function_books;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;
import com.function.models.Book;
import com.function.models.Book.BookStatus;
import com.function.repository.BookRepository;
import com.google.gson.Gson;

import java.util.Optional;

public class BooksFunction {
    private final BookRepository bookRepository = new BookRepository();
    private final Gson gson = new Gson();

    /**
     * Get all books
     * GET /api/books
     */
    @FunctionName("GetAllBooks")
    public HttpResponseMessage getAllBooks(
            @HttpTrigger(name = "req", methods = {
                    HttpMethod.GET }, authLevel = AuthorizationLevel.ANONYMOUS, route = "books") HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        context.getLogger().info("Getting all books");

        try {
            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(gson.toJson(bookRepository.findAll()))
                    .build();
        } catch (Exception e) {
            context.getLogger().severe("Error getting books: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error getting books: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Get book by ID
     * GET /api/books/{id}
     */
    @FunctionName("GetBookById")
    public HttpResponseMessage getBookById(
            @HttpTrigger(name = "req", methods = {
                    HttpMethod.GET }, authLevel = AuthorizationLevel.ANONYMOUS, route = "books/{id}") HttpRequestMessage<Optional<String>> request,
            @BindingName("id") String id,
            final ExecutionContext context) {

        context.getLogger().info("Getting book with ID: " + id);

        Optional<Book> book = bookRepository.findById(id);

        if (book.isPresent()) {
            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(gson.toJson(book.get()))
                    .build();
        } else {
            return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                    .body("Book not found with ID: " + id)
                    .build();
        }
    }

    /**
     * Get available books
     * GET /api/books/available
     */
    @FunctionName("GetAvailableBooks")
    public HttpResponseMessage getAvailableBooks(
            @HttpTrigger(name = "req", methods = {
                    HttpMethod.GET }, authLevel = AuthorizationLevel.ANONYMOUS, route = "books/status/available") HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        context.getLogger().info("Getting available books");

        try {
            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(gson.toJson(bookRepository.findAvailableBooks()))
                    .build();
        } catch (Exception e) {
            context.getLogger().severe("Error getting available books: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error getting available books: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Create new book
     * POST /api/books
     */
    @FunctionName("CreateBook")
    public HttpResponseMessage createBook(
            @HttpTrigger(name = "req", methods = {
                    HttpMethod.POST }, authLevel = AuthorizationLevel.ANONYMOUS, route = "books") HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        context.getLogger().info("Creating new book");

        try {
            String body = request.getBody().orElse("");

            if (body.isEmpty()) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .body("Request body is required")
                        .build();
            }

            Book book = gson.fromJson(body, Book.class);

            // Generate ID if not provided
            if (book.getId() == null || book.getId().isEmpty()) {
                book.setId(bookRepository.getNextId());
            }

            // Set default status if not provided
            if (book.getStatus() == null) {
                book.setStatus(BookStatus.AVAILABLE);
            }

            Book savedBook = bookRepository.save(book);

            return request.createResponseBuilder(HttpStatus.CREATED)
                    .header("Content-Type", "application/json")
                    .body(gson.toJson(savedBook))
                    .build();
        } catch (Exception e) {
            context.getLogger().severe("Error creating book: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating book: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Update book
     * PUT /api/books/{id}
     */
    @FunctionName("UpdateBook")
    public HttpResponseMessage updateBook(
            @HttpTrigger(name = "req", methods = {
                    HttpMethod.PUT }, authLevel = AuthorizationLevel.ANONYMOUS, route = "books/{id}") HttpRequestMessage<Optional<String>> request,
            @BindingName("id") String id,
            final ExecutionContext context) {

        context.getLogger().info("Updating book with ID: " + id);

        try {
            // Check if book exists
            Optional<Book> existingBook = bookRepository.findById(id);
            if (!existingBook.isPresent()) {
                return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                        .body("Book not found with ID: " + id)
                        .build();
            }

            String body = request.getBody().orElse("");

            if (body.isEmpty()) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .body("Request body is required")
                        .build();
            }

            Book book = gson.fromJson(body, Book.class);
            book.setId(id); // Ensure ID from URL is used

            Book updatedBook = bookRepository.save(book);

            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(gson.toJson(updatedBook))
                    .build();
        } catch (Exception e) {
            context.getLogger().severe("Error updating book: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating book: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Update book status
     * PATCH /api/books/{id}/status
     */
    @FunctionName("UpdateBookStatus")
    public HttpResponseMessage updateBookStatus(
            @HttpTrigger(name = "req", methods = {
                    HttpMethod.PATCH }, authLevel = AuthorizationLevel.ANONYMOUS, route = "books/{id}/status") HttpRequestMessage<Optional<String>> request,
            @BindingName("id") String id,
            final ExecutionContext context) {

        context.getLogger().info("Updating status for book ID: " + id);

        try {
            String statusParam = request.getQueryParameters().get("status");

            if (statusParam == null || statusParam.isEmpty()) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .body("Status parameter is required")
                        .build();
            }

            BookStatus status = BookStatus.valueOf(statusParam.toUpperCase());
            boolean updated = bookRepository.updateStatus(id, status);

            if (updated) {
                Optional<Book> book = bookRepository.findById(id);
                return request.createResponseBuilder(HttpStatus.OK)
                        .header("Content-Type", "application/json")
                        .body(gson.toJson(book.get()))
                        .build();
            } else {
                return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                        .body("Book not found with ID: " + id)
                        .build();
            }
        } catch (IllegalArgumentException e) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("Invalid status. Valid values: AVAILABLE, BORROWED, RESERVED")
                    .build();
        } catch (Exception e) {
            context.getLogger().severe("Error updating book status: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating book status: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Delete book
     * DELETE /api/books/{id}
     */
    @FunctionName("DeleteBook")
    public HttpResponseMessage deleteBook(
            @HttpTrigger(name = "req", methods = {
                    HttpMethod.DELETE }, authLevel = AuthorizationLevel.ANONYMOUS, route = "books/{id}") HttpRequestMessage<Optional<String>> request,
            @BindingName("id") String id,
            final ExecutionContext context) {

        context.getLogger().info("Deleting book with ID: " + id);

        boolean deleted = bookRepository.deleteById(id);

        if (deleted) {
            return request.createResponseBuilder(HttpStatus.OK)
                    .body("Book deleted successfully")
                    .build();
        } else {
            return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                    .body("Book not found with ID: " + id)
                    .build();
        }
    }
}
