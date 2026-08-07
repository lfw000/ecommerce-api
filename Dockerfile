# 1. Stage: Build

# Image to compile
FROM eclipse-temurin:21-jdk-alpine AS builder

# Set the workidr
WORKDIR /app

# Copy Maven files
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Download dependencies (cacheable layer)
RUN ./mvnw dependency:go-offline

# Copy source code
COPY src src

# Compile and packaging (ignore tests to make it faster)
RUN ./mvnw package -DskipTests

# 2. Stage: Execution

# Image to execute
FROM eclipse-temurin:21-jre-alpine

# Instal useful tools (optional)
RUN apk add --no-cache curl

# Create non-root user (for better security)
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Set the workdir
WORKDIR /app

# Copy JAR from the build stage
COPY --from=builder /app/target/*.jar app.jar

# Create folder for logs
RUN mkdir -p /var/log/ecommerce-api && chown appuser:appgroup /var/log/ecommerce-api

# Use non-root user
USER appuser

# Define the port
EXPOSE 8080

# Environment variables
ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Start command
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]