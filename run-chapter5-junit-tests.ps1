$ErrorActionPreference = "Continue"
if ($PSVersionTable.PSVersion.Major -ge 7) {
    $PSNativeCommandUseErrorActionPreference = $false
}

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$outputRoot = Join-Path $projectRoot "target\chapter5-test-execution\$timestamp"

New-Item -ItemType Directory -Force -Path $outputRoot | Out-Null

$testGroups = @(
    @{
        Name = "5.1 Functional Testing"
        Test = "FunctionalTestingSuite"
    },
    @{
        Name = "5.2 Integration Testing"
        Test = "BookingApiIntegrationTest"
    },
    @{
        Name = "5.3 System Testing"
        Test = "BookingSystemFlowMockMvcTest"
    },
    @{
        Name = "5.4 Performance Testing"
        Test = "BookingPerformanceSmokeTest"
    },
    @{
        Name = "5.5 Security Testing"
        Test = "SecurityAccessIntegrationTest"
    },
    @{
        Name = "5.6 Regression Testing"
        Test = "RegressionStableSuite"
    }
)

$results = @()

foreach ($group in $testGroups) {
    $safeName = ($group.Name -replace '[^\w\-\.]+', '_')
    $logPath = Join-Path $outputRoot "$safeName.log"
    Write-Host "Running $($group.Name) with test selector $($group.Test)..."

    Push-Location $projectRoot
    try {
        & .\mvnw.cmd -q "-Dtest=$($group.Test)" test *>&1 | Tee-Object -FilePath $logPath
        $status = if ($LASTEXITCODE -eq 0) { "PASS" } else { "FAIL" }
    }
    finally {
        Pop-Location
    }

    $results += [PSCustomObject]@{
        Section = $group.Name
        TestSelector = $group.Test
        Status = $status
        LogFile = $logPath
    }
}

$summaryPath = Join-Path $outputRoot "summary.txt"
"CHAPTER 5 JUNIT EXECUTION SUMMARY`r`n" | Set-Content -Path $summaryPath
foreach ($result in $results) {
    Add-Content -Path $summaryPath -Value ("{0} | {1} | {2}" -f $result.Section, $result.TestSelector, $result.Status)
    Add-Content -Path $summaryPath -Value ("Log: {0}" -f $result.LogFile)
}

Write-Host ""
Write-Host "Execution summary:"
$results | Format-Table -AutoSize
Write-Host ""
Write-Host "Saved logs and summary to: $outputRoot"
