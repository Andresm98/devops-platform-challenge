

# ADR-003: Aprovisionamiento Automatizado (CD), Orquestación de Contenedores en AWS, Enrutamiento Perimetral y Observabilidad SRE Pragmática

## Estado

Aceptado

## Fecha

2026-05-28

## Contexto

Una vez que el artefacto ha sido validado y empaquetado en la fase de CI, la plataforma necesita ser desplegada de manera idempotente en la nube de AWS. Este despliegue debe asegurar alta disponibilidad, autoescalado elástico ante ráfagas de tráfico, enrutamiento seguro de las APIs de negocio a través de la ruta `/DevOps`, y visibilidad operativa inmediata orientada a las **4 Señales de Oro de SRE** sin sobrecargar económicamente o a nivel de cómputo la infraestructura unificada del nodo.

## Decisión

Se implementa una estrategia de Entrega Continua (CD) y Operación Pragmática basada en los siguientes pilares:

1. **Infraestructura e Idempotencia Cloud (Terraform + K3s):**
* Automatización de la infraestructura en AWS mediante Terraform. El pipeline de CD ejecuta `terraform apply -auto-approve` en la ruta `terraform/environments/prod`.
* Captura dinámica de la IP pública del servidor a través de `terraform output -raw k3s_server_ip` para orquestar los pasos subsecuentes de configuración mediante SSH seguro.


2. **Estrategia de Despliegue Atómico (Zero-Downtime):**
* Uso de Git en el servidor remoto para descargar de manera limpia el estado deseado de los manifiestos alojados en la rama **`main`**.
* Sustitución atómica en caliente de la imagen del contenedor reemplazando `latest` por el hash inmutable auditado (`${{ github.sha }}`) mediante `sed`.
* Ejecución declarativa mediante **Kustomize** (`kubectl apply -k .`).
* Inyección de una compuerta anti-caídas mediante `kubectl rollout status`. Si los nuevos Pods no superan los *Liveness* o *Readiness Probes*, Kubernetes aborta el despliegue manteniendo intacta la versión anterior.


3. **Enrutamiento y Perímetro de Red Declarativo (Kong Gateway):**
* Instalación automatizada de **Kong Ingress Controller** en modo sin base de datos (DB-less) en el namespace `kong`.
* Despliegue de un recurso de tipo `Ingress` con `ingressClassName: kong` en el namespace `devops-app`, exponiendo de forma segura el endpoint `/DevOps` hacia el balanceador del clúster sin exponer puertos internos de la JVM.


4. **Seguridad y Desacoplamiento de Configuraciones (ConfigMaps y Secrets):**
* Migración de las variables de entorno de `Docker-Compose` hacia objetos nativos del clúster.
* Las configuraciones del entorno (como los niveles de LOG) se almacenan en un `ConfigMap`.
* Las llaves criptográficas (`JWT_SECRET` y `API_KEY`) se inyectan cifradas en Base64 mediante un archivo `Secret`. Se exponen directamente en la memoria RAM de los contenedores a través de la directiva `envFrom` en el Deployment.


5. **Escalabilidad Elástica de Carga:**
* Configuración de un **Horizontal Pod Autoscaler (HPA)** nativo que monitorea las métricas del servidor de métricas de Kubernetes (`metrics-server`). Si el promedio de uso de CPU excede el **75%**, el clúster clona dinámicamente instancias de la aplicación desde un mínimo de 2 pods hasta un tope de 6 pods en paralelo.


6. **Observabilidad Pragmática y SRE (Prometheus + Grafana):**
* Configuración e inyección del plugin de registro de Micrometer (`micrometer-registry-prometheus`) y exposición de endpoints controlados en la configuración de Spring Boot (`management.endpoints.web.exposure.include=health,info,prometheus`).
* Inclusión de anotaciones de Scraping dinámico (`prometheus.io/scrape: "true"`) en la metadata del Deployment.
* Despliegue de un stack ultraligero compuesto por Prometheus Server y Grafana dentro de un namespace aislado llamado `monitoring`.
* Importación de un SRE Dashboard corporativo en formato JSON listo para Grafana, rastreando la Disponibilidad del Servicio (%), el Rendimiento (Throughput), la Latencia Promedio y la Tasa de errores HTTP 5xx.



---

## Trade-offs / Consecuencias

### Positivas

* **Cero Downtime Garantizado:** El uso coordinado de Kustomize, probes de salud y `rollout status` asegura que los usuarios de banca nunca perciban lentitud o interrupciones durante las ventanas de despliegue.
* **Gestión Declarativa Total:** Toda la topología de la red (Kong), seguridad (Secrets), escalabilidad (HPA) y monitoreo (Prometheus) se maneja como código, asegurando la reproducibilidad total del clúster en cualquier otra región de AWS.
* **Cumplimiento de Estándares SRE:** El Dashboard integrado de Grafana permite realizar análisis de causa raíz rápidos (RCA) ante picos de errores simulados por la mesa evaluadora.

### Negativas / Costes (Trade-offs Críticos)

* **Riesgo Operativo en Modo DB-less de Kong:** Al configurar Kong Ingress Controller de forma declarativa y ligera sin una base de datos Postgres corporativa, los cambios de configuración se aplican de manera global recargando la configuración en memoria. Esto es ideal para ahorrar memoria RAM y almacenamiento en la instancia EC2 t2.micro/t3.medium, pero impide persistir configuraciones personalizadas del ciclo de vida de Kong por fuera de la API nativa de Kubernetes.
* **Persistencia de Métricas Volátil:** El almacenamiento de Prometheus se configuró utilizando `emptyDir: {}` para evitar la sobrecarga y los costos asociados con el aprovisionamiento dinámico de volúmenes elásticos de AWS (EBS/EFS). **Trade-off:** Si el pod de Prometheus se reinicia, los datos históricos de métricas se perderán, manteniendo únicamente los datos recopilados en tiempo real desde el último arranque. Esto se acepta deliberadamente debido al enfoque pragmático y de bajo consumo de recursos exigido por el entorno del reto.
* **Sobrecarga de Red por Scraping Síncrono:** La recolección de métricas a través del endpoint `/actuator/prometheus` ocurre de forma síncrona cada 15 segundos. En momentos de alto tráfico masivo, este intervalo genera consumo adicional de procesamiento CPU en la máquina virtual de Java, lo cual fue mitigado asignando límites de recursos holgados (`512Mi` de RAM y `500m` de CPU) en el archivo de Deployment.

---

## Alternativas consideradas

* **ArgoCD / FluxCD:** Descartados para el flujo de CD en favor de un script directo vía SSH-Action en GitHub Actions. Aunque las herramientas de GitOps son el estándar enterprise actual, introducen controladores pesados adicionales que comprometen la estabilidad de la memoria RAM del nodo único disponible en el reto.

---
**Software Engineer:** Santiago Moreta