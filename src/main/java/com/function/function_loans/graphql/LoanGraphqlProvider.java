package com.function.function_loans.graphql;

import com.function.repository.LoanRepository;
import com.function.repository.BookRepository;
import com.function.repository.UserRepository;
import com.function.models.Loan;
import com.function.models.Loan.LoanStatus;
import com.function.models.Book;
import com.function.models.User;
import com.function.events.EventGridPublisher;
import com.function.events.LoanCreatedEvent;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Proveedor de GraphQL para operaciones sobre préstamos.
 * Define el esquema GraphQL y configura los data fetchers para las consultas y
 * mutaciones disponibles.
 */
public class LoanGraphqlProvider {
    private static final String FIELD_LOAN = "loan";
    private static final String FIELD_SUCCESS = "success";
    private static final String FIELD_MESSAGE = "message";
    private static final String FIELD_LOAN_DATE = "loanDate";
    private static final String FIELD_EXPECTED_RETURN_DATE = "expectedReturnDate";
    private static final String FIELD_STATUS = "status";
    private static final String FIELD_VALUE = "value";
    private static final String ERROR_LOAN_NOT_FOUND = "Loan not found with ID: ";

    private final GraphQL graphQL;
    private final LoanRepository loanRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final EventGridPublisher eventGridPublisher;
    private final Logger logger;

    public LoanGraphqlProvider() {
        this(new LoanRepository(), new BookRepository(), new UserRepository(), null, null);
    }

    public LoanGraphqlProvider(EventGridPublisher eventGridPublisher, Logger logger) {
        this(new LoanRepository(), new BookRepository(), new UserRepository(), eventGridPublisher, logger);
    }

    LoanGraphqlProvider(LoanRepository loanRepository, BookRepository bookRepository, UserRepository userRepository,
            EventGridPublisher eventGridPublisher, Logger logger) {
        this.loanRepository = loanRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.eventGridPublisher = eventGridPublisher;
        this.logger = logger;

        String schemaString = "type Query {" +
                "  loans: [Loan!]!" +
                "  loan(id: ID!): Loan" +
                "  activeLoans: [Loan!]!" +
                "  overdueLoans: [Loan!]!" +
                "  loansByUserId(userId: ID!): [Loan!]!" +
                "  loansByBookId(bookId: ID!): [Loan!]!" +
                "}" +
                "" +
                "type Mutation {" +
                "  createLoan(input: CreateLoanInput!): LoanPayload!" +
                "  updateLoan(id: ID!, input: UpdateLoanInput!): LoanPayload!" +
                "  returnBook(id: ID!): LoanPayload!" +
                "  deleteLoan(id: ID!): DeleteLoanPayload!" +
                "}" +
                "" +
                "input CreateLoanInput {" +
                "  bookId: ID!" +
                "  userId: ID!" +
                "  loanDate: String" +
                "  expectedReturnDate: String" +
                "}" +
                "" +
                "input UpdateLoanInput {" +
                "  expectedReturnDate: String" +
                "  status: String" +
                "}" +
                "" +
                "type LoanPayload {" +
                "  loan: Loan" +
                "  success: Boolean!" +
                "  message: String" +
                "}" +
                "" +
                "type DeleteLoanPayload {" +
                "  success: Boolean!" +
                "  message: String" +
                "}" +
                "" +
                "type Loan {" +
                "  id: ID!" +
                "  bookId: String!" +
                "  userId: String!" +
                "  loanDate: String!" +
                "  expectedReturnDate: String!" +
                "  actualReturnDate: String" +
                "  status: String!" +
                "}";

        TypeDefinitionRegistry typeRegistry = new SchemaParser().parse(schemaString);

        RuntimeWiring runtimeWiring = RuntimeWiring.newRuntimeWiring()
                .type("Query", builder -> builder
                        .dataFetcher("loans", environment -> loanRepository.findAll())
                        .dataFetcher("loan", environment -> {
                            String id = environment.getArgument("id");
                            return loanRepository.findById(id).orElse(null);
                        })
                        .dataFetcher("activeLoans", environment -> loanRepository.findActiveLoans())
                        .dataFetcher("overdueLoans", environment -> {
                            loanRepository.updateOverdueLoans();
                            return loanRepository.findOverdueLoans();
                        })
                        .dataFetcher("loansByUserId", environment -> {
                            String userId = environment.getArgument("userId");
                            return loanRepository.findByUserId(userId);
                        })
                        .dataFetcher("loansByBookId", environment -> {
                            String bookId = environment.getArgument("bookId");
                            return loanRepository.findByBookId(bookId);
                        }))
                .type("Mutation", builder -> builder
                        .dataFetcher("createLoan", environment -> {
                            Map<String, Object> input = environment.getArgument("input");
                            return createLoan(input);
                        })
                        .dataFetcher("updateLoan", environment -> {
                            String id = environment.getArgument("id");
                            Map<String, Object> input = environment.getArgument("input");
                            return updateLoan(id, input);
                        })
                        .dataFetcher("returnBook", environment -> {
                            String id = environment.getArgument("id");
                            return returnBook(id);
                        })
                        .dataFetcher("deleteLoan", environment -> {
                            String id = environment.getArgument("id");
                            return deleteLoan(id);
                        }))
                .build();

        GraphQLSchema schema = new SchemaGenerator().makeExecutableSchema(typeRegistry, runtimeWiring);
        this.graphQL = GraphQL.newGraphQL(schema).build();
    }

