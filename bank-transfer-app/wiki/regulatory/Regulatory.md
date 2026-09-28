---
domain: regulatory
owner: Head of Compliance
last-reviewed: 2025-10-20
status: approved
tags:
  - psd2
  - sca
  - gdpr
  - aml
  - marisk
  - bait
  - dora
---

# Regulatory Requirements — Demo Bank

> Compliance constraints derived from banking regulations (BaFin, ECB, PSD2).
> These are non-negotiable. They override convenience, performance, and feature requests.

## 1. Transaction Limits & Verification (PSD2 / ZAG)

Strong Customer Authentication (SCA) is legally required for electronic payments.

- **SCA trigger**: Every payment transaction > 30 EUR requires two-factor authentication
- **Daily limits**: Default 10,000 EUR per customer per day. Configurable per product but never above regulatory cap.
- **Cumulative monitoring**: If cumulative contactless/low-value transactions exceed 150 EUR or 5 transactions since last SCA — force re-authentication.
- **Exemptions**: Trusted beneficiaries (whitelist), recurring fixed-amount payments, merchant-initiated transactions — each requires documented risk assessment.

```yaml
# Configuration — not hardcoded
transfer:
  sca-threshold: 30.00
  daily-limit: 10000.00
  contactless-cumulative-limit: 150.00
  contactless-cumulative-count: 5
```

> [!danger] Implementation rule
> Limits are enforced at the **service layer**, never at the frontend only.
> Frontend validation is UX — backend validation is compliance.
> See [[CodingGuidelines#1. API Design: Contract-First]] for the service-layer delegation pattern.

## 2. Data Retention & Deletion (GDPR / BDSG)

Personal data has a lifecycle. Respect it.

- **Transaction records**: Retain for **10 years** (HGB §257, AO §147) — then delete
- **Customer identity data**: Retain for **5 years** after account closure (GwG §8)
- **Consent records**: Retain for duration of consent + 3 years
- **Application logs with PII**: Maximum **90 days**, then anonymize or delete
- **Right to erasure**: Must be fulfillable within **30 days**. Design data models to support selective deletion without breaking referential integrity.

```sql
-- Data model must support soft-delete + anonymization
ALTER TABLE customers ADD COLUMN anonymized_at TIMESTAMP;
ALTER TABLE customers ADD COLUMN deletion_requested_at TIMESTAMP;
```

> [!warning] No PII in caches
> No personal data in caches without TTL. No PII in search indexes without documented legal basis.
> PII masking in logs is enforced via [[SecurityGuidelines#5. Logging, Audit & Data Protection]].

## 3. Anti-Money Laundering (AML / GwG)

Every financial transaction is subject to AML monitoring.

- **KYC verification**: Required before account opening. Identity document + video ident or PostIdent.
- **Transaction monitoring**: Real-time screening against sanctions lists (EU, UN, OFAC). Batch analysis for suspicious patterns (structuring, rapid movement, unusual geography).
- **Suspicious Activity Reports (SAR)**: Auto-flagged transactions forwarded to Compliance within **24 hours**. Filed with FIU within **3 business days**.
- **Threshold reporting**: Cash transactions >= 10,000 EUR reported automatically.
- **PEP screening**: Politically Exposed Persons flagged during onboarding and continuously.

```java
// Every transfer service must call AML check before execution
public TransferResponse executeTransfer(TransferRequest request) {
    amlService.screenTransaction(request);  // Mandatory — never skip
    sanctionsService.checkBeneficiary(request.beneficiaryName(), request.beneficiaryIban());
    return processTransfer(request);
}
```

> [!danger] No bypass
> **Never** implement a bypass for AML checks — not in dev, not in staging, not for "test accounts."

## 4. Audit Trail Requirements (MaRisk / BAIT)

Regulators will audit. Be ready.

- **Every state change** must be traceable: who initiated, who approved, what changed, when
- **Four-eyes principle**: Transfers above 50,000 EUR require approval by a second authorized person. Enforced in code, not process.
- **Immutable audit log**: Append-only storage. No updates, no deletes. Separate from application database.
- **Retention**: Audit logs retained for **10 years**
- **Access**: Audit data accessible to internal audit and BaFin within **2 business days** on request.

```java
@Entity
@Table(name = "audit_log")
public class AuditEntry {
    @Id private UUID id;
    private String action;        // TRANSFER_INITIATED, TRANSFER_APPROVED, etc.
    private String initiatedBy;   // User ID
    private String approvedBy;    // User ID (if applicable)
    private Instant timestamp;
    private String entityType;    // "Transfer", "Account", etc.
    private String entityId;
    private String changePayload; // JSON diff — no PII in clear text
}
```

> [!tip] Implementation
> The four-eyes principle and audit trail must be reflected in the [[PullRequestTemplate#3. Review Rules]] for code changes as well.

## 5. Operational Resilience (DORA)

The Digital Operational Resilience Act (DORA) applies from January 2025.

- **Availability SLA**: Core banking services must achieve **99.9% uptime** (max ~8.7h downtime/year)
- **Recovery Time Objective (RTO)**: Critical payment services — **2 hours**
- **Recovery Point Objective (RPO)**: Transaction data — **0 data loss** (synchronous replication)
- **ICT incident reporting**: Major incidents reported to BaFin within **4 hours** of classification
- **Third-party risk**: All critical ICT providers (cloud, payments, identity) must have exit strategies and contractual audit rights documented.

```yaml
# Health check endpoint — required for monitoring
management:
  endpoints:
    web:
      exposure:
        include: health, info, metrics
  endpoint:
    health:
      show-details: when-authorized
```

> [!info] Incident handling
> For incident classification and reporting procedures, see [[IncidentResponseTemplate]].
> DORA requires reporting within 4 hours — the template enforces this.

**Testing**: Annual disaster recovery tests. Quarterly failover drills. Results documented and available for regulatory review.
