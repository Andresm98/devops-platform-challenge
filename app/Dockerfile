# ─── Stage 1: Build (Construcción del artefacto) ─────────────────────────────
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder

WORKDIR /workspace

# Copiar el POM y descargar las dependencias para aprovechar la caché de Docker layers
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiar el código fuente y las reglas de Checkstyle
COPY src ./src
COPY checkstyle.xml .

# Compilar y empaquetar omitiendo los tests (los tests se corren en el pipeline de CI antes del build de la imagen)
RUN mvn clean package -DskipTests -B

# ─── Stage 2: Runtime (Entorno ligero de ejecución) ──────────────────────────
FROM eclipse-temurin:21-jre-alpine AS runtime

WORKDIR /app

# Principio de menor privilegio: Crear un usuario de sistema sin permisos de root
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copiar el archivo ejecutable compilado desde la fase anterior
COPY --from=builder /workspace/target/*.jar app.jar

# Cambiar los permisos del archivo para el usuario seguro
RUN chown appuser:appgroup app.jar

# Cambiar al usuario no-root
USER appuser

# Exponer el puerto nativo de la aplicación
EXPOSE 8080

# Healthcheck nativo de Kubernetes / Plataformas usando Actuator
HEALTHCHECK --interval=30s --timeout=5s --start-period=20s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Comando de arranque optimizado para entornos de contenedores
ENTRYPOINT ["java", "-jar", "app.jar"]