# ========================================================
# Dockerfile for Event Management System (EMS Web v2.0)
# Cloud deployment ready (Render, Railway, Fly.io, etc.)
# ========================================================

FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

# Auto-download required libraries during container build
RUN mkdir -p lib && \
    wget -q "https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.3.0/mysql-connector-j-8.3.0.jar" -O lib/mysql-connector-j-8.3.0.jar && \
    wget -q "https://repo1.maven.org/maven2/com/formdev/flatlaf/3.4.1/flatlaf-3.4.1.jar" -O lib/flatlaf-3.4.1.jar

# Copy source code and resources
COPY src/ ./src/

# Compile application
RUN mkdir -p bin && \
    javac -cp "lib/*:bin" -d bin $(find src/main/java -name "*.java") && \
    cp -r src/main/resources/* bin/

# Production runner image (lightweight JRE)
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY --from=builder /app/bin ./bin
COPY --from=builder /app/lib ./lib

# Dynamic cloud port (Render sets $PORT automatically, defaults to 8080)
ENV PORT=8080
EXPOSE 8080

CMD ["java", "-cp", "bin:lib/*", "com.ems.Main"]
