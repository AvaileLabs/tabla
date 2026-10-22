# Project instructions

These instructions apply to coding agents working on Tabla, including Codex and
Junie. Read `README.md` for current behavior and configuration, and inspect the
relevant implementation before proposing changes.

## General

- Discover and use relevant IntelliJ IDEA agent skills and tools when available.
- Write synchronous Kotlin. Virtual threads are enabled application-wide through Spring Boot configuration.
- Prefer plain Kotlin over Spring-specific code unless Spring provides a clear benefit.
- Utilize `kotlinx.collections.immutable` for application-owned collections; confine mutation to implementation details.

## Database migrations

- Only create Flyway migrations using `./scripts/new-migration <description>`,
  with lowercase snake_case descriptions.
- Write PostgreSQL-native SQL with lowercase keywords and identifiers.
- Format SQL using IntelliJ IDEA's formatter with the PostgreSQL dialect.
- Once a Flyway migration is merged into the master branch, it may not be modified or deleted.

## Agent-specific workflow

- Use Git commands for tracked file operations, including `git mv` for moves or renames and `git rm` for deletions.
- Do not modify README.md unless explicitly requested.
- Use KDoc only for API documentation, not for implementation details.
- Inspect the database's live schema through IntelliJ IDEA's database tools using Spring Debugger's
  `TablaApplication` connection; do not infer database state from Flyway migration history.
