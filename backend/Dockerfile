FROM gradle:8.4-jdk17 AS build
WORKDIR /home/gradle/src
COPY --chown=gradle:gradle build.gradle.kts settings.gradle.kts ./
RUN gradle dependencies --no-daemon || true
COPY --chown=gradle:gradle . .

RUN gradle build -x test --no-daemon

FROM eclipse-temurin:17-jre-jammy
VOLUME /tmp

RUN mkdir -p ./data

COPY --from=build /home/gradle/src/build/libs/*.jar app.jar

# Health check: ping /actuator/health every 30s, timeout after 10s, retries 3 times
# health check will be managed by traefik
# HEALTHCHECK --interval=30s --timeout=10s --start-period=10s --retries=3 \
#  CMD curl --fail http://localhost:7070/actuator/health || exit 1

ENTRYPOINT ["java", "-XX:-UseContainerSupport", "-jar", "/app.jar"]