package com.academictracker.dao;

import com.academictracker.model.DetalleMatricula;
import com.academictracker.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for DetalleMatricula entity
 */
public class DetalleMatriculaDAO {
    private static final Logger logger = LoggerFactory.getLogger(DetalleMatriculaDAO.class);

    public DetalleMatricula create(DetalleMatricula detalle) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();

            // Get the next available ID
            String getMaxIdSql = "SELECT NVL(MAX(id_detalle), 0) + 1 AS next_id FROM DetalleMatricula";
            stmt = conn.prepareStatement(getMaxIdSql);
            rs = stmt.executeQuery();

            long nextId = 1;
            if (rs.next()) {
                nextId = rs.getLong("next_id");
            }
            rs.close();
            stmt.close();

            // Insert the new detalle
            String sql = "INSERT INTO DetalleMatricula (id_detalle, id_matricula, id_grupo, fecha_inscripcion, estado, numero_intento) " +
                        "VALUES (?, ?, ?, ?, ?, ?)";

            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, nextId);
            stmt.setLong(2, detalle.getIdMatricula());
            stmt.setLong(3, detalle.getIdGrupo());
            stmt.setTimestamp(4, Timestamp.valueOf(detalle.getFechaInscripcion() != null ?
                detalle.getFechaInscripcion() : LocalDateTime.now()));
            stmt.setString(5, detalle.getEstado() != null ? detalle.getEstado() : "inscrito");
            stmt.setInt(6, detalle.getNumeroIntento() != null ? detalle.getNumeroIntento() : 1);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Creating detalle matricula failed, no rows affected.");
            }

            detalle.setIdDetalle(nextId);
            logger.info("DetalleMatricula created with ID: {}", nextId);
            return detalle;

        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { /* ignore */ }
            if (stmt != null) try { stmt.close(); } catch (SQLException e) { /* ignore */ }
            if (conn != null) try { conn.close(); } catch (SQLException e) { /* ignore */ }
        }
    }

    public Optional<DetalleMatricula> findById(Long idDetalle) throws SQLException {
        String sql = "SELECT dm.*, " +
                    "g.numero_grupo, g.cod_asignatura, " +
                    "a.nombre as asignatura_nombre, a.creditos " +
                    "FROM DetalleMatricula dm " +
                    "LEFT JOIN Grupo g ON dm.id_grupo = g.id_grupo " +
                    "LEFT JOIN Asignatura a ON g.cod_asignatura = a.cod_asignatura " +
                    "WHERE dm.id_detalle = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, idDetalle);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToDetalle(rs));
                }
            }
        }

        return Optional.empty();
    }

    public List<DetalleMatricula> findByMatricula(Long idMatricula) throws SQLException {
        String sql = "SELECT dm.*, " +
                    "g.numero_grupo, g.cod_asignatura, g.cupo_maximo, g.cupo_ocupado, " +
                    "a.nombre as asignatura_nombre, a.creditos, " +
                    "s.nombre as sede_nombre " +
                    "FROM DetalleMatricula dm " +
                    "LEFT JOIN Grupo g ON dm.id_grupo = g.id_grupo " +
                    "LEFT JOIN Asignatura a ON g.cod_asignatura = a.cod_asignatura " +
                    "LEFT JOIN Sede s ON g.id_sede = s.id_sede " +
                    "WHERE dm.id_matricula = ? " +
                    "ORDER BY a.nombre";

        List<DetalleMatricula> detalles = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, idMatricula);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    detalles.add(mapResultSetToDetalleWithJoins(rs));
                }
            }
        }

        return detalles;
    }

    public List<DetalleMatricula> findByGrupo(Long idGrupo) throws SQLException {
        String sql = "SELECT dm.*, " +
                    "g.numero_grupo, g.cod_asignatura, " +
                    "a.nombre as asignatura_nombre " +
                    "FROM DetalleMatricula dm " +
                    "LEFT JOIN Grupo g ON dm.id_grupo = g.id_grupo " +
                    "LEFT JOIN Asignatura a ON g.cod_asignatura = a.cod_asignatura " +
                    "WHERE dm.id_grupo = ? " +
                    "ORDER BY dm.fecha_inscripcion";

        List<DetalleMatricula> detalles = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, idGrupo);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    detalles.add(mapResultSetToDetalleWithJoins(rs));
                }
            }
        }

        return detalles;
    }

    public boolean update(DetalleMatricula detalle) throws SQLException {
        String sql = "UPDATE DetalleMatricula SET estado = ?, numero_intento = ? WHERE id_detalle = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, detalle.getEstado() != null ? detalle.getEstado() : "inscrito");
            stmt.setInt(2, detalle.getNumeroIntento() != null ? detalle.getNumeroIntento() : 1);
            stmt.setLong(3, detalle.getIdDetalle());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("DetalleMatricula updated successfully: {}", detalle.getIdDetalle());
                return true;
            }
        }

        return false;
    }

    public boolean delete(Long idDetalle) throws SQLException {
        String sql = "DELETE FROM DetalleMatricula WHERE id_detalle = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, idDetalle);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("DetalleMatricula deleted: {}", idDetalle);
                return true;
            }
        }

        return false;
    }

    public boolean isStudentEnrolledInGrupo(Long idMatricula, Long idGrupo) throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM DetalleMatricula " +
                    "WHERE id_matricula = ? AND id_grupo = ? AND estado IN ('inscrito', 'cursando')";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, idMatricula);
            stmt.setLong(2, idGrupo);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count") > 0;
                }
            }
        }

        return false;
    }

    private DetalleMatricula mapResultSetToDetalle(ResultSet rs) throws SQLException {
        DetalleMatricula detalle = new DetalleMatricula();
        detalle.setIdDetalle(rs.getLong("id_detalle"));
        detalle.setIdMatricula(rs.getLong("id_matricula"));
        detalle.setIdGrupo(rs.getLong("id_grupo"));

        Timestamp timestamp = rs.getTimestamp("fecha_inscripcion");
        if (timestamp != null) {
            detalle.setFechaInscripcion(timestamp.toLocalDateTime());
        }

        detalle.setEstado(rs.getString("estado"));
        detalle.setNumeroIntento(rs.getInt("numero_intento"));

        return detalle;
    }

    private DetalleMatricula mapResultSetToDetalleWithJoins(ResultSet rs) throws SQLException {
        DetalleMatricula detalle = mapResultSetToDetalle(rs);

        // Create and populate Grupo if available
        try {
            com.academictracker.model.Grupo grupo = new com.academictracker.model.Grupo();
            grupo.setIdGrupo(rs.getLong("id_grupo"));
            grupo.setNumeroGrupo(rs.getInt("numero_grupo"));
            grupo.setCodAsignatura(rs.getString("cod_asignatura"));

            // Check if cupo_maximo exists in the result set
            try {
                grupo.setCupoMaximo(rs.getInt("cupo_maximo"));
                grupo.setCupoOcupado(rs.getInt("cupo_ocupado"));
            } catch (SQLException e) {
                // Columns not available, skip
            }

            // Create and populate Asignatura if available
            String asignaturaNombre = rs.getString("asignatura_nombre");
            if (asignaturaNombre != null) {
                com.academictracker.model.Asignatura asignatura = new com.academictracker.model.Asignatura();
                asignatura.setCodAsignatura(rs.getString("cod_asignatura"));
                asignatura.setNombre(asignaturaNombre);
                try {
                    asignatura.setCreditos(rs.getInt("creditos"));
                } catch (SQLException e) {
                    // Column not available
                }
                grupo.setAsignatura(asignatura);
            }

            detalle.setGrupo(grupo);
        } catch (SQLException e) {
            // Columns not available, skip
        }

        return detalle;
    }
}

