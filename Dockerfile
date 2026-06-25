FROM maven:3.9.11-eclipse-temurin-17 AS build

WORKDIR /app

COPY pom.xml .
RUN mvn -B -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -B -DskipTests clean package

FROM tomcat:10.1-jdk17-temurin

ENV CATALINA_OPTS="-Dfile.encoding=UTF-8 -Djava.security.egd=file:/dev/./urandom"

RUN rm -rf /usr/local/tomcat/webapps/*

COPY --from=build /app/target/CarRentalSystem.war /usr/local/tomcat/webapps/CarRentalSystem.war

EXPOSE 8080

CMD ["sh", "-c", "sed -i \"s/port=\\\"8080\\\"/port=\\\"${PORT:-8080}\\\"/\" /usr/local/tomcat/conf/server.xml && catalina.sh run"]
