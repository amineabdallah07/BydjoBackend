FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn package -DskipTests -B


FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN apk add --no-cache fontconfig ttf-dejavu

COPY --from=build /app/target/*.jar app.jar

RUN mkdir -p /app/uploads

EXPOSE 8080

ENV JAVA_OPTS="-Xmx512m -Xms256m"
ENV PORT=8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]