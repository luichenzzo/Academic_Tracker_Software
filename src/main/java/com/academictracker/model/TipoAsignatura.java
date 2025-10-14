package com.academictracker.model;

/**
 * TipoAsignatura entity - Represents types of subjects/courses
 */
public class TipoAsignatura {
    private Long idTipo;
    private String tipo;
    private String descripcion;

    // Constructors
    public TipoAsignatura() {
    }

    public TipoAsignatura(String tipo, String descripcion) {
        this.tipo = tipo;
        this.descripcion = descripcion;
    }

    public TipoAsignatura(Long idTipo, String tipo, String descripcion) {
        this.idTipo = idTipo;
        this.tipo = tipo;
        this.descripcion = descripcion;
    }

    // Getters and Setters
    public Long getIdTipo() {
        return idTipo;
    }

    public void setIdTipo(Long idTipo) {
        this.idTipo = idTipo;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    @Override
    public String toString() {
        return tipo;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        TipoAsignatura that = (TipoAsignatura) obj;
        return idTipo != null && idTipo.equals(that.idTipo);
    }

    @Override
    public int hashCode() {
        return idTipo != null ? idTipo.hashCode() : 0;
    }
}
