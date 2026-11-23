
# Development Roadmap

This roadmap tracks the planned evolution of Enterprise Platform from its current shared-schema backend into a more production-oriented multi-tenant SaaS platform.

## Phase 1 — Core Backend Foundation

**Status: Implemented in source; further validation required**

- [x] Spring Boot application foundation.
- [x] Company management.
- [x] User management.
- [x] Role and permission management.
- [x] Task management.
- [x] JWT authentication.
- [x] BCrypt password hashing.
- [x] Tenant-aware repository operations.
- [x] Redis token revocation.
- [x] Redis-based tenant rate limiting.

## Phase 2 — Security and Reliability

**Status: In progress**

- [ ] Platform-level permission enforcement.
- [ ] Secure initial account provisioning.
- [ ] Redis failure-handling improvements.
- [ ] Remove hardcoded tenant exclusions.
- [ ] Improve validation and error handling.
- [ ] Cross-tenant access tests.
- [ ] RBAC and privilege escalation tests.
- [ ] JWT revocation tests.

## Phase 3 — Schema-per-Tenant Architecture

**Status: Planned**

- [ ] Design tenant schema isolation.
- [ ] Implement tenant discovery.
- [ ] Implement tenant-specific database schema routing.
- [ ] Automate tenant schema provisioning.
- [ ] Manage tenant schema lifecycle.
- [ ] Validate tenant isolation boundaries.

## Phase 4 — Database Migration Management

**Status: Planned**

- [ ] Introduce Flyway.
- [ ] Create initial database migrations.
- [ ] Implement tenant schema migrations.
- [ ] Replace automatic Hibernate schema updates with validation.
- [ ] Test migration compatibility.

## Phase 5 — Containerization and Infrastructure

**Status: Planned**

Current Docker Compose configuration provides Redis infrastructure only.

Planned improvements:

- [ ] Dockerfile for Spring Boot application.
- [ ] MySQL container configuration.
- [ ] Complete Docker Compose environment.
- [ ] Environment-based configuration.
- [ ] Persistent database and Redis volumes.
- [ ] Health checks and service dependencies.

## Phase 6 — Testing and Deployment

**Status: Planned**

- [ ] Unit testing.
- [ ] Integration testing.
- [ ] Multi-tenant isolation testing.
- [ ] Authentication and authorization testing.
- [ ] CI/CD pipeline.
- [ ] Cloud deployment.
- [ ] Logging and monitoring.
- [ ] Production configuration review.

---

The roadmap is iterative. Features may be refined as the architecture and implementation evolve.
