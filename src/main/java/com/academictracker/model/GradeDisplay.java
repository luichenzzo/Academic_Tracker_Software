package com.academictracker.model;

public class GradeDisplay {
    private final String studentId;
    private final String studentName;
    private final String grade;
    private final String status;
    private final Long enrollmentId; // id_detalle from DetalleMatricula

    public GradeDisplay(String studentId, String studentName, String grade, String status, Long enrollmentId) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.grade = grade;
        this.status = status;
        this.enrollmentId = enrollmentId;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getGrade() {
        return grade;
    }

    public String getStatus() {
        return status;
    }

    public Long getEnrollmentId() {
        return enrollmentId;
    }
}
