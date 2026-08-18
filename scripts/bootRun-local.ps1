# Loads gitignored .env into the process, then runs Spring Boot (local profile).
$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
Set-Location $root

$envFile = Join-Path $root ".env"
if (Test-Path $envFile) {
    Get-Content $envFile | ForEach-Object {
        $line = $_.Trim()
        if ($line -eq "" -or $line.StartsWith("#")) { return }
        $parts = $line -split "=", 2
        if ($parts.Count -eq 2) {
            [System.Environment]::SetEnvironmentVariable($parts[0].Trim(), $parts[1].Trim(), "Process")
        }
    }
    Write-Host "Loaded environment from .env"
} else {
    Write-Host "Warning: .env not found. Mail credentials may be missing."
}

if (-not $env:JAVA_HOME -or -not (Test-Path $env:JAVA_HOME)) {
    $candidate = "C:\Users\G29\.jdks\corretto-17.0.19"
    if (Test-Path $candidate) { $env:JAVA_HOME = $candidate }
}
$env:Path = "$env:JAVA_HOME\bin;" + $env:Path

.\gradlew.bat bootRun --args="--spring.profiles.active=local"
