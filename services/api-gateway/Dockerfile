# --- Stage 1: Build the application ---
# Use a Maven image that includes a JDK to build our application
FROM maven:3.8-amazoncorretto-17 AS build

# Set the working directory
WORKDIR /app

# Copy the pom.xml first to leverage Docker's layer caching.
# This layer will only be rebuilt if the pom.xml changes.
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy the rest of the source code
COPY src ./src

# Build the application, skipping tests as they should be run in a separate CI step
RUN mvn package -DskipTests

# --- Stage 2: Create the final, optimized runtime image ---
# Use a minimal, secure base image
FROM amazoncorretto:17-alpine-jdk

# Set the working directory
WORKDIR /app

# Create a non-privileged user and group to run the application
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copy the built JAR from the 'build' stage
# The JAR name is now dynamic, based on the pom.xml artifactId and version
COPY --from=build /app/target/*.jar app.jar

# Expose the port the application runs on
EXPOSE 8000

# Define the command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]