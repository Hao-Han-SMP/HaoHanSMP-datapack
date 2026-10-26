#!/bin/bash

OUTPUT_DIR="out"
[ -d "$OUTPUT_DIR" ] || mkdir -p "$OUTPUT_DIR"

if [ -f "build.gradle" ]; then
    echo "Building HaoHan-WorldGen Paper/Purpur plugin jar..."
    ./gradlew jar
    JAR_SOURCE="build/libs/HaoHan-WorldGen-1.0.0.jar"
    if [ -f "$JAR_SOURCE" ]; then
        cp "$JAR_SOURCE" "${OUTPUT_DIR}/HaoHan-WorldGen-1.0.0.jar"
        echo "Plugin jar packaged successfully: ${OUTPUT_DIR}/HaoHan-WorldGen-1.0.0.jar"
    fi
fi
