# Project instructions

These instructions apply to coding agents working on Tabla, including Codex and
Junie. Read `README.md` for current behavior and configuration, and inspect the
relevant implementation before proposing changes.

## General

- Write synchronous Kotlin. Virtual threads are enabled application-wide through Spring Boot configuration.
- Prefer plain Kotlin over Spring-specific code unless Spring provides a clear benefit.
- Utilize `kotlinx.collections.immutable` for application-owned collections; confine mutation to implementation details.
- Prefer declarative, expression-oriented Kotlin over imperative loops and mutable accumulators when it improves code
  readability.

## Database migrations

- Only create Flyway migrations using `./scripts/new-migration <description>`,
  with lowercase snake_case descriptions.
- Write PostgreSQL-native SQL with lowercase keywords and identifiers.
- Format SQL using IntelliJ IDEA's formatter with the PostgreSQL dialect.
- Once a Flyway migration is merged into the master branch, it may not be modified or deleted.

## Agent-specific workflow

- Discover and use relevant IntelliJ IDEA agent skills and tools when available. If IntelliJ IDEA's MCP server is
  unavailable, ask the user to enable it.
- Reuse the JDK provisioned for this project's Gradle toolchain under `GRADLE_USER_HOME/jdks`
  (default `~/.gradle/jdks`). Set `JAVA_HOME` for individual commands when needed.
- Use Git commands for tracked file operations, including `git mv` for moves or renames and `git rm` for deletions.
- Do not modify README.md unless explicitly requested.
- Use KDoc only for API documentation, not for implementation details.
- Inspect the database's live schema through IntelliJ IDEA's database tools using Spring Debugger's
  `TablaApplication` connection; do not infer database state from Flyway migration history.
