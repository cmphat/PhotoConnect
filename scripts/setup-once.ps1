[CmdletBinding()]
param(
    [ValidateSet('All', 'SqlServer', 'Jwt', 'Gmail', 'Cloudinary', 'Google', 'Payment')]
    [string]$Section = 'All'
)

$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'PhotoConnect.LocalConfig.ps1')

$projectRoot = Get-PhotoConnectProjectRoot -ScriptDirectory $PSScriptRoot
$paths = Get-PhotoConnectLocalPaths -ProjectRoot $projectRoot
$values = Read-PhotoConnectConfig -SecretsPath $paths.Secrets -AllowMissing

function Confirm-Choice {
    param([string]$Prompt, [bool]$Default = $false)
    $suffix = if ($Default) { ' [Y/n]' } else { ' [y/N]' }
    $answer = Read-Host ($Prompt + $suffix)
    if ([string]::IsNullOrWhiteSpace($answer)) { return $Default }
    return $answer.Trim() -match '^(?i:y|yes)$'
}

function Read-RequiredText {
    param([string]$Prompt, [string]$DefaultValue = '')
    while ($true) {
        $label = if ([string]::IsNullOrWhiteSpace($DefaultValue)) { $Prompt } else { "$Prompt [$DefaultValue]" }
        $answer = Read-Host $label
        if ([string]::IsNullOrWhiteSpace($answer)) { $answer = $DefaultValue }
        if (-not [string]::IsNullOrWhiteSpace($answer)) { return $answer.Trim() }
        Write-Warning 'A value is required.'
    }
}

function Read-SecretText {
    param([string]$Prompt)
    while ($true) {
        $secure = Read-Host $Prompt -AsSecureString
        $plain = ConvertTo-PhotoConnectPlainText -SecureValue $secure
        if (-not [string]::IsNullOrWhiteSpace($plain)) { return $plain }
        Write-Warning 'A value is required.'
    }
}

function Should-ConfigureSection {
    param([string]$Name, [string[]]$Keys, [bool]$Optional)
    $configured = Test-PhotoConnectValueSet -Values $values -Names $Keys
    if ($Section -ne 'All' -and $Section -ne $Name) { return $false }
    if ($configured) {
        return Confirm-Choice -Prompt "$Name is already configured. Replace only this integration?" -Default $false
    }
    if ($Optional -and $Section -eq 'All') {
        return Confirm-Choice -Prompt "Configure optional integration $Name now?" -Default $false
    }
    return $true
}

if (Should-ConfigureSection -Name 'SqlServer' -Keys @('DB_URL', 'DB_USERNAME', 'DB_PASSWORD') -Optional $false) {
    $values.DB_URL = Read-RequiredText -Prompt 'SQL Server JDBC URL' -DefaultValue 'jdbc:sqlserver://localhost:1433;databaseName=PhotoConnect;encrypt=true;trustServerCertificate=true'
    $values.DB_USERNAME = Read-RequiredText -Prompt 'SQL Server login'
    $values.DB_PASSWORD = Read-SecretText -Prompt 'SQL Server password'
}

if (Should-ConfigureSection -Name 'Jwt' -Keys @('JWT_SECRET') -Optional $false) {
    $randomBytes = New-Object byte[] 64
    [Security.Cryptography.RandomNumberGenerator]::Fill($randomBytes)
    $values.JWT_SECRET = [Convert]::ToBase64String($randomBytes)
    Write-Host 'Generated a new strong JWT signing key.' -ForegroundColor Green
}

