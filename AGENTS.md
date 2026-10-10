# Project instructions

These instructions apply to coding agents working on Tabla, including Codex and
Junie. Read `README.md` for current behavior and configuration, and inspect the
relevant implementation before proposing changes.

## General

- Discover and use relevant IntelliJ IDEA agent skills and tools when available.
- Write synchronous Kotlin. Virtual threads are enabled application-wide through Spring Boot configuration.

## Database migrations

- Create Flyway migrations using `./scripts/new-migration <description>`,
  with lowercase snake_case descriptions.
- Use PostgreSQL-native SQL and test migrations against PostgreSQL using Testcontainers.