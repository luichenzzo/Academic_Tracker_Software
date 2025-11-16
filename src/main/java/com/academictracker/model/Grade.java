package com.academictracker.model;

import java.sql.Timestamp;

public class Grade {
    private Long gradeId;
    private Long enrollmentId;
    private Double gradeValue;
    private String comments;
    private Long gradedBy;
    private Long ruleId; // id_regla from Calificacion
    private Timestamp registeredAt; // fecha_registro from Calificacion

    public Grade() {
    }

    public Grade(Long gradeId, Long enrollmentId, Double gradeValue, String comments, Long gradedBy) {
        this.gradeId = gradeId;
        this.enrollmentId = enrollmentId;
        this.gradeValue = gradeValue;
        this.comments = comments;
        this.gradedBy = gradedBy;
    }

    public Long getGradeId() {
        return gradeId;
    }

    public void setGradeId(Long gradeId) {
        this.gradeId = gradeId;
    }

    public Long getEnrollmentId() {
        return enrollmentId;
    }

    public void setEnrollmentId(Long enrollmentId) {
        this.enrollmentId = enrollmentId;
    }

    public Double getGradeValue() {
        return gradeValue;
    }

    public void setGradeValue(Double gradeValue) {
        this.gradeValue = gradeValue;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public Long getGradedBy() {
        return gradedBy;
    }

    public void setGradedBy(Long gradedBy) {
        this.gradedBy = gradedBy;
    }

    public Long getRuleId() {
        return ruleId;
    }

    public void setRuleId(Long ruleId) {
        this.ruleId = ruleId;
    }

    public Timestamp getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(Timestamp registeredAt) {
        this.registeredAt = registeredAt;
    }
}
