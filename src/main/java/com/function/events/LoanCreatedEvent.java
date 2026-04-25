package com.function.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;

/**
 * Evento publicado cuando se crea un nuevo préstamo.
 * Contiene información sobre el préstamo, libro y fecha de devolución esperada.
 */
public class LoanCreatedEvent {

    @JsonProperty("loanId")
    private String loanId;

    @JsonProperty("userId")
    private String userId;

    @JsonProperty("bookId")
    private String bookId;

    @JsonProperty("bookTitle")
    private String bookTitle;

    @JsonProperty("loanDate")
    private String loanDate;

    @JsonProperty("expectedReturnDate")
    private String expectedReturnDate;

    @JsonProperty("userName")
    private String userName;

    @JsonProperty("userEmail")
    private String userEmail;

    public LoanCreatedEvent() {
    }

    public LoanCreatedEvent(String loanId, String userId, String bookId, String bookTitle,
            String loanDate, String expectedReturnDate, String userName, String userEmail) {
        this.loanId = loanId;
        this.userId = userId;
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.loanDate = loanDate;
        this.expectedReturnDate = expectedReturnDate;
        this.userName = userName;
        this.userEmail = userEmail;
    }

    // Getters y Setters
    public String getLoanId() {
        return loanId;
    }

    public void setLoanId(String loanId) {
        this.loanId = loanId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public String getLoanDate() {
        return loanDate;
    }

    public void setLoanDate(String loanDate) {
        this.loanDate = loanDate;
    }

    public String getExpectedReturnDate() {
        return expectedReturnDate;
    }

    public void setExpectedReturnDate(String expectedReturnDate) {
        this.expectedReturnDate = expectedReturnDate;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    @Override
    public String toString() {
        return "LoanCreatedEvent{" +
                "loanId='" + loanId + '\'' +
                ", userId='" + userId + '\'' +
                ", bookId='" + bookId + '\'' +
                ", bookTitle='" + bookTitle + '\'' +
                ", loanDate='" + loanDate + '\'' +
                ", expectedReturnDate='" + expectedReturnDate + '\'' +
                ", userName='" + userName + '\'' +
                ", userEmail='" + userEmail + '\'' +
                '}';
    }
}
