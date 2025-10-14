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
 * Data Access Object for Program entity - Bridge to ProgramaAcademico table
 */
public class ProgramDAO {
    private static final Logger logger = LoggerFactory.getLogger(ProgramDAO.class);

    public Program create(Program program) throws SQLException {
        String sql = "INSERT INTO ProgramaAcademico (cod_programa, codigo_programa, nombre, creditos_totales, duracion_semestres, id_tipo_programa, id_facultad) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, program.getCodPrograma());
            stmt.setString(2, program.getCodigoPrograma());
            stmt.setString(3, program.getNombre());
            stmt.setInt(4, program.getCreditosTotales() != null ? program.getCreditosTotales() : 0);
            stmt.setInt(5, program.getDuracionSemestres() != null ? program.getDuracionSemestres() : 10);
            stmt.setLong(6, program.getIdTipoPrograma() != null ? program.getIdTipoPrograma() : 1); // Default to pregrado
            stmt.setLong(7, program.getIdFacultad() != null ? program.getIdFacultad() : 1); // Default faculty

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Creating program failed, no rows affected.");
            }
            
            logger.info("Program created: {}", program.getNombre());
            return program;
        }
    }

    public Optional<Program> findById(Long programId) throws SQLException {
        String sql = "SELECT p.*, tp.nombre as tipo_programa, f.nombre as facultad_nombre " +
                    "FROM ProgramaAcademico p " +
                    "LEFT JOIN TipoPrograma tp ON p.id_tipo_programa = tp.id_tipo_programa " +
                    "LEFT JOIN Facultad f ON p.id_facultad = f.id_facultad " +
                    "WHERE p.cod_programa = ?";

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

    public List<Program> findAll() throws SQLException {
        String sql = "SELECT p.*, tp.nombre as tipo_programa, f.nombre as facultad_nombre " +
                    "FROM ProgramaAcademico p " +
                    "LEFT JOIN TipoPrograma tp ON p.id_tipo_programa = tp.id_tipo_programa " +
                    "LEFT JOIN Facultad f ON p.id_facultad = f.id_facultad " +
                    "ORDER BY p.nombre";

        List<Program> programs = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                programs.add(mapResultSetToProgram(rs));
            }
        }

        return programs;
    }

    public List<Program> findByFaculty(Long facultyId) throws SQLException {
        String sql = "SELECT p.*, tp.nombre as tipo_programa, f.nombre as facultad_nombre " +
                    "FROM ProgramaAcademico p " +
                    "LEFT JOIN TipoPrograma tp ON p.id_tipo_programa = tp.id_tipo_programa " +
                    "LEFT JOIN Facultad f ON p.id_facultad = f.id_facultad " +
                    "WHERE p.id_facultad = ? " +
                    "ORDER BY p.nombre";

        List<Program> programs = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, facultyId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    programs.add(mapResultSetToProgram(rs));
                }
            }
        }

        return programs;
    }

    public boolean update(Program program) throws SQLException {
        String sql = "UPDATE ProgramaAcademico SET codigo_programa = ?, nombre = ?, creditos_totales = ?, duracion_semestres = ?, id_tipo_programa = ?, id_facultad = ? WHERE cod_programa = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, program.getCodigoPrograma());
            stmt.setString(2, program.getNombre());
            stmt.setInt(3, program.getCreditosTotales() != null ? program.getCreditosTotales() : 0);
            stmt.setInt(4, program.getDuracionSemestres() != null ? program.getDuracionSemestres() : 10);
            stmt.setLong(5, program.getIdTipoPrograma() != null ? program.getIdTipoPrograma() : 1);
            stmt.setLong(6, program.getIdFacultad() != null ? program.getIdFacultad() : 1);
            stmt.setLong(7, program.getCodPrograma());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Program updated successfully: {}", program.getNombre());
                return true;
            }
        }

        return false;
    }

    public boolean delete(Long programId) throws SQLException {
        String sql = "DELETE FROM ProgramaAcademico WHERE cod_programa = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, programId);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Program deleted successfully: {}", programId);
                return true;
            }
        }

        return false;
    }

    // Legacy compatibility methods
    public Optional<Program> findByCode(String programCode) throws SQLException {
        String sql = "SELECT p.*, tp.nombre as tipo_programa, f.nombre as facultad_nombre " +
                    "FROM ProgramaAcademico p " +
                    "LEFT JOIN TipoPrograma tp ON p.id_tipo_programa = tp.id_tipo_programa " +
                    "LEFT JOIN Facultad f ON p.id_facultad = f.id_facultad " +
                    "WHERE p.codigo_programa = ?";

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

    private Program mapResultSetToProgram(ResultSet rs) throws SQLException {
        Program program = new Program();

        program.setCodPrograma(rs.getLong("cod_programa"));
        program.setCodigoPrograma(rs.getString("codigo_programa"));
        program.setNombre(rs.getString("nombre"));
        program.setCreditosTotales(rs.getInt("creditos_totales"));
        program.setDuracionSemestres(rs.getInt("duracion_semestres"));
        program.setIdTipoPrograma(rs.getLong("id_tipo_programa"));
        program.setIdFacultad(rs.getLong("id_facultad"));

        // Legacy compatibility fields
        program.setProgramId(rs.getLong("cod_programa"));
        program.setProgramCode(rs.getString("codigo_programa"));
        program.setProgramName(rs.getString("nombre"));
        program.setDescription(rs.getString("tipo_programa"));
        program.setDurationYears(rs.getInt("duracion_semestres") / 2); // Convert semesters to years
        program.setCreditsRequired(rs.getInt("creditos_totales"));

        return program;
    }
}
