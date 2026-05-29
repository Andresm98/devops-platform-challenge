#!/usr/bin/env python3
import requests
import sys
import json
import os
import logging
import http.client as http_client
from dotenv import load_dotenv

# ===== VERBOSE HTTP (equivalente a curl -v) =====
http_client.HTTPConnection.debuglevel = 1

logging.basicConfig(level=logging.DEBUG)

logging.getLogger("urllib3").setLevel(logging.DEBUG)
logging.getLogger("urllib3").propagate = True
# ================================================
load_dotenv()

# 2. Extraer variables SIN valores por defecto/harcodeados
# Si la variable no existe en el .env, el valor será None
APP_HOST = os.getenv("APP_HOST")
APP_PORT = os.getenv("APP_PORT")
API_KEY = os.getenv("API_KEY")
CLIENT_NAME = "NTT-Data-Test-Client"

def run_automation():
    # VALIDACIÓN CRÍTICA: Si falta algo en el .env, el script se detiene.
    missing_vars = [v for v in ["APP_HOST", "APP_PORT", "API_KEY"] if not os.getenv(v)]
    if missing_vars:
        print(f" ERROR: Faltan variables en el .env: {', '.join(missing_vars)}")
        print("Asegúrate de que tu archivo .env existe y está configurado. El proceso CD lo hará por ti, pero si estás ejecutando localmente, debes crearlo manualmente.")
        sys.exit(1)

    base_url = f"http://{APP_HOST}:{APP_PORT}/DevOps"

    print(f"--- Iniciando Flujo DevOps ---")
    print(f" Objetivo: {base_url}")

    try:
        # PASO 1: Obtener Token JWT, expira en 10 minutos y es necesario para la autenticación
        print("ETAPA [1/2] Solicitando JWT...")
        r_auth = requests.post(
            f"{base_url}/auth/token",
            json={"clientName": CLIENT_NAME},
            timeout=5
        )
        r_auth.raise_for_status()
        token = r_auth.json().get("token")

        # PASO 2: Usar Token
        print("ETAPA [2/2] Enviando data al endpoint...")
        headers = {
            "X-Parse-REST-API-Key": API_KEY,
            "X-JWT-KWY": token,
            "Content-Type": "application/json"
        }

        payload = {
            "message": "This is a test",
            "to": "Juan Perez",
            "from": "Rita Asturia",
            "timeToLifeSec": 45
        }

        r_final = requests.post(base_url, json=payload, headers=headers, timeout=5)

        print("\n RESULTADO DE LA CONEXIÓN:")
        print(json.dumps(r_final.json() if r_final.ok else r_final.text, indent=2))

        print("\n=== REQUEST HEADERS ===")
        print(json.dumps(headers, indent=2))

        print("\n=== REQUEST BODY ===")
        print(json.dumps(payload, indent=2))

        print("\n=== RESPONSE STATUS ===")
        print(r_final.status_code)

        print("\n=== RESPONSE HEADERS ===")
        print(dict(r_final.headers))

        print("\n=== RESPONSE BODY ===")
        print(r_final.text)
    except Exception as e:
        print(f"\n ERROR DE CONEXIÓN: {e}")
        sys.exit(1)

if __name__ == "__main__":
    run_automation()