[CmdletBinding()]
param()

$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'PhotoConnect.LocalConfig.ps1')

$projectRoot = Get-PhotoConnectProjectRoot -ScriptDirectory $PSScriptRoot
$paths = Get-PhotoConnectLocalPaths -ProjectRoot $projectRoot
if (-not (Test-Path -LiteralPath $paths.Process -PathType Leaf)) {
    Write-Host 'No launcher-managed PhotoConnect process is recorded.'
    exit 0
}

try {
    $metadata = Get-Content -LiteralPath $paths.Process -Raw | ConvertFrom-Json
    $rootPid = [int]$metadata.Pid
    if ([IO.Path]::GetFullPath([string]$metadata.ProjectRoot) -ne $projectRoot -or $metadata.Command -ne 'maven-spring-boot-run') {
        throw 'Process metadata does not identify this PhotoConnect project.'
    }
    $root = Get-Process -Id $rootPid -ErrorAction SilentlyContinue
    if (-not $root) {
        Remove-Item -LiteralPath $paths.Process -Force
        Write-Host 'The recorded PhotoConnect process is no longer running.'
        exit 0
    }
    if ($null -eq $metadata.StartedAtUnixMs) {
        throw 'Process metadata uses an obsolete timestamp format; refusing to stop it.'
    }
    $actualStartUnixMs = ([DateTimeOffset]$root.StartTime).ToUnixTimeMilliseconds()
    if ([Math]::Abs($actualStartUnixMs - [long]$metadata.StartedAtUnixMs) -gt 2000) {
        throw 'The recorded PID has been reused by another process; refusing to stop it.'
    }

    $all = Get-CimInstance Win32_Process | Select-Object ProcessId, ParentProcessId
    $descendants = New-Object System.Collections.Generic.List[int]
    $frontier = @($rootPid)
    while ($frontier.Count -gt 0) {
        $parent = $frontier[0]
        if ($frontier.Count -eq 1) { $frontier = @() } else { $frontier = $frontier[1..($frontier.Count - 1)] }
        $children = @($all | Where-Object { $_.ParentProcessId -eq $parent } | Select-Object -ExpandProperty ProcessId)
        foreach ($child in $children) {
            $descendants.Add([int]$child)
            $frontier += [int]$child
        }
    }
    $orderedDescendants = $descendants.ToArray()
    [array]::Reverse($orderedDescendants)
    foreach ($childPid in $orderedDescendants) { Stop-Process -Id $childPid -ErrorAction SilentlyContinue }
    Stop-Process -Id $rootPid -ErrorAction Stop
    Remove-Item -LiteralPath $paths.Process -Force
    Write-Host 'Stopped the PhotoConnect process started by the launcher.' -ForegroundColor Green
}
catch {
    throw "PhotoConnect was not stopped: $($_.Exception.Message)"
}
