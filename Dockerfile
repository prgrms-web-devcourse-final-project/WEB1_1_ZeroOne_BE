FROM openjdk:17-jdk

ARG JAR_FILE=build/libs/backend-0.0.1-SNAPSHOT.jar

COPY ${JAR_FILE} palettee-server.jar

CMD ["java", "-jar", "-Duser.timezone=Asia/Seoul", "palettee-server.jar"]
