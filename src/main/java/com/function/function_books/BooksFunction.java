package com.function.function_books;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;
import com.function.models.Book;
import com.function.models.Book.BookStatus;
import com.function.repository.BookRepository;
import com.google.gson.Gson;

import java.util.Optional;

/**
 * Funciones HTTP para gestionar libros del catálogo de la biblioteca.
 * Expone endpoints REST para operaciones CRUD de libros.
 */
public class BooksFunction {
    private final BookRepository bookRepository = new BookRepository();
    private final Gson gson = new Gson();

    /**
     * Obtiene todos los libros del catálogo.
     * 
     * @param request solicitud HTTP
     * @param context contexto de ejecución de Azure Functions
     * @return respuesta HTTP con lista de libros en formato JSON
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
     * Obtiene un libro por su ID.
     * 
     * @param request solicitud HTTP
     * @param id      identificador del libro
     * @param context contexto de ejecución de Azure Functions
     * @return respuesta HTTP con el libro en formato JSON o error 404
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
     * Obtiene todos los libros disponibles para préstamo.
     * 
     * @param request solicitud HTTP
     * @param context contexto de ejecución de Azure Functions
     * @return respuesta HTTP con lista de libros disponibles en formato JSON
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
     * Crea un nuevo libro en el catálogo.
     * 
     * @param request solicitud HTTP con datos del libro en el body (JSON)
     * @param context contexto de ejecución de Azure Functions
     * @return respuesta HTTP con el libro creado o errores de validación
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

            if (book.getId() == null || book.getId().isEmpty()) {
                book.setId(bookRepository.getNextId());
            }

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
     * Actualiza un libro existente.
     * 
     * @param request solicitud HTTP con datos actualizados del libro (JSON)
     * @param id      identificador del libro a actualizar
     * @param context contexto de ejecución de Azure Functions
     * @return respuesta HTTP con el libro actualizado o error 404
     */
    @FunctionName("UpdateBook")
    public HttpResponseMessage updateBook(
            @HttpTrigger(name = "req", methods = {
                    HttpMethod.PUT }, authLevel = AuthorizationLevel.ANONYMOUS, route = "books/{id}") HttpRequestMessage<Optional<String>> request,
            @BindingName("id") String id,
            final ExecutionContext context) {

        context.getLogger().info("Updating book with ID: " + id);

        try {
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
            book.setId(id);

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
     * Actualiza el estado de disponibilidad de un libro.
     * 
     * @param request solicitud HTTP con parámetro status en query string
     * @param id      identificador del libro
     * @param context contexto de ejecución de Azure Functions
     * @return respuesta HTTP con el libro actualizado o errores de validación
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
     * Elimina un libro del catálogo.
     * 
     * @param request solicitud HTTP
     * @param id      identificador del libro a eliminar
     * @param context contexto de ejecución de Azure Functions
     * @return respuesta HTTP con confirmación o error 404
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
