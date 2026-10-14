package com.airtortilla.io;

import com.airtortilla.model.Reserva;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EscritorCsv {

    private static final String CABECERA_CSV = "id,localizador,nombre_pasajero,origen,destino,codigo_pais_destino,fecha_reserva";
    private static final DateTimeFormatter FORMATEADOR_FECHA_FICHERO = DateTimeFormatter.ofPattern("yyyy_MM_dd");

    public String generarNombreFichero(String codigoPais, LocalDate fecha) {
        String pais = codigoPais != null ? codigoPais.trim().toUpperCase() : "XX";
        String fechaStr = fecha != null ? fecha.format(FORMATEADOR_FECHA_FICHERO) : "0000_00_00";
        return "AirTortilla_" + pais + "_" + fechaStr + ".csv";
    }

    public List<File> exportarReservasPorPaisYFecha(List<Reserva> reservas, String rutaDirectorioSalida) throws IOException {
        List<File> ficherosCreados = new ArrayList<>();
        if (reservas == null || reservas.isEmpty()) {
            return ficherosCreados;
        }

        File carpeta = new File(rutaDirectorioSalida != null && !rutaDirectorioSalida.trim().isEmpty() ? rutaDirectorioSalida : ".");
        if (!carpeta.exists()) {
            carpeta.mkdirs();
        }

        Map<String, List<Reserva>> particiones = new HashMap<>();
        for (Reserva r : reservas) {
            if (r != null && r.getCodigoPaisDestino() != null && r.getFechaReserva() != null) {
                String clave = r.getCodigoPaisDestino().trim().toUpperCase() + "|" + r.getFechaReserva().toString();
                particiones.computeIfAbsent(clave, k -> new ArrayList<>()).add(r);
            }
        }

        for (Map.Entry<String, List<Reserva>> entrada : particiones.entrySet()) {
            String[] partes = entrada.getKey().split("\\|");
            String pais = partes[0];
            LocalDate fecha = LocalDate.parse(partes[1]);

            String nombreFichero = generarNombreFichero(pais, fecha);
            File archivoSalida = new File(carpeta, nombreFichero);

            escribirFicheroCompleto(entrada.getValue(), archivoSalida);
            ficherosCreados.add(archivoSalida);
        }

        return ficherosCreados;
    }

    public void escribirFicheroCompleto(List<Reserva> reservas, File archivoSalida) throws IOException {
        if (archivoSalida.getParentFile() != null && !archivoSalida.getParentFile().exists()) {
            archivoSalida.getParentFile().mkdirs();
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(archivoSalida))) {
            bw.write(CABECERA_CSV);
            bw.newLine();
            if (reservas != null) {
                for (Reserva r : reservas) {
                    bw.write(convertirReservaACsv(r));
                    bw.newLine();
                }
            }
        }
    }

    public void escribirLinea(Reserva reserva, File archivoSalida) throws IOException {
        if (reserva == null || archivoSalida == null) {
            return;
        }

        if (archivoSalida.getParentFile() != null && !archivoSalida.getParentFile().exists()) {
            archivoSalida.getParentFile().mkdirs();
        }

        boolean existeYTieneContenido = archivoSalida.exists() && archivoSalida.length() > 0;

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(archivoSalida, true))) {
            if (!existeYTieneContenido) {
                bw.write(CABECERA_CSV);
                bw.newLine();
            }
            bw.write(convertirReservaACsv(reserva));
            bw.newLine();
        }
    }

    public String convertirReservaACsv(Reserva r) {
        if (r == null) {
            return "";
        }
        String id = r.getId() != null ? r.getId() : "";
        String localizador = r.getLocalizador() != null ? r.getLocalizador() : "N/A";
        String nombre = r.getNombrePasajero() != null ? r.getNombrePasajero() : "";
        String origen = r.getOrigen() != null ? r.getOrigen() : "";
        String destino = r.getDestino() != null ? r.getDestino() : "";
        String codigoPais = r.getCodigoPaisDestino() != null ? r.getCodigoPaisDestino().toUpperCase() : "";
        String fecha = r.getFechaReserva() != null ? r.getFechaReserva().toString() : "";

        return id + "," + localizador + "," + nombre + "," + origen + "," + destino + "," + codigoPais + "," + fecha;
    }
}
