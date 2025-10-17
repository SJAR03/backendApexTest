# Primer paso: BUILDER
# Usa una imagen de Maven para compilar el proyecto
FROM maven:3.9.5-eclipse-temurin-21 AS builder
LABEL authors="SJAR03"
WORKDIR /app
COPY pom.xml .
COPY src ./src
# Compila el JAR
RUN mvn clean package -DskipTests

# Segundo paso: RUNNER (Imagen final)
# Usa la imagen ligera de JRE para ejecutar la aplicación
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
# Copia solo el JAR
COPY --from=builder /app/target/orders-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]