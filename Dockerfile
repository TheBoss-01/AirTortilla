# ==============================================================================
# Dockerfile - Despliegue de AirTortilla en Contenedor Virtual
# Permite empaquetar y ejecutar la aplicación de forma portable y desacoplada
# ==============================================================================

# Etapa 1: Compilación de la aplicación
FROM eclipse-temurin:21-jdk-alpine AS compilador
WORKDIR /build

COPY lib/ ./lib/
COPY src/ ./src/

RUN mkdir -p bin && javac -encoding UTF-8 -cp "lib/*" -d bin $(find src -name "*.java")

# Etapa 2: Imagen final de ejecución ligera (JRE)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copia de binarios compilados y dependencias JDBC
COPY --from=compilador /build/bin ./bin
COPY --from=compilador /build/lib ./lib
COPY web/ ./web
COPY sql/ ./sql
COPY AirTortilla_Reservas_2020.csv ./

# Puerto para el servidor web y API REST
EXPOSE 8080

# Volumen persistente para los archivos particionados generados
VOLUME ["/app/salida"]

ENTRYPOINT ["java", "-cp", "bin:lib/*", "com.airtortilla.Main"]
