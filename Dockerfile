FROM openjdk:17-alpine AS build
COPY .mvn .mvn
COPY mvnw .

COPY pom.xml .
COPY src src
RUN ./mvnw -B package

From openjdk:17-alpine
COPY output output
COPY --from=build target/*.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]