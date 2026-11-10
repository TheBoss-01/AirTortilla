package com.airtortilla.test;

import com.airtortilla.model.RegistroAuditoria;
import com.airtortilla.model.Reserva;
import com.airtortilla.model.RolUsuario;
import com.airtortilla.model.Usuario;
import com.airtortilla.service.AuditoriaService;
import com.airtortilla.service.AutenticacionService;
import com.airtortilla.service.MigracionRelacionalService;

import java.util.List;

/**
 * Script de validacion automatizada de requisitos de la Entrega 2.
 */
public class PruebasEntrega2 {

    public static void main(String[] args) {
        System.out.println("=== VALIDACION DE REQUISITOS ENTREGA 2 ===");

        AutenticacionService authService = new AutenticacionService();
        AuditoriaService auditoriaService = new AuditoriaService();
        MigracionRelacionalService migracionService = new MigracionRelacionalService();

        // Test 1: US1 - Login de empleado y admin
        System.out.println("\n[1] Probando US1 - Autenticacion de usuarios...");
        Usuario admin = authService.iniciarSesion("admin", "admin123", "TEST-IP");
        if (admin == null || admin.getRol() != RolUsuario.ADMIN) {
            throw new AssertionError("Fallo en login de admin");
        }
        System.out.println("  -> Login ADMIN correcto: " + admin.getUsername() + " (" + admin.getRol() + ")");

        Usuario emp = authService.iniciarSesion("empleado", "empleado123", "TEST-IP");
        if (emp == null || emp.getRol() != RolUsuario.EMPLEADO) {
            throw new AssertionError("Fallo en login de empleado");
        }
        System.out.println("  -> Login EMPLEADO correcto: " + emp.getUsername() + " (" + emp.getRol() + ")");

        // Test 2: US2 - Alta de usuarios (Seguridad: solo admin)
        System.out.println("\n[2] Probando US2 - Alta de usuarios...");
        // Intentar dar de alta siendo empleado (debe fallar)
        authService.setUsuarioActual(emp);
        try {
            authService.darDeAltaUsuario("hacker", "hack123", RolUsuario.EMPLEADO, "Hacker", "TEST-IP");
            throw new AssertionError("Seguridad vulnerada: un empleado pudo dar de alta a otro usuario");
        } catch (SecurityException e) {
            System.out.println("  -> Control de acceso verificado: " + e.getMessage());
        }

        // Dar de alta siendo admin (debe funcionar)
        authService.setUsuarioActual(admin);
        String testUser = "operador_" + System.currentTimeMillis();
        boolean creado = authService.darDeAltaUsuario(testUser, "op1234", RolUsuario.EMPLEADO, "Operador Test", "TEST-IP");
        if (!creado) {
            throw new AssertionError("Fallo al dar de alta usuario siendo admin");
        }
        System.out.println("  -> Alta de usuario siendo admin exitosa: " + testUser);

        // Test 3: US3 - Trazabilidad y auditoria
        System.out.println("\n[3] Probando US3 - Trazabilidad de modificaciones...");
        List<RegistroAuditoria> logs = auditoriaService.obtenerUltimosEventos(10);
        if (logs.isEmpty()) {
            throw new AssertionError("La bitacora de auditoria esta vacia");
        }
        System.out.println("  -> Total de eventos de trazabilidad registrados recientemente: " + logs.size());
        for (RegistroAuditoria log : logs) {
            System.out.println("     * [" + log.getFechaHora() + "] (" + log.getUsuario() + ") " + log.getAccion() + " -> " + log.getDetalles());
        }

        // Test 4: Migracion a Base de Datos Relacional
        System.out.println("\n[4] Probando Migracion Relacional...");
        try {
            int migradas = migracionService.migrarDesdeCsv("AirTortilla_Reservas_2020.csv", "admin", "TEST-IP");
            System.out.println("  -> Reservas migradas a SQLite con exito: " + migradas);
            List<Reserva> enBd = migracionService.obtenerReservasRelacionales();
            System.out.println("  -> Reservas consultadas directamente desde BD: " + enBd.size());
            if (enBd.isEmpty()) {
                throw new AssertionError("No se encontraron reservas en la BD relacional");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error en migracion relacional", e);
        }

        System.out.println("\n=== TODAS LAS PRUEBAS DE LA ENTREGA 2 HAN PASADO CON EXITO ===");
    }
}

