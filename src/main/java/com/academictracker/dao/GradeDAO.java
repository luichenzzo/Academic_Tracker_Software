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

        String sql = "SELECT e.cod_estudiante AS student_id, (e.nombres || ' ' || e.apellidos) AS student_name, " +
                "NVL(nd.nota_definitiva, c.nota) AS grade_value, dm.id_detalle AS enrollment_id " +
                "FROM Grupo g " +
                "JOIN DetalleMatricula dm ON dm.id_grupo = g.id_grupo " +
                "JOIN Matricula m ON m.id_matricula = dm.id_matricula " +
                "JOIN Estudiante e ON e.cod_estudiante = m.cod_estudiante " +
                // Subquery c selects the most recent Calificacion per id_detalle
                "LEFT JOIN (SELECT id_detalle, nota FROM (SELECT id_detalle, nota, ROW_NUMBER() OVER (PARTITION BY id_detalle ORDER BY fecha_registro DESC) rn FROM Calificacion) WHERE rn = 1) c ON c.id_detalle = dm.id_detalle " +
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

                    Long enrollmentId = rs.getLong("enrollment_id");
                    if (rs.wasNull()) enrollmentId = null;

                    String gradeStr = gradeVal != null ? String.format("%.2f", gradeVal) : "N/A";
                    String status;
                    if (gradeVal == null) {
                        status = "Sin nota";
                    } else if (gradeVal >= 3.0) {
                        status = "Aprobado";
                    } else {
                        status = "Reprobado";
                    }

                    result.add(new GradeDisplay(studentId, studentName, gradeStr, status, enrollmentId));
                }
            }
        }

        return result;
    }

    // ---------------- Domain CRUD for Grade (Calificacion) ----------------

    public Grade create(Grade grade) throws SQLException {
        // Instead of inserting into Calificacion (which requires a non-null id_regla),
        // store the value in NotaDefinitiva (one per detalle) — insert or update.
        if (grade.getEnrollmentId() == null) {
            throw new SQLException("Enrollment id (id_detalle) is required to create a grade");
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Check if a NotaDefinitiva already exists for this enrollment (id_detalle)
            String findSql = "SELECT id_nota_definitiva FROM NotaDefinitiva WHERE id_detalle = ?";
            try (PreparedStatement psFind = conn.prepareStatement(findSql)) {
                psFind.setLong(1, grade.getEnrollmentId());
                try (ResultSet rs = psFind.executeQuery()) {
                    if (rs.next()) {
                        long existingId = rs.getLong("id_nota_definitiva");
                        String updateSql = "UPDATE NotaDefinitiva SET nota_definitiva = ?, fecha_calculo = CURRENT_TIMESTAMP WHERE id_nota_definitiva = ?";
                        try (PreparedStatement psUpd = conn.prepareStatement(updateSql)) {
                            psUpd.setDouble(1, grade.getGradeValue() != null ? grade.getGradeValue() : 0.0);
                            psUpd.setLong(2, existingId);
                            int updated = psUpd.executeUpdate();
                            if (updated == 0) {
                                throw new SQLException("Updating NotaDefinitiva failed, no rows affected.");
                            }
                        }

                        grade.setGradeId(existingId);
                        return grade;
                    }
                }
            }

            // Insert new NotaDefinitiva
            String getMaxSql = "SELECT NVL(MAX(id_nota_definitiva), 0) + 1 AS next_id FROM NotaDefinitiva";
            long nextId = 1;
            try (PreparedStatement psMax = conn.prepareStatement(getMaxSql);
                 ResultSet rsMax = psMax.executeQuery()) {
                if (rsMax.next()) {
                    nextId = rsMax.getLong("next_id");
                }
            }

            String insertSql = "INSERT INTO NotaDefinitiva (id_nota_definitiva, id_detalle, nota_definitiva, fecha_calculo, cerrada) VALUES (?, ?, ?, CURRENT_TIMESTAMP, 0)";
            try (PreparedStatement psIns = conn.prepareStatement(insertSql)) {
                psIns.setLong(1, nextId);
                psIns.setLong(2, grade.getEnrollmentId());
                psIns.setDouble(3, grade.getGradeValue() != null ? grade.getGradeValue() : 0.0);

                int rows = psIns.executeUpdate();
                if (rows == 0) {
                    throw new SQLException("Creating NotaDefinitiva failed, no rows affected.");
                }

                grade.setGradeId(nextId);
                return grade;
            }
        }
    }

    /**
     * Create a new Calificacion (individual grade record) for an enrollment.
     * If the enrollment's group has no evaluation rule (ReglaEvaluacion), a default rule is created.
     * This allows teachers to add multiple grade records per student (e.g., project evaluations).
     */
    public Grade createCalificacion(Grade grade) throws SQLException {
        if (grade.getEnrollmentId() == null) {
            throw new SQLException("Enrollment id (id_detalle) is required to create a Calificacion");
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            // 1) Find the group for the enrollment
            Long groupId = null;
            String groupSql = "SELECT id_grupo FROM DetalleMatricula WHERE id_detalle = ?";
            try (PreparedStatement ps = conn.prepareStatement(groupSql)) {
                ps.setLong(1, grade.getEnrollmentId());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        groupId = rs.getLong("id_grupo");
                    }
                }
            }

            if (groupId == null) {
                throw new SQLException("Could not find group for enrollment id: " + grade.getEnrollmentId());
            }

            // 2) Find an existing ReglaEvaluacion for the group (prefer an automatic one created previously)
            Long reglaId = null;
            String findAutoReglaSql = "SELECT id_regla FROM ReglaEvaluacion WHERE id_grupo = ? AND nombre_item LIKE 'Auto:%' AND ROWNUM = 1";
            try (PreparedStatement ps = conn.prepareStatement(findAutoReglaSql)) {
                ps.setLong(1, groupId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        reglaId = rs.getLong("id_regla");
                    }
                }
            }

            // If no auto rule found, try to find any existing rule for the group
            if (reglaId == null) {
                String findReglaSql = "SELECT id_regla FROM ReglaEvaluacion WHERE id_grupo = ? AND ROWNUM = 1";
                try (PreparedStatement ps = conn.prepareStatement(findReglaSql)) {
                    ps.setLong(1, groupId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            reglaId = rs.getLong("id_regla");
                        }
                    }
                }
            }

            // 3) If no rule exists at all, create a single default auto rule for this group (one per group)
            if (reglaId == null) {
                // Prepare name for auto rule
                String autoName = grade.getComments() != null && !grade.getComments().isBlank() ? "Auto: " + grade.getComments() : "Auto: Evaluación rápida";

                // Use MERGE so concurrent callers won't create duplicates (Oracle MERGE is atomic)
                String mergeSql = "MERGE INTO ReglaEvaluacion r " +
                                  "USING (SELECT ? AS id_grupo, ? AS nombre_item FROM dual) src " +
                                  "ON (r.id_grupo = src.id_grupo AND r.nombre_item = src.nombre_item) " +
                                  "WHEN NOT MATCHED THEN " +
                                  "INSERT (id_regla, id_grupo, nombre_item, porcentaje) " +
                                  "VALUES ((SELECT NVL(MAX(id_regla), 0) + 1 FROM ReglaEvaluacion), src.id_grupo, src.nombre_item, ?)";

                try (PreparedStatement ps = conn.prepareStatement(mergeSql)) {
                    ps.setLong(1, groupId);
                    ps.setString(2, autoName);
                    ps.setDouble(3, 100.0);
                    ps.executeUpdate();
                }

                // Retrieve the regla id (either existing or just inserted)
                String selectReglaSql = "SELECT id_regla FROM ReglaEvaluacion WHERE id_grupo = ? AND nombre_item = ? AND ROWNUM = 1";
                try (PreparedStatement ps = conn.prepareStatement(selectReglaSql)) {
                    ps.setLong(1, groupId);
                    ps.setString(2, autoName);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            reglaId = rs.getLong("id_regla");
                        }
                    }
                }
            }

            // 4) Generate next id for Calificacion
            long nextId = 1;
            String maxSql = "SELECT NVL(MAX(id_calificacion), 0) + 1 AS next_id FROM Calificacion";
            try (PreparedStatement ps = conn.prepareStatement(maxSql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    nextId = rs.getLong("next_id");
                }
            }

            // 5) Insert new Calificacion record
            String insertSql = "INSERT INTO Calificacion (id_calificacion, id_detalle, id_regla, nota, fecha_registro, id_docente_registra) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                ps.setLong(1, nextId);
                ps.setLong(2, grade.getEnrollmentId());
                ps.setLong(3, reglaId);
                ps.setDouble(4, grade.getGradeValue() != null ? grade.getGradeValue() : 0.0);
                if (grade.getGradedBy() != null) {
                    ps.setLong(5, grade.getGradedBy());
                } else {
                    ps.setNull(5, java.sql.Types.BIGINT);
                }

                int rows = ps.executeUpdate();
                if (rows == 0) {
                    throw new SQLException("Creating Calificacion failed, no rows affected.");
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

    /**
     * Returns final calculated grades stored in NotaDefinitiva for a specific student.
     * This is used when final grades are stored in the NotaDefinitiva table instead of Calificacion.
     */
    public List<Grade> findFinalGradesByStudent(String studentId) throws SQLException {
        String sql = "SELECT nd.id_nota_definitiva AS id_nota_definitiva, nd.id_detalle AS id_detalle, nd.nota_definitiva AS nota_definitiva " +
                     "FROM NotaDefinitiva nd " +
                     "JOIN DetalleMatricula dm ON nd.id_detalle = dm.id_detalle " +
                     "JOIN Matricula m ON dm.id_matricula = m.id_matricula " +
                     "WHERE m.cod_estudiante = ? ORDER BY nd.fecha_calculo DESC";

        List<Grade> list = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Grade g = new Grade();
                    g.setGradeId(rs.getLong("id_nota_definitiva"));
                    long enrollmentId = rs.getLong("id_detalle");
                    if (!rs.wasNull()) g.setEnrollmentId(enrollmentId);
                    double nota = rs.getDouble("nota_definitiva");
                    if (!rs.wasNull()) g.setGradeValue(nota);
                    list.add(g);
                }
            }
        }

        return list;
    }

    /**
     * Find a final grade (NotaDefinitiva) for a specific enrollment (id_detalle).
     */
    public Optional<Grade> findFinalGradeByEnrollment(Long enrollmentId) throws SQLException {
        String sql = "SELECT id_nota_definitiva, id_detalle, nota_definitiva FROM NotaDefinitiva WHERE id_detalle = ? ORDER BY fecha_calculo DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, enrollmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Grade g = new Grade();
                    g.setGradeId(rs.getLong("id_nota_definitiva"));
                    long det = rs.getLong("id_detalle");
                    if (!rs.wasNull()) g.setEnrollmentId(det);
                    double nota = rs.getDouble("nota_definitiva");
                    if (!rs.wasNull()) g.setGradeValue(nota);
                    return Optional.of(g);
                }
            }
        }

        return Optional.empty();
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
