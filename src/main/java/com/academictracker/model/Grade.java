package com.academictracker.model;

import java.time.LocalDateTime;

/**
 * Grade entity
 */
public class Grade {
    private Long gradeId;
    private Long enrollmentId;
    private Double gradeValue;
    private String gradeLetter;
    private String comments;
    private Long gradedBy;
    private LocalDateTime gradedAt;

    // Constructors
    public Grade() {
    }

    public Grade(Long gradeId, Long enrollmentId, Double gradeValue, Long gradedBy) {
        this.gradeId = gradeId;
        this.enrollmentId = enrollmentId;
        this.gradeValue = gradeValue;
        this.gradedBy = gradedBy;
        this.gradeLetter = calculateLetterGrade(gradeValue);
    }

    // Getters and Setters
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
        this.gradeLetter = calculateLetterGrade(gradeValue);
    }

    public String getGradeLetter() {
        return gradeLetter;
    }

    public void setGradeLetter(String gradeLetter) {
        this.gradeLetter = gradeLetter;
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

    public LocalDateTime getGradedAt() {
        return gradedAt;
    }

    public void setGradedAt(LocalDateTime gradedAt) {
        this.gradedAt = gradedAt;
    }

    /**
     * Calculate letter grade from numeric value
     */
    private String calculateLetterGrade(Double value) {
        if (value == null) return null;
        if (value >= 90) return "A";
        if (value >= 80) return "B";
        if (value >= 70) return "C";
        if (value >= 60) return "D";
        return "F";
    }

    @Override
    public String toString() {
        return "Grade{" +
                "gradeId=" + gradeId +
                ", enrollmentId=" + enrollmentId +
                ", gradeValue=" + gradeValue +
                ", gradeLetter='" + gradeLetter + '\'' +
                ", gradedAt=" + gradedAt +
                '}';
    }
}
