param(
    [string]$Suite = "com.example.QLDatVe.services.BookingTestSuite",
    [string]$SandboxDirName = "automation_sandbox",
    [switch]$CleanupSandbox,
    [switch]$UseOnlineMode
)

$ErrorActionPreference = "Stop"

function Remove-WorkspaceDirectory {
    param(
        [string]$TargetPath,
        [string]$WorkspaceRoot
    )

    if (-not (Test-Path -LiteralPath $TargetPath)) {
        return
    }

    $resolvedTarget = (Resolve-Path -LiteralPath $TargetPath).Path
    if (-not $resolvedTarget.StartsWith($WorkspaceRoot, [System.StringComparison]::OrdinalIgnoreCase)) {
        throw "Refusing to remove path outside workspace: $resolvedTarget"
    }

    Remove-Item -LiteralPath $resolvedTarget -Recurse -Force
}

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$artifactRoot = Join-Path $projectRoot "Bao-cao-KTPM\automation-artifacts"
$runStamp = Get-Date -Format "yyyyMMdd-HHmmss"
$sandboxPath = Join-Path $projectRoot ($SandboxDirName + "_" + $runStamp)
$runDir = Join-Path $artifactRoot $runStamp
$consoleLog = Join-Path $runDir "automation-console.log"
$summaryPath = Join-Path $runDir "summary.txt"
$scriptExitCode = 1

New-Item -ItemType Directory -Path $artifactRoot -Force | Out-Null
New-Item -ItemType Directory -Path $runDir -Force | Out-Null

New-Item -ItemType Directory -Path $sandboxPath | Out-Null

foreach ($item in @("pom.xml", "mvnw", "mvnw.cmd", ".mvn", "src")) {
    Copy-Item -LiteralPath (Join-Path $projectRoot $item) -Destination $sandboxPath -Recurse
}

Push-Location $sandboxPath
try {
    $mvnArgs = @()
    if (-not $UseOnlineMode) {
        $mvnArgs += "-o"
    }
    $mvnArgs += "-Dtest=$Suite"
    $mvnArgs += "test"

    $stdoutLog = Join-Path $runDir "automation-stdout.log"
    $stderrLog = Join-Path $runDir "automation-stderr.log"

    $process = Start-Process `
        -FilePath (Join-Path $sandboxPath "mvnw.cmd") `
        -ArgumentList $mvnArgs `
        -WorkingDirectory $sandboxPath `
        -NoNewWindow `
        -Wait `
        -PassThru `
        -RedirectStandardOutput $stdoutLog `
        -RedirectStandardError $stderrLog

    $testExitCode = $process.ExitCode

    $combinedLog = @()
    if (Test-Path -LiteralPath $stdoutLog) {
        $combinedLog += Get-Content -LiteralPath $stdoutLog
    }
    if (Test-Path -LiteralPath $stderrLog) {
        $combinedLog += Get-Content -LiteralPath $stderrLog
    }
    $combinedLog | Set-Content -LiteralPath $consoleLog -Encoding utf8

    $surefireSource = Join-Path $sandboxPath "target\surefire-reports"
    if (Test-Path -LiteralPath $surefireSource) {
        Copy-Item -LiteralPath $surefireSource -Destination (Join-Path $runDir "surefire-reports") -Recurse
    }

    $tests = 0
    $failures = 0
    $errors = 0
    $skipped = 0

    $combinedText = [string]::Join([Environment]::NewLine, $combinedLog)
    $summaryMatches = [regex]::Matches(
        $combinedText,
        'Tests run:\s*(\d+),\s*Failures:\s*(\d+),\s*Errors:\s*(\d+),\s*Skipped:\s*(\d+)'
    )
    if ($summaryMatches.Count -gt 0) {
        $lastSummary = $summaryMatches[$summaryMatches.Count - 1]
        $tests = [int]$lastSummary.Groups[1].Value
        $failures = [int]$lastSummary.Groups[2].Value
        $errors = [int]$lastSummary.Groups[3].Value
        $skipped = [int]$lastSummary.Groups[4].Value
    }

    $passed = $tests - $failures - $errors - $skipped
    @(
        "suite=$Suite"
        "run_dir=$runDir"
        "tests=$tests"
        "passed=$passed"
        "failures=$failures"
        "errors=$errors"
        "skipped=$skipped"
        "exit_code=$testExitCode"
        "mode=$(if ($UseOnlineMode) { 'online' } else { 'offline' })"
    ) | Set-Content -LiteralPath $summaryPath -Encoding ascii

    Write-Host ""
    Write-Host "Automation run completed."
    Write-Host "Artifacts: $runDir"
    Write-Host "Tests: $tests | Passed: $passed | Failures: $failures | Errors: $errors | Skipped: $skipped"
    $scriptExitCode = $testExitCode
}
finally {
    Pop-Location
    if ($CleanupSandbox) {
        try {
            Remove-WorkspaceDirectory -TargetPath $sandboxPath -WorkspaceRoot $projectRoot
        } catch {
            Write-Warning "Could not remove sandbox: $sandboxPath"
        }
    }
}

exit $scriptExitCode
