#Requires -RunAsAdministrator
<#
.SYNOPSIS
  Docker 不可用时，用本机 MySQL80 初始化开发数据库。

.USAGE
  右键「以管理员身份运行 PowerShell」，执行：
  Set-ExecutionPolicy -Scope Process Bypass -Force
  .\scripts\setup-local-mysql.ps1 -RootPassword "你的root密码"

  若忘记 root 密码，请用 MySQL Workbench 重置后再运行本脚本。
#>
param(
    [Parameter(Mandatory = $true)]
    [string]$RootPassword
)

$ErrorActionPreference = 'Stop'
$ProjectRoot = Split-Path -Parent $PSScriptRoot
$SqlFile = Join-Path $ProjectRoot 'docker\mysql\init\01-init.sql'
$MysqlBin = 'C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe'

if (-not (Test-Path $MysqlBin)) {
    Write-Error "未找到 MySQL：$MysqlBin"
}

Write-Host '>> 启动 MySQL80 服务...'
Start-Service MySQL80
Start-Sleep -Seconds 3
if ((Get-Service MySQL80).Status -ne 'Running') {
    Write-Error 'MySQL80 启动失败，请检查服务或 my.ini 配置'
}

Write-Host '>> 创建数据库与用户...'
$bootstrap = @"
CREATE DATABASE IF NOT EXISTS credit_bank DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'creditbank'@'localhost' IDENTIFIED BY 'creditbank123';
CREATE USER IF NOT EXISTS 'creditbank'@'%' IDENTIFIED BY 'creditbank123';
GRANT ALL PRIVILEGES ON credit_bank.* TO 'creditbank'@'localhost';
GRANT ALL PRIVILEGES ON credit_bank.* TO 'creditbank'@'%';
FLUSH PRIVILEGES;
"@

& $MysqlBin -uroot "-p$RootPassword" -e $bootstrap

if (-not (Test-Path $SqlFile)) {
    Write-Error "未找到初始化脚本：$SqlFile"
}

Write-Host '>> 导入表结构与初始数据（可能需要 1-2 分钟）...'
Get-Content $SqlFile -Raw -Encoding UTF8 | & $MysqlBin -uroot "-p$RootPassword" credit_bank

Write-Host ''
Write-Host '完成！连接信息：'
Write-Host '  主机: localhost:3306'
Write-Host '  数据库: credit_bank'
Write-Host '  用户名: creditbank'
Write-Host '  密码: creditbank123'
Write-Host ''
Write-Host '测试账号: student1 / admin123'
Write-Host '启动后端: cd backend; ..\tools\apache-maven-3.9.6\bin\mvn.cmd spring-boot:run'
