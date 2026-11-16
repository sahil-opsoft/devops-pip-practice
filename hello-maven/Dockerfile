# ---------- Stage 1: build the JAR with Maven ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /src
# Copy only pom.xml first: dependencies are downloaded in their own cached layer
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
# Source changes only invalidate the layers from here down
COPY src ./src
RUN mvn -B package dependency:copy-dependencies -DincludeScope=runtime -DoutputDirectory=target/lib

# ---------- Stage 2: small runtime image ----------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S app && adduser -S app -G app
COPY --from=build /src/target/lib/ lib/
COPY --from=build /src/target/hello-maven-1.0-SNAPSHOT.jar app.jar
USER app
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=3s CMD wget -qO- http://localhost:8080/ || exit 1
ENTRYPOINT ["java", "-cp", "app.jar:lib/*", "com.pip.App"]
