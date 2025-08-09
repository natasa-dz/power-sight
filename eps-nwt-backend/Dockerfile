FROM eclipse-temurin:21-jdk

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

# Install netcat for TCP port checking
RUN apt-get update && apt-get install -y netcat-openbsd && rm -rf /var/lib/apt/lists/*

# Add wait script
COPY wait-for-postgres.sh wait-for-postgres.sh
RUN chmod +x wait-for-postgres.sh

ENTRYPOINT ["./wait-for-postgres.sh"]
