FROM maven:3-eclipse-temurin-21 AS build

COPY src /app/src
COPY pom.xml /app

WORKDIR /app
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jdk-jammy

COPY --from=build /app/target/indux-1.0.1200.jar /app/app.jar

WORKDIR /app
EXPOSE 3505

CMD ["java", "-jar", "app.jar"]