if (Should-ConfigureSection -Name 'Gmail' -Keys @('MAIL_USERNAME', 'MAIL_APP_PASSWORD') -Optional $true) {
    $values.MAIL_HOST = Read-RequiredText -Prompt 'SMTP host' -DefaultValue 'smtp.gmail.com'
    $values.MAIL_PORT = Read-RequiredText -Prompt 'SMTP port' -DefaultValue '587'
    $values.MAIL_USERNAME = Read-RequiredText -Prompt 'Gmail address'
    $values.MAIL_APP_PASSWORD = Read-SecretText -Prompt 'Gmail App Password'
    $values.MAIL_FROM = Read-RequiredText -Prompt 'From address' -DefaultValue $values.MAIL_USERNAME
    $values.MAIL_FROM_NAME = Read-RequiredText -Prompt 'From display name' -DefaultValue 'PhotoConnect Support'
    $values.MAIL_CAPTURE_MODE = 'false'
}

if (Should-ConfigureSection -Name 'Cloudinary' -Keys @('CLOUDINARY_CLOUD_NAME', 'CLOUDINARY_API_KEY', 'CLOUDINARY_API_SECRET') -Optional $true) {
    $values.CLOUDINARY_CLOUD_NAME = Read-RequiredText -Prompt 'Cloudinary cloud name'
    $values.CLOUDINARY_API_KEY = Read-SecretText -Prompt 'Cloudinary API key'
    $values.CLOUDINARY_API_SECRET = Read-SecretText -Prompt 'Cloudinary API secret'
}

if (Should-ConfigureSection -Name 'Google' -Keys @('GOOGLE_CLIENT_ID', 'GOOGLE_CLIENT_SECRET') -Optional $true) {
    $values.GOOGLE_CLIENT_ID = Read-RequiredText -Prompt 'Google OAuth Client ID'
    $values.GOOGLE_CLIENT_SECRET = Read-SecretText -Prompt 'Google OAuth Client Secret'
}

if (Should-ConfigureSection -Name 'Payment' -Keys @('PAYMENT_PROVIDER') -Optional $true) {
    Write-Host "`nConfigure V6 Real Payment Gateway (PayOS / SePay). Press Enter to keep DEMO simulation." -ForegroundColor Cyan
    $provider = Read-RequiredText -Prompt 'Payment provider (demo, payos, sepay)' -DefaultValue 'demo'
    $values.PAYMENT_PROVIDER = $provider.ToLower()
    if ($values.PAYMENT_PROVIDER -eq 'payos') {
        $values.PAYMENT_MODE = 'REAL'
        $values.PAYMENT_REAL_ENABLED = 'true'
        $values.PAYOS_CLIENT_ID = Read-RequiredText -Prompt 'PayOS Client ID'
        $values.PAYOS_API_KEY = Read-SecretText -Prompt 'PayOS API Key'
        $values.PAYOS_CHECKSUM_KEY = Read-SecretText -Prompt 'PayOS Checksum Key'
    } elseif ($values.PAYMENT_PROVIDER -eq 'sepay') {
        $values.PAYMENT_MODE = 'REAL'
        $values.PAYMENT_REAL_ENABLED = 'true'
        $values.SEPAY_API_KEY = Read-SecretText -Prompt 'SePay API Key'
        $values.SEPAY_ACCOUNT_NUMBER = Read-RequiredText -Prompt 'SePay Account Number'
        $values.SEPAY_BANK_NAME = Read-RequiredText -Prompt 'SePay Bank Name' -DefaultValue 'MBBank'
    } else {
        $values.PAYMENT_PROVIDER = 'demo'
        $values.PAYMENT_MODE = 'DEMO'
        $values.PAYMENT_REAL_ENABLED = 'false'
        Write-Host 'Selected DEMO simulation mode. VietQR simulation active.' -ForegroundColor Yellow
    }
}

if (-not (Test-PhotoConnectValueSet -Values $values -Names @('DB_URL', 'DB_USERNAME', 'DB_PASSWORD', 'JWT_SECRET'))) {
    throw 'Required SQL Server or JWT configuration is incomplete.'
}

Save-PhotoConnectConfig -Values $values -Paths $paths
Write-Host 'PhotoConnect local configuration saved with Windows DPAPI.' -ForegroundColor Green
Write-Host 'Run: .\scripts\check-setup.ps1'
Write-Host 'Then: .\scripts\start-photoconnect.ps1'
