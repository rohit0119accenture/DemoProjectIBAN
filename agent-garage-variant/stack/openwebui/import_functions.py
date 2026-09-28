"""
Idempotent import of Open WebUI pipe functions via REST API.

Runs as an init-container after Open WebUI becomes healthy. For every
*.py file under /functions:
  1. Auth as the admin user
  2. Create the function (or update if it already exists)
  3. Push valves derived from environment variables so the pipe immediately
     points at the right backend (n8n webhook in this demo)
"""

import json
import os
import sys
import time
import urllib.error
import urllib.request
from pathlib import Path


OWUI_URL = os.environ.get("OWUI_URL", "http://openwebui:8080")
ADMIN_EMAIL = os.environ.get("OWUI_ADMIN_EMAIL", "admin@test.com")
ADMIN_PASSWORD = os.environ.get("OWUI_ADMIN_PASSWORD", "admin123")
FUNCTIONS_DIR = Path("/functions")

# Demo-02-specific config
N8N_WEBHOOK_PATH = os.environ.get(
    "N8N_WEBHOOK_PATH", "880e3fd7-2418-4344-80e0-91f8305cab86"
)
N8N_BASE_URL = os.environ.get("N8N_BASE_URL", "http://n8n:5678")


def api(method, path, payload=None, token=None):
    url = OWUI_URL + path
    data = json.dumps(payload).encode() if payload else None
    headers = {"Content-Type": "application/json"}
    if token:
        headers["Authorization"] = f"Bearer {token}"
    req = urllib.request.Request(url, data=data, headers=headers, method=method)
    try:
        with urllib.request.urlopen(req, timeout=15) as resp:
            return json.loads(resp.read())
    except urllib.error.HTTPError as exc:
        body = exc.read().decode(errors="replace")
        raise RuntimeError(f"HTTP {exc.code} {method} {path}: {body}") from exc


def wait_for_openwebui(max_tries=30, delay=5):
    print(f"Waiting for OpenWebUI at {OWUI_URL} ...")
    for i in range(max_tries):
        try:
            urllib.request.urlopen(OWUI_URL + "/health", timeout=5)
            print("OpenWebUI is up.")
            return
        except Exception:
            print(f"  [{i + 1}/{max_tries}] not ready, retry in {delay}s ...")
            time.sleep(delay)
    print("ERROR: OpenWebUI did not become healthy in time.", file=sys.stderr)
    sys.exit(1)


def valves_for(func_id):
    """Per-function valve override map. Only IDs listed here get valves applied."""
    if func_id == "user_story_creator":
        return {
            "n8n_url": f"{N8N_BASE_URL}/webhook/{N8N_WEBHOOK_PATH}",
            "n8n_bearer_token": "not-required-but-must-be-set",
            "input_field": "chatInput",
            "response_field": "output.message",
            "emit_interval": 2.0,
            "enable_status_indicator": True,
        }
    return None


def main():
    wait_for_openwebui()

    print(f"Signing in as {ADMIN_EMAIL} ...")
    auth = api(
        "POST",
        "/api/v1/auths/signin",
        {"email": ADMIN_EMAIL, "password": ADMIN_PASSWORD},
    )
    token = auth["token"]
    print("Auth OK.")

    function_files = sorted(FUNCTIONS_DIR.glob("*.py"))
    if not function_files:
        print("No function files found in /functions — nothing to import.")
        return

    for func_file in function_files:
        content = func_file.read_text()

        func_id = func_file.stem
        func_name = func_id
        for line in content.splitlines():
            if line.strip().startswith("title:"):
                func_name = line.split(":", 1)[1].strip()
                break

        print(f"\nProcessing function: {func_id} ({func_name})")

        # Create or update
        try:
            api("GET", f"/api/v1/functions/id/{func_id}", token=token)
            print(f"  exists -> updating ...")
            api(
                "POST",
                f"/api/v1/functions/id/{func_id}/update",
                {
                    "id": func_id,
                    "name": func_name,
                    "content": content,
                    "meta": {"description": func_name},
                },
                token=token,
            )
            print(f"  updated: {func_id}")
        except RuntimeError as exc:
            if any(
                m in str(exc).lower() for m in ("404", "not found", "could not find")
            ):
                print(f"  not found -> creating ...")
                api(
                    "POST",
                    "/api/v1/functions/create",
                    {
                        "id": func_id,
                        "name": func_name,
                        "content": content,
                        "meta": {"description": func_name},
                    },
                    token=token,
                )
                print(f"  created: {func_id}")
            else:
                print(f"  ERROR: {exc}", file=sys.stderr)
                sys.exit(1)

        # Push valves
        valves = valves_for(func_id)
        if valves:
            try:
                api(
                    "POST",
                    f"/api/v1/functions/id/{func_id}/valves/update",
                    valves,
                    token=token,
                )
                print(f"  valves applied: {func_id}")
                print(f"    n8n_url -> {valves['n8n_url']}")
            except RuntimeError as exc:
                print(f"  WARN: failed to apply valves for {func_id}: {exc}")

        # Ensure the function is active (newly created pipes default to inactive)
        try:
            info = api("GET", f"/api/v1/functions/id/{func_id}", token=token)
            if not info.get("is_active"):
                api(
                    "POST",
                    f"/api/v1/functions/id/{func_id}/toggle",
                    token=token,
                )
                print(f"  activated: {func_id}")
            else:
                print(f"  already active: {func_id}")
        except RuntimeError as exc:
            print(f"  WARN: failed to toggle active state for {func_id}: {exc}")

    print("\n=== Open WebUI function import done ===")


if __name__ == "__main__":
    main()
