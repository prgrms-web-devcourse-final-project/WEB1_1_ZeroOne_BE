FROM openjdk:17-jdk-alpine
VOLUME /tmp
COPY build/libs/*.jar palettee-server.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/palettee-server.jar"]
