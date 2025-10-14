package com.academictracker.dao;

import com.academictracker.model.Grade;
import com.academictracker.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Grade entity - Bridge to Calificacion/NotaDefinitiva tables
 */
public class GradeDAO {
    private static final Logger logger = LoggerFactory.getLogger(GradeDAO.class);

    public Grade create(Grade grade) throws SQLException {
        // First, find or create a ReglaEvaluacion for the grade
        Long reglaId = getOrCreateReglaEvaluacion(grade.getEnrollmentId(), "Evaluación General", 100.0);

        String sql = "INSERT INTO Calificacion (id_detalle, id_regla, nota, fecha_registro, id_docente_registra) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, new String[]{"id_calificacion"})) {

            stmt.setLong(1, grade.getEnrollmentId()); // Maps to id_detalle
            stmt.setLong(2, reglaId);
            stmt.setDouble(3, grade.getGradeValue() != null ? grade.getGradeValue() : 0.0);
            stmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setLong(5, grade.getGradedBy() != null ? grade.getGradedBy() : 1); // Default teacher

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Creating grade failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    grade.setGradeId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("Creating grade failed, no ID obtained.");
                }
            }
            
            // Also create/update NotaDefinitiva if this is a final grade
            updateNotaDefinitiva(grade.getEnrollmentId(), grade.getGradeValue());

            logger.info("Grade created for enrollment: {}", grade.getEnrollmentId());
            return grade;
        }
    }

    public Optional<Grade> findById(Long gradeId) throws SQLException {
        String sql = "SELECT c.*, re.nombre_item, re.porcentaje, d.nombres as docente_nombres, d.apellidos as docente_apellidos " +
                    "FROM Calificacion c " +
                    "LEFT JOIN ReglaEvaluacion re ON c.id_regla = re.id_regla " +
                    "LEFT JOIN Docente d ON c.id_docente_registra = d.id_docente " +
                    "WHERE c.id_calificacion = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, gradeId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToGrade(rs));
                }
            }
        }

        return Optional.empty();
    }

    public List<Grade> findByEnrollment(Long enrollmentId) throws SQLException {
        String sql = "SELECT c.*, re.nombre_item, re.porcentaje, d.nombres as docente_nombres, d.apellidos as docente_apellidos " +
                    "FROM Calificacion c " +
                    "LEFT JOIN ReglaEvaluacion re ON c.id_regla = re.id_regla " +
                    "LEFT JOIN Docente d ON c.id_docente_registra = d.id_docente " +
                    "WHERE c.id_detalle = ? " +
                    "ORDER BY c.fecha_registro DESC";

        List<Grade> grades = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, enrollmentId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    grades.add(mapResultSetToGrade(rs));
                }
            }
        }

        return grades;
    }

    public List<Grade> findByStudent(String studentId) throws SQLException {
        String sql = "SELECT c.*, re.nombre_item, re.porcentaje, d.nombres as docente_nombres, d.apellidos as docente_apellidos " +
                    "FROM Calificacion c " +
                    "JOIN DetalleMatricula dm ON c.id_detalle = dm.id_detalle " +
                    "JOIN Matricula m ON dm.id_matricula = m.id_matricula " +
                    "LEFT JOIN ReglaEvaluacion re ON c.id_regla = re.id_regla " +
                    "LEFT JOIN Docente d ON c.id_docente_registra = d.id_docente " +
                    "WHERE m.cod_estudiante = ? " +
                    "ORDER BY c.fecha_registro DESC";

        List<Grade> grades = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, studentId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    grades.add(mapResultSetToGrade(rs));
                }
            }
        }

        return grades;
    }

    public boolean update(Grade grade) throws SQLException {
        String sql = "UPDATE Calificacion SET nota = ?, id_docente_registra = ? WHERE id_calificacion = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDouble(1, grade.getGradeValue() != null ? grade.getGradeValue() : 0.0);
            stmt.setLong(2, grade.getGradedBy() != null ? grade.getGradedBy() : 1);
            stmt.setLong(3, grade.getGradeId());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                // Update NotaDefinitiva as well
                updateNotaDefinitiva(grade.getEnrollmentId(), grade.getGradeValue());
                logger.info("Grade updated successfully: {}", grade.getGradeId());
                return true;
            }
        }

        return false;
    }

    public boolean delete(Long gradeId) throws SQLException {
        String sql = "DELETE FROM Calificacion WHERE id_calificacion = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, gradeId);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Grade deleted successfully: {}", gradeId);
                return true;
            }
        }

        return false;
    }

    // Helper method to get or create ReglaEvaluacion
    private Long getOrCreateReglaEvaluacion(Long enrollmentId, String nombreItem, Double porcentaje) throws SQLException {
        // Find the group for this enrollment
        String findGroupSql = "SELECT dm.id_grupo FROM DetalleMatricula dm WHERE dm.id_detalle = ?";
        Long grupoId = null;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(findGroupSql)) {

            stmt.setLong(1, enrollmentId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    grupoId = rs.getLong("id_grupo");
                }
            }
        }

        if (grupoId == null) {
            throw new SQLException("Could not find group for enrollment");
        }

        // Try to find existing regla
        String findReglaSql = "SELECT id_regla FROM ReglaEvaluacion WHERE id_grupo = ? AND nombre_item = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(findReglaSql)) {

            stmt.setLong(1, grupoId);
            stmt.setString(2, nombreItem);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("id_regla");
                }
            }
        }

        // Create new regla if not found
        String createReglaSql = "INSERT INTO ReglaEvaluacion (id_grupo, nombre_item, porcentaje) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(createReglaSql, new String[]{"id_regla"})) {

            stmt.setLong(1, grupoId);
            stmt.setString(2, nombreItem);
            stmt.setBigDecimal(3, java.math.BigDecimal.valueOf(porcentaje));

            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1);
                }
            }
        }

        throw new SQLException("Could not create or find ReglaEvaluacion");
    }

    // Helper method to update NotaDefinitiva
    private void updateNotaDefinitiva(Long enrollmentId, Double nota) throws SQLException {
        String upsertSql = "MERGE INTO NotaDefinitiva nd " +
                          "USING (SELECT ? as id_detalle, ? as nota_definitiva FROM dual) src " +
                          "ON (nd.id_detalle = src.id_detalle) " +
                          "WHEN MATCHED THEN UPDATE SET nota_definitiva = src.nota_definitiva, fecha_calculo = CURRENT_TIMESTAMP " +
                          "WHEN NOT MATCHED THEN INSERT (id_detalle, nota_definitiva, fecha_calculo, cerrada) " +
                          "VALUES (src.id_detalle, src.nota_definitiva, CURRENT_TIMESTAMP, 0)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(upsertSql)) {

            stmt.setLong(1, enrollmentId);
            stmt.setDouble(2, nota != null ? nota : 0.0);

            stmt.executeUpdate();
        }
    }

    private Grade mapResultSetToGrade(ResultSet rs) throws SQLException {
        Grade grade = new Grade();

        grade.setGradeId(rs.getLong("id_calificacion"));
        grade.setEnrollmentId(rs.getLong("id_detalle"));
        grade.setGradeValue(rs.getDouble("nota"));
        grade.setGradedBy(rs.getLong("id_docente_registra"));

        Timestamp fechaRegistro = rs.getTimestamp("fecha_registro");
        if (fechaRegistro != null) {
            grade.setGradedAt(fechaRegistro.toLocalDateTime());
        }

        // Set grade letter based on Colombian grading system
        double nota = rs.getDouble("nota");
        if (nota >= 4.5) {
            grade.setGradeLetter("A");
        } else if (nota >= 4.0) {
            grade.setGradeLetter("B");
        } else if (nota >= 3.0) {
            grade.setGradeLetter("C");
        } else {
            grade.setGradeLetter("F");
        }
        
        grade.setComments(rs.getString("nombre_item"));

        return grade;
    }
}
