package com.airtortilla.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Gestor centralizado de conexion a la base de datos relacional (SQLite JDBC).
 * Cumple con el requerimiento de migracion por legislacion a infraestructura relacional.
 */
public class ConexionBD {

    private static final String URL_BD_DEFECTO = "jdbc:sqlite:airtortilla.db";
    private static String urlActual = URL_BD_DEFECTO;
    private static boolean inicializada = false;

    private ConexionBD() {
    }

    /**
     * Permite configurar una URL de base de datos alternativa (por ejemplo para tests).
     */
    public static synchronized void setUrl(String url) {
        urlActual = url;
        inicializada = false;
    }

    /**
     * Obtiene una conexion activa a la base de datos relacional, inicializando el esquema si es necesario.
     */
    public static synchronized Connection obtenerConexion() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException ignored) {
        }

        Connection conexion = DriverManager.getConnection(urlActual);
        if (!inicializada) {
            inicializarEsquema(conexion);
            inicializada = true;
        }
        return conexion;
    }

    /**
     * Crea las tablas relacionales e inserta datos iniciales si no existen.
     */
    private static void inicializarEsquema(Connection conexion) {
        String ddlUsuarios = "CREATE TABLE IF NOT EXISTS usuarios (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username VARCHAR(50) NOT NULL UNIQUE, " +
                "password_hash VARCHAR(64) NOT NULL, " +
                "rol VARCHAR(20) NOT NULL CHECK(rol IN ('ADMIN', 'EMPLEADO')), " +
                "nombre_completo VARCHAR(100) NOT NULL, " +
                "fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "activo INTEGER NOT NULL DEFAULT 1 CHECK(activo IN (0, 1))" +
                ");";

        String idxUsuario = "CREATE UNIQUE INDEX IF NOT EXISTS idx_usuarios_username ON usuarios(username);";

        String ddlReservas = "CREATE TABLE IF NOT EXISTS reservas (" +
                "id VARCHAR(50) PRIMARY KEY, " +
                "nombre_pasajero VARCHAR(100) NOT NULL, " +
                "origen VARCHAR(50) NOT NULL, " +
                "destino VARCHAR(50) NOT NULL, " +
                "codigo_pais_destino VARCHAR(2) NOT NULL, " +
                "fecha_reserva DATE NOT NULL, " +
                "localizador VARCHAR(50) DEFAULT 'N/A', " +
                "fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ");";

        String idxReservas = "CREATE INDEX IF NOT EXISTS idx_reservas_pais_fecha ON reservas(codigo_pais_destino, fecha_reserva);";

        String ddlAuditoria = "CREATE TABLE IF NOT EXISTS auditoria (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "usuario VARCHAR(50) NOT NULL, " +
                "accion VARCHAR(50) NOT NULL, " +
                "entidad VARCHAR(50) NOT NULL, " +
                "detalles TEXT, " +
                "ip_origen VARCHAR(45) DEFAULT '127.0.0.1', " +
                "fecha_hora TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ");";

        String idxAuditoria = "CREATE INDEX IF NOT EXISTS idx_auditoria_fecha ON auditoria(fecha_hora DESC);";

        // Admin: 'admin123' (SHA-256: 240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9)
        // Empleado: 'empleado123' (SHA-256: 5994471abb01112afcc18159f6cc74b4f511b99806da59b3caf5a9c173cacfc5)
        String seedUsuarios = "INSERT OR IGNORE INTO usuarios (username, password_hash, rol, nombre_completo, activo) " +
                "VALUES " +
                "('admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'ADMIN', 'Administrador del Sistema', 1), " +
                "('empleado', '5994471abb01112afcc18159f6cc74b4f511b99806da59b3caf5a9c173cacfc5', 'EMPLEADO', 'Carlos Empleado', 1);";

        try (Statement stmt = conexion.createStatement()) {
            stmt.execute(ddlUsuarios);
            stmt.execute(idxUsuario);
            stmt.execute(ddlReservas);
            stmt.execute(idxReservas);
            stmt.execute(ddlAuditoria);
            stmt.execute(idxAuditoria);
            stmt.execute(seedUsuarios);
        } catch (SQLException e) {
            System.err.println("Advertencia al inicializar esquema relacional: " + e.getMessage());
        }
    }
}
