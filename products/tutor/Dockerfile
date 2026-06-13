FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app
COPY tutor-backend/ .
RUN ./mvnw clean package -DskipTests -pl tutor-app -am

FROM eclipse-temurin:21-jre-jammy
RUN groupadd -g 1001 appuser && useradd -u 1001 -g appuser -s /bin/false appuser
USER appuser
WORKDIR /app
COPY --from=build /app/tutor-app/target/*.jar app.jar
EXPOSE 8080 9090
ENTRYPOINT ["java", "-jar", "app.jar"]
