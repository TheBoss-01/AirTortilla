package com.airtortilla.service;

import com.airtortilla.db.AuditoriaDAO;
import com.airtortilla.db.ReservaDAO;
import com.airtortilla.io.LectorCsv;
import com.airtortilla.model.RegistroAuditoria;
import com.airtortilla.model.Reserva;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Servicio encargado de la migracion obligatoria por legislacion
 * de la infraestructura de reservas hacia la Base de Datos Relacional.
 */
public class MigracionRelacionalService {

    private final ReservaDAO reservaDAO;
    private final AuditoriaDAO auditoriaDAO;
    private final LectorCsv lectorCsv;
    private final IdentificadorService idService;

    public MigracionRelacionalService() {
        this.reservaDAO = new ReservaDAO();
        this.auditoriaDAO = new AuditoriaDAO();
        this.lectorCsv = new LectorCsv();
        this.idService = new IdentificadorService();
    }

    public MigracionRelacionalService(ReservaDAO reservaDAO, AuditoriaDAO auditoriaDAO) {
        this.reservaDAO = reservaDAO;
        this.auditoriaDAO = auditoriaDAO;
        this.lectorCsv = new LectorCsv();
        this.idService = new IdentificadorService();
    }

    /**
     * Realiza la migracion desde un fichero CSV hacia la base de datos relacional.
     *
     * @param rutaCsv    Ruta del archivo CSV original
     * @param usuario    Usuario que solicita la migracion (para trazabilidad)
     * @param ipOrigen   Origen de la peticion
     * @return Numero de reservas migradas con exito
     * @throws IOException Si ocurre un error al leer el CSV
     */
    public int migrarDesdeCsv(String rutaCsv, String usuario, String ipOrigen) throws IOException {
        File archivo = new File(rutaCsv);
        if (!archivo.exists()) {
            throw new IOException("El archivo CSV no existe en la ruta: " + rutaCsv);
        }

        List<Reserva> reservas = lectorCsv.leerTodas(rutaCsv);
        idService.procesarIdentificadoresYLocalizadores(reservas);

        int insertadas = reservaDAO.insertarLote(reservas);

        auditoriaDAO.registrar(new RegistroAuditoria(
                usuario != null ? usuario : "SISTEMA",
                "MIGRACION_BD_RELACIONAL",
                "RESERVAS",
                "Migracion de " + insertadas + " reservas desde '" + archivo.getName() + "' a la tabla relacional 'reservas'",
                ipOrigen
        ));

        return insertadas;
    }

    /**
     * Persiste una lista de reservas en memoria en la base de datos relacional.
     */
    public int migrarReservasEnMemoria(List<Reserva> reservas, String usuario, String ipOrigen) {
        if (reservas == null || reservas.isEmpty()) {
            return 0;
        }
        int insertadas = reservaDAO.insertarLote(reservas);
        auditoriaDAO.registrar(new RegistroAuditoria(
                usuario != null ? usuario : "SISTEMA",
                "PERSISTENCIA_RESERVAS",
                "RESERVAS",
                "Persistencia relacional de " + insertadas + " reservas en la base de datos",
                ipOrigen
        ));
        return insertadas;
    }

    /**
     * Obtiene todas las reservas directamente desde la base de datos relacional.
     */
    public List<Reserva> obtenerReservasRelacionales() {
        return reservaDAO.listarTodas();
    }

    public int totalReservasEnBd() {
        return reservaDAO.contar();
    }
}

