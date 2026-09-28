# Demo 02 — IBAN Validation

**Duration:** ~15–20 minutes (3 phases)  
**Difficulty:** Medium

> 📖 **Pick a variant for the full step-by-step**:
> - Agent Garage (n8n + Open WebUI + Claude Code) → [`agent-garage-variant/RUNBOOK.md`](agent-garage-variant/RUNBOOK.md)
> - Agentic Studio (Accenture Agent Execution Environment) → [`agentic-studio-variant/RUNBOOK.md`](agentic-studio-variant/RUNBOOK.md)
>
> 🗺️ **Variant-agnostic orientation** is in [`RUNBOOK.md`](RUNBOOK.md).  
> 🖼️ **Slide-by-slide walkthrough** (Agent Garage) is in [`docs/flow.md`](docs/flow.md).

---

## What This Demo Shows

A complete, autonomous SDLC workflow: a BA agent writes the user story,
a developer agent implements backend validation, frontend feedback, and
tests — without any manual intervention. From a chat prompt to a finished,
review-ready PR.

**Key message:** Not a co-pilot helping a developer — an entire team
working autonomously. BA, developer, verifier — coordinated, and bound
to your organisation's standards.

---

## Three phases

| Phase | Actor | Deliverable |
|---|---|---|
| 1 | Product Owner / Business Analyst | User story as **GitHub issue** |
| 2 | Developer | Backend + frontend + unit/integration tests, **PR** |
| 3 | Verification | **Differs by variant** (see below) |

**Phase 3 by variant**:
- **Agent Garage** — UI Tester adds a **Selenium E2E suite** mapped 1:1
  to acceptance criteria (Claude Code in VS Code, see
  [`ui-test-reference/`](ui-test-reference/)).
- **Agentic Studio** — Code Reviewer reacts to the PR being opened and
  posts structured **review findings** as PR comments.

The workshop deck [`docs/03-IBAN-Validation.pptx`](docs/03-IBAN-Validation.pptx)
(11 slides, rendered as JPGs under [`docs/slides/`](docs/slides/))
illustrates the Agent Garage flow visually.

## Two variants

| Variant | Stack | Playbook |
|---|---|---|
| **Agent Garage** | n8n + Open WebUI + Claude Code | [`agent-garage-variant/RUNBOOK.md`](agent-garage-variant/RUNBOOK.md) — 3-phase playbook with slides + screenshots inline |
| **Agentic Studio** | Accenture Agent Execution Environment (`http://localhost:4000`) | [`agentic-studio-variant/RUNBOOK.md`](agentic-studio-variant/RUNBOOK.md) — 3-phase playbook with 19 live-run screenshots inline |

Both variants tell the same 3-phase story; only the tooling and the
exact Phase 3 role differ. Pick one — the variant RUNBOOK is the single
source of truth for that flow (setup, demo script, troubleshooting).

---

## Starting Point

The app in `bank-transfer-app/` is a complete SEPA transfer form (Spring
Boot backend + React frontend). The IBAN field accepts any input without
validation — that is the demo starting point.

To bring it up locally (needed by both variants for Phase 2):

```bash
cd bank-transfer-app
mvn spring-boot:run             # http://localhost:8089

cd bank-transfer-app/frontend
npm install && npm run dev      # http://localhost:5173 (proxies /api → 8089)
```

---

## Quick replay (`Makefile`)

For a scripted replay of the post-Phase-2 / post-Phase-3 end state
(without actually running the agent flow), the top-level `Makefile`
orchestrates the two reference overlays:

```bash
make serve      # start backend (:8089) + frontend (:5173) in background
make feature    # overlay the IBAN-validation feature impl  (feature-impl-reference/)
make test       # extend pom + copy E2E suite + run mvn test (ui-test-reference/)
make clean      # stop servers + revert bank-transfer-app to pristine state
```

`make all` chains `serve → feature → test`. A passing run prints
**`Tests run: 11, Failures: 0, Errors: 0`** for the Selenium suite.

The two overlays — [`feature-impl-reference/`](feature-impl-reference/) and
[`ui-test-reference/`](ui-test-reference/) — are independently usable
(each has its own `make up` / `make down` and README). The root Makefile
just chains them and manages the long-running dev servers (logs in
`.run/`, gitignored). Designed so the demo bank-transfer-app stays
pristine on disk between runs.

---

## Test IBANs

| IBAN | Status |
|------|--------|
| `DE89 3704 0044 0532 0130 00` | ✅ Valid |
| `DE89 3704 0044 0532 0130 01` | ❌ Invalid (check digit wrong) |
| `GB29 NWBK 6016 1331 9268 19` | ✅ Valid (UK — shows country format support) |
| `XX00 0000 0000 0000` | ❌ Invalid (unknown country code) |

---

## Application Structure

```
bank-transfer-app/
├── CLAUDE.md                    # Project quick reference for Claude Code
├── pom.xml                      # Spring Boot 2.7.4, Java 17
├── src/main/java/               # Backend source code
├── frontend/                    # React 18 + TypeScript + Vite
├── wiki/                        # Knowledge base (Obsidian vault)
│   ├── architecture/            # Coding guidelines
│   ├── security/                # Security guidelines
│   ├── regulatory/              # PSD2, GDPR, AML, MaRisk, DORA
│   ├── testing/                 # Testing strategy
│   └── skills/                  # Skill workflows (feature.md, bugfix.md, ...)
├── backlog/                     # Feature requirements
└── .claude/commands/            # Slash commands for Claude Code
```
