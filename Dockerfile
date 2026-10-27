FROM maven:3.9.16-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN --mount=type=cache,id=maven-repo,target=/root/.m2 mvn -B clean package -DskipTests

FROM eclipse-temurin:21-jre

WORKDIR /app

RUN groupadd --system app && useradd --system --gid app --no-create-home --shell /usr/sbin/nologin app

COPY --from=build --chown=app:app /app/target/*.jar app.jar

EXPOSE 8080

USER app

HEALTHCHECK --interval=10s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -fsS http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]