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
     * Create a new sede
     */
    public Sede create(Sede sede) throws SQLException {
        String sql = "INSERT INTO Sede (nombre, municipio, direccion, telefono) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, new String[]{"id_sede"})) {

            stmt.setString(1, sede.getNombre());
            stmt.setString(2, sede.getMunicipio());
            stmt.setString(3, sede.getDireccion());
            stmt.setString(4, sede.getTelefono());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Creating sede failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    sede.setIdSede(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("Creating sede failed, no ID obtained.");
                }
            }

            logger.info("Sede created successfully: {}", sede.getNombre());
            return sede;
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
