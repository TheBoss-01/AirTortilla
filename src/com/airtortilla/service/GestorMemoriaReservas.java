package com.airtortilla.service;

import com.airtortilla.algoritmos.OrdenadorMergeSort;
import com.airtortilla.model.Reserva;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class GestorMemoriaReservas {

    private final List<Reserva> reservasEnMemoria;

    public GestorMemoriaReservas() {
        this.reservasEnMemoria = new ArrayList<>();
    }

    public GestorMemoriaReservas(List<Reserva> reservasIniciales) {
        this.reservasEnMemoria = new ArrayList<>();
        if (reservasIniciales != null) {
            this.reservasEnMemoria.addAll(reservasIniciales);
        }
    }

    public void agregarReserva(Reserva reserva) {
        if (reserva != null) {
            this.reservasEnMemoria.add(reserva);
        }
    }

    public void agregarTodas(List<Reserva> reservas) {
        if (reservas != null) {
            this.reservasEnMemoria.addAll(reservas);
        }
    }

    public List<Reserva> obtenerTodas() {
        return Collections.unmodifiableList(this.reservasEnMemoria);
    }

    public int cantidadReservas() {
        return this.reservasEnMemoria.size();
    }

    public void limpiarMemoria() {
        this.reservasEnMemoria.clear();
    }

    public void ordenarPorCriterioNatural() {
        List<Reserva> ordenadas = OrdenadorMergeSort.ordenar(this.reservasEnMemoria);
        this.reservasEnMemoria.clear();
        this.reservasEnMemoria.addAll(ordenadas);
    }

    public void ordenarPorComparador(Comparator<Reserva> comparador) {
        List<Reserva> ordenadas = OrdenadorMergeSort.ordenar(this.reservasEnMemoria, comparador);
        this.reservasEnMemoria.clear();
        this.reservasEnMemoria.addAll(ordenadas);
    }
}
