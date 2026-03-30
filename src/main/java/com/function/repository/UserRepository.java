package com.function.repository;

import com.function.config.DatabaseConfig;
import com.function.models.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Repositorio para operaciones CRUD de usuarios.
 * Gestiona la persistencia de usuarios en la base de datos Oracle.
 */
public class UserRepository {
    private static final Logger logger = Logger.getLogger(UserRepository.class.getName());

    /**
     * Obtiene todos los usuarios del sistema.
     * 
     * @return lista de todos los usuarios ordenados por ID
     */
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT id, first_name, last_name, rut, email, active FROM users ORDER BY id";

        try (Connection conn = DatabaseConfig.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
            logger.info("Found " + users.size() + " users");
        } catch (SQLException e) {
            logger.severe("Error fetching all users: " + e.getMessage());
            throw new RuntimeException("Error fetching users", e);
        }

        return users;
    }

    /**
     * Busca un usuario por su ID.
     * 
     * @param id identificador del usuario
     * @return Optional con el usuario si existe, vacío en caso contrario
     */
    public Optional<User> findById(String id) {
        String sql = "SELECT id, first_name, last_name, rut, email, active FROM users WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            logger.severe("Error fetching user by id: " + e.getMessage());
            throw new RuntimeException("Error fetching user by id", e);
        }

        return Optional.empty();
    }

    /**
     * Busca un usuario por su RUT.
     * 
     * @param rut RUT del usuario (formato chileno)
     * @return Optional con el usuario si existe, vacío en caso contrario
     */
    public Optional<User> findByRut(String rut) {
        String sql = "SELECT id, first_name, last_name, rut, email, active FROM users WHERE rut = ?";

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, rut);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            logger.severe("Error fetching user by RUT: " + e.getMessage());
            throw new RuntimeException("Error fetching user by RUT", e);
        }

        return Optional.empty();
    }

    /**
     * Busca un usuario por su correo electrónico.
     * 
     * @param email correo electrónico del usuario
     * @return Optional con el usuario si existe, vacío en caso contrario
     */
    public Optional<User> findByEmail(String email) {
        String sql = "SELECT id, first_name, last_name, rut, email, active FROM users WHERE email = ?";

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            logger.severe("Error fetching user by email: " + e.getMessage());
            throw new RuntimeException("Error fetching user by email", e);
        }

        return Optional.empty();
    }

    /**
     * Guarda un usuario en la base de datos.
     * Si el usuario tiene ID y existe, lo actualiza; en caso contrario, crea uno
     * nuevo.
     * 
     * @param user usuario a guardar
     * @return usuario guardado con su ID asignado
     */
    public User save(User user) {
        if (user.getId() != null && !user.getId().isEmpty() && findById(user.getId()).isPresent()) {
            // Update existing user
            String sql = "UPDATE users SET first_name = ?, last_name = ?, rut = ?, email = ?, active = ? WHERE id = ?";

            try (Connection conn = DatabaseConfig.getConnection();
                    PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, user.getFirstName());
                pstmt.setString(2, user.getLastName());
                pstmt.setString(3, user.getRut());
                pstmt.setString(4, user.getEmail());
                pstmt.setInt(5, user.isActive() ? 1 : 0);
                pstmt.setString(6, user.getId());

                int rowsAffected = pstmt.executeUpdate();
                logger.info("Updated user with id: " + user.getId() + " (" + rowsAffected + " rows)");
            } catch (SQLException e) {
                logger.severe("Error updating user: " + e.getMessage());
                throw new RuntimeException("Error updating user", e);
            }
        } else {
            String sql = "INSERT INTO users (id, first_name, last_name, rut, email, active) VALUES (?, ?, ?, ?, ?, ?)";

            try (Connection conn = DatabaseConfig.getConnection();
                    PreparedStatement pstmt = conn.prepareStatement(sql)) {

                if (user.getId() == null || user.getId().isEmpty()) {
                    user.setId(getNextId());
                }

                pstmt.setString(1, user.getId());
                pstmt.setString(2, user.getFirstName());
                pstmt.setString(3, user.getLastName());
                pstmt.setString(4, user.getRut());
                pstmt.setString(5, user.getEmail());
                pstmt.setInt(6, user.isActive() ? 1 : 0);

                pstmt.executeUpdate();
                logger.info("Created new user with id: " + user.getId());
            } catch (SQLException e) {
                logger.severe("Error creating user: " + e.getMessage());
                throw new RuntimeException("Error creating user", e);
            }
        }

        return user;
    }

    /**
     * Elimina un usuario por su ID.
     * 
     * @param id identificador del usuario a eliminar
     * @return true si se eliminó, false si no se encontró
     */
    public boolean deleteById(String id) {
        String sql = "DELETE FROM users WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);
            int rowsAffected = pstmt.executeUpdate();
            logger.info("Deleted user with id: " + id + " (" + rowsAffected + " rows)");
            return rowsAffected > 0;

        } catch (SQLException e) {
            logger.severe("Error deleting user: " + e.getMessage());
            throw new RuntimeException("Error deleting user", e);
        }
    }

    /**
     * Obtiene todos los usuarios activos.
     * 
     * @return lista de usuarios con estado activo
     */
    public List<User> findActiveUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT id, first_name, last_name, rut, email, active FROM users WHERE active = 1 ORDER BY id";

        try (Connection conn = DatabaseConfig.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
            logger.info("Found " + users.size() + " active users");
        } catch (SQLException e) {
            logger.severe("Error fetching active users: " + e.getMessage());
            throw new RuntimeException("Error fetching active users", e);
        }

        return users;
    }

    /**
     * Obtiene el siguiente ID disponible para un nuevo usuario.
     * 
     * @return siguiente ID como String
     */
    public String getNextId() {
        String sql = "SELECT NVL(MAX(TO_NUMBER(id)), 0) + 1 as next_id FROM users";

        try (Connection conn = DatabaseConfig.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return String.valueOf(rs.getInt("next_id"));
            }
        } catch (SQLException e) {
            logger.severe("Error getting next user id: " + e.getMessage());
            throw new RuntimeException("Error getting next user id", e);
        }

        return "1";
    }

    /**
     * Mapea un ResultSet a un objeto User.
     * 
     * @param rs ResultSet con los datos del usuario
     * @return objeto User poblado con los datos
     * @throws SQLException si hay error al leer el ResultSet
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getString("id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("rut"),
                rs.getString("email"),
                rs.getInt("active") == 1);
    }
}
