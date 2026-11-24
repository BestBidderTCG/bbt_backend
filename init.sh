#!/bin/bash

if [ -f .env ]; then
    set -a
    source .env
    set +a
fi

# --- Notificaciones push (FCM) ---
# Valores por defecto: desactivado. Se activan desde .env o desde la terminal.
export FIREBASE_ENABLED="${FIREBASE_ENABLED:-false}"
export PUSH_TEST_ENDPOINT="${PUSH_TEST_ENDPOINT:-false}"

if [ "$FIREBASE_ENABLED" = "true" ]; then
    if [ -z "$FIREBASE_CREDENTIALS_JSON" ] && [ ! -f "$FIREBASE_CREDENTIALS_PATH" ]; then
        echo "ERROR: FIREBASE_ENABLED=true pero no hay credenciales."
        echo "Define FIREBASE_CREDENTIALS_PATH (ruta a un fichero existente) o FIREBASE_CREDENTIALS_JSON en .env"
        exit 1
    fi
    echo "FCM activado (endpoint de prueba: $PUSH_TEST_ENDPOINT)"
else
    echo "FCM desactivado: las push solo se escriben en el log"
fi

echo "Levantando PostgreSQL..."
docker compose up -d

echo "Esperando a que PostgreSQL este listo..."
until docker exec -i bbt_postgres pg_isready -U user -d bestbidder > /dev/null 2>&1; do
    sleep 1
done

echo "PostgreSQL listo. Arrancando backend..."
JAVA_HOME=$(dirname $(dirname $(readlink -f $(which java)))) ./gradlew bootRun