FROM amazoncorretto:17
COPY ./target/user-manager-0.0.1-SNAPSHOT.jar /usr/src
WORKDIR /usr/src
EXPOSE 8080
CMD ["java", "-jar", "user-manager-0.0.1-SNAPSHOT.jar"]