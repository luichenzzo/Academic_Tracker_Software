package com.academictracker.dao;

import com.academictracker.model.Matricula;
import com.academictracker.model.PeriodoAcademico;
import com.academictracker.model.Student;
import com.academictracker.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Matricula entity
 */
public class MatriculaDAO {
    private static final Logger logger = LoggerFactory.getLogger(MatriculaDAO.class);

    public Matricula create(Matricula matricula) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();

            // First, get the next available ID using Oracle pattern
            String getMaxIdSql = "SELECT NVL(MAX(id_matricula), 0) + 1 AS next_id FROM Matricula";
            stmt = conn.prepareStatement(getMaxIdSql);
            rs = stmt.executeQuery();

            long nextId = 1;
            if (rs.next()) {
                nextId = rs.getLong("next_id");
            }
            rs.close();
            stmt.close();

            // Now insert the new matricula with the generated ID
            String sql = "INSERT INTO Matricula (id_matricula, cod_estudiante, cod_periodo, fecha_matricula, total_creditos, estado) " +
                        "VALUES (?, ?, ?, ?, ?, ?)";

            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, nextId);
            stmt.setString(2, matricula.getCodEstudiante());
            stmt.setString(3, matricula.getCodPeriodo());
            stmt.setTimestamp(4, Timestamp.valueOf(matricula.getFechaMatricula() != null ?
                matricula.getFechaMatricula() : LocalDateTime.now()));
            stmt.setInt(5, matricula.getTotalCreditos() != null ? matricula.getTotalCreditos() : 0);
            stmt.setString(6, matricula.getEstado() != null ? matricula.getEstado() : "activa");

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Creating matricula failed, no rows affected.");
            }

            matricula.setIdMatricula(nextId);
            logger.info("Matricula created for student: {} in period: {} with ID: {}",
                matricula.getCodEstudiante(), matricula.getCodPeriodo(), nextId);
            return matricula;

        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { /* ignore */ }
            if (stmt != null) try { stmt.close(); } catch (SQLException e) { /* ignore */ }
            if (conn != null) try { conn.close(); } catch (SQLException e) { /* ignore */ }
        }
    }

    public Optional<Matricula> findById(Long idMatricula) throws SQLException {
        String sql = "SELECT m.*, " +
                    "e.nombres as estudiante_nombres, e.apellidos as estudiante_apellidos, " +
                    "p.nombre as periodo_nombre " +
                    "FROM Matricula m " +
                    "LEFT JOIN Estudiante e ON m.cod_estudiante = e.cod_estudiante " +
                    "LEFT JOIN PeriodoAcademico p ON m.cod_periodo = p.cod_periodo " +
                    "WHERE m.id_matricula = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, idMatricula);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToMatricula(rs));
                }
            }
        }

        return Optional.empty();
    }

    public List<Matricula> findByStudent(String codEstudiante) throws SQLException {
        String sql = "SELECT m.*, " +
                    "e.nombres as estudiante_nombres, e.apellidos as estudiante_apellidos, " +
                    "p.nombre as periodo_nombre " +
                    "FROM Matricula m " +
                    "LEFT JOIN Estudiante e ON m.cod_estudiante = e.cod_estudiante " +
                    "LEFT JOIN PeriodoAcademico p ON m.cod_periodo = p.cod_periodo " +
                    "WHERE m.cod_estudiante = ? " +
                    "ORDER BY m.fecha_matricula DESC";

        List<Matricula> matriculas = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, codEstudiante);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    matriculas.add(mapResultSetToMatricula(rs));
                }
            }
        }

        return matriculas;
    }

    public List<Matricula> findByPeriod(String codPeriodo) throws SQLException {
        String sql = "SELECT m.*, " +
                    "e.nombres as estudiante_nombres, e.apellidos as estudiante_apellidos, " +
                    "p.nombre as periodo_nombre " +
                    "FROM Matricula m " +
                    "LEFT JOIN Estudiante e ON m.cod_estudiante = e.cod_estudiante " +
                    "LEFT JOIN PeriodoAcademico p ON m.cod_periodo = p.cod_periodo " +
                    "WHERE m.cod_periodo = ? " +
                    "ORDER BY m.fecha_matricula DESC";

        List<Matricula> matriculas = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, codPeriodo);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    matriculas.add(mapResultSetToMatricula(rs));
                }
            }
        }

        return matriculas;
    }

    public List<Matricula> findAll() throws SQLException {
        String sql = "SELECT m.*, " +
                    "e.nombres as estudiante_nombres, e.apellidos as estudiante_apellidos, " +
                    "p.nombre as periodo_nombre " +
                    "FROM Matricula m " +
                    "LEFT JOIN Estudiante e ON m.cod_estudiante = e.cod_estudiante " +
                    "LEFT JOIN PeriodoAcademico p ON m.cod_periodo = p.cod_periodo " +
                    "ORDER BY m.fecha_matricula DESC";

        List<Matricula> matriculas = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                matriculas.add(mapResultSetToMatricula(rs));
            }
        }

        return matriculas;
    }

    public boolean update(Matricula matricula) throws SQLException {
        String sql = "UPDATE Matricula SET total_creditos = ?, estado = ? WHERE id_matricula = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, matricula.getTotalCreditos() != null ? matricula.getTotalCreditos() : 0);
            stmt.setString(2, matricula.getEstado() != null ? matricula.getEstado() : "activa");
            stmt.setLong(3, matricula.getIdMatricula());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Matricula updated successfully: {}", matricula.getIdMatricula());
                return true;
            }
        }

        return false;
    }

    public boolean delete(Long idMatricula) throws SQLException {
        String sql = "DELETE FROM Matricula WHERE id_matricula = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, idMatricula);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Matricula deleted successfully: {}", idMatricula);
                return true;
            }
        }

        return false;
    }

    private Matricula mapResultSetToMatricula(ResultSet rs) throws SQLException {
        Matricula matricula = new Matricula();

        matricula.setIdMatricula(rs.getLong("id_matricula"));
        matricula.setCodEstudiante(rs.getString("cod_estudiante"));
        matricula.setCodPeriodo(rs.getString("cod_periodo"));

        Timestamp fechaMatricula = rs.getTimestamp("fecha_matricula");
        if (fechaMatricula != null) {
            matricula.setFechaMatricula(fechaMatricula.toLocalDateTime());
        }

        matricula.setTotalCreditos(rs.getInt("total_creditos"));
        matricula.setEstado(rs.getString("estado"));

        // Set related objects if available
        String estudianteNombres = rs.getString("estudiante_nombres");
        if (estudianteNombres != null) {
            Student estudiante = new Student();
            estudiante.setCodEstudiante(rs.getString("cod_estudiante"));
            estudiante.setNombres(estudianteNombres);
            estudiante.setApellidos(rs.getString("estudiante_apellidos"));
            matricula.setEstudiante(estudiante);
        }

        String periodoNombre = rs.getString("periodo_nombre");
        if (periodoNombre != null) {
            PeriodoAcademico periodo = new PeriodoAcademico();
            periodo.setCodPeriodo(rs.getString("cod_periodo"));
            periodo.setNombre(periodoNombre);
            matricula.setPeriodoAcademico(periodo);
        }

        return matricula;
    }
}
