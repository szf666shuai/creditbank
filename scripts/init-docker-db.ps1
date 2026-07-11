<#
.SYNOPSIS
  Use Docker MySQL to create the credit_bank database and run all SQL scripts.

.USAGE
  powershell -ExecutionPolicy Bypass -File .\scripts\init-docker-db.ps1

  To rebuild a clean database before importing:
  powershell -ExecutionPolicy Bypass -File .\scripts\init-docker-db.ps1 -ResetDatabase
#>
param(
    [switch]$ResetDatabase
)

$ErrorActionPreference = 'Stop'

$ProjectRoot = Split-Path -Parent $PSScriptRoot
$DockerDir = Join-Path $ProjectRoot 'docker'
$InitSql = Join-Path $DockerDir 'mysql\init\01-init.sql'
$MigrationsDir = Join-Path $DockerDir 'mysql\migrations'

$ContainerName = 'credit-bank-mysql'
$RootPassword = 'root123456'
$DatabaseName = 'credit_bank'

function Invoke-DockerMysql {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Sql
    )

    $Sql | docker exec -i -e "MYSQL_PWD=$RootPassword" $ContainerName mysql --default-character-set=utf8mb4 -uroot
}

function Invoke-DockerMysqlFile {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Path,
        [string]$Database
    )

    Write-Host ">> Running SQL: $Path"
    $remoteDir = '/tmp/creditbank-sql'
    $remotePath = "$remoteDir/$([IO.Path]::GetFileName($Path))"
    docker exec $ContainerName mkdir -p $remoteDir
    docker cp $Path "${ContainerName}:$remotePath"
    if ($Database) {
        docker exec -e "MYSQL_PWD=$RootPassword" $ContainerName mysql --default-character-set=utf8mb4 -uroot $Database -e "source $remotePath"
    } else {
        docker exec -e "MYSQL_PWD=$RootPassword" $ContainerName mysql --default-character-set=utf8mb4 -uroot -e "source $remotePath"
    }
}

if (-not (Test-Path $InitSql)) {
    throw "Init SQL file not found: $InitSql"
}

if (-not (Test-Path $MigrationsDir)) {
    throw "Migrations directory not found: $MigrationsDir"
}

Write-Host '>> Starting Docker MySQL / Redis containers...'
Push-Location $DockerDir
try {
    docker compose up -d mysql redis
} finally {
    Pop-Location
}

Write-Host '>> Waiting for MySQL to become ready...'
$ready = $false
for ($i = 1; $i -le 60; $i++) {
    docker exec -e "MYSQL_PWD=$RootPassword" $ContainerName mysqladmin --default-character-set=utf8mb4 ping -uroot --silent 1>$null 2>$null
    if ($LASTEXITCODE -eq 0) {
        $ready = $true
        break
    }
    Start-Sleep -Seconds 2
}

if (-not $ready) {
    throw 'MySQL container did not become ready in time. Check: docker logs credit-bank-mysql'
}

if ($ResetDatabase) {
    Write-Host ">> Recreating database: $DatabaseName"
    Invoke-DockerMysql -Sql @"
DROP DATABASE IF EXISTS $DatabaseName;
CREATE DATABASE $DatabaseName DEFAULT CHARACTER SET utf8mb4 DEFAULT COLLATE utf8mb4_unicode_ci;
"@
}

Write-Host '>> Importing full initialization script...'
Invoke-DockerMysqlFile -Path $InitSql

$migrationFiles = Get-ChildItem -Path $MigrationsDir -Filter '*.sql' -File | Sort-Object Name
foreach ($file in $migrationFiles) {
    Invoke-DockerMysqlFile -Path $file.FullName -Database $DatabaseName
}

Write-Host '>> Checking database table count...'
$tableCount = docker exec -e "MYSQL_PWD=$RootPassword" $ContainerName mysql --default-character-set=utf8mb4 -uroot -N -B -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = '$DatabaseName';"

Write-Host ''
Write-Host 'Done.'
Write-Host "  Database: $DatabaseName"
Write-Host "  Table count: $tableCount"
Write-Host '  MySQL:  localhost:3307'
Write-Host '  User:   creditbank'
Write-Host '  Pass:   creditbank123'
