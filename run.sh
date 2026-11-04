#!/bin/bash
# ========================================================
# Event Management System (EMS Web v2.0)
# One-Click Web Server & Application Launcher
# ========================================================

set -e

# Change directory to script location
cd "$(dirname "$0")"

echo "========================================================"
echo "🎟️  Launching Event Management System (Web Application)"
echo "========================================================"

# Auto-detect installed JDK
if [ -z "$JAVA_HOME" ] || [ ! -d "$JAVA_HOME" ]; then
    for jdk in /Library/Java/JavaVirtualMachines/*/Contents/Home; do
        if [ -d "$jdk" ]; then
            export JAVA_HOME="$jdk"
            export PATH="$JAVA_HOME/bin:$PATH"
            break
        fi
    done
fi

if ! command -v java &> /dev/null; then
    echo "❌ Error: Java Runtime (JDK) is not located."
    exit 1
fi

JAVA_VER=$(java -version 2>&1 | head -n 1)
echo "✓ Using Java: $JAVA_VER"

# Ensure lib and bin
mkdir -p lib bin

# Copy resources
if [ -d "src/main/resources" ]; then
    cp -r src/main/resources/* bin/
fi

# Compile all classes
echo "🔨 Compiling web application..."
javac -cp "lib/*:bin" -d bin $(find src/main/java -name "*.java")

echo "✓ Compilation successful!"
echo "🚀 Starting Web Application Server..."
echo "========================================================"

# Start Web Server & auto-open browser
java -cp "bin:lib/*" com.ems.Main
