# Guía Explicativa del Proyecto AirTortilla
### Asignatura: Acceso a Datos (2º DAM)

Esta guía contiene la explicación técnica, funcional y de arquitectura del proyecto para comprender su diseño en profundidad, defenderlo con solidez ante el profesor y responder a cualquier pregunta técnica sobre algoritmia, persistencia relacional, seguridad y control de acceso.

---

## 1. Objetivo General y Evolución del Proyecto

AirTortilla es una solución integral para la gestión, clasificación, persistencia y auditoría de reservas aéreas desarrollada para la compañía aérea AirTortilla. El proyecto se ha estructurado en entregas incrementales y escalables:

### Entrega 1: Procesamiento en Memoria y Algoritmia Eficiente
1. **Lectura y parseo CSV:** Carga desacoplada de registros en memoria sin dependencias externas (`Reserva`).
2. **Identificación unívoca:** Asignación de ID obligatorio por reserva (`RES-XXXXXXXX`).
3. **Agrupación por localizador:** Asignación de localizador compartido (`LOC-XXXXX`) a pasajeros con mismo **Nombre + Origen + Destino** en tiempo lineal $O(n)$.
4. **Ordenación garantizada:** Implementación de **MergeSort** garantizando complejidad $O(n \log n)$.
5. **Particionado y exportación:** Generación de ficheros `AirTortilla_XX_YYYY_MM_DD.csv` en la carpeta `salida/`.
6. **Interfaz dual:** Modalidad Total y Modalidad Línea a Línea (Web interactiva y Terminal).

