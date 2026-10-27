[CmdletBinding()]
param(
    [ValidateRange(15, 600)][int]$StartupTimeoutSeconds = 240
)

$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'PhotoConnect.LocalConfig.ps1')

$projectRoot = Get-PhotoConnectProjectRoot -ScriptDirectory $PSScriptRoot
$paths = Get-PhotoConnectLocalPaths -ProjectRoot $projectRoot
$values = Resolve-PhotoConnectLaunchConfig -Values (Read-PhotoConnectConfig -SecretsPath $paths.Secrets)

function Get-JavaMajorVersion {
    param([string]$JavaExecutable)
    $output = cmd /d /c "`"$javaExecutable`" -version 2>&1" | Out-String
    if ($output -match 'version\s+"(?<major>\d+)') { return [int]$Matches.major }
    return 0
}

$javaCandidates = @()
if ($env:JAVA_HOME) { $javaCandidates += (Join-Path $env:JAVA_HOME 'bin\java.exe') }
$javaCandidates += @(
    'C:\Program Files\Java\jdk-21\bin\java.exe',
    'C:\Program Files\Microsoft\jdk-21*\bin\java.exe',
    'C:\Program Files\Eclipse Adoptium\jdk-21*\bin\java.exe',
    'C:\Program Files\Zulu\zulu-21*\bin\java.exe'
)
$javaCandidates = $javaCandidates | ForEach-Object { Get-Item $_ -ErrorAction SilentlyContinue } | Select-Object -ExpandProperty FullName -Unique
$javaExecutable = $javaCandidates | Where-Object { (Get-JavaMajorVersion $_) -eq 21 } | Select-Object -First 1
if (-not $javaExecutable) {
    $javaCommand = Get-Command java -ErrorAction SilentlyContinue
    if (-not $javaCommand) { throw 'Java is not installed or not available on PATH.' }
    $javaExecutable = $javaCommand.Source
}
$javaMajor = Get-JavaMajorVersion $javaExecutable
if ($javaMajor -lt 21) { throw "Java 21 or newer is required; found Java $javaMajor." }
$javaSettings = cmd /d /c "`"$javaExecutable`" -XshowSettings:properties -version 2>&1" | Out-String
if ($javaSettings -notmatch '(?m)^\s*java\.home\s*=\s*(?<home>.+?)\s*$') {
    throw 'Unable to determine JAVA_HOME from the selected Java runtime.'
}
$javaHome = $Matches.home.Trim()
if (-not (Test-Path -LiteralPath (Join-Path $javaHome 'bin\java.exe'))) {
    throw 'The selected Java runtime reported an invalid JAVA_HOME.'
}

$maven = Get-Command mvn.cmd -ErrorAction SilentlyContinue
if (-not $maven) { $maven = Get-Command mvn -ErrorAction SilentlyContinue }
if (-not $maven) { throw 'Maven is not installed or not available on PATH.' }

Write-Host 'Checking SQL Server connectivity...'
if (-not (Test-PhotoConnectSqlConnection -JdbcUrl $values.DB_URL -Username $values.DB_USERNAME -Password $values.DB_PASSWORD)) {
    throw 'SQL Server connectivity check failed.'
}
Write-Host 'SQL Server connectivity: VERIFIED' -ForegroundColor Green

$listener = Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue | Select-Object -First 1
if ($listener) {
    throw "Port 8080 is already in use by PID $($listener.OwningProcess). Stop that process or run stop-photoconnect.ps1 if it belongs to PhotoConnect."
}

$managedNames = @(
    'DB_URL', 'DB_USERNAME', 'DB_PASSWORD', 'JWT_SECRET',
    'MAIL_HOST', 'MAIL_PORT', 'MAIL_USERNAME', 'MAIL_APP_PASSWORD', 'MAIL_PASSWORD',
    'MAIL_FROM', 'MAIL_FROM_NAME', 'MAIL_CAPTURE_MODE',
    'CLOUDINARY_CLOUD_NAME', 'CLOUDINARY_API_KEY', 'CLOUDINARY_API_SECRET',
    'GOOGLE_CLIENT_ID', 'GOOGLE_CLIENT_SECRET',
    'SUPABASE_URL', 'SUPABASE_ANON_KEY', 'SUPABASE_OAUTH_REDIRECT_URI'
)
foreach ($name in $managedNames) {
    [Environment]::SetEnvironmentVariable($name, $null, 'Process')
}
foreach ($entry in $values.GetEnumerator()) {
    [Environment]::SetEnvironmentVariable([string]$entry.Key, [string]$entry.Value, 'Process')
}
$env:JAVA_HOME = $javaHome
$env:Path = (Join-Path $javaHome 'bin') + [IO.Path]::PathSeparator + $env:Path

if (-not (Test-Path -LiteralPath $paths.Directory)) {
    New-Item -ItemType Directory -Path $paths.Directory -Force | Out-Null
}
$process = $null
try {
    Write-Host "Starting PhotoConnect with Java $javaMajor..."
    $process = Start-Process -FilePath $maven.Source -ArgumentList @('spring-boot:run') -WorkingDirectory $projectRoot -NoNewWindow -PassThru
    $metadata = [ordered]@{
        Pid             = $process.Id
        StartedAtUnixMs = ([DateTimeOffset]$process.StartTime).ToUnixTimeMilliseconds()
        ProjectRoot     = $projectRoot
        Command         = 'maven-spring-boot-run'
    }
    $metadata | ConvertTo-Json | Set-Content -LiteralPath $paths.Process -Encoding UTF8 -NoNewline

    $deadline = [DateTime]::UtcNow.AddSeconds($StartupTimeoutSeconds)
    $ready = $false
    while ([DateTime]::UtcNow -lt $deadline) {
        if ($process.HasExited) {
            throw "Maven exited before PhotoConnect became ready (exit code $($process.ExitCode))."
        }
        try {
            $response = Invoke-WebRequest -Uri 'http://localhost:8080/login' -UseBasicParsing -TimeoutSec 10
            if ($response.StatusCode -eq 200) { $ready = $true; break }
        }
        catch { }
        Start-Sleep -Seconds 2
    }
    if (-not $ready) { throw "PhotoConnect did not become ready within $StartupTimeoutSeconds seconds." }

    Write-Host 'PhotoConnect started successfully:' -ForegroundColor Green
    Write-Host 'http://localhost:8080/login' -ForegroundColor Cyan
    $process.WaitForExit()
    exit $process.ExitCode
}
finally {
    if ($process -and $process.HasExited -and (Test-Path -LiteralPath $paths.Process)) {
        try {
            $saved = Get-Content -LiteralPath $paths.Process -Raw | ConvertFrom-Json
            if ([int]$saved.Pid -eq $process.Id) { Remove-Item -LiteralPath $paths.Process -Force }
        }
        catch { }
    }
    foreach ($name in $managedNames) {
        [Environment]::SetEnvironmentVariable([string]$name, $null, 'Process')
    }
}
