package com.function.models;

import java.time.LocalDate;

/**
 * Representa un préstamo de libro a un usuario.
 * Gestiona las fechas de préstamo, retorno esperado y real, junto con el estado
 * del préstamo.
 */
public class Loan {
    private String id;
    private String bookId;
    private String userId;
    private LocalDate loanDate;
    private LocalDate expectedReturnDate;
    private LocalDate actualReturnDate;
    private LoanStatus status;

    /**
     * Constructor vacío para inicialización por defecto.
     */
    public Loan() {
    }

    /**
     * Constructor con todos los parámetros.
     * 
     * @param id                 identificador único del préstamo
     * @param bookId             identificador del libro prestado
     * @param userId             identificador del usuario que realiza el préstamo
     * @param loanDate           fecha en que se realizó el préstamo
     * @param expectedReturnDate fecha esperada de devolución
     * @param actualReturnDate   fecha real de devolución (null si no devuelto)
     * @param status             estado actual del préstamo
     */
    public Loan(String id, String bookId, String userId, LocalDate loanDate,
            LocalDate expectedReturnDate, LocalDate actualReturnDate, LoanStatus status) {
        this.id = id;
        this.bookId = bookId;
        this.userId = userId;
        this.loanDate = loanDate;
        this.expectedReturnDate = expectedReturnDate;
        this.actualReturnDate = actualReturnDate;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public LocalDate getLoanDate() {
        return loanDate;
    }

    public void setLoanDate(LocalDate loanDate) {
        this.loanDate = loanDate;
    }

    public LocalDate getExpectedReturnDate() {
        return expectedReturnDate;
    }

    public void setExpectedReturnDate(LocalDate expectedReturnDate) {
        this.expectedReturnDate = expectedReturnDate;
    }

    public LocalDate getActualReturnDate() {
        return actualReturnDate;
    }

    public void setActualReturnDate(LocalDate actualReturnDate) {
        this.actualReturnDate = actualReturnDate;
    }

    public LoanStatus getStatus() {
        return status;
    }

    public void setStatus(LoanStatus status) {
        this.status = status;
    }

    /**
     * Estados posibles de un préstamo en el sistema.
     */
    public enum LoanStatus {
        ACTIVE,
        RETURNED,
        OVERDUE
    }
}
