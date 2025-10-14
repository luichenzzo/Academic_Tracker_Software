package com.academictracker.dao;

import com.academictracker.model.User;
import com.academictracker.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for UsuarioSistema entity
 */
public class UserDAO {
    private static final Logger logger = LoggerFactory.getLogger(UserDAO.class);

    /**
     * Create a new user
     */
    public User create(User user) throws SQLException {
        String sql = "INSERT INTO UsuarioSistema (username, password_hash, rol, id_referencia, tipo_referencia, activo) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, new String[]{"id_usuario"})) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPasswordHash());
            stmt.setString(3, user.getRol().getValue());
            stmt.setString(4, user.getIdReferencia());
            stmt.setString(5, user.getTipoReferencia());
            stmt.setInt(6, user.isActivo() ? 1 : 0);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Creating user failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    user.setIdUsuario(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("Creating user failed, no ID obtained.");
                }
            }
            
            logger.info("User created successfully: {}", user.getUsername());
            return user;
        }
    }

    /**
     * Find user by username
     */
    public Optional<User> findByUsername(String username) throws SQLException {
        String sql = "SELECT id_usuario, username, password_hash, rol, id_referencia, tipo_referencia, activo FROM UsuarioSistema WHERE username = ? AND activo = 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        }

        return Optional.empty();
    }

    /**
     * Find user by ID
     */
    public Optional<User> findById(Long userId) throws SQLException {
        String sql = "SELECT id_usuario, username, password_hash, rol, id_referencia, tipo_referencia, activo FROM UsuarioSistema WHERE id_usuario = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        }

        return Optional.empty();
    }

    /**
     * Update user
     */
    public boolean update(User user) throws SQLException {
        String sql = "UPDATE UsuarioSistema SET username = ?, password_hash = ?, rol = ?, id_referencia = ?, tipo_referencia = ?, activo = ? WHERE id_usuario = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPasswordHash());
            stmt.setString(3, user.getRol().getValue());
            stmt.setString(4, user.getIdReferencia());
            stmt.setString(5, user.getTipoReferencia());
            stmt.setInt(6, user.isActivo() ? 1 : 0);
            stmt.setLong(7, user.getIdUsuario());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("User updated successfully: {}", user.getUsername());
                return true;
            }
        }

        return false;
    }

    /**
     * Delete user (soft delete - set activo = 0)
     */
    public boolean delete(Long userId) throws SQLException {
        String sql = "UPDATE UsuarioSistema SET activo = 0 WHERE id_usuario = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, userId);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("User deactivated successfully: {}", userId);
                return true;
            }
        }

        return false;
    }

    /**
     * Find all active users
     */
    public List<User> findAll() throws SQLException {
        String sql = "SELECT id_usuario, username, password_hash, rol, id_referencia, tipo_referencia, activo FROM UsuarioSistema WHERE activo = 1 ORDER BY username";

        List<User> users = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        }

        return users;
    }

    /**
     * Find users by role
     */
    public List<User> findByRole(User.UserRole role) throws SQLException {
        String sql = "SELECT id_usuario, username, password_hash, rol, id_referencia, tipo_referencia, activo FROM UsuarioSistema WHERE rol = ? AND activo = 1 ORDER BY username";

        List<User> users = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, role.getValue());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    users.add(mapResultSetToUser(rs));
                }
            }
        }

        return users;
    }

    /**
     * Check if username exists
     */
    public boolean existsByUsername(String username) throws SQLException {
        String sql = "SELECT COUNT(*) FROM UsuarioSistema WHERE username = ? AND activo = 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }

        return false;
    }

    /**
     * Map ResultSet to User object
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setIdUsuario(rs.getLong("id_usuario"));
        user.setUsername(rs.getString("username"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setRol(User.UserRole.fromString(rs.getString("rol")));
        user.setIdReferencia(rs.getString("id_referencia"));
        user.setTipoReferencia(rs.getString("tipo_referencia"));
        user.setActivo(rs.getInt("activo") == 1);
        return user;
    }
}
