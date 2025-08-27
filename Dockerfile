# Etapa 1: build (opcional si compilas localmente)
# FROM maven:3.9.6-eclipse-temurin-21 AS build
# WORKDIR /app
# COPY . .
# RUN mvn clean package -DskipTests

# Etapa 2: runtime
FROM eclipse-temurin:21-jdk-alpine

# Directorio de trabajo dentro del contenedor
WORKDIR /app

# Copia el JAR generado al contenedor
COPY target/security-0.0.1-SNAPSHOT.jar app.jar

# Exponemos el puerto que usará Spring Boot (si usas otro, cambia aquí)
EXPOSE 8080

# Comando para ejecutar la app
ENTRYPOINT ["java", "-jar", "app.jar"]