    public ExecutionResult execute(String query) {
        return graphQL.execute(
                ExecutionInput.newExecutionInput()
                        .query(query)
                        .build());
    }

    /**
     * Crea un nuevo préstamo de libro.
     */
    private Map<String, Object> createLoan(Map<String, Object> input) {
        try {
            String bookId = (String) input.get("bookId");
            String userId = (String) input.get("userId");

            Map<String, Object> validationError = validateLoanInputs(bookId, userId);
            if (validationError != null) {
                return validationError;
            }

            Map<String, Object> resourceError = validateLoanResources(bookId, userId);
            if (resourceError != null) {
                return resourceError;
            }

            LocalDate loanDate = parseLoanDate(input);
            if (loanDate == null) {
                return createErrorResponse("Invalid loan date format. Use YYYY-MM-DD");
            }

            LocalDate expectedReturnDate = parseExpectedReturnDate(input, loanDate);
            if (expectedReturnDate == null) {
                return createErrorResponse("Invalid expected return date format. Use YYYY-MM-DD");
            }

            if (expectedReturnDate.isBefore(loanDate)) {
                return createErrorResponse("Expected return date cannot be before loan date");
            }

            Loan loan = buildLoan(bookId, userId, loanDate, expectedReturnDate);
            Loan savedLoan = loanRepository.save(loan);

            bookRepository.updateStatus(bookId, Book.BookStatus.BORROWED);

            // Publicar evento a Event Grid (no bloquea si falla)
            try {
                publishLoanCreatedEvent(savedLoan, bookId, userId);
            } catch (Exception eventError) {
                if (logger != null) {
                    logger.warning("Error publicando evento, pero el préstamo se creó correctamente: "
                            + eventError.getMessage());
                }
            }

            return Map.of(
                    FIELD_LOAN, savedLoan,
                    FIELD_SUCCESS, true,
                    FIELD_MESSAGE, "Loan created successfully");
        } catch (Exception e) {
            if (logger != null) {
                logger.severe("Error creando préstamo: " + e.getMessage());
                e.printStackTrace();
            }
            return createErrorResponse("Error creating loan: " + e.getMessage());
        }
    }

    private Map<String, Object> validateLoanInputs(String bookId, String userId) {
        if (bookId == null || bookId.isEmpty()) {
            return createErrorResponse("Book ID is required");
        }
        if (userId == null || userId.isEmpty()) {
            return createErrorResponse("User ID is required");
        }
        return null;
    }

    private Map<String, Object> validateLoanResources(String bookId, String userId) {
        Optional<Book> book = bookRepository.findById(bookId);
        if (!book.isPresent()) {
            return createErrorResponse("Book not found with ID: " + bookId);
        }

        if (book.get().getStatus() != Book.BookStatus.AVAILABLE) {
            return createErrorResponse("Book is not available for loan");
        }

        Optional<User> user = userRepository.findById(userId);
        if (!user.isPresent()) {
            return createErrorResponse("User not found with ID: " + userId);
        }

        return null;
    }

    private LocalDate parseLoanDate(Map<String, Object> input) {
        if (input.containsKey(FIELD_LOAN_DATE) && input.get(FIELD_LOAN_DATE) != null) {
            try {
                return LocalDate.parse((String) input.get(FIELD_LOAN_DATE));
            } catch (Exception e) {
                return null;
            }
        }
        return LocalDate.now();
    }

