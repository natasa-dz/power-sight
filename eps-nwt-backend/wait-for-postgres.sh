#!/bin/sh
# wait-for-postgres.sh

set -e

HOST="$POSTGRES_HOST"
PORT=5432

echo "Waiting for Postgres at $HOST:$PORT..."

# Loop until the port is open
while ! nc -z "$HOST" "$PORT"; do
  sleep 1
done

echo "Postgres is ready. Starting app..."
exec java -jar /app.jar
