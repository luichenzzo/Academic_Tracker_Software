package com.academictracker.model;

/**
 * Asignatura entity - Represents university subjects/courses
 */
public class Asignatura {
    private String codAsignatura;
    private String nombre;
    private Integer creditos;
    private Integer horasSemanales;
    private Integer semestreSugerido;
    private boolean esTrabajoGrado;
    private Long idTipo;
    private Long codPrograma;

    // For joins
    private TipoAsignatura tipoAsignatura;
    private Program programa;

    // Constructors
    public Asignatura() {
    }

    public Asignatura(String codAsignatura, String nombre, Integer creditos) {
        this.codAsignatura = codAsignatura;
        this.nombre = nombre;
        this.creditos = creditos;
    }

    public Asignatura(String codAsignatura, String nombre, Integer creditos, Integer horasSemanales,
                      Integer semestreSugerido, boolean esTrabajoGrado, Long idTipo, Long codPrograma) {
        this.codAsignatura = codAsignatura;
        this.nombre = nombre;
        this.creditos = creditos;
        this.horasSemanales = horasSemanales;
        this.semestreSugerido = semestreSugerido;
        this.esTrabajoGrado = esTrabajoGrado;
        this.idTipo = idTipo;
        this.codPrograma = codPrograma;
    }

    // Getters and Setters
    public String getCodAsignatura() {
        return codAsignatura;
    }

    public void setCodAsignatura(String codAsignatura) {
        this.codAsignatura = codAsignatura;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Integer getCreditos() {
        return creditos;
    }

    public void setCreditos(Integer creditos) {
        this.creditos = creditos;
    }

    public Integer getHorasSemanales() {
        return horasSemanales;
    }

    public void setHorasSemanales(Integer horasSemanales) {
        this.horasSemanales = horasSemanales;
    }

    public Integer getSemestreSugerido() {
        return semestreSugerido;
    }

    public void setSemestreSugerido(Integer semestreSugerido) {
        this.semestreSugerido = semestreSugerido;
    }

    public boolean isEsTrabajoGrado() {
        return esTrabajoGrado;
    }

    public void setEsTrabajoGrado(boolean esTrabajoGrado) {
        this.esTrabajoGrado = esTrabajoGrado;
    }

    public Long getIdTipo() {
        return idTipo;
    }

    public void setIdTipo(Long idTipo) {
        this.idTipo = idTipo;
    }

    public Long getCodPrograma() {
        return codPrograma;
    }

    public void setCodPrograma(Long codPrograma) {
        this.codPrograma = codPrograma;
    }

    public TipoAsignatura getTipoAsignatura() {
        return tipoAsignatura;
    }

    public void setTipoAsignatura(TipoAsignatura tipoAsignatura) {
        this.tipoAsignatura = tipoAsignatura;
        if (tipoAsignatura != null) {
            this.idTipo = tipoAsignatura.getIdTipo();
        }
    }

    public Program getPrograma() {
        return programa;
    }

    public void setPrograma(Program programa) {
        this.programa = programa;
        if (programa != null) {
            this.codPrograma = programa.getCodPrograma();
        }
    }

    @Override
    public String toString() {
        return codAsignatura + " - " + nombre;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Asignatura that = (Asignatura) obj;
        return codAsignatura != null && codAsignatura.equals(that.codAsignatura);
    }

    @Override
    public int hashCode() {
        return codAsignatura != null ? codAsignatura.hashCode() : 0;
    }
}
