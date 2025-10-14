package com.academictracker.model;

import java.time.LocalDate;

/**
 * Enrollment entity - Compatibility layer for DetalleMatricula
 * This maintains compatibility with existing code while using the new database structure
 */
public class Enrollment {
    private Long enrollmentId; // Maps to idDetalle
    private String studentId; // Maps to codEstudiante from matricula
    private Long courseId; // Maps to codAsignatura from grupo
    private LocalDate enrollmentDate;
    private EnrollmentStatus status;

    // New fields for the updated structure
    private Long idDetalle;
    private Long idMatricula;
    private Long idGrupo;
    private DetalleMatricula detalleMatricula;

    public enum EnrollmentStatus {
        ACTIVE("inscrito"),
        COMPLETED("completado"),
        DROPPED("retirado"),
        FAILED("cancelado");

        private final String dbValue;

        EnrollmentStatus(String dbValue) {
            this.dbValue = dbValue;
        }

        public String getDbValue() {
            return dbValue;
        }

        public static EnrollmentStatus fromDbValue(String dbValue) {
            for (EnrollmentStatus status : values()) {
                if (status.dbValue.equals(dbValue)) {
                    return status;
                }
            }
            return ACTIVE; // Default
        }
    }

    // Constructors
    public Enrollment() {
    }

    public Enrollment(Long enrollmentId, String studentId, Long courseId, LocalDate enrollmentDate, EnrollmentStatus status) {
        this.enrollmentId = enrollmentId;
        this.studentId = studentId;
        this.courseId = courseId;
        this.enrollmentDate = enrollmentDate;
        this.status = status;
    }

    // New constructor for updated structure
    public Enrollment(DetalleMatricula detalleMatricula) {
        this.detalleMatricula = detalleMatricula;
        this.idDetalle = detalleMatricula.getIdDetalle();
        this.idMatricula = detalleMatricula.getIdMatricula();
        this.idGrupo = detalleMatricula.getIdGrupo();
        this.enrollmentId = detalleMatricula.getIdDetalle();
        this.enrollmentDate = detalleMatricula.getFechaInscripcion() != null ?
                             detalleMatricula.getFechaInscripcion().toLocalDate() : LocalDate.now();
        this.status = EnrollmentStatus.fromDbValue(detalleMatricula.getEstado());

        // Extract student and course info from joins
        if (detalleMatricula.getMatricula() != null) {
            this.studentId = detalleMatricula.getMatricula().getCodEstudiante();
        }
        if (detalleMatricula.getGrupo() != null && detalleMatricula.getGrupo().getAsignatura() != null) {
            try {
                this.courseId = Long.parseLong(detalleMatricula.getGrupo().getCodAsignatura());
            } catch (NumberFormatException e) {
                this.courseId = null; // Handle non-numeric course codes
            }
        }
    }

    // Getters and Setters
    public Long getEnrollmentId() {
        return enrollmentId;
    }

    public void setEnrollmentId(Long enrollmentId) {
        this.enrollmentId = enrollmentId;
        this.idDetalle = enrollmentId;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public LocalDate getEnrollmentDate() {
        return enrollmentDate;
    }

    public void setEnrollmentDate(LocalDate enrollmentDate) {
        this.enrollmentDate = enrollmentDate;
    }

    public EnrollmentStatus getStatus() {
        return status;
    }

    public void setStatus(EnrollmentStatus status) {
        this.status = status;
    }

    // New getters/setters for updated structure
    public Long getIdDetalle() {
        return idDetalle;
    }

    public void setIdDetalle(Long idDetalle) {
        this.idDetalle = idDetalle;
        this.enrollmentId = idDetalle;
    }

    public Long getIdMatricula() {
        return idMatricula;
    }

    public void setIdMatricula(Long idMatricula) {
        this.idMatricula = idMatricula;
    }

    public Long getIdGrupo() {
        return idGrupo;
    }

    public void setIdGrupo(Long idGrupo) {
        this.idGrupo = idGrupo;
    }

    public DetalleMatricula getDetalleMatricula() {
        return detalleMatricula;
    }

    public void setDetalleMatricula(DetalleMatricula detalleMatricula) {
        this.detalleMatricula = detalleMatricula;
    }

    @Override
    public String toString() {
        return "Enrollment " + enrollmentId + " - Student: " + studentId + " Course: " + courseId;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Enrollment that = (Enrollment) obj;
        return enrollmentId != null && enrollmentId.equals(that.enrollmentId);
    }

    @Override
    public int hashCode() {
        return enrollmentId != null ? enrollmentId.hashCode() : 0;
    }
}
