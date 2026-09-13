# hospital-management-system

Hospital Management System — Spring Boot 3.5 / Java 21, Thymeleaf, Spring Security, Spring Data JPA, Flyway, PostgreSQL.

## Run locally

Requires Java 21 and a PostgreSQL database (the `local` profile expects `jdbc:postgresql://localhost:5433/hospital`,
user `postgres`; override with `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`).

```bash
SPRING_PROFILES_ACTIVE=local ./mvnw spring-boot:run
```

Flyway creates the schema (`src/main/resources/db/migration`) on first start. The `local` profile also activates
the `demo` profile, which seeds sample data when the database has no users:

| Username   | Password     | Role    |
|------------|--------------|---------|
| `admin`    | `admin123`   | ADMIN   |
| `dr.house` | `doctor123`  | DOCTOR  |
| `patient`  | `patient123` | PATIENT |

Set `DEMO_RESET_TOKEN` to enable `POST /internal/demo/reset` (header `X-Reset-Token`), which wipes and re-seeds the
demo data. Health: `GET /actuator/health`.

## Configuration

All settings live in `src/main/resources/application.yml` and are driven by environment variables
(`PORT`, `SPRING_DATASOURCE_*`, `COOKIE_SECURE`, `DEMO_RESET_TOKEN`). For Docker Compose copy `.env.example` to `.env`;
for Kubernetes create the `hms-db-credentials` Secret (see `k8s/secret.example.yaml`).
