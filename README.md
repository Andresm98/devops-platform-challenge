# DevOps & Microservices Challenge - Enterprise Platform

Este repositorio contiene la implementación punta a punta E2E de un ecosistema de microservicios nativo de la nube, seguro y con autoescalamiento elástico, diseñado bajo principios de **Arquitectura Hexagonal (Ports & Adapters)** y operado mediante una cultura **DevSecOps** madura con despliegue automatizado en AWS.

---

## 🏗️ Arquitectura de Software y Topología

Arquitectura orientada a microservicios sobre K3s, protegida por Kong API Gateway. La infraestructura es elástica gracias al uso de Kubernetes HPA y cuenta con un stack de observabilidad completo (Prometheus/Grafana) para monitoreo en tiempo real, todo orquestado mediante un pipeline de GitHub Actions.
```text
[ DESARROLLADOR ] ──> Git Push ──> [ GITHUB ACTIONS (CI/CD) ON DEMAND ]
                                          │
    ┌─────────────────────────────────────┴────────────────────────────────┐
    │ 1. SONARCLOUD (Static Analysis) - Coverage (Line = 80%, Branch = 75%)│
    │ 2. Trivy (Container Scanning) + Checkov (IaC)                        │
    │ 3. DOCKER BUILD & PUSH (Docker Hub)                                  │
    │ 4. K3S DEPLOY (Kustomize)        ───┐                                │
    └─────────────────────────────────────┼────────────────────────────────┘
                                          ▼
                                 [ CLÚSTER K3S (EC2) ]
                                          │
[ TRÁFICO ] ──> [ KONG API GATEWAY ] ─────┤
  (Público)      (Ingress Controller)     │      [ HPA ] (Auto-scaling)
                         │                │    ┌───┴───┐
                (Rules / Plugins)         ├──> [ POD 1 ] [ POD 2 ] ... [ POD N ]
                         │                │    └───────┬───────┘
                         ▼                │            │ (Expose /metrics)
              [ SERVICIO KUBERNETES ] ────┘            ▼
                                             [ PROMETHEUS SERVER ]
                                                       │ (Pull metrics)
                                                       ▼
                                             [ GRAFANA DASHBOARDS ]
```

### 1. El Microservicio (`./app`)

Construido con **Spring Boot 3.x** y **Java 21**, estructurado de forma agnóstica al framework:

* **Domain-Driven Design (DDD):** El núcleo lógico de negocio no posee dependencias externas.
* **Seguridad Multi-Capa en Código:** Filtros perimetrales internos que interceptan las cabeceras `X-Parse-REST-API-Key` y validan la firma criptográfica de tokens **JWT** (con expiración de 10 minutos).
* **Observabilidad Nativa:** Integración del registro de **Micrometer Prometheus** expuesto dinámicamente en el endpoint `/actuator/prometheus`.

---

