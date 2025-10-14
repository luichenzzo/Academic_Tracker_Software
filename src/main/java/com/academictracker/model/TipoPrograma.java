package com.academictracker.model;

/**
 * TipoPrograma entity - Represents types of academic programs
 */
public class TipoPrograma {
    private Long idTipoPrograma;
    private String nombre;
    private String descripcion;
    private boolean requiereTrabajoGrado;

    // Constructors
    public TipoPrograma() {
    }

    public TipoPrograma(String nombre, String descripcion, boolean requiereTrabajoGrado) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.requiereTrabajoGrado = requiereTrabajoGrado;
    }

    public TipoPrograma(Long idTipoPrograma, String nombre, String descripcion, boolean requiereTrabajoGrado) {
        this.idTipoPrograma = idTipoPrograma;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.requiereTrabajoGrado = requiereTrabajoGrado;
    }

    // Getters and Setters
    public Long getIdTipoPrograma() {
        return idTipoPrograma;
    }

    public void setIdTipoPrograma(Long idTipoPrograma) {
        this.idTipoPrograma = idTipoPrograma;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public boolean isRequiereTrabajoGrado() {
        return requiereTrabajoGrado;
    }

    public void setRequiereTrabajoGrado(boolean requiereTrabajoGrado) {
        this.requiereTrabajoGrado = requiereTrabajoGrado;
    }

    @Override
    public String toString() {
        return nombre;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        TipoPrograma that = (TipoPrograma) obj;
        return idTipoPrograma != null && idTipoPrograma.equals(that.idTipoPrograma);
    }

    @Override
    public int hashCode() {
        return idTipoPrograma != null ? idTipoPrograma.hashCode() : 0;
    }
}
