---
domain: templates
owner: Head of Engineering
last-reviewed: 2025-11-15
status: approved
tags:
  - incident-response
  - dora
  - escalation
  - post-mortem
---

# Incident Response Template — Demo Bank

> Standard template for security and operational incidents.
> DORA-compliant: documentation starts at detection, not at resolution.

## 1. Incident Classification

| Severity | Criterion | Response Time | Report to Regulator |
|----------|-----------|---------------|---------------------|
| **SEV-1** | Payment services down, data loss | Immediate, War Room | Yes, < 4h |
| **SEV-2** | Single service degraded, no data loss | < 30 min | Situational |
| **SEV-3** | Performance degradation, workaround available | < 2h | No |
| **SEV-4** | Cosmetic issue, no business impact | Next sprint | No |

> [!danger] SEV-1 reporting deadline
> DORA requires regulatory reporting within **4 hours** of classification.
> See [[Regulatory#5. Operational Resilience (DORA)]] for full requirements.

## 2. Incident Report Structure

```markdown
# Incident Report: {INCIDENT-ID}

**Severity**: SEV-{1-4}
**Status**: Detected | Investigating | Mitigated | Resolved | Post-Mortem
**Detection Time**: YYYY-MM-DD HH:MM UTC
**Resolution Time**: YYYY-MM-DD HH:MM UTC
**Duration**: X hours Y minutes
**Incident Commander**: {Name}

## Impact
<!-- Who and what was affected? -->
- Affected services:
- Affected customers (count/segment):
- Financial impact:

## Timeline
| Time (UTC) | Event |
|------------|-------|
| HH:MM | Alert triggered by {monitoring system} |
| HH:MM | Incident Commander assigned |
| HH:MM | Root cause identified |
| HH:MM | Mitigation applied |
| HH:MM | Service fully restored |

## Root Cause
<!-- Technical root cause. No blame. -->

## Mitigation & Resolution
<!-- What was done to resolve the incident? -->

## Action Items
| # | Action | Owner | Deadline | Status |
|---|--------|-------|----------|--------|
| 1 | ... | ... | ... | Open |

## Regulatory Notification
- [ ] Regulatory report required: Yes / No
- [ ] Report filed on: YYYY-MM-DD
- [ ] Customer notification required: Yes / No
```

## 3. Escalation Path

```
Developer on-call
    → Engineering Lead (SEV-3+)
        → Head of Engineering (SEV-2+)
            → CTO + CISO + Compliance (SEV-1)
                → Regulatory report (SEV-1, < 4h)
```

## 4. Post-Mortem Requirements

- **SEV-1 and SEV-2**: Post-mortem within 5 business days
- Blameless culture: focus on systems, not individuals
- Outcome: At least 3 action items with owner and deadline
- Post-mortem presented to the team and archived

## 5. Communication

- **Internal**: Slack channel `#incidents` — all updates in real-time
- **Status page**: External communication via status.demobank.com
- **Customers**: Data breach notification within 72h ([[Regulatory#2. Data Retention & Deletion (GDPR / BDSG)|GDPR Art. 33/34]])
- **Regulators**: DORA-compliant report via established reporting channel

> [!info] Security incidents
> If the incident involves a security breach, also apply [[SecurityGuidelines#3. Secrets Management]] — any exposed secrets must be rotated immediately.
