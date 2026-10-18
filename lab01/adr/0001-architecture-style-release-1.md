# ADR-0001: Adopt Modular Monolith Batch Architecture for Release 1

Status: Accepted  
Date: 2026-10-18 · Deciders: Mengly Eang, Engineering Team

## Context
The Angkor Mart sales report system requires ingesting approximately 3,000,000 monthly transaction rows across three retail branches and generating consolidated financial reports by the morning of the second day of each month. 
Key architectural forces include:
- **QA-1 (Performance):** Ingestion and aggregation must complete in $\le 60\text{ s}$ wall-clock time on an 8-core server.
- **QA-4 (Modifiability):** New report formats (PDF, Excel, Web) must be added in $\le 2$ person-days without altering core calculations.
- **QA-6 (Testability):** Core calculation suites must execute in-memory in $\le 30\text{ s}$ without external database dependencies.
- **Project Constraints:** Two-developer team, tight academic delivery iterations, and no dedicated DevOps infrastructure.

## Decision
We will build Release 1 as an in-process **Modular Monolith Batch** application within a single Maven artifact. Core sub-domains are structured into decoupled Java packages (`model`, `ingest`, `render`, `engine`) with architectural boundaries enforced by automated fitness tests (`DependencyRulesTest`).

## Alternatives considered
- **Microservices / Distributed Streaming Pipeline (e.g., Kafka + Spring Boot workers):**  
  Attractive for elastic, independent container scaling across nodes. Rejected because distributed messaging introduces substantial serialization overhead, network latency, operational complexity, and eventual consistency challenges that are unnecessary for a 3-million-row monthly batch on a single multi-core server.
- **Multi-Module Maven Build with OSGi / Plugin Architecture:**  
  Attractive for enforcing strict physical JAR boundaries between rendering and core engine. Rejected for Release 1 because managing multiple POMs and dynamic plugin loaders adds premature configuration overhead for a two-person team; package-private visibility and architectural tests provide equivalent safety.

## Consequences
- `+` **QA-1:** Zero network serialization and memory-local streaming allow processing 3,000,000 records well under the 60-second limit.
- `+` **QA-6:** Unit tests run entirely in-memory within milliseconds with no mocks, Docker containers, or live databases needed.
- `+` **QA-4:** Strategy and Observer design patterns cleanly isolate rendering formats and loading notifications.
- `−` **QA-2:** The application is bound to vertical hardware scaling; running across multiple machines requires future refactoring.
- `−` **Coupling Risk:** In a single compilation unit, developers could inadvertently introduce illegal package imports if architectural fitness tests are disabled.

Revisit when: Total monthly volume across all branches exceeds 10,000,000 rows or business requirements mandate real-time intra-day streaming reports rather than monthly batch generation.
