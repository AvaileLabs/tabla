# Project instructions

These instructions apply to coding agents working on Tabla, including Codex and
Junie. Read `README.md` for current behavior and configuration, and inspect the
relevant implementation before proposing changes.

## General

- Discover and use relevant IntelliJ IDEA agent skills and tools when available.
- Write synchronous Kotlin. Virtual threads are enabled application-wide through Spring Boot configuration.
- Do not add or update documentation unless explicitly requested.
- Use Git commands for tracked file operations, including `git mv` for moves or renames and `git rm` for deletions.
- Prefer plain Kotlin over Spring-specific code unless Spring provides a clear benefit.

## Database migrations

- Only create Flyway migrations using `./scripts/new-migration <description>`,
  with lowercase snake_case descriptions.
- Write PostgreSQL-native SQL with lowercase keywords and identifiers.
- Format SQL using IntelliJ IDEA's formatter with the PostgreSQL dialect.
- Once a Flyway migration is merged into the master branch, it may not be modified or deleted.
