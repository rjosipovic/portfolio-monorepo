FROM amazoncorretto:17
COPY ./target/analytics-manager-0.0.1-SNAPSHOT.jar /usr/src
WORKDIR /usr/src
EXPOSE 8080
CMD ["java", "-jar", "analytics-manager-0.0.1-SNAPSHOT.jar"]