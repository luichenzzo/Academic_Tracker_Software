package com.academictracker.model;

import java.time.LocalDateTime;

/**
 * Matricula entity - Represents student enrollment in a period
 */
public class Matricula {
    private Long idMatricula;
    private String codEstudiante;
    private String codPeriodo;
    private LocalDateTime fechaMatricula;
    private Integer totalCreditos;
    private String estado;

    // For joins
    private Student estudiante;
    private PeriodoAcademico periodoAcademico;

    // Constructors
    public Matricula() {
    }

    public Matricula(String codEstudiante, String codPeriodo) {
        this.codEstudiante = codEstudiante;
        this.codPeriodo = codPeriodo;
        this.fechaMatricula = LocalDateTime.now();
        this.totalCreditos = 0;
        this.estado = "activa";
    }

    // Getters and Setters
    public Long getIdMatricula() {
        return idMatricula;
    }

    public void setIdMatricula(Long idMatricula) {
        this.idMatricula = idMatricula;
    }

    public String getCodEstudiante() {
        return codEstudiante;
    }

    public void setCodEstudiante(String codEstudiante) {
        this.codEstudiante = codEstudiante;
    }

    public String getCodPeriodo() {
        return codPeriodo;
    }

    public void setCodPeriodo(String codPeriodo) {
        this.codPeriodo = codPeriodo;
    }

    public LocalDateTime getFechaMatricula() {
        return fechaMatricula;
    }

    public void setFechaMatricula(LocalDateTime fechaMatricula) {
        this.fechaMatricula = fechaMatricula;
    }

    public Integer getTotalCreditos() {
        return totalCreditos;
    }

    public void setTotalCreditos(Integer totalCreditos) {
        this.totalCreditos = totalCreditos;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Student getEstudiante() {
        return estudiante;
    }

    public void setEstudiante(Student estudiante) {
        this.estudiante = estudiante;
        if (estudiante != null) {
            this.codEstudiante = estudiante.getCodEstudiante();
        }
    }

    public PeriodoAcademico getPeriodoAcademico() {
        return periodoAcademico;
    }

    public void setPeriodoAcademico(PeriodoAcademico periodoAcademico) {
        this.periodoAcademico = periodoAcademico;
        if (periodoAcademico != null) {
            this.codPeriodo = periodoAcademico.getCodPeriodo();
        }
    }

    @Override
    public String toString() {
        return "Matrícula " + idMatricula + " - " + codEstudiante + " (" + codPeriodo + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Matricula matricula = (Matricula) obj;
        return idMatricula != null && idMatricula.equals(matricula.idMatricula);
    }

    @Override
    public int hashCode() {
        return idMatricula != null ? idMatricula.hashCode() : 0;
    }
}
