@echo off
chcp 65001 >nul
echo ========================================
echo  终身学习学分银行平台 - 一键启动脚本
echo ========================================

echo.
echo [1/3] 启动 Docker 基础服务 (MySQL + Redis + Nginx)...
cd /d "%~dp0docker"
docker compose up -d
if ERRORLEVEL 1 (
    echo [警告] Docker 启动失败，请确认 Docker Desktop 已运行
)

echo.
echo [2/3] 等待 MySQL 初始化 (15秒)...
timeout /t 15 /nobreak >nul

echo.
echo [3/3] 请在两个终端分别执行:
echo   后端: cd backend ^&^& ..\tools\apache-maven-3.9.6\bin\mvn.cmd spring-boot:run
echo   前端: cd frontend ^&^& npm run dev
echo.
echo 访问地址:
echo   前端: http://localhost:5173
echo   后端: http://localhost:8080/api/health
echo   Nginx: http://localhost:80
pause
