package com.academictracker.model;

/**
 * Facultad entity - Represents university faculties
 */
public class Facultad {
    private Long idFacultad;
    private String codigoFacultad;
    private String nombre;
    private String decano;
    private Long idSede;

    // For joins
    private Sede sede;

    // Constructors
    public Facultad() {
    }

    public Facultad(String codigoFacultad, String nombre, Long idSede) {
        this.codigoFacultad = codigoFacultad;
        this.nombre = nombre;
        this.idSede = idSede;
    }

    public Facultad(Long idFacultad, String codigoFacultad, String nombre, String decano, Long idSede) {
        this.idFacultad = idFacultad;
        this.codigoFacultad = codigoFacultad;
        this.nombre = nombre;
        this.decano = decano;
        this.idSede = idSede;
    }

    // Getters and Setters
    public Long getIdFacultad() {
        return idFacultad;
    }

    public void setIdFacultad(Long idFacultad) {
        this.idFacultad = idFacultad;
    }

    public String getCodigoFacultad() {
        return codigoFacultad;
    }

    public void setCodigoFacultad(String codigoFacultad) {
        this.codigoFacultad = codigoFacultad;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDecano() {
        return decano;
    }

    public void setDecano(String decano) {
        this.decano = decano;
    }

    public Long getIdSede() {
        return idSede;
    }

    public void setIdSede(Long idSede) {
        this.idSede = idSede;
    }

    public Sede getSede() {
        return sede;
    }

    public void setSede(Sede sede) {
        this.sede = sede;
        if (sede != null) {
            this.idSede = sede.getIdSede();
        }
    }

    @Override
    public String toString() {
        return codigoFacultad + " - " + nombre;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Facultad facultad = (Facultad) obj;
        return idFacultad != null && idFacultad.equals(facultad.idFacultad);
    }

    @Override
    public int hashCode() {
        return idFacultad != null ? idFacultad.hashCode() : 0;
    }
}
