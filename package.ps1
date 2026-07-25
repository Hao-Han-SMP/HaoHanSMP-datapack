$DirName = Split-Path -Leaf $PWD
$OutputDir = "out"
$ResourcePackDir = "resourcepack"

# Tạo thư mục out nếu chưa có
if (!(Test-Path $OutputDir)) {
    New-Item -ItemType Directory -Path $OutputDir | Out-Null
}

$OutputName = Join-Path $OutputDir "$DirName.zip"

# Xóa file zip cũ nếu có
if (Test-Path $OutputName) {
    Remove-Item $OutputName -Force
}

# Các thư mục và file cần nén
$FilesToZip = @()
if (Test-Path "data") { $FilesToZip += "data" }
if (Test-Path "pack.mcmeta") { $FilesToZip += "pack.mcmeta" }
if (Test-Path "pack.png") { $FilesToZip += "pack.png" }
if (Test-Path "LICENSE") { $FilesToZip += "LICENSE" }
if (Test-Path "README.md") { $FilesToZip += "README.md" }

if ($FilesToZip.Count -eq 0) {
    Write-Error "Lỗi: Không tìm thấy file datapack nào để nén!"
    exit 1
}

Write-Host "Đang nén datapack..."
Compress-Archive -Path $FilesToZip -DestinationPath $OutputName -Force

if (Test-Path $ResourcePackDir) {
    $ResourceOutputName = Join-Path $OutputDir "$DirName-resources.zip"
    if (Test-Path $ResourceOutputName) {
        Remove-Item $ResourceOutputName -Force
    }

    Write-Host "Dang nen resource pack..."
    Compress-Archive -Path (Join-Path $ResourcePackDir "*") -DestinationPath $ResourceOutputName -Force
    Write-Host "Nen resource pack thanh cong: $ResourceOutputName"
}
Write-Host "Nén thành công: $OutputName"
