package com.airtortilla.db;

import com.airtortilla.model.RolUsuario;
import com.airtortilla.model.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos relacional para la entidad Usuario (US1 y US2).
 */
public class UsuarioDAO {

    public Usuario validarCredenciales(String username, String passwordHash) {
        String sql = "SELECT id, username, password_hash, rol, nombre_completo, fecha_creacion, activo " +
                "FROM usuarios WHERE username = ? AND password_hash = ? AND activo = 1;";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, passwordHash);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearUsuario(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al validar credenciales: " + e.getMessage());
        }
        return null;
    }

    public Usuario buscarPorUsername(String username) {
        String sql = "SELECT id, username, password_hash, rol, nombre_completo, fecha_creacion, activo " +
                "FROM usuarios WHERE username = ?;";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearUsuario(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar usuario: " + e.getMessage());
        }
        return null;
    }

    public boolean existeUsername(String username) {
        String sql = "SELECT 1 FROM usuarios WHERE username = ?;";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar existencia de usuario: " + e.getMessage());
        }
        return false;
    }

    public boolean insertar(Usuario usuario) {
        String sql = "INSERT INTO usuarios (username, password_hash, rol, nombre_completo, activo) " +
                "VALUES (?, ?, ?, ?, ?);";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, usuario.getUsername());
            ps.setString(2, usuario.getPasswordHash());
            ps.setString(3, usuario.getRol().name());
            ps.setString(4, usuario.getNombreCompleto());
            ps.setInt(5, usuario.isActivo() ? 1 : 0);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al insertar usuario: " + e.getMessage());
            return false;
        }
    }

    public List<Usuario> listarTodos() {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT id, username, password_hash, rol, nombre_completo, fecha_creacion, activo " +
                "FROM usuarios ORDER BY fecha_creacion DESC;";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapearUsuario(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar usuarios: " + e.getMessage());
        }
        return lista;
    }

    public boolean desactivar(String username) {
        String sql = "UPDATE usuarios SET activo = 0 WHERE username = ?;";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al desactivar usuario: " + e.getMessage());
            return false;
        }
    }

    private Usuario mapearUsuario(ResultSet rs) throws SQLException {
        return new Usuario(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("password_hash"),
                RolUsuario.desdeTexto(rs.getString("rol")),
                rs.getString("nombre_completo"),
                rs.getString("fecha_creacion"),
                rs.getInt("activo") == 1
        );
    }
}
