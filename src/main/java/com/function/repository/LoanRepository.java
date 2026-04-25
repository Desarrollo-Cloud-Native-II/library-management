package com.function.repository;

import com.function.config.DatabaseConfig;
import com.function.models.Loan;
import com.function.models.Loan.LoanStatus;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Repositorio para operaciones CRUD de préstamos.
 * Gestiona la persistencia de préstamos de libros en la base de datos Oracle.
 */
public class LoanRepository {
    private static final Logger logger = Logger.getLogger(LoanRepository.class.getName());

    /**
     * Obtiene todos los préstamos del sistema.
     * 
     * @return lista de todos los préstamos ordenados por fecha de préstamo
     *         descendente
     */
    public List<Loan> findAll() {
        List<Loan> loans = new ArrayList<>();
        String sql = "SELECT id, book_id, user_id, loan_date, expected_return_date, actual_return_date, status FROM loans ORDER BY loan_date DESC";

        try (Connection conn = DatabaseConfig.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                loans.add(mapResultSetToLoan(rs));
            }
            logger.info("Found " + loans.size() + " loans");
        } catch (SQLException e) {
            logger.severe("Error fetching all loans: " + e.getMessage());
            throw new RuntimeException("Error fetching loans", e);
        }

        return loans;
    }

    /**
     * Busca un préstamo por su ID.
     * 
     * @param id identificador del préstamo
     * @return Optional con el préstamo si existe, vacío en caso contrario
     */
    public Optional<Loan> findById(String id) {
        String sql = "SELECT id, book_id, user_id, loan_date, expected_return_date, actual_return_date, status FROM loans WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToLoan(rs));
            }
        } catch (SQLException e) {
            logger.severe("Error fetching loan by id: " + e.getMessage());
            throw new RuntimeException("Error fetching loan by id", e);
        }

        return Optional.empty();
    }

    /**
     * Obtiene todos los préstamos de un libro específico.
     * 
     * @param bookId identificador del libro
     * @return lista de préstamos del libro ordenados por fecha descendente
     */
    public List<Loan> findByBookId(String bookId) {
        List<Loan> loans = new ArrayList<>();
        String sql = "SELECT id, book_id, user_id, loan_date, expected_return_date, actual_return_date, status FROM loans WHERE book_id = ? ORDER BY loan_date DESC";

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, bookId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                loans.add(mapResultSetToLoan(rs));
            }
            logger.info("Found " + loans.size() + " loans for book id: " + bookId);
        } catch (SQLException e) {
            logger.severe("Error fetching loans by book id: " + e.getMessage());
            throw new RuntimeException("Error fetching loans by book id", e);
        }

        return loans;
    }

    /**
     * Obtiene todos los préstamos de un usuario específico.
     * 
     * @param userId identificador del usuario
     * @return lista de préstamos del usuario ordenados por fecha descendente
     */
    public List<Loan> findByUserId(String userId) {
        List<Loan> loans = new ArrayList<>();
        String sql = "SELECT id, book_id, user_id, loan_date, expected_return_date, actual_return_date, status FROM loans WHERE user_id = ? ORDER BY loan_date DESC";

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                loans.add(mapResultSetToLoan(rs));
            }
            logger.info("Found " + loans.size() + " loans for user id: " + userId);
        } catch (SQLException e) {
            logger.severe("Error fetching loans by user id: " + e.getMessage());
            throw new RuntimeException("Error fetching loans by user id", e);
        }

        return loans;
    }

    /**
     * Obtiene todos los préstamos activos.
     * 
     * @return lista de préstamos con estado ACTIVE ordenados por fecha de retorno
     *         esperada
     */
    public List<Loan> findActiveLoans() {
        List<Loan> loans = new ArrayList<>();
        String sql = "SELECT id, book_id, user_id, loan_date, expected_return_date, actual_return_date, status FROM loans WHERE status = 'ACTIVE' ORDER BY expected_return_date";

        try (Connection conn = DatabaseConfig.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                loans.add(mapResultSetToLoan(rs));
            }
            logger.info("Found " + loans.size() + " active loans");
        } catch (SQLException e) {
            logger.severe("Error fetching active loans: " + e.getMessage());
            throw new RuntimeException("Error fetching active loans", e);
        }

        return loans;
    }

    /**
     * Obtiene todos los préstamos vencidos.
     * 
     * @return lista de préstamos con estado OVERDUE ordenados por fecha de retorno
     *         esperada
     */
    public List<Loan> findOverdueLoans() {
        List<Loan> loans = new ArrayList<>();
        String sql = "SELECT id, book_id, user_id, loan_date, expected_return_date, actual_return_date, status FROM loans WHERE status = 'OVERDUE' ORDER BY expected_return_date";

        try (Connection conn = DatabaseConfig.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                loans.add(mapResultSetToLoan(rs));
            }
            logger.info("Found " + loans.size() + " overdue loans");
        } catch (SQLException e) {
            logger.severe("Error fetching overdue loans: " + e.getMessage());
            throw new RuntimeException("Error fetching overdue loans", e);
        }

        return loans;
    }

    /**
     * Obtiene el préstamo activo de un libro específico.
     * 
     * @param bookId identificador del libro
     * @return Optional con el préstamo activo si existe, vacío en caso contrario
     */
    public Optional<Loan> findActiveLoanByBookId(String bookId) {
        String sql = "SELECT id, book_id, user_id, loan_date, expected_return_date, actual_return_date, status FROM loans WHERE book_id = ? AND status = 'ACTIVE'";

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, bookId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToLoan(rs));
            }
        } catch (SQLException e) {
            logger.severe("Error fetching active loan by book id: " + e.getMessage());
            throw new RuntimeException("Error fetching active loan by book id", e);
        }

        return Optional.empty();
    }

    /**
     * Guarda un préstamo en la base de datos.
     * Si el préstamo tiene ID y existe, lo actualiza; en caso contrario, crea uno
     * nuevo.
     * 
     * @param loan préstamo a guardar
     * @return préstamo guardado con su ID asignado
     */
    public Loan save(Loan loan) {
        if (loan.getId() != null && !loan.getId().isEmpty() && findById(loan.getId()).isPresent()) {

            String sql = "UPDATE loans SET book_id = ?, user_id = ?, loan_date = ?, expected_return_date = ?, " +
                    "actual_return_date = ?, status = ? WHERE id = ?";

            try (Connection conn = DatabaseConfig.getConnection();
                    PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, loan.getBookId());
                pstmt.setString(2, loan.getUserId());
                pstmt.setDate(3, Date.valueOf(loan.getLoanDate()));
                pstmt.setDate(4, Date.valueOf(loan.getExpectedReturnDate()));

                if (loan.getActualReturnDate() != null) {
                    pstmt.setDate(5, Date.valueOf(loan.getActualReturnDate()));
                } else {
                    pstmt.setNull(5, Types.DATE);
                }

                pstmt.setString(6, loan.getStatus().name());
                pstmt.setString(7, loan.getId());

                int rowsAffected = pstmt.executeUpdate();
                logger.info("Updated loan with id: " + loan.getId() + " (" + rowsAffected + " rows)");
            } catch (SQLException e) {
                logger.severe("Error updating loan: " + e.getMessage());
                throw new RuntimeException("Error updating loan", e);
            }
        } else {
            String sql = "INSERT INTO loans (id, book_id, user_id, loan_date, expected_return_date, actual_return_date, status) "
                    +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

            try (Connection conn = DatabaseConfig.getConnection();
                    PreparedStatement pstmt = conn.prepareStatement(sql)) {

                if (loan.getId() == null || loan.getId().isEmpty()) {
                    loan.setId(getNextId());
                }

                pstmt.setString(1, loan.getId());
                pstmt.setString(2, loan.getBookId());
                pstmt.setString(3, loan.getUserId());
                pstmt.setDate(4, Date.valueOf(loan.getLoanDate()));
                pstmt.setDate(5, Date.valueOf(loan.getExpectedReturnDate()));

                if (loan.getActualReturnDate() != null) {
                    pstmt.setDate(6, Date.valueOf(loan.getActualReturnDate()));
                } else {
                    pstmt.setNull(6, Types.DATE);
                }

                pstmt.setString(7, loan.getStatus().name());

                pstmt.executeUpdate();
                logger.info("Created new loan with id: " + loan.getId());
            } catch (SQLException e) {
                logger.severe("Error creating loan: " + e.getMessage());
                throw new RuntimeException("Error creating loan", e);
            }
        }

        return loan;
    }

    /**
     * Elimina un préstamo por su ID.
     * 
     * @param id identificador del préstamo a eliminar
     * @return true si se eliminó, false si no se encontró
     */
    public boolean deleteById(String id) {
        String sql = "DELETE FROM loans WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);
            int rowsAffected = pstmt.executeUpdate();
            logger.info("Deleted loan with id: " + id + " (" + rowsAffected + " rows)");
            return rowsAffected > 0;

        } catch (SQLException e) {
            logger.severe("Error deleting loan: " + e.getMessage());
            throw new RuntimeException("Error deleting loan", e);
        }
    }

    /**
     * Marca un libro como devuelto estableciendo la fecha de devolución real.
     * 
     * @param loanId     identificador del préstamo
     * @param returnDate fecha de devolución
     * @return true si se registró la devolución, false si no se encontró el
     *         préstamo activo
     */
    public boolean returnBook(String loanId, LocalDate returnDate) {
        String sql = "UPDATE loans SET actual_return_date = ?, status = 'RETURNED' WHERE id = ? AND status IN ('ACTIVE', 'OVERDUE')";

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, Date.valueOf(returnDate));
            pstmt.setString(2, loanId);

            int rowsAffected = pstmt.executeUpdate();
            logger.info("Returned book for loan id: " + loanId + " (" + rowsAffected + " rows)");
            return rowsAffected > 0;

        } catch (SQLException e) {
            logger.severe("Error returning book: " + e.getMessage());
            throw new RuntimeException("Error returning book", e);
        }
    }

    /**
     * Actualiza el estado de los préstamos activos que han superado su fecha de
     * retorno esperada.
     * Cambia el estado de ACTIVE a OVERDUE para préstamos vencidos.
     */
    public void updateOverdueLoans() {
        String sql = "UPDATE loans SET status = 'OVERDUE' WHERE status = 'ACTIVE' AND expected_return_date < SYSDATE";

        try (Connection conn = DatabaseConfig.getConnection();
                Statement stmt = conn.createStatement()) {

            int rowsAffected = stmt.executeUpdate(sql);
            logger.info("Updated " + rowsAffected + " loans to OVERDUE status");

        } catch (SQLException e) {
            logger.severe("Error updating overdue loans: " + e.getMessage());
            throw new RuntimeException("Error updating overdue loans", e);
        }
    }

    /**
     * Obtiene el siguiente ID disponible para un nuevo préstamo.
     * 
     * @return siguiente ID como String
     */
    public String getNextId() {
        String sql = "SELECT NVL(MAX(TO_NUMBER(id)), 0) + 1 as next_id FROM loans";

        try (Connection conn = DatabaseConfig.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return String.valueOf(rs.getInt("next_id"));
            }
        } catch (SQLException e) {
            logger.severe("Error getting next loan id: " + e.getMessage());
            throw new RuntimeException("Error getting next loan id", e);
        }

        return "1";
    }

    /**
     * Mapea un ResultSet a un objeto Loan.
     * 
     * @param rs ResultSet con los datos del préstamo
     * @return objeto Loan poblado con los datos
     * @throws SQLException si hay error al leer el ResultSet
     */
    private Loan mapResultSetToLoan(ResultSet rs) throws SQLException {
        String statusStr = rs.getString("status");
        LoanStatus status = LoanStatus.valueOf(statusStr);

        Date actualReturnDateSql = rs.getDate("actual_return_date");
        LocalDate actualReturnDate = actualReturnDateSql != null ? actualReturnDateSql.toLocalDate() : null;

        return new Loan(
                rs.getString("id"),
                rs.getString("book_id"),
                rs.getString("user_id"),
                rs.getDate("loan_date").toLocalDate(),
                rs.getDate("expected_return_date").toLocalDate(),
                actualReturnDate,
                status);
    }
}
