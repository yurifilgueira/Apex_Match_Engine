FROM azul/zulu-openjdk:25-latest AS java-builder
WORKDIR /app
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
COPY apex-common/pom.xml ./apex-common/
COPY apex-engine/pom.xml ./apex-engine/
COPY apex-gateway/pom.xml ./apex-gateway/
RUN chmod +x mvnw
RUN --mount=type=cache,target=/root/.m2 ./mvnw dependency:go-offline
COPY apex-common/src ./apex-common/src
COPY apex-engine/src ./apex-engine/src
COPY apex-gateway/src ./apex-gateway/src
RUN --mount=type=cache,target=/root/.m2 ./mvnw clean install -DskipTests jar:test-jar

FROM azul/zulu-openjdk:25-latest AS java-final
WORKDIR /app
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*
COPY --from=java-builder /app/apex-engine/target/apex_matching_engine-0.0.1-SNAPSHOT.jar engine.jar
COPY --from=java-builder /app/apex-engine/target/apex_matching_engine-0.0.1-SNAPSHOT-tests.jar engine-tests.jar
COPY --from=java-builder /app/apex-gateway/target/apex-gateway-0.0.1-SNAPSHOT.jar gateway.jar
ENTRYPOINT ["java", "-jar", "engine.jar"]

FROM golang:1.25-alpine AS k6-builder
RUN apk add --no-cache git
RUN go install go.k6.io/xk6/cmd/xk6@latest
RUN xk6 build --with github.com/grafana/xk6-tcp

FROM alpine:3.18 AS k6-final
RUN apk add --no-cache ca-certificates
COPY --from=k6-builder /go/k6 /usr/bin/k6
ENTRYPOINT ["k6"]
