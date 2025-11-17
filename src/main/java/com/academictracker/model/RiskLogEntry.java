package com.academictracker.model;

import java.sql.Timestamp;

public class RiskLogEntry {
    private Long idLog;
    private String codEstudiante;
    private String nombreEstudiante;
    private Integer nivelAnterior;
    private Integer nivelNuevo;
    private String accion;
    private String mensaje;
    private Timestamp fechaRun;

    public Long getIdLog() { return idLog; }
    public void setIdLog(Long idLog) { this.idLog = idLog; }

    public String getCodEstudiante() { return codEstudiante; }
    public void setCodEstudiante(String codEstudiante) { this.codEstudiante = codEstudiante; }

    public String getNombreEstudiante() { return nombreEstudiante; }
    public void setNombreEstudiante(String nombreEstudiante) { this.nombreEstudiante = nombreEstudiante; }

    public Integer getNivelAnterior() { return nivelAnterior; }
    public void setNivelAnterior(Integer nivelAnterior) { this.nivelAnterior = nivelAnterior; }

    public Integer getNivelNuevo() { return nivelNuevo; }
    public void setNivelNuevo(Integer nivelNuevo) { this.nivelNuevo = nivelNuevo; }

    public String getAccion() { return accion; }
    public void setAccion(String accion) { this.accion = accion; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public Timestamp getFechaRun() { return fechaRun; }
    public void setFechaRun(Timestamp fechaRun) { this.fechaRun = fechaRun; }
}

// RiskLogEntry model removed — logging is produced via DBMS_OUTPUT in the stored procedure per current design.
// This file kept as placeholder to avoid accidental re-creation; delete if you prefer.
