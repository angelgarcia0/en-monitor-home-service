# en-monitor-home-service

EnergyMonitor home microservice: creates homes and joins members to them with an access code,
manages their roles, favourites and ownership transfer, keeps the consumption thresholds and the
home type catalogue, answers the other services who may act on a home and announces deleted homes
over RabbitMQ. Spring Boot 4, Java 21, hexagonal architecture.

It is being extracted from the `home` module of `energy-monitor-backend`, following the same steps
as the security, device and measurement services.

## Requirements

- Java 21 (the repository brings `./mvnw`, no Maven install needed).
- MySQL from the `infra` repository: the schema is shared with `energy-monitor-backend` and the
  security, device and measurement services.

## Run locally

```bash
cp .env.example .env
cp src/main/resources/application-local.yaml.example src/main/resources/application-local.yaml
# set the MYSQL_PASSWORD of infra/.env in both files
./mvnw spring-boot:run          # http://localhost:8086/actuator/health
```

## Tests

```bash
SPRING_DATASOURCE_PASSWORD=... ./mvnw test
```

The tests run under the `test` profile against the MySQL of `infra`.
