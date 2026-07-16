# Paso 1: Compilar la aplicación usando Maven con JDK 17
FROM maven:3.8.5-openjdk-17 AS build
COPY . .
RUN mvn clean package -DskipTests

# Paso 2: Crear la imagen de ejecución ligera usando Eclipse Temurin (JDK 17)
FROM eclipse-temurin:17-jre-slim
COPY --from=build /target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]