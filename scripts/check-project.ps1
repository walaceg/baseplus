param(
    [switch]$SkipBackend,
    [switch]$SkipFrontend
)

$ErrorActionPreference = "Stop"

$Root = Split-Path -Parent $PSScriptRoot
$Backend = Join-Path $Root "baseplus-backend"
$Frontend = Join-Path $Root "baseplus-frontend"

function Invoke-Step {
    param(
        [string]$Name,
        [scriptblock]$Action
    )

    Write-Host ""
    Write-Host "==> $Name" -ForegroundColor Cyan
    & $Action
}

function Resolve-MavenCommand {
    $mvnw = Join-Path $Backend "mvnw.cmd"
    if (Test-Path $mvnw) {
        return $mvnw
    }

    $mvn = Get-Command "mvn.cmd" -ErrorAction SilentlyContinue
    if ($mvn) {
        return $mvn.Source
    }

    $mvn = Get-Command "mvn" -ErrorAction SilentlyContinue
    if ($mvn) {
        return $mvn.Source
    }

    $knownMaven = "C:\Users\Walace\.m2\wrapper\dists\apache-maven-3.9.14-bin\1cb7fhup6b5n3bed6kckbrnspv\apache-maven-3.9.14\bin\mvn.cmd"
    if (Test-Path $knownMaven) {
        return $knownMaven
    }

    $mavenFromWrapperCache = Get-ChildItem -Path "$env:USERPROFILE\.m2\wrapper\dists" -Recurse -Filter "mvn.cmd" -ErrorAction SilentlyContinue |
        Select-Object -First 1
    if ($mavenFromWrapperCache) {
        return $mavenFromWrapperCache.FullName
    }

    throw "Maven nao encontrado. Instale Maven ou adicione mvn ao PATH."
}

function Invoke-NativeCommand {
    param(
        [string]$Command,
        [string[]]$Arguments
    )

    & $Command @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "Comando falhou com codigo ${LASTEXITCODE}: $Command $($Arguments -join ' ')"
    }
}

function Initialize-JavaHome {
    if ($env:JAVA_HOME -and ($env:JAVA_HOME.EndsWith("\bin") -or $env:JAVA_HOME.EndsWith("/bin"))) {
        $candidateHome = Split-Path -Parent $env:JAVA_HOME
        if (Test-Path (Join-Path $candidateHome "bin\java.exe")) {
            $env:JAVA_HOME = $candidateHome
            $env:Path = "$candidateHome\bin;$env:Path"
            return
        }
    }

    if ($env:JAVA_HOME -and (Test-Path (Join-Path $env:JAVA_HOME "bin\java.exe"))) {
        return
    }

    $knownJavaHome = "C:\Program Files\Java\jdk-23"
    if (Test-Path $knownJavaHome) {
        $env:JAVA_HOME = $knownJavaHome
        $env:Path = "$knownJavaHome\bin;$env:Path"
    }
}

function Resolve-NpmCommand {
    $npm = Get-Command "npm.cmd" -ErrorAction SilentlyContinue
    if ($npm) {
        return $npm.Source
    }

    $npm = Get-Command "npm" -ErrorAction SilentlyContinue
    if ($npm) {
        return $npm.Source
    }

    throw "npm nao encontrado. Instale Node.js/npm ou adicione npm ao PATH."
}

if (-not $SkipBackend) {
    Invoke-Step "Backend tests" {
        Initialize-JavaHome
        $maven = Resolve-MavenCommand
        Push-Location $Backend
        try {
            Invoke-NativeCommand $maven @("test")
        } finally {
            Pop-Location
        }
    }
}

if (-not $SkipFrontend) {
    Invoke-Step "Frontend build" {
        $npm = Resolve-NpmCommand
        Push-Location $Frontend
        try {
            Invoke-NativeCommand $npm @("run", "build")
        } finally {
            Pop-Location
        }
    }
}

Write-Host ""
Write-Host "Base+ validation finished." -ForegroundColor Green
