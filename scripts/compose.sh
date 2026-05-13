#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

command="${1:-help}"
service="${2:-all}"

case "$command" in
  up)
    docker compose up --build -d
    ;;
  up-db)
    docker compose up -d db
    ;;
  down)
    docker compose down
    ;;
  logs)
    docker compose logs -f --tail=200
    ;;
  logs-app)
    docker compose logs -f --tail=200 app
    ;;
  logs-db)
    docker compose logs -f --tail=200 db
    ;;
  reset)
    docker compose down -v
    docker compose up --build -d
    ;;
  restart)
    case "$service" in
      app|db)
        docker compose restart "$service"
        ;;
      all)
        docker compose restart
        ;;
      *)
        echo "Invalid service for restart: $service (allowed: app, db, all)" >&2
        exit 1
        ;;
    esac
    ;;
  ps)
    docker compose ps
    ;;
  status)
    docker compose ps
    ;;
  health)
    if curl -fsS "http://localhost:58080/api/health" || \
       curl -fsS "http://localhost:58080/actuator/health" || \
       curl -fsS "http://localhost:8080/api/health" || \
       curl -fsS "http://localhost:8080/actuator/health"; then
      echo
      exit 0
    fi

    echo "Health check failed. Tried /api/health and /actuator/health on localhost:58080 and localhost:8080" >&2
    exit 1
    ;;
  open)
    url="http://localhost:58080/api/health"

    if command -v xdg-open >/dev/null 2>&1; then
      xdg-open "$url" >/dev/null 2>&1 || true
    elif command -v open >/dev/null 2>&1; then
      open "$url" >/dev/null 2>&1 || true
    elif command -v cmd.exe >/dev/null 2>&1; then
      cmd.exe /c start "" "$url" >/dev/null 2>&1 || true
    else
      echo "No opener found. Open this URL manually: $url"
      exit 1
    fi

    echo "Opened: $url"
    ;;
  swagger)
    url="http://localhost:58081"

    if command -v xdg-open >/dev/null 2>&1; then
      xdg-open "$url" >/dev/null 2>&1 || true
    elif command -v open >/dev/null 2>&1; then
      open "$url" >/dev/null 2>&1 || true
    elif command -v cmd.exe >/dev/null 2>&1; then
      cmd.exe /c start "" "$url" >/dev/null 2>&1 || true
    else
      echo "No opener found. Open this URL manually: $url"
      exit 1
    fi

    echo "Opened: $url"
    ;;
  swagger-docs)
    url="http://localhost:58080/v3/api-docs"

    if command -v xdg-open >/dev/null 2>&1; then
      xdg-open "$url" >/dev/null 2>&1 || true
    elif command -v open >/dev/null 2>&1; then
      open "$url" >/dev/null 2>&1 || true
    elif command -v cmd.exe >/dev/null 2>&1; then
      cmd.exe /c start "" "$url" >/dev/null 2>&1 || true
    else
      echo "No opener found. Open this URL manually: $url"
      exit 1
    fi

    echo "Opened: $url"
    ;;
  config)
    docker compose config
    ;;
  help|--help|-h)
    cat <<'EOF'
Usage: ./scripts/compose.sh <command>

Commands:
  up      Build and start app + database in background
  up-db   Start only the PostgreSQL service
  down    Stop and remove containers/network
  logs    Follow logs from all services
  logs-app Follow logs from app service
  logs-db Follow logs from db service
  reset   Recreate everything and clear database volume
  restart [app|db|all] Restart one service or all services
  ps      Show service status
  status  Alias for ps
  health  Check API health endpoint (localhost:58080, fallback localhost:8080)
  open    Open health endpoint in default browser
  swagger Open Swagger UI in default browser
  swagger-docs Open OpenAPI JSON in default browser
  config  Validate and print merged compose config
EOF
    ;;
  *)
    echo "Unknown command: $command" >&2
    echo "Run './scripts/compose.sh help' for usage." >&2
    exit 1
    ;;
esac

