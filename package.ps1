$DirName = Split-Path -Leaf $PWD
$OutputDir = "out"
$ResourcePackDir = "resourcepack"

if (!(Test-Path $OutputDir)) {
    New-Item -ItemType Directory -Path $OutputDir | Out-Null
}

$OutputName = Join-Path $OutputDir "$DirName.zip"

if (Test-Path $OutputName) {
    Remove-Item $OutputName -Force
}

$FilesToZip = @()
if (Test-Path "data") { $FilesToZip += "data" }
if (Test-Path "pack.mcmeta") { $FilesToZip += "pack.mcmeta" }
if (Test-Path "pack.png") { $FilesToZip += "pack.png" }
if (Test-Path "LICENSE") { $FilesToZip += "LICENSE" }
if (Test-Path "README.md") { $FilesToZip += "README.md" }

if ($FilesToZip.Count -eq 0) {
    Write-Error "Error: No datapack files found to package!"
    exit 1
}

Write-Host "Packaging datapack..."
Compress-Archive -Path $FilesToZip -DestinationPath $OutputName -Force

if (Test-Path $ResourcePackDir) {
    $ResourceOutputName = Join-Path $OutputDir "$DirName-resources.zip"
    if (Test-Path $ResourceOutputName) {
        Remove-Item $ResourceOutputName -Force
    }

    Write-Host "Packaging resource pack..."
    Compress-Archive -Path (Join-Path $ResourcePackDir "*") -DestinationPath $ResourceOutputName -Force
    Write-Host "Resource pack packaged successfully: $ResourceOutputName"
}
Write-Host "Successfully packaged: $OutputName"


