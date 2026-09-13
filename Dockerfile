# syntax=docker/dockerfile:1

# ---- build stage: resolve dependencies first so they are cached independently of source changes
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline

COPY src/ src/
RUN ./mvnw -B -q -DskipTests package \
 && java -Djarmode=tools -jar target/*.jar extract --layers --launcher --destination extracted

# ---- runtime stage: small JRE, non-root user, layered copy for better cache reuse on redeploys
FROM eclipse-temurin:21-jre-alpine AS runtime

RUN addgroup -S hms && adduser -S -G hms -H -s /sbin/nologin hms
WORKDIR /app

COPY --from=build --chown=hms:hms /workspace/extracted/dependencies/ ./
COPY --from=build --chown=hms:hms /workspace/extracted/spring-boot-loader/ ./
COPY --from=build --chown=hms:hms /workspace/extracted/snapshot-dependencies/ ./
COPY --from=build --chown=hms:hms /workspace/extracted/application/ ./

USER hms

# Tuned for a small (512 MiB) Cloud Run instance: cap heap, single-threaded GC, fast start.
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=70 -XX:+UseSerialGC -XX:TieredStopAtLevel=1 -Xss512k -XX:+ExitOnOutOfMemoryError"

EXPOSE 8080
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
