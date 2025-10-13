package com.academictracker.dao;

import com.academictracker.model.Course;
import com.academictracker.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Course entity
 */
public class CourseDAO {
    private static final Logger logger = LoggerFactory.getLogger(CourseDAO.class);

    public Course create(Course course) throws SQLException {
        String sql = "INSERT INTO courses (course_code, course_name, description, credits, program_id, teacher_id, semester, max_students) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, new String[]{"course_id"})) {
            
            stmt.setString(1, course.getCourseCode());
            stmt.setString(2, course.getCourseName());
            stmt.setString(3, course.getDescription());
            stmt.setInt(4, course.getCredits());
            stmt.setLong(5, course.getProgramId());
            
            if (course.getTeacherId() != null) {
                stmt.setLong(6, course.getTeacherId());
            } else {
                stmt.setNull(6, Types.BIGINT);
            }
            
            stmt.setString(7, course.getSemester());
            stmt.setInt(8, course.getMaxStudents());
            
            stmt.executeUpdate();
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    course.setCourseId(generatedKeys.getLong(1));
                }
            }
            
            logger.info("Course created: {}", course.getCourseName());
            return course;
        }
    }

    public Optional<Course> findById(Long courseId) throws SQLException {
        String sql = "SELECT * FROM courses WHERE course_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, courseId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCourse(rs));
                }
            }
        }
        return Optional.empty();
    }

    public Optional<Course> findByCode(String courseCode) throws SQLException {
        String sql = "SELECT * FROM courses WHERE course_code = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, courseCode);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCourse(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<Course> findAll() throws SQLException {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT * FROM courses ORDER BY course_code";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                courses.add(mapResultSetToCourse(rs));
            }
        }
        return courses;
    }

    public List<Course> findByProgramId(Long programId) throws SQLException {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT * FROM courses WHERE program_id = ? ORDER BY course_code";
        
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

    public List<Course> findByTeacherId(Long teacherId) throws SQLException {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT * FROM courses WHERE teacher_id = ? ORDER BY course_code";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, teacherId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    courses.add(mapResultSetToCourse(rs));
                }
            }
        }
        return courses;
    }

    public void update(Course course) throws SQLException {
        String sql = "UPDATE courses SET course_code = ?, course_name = ?, description = ?, credits = ?, program_id = ?, teacher_id = ?, semester = ?, max_students = ? WHERE course_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, course.getCourseCode());
            stmt.setString(2, course.getCourseName());
            stmt.setString(3, course.getDescription());
            stmt.setInt(4, course.getCredits());
            stmt.setLong(5, course.getProgramId());
            
            if (course.getTeacherId() != null) {
                stmt.setLong(6, course.getTeacherId());
            } else {
                stmt.setNull(6, Types.BIGINT);
            }
            
            stmt.setString(7, course.getSemester());
            stmt.setInt(8, course.getMaxStudents());
            stmt.setLong(9, course.getCourseId());
            
            stmt.executeUpdate();
            logger.info("Course updated: {}", course.getCourseId());
        }
    }

    public void delete(Long courseId) throws SQLException {
        String sql = "DELETE FROM courses WHERE course_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, courseId);
            stmt.executeUpdate();
            logger.info("Course deleted: {}", courseId);
        }
    }

    private Course mapResultSetToCourse(ResultSet rs) throws SQLException {
        Course course = new Course();
        course.setCourseId(rs.getLong("course_id"));
        course.setCourseCode(rs.getString("course_code"));
        course.setCourseName(rs.getString("course_name"));
        course.setDescription(rs.getString("description"));
        course.setCredits(rs.getInt("credits"));
        course.setProgramId(rs.getLong("program_id"));
        
        long teacherId = rs.getLong("teacher_id");
        if (!rs.wasNull()) {
            course.setTeacherId(teacherId);
        }
        
        course.setSemester(rs.getString("semester"));
        course.setMaxStudents(rs.getInt("max_students"));
        
        return course;
    }
}
