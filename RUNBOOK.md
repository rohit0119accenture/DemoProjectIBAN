# Demo 02 — IBAN Validation — Overview

This file is an **orientation page**. The actual end-to-end playbooks live
inside each variant. Pick the variant that matches the stack you're running
the demo on:

| Variant | Stack | Playbook |
|---|---|---|
| **Agent Garage** | n8n + Open WebUI + Claude Code (terminal + IDE) | [`agent-garage-variant/RUNBOOK.md`](agent-garage-variant/RUNBOOK.md) |
| **Agentic Studio** | Accenture Agent Execution Environment | [`agentic-studio-variant/RUNBOOK.md`](agentic-studio-variant/RUNBOOK.md) |

Both variants tell the same 3-phase story; only the tooling and the
exact role split in Phase 3 differ (see footnote on the table below).

---

## The 3-phase story (variant-agnostic)

```
   Phase 1                  Phase 2                       Phase 3
 ┌────────────┐         ┌──────────────────┐           ┌──────────────┐
 │  Product   │ handover│   Developer      │ handover  │  UI Tester   │
 │  Owner     │ ───────▶│                  │ ────────▶ │              │
 │            │  via GH │   implements     │  via GH   │  adds        │
 │  drafts    │  issue  │   backend +      │  branch   │  Selenium    │
 │  a user    │         │   frontend +     │           │  E2E suite   │
 │  story     │         │   tests          │           │  (1:1 to AC) │
 └────────────┘         └──────────────────┘           └──────────────┘
       │                         │                              │
       ▼                         ▼                              ▼
  user story            backend + frontend                  11 E2E tests
  as GH issue           + unit/integration tests            green
                        + manual test in browser
```

| Phase | Actor | Deliverable |
|---|---|---|
| 1 | Product Owner / Business Analyst | User story as a **GitHub issue** in the demo repo |
| 2 | Developer | Backend + frontend + unit/integration tests, **PR** on a feature branch |
| 3 | Verification | **Phase-3 role differs by variant** — see below |

**Phase 3 by variant**:
- **Agent Garage** — UI Tester adds a **Selenium E2E suite** mapped 1:1
  to the acceptance criteria, committed on the same branch.
- **Agentic Studio** — Code Reviewer reacts to the PR being opened and
  posts structured **review findings** as PR comments.

The workshop deck [`docs/03-IBAN-Validation.pptx`](docs/03-IBAN-Validation.pptx)
walks the Agent Garage flow visually — slides are rendered under
[`docs/slides/`](docs/slides/) and embedded inline in
[`agent-garage-variant/RUNBOOK.md`](agent-garage-variant/RUNBOOK.md). A
slide-by-slide commentary is in [`docs/flow.md`](docs/flow.md). The
Agentic Studio variant has its own per-step live-run screenshots embedded
in [`agentic-studio-variant/RUNBOOK.md`](agentic-studio-variant/RUNBOOK.md).

---

## Repo layout

```
demo-02-iban-validation/
├── README.md                                ← entry point
├── RUNBOOK.md                               ← this orientation file
├── Makefile                                 ← serve / feature / test / clean orchestration
├── docs/
│   ├── 03-IBAN-Validation.pptx              ← workshop deck (Agent Garage flow)
│   ├── slides/                              ← per-slide JPGs
│   ├── screenshots/phase-1/                 ← Agent Garage Phase 1 live-run shots
│   ├── flow.md                              ← slide-by-slide walkthrough
│   └── README.md
├── bank-transfer-app/                       ← Phase 2 START state (no IBAN validation)
├── feature-impl-reference/                  ← Phase 2 END-state overlay (make up applies it)
├── ui-test-reference/                       ← Phase 3 Agent Garage reference (Selenium E2E)
├── agent-garage-variant/                    ← Agent Garage stack + 3-phase RUNBOOK
│   ├── RUNBOOK.md                           ← canonical playbook for this variant
│   ├── stack/                               ← docker-compose: postgres + n8n + openwebui
│   └── tests/                               ← Phase 1 automated regression
└── agentic-studio-variant/                  ← Agentic Studio 3-phase RUNBOOK
    ├── RUNBOOK.md                           ← canonical playbook for this variant
    └── docs/screenshots/{setup,phase-1,phase-2,closing}/   ← inline screenshots
```

---

## Quick replay (without running the agent flow)

The top-level `Makefile` orchestrates the two reference overlays so you
can replay the post-Phase-2 / post-Phase-3 end-state on a clean
bank-transfer-app:

| Target | What it does |
|---|---|
| `make serve`   | Starts backend (`:8089`) + frontend (`:5173`) in the background. Installs `frontend/node_modules` on first run. Logs in `.run/`. |
| `make feature` | Overlays the IBAN-validation feature implementation onto bank-transfer-app's frontend. Vite hot-reloads it. |
| `make test`    | Extends pom.xml (adds Selenium dep + Surefire E2E include + overrides Spring Boot's BOM-pinned `selenium.version`), copies the E2E test into bank-transfer-app, runs `mvn test -Dtest=IbanValidationE2E`. |
| `make clean`   | Stops both servers, reverts the frontend files (`git checkout HEAD`), reverts `pom.xml`, removes the copied E2E test directory, removes `.run/`. |
| `make all`     | `serve → feature → test` in sequence. |

A passing replay prints **`Tests run: 11, Failures: 0, Errors: 0`** for
the Selenium suite. bank-transfer-app is pristine again after `make clean`.

---

## Quick links

- **Run the demo end-to-end (Agent Garage)** → [`agent-garage-variant/RUNBOOK.md`](agent-garage-variant/RUNBOOK.md)
- **Run the demo end-to-end (Agentic Studio)** → [`agentic-studio-variant/RUNBOOK.md`](agentic-studio-variant/RUNBOOK.md)
- **Look up Phase 3 deliverable (Agent Garage / Selenium suite)** → [`ui-test-reference/README.md`](ui-test-reference/README.md)
- **See the slide flow** → [`docs/flow.md`](docs/flow.md)
- **Watch Phase 1 in screenshots (Agent Garage)** → [`docs/screenshots/phase-1/README.md`](docs/screenshots/phase-1/README.md)
