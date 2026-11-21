# Etapa 1: Compilación del proyecto
FROM gradle:8.7-jdk17 AS build
WORKDIR /app
COPY . .
RUN gradle bootJar --no-daemon -x test && rm -f build/libs/*-plain.jar

# Etapa 2: Ejecución ligera en producción
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
EXPOSE 8080
COPY --from=build /app/build/libs/*.jar app.jar
ENTRYPOINT ["sh", "-c", "java -XX:MaxRAMPercentage=75.0 -Dserver.port=${PORT:-8080} -jar app.jar"]