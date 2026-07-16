# Paso 1: Compilar la aplicación usando Maven con JDK 17 (o cambia el 17 por tu versión de Java)
FROM maven:3.8.5-openjdk-17 AS build
COPY . .
RUN mvn clean package -DskipTests

# Paso 2: Crear la imagen de ejecución ligera
FROM openjdk:17-jdk-slim
COPY --from=build /target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]  