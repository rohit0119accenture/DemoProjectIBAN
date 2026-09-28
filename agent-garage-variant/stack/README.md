# Stack

Minimal docker-compose stack for Demo 02 (Agent Garage variant): n8n,
PostgreSQL, Open WebUI. Setup procedure is in the parent
[`RUNBOOK.md`](../RUNBOOK.md) (§3–§4).

## Layout

```
stack/
├── docker-compose.yml
├── .env.example
├── n8n/
│   └── backup/
│       ├── workflows/                            ← imported at boot
│       │   └── 6_User_Story_Assistant_GitHub.json
│       └── credentials/                          ← put your exported credentials here (optional)
└── openwebui/
    ├── functions/
    │   └── user_story_creator.py                 ← pipe (auto-imported)
    └── import_functions.py                       ← init-container script
```

## What auto-imports

- **n8n-import** init container: loads `n8n/backup/workflows/*.json` and any
  files under `n8n/backup/credentials/` (the demo ships without credential
  files — you create them manually in the n8n UI; see RUNBOOK §4.3).
- **openwebui-functions-import** init container: uploads
  `openwebui/functions/*.py` via the OpenWebUI REST API and pushes the
  `n8n_url` valve into the pipe so it talks to the right webhook.

Both init containers are **idempotent** — re-running `docker compose up -d`
won't duplicate anything.

## Reset

```bash
docker compose down -v       # wipes all volumes (postgres, n8n, openwebui)
docker compose up -d
```

You'll have to re-create the Anthropic + GitHub credentials in the n8n UI
(they live in the n8n volume which `down -v` wiped).
