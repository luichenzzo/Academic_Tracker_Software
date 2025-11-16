package com.academictracker.model;

public class GradeDisplay {
    private final String studentId;
    private final String studentName;
    private final String grade;
    private final String status;

    public GradeDisplay(String studentId, String studentName, String grade, String status) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.grade = grade;
        this.status = status;
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
}

