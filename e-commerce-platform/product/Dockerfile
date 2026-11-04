FROM maven:3.9.16-eclipse-temurin-26 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -pl product -am -DskipTests

FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /app/product/target/*.jar app.jar
COPY --from=build /root/.m2/repository/io/opentelemetry/javaagent/opentelemetry-javaagent/*/opentelemetry-javaagent-*.jar libs/opentelemetry-javaagent.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
