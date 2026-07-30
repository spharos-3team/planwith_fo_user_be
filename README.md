# PlanWith FO User Backend

Spring Boot MSA Backend for project **planwith** (`fo-user-be`).

| Item | Value |
|------|-------|
| Repository | `planwith_fo_user_be` |
| Package | `com.planwith.user` |
| Spring application name / Eureka ID | `fo-user-be` |
| Discovery service ID | `discovery` |

## Versions

| Component | Version |
|-----------|---------|
| Java | 21 (Gradle toolchain) |
| Spring Boot | 3.3.2 |
| Spring Cloud | 2023.0.3 |
| Build | Gradle Groovy DSL |

## Architecture

Hexagonal layers: `adapter → application → domain`

Domain boundaries (MSA-ready in one deployable): `auth`, `system`, `user`, `plan`, `payment`, `realtime`

## Run (local)

1. Start dependencies: `docker compose -f compose.dependencies.yml up -d`
2. Copy `.env.example` and set secrets (especially `GATEWAY_INTERNAL_TOKEN` and JWT keys for non-local)
3. `./gradlew bootRun` (default profile: `local`)

- Port: `8080`
- API prefix: `/api/v1`
- Timezone: UTC
- Eureka Service ID: `fo-user-be`
- Discovery default zone: `http://localhost:8761/eureka/` (service id `discovery`)

### Profiles

| Profile | Purpose |
|---------|---------|
| `local` | Local with deps; ephemeral JWT keys if PEM paths empty |
| `local-direct` | CORS on, Gateway trust check off (direct client) |
| `test` | H2 + in-memory Redis adapters; Eureka/Mongo/Kafka/Redis auto-config off |
| `prod` | Requires env secrets; Gateway trust on |

## Auth (Backend responsibility)

- Signup / Login / Refresh / Logout / Logout-all (+ email/social helpers)
- Password hashing via `PasswordEncoderFactories.createDelegatingPasswordEncoder()`
- Access Token: **RS256** via `NimbusJwtEncoder`, header `kid`, claims `iss/sub/aud/iat/nbf/exp/jti/roles/scope/session_id`
- Refresh Token: opaque `SecureRandom`, **hash only in Redis**, rotation + family reuse detection, **HttpOnly cookie only**
- JWKS: `GET /oauth2/jwks`

Gateway validates Access Tokens. Backend trusts Gateway via `X-Gateway-Internal-Token` and reads user context headers (`X-Auth-*`).

## Tests / Build

```bash
./gradlew clean test
./gradlew clean build
./gradlew integrationTest   # Testcontainers; requires Docker + RUN_TESTCONTAINERS=true
docker compose -f compose.dependencies.yml config
```

Default `test` / `build` do **not** require Docker.

## Compose

`compose.dependencies.yml` includes **MySQL, MongoDB, Redis, Kafka only** (no discovery/gateway/backend/frontend).
