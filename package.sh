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

if [ ${#FILES_TO_ZIP[@]} -gt 0 ]; then
    echo "Packaging datapack zip..."
    if command -v zip >/dev/null 2>&1; then
        zip -rq "$OUTPUT_NAME" "${FILES_TO_ZIP[@]}"
        echo "Successfully packaged datapack zip: $OUTPUT_NAME"
    fi
fi

if [ -d "$RESOURCE_PACK_DIR" ]; then
    [ -f "$RESOURCE_OUTPUT_NAME" ] && rm "$RESOURCE_OUTPUT_NAME"
    (
        cd "$RESOURCE_PACK_DIR" || exit 1
        zip -rq "../$RESOURCE_OUTPUT_NAME" .
    )
    echo "Resource pack packaged successfully: $RESOURCE_OUTPUT_NAME"
fi

if [ -f "build.gradle" ]; then
    echo "Building Purpur/Paper plugin jar..."
    ./gradlew jar
    JAR_SOURCE="build/libs/HaoHanSMP-datapack-1.0.0.jar"
    if [ -f "$JAR_SOURCE" ]; then
        cp "$JAR_SOURCE" "${OUTPUT_DIR}/HaoHanSMP-datapack-1.0.0.jar"
        echo "Plugin jar packaged successfully: ${OUTPUT_DIR}/HaoHanSMP-datapack-1.0.0.jar"
    fi
fi
