---
domain: templates
owner: Engineering Lead
last-reviewed: 2025-11-01
status: approved
tags:
  - pull-request
  - code-review
  - definition-of-done
  - branching
---

# Pull Request Template — Demo Bank

> Standard template for all pull requests.
> Every PR must follow this structure.

## 1. PR Title Convention

Format: `{type}: {short description}`

| Type | Usage |
|------|-------|
| `feat` | New functionality |
| `fix` | Bug fix |
| `refactor` | Code restructuring without behavior change |
| `security` | Security-related change |
| `docs` | Documentation |
| `test` | Tests only added/changed |
| `chore` | Build, dependencies, config |

```
feat: Add IBAN validation to transfer endpoint
fix: Correct daily limit calculation for joint accounts
security: Upgrade Spring Boot to patch CVE-2024-XXXXX
```

## 2. PR Description Structure

Every PR description must contain these sections:

```markdown
## Summary
<!-- What was changed and why? 2-3 sentences. -->

## Changes
<!-- Bullet points of concrete changes -->
- Added `IbanValidator` with checksum verification
- Extended `TransferRequest` with `@Pattern` annotation
- Added unit tests for edge cases (invalid length, wrong checksum)

## Regulatory Impact
<!-- Which regulatory requirements are affected? -->
- [ ] PSD2 / SCA — not affected
- [ ] AML / GwG — not affected
- [ ] GDPR / BDSG — not affected
- [ ] MaRisk / BAIT — not affected

## Security Checklist
- [ ] No secrets in code
- [ ] Input validation present
- [ ] No SQL injection risks
- [ ] Logging contains no PII
- [ ] Dependencies checked for CVEs

## Testing
- [ ] Unit tests written
- [ ] Integration tests written
- [ ] Coverage >= 80% on changed files
- [ ] Manually tested (screenshot/video if UI)

## Deployment Notes
<!-- Any special notes for deployment? -->
- [ ] DB migration included
- [ ] Feature flag required
- [ ] Configuration change in Vault
- [ ] No special notes
```

> [!info] Where do these rules come from?
> - Regulatory Impact → [[Regulatory]]
> - Security Checklist → [[SecurityGuidelines]]
> - Testing requirements → [[TestingStrategy#3. Quality Gates in CI Pipeline]]

## 3. Review Rules

- Minimum **2 approvals** before merge
- Security-relevant PRs: additional review by Security Champion
- Regulatory changes: add Compliance team as reviewer
- **Four-eyes principle**: Author may not approve their own PR
- Auto-merge only after green CI pipeline and all approvals

> [!warning] Four-eyes principle
> This is not just a best practice — it is a regulatory requirement.
> See [[Regulatory#4. Audit Trail Requirements (MaRisk / BAIT)]].

## 4. Branch Convention

Format: `{type}/{ticket-id}-{short-description}`

```
feat/BANK-1234-iban-validation
fix/BANK-5678-daily-limit-calculation
security/BANK-9012-spring-boot-cve
```

- Branches deleted after merge
- No direct push to `main` or `develop`
- Rebase before merge (no merge-commit clutter)

## 5. Definition of Done

A PR is only "Done" when:

- [ ] Code compiles and all tests green
- [ ] Coverage thresholds met
- [ ] PR template fully completed
- [ ] At least 2 reviews with approval
- [ ] CI pipeline fully green
- [ ] API catalog updated (if new/changed endpoint)
- [ ] No open comments / conversations
