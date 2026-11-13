# Project instructions

These instructions apply to coding agents working on Tabla, including Codex and
Junie. Read `README.md` for current behavior and configuration, and inspect the
relevant implementation before proposing changes.

## General

- Do not introduce backward compatibility unless explicitly requested.
- Write synchronous Kotlin. Virtual threads are enabled application-wide through Spring Boot configuration.
- Prefer plain Kotlin over Spring-specific code unless Spring provides a clear benefit.
- Prefer value classes over data classes for value types, including multi-property value classes enabled by
  `-Xfull-value-classes`. Use data classes only when value classes are unsuitable.
- Utilize `kotlinx.collections.immutable` for application-owned collections; confine mutation to implementation details.
- Prefer declarative, expression-oriented Kotlin over imperative loops and mutable accumulators when it improves code
  readability.
- Match version catalog library aliases exactly to artifact names. Use kebab-case for all catalog aliases.

## Database migrations

- Only create Flyway migrations using `./scripts/new-migration <description>`,
  with lowercase snake_case descriptions.
- Write PostgreSQL-native SQL with lowercase keywords and identifiers.
- Format SQL using IntelliJ IDEA's formatter with the PostgreSQL dialect.
- Once a Flyway migration is merged into the master branch, it may not be modified or deleted.

## MCP server

- Target `MODERN` only. Do not support legacy MCP configurations or fallback protocols.

## Agent-specific workflow

- Discover and use relevant IntelliJ IDEA agent skills and tools when available. If IntelliJ IDEA's MCP server is
  unavailable, ask the user to enable it.
- Use the Gradle wrapper with IntelliJ IDEA's `GRADLE_USER_HOME` (default `~/.gradle`). Reuse JDKs under
  `GRADLE_USER_HOME/jdks` and respect `gradle/gradle-daemon-jvm.properties`.
- Rely on compiler guarantees wherever possible. Write tests sparingly, only when they provide substantial verification
  beyond what the compiler can establish.
- Use Git commands for tracked file operations, including `git mv` for moves or renames and `git rm` for deletions.
- Do not modify README.md unless explicitly requested.
- Use KDoc only for API documentation, not for implementation details.
- When inspecting the live database schema, use IntelliJ IDEA's database tools through Spring Debugger's
  `TablaApplication` connection; do not infer database state from Flyway migration history. If the `[DEBUG]` JDBC
  connection is absent, use IntelliJ IDEA's MCP tools to reuse or start the saved `TablaApplication` debug configuration
  and recheck connections.