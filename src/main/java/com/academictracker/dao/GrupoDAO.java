package com.academictracker.dao;

import com.academictracker.model.Grupo;
import com.academictracker.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Grupo (Course Groups)
 */
public class GrupoDAO {
    private static final Logger logger = LoggerFactory.getLogger(GrupoDAO.class);

    public Grupo create(Grupo grupo) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();

            // First, get the next available ID
            String getMaxIdSql = "SELECT NVL(MAX(id_grupo), 0) + 1 AS next_id FROM Grupo";
            stmt = conn.prepareStatement(getMaxIdSql);
            rs = stmt.executeQuery();

            long nextId = 1;
            if (rs.next()) {
                nextId = rs.getLong("next_id");
            }
            rs.close();
            stmt.close();

            // Now insert the new grupo with the generated ID
            String sql = "INSERT INTO Grupo (id_grupo, numero_grupo, cupo_maximo, cupo_ocupado, cod_asignatura, cod_periodo, id_sede, activo) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, nextId);
            stmt.setInt(2, grupo.getNumeroGrupo());
            stmt.setInt(3, grupo.getCupoMaximo());
            stmt.setInt(4, grupo.getCupoOcupado() != null ? grupo.getCupoOcupado() : 0);
            stmt.setString(5, grupo.getCodAsignatura());
            stmt.setString(6, grupo.getCodPeriodo());
            stmt.setLong(7, grupo.getIdSede());
            stmt.setInt(8, grupo.isActivo() ? 1 : 0);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Creating grupo failed, no rows affected.");
            }

            grupo.setIdGrupo(nextId);
            logger.info("Grupo created with ID: {}", grupo.getIdGrupo());
            return grupo;

        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { /* ignore */ }
            if (stmt != null) try { stmt.close(); } catch (SQLException e) { /* ignore */ }
            if (conn != null) try { conn.close(); } catch (SQLException e) { /* ignore */ }
        }
    }

    public Optional<Grupo> findById(Long idGrupo) throws SQLException {
        String sql = "SELECT * FROM Grupo WHERE id_grupo = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, idGrupo);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToGrupo(rs));
            }
            return Optional.empty();
        }
    }

    public List<Grupo> findAll() throws SQLException {
        String sql = "SELECT g.*, " +
                     "s.nombre as sede_nombre, " +
                     "a.cod_asignatura, a.nombre as asignatura_nombre, a.creditos, a.semestre_sugerido, a.cod_programa, " +
                     "p.nombre as periodo_nombre " +
                     "FROM Grupo g " +
                     "LEFT JOIN Sede s ON g.id_sede = s.id_sede " +
                     "LEFT JOIN Asignatura a ON g.cod_asignatura = a.cod_asignatura " +
                     "LEFT JOIN PeriodoAcademico p ON g.cod_periodo = p.cod_periodo " +
                     "WHERE g.activo = 1 " +
                     "ORDER BY g.cod_asignatura, g.numero_grupo";
        List<Grupo> grupos = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                grupos.add(mapResultSetToGrupoWithJoins(rs));
            }
        }
        return grupos;
    }

    public List<Grupo> findByAsignatura(String codAsignatura) throws SQLException {
        String sql = "SELECT g.*, " +
                     "s.nombre as sede_nombre, " +
                     "a.cod_asignatura, a.nombre as asignatura_nombre, a.creditos, a.semestre_sugerido, a.cod_programa, " +
                     "p.nombre as periodo_nombre " +
                     "FROM Grupo g " +
                     "LEFT JOIN Sede s ON g.id_sede = s.id_sede " +
                     "LEFT JOIN Asignatura a ON g.cod_asignatura = a.cod_asignatura " +
                     "LEFT JOIN PeriodoAcademico p ON g.cod_periodo = p.cod_periodo " +
                     "WHERE g.cod_asignatura = ? AND g.activo = 1 " +
                     "ORDER BY g.numero_grupo";
        List<Grupo> grupos = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, codAsignatura);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                grupos.add(mapResultSetToGrupoWithJoins(rs));
            }
        }
        return grupos;
    }

    public boolean update(Grupo grupo) throws SQLException {
        String sql = "UPDATE Grupo SET numero_grupo = ?, cupo_maximo = ?, cupo_ocupado = ?, " +
                     "cod_asignatura = ?, cod_periodo = ?, id_sede = ?, activo = ? WHERE id_grupo = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, grupo.getNumeroGrupo());
            stmt.setInt(2, grupo.getCupoMaximo());
            stmt.setInt(3, grupo.getCupoOcupado() != null ? grupo.getCupoOcupado() : 0);
            stmt.setString(4, grupo.getCodAsignatura());
            stmt.setString(5, grupo.getCodPeriodo());
            stmt.setLong(6, grupo.getIdSede());
            stmt.setInt(7, grupo.isActivo() ? 1 : 0);
            stmt.setLong(8, grupo.getIdGrupo());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Grupo updated: {}", grupo.getIdGrupo());
                return true;
            }
            return false;
        }
    }

    public boolean delete(Long idGrupo) throws SQLException {
        String sql = "UPDATE Grupo SET activo = 0 WHERE id_grupo = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, idGrupo);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Grupo deactivated: {}", idGrupo);
                return true;
            }
            return false;
        }
    }

    private Grupo mapResultSetToGrupo(ResultSet rs) throws SQLException {
        Grupo grupo = new Grupo();
        grupo.setIdGrupo(rs.getLong("id_grupo"));
        grupo.setNumeroGrupo(rs.getInt("numero_grupo"));
        grupo.setCupoMaximo(rs.getInt("cupo_maximo"));
        grupo.setCupoOcupado(rs.getInt("cupo_ocupado"));
        grupo.setCodAsignatura(rs.getString("cod_asignatura"));
        grupo.setCodPeriodo(rs.getString("cod_periodo"));
        grupo.setIdSede(rs.getLong("id_sede"));
        grupo.setActivo(rs.getInt("activo") == 1);
        return grupo;
    }

    private Grupo mapResultSetToGrupoWithJoins(ResultSet rs) throws SQLException {
        Grupo grupo = new Grupo();
        grupo.setIdGrupo(rs.getLong("id_grupo"));
        grupo.setNumeroGrupo(rs.getInt("numero_grupo"));
        grupo.setCupoMaximo(rs.getInt("cupo_maximo"));
        grupo.setCupoOcupado(rs.getInt("cupo_ocupado"));
        grupo.setCodAsignatura(rs.getString("cod_asignatura"));
        grupo.setCodPeriodo(rs.getString("cod_periodo"));
        grupo.setIdSede(rs.getLong("id_sede"));
        grupo.setActivo(rs.getInt("activo") == 1);

        // Set related objects if available
        String sedeNombre = rs.getString("sede_nombre");
        if (sedeNombre != null) {
            com.academictracker.model.Sede sede = new com.academictracker.model.Sede();
            sede.setIdSede(rs.getLong("id_sede"));
            sede.setNombre(sedeNombre);
            grupo.setSede(sede);
        }

        String asignaturaNombre = rs.getString("asignatura_nombre");
        if (asignaturaNombre != null) {
            com.academictracker.model.Asignatura asignatura = new com.academictracker.model.Asignatura();
            asignatura.setCodAsignatura(rs.getString("cod_asignatura"));
            asignatura.setNombre(asignaturaNombre);

            try {
                asignatura.setCreditos(rs.getInt("creditos"));
                asignatura.setSemestreSugerido(rs.getInt("semestre_sugerido"));
                asignatura.setCodPrograma(rs.getLong("cod_programa"));
            } catch (SQLException e) {
                logger.debug("Some asignatura fields not available in result set");
            }

            grupo.setAsignatura(asignatura);
        }

        String periodoNombre = rs.getString("periodo_nombre");
        if (periodoNombre != null) {
            com.academictracker.model.PeriodoAcademico periodo = new com.academictracker.model.PeriodoAcademico();
            periodo.setCodPeriodo(rs.getString("cod_periodo"));
            periodo.setNombre(periodoNombre);
            grupo.setPeriodoAcademico(periodo);
        }

        return grupo;
    }
}
