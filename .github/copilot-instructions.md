
# GitHub Copilot Instructions for Nais Examples

This repository demonstrates cloud native applications running on the NAIS platform with observability, security, and best practices.

## Architecture Overview

**Services:**
- `quotes-frontend` - Next.js frontend with server-side API routes
- `quotes-backend` - Kotlin/Ktor REST API
- `quotes-analytics` - .NET analytics service with OpenTelemetry
- `quotes-loadgen` - Go load generator for testing

**Key Patterns:**
- Services communicate via HTTP without port specifications (`http://service-name`)
- NAIS access policies control inter-service communication
- OpenTelemetry auto-instrumentation for observability
- Environment-specific configuration via NAIS manifests

## Copilot Behavior Guidelines

**Code Quality:**
- Follow language idioms and existing patterns
- Maintain consistency with existing code structure
- Implement proper error handling and logging
- Use environment variables for configuration (never hardcode secrets)
- Do not add code comments unless explicitly asked
- Do not add new documentation files unless explicitly asked

**NAIS Platform Specifics:**
- Service URLs use standard HTTP (no ports): `http://quotes-backend`
- Update `.nais/app.yaml` files for deployment configuration
- Configure `accessPolicy` rules for service-to-service communication
- Use auto-instrumentation instead of manual OpenTelemetry setup in production

**Development Workflow:**
- Run `mise run ci` to verify all changes (linting, tests, builds)
- Update dependabot configuration for new package ecosystems
- Follow existing project structure and naming conventions

**Testing & Verification:**
Always run `mise run ci` after making changes to ensure code quality and compatibility across all services.
