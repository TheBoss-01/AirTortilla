package com.airtortilla.model;

/**
 * Roles de usuario disponibles en el sistema AirTortilla.
 */
public enum RolUsuario {
    ADMIN,
    EMPLEADO;

    public static RolUsuario desdeTexto(String texto) {
        if (texto == null) {
            return EMPLEADO;
        }
        try {
            return RolUsuario.valueOf(texto.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return EMPLEADO;
        }
    }
}

