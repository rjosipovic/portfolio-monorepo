# --- Stage 1: Build the application ---
# Use a Maven image that includes a JDK to build our application
FROM eclipse-temurin:17-jdk-jammy AS builder

# Set the working directory
WORKDIR /app

# Copy the Maven wrapper and pom.xml to resolve dependencies
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Download dependencies to a separate, cacheable layer
RUN ./mvnw dependency:go-offline

# Copy the rest of the source code
COPY src ./src

# Build the application, skipping tests as they should be run in a separate CI step
RUN ./mvnw package -DskipTests

# --- Stage 2: Create the final, optimized runtime image ---
# Use a minimal, secure base image
FROM eclipse-temurin:17-jre-jammy

# Set the working directory
WORKDIR /app

# Create a non-privileged user and group to run the application
RUN addgroup --system appgroup && adduser --system --ingroup appgroup --no-create-home appuser
USER appuser

# Copy the built JAR from the 'builder' stage
COPY --from=builder /app/target/*.jar app.jar

# Expose the port the application runs on
EXPOSE 8080

# Define the command to run the application with container-aware memory settings
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-jar", "app.jar"]