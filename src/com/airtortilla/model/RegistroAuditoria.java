package com.airtortilla.model;

/**
 * Entidad que representa un registro de trazabilidad y auditoria de seguridad.
 * Satisface la US3: "Como Administrador de Sistemas Quiero mantener la trazabilidad
 * de las modificaciones para mantener las buenas practicas de seguridad en la aplicacion".
 */
public class RegistroAuditoria {

    private int id;
    private String usuario;
    private String accion;
    private String entidad;
    private String detalles;
    private String ipOrigen;
    private String fechaHora;

    public RegistroAuditoria() {
        this.ipOrigen = "LOCAL";
    }

    public RegistroAuditoria(int id, String usuario, String accion, String entidad, String detalles, String ipOrigen, String fechaHora) {
        this.id = id;
        this.usuario = usuario;
        this.accion = accion;
        this.entidad = entidad;
        this.detalles = detalles;
        this.ipOrigen = ipOrigen != null ? ipOrigen : "LOCAL";
        this.fechaHora = fechaHora;
    }

    public RegistroAuditoria(String usuario, String accion, String entidad, String detalles, String ipOrigen) {
        this(0, usuario, accion, entidad, detalles, ipOrigen, null);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getAccion() {
        return accion;
    }

    public void setAccion(String accion) {
        this.accion = accion;
    }

    public String getEntidad() {
        return entidad;
    }

    public void setEntidad(String entidad) {
        this.entidad = entidad;
    }

    public String getDetalles() {
        return detalles;
    }

    public void setDetalles(String detalles) {
        this.detalles = detalles;
    }

    public String getIpOrigen() {
        return ipOrigen;
    }

    public void setIpOrigen(String ipOrigen) {
        this.ipOrigen = ipOrigen;
    }

    public String getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(String fechaHora) {
        this.fechaHora = fechaHora;
    }

    @Override
    public String toString() {
        return "[" + fechaHora + "] (" + usuario + ") " + accion + " sobre " + entidad + ": " + detalles;
    }
}

