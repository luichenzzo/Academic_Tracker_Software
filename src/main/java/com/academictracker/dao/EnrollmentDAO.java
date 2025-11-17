package com.academictracker.dao;

import com.academictracker.model.Enrollment;
import com.academictracker.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO para operaciones sobre DetalleMatricula (inscripciones)
 */
public class EnrollmentDAO {

    public EnrollmentDAO() {
    }

    // ---------------- Existing deleteEnrollment kept for internal use ----------------
    public boolean deleteEnrollment(Long detalleId) throws SQLException {
        if (detalleId == null) return false;

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // find group for the detalle
                Long groupId = null;
                String groupSql = "SELECT id_grupo FROM DetalleMatricula WHERE id_detalle = ?";
                try (PreparedStatement ps = conn.prepareStatement(groupSql)) {
                    ps.setLong(1, detalleId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) groupId = rs.getLong("id_grupo");
                    }
                }

                // delete detalle (this may be blocked by trigger trg_prevent_withdrawal_if_started)
                String delSql = "DELETE FROM DetalleMatricula WHERE id_detalle = ?";
                try (PreparedStatement ps = conn.prepareStatement(delSql)) {
                    ps.setLong(1, detalleId);
                    int rows = ps.executeUpdate();
                    if (rows == 0) {
                        conn.rollback();
                        return false;
                    }
                }

                // decrement group's cupo_ocupado
                if (groupId != null) {
                    String updSql = "UPDATE Grupo SET cupo_ocupado = NVL(cupo_ocupado,0) - 1 WHERE id_grupo = ? AND cupo_ocupado > 0";
                    try (PreparedStatement ps = conn.prepareStatement(updSql)) {
                        ps.setLong(1, groupId);
                        ps.executeUpdate();
                    }
                }

                conn.commit();
                return true;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    // ---------------- Adapter methods expected by AcademicService ----------------

