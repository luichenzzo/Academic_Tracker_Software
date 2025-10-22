package com.academictracker.dao;

import com.academictracker.model.Teacher;
import com.academictracker.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Teacher entity - Bridge to Docente table
 */
public class TeacherDAO {
    private static final Logger logger = LoggerFactory.getLogger(TeacherDAO.class);

    public Teacher create(Teacher teacher) throws SQLException {
        // Get next available ID if not provided
        if (teacher.getIdDocente() == null) {
            teacher.setIdDocente(getNextId());
        }

        String sql = "INSERT INTO Docente (id_docente, numero_documento, tipo_documento, nombres, apellidos, correo_institucional, telefono, horas_asignadas, activo) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, teacher.getIdDocente());
            stmt.setString(2, teacher.getNumeroDocumento());
            stmt.setString(3, teacher.getTipoDocumento() != null ? teacher.getTipoDocumento() : "CC");
            stmt.setString(4, teacher.getNombres());
            stmt.setString(5, teacher.getApellidos());
            stmt.setString(6, teacher.getCorreoInstitucional());
            stmt.setString(7, teacher.getTelefono());
            stmt.setBigDecimal(8, teacher.getHorasAsignadas());
            stmt.setInt(9, teacher.isActivo() ? 1 : 0);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Creating teacher failed, no rows affected.");
            }

            logger.info("Teacher created: {} {} with ID: {}", teacher.getNombres(), teacher.getApellidos(), teacher.getIdDocente());
            return teacher;
        }
    }

    /**
     * Get next available ID for manual assignment
     */
    private Long getNextId() throws SQLException {
        String sql = "SELECT NVL(MAX(id_docente), 0) + 1 FROM Docente";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getLong(1);
            }
            return 1L;
        }
    }

    public Optional<Teacher> findById(Long teacherId) throws SQLException {
        String sql = "SELECT * FROM Docente WHERE id_docente = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, teacherId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToTeacher(rs));
                }
            }
        }

        return Optional.empty();
    }

    public Optional<Teacher> findByDocumento(String numeroDocumento) throws SQLException {
        String sql = "SELECT * FROM Docente WHERE numero_documento = ? AND activo = 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, numeroDocumento);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToTeacher(rs));
                }
            }
        }

        return Optional.empty();
    }

    public List<Teacher> findAll() throws SQLException {
        String sql = "SELECT * FROM Docente WHERE activo = 1 ORDER BY apellidos, nombres";

        List<Teacher> teachers = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                teachers.add(mapResultSetToTeacher(rs));
            }
        }

        return teachers;
    }

    public boolean update(Teacher teacher) throws SQLException {
        String sql = "UPDATE Docente SET numero_documento = ?, tipo_documento = ?, nombres = ?, apellidos = ?, correo_institucional = ?, telefono = ?, horas_asignadas = ?, activo = ? WHERE id_docente = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, teacher.getNumeroDocumento());
            stmt.setString(2, teacher.getTipoDocumento());
            stmt.setString(3, teacher.getNombres());
            stmt.setString(4, teacher.getApellidos());
            stmt.setString(5, teacher.getCorreoInstitucional());
            stmt.setString(6, teacher.getTelefono());
            stmt.setBigDecimal(7, teacher.getHorasAsignadas());
            stmt.setInt(8, teacher.isActivo() ? 1 : 0);
            stmt.setLong(9, teacher.getIdDocente());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Teacher updated successfully: {}", teacher.getIdDocente());
                return true;
            }
        }

        return false;
    }

    public boolean delete(Long teacherId) throws SQLException {
        String sql = "UPDATE Docente SET activo = 0 WHERE id_docente = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, teacherId);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Teacher deactivated successfully: {}", teacherId);
                return true;
            }
        }

        return false;
    }

    public boolean existsByDocumento(String numeroDocumento) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Docente WHERE numero_documento = ? AND activo = 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, numeroDocumento);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }

        return false;
    }

    private Teacher mapResultSetToTeacher(ResultSet rs) throws SQLException {
        Teacher teacher = new Teacher();

        teacher.setIdDocente(rs.getLong("id_docente"));
        teacher.setNumeroDocumento(rs.getString("numero_documento"));
        teacher.setTipoDocumento(rs.getString("tipo_documento"));
        teacher.setNombres(rs.getString("nombres"));
        teacher.setApellidos(rs.getString("apellidos"));
        teacher.setCorreoInstitucional(rs.getString("correo_institucional"));
        teacher.setTelefono(rs.getString("telefono"));
        teacher.setHorasAsignadas(rs.getBigDecimal("horas_asignadas"));
        teacher.setActivo(rs.getInt("activo") == 1);

        return teacher;
    }
}
