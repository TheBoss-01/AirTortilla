-- =============================================================================
-- AIRTORTILLA - SISTEMA DE GESTION DE RESERVAS AEREAS
-- ENTREGA 2: MIGRACION A BASE DE DATOS RELACIONAL Y CONTROL DE ACCESO
-- =============================================================================
-- Este archivo contiene las definiciones DDL (esquema) y las sentencias DML (consultas)
-- empleadas para satisfacer las User Stories de la Entrega 2:
--   US1: Autenticacion de empleados (Login)
--   US2: Alta de usuarios por Administrador de Sistemas
--   US3: Trazabilidad y auditoria de modificaciones del sistema
--   Migracion Relacional: Persistencia relacional de reservas aereas
-- =============================================================================

-- =============================================================================
-- SECCION 1: DEFINICION DEL ESQUEMA DDL (TABLAS E INDICES)
-- =============================================================================

-- 1.1. Tabla de Usuarios (Soporte US1 y US2)
CREATE TABLE IF NOT EXISTS usuarios (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(64) NOT NULL,
    rol VARCHAR(20) NOT NULL CHECK(rol IN ('ADMIN', 'EMPLEADO')),
    nombre_completo VARCHAR(100) NOT NULL,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    activo INTEGER NOT NULL DEFAULT 1 CHECK(activo IN (0, 1))
);

-- Indice para optimizar busquedas por nombre de usuario durante el login O(1)
CREATE UNIQUE INDEX IF NOT EXISTS idx_usuarios_username ON usuarios(username);


