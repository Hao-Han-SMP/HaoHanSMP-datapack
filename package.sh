#!/bin/bash

# Tên file zip đầu ra
DIR_NAME=$(basename "$PWD")
OUTPUT_DIR="out"
OUTPUT_NAME="${OUTPUT_DIR}/${DIR_NAME}.zip"
RESOURCE_PACK_DIR="resourcepack"
RESOURCE_OUTPUT_NAME="${OUTPUT_DIR}/${DIR_NAME}-resources.zip"

# Tạo thư mục out nếu chưa có
[ -d "$OUTPUT_DIR" ] || mkdir -p "$OUTPUT_DIR"

# Xóa file zip cũ nếu có
[ -f "$OUTPUT_NAME" ] && rm "$OUTPUT_NAME"

# Các thư mục và file cần nén
FILES_TO_ZIP=()
[ -d "data" ] && FILES_TO_ZIP+=("data")
[ -f "pack.mcmeta" ] && FILES_TO_ZIP+=("pack.mcmeta")
[ -f "pack.png" ] && FILES_TO_ZIP+=("pack.png")
[ -f "LICENSE" ] && FILES_TO_ZIP+=("LICENSE")
[ -f "README.md" ] && FILES_TO_ZIP+=("README.md")

if [ ${#FILES_TO_ZIP[@]} -eq 0 ]; then
    echo "Lỗi: Không tìm thấy file datapack nào để nén!"
    exit 1
fi

echo "Đang nén datapack..."

# 1. Dùng lệnh 'zip' (Chế độ im lặng -q)
if command -v zip >/dev/null 2>&1; then
    zip -rq "$OUTPUT_NAME" "${FILES_TO_ZIP[@]}"

    if [ -d "$RESOURCE_PACK_DIR" ]; then
        [ -f "$RESOURCE_OUTPUT_NAME" ] && rm "$RESOURCE_OUTPUT_NAME"
        (
            cd "$RESOURCE_PACK_DIR" || exit 1
            zip -rq "../$RESOURCE_OUTPUT_NAME" .
        )
        echo "Nen resource pack thanh cong: $RESOURCE_OUTPUT_NAME"
    fi

    echo "Nén thành công: $OUTPUT_NAME"
    exit 0
else
    echo "Lỗi: Không tìm thấy lệnh 'zip' trên hệ thống!"
    echo "Vui lòng cài đặt 'zip' (ví dụ: 'sudo apt install zip' trên Debian/Ubuntu) để tiếp tục."
    exit 1
fi

