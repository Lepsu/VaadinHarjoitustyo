# Buildivaihe
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Kopioi Maven wrapper ja pom.xml
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Lataa riippuvuudet
RUN ./mvnw dependency:resolve -q

# Kopioi lähdekoodi ja buildaa
COPY src src
RUN ./mvnw package -DskipTests -Pproduction

# Ajovaihee
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]