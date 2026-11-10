package com.airtortilla;

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
import com.airtortilla.web.ServidorWeb;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {

    private static final String FICHERO_ENTRADA_DEFECTO = "AirTortilla_Reservas_2020.csv";
    private static final String CARPETA_SALIDA = "salida";
    private static final int PUERTO_WEB = 8080;

    public static void main(String[] args) {
        ServidorWeb servidorWeb = new ServidorWeb(PUERTO_WEB);
        try {
            servidorWeb.iniciar();
            System.out.println("Servidor web iniciado en: http://localhost:" + PUERTO_WEB);
            abrirNavegador("http://localhost:" + PUERTO_WEB);
        } catch (IOException e) {
            System.err.println("No se pudo iniciar el servidor web: " + e.getMessage());
        }

        Scanner scanner = new Scanner(System.in);
        LectorCsv lectorCsv = new LectorCsv();
        EscritorCsv escritorCsv = new EscritorCsv();
        IdentificadorService identificadorService = new IdentificadorService();
        AutenticacionService authService = servidorWeb.getAuthService();
        AuditoriaService auditoriaService = servidorWeb.getAuditoriaService();
        MigracionRelacionalService migracionService = servidorWeb.getMigracionService();

        boolean salir = false;
        while (!salir) {
            mostrarMenuPrincipal(authService);
            String opcion = scanner.nextLine().trim();

            switch (opcion) {
                case "1":
                    ejecutarModalidadTotal(scanner, lectorCsv, escritorCsv, identificadorService, migracionService, authService);
                    break;
                case "2":
                    ejecutarModalidadLineaALinea(scanner, lectorCsv, escritorCsv, identificadorService, migracionService, authService);
                    break;
                case "3":
                    consultarArchivosGenerados();
                    break;
                case "4":
                    generarFicheroPruebas(auditoriaService, authService);
                    break;
                case "5":
                    gestionarSesionUsuario(scanner, authService);
                    break;
                case "6":
                    darDeAltaUsuarioCli(scanner, authService);
                    break;
                case "7":
                    consultarAuditoriaCli(auditoriaService, authService);
                    break;
                case "8":
                    sincronizarBdRelacionalCli(scanner, migracionService, authService);
                    break;
                case "9":
                    abrirNavegador("http://localhost:" + PUERTO_WEB);
                    break;
                case "10":
                    salir = true;
                    servidorWeb.detener();
                    System.out.println("\nGracias por utilizar el sistema AirTortilla. Finalizando aplicacion.");
                    break;
                default:
                    System.out.println("\nOpcion no valida. Seleccione una opcion del 1 al 10.");
                    break;
            }
        }
        scanner.close();
    }

    private static void abrirNavegador(String url) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
            }
        } catch (Exception ignored) {
        }
    }

    private static void mostrarMenuPrincipal(AutenticacionService auth) {
        Usuario u = auth.getUsuarioActual();
        String estadoUsuario = u != null ? u.getUsername() + " [" + u.getRol() + "]" : "No autenticado (Invitado)";

        System.out.println("\n=======================================================");
        System.out.println("          SISTEMA DE RESERVAS AIRTORTILLA              ");
        System.out.println("  [WEB] Aplicacion activa en: http://localhost:" + PUERTO_WEB);
        System.out.println("  [USUARIO]: " + estadoUsuario);
        System.out.println("=======================================================");
        System.out.println("--- GESTION Y PROCESAMIENTO DE RESERVAS ---");
        System.out.println("1. Ejecutar procesamiento en MODALIDAD TOTAL");
        System.out.println("2. Ejecutar procesamiento en MODALIDAD LINEA A LINEA");
        System.out.println("3. Consultar archivos generados (CSV)");
        System.out.println("4. Generar/Restaurar datos de prueba CSV");
        System.out.println("--- ENTREGA 2: SEGURIDAD, USUARIOS Y BD RELACIONAL ---");
        System.out.println("5. Iniciar / Cerrar sesión (US1)");
        System.out.println("6. Dar de alta nuevo usuario (US2 - Solo Admin)");
        System.out.println("7. Consultar trazabilidad y auditoria (US3 - Solo Admin)");
        System.out.println("8. Migrar/Sincronizar reservas con BD Relacional");
        System.out.println("--- OPCIONES GENERALES ---");
        System.out.println("9. Abrir interfaz WEB en el navegador");
        System.out.println("10. Salir");
        System.out.print("Seleccione una opcion (1-10): ");
    }

    private static void ejecutarModalidadTotal(Scanner scanner, LectorCsv lector, EscritorCsv escritor,
                                              IdentificadorService idService, MigracionRelacionalService migracion,
                                              AutenticacionService auth) {
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
            System.out.println("\n[1/5] Leyendo reservas desde CSV...");
            List<Reserva> reservas = lector.leerTodas(ruta);
            System.out.println("      Se han cargado " + reservas.size() + " reservas en memoria.");

            System.out.println("[2/5] Generando identificadores y asignando localizadores coincidentes O(n)...");
            idService.procesarIdentificadoresYLocalizadores(reservas);

            Map<String, Integer> conteoLocalizadores = new HashMap<>();
            for (Reserva r : reservas) {
                if (r.getLocalizador() != null && !r.getLocalizador().equals("N/A")) {
                    conteoLocalizadores.put(r.getLocalizador(), conteoLocalizadores.getOrDefault(r.getLocalizador(), 0) + 1);
                }
            }

            System.out.println("[3/5] Ordenando reservas mediante MergeSort O(n log n)...");
            List<Reserva> reservasOrdenadas = OrdenadorMergeSort.ordenar(reservas);

            System.out.println("[4/5] Particionando y exportando ficheros por pais y fecha...");
            List<File> ficherosCreados = escritor.exportarReservasPorPaisYFecha(reservasOrdenadas, CARPETA_SALIDA);

            System.out.println("[5/5] Persistiendo reservas en Base de Datos Relacional (SQLite)...");
            String usuario = auth.estaAutenticado() ? auth.getUsuarioActual().getUsername() : "TERMINAL";
            migracion.migrarReservasEnMemoria(reservasOrdenadas, usuario, "TERMINAL");

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
        System.out.println("Base de datos relacional: Actualizada y sincronizada.");
        System.out.println("=======================================================");
    }

    private static void ejecutarModalidadLineaALinea(Scanner scanner, LectorCsv lector, EscritorCsv escritor,
                                                    IdentificadorService idService, MigracionRelacionalService migracion,
                                                    AutenticacionService auth) {
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
            System.out.println("Presione [ENTER] para avanzar registro a registro (o escriba 'auto' para procesar el resto):");

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

            String usuario = auth.estaAutenticado() ? auth.getUsuarioActual().getUsername() : "TERMINAL";
            migracion.migrarReservasEnMemoria(todas, usuario, "TERMINAL");
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

    private static void generarFicheroPruebas(AuditoriaService auditoria, AutenticacionService auth) {
        System.out.println("\n--- GENERADOR DE PRUEBAS ---");
        File creado = GeneradorDatosPrueba.generarCsvPrueba(FICHERO_ENTRADA_DEFECTO);
        String usuario = auth.estaAutenticado() ? auth.getUsuarioActual().getUsername() : "TERMINAL";
        auditoria.registrarEvento(usuario, "GENERAR_CSV_PRUEBAS", "FICHERO", "Generado archivo de prueba CSV", "TERMINAL");
        System.out.println("Fichero listo para pruebas: " + creado.getAbsolutePath());
    }

    // =========================================================================
    // METODOS ENTREGA 2 EN CLI
    // =========================================================================

    private static void gestionarSesionUsuario(Scanner scanner, AutenticacionService auth) {
        System.out.println("\n--- CONTROL DE ACCESO Y AUTENTICACION (US1) ---");
        if (auth.estaAutenticado()) {
            Usuario u = auth.getUsuarioActual();
            System.out.println("Usuario activo: " + u.getNombreCompleto() + " (" + u.getUsername() + ") | Rol: " + u.getRol());
            System.out.print("¿Desea cerrar la sesion actual? (s/n): ");
            String resp = scanner.nextLine().trim();
            if (resp.equalsIgnoreCase("s")) {
                auth.cerrarSesion("TERMINAL");
                System.out.println("Sesion cerrada correctamente.");
            }
            return;
        }

        System.out.print("Ingrese nombre de usuario: ");
        String username = scanner.nextLine().trim();
        System.out.print("Ingrese contrasena: ");
        String pass = scanner.nextLine().trim();

        Usuario u = auth.iniciarSesion(username, pass, "TERMINAL");
        if (u != null) {
            System.out.println("\n[LOGIN EXITOSO] Bienvenido, " + u.getNombreCompleto() + " (Rol: " + u.getRol() + ")");
        } else {
            System.out.println("\n[ERROR] Credenciales incorrectas.");
        }
    }

    private static void darDeAltaUsuarioCli(Scanner scanner, AutenticacionService auth) {
        System.out.println("\n--- ALTA DE USUARIOS (US2 - ADMINISTRADOR) ---");
        if (!auth.estaAutenticado() || !auth.getUsuarioActual().esAdmin()) {
            System.out.println("[ACCESO DENEGADO] Se requieren permisos de Administrador de Sistemas para dar de alta usuarios.");
            System.out.println("Inicie sesion desde la opcion 5 con el usuario 'admin'.");
            return;
        }

        System.out.print("Nuevo nombre de usuario (username): ");
        String username = scanner.nextLine().trim();
        System.out.print("Contrasena (minimo 4 caracteres): ");
        String pass = scanner.nextLine().trim();
        System.out.print("Rol (1: EMPLEADO, 2: ADMIN) [1]: ");
        String rolOpt = scanner.nextLine().trim();
        RolUsuario rol = rolOpt.equals("2") ? RolUsuario.ADMIN : RolUsuario.EMPLEADO;
        System.out.print("Nombre completo: ");
        String nombre = scanner.nextLine().trim();

        try {
            boolean creado = auth.darDeAltaUsuario(username, pass, rol, nombre, "TERMINAL");
            if (creado) {
                System.out.println("[EXITO] Usuario '" + username + "' registrado con rol " + rol + " en la BD relacional.");
            }
        } catch (Exception e) {
            System.err.println("[ERROR] " + e.getMessage());
        }
    }

    private static void consultarAuditoriaCli(AuditoriaService auditoria, AutenticacionService auth) {
        System.out.println("\n--- TRAZABILIDAD Y AUDITORIA (US3 - ADMINISTRADOR) ---");
        if (!auth.estaAutenticado() || !auth.getUsuarioActual().esAdmin()) {
            System.out.println("[ACCESO DENEGADO] Solo el Administrador de Sistemas puede consultar la trazabilidad.");
            System.out.println("Inicie sesion desde la opcion 5 con el usuario 'admin'.");
            return;
        }

        List<RegistroAuditoria> logs = auditoria.obtenerUltimosEventos(20);
        System.out.println("Ultimos eventos registrados en la bitacora relacional:");
        for (RegistroAuditoria log : logs) {
            System.out.println(" - [" + log.getFechaHora() + "] (" + log.getUsuario() + ") "
                    + log.getAccion() + " -> " + log.getDetalles() + " [" + log.getIpOrigen() + "]");
        }
    }

    private static void sincronizarBdRelacionalCli(Scanner scanner, MigracionRelacionalService migracion, AutenticacionService auth) {
        System.out.println("\n--- MIGRACION A BASE DE DATOS RELACIONAL (LEGISLACION) ---");
        System.out.print("Ingrese ruta del CSV a migrar [" + FICHERO_ENTRADA_DEFECTO + "]: ");
        String ruta = scanner.nextLine().trim();
        if (ruta.isEmpty()) {
            ruta = FICHERO_ENTRADA_DEFECTO;
        }

        try {
            String usuario = auth.estaAutenticado() ? auth.getUsuarioActual().getUsername() : "TERMINAL";
            int total = migracion.migrarDesdeCsv(ruta, usuario, "TERMINAL");
            System.out.println("[EXITO] Migradas " + total + " reservas hacia la base de datos relacional SQLite.");
            System.out.println("Total de registros almacenados en tabla 'reservas': " + migracion.totalReservasEnBd());
        } catch (Exception e) {
            System.err.println("[ERROR] No se pudo completar la migracion: " + e.getMessage());
        }
    }
}
