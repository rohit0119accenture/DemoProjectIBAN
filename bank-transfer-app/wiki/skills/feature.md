---
skill: feature
role: Developer
triggers:
  - new feature
  - user story
  - feature implementation
  - new functionality
tags:
  - skill
  - feature
  - implementation
---

# Skill: feature

> End-to-end workflow for implementing a new feature.
> Ensures every feature meets architectural, security, regulatory, and testing standards before merge.

## Role
Developer

## Required Domains
- architecture → [[CodingGuidelines]]
- security → [[SecurityGuidelines]]
- regulatory → [[Regulatory]]
- testing → [[TestingStrategy]]

## Preconditions
- User story or feature ticket exists with acceptance criteria
- Architecture Board has approved the design (if new service/resource)
- Branch created following [[PullRequestTemplate#4. Branch Convention]]

## Steps

### 1. Assess Regulatory Impact
- Does the feature touch financial transactions? → [[Regulatory#1. Transaction Limits & Verification (PSD2 / ZAG)|PSD2/SCA]] applies
- Does the feature process or store personal data? → [[Regulatory#2. Data Retention & Deletion (GDPR / BDSG)|GDPR]] applies
- Does the feature involve payments or beneficiaries? → [[Regulatory#3. Anti-Money Laundering (AML / GwG)|AML]] screening required
- Does the feature require audit trail? → [[Regulatory#4. Audit Trail Requirements (MaRisk / BAIT)|MaRisk]] applies

> [!danger] Regulatory is non-negotiable
> If any regulatory domain applies, the corresponding controls **must** be implemented in the same PR.
> Do not defer compliance to a follow-up ticket.

### 2. Design API Contract (if applicable)
- Define OpenAPI 3.0 spec before writing code
- Path convention: `/api/v{version}/{resource}` (lowercase, plural nouns)
- Request/response models as Java Records (backend) / TypeScript interfaces (frontend)
- Error responses use standard `ErrorResponse` schema

> [!tip] Embedded: API design rules
> ![[CodingGuidelines#1. API Design: Contract-First]]

### 3. Implement Backend
- Controller delegates to service layer — no business logic in controllers
- Apply naming conventions per [[CodingGuidelines#2. Naming Conventions]]
- Domain-specific exceptions, mapped via `@ControllerAdvice`
- Structured JSON logging with `correlationId`
- No hardcoded configuration values — use `application.yml` with externalized config

### 4. Apply Security Controls
- Endpoint secured with OAuth 2.0 / OIDC
- Authorization via `@PreAuthorize` at service layer
- All inputs validated with Bean Validation annotations
- No secrets in code — Vault references only
- PII masked in all log output

> [!warning] Embedded: Security checklist
> ![[SecurityGuidelines#2. Input Validation & Injection Prevention]]

### 5. Implement Frontend (if applicable)
- React component in PascalCase (`TransferForm.tsx`)
- Every API call handles loading, success, and error states
- No `dangerouslySetInnerHTML` without security review
- Sanitize all user-provided content rendered in the UI
- Accessibility: WCAG 2.1 AA compliance (keyboard navigation, ARIA labels)

### 6. Write Tests

> [!danger] No tests = no merge
> Every feature PR must include unit, integration, and (where applicable) E2E tests.

- **Unit tests**: Service layer logic (JUnit 5 + Mockito / Vitest)
- **Integration tests**: Controller endpoints (MockMvc / Testcontainers)
- **Frontend tests**: Component behavior (Vitest + Testing Library)
- **E2E tests**: Critical user journey if the feature adds a new flow (Playwright)
- Naming: `should_expectedBehavior_when_condition()`
- Coverage: >= 80% line coverage on changed files

> [!info] Embedded: Quality gates
> ![[TestingStrategy#3. Quality Gates in CI Pipeline]]

### 7. Create Pull Request
- Fill out [[PullRequestTemplate]] completely
- Regulatory Impact section: check all applicable boxes
- Security Checklist: all items verified
- Link feature ticket in PR
- Add screenshots/video for UI changes
- Request minimum 2 reviewers
- If security-relevant: add Security Champion (see [[security-review]])

> [!tip] Definition of Done
> ![[PullRequestTemplate#5. Definition of Done]]