    public List<Enrollment> findByStudent(String codEstudiante) throws SQLException {
        List<Enrollment> list = new ArrayList<>();
        String sql = "SELECT dm.id_detalle, dm.id_matricula, dm.id_grupo, dm.fecha_inscripcion, dm.estado " +
                     "FROM DetalleMatricula dm " +
                     "JOIN Matricula m ON dm.id_matricula = m.id_matricula " +
                     "WHERE m.cod_estudiante = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, codEstudiante);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Enrollment e = new Enrollment();
                    long idDetalle = rs.getLong("id_detalle");
                    e.setEnrollmentId(idDetalle);
                    e.setIdDetalle(idDetalle);
                    long idMat = rs.getLong("id_matricula"); if (!rs.wasNull()) e.setIdMatricula(idMat);
                    long idGrupo = rs.getLong("id_grupo"); if (!rs.wasNull()) e.setIdGrupo(idGrupo);
                    Timestamp ts = rs.getTimestamp("fecha_inscripcion");
                    if (ts != null) e.setEnrollmentDate(ts.toLocalDateTime().toLocalDate());
                    String estado = rs.getString("estado");
                    e.setStatus(Enrollment.EnrollmentStatus.fromDbValue(estado));
                    e.setStudentId(codEstudiante);
                    // For compatibility, set courseId to the group id
                    e.setCourseId(e.getIdGrupo());
                    list.add(e);
                }
            }
        }
        return list;
    }

    public List<Enrollment> findByCourse(Long groupId) throws SQLException {
        List<Enrollment> list = new ArrayList<>();
        String sql = "SELECT dm.id_detalle, dm.id_matricula, dm.id_grupo, dm.fecha_inscripcion, dm.estado " +
                     "FROM DetalleMatricula dm WHERE dm.id_grupo = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, groupId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Enrollment e = new Enrollment();
                    long idDetalle = rs.getLong("id_detalle");
                    e.setEnrollmentId(idDetalle);
                    e.setIdDetalle(idDetalle);
                    long idMat = rs.getLong("id_matricula"); if (!rs.wasNull()) e.setIdMatricula(idMat);
                    long idGrupo = rs.getLong("id_grupo"); if (!rs.wasNull()) e.setIdGrupo(idGrupo);
                    Timestamp ts = rs.getTimestamp("fecha_inscripcion");
                    if (ts != null) e.setEnrollmentDate(ts.toLocalDateTime().toLocalDate());
                    String estado = rs.getString("estado");
                    e.setStatus(Enrollment.EnrollmentStatus.fromDbValue(estado));
                    e.setCourseId(idGrupo);
                    list.add(e);
                }
            }
        }
        return list;
    }

    public Enrollment create(Enrollment enrollment) throws SQLException {
        if (enrollment == null) throw new SQLException("Enrollment is null");

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Ensure we have an id_matricula: find existing matricula for student in active period
                Long idMatricula = enrollment.getIdMatricula();
                if (idMatricula == null && enrollment.getStudentId() != null) {
                    String matSql = "SELECT id_matricula FROM Matricula WHERE cod_estudiante = ? AND ROWNUM = 1";
                    try (PreparedStatement ps = conn.prepareStatement(matSql)) {
                        ps.setString(1, enrollment.getStudentId());
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) idMatricula = rs.getLong("id_matricula");
                        }
                    }
                }

                if (idMatricula == null) {
                    // create a new Matricula minimal record for the student (use active period if available)
                    String periodoSql = "SELECT cod_periodo FROM PeriodoAcademico WHERE activo = 1 AND ROWNUM = 1";
                    String codPeriodo = null;
                    try (PreparedStatement ps = conn.prepareStatement(periodoSql);
                         ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) codPeriodo = rs.getString("cod_periodo");
                    }
                    String maxMatSql = "SELECT NVL(MAX(id_matricula),0)+1 AS next_id FROM Matricula";
                    long nextMat = 1;
                    try (PreparedStatement ps = conn.prepareStatement(maxMatSql);
                         ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) nextMat = rs.getLong("next_id");
                    }
                    String insMat = "INSERT INTO Matricula (id_matricula, cod_estudiante, cod_periodo, fecha_matricula, total_creditos, estado) VALUES (?, ?, ?, CURRENT_TIMESTAMP, 0, 'activa')";
                    try (PreparedStatement ps = conn.prepareStatement(insMat)) {
                        ps.setLong(1, nextMat);
                        ps.setString(2, enrollment.getStudentId());
                        if (codPeriodo != null) ps.setString(3, codPeriodo); else ps.setNull(3, java.sql.Types.VARCHAR);
                        ps.executeUpdate();
                        idMatricula = nextMat;
                    }
                }

                // insert detalle
                long nextDetalle = 1;
                String maxDetSql = "SELECT NVL(MAX(id_detalle),0)+1 AS next_id FROM DetalleMatricula";
                try (PreparedStatement ps = conn.prepareStatement(maxDetSql);
                     ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) nextDetalle = rs.getLong("next_id");
                }

                String insDet = "INSERT INTO DetalleMatricula (id_detalle, id_matricula, id_grupo, fecha_inscripcion, estado) VALUES (?, ?, ?, CURRENT_TIMESTAMP, ?)";
                try (PreparedStatement ps = conn.prepareStatement(insDet)) {
                    ps.setLong(1, nextDetalle);
                    ps.setLong(2, idMatricula);
                    if (enrollment.getCourseId() != null) ps.setLong(3, enrollment.getCourseId()); else ps.setNull(3, java.sql.Types.BIGINT);
                    ps.setString(4, enrollment.getStatus() != null ? enrollment.getStatus().getDbValue() : Enrollment.EnrollmentStatus.ACTIVE.getDbValue());
                    ps.executeUpdate();
                }

                // increment group cupo_ocupado
                if (enrollment.getCourseId() != null) {
                    String updGrupo = "UPDATE Grupo SET cupo_ocupado = NVL(cupo_ocupado,0) + 1 WHERE id_grupo = ?";
                    try (PreparedStatement ps = conn.prepareStatement(updGrupo)) {
                        ps.setLong(1, enrollment.getCourseId());
                        ps.executeUpdate();
                    }
                }

                conn.commit();
                enrollment.setEnrollmentId(nextDetalle);
                enrollment.setIdDetalle(nextDetalle);
                enrollment.setIdMatricula(idMatricula);
                return enrollment;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public boolean update(Enrollment enrollment) throws SQLException {
        if (enrollment == null || enrollment.getEnrollmentId() == null) return false;
        String sql = "UPDATE DetalleMatricula SET estado = ? WHERE id_detalle = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, enrollment.getStatus() != null ? enrollment.getStatus().getDbValue() : Enrollment.EnrollmentStatus.ACTIVE.getDbValue());
            ps.setLong(2, enrollment.getEnrollmentId());
            int rows = ps.executeUpdate();
            return rows > 0;
        }
    }

    public boolean delete(Long detalleId) throws SQLException {
        return deleteEnrollment(detalleId);
    }

    public Optional<Enrollment> findById(Long detalleId) throws SQLException {
        String sql = "SELECT dm.id_detalle, dm.id_matricula, dm.id_grupo, dm.fecha_inscripcion, dm.estado, m.cod_estudiante " +
                     "FROM DetalleMatricula dm JOIN Matricula m ON dm.id_matricula = m.id_matricula WHERE dm.id_detalle = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, detalleId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Enrollment e = new Enrollment();
                    long idDetalle = rs.getLong("id_detalle");
                    e.setEnrollmentId(idDetalle);
                    e.setIdDetalle(idDetalle);
                    long idMat = rs.getLong("id_matricula"); if (!rs.wasNull()) e.setIdMatricula(idMat);
                    long idGrupo = rs.getLong("id_grupo"); if (!rs.wasNull()) e.setIdGrupo(idGrupo);
                    Timestamp ts = rs.getTimestamp("fecha_inscripcion"); if (ts != null) e.setEnrollmentDate(ts.toLocalDateTime().toLocalDate());
                    e.setStatus(Enrollment.EnrollmentStatus.fromDbValue(rs.getString("estado")));
                    e.setStudentId(rs.getString("cod_estudiante"));
                    e.setCourseId(e.getIdGrupo());
                    return Optional.of(e);
                }
            }
        }
        return Optional.empty();
    }
}
