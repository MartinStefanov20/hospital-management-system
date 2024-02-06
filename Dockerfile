FROM openjdk:11
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

COPY src/main/resources/templates/ /app/templates/
COPY src/main/resources/static/ /app/static/

ENTRYPOINT ["java", "-jar", "/app.jar"]