Para garantizar un código robusto y libre de vulnerabilidades, el proyecto integra un análisis estático automático mediante **SonarCloud**.
#### Estado del Proyecto
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/quality_gate?project=devops-platform-challenge)](https://sonarcloud.io/summary/new_code?id=devops-platform-challenge)

| Métrica | Enlace | Estado |
| :--- | :--- | :--- |
| **Análisis Completo** | [Consultar en SonarCloud](https://sonarcloud.io/organizations/devops-platform-challenge/projects) | Completo |
| **Quality Gate** | Pasa/Falla según estándares de seguridad y mantenibilidad | Completo |

### Stack de Análisis
*   **Static Analysis:** Detección de Bugs, Vulnerabilidades y Code Smells.
*   **Integración:** GitHub Actions Pipeline.
*   **Frecuencia:** Ejecución automática en cada `push` a la rama `main` y `dev`.

### 2. La Plataforma de Infraestructura (`./k8s` & `./terraform`)

Un entorno elástico de alta disponibilidad orquestado de manera 100% declarativa:

| Capa | Tecnología | Propósito Específico                                                                      |
| --- | --- |-------------------------------------------------------------------------------------------|
| **Aprovisionamiento** | **Terraform** | Definición de infraestructura inmutable (EC2, VPC, Security Groups) de forma idempotente. |
| **Orquestación** | **Kubernetes (K3s)** | Control del ciclo de vida, autoreparación (*Auto-healing*) y balanceo interno.            |
| **API Gateway** | **Kong Ingress Controller** | Perímetro de red unificado, enrutamiento semántico declarativo y abstracción de puertos.  |
| **Escalabilidad** | **K8s HPA (Horizontal Pod Autoscaler)** | Clonación dinámica de pods (mínimo 2, máximo 6) al superar el **75% de uso de CPU**.      |
| **Observabilidad** | **Prometheus & Grafana** | Captura y visualización en tiempo real de las **4 Señales de Oro SRE**.                   |

---

## 🛡️ Pipeline DevSecOps (Shift-Left & Continuous Integration/Delivery)

El ciclo de automatización de Git/GitHub Actions está diseñado bajo un estricto enfoque de **Zero-Trust**, segregado en dos pipelines integrados:

### CI (Integración Continua)

1. **Construcción y TDD:** Compilación estricta y ejecución de pruebas unitarias/integración en aislamiento.
2. **SAST & Deuda Técnica (SonarCloud):** Análisis estático del código fuente para asegurar la mantenibilidad y detectar vulnerabilidades lógicas.
3. **SCA & Container Scanning (Trivy):** Escaneo de seguridad sobre la imagen Docker multi-stage basada en **Alpine**. Bloquea el flujo ante vulnerabilidades `CRITICAL`.
4. **IaC Scanning (Checkov):** Auditoría estática sobre los manifiestos de Terraform y Kubernetes previniendo configuraciones de red o permisos inseguros.

### CD (Entrega Continua)

* **Aprovisionamiento en Vivo:** Inicialización y aplicación automática de cambios de Terraform.
* **Despliegue Atómico (Zero-Downtime):** Uso de **Kustomize** para inyectar tags inmutables basados en el hash del commit (`${{ github.sha }}`) y ejecución de estrategias **Rolling Update**.
* **Compuertas de Calidad:** Verificación en caliente del despliegue mediante `kubectl rollout status`. Si los *Liveness/Readiness Probes* fallan, el cambio se congela automáticamente preservando la versión sana anterior.

---

## 🗄️ Gestión Segura de Configuraciones y Secretos

En entornos de producción, los datos sensibles no se versionan en texto plano en Git. Kubernetes gestiona de forma nativa el desacoplamiento en el namespace `devops-app`:

* **ConfigMap (`devops-config`):** Almacena las llaves criptográficas (`API_KEY` y `JWT_SECRET`) codificadas en **Base64**. Al iniciar el Pod, Kubernetes inyecta estos valores directamente en la memoria RAM del contenedor como variables de entorno (`envFrom`), impidiendo que sean escritas en disco duro o expuestas en logs.

---

## 🚀 Guías de Operación y Pruebas

### Prueba Directa en base a cURL (Reto Técnico)

Para validar la correcta integración del API Gateway, la Seguridad JWT y la conectividad del clúster, ejecutar el siguiente bloque en la terminal:

```bash
# Copiar y pegar el siguiente bloque en tu terminal Bash, reemplazar la IP pública 
# y el puerto por los valores correspondientes al despliegue en AWS.
# Requiere tener instalado `jq` para parsear el JSON de respuesta y extraer el token JWT.

# 1. Obtener Token JWT
JWT=$(curl -s -X POST "http://18.216.20.102:32548/DevOps/auth/token" \
  -H "Content-Type: application/json" \
  -d '{"clientName":"NTT-Data-Client"}' | jq -r '.token')

# 2. Enviar petición protegida
curl -X POST "http://18.216.20.102:32548/DevOps" \
  -H "X-Parse-REST-API-Key: 2f5ae96c-b558-4c7b-a590-a501ae1c3f6c" \
  -H "X-JWT-KWY: ${JWT}" \
  -H "Content-Type: application/json" \
  -d '{"message":"Test","to":"Juan","from":"Rita","timeToLifeSec":45}'
```

#### ¿Cómo funcionan los comandos de pruebas?

* **1.A.** El Gestor API **Kong Ingress Controller** expone el endpoint `/DevOps` en el puerto **32548 NodePort**, protegido por la API Key y el token JWT.

* **1.B.** El JWT se obtiene mediante una petición **POST** al endpoint `/auth/token` con el nombre del cliente en el payload.

* **2.A.** El token se incluye en la cabecera `X-JWT-KWY` junto con la API Key en `X-Parse-REST-API-Key` para enviar una petición POST.

* **2.B.** El token dura 10 minutos, y el endpoint **validará** tanto la firma criptográfica del JWT como la presencia de la API Key correcta antes de procesar el mensaje.

* **2.C.** Si se envía un verbo HTTP diferente a **POST**, el servicio responderá con un string plano `ERROR` sin procesar la solicitud.


* **OBSERVACIÓN:** Estas pruebas fueron realizadas en el pipeline de CI/CD, pero también puedes ejecutarlas localmente apuntando al puerto expuesto por Docker-Compose o al clúster de Kubernetes según tu entorno de desarrollo.

* **NOTA:** Asegúrate de reemplazar la IP pública y el puerto por los valores correspondientes a tu despliegue en AWS.

### 💻 Opción A: Simulación y Pruebas Locales (Docker-Compose)

Diseñado para el desarrollo ágil local, levantando 2 réplicas del servicio detrás de un balanceador NGINX:

```bash
docker-compose up -d --build
```

### ☁️ Opción B: Operación e Inspección en AWS Producción (Kubernetes)

Para interactuar y auditar el clúster productivo real en la instancia EC2, ejecuta los siguientes comandos desde la terminal:

```bash
# 1. Verificar el estado de salud de todos los pods (App, Kong y Monitoreo)
sudo kubectl get pods -A

# 2. Comprobar las reglas perimetrales del Ingress de Kong
sudo kubectl get ingress devops-ingress -n devops-app
 
# 3. Balanceador de Carga y Alta Disponibilidad
sudo kubectl get deployment devops-service -n devops-app

# 4. Verificar Escalabilidad Dinámica (HPA)
sudo kubectl get hpa -n devops-app

# 5. Verificar Gestor API 
sudo kubectl get pods -n kong
 
#9 Verificar Métricas de Prometheus con Grafana (user: admin / pass: admin123) en el puerto 3000 (Verificar Security Group de AWS para permitir el acceso)
sudo k3s kubectl port-forward --address 0.0.0.0 svc/grafana-service 3000:80 -n monitoring

```

---

## 🧪 Ejecución de Pruebas de Integración Automatizadas

Se generó con un script de automatización en Python (`test_endpoint.py`) utilizado en la fase final del pipeline de GitHub Actions como Smoke Test. Éste se conecta de manera externa mediante la IP pública corporativa en el puerto HTTP estándar controlada por el Gateway de Kong.

```bash
# 1. Configurar entorno de dependencias
pip install -r ./instalations/requirements.txt

# 2. Configurar las variables en tu archivo .env local o dejar que GitHub Secrets las inyecte en el CI/CD
# APP_HOST=18.116.118.224
# APP_PORT=80
# API_KEY=2f5ae96c-b558-4c7b-a590-a501ae1c3f6c

# 3. Ejecutar el flujo automatizado E2E
python test_endpoint.py

```

### Reporte de Cobertura de Código (TDD)

El proyecto exige un estándar mínimo de **≥ 80% de cobertura** calculado por JaCoCo. Localmente puedes ejecutar y compilar el reporte con:

```bash
./mvnw clean verify
```
* **Ruta local del informe interactivo:** `./app/target/site/jacoco/index.html`

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

## 📈 Observabilidad SRE en Producción

El ecosistema cuenta con un stack de monitoreo optimizado e independiente desplegado bajo el namespace `monitoring`.

* **Prometheus Server:** Realiza un Scraping síncrono cada 15 segundos sobre los pods de la aplicación utilizando las anotaciones `prometheus.io/scrape: "true"` configuradas en la metadata del despliegue.
* **Grafana Dashboards:** Visualización unificada del comportamiento de la plataforma orientada al negocio y a la disponibilidad técnica:
* **Métricas de Infraestructura:** Consumo de CPU por pod y conteo de réplicas controladas por el HPA.
* **Métricas SRE (Golden Signals):** Tasa de rendimiento (Throughput), latencia promedio de respuestas y detección temprana de incidentes mediante el conteo de errores HTTP 5xx.



---

## 📝 Registro de Decisiones de Arquitectura (ADRs)

Para profundizar en las justificaciones técnicas, los balances de ingeniería y los trade-offs considerados durante el desarrollo, consulte la documentación oficial en `docs/adrs/`:

* **ADR-001:** Diseño de Microservicio Spring Boot con Arquitectura Hexagonal y enfoque DevSecOps.
* **ADR-002:** Automatización de Integración Continua (CI), Empaquetado Seguro y Análisis DevSecOps (Shift-Left).
* **ADR-003:** Aprovisionamiento Automatizado (CD), Orquestación de Contenedores en AWS, Enrutamiento Perimetral y Observabilidad SRE Pragmática.

---

## 📋 Checklist de Hitos Conseguidos

| Requisito del Reto | Estado | Implementación Técnica Específica                                                                                                           |
| --- |--|---------------------------------------------------------------------------------------------------------------------------------------------|
| **Endpoint Único `/DevOps` (POST)** | Completo | Implementado en Spring Boot 3.x, validando estrictamente el payload JSON requerido.                                                         |
| **Manejo de Métodos no Permitidos** | Completo | Filtro global que intercepta cualquier verbo diferente a `POST` y retorna un string plano `ERROR`.                                          |
| **Seguridad por API Key de Reto** | Completo | Validación perimetral en cabecera `X-Parse-REST-API-Key` contra el valor exacto `2f5ae96c-b558-4c7b-a590-a501ae1c3f6c`.                     |
| **Autenticación y Token JWT Único** | Completo | Generación dinámica por transacción (`/auth/token`) con firma criptográfica y expiración controlada de 10 minutos.                          |
| **Estructura de cURL de Validación** | Completo | Validado de extremo a extremo mediante el paso del token en la cabecera solicitada `X-JWT-KWY`.                                             |
| **Containerización Segura** | Completo | Dockerfile con construcción **Multi-Stage Build** basado en Alpine Linux ejecutándose bajo un usuario no-root.                              |
| **Balanceador de Carga e Infraestructura** | Completo | **Local:** Docker-Compose con 2 réplicas de la App detrás de NGINX.<br>                                                                     
| **Producción:** Clúster Kubernetes (K3s) en AWS con `Service` e `Ingress` de Kong actuando como balanceador de capa 7. |  Completo | Los componentes de la arquitectura se encuentran desplegados en el cluster AWS.                                                             |
| **Pipeline CI/CD Automatizado** |  Completo | Orquestado mediante GitHub Actions (`ci.yml` y `cd.yml`) con ejecución inmediata ante eventos en la rama principal.                         |
| **Rama `master`/`main` Despliega a Prod** |  Completo | Configurado el trigger automático del CD exclusivamente al consolidar cambios en la rama `main`.                                            |
| **Soporte Bajo Demanda y Versiones** |  Completo | Implementación de `workflow_dispatch` en GitHub Actions, parametrizando tags basados en el hash inmutable del commit (`${{ github.sha }}`). |
| **Pruebas Automatizadas Integradas** |  Completo | Cobertura doble: Tests unitarios/integración en Java (JUnit/Mockito) y Smoke Tests E2E en Python (`test_endpoint.py`).                      |
| **Uso de Metodología TDD** |  | Ciclo de diseño de software guiado por pruebas antes de la lógica de negocio en la capa de aplicación.                                      |
| **Análisis Estático de Código (SAST)** |  Completo | Integración automatizada en el pipeline de CI con **SonarCloud** y validación de diseño con **Checkstyle**.                                 |
| **Análisis de Seguridad de Infraestructura** |  Completo | Escaneo de vulnerabilidades en contenedores con **Trivy** y auditoría de seguridad de IaC con **Checkov**.                                  |
| **Cobertura de Código Analizada** |  Completo | Cobertura dinámica calculada por **JaCoCo** con un *Quality Gate* bloqueante configurado en **≥ 80%**.                                      |
| **Gestor de APIs (API Gateway)** |  Completo | Despliegue de **Kong Ingress Controller** en modo declarativo (DB-less) abstrayendo la red interna del clúster.                             |
| **Escalabilidad Dinámica** |  Completo | Configuración de un **Horizontal Pod Autoscaler (HPA)** que escala horizontalmente de 2 a 6 pods basados en métricas reales de CPU.         |
| **Monitoreo y Cultura SRE** |  Completo | Stack completo de **Prometheus** (Scraping cada 15s mediante anotaciones) y **Grafana** analizando las *4 Señales de Oro*.                  |

---

### ⚖️ Tabla de Trade-offs (Balances de Ingeniería)

Durante el diseño e implementación de esta plataforma se tomaron decisiones de arquitectura guiadas por el pragmatismo, la eficiencia de costes y las restricciones del entorno del reto. A continuación se desglosan los compromisos técnicos asumidos:

| Componente / Decisión                                       | Ventaja Ganada (Pros) | Compromiso Asumido (Cons / Trade-offs) | Mitigación o Justificación                                                                                                                                                                                                                                                                                                                                                    |
|-------------------------------------------------------------| --- | --- |-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Arquitectura Hexagonal en el Microservicio**              | Aislamiento total del negocio. Mantenibilidad enterprise y desacoplamiento absoluto de librerías externas o bases de datos. | Mayor cantidad de archivos iniciales (*boilerplate code*) y abstracciones (puertos e interfaces) para un endpoint único. | Se justifica plenamente debido a que establece una base limpia y escalable para el crecimiento orgánico hacia una plataforma bancaria real.                                                                                                                                                                                                                                   |
| **Estrategia de Squash Merging en GitHub**                  | Historial de commits en la rama `main` impecable, limpio y lineal. Facilita auditorías forenses rápidas y reversiones atómicas de pipelines en producción. | Pérdida del rastro granular de micro-commits individuales realizados por el desarrollador durante la fase de codificación en la rama `dev`. | El detalle de desarrollo permanece intacto en los Pull Requests cerrados, preservando la trazabilidad sin ensuciar el historial de producción.                                                                                                                                                                                                                                |
| **Kong API Gateway en Modo DB-less**                        | Consumo mínimo de memoria RAM y almacenamiento en la instancia EC2. Elimina la complejidad de administrar una base de datos relacional (PostgreSQL) para el Gateway. | Las configuraciones de Kong son estrictamente globales y estáticas a través de la API de Kubernetes. No permite persistir datos dinámicos en caliente por fuera de los manifiestos. | Dado que el perímetro se define de forma 100% declarativa por GitOps (`ingress.yaml`), el modo sin base de datos es óptimo, ligero y perfectamente reproducible.                                                                                                                                                                                                              |
| **Definición de Infraestructura Inmutable con Terraform** | Garantiza la idempotencia, consistencia del entorno y previene el "drift" de configuración. Todo cambio estructural se despliega destruyendo y recreando componentes de forma predecible. | Los recursos de larga duración (como la instancia EC2 que sostiene a K3s o la VPC) requieren ventanas de mantenimiento si un cambio estructural fuerza su recreación (*destroy & create*). | Se justifica plenamente bajo el principio de *Infrastructure as Code (IaC)*. Para cambios mutables internos del sistema operativo o parches rápidos, en un entorno empresarial real se delegaría a herramientas de gestión de configuración (como Ansible) o imágenes mutadas previamente (AMIs vía Packer).                                                                  |
| **Uso de ConfigMaps para Parámetros de Entorno (Fase Inicial)** | Centralización extremadamente ágil de la configuración global de la aplicación. Permite la inyección directa en la memoria RAM (`envFrom`) sin persistir texto plano en el disco del contenedor. | Almacenar credenciales (como `API_KEY` o `JWT_SECRET`) en un ConfigMap expone los datos a cualquier rol con permisos de lectura básicos en el clúster. Además, Base64 **no es cifrado**, es solo codificación. | Asumido exclusivamente para la naturaleza aislada y demostrativa de este reto técnico. En un entorno productivo empresarial, la mitigación inmediata es migrar a objetos `Secret` de Kubernetes (con RBAC/ABAC restrictivos) o, idealmente, integrar un operador dinámico como **External Secrets Operator (ESO)** conectado a **AWS Secrets Manager** o **HashiCorp Vault**. |
| **Almacenamiento de Prometheus en `emptyDir: {}`**          | Evita la sobrecarga de latencia y los costos de facturación asociados al aprovisionamiento dinámico de volúmenes elásticos de AWS (EBS/EFS) en un nodo único. | Los datos históricos de las métricas y las gráficas de Grafana son volátiles. Si el Pod de Prometheus se reinicia por mantenimiento, el histórico se destruye. | Aceptable para los propósitos del reto técnico. El objetivo primordial es demostrar la capacidad de captura de métricas en tiempo real y el funcionamiento del Scraping síncrono.                                                                                                                                                                                             |
| **Aislamiento de Entornos por Namespaces en un Nodo Único** | Reducción drástica de costos operativos en AWS utilizando una única instancia EC2 (con K3s) para simular el comportamiento de aislamiento de entornos. | Compartición del mismo plano de datos y recursos de CPU/Memoria subyacentes entre el tráfico de la aplicación y las herramientas de monitoreo. | Mitigado mediante la asignación estricta de límites de recursos (`limits` y `requests`) en el manifiesto del `Deployment`, impidiendo que un componente sature al otro.                                                                                                                                                                                                       |
---
### Autor
 Software Engineer: Santiago Andres 