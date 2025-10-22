package com.academictracker.dao;

import com.academictracker.model.Sede;
import com.academictracker.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Sede entity
 */
public class SedeDAO {
    private static final Logger logger = LoggerFactory.getLogger(SedeDAO.class);

    /**
     * Create a new sede - requires manual ID
     */
    public Sede create(Sede sede) throws SQLException {
        // Get next available ID
        if (sede.getIdSede() == null) {
            sede.setIdSede(getNextId());
        }

        String sql = "INSERT INTO Sede (id_sede, nombre, municipio, direccion, telefono) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, sede.getIdSede());
            stmt.setString(2, sede.getNombre());
            stmt.setString(3, sede.getMunicipio());
            stmt.setString(4, sede.getDireccion());
            stmt.setString(5, sede.getTelefono());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Creating sede failed, no rows affected.");
            }

            logger.info("Sede created successfully: {} with ID: {}", sede.getNombre(), sede.getIdSede());
            return sede;
        }
    }

    /**
     * Get next available ID for manual assignment
     */
    private Long getNextId() throws SQLException {
        String sql = "SELECT NVL(MAX(id_sede), 0) + 1 FROM Sede";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getLong(1);
            }
            return 1L;
        }
    }

    /**
     * Find sede by ID
     */
    public Optional<Sede> findById(Long idSede) throws SQLException {
        String sql = "SELECT id_sede, nombre, municipio, direccion, telefono FROM Sede WHERE id_sede = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, idSede);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToSede(rs));
                }
            }
        }

        return Optional.empty();
    }

    /**
     * Find all sedes
     */
    public List<Sede> findAll() throws SQLException {
        String sql = "SELECT id_sede, nombre, municipio, direccion, telefono FROM Sede ORDER BY nombre";

        List<Sede> sedes = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                sedes.add(mapResultSetToSede(rs));
            }
        }

        return sedes;
    }

    /**
     * Update sede
     */
    public boolean update(Sede sede) throws SQLException {
        String sql = "UPDATE Sede SET nombre = ?, municipio = ?, direccion = ?, telefono = ? WHERE id_sede = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, sede.getNombre());
            stmt.setString(2, sede.getMunicipio());
            stmt.setString(3, sede.getDireccion());
            stmt.setString(4, sede.getTelefono());
            stmt.setLong(5, sede.getIdSede());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Sede updated successfully: {}", sede.getNombre());
                return true;
            }
        }

        return false;
    }

    /**
     * Delete sede
     */
    public boolean delete(Long idSede) throws SQLException {
        String sql = "DELETE FROM Sede WHERE id_sede = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, idSede);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Sede deleted successfully: {}", idSede);
                return true;
            }
        }

        return false;
    }

    /**
     * Map ResultSet to Sede object
     */
    private Sede mapResultSetToSede(ResultSet rs) throws SQLException {
        Sede sede = new Sede();
        sede.setIdSede(rs.getLong("id_sede"));
        sede.setNombre(rs.getString("nombre"));
        sede.setMunicipio(rs.getString("municipio"));
        sede.setDireccion(rs.getString("direccion"));
        sede.setTelefono(rs.getString("telefono"));
        return sede;
    }
}
