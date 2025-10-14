package com.academictracker.dao;

import com.academictracker.model.Student;
import com.academictracker.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Estudiante entity
 */
public class StudentDAO {
    private static final Logger logger = LoggerFactory.getLogger(StudentDAO.class);

    public Student create(Student student) throws SQLException {
        String sql = "INSERT INTO Estudiante (cod_estudiante, numero_documento, tipo_documento, nombres, apellidos, correo_institucional, telefono, fecha_ingreso, nivel_riesgo, cod_programa, id_sede, activo) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, student.getCodEstudiante());
            stmt.setString(2, student.getNumeroDocumento());
            stmt.setString(3, student.getTipoDocumento());
            stmt.setString(4, student.getNombres());
            stmt.setString(5, student.getApellidos());
            stmt.setString(6, student.getCorreoInstitucional());
            stmt.setString(7, student.getTelefono());
            stmt.setDate(8, student.getFechaIngreso() != null ? Date.valueOf(student.getFechaIngreso()) : Date.valueOf(java.time.LocalDate.now()));
            stmt.setInt(9, student.getNivelRiesgo() != null ? student.getNivelRiesgo() : 0);
            stmt.setLong(10, student.getCodPrograma());
            stmt.setLong(11, student.getIdSede());
            stmt.setInt(12, student.isActivo() ? 1 : 0);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Creating student failed, no rows affected.");
            }

            logger.info("Student created: {} {}", student.getNombres(), student.getApellidos());
            return student;
        }
    }

    public Optional<Student> findById(String codEstudiante) throws SQLException {
        String sql = "SELECT e.*, p.nombre as programa_nombre, s.nombre as sede_nombre " +
                    "FROM Estudiante e " +
                    "LEFT JOIN ProgramaAcademico p ON e.cod_programa = p.cod_programa " +
                    "LEFT JOIN Sede s ON e.id_sede = s.id_sede " +
                    "WHERE e.cod_estudiante = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, codEstudiante);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToStudent(rs));
                }
            }
        }

        return Optional.empty();
    }

    // Legacy method for compatibility
    public Optional<Student> findById(Long studentId) throws SQLException {
        return findById(studentId.toString());
    }

    public Optional<Student> findByDocumento(String numeroDocumento) throws SQLException {
        String sql = "SELECT e.*, p.nombre as programa_nombre, s.nombre as sede_nombre " +
                    "FROM Estudiante e " +
                    "LEFT JOIN ProgramaAcademico p ON e.cod_programa = p.cod_programa " +
                    "LEFT JOIN Sede s ON e.id_sede = s.id_sede " +
                    "WHERE e.numero_documento = ? AND e.activo = 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, numeroDocumento);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToStudent(rs));
                }
            }
        }

        return Optional.empty();
    }

    public List<Student> findAll() throws SQLException {
        String sql = "SELECT e.*, p.nombre as programa_nombre, s.nombre as sede_nombre " +
                    "FROM Estudiante e " +
                    "LEFT JOIN ProgramaAcademico p ON e.cod_programa = p.cod_programa " +
                    "LEFT JOIN Sede s ON e.id_sede = s.id_sede " +
                    "WHERE e.activo = 1 " +
                    "ORDER BY e.apellidos, e.nombres";

        List<Student> students = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                students.add(mapResultSetToStudent(rs));
            }
        }

        return students;
    }

    public List<Student> findByProgram(Long codPrograma) throws SQLException {
        String sql = "SELECT e.*, p.nombre as programa_nombre, s.nombre as sede_nombre " +
                    "FROM Estudiante e " +
                    "LEFT JOIN ProgramaAcademico p ON e.cod_programa = p.cod_programa " +
                    "LEFT JOIN Sede s ON e.id_sede = s.id_sede " +
                    "WHERE e.cod_programa = ? AND e.activo = 1 " +
                    "ORDER BY e.apellidos, e.nombres";

        List<Student> students = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, codPrograma);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    students.add(mapResultSetToStudent(rs));
                }
            }
        }

        return students;
    }

    public boolean update(Student student) throws SQLException {
        String sql = "UPDATE Estudiante SET numero_documento = ?, tipo_documento = ?, nombres = ?, apellidos = ?, correo_institucional = ?, telefono = ?, nivel_riesgo = ?, cod_programa = ?, id_sede = ?, activo = ? WHERE cod_estudiante = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, student.getNumeroDocumento());
            stmt.setString(2, student.getTipoDocumento());
            stmt.setString(3, student.getNombres());
            stmt.setString(4, student.getApellidos());
            stmt.setString(5, student.getCorreoInstitucional());
            stmt.setString(6, student.getTelefono());
            stmt.setInt(7, student.getNivelRiesgo() != null ? student.getNivelRiesgo() : 0);
            stmt.setLong(8, student.getCodPrograma());
            stmt.setLong(9, student.getIdSede());
            stmt.setInt(10, student.isActivo() ? 1 : 0);
            stmt.setString(11, student.getCodEstudiante());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Student updated successfully: {}", student.getCodEstudiante());
                return true;
            }
        }

        return false;
    }

    public boolean delete(String codEstudiante) throws SQLException {
        String sql = "UPDATE Estudiante SET activo = 0 WHERE cod_estudiante = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, codEstudiante);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Student deactivated successfully: {}", codEstudiante);
                return true;
            }
        }

        return false;
    }

    // Legacy method for compatibility
    public boolean delete(Long studentId) throws SQLException {
        return delete(studentId.toString());
    }

    public boolean existsByDocumento(String numeroDocumento) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Estudiante WHERE numero_documento = ? AND activo = 1";

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

    private Student mapResultSetToStudent(ResultSet rs) throws SQLException {
        Student student = new Student();
        student.setCodEstudiante(rs.getString("cod_estudiante"));
        student.setNumeroDocumento(rs.getString("numero_documento"));
        student.setTipoDocumento(rs.getString("tipo_documento"));
        student.setNombres(rs.getString("nombres"));
        student.setApellidos(rs.getString("apellidos"));
        student.setCorreoInstitucional(rs.getString("correo_institucional"));
        student.setTelefono(rs.getString("telefono"));

        Date fechaIngreso = rs.getDate("fecha_ingreso");
        if (fechaIngreso != null) {
            student.setFechaIngreso(fechaIngreso.toLocalDate());
        }
        
        student.setNivelRiesgo(rs.getInt("nivel_riesgo"));
        student.setCodPrograma(rs.getLong("cod_programa"));
        student.setIdSede(rs.getLong("id_sede"));
        student.setActivo(rs.getInt("activo") == 1);

        return student;
    }
}
