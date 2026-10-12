FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /workspace

ARG MODULE

COPY . .

RUN mvn -pl ${MODULE} -am clean package -DskipTests

RUN mkdir -p /output && \
    cp ${MODULE}/target/*.jar /output/app.jar


FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /output/app.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]