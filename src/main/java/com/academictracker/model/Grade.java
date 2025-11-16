package com.academictracker.model;

public class Grade {
    private Long gradeId;
    private Long enrollmentId;
    private Double gradeValue;
    private String comments;
    private Long gradedBy;

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
}
