FROM openjdk:21-jdk
ARG JAR_FILE=./module/application/app-general-batch/build/libs/app-general-batch.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]