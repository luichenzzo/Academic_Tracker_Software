package com.academictracker.model;

import java.time.LocalDate;

/**
 * Estudiante entity - Represents university students
 */
public class Student {
    private String codEstudiante;
    private String numeroDocumento;
    private String tipoDocumento;
    private String nombres;
    private String apellidos;
    private String correoInstitucional;
    private String telefono;
    private LocalDate fechaIngreso;
    private Integer nivelRiesgo;
    private Long codPrograma;
    private Long idSede;
    private boolean activo;

    // For joins
    private Program programa;
    private Sede sede;

    // Constructors
    public Student() {
    }

    public Student(String codEstudiante, String nombres, String apellidos) {
        this.codEstudiante = codEstudiante;
        this.nombres = nombres;
        this.apellidos = apellidos;
    }

    public Student(String codEstudiante, String numeroDocumento, String tipoDocumento,
                   String nombres, String apellidos, String correoInstitucional,
                   LocalDate fechaIngreso, Long codPrograma, Long idSede) {
        this.codEstudiante = codEstudiante;
        this.numeroDocumento = numeroDocumento;
        this.tipoDocumento = tipoDocumento;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.correoInstitucional = correoInstitucional;
        this.fechaIngreso = fechaIngreso;
        this.codPrograma = codPrograma;
        this.idSede = idSede;
        this.activo = true;
        this.nivelRiesgo = 0;
    }

    // Getters and Setters
    public String getCodEstudiante() {
        return codEstudiante;
    }

    public void setCodEstudiante(String codEstudiante) {
        this.codEstudiante = codEstudiante;
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

    public LocalDate getFechaIngreso() {
        return fechaIngreso;
    }

    public void setFechaIngreso(LocalDate fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }

    public Integer getNivelRiesgo() {
        return nivelRiesgo;
    }

    public void setNivelRiesgo(Integer nivelRiesgo) {
        this.nivelRiesgo = nivelRiesgo;
    }

    public Long getCodPrograma() {
        return codPrograma;
    }

    public void setCodPrograma(Long codPrograma) {
        this.codPrograma = codPrograma;
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

    public Program getPrograma() {
        return programa;
    }

    public void setPrograma(Program programa) {
        this.programa = programa;
        if (programa != null) {
            this.codPrograma = programa.getCodPrograma();
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

    public Long getStudentId() {
        return codEstudiante != null ? Long.parseLong(codEstudiante) : null;
    }

    public void setStudentId(Long studentId) {
        this.codEstudiante = studentId != null ? studentId.toString() : null;
    }

    // Legacy methods for compatibility
    public LocalDate getDateOfBirth() {
        return null; // Not used in new structure
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        // Not used in new structure
    }

    public String getPhone() {
        return telefono;
    }

    public void setPhone(String phone) {
        this.telefono = phone;
    }

    public String getAddress() {
        return ""; // Not used in new structure
    }

    public void setAddress(String address) {
        // Not used in new structure
    }

    public LocalDate getEnrollmentDate() {
        return fechaIngreso;
    }

    public void setEnrollmentDate(LocalDate enrollmentDate) {
        this.fechaIngreso = enrollmentDate;
    }

    public Long getUserId() {
        return null; // Not used in new structure
    }

    public void setUserId(Long userId) {
        // Not used in new structure
    }

    @Override
    public String toString() {
        return codEstudiante + " - " + getNombreCompleto();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Student student = (Student) obj;
        return codEstudiante != null && codEstudiante.equals(student.codEstudiante);
    }

    @Override
    public int hashCode() {
        return codEstudiante != null ? codEstudiante.hashCode() : 0;
    }
}
