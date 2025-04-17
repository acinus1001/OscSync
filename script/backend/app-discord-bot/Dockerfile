FROM openjdk:21-jdk

ARG BUILD_ROOT=./module/application/app-discord-bot/build/libs
ARG JAR_FILE=${BUILD_ROOT}/app-discord-bot-plain.jar
ARG DEPENDENCY_ROOT=${BUILD_ROOT}/lib/

COPY ${DEPENDENCY_ROOT} /lib/

COPY ${JAR_FILE} /app.jar

ENTRYPOINT ["java", "-cp", "/app.jar:/lib/*", "dev.kuro9.DiscordBotApplicationKt"]