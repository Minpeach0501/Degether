FROM openjdk:8-jre
COPY build/libs/degether-0.0.1-SNAPSHOT.jar degether.jar
ENTRYPOINT ["java", "-jar", "degether.jar"]