package com.airtortilla.db;

import com.airtortilla.model.RegistroAuditoria;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos relacional para el registro de trazabilidad y auditoria (US3).
 */
public class AuditoriaDAO {

    public boolean registrar(RegistroAuditoria r) {
        String sql = "INSERT INTO auditoria (usuario, accion, entidad, detalles, ip_origen, fecha_hora) " +
                "VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP);";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, r.getUsuario() != null ? r.getUsuario() : "ANONIMO");
            ps.setString(2, r.getAccion());
            ps.setString(3, r.getEntidad());
            ps.setString(4, r.getDetalles());
            ps.setString(5, r.getIpOrigen() != null ? r.getIpOrigen() : "LOCAL");
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al registrar auditoria: " + e.getMessage());
            return false;
        }
    }

    public List<RegistroAuditoria> listarUltimos(int limite) {
        List<RegistroAuditoria> lista = new ArrayList<>();
        String sql = "SELECT id, usuario, accion, entidad, detalles, ip_origen, fecha_hora " +
                "FROM auditoria ORDER BY id DESC LIMIT ?;";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, limite > 0 ? limite : 50);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearAuditoria(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al listar registros de auditoria: " + e.getMessage());
        }
        return lista;
    }

    public List<RegistroAuditoria> listarPorUsuario(String usuario) {
        List<RegistroAuditoria> lista = new ArrayList<>();
        String sql = "SELECT id, usuario, accion, entidad, detalles, ip_origen, fecha_hora " +
                "FROM auditoria WHERE usuario = ? ORDER BY id DESC;";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, usuario);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearAuditoria(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al listar auditoria por usuario: " + e.getMessage());
        }
        return lista;
    }

    private RegistroAuditoria mapearAuditoria(ResultSet rs) throws SQLException {
        return new RegistroAuditoria(
                rs.getInt("id"),
                rs.getString("usuario"),
                rs.getString("accion"),
                rs.getString("entidad"),
                rs.getString("detalles"),
                rs.getString("ip_origen"),
                rs.getString("fecha_hora")
        );
    }
}

