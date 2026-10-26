# Architecture

`home-service` follows **Hexagonal Architecture (Ports and Adapters)**, like the other services
of EnergyMonitor. The business rules of a home (membership, roles, ownership, thresholds) stay
isolated from Spring, JPA, HTTP and RabbitMQ, so they can be tested on their own.

The code is the `home` module of `energy-monitor-backend`, moved out with its structure, tests
and changesets. The package is `com.energymonitor.home`.

## Layers

```
com.energymonitor.home
│
├── api                     → Events: HomeDeleted, HomeOwnershipTransferred; LimitPeriod
├── domain/model            → Home, UserHome, Role, permissions, HomeThresholds, HomeType
├── application
│   ├── command, result     → Input and output of the use cases
│   ├── exception           → Application exceptions
│   ├── port/in             → Use case interfaces (CreateHome, JoinHome, TransferHomeOwnership,
│   │                         DescribeHomeAccess, DescribeHome, CheckAccountDeletion, ...)
│   ├── port/out            → Persistence, events, identifiers, access codes, system defaults,
│   │                         UserDirectoryPort
│   └── usecase             → One service per use case (no Spring annotations)
├── adapter
│   ├── in/web              → Home, threshold and home type endpoints
│   ├── in/internal         → Internal API for the other services
│   ├── in/messaging        → account.deleted consumer
│   └── out
│       ├── persistence     → JPA entities, repositories, mappers, adapters
│       ├── messaging       → Transactional outbox, relay and RabbitMQ publisher
│       ├── event           → Publishes the domain events and writes home.deleted to the outbox
│       ├── security        → Asks the security service for the names of the users (HTTP)
│       ├── config          → Default consumption limits
│       └── generator       → Identifiers and access codes
└── infrastructure          → Spring wiring: filter chains, JWT validation, internal API,
                              messaging, transactions, configuration properties
```

- **Domain**: no Spring, JPA or HTTP; the roles and their permissions live here.
- **Application**: plain classes registered by `HomeServiceConfiguration`. Transactions are
  applied around them in `infrastructure` (`TransactionalDeleteHome`,
  `TransactionalTransferHomeOwnership`), not inside the use cases.
- **Adapters**: thin controllers and consumers; everything external is behind a port.

## Who talks to whom

```
web / mobile ──► gateway :8080 ──► home :8086      /api/v1/homes/**, /api/v1/home-types/**
device :8084 ────────────────────► home :8086      /internal/homes/{id}/access
measurement :8085 ───────────────► home :8086      /internal/homes/{id}/access, /thresholds
security :8082 ──────────────────► home :8086      /internal/accounts/{id}/deletion-check
backend :8083 ───────────────────► home :8086      /internal/homes/{id}, /access, /thresholds
home :8086 ──────────────────────► security :8082  /internal/users/profiles
home ◄──── AMQP ────► rabbitmq :5672               home.deleted out, account.deleted in
```

The gateway sends `/api/v1/homes/*/devices/**` to the device service and
`/api/v1/homes/*/consumption/**` to the measurement service; the rest of `/api/v1/homes/**` comes
here.

## Tokens and callers

- **Users**: the security service signs the access tokens (RS256); this service validates them
  with its public key and issuer (`https://energy-monitor-backend`) and reads the user from `sub`.
- **Services**: `/internal/homes/**` and `/internal/accounts/**` require an allowed network **and**
  `X-Internal-Token`, compared in constant time; no token configured refuses everything. A user's
  access token does not open them.
- **Outbound**: the names of the members come from the security service with the same token. If
  it does not answer within the timeouts, the members are listed without names.
- **Behind the gateway**: the caller address is taken from `X-Forwarded-For` only from loopback
  and the Docker network, so a relayed outside caller cannot reach the internal routes.

## Internal API

