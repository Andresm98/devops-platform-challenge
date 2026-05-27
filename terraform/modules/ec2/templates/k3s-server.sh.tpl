#!/bin/bash
# Actualización básica del sistema operativo base
apt-get update -y && apt-get upgrade -y

# Instalación de utilitarios ligeros indispensables
apt-get install -y curl wget git

# Despliegue optimizado de K3s con exclusión de componentes redundantes
curl -sfL https://get.k3s.io | INSTALL_K3S_EXEC="--disable traefik --disable servicelb --kubelet-arg='eviction-hard=memory.available<${eviction_threshold}'" sh -

# Garantizar permisos de lectura globales de la config para automatizaciones locales rápidas
chmod 644 /etc/rancher/k3s/k3s.yaml