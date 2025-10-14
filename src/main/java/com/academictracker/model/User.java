package com.academictracker.model;

/**
 * UsuarioSistema entity - Represents system users for authentication
 */
public class User {
    private Long idUsuario;
    private String username;
    private String passwordHash;
    private UserRole rol;
    private String idReferencia;
    private String tipoReferencia;
    private boolean activo;

    public enum UserRole {
        ADMIN("admin"),
        DOCENTE("docente"),
        ESTUDIANTE("estudiante");

        private final String value;

        UserRole(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static UserRole fromString(String value) {
            for (UserRole role : UserRole.values()) {
                if (role.value.equalsIgnoreCase(value)) {
                    return role;
                }
            }
            throw new IllegalArgumentException("Unknown role: " + value);
        }

        @Override
        public String toString() {
            return value;
        }
    }

    // Constructors
    public User() {
    }

    public User(String username, String passwordHash, UserRole rol) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.rol = rol;
        this.activo = true;
    }

    public User(Long idUsuario, String username, String passwordHash, UserRole rol,
                String idReferencia, String tipoReferencia) {
        this.idUsuario = idUsuario;
        this.username = username;
        this.passwordHash = passwordHash;
        this.rol = rol;
        this.idReferencia = idReferencia;
        this.tipoReferencia = tipoReferencia;
        this.activo = true;
    }

    // Getters and Setters
    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public UserRole getRol() {
        return rol;
    }

    public void setRol(UserRole rol) {
        this.rol = rol;
    }

    public String getIdReferencia() {
        return idReferencia;
    }

    public void setIdReferencia(String idReferencia) {
        this.idReferencia = idReferencia;
    }

    public String getTipoReferencia() {
        return tipoReferencia;
    }

    public void setTipoReferencia(String tipoReferencia) {
        this.tipoReferencia = tipoReferencia;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    // Legacy compatibility methods for existing code
    public Long getUserId() {
        return idUsuario;
    }

    public void setUserId(Long userId) {
        this.idUsuario = userId;
    }

    public UserRole getRole() {
        return rol;
    }

    public void setRole(UserRole role) {
        this.rol = role;
    }

    public String getEmail() {
        return ""; // For compatibility - email is now in Student/Teacher entities
    }

    public void setEmail(String email) {
        // For compatibility - email is now in Student/Teacher entities
    }

    public boolean isActive() {
        return activo;
    }

    public void setActive(boolean active) {
        this.activo = active;
    }

    @Override
    public String toString() {
        return username + " (" + rol + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        User user = (User) obj;
        return idUsuario != null && idUsuario.equals(user.idUsuario);
    }

    @Override
    public int hashCode() {
        return idUsuario != null ? idUsuario.hashCode() : 0;
    }
}
