package com.function.models;

import java.time.LocalDate;

public class Loan {
    private String id;
    private String bookId;
    private String userId;
    private LocalDate loanDate;
    private LocalDate expectedReturnDate;
    private LocalDate actualReturnDate;
    private LoanStatus status;

    // Empty constructor
    public Loan() {
    }

    // Constructor with all parameters
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

    // Getters and Setters
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

    // Enum for loan status
    public enum LoanStatus {
        ACTIVE,
        RETURNED,
        OVERDUE
    }
}
