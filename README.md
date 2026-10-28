# en-monitor-home-service

EnergyMonitor home microservice: creates homes and joins members to them with an access code,
manages their roles, favourites and ownership transfer, keeps the consumption thresholds and the
home type catalogue, answers the other services who may act on a home and announces deleted homes
over RabbitMQ. Spring Boot 4, Java 21, hexagonal architecture.

It is being extracted from the `home` module of `energy-monitor-backend`, following the same steps
as the security, device and measurement services. The design, the decisions and the migration
status are in [`docs/Architecture.md`](docs/Architecture.md).

## What it serves

| Path | Who calls it | What it does |
|---|---|---|
| `POST /api/v1/homes` | web and mobile, through the gateway | Creates a home; the caller is its owner |
| `GET /api/v1/homes` | idem | Homes of the caller, with its role and whether each is a favourite |
| `POST /api/v1/homes/join` | idem | Joins a home with its access code |
| `GET /api/v1/homes/{homeId}/members` | idem | Members with their names and roles (any member) |
| `PUT /api/v1/homes/{homeId}/favorite` | idem | Marks or unmarks the home as favourite |
| `DELETE /api/v1/homes/{homeId}/members/me` | idem | Leaves the home (not its last owner) |
| `DELETE /api/v1/homes/{homeId}/members/{userId}` | idem | Removes a member (owner) |
| `PUT /api/v1/homes/{homeId}/owner` | idem | Hands the home over to a member (owner) |
| `DELETE /api/v1/homes/{homeId}` | idem | Deletes the home and announces `home.deleted` (owner) |
| `GET`, `PUT /api/v1/homes/{homeId}/thresholds`, `POST .../defaults` | idem | Consumption limits of the home (changing them: owner) |
| `GET /api/v1/home-types`, `/{idHomeType}` | idem | Home type catalogue |
| `/internal/homes/**`, `/internal/accounts/**` | the other services, never the gateway | See `docs/Architecture.md` |

Swagger UI: `http://localhost:8086/swagger-ui.html` (the internal API is hidden there).

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

## Behind the gateway

Web and mobile requests arrive through the API gateway (`/api/v1/homes/**`,
`/api/v1/home-types/**`), which validates the token first; this service validates it again.
From loopback or a Docker network (`172.16.0.0/12`) the container takes the caller's address and
scheme from `X-Forwarded-For` / `X-Forwarded-Proto` (`server.forward-headers-strategy=native`,
`server.tomcat.remoteip.internal-proxies`); any other private network is not trusted, so a machine
of the LAN cannot pass as `127.0.0.1`.

The internal API checks that address: a request relayed from outside counts as the outside caller
and is refused, even if it forwards `127.0.0.1`. The gateway never forwards `/internal/**`; the
other services call this service directly over the Docker network. `BehindGatewayTest` checks it
on a running server.

## Tests

```bash
SPRING_DATASOURCE_PASSWORD=... ./mvnw test
```

The tests run under the `test` profile against the MySQL of `infra`. The RabbitMQ publisher test
also uses the RabbitMQ of `infra` (`RABBITMQ_USERNAME`, `RABBITMQ_PASSWORD`) and is skipped when no
broker answers.

## Docker and CI/CD

```bash
docker build -t energy-monitor-home .
```

Multi-stage build (Maven on JDK 21, then JRE 21), running as uid 10001. The infra repository
builds it as `home`: no published port, `public.pem` only, health on `/actuator/health`. The
gateway and the other services reach it as `home:8086`.

`.github/workflows/ci-cd.yml`:

- on every pull request and push to `dev` and `main`: `./mvnw test` against an empty MySQL 8.4
  service of the runner (Liquibase creates the home tables; no RabbitMQ needed), and
  `docker build`;
- after a push to `main`, and the approval of the `production` environment: `infra/deploy/deploy.sh
  home` over SSH, which rebuilds only this container and waits for every service to be healthy,
  then a smoke test through the gateway. The environment needs the same secrets as the other
  repositories: `DEPLOY_HOST`, `DEPLOY_USER`, `DEPLOY_SSH_KEY` and `DEPLOY_KNOWN_HOSTS`.
