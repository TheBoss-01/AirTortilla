package com.airtortilla.db;

import com.airtortilla.model.Reserva;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos relacional para la persistencia y migracion de reservas (Legislacion AirTortilla).
 */
public class ReservaDAO {

    public boolean insertar(Reserva r) {
        String sql = "INSERT OR REPLACE INTO reservas (id, nombre_pasajero, origen, destino, codigo_pais_destino, fecha_reserva, localizador) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?);";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, r.getId());
            ps.setString(2, r.getNombrePasajero());
            ps.setString(3, r.getOrigen());
            ps.setString(4, r.getDestino());
            ps.setString(5, r.getCodigoPaisDestino());
            ps.setDate(6, r.getFechaReserva() != null ? Date.valueOf(r.getFechaReserva()) : null);
            ps.setString(7, r.getLocalizador() != null ? r.getLocalizador() : "N/A");
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al insertar reserva en BD relacional: " + e.getMessage());
            return false;
        }
    }

    public int insertarLote(List<Reserva> reservas) {
        String sql = "INSERT OR REPLACE INTO reservas (id, nombre_pasajero, origen, destino, codigo_pais_destino, fecha_reserva, localizador) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?);";
        int insertadas = 0;
        try (Connection con = ConexionBD.obtenerConexion()) {
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                for (Reserva r : reservas) {
                    ps.setString(1, r.getId());
                    ps.setString(2, r.getNombrePasajero());
                    ps.setString(3, r.getOrigen());
                    ps.setString(4, r.getDestino());
                    ps.setString(5, r.getCodigoPaisDestino());
                    ps.setDate(6, r.getFechaReserva() != null ? Date.valueOf(r.getFechaReserva()) : null);
                    ps.setString(7, r.getLocalizador() != null ? r.getLocalizador() : "N/A");
                    ps.addBatch();
                }
                int[] resultados = ps.executeBatch();
                con.commit();
                for (int res : resultados) {
                    if (res >= 0 || res == Statement.SUCCESS_NO_INFO) {
                        insertadas++;
                    }
                }
            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("Error al insertar lote de reservas en BD: " + e.getMessage());
        }
        return insertadas;
    }

    public List<Reserva> listarTodas() {
        List<Reserva> lista = new ArrayList<>();
        String sql = "SELECT id, localizador, nombre_pasajero, origen, destino, codigo_pais_destino, fecha_reserva " +
                "FROM reservas ORDER BY fecha_reserva ASC, codigo_pais_destino ASC, id ASC;";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String id = rs.getString("id");
                String localizador = rs.getString("localizador");
                String nombre = rs.getString("nombre_pasajero");
                String origen = rs.getString("origen");
                String destino = rs.getString("destino");
                String pais = rs.getString("codigo_pais_destino");
                Date d = rs.getDate("fecha_reserva");
                LocalDate fecha = d != null ? d.toLocalDate() : null;
                lista.add(new Reserva(id, localizador, nombre, origen, destino, pais, fecha));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar reservas desde BD: " + e.getMessage());
        }
        return lista;
    }

    public int contar() {
        String sql = "SELECT COUNT(*) FROM reservas;";
        try (Connection con = ConexionBD.obtenerConexion();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error al contar reservas en BD: " + e.getMessage());
        }
        return 0;
    }

    public boolean limpiar() {
        String sql = "DELETE FROM reservas;";
        try (Connection con = ConexionBD.obtenerConexion();
             Statement stmt = con.createStatement()) {
            stmt.executeUpdate(sql);
            return true;
        } catch (SQLException e) {
            System.err.println("Error al vaciar tabla de reservas: " + e.getMessage());
            return false;
        }
    }
}

