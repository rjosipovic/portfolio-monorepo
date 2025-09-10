FROM amazoncorretto:17
COPY ./target/notification-manager-0.0.1-SNAPSHOT.jar /usr/src
WORKDIR /usr/src
EXPOSE 8080
CMD ["java", "-jar", "notification-manager-0.0.1-SNAPSHOT.jar"]