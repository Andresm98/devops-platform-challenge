# 🚀 DevOps & Microservices Challenge - NTT DATA

Este proyecto implementa un ecosistema de microservicios robusto, seguro y altamente escalable, diseñado bajo principios de **Arquitectura Hexagonal** y operado mediante una cultura **DevSecOps** integral.

## 🏗️ Resumen de la Arquitectura de Software

El microservicio central está construido con **Spring Boot 3.x**, priorizando la mantenibilidad y el desacoplamiento:

* **Domain-Driven Design (DDD):** Lógica de negocio aislada de frameworks externos.
* **Seguridad Multi-Capa:** Validación de `X-Parse-REST-API-Key` y firma criptográfica de **JWT**.
* **Calidad de Código:** Cobertura de tests >80% con **JaCoCo** y análisis estático con **Checkstyle**.

---

## 🛠️ Stack Tecnológico de Infraestructura (The Platform)

| Capa | Tecnología | Propósito |
| --- | --- | --- |
| **API Gateway** | **Kong Gateway** | Gestión de tráfico, Auth perimetral y Rate Limiting. |
| **Orquestación** | **Kubernetes (K8s)** | Auto-healing, escalabilidad y despliegue declarativo. |
| **IaC** | **Terraform** | Infraestructura como Código para el aprovisionamiento Cloud. |
| **Containerization** | **Docker** | Multi-stage builds (Alpine) para imágenes seguras y ligeras. |
| **CI/CD Pipeline** | **GitHub Actions** | Automatización total desde el commit hasta el despliegue. |
| **Observabilidad** | **Prometheus & Grafana** | Monitoreo de métricas de salud y rendimiento (Actuator). |

---

## 🛡️ Ciclo DevSecOps (Shift-Left Security)

Nuestra tubería de automatización no solo compila; garantiza la integridad de la entrega:

1. **SAST (SonarCloud):** Análisis de vulnerabilidades en el código fuente.
2. **SCA & Container Scanning (Trivy):** Detección de CVEs en dependencias y capas de la imagen Docker.
3. **IaC Scanning (Checkov):** Auditoría de seguridad en manifiestos de Terraform y K8s.
4. **Quality Gates:** El despliegue se bloquea automáticamente si la cobertura de JaCoCo es insuficiente o existen vulnerabilidades `CRITICAL`.

---

## 🚀 Guía de Inicio Rápido (Local Testing)

### 1. Requisitos Previos

* Docker & Docker Compose
* Python 3.x (para scripts de automatización de pruebas)

### 2. Despliegue del Entorno Local

Levanta el microservicio con 2 réplicas detrás de un balanceador NGINX (simulando el entorno productivo):

```bash
docker-compose up -d --build

```

### 3. Automatización de Pruebas

Contamos con un suite de validación rápida en Python:

```bash
# Instalar dependencias
pip install -r ./instalations/requirements.txt

# Ejecutar test de integración (obtiene JWT y envía Payload)
python test_endpoint.py

```

### 4. Prueba Manual (Manual Trigger)

Si prefieres usar `curl`, sigue este flujo:

```bash
# 1. Obtener Token JWT
JWT=$(curl -s -X POST "http://localhost/DevOps/auth/token" \
  -H "Content-Type: application/json" \
  -d '{"clientName":"NTT-Data-Client"}' | jq -r '.token')

# 2. Enviar petición protegida
curl -X POST "http://localhost/DevOps" \
  -H "X-Parse-REST-API-Key: 2f5ae96c-b558-4c7b-a590-a501ae1c3f6c" \
  -H "X-JWT-KWY: ${JWT}" \
  -H "Content-Type: application/json" \
  -d '{"message":"Test","to":"Juan","from":"Rita","timeToLifeSec":45}'

```
---

## 🧪 Estrategia de Testing y Calidad de Código

La plataforma sigue un enfoque **TDD (Test Driven Development)**, garantizando que cada funcionalidad esté respaldada por pruebas automatizadas antes de su integración.

### Ejecución de Pruebas y Análisis

Para ejecutar el ciclo completo de vida de pruebas (unitarias e integración) y generar los reportes de cobertura, utiliza el wrapper de Maven incluido:

```bash
# Limpiar, ejecutar tests y generar reportes de cobertura
./mvnw clean verify
```

### Reporte de Cobertura (JaCoCo)

Tras la ejecución del comando anterior, se genera un informe detallado en formato HTML que desglosa la cobertura por clases, métodos y líneas de código.

* **Ruta del reporte:**
  `devops-platform-challenge/app/target/site/jacoco/index.html`

> **Criterio de Aceptación:** El pipeline de CI fallará automáticamente si la cobertura total de líneas es inferior al **80%**, asegurando la robustez exigida por los estándares de NTT DATA.


---


## 📈 Roadmap de Escalabilidad (Próximos Pasos)

* [ ] **K8s HPA:** Configuración de escalado automático basado en consumo de CPU/Memoria.
* [ ] **Zero Downtime:** Implementación de estrategias *Rolling Update* y *Liveness/Readiness Probes*.
* [ ] **Service Mesh:** Evaluación de mTLS para comunicación segura entre pods.

---

> **Nota de Seguridad:** Este repositorio utiliza escaneos automatizados. Cualquier vulnerabilidad detectada en las dependencias detendrá el ciclo de liberación para garantizar una plataforma **"Zero-Trust"**.
 