-- 1.2. Tabla de Reservas Relacional (Soporte Migracion por Legislacion)
CREATE TABLE IF NOT EXISTS reservas (
    id VARCHAR(50) PRIMARY KEY,
    nombre_pasajero VARCHAR(100) NOT NULL,
    origen VARCHAR(50) NOT NULL,
    destino VARCHAR(50) NOT NULL,
    codigo_pais_destino VARCHAR(2) NOT NULL,
    fecha_reserva DATE NOT NULL,
    localizador VARCHAR(50) DEFAULT 'N/A',
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indices para acelerar consultas de particionado por pais, fecha y localizadores
CREATE INDEX IF NOT EXISTS idx_reservas_pais_fecha ON reservas(codigo_pais_destino, fecha_reserva);
CREATE INDEX IF NOT EXISTS idx_reservas_localizador ON reservas(localizador);
CREATE INDEX IF NOT EXISTS idx_reservas_pasajero_ruta ON reservas(nombre_pasajero, origen, destino);


-- 1.3. Tabla de Trazabilidad y Auditoria (Soporte US3)
CREATE TABLE IF NOT EXISTS auditoria (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    usuario VARCHAR(50) NOT NULL,
    accion VARCHAR(50) NOT NULL,
    entidad VARCHAR(50) NOT NULL,
    detalles TEXT,
    ip_origen VARCHAR(45) DEFAULT '127.0.0.1',
    fecha_hora TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indice para consultas cronologicas de auditoria de seguridad
CREATE INDEX IF NOT EXISTS idx_auditoria_fecha ON auditoria(fecha_hora DESC);
CREATE INDEX IF NOT EXISTS idx_auditoria_usuario ON auditoria(usuario);


-- =============================================================================
-- SECCION 2: DATOS INICIALES SEMILLA (SEED DATA)
-- =============================================================================

-- Contrasenas en SHA-256:
-- 'admin123'    -> 240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9
-- 'empleado123' -> ccc13e8ab0819e3ab61719de4071ecae6c1d3cd35dc48b91cad3481f20922f9f

INSERT OR IGNORE INTO usuarios (username, password_hash, rol, nombre_completo, activo)
VALUES 
    ('admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'ADMIN', 'Administrador del Sistema', 1),
    ('empleado', 'ccc13e8ab0819e3ab61719de4071ecae6c1d3cd35dc48b91cad3481f20922f9f', 'EMPLEADO', 'Carlos Empleado', 1);

-- Registro de auditoria inicial
INSERT INTO auditoria (usuario, accion, entidad, detalles, ip_origen)
VALUES ('SISTEMA', 'INICIALIZACION', 'BASE_DE_DATOS', 'Esquema relacional y usuarios iniciales creados correctamente', 'LOCAL');


-- =============================================================================
-- SECCION 3: CONSULTAS DE AUTENTICACION Y SEGURIDAD (US1)
-- =============================================================================

-- 3.1. Validar credenciales de usuario durante el login
-- Parametros: ?1 = username, ?2 = password_hash
SELECT id, username, rol, nombre_completo, activo, fecha_creacion
FROM usuarios
WHERE username = ? AND password_hash = ? AND activo = 1;

-- 3.2. Obtener datos de usuario por nombre de usuario
-- Parametro: ? = username
SELECT id, username, rol, nombre_completo, activo, fecha_creacion
FROM usuarios
WHERE username = ?;


-- =============================================================================
-- SECCION 4: CONSULTAS DE GESTION DE USUARIOS (US2 - ADMINISTRADOR)
-- =============================================================================

-- 4.1. Comprobar si un nombre de usuario ya existe
-- Parametro: ? = username
SELECT COUNT(*) AS total
FROM usuarios
WHERE username = ?;

-- 4.2. Dar de alta nuevo usuario (ejecutado exclusivamente por Administrador)
-- Parametros: ?1 = username, ?2 = password_hash, ?3 = rol, ?4 = nombre_completo
INSERT INTO usuarios (username, password_hash, rol, nombre_completo, activo)
VALUES (?, ?, ?, ?, 1);

-- 4.3. Listar todos los usuarios dados de alta en el sistema
SELECT id, username, rol, nombre_completo, fecha_creacion, activo
FROM usuarios
ORDER BY fecha_creacion DESC;

-- 4.4. Desactivar un usuario (baja logica para preservar trazabilidad)
-- Parametro: ? = username
UPDATE usuarios
SET activo = 0
WHERE username = ?;


-- =============================================================================
-- SECCION 5: CONSULTAS DE TRAZABILIDAD Y AUDITORIA (US3)
-- =============================================================================

-- 5.1. Registrar evento de modificacion o acceso en la bitacora de auditoria
-- Parametros: ?1 = usuario, ?2 = accion, ?3 = entidad, ?4 = detalles, ?5 = ip_origen
INSERT INTO auditoria (usuario, accion, entidad, detalles, ip_origen, fecha_hora)
VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP);

-- 5.2. Consultar las ultimas acciones registradas para inspeccion de seguridad
-- Parametro: LIMIT N (por ejemplo 100)
SELECT id, usuario, accion, entidad, detalles, ip_origen, fecha_hora
FROM auditoria
ORDER BY id DESC
LIMIT 100;

-- 5.3. Consultar trazabilidad de un usuario especifico
-- Parametro: ? = usuario
SELECT id, usuario, accion, entidad, detalles, ip_origen, fecha_hora
FROM auditoria
WHERE usuario = ?
ORDER BY id DESC;

-- 5.4. Consultar trazabilidad por tipo de accion (ej. 'ALTA_USUARIO', 'LOGIN', 'PROCESAMIENTO')
-- Parametro: ? = accion
SELECT id, usuario, accion, entidad, detalles, ip_origen, fecha_hora
FROM auditoria
WHERE accion = ?
ORDER BY id DESC;


-- =============================================================================
-- SECCION 6: CONSULTAS DE MIGRACION Y PERSISTENCIA DE RESERVAS
-- =============================================================================

-- 6.1. Insertar reserva persistida (soporte para importacion o migracion)
-- Parametros: ?1 = id, ?2 = nombre_pasajero, ?3 = origen, ?4 = destino, 
--             ?5 = codigo_pais_destino, ?6 = fecha_reserva, ?7 = localizador
INSERT OR REPLACE INTO reservas (id, nombre_pasajero, origen, destino, codigo_pais_destino, fecha_reserva, localizador)
VALUES (?, ?, ?, ?, ?, ?, ?);

-- 6.2. Contar total de reservas persistidas en base de datos
SELECT COUNT(*) AS total_reservas FROM reservas;

-- 6.3. Listar todas las reservas ordenadas por fecha y pais (replica de MergeSort en SQL)
SELECT id, localizador, nombre_pasajero, origen, destino, codigo_pais_destino, fecha_reserva, fecha_registro
FROM reservas
ORDER BY fecha_reserva ASC, codigo_pais_destino ASC, id ASC;

-- 6.4. Consultar reservas agrupadas por pais de destino y fecha para generar particiones
SELECT codigo_pais_destino, fecha_reserva, COUNT(*) AS total_pasajeros
FROM reservas
GROUP BY codigo_pais_destino, fecha_reserva
ORDER BY fecha_reserva ASC, codigo_pais_destino ASC;

-- 6.5. Consultar pasajeros agrupados bajo un mismo localizador
-- Parametro: ? = localizador
SELECT id, nombre_pasajero, origen, destino, codigo_pais_destino, fecha_reserva
FROM reservas
WHERE localizador = ?
ORDER BY nombre_pasajero ASC;

-- 6.6. Limpiar tabla de reservas previa a una nueva migracion completa
DELETE FROM reservas;
