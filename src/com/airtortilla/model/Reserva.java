package com.airtortilla.model;

import java.time.LocalDate;
import java.util.Objects;

public class Reserva implements Comparable<Reserva> {
    private String id;
    private String localizador;
    private String nombrePasajero;
    private String origen;
    private String destino;
    private String codigoPaisDestino;
    private LocalDate fechaReserva;

    public Reserva() {
    }

    public Reserva(String id, String localizador, String nombrePasajero, String origen, String destino, String codigoPaisDestino, LocalDate fechaReserva) {
        this.id = id;
        this.localizador = localizador;
        this.nombrePasajero = nombrePasajero;
        this.origen = origen;
        this.destino = destino;
        this.codigoPaisDestino = codigoPaisDestino;
        this.fechaReserva = fechaReserva;
    }

    public Reserva(String nombrePasajero, String origen, String destino, String codigoPaisDestino, LocalDate fechaReserva) {
        this.nombrePasajero = nombrePasajero;
        this.origen = origen;
        this.destino = destino;
        this.codigoPaisDestino = codigoPaisDestino;
        this.fechaReserva = fechaReserva;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLocalizador() {
        return localizador;
    }

    public void setLocalizador(String localizador) {
        this.localizador = localizador;
    }

    public String getNombrePasajero() {
        return nombrePasajero;
    }

    public void setNombrePasajero(String nombrePasajero) {
        this.nombrePasajero = nombrePasajero;
    }

    public String getOrigen() {
        return origen;
    }

    public void setOrigen(String origen) {
        this.origen = origen;
    }

    public String getDestino() {
        return destino;
    }

    public void setDestino(String destino) {
        this.destino = destino;
    }

    public String getCodigoPaisDestino() {
        return codigoPaisDestino;
    }

    public void setCodigoPaisDestino(String codigoPaisDestino) {
        this.codigoPaisDestino = codigoPaisDestino;
    }

    public LocalDate getFechaReserva() {
        return fechaReserva;
    }

    public void setFechaReserva(LocalDate fechaReserva) {
        this.fechaReserva = fechaReserva;
    }

    public String generarClaveCoincidencia() {
        String nombre = this.nombrePasajero != null ? this.nombrePasajero.trim().toLowerCase() : "";
        String orig = this.origen != null ? this.origen.trim().toLowerCase() : "";
        String dest = this.destino != null ? this.destino.trim().toLowerCase() : "";
        return nombre + "|" + orig + "|" + dest;
    }

    @Override
    public int compareTo(Reserva otra) {
        if (otra == null) {
            return 1;
        }
        int cmpFecha = 0;
        if (this.fechaReserva != null && otra.fechaReserva != null) {
            cmpFecha = this.fechaReserva.compareTo(otra.fechaReserva);
        } else if (this.fechaReserva != null) {
            cmpFecha = 1;
        } else if (otra.fechaReserva != null) {
            cmpFecha = -1;
        }
        if (cmpFecha != 0) {
            return cmpFecha;
        }
        int cmpPais = 0;
        if (this.codigoPaisDestino != null && otra.codigoPaisDestino != null) {
            cmpPais = this.codigoPaisDestino.compareToIgnoreCase(otra.codigoPaisDestino);
        }
        if (cmpPais != 0) {
            return cmpPais;
        }
        if (this.id != null && otra.id != null) {
            return this.id.compareTo(otra.id);
        }
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reserva reserva = (Reserva) o;
        return Objects.equals(id, reserva.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Reserva{" +
                "id='" + id + '\'' +
                ", localizador='" + localizador + '\'' +
                ", nombrePasajero='" + nombrePasajero + '\'' +
                ", origen='" + origen + '\'' +
                ", destino='" + destino + '\'' +
                ", codigoPaisDestino='" + codigoPaisDestino + '\'' +
                ", fechaReserva=" + fechaReserva +
                '}';
    }
}
