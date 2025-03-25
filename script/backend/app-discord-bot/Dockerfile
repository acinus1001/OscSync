FROM openjdk:21-jdk
ARG JAR_FILE=./module/application/app-discord-bot/build/libs/app-discord-bot.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]