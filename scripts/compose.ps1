param(
    [Parameter(Position = 0)]
    [ValidateSet("up", "up-db", "down", "logs", "logs-app", "logs-db", "reset", "restart", "ps", "status", "health", "open", "swagger", "swagger-docs", "config", "help")]
    [string]$Command = "help",

    [Parameter(Position = 1)]
    [ValidateSet("app", "db", "all")]
    [string]$Service = "all"
)

$ErrorActionPreference = "Stop"

$rootDir = Split-Path -Parent $PSScriptRoot
Set-Location $rootDir

switch ($Command) {
    "up" {
        docker compose up --build -d
    }
    "up-db" {
        docker compose up -d db
    }
    "down" {
        docker compose down
    }
    "logs" {
        docker compose logs -f --tail=200
    }
    "logs-app" {
        docker compose logs -f --tail=200 app
    }
    "logs-db" {
        docker compose logs -f --tail=200 db
    }
    "reset" {
        docker compose down -v
        docker compose up --build -d
    }
    "restart" {
        if ($Service -eq "all") {
            docker compose restart
        }
        else {
            docker compose restart $Service
        }
    }
    "ps" {
        docker compose ps
    }
    "status" {
        docker compose ps
    }
    "health" {
        $uris = @(
            "http://localhost:58080/api/health",
            "http://localhost:58080/actuator/health",
            "http://localhost:8080/api/health",
            "http://localhost:8080/actuator/health"
        )

        foreach ($uri in $uris) {
            try {
                Invoke-RestMethod -Uri $uri -Method Get | ConvertTo-Json -Depth 10
                exit 0
            }
            catch {
                # Try next endpoint.
            }
        }

        Write-Error "Health check failed. Tried /api/health and /actuator/health on localhost:58080 and localhost:8080"
        exit 1
    }
    "open" {
        $url = "http://localhost:58080/api/health"
        Start-Process $url
        Write-Host "Opened: $url"
    }
    "swagger" {
        $url = "http://localhost:58081"
        Start-Process $url
        Write-Host "Opened: $url"
    }
    "swagger-docs" {
        $url = "http://localhost:58080/v3/api-docs"
        Start-Process $url
        Write-Host "Opened: $url"
    }
    "config" {
        docker compose config
    }
    default {
        @"
Usage: .\scripts\compose.ps1 <command>

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
"@ | Write-Host
    }
}

