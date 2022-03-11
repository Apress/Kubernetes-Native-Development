#
# Dev stage
#
FROM registry.access.redhat.com/ubi8/openjdk-11 AS build
WORKDIR /home/jboss
COPY pom.xml /home/jboss
COPY . /home/jboss
CMD ["./mvnw","compile","quarkus:dev", "-Dquarkus.datasource.devservices=false", "-Dquarkus.datasource.jdbc.url=jdbc:postgresql://postgis:5432/news?user=postgres&password=banane"]
