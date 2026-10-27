[CmdletBinding()]
param()

$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'PhotoConnect.LocalConfig.ps1')

$projectRoot = Get-PhotoConnectProjectRoot -ScriptDirectory $PSScriptRoot
$paths = Get-PhotoConnectLocalPaths -ProjectRoot $projectRoot

function Write-Status {
    param([string]$Name, [string]$Status, [string]$Detail = '')
    $color = if ($Status -eq 'VERIFIED') { 'Green' } elseif ($Status -eq 'CONFIGURED') { 'Cyan' } else { 'Yellow' }
    $suffix = if ($Detail) { " - $Detail" } else { '' }
    Write-Host ("{0}: {1}{2}" -f $Name, $Status, $suffix) -ForegroundColor $color
}

try {
    $values = Read-PhotoConnectConfig -SecretsPath $paths.Secrets
    Write-Status 'DPAPI local configuration' 'VERIFIED' 'decrypted for current Windows user'
}
catch {
    Write-Status 'DPAPI local configuration' 'NOT CONFIGURED' $_.Exception.Message
    exit 1
}

$java = Get-Command java -ErrorAction SilentlyContinue
$javaFailed = $false
if ($java) {
    $previousErrorActionPreference = $ErrorActionPreference
    try {
        # Windows PowerShell 5.1 wraps a native program's normal stderr as
        # NativeCommandError when ErrorActionPreference is Stop. Java writes
        # its version banner to stderr, so inspect the native exit code instead.
        $ErrorActionPreference = 'Continue'
        $javaOutput = & $java.Source -version 2>&1 | Out-String
        $javaExitCode = $LASTEXITCODE
    }
    catch {
        $javaOutput = ''
        $javaExitCode = -1
    }
    finally {
        $ErrorActionPreference = $previousErrorActionPreference
    }

    if ($javaExitCode -ne 0) {
        Write-Status 'Java' 'FAILED' "java -version exited with code $javaExitCode"
        $javaFailed = $true
    } else {
        $javaMajor = if ($javaOutput -match 'version\s+"(?<major>\d+)') { [int]$Matches.major } else { 0 }
        if ($javaMajor -ge 21) {
            Write-Status 'Java' 'VERIFIED' "version $javaMajor"
        } else {
            Write-Status 'Java' 'FAILED' 'Java 21 or newer is required'
            $javaFailed = $true
        }
    }
} else {
    Write-Status 'Java' 'MISSING'
    $javaFailed = $true
}
$maven = Get-Command mvn -ErrorAction SilentlyContinue
if ($maven) { Write-Status 'Maven' 'VERIFIED' 'command is available' } else { Write-Status 'Maven' 'MISSING' }

foreach ($name in @('DB_URL', 'DB_USERNAME', 'DB_PASSWORD', 'JWT_SECRET')) {
    $state = if ($values.ContainsKey($name) -and -not [string]::IsNullOrWhiteSpace([string]$values[$name])) { 'CONFIGURED' } else { 'MISSING' }
    Write-Status $name $state
}

if (Test-PhotoConnectValueSet -Values $values -Names @('DB_URL', 'DB_USERNAME', 'DB_PASSWORD')) {
    try {
        if (Test-PhotoConnectSqlConnection -JdbcUrl $values.DB_URL -Username $values.DB_USERNAME -Password $values.DB_PASSWORD) {
            Write-Status 'SQL Server connectivity' 'VERIFIED'
        }
    }
    catch { Write-Status 'SQL Server connectivity' 'FAILED' 'connection could not be established' }
} else { Write-Status 'SQL Server connectivity' 'NOT CONFIGURED' }

if (Test-PhotoConnectValueSet -Values $values -Names @('MAIL_USERNAME', 'MAIL_APP_PASSWORD')) {
    Write-Status 'Gmail SMTP' 'CONFIGURED' 'credentials present; no message was sent'
    if ($values.ContainsKey('MAIL_CAPTURE_MODE') -and $values.MAIL_CAPTURE_MODE -eq 'false') {
        Write-Status 'Gmail capture mode' 'CONFIGURED' 'disabled for real SMTP'
    } else {
        Write-Status 'Gmail capture mode' 'FAILED' 'run setup-once.ps1 -Section Gmail'
    }
} else { Write-Status 'Gmail SMTP' 'NOT CONFIGURED' 'launcher will use capture mode' }

