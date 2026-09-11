FROM eclipse-temurin:25-jdk AS build

WORKDIR /workspace

COPY pom.xml mvnw ./
COPY .mvn/ .mvn/
RUN chmod +x mvnw

COPY src/ src/
RUN ./mvnw clean verify
RUN cp target/mestre-0.0.1-SNAPSHOT.jar /tmp/application.jar

FROM eclipse-temurin:25-jre

WORKDIR /app
COPY --from=build /tmp/application.jar application.jar

EXPOSE 8080
USER 10001:10001

ENTRYPOINT ["java", "-jar", "/app/application.jar"]
