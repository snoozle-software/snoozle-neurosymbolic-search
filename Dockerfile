FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn -B package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /workspace/target/*.jar app.jar
ENV NEO4J_URI=bolt://neo4j:7687 \
    NEO4J_USERNAME=neo4j \
    NEO4J_PASSWORD=password \
    EMBEDDING_SERVICE_URL=http://embedding-service:5000/embed
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
