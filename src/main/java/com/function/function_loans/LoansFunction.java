package com.function.function_loans;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;
import com.function.models.Loan;
import com.function.models.Loan.LoanStatus;
import com.function.models.Book;
import com.function.repository.LoanRepository;
import com.function.repository.BookRepository;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Funciones HTTP para gestionar préstamos de libros.
 * Expone endpoints REST para operaciones CRUD de préstamos y devoluciones.
 */
public class LoansFunction {
    private final LoanRepository loanRepository = new LoanRepository();
    private final BookRepository bookRepository = new BookRepository();
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new com.google.gson.JsonDeserializer<LocalDate>() {
                @Override
                public LocalDate deserialize(com.google.gson.JsonElement json, java.lang.reflect.Type type,
                        com.google.gson.JsonDeserializationContext context) throws com.google.gson.JsonParseException {
                    return LocalDate.parse(json.getAsString());
                }
            })
            .registerTypeAdapter(LocalDate.class, new com.google.gson.JsonSerializer<LocalDate>() {
                @Override
                public com.google.gson.JsonElement serialize(LocalDate date, java.lang.reflect.Type type,
                        com.google.gson.JsonSerializationContext context) {
                    return new com.google.gson.JsonPrimitive(date.toString());
                }
            })
            .create();

    /**
     * Obtiene todos los préstamos del sistema.
     * 
     * @param request solicitud HTTP
     * @param context contexto de ejecución de Azure Functions
     * @return respuesta HTTP con lista de préstamos en formato JSON
     */
    @FunctionName("GetAllLoans")
    public HttpResponseMessage getAllLoans(
            @HttpTrigger(name = "req", methods = {
                    HttpMethod.GET }, authLevel = AuthorizationLevel.ANONYMOUS, route = "loans") HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        context.getLogger().info("Getting all loans");

        try {
            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(gson.toJson(loanRepository.findAll()))
                    .build();
        } catch (Exception e) {
            context.getLogger().severe("Error getting loans: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error getting loans: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Obtiene un préstamo por su ID.
     * 
     * @param request solicitud HTTP
     * @param id      identificador del préstamo
     * @param context contexto de ejecución de Azure Functions
     * @return respuesta HTTP con el préstamo en formato JSON o error 404
     */
    @FunctionName("GetLoanById")
    public HttpResponseMessage getLoanById(
            @HttpTrigger(name = "req", methods = {
                    HttpMethod.GET }, authLevel = AuthorizationLevel.ANONYMOUS, route = "loans/{id}") HttpRequestMessage<Optional<String>> request,
            @BindingName("id") String id,
            final ExecutionContext context) {

        context.getLogger().info("Getting loan with ID: " + id);

        Optional<Loan> loan = loanRepository.findById(id);

        if (loan.isPresent()) {
            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(gson.toJson(loan.get()))
                    .build();
        } else {
            return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                    .body("Loan not found with ID: " + id)
                    .build();
        }
    }

    /**
     * Obtiene todos los préstamos activos.
     * 
     * @param request solicitud HTTP
     * @param context contexto de ejecución de Azure Functions
     * @return respuesta HTTP con lista de préstamos activos en formato JSON
     */
    @FunctionName("GetActiveLoans")
    public HttpResponseMessage getActiveLoans(
            @HttpTrigger(name = "req", methods = {
                    HttpMethod.GET }, authLevel = AuthorizationLevel.ANONYMOUS, route = "loans/status/active") HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        context.getLogger().info("Getting active loans");

        try {
            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(gson.toJson(loanRepository.findActiveLoans()))
                    .build();
        } catch (Exception e) {
            context.getLogger().severe("Error getting active loans: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error getting active loans: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Obtiene todos los préstamos vencidos.
     * Actualiza automáticamente el estado de préstamos vencidos antes de consultar.
     * 
     * @param request solicitud HTTP
     * @param context contexto de ejecución de Azure Functions
     * @return respuesta HTTP con lista de préstamos vencidos en formato JSON
     */
    @FunctionName("GetOverdueLoans")
    public HttpResponseMessage getOverdueLoans(
            @HttpTrigger(name = "req", methods = {
                    HttpMethod.GET }, authLevel = AuthorizationLevel.ANONYMOUS, route = "loans/status/overdue") HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        context.getLogger().info("Getting overdue loans");

        try {
            loanRepository.updateOverdueLoans();

            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(gson.toJson(loanRepository.findOverdueLoans()))
                    .build();
        } catch (Exception e) {
            context.getLogger().severe("Error getting overdue loans: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error getting overdue loans: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Obtiene todos los préstamos de un usuario específico.
     * 
     * @param request solicitud HTTP
     * @param userId  identificador del usuario
     * @param context contexto de ejecución de Azure Functions
     * @return respuesta HTTP con lista de préstamos del usuario en formato JSON
     */
    @FunctionName("GetLoansByUserId")
    public HttpResponseMessage getLoansByUserId(
            @HttpTrigger(name = "req", methods = {
                    HttpMethod.GET }, authLevel = AuthorizationLevel.ANONYMOUS, route = "loans/user/{userId}") HttpRequestMessage<Optional<String>> request,
            @BindingName("userId") String userId,
            final ExecutionContext context) {

        context.getLogger().info("Getting loans for user ID: " + userId);

        try {
            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(gson.toJson(loanRepository.findByUserId(userId)))
                    .build();
        } catch (Exception e) {
            context.getLogger().severe("Error getting loans by user: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error getting loans by user: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Obtiene todos los préstamos de un libro específico.
     * 
     * @param request solicitud HTTP
     * @param bookId  identificador del libro
     * @param context contexto de ejecución de Azure Functions
     * @return respuesta HTTP con lista de préstamos del libro en formato JSON
     */
    @FunctionName("GetLoansByBookId")
    public HttpResponseMessage getLoansByBookId(
            @HttpTrigger(name = "req", methods = {
                    HttpMethod.GET }, authLevel = AuthorizationLevel.ANONYMOUS, route = "loans/book/{bookId}") HttpRequestMessage<Optional<String>> request,
            @BindingName("bookId") String bookId,
            final ExecutionContext context) {

        context.getLogger().info("Getting loans for book ID: " + bookId);

        try {
            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(gson.toJson(loanRepository.findByBookId(bookId)))
                    .build();
        } catch (Exception e) {
            context.getLogger().severe("Error getting loans by book: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error getting loans by book: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Crea un nuevo préstamo de libro.
     * Valida disponibilidad del libro y actualiza su estado a BORROWED.
     * 
     * @param request solicitud HTTP con datos del préstamo en el body (JSON)
     * @param context contexto de ejecución de Azure Functions
     * @return respuesta HTTP con el préstamo creado o errores de validación
     */
    @FunctionName("CreateLoan")
    public HttpResponseMessage createLoan(
            @HttpTrigger(name = "req", methods = {
                    HttpMethod.POST }, authLevel = AuthorizationLevel.ANONYMOUS, route = "loans") HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        context.getLogger().info("Creating new loan");

        try {
            String body = request.getBody().orElse("");

            if (body.isEmpty()) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .body("Request body is required")
                        .build();
            }

            Loan loan = gson.fromJson(body, Loan.class);

            if (loan.getBookId() == null || loan.getBookId().isEmpty()) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .body("Book ID is required")
                        .build();
            }

            if (loan.getUserId() == null || loan.getUserId().isEmpty()) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .body("User ID is required")
                        .build();
            }

            Optional<Book> book = bookRepository.findById(loan.getBookId());
            if (!book.isPresent()) {
                return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                        .body("Book not found with ID: " + loan.getBookId())
                        .build();
            }

            if (book.get().getStatus() != Book.BookStatus.AVAILABLE) {
                return request.createResponseBuilder(HttpStatus.CONFLICT)
                        .body("Book is not available for loan")
                        .build();
            }

            if (loan.getId() == null || loan.getId().isEmpty()) {
                loan.setId(loanRepository.getNextId());
            }

            if (loan.getLoanDate() == null) {
                loan.setLoanDate(LocalDate.now());
            }

            if (loan.getExpectedReturnDate() == null) {
                loan.setExpectedReturnDate(loan.getLoanDate().plusDays(14));
            }

            loan.setStatus(LoanStatus.ACTIVE);

            Loan savedLoan = loanRepository.save(loan);

            bookRepository.updateStatus(loan.getBookId(), Book.BookStatus.BORROWED);

            return request.createResponseBuilder(HttpStatus.CREATED)
                    .header("Content-Type", "application/json")
                    .body(gson.toJson(savedLoan))
                    .build();
        } catch (Exception e) {
            context.getLogger().severe("Error creating loan: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating loan: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Registra la devolución de un libro prestado.
     * Actualiza el estado del préstamo a RETURNED y el estado del libro a
     * AVAILABLE.
     * 
     * @param request solicitud HTTP
     * @param id      identificador del préstamo a devolver
     * @param context contexto de ejecución de Azure Functions
     * @return respuesta HTTP con el préstamo actualizado o errores de validación
     */
    @FunctionName("ReturnBook")
    public HttpResponseMessage returnBook(
            @HttpTrigger(name = "req", methods = {
                    HttpMethod.PATCH }, authLevel = AuthorizationLevel.ANONYMOUS, route = "loans/{id}/return") HttpRequestMessage<Optional<String>> request,
            @BindingName("id") String id,
            final ExecutionContext context) {

        context.getLogger().info("Returning book for loan ID: " + id);

        try {
            Optional<Loan> loanOpt = loanRepository.findById(id);

            if (!loanOpt.isPresent()) {
                return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                        .body("Loan not found with ID: " + id)
                        .build();
            }

            Loan loan = loanOpt.get();

            if (loan.getStatus() == LoanStatus.RETURNED) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .body("Loan already returned")
                        .build();
            }

            LocalDate returnDate = LocalDate.now();
            boolean returned = loanRepository.returnBook(id, returnDate);

            if (returned) {
                bookRepository.updateStatus(loan.getBookId(), Book.BookStatus.AVAILABLE);

                Optional<Loan> updatedLoan = loanRepository.findById(id);
                return request.createResponseBuilder(HttpStatus.OK)
                        .header("Content-Type", "application/json")
                        .body(gson.toJson(updatedLoan.get()))
                        .build();
            } else {
                return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Failed to return book")
                        .build();
            }
        } catch (Exception e) {
            context.getLogger().severe("Error returning book: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error returning book: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Elimina un préstamo del sistema.
     * 
     * @param request solicitud HTTP
     * @param id      identificador del préstamo a eliminar
     * @param context contexto de ejecución de Azure Functions
     * @return respuesta HTTP con confirmación o error 404
     */
    @FunctionName("DeleteLoan")
    public HttpResponseMessage deleteLoan(
            @HttpTrigger(name = "req", methods = {
                    HttpMethod.DELETE }, authLevel = AuthorizationLevel.ANONYMOUS, route = "loans/{id}") HttpRequestMessage<Optional<String>> request,
            @BindingName("id") String id,
            final ExecutionContext context) {

        context.getLogger().info("Deleting loan with ID: " + id);

        boolean deleted = loanRepository.deleteById(id);

        if (deleted) {
            return request.createResponseBuilder(HttpStatus.OK)
                    .body("Loan deleted successfully")
                    .build();
        } else {
            return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                    .body("Loan not found with ID: " + id)
                    .build();
        }
    }
}
