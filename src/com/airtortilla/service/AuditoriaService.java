package com.airtortilla.service;

import com.airtortilla.db.AuditoriaDAO;
import com.airtortilla.model.RegistroAuditoria;

import java.util.List;

/**
 * Servicio de trazabilidad y auditoria de seguridad (US3).
 * Satisface: "Como Administrador de Sistemas Quiero mantener la trazabilidad de las
 * modificaciones para mantener las buenas practicas de seguridad en la aplicacion".
 */
public class AuditoriaService {

    private final AuditoriaDAO auditoriaDAO;

    public AuditoriaService() {
        this.auditoriaDAO = new AuditoriaDAO();
    }

    public AuditoriaService(AuditoriaDAO auditoriaDAO) {
        this.auditoriaDAO = auditoriaDAO;
    }

    public void registrarEvento(String usuario, String accion, String entidad, String detalles, String ipOrigen) {
        RegistroAuditoria registro = new RegistroAuditoria(usuario, accion, entidad, detalles, ipOrigen);
        auditoriaDAO.registrar(registro);
    }

    public List<RegistroAuditoria> obtenerUltimosEventos(int limite) {
        return auditoriaDAO.listarUltimos(limite);
    }

    public List<RegistroAuditoria> obtenerEventosPorUsuario(String usuario) {
        return auditoriaDAO.listarPorUsuario(usuario);
    }
}