    private LocalDate parseExpectedReturnDate(Map<String, Object> input, LocalDate loanDate) {
        if (input.containsKey(FIELD_EXPECTED_RETURN_DATE) && input.get(FIELD_EXPECTED_RETURN_DATE) != null) {
            try {
                return LocalDate.parse((String) input.get(FIELD_EXPECTED_RETURN_DATE));
            } catch (Exception e) {
                return null;
            }
        }
        return loanDate.plusDays(14);
    }

    private Loan buildLoan(String bookId, String userId, LocalDate loanDate, LocalDate expectedReturnDate) {
        Loan loan = new Loan();
        loan.setId(loanRepository.getNextId());
        loan.setBookId(bookId);
        loan.setUserId(userId);
        loan.setLoanDate(loanDate);
        loan.setExpectedReturnDate(expectedReturnDate);
        loan.setStatus(LoanStatus.ACTIVE);
        return loan;
    }

    private Map<String, Object> createErrorResponse(String message) {
        return Map.of(
                FIELD_LOAN, null,
                FIELD_SUCCESS, false,
                FIELD_MESSAGE, message);
    }

    /**
     * Registra la devolución de un libro prestado.
     */
    private Map<String, Object> returnBook(String id) {
        try {
            Optional<Loan> loanOpt = loanRepository.findById(id);

            if (!loanOpt.isPresent()) {
                return Map.of(
                        FIELD_LOAN, null,
                        FIELD_SUCCESS, false,
                        FIELD_MESSAGE, ERROR_LOAN_NOT_FOUND + id);
            }

            Loan loan = loanOpt.get();

            if (loan.getStatus() == LoanStatus.RETURNED) {
                return Map.of(
                        FIELD_LOAN, loan,
                        FIELD_SUCCESS, false,
                        FIELD_MESSAGE, "Loan already returned");
            }

            LocalDate returnDate = LocalDate.now();
            boolean returned = loanRepository.returnBook(id, returnDate);

            if (returned) {
                bookRepository.updateStatus(loan.getBookId(), Book.BookStatus.AVAILABLE);
                Optional<Loan> updatedLoan = loanRepository.findById(id);

                if (updatedLoan.isPresent()) {
                    return Map.of(
                            FIELD_LOAN, updatedLoan.get(),
                            FIELD_SUCCESS, true,
                            FIELD_MESSAGE, "Book returned successfully");
                } else {
                    return Map.of(
                            FIELD_LOAN, null,
                            FIELD_SUCCESS, false,
                            FIELD_MESSAGE, "Loan updated but could not retrieve updated data");
                }
            } else {
                return Map.of(
                        FIELD_LOAN, null,
                        FIELD_SUCCESS, false,
                        FIELD_MESSAGE, "Failed to return book");
            }
        } catch (Exception e) {
            String errorMessage = e.getMessage() != null ? e.getMessage() : e.getClass().getName();
            if (logger != null) {
                logger.severe("Error al devolver libro: " + errorMessage);
                e.printStackTrace();
            }
            return Map.of(
                    FIELD_LOAN, null,
                    FIELD_SUCCESS, false,
                    FIELD_MESSAGE, "Error returning book: " + errorMessage);
        }
    }

    /**
     * Actualiza un préstamo existente.
     */
    private Map<String, Object> updateLoan(String id, Map<String, Object> input) {
        try {
            Optional<Loan> existingLoanOpt = loanRepository.findById(id);
            if (!existingLoanOpt.isPresent()) {
                return Map.of(
                        FIELD_LOAN, null,
                        FIELD_SUCCESS, false,
                        FIELD_MESSAGE, ERROR_LOAN_NOT_FOUND + id);
            }

            Loan existingLoan = existingLoanOpt.get();

            if (input.containsKey(FIELD_EXPECTED_RETURN_DATE) && input.get(FIELD_EXPECTED_RETURN_DATE) != null) {
                Map<String, Object> dateResult = parseUpdatedExpectedReturnDate(
                        (String) input.get(FIELD_EXPECTED_RETURN_DATE),
                        existingLoan.getLoanDate());
                if (!Boolean.TRUE.equals(dateResult.get(FIELD_SUCCESS))) {
                    return Map.of(
                            FIELD_LOAN, null,
                            FIELD_SUCCESS, false,
                            FIELD_MESSAGE, dateResult.get(FIELD_MESSAGE));
                }
                existingLoan.setExpectedReturnDate((LocalDate) dateResult.get(FIELD_VALUE));
            }

            if (input.containsKey(FIELD_STATUS) && input.get(FIELD_STATUS) != null) {
                Map<String, Object> statusResult = parseUpdatedStatus((String) input.get(FIELD_STATUS));
                if (!Boolean.TRUE.equals(statusResult.get(FIELD_SUCCESS))) {
                    return Map.of(
                            FIELD_LOAN, null,
                            FIELD_SUCCESS, false,
                            FIELD_MESSAGE, statusResult.get(FIELD_MESSAGE));
                }
                existingLoan.setStatus((LoanStatus) statusResult.get(FIELD_VALUE));
            }

            Loan updatedLoan = loanRepository.save(existingLoan);

            return Map.of(
                    FIELD_LOAN, updatedLoan,
                    FIELD_SUCCESS, true,
                    FIELD_MESSAGE, "Loan updated successfully");
        } catch (Exception e) {
            return Map.of(
                    FIELD_LOAN, null,
                    FIELD_SUCCESS, false,
                    FIELD_MESSAGE, "Error updating loan: " + e.getMessage());
        }
    }

