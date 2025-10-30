# Multi-stage build for Spring Boot application

# Stage 1: Build with Maven
FROM maven:3.9-eclipse-temurin-17-alpine AS builder

WORKDIR /app

# Copy parent POM and module POMs
COPY pom.xml .
COPY supplychainx-common/pom.xml supplychainx-common/
COPY supplychainx-security/pom.xml supplychainx-security/
COPY supplychainx-supply/pom.xml supplychainx-supply/
COPY supplychainx-production/pom.xml supplychainx-production/
COPY supplychainx-delivery/pom.xml supplychainx-delivery/
COPY supplychainx-audit/pom.xml supplychainx-audit/
COPY supplychainx-app/pom.xml supplychainx-app/

# Download dependencies (cached layer)
RUN mvn dependency:go-offline -B

# Copy source code
COPY supplychainx-common/src supplychainx-common/src
COPY supplychainx-security/src supplychainx-security/src
COPY supplychainx-supply/src supplychainx-supply/src
COPY supplychainx-production/src supplychainx-production/src
COPY supplychainx-delivery/src supplychainx-delivery/src
COPY supplychainx-audit/src supplychainx-audit/src
COPY supplychainx-app/src supplychainx-app/src

# Build the application
RUN mvn clean package -DskipTests -pl supplychainx-app -am

# Stage 2: Runtime with JRE
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Create non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy JAR from builder stage
COPY --from=builder /app/supplychainx-app/target/supplychainx-app-*.jar app.jar

# Expose application port
EXPOSE 8090

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8090/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", \
  "app.jar"]
