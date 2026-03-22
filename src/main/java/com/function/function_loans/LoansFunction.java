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
     * Get all loans
     * GET /api/loans
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
     * Get loan by ID
     * GET /api/loans/{id}
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
     * Get active loans
     * GET /api/loans/active
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
     * Get overdue loans
     * GET /api/loans/overdue
     */
    @FunctionName("GetOverdueLoans")
    public HttpResponseMessage getOverdueLoans(
            @HttpTrigger(name = "req", methods = {
                    HttpMethod.GET }, authLevel = AuthorizationLevel.ANONYMOUS, route = "loans/status/overdue") HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        context.getLogger().info("Getting overdue loans");

        try {
            // Update overdue status first
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
     * Get loans by user ID
     * GET /api/loans/user/{userId}
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
     * Get loans by book ID
     * GET /api/loans/book/{bookId}
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
     * Create new loan (borrow a book)
     * POST /api/loans
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

            // Validate required fields
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

            // Check if book is available
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

            // Generate ID if not provided
            if (loan.getId() == null || loan.getId().isEmpty()) {
                loan.setId(loanRepository.getNextId());
            }

            // Set loan date to today if not provided
            if (loan.getLoanDate() == null) {
                loan.setLoanDate(LocalDate.now());
            }

            // Set expected return date (14 days from loan date) if not provided
            if (loan.getExpectedReturnDate() == null) {
                loan.setExpectedReturnDate(loan.getLoanDate().plusDays(14));
            }

            // Set status to ACTIVE
            loan.setStatus(LoanStatus.ACTIVE);

            // Save loan
            Loan savedLoan = loanRepository.save(loan);

            // Update book status to BORROWED
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
     * Return a book
     * PATCH /api/loans/{id}/return
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

            // Return the book
            LocalDate returnDate = LocalDate.now();
            boolean returned = loanRepository.returnBook(id, returnDate);

            if (returned) {
                // Update book status to AVAILABLE
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
     * Delete loan
     * DELETE /api/loans/{id}
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
