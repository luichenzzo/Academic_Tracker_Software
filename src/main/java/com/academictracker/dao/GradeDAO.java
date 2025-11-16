package com.academictracker.dao;

import com.academictracker.model.Grade;
import com.academictracker.model.GradeDisplay;
import com.academictracker.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/*
 GradeDAO provides both UI-friendly GradeDisplay lists and domain Grade CRUD methods.
 Uses Calificacion table for individual grade records.
*/
public class GradeDAO {

    public GradeDAO() {
    }

    /**
     * Returns grades for the given course code and group number for UI display.
     */
    public List<GradeDisplay> getGradesForGroup(String courseCode, Integer groupNumber) throws SQLException {
        List<GradeDisplay> result = new ArrayList<>();

        String sql = "SELECT e.cod_estudiante AS student_id, (e.nombres || ' ' || e.apellidos) AS student_name, nd.nota_definitiva AS grade_value " +
                "FROM Grupo g " +
                "JOIN DetalleMatricula dm ON dm.id_grupo = g.id_grupo " +
                "JOIN Matricula m ON m.id_matricula = dm.id_matricula " +
                "JOIN Estudiante e ON e.cod_estudiante = m.cod_estudiante " +
                "LEFT JOIN NotaDefinitiva nd ON nd.id_detalle = dm.id_detalle " +
                "WHERE g.cod_asignatura = ? AND g.numero_grupo = ? " +
                "ORDER BY e.apellidos, e.nombres";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, courseCode);
            if (groupNumber != null) {
                ps.setInt(2, groupNumber);
            } else {
                ps.setNull(2, java.sql.Types.INTEGER);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String studentId = rs.getString("student_id");
                    String studentName = rs.getString("student_name");

                    Double gradeVal = null;
                    Object gObj = rs.getObject("grade_value");
                    if (gObj != null) {
                        gradeVal = rs.getDouble("grade_value");
                    }

                    String gradeStr = gradeVal != null ? String.format("%.2f", gradeVal) : "N/A";
                    String status;
                    if (gradeVal == null) {
                        status = "Sin nota";
                    } else if (gradeVal >= 3.0) {
                        status = "Aprobado";
                    } else {
                        status = "Reprobado";
                    }

                    result.add(new GradeDisplay(studentId, studentName, gradeStr, status));
                }
            }
        }

        return result;
    }

    // ---------------- Domain CRUD for Grade (Calificacion) ----------------

    public Grade create(Grade grade) throws SQLException {
        String getMaxSql = "SELECT NVL(MAX(id_calificacion), 0) + 1 AS next_id FROM Calificacion";
        String insertSql = "INSERT INTO Calificacion (id_calificacion, id_detalle, id_regla, nota, fecha_registro, id_docente_registra) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement getMaxStmt = conn.prepareStatement(getMaxSql);
             ResultSet rs = getMaxStmt.executeQuery()) {

            long nextId = 1;
            if (rs.next()) {
                nextId = rs.getLong("next_id");
            }

            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                insertStmt.setLong(1, nextId);
                insertStmt.setLong(2, grade.getEnrollmentId() != null ? grade.getEnrollmentId() : 0);
                // id_regla is optional; store NULL
                insertStmt.setNull(3, java.sql.Types.BIGINT);
                insertStmt.setDouble(4, grade.getGradeValue() != null ? grade.getGradeValue() : 0.0);
                insertStmt.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
                if (grade.getGradedBy() != null) {
                    insertStmt.setLong(6, grade.getGradedBy());
                } else {
                    insertStmt.setNull(6, java.sql.Types.BIGINT);
                }

                int rows = insertStmt.executeUpdate();
                if (rows == 0) {
                    throw new SQLException("Creating grade failed, no rows affected.");
                }

                grade.setGradeId(nextId);
                return grade;
            }
        }
    }

    public boolean update(Grade grade) throws SQLException {
        if (grade.getGradeId() == null) return false;

        String sql = "UPDATE Calificacion SET nota = ?, id_docente_registra = ?, fecha_registro = ? WHERE id_calificacion = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDouble(1, grade.getGradeValue() != null ? grade.getGradeValue() : 0.0);
            if (grade.getGradedBy() != null) {
                ps.setLong(2, grade.getGradedBy());
            } else {
                ps.setNull(2, java.sql.Types.BIGINT);
            }
            ps.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            ps.setLong(4, grade.getGradeId());

            int rows = ps.executeUpdate();
            return rows > 0;
        }
    }

    public boolean delete(Long gradeId) throws SQLException {
        String sql = "DELETE FROM Calificacion WHERE id_calificacion = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, gradeId);
            int rows = ps.executeUpdate();
            return rows > 0;
        }
    }

    public Optional<Grade> findById(Long gradeId) throws SQLException {
        String sql = "SELECT * FROM Calificacion WHERE id_calificacion = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, gradeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Grade g = mapResultSetToGrade(rs);
                    return Optional.of(g);
                }
            }
        }

        return Optional.empty();
    }

    public List<Grade> findByEnrollment(Long enrollmentId) throws SQLException {
        String sql = "SELECT * FROM Calificacion WHERE id_detalle = ? ORDER BY fecha_registro DESC";
        List<Grade> list = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, enrollmentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToGrade(rs));
                }
            }
        }

        return list;
    }

    public List<Grade> findByStudent(String studentId) throws SQLException {
        String sql = "SELECT c.* FROM Calificacion c " +
                     "JOIN DetalleMatricula dm ON c.id_detalle = dm.id_detalle " +
                     "JOIN Matricula m ON dm.id_matricula = m.id_matricula " +
                     "WHERE m.cod_estudiante = ? ORDER BY c.fecha_registro DESC";
        List<Grade> list = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToGrade(rs));
                }
            }
        }

        return list;
    }

    private Grade mapResultSetToGrade(ResultSet rs) throws SQLException {
        Grade g = new Grade();
        g.setGradeId(rs.getLong("id_calificacion"));
        g.setEnrollmentId(rs.getLong("id_detalle"));
        double nota = rs.getDouble("nota");
        if (!rs.wasNull()) {
            g.setGradeValue(nota);
        }
        // Comments are not stored in Calificacion table; leave null
        long gradedBy = rs.getLong("id_docente_registra");
        if (!rs.wasNull()) {
            g.setGradedBy(gradedBy);
        }
        return g;
    }

    // ...existing code...
}
