
# GitHub Copilot Instructions for This Repository

Welcome to the Nais Examples repository! This document provides guidance for using GitHub Copilot effectively in this codebase. Please follow the instructions below to ensure consistency, quality, and best practices across all services and technologies in this repository.

---

## General Instructions

- **Code Quality:** Always generate clean, readable, and idiomatic code. Follow the conventions of the language and framework in use.
- **Documentation:** Add or update comments and documentation for new code, especially for public APIs and complex logic.
- **Testing:** When adding new features or fixing bugs, generate or update relevant tests.
- **Security:** Avoid hardcoding secrets or credentials. Use environment variables or secret management solutions.
- **Error Handling:** Implement proper error handling and logging. Avoid silent failures.
- **Consistency:** Follow the existing project structure and naming conventions.
- **Dependencies:** Use only necessary dependencies. Prefer official or well-maintained libraries.
- **Pull Requests:** Ensure all code passes linting, formatting, and tests before submitting a PR.

---


## Language & Framework Specific Instructions

### 1. Next.js (Frontend)
- Use functional React components and hooks.
- Prefer TypeScript for new files/components.
- Use Tailwind CSS for UI.
- Organize components in the `components/` directory and pages in the `pages/` or `app/` directory.
- Use Next.js API routes for backend logic only when necessary.
- Optimize for accessibility and performance.
- Use environment variables for configuration (never commit secrets).

### 2. Kotlin + Ktor (Backend)
- Use idiomatic Kotlin (prefer `val` over `var`, use data classes, etc.).
- Organize code by feature (routes, services, repositories, models, etc.).
- Use Ktor features for routing, dependency injection, and configuration.
- Use JPA/Hibernate or Exposed for database access; prefer constructor injection.
- Write integration and unit tests for routes and services.
- Use configuration files (`application.conf` or `application.yaml`) for environment-specific settings.

### 3. Go (Load Generator)
- Use idiomatic Go (short variable names, error handling, etc.).
- Organize code by package (cmd, internal, etc.).
- Write clear, minimal code for load generation and HTTP requests.
- Add comments and documentation for exported functions.
- Write unit tests for core logic if applicable.

### 4. PostgreSQL (Database)
- Use migrations (e.g., Flyway, Liquibase, or manual SQL) for schema changes if needed.
- Store connection details in environment variables or secrets.
- Do not commit database dumps or sensitive data.

### 5. Docker & Docker Compose
- Write clear, minimal Dockerfiles for each service.
- Use multi-stage builds for production images when possible.
- Keep `docker-compose.yaml` up to date with all services and dependencies.
- Expose only necessary ports and use environment variables for configuration.

---

## Additional Notes
- For more details on each service, see the README in the respective subdirectory.
- If you are unsure about conventions or best practices, refer to the official documentation for the language or framework in use.
- When in doubt, prefer clarity and maintainability over cleverness.
