package com.academictracker.dao;

import com.academictracker.model.Program;
import com.academictracker.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Program entity
 */
public class ProgramDAO {
    private static final Logger logger = LoggerFactory.getLogger(ProgramDAO.class);

    public Program create(Program program) throws SQLException {
        String sql = "INSERT INTO programs (program_code, program_name, description, duration_years, credits_required) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, new String[]{"program_id"})) {
            
            stmt.setString(1, program.getProgramCode());
            stmt.setString(2, program.getProgramName());
            stmt.setString(3, program.getDescription());
            stmt.setInt(4, program.getDurationYears());
            stmt.setInt(5, program.getCreditsRequired());
            
            stmt.executeUpdate();
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    program.setProgramId(generatedKeys.getLong(1));
                }
            }
            
            logger.info("Program created: {}", program.getProgramName());
            return program;
        }
    }

    public Optional<Program> findById(Long programId) throws SQLException {
        String sql = "SELECT * FROM programs WHERE program_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, programId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToProgram(rs));
                }
            }
        }
        return Optional.empty();
    }

    public Optional<Program> findByCode(String programCode) throws SQLException {
        String sql = "SELECT * FROM programs WHERE program_code = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, programCode);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToProgram(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<Program> findAll() throws SQLException {
        List<Program> programs = new ArrayList<>();
        String sql = "SELECT * FROM programs ORDER BY program_name";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                programs.add(mapResultSetToProgram(rs));
            }
        }
        return programs;
    }

    public void update(Program program) throws SQLException {
        String sql = "UPDATE programs SET program_code = ?, program_name = ?, description = ?, duration_years = ?, credits_required = ? WHERE program_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, program.getProgramCode());
            stmt.setString(2, program.getProgramName());
            stmt.setString(3, program.getDescription());
            stmt.setInt(4, program.getDurationYears());
            stmt.setInt(5, program.getCreditsRequired());
            stmt.setLong(6, program.getProgramId());
            
            stmt.executeUpdate();
            logger.info("Program updated: {}", program.getProgramId());
        }
    }

    public void delete(Long programId) throws SQLException {
        String sql = "DELETE FROM programs WHERE program_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, programId);
            stmt.executeUpdate();
            logger.info("Program deleted: {}", programId);
        }
    }

    private Program mapResultSetToProgram(ResultSet rs) throws SQLException {
        Program program = new Program();
        program.setProgramId(rs.getLong("program_id"));
        program.setProgramCode(rs.getString("program_code"));
        program.setProgramName(rs.getString("program_name"));
        program.setDescription(rs.getString("description"));
        program.setDurationYears(rs.getInt("duration_years"));
        program.setCreditsRequired(rs.getInt("credits_required"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            program.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        return program;
    }
}
