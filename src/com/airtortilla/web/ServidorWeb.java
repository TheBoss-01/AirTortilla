package com.airtortilla.web;

import com.airtortilla.algoritmos.OrdenadorMergeSort;
import com.airtortilla.io.EscritorCsv;
import com.airtortilla.io.LectorCsv;
import com.airtortilla.model.RegistroAuditoria;
import com.airtortilla.model.Reserva;
import com.airtortilla.model.RolUsuario;
import com.airtortilla.model.Usuario;
import com.airtortilla.service.AuditoriaService;
import com.airtortilla.service.AutenticacionService;
import com.airtortilla.service.IdentificadorService;
import com.airtortilla.service.MigracionRelacionalService;
import com.airtortilla.test.GeneradorDatosPrueba;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Servidor HTTP embebido en Java SE con API REST y soporte para interfaz web.
 * Incluye endpoints para US1 (Login), US2 (Alta Usuarios), US3 (Auditoria) y Migracion Relacional.
 */
public class ServidorWeb {

    private final int puerto;
    private HttpServer servidor;
    private final LectorCsv lectorCsv;
    private final EscritorCsv escritorCsv;
    private final IdentificadorService idService;
    private final AutenticacionService authService;
    private final AuditoriaService auditoriaService;
    private final MigracionRelacionalService migracionService;
    private final String carpetaSalida;

    public ServidorWeb(int puerto) {
        this.puerto = puerto;
        this.lectorCsv = new LectorCsv();
        this.escritorCsv = new EscritorCsv();
        this.idService = new IdentificadorService();
        this.authService = new AutenticacionService();
        this.auditoriaService = new AuditoriaService();
        this.migracionService = new MigracionRelacionalService();
        this.carpetaSalida = "salida";
    }

    public void iniciar() throws IOException {
        servidor = HttpServer.create(new InetSocketAddress(puerto), 0);

        // Rutas estaticas
        servidor.createContext("/", new ManejadorEstatico());
        servidor.createContext("/estilos.css", new ManejadorEstatico());
        servidor.createContext("/app.js", new ManejadorEstatico());

        // Rutas API existentes (Entrega 1)
        servidor.createContext("/api/procesar-total", new ManejadorProcesarTotal());
        servidor.createContext("/api/leer-reservas", new ManejadorLeerReservas());
        servidor.createContext("/api/archivos", new ManejadorArchivos());
        servidor.createContext("/api/generar-pruebas", new ManejadorGenerarPruebas());
        servidor.createContext("/api/descargar", new ManejadorDescargar());

        // Nuevas rutas API (Entrega 2)
        servidor.createContext("/api/auth/login", new ManejadorLogin());
        servidor.createContext("/api/auth/logout", new ManejadorLogout());
        servidor.createContext("/api/auth/estado", new ManejadorEstadoAuth());
        servidor.createContext("/api/usuarios/alta", new ManejadorAltaUsuario());
        servidor.createContext("/api/usuarios", new ManejadorListarUsuarios());
        servidor.createContext("/api/auditoria", new ManejadorAuditoria());
        servidor.createContext("/api/bd/migrar", new ManejadorMigrarBD());
        servidor.createContext("/api/bd/reservas", new ManejadorReservasBD());

        servidor.setExecutor(null);
        servidor.start();
    }

    public void detener() {
        if (servidor != null) {
            servidor.stop(0);
        }
    }

    public AutenticacionService getAuthService() {
        return authService;
    }

    public AuditoriaService getAuditoriaService() {
        return auditoriaService;
    }

    public MigracionRelacionalService getMigracionService() {
        return migracionService;
    }

    // =========================================================================
    // MANEJADORES ESTATICOS Y DE ENTREGA 1
    // =========================================================================

