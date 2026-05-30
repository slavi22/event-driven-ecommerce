FROM maven:3.9.16-eclipse-temurin-26 AS build
WORKDIR /app
COPY pom.xml .
COPY common/ common/
COPY product/ product/
RUN mvn clean package -pl product -am -DskipTests

FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /app/product/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
