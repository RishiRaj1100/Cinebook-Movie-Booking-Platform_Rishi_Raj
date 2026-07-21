# Multi-stage Dockerfile for CineBook Spring Boot Backend
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests && \
    mkdir -p /app/target/extracted && \
    cd /app/target/extracted && \
    jar -xf /app/target/cinebook-backend-1.0.0.jar

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/extracted/BOOT-INF/lib /app/lib
COPY --from=build /app/target/extracted/BOOT-INF/classes /app/classes
COPY --from=build /app/target/extracted/META-INF /app/META-INF
EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=postgres
CMD ["java", "-cp", "classes:lib/*", "com.cinebook.CinebookApplication"]
