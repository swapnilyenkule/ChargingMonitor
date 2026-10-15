#!/bin/sh
# Gradle wrapper script - downloads and runs Gradle automatically
set -e
APP_NAME="Gradle"
DIRNAME=$(dirname "$0")
CLASSPATH=$DIRNAME/gradle/wrapper/gradle-wrapper.jar

# Download gradle-wrapper.jar if missing
if [ ! -f "$CLASSPATH" ]; then
    WRAPPER_URL="https://services.gradle.org/distributions/gradle-8.2-bin.zip"
    echo "Downloading Gradle..."
    mkdir -p "$DIRNAME/gradle/wrapper"
    curl -sL "https://raw.githubusercontent.com/gradle/gradle/v8.2.0/gradle/wrapper/gradle-wrapper.jar" -o "$CLASSPATH"
fi

exec java -jar "$CLASSPATH" "$@"
