package com.academictracker.dao;

import com.academictracker.model.Enrollment;
import com.academictracker.model.DetalleMatricula;
import com.academictracker.model.Matricula;
import com.academictracker.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Enrollment entity - Bridge to DetalleMatricula/Matricula tables
 */
public class EnrollmentDAO {
    private static final Logger logger = LoggerFactory.getLogger(EnrollmentDAO.class);

    public Enrollment create(Enrollment enrollment) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();

            // First, ensure there's a matricula for this student and period
            Long matriculaId = getOrCreateMatricula(enrollment.getStudentId(), getCurrentPeriod());

            // Get the next available ID using Oracle pattern
            String getMaxIdSql = "SELECT NVL(MAX(id_detalle), 0) + 1 AS next_id FROM DetalleMatricula";
            stmt = conn.prepareStatement(getMaxIdSql);
            rs = stmt.executeQuery();

            long nextId = 1;
            if (rs.next()) {
                nextId = rs.getLong("next_id");
            }
            rs.close();
            stmt.close();

            // Now insert the new enrollment with the generated ID
            String sql = "INSERT INTO DetalleMatricula (id_detalle, id_matricula, id_grupo, fecha_inscripcion, estado, numero_intento) " +
                        "VALUES (?, ?, ?, ?, ?, ?)";

            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, nextId);
            stmt.setLong(2, matriculaId);
            stmt.setLong(3, enrollment.getIdGrupo() != null ? enrollment.getIdGrupo() : 1); // Default group
            stmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setString(5, enrollment.getStatus().getDbValue());
            stmt.setInt(6, 1); // First attempt

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Creating enrollment failed, no rows affected.");
            }

            enrollment.setEnrollmentId(nextId);
            enrollment.setIdDetalle(nextId);

            logger.info("Enrollment created for student: {} with ID: {}", enrollment.getStudentId(), nextId);
            return enrollment;

        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { /* ignore */ }
            if (stmt != null) try { stmt.close(); } catch (SQLException e) { /* ignore */ }
            if (conn != null) try { conn.close(); } catch (SQLException e) { /* ignore */ }
        }
    }

    public Optional<Enrollment> findById(Long enrollmentId) throws SQLException {
        String sql = "SELECT dm.*, m.cod_estudiante, m.cod_periodo, g.cod_asignatura, a.nombre as asignatura_nombre " +
                    "FROM DetalleMatricula dm " +
                    "JOIN Matricula m ON dm.id_matricula = m.id_matricula " +
                    "LEFT JOIN Grupo g ON dm.id_grupo = g.id_grupo " +
                    "LEFT JOIN Asignatura a ON g.cod_asignatura = a.cod_asignatura " +
                    "WHERE dm.id_detalle = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, enrollmentId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToEnrollment(rs));
                }
            }
        }

        return Optional.empty();
    }

    public List<Enrollment> findByStudent(String studentId) throws SQLException {
        String sql = "SELECT dm.*, m.cod_estudiante, m.cod_periodo, g.cod_asignatura, a.nombre as asignatura_nombre " +
                    "FROM DetalleMatricula dm " +
                    "JOIN Matricula m ON dm.id_matricula = m.id_matricula " +
                    "LEFT JOIN Grupo g ON dm.id_grupo = g.id_grupo " +
                    "LEFT JOIN Asignatura a ON g.cod_asignatura = a.cod_asignatura " +
                    "WHERE m.cod_estudiante = ? " +
                    "ORDER BY dm.fecha_inscripcion DESC";

        List<Enrollment> enrollments = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, studentId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    enrollments.add(mapResultSetToEnrollment(rs));
                }
            }
        }

        return enrollments;
    }

    public List<Enrollment> findByCourse(Long courseId) throws SQLException {
        String sql = "SELECT dm.*, m.cod_estudiante, m.cod_periodo, g.cod_asignatura, a.nombre as asignatura_nombre " +
                    "FROM DetalleMatricula dm " +
                    "JOIN Matricula m ON dm.id_matricula = m.id_matricula " +
                    "JOIN Grupo g ON dm.id_grupo = g.id_grupo " +
                    "LEFT JOIN Asignatura a ON g.cod_asignatura = a.cod_asignatura " +
                    "WHERE g.cod_asignatura = ? " +
                    "ORDER BY dm.fecha_inscripcion DESC";

        List<Enrollment> enrollments = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, courseId.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    enrollments.add(mapResultSetToEnrollment(rs));
                }
            }
        }

        return enrollments;
    }

    public boolean update(Enrollment enrollment) throws SQLException {
        String sql = "UPDATE DetalleMatricula SET estado = ? WHERE id_detalle = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, enrollment.getStatus().getDbValue());
            stmt.setLong(2, enrollment.getIdDetalle());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Enrollment updated successfully: {}", enrollment.getEnrollmentId());
                return true;
            }
        }

        return false;
    }

    public boolean delete(Long enrollmentId) throws SQLException {
        String sql = "UPDATE DetalleMatricula SET estado = 'cancelado' WHERE id_detalle = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, enrollmentId);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Enrollment cancelled successfully: {}", enrollmentId);
                return true;
            }
        }

        return false;
    }

    // Helper method to get or create matricula for a student in current period
    private Long getOrCreateMatricula(String studentId, String periodo) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();

            // First try to find existing matricula
            String findSql = "SELECT id_matricula FROM Matricula WHERE cod_estudiante = ? AND cod_periodo = ?";
            stmt = conn.prepareStatement(findSql);
            stmt.setString(1, studentId);
            stmt.setString(2, periodo);
            rs = stmt.executeQuery();

            if (rs.next()) {
                Long matriculaId = rs.getLong("id_matricula");
                return matriculaId;
            }
            rs.close();
            stmt.close();

            // If not found, create new matricula using Oracle pattern
            String getMaxIdSql = "SELECT NVL(MAX(id_matricula), 0) + 1 AS next_id FROM Matricula";
            stmt = conn.prepareStatement(getMaxIdSql);
            rs = stmt.executeQuery();

            long nextId = 1;
            if (rs.next()) {
                nextId = rs.getLong("next_id");
            }
            rs.close();
            stmt.close();

            String createSql = "INSERT INTO Matricula (id_matricula, cod_estudiante, cod_periodo, fecha_matricula, total_creditos, estado) " +
                              "VALUES (?, ?, ?, ?, ?, ?)";
            stmt = conn.prepareStatement(createSql);
            stmt.setLong(1, nextId);
            stmt.setString(2, studentId);
            stmt.setString(3, periodo);
            stmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(5, 0);
            stmt.setString(6, "activa");

            stmt.executeUpdate();
            return nextId;

        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { /* ignore */ }
            if (stmt != null) try { stmt.close(); } catch (SQLException e) { /* ignore */ }
            if (conn != null) try { conn.close(); } catch (SQLException e) { /* ignore */ }
        }
    }

    private String getCurrentPeriod() {
        // For now, return a default period. This should be configurable
        return "2025-I";
    }

    private Enrollment mapResultSetToEnrollment(ResultSet rs) throws SQLException {
        Enrollment enrollment = new Enrollment();

        enrollment.setEnrollmentId(rs.getLong("id_detalle"));
        enrollment.setIdDetalle(rs.getLong("id_detalle"));
        enrollment.setIdMatricula(rs.getLong("id_matricula"));
        enrollment.setIdGrupo(rs.getLong("id_grupo"));
        enrollment.setStudentId(rs.getString("cod_estudiante"));

        // Map course ID from asignatura code
        String codAsignatura = rs.getString("cod_asignatura");
        if (codAsignatura != null) {
            try {
                enrollment.setCourseId(Long.parseLong(codAsignatura));
            } catch (NumberFormatException e) {
                enrollment.setCourseId((long) codAsignatura.hashCode() & 0x7fffffffL);
            }
        }

        Timestamp fechaInscripcion = rs.getTimestamp("fecha_inscripcion");
        if (fechaInscripcion != null) {
            enrollment.setEnrollmentDate(fechaInscripcion.toLocalDateTime().toLocalDate());
        }
        
        enrollment.setStatus(Enrollment.EnrollmentStatus.fromDbValue(rs.getString("estado")));

        return enrollment;
    }
}
