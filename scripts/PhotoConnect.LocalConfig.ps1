Set-StrictMode -Version Latest

function Get-PhotoConnectProjectRoot {
    param([Parameter(Mandatory = $true)][string]$ScriptDirectory)
    return [IO.Path]::GetFullPath((Join-Path $ScriptDirectory '..'))
}

function Get-PhotoConnectLocalPaths {
    param([Parameter(Mandatory = $true)][string]$ProjectRoot)
    $localDirectory = Join-Path $ProjectRoot '.photoconnect-local'
    return @{
        Directory  = $localDirectory
        Secrets    = Join-Path $localDirectory 'secrets.dpapi.json'
        Process    = Join-Path $localDirectory 'process.json'
    }
}

function Get-CurrentWindowsSid {
    if ($PSVersionTable.PSEdition -eq 'Core' -and -not $IsWindows) {
        throw 'PhotoConnect local secret storage requires Windows.'
    }
    return [Security.Principal.WindowsIdentity]::GetCurrent().User.Value
}

function ConvertTo-PhotoConnectPlainText {
    param([Parameter(Mandatory = $true)][Security.SecureString]$SecureValue)
    $pointer = [IntPtr]::Zero
    try {
        $pointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($SecureValue)
        return [Runtime.InteropServices.Marshal]::PtrToStringBSTR($pointer)
    }
    finally {
        if ($pointer -ne [IntPtr]::Zero) {
            [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($pointer)
        }
    }
}

function Protect-PhotoConnectValue {
    param([AllowEmptyString()][string]$Value)
    $secure = ConvertTo-SecureString -String $Value -AsPlainText -Force
    return ConvertFrom-SecureString -SecureString $secure
}

function Unprotect-PhotoConnectValue {
    param([Parameter(Mandatory = $true)][string]$CipherText)
    try {
        $secure = ConvertTo-SecureString -String $CipherText -ErrorAction Stop
        return ConvertTo-PhotoConnectPlainText -SecureValue $secure
    }
    catch {
        throw 'Unable to decrypt PhotoConnect local configuration. Run setup-once.ps1 under the Windows account that created it.'
    }
}

function Set-PhotoConnectPrivateDirectoryAcl {
    param([Parameter(Mandatory = $true)][string]$Directory)
    try {
        $identity = [Security.Principal.WindowsIdentity]::GetCurrent().Name
        $existingAcl = Get-Acl -LiteralPath $Directory -ErrorAction Stop
        $existingRule = $existingAcl.Access | Where-Object {
            $_.IdentityReference.Value -eq $identity -and
            $_.AccessControlType -eq [Security.AccessControl.AccessControlType]::Allow -and
            ($_.FileSystemRights -band [Security.AccessControl.FileSystemRights]::FullControl) -eq [Security.AccessControl.FileSystemRights]::FullControl
        } | Select-Object -First 1
        if ($existingAcl.AreAccessRulesProtected -and $existingRule) {
            return
        }
        $acl = New-Object Security.AccessControl.DirectorySecurity
        $acl.SetAccessRuleProtection($true, $false)
        $rights = [Security.AccessControl.FileSystemRights]::FullControl
        $inheritance = [Security.AccessControl.InheritanceFlags]'ContainerInherit, ObjectInherit'
        $propagation = [Security.AccessControl.PropagationFlags]::None
        $type = [Security.AccessControl.AccessControlType]::Allow
        $rule = New-Object Security.AccessControl.FileSystemAccessRule($identity, $rights, $inheritance, $propagation, $type)
        $acl.AddAccessRule($rule)
        Set-Acl -LiteralPath $Directory -AclObject $acl -ErrorAction Stop
    }
    catch {
        throw 'Failed to restrict the local configuration directory to the current Windows user.'
    }
}

function Read-PhotoConnectConfig {
    param(
        [Parameter(Mandatory = $true)][string]$SecretsPath,
        [switch]$AllowMissing
    )
    if (-not (Test-Path -LiteralPath $SecretsPath -PathType Leaf)) {
        if ($AllowMissing) { return @{} }
        throw 'Local configuration is missing. Run .\scripts\setup-once.ps1 first.'
    }

    try {
        $document = Get-Content -LiteralPath $SecretsPath -Raw -ErrorAction Stop | ConvertFrom-Json -ErrorAction Stop
    }
    catch {
        throw 'Local configuration is unreadable. Run setup-once.ps1 to repair it.'
    }

    $currentSid = Get-CurrentWindowsSid
    if ([string]::IsNullOrWhiteSpace([string]$document.OwnerSid) -or $document.OwnerSid -ne $currentSid) {
        throw 'Local configuration belongs to a different Windows account. Run setup-once.ps1 under this account.'
    }

    $values = @{}
    if ($null -ne $document.Values) {
        foreach ($property in $document.Values.PSObject.Properties) {
            $values[$property.Name] = Unprotect-PhotoConnectValue -CipherText ([string]$property.Value)
        }
    }
    return $values
}

function Save-PhotoConnectConfig {
    param(
        [Parameter(Mandatory = $true)][hashtable]$Values,
        [Parameter(Mandatory = $true)][hashtable]$Paths
    )
    if (-not (Test-Path -LiteralPath $Paths.Directory)) {
        New-Item -ItemType Directory -Path $Paths.Directory -Force | Out-Null
    }
    Set-PhotoConnectPrivateDirectoryAcl -Directory $Paths.Directory

    $encrypted = [ordered]@{}
    foreach ($name in ($Values.Keys | Sort-Object)) {
        if ($null -ne $Values[$name]) {
            $encrypted[$name] = Protect-PhotoConnectValue -Value ([string]$Values[$name])
        }
    }
    $document = [ordered]@{
        FormatVersion = 1
        OwnerSid      = Get-CurrentWindowsSid
        UpdatedAtUtc  = [DateTime]::UtcNow.ToString('o')
        Values        = $encrypted
    }
    $temporary = Join-Path $Paths.Directory ('secrets.' + [Guid]::NewGuid().ToString('N') + '.tmp')
    try {
        $document | ConvertTo-Json -Depth 5 | Set-Content -LiteralPath $temporary -Encoding UTF8 -NoNewline
        Move-Item -LiteralPath $temporary -Destination $Paths.Secrets -Force
    }
    finally {
        if (Test-Path -LiteralPath $temporary) {
            Remove-Item -LiteralPath $temporary -Force
        }
    }
}

function Test-PhotoConnectSqlConnection {
    param(
        [Parameter(Mandatory = $true)][string]$JdbcUrl,
        [Parameter(Mandatory = $true)][string]$Username,
        [Parameter(Mandatory = $true)][string]$Password
    )
    if ($JdbcUrl -notmatch '^jdbc:sqlserver://(?<server>[^;]+)(?<properties>;.*)?$') {
        throw 'DB_URL must use the jdbc:sqlserver:// format.'
    }
    $server = $Matches.server
    $rawProperties = [string]$Matches.properties
    if ($server -match '^(?<host>.+):(?<port>\d+)$') {
        $server = $Matches.host + ',' + $Matches.port
    }
    $properties = @{}
    foreach ($part in ($rawProperties -split ';')) {
        if ($part -match '^(?<key>[^=]+)=(?<value>.*)$') {
            $properties[$Matches.key.ToLowerInvariant()] = $Matches.value
        }
    }
    $builder = New-Object System.Data.SqlClient.SqlConnectionStringBuilder
    $builder['Data Source'] = $server
    $builder['User ID'] = $Username
    $builder['Password'] = $Password
    $builder['Connect Timeout'] = 5
    $builder['Encrypt'] = if ($properties.ContainsKey('encrypt')) { [Convert]::ToBoolean($properties.encrypt) } else { $true }
    $builder['TrustServerCertificate'] = if ($properties.ContainsKey('trustservercertificate')) { [Convert]::ToBoolean($properties.trustservercertificate) } else { $false }
    if ($properties.ContainsKey('databasename')) { $builder['Initial Catalog'] = $properties.databasename }

    $connection = New-Object System.Data.SqlClient.SqlConnection($builder.ConnectionString)
    try {
        $connection.Open()
        $command = $connection.CreateCommand()
        $command.CommandText = 'SELECT 1'
        return ([int]$command.ExecuteScalar() -eq 1)
    }
    finally {
        $connection.Dispose()
    }
}

function Test-PhotoConnectValueSet {
    param([hashtable]$Values, [string[]]$Names)
    foreach ($name in $Names) {
        if (-not $Values.ContainsKey($name) -or [string]::IsNullOrWhiteSpace([string]$Values[$name])) {
            return $false
        }
    }
    return $true
}

function Resolve-PhotoConnectLaunchConfig {
    param([Parameter(Mandatory = $true)][hashtable]$Values)
    $resolved = @{}
    foreach ($entry in $Values.GetEnumerator()) { $resolved[$entry.Key] = $entry.Value }

    if (-not (Test-PhotoConnectValueSet -Values $resolved -Names @('DB_URL', 'DB_USERNAME', 'DB_PASSWORD', 'JWT_SECRET'))) {
        throw 'Required configuration is incomplete. Run setup-once.ps1.'
    }

    $gmailKeys = @('MAIL_USERNAME', 'MAIL_APP_PASSWORD')
    $gmailConfigured = Test-PhotoConnectValueSet -Values $resolved -Names $gmailKeys
    $gmailCount = @($gmailKeys | Where-Object { $resolved.ContainsKey($_) -and -not [string]::IsNullOrWhiteSpace([string]$resolved[$_]) }).Count
    if ($gmailCount -ne 0 -and -not $gmailConfigured) {
        throw 'Gmail configuration is incomplete. Run setup-once.ps1 -Section Gmail.'
    }
    $resolved.MAIL_CAPTURE_MODE = if ($gmailConfigured) { 'false' } else { 'true' }

    $cloudinaryKeys = @('CLOUDINARY_CLOUD_NAME', 'CLOUDINARY_API_KEY', 'CLOUDINARY_API_SECRET')
    $cloudinaryCount = @($cloudinaryKeys | Where-Object { $resolved.ContainsKey($_) -and -not [string]::IsNullOrWhiteSpace([string]$resolved[$_]) }).Count
    if ($cloudinaryCount -ne 0 -and $cloudinaryCount -ne $cloudinaryKeys.Count) {
        throw 'Cloudinary configuration is incomplete. Run setup-once.ps1 -Section Cloudinary.'
    }

    # Obsolete Supabase values may remain in an older encrypted file for audit/recovery,
    # but they are never exported to the application process.
    foreach ($obsoleteName in @('SUPABASE_URL', 'SUPABASE_ANON_KEY', 'SUPABASE_OAUTH_REDIRECT_URI')) {
        $resolved.Remove($obsoleteName)
    }

    $oauthKeys = @('GOOGLE_CLIENT_ID', 'GOOGLE_CLIENT_SECRET')
    $oauthCount = @($oauthKeys | Where-Object { $resolved.ContainsKey($_) -and -not [string]::IsNullOrWhiteSpace([string]$resolved[$_]) }).Count
    if ($oauthCount -ne 0 -and $oauthCount -ne $oauthKeys.Count) {
        throw 'Google OAuth configuration is incomplete. Run setup-once.ps1 -Section Google.'
    }
    return $resolved
}
