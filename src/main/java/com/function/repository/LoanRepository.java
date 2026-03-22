package com.function.repository;

import com.function.models.Loan;
import com.function.models.Loan.LoanStatus;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class LoanRepository {
    private static List<Loan> loans = new ArrayList<>();

    // Initialize with static data
    static {
        // Active loan: User 1 borrowed Book 4 (El Principito)
        loans.add(new Loan("1", "4", "1",
                LocalDate.of(2026, 3, 15),
                LocalDate.of(2026, 3, 29),
                null,
                LoanStatus.ACTIVE));

        // Returned loan: User 2 borrowed and returned Book 2 (1984)
        loans.add(new Loan("2", "2", "2",
                LocalDate.of(2026, 3, 1),
                LocalDate.of(2026, 3, 15),
                LocalDate.of(2026, 3, 14),
                LoanStatus.RETURNED));

        // Overdue loan: User 3 borrowed Book 1 and is late
        loans.add(new Loan("3", "1", "3",
                LocalDate.of(2026, 2, 20),
                LocalDate.of(2026, 3, 6),
                null,
                LoanStatus.OVERDUE));
    }

    // Get all loans
    public List<Loan> findAll() {
        return new ArrayList<>(loans);
    }

    // Get loan by ID
    public Optional<Loan> findById(String id) {
        return loans.stream()
                .filter(loan -> loan.getId().equals(id))
                .findFirst();
    }

    // Get loans by book ID
    public List<Loan> findByBookId(String bookId) {
        return loans.stream()
                .filter(loan -> loan.getBookId().equals(bookId))
                .collect(Collectors.toList());
    }

    // Get loans by user ID
    public List<Loan> findByUserId(String userId) {
        return loans.stream()
                .filter(loan -> loan.getUserId().equals(userId))
                .collect(Collectors.toList());
    }

    // Get active loans
    public List<Loan> findActiveLoans() {
        return loans.stream()
                .filter(loan -> loan.getStatus() == LoanStatus.ACTIVE)
                .collect(Collectors.toList());
    }

    // Get overdue loans
    public List<Loan> findOverdueLoans() {
        return loans.stream()
                .filter(loan -> loan.getStatus() == LoanStatus.OVERDUE)
                .collect(Collectors.toList());
    }

    // Get active loan for a specific book
    public Optional<Loan> findActiveLoanByBookId(String bookId) {
        return loans.stream()
                .filter(loan -> loan.getBookId().equals(bookId) &&
                        loan.getStatus() == LoanStatus.ACTIVE)
                .findFirst();
    }

    // Create new loan
    public Loan save(Loan loan) {
        // Check if loan already exists
        Optional<Loan> existing = findById(loan.getId());
        if (existing.isPresent()) {
            // Update existing loan
            loans.remove(existing.get());
        }
        loans.add(loan);
        return loan;
    }

    // Delete loan
    public boolean deleteById(String id) {
        return loans.removeIf(loan -> loan.getId().equals(id));
    }

    // Return a book (mark loan as returned)
    public boolean returnBook(String loanId, LocalDate returnDate) {
        Optional<Loan> loan = findById(loanId);
        if (loan.isPresent() && loan.get().getStatus() == LoanStatus.ACTIVE) {
            loan.get().setActualReturnDate(returnDate);
            loan.get().setStatus(LoanStatus.RETURNED);
            return true;
        }
        return false;
    }

    // Check and update overdue loans
    public void updateOverdueLoans() {
        LocalDate today = LocalDate.now();
        loans.stream()
                .filter(loan -> loan.getStatus() == LoanStatus.ACTIVE &&
                        loan.getExpectedReturnDate().isBefore(today))
                .forEach(loan -> loan.setStatus(LoanStatus.OVERDUE));
    }

    // Get next ID for new loans
    public String getNextId() {
        return String.valueOf(loans.size() + 1);
    }
}
