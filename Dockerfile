FROM openjdk:13-alpine
WORKDIR /app
COPY . .
RUN chmod +x gradlew
RUN ./gradlew build --console=plain --info

FROM openjdk:13-alpine
WORKDIR /app
COPY --from=0 /app/build/libs/budget-analyzer.jar .
CMD java -Xms64m -Xmx512m -jar budget-analyzer.jar
