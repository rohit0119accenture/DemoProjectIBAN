# Demo 02 — Agent Garage variant — Tests

Two automated tests cover the happy path and the workflow-level confirmation
gate of the User Story Assistant.

## Prerequisites

- Stack running locally (see `../stack/README.md` or the top-level `RUNBOOK.md`).
- Python ≥ 3.10 and a venv:
  ```bash
  python3 -m venv .venv
  source .venv/bin/activate
  pip install -r requirements.txt
  playwright install chromium
  ```
- `gh` CLI authenticated for the target repo
  (`gh auth status` must show github.com login). Alternatively set `GH_TOKEN`.

## Tests

### `e2e_webhook.py` — direct n8n webhook (fast, no browser)

Talks straight to the n8n webhook. Verifies:
- **Scenario A (happy path)**: short prompt + clarifications + `push` ⇒ new GitHub issue.
- **Scenario B (confirmation gate)**: a long first-turn message that tries to
  push immediately must NOT create any issue.

```bash
python e2e_webhook.py
```

### `e2e_owui.py` — full E2E via Open WebUI (Playwright)

Real browser, real login, real chat input. Verifies the entire user-facing flow
including the Pipe ↔ n8n integration.

```bash
python e2e_owui.py
# screenshots go to ./shots/
```

## Configuration

All tests honour these env vars:

| Var | Default |
|---|---|
| `OWUI_URL` | `http://localhost:3000` |
| `OWUI_ADMIN_EMAIL` | `admin@test.com` |
| `OWUI_ADMIN_PASSWORD` | `admin123` |
| `N8N_URL` | `http://localhost:5678/webhook/880e3fd7-2418-4344-80e0-91f8305cab86` |
| `GITHUB_REPO` | `AccentureCodeFoundry/313885_agentic-demo-collection` |
| `SHOTS_DIR` | `./shots` (e2e_owui only) |
| `DEMO_PROMPT` | the SEPA IBAN prompt (e2e_owui only) |

## What "PASS" means

For `e2e_owui.py` and `e2e_webhook.py` scenario A:
- A new GitHub issue exists in the target repo.
- Issue number is higher than the most recent issue captured before the run.
- Issue title contains "iban".

For `e2e_webhook.py` scenario B:
- The latest issue number after the run is unchanged from before — the gate
  successfully blocked the push.
