---
domain: architecture
owner: Engineering Lead
last-reviewed: 2025-12-01
status: approved
tags:
  - api-design
  - naming
  - error-handling
  - testing
  - dependencies
---

# Coding Guidelines — Demo Bank

> Organizational standard for all backend and frontend development.
> These rules apply to every code contribution — manual or AI-assisted.

## 1. API Design: Contract-First

All REST APIs **must** be defined as OpenAPI 3.0 specs before implementation begins.

- Path convention: `/api/v{version}/{resource}` (lowercase, plural nouns)
- Request/response models: Java Records (backend), TypeScript interfaces (frontend)
- No business logic in controllers — controllers delegate to service layer
- Every endpoint must return structured error responses (`ErrorResponse` with `code`, `message`, `details`)

```yaml
# Example: Correct
POST /api/v1/transfers

# Example: Wrong
POST /api/doTransfer
POST /transferMoney
```

## 2. Naming Conventions

| Context | Convention | Example |
|---------|-----------|---------|
| Java classes | PascalCase | `TransferService` |
| Java methods | camelCase | `validateAmount()` |
| REST paths | kebab-case | `/api/v1/account-statements` |
| DB columns | snake_case | `created_at` |
| React components | PascalCase | `TransferForm.tsx` |
| TypeScript functions | camelCase | `formatCurrency()` |
| Environment variables | UPPER_SNAKE | `DB_CONNECTION_URL` |

> [!warning] No abbreviations
> No abbreviations in public APIs. `transaction`, not `txn`. `account`, not `acct`.

## 3. Error Handling Strategy

Fail fast, fail explicitly:

- **Backend**: Use domain-specific exceptions (`InsufficientFundsException`, `AccountNotFoundException`). Map to HTTP status codes via `@ControllerAdvice`. Never catch generic `Exception` silently.
- **Frontend**: Every API call must handle loading, success, and error states. Use try/catch with user-visible error messages. Never swallow errors in `console.log` only.
- **Logging**: Structured JSON logs (SLF4J + Logback). Include `correlationId` in every log line. Log at ERROR for failures, WARN for degradation, INFO for business events.

```java
// Correct
throw new TransferLimitExceededException(amount, dailyLimit);

// Wrong
throw new RuntimeException("Transfer failed");
```

> [!tip] See also
> Logging rules overlap with [[SecurityGuidelines#5. Logging, Audit & Data Protection]] — ensure PII masking is applied.

## 4. Testing Requirements

Every pull request must include:

- **Unit tests** for all service-layer logic (JUnit 5, Mockito)
- **Integration tests** for controller endpoints (MockMvc or WebTestClient)
- **Frontend tests** for user-facing components (Vitest + Testing Library)
- Minimum coverage: **80% line coverage** on changed files
- Test naming: `should_expectedBehavior_when_condition()`

```java
@Test
void should_rejectTransfer_when_amountExceedsDailyLimit() { ... }
```

> [!danger] No tests = no merge
> No exceptions. See [[TestingStrategy]] for full quality gates and test pyramid.

## 5. Dependency and Configuration Management

- **No hardcoded values**: All environment-specific config goes to `application.yml` with profile overrides (`application-dev.yml`, `application-prod.yml`)
- **Dependency updates**: Renovate bot runs weekly. Security patches merged within 48h.
- **No new dependencies** without Architecture Board approval for: ORMs, HTTP clients, serialization libraries, security frameworks
- **Frontend**: Pin exact versions in `package.json`. No `^` or `~` ranges in production dependencies.

```yaml
# Correct (application.yml)
transfer:
  daily-limit: ${TRANSFER_DAILY_LIMIT:10000}

# Wrong (hardcoded in Java)
private static final int DAILY_LIMIT = 10000;
```

> [!info] Regulatory connection
> Configuration values for transaction limits must comply with [[Regulatory#1. Transaction Limits & Verification (PSD2 / ZAG)]].