    /**
     * Parsea y valida una nueva fecha de retorno esperada.
     */
    private Map<String, Object> parseUpdatedExpectedReturnDate(String expectedReturnDateStr, LocalDate loanDate) {
        try {
            LocalDate newExpectedReturnDate = LocalDate.parse(expectedReturnDateStr);
            if (newExpectedReturnDate.isBefore(loanDate)) {
                return Map.of(
                        FIELD_SUCCESS, false,
                        FIELD_MESSAGE, "Expected return date cannot be before loan date");
            }
            return Map.of(
                    FIELD_SUCCESS, true,
                    FIELD_VALUE, newExpectedReturnDate);
        } catch (Exception e) {
            return Map.of(
                    FIELD_SUCCESS, false,
                    FIELD_MESSAGE, "Invalid expected return date format. Use YYYY-MM-DD");
        }
    }

    /**
     * Parsea y valida un nuevo estado de préstamo.
     */
    private Map<String, Object> parseUpdatedStatus(String statusStr) {
        try {
            LoanStatus newStatus = LoanStatus.valueOf(statusStr.toUpperCase());
            return Map.of(
                    FIELD_SUCCESS, true,
                    FIELD_VALUE, newStatus);
        } catch (IllegalArgumentException e) {
            return Map.of(
                    FIELD_SUCCESS, false,
                    FIELD_MESSAGE, "Invalid status. Valid values: ACTIVE, RETURNED, OVERDUE");
        }
    }

    /**
     * Elimina un préstamo del sistema.
     */
    private Map<String, Object> deleteLoan(String id) {
        try {
            boolean deleted = loanRepository.deleteById(id);

            if (deleted) {
                return Map.of(
                        FIELD_SUCCESS, true,
                        FIELD_MESSAGE, "Loan deleted successfully");
            } else {
                return Map.of(
                        FIELD_SUCCESS, false,
                        FIELD_MESSAGE, ERROR_LOAN_NOT_FOUND + id);
            }
        } catch (Exception e) {
            return Map.of(
                    FIELD_SUCCESS, false,
                    FIELD_MESSAGE, "Error deleting loan: " + e.getMessage());
        }
    }

    /**
     * Publica un evento LoanCreated a Azure Event Grid.
     */
    private void publishLoanCreatedEvent(Loan loan, String bookId, String userId) {
        if (eventGridPublisher == null || logger == null) {
            if (logger != null) {
                logger.warning("EventGridPublisher no configurado. Evento no publicado.");
            }
            return;
        }

        try {
            // Obtener información del libro
            Optional<Book> bookOpt = bookRepository.findById(bookId);
            String bookTitle = bookOpt.map(Book::getTitle).orElse("Unknown Book");

            // Obtener información del usuario
            Optional<User> userOpt = userRepository.findById(userId);
            String userName = "Unknown User";
            String userEmail = "no-email@library.com";

            if (userOpt.isPresent()) {
                User user = userOpt.get();
                userName = user.getFirstName() + " " + user.getLastName();
                userEmail = user.getEmail();
            }

            // Crear el evento
            LoanCreatedEvent event = new LoanCreatedEvent(
                    loan.getId(),
                    loan.getUserId(),
                    loan.getBookId(),
                    bookTitle,
                    loan.getLoanDate().toString(),
                    loan.getExpectedReturnDate().toString(),
                    userName,
                    userEmail);

            // Publicar a Event Grid
            eventGridPublisher.publishLoanCreatedEvent(event, logger);

        } catch (Exception e) {
            logger.severe("Error al publicar evento LoanCreated: " + e.getMessage());
        }
    }
}