    private class ManejadorEstatico implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/")) {
                path = "/index.html";
            }

            File archivo = new File("web" + path);
            if (!archivo.exists() || archivo.isDirectory()) {
                String error = "<html><body><h1>404 Recurso no encontrado</h1></body></html>";
                byte[] bytes = error.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                exchange.sendResponseHeaders(404, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
                return;
            }

            String contentType = "text/html; charset=UTF-8";
            if (path.endsWith(".css")) {
                contentType = "text/css; charset=UTF-8";
            } else if (path.endsWith(".js")) {
                contentType = "application/javascript; charset=UTF-8";
            } else if (path.endsWith(".json")) {
                contentType = "application/json; charset=UTF-8";
            }

            byte[] bytes = Files.readAllBytes(archivo.toPath());
            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    private class ManejadorLeerReservas implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            configurarCors(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            File csvFile = new File("AirTortilla_Reservas_2020.csv");
            if (!csvFile.exists()) {
                GeneradorDatosPrueba.generarCsvPrueba("AirTortilla_Reservas_2020.csv");
            }

            List<Reserva> lista = lectorCsv.leerTodas("AirTortilla_Reservas_2020.csv");
            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < lista.size(); i++) {
                Reserva r = lista.get(i);
                json.append("{")
                        .append("\"nombrePasajero\":\"").append(escaparJson(r.getNombrePasajero())).append("\",")
                        .append("\"origen\":\"").append(escaparJson(r.getOrigen())).append("\",")
                        .append("\"destino\":\"").append(escaparJson(r.getDestino())).append("\",")
                        .append("\"codigoPaisDestino\":\"").append(escaparJson(r.getCodigoPaisDestino())).append("\",")
                        .append("\"fechaReserva\":\"").append(r.getFechaReserva()).append("\"")
                        .append("}");
                if (i < lista.size() - 1) {
                    json.append(",");
                }
            }
            json.append("]");

            enviarJson(exchange, 200, json.toString());
        }
    }

    private class ManejadorProcesarTotal implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            configurarCors(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            File csvFile = new File("AirTortilla_Reservas_2020.csv");
            if (!csvFile.exists()) {
                GeneradorDatosPrueba.generarCsvPrueba("AirTortilla_Reservas_2020.csv");
            }

            List<Reserva> reservas = lectorCsv.leerTodas("AirTortilla_Reservas_2020.csv");
            idService.procesarIdentificadoresYLocalizadores(reservas);

            Map<String, Integer> conteoLoc = new HashMap<>();
            for (Reserva r : reservas) {
                if (r.getLocalizador() != null && !r.getLocalizador().equals("N/A")) {
                    conteoLoc.put(r.getLocalizador(), conteoLoc.getOrDefault(r.getLocalizador(), 0) + 1);
                }
            }

            List<Reserva> ordenadas = OrdenadorMergeSort.ordenar(reservas);
            List<File> ficheros = escritorCsv.exportarReservasPorPaisYFecha(ordenadas, carpetaSalida);

            // Persistencia relacional automatica y registro de trazabilidad
            String usuarioActivo = authService.estaAutenticado() ? authService.getUsuarioActual().getUsername() : "ANONIMO";
            migracionService.migrarReservasEnMemoria(ordenadas, usuarioActivo, obtenerIpRemota(exchange));

            StringBuilder json = new StringBuilder("{");
            json.append("\"exito\":true,");
            json.append("\"totalReservas\":").append(reservas.size()).append(",");
            json.append("\"totalCoincidencias\":").append(conteoLoc.size()).append(",");
            json.append("\"persistenciaRelacional\":true,");

            json.append("\"localizadores\":[");
            int c = 0;
            for (Map.Entry<String, Integer> e : conteoLoc.entrySet()) {
                json.append("{\"localizador\":\"").append(e.getKey()).append("\",\"pasajeros\":").append(e.getValue()).append("}");
                if (++c < conteoLoc.size()) {
                    json.append(",");
                }
            }
            json.append("],");

            json.append("\"ficheros\":[");
            for (int i = 0; i < ficheros.size(); i++) {
                File f = ficheros.get(i);
                json.append("{\"nombre\":\"").append(f.getName()).append("\",\"tamano\":").append(f.length()).append("}");
                if (i < ficheros.size() - 1) {
                    json.append(",");
                }
            }
            json.append("],");

            json.append("\"reservas\":[");
            for (int i = 0; i < ordenadas.size(); i++) {
                Reserva r = ordenadas.get(i);
                json.append("{")
                        .append("\"id\":\"").append(r.getId()).append("\",")
                        .append("\"localizador\":\"").append(r.getLocalizador()).append("\",")
                        .append("\"nombrePasajero\":\"").append(escaparJson(r.getNombrePasajero())).append("\",")
                        .append("\"origen\":\"").append(escaparJson(r.getOrigen())).append("\",")
                        .append("\"destino\":\"").append(escaparJson(r.getDestino())).append("\",")
                        .append("\"codigoPaisDestino\":\"").append(escaparJson(r.getCodigoPaisDestino())).append("\",")
                        .append("\"fechaReserva\":\"").append(r.getFechaReserva()).append("\"")
                        .append("}");
                if (i < ordenadas.size() - 1) {
                    json.append(",");
                }
            }
            json.append("]");
            json.append("}");

            enviarJson(exchange, 200, json.toString());
        }
    }

    private class ManejadorArchivos implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            configurarCors(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            File carpeta = new File(carpetaSalida);
            File[] files = carpeta.listFiles((dir, name) -> name.startsWith("AirTortilla_") && name.endsWith(".csv"));
            if (files == null) {
                files = new File[0];
            }

            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < files.length; i++) {
                File f = files[i];
                int lineas = 0;
                try (BufferedReader br = new BufferedReader(new FileReader(f))) {
                    while (br.readLine() != null) {
                        lineas++;
                    }
                }
                json.append("{")
                        .append("\"nombre\":\"").append(f.getName()).append("\",")
                        .append("\"registros\":").append(Math.max(0, lineas - 1)).append(",")
                        .append("\"tamano\":").append(f.length())
                        .append("}");
                if (i < files.length - 1) {
                    json.append(",");
                }
            }
            json.append("]");

            enviarJson(exchange, 200, json.toString());
        }
    }

    private class ManejadorGenerarPruebas implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            configurarCors(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            File f = GeneradorDatosPrueba.generarCsvPrueba("AirTortilla_Reservas_2020.csv");
            String ip = obtenerIpRemota(exchange);
            String usuario = authService.estaAutenticado() ? authService.getUsuarioActual().getUsername() : "ANONIMO";
            auditoriaService.registrarEvento(usuario, "RESTAURAR_CSV_PRUEBAS", "FICHERO", "Generacion de CSV de prueba: " + f.getName(), ip);

            String json = "{\"exito\":true,\"mensaje\":\"Fichero generado correctamente\",\"archivo\":\"" + f.getName() + "\"}";
            enviarJson(exchange, 200, json);
        }
    }

    private class ManejadorDescargar implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            String nombreArchivo = null;
            if (query != null && query.startsWith("archivo=")) {
                nombreArchivo = query.substring("archivo=".length());
            }

            if (nombreArchivo == null || nombreArchivo.contains("..") || nombreArchivo.contains("/") || nombreArchivo.contains("\\")) {
                byte[] error = "Archivo no valido".getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(400, error.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(error);
                }
                return;
            }

            File archivo = new File(carpetaSalida, nombreArchivo);
            if (!archivo.exists()) {
                byte[] error = "Archivo no encontrado".getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(404, error.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(error);
                }
                return;
            }

            byte[] bytes = Files.readAllBytes(archivo.toPath());
            exchange.getResponseHeaders().set("Content-Type", "text/csv; charset=UTF-8");
            exchange.getResponseHeaders().set("Content-Disposition", "attachment; filename=\"" + archivo.getName() + "\"");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    // =========================================================================
    // NUEVOS MANEJADORES DE ENTREGA 2 (US1, US2, US3, BD RELACIONAL)
    // =========================================================================

    /**
     * US1: Como Empleado Quiero Poder hacer log in para acceder a los recursos de la compania
     */
    private class ManejadorLogin implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            configurarCors(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                enviarJson(exchange, 405, "{\"exito\":false,\"mensaje\":\"Metodo no permitido\"}");
                return;
            }

            String body = leerCuerpoTexto(exchange);
            Map<String, String> params = extraerCamposJson(body);
            String username = params.get("username");
            String password = params.get("password");

            String ip = obtenerIpRemota(exchange);
            Usuario u = authService.iniciarSesion(username, password, ip);

            if (u != null) {
                String resp = "{"
                        + "\"exito\":true,"
                        + "\"mensaje\":\"Inicio de sesion correcto\","
                        + "\"usuario\":{"
                        + "\"id\":" + u.getId() + ","
                        + "\"username\":\"" + escaparJson(u.getUsername()) + "\","
                        + "\"rol\":\"" + u.getRol().name() + "\","
                        + "\"nombreCompleto\":\"" + escaparJson(u.getNombreCompleto()) + "\""
                        + "}"
                        + "}";
                enviarJson(exchange, 200, resp);
            } else {
                enviarJson(exchange, 401, "{\"exito\":false,\"mensaje\":\"Credenciales invalidas\"}");
            }
        }
    }

    private class ManejadorLogout implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            configurarCors(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            authService.cerrarSesion(obtenerIpRemota(exchange));
            enviarJson(exchange, 200, "{\"exito\":true,\"mensaje\":\"Sesion finalizada\"}");
        }
    }

    private class ManejadorEstadoAuth implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            configurarCors(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            Usuario u = authService.getUsuarioActual();
            if (u != null) {
                String resp = "{"
                        + "\"autenticado\":true,"
                        + "\"usuario\":{"
                        + "\"id\":" + u.getId() + ","
                        + "\"username\":\"" + escaparJson(u.getUsername()) + "\","
                        + "\"rol\":\"" + u.getRol().name() + "\","
                        + "\"nombreCompleto\":\"" + escaparJson(u.getNombreCompleto()) + "\""
                        + "}"
                        + "}";
                enviarJson(exchange, 200, resp);
            } else {
                enviarJson(exchange, 200, "{\"autenticado\":false}");
            }
        }
    }

    /**
     * US2: Como Administrador de Sistemas quiero poder dar de alta usuarios
     */
    private class ManejadorAltaUsuario implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            configurarCors(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                enviarJson(exchange, 405, "{\"exito\":false,\"mensaje\":\"Metodo no permitido\"}");
                return;
            }

            Usuario actual = authService.getUsuarioActual();
            if (actual == null || !actual.esAdmin()) {
                enviarJson(exchange, 403, "{\"exito\":false,\"mensaje\":\"Acceso denegado: se requieren permisos de Administrador\"}");
                return;
            }

            String body = leerCuerpoTexto(exchange);
            Map<String, String> params = extraerCamposJson(body);

            String username = params.get("username");
            String password = params.get("password");
            String rolStr = params.get("rol");
            String nombreCompleto = params.get("nombreCompleto");

            try {
                RolUsuario rol = RolUsuario.desdeTexto(rolStr);
                boolean creado = authService.darDeAltaUsuario(username, password, rol, nombreCompleto, obtenerIpRemota(exchange));
                if (creado) {
                    enviarJson(exchange, 201, "{\"exito\":true,\"mensaje\":\"Usuario dado de alta satisfactoriamente\"}");
                } else {
                    enviarJson(exchange, 500, "{\"exito\":false,\"mensaje\":\"No se pudo crear el usuario\"}");
                }
            } catch (IllegalArgumentException | SecurityException e) {
                enviarJson(exchange, 400, "{\"exito\":false,\"mensaje\":\"" + escaparJson(e.getMessage()) + "\"}");
            }
        }
    }

    private class ManejadorListarUsuarios implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            configurarCors(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            Usuario actual = authService.getUsuarioActual();
            if (actual == null || !actual.esAdmin()) {
                enviarJson(exchange, 403, "{\"exito\":false,\"mensaje\":\"Acceso denegado: solo Administradores pueden ver la lista de usuarios\"}");
                return;
            }

            List<Usuario> lista = authService.listarUsuarios();
            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < lista.size(); i++) {
                Usuario u = lista.get(i);
                json.append("{")
                        .append("\"id\":").append(u.getId()).append(",")
                        .append("\"username\":\"").append(escaparJson(u.getUsername())).append("\",")
                        .append("\"rol\":\"").append(u.getRol().name()).append("\",")
                        .append("\"nombreCompleto\":\"").append(escaparJson(u.getNombreCompleto())).append("\",")
                        .append("\"fechaCreacion\":\"").append(escaparJson(u.getFechaCreacion())).append("\",")
                        .append("\"activo\":").append(u.isActivo())
                        .append("}");
                if (i < lista.size() - 1) {
                    json.append(",");
                }
            }
            json.append("]");
            enviarJson(exchange, 200, json.toString());
        }
    }

    /**
     * US3: Como Administrador de Sistemas Quiero mantener la trazabilidad de las modificaciones
     */
    private class ManejadorAuditoria implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            configurarCors(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            Usuario actual = authService.getUsuarioActual();
            if (actual == null || !actual.esAdmin()) {
                enviarJson(exchange, 403, "{\"exito\":false,\"mensaje\":\"Acceso denegado: solo Administradores pueden consultar la trazabilidad\"}");
                return;
            }

            List<RegistroAuditoria> logs = auditoriaService.obtenerUltimosEventos(100);
            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < logs.size(); i++) {
                RegistroAuditoria r = logs.get(i);
                json.append("{")
                        .append("\"id\":").append(r.getId()).append(",")
                        .append("\"usuario\":\"").append(escaparJson(r.getUsuario())).append("\",")
                        .append("\"accion\":\"").append(escaparJson(r.getAccion())).append("\",")
                        .append("\"entidad\":\"").append(escaparJson(r.getEntidad())).append("\",")
                        .append("\"detalles\":\"").append(escaparJson(r.getDetalles())).append("\",")
                        .append("\"ipOrigen\":\"").append(escaparJson(r.getIpOrigen())).append("\",")
                        .append("\"fechaHora\":\"").append(escaparJson(r.getFechaHora())).append("\"")
                        .append("}");
                if (i < logs.size() - 1) {
                    json.append(",");
                }
            }
            json.append("]");
            enviarJson(exchange, 200, json.toString());
        }
    }

    /**
     * Migracion por legislacion a Base de Datos Relacional
     */
    private class ManejadorMigrarBD implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            configurarCors(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                enviarJson(exchange, 405, "{\"exito\":false,\"mensaje\":\"Metodo no permitido\"}");
                return;
            }

            File csvFile = new File("AirTortilla_Reservas_2020.csv");
            if (!csvFile.exists()) {
                GeneradorDatosPrueba.generarCsvPrueba("AirTortilla_Reservas_2020.csv");
            }

            String usuario = authService.estaAutenticado() ? authService.getUsuarioActual().getUsername() : "ANONIMO";
            try {
                int total = migracionService.migrarDesdeCsv("AirTortilla_Reservas_2020.csv", usuario, obtenerIpRemota(exchange));
                String json = "{\"exito\":true,\"totalMigradas\":" + total + ",\"mensaje\":\"Migracion relacional completada con exito\"}";
                enviarJson(exchange, 200, json);
            } catch (Exception e) {
                enviarJson(exchange, 500, "{\"exito\":false,\"mensaje\":\"" + escaparJson(e.getMessage()) + "\"}");
            }
        }
    }

    private class ManejadorReservasBD implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            configurarCors(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            List<Reserva> lista = migracionService.obtenerReservasRelacionales();
            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < lista.size(); i++) {
                Reserva r = lista.get(i);
                json.append("{")
                        .append("\"id\":\"").append(r.getId()).append("\",")
                        .append("\"localizador\":\"").append(r.getLocalizador()).append("\",")
                        .append("\"nombrePasajero\":\"").append(escaparJson(r.getNombrePasajero())).append("\",")
                        .append("\"origen\":\"").append(escaparJson(r.getOrigen())).append("\",")
                        .append("\"destino\":\"").append(escaparJson(r.getDestino())).append("\",")
                        .append("\"codigoPaisDestino\":\"").append(escaparJson(r.getCodigoPaisDestino())).append("\",")
                        .append("\"fechaReserva\":\"").append(r.getFechaReserva()).append("\"")
                        .append("}");
                if (i < lista.size() - 1) {
                    json.append(",");
                }
            }
            json.append("]");

            enviarJson(exchange, 200, json.toString());
        }
    }

    // =========================================================================
    // UTILIDADES
    // =========================================================================

    private void configurarCors(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }

    private void enviarJson(HttpExchange exchange, int codigo, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(codigo, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private String leerCuerpoTexto(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody();
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            byte[] buf = new byte[1024];
            int read;
            while ((read = is.read(buf)) != -1) {
                bos.write(buf, 0, read);
            }
            return bos.toString(StandardCharsets.UTF_8.name());
        }
    }

    private String obtenerIpRemota(HttpExchange exchange) {
        if (exchange.getRemoteAddress() != null && exchange.getRemoteAddress().getAddress() != null) {
            return exchange.getRemoteAddress().getAddress().getHostAddress();
        }
        return "127.0.0.1";
    }

    private Map<String, String> extraerCamposJson(String json) {
        Map<String, String> mapa = new HashMap<>();
        if (json == null || json.trim().isEmpty()) {
            return mapa;
        }

        Pattern p = Pattern.compile("\"([^\"]+)\"\\s*:\\s*(\"[^\"]*\"|[^,}\\]]+)");
        Matcher m = p.matcher(json);
        while (m.find()) {
            String clave = m.group(1);
            String valor = m.group(2).trim();
            if (valor.startsWith("\"") && valor.endsWith("\"")) {
                valor = valor.substring(1, valor.length() - 1);
            }
            mapa.put(clave, valor);
        }
        return mapa;
    }

    private String escaparJson(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
