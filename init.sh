#!/bin/bash

echo "Levantando PostgreSQL..."
docker compose up -d

echo "Esperando a que PostgreSQL este listo..."
until docker exec -i bbt_postgres pg_isready -U user -d bestbidder > /dev/null 2>&1; do
    sleep 1
done

echo "PostgreSQL listo. Arrancando backend..."
JAVA_HOME=$(dirname $(dirname $(readlink -f $(which java)))) ./gradlew bootRun