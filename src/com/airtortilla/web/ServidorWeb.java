package com.airtortilla.web;

import com.airtortilla.algoritmos.OrdenadorMergeSort;
import com.airtortilla.io.EscritorCsv;
import com.airtortilla.io.LectorCsv;
import com.airtortilla.model.Reserva;
import com.airtortilla.service.IdentificadorService;
import com.airtortilla.test.GeneradorDatosPrueba;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServidorWeb {

    private final int puerto;
    private HttpServer servidor;
    private final LectorCsv lectorCsv;
    private final EscritorCsv escritorCsv;
    private final IdentificadorService idService;
    private final String carpetaSalida;

    public ServidorWeb(int puerto) {
        this.puerto = puerto;
        this.lectorCsv = new LectorCsv();
        this.escritorCsv = new EscritorCsv();
        this.idService = new IdentificadorService();
        this.carpetaSalida = "salida";
    }

    public void iniciar() throws IOException {
        servidor = HttpServer.create(new InetSocketAddress(puerto), 0);
        servidor.createContext("/", new ManejadorEstatico());
        servidor.createContext("/estilos.css", new ManejadorEstatico());
        servidor.createContext("/app.js", new ManejadorEstatico());
        servidor.createContext("/api/procesar-total", new ManejadorProcesarTotal());
        servidor.createContext("/api/leer-reservas", new ManejadorLeerReservas());
        servidor.createContext("/api/archivos", new ManejadorArchivos());
        servidor.createContext("/api/generar-pruebas", new ManejadorGenerarPruebas());
        servidor.createContext("/api/descargar", new ManejadorDescargar());
        servidor.setExecutor(null);
        servidor.start();
    }

    public void detener() {
        if (servidor != null) {
            servidor.stop(0);
        }
    }

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

            StringBuilder json = new StringBuilder("{");
            json.append("\"exito\":true,");
            json.append("\"totalReservas\":").append(reservas.size()).append(",");
            json.append("\"totalCoincidencias\":").append(conteoLoc.size()).append(",");

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

    private void configurarCors(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
    }

    private void enviarJson(HttpExchange exchange, int codigo, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(codigo, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
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
