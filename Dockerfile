FROM azul/zulu-openjdk:25-latest AS builder
WORKDIR /app
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw
RUN --mount=type=cache,target=/root/.m2 ./mvnw dependency:go-offline
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 ./mvnw clean package -DskipTests jar:test-jar

FROM azul/zulu-openjdk:25-latest
LABEL authors="yuris"
WORKDIR /app
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*
COPY --from=builder /app/target/apex_matching_engine-0.0.1-SNAPSHOT.jar app.jar
COPY --from=builder /app/target/apex_matching_engine-0.0.1-SNAPSHOT-tests.jar tests.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
