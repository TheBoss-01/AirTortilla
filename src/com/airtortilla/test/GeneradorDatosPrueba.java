package com.airtortilla.test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class GeneradorDatosPrueba {

    private static final String RUTA_DEFECTO = "AirTortilla_Reservas_2020.csv";

    public static void main(String[] args) {
        String ruta = args.length > 0 ? args[0] : RUTA_DEFECTO;
        generarCsvPrueba(ruta);
    }

    public static File generarCsvPrueba(String rutaArchivo) {
        File archivo = new File(rutaArchivo);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(archivo))) {
            bw.write("nombre_pasajero,origen,destino,codigo_pais_destino,fecha_reserva");
            bw.newLine();

            bw.write("Carlos Ruiz,Madrid,Paris,FR,2020-03-10");
            bw.newLine();
            bw.write("Carlos Ruiz,Madrid,Paris,FR,2020-03-11");
            bw.newLine();
            bw.write("Carlos Ruiz,Madrid,Paris,FR,2020-03-12");
            bw.newLine();
            bw.write("Carlos Ruiz,Madrid,Paris,FR,2020-03-13");
            bw.newLine();
            bw.write("Carlos Ruiz,Madrid,Paris,FR,2020-03-14");
            bw.newLine();

            bw.write("Elena Gomez,Barcelona,Roma,IT,2020-04-05");
            bw.newLine();
            bw.write("Elena Gomez,Barcelona,Roma,IT,2020-04-06");
            bw.newLine();

            bw.write("David Fernandez,Sevilla,Londres,GB,2020-05-20");
            bw.newLine();
            bw.write("David Fernandez,Sevilla,Londres,GB,2020-05-21");
            bw.newLine();
            bw.write("David Fernandez,Sevilla,Londres,GB,2020-05-22");
            bw.newLine();

            bw.write("Lucia Santos,Valencia,Berlin,DE,2020-06-01");
            bw.newLine();
            bw.write("Marcos Perez,Bilbao,Lisboa,PT,2020-06-15");
            bw.newLine();
            bw.write("Sofia Navarro,Madrid,Roma,IT,2020-07-02");
            bw.newLine();

            System.out.println("Fichero generado exitosamente en: " + archivo.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error al generar fichero de prueba: " + e.getMessage());
        }
        return archivo;
    }
}
