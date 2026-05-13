# syntax=docker/dockerfile:1

FROM gradle:9.5.1-jdk21 AS build
WORKDIR /app

# Copy Gradle wrapper and metadata first to keep layer caching effective.
COPY gradlew gradlew.bat build.gradle.kts settings.gradle.kts ./
COPY gradle ./gradle

# Copy source and build the executable Spring Boot jar.
COPY src ./src
RUN gradle bootJar -x test --no-daemon

FROM eclipse-temurin:25-jre
WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

