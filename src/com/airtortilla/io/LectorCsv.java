package com.airtortilla.io;

import com.airtortilla.model.Reserva;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class LectorCsv {

    public List<Reserva> leerTodas(String rutaArchivo) throws IOException {
        List<Reserva> reservas = new ArrayList<>();
        File archivo = new File(rutaArchivo);
        if (!archivo.exists()) {
            throw new IOException("El archivo no existe: " + rutaArchivo);
        }

        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            boolean primeraLinea = true;
            while ((linea = br.readLine()) != null) {
                if (linea.trim().isEmpty()) {
                    continue;
                }
                if (primeraLinea) {
                    primeraLinea = false;
                    if (esCabecera(linea)) {
                        continue;
                    }
                }
                Reserva reserva = parsearLinea(linea);
                if (reserva != null) {
                    reservas.add(reserva);
                }
            }
        }
        return reservas;
    }

    public void leerLineaALinea(String rutaArchivo, Consumer<Reserva> consumidor) throws IOException {
        File archivo = new File(rutaArchivo);
        if (!archivo.exists()) {
            throw new IOException("El archivo no existe: " + rutaArchivo);
        }

        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            boolean primeraLinea = true;
            while ((linea = br.readLine()) != null) {
                if (linea.trim().isEmpty()) {
                    continue;
                }
                if (primeraLinea) {
                    primeraLinea = false;
                    if (esCabecera(linea)) {
                        continue;
                    }
                }
                Reserva reserva = parsearLinea(linea);
                if (reserva != null && consumidor != null) {
                    consumidor.accept(reserva);
                }
            }
        }
    }

    public Reserva parsearLinea(String linea) {
        if (linea == null || linea.trim().isEmpty()) {
            return null;
        }

        String[] tokens = linea.split(",");
        if (tokens.length < 5) {
            return null;
        }

        String nombre = tokens[0].trim();
        String origen = tokens[1].trim();
        String destino = tokens[2].trim();
        String codigoPais = tokens[3].trim().toUpperCase();
        String fechaStr = tokens[4].trim();

        LocalDate fecha;
        try {
            fecha = LocalDate.parse(fechaStr);
        } catch (DateTimeParseException e) {
            return null;
        }

        return new Reserva(nombre, origen, destino, codigoPais, fecha);
    }

    private boolean esCabecera(String linea) {
        String lower = linea.toLowerCase();
        return lower.contains("nombre") || lower.contains("pasajero") || lower.contains("origen");
    }
}
