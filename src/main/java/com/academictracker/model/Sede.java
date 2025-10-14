package com.academictracker.model;

/**
 * Sede entity - Represents university campuses/locations
 */
public class Sede {
    private Long idSede;
    private String nombre;
    private String municipio;
    private String direccion;
    private String telefono;

    // Constructors
    public Sede() {
    }

    public Sede(String nombre, String municipio) {
        this.nombre = nombre;
        this.municipio = municipio;
    }

    public Sede(Long idSede, String nombre, String municipio, String direccion, String telefono) {
        this.idSede = idSede;
        this.nombre = nombre;
        this.municipio = municipio;
        this.direccion = direccion;
        this.telefono = telefono;
    }

    // Getters and Setters
    public Long getIdSede() {
        return idSede;
    }

    public void setIdSede(Long idSede) {
        this.idSede = idSede;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getMunicipio() {
        return municipio;
    }

    public void setMunicipio(String municipio) {
        this.municipio = municipio;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    @Override
    public String toString() {
        return nombre + " - " + municipio;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Sede sede = (Sede) obj;
        return idSede != null && idSede.equals(sede.idSede);
    }

    @Override
    public int hashCode() {
        return idSede != null ? idSede.hashCode() : 0;
    }
}
