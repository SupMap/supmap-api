FROM maven:3.9-amazoncorretto-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests


FROM amazoncorretto:21-alpine
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=build /app/target/supmap-api-0.0.1-SNAPSHOT.jar app.jar

ENV SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/supmap-database
ENV SPRING_DATASOURCE_USERNAME=supmap
ENV SPRING_DATASOURCE_PASSWORD=supmap

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
