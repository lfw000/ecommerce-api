# Stage 1: Build
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder

# Set the working directory
WORKDIR /app

# Copy the pom.xml
COPY pom.xml .

# Download dependencies (cacheable layer)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src src

# Compile and package (skip tests for speed)
RUN mvn package -DskipTests -B

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine

# Install useful tools
RUN apk add --no-cache curl

# Create non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup


# Set the working directory
WORKDIR /app

# Copy JAR from the build stage
COPY --from=builder /app/target/*.jar app.jar

# Create directory for logs
RUN mkdir -p /var/log/ecommerce-api && chown appuser:appgroup /var/log/ecommerce-api

# Change to non-root user
USER appuser

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# JVM configuration (can be changed on the docker-compose)
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Start the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
