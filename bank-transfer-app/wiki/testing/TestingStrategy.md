---
domain: testing
owner: QA Lead
last-reviewed: 2025-11-01
status: approved
tags:
  - test-pyramid
  - quality-gates
  - coverage
  - test-data
  - performance
  - contract-testing
---

# Testing Strategy — Demo Bank

> Mandatory testing standards for all development teams.
> No deployment without passing quality gates.

## 1. Test Pyramid

Test distribution strictly follows the pyramid — no inversion allowed.

| Level | Share | Technology | Max Runtime |
|-------|-------|------------|-------------|
| Unit Tests | 70% | JUnit 5, Mockito, Vitest | < 5 min |
| Integration Tests | 20% | MockMvc, Testcontainers | < 10 min |
| E2E Tests | 10% | Playwright | < 15 min |

- **Unit Tests** verify isolated business logic in the service layer. Dependencies are mocked.
- **Integration Tests** verify the interplay of controller, service, and database. Testcontainers for real DB instances.
- **E2E Tests** cover critical user journeys: Login → Transfer → Confirmation.

```java
// Unit Test — isolated service logic
@Test
void should_rejectTransfer_when_dailyLimitExceeded() {
    when(limitService.getRemainingLimit(userId)).thenReturn(BigDecimal.valueOf(500));
    assertThrows(DailyLimitExceededException.class,
        () -> transferService.execute(transferOf(1000)));
}
```

## 2. Test Naming and Structure

Uniform convention across all teams:

- **Methods**: `should_expectedBehavior_when_condition()`
- **Classes**: `{ClassName}Test` for unit, `{ClassName}IT` for integration
- **Files (Frontend)**: `{Component}.test.tsx`
- **Arrange-Act-Assert** pattern in every test. No logic inside tests.

```
src/test/java/com/demobank/transfer/
├── service/
│   └── TransferServiceTest.java          ← Unit
├── controller/
│   └── TransferControllerIT.java         ← Integration
└── e2e/
    └── TransferJourneyE2E.java           ← End-to-End
```

> [!tip] Naming alignment
> Test naming convention must match [[CodingGuidelines#2. Naming Conventions]].

## 3. Quality Gates in CI Pipeline

Every pull request passes automatically:

| Gate | Criterion | Blocks Merge |
|------|-----------|--------------|
| Unit Tests | 100% green | Yes |
| Integration Tests | 100% green | Yes |
| Line Coverage | >= 80% on changed files | Yes |
| Branch Coverage | >= 70% on changed files | Yes |
| Mutation Testing | >= 60% mutation score (critical services) | Yes |
| E2E Smoke | Login + Transfer Journey | Yes |

Coverage measured with JaCoCo (backend) and v8 (frontend). Reports posted as PR comment.

> [!warning] Merge blocker
> These gates are enforced in CI. PRs that don't pass are **not mergeable**.
> See [[PullRequestTemplate#5. Definition of Done]] for the full checklist.

## 4. Test Data Management

> [!danger] No production data
> No production data in test environments. Never. This is a [[Regulatory#2. Data Retention & Deletion (GDPR / BDSG)|GDPR]] requirement.

- **Test data factories**: Each team maintains builder classes for domain objects.
- **Determinism**: Tests generate their own data, never depend on seed data.
- **Cleanup**: Every integration test cleans up after itself (`@Transactional` rollback or explicit cleanup).

```java
// Test data factory
public class TransferFixtures {
    public static TransferRequest aValidTransfer() {
        return new TransferRequest("DE89370400440532013000", "John Doe",
            BigDecimal.valueOf(250.00), "EUR", "Rent February");
    }

    public static TransferRequest anInvalidTransfer() {
        return new TransferRequest("INVALID", "", BigDecimal.valueOf(-1), "XXX", "");
    }
}
```

## 5. Non-Functional Tests

Beyond functional correctness:

- **Performance**: k6 load tests for critical endpoints. Threshold: p95 < 200ms at 100 concurrent users.
- **Security**: OWASP ZAP scan as CI step. No High/Critical findings in release.
- **Accessibility**: axe-core checks in frontend tests. WCAG 2.1 AA compliance.
- **Contract Tests**: Pact tests for service communication. Provider verification blocks deployment.

```javascript
// k6 performance threshold
export const options = {
  thresholds: {
    http_req_duration: ['p(95)<200'],
    http_req_failed: ['rate<0.01'],
  },
};
```

> [!info] DORA connection
> Performance and availability testing supports [[Regulatory#5. Operational Resilience (DORA)]] compliance (99.9% uptime SLA).
