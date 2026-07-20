#!/bin/bash
# Runs once on first boot of a fresh Postgres volume (mounted into
# /docker-entrypoint-initdb.d by docker-compose.yaml).
#
# Postgres already creates POSTGRES_DB (keycloakDB) for Keycloak's backing store.
# The Spring Boot app uses a SEPARATE database (ticketDB) — create it here so the
# stack is runnable from a clean `docker compose up` with no manual steps.
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
    SELECT 'CREATE DATABASE "ticketDB"'
    WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'ticketDB')\gexec
EOSQL