if (Test-PhotoConnectValueSet -Values $values -Names @('CLOUDINARY_CLOUD_NAME', 'CLOUDINARY_API_KEY', 'CLOUDINARY_API_SECRET')) {
    Write-Status 'Cloudinary' 'CONFIGURED' 'credentials present; remote API not called'
} else { Write-Status 'Cloudinary' 'NOT CONFIGURED' }

$googleKeys = @('GOOGLE_CLIENT_ID', 'GOOGLE_CLIENT_SECRET')
$googleCount = @($googleKeys | Where-Object { $values.ContainsKey($_) -and -not [string]::IsNullOrWhiteSpace([string]$values[$_]) }).Count
if ($googleCount -eq $googleKeys.Count) {
    Write-Status 'Google OAuth' 'CONFIGURED' 'credentials present; interactive login not executed'
    try {
        $discovery = Invoke-WebRequest -Uri 'https://accounts.google.com/.well-known/openid-configuration' -UseBasicParsing -TimeoutSec 5
        if ($discovery.StatusCode -eq 200) {
            Write-Status 'Google OIDC provider metadata' 'VERIFIED' 'provider endpoint is reachable'
        }
    }
    catch { Write-Status 'Google OIDC provider metadata' 'NOT VERIFIED' 'provider endpoint is not reachable' }
} elseif ($googleCount -eq 0) {
    Write-Status 'Google OAuth' 'NOT CONFIGURED' 'email/password login remains available'
} else {
    Write-Status 'Google OAuth' 'NOT CONFIGURED' 'client ID or client secret is missing'
}

$paymentMode = if ($values.ContainsKey('PAYMENT_MODE') -and -not [string]::IsNullOrWhiteSpace([string]$values.PAYMENT_MODE)) { [string]$values.PAYMENT_MODE } else { 'DEMO' }
$paymentProvider = if ($values.ContainsKey('PAYMENT_PROVIDER') -and -not [string]::IsNullOrWhiteSpace([string]$values.PAYMENT_PROVIDER)) { [string]$values.PAYMENT_PROVIDER.ToLower() } else { 'demo' }
$realEnabled = if ($values.ContainsKey('PAYMENT_REAL_ENABLED')) { [string]$values.PAYMENT_REAL_ENABLED -eq 'true' } else { $false }

if (-not $realEnabled -or $paymentMode -eq 'DEMO' -or $paymentProvider -eq 'demo') {
    Write-Status 'Payment Gateway' 'SIMULATION_AVAILABLE' 'Demo adapter active with ZXing simulation QR'
} elseif ($paymentProvider -eq 'payos') {
    if (Test-PhotoConnectValueSet -Values $values -Names @('PAYOS_CLIENT_ID', 'PAYOS_API_KEY', 'PAYOS_CHECKSUM_KEY')) {
        Write-Status 'Payment Gateway (PayOS)' 'REAL_PROVIDER_CONFIGURED' 'credentials present in DPAPI'
    } else {
        Write-Status 'Payment Gateway (PayOS)' 'REAL_PROVIDER_NOT_CONFIGURED' 'missing client ID, API key or checksum key'
    }
} elseif ($paymentProvider -eq 'sepay') {
    if (Test-PhotoConnectValueSet -Values $values -Names @('SEPAY_API_KEY', 'SEPAY_ACCOUNT_NUMBER')) {
        Write-Status 'Payment Gateway (SePay)' 'REAL_PROVIDER_CONFIGURED' 'credentials present in DPAPI'
    } else {
        Write-Status 'Payment Gateway (SePay)' 'REAL_PROVIDER_NOT_CONFIGURED' 'missing API key or account number'
    }
} else {
    Write-Status 'Payment Gateway' 'REAL_PROVIDER_NOT_CONFIGURED' "unknown provider '$paymentProvider'"
}

try {
    $response = Invoke-WebRequest -Uri 'http://localhost:8080/login' -UseBasicParsing -TimeoutSec 3
    if ($response.StatusCode -eq 200) { Write-Status 'PhotoConnect HTTP' 'VERIFIED' 'http://localhost:8080/login' }
} catch { Write-Status 'PhotoConnect HTTP' 'NOT RUNNING' }

if ($javaFailed) { exit 1 }
