package com.academictracker.model;

import java.time.LocalDate;

/**
 * PeriodoAcademico entity - Represents academic periods/semesters
 */
public class PeriodoAcademico {
    private String codPeriodo;
    private String nombre;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private LocalDate fechaInicioMatriculas;
    private LocalDate fechaFinMatriculas;
    private LocalDate fechaCierreNotas;
    private boolean activo;

    // Constructors
    public PeriodoAcademico() {
    }

    public PeriodoAcademico(String codPeriodo, String nombre) {
        this.codPeriodo = codPeriodo;
        this.nombre = nombre;
    }

    public PeriodoAcademico(String codPeriodo, String nombre, LocalDate fechaInicio,
                           LocalDate fechaFin, LocalDate fechaInicioMatriculas,
                           LocalDate fechaFinMatriculas, LocalDate fechaCierreNotas) {
        this.codPeriodo = codPeriodo;
        this.nombre = nombre;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.fechaInicioMatriculas = fechaInicioMatriculas;
        this.fechaFinMatriculas = fechaFinMatriculas;
        this.fechaCierreNotas = fechaCierreNotas;
        this.activo = false;
    }

    // Getters and Setters
    public String getCodPeriodo() {
        return codPeriodo;
    }

    public void setCodPeriodo(String codPeriodo) {
        this.codPeriodo = codPeriodo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public LocalDate getFechaInicioMatriculas() {
        return fechaInicioMatriculas;
    }

    public void setFechaInicioMatriculas(LocalDate fechaInicioMatriculas) {
        this.fechaInicioMatriculas = fechaInicioMatriculas;
    }

    public LocalDate getFechaFinMatriculas() {
        return fechaFinMatriculas;
    }

    public void setFechaFinMatriculas(LocalDate fechaFinMatriculas) {
        this.fechaFinMatriculas = fechaFinMatriculas;
    }

    public LocalDate getFechaCierreNotas() {
        return fechaCierreNotas;
    }

    public void setFechaCierreNotas(LocalDate fechaCierreNotas) {
        this.fechaCierreNotas = fechaCierreNotas;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    @Override
    public String toString() {
        return codPeriodo + " - " + nombre;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PeriodoAcademico that = (PeriodoAcademico) obj;
        return codPeriodo != null && codPeriodo.equals(that.codPeriodo);
    }

    @Override
    public int hashCode() {
        return codPeriodo != null ? codPeriodo.hashCode() : 0;
    }
}
