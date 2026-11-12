# Stage 1: Build
FROM gradle:8-jdk21-alpine AS builder
WORKDIR /app
COPY . .
RUN gradle build --no-daemon -x test

# Stage 2: Runtime
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
# O container roda em UTC. Sem fixar o fuso, LocalDateTime.now() grava as
# transações 3h à frente do horário de Brasília (e as das 21h caem no dia seguinte).
ENTRYPOINT ["java", "-Duser.timezone=America/Sao_Paulo", "-jar", "app.jar"]

