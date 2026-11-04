#!/bin/bash
# Double-clickable macOS launcher for Event Management System (Web Application)
cd "$(dirname "$0")"

for jdk in /Library/Java/JavaVirtualMachines/*/Contents/Home; do
    if [ -d "$jdk" ]; then
        export JAVA_HOME="$jdk"
        export PATH="$JAVA_HOME/bin:$PATH"
        break
    fi
done

echo "================================================="
echo "🎟️ Starting EMS Web Server..."
echo "================================================="

# Compile if needed
if [ ! -d "bin/com" ]; then
    mkdir -p bin
    javac -cp "lib/*:bin" -d bin $(find src/main/java -name "*.java")
    cp -r src/main/resources/* bin/
fi

# Launch Web Server (opens http://localhost:8080 in default browser)
java -cp "bin:lib/*" com.ems.Main
