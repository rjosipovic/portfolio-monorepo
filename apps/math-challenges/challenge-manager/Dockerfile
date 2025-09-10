FROM amazoncorretto:17
COPY ./target/challenge-manager-0.0.1-SNAPSHOT.jar /usr/src/
WORKDIR /usr/src/
EXPOSE 8080
CMD ["java", "-jar", "challenge-manager-0.0.1-SNAPSHOT.jar"]