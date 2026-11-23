
# Enterprise Multi-Tenant SaaS Platform

A backend engineering project focused on building a secure, role-based, multi-tenant SaaS platform using Java and Spring Boot.

The platform provides company management, tenant-level user and role management, permission-based authorization, task management, JWT authentication, and Redis-backed infrastructure.

> **Project status:** Active Development  
> This is an evolving engineering project. Some production-oriented features and security improvements are still planned.

## Technology Stack

| Technology | Purpose |
|---|---|
| Java 25 | Backend development |
| Spring Boot 4.1.2 | Application framework |
| Spring Security | Authentication and authorization |
| JWT (JJWT) | Stateless authentication |
| Spring Data JPA | Persistence layer |
| MySQL | Relational database |
| Redis | Token revocation and rate limiting |
| Maven | Dependency and build management |
| Docker Compose | Local Redis infrastructure |

## Implemented Features

### 1. Authentication and Security
- JWT-based login and authentication.
- BCrypt password hashing.
- Stateless Spring Security configuration.
- Account and company activation-state validation.
- Forced password change for newly provisioned accounts.
- Redis-backed token revocation and session blocklisting.

### 2. Multi-Tenant Management
- Company provisioning and management.
- Company-scoped users, roles and tasks.
- Tenant-aware repository queries.
- Company-level activation and deactivation.
- Shared-schema tenant isolation using `company_id`.

### 3. Role-Based Access Control (RBAC)
- Dynamic role creation and management.
- Permission-based authorization.
- Role-permission assignment.
- User-role assignment.
- Privilege escalation checks during role assignment.
- Self-demotion and self-lockout safeguards.

### 4. Task Management
- Task creation, retrieval, updating and deletion.
- Task assignment to tenant users.
- Task status and priority management.
- Pagination and status-based filtering.

### 5. Redis Infrastructure
- Distributed token revocation.
- Tenant-level API rate limiting.
- Redis Lua-based rate-limit operations.

## Current Architecture

The application follows a layered architecture:

- **Controller layer:** REST API endpoints.
- **Service layer:** Business logic and authorization-related operations.
- **Repository layer:** Database access using Spring Data JPA.
- **Domain layer:** Entities and application enums.
- **Security layer:** JWT filters, user details, password-change enforcement and rate limiting.
- **Configuration layer:** Security beans and platform bootstrap configuration.

## Current Multi-Tenancy Model

The current implementation uses a **shared database and shared schema** model.

Tenant-owned records are associated with a company through `company_id`. Repository queries use company identifiers to scope tenant operations.

Schema-per-tenant isolation is a planned architectural upgrade and is not yet implemented.

## API Overview

| API | Purpose |
|---|---|
| `/api/auth` | Authentication and password operations |
| `/api/platform/companies` | Platform company management |
| `/api/tenant/users` | Tenant user management |
| `/api/tenant/roles` | Tenant role and permission management |
| `/api/tenant/tasks` | Tenant task management |

See the controller classes under `web/controller` for the implemented HTTP methods and request/response structures.

## Project Structure

```text
src/main/java/com/ateeb/enterprise_platform/
│
├── config/
│
├── domain/
│   ├── entity/
│   └── enums/
│
├── repository/
│
├── security/
│
├── service/
│
└── web/
    ├── controller/
    ├── dto/
    │   ├── request/
    │   └── response/
    └── GlobalExceptionHandler.java

src/main/resources/
    └── application.properties

src/test/
    └── java/

docker-compose.yml
pom.xml
```

## Local Development Setup

### Prerequisites

- JDK 25
- MySQL
- Docker Desktop
- Redis (provided through Docker Compose)
- IntelliJ IDEA or another Java IDE

### 1. Clone the repository

```bash
git clone https://github.com/YOUR_USERNAME/enterprise-platform.git
cd enterprise-platform
```

### 2. Create the MySQL database

Create a database named:

```sql
CREATE DATABASE enterprise_platform;
```

Configure the MySQL connection credentials in your IntelliJ Run Configuration.

### 3. Start Redis

```bash
docker compose up -d redis
```

The current Compose configuration starts Redis only.

### 4. Configure environment variables

Open:

`Run → Edit Configurations → Environment variables`

Use the variables documented in `.env.example`.

Generate a strong JWT secret and use unique local credentials. Do not commit these values.

### 5. Run the application

Run `EnterprisePlatformApplication.java` from IntelliJ.

The application also includes the Maven wrapper:

```bash
./mvnw spring-boot:run
```

On Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

The MySQL database and Redis service must be available before starting the application.

## Current Development Limitations

This repository is an active development project and is not presented as production-ready.

Known areas requiring further work:

- Platform company-management endpoints need dedicated permission-level authorization.
- Initial account provisioning currently uses a common default password.
- Redis token-revocation handling needs a secure failure strategy when Redis is unavailable.
- Tenant rate limiting contains a hardcoded company-ID exclusion.
- Hibernate currently manages schema updates using `ddl-auto=update`.
- SQL statement logging is enabled.
- Automated integration and security testing is limited.

Do not deploy the current implementation with real customer data.

## Development Roadmap

- [ ] Schema-per-tenant database architecture.
- [ ] Tenant schema provisioning and lifecycle management.
- [ ] Flyway database migrations.
- [ ] Improved platform-level authorization.
- [ ] Secure initial account provisioning.
- [ ] Redis failure-handling strategy.
- [ ] Complete Docker environment (Spring Boot, MySQL and Redis).
- [ ] Integration and tenant-isolation tests.
- [ ] CI/CD pipeline.
- [ ] Cloud deployment and monitoring.

## Engineering Goals

The project is being developed to explore:

- Enterprise backend architecture.
- Multi-tenant SaaS design.
- Authentication and authorization.
- Database isolation strategies.
- Distributed infrastructure.
- Secure API development.
- Production-oriented deployment practices.

---

**Maintained as an ongoing backend engineering project.**
