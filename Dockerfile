# Stage 1: Build the application using Maven
FROM maven:latest AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

# Stage 2: Create the final image to run the application
FROM openjdk:17-jdk-slim

WORKDIR /app

COPY --from=build /app/target/*.jar ./app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
