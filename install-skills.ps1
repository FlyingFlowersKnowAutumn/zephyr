# Skill Installer Script
# Use: Run this script in your new project root directory

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "  Skill Installer" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# Source directory
$sourceDir = "C:\Users\forever\.codex"

# Target directory (current project's .claude/skills)
$targetDir = Join-Path $PWD ".claude\skills"

Write-Host "Source: $sourceDir" -ForegroundColor Yellow
Write-Host "Target: $targetDir" -ForegroundColor Yellow
Write-Host ""

# Create target directory if not exists
if (-not (Test-Path $targetDir)) {
    Write-Host "Creating target directory..." -ForegroundColor Green
    New-Item -ItemType Directory -Force -Path $targetDir | Out-Null
}

# Skill source paths
$skillSources = @(
    "$sourceDir\gsap-skills\skills",
    "$sourceDir\gsap-community-skills\skills",
    "$sourceDir\superpowers\skills",
    "$sourceDir\codegraph\.claude\skills"
)

# Copy skills
$totalSkills = 0
foreach ($src in $skillSources) {
    if (Test-Path $src) {
        Write-Host "Copying skills from $src..." -ForegroundColor Green
        Copy-Item -Path "$src\*" -Destination $targetDir -Recurse -Force
        $count = (Get-ChildItem -Path $src -Directory | Measure-Object).Count
        $totalSkills += $count
        Write-Host "  Copied $count skills" -ForegroundColor Gray
    } else {
        Write-Host "Warning: Path not found $src" -ForegroundColor Yellow
    }
}

Write-Host ""
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "  Install complete!" -ForegroundColor Green
Write-Host "  Total: $totalSkills skills" -ForegroundColor Green
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Please restart your IDE or reopen the project." -ForegroundColor Yellow
Write-Host ""
