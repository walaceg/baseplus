# syntax=docker/dockerfile:1

FROM maven:3.9-eclipse-temurin-17-alpine AS build

WORKDIR /workspace

COPY pom.xml ./
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -ntp dependency:go-offline

COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -ntp -DskipTests clean package

FROM eclipse-temurin:17-jre-alpine AS runtime

RUN addgroup -S baseplus \
    && adduser -S -D -H -u 10001 -G baseplus baseplus

WORKDIR /app

RUN mkdir -p /app/uploads \
    && chown baseplus:baseplus /app /app/uploads

COPY --from=build --chown=baseplus:baseplus /workspace/target/baseplus-backend-*.jar /app/app.jar

USER baseplus:baseplus

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
