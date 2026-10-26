param([switch]$SkipJdk)

$ErrorActionPreference = 'Stop'
$projectRoot = $PSScriptRoot
$lock = Get-Content (Join-Path $projectRoot 'dependencies.lock.json') -Raw | ConvertFrom-Json

foreach ($artifact in @($lock.mods) + $(if (!$SkipJdk) { @($lock.jdk) })) {
    $destination = Join-Path $projectRoot $artifact.path
    New-Item -ItemType Directory -Force (Split-Path $destination) | Out-Null
    if (!(Test-Path -LiteralPath $destination)) {
        Write-Host "Загрузка $($artifact.path)"
        Invoke-WebRequest -Uri $artifact.url -OutFile "$destination.download" -TimeoutSec 180
        if ((Get-FileHash "$destination.download" -Algorithm $artifact.algorithm).Hash -ne $artifact.hash) {
            throw "Контрольная сумма не совпала: $($artifact.path)"
        }
        Move-Item -LiteralPath "$destination.download" -Destination $destination
    }
    if ((Get-FileHash $destination -Algorithm $artifact.algorithm).Hash -ne $artifact.hash) {
        throw "Повреждён локальный файл: $($artifact.path)"
    }
}

if (!$SkipJdk) {
    $jdkDirectory = Join-Path $projectRoot '.cache/tools/jdk'
    $installed = @(Get-ChildItem $jdkDirectory -Directory -ErrorAction SilentlyContinue |
            Where-Object { Test-Path (Join-Path $_.FullName 'bin/java.exe') })
    if ($installed.Count -eq 0) {
        Expand-Archive -LiteralPath (Join-Path $projectRoot $lock.jdk.path) -DestinationPath $jdkDirectory
    }
}

Write-Host 'Локальные зависимости проверены. Сборка: .\gradlew.bat build'
