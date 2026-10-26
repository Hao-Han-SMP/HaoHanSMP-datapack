$OutputDir = "out"

if (!(Test-Path $OutputDir)) {
    New-Item -ItemType Directory -Path $OutputDir | Out-Null
}

# Build Paper/Purpur Plugin Jar
if (Test-Path "build.gradle") {
    Write-Host "Building HaoHan-WorldGen Paper/Purpur plugin jar..."
    .\gradlew.bat jar
    $JarSource = "build\libs\HaoHan-WorldGen-1.0.0.jar"
    if (Test-Path $JarSource) {
        $JarDest = Join-Path $OutputDir "HaoHan-WorldGen-1.0.0.jar"
        Copy-Item $JarSource $JarDest -Force
        Write-Host "Plugin jar packaged successfully: $JarDest"
    }
}
