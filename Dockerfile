# Paso 1: Compilar la aplicación usando Maven con Amazon Corretto 17
FROM maven:3.9-amazoncorretto-17 AS build
COPY . .
RUN mvn clean package -DskipTests

# Paso 2: Crear la imagen de ejecución ligera usando Amazon Corretto (JDK 17)
FROM amazoncorretto:17-alpine-jdk
COPY --from=build /target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]