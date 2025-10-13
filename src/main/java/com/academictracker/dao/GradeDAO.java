package com.academictracker.dao;

import com.academictracker.model.Grade;
import com.academictracker.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Grade entity
 */
public class GradeDAO {
    private static final Logger logger = LoggerFactory.getLogger(GradeDAO.class);

    public Grade create(Grade grade) throws SQLException {
        String sql = "INSERT INTO grades (enrollment_id, grade_value, grade_letter, comments, graded_by, graded_at) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, new String[]{"grade_id"})) {
            
            stmt.setLong(1, grade.getEnrollmentId());
            
            if (grade.getGradeValue() != null) {
                stmt.setDouble(2, grade.getGradeValue());
            } else {
                stmt.setNull(2, Types.DOUBLE);
            }
            
            stmt.setString(3, grade.getGradeLetter());
            stmt.setString(4, grade.getComments());
            
            if (grade.getGradedBy() != null) {
                stmt.setLong(5, grade.getGradedBy());
            } else {
                stmt.setNull(5, Types.BIGINT);
            }
            
            stmt.setTimestamp(6, grade.getGradedAt() != null ? 
                Timestamp.valueOf(grade.getGradedAt()) : Timestamp.valueOf(java.time.LocalDateTime.now()));
            
            stmt.executeUpdate();
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    grade.setGradeId(generatedKeys.getLong(1));
                }
            }
            
            logger.info("Grade created: enrollmentId={}", grade.getEnrollmentId());
            return grade;
        }
    }

    public Optional<Grade> findById(Long gradeId) throws SQLException {
        String sql = "SELECT * FROM grades WHERE grade_id = ?";
        
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

    public Optional<Grade> findByEnrollmentId(Long enrollmentId) throws SQLException {
        String sql = "SELECT * FROM grades WHERE enrollment_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, enrollmentId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToGrade(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<Grade> findAll() throws SQLException {
        List<Grade> grades = new ArrayList<>();
        String sql = "SELECT * FROM grades ORDER BY graded_at DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                grades.add(mapResultSetToGrade(rs));
            }
        }
        return grades;
    }

    public List<Grade> findByGradedBy(Long teacherId) throws SQLException {
        List<Grade> grades = new ArrayList<>();
        String sql = "SELECT * FROM grades WHERE graded_by = ? ORDER BY graded_at DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, teacherId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    grades.add(mapResultSetToGrade(rs));
                }
            }
        }
        return grades;
    }

    public void update(Grade grade) throws SQLException {
        String sql = "UPDATE grades SET enrollment_id = ?, grade_value = ?, grade_letter = ?, comments = ?, graded_by = ?, graded_at = ? WHERE grade_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, grade.getEnrollmentId());
            
            if (grade.getGradeValue() != null) {
                stmt.setDouble(2, grade.getGradeValue());
            } else {
                stmt.setNull(2, Types.DOUBLE);
            }
            
            stmt.setString(3, grade.getGradeLetter());
            stmt.setString(4, grade.getComments());
            
            if (grade.getGradedBy() != null) {
                stmt.setLong(5, grade.getGradedBy());
            } else {
                stmt.setNull(5, Types.BIGINT);
            }
            
            stmt.setTimestamp(6, grade.getGradedAt() != null ? 
                Timestamp.valueOf(grade.getGradedAt()) : null);
            stmt.setLong(7, grade.getGradeId());
            
            stmt.executeUpdate();
            logger.info("Grade updated: {}", grade.getGradeId());
        }
    }

    public void delete(Long gradeId) throws SQLException {
        String sql = "DELETE FROM grades WHERE grade_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, gradeId);
            stmt.executeUpdate();
            logger.info("Grade deleted: {}", gradeId);
        }
    }

    private Grade mapResultSetToGrade(ResultSet rs) throws SQLException {
        Grade grade = new Grade();
        grade.setGradeId(rs.getLong("grade_id"));
        grade.setEnrollmentId(rs.getLong("enrollment_id"));
        
        double gradeValue = rs.getDouble("grade_value");
        if (!rs.wasNull()) {
            grade.setGradeValue(gradeValue);
        }
        
        grade.setGradeLetter(rs.getString("grade_letter"));
        grade.setComments(rs.getString("comments"));
        
        long gradedBy = rs.getLong("graded_by");
        if (!rs.wasNull()) {
            grade.setGradedBy(gradedBy);
        }
        
        Timestamp gradedAt = rs.getTimestamp("graded_at");
        if (gradedAt != null) {
            grade.setGradedAt(gradedAt.toLocalDateTime());
        }
        
        return grade;
    }
}
