---
skill: api-endpoint
role: Developer
triggers:
  - new REST endpoint
  - new API resource
  - API implementation
tags:
  - skill
  - api
  - endpoint
---

# Skill: api-endpoint

> Procedural guide for implementing a new REST API endpoint.
> Ensures every endpoint meets coding, security, and regulatory standards.

## Role
Developer

## Required Domains
- architecture → [[CodingGuidelines]]
- security → [[SecurityGuidelines]]
- regulatory → [[Regulatory]]

## Preconditions
- OpenAPI spec exists for the endpoint
- Architecture Board has approved the resource design

## Steps

### 1. Validate Regulatory Constraints
- Check if the endpoint handles financial transactions → AML screening required
- Check if PII is involved → GDPR data retention rules apply
- Check if SCA thresholds are triggered → PSD2 authentication required
- Document applicable regulatory constraints in the PR description

> [!warning] Embedded: Regulatory constraints for transactions
> ![[Regulatory#1. Transaction Limits & Verification (PSD2 / ZAG)]]

### 2. Define OpenAPI Contract
- Path follows `/api/v{version}/{resource}` convention
- Request/response modeled as Java Records
- Error responses use standard `ErrorResponse` schema
- Contract reviewed before implementation begins

### 3. Apply Security Controls
- Endpoint secured with OAuth 2.0 / OIDC
- Authorization via `@PreAuthorize` at service layer
- Input validation with Bean Validation annotations
- No secrets in code — Vault references only

> [!warning] Embedded: Input validation rules
> ![[SecurityGuidelines#2. Input Validation & Injection Prevention]]

### 4. Implement with Coding Standards
- Controller delegates to service layer (no business logic in controller)
- Naming conventions enforced (see [[CodingGuidelines#2. Naming Conventions]])
- Structured JSON logging with correlationId
- Error handling via domain-specific exceptions + @ControllerAdvice

### 5. Create Test Scaffold
- Unit tests for service layer (JUnit 5 + Mockito)
- Integration tests for controller (MockMvc)
- Test naming: `should_expectedBehavior_when_condition()`
- Minimum 80% line coverage on changed files

> [!info] Full testing requirements
> See [[TestingStrategy#3. Quality Gates in CI Pipeline]] for all quality gates.

### 6. Update API Catalog
- Register endpoint in API catalog
- Update Swagger UI configuration
- Notify API consumers if breaking changes exist
- Submit PR using [[PullRequestTemplate]]
