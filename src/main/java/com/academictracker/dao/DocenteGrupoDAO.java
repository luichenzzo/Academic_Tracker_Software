package com.academictracker.dao;

import com.academictracker.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for DocenteGrupo (Teacher-Group assignments)
 */
public class DocenteGrupoDAO {
    private static final Logger logger = LoggerFactory.getLogger(DocenteGrupoDAO.class);

    /**
     * Assign a teacher to a group
     */
    public void assignTeacherToGroup(Long idDocente, Long idGrupo, double horasGrupo, boolean esPrincipal) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();

            // First, get the next available ID
            String getMaxIdSql = "SELECT NVL(MAX(id_docente_grupo), 0) + 1 AS next_id FROM DocenteGrupo";
            stmt = conn.prepareStatement(getMaxIdSql);
            rs = stmt.executeQuery();

            long nextId = 1;
            if (rs.next()) {
                nextId = rs.getLong("next_id");
            }
            rs.close();
            stmt.close();

            // Now insert the new assignment with the generated ID
            String sql = "INSERT INTO DocenteGrupo (id_docente_grupo, id_docente, id_grupo, horas_grupo, es_principal) " +
                         "VALUES (?, ?, ?, ?, ?)";

            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, nextId);
            stmt.setLong(2, idDocente);
            stmt.setLong(3, idGrupo);
            stmt.setDouble(4, horasGrupo);
            stmt.setInt(5, esPrincipal ? 1 : 0);

            stmt.executeUpdate();
            logger.info("Teacher {} assigned to group {} with ID {}", idDocente, idGrupo, nextId);

        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { /* ignore */ }
            if (stmt != null) try { stmt.close(); } catch (SQLException e) { /* ignore */ }
            if (conn != null) try { conn.close(); } catch (SQLException e) { /* ignore */ }
        }
    }

    /**
     * Check if a teacher is already assigned to a group
     */
    public boolean isTeacherAssignedToGroup(Long idDocente, Long idGrupo) throws SQLException {
        String sql = "SELECT COUNT(*) FROM DocenteGrupo WHERE id_docente = ? AND id_grupo = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, idDocente);
            stmt.setLong(2, idGrupo);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        }
    }

    /**
     * Get all teachers assigned to a group
     */
    public List<Long> getTeachersForGroup(Long idGrupo) throws SQLException {
        String sql = "SELECT id_docente FROM DocenteGrupo WHERE id_grupo = ?";
        List<Long> teachers = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, idGrupo);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                teachers.add(rs.getLong("id_docente"));
            }
        }
        return teachers;
    }

    /**
     * Get all groups for a teacher
     */
    public List<Long> getGroupsForTeacher(Long idDocente) throws SQLException {
        String sql = "SELECT id_grupo FROM DocenteGrupo WHERE id_docente = ?";
        List<Long> groups = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, idDocente);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                groups.add(rs.getLong("id_grupo"));
            }
        }
        return groups;
    }

    /**
     * Remove a teacher from a group
     */
    public void removeTeacherFromGroup(Long idDocente, Long idGrupo) throws SQLException {
        String sql = "DELETE FROM DocenteGrupo WHERE id_docente = ? AND id_grupo = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, idDocente);
            stmt.setLong(2, idGrupo);

            stmt.executeUpdate();
            logger.info("Teacher {} removed from group {}", idDocente, idGrupo);
        }
    }

    /**
     * Get teacher assignments with details
     */
    public List<TeacherGroupAssignment> getTeacherAssignmentsWithDetails(Long idGrupo) throws SQLException {
        String sql = "SELECT dg.id_docente, d.nombres, d.apellidos, d.correo_institucional, " +
                     "dg.horas_grupo, dg.es_principal " +
                     "FROM DocenteGrupo dg " +
                     "JOIN Docente d ON dg.id_docente = d.id_docente " +
                     "WHERE dg.id_grupo = ?";

        List<TeacherGroupAssignment> assignments = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, idGrupo);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                TeacherGroupAssignment assignment = new TeacherGroupAssignment();
                assignment.idDocente = rs.getLong("id_docente");
                assignment.nombres = rs.getString("nombres");
                assignment.apellidos = rs.getString("apellidos");
                assignment.correoInstitucional = rs.getString("correo_institucional");
                assignment.horasGrupo = rs.getDouble("horas_grupo");
                assignment.esPrincipal = rs.getInt("es_principal") == 1;
                assignments.add(assignment);
            }
        }
        return assignments;
    }

    /**
     * Get all groups assigned to a teacher with full details
     */
    public List<GroupAssignmentDetails> getGroupsForTeacherWithDetails(Long idDocente) throws SQLException {
        String sql = "SELECT g.id_grupo, g.numero_grupo, g.cupo_maximo, g.cupo_ocupado, " +
                     "a.cod_asignatura, a.nombre as asignatura_nombre, " +
                     "p.cod_periodo, p.nombre as periodo_nombre, " +
                     "s.id_sede, s.nombre as sede_nombre, " +
                     "dg.horas_grupo, dg.es_principal " +
                     "FROM DocenteGrupo dg " +
                     "JOIN Grupo g ON dg.id_grupo = g.id_grupo " +
                     "JOIN Asignatura a ON g.cod_asignatura = a.cod_asignatura " +
                     "JOIN PeriodoAcademico p ON g.cod_periodo = p.cod_periodo " +
                     "JOIN Sede s ON g.id_sede = s.id_sede " +
                     "WHERE dg.id_docente = ? AND g.activo = 1 " +
                     "ORDER BY p.cod_periodo DESC, a.nombre, g.numero_grupo";

        List<GroupAssignmentDetails> groups = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, idDocente);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                GroupAssignmentDetails details = new GroupAssignmentDetails();
                details.idGrupo = rs.getLong("id_grupo");
                details.numeroGrupo = rs.getInt("numero_grupo");
                details.cupoMaximo = rs.getInt("cupo_maximo");
                details.cupoOcupado = rs.getInt("cupo_ocupado");
                details.codAsignatura = rs.getString("cod_asignatura");
                details.asignaturaNombre = rs.getString("asignatura_nombre");
                details.codPeriodo = rs.getString("cod_periodo");
                details.periodoNombre = rs.getString("periodo_nombre");
                details.idSede = rs.getLong("id_sede");
                details.sedeNombre = rs.getString("sede_nombre");
                details.horasGrupo = rs.getDouble("horas_grupo");
                details.esPrincipal = rs.getInt("es_principal") == 1;
                groups.add(details);
            }
        }
        return groups;
    }

    public static class TeacherGroupAssignment {
        public Long idDocente;
        public String nombres;
        public String apellidos;
        public String correoInstitucional;
        public double horasGrupo;
        public boolean esPrincipal;

        public String getFullName() {
            return nombres + " " + apellidos;
        }
    }

    public static class GroupAssignmentDetails {
        public Long idGrupo;
        public Integer numeroGrupo;
        public Integer cupoMaximo;
        public Integer cupoOcupado;
        public String codAsignatura;
        public String asignaturaNombre;
        public String codPeriodo;
        public String periodoNombre;
        public Long idSede;
        public String sedeNombre;
        public double horasGrupo;
        public boolean esPrincipal;

        public String getCapacityDisplay() {
            return cupoOcupado + " / " + cupoMaximo;
        }

        public int getAvailableSpots() {
            return cupoMaximo - cupoOcupado;
        }
    }
}
