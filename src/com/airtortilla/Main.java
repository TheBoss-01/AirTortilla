package com.airtortilla;

import com.airtortilla.algoritmos.OrdenadorMergeSort;
import com.airtortilla.io.EscritorCsv;
import com.airtortilla.io.LectorCsv;
import com.airtortilla.model.Reserva;
import com.airtortilla.service.IdentificadorService;
import com.airtortilla.test.GeneradorDatosPrueba;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {

    private static final String FICHERO_ENTRADA_DEFECTO = "AirTortilla_Reservas_2020.csv";
    private static final String CARPETA_SALIDA = "salida";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        LectorCsv lectorCsv = new LectorCsv();
        EscritorCsv escritorCsv = new EscritorCsv();
        IdentificadorService identificadorService = new IdentificadorService();

        boolean salir = false;
        while (!salir) {
            mostrarMenuPrincipal();
            String opcion = scanner.nextLine().trim();

            switch (opcion) {
                case "1":
                    ejecutarModalidadTotal(scanner, lectorCsv, escritorCsv, identificadorService);
                    break;
                case "2":
                    ejecutarModalidadLineaALinea(scanner, lectorCsv, escritorCsv, identificadorService);
                    break;
                case "3":
                    consultarArchivosGenerados();
                    break;
                case "4":
                    generarFicheroPruebas();
                    break;
                case "5":
                    salir = true;
                    System.out.println("\nGracias por utilizar el sistema AirTortilla. Finalizando aplicacion.");
                    break;
                default:
                    System.out.println("\nOpcion no valida. Seleccione una opcion del 1 al 5.");
                    break;
            }
        }
        scanner.close();
    }

    private static void mostrarMenuPrincipal() {
        System.out.println("\n=======================================================");
        System.out.println("          SISTEMA DE RESERVAS AIRTORTILLA              ");
        System.out.println("=======================================================");
        System.out.println("1. Ejecutar procesamiento en MODALIDAD TOTAL");
        System.out.println("2. Ejecutar procesamiento en MODALIDAD LINEA A LINEA");
        System.out.println("3. Consultar archivos generados");
        System.out.println("4. Generar/Restaurar datos de prueba CSV");
        System.out.println("5. Salir");
        System.out.print("Seleccione una opcion (1-5): ");
    }

    private static void ejecutarModalidadTotal(Scanner scanner, LectorCsv lector, EscritorCsv escritor, IdentificadorService idService) {
        System.out.println("\n--- MODALIDAD TOTAL ---");
        System.out.print("Ingrese la ruta del fichero CSV [" + FICHERO_ENTRADA_DEFECTO + "]: ");
        String ruta = scanner.nextLine().trim();
        if (ruta.isEmpty()) {
            ruta = FICHERO_ENTRADA_DEFECTO;
        }

        File fEntrada = new File(ruta);
        if (!fEntrada.exists()) {
            System.out.println("El fichero " + ruta + " no existe. Puede generarlo desde la opcion 4.");
            return;
        }

        try {
            System.out.println("\n[1/4] Leyendo reservas desde CSV...");
            List<Reserva> reservas = lector.leerTodas(ruta);
            System.out.println("      Se han cargado " + reservas.size() + " reservas en memoria.");

            System.out.println("[2/4] Generando identificadores y asignando localizadores coincidentes O(n)...");
            idService.procesarIdentificadoresYLocalizadores(reservas);

            Map<String, Integer> conteoLocalizadores = new HashMap<>();
            for (Reserva r : reservas) {
                if (r.getLocalizador() != null && !r.getLocalizador().equals("N/A")) {
                    conteoLocalizadores.put(r.getLocalizador(), conteoLocalizadores.getOrDefault(r.getLocalizador(), 0) + 1);
                }
            }

            System.out.println("[3/4] Ordenando reservas mediante MergeSort O(n log n)...");
            List<Reserva> reservasOrdenadas = OrdenadorMergeSort.ordenar(reservas);

            System.out.println("[4/4] Particionando y exportando ficheros por pais y fecha...");
            List<File> ficherosCreados = escritor.exportarReservasPorPaisYFecha(reservasOrdenadas, CARPETA_SALIDA);

            mostrarFeedbackTotal(reservasOrdenadas, conteoLocalizadores, ficherosCreados);

        } catch (IOException e) {
            System.err.println("Error durante la ejecucion en modalidad total: " + e.getMessage());
        }
    }

    private static void mostrarFeedbackTotal(List<Reserva> reservas, Map<String, Integer> localizadores, List<File> ficheros) {
        System.out.println("\n=======================================================");
        System.out.println("                FEEDBACK DE OPERACION                  ");
        System.out.println("=======================================================");
        System.out.println("Estado de ejecucion: COMPLETADA CON EXITO");
        System.out.println("Total de reservas procesadas: " + reservas.size());
        System.out.println("Grupos de coincidencias detectados: " + localizadores.size());

        for (Map.Entry<String, Integer> entry : localizadores.entrySet()) {
            System.out.println("  - Localizador: " + entry.getKey() + " (" + entry.getValue() + " pasajeros coincidentes)");
        }

        System.out.println("\nFicheros generados en el directorio [" + CARPETA_SALIDA + "]:");
        for (File f : ficheros) {
            System.out.println("  - " + f.getName() + " (" + f.length() + " bytes)");
        }
        System.out.println("=======================================================");
    }

    private static void ejecutarModalidadLineaALinea(Scanner scanner, LectorCsv lector, EscritorCsv escritor, IdentificadorService idService) {
        System.out.println("\n--- MODALIDAD LINEA A LINEA ---");
        System.out.print("Ingrese la ruta del fichero CSV [" + FICHERO_ENTRADA_DEFECTO + "]: ");
        String ruta = scanner.nextLine().trim();
        if (ruta.isEmpty()) {
            ruta = FICHERO_ENTRADA_DEFECTO;
        }

        File fEntrada = new File(ruta);
        if (!fEntrada.exists()) {
            System.out.println("El fichero " + ruta + " no existe. Puede generarlo desde la opcion 4.");
            return;
        }

        File carpetaSalida = new File(CARPETA_SALIDA);
        if (!carpetaSalida.exists()) {
            carpetaSalida.mkdirs();
        }

        try {
            List<Reserva> todas = lector.leerTodas(ruta);
            idService.procesarIdentificadoresYLocalizadores(todas);

            System.out.println("\nIniciando lectura y procesamiento linea a linea...");
            System.out.println("Presione [ENTER] para avanzar registro a registro (o escriba 'auto' para finalizar el resto):");

            boolean modoAutomatico = false;
            int contador = 0;

            for (Reserva r : todas) {
                contador++;
                System.out.println("\n[Registro " + contador + " de " + todas.size() + "]");
                System.out.println("  Pasajero: " + r.getNombrePasajero());
                System.out.println("  Ruta: " + r.getOrigen() + " -> " + r.getDestino() + " (" + r.getCodigoPaisDestino() + ")");
                System.out.println("  Fecha: " + r.getFechaReserva());
                System.out.println("  ID Generado: " + r.getId());
                System.out.println("  Localizador: " + r.getLocalizador());

                String nombreFichero = escritor.generarNombreFichero(r.getCodigoPaisDestino(), r.getFechaReserva());
                File archivoDestino = new File(carpetaSalida, nombreFichero);
                escritor.escribirLinea(r, archivoDestino);
                System.out.println("  --> Anexado a fichero: " + nombreFichero);

                if (!modoAutomatico && contador < todas.size()) {
                    System.out.print("Presione [ENTER] para siguiente o 'auto' para procesar el resto: ");
                    String comando = scanner.nextLine().trim();
                    if (comando.equalsIgnoreCase("auto")) {
                        modoAutomatico = true;
                    }
                }
            }

            System.out.println("\nProcesamiento linea a linea finalizado. Total registros: " + contador);

        } catch (IOException e) {
            System.err.println("Error durante la ejecucion linea a linea: " + e.getMessage());
        }
    }

    private static void consultarArchivosGenerados() {
        System.out.println("\n--- ARCHIVOS CSV GENERADOS ---");
        File carpeta = new File(CARPETA_SALIDA);
        if (!carpeta.exists() || !carpeta.isDirectory()) {
            System.out.println("Aun no se ha generado la carpeta de salida [" + CARPETA_SALIDA + "].");
            return;
        }

        File[] ficheros = carpeta.listFiles((dir, name) -> name.startsWith("AirTortilla_") && name.endsWith(".csv"));
        if (ficheros == null || ficheros.length == 0) {
            System.out.println("No hay ficheros de reservas en [" + CARPETA_SALIDA + "].");
            return;
        }

        System.out.println("Total de ficheros encontrados: " + ficheros.length);
        for (File f : ficheros) {
            int lineas = 0;
            try (BufferedReader br = new BufferedReader(new FileReader(f))) {
                while (br.readLine() != null) {
                    lineas++;
                }
            } catch (IOException ignored) {
            }
            System.out.println(" - " + f.getName() + " | Registros: " + Math.max(0, lineas - 1) + " | Tamano: " + f.length() + " bytes");
        }
    }

    private static void generarFicheroPruebas() {
        System.out.println("\n--- GENERADOR DE PRUEBAS ---");
        File creado = GeneradorDatosPrueba.generarCsvPrueba(FICHERO_ENTRADA_DEFECTO);
        System.out.println("Fichero listo para pruebas: " + creado.getAbsolutePath());
    }
}
