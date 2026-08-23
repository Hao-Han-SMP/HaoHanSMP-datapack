#!/bin/bash

DIR_NAME=$(basename "$PWD")
OUTPUT_DIR="out"
OUTPUT_NAME="${OUTPUT_DIR}/${DIR_NAME}.zip"
RESOURCE_PACK_DIR="resourcepack"
RESOURCE_OUTPUT_NAME="${OUTPUT_DIR}/${DIR_NAME}-resources.zip"

[ -d "$OUTPUT_DIR" ] || mkdir -p "$OUTPUT_DIR"
[ -f "$OUTPUT_NAME" ] && rm "$OUTPUT_NAME"

FILES_TO_ZIP=()
[ -d "data" ] && FILES_TO_ZIP+=("data")
[ -f "pack.mcmeta" ] && FILES_TO_ZIP+=("pack.mcmeta")
[ -f "pack.png" ] && FILES_TO_ZIP+=("pack.png")
[ -f "LICENSE" ] && FILES_TO_ZIP+=("LICENSE")
[ -f "README.md" ] && FILES_TO_ZIP+=("README.md")

if [ ${#FILES_TO_ZIP[@]} -eq 0 ]; then
    echo "Error: No datapack files found to package!"
    exit 1
fi

echo "Packaging datapack..."

if command -v zip >/dev/null 2>&1; then
    zip -rq "$OUTPUT_NAME" "${FILES_TO_ZIP[@]}"

    if [ -d "$RESOURCE_PACK_DIR" ]; then
        [ -f "$RESOURCE_OUTPUT_NAME" ] && rm "$RESOURCE_OUTPUT_NAME"
        (
            cd "$RESOURCE_PACK_DIR" || exit 1
            zip -rq "../$RESOURCE_OUTPUT_NAME" .
        )
        echo "Resource pack packaged successfully: $RESOURCE_OUTPUT_NAME"
    fi

    echo "Successfully packaged: $OUTPUT_NAME"
    exit 0

else
    echo "Error: 'zip' command not found on system!"
    echo "Please install 'zip' (e.g. 'sudo apt install zip' on Debian/Ubuntu) to proceed."
    exit 1
fi


