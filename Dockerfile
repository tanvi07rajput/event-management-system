# ========================================================
# Dockerfile for Event Management System (EMS Web v2.0)
# Cloud deployment ready (Render, Railway, Fly.io, etc.)
# ========================================================

FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

# Copy libraries, sources, and resources
COPY lib/ ./lib/
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

# Dynamic cloud port (default 8080)
ENV PORT=8080
EXPOSE 8080

CMD ["java", "-cp", "bin:lib/*", "com.ems.Main"]