| Path | Caller | Answer |
|---|---|---|
| `GET /internal/homes/{id}/access?userId=` | device, measurement, backend | Always 200: `{exists, member, owner}` |
| `GET /internal/homes/{id}/thresholds` | measurement, backend | 200 `{dailyLimit, monthlyLimit, limitPeriod}`, or 404 |
| `GET /internal/homes/{id}` | backend (notifications) | 200 `{homeId, name, homeTypeId, address, creationDate, memberIds}`, or 404 |
| `GET /internal/accounts/{id}/deletion-check` | security | 204 lets the account go, 409 with the message when it is the last owner of a home |

## Messaging

| Direction | Exchange | Routing key | Payload |
|---|---|---|---|
| out | `energy-monitor.home` | `home.deleted` | `homeId, deletedBy, deletedAt` |
| in | `energy-monitor.security` | `account.deleted` | `userId, deletedAt` |

- **Outbox** (`home_outbox`, `home-007`): written in the transaction that deletes the home; a
  relay publishes every second, oldest first, with correlated confirms and mandatory routing, and
  stops at the first failure so order holds. Delivery is at least once with a message id; the
  device service consumes it on `device-service.device.home-deleted` and unlinks the home's
  modules.
- **Consumer**: `home-service.home.account-deleted`, durable, with a dead-letter queue on
  `home-service.dlx`. It removes the account's memberships and the homes it was the only owner
  of; a home with other members passes to the oldest one. A second delivery changes nothing.
- **While energy-monitor-backend still serves the homes**, both stay off
  (`HOME_OUTBOX_RELAY_ENABLED`, `ACCOUNT_DELETED_CONSUMER_ENABLED`): the backend relays the shared
  `home_outbox` table, rows written here included, and consumes `account.deleted` from a queue of
  its own. The consumer's queue is not even declared until then, so it does not pile up messages
  the backend already handled.

## Database

- The same MySQL schema as the other services (ADR-003 of the platform); this service owns
  `home_type`, `home`, `home_thresholds`, `user_home` and `home_outbox`.
- `home-001` to `home-007` keep the path they had in the monolith
  (`db/changelog/home/changes/...`), so Liquibase recognises them as applied. On a new database
  (CI) they create the tables and seed the six home types.
- `user_id` references an account by identifier only: no foreign key across services.

## Decisions

The platform decisions (shared schema, event transport, gateway) are the ADRs of `en-monitor-docs`
(`05-architecture/decisions`). The ones taken for this extraction:

- **Full migration with the same contracts**: public and internal paths and bodies stay as they
  were in the backend, so the web, the mobile app and the other services only change the address
  they call.
- **One owner at a time**: the relay and the consumer are switched on together with the gateway
  route, when the backend's module is removed; two relays on the shared outbox would publish every
  message twice and break its order.
- **A home summary for the backend**: `GET /internal/homes/{id}` replaces `findHome` and
  `memberIds` of the in-process `HomeApi`, so the backend keeps that interface behind a client.
- **Names from the security service over HTTP**, not from its tables, degrading to no names.

## Migration status

| Phase | Content | Status |
|---|---|---|
| 1 | Project structure | Done |
| 2 | Domain, use cases, persistence and changesets, endpoints with JWT validation, security service client | Done |
| 4 | Internal API for the other services | Done |
| 5 | RabbitMQ, transactional outbox (`home.deleted`) and `account.deleted` consumer, off by default | Done |
| 6 | Forwarded headers behind the gateway | Done |
| 7 | Image, CI/CD and this document | Done |
| 8 | energy-monitor-backend reads homes through a client of this service | Pending |
| 9 | Gateway routes `/api/v1/homes/**` and `/api/v1/home-types/**` here | Pending |
| 10 | Security, device and measurement services call this service | Pending |
| 11 | infra: `home` container, relay and consumer on | Pending |
| 12 | `home` module removed from energy-monitor-backend | Pending |

Phase 3 of the plan, the security service client, was done inside phase 2.

## Working principles

1. Preserve the Hexagonal / Ports and Adapters structure.
2. Keep the domain free of Spring, JPA and messaging dependencies.
3. No business logic in controllers, consumers or JPA entities.
4. Every external dependency behind an output port.
5. Never import another service's entities or repositories; refer to them by identifier.
