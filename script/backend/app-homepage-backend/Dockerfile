FROM eclipse-temurin:21
ARG JAR_FILE=./module/application/app-homepage-backend/build/libs/app-homepage-backend.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
