package com.airtortilla.model;

/**
 * Entidad que representa un usuario del sistema (Empleado o Administrador).
 */
public class Usuario {

    private int id;
    private String username;
    private String passwordHash;
    private RolUsuario rol;
    private String nombreCompleto;
    private String fechaCreacion;
    private boolean activo;

    public Usuario() {
        this.activo = true;
        this.rol = RolUsuario.EMPLEADO;
    }

    public Usuario(int id, String username, String passwordHash, RolUsuario rol, String nombreCompleto, String fechaCreacion, boolean activo) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.rol = rol;
        this.nombreCompleto = nombreCompleto;
        this.fechaCreacion = fechaCreacion;
        this.activo = activo;
    }

    public Usuario(String username, String passwordHash, RolUsuario rol, String nombreCompleto) {
        this(0, username, passwordHash, rol, nombreCompleto, null, true);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public RolUsuario getRol() {
        return rol;
    }

    public void setRol(RolUsuario rol) {
        this.rol = rol;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(String fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public boolean esAdmin() {
        return this.rol == RolUsuario.ADMIN;
    }

    @Override
    public String toString() {
        return "Usuario{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", rol=" + rol +
                ", nombreCompleto='" + nombreCompleto + '\'' +
                ", activo=" + activo +
                '}';
    }
}
