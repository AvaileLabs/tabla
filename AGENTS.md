# Project instructions

These instructions apply to coding agents working on Tabla, including Codex and
Junie. Read `README.md` for current behavior and configuration, and inspect the
relevant implementation before proposing changes.

## IntelliJ tools and skills

- Prefer available IntelliJ semantic tools for code questions: symbol search and
  symbol information for declarations/types, and call hierarchy for callers and
  callees. Use IDE rename refactoring for symbol renames instead of text replacement.
- Use IntelliJ inspections on changed Kotlin files to catch IDE errors and warnings.
  Use PSI-tree tools when syntax structure is relevant; do not generate trees for
  routine edits that do not need them.
- When runtime values, branches, call order, thread behavior, or a long call chain
  need investigation, discover and read the available `ij-debugger` skill, then
  follow it. Also use it when the user explicitly requests debugging. Prefer
  non-suspending logpoints where appropriate, and distinguish observed evidence
  from hypotheses.
- Discover skills and tool contracts from the active agent's environment. Do not
  hardcode machine-specific JetBrains cache paths or assume Codex and Junie expose
  identical skills, tool names, or invocation mechanisms.
- If a relevant skill or IDE tool is unavailable, state that briefly and use the
  best available alternative: source inspection, targeted searches, and tests.
  Do not claim IDE or debugger validation that was not actually performed.
- Shell tools remain appropriate for Gradle, Git, file discovery, and plain-text
  searches. Use IDE tooling where its semantic understanding helps, rather than
  requiring IDE tools for every operation.

## Kotlin and Spring conventions

Write synchronous Kotlin. Virtual threads are enabled application-wide through Spring Boot configuration.
