---
skill: security-review
role: Security Champion
triggers:
  - security label on PR
  - auth code changes
  - payment code changes
tags:
  - skill
  - security
  - code-review
---

# Skill: security-review

> Checklist for security reviews of pull requests.
> Activated for PRs with security label or changes to auth/payment code.

## Role
Security Champion

## Required Domains
- security → [[SecurityGuidelines]]
- regulatory → [[Regulatory]]
- testing → [[TestingStrategy]]

## Preconditions
- PR is created and has at least one functional review
- CI pipeline is green

## Steps

### 1. Verify Authentication & Authorization
- Are all new endpoints secured with auth?
- Is authorization enforced at service layer (not just controller)?
- No hardcoded credentials or API keys?
- Token handling correct (no tokens in URLs or logs)?

> [!warning] Embedded: Auth requirements
> ![[SecurityGuidelines#1. Authentication & Authorization]]

### 2. Verify Input Validation
- All external inputs annotated with Bean Validation?
- No string concatenation in SQL/JPQL?
- File uploads: MIME type whitelist and size limit?
- Frontend: XSS vectors in dynamic content secured?

### 3. Verify Data & Logging
- No PII in logs (IBANs, names, addresses masked)?
- Audit trail present for state-changing operations?
- GDPR-relevant fields tagged with retention policy?
- Error responses free of stack traces or internal paths?

> [!info] Regulatory basis
> PII masking and audit trail requirements come from:
> - [[SecurityGuidelines#5. Logging, Audit & Data Protection]]
> - [[Regulatory#4. Audit Trail Requirements (MaRisk / BAIT)]]

### 4. Verify Dependencies
- New dependencies checked for CVEs (OWASP Dependency-Check)?
- License compatibility verified?
- No `latest` tags in container images?
- Lock files committed and consistent?

### 5. Regulatory Review
- AML screening integrated for new payment flows? → [[Regulatory#3. Anti-Money Laundering (AML / GwG)]]
- SCA thresholds correctly implemented? → [[Regulatory#1. Transaction Limits & Verification (PSD2 / ZAG)]]
- Four-eyes principle for transactions > 50,000 EUR? → [[Regulatory#4. Audit Trail Requirements (MaRisk / BAIT)]]
- DORA requirements: health checks, recovery capability? → [[Regulatory#5. Operational Resilience (DORA)]]

### 6. Document Results

> [!danger] Merge blocker
> - Findings as PR comments with severity (Critical/High/Medium/Low)
> - **Critical/High: blocks merge until resolved**
> - Medium/Low: can be addressed as follow-up ticket
> - Approval only when no open Critical/High findings
