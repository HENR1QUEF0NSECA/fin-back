# Script para facilitar execução do Docker Compose
# Use: .\docker-start.ps1

param(
    [string]$action = "up",
    [switch]$detach = $false
)

Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "Finance App - Docker Manager" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

$actions = @("up", "down", "restart", "logs", "status")

if ($action -eq "help" -or $action -eq "?") {
    Write-Host "Uso: .\docker-start.ps1 [acao] [opcoes]" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Acoes disponíveis:" -ForegroundColor Yellow
    Write-Host "  up          - Inicia os containers (padrão)" -ForegroundColor Green
    Write-Host "  down        - Para os containers" -ForegroundColor Red
    Write-Host "  restart     - Reinicia os containers" -ForegroundColor Yellow
    Write-Host "  logs        - Mostra logs em tempo real" -ForegroundColor Blue
    Write-Host "  status      - Mostra status dos containers" -ForegroundColor Magenta
    Write-Host ""
    Write-Host "Exemplos:" -ForegroundColor Cyan
    Write-Host "  .\docker-start.ps1                # Inicia em foreground" -ForegroundColor Gray
    Write-Host "  .\docker-start.ps1 up -detach     # Inicia em background" -ForegroundColor Gray
    Write-Host "  .\docker-start.ps1 down           # Para" -ForegroundColor Gray
    Write-Host "  .\docker-start.ps1 logs           # Mostra logs" -ForegroundColor Gray
    exit 0
}

switch ($action.ToLower()) {
    "up" {
        # O docker-compose.yml le DB_PASSWORD e JWT_SECRET do arquivo .env
        if (-not (Test-Path ".env")) {
            if (-not (Test-Path ".env.example")) {
                Write-Host "[!] Arquivo .env nao encontrado (nem o .env.example para copiar)" -ForegroundColor Red
                exit 1
            }
            Copy-Item ".env.example" ".env"
            Write-Host "[!] Arquivo .env criado a partir do .env.example" -ForegroundColor Yellow
            Write-Host "    Revise os valores antes de subir, principalmente:" -ForegroundColor Yellow
            Write-Host "    - DB_PASSWORD: senha do Postgres (vale na primeira criacao do volume)" -ForegroundColor Yellow
            Write-Host "    - JWT_SECRET: no minimo 32 caracteres (ex.: openssl rand -base64 48)" -ForegroundColor Yellow
            Write-Host "    Depois rode de novo: .\docker-start.ps1 up" -ForegroundColor Cyan
            exit 0
        }

        Write-Host "[*] Iniciando containers..." -ForegroundColor Blue
        Write-Host "    - PostgreSQL em localhost:5432" -ForegroundColor Gray
        Write-Host "    - API em http://localhost:8080" -ForegroundColor Gray
        Write-Host ""

        if ($detach) {
            Write-Host "[*] Modo background (-d)" -ForegroundColor Cyan
            docker-compose up -d --build
        } else {
            Write-Host "[*] Modo foreground (pressione Ctrl+C para parar)" -ForegroundColor Cyan
            docker-compose up --build
        }
        break
    }
    "down" {
        Write-Host "[*] Parando containers..." -ForegroundColor Yellow
        docker-compose down
        Write-Host "[✓] Containers parados" -ForegroundColor Green
        break
    }
    "restart" {
        Write-Host "[*] Reiniciando containers..." -ForegroundColor Yellow
        docker-compose restart
        Write-Host "[✓] Containers reiniciados" -ForegroundColor Green
        break
    }
    "logs" {
        Write-Host "[*] Mostrando logs (_detendo com Ctrl+C)..." -ForegroundColor Blue
        docker-compose logs -f
        break
    }
    "status" {
        Write-Host "[*] Status dos containers:" -ForegroundColor Blue
        docker-compose ps
        break
    }
    default {
        Write-Host "[!] Acao desconhecida: '$action'" -ForegroundColor Red
        Write-Host "[*] Digite '.\docker-start.ps1 help' para ver opcoes disponíveis" -ForegroundColor Yellow
        exit 1
    }
}

