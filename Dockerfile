# ── Build stage ──────────────────────────────────────────────────────────────
# Run `mvn package -DskipTests` before building this image,
# or switch to a multi-stage build with a maven image once Java 25 variants are stable.
FROM eclipse-temurin:25-jre AS runtime

WORKDIR /app

# Run as a non-root user
RUN groupadd --system appgroup \
 && useradd  --system --gid appgroup appuser
USER appuser

COPY target/insurance-bff-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
