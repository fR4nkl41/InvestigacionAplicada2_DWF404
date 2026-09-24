#Etapa 1: Build con Maven
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

#Copiar primero el pom.xml para aprovechar el caché de dependencias
COPY pom.xml .
RUN mvn dependency:go-offline -B

#Copiar el código fuente y construir el JAR
COPY src ./src
RUN mvn clean package -DskipTests


#Etapa 2: Runtime ligero
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

#Crear usuario no root por seguridad
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

#Copiar el JAR desde la etapa de build
COPY --from=build /app/target/*.jar app.jar

#Exponer el puerto
EXPOSE 8080

#Variables de entorno por defecto
ENV JAVA_OPTS="-Xms256m -Xmx512m"

#Healthcheck
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD wget --quiet --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
