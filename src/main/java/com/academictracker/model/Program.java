package com.academictracker.model;

import java.time.LocalDateTime;

/**
 * Program entity (e.g., Computer Science, Engineering)
 */
public class Program {
    private Long programId;
    private String programCode;
    private String programName;
    private String description;
    private int durationYears;
    private int creditsRequired;
    private LocalDateTime createdAt;

    // Constructors
    public Program() {
    }

    public Program(Long programId, String programCode, String programName, int durationYears, int creditsRequired) {
        this.programId = programId;
        this.programCode = programCode;
        this.programName = programName;
        this.durationYears = durationYears;
        this.creditsRequired = creditsRequired;
    }

    // Getters and Setters
    public Long getProgramId() {
        return programId;
    }

    public void setProgramId(Long programId) {
        this.programId = programId;
    }

    public String getProgramCode() {
        return programCode;
    }

    public void setProgramCode(String programCode) {
        this.programCode = programCode;
    }

    public String getProgramName() {
        return programName;
    }

    public void setProgramName(String programName) {
        this.programName = programName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getDurationYears() {
        return durationYears;
    }

    public void setDurationYears(int durationYears) {
        this.durationYears = durationYears;
    }

    public int getCreditsRequired() {
        return creditsRequired;
    }

    public void setCreditsRequired(int creditsRequired) {
        this.creditsRequired = creditsRequired;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Program{" +
                "programId=" + programId +
                ", programCode='" + programCode + '\'' +
                ", programName='" + programName + '\'' +
                ", durationYears=" + durationYears +
                ", creditsRequired=" + creditsRequired +
                '}';
    }
}
