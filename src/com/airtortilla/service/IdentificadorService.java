package com.airtortilla.service;

import com.airtortilla.model.Reserva;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class IdentificadorService {

    public String generarIdUnico() {
        return "RES-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    public String generarCodigoLocalizador() {
        return "LOC-" + UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
    }

    public void procesarIdentificadoresYLocalizadores(List<Reserva> reservas) {
        if (reservas == null || reservas.isEmpty()) {
            return;
        }

        for (Reserva r : reservas) {
            if (r.getId() == null || r.getId().trim().isEmpty()) {
                r.setId(generarIdUnico());
            }
        }

        Map<String, List<Reserva>> grupos = new HashMap<>();
        for (Reserva r : reservas) {
            String clave = r.generarClaveCoincidencia();
            grupos.computeIfAbsent(clave, k -> new ArrayList<>()).add(r);
        }

        for (Map.Entry<String, List<Reserva>> entrada : grupos.entrySet()) {
            List<Reserva> listaGrupo = entrada.getValue();
            if (listaGrupo.size() >= 2) {
                String localizador = generarCodigoLocalizador();
                for (Reserva r : listaGrupo) {
                    r.setLocalizador(localizador);
                }
            } else {
                for (Reserva r : listaGrupo) {
                    if (r.getLocalizador() == null || r.getLocalizador().trim().isEmpty()) {
                        r.setLocalizador("N/A");
                    }
                }
            }
        }
    }

    public String obtenerOGenerarLocalizador(String claveCoincidencia, Map<String, String> cacheLocalizadores) {
        return cacheLocalizadores.computeIfAbsent(claveCoincidencia, k -> generarCodigoLocalizador());
    }
}
