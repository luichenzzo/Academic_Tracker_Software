package com.academictracker.model;

/**
 * Grupo entity - Represents course groups/sections
 */
public class Grupo {
    private Long idGrupo;
    private Integer numeroGrupo;
    private Integer cupoMaximo;
    private Integer cupoOcupado;
    private String codAsignatura;
    private String codPeriodo;
    private Long idSede;
    private boolean activo;

    // For joins
    private Asignatura asignatura;
    private PeriodoAcademico periodoAcademico;
    private Sede sede;

    // Constructors
    public Grupo() {
    }

    public Grupo(Integer numeroGrupo, Integer cupoMaximo, String codAsignatura, String codPeriodo, Long idSede) {
        this.numeroGrupo = numeroGrupo;
        this.cupoMaximo = cupoMaximo;
        this.codAsignatura = codAsignatura;
        this.codPeriodo = codPeriodo;
        this.idSede = idSede;
        this.cupoOcupado = 0;
        this.activo = true;
    }

    // Getters and Setters
    public Long getIdGrupo() {
        return idGrupo;
    }

    public void setIdGrupo(Long idGrupo) {
        this.idGrupo = idGrupo;
    }

    public Integer getNumeroGrupo() {
        return numeroGrupo;
    }

    public void setNumeroGrupo(Integer numeroGrupo) {
        this.numeroGrupo = numeroGrupo;
    }

    public Integer getCupoMaximo() {
        return cupoMaximo;
    }

    public void setCupoMaximo(Integer cupoMaximo) {
        this.cupoMaximo = cupoMaximo;
    }

    public Integer getCupoOcupado() {
        return cupoOcupado;
    }

    public void setCupoOcupado(Integer cupoOcupado) {
        this.cupoOcupado = cupoOcupado;
    }

    public String getCodAsignatura() {
        return codAsignatura;
    }

    public void setCodAsignatura(String codAsignatura) {
        this.codAsignatura = codAsignatura;
    }

    public String getCodPeriodo() {
        return codPeriodo;
    }

    public void setCodPeriodo(String codPeriodo) {
        this.codPeriodo = codPeriodo;
    }

    public Long getIdSede() {
        return idSede;
    }

    public void setIdSede(Long idSede) {
        this.idSede = idSede;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public Asignatura getAsignatura() {
        return asignatura;
    }

    public void setAsignatura(Asignatura asignatura) {
        this.asignatura = asignatura;
        if (asignatura != null) {
            this.codAsignatura = asignatura.getCodAsignatura();
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

    public Sede getSede() {
        return sede;
    }

    public void setSede(Sede sede) {
        this.sede = sede;
        if (sede != null) {
            this.idSede = sede.getIdSede();
        }
    }

    public int getCuposDisponibles() {
        return cupoMaximo - cupoOcupado;
    }

    @Override
    public String toString() {
        return "Grupo " + numeroGrupo + " - " + (asignatura != null ? asignatura.getNombre() : codAsignatura);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Grupo grupo = (Grupo) obj;
        return idGrupo != null && idGrupo.equals(grupo.idGrupo);
    }

    @Override
    public int hashCode() {
        return idGrupo != null ? idGrupo.hashCode() : 0;
    }
}
