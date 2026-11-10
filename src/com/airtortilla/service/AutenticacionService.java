package com.airtortilla.service;

import com.airtortilla.db.AuditoriaDAO;
import com.airtortilla.db.UsuarioDAO;
import com.airtortilla.model.RegistroAuditoria;
import com.airtortilla.model.RolUsuario;
import com.airtortilla.model.Usuario;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * Servicio de autenticacion y control de acceso (US1 y US2).
 * Gestiona el inicio de sesion, la sesion activa y el alta segura de usuarios.
 */
public class AutenticacionService {

    private final UsuarioDAO usuarioDAO;
    private final AuditoriaDAO auditoriaDAO;
    private Usuario usuarioActual;

    public AutenticacionService() {
        this.usuarioDAO = new UsuarioDAO();
        this.auditoriaDAO = new AuditoriaDAO();
    }

    public AutenticacionService(UsuarioDAO usuarioDAO, AuditoriaDAO auditoriaDAO) {
        this.usuarioDAO = usuarioDAO;
        this.auditoriaDAO = auditoriaDAO;
    }

    /**
     * US1: Como Empleado Quiero Poder hacer log in para acceder a los recursos de la compania.
     */
    public Usuario iniciarSesion(String username, String password, String ipOrigen) {
        if (username == null || password == null) {
            return null;
        }

        String hash = calcularHashSha256(password);
        Usuario u = usuarioDAO.validarCredenciales(username.trim(), hash);

        if (u != null) {
            this.usuarioActual = u;
            auditoriaDAO.registrar(new RegistroAuditoria(
                    u.getUsername(),
                    "LOGIN_EXITOSO",
                    "AUTENTICACION",
                    "Inicio de sesion con rol " + u.getRol(),
                    ipOrigen
            ));
            return u;
        } else {
            auditoriaDAO.registrar(new RegistroAuditoria(
                    username,
                    "LOGIN_FALLIDO",
                    "AUTENTICACION",
                    "Credenciales invalidas para el usuario: " + username,
                    ipOrigen
            ));
            return null;
        }
    }

    /**
     * Cierra la sesion del usuario activo y registra el evento de trazabilidad.
     */
    public void cerrarSesion(String ipOrigen) {
        if (this.usuarioActual != null) {
            auditoriaDAO.registrar(new RegistroAuditoria(
                    this.usuarioActual.getUsername(),
                    "LOGOUT",
                    "AUTENTICACION",
                    "Cierre voluntario de sesion",
                    ipOrigen
            ));
            this.usuarioActual = null;
        }
    }

    /**
     * US2: Como Administrador de Sistemas quiero poder dar de alta usuarios para
     * garantizar la seguridad interna de la aplicacion.
     */
    public boolean darDeAltaUsuario(String username, String password, RolUsuario rol, String nombreCompleto, String ipOrigen) {
        if (this.usuarioActual == null || !this.usuarioActual.esAdmin()) {
            throw new SecurityException("Acceso denegado: solo un Administrador de Sistemas puede dar de alta nuevos usuarios.");
        }

        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de usuario no puede estar vacio.");
        }
        if (password == null || password.trim().length() < 4) {
            throw new IllegalArgumentException("La contrasena debe tener al menos 4 caracteres.");
        }
        if (usuarioDAO.existeUsername(username.trim())) {
            throw new IllegalArgumentException("El nombre de usuario '" + username + "' ya existe en el sistema.");
        }

        String hash = calcularHashSha256(password);
        Usuario nuevo = new Usuario(username.trim(), hash, rol != null ? rol : RolUsuario.EMPLEADO, nombreCompleto);

        boolean creado = usuarioDAO.insertar(nuevo);
        if (creado) {
            auditoriaDAO.registrar(new RegistroAuditoria(
                    this.usuarioActual.getUsername(),
                    "ALTA_USUARIO",
                    "USUARIO",
                    "Alta del usuario '" + username + "' con rol " + nuevo.getRol(),
                    ipOrigen
            ));
        }
        return creado;
    }

    public List<Usuario> listarUsuarios() {
        if (this.usuarioActual == null || !this.usuarioActual.esAdmin()) {
            throw new SecurityException("Acceso denegado: solo el Administrador puede consultar el listado de usuarios.");
        }
        return usuarioDAO.listarTodos();
    }

    public Usuario getUsuarioActual() {
        return usuarioActual;
    }

    public void setUsuarioActual(Usuario usuarioActual) {
        this.usuarioActual = usuarioActual;
    }

    public boolean estaAutenticado() {
        return usuarioActual != null;
    }

    /**
     * Funcion de hashing SHA-256 estandar de Java SE para almacenar contrasenas de forma segura.
     */
    public static String calcularHashSha256(String textoPlano) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(textoPlano.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error al calcular hash SHA-256", e);
        }
    }
}