### Entrega 2: Migración Relacional, Seguridad y Trazabilidad
1. **Cumplimiento legislativo de persistencia relacional:** Migración de la infraestructura de datos a una Base de Datos Relacional (SQLite vía JDBC estándar) con tabla `reservas`, claves primarias e índices.
2. **US1 (Empleado):** Control de acceso e inicio de sesión seguro (Login) con contraseñas protegidas mediante hash criptográfico SHA-256.
3. **US2 (Administrador de Sistemas):** Alta y administración de usuarios del sistema con validación de unicidad de username y restricción estricta por roles (RBAC: `ADMIN` y `EMPLEADO`).
4. **US3 (Administrador de Sistemas):** Bitácora inmutable de trazabilidad y auditoría de seguridad para registrar quién, qué, cuándo y desde dónde se realizan modificaciones en el sistema.
5. **Archivo centralizado de consultas SQL:** Generación del archivo [`sql/consultas.sql`](file:///sql/consultas.sql) con todo el esquema DDL y sentencias DML parametrizadas.
6. **Contenedores virtuales:** Confección de `Dockerfile` multi-stage y `docker-compose.yml` para facilitar despliegues desacoplados y portables.

---

## 2. Mapa Completo de Clases y Arquitectura

El proyecto sigue una arquitectura por capas desacoplada, escalable y mantenible:

```text
AirTortilla/
├── src/
│   └── com/airtortilla/
│       ├── Main.java                         -> Punto de entrada CLI, orquesta servidor web y terminal
│       ├── algoritmos/
│       │   └── OrdenadorMergeSort.java       -> Algoritmo MergeSort O(n log n) Divide y Vencerás
│       ├── db/
│       │   ├── ConexionBD.java               -> Conexión JDBC SQLite Singleton con auto-inicialización DDL
│       │   ├── UsuarioDAO.java               -> Acceso a datos relacional para usuarios (US1 y US2)
│       │   ├── AuditoriaDAO.java             -> Inserción y consulta inmutable de bitácora (US3)
│       │   └── ReservaDAO.java               -> Persistencia relacional de reservas por lotes (Batch)
│       ├── io/
│       │   ├── LectorCsv.java                -> Lectura segura y parseo del archivo CSV
│       │   └── EscritorCsv.java              -> Escritura particionada AirTortilla_XX_YYYY_MM_DD.csv
│       ├── model/
│       │   ├── Reserva.java                  -> Entidad de dominio (implementa Comparable<Reserva>)
│       │   ├── RolUsuario.java               -> Enumeración de roles (ADMIN, EMPLEADO)
│       │   ├── Usuario.java                  -> Entidad de usuario autenticado
│       │   └── RegistroAuditoria.java        -> Entidad de evento de trazabilidad y seguridad
│       ├── service/
│       │   ├── IdentificadorService.java     -> Generación de IDs y localizadores con Hash O(n)
│       │   ├── FiltroPaisService.java        -> Agrupación por país ISO y fecha en O(n)
│       │   ├── GestorMemoriaReservas.java    -> Repositorio de objetos en memoria
│       │   ├── AutenticacionService.java     -> Lógica de login, hash SHA-256, sesiones y RBAC
│       │   ├── AuditoriaService.java         -> Fachada para registro uniforme de trazabilidad
│       │   └── MigracionRelacionalService.java -> Sincronización masiva de reservas CSV a SQLite
│       ├── test/
│       │   ├── GeneradorDatosPrueba.java     -> Generador del CSV de prueba (casos 2, 3 y 5 coincidentes)
│       │   └── PruebasEntrega2.java          -> Suite automatizada de pruebas para US1, US2, US3 y BD
│       └── web/
│           └── ServidorWeb.java              -> Servidor HTTP nativo Java (puerto 8080) con API REST
├── lib/                                      -> Conectores JDBC y dependencias de ejecución
│   ├── sqlite-jdbc-3.46.1.0.jar              -> Driver oficial SQLite JDBC
│   ├── slf4j-api-2.0.13.jar                  -> Fachada de logging para JDBC
│   └── slf4j-nop-2.0.13.jar                  -> Logger silencioso de alto rendimiento
├── sql/
│   └── consultas.sql                         -> Archivo SQL con DDL, DML, índices y consultas del negocio
├── web/                                      -> Frontend interactivo SPA (Single Page Application)
│   ├── index.html                            -> Dashboard con Glassmorphism y 4 pestañas de trabajo
│   ├── estilos.css                           -> Hoja de estilos moderna y responsive
│   └── app.js                                -> Lógica cliente para reservas, usuarios, auditoría y BD
├── Dockerfile                                -> Empaquetado portable multi-stage (JDK 21 + JRE alpine)
├── docker-compose.yml                        -> Orquestación de contenedor virtual en un solo comando
├── README.md                                 -> Documentación del repositorio con desglose de tareas
└── GUIA_EXPLICATIVA.md                       -> Guía técnica para defensa y preguntas de examen
```

---

## 3. Justificación de Complejidad Algorítmica y Rendimiento

Este apartado es fundamental para la defensa ante el profesor de Acceso a Datos:

### A. Detección de Pasajeros Coincidentes en $O(n)$
- **Enfoque ingenuo prohibido ($O(n^2)$):** Dos bucles `for` anidados comparando cada registro contra todos los demás. Para 10.000 reservas supondría $10^8$ iteraciones.
- **Solución implementada ($O(n)$):** En [`IdentificadorService.java`](file:///src/com/airtortilla/service/IdentificadorService.java), se calcula una clave hash normalizada: `nombre|origen|destino`. Se utiliza un `HashMap<String, List<Reserva>>`.
  - Inserción y búsqueda en tabla hash: coste amortizado $O(1)$.
  - Agrupación total de $n$ elementos: $n \times O(1) = O(n)$.
  - Asignación de localizadores a los grupos: $O(n)$.
  - **Coste global:** estrictamente lineal $O(n)$.

### B. Algoritmo MergeSort en $O(n \log n)$
- En [`OrdenadorMergeSort.java`](file:///src/com/airtortilla/algoritmos/OrdenadorMergeSort.java) se aplica el paradigma **Divide y Vencerás**:
  1. **División recursiva:** Divide la colección a la mitad de forma sucesiva. La profundidad del árbol de recursión es $\log_2(n)$.
  2. **Fase de mezcla (Merge):** Fusiona dos listas ordenadas comparando sus elementos frontales en tiempo lineal $O(n)$ por nivel del árbol.
  3. **Coste temporal:** $n \times \log_2(n) \rightarrow O(n \log n)$ tanto en el mejor, como en el caso medio y peor caso.
  4. **Criterio de ordenación:** Implementado en `Reserva.compareTo(Reserva otra)`: compara primero por `fechaReserva`, luego por `codigoPaisDestino` y finalmente por `id`.

### C. Persistencia Relacional Eficiente con Lotes JDBC (`Batching`)
- Al migrar reservas a la base de datos relacional, realizar $n$ sentencias `INSERT` individuales generaría $n$ viajes de red/bloqueos de disco y $n$ transacciones implícitas ($O(n)$ escrituras físicas lentas).
- En [`ReservaDAO.java`](file:///src/com/airtortilla/db/ReservaDAO.java), se desactiva el autocommit (`con.setAutoCommit(false)`), se acumulan las sentencias con `ps.addBatch()` y se ejecutan en un único lote con `ps.executeBatch()` bajo una sola transacción atómica, reduciendo el coste de I/O en más de un 95%.

---

## 4. Implementación Detallada de las User Stories (Entrega 2)

### US1: Autenticación de Empleados (Login)
- **Objetivo:** *"Como Empleado Quiero Poder hacer log in para acceder a los recursos de la compañía"*.
- **Implementación técnica:**
  - Tabla `usuarios` con columnas `username`, `password_hash`, `rol`, `nombre_completo`, `fecha_creacion`, `activo`.
  - Las contraseñas se almacenan procesadas mediante **SHA-256** utilizando [`java.security.MessageDigest`](file:///src/com/airtortilla/service/AutenticacionService.java).
  - El endpoint `POST /api/auth/login` valida credenciales mediante consultas preparadas (`PreparedStatement`) inmunes a SQL Injection.
  - En caso de éxito, se establece la sesión activa y se registra un evento `LOGIN_EXITOSO` en la auditoría; en caso contrario, se registra `LOGIN_FALLIDO`.
  - Cuentas iniciales precargadas:
    - **Administrador:** `admin` / `admin123`
    - **Empleado:** `empleado` / `empleado123`

### US2: Alta de Usuarios por el Administrador de Sistemas
- **Objetivo:** *"Como Administrador de Sistemas quiero poder dar de alta usuarios para garantizar la seguridad interna de la aplicación"*.
- **Implementación técnica:**
  - Control de acceso basado en roles (**RBAC**). Solo usuarios con rol `ADMIN` pueden invocar `AutenticacionService.darDeAltaUsuario(...)` o el endpoint `POST /api/usuarios/alta`.
  - Si un usuario no autenticado o con rol `EMPLEADO` intenta acceder, el servidor retorna `403 Forbidden` y no procesa la creación.
  - Se valida que el nombre de usuario no esté duplicado en la base de datos y que la contraseña cumpla los requisitos mínimos de longitud.
  - La interfaz web ofrece un formulario específico y una tabla en tiempo real con los usuarios registrados.

### US3: Trazabilidad y Auditoría de Modificaciones
- **Objetivo:** *"Como Administrador de Sistemas Quiero mantener la trazabilidad de las modificaciones para mantener las buenas prácticas de seguridad en la aplicación"*.
- **Implementación técnica:**
  - Tabla `auditoria` en SQLite con los campos: `id`, `usuario`, `accion`, `entidad`, `detalles`, `ip_origen`, `fecha_hora`.
  - Se registran automáticamente todas las operaciones críticas:
    - Autenticación (`LOGIN_EXITOSO`, `LOGIN_FALLIDO`, `LOGOUT`).
    - Gestión de usuarios (`ALTA_USUARIO`).
    - Migración y persistencia (`MIGRACION_BD_RELACIONAL`, `PERSISTENCIA_RESERVAS`).
    - Generación de archivos de prueba (`RESTAURAR_CSV_PRUEBAS`).
  - La bitácora se consulta cronológicamente (`ORDER BY id DESC`) desde la pestaña **Trazabilidad y Seguridad** (solo accesible por Administradores) o desde el menú terminal.

---

## 5. El Archivo de Consultas Relacionales (`sql/consultas.sql`)

El enunciado requiere explícitamente generar las consultas en un archivo SQL. En [`sql/consultas.sql`](file:///sql/consultas.sql) se encuentran estructuradas las siguientes secciones:

1. **Sección 1 - DDL (Definición de Esquema):**
   - `CREATE TABLE IF NOT EXISTS usuarios` (con constraints `UNIQUE` y `CHECK`).
   - `CREATE TABLE IF NOT EXISTS reservas` (con clave primaria unívoca).
   - `CREATE TABLE IF NOT EXISTS auditoria` (con timestamps automáticos).
   - Índices optimizados: `idx_usuarios_username`, `idx_reservas_pais_fecha`, `idx_reservas_localizador`, `idx_auditoria_fecha`.
2. **Sección 2 - Datos Semilla (Seed Data):**
   - Inserción de usuarios iniciales `admin` y `empleado` con hashes SHA-256 precalculados.
   - Evento de inicialización de auditoría.
3. **Sección 3 - Consultas de Autenticación (US1):**
   - Validación de credenciales activas.
4. **Sección 4 - Consultas de Gestión de Usuarios (US2):**
   - Comprobación de existencia de username, inserción de nuevos usuarios y listado administrativo.
5. **Sección 5 - Consultas de Trazabilidad y Seguridad (US3):**
   - Inserción de eventos de auditoría y consultas con límite de resultados o filtros por usuario y acción.
6. **Sección 6 - Consultas de Migración y Negocio:**
   - Inserción masiva `INSERT OR REPLACE`, agrupaciones por país/fecha y consulta de localizadores coincidentes.

---

## 6. Despliegue en Contenedores Virtuales (Docker)

Para dar respuesta al interés de AirTortilla de confeccionar contenedores virtuales que faciliten despliegues:

- **[`Dockerfile`](file:///Dockerfile):**
  - **Etapa de compilación (Builder):** Imagen base `eclipse-temurin:21-jdk-alpine` que compila todos los ficheros `.java` enlazando las librerías JDBC de `lib/`.
  - **Etapa final de ejecución (Runner):** Imagen ligera `eclipse-temurin:21-jre-alpine` que únicamente copia el directorio `bin/`, `lib/`, `web/`, `sql/` y los datos CSV.
  - Expone el puerto `8080` y monta un volumen persistente para `/app/salida`.
- **[`docker-compose.yml`](file:///docker-compose.yml):**
  - Permite levantar la aplicación completa con:
    ```bash
    docker-compose up --build -d
    ```

---

## 7. Preguntas Clave para Defender el Proyecto ante el Profesor

### Preguntas Generales y de Entrega 1

1. **¿Por qué el archivo CSV no aparece en el historial de Git?**
   - *Respuesta:* Por requerimiento de confidencialidad y buenas prácticas, en `.gitignore` se definió la regla `*.csv`. Para reproducir las pruebas se incluye el script `GeneradorDatosPrueba.java`, que genera localmente el fichero con los 3 casos requeridos (uno de 5 personas, uno de 3 y uno de 2).

2. **¿Qué criterio se usó para comparar reservas en MergeSort?**
   - *Respuesta:* La entidad `Reserva` implementa `Comparable<Reserva>`. Compara primero por `fechaReserva`, luego por `codigoPaisDestino` y finalmente por su `id` único.

3. **¿Cómo se comunica la web con Java sin usar Tomcat o Spring Boot?**
   - *Respuesta:* Se utilizó el servidor nativo de la biblioteca estándar de Java `com.sun.net.httpserver.HttpServer`. Permite levantar endpoints REST ligeros en el puerto 8080 sin sobrecargar la aplicación con frameworks pesados.

4. **¿Dónde se guardan los archivos generados y con qué nombre?**
   - *Respuesta:* En el directorio `salida/`. Respetan la regla `AirTortilla_XX_YYYY_MM_DD.csv`, donde `XX` es el código ISO (ej. `ES`, `FR`, `IT`, `GB`) y la fecha corresponde al año, mes y día de la reserva.

---

### Preguntas Específicas de la Entrega 2

5. **¿Qué motor de base de datos se ha empleado para la migración por legislación y por qué?**
   - *Respuesta:* Se ha empleado **SQLite mediante conector JDBC** (`org.sqlite.JDBC`). Ofrece un motor relacional completo (cumplimiento ACID, tipos de datos, índices, claves primarias y sintaxis SQL ANSI) que persiste en un archivo local (`airtortilla.db`) sin obligar al profesor a tener un servidor externo (como MySQL o PostgreSQL) corriendo previamente en su máquina, garantizando portabilidad absoluta.

6. **¿Cómo se ha protegido la aplicación contra inyecciones SQL (SQL Injection)?**
   - *Respuesta:* En todos los métodos de los DAOs (`UsuarioDAO`, `ReservaDAO`, `AuditoriaDAO`) se utiliza estrictamente `PreparedStatement` con parámetros posicionales tipados (`?`), evitando la concatenación directa de cadenas en sentencias SQL.

7. **¿Cómo se almacenan las contraseñas para cumplir con la US1 (Login)?**
   - *Respuesta:* No se almacenan en texto plano. Se calcula el hash criptográfico unidireccional **SHA-256** mediante la biblioteca nativa `java.security.MessageDigest`. Durante el inicio de sesión se calcula el hash de la contraseña introducida y se compara contra el valor persistido en la tabla `usuarios`.

8. **¿Cómo se restringe el alta de usuarios exclusivamente a Administradores (US2)?**
   - *Respuesta:* Mediante control de acceso basado en roles (**RBAC**). En `AutenticacionService.darDeAltaUsuario(...)` se valida que el usuario de la sesión activa posea el rol `RolUsuario.ADMIN`. Si un empleado o un usuario no autenticado intenta realizar la petición, se lanza una `SecurityException` y el servidor HTTP devuelve un código de estado `403 Forbidden`.

9. **¿Qué información se registra para cumplir con la trazabilidad (US3)?**
   - *Respuesta:* La tabla `auditoria` almacena:
     - `usuario`: Quién realizó la operación (o `ANONIMO` si falló la autenticación).
     - `accion`: Tipo de evento (`LOGIN_EXITOSO`, `LOGIN_FALLIDO`, `ALTA_USUARIO`, `MIGRACION_BD_RELACIONAL`, etc.).
     - `entidad`: Objeto afectado (`AUTENTICACION`, `USUARIO`, `RESERVAS`, `FICHERO`).
     - `detalles`: Resumen textual de la modificación.
     - `ip_origen`: IP del cliente o `TERMINAL`.
     - `fecha_hora`: Marca de tiempo generada automáticamente por la base de datos (`CURRENT_TIMESTAMP`).

10. **¿Por qué la persistencia de reservas es escalable ante grandes volúmenes de datos?**
    - *Respuesta:* En `ReservaDAO.insertarLote(...)` se utiliza el mecanismo de **lotes JDBC (`addBatch()` y `executeBatch()`)** dentro de una **transacción explícita (`setAutoCommit(false)`)**. Esto reduce drásticamente el número de operaciones de escritura a disco de $N$ accesos individuales a una única operación en bloque atómica.

11. **¿Dónde están ubicadas las consultas SQL solicitadas en el enunciado?**
    - *Respuesta:* En el directorio `sql/`, dentro del archivo [`sql/consultas.sql`](file:///sql/consultas.sql). Contiene el DDL de todas las tablas e índices, datos iniciales y todas las consultas DML requeridas para login, altas, trazabilidad y particionado de reservas.

12. **¿Cómo se ejecutan las pruebas automatizadas de la Entrega 2?**
    - *Respuesta:* Mediante la clase de prueba incluida [`PruebasEntrega2.java`](file:///src/com/airtortilla/test/PruebasEntrega2.java) con el comando:
      ```bash
      java -cp "bin;lib/*" com.airtortilla.test.PruebasEntrega2
      ```
      Este script ejecuta automáticamente tests de login con éxito y fallo, intentos no autorizados de alta de usuario por empleados, verificación de registros de auditoría y prueba de migración relacional.
