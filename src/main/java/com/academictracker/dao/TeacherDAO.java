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
 * Data Access Object for Teacher entity
 */
public class TeacherDAO {
    private static final Logger logger = LoggerFactory.getLogger(TeacherDAO.class);

    public Teacher create(Teacher teacher) throws SQLException {
        String sql = "INSERT INTO teachers (user_id, first_name, last_name, department, phone, hire_date) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, new String[]{"teacher_id"})) {
            
            stmt.setLong(1, teacher.getUserId());
            stmt.setString(2, teacher.getFirstName());
            stmt.setString(3, teacher.getLastName());
            stmt.setString(4, teacher.getDepartment());
            stmt.setString(5, teacher.getPhone());
            stmt.setDate(6, teacher.getHireDate() != null ? Date.valueOf(teacher.getHireDate()) : Date.valueOf(java.time.LocalDate.now()));
            
            stmt.executeUpdate();
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    teacher.setTeacherId(generatedKeys.getLong(1));
                }
            }
            
            logger.info("Teacher created: {} {}", teacher.getFirstName(), teacher.getLastName());
            return teacher;
        }
    }

    public Optional<Teacher> findById(Long teacherId) throws SQLException {
        String sql = "SELECT * FROM teachers WHERE teacher_id = ?";
        
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

    public Optional<Teacher> findByUserId(Long userId) throws SQLException {
        String sql = "SELECT * FROM teachers WHERE user_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToTeacher(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<Teacher> findAll() throws SQLException {
        List<Teacher> teachers = new ArrayList<>();
        String sql = "SELECT * FROM teachers ORDER BY last_name, first_name";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                teachers.add(mapResultSetToTeacher(rs));
            }
        }
        return teachers;
    }

    public void update(Teacher teacher) throws SQLException {
        String sql = "UPDATE teachers SET user_id = ?, first_name = ?, last_name = ?, department = ?, phone = ?, hire_date = ? WHERE teacher_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, teacher.getUserId());
            stmt.setString(2, teacher.getFirstName());
            stmt.setString(3, teacher.getLastName());
            stmt.setString(4, teacher.getDepartment());
            stmt.setString(5, teacher.getPhone());
            stmt.setDate(6, teacher.getHireDate() != null ? Date.valueOf(teacher.getHireDate()) : null);
            stmt.setLong(7, teacher.getTeacherId());
            
            stmt.executeUpdate();
            logger.info("Teacher updated: {}", teacher.getTeacherId());
        }
    }

    public void delete(Long teacherId) throws SQLException {
        String sql = "DELETE FROM teachers WHERE teacher_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, teacherId);
            stmt.executeUpdate();
            logger.info("Teacher deleted: {}", teacherId);
        }
    }

    private Teacher mapResultSetToTeacher(ResultSet rs) throws SQLException {
        Teacher teacher = new Teacher();
        teacher.setTeacherId(rs.getLong("teacher_id"));
        teacher.setUserId(rs.getLong("user_id"));
        teacher.setFirstName(rs.getString("first_name"));
        teacher.setLastName(rs.getString("last_name"));
        teacher.setDepartment(rs.getString("department"));
        teacher.setPhone(rs.getString("phone"));
        
        Date hireDate = rs.getDate("hire_date");
        if (hireDate != null) {
            teacher.setHireDate(hireDate.toLocalDate());
        }
        
        return teacher;
    }
}
