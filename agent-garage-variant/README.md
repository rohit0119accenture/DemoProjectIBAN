# Demo 02 — IBAN Validation — Agent Garage variant

The n8n / Open WebUI flavour of Demo 02. The **canonical 3-phase playbook**
for this variant (setup, demo script for all three phases, automated tests,
troubleshooting) lives in **[RUNBOOK.md](RUNBOOK.md)** — that's the file to
open before running the demo.

```
demo-02-iban-validation/agent-garage-variant/
├── RUNBOOK.md       ← 3-phase playbook (Phase 1 chat + Phase 2 dev + Phase 3 UI test)
├── stack/           ← docker compose + n8n workflow + OWUI pipe (Phase 1 stack)
└── tests/           ← Phase 1 automated regression (webhook + Playwright)
```

## TL;DR — Phase 1 stack

```bash
cd stack
cp .env.example .env             # fill ANTHROPIC_API_KEY, generate n8n secrets
docker compose up -d
# create n8n credentials (Anthropic, GitHub) manually and link them on the
# two nodes — details in RUNBOOK §4.3
open http://localhost:3000       # admin@test.com / admin123
```

Send this prompt in the "User Story Assistant" chat:

> Our current SEPA transfer form does not validate IBANs. Create a story to
> implement this. Use the API provided by https://openiban.com/ for this
> purpose.

Answer the clarification(s) with `1`, then send `push`. A GitHub issue is
created in the configured repo (Phase 1 deliverable). The handover to
Phase 2 (developer agent in `bank-transfer-app/`) and Phase 3 (UI tester
agent adds Selenium suite) is covered in [`RUNBOOK.md`](RUNBOOK.md) §Phase 2
and §Phase 3.

## Sibling variant

The other Demo-02 implementation is the **Agentic Studio variant** — same
demo, different stack. Don't confuse the two.
