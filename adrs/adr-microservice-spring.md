# ADR-001: Diseño de Microservicio Spring Boot con Arquitectura Hexagonal y enfoque DevSecOps

## Estado
Propuesto / Aceptado

## Fecha
2026-05-27

## Contexto

La plataforma requiere el diseño e implementación de un microservicio backend basado en Spring Boot que cumpla con estándares de nivel enterprise, incluyendo:

- Arquitectura limpia y desacoplada
- Seguridad a nivel de API (JWT + API Key + firma)
- Alta calidad de código (TDD + análisis estático)
- Despliegue cloud-native en Kubernetes
- Integración con pipeline DevSecOps completo
- Observabilidad y preparación para operación en producción

Además, el sistema debe ser escalable, mantenible y preparado para entornos multicloud o Kubernetes gestionado.

---

## Decisión

Se adopta el siguiente enfoque arquitectónico y tecnológico:

### 1. Arquitectura del microservicio
- Se implementa **Arquitectura Hexagonal (Ports & Adapters)**.
- Separación estricta entre:
    - Dominio
    - Aplicación
    - Infraestructura
- El núcleo del negocio no depende de frameworks externos.

---

### 2. Stack tecnológico
- **Spring Boot** como framework base
- **Maven** como gestor de dependencias y build tool
- **Java 21+**
- **Spring Web** para API REST
- **Spring Boot Actuator** para métricas y health checks
- **JUnit + Mockito** para TDD

---

### 3. Seguridad
Se implementa un enfoque de seguridad en capas:

- Validación de **API Key**
- Autenticación mediante **JWT firmado**
- Posible delegación de autenticación a **API Gateway (Kong)**
- Filtros en capa de infraestructura

---

### 4. Containerización
- Imagen Docker multi-stage basada en **Alpine**
- Ejecución como usuario no-root
- Optimización de tamaño y superficie de ataque reducida

---

### 5. Infraestructura y despliegue
- Despliegue en **Kubernetes**
- Uso de:
    - `Deployment`
    - `Service`
    - `HPA (Horizontal Pod Autoscaler)`
- Ingreso controlado mediante **Kong Ingress Controller**

---

### 6. DevSecOps (CI/CD)
Pipeline automatizado con GitHub Actions:

- Fase Build:
    - Compilación Maven
    - Tests unitarios/integración
- Fase Quality:
    - SonarQube / SonarCloud (calidad + coverage JaCoCo)
- Fase Security:
    - Trivy (vulnerabilidades en contenedor)
    - Checkov (IaC security scanning)
- Fase Deploy:
    - Despliegue en Kubernetes

---

### 7. Calidad de código
- TDD obligatorio
- Cobertura mínima objetivo: ≥ 80%
- Reporte generado con **JaCoCo**
- Análisis estático obligatorio en pipeline

---

### 8. Observabilidad
- Spring Actuator expone métricas en `/actuator/prometheus`
- Preparado para integración con:
    - Prometheus
    - Grafana

---

## Consecuencias

### Positivas
- Arquitectura altamente mantenible y escalable
- Seguridad reforzada en múltiples capas
- Pipeline DevSecOps alineado con estándares enterprise
- Preparación para cloud-native y Kubernetes real
- Alta calidad de código medible automáticamente

### Negativas / Costes
- Mayor complejidad inicial de configuración
- Curva de aprendizaje en herramientas DevSecOps
- Mayor número de componentes en el sistema (Kong, Kubernetes, Sonar, etc.)

---

## Alternativas consideradas

### 1. Arquitectura monolítica
- Rechazada por falta de escalabilidad y alineación con cloud-native

### 2. Spring MVC sin hexagonal
- Rechazada por acoplamiento fuerte entre capas

### 3. NGINX Ingress en lugar de Kong
- Aceptable técnicamente, pero menos completo en capacidades API management

---

## Notas finales

Este ADR establece la base arquitectónica del sistema. Cualquier evolución futura (multi-microservicio, event-driven architecture, service mesh como Istio) deberá ser documentada en ADRs adicionales.