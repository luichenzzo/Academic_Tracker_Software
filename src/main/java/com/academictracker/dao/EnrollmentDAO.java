package com.academictracker.dao;

import com.academictracker.model.Enrollment;
import com.academictracker.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Enrollment entity
 */
public class EnrollmentDAO {
    private static final Logger logger = LoggerFactory.getLogger(EnrollmentDAO.class);

    public Enrollment create(Enrollment enrollment) throws SQLException {
        String sql = "INSERT INTO enrollments (student_id, course_id, enrollment_date, status) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, new String[]{"enrollment_id"})) {
            
            stmt.setLong(1, enrollment.getStudentId());
            stmt.setLong(2, enrollment.getCourseId());
            stmt.setDate(3, enrollment.getEnrollmentDate() != null ? 
                Date.valueOf(enrollment.getEnrollmentDate()) : Date.valueOf(java.time.LocalDate.now()));
            stmt.setString(4, enrollment.getStatus().name());
            
            stmt.executeUpdate();
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    enrollment.setEnrollmentId(generatedKeys.getLong(1));
                }
            }
            
            logger.info("Enrollment created: studentId={}, courseId={}", 
                enrollment.getStudentId(), enrollment.getCourseId());
            return enrollment;
        }
    }

    public Optional<Enrollment> findById(Long enrollmentId) throws SQLException {
        String sql = "SELECT * FROM enrollments WHERE enrollment_id = ?";
        
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

    public List<Enrollment> findAll() throws SQLException {
        List<Enrollment> enrollments = new ArrayList<>();
        String sql = "SELECT * FROM enrollments ORDER BY enrollment_date DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                enrollments.add(mapResultSetToEnrollment(rs));
            }
        }
        return enrollments;
    }

    public List<Enrollment> findByStudentId(Long studentId) throws SQLException {
        List<Enrollment> enrollments = new ArrayList<>();
        String sql = "SELECT * FROM enrollments WHERE student_id = ? ORDER BY enrollment_date DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, studentId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    enrollments.add(mapResultSetToEnrollment(rs));
                }
            }
        }
        return enrollments;
    }

    public List<Enrollment> findByCourseId(Long courseId) throws SQLException {
        List<Enrollment> enrollments = new ArrayList<>();
        String sql = "SELECT * FROM enrollments WHERE course_id = ? ORDER BY enrollment_date DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, courseId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    enrollments.add(mapResultSetToEnrollment(rs));
                }
            }
        }
        return enrollments;
    }

    public Optional<Enrollment> findByStudentAndCourse(Long studentId, Long courseId) throws SQLException {
        String sql = "SELECT * FROM enrollments WHERE student_id = ? AND course_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, studentId);
            stmt.setLong(2, courseId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToEnrollment(rs));
                }
            }
        }
        return Optional.empty();
    }

    public void update(Enrollment enrollment) throws SQLException {
        String sql = "UPDATE enrollments SET student_id = ?, course_id = ?, enrollment_date = ?, status = ? WHERE enrollment_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, enrollment.getStudentId());
            stmt.setLong(2, enrollment.getCourseId());
            stmt.setDate(3, enrollment.getEnrollmentDate() != null ? 
                Date.valueOf(enrollment.getEnrollmentDate()) : null);
            stmt.setString(4, enrollment.getStatus().name());
            stmt.setLong(5, enrollment.getEnrollmentId());
            
            stmt.executeUpdate();
            logger.info("Enrollment updated: {}", enrollment.getEnrollmentId());
        }
    }

    public void delete(Long enrollmentId) throws SQLException {
        String sql = "DELETE FROM enrollments WHERE enrollment_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, enrollmentId);
            stmt.executeUpdate();
            logger.info("Enrollment deleted: {}", enrollmentId);
        }
    }

    private Enrollment mapResultSetToEnrollment(ResultSet rs) throws SQLException {
        Enrollment enrollment = new Enrollment();
        enrollment.setEnrollmentId(rs.getLong("enrollment_id"));
        enrollment.setStudentId(rs.getLong("student_id"));
        enrollment.setCourseId(rs.getLong("course_id"));
        
        Date enrollmentDate = rs.getDate("enrollment_date");
        if (enrollmentDate != null) {
            enrollment.setEnrollmentDate(enrollmentDate.toLocalDate());
        }
        
        enrollment.setStatus(Enrollment.EnrollmentStatus.valueOf(rs.getString("status")));
        
        return enrollment;
    }
}
