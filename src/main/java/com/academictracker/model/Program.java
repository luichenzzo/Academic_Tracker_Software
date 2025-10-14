package com.academictracker.model;

/**
 * ProgramaAcademico entity - Represents academic programs
 */
public class Program {
    private Long codPrograma;
    private String codigoPrograma;
    private String nombre;
    private Integer creditosTotales;
    private Integer duracionSemestres;
    private Long idTipoPrograma;
    private Long idFacultad;

    // For joins
    private TipoPrograma tipoPrograma;
    private Facultad facultad;

    // Constructors
    public Program() {
    }

    public Program(Long codPrograma, String codigoPrograma, String nombre) {
        this.codPrograma = codPrograma;
        this.codigoPrograma = codigoPrograma;
        this.nombre = nombre;
    }

    public Program(Long codPrograma, String codigoPrograma, String nombre, Integer creditosTotales,
                   Integer duracionSemestres, Long idTipoPrograma, Long idFacultad) {
        this.codPrograma = codPrograma;
        this.codigoPrograma = codigoPrograma;
        this.nombre = nombre;
        this.creditosTotales = creditosTotales;
        this.duracionSemestres = duracionSemestres;
        this.idTipoPrograma = idTipoPrograma;
        this.idFacultad = idFacultad;
    }

    // Getters and Setters
    public Long getCodPrograma() {
        return codPrograma;
    }

    public void setCodPrograma(Long codPrograma) {
        this.codPrograma = codPrograma;
    }

    public String getCodigoPrograma() {
        return codigoPrograma;
    }

    public void setCodigoPrograma(String codigoPrograma) {
        this.codigoPrograma = codigoPrograma;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Integer getCreditosTotales() {
        return creditosTotales;
    }

    public void setCreditosTotales(Integer creditosTotales) {
        this.creditosTotales = creditosTotales;
    }

    public Integer getDuracionSemestres() {
        return duracionSemestres;
    }

    public void setDuracionSemestres(Integer duracionSemestres) {
        this.duracionSemestres = duracionSemestres;
    }

    public Long getIdTipoPrograma() {
        return idTipoPrograma;
    }

    public void setIdTipoPrograma(Long idTipoPrograma) {
        this.idTipoPrograma = idTipoPrograma;
    }

    public Long getIdFacultad() {
        return idFacultad;
    }

    public void setIdFacultad(Long idFacultad) {
        this.idFacultad = idFacultad;
    }

    public TipoPrograma getTipoPrograma() {
        return tipoPrograma;
    }

    public void setTipoPrograma(TipoPrograma tipoPrograma) {
        this.tipoPrograma = tipoPrograma;
        if (tipoPrograma != null) {
            this.idTipoPrograma = tipoPrograma.getIdTipoPrograma();
        }
    }

    public Facultad getFacultad() {
        return facultad;
    }

    public void setFacultad(Facultad facultad) {
        this.facultad = facultad;
        if (facultad != null) {
            this.idFacultad = facultad.getIdFacultad();
        }
    }

    // Legacy compatibility methods for existing code
    public Long getProgramId() {
        return codPrograma;
    }

    public void setProgramId(Long programId) {
        this.codPrograma = programId;
    }

    public String getProgramCode() {
        return codigoPrograma;
    }

    public void setProgramCode(String programCode) {
        this.codigoPrograma = programCode;
    }

    public String getProgramName() {
        return nombre;
    }

    public void setProgramName(String programName) {
        this.nombre = programName;
    }

    public String getDescription() {
        return tipoPrograma != null ? tipoPrograma.getDescripcion() : "";
    }

    public void setDescription(String description) {
        // For compatibility - description is now handled by TipoPrograma
    }

    public Integer getDurationYears() {
        return duracionSemestres != null ? duracionSemestres / 2 : 0;
    }

    public void setDurationYears(Integer durationYears) {
        this.duracionSemestres = durationYears != null ? durationYears * 2 : 0;
    }

    public Integer getCreditsRequired() {
        return creditosTotales;
    }

    public void setCreditsRequired(Integer creditsRequired) {
        this.creditosTotales = creditsRequired;
    }

    @Override
    public String toString() {
        return codigoPrograma + " - " + nombre;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Program program = (Program) obj;
        return codPrograma != null && codPrograma.equals(program.codPrograma);
    }

    @Override
    public int hashCode() {
        return codPrograma != null ? codPrograma.hashCode() : 0;
    }
}
