#!/bin/bash
# Actualización básica del sistema operativo base
apt-get update -y && apt-get upgrade -y

# Instalación de utilitarios ligeros indispensables
apt-get install -y curl wget git

# 1. Crear el directorio de configuración para K3s
mkdir -p /etc/rancher/k3s

# 2. Escribir la configuración limpia en YAML (Evita problemas de comillas y caracteres especiales)
cat << 'EOF' > /etc/rancher/k3s/config.yaml
disable:
  - traefik
  - servicelb
kubelet-arg:
  - "eviction-hard=memory.available<${eviction_threshold}"
EOF

# 3. Despliegue de K3s (Detectará automáticamente el archivo config.yaml anterior)
curl -sfL https://get.k3s.io | sh -

# Garantizar permisos de lectura globales de la config para automatizaciones locales rápidas
chmod 644 /etc/rancher/k3s/k3s.yaml