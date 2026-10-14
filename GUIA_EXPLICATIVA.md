# Guía Explicativa del Proyecto AirTortilla
### Asignatura: Acceso a Datos (2º DAM)

Esta guía contiene la explicación técnica y funcional del proyecto para comprender su arquitectura, defenderlo ante el profesor y responder a preguntas técnicas sobre complejidad algorítmica y diseño.

---

## 1. Objetivo del Proyecto

AirTortilla es una compañía aérea que necesita distribuir y clasificar sus reservas de vuelos sin depender de bases de datos relacionales o externas.

El sistema debe:
1. **Leer un archivo CSV original** en local sin subirlo a GitHub (`.gitignore`).
2. **Parsear los datos** a objetos `Reserva` en memoria (Nombre, Origen, Destino, País de destino ISO y Fecha).
3. **Generar identificadores unívocos** obligatorios (`RES-XXXXXXXX`).
4. **Asignar localizadores compartidos** (`LOC-XXXXXX`) cuando 2 o más reservas coinciden en **Nombre + Origen + Destino** (con casos de prueba de 2, 3 y 5 pasajeros coincidentes).
5. **Filtrar y particionar por país de destino** (código ISO de 2 letras).
6. **Ordenar eficientemente** mediante **MergeSort** garantizando una complejidad de $O(n \log n)$.
7. **Exportar a múltiples ficheros CSV** con el formato exacto: `AirTortilla_XX_YYYY_MM_DD.csv`.
8. **Ofrecer una interfaz web y terminal** con selector de **Modalidad Total** y **Modalidad Línea a Línea** con feedback visual de la operación.

---

## 2. Mapa de Clases y Arquitectura

```text
src/
└── com/airtortilla/
    ├── Main.java                         -> Punto de entrada, arranca ServidorWeb y abre navegador
    ├── model/
    │   └── Reserva.java                  -> Entidad del dominio (implementa Comparable<Reserva>)
    ├── algoritmos/
    │   └── OrdenadorMergeSort.java       -> Algoritmo MergeSort O(n log n) divide y vencerás
    ├── service/
    │   ├── IdentificadorService.java     -> Generación de IDs y localizadores con Hash O(n)
    │   ├── FiltroPaisService.java        -> Agrupación por país ISO y fecha en O(n)
    │   └── GestorMemoriaReservas.java    -> Repositorio de objetos en memoria sin BD
    ├── io/
    │   ├── LectorCsv.java                -> Lectura total y por stream (callback Consumer)
    │   └── EscritorCsv.java              -> Escritura particionada AirTortilla_XX_YYYY_MM_DD.csv
    ├── test/
    │   └── GeneradorDatosPrueba.java     -> Script que genera el CSV de prueba con los casos requeridos
    └── web/
        └── ServidorWeb.java              -> Servidor HTTP nativo Java (puerto 8080) con API REST

web/                                      -> Frontend de la aplicación web
├── index.html                            -> Dashboard visual según boceto de clase
├── estilos.css                           -> Diseño moderno con Glassmorphism
└── app.js                                -> Lógica cliente para Modalidad Total y Línea a Línea
```

---

## 3. Justificación de Complejidad Algorítmica (Big O)

Este es el apartado más valorado en la asignatura:

### A. ¿Por qué se evita $O(n^2)$ al buscar pasajeros coincidentes?
- **El error clásico ($O(n^2)$):** Comparar cada reserva contra todas las demás usando dos bucles `for` anidados. Para 10.000 reservas, supondría $100.000.000$ de comparaciones.
- **Nuestra solución ($O(n)$):** En `IdentificadorService.java`, se calcula una clave única (`nombre|origen|destino`) y se inserta en un `HashMap<String, List<Reserva>>`.
  - El acceso e inserción en la tabla hash tiene un coste constante amortizado de $O(1)$.
  - Recorrer las $n$ reservas toma $n \times O(1) = O(n)$.
  - Recorrer los grupos formados toma $O(n)$.
  - **Coste total: estrictamente $O(n)$ lineal.**

### B. ¿Por qué MergeSort y cómo funciona su $O(n \log n)$?
- En `OrdenadorMergeSort.java` se aplica el principio de **Divide y Vencerás**:
  1. **Divide:** Se divide recursivamente la lista en dos mitades hasta llegar a sublistas de 1 elemento (caso base trivial). Como cada división divide el tamaño entre 2, la profundidad del árbol de recursión es $\log_2(n)$.
  2. **Vence (Merge):** Se fusionan ordenadamente dos sublistas comparando los elementos frontales mediante `Comparable<Reserva>`, lo que tiene un coste lineal $O(n)$ por nivel.
  3. **Multiplicación:** $n \times \log_2(n) \rightarrow O(n \log n)$.
- MergeSort es un algoritmo **estable** (mantiene el orden relativo de elementos equivalentes) y garantiza $O(n \log n)$ tanto en el mejor, como en el caso medio y peor caso.

---

## 4. Modalidades de Ejecución

### Modalidad Total
1. Carga todo el archivo CSV en memoria en una colección `List<Reserva>`.
2. Procesa todos los identificadores y localizadores en bloque en $O(n)$.
3. Ordena toda la lista con MergeSort $O(n \log n)$.
4. Particiona y escribe todos los ficheros `AirTortilla_XX_YYYY_MM_DD.csv` en la carpeta `salida/`.
5. Muestra las métricas completas en el panel de Feedback.

### Modalidad Línea a Línea
1. Lee y procesa registro a registro de forma interactiva.
2. Permite avanzar manualmente con el botón "Siguiente Registro" o en modo "Auto Play" continuo.
3. Permite inspeccionar en vivo cómo se le asigna el ID a cada pasajero y cómo se le asocia el localizador si coincide con un registro previo.
4. Va escribiendo incrementalmente en el archivo correspondiente `AirTortilla_XX_YYYY_MM_DD.csv`.

---

## 5. Preguntas Clave para Defender el Proyecto ante el Profesor

1. **¿Por qué el archivo CSV no aparece en el historial de Git?**
   - *Respuesta:* Por requerimiento de seguridad y buenas prácticas, en `.gitignore` se definió la regla `*.csv`. Para reproducir las pruebas se incluye el script `GeneradorDatosPrueba.java`, que genera localmente el fichero con los 3 casos requeridos (uno de 5 personas, uno de 3 y uno de 2).

2. **¿Qué criterio se usó para comparar reservas en MergeSort?**
   - *Respuesta:* La entidad `Reserva` implementa `Comparable<Reserva>`. Compara primero por `fechaReserva`, luego por `codigoPaisDestino` y finalmente por su `id` único.

3. **¿Cómo se comunica la web con Java sin usar Tomcat o Spring Boot?**
   - *Respuesta:* Se utilizó el servidor nativo de la biblioteca estándar de Java `com.sun.net.httpserver.HttpServer`. Permite levantar endpoints REST ligeros en el puerto 8080 sin necesidad de añadir dependencias externas ni ficheros `.jar` adicionales.

4. **¿Dónde se guardan los archivos generados y con qué nombre?**
   - *Respuesta:* En el directorio `salida/`. Respetan la regla `AirTortilla_XX_YYYY_MM_DD.csv`, donde `XX` es el código ISO (ej. `ES`, `FR`, `IT`, `GB`) y la fecha corresponde al año, mes y día de la reserva.
