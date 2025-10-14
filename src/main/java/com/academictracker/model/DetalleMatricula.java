package com.academictracker.model;

import java.time.LocalDateTime;

/**
 * DetalleMatricula entity - Represents student enrollment in specific course groups
 */
public class DetalleMatricula {
    private Long idDetalle;
    private Long idMatricula;
    private Long idGrupo;
    private LocalDateTime fechaInscripcion;
    private String estado;
    private Integer numeroIntento;

    // For joins
    private Matricula matricula;
    private Grupo grupo;

    // Constructors
    public DetalleMatricula() {
    }

    public DetalleMatricula(Long idMatricula, Long idGrupo) {
        this.idMatricula = idMatricula;
        this.idGrupo = idGrupo;
        this.fechaInscripcion = LocalDateTime.now();
        this.estado = "inscrito";
        this.numeroIntento = 1;
    }

    // Getters and Setters
    public Long getIdDetalle() {
        return idDetalle;
    }

    public void setIdDetalle(Long idDetalle) {
        this.idDetalle = idDetalle;
    }

    public Long getIdMatricula() {
        return idMatricula;
    }

    public void setIdMatricula(Long idMatricula) {
        this.idMatricula = idMatricula;
    }

    public Long getIdGrupo() {
        return idGrupo;
    }

    public void setIdGrupo(Long idGrupo) {
        this.idGrupo = idGrupo;
    }

    public LocalDateTime getFechaInscripcion() {
        return fechaInscripcion;
    }

    public void setFechaInscripcion(LocalDateTime fechaInscripcion) {
        this.fechaInscripcion = fechaInscripcion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Integer getNumeroIntento() {
        return numeroIntento;
    }

    public void setNumeroIntento(Integer numeroIntento) {
        this.numeroIntento = numeroIntento;
    }

    public Matricula getMatricula() {
        return matricula;
    }

    public void setMatricula(Matricula matricula) {
        this.matricula = matricula;
        if (matricula != null) {
            this.idMatricula = matricula.getIdMatricula();
        }
    }

    public Grupo getGrupo() {
        return grupo;
    }

    public void setGrupo(Grupo grupo) {
        this.grupo = grupo;
        if (grupo != null) {
            this.idGrupo = grupo.getIdGrupo();
        }
    }

    @Override
    public String toString() {
        return "Detalle " + idDetalle + " - " + (grupo != null ? grupo.toString() : "Grupo " + idGrupo);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        DetalleMatricula that = (DetalleMatricula) obj;
        return idDetalle != null && idDetalle.equals(that.idDetalle);
    }

    @Override
    public int hashCode() {
        return idDetalle != null ? idDetalle.hashCode() : 0;
    }
}
