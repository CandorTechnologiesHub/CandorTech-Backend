# CanDor Backend

Backend service for **CanDor**, a fintech platform being designed to support secure user onboarding, authentication, wallet-driven financial services, and future integrations for payments and related transaction workflows.

This repository currently represents the **backend foundation** of the platform. It focuses on the core application setup, authentication, database integration, configuration management, and supporting infrastructure required for later product expansion.

---

## Table of Contents
- [Overview](#overview)
- [Project Status](#project-status)
- [Product Direction](#product-direction)
- [Current Backend Capabilities](#current-backend-capabilities)
- [Technology Stack](#technology-stack)
- [Architecture Overview](#architecture-overview)
- [Getting Started](#getting-started)
- [Prerequisites](#prerequisites)
- [Configuration](#configuration)
- [Running the Application](#running-the-application)
- [API Summary](#api-summary)
- [Authentication](#authentication)
- [Email Support](#email-support)
- [Testing](#testing)
- [CI](#ci)
- [Security](#security)
- [Roadmap](#roadmap)
- [Contributing](#contributing)
- [License](#license)

---

## Overview

**CanDor** is being built as a secure, scalable financial services platform. The wider product vision includes wallet-enabled experiences, transaction services, and additional financial capabilities as the platform matures.

This codebase currently delivers the backend groundwork needed to support that direction, with emphasis on secure authentication, persistence, service abstraction, and environment-aware deployment practices.

---

## Project Status

**Stage:** Early MVP backend foundation / actively evolving

The current repository is centered on core platform capabilities such as:
- user authentication,
- token-based access control,
- persistent data storage,
- environment-driven configuration,
- transactional email infrastructure,
- CI-based build automation.

---

## Product Direction

CanDor is being designed to grow into a dependable financial platform with support for secure account access, wallet functionality, transaction processing, utility-related services, and additional ecosystem integrations.

The product direction include:
- wallet operations,
- transaction history,
- payments and service purchases,
- transfer workflows,
- fee transparency,
- analytics and reporting,
- compliance-aware operational tooling.

---

## Current Backend Capabilities

The repository currently includes foundational backend capabilities such as:

### Authentication and access control
- user registration and sign-in flows,
- token-based authentication,
- protected user access patterns,
- role-aware authorization support.

### Persistence and application foundation
- relational database integration,
- application-layer service and repository abstraction,
- centralized exception handling,
- structured backend response patterns.

### Security foundation
- password hashing,
- stateless authentication approach,
- environment-based configuration,
- configurable cross-origin controls.

### Email infrastructure
- pluggable email provider support,
- asynchronous email processing,
- retry-capable email workflows,
- template-based transactional email rendering.

### Delivery and maintainability
- automated build workflow,
- profile-aware runtime configuration,
- API documentation support scaffold,
- maintainable layered backend architecture.

---

## Technology Stack

### Core backend
- **Java 17**
- **Spring Boot**
- **Spring Web**
- **Spring Security**
- **Spring Data JPA**
- **Spring Validation**

### Data and persistence
- **PostgreSQL**
- **Hibernate / JPA**

### Security and authentication
- **JWT**
- **BCrypt**

### Email and templating
- **JavaMail / SMTP support**
- **SendGrid support**
- **Thymeleaf**
- **Spring Retry**

### Tooling
- **Maven**
- **OpenAPI / Swagger support**
- **GitHub Actions**
- **Logback**
- **Lombok**

---

## Architecture Overview

The backend follows a layered service architecture built for maintainability, extensibility, and secure request handling.

At a high level, the application includes:
- HTTP endpoint handling,
- business service orchestration,
- data persistence abstraction,
- security and configuration layers,
- consistent exception and response handling.

---

## Getting Started

### Quick start

1. Clone the repository.
2. Copy `.env.example` to `.env`
3. Update environment variables
4. Provision a PostgreSQL database.
5. Run the application with the project build tooling.
6. Test the authentication flow and any enabled local integrations.

---

## Environment Configuration

The application imports configuration from a local `.env` file.

Create your local environment file:

```bash
cp .env.example .env
```
---

## Prerequisites

Before running the project locally, make sure you have:
- **Java 17** installed,
- **PostgreSQL** available,
- **Maven** installed or use the included Maven Wrapper,
- valid provider credentials if you plan to test external email delivery.

Recommended:
- IntelliJ IDEA or VS Code,
- Postman or Insomnia or Swagger for API testing.

---

## Configuration

The application uses **environment-based configuration** and is designed so that secrets and deployment-specific values remain outside committed source code.

### Public guidance
- Do not commit real secrets, credentials, tokens, or provider keys.
- Keep local configuration files untracked.
- Use separate values for development, staging, and production.
- Rotate secrets regularly and store production secrets in a managed secret store where possible.

### Typical configuration categories
The application require values in categories such as:
- database connectivity,
- token signing / authentication,
- allowed client origins,
- email provider settings,
- deployment profile selection.

Check out the .env.example.

---

## Running the Application

### Using Maven Wrapper

On macOS/Linux:

```bash
./mvnw spring-boot:run
```

On Windows:

```bat
mvnw.cmd spring-boot:run
```

### Build the project

```bash
./mvnw clean package
```

### Run the packaged artifact

```bash
java -jar target/*.jar
```

Runtime defaults may differ by environment and active profile.

---

## API Summary

This backend currently exposes authentication-related capabilities and supporting protected access patterns.

Public-facing API documentation should describe:
- what the endpoint does,
- authentication requirements,
- request and response expectations,
- validation and error behavior,
- role requirements where applicable.

This README intentionally avoids publishing a full endpoint inventory, response contracts, and internal implementation details.

---

## Authentication

The backend uses a token-based authentication model for protected resources.

At a high level:
1. A user registers or signs in.
2. The backend validates credentials and applicable rules.
3. A signed token is issued on successful authentication.
4. Protected routes require a valid bearer token.
5. Authorization rules are enforced before protected resources are served.

---

## Email Support

The codebase includes an email delivery abstraction intended for transactional workflows.

Supported patterns include:
- provider-based email delivery,
- asynchronous sending,
- retry-aware execution,
- HTML template rendering.

Provider credentials and sender configuration should always be injected securely through environment management and never hardcoded.

---

## Testing

Run tests with:

```bash
./mvnw test
```

Run a full verification build with:

```bash
./mvnw clean verify
```

As the platform expands, the test suite should continue growing across controller, service, persistence, security, and integration layers.

---

## CI

The repository includes automated build support through GitHub Actions.

Current and future CI goals include:
- repeatable builds,
- dependency caching,
- automated test execution,
- static analysis,
- quality and security checks.

---

## Security

Security is a core priority for this backend.

### Current security foundations
- hashed password storage,
- token-based authentication,
- separation of configuration from source code,
- role-aware access patterns,
- environment-specific configuration support.

### Public repository security practices
To reduce exposure in a public codebase:
- never publish real credentials,
- avoid documenting internal secret names and sensitive defaults,
- avoid exposing infrastructure assumptions unnecessarily,
- avoid publishing operationally useful attack details,
- move sensitive runbooks and deployment notes to private internal documentation.

### Recommended production hardening
Before production-scale release, typical areas to strengthen include:
- rate limiting,
- refresh-token strategy,
- audit logging,
- secret management,
- database migrations,
- observability and alerting,
- compliance and fraud controls,
- stricter administrative boundaries,
- verification and recovery workflows.

---

## Roadmap

As the CanDor platform evolves, the backend is expected to grow beyond the current foundation.

Future modules include:
- wallet operations,
- transaction history,
- service purchase flows,
- transfer processing,
- notifications,
- administrative tooling,
- reporting and analytics,
- compliance-oriented controls.

These items reflect platform direction and should be updated as implementation progresses.

---

## Contributing

To keep the codebase maintainable as it grows:

1. Create focused feature branches.
2. Keep changes scoped and well-reviewed.
3. Externalize all sensitive configuration.
4. Add or update tests alongside feature work.
5. Document behavior changes appropriately.
6. Avoid introducing secrets, credentials, or environment assumptions into version control.

### Suggested engineering conventions
- Keep controllers thin and services focused.
- Prefer DTO-based public API contracts.
- Validate input consistently.
- Handle exceptions centrally.
- Keep security-sensitive changes especially well reviewed.

---

## License

No license file is currently included in this repository.

Until a license is added, rights should be treated in accordance with project ownership and repository policy.

---

## Final Notes

This public README is intentionally written to balance **professional project presentation** with **security-conscious disclosure**.

It gives contributors and reviewers enough context to understand the repository’s purpose and maturity without exposing sensitive operational details that are better kept private.

**For further questions reach out to Admin.**
