package com.academictracker.dao;

import com.academictracker.model.Course;
import com.academictracker.model.Asignatura;
import com.academictracker.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Course entity - Bridge to Asignatura table
 * This maintains compatibility with existing Course model while using new database structure
 */
public class CourseDAO {
    private static final Logger logger = LoggerFactory.getLogger(CourseDAO.class);

    public Course create(Course course) throws SQLException {
        String sql = "INSERT INTO Asignatura (cod_asignatura, nombre, creditos, horas_semanales, semestre_sugerido, es_trabajo_grado, id_tipo, cod_programa) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, course.getCourseCode());
            stmt.setString(2, course.getCourseName());
            stmt.setInt(3, course.getCredits());
            stmt.setInt(4, course.getCredits() * 2); // Estimate hours per week
            stmt.setInt(5, course.getSemester() != null ? Integer.parseInt(course.getSemester()) : 1);
            stmt.setInt(6, 0); // Default not trabajo grado
            stmt.setLong(7, 1); // Default tipo asignatura (basic)
            stmt.setLong(8, course.getProgramId());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Creating course failed, no rows affected.");
            }
            
            logger.info("Course created: {}", course.getCourseName());
            return course;
        }
    }

    public Optional<Course> findById(Long courseId) throws SQLException {
        String sql = "SELECT a.*, ta.tipo, p.nombre as programa_nombre " +
                    "FROM Asignatura a " +
                    "LEFT JOIN TipoAsignatura ta ON a.id_tipo = ta.id_tipo " +
                    "LEFT JOIN ProgramaAcademico p ON a.cod_programa = p.cod_programa " +
                    "WHERE a.cod_asignatura = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, courseId.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCourse(rs));
                }
            }
        }

        return Optional.empty();
    }

    public List<Course> findAll() throws SQLException {
        String sql = "SELECT a.*, ta.tipo, p.nombre as programa_nombre " +
                    "FROM Asignatura a " +
                    "LEFT JOIN TipoAsignatura ta ON a.id_tipo = ta.id_tipo " +
                    "LEFT JOIN ProgramaAcademico p ON a.cod_programa = p.cod_programa " +
                    "ORDER BY a.nombre";

        List<Course> courses = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                courses.add(mapResultSetToCourse(rs));
            }
        }

        return courses;
    }

    public List<Course> findByProgram(Long programId) throws SQLException {
        String sql = "SELECT a.*, ta.tipo, p.nombre as programa_nombre " +
                    "FROM Asignatura a " +
                    "LEFT JOIN TipoAsignatura ta ON a.id_tipo = ta.id_tipo " +
                    "LEFT JOIN ProgramaAcademico p ON a.cod_programa = p.cod_programa " +
                    "WHERE a.cod_programa = ? " +
                    "ORDER BY a.semestre_sugerido, a.nombre";

        List<Course> courses = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, programId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    courses.add(mapResultSetToCourse(rs));
                }
            }
        }

        return courses;
    }

    public boolean update(Course course) throws SQLException {
        String sql = "UPDATE Asignatura SET nombre = ?, creditos = ?, horas_semanales = ?, semestre_sugerido = ?, cod_programa = ? WHERE cod_asignatura = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, course.getCourseName());
            stmt.setInt(2, course.getCredits());
            stmt.setInt(3, course.getCredits() * 2); // Estimate hours per week
            stmt.setInt(4, course.getSemester() != null ? Integer.parseInt(course.getSemester()) : 1);
            stmt.setLong(5, course.getProgramId());
            stmt.setString(6, course.getCourseCode());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Course updated successfully: {}", course.getCourseName());
                return true;
            }
        }

        return false;
    }

    public boolean delete(Long courseId) throws SQLException {
        String sql = "DELETE FROM Asignatura WHERE cod_asignatura = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, courseId.toString());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Course deleted successfully: {}", courseId);
                return true;
            }
        }

        return false;
    }

    private Course mapResultSetToCourse(ResultSet rs) throws SQLException {
        Course course = new Course();

        // Map from Asignatura to Course for compatibility
        try {
            course.setCourseId(Long.parseLong(rs.getString("cod_asignatura")));
        } catch (NumberFormatException e) {
            course.setCourseId(rs.getString("cod_asignatura").hashCode() & 0x7fffffffL); // Generate ID from code
        }
        
        course.setCourseCode(rs.getString("cod_asignatura"));
        course.setCourseName(rs.getString("nombre"));
        course.setDescription(rs.getString("tipo")); // Use tipo as description
        course.setCredits(rs.getInt("creditos"));
        course.setProgramId(rs.getLong("cod_programa"));
        course.setSemester(String.valueOf(rs.getInt("semestre_sugerido")));
        course.setMaxStudents(30); // Default value

        return course;
    }
}
