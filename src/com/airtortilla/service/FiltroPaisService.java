package com.airtortilla.service;

import com.airtortilla.model.Reserva;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FiltroPaisService {

    public List<Reserva> filtrarPorPaisDestino(List<Reserva> reservas, String codigoIsoPais) {
        if (reservas == null || codigoIsoPais == null) {
            return new ArrayList<>();
        }
        String codigoLimpio = codigoIsoPais.trim().toUpperCase();
        List<Reserva> resultado = new ArrayList<>();
        for (Reserva r : reservas) {
            if (r != null && r.getCodigoPaisDestino() != null) {
                if (r.getCodigoPaisDestino().trim().equalsIgnoreCase(codigoLimpio)) {
                    resultado.add(r);
                }
            }
        }
        return resultado;
    }

    public Set<String> obtenerPaisesDestinoUnicos(List<Reserva> reservas) {
        if (reservas == null || reservas.isEmpty()) {
            return Collections.emptySet();
        }
        Set<String> paises = new HashSet<>();
        for (Reserva r : reservas) {
            if (r != null && r.getCodigoPaisDestino() != null && !r.getCodigoPaisDestino().trim().isEmpty()) {
                paises.add(r.getCodigoPaisDestino().trim().toUpperCase());
            }
        }
        return paises;
    }

    public Map<String, List<Reserva>> agruparPorPaisDestino(List<Reserva> reservas) {
        Map<String, List<Reserva>> agrupacion = new HashMap<>();
        if (reservas == null) {
            return agrupacion;
        }
        for (Reserva r : reservas) {
            if (r != null && r.getCodigoPaisDestino() != null && !r.getCodigoPaisDestino().trim().isEmpty()) {
                String pais = r.getCodigoPaisDestino().trim().toUpperCase();
                agrupacion.computeIfAbsent(pais, k -> new ArrayList<>()).add(r);
            }
        }
        return agrupacion;
    }

    public Map<String, List<Reserva>> agruparPorPaisYFecha(List<Reserva> reservas) {
        Map<String, List<Reserva>> agrupacion = new HashMap<>();
        if (reservas == null) {
            return agrupacion;
        }
        for (Reserva r : reservas) {
            if (r != null && r.getCodigoPaisDestino() != null && r.getFechaReserva() != null) {
                String pais = r.getCodigoPaisDestino().trim().toUpperCase();
                String fechaFormato = r.getFechaReserva().toString().replace("-", "_");
                String clave = pais + "_" + fechaFormato;
                agrupacion.computeIfAbsent(clave, k -> new ArrayList<>()).add(r);
            }
        }
        return agrupacion;
    }
}
