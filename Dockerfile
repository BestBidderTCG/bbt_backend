# Etapa 1: Compilación del proyecto
FROM gradle:8.7-jdk17 AS build
WORKDIR /app
COPY . .
RUN gradle bootJar --no-daemon -x test

# Etapa 2: Ejecución ligera en producción
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
EXPOSE 8080
COPY --from=build /app/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]