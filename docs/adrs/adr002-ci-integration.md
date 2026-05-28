# ADR-002: Automatización de Integración Continua (CI), Empaquetado Seguro y Análisis DevSecOps (Shift-Left)

## Estado

Aceptado

## Fecha

2026-05-28

## Contexto

El ciclo de vida del desarrollo de software (SDLC) de la plataforma requiere que cada cambio de código o configuración de infraestructura sea auditado de manera automática antes de generar artefactos distribuibles. Se busca evitar que secretos expuestos, vulnerabilidades críticas de dependencias de terceros (OS/librerías) o degradación en la calidad del código alcancen los entornos de ejecución en la nube.

## Decisión

Se decide implementar un pipeline multicapa de Integración Continua (CI) estructurado en GitHub Actions, ejecutando herramientas automatizadas de análisis estático y dinámico bajo el enfoque de **Shift-Left Security**:

1. **Construcción y Calidad de Código (Java/Spring Boot):**
* Uso de GitHub Actions con runners `ubuntu-latest` configurados con JDK 21 (Temurin).
* Verificación estática y de diseño de código mediante Checkstyle integrado en Maven.
* Ejecución de pruebas unitarias bajo metodología **TDD** bloqueando el progreso ante cualquier fallo.


2. **Auditoría de Cobertura y Deuda Técnica:**
* Inyección de **JaCoCo** para el cálculo dinámico de cobertura, estableciendo un *Quality Gate* estricto de **≥ 80%**.
* Publicación del reporte de cobertura directamente en los comentarios de los Pull Requests (`madrapps/jacoco-report`).
* Integración nativa con **SonarCloud** para rastrear vulnerabilidades del código fuente, *code smells* y mantener la calidad del repositorio a nivel Enterprise.


3. **Seguridad en la Infraestructura como Código (IaC):**
* Adopción de **Checkov** como escáner estático de seguridad para los manifiestos de Terraform. Permite detectar configuraciones inseguras (ej. puertos abiertos al mundo o falta de encriptación) en fase de pre-aprovisionamiento.


4. **Contenerización y Seguridad de Artefactos:**
* Dockerización de la aplicación Spring Boot utilizando una estrategia **Multi-Stage Build** optimizada sobre imágenes base Alpine para mitigar la superficie de ataque.
* Orquestación del build local en el runner mediante Docker Buildx.
* Análisis de vulnerabilidades del contenedor mediante **Trivy**, configurado para romper el pipeline (Exit Code 1) si detecta CVEs de severidad **CRITICAL**.
* Empaquetado y publicación automática en Docker Hub bajo el tag auditado `${{ github.sha }}` y el tag dinámico `latest` asignados al repositorio del usuario `andy17u7`.



---

## Consecuencias

### Positivas

* **Garantía DevSecOps Real:** Ninguna imagen Docker es publicada en Docker Hub si contiene deuda técnica no aprobada en SonarCloud o vulnerabilidades críticas detectadas por Trivy.
* **Trazabilidad Absoluta:** Cada artefacto en Docker Hub está directamente vinculado a un commit hash (`github.sha`) específico e inmutable.
* **Feedback Temprano:** Los desarrolladores reciben auditorías de cobertura e infraestructura directamente en la interfaz de GitHub sin intervención del equipo de operaciones.

### Negativas / Trade-offs

* **Incremento en los Tiempos de Ejecución:** El pipeline tarda más tiempo en completarse debido al aprovisionamiento del entorno Java, la ejecución de escaneos profundos de Trivy y la comunicación síncrona con las APIs de SonarCloud.
* **Falsos Positivos de Checkov:** Configurar Checkov con un criterio restrictivo inicial requiere añadir excepciones explícitas (`soft_fail: true` temporal) en componentes de red cloud legítimos para evitar bloqueos innecesarios en etapas tempranas.

---

## Alternativas consideradas

* **Snyk Container Scan:** Descartado en favor de **Trivy** por la velocidad de actualización de bases de datos de vulnerabilidades en arquitecturas Alpine de código abierto y su integración nativa en entornos de consola CLI sin licenciamientos adicionales obligatorios.

---
**Software Engineer:** Santiago Moreta

