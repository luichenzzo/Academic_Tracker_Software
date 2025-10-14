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
        // First, ensure there's a matricula for this student and period
        Long matriculaId = getOrCreateMatricula(enrollment.getStudentId(), getCurrentPeriod());

        String sql = "INSERT INTO DetalleMatricula (id_matricula, id_grupo, fecha_inscripcion, estado, numero_intento) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, new String[]{"id_detalle"})) {

            stmt.setLong(1, matriculaId);
            stmt.setLong(2, enrollment.getIdGrupo() != null ? enrollment.getIdGrupo() : 1); // Default group
            stmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setString(4, enrollment.getStatus().getDbValue());
            stmt.setInt(5, 1); // First attempt

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Creating enrollment failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    enrollment.setEnrollmentId(generatedKeys.getLong(1));
                    enrollment.setIdDetalle(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("Creating enrollment failed, no ID obtained.");
                }
            }
            
            logger.info("Enrollment created for student: {}", enrollment.getStudentId());
            return enrollment;
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
        // First try to find existing matricula
        String findSql = "SELECT id_matricula FROM Matricula WHERE cod_estudiante = ? AND cod_periodo = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(findSql)) {

            stmt.setString(1, studentId);
            stmt.setString(2, periodo);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("id_matricula");
                }
            }
        }

        // If not found, create new matricula
        String createSql = "INSERT INTO Matricula (cod_estudiante, cod_periodo, fecha_matricula, total_creditos, estado) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(createSql, new String[]{"id_matricula"})) {

            stmt.setString(1, studentId);
            stmt.setString(2, periodo);
            stmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(4, 0);
            stmt.setString(5, "activa");

            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1);
                }
            }
        }

        throw new SQLException("Could not create or find matricula");
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
