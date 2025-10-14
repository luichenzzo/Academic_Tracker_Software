package com.academictracker.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Docente entity - Represents university teachers/professors
 */
public class Teacher {
    private Long idDocente;
    private String numeroDocumento;
    private String tipoDocumento;
    private String nombres;
    private String apellidos;
    private String correoInstitucional;
    private String telefono;
    private BigDecimal horasAsignadas;
    private boolean activo;

    // Constructors
    public Teacher() {
    }

    public Teacher(String nombres, String apellidos, String correoInstitucional) {
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.correoInstitucional = correoInstitucional;
        this.activo = true;
        this.horasAsignadas = BigDecimal.ZERO;
    }

    public Teacher(Long idDocente, String numeroDocumento, String tipoDocumento,
                   String nombres, String apellidos, String correoInstitucional) {
        this.idDocente = idDocente;
        this.numeroDocumento = numeroDocumento;
        this.tipoDocumento = tipoDocumento;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.correoInstitucional = correoInstitucional;
        this.activo = true;
        this.horasAsignadas = BigDecimal.ZERO;
    }

    // Getters and Setters
    public Long getIdDocente() {
        return idDocente;
    }

    public void setIdDocente(Long idDocente) {
        this.idDocente = idDocente;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(String tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getCorreoInstitucional() {
        return correoInstitucional;
    }

    public void setCorreoInstitucional(String correoInstitucional) {
        this.correoInstitucional = correoInstitucional;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public BigDecimal getHorasAsignadas() {
        return horasAsignadas;
    }

    public void setHorasAsignadas(BigDecimal horasAsignadas) {
        this.horasAsignadas = horasAsignadas;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    // Convenience methods
    public String getNombreCompleto() {
        return nombres + " " + apellidos;
    }

    // Legacy compatibility methods for existing code
    public String getFirstName() {
        return nombres;
    }

    public void setFirstName(String firstName) {
        this.nombres = firstName;
    }

    public String getLastName() {
        return apellidos;
    }

    public void setLastName(String lastName) {
        this.apellidos = lastName;
    }

    public Long getTeacherId() {
        return idDocente;
    }

    public void setTeacherId(Long teacherId) {
        this.idDocente = teacherId;
    }

    public String getDepartment() {
        return ""; // For compatibility - can be removed later
    }

    public void setDepartment(String department) {
        // For compatibility - can be removed later
    }

    public String getPhone() {
        return telefono;
    }

    public void setPhone(String phone) {
        this.telefono = phone;
    }

    public LocalDate getHireDate() {
        return null; // Not used in new structure
    }

    public void setHireDate(LocalDate hireDate) {
        // Not used in new structure
    }

    public Long getUserId() {
        return null; // Not used in new structure
    }

    public void setUserId(Long userId) {
        // Not used in new structure
    }

    @Override
    public String toString() {
        return idDocente + " - " + getNombreCompleto();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Teacher teacher = (Teacher) obj;
        return idDocente != null && idDocente.equals(teacher.idDocente);
    }

    @Override
    public int hashCode() {
        return idDocente != null ? idDocente.hashCode() : 0;
    }
}
