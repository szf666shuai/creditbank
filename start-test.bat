@echo off
setlocal EnableExtensions EnableDelayedExpansion
chcp 65001 >nul

set "ROOT=%~dp0"
set "DOCKER_DIR=%ROOT%docker"
set "BACKEND_DIR=%ROOT%backend"
set "FRONTEND_DIR=%ROOT%frontend"
set "DOCKER_DESKTOP=%ProgramFiles%\Docker\Docker\Docker Desktop.exe"

title Credit Bank - Start Test Environment
echo ========================================
echo   Credit Bank - Start Test Environment
echo ========================================
echo.

where docker >nul 2>nul
if errorlevel 1 (
    echo [ERROR] Docker was not found. Install Docker Desktop first.
    goto :failed
)
where java >nul 2>nul
if errorlevel 1 (
    echo [ERROR] Java was not found. Install JDK 17 or newer first.
    goto :failed
)
where node >nul 2>nul
if errorlevel 1 (
    echo [ERROR] Node.js was not found. Install Node.js 18 or newer first.
    goto :failed
)

echo [1/5] Checking Docker Desktop...
set /a WAITED=0
docker info >nul 2>nul
if errorlevel 1 (
    if exist "%DOCKER_DESKTOP%" (
        echo       Starting Docker Desktop...
        start "" "%DOCKER_DESKTOP%"
    ) else (
        echo [ERROR] Start Docker Desktop manually and try again.
        goto :failed
    )
)

:wait_docker
docker info >nul 2>nul
if not errorlevel 1 goto :docker_ready
timeout /t 3 /nobreak >nul
set /a WAITED+=3
if !WAITED! GEQ 180 (
    echo [ERROR] Docker Desktop did not start within 180 seconds.
    goto :failed
)
echo       Waiting for Docker... !WAITED!/180 seconds
goto :wait_docker

:docker_ready
echo       Docker is ready.

echo [2/5] Starting MySQL, Redis and Nginx...
pushd "%DOCKER_DIR%"
docker compose up -d
if errorlevel 1 (
    popd
    echo [ERROR] Docker services failed to start.
    goto :failed
)
popd

echo [3/5] Waiting for MySQL initialization...
set /a WAITED=0
:wait_mysql
set "MYSQL_STATUS="
for /f "usebackq delims=" %%S in (`docker inspect --format "{{.State.Health.Status}}" credit-bank-mysql 2^>nul`) do set "MYSQL_STATUS=%%S"
if /i "!MYSQL_STATUS!"=="healthy" goto :mysql_ready
timeout /t 3 /nobreak >nul
set /a WAITED+=3
if !WAITED! GEQ 120 (
    echo [ERROR] MySQL did not become healthy within 120 seconds.
    docker compose -f "%DOCKER_DIR%\docker-compose.yml" ps
    goto :failed
)
echo       Waiting for MySQL... !WAITED!/120 seconds
goto :wait_mysql

:mysql_ready
echo       MySQL is ready.

echo [4/5] Starting backend...
curl.exe -fsS http://localhost:8080/api/health >nul 2>nul
if not errorlevel 1 (
    echo       Backend is already running on port 8080. Skipping.
) else (
    set "PORT_8080_PID="
    for /f "usebackq delims=" %%P in (`powershell -NoProfile -Command "$connection = Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue ^| Select-Object -First 1; if ($connection) { $connection.OwningProcess }"`) do set "PORT_8080_PID=%%P"
    if defined PORT_8080_PID (
        echo [ERROR] Port 8080 is occupied by PID !PORT_8080_PID!.
        tasklist /FI "PID eq !PORT_8080_PID!"
        echo         Stop it with: taskkill /PID !PORT_8080_PID! /F
        goto :failed
    )
    start "Credit Bank Backend" /D "%BACKEND_DIR%" cmd /k "call mvnw.cmd spring-boot:run"
)

echo [5/5] Starting frontend...
if not exist "%FRONTEND_DIR%\node_modules" (
    echo       Installing frontend dependencies...
    pushd "%FRONTEND_DIR%"
    call npm install
    if errorlevel 1 (
        popd
        echo [ERROR] npm install failed.
        goto :failed
    )
    popd
)
start "Credit Bank Frontend" /D "%FRONTEND_DIR%" cmd /k "npm run dev"

echo.
echo ========================================
echo   Startup commands completed
echo ========================================
echo   Frontend: https://localhost:5173
echo   Health:   http://localhost:8080/api/health
echo   Account:  admin / admin123
echo.
echo Keep the backend and frontend windows open.
echo First visit may show a self-signed HTTPS warning; click Advanced - Continue.
echo The first backend startup may take several minutes.
echo.
pause
exit /b 0

:failed
echo.
echo Startup failed. Fix the error above and try again.
pause
exit /b 1