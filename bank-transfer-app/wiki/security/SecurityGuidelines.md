---
domain: security
owner: CISO
last-reviewed: 2025-11-15
status: approved
tags:
  - authentication
  - authorization
  - input-validation
  - secrets
  - dependency-security
  - logging
  - pii
---

# Security Guidelines — Demo Bank

> Mandatory security standards for all development activities.
> Non-compliance blocks deployment. No exceptions.

## 1. Authentication & Authorization

All API endpoints **must** be secured. No anonymous access in production.

- **Authentication**: OAuth 2.0 / OpenID Connect via corporate Identity Provider
- **Authorization**: Role-Based Access Control (RBAC) enforced at service layer, not controller
- **Token handling**: JWTs validated on every request. Never trust client-side claims without server verification.
- **Session management**: Stateless backend. No server-side sessions. Token expiry: 15 minutes access, 8 hours refresh.

```java
// Correct — authorization at service layer
@PreAuthorize("hasRole('TRANSFER_APPROVER')")
public TransferResponse approveTransfer(TransferRequest request) { ... }

// Wrong — authorization only at controller, bypassable via internal calls
```

> [!danger] Forbidden patterns
> - Hardcoded API keys or passwords in source code
> - Basic Auth in any environment
> - Disabling CSRF protection without documented justification

## 2. Input Validation & Injection Prevention

Every external input is untrusted. Validate at system boundaries.

- **Backend**: Use Bean Validation (`@NotNull`, `@Size`, `@Pattern`) on all request DTOs. Additional business validation in service layer.
- **SQL**: Parameterized queries only. No string concatenation for SQL. JPA/Hibernate named parameters or Spring JDBC `NamedParameterJdbcTemplate`.
- **Frontend**: Sanitize all rendered content. React's JSX escaping is baseline — additionally sanitize any `dangerouslySetInnerHTML` usage (which requires security review).
- **File uploads**: Whitelist allowed MIME types. Scan with antivirus. Max size 10MB. Never execute uploaded content.

```java
// Correct
@Pattern(regexp = "^[A-Z]{2}[0-9]{2}[A-Z0-9]{11,30}$", message = "Invalid IBAN format")
private String iban;

// Wrong
String query = "SELECT * FROM accounts WHERE iban = '" + userInput + "'";
```

## 3. Secrets Management

> [!danger] Zero tolerance
> No secrets in code. No secrets in Git. Ever.
> Any secret committed — even briefly — is considered compromised and must be rotated immediately.

- **Storage**: HashiCorp Vault for all secrets (DB credentials, API keys, certificates)
- **Access**: Applications retrieve secrets at runtime via Vault Agent sidecar
- **Rotation**: Database passwords rotate every 30 days (automated). API keys rotate every 90 days.
- **Detection**: Pre-commit hooks scan for secrets (gitleaks). CI pipeline blocks on detection.

```yaml
# Correct (application.yml referencing Vault)
spring:
  datasource:
    url: ${vault:database/creds/readonly/connection_url}

# Wrong (hardcoded)
spring:
  datasource:
    password: SuperSecret123!
```

## 4. Dependency Security

Third-party code is an attack surface. Manage it.

- **Scanning**: OWASP Dependency-Check runs in every CI build. CVE score >= 7.0 blocks the pipeline.
- **Approval**: New dependencies require security review. Evaluate: maintenance status, known CVEs, license, transitive dependencies.
- **Updates**: Critical/High CVEs patched within 48 hours. Medium within 2 weeks. Low within next sprint.
- **Lock files**: `pom.xml` pins exact versions. `package-lock.json` committed. No floating versions.
- **Container images**: Base images from approved registry only. Scanned with Trivy. No `latest` tags.

```xml
<!-- Correct — pinned version -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
    <version>2.7.4</version>
</dependency>
```

> [!tip] See also
> Dependency pinning rules also apply in [[CodingGuidelines#5. Dependency and Configuration Management]].

## 5. Logging, Audit & Data Protection

Log for security. Never log secrets or PII.

- **Audit trail**: All state-changing operations logged with: `who` (user ID), `what` (action), `when` (timestamp), `outcome` (success/failure)
- **PII masking**: Account numbers, names, addresses **must** be masked in logs. Show max last 4 digits of IBAN. Never log full card numbers.
- **Log integrity**: Logs shipped to central SIEM (Splunk/ELK) within 60 seconds. Tamper-evident storage. Retention: 7 years (regulatory).
- **Error messages**: Never expose stack traces, internal paths, or system details to API consumers. Generic user-facing errors, detailed internal logs.

```java
// Correct
log.info("Transfer executed: userId={}, toIban=***{}, amount={}", userId, lastFour(iban), amount);

// Wrong
log.info("Transfer: " + request.toString()); // May contain full PII
```

> [!warning] Regulatory requirement
> Audit trail and retention rules are driven by [[Regulatory#4. Audit Trail Requirements (MaRisk / BAIT)]]. Log retention of 7 years is a legal obligation.
