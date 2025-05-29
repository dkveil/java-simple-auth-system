# Demo API – Spring Boot 3 · JWT in HttpOnly Cookies

This repository contains a small reference application that demonstrates:

- User registration & login backed by PostgreSQL
- Stateless authentication with **JWT sent in HttpOnly cookies**
- Access/refresh-token rotation
- Role-based authorization (`ROLE_USER`, `ROLE_ADMIN`)
- OpenAPI 3 documentation with Swagger-UI
- Docker + Docker Compose workflow

---

## Table of Contents

1. Requirements
2. Quick Start (Docker Compose)
3. Running Locally with Maven
4. Environment Variables
5. Important Endpoints
6. Running Tests
7. License

---

## 1. Requirements

Choose **one** of the following:

- Docker ≥ 20.10 and Docker Compose ≥ 2.20
- Java 17 JDK and Maven ≥ 3.9

---

## 2. Quick Start (Docker Compose)

1. Clone the repo:

   ```bash
   git clone https://github.com/dkveil/java-simple-auth-system
   cd java-simple-auth-system
   ```

2. Copy the example environment file **and adjust the values**:

   ```bash
   cp .env.example .env
   ```

3. Build and run everything:

   ```bash
   docker compose up --build
   ```

4. After the containers start, you should see:

   _Backend_: <http://localhost:8080>
   _Swagger-UI_: <http://localhost:8080/swagger-ui.html>

---

## 3. Running Locally with Maven

1. Make sure a PostgreSQL instance is running (e.g. via Docker).
2. Export the same variables that are listed in `.env.example` (or source the file).
3. Start the application:

   ```bash
   ./mvnw spring-boot:run      # macOS / Linux
   mvnw.cmd spring-boot:run    # Windows
   ```

---

## 4. Environment Variables

The full list is in `compose.yml` and the various `application*.yml` files.
Commonly changed values are:

| Variable                        | Default in `.env.example` | Description                 |
| ------------------------------- | ------------------------- | --------------------------- |
| `POSTGRES_DB`                   | postgres                  | Database name               |
| `POSTGRES_USER`                 | root                      | DB user                     |
| `POSTGRES_PASSWORD`             | password                  | DB password                 |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | update                    | Hibernate DDL strategy      |
| `SECURITY_JWT_SECRET`           | (generated)               | Base64-encoded signing key  |
| `SECURITY_JWT_EXPIRATION_MS`    | 3600000                   | Access-token lifetime (ms)  |
| `SECURITY_JWT_REFRESH_EXP_MS`   | 604800000                 | Refresh-token lifetime (ms) |

---

## 5. Important Endpoints

| Method | URI              | Description             |
| ------ | ---------------- | ----------------------- |
| POST   | `/auth/register` | Register new user       |
| POST   | `/auth/login`    | Login & receive cookies |
| POST   | `/auth/refresh`  | Rotate tokens           |
| POST   | `/auth/logout`   | Revoke refresh token    |
| GET    | `/auth/me`       | Current user info       |
| GET    | `/example/me`    | Public example endpoint |

All routes are documented and can be tested from Swagger-UI.

---

## 6. Running Tests

```bash
./mvnw test
```

---

## 7. License

This project is released under the [MIT License](LICENSE).
