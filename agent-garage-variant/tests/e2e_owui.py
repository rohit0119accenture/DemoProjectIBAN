"""
End-to-end test for Demo 02 (Agent Garage / n8n variant).

What it does:
  1. Logs into Open WebUI at OWUI_URL with admin credentials.
  2. Opens a brand-new chat (model "User Story Assistant").
  3. Sends the IBAN-validation prompt.
  4. Auto-answers numeric clarifications with "1" until the assistant displays
     the final confirmation menu ("Push the story to GitHub").
  5. Sends "push" to confirm.
  6. Polls the GitHub API (via `gh` CLI) to verify a new issue was created
     in the target repository.

Requires:
  pip install -r requirements.txt
  playwright install chromium
  gh CLI authenticated to github.com (or set GH_TOKEN)

Run:
  python e2e_owui.py
"""

import json
import os
import subprocess
import sys
import time

from playwright.sync_api import sync_playwright, TimeoutError as PWTimeout

OWUI_URL = os.environ.get("OWUI_URL", "http://localhost:3000")
EMAIL = os.environ.get("OWUI_ADMIN_EMAIL", "admin@test.com")
PASSWORD = os.environ.get("OWUI_ADMIN_PASSWORD", "admin123")
REPO = os.environ.get(
    "GITHUB_REPO", "AccentureCodeFoundry/313885_agentic-demo-collection"
)
SHOTS_DIR = os.environ.get("SHOTS_DIR", os.path.join(os.path.dirname(__file__), "shots"))
PROMPT = os.environ.get(
    "DEMO_PROMPT",
    (
        "Our current SEPA transfer form does not validate IBANs. "
        "Create a story to implement this. "
        "Use the API provided by https://openiban.com/ for this purpose."
    ),
)

PLACEHOLDER = "your request is being processed"


def screenshot(page, name):
    os.makedirs(SHOTS_DIR, exist_ok=True)
    path = os.path.join(SHOTS_DIR, f"{name}.png")
    page.screenshot(path=path, full_page=True)
    print(f"[shot] {path}")


def get_last_assistant_text(page):
    for sel in (
        ".chat-assistant .markdown-prose",
        "[data-message-role='assistant'] .markdown-prose",
        ".chat-assistant",
        "div.prose",
    ):
        try:
            els = page.locator(sel).all()
            if els:
                return (els[-1].text_content() or "").strip()
        except Exception:
            continue
    return ""


def wait_for_real_response(page, since_text, timeout_s=180, settle_ms=2000):
    deadline = time.time() + timeout_s
    last = ""
    stable_since = None
    while time.time() < deadline:
        current = get_last_assistant_text(page)
        is_placeholder = PLACEHOLDER in current.lower() or not current.strip()
        is_new = current != since_text
        if is_new and not is_placeholder:
            if current == last:
                if stable_since is None:
                    stable_since = time.time()
                elif (time.time() - stable_since) * 1000 >= settle_ms:
                    return current
            else:
                last = current
                stable_since = None
        time.sleep(0.4)
    raise PWTimeout(
        f"No stable non-placeholder response within {timeout_s}s "
        f"(last len={len(last)}, baseline len={len(since_text)})"
    )


def send_message(page, text):
    inp = page.locator("#chat-input")
    inp.click()
    inp.fill(text)
    page.wait_for_timeout(250)
    send_btn = page.locator("button[aria-label*='Send' i]")
    if send_btn.count() > 0 and send_btn.first.is_visible():
        send_btn.first.click()
    else:
        inp.press("Enter")


def gh_latest_issue():
    try:
        out = subprocess.check_output(
            [
                "gh", "issue", "list",
                "--repo", REPO,
                "--state", "all",
                "--limit", "1",
                "--json", "number,title,url,createdAt",
            ],
            stderr=subprocess.STDOUT,
            timeout=20,
        )
        data = json.loads(out)
        if data:
            return data[0]
    except Exception as e:
        print("[gh] error:", e)
    return None


def main():
    with sync_playwright() as p:
        browser = p.chromium.launch(headless=True)
        ctx = browser.new_context(viewport={"width": 1400, "height": 900})
        page = ctx.new_page()
        page.on(
            "console",
            lambda msg: msg.type == "error" and print(f"[console.err] {msg.text}"),
        )

        # ---- LOGIN ----
        page.goto(OWUI_URL + "/", wait_until="networkidle")
        if "/auth" in page.url:
            page.fill("#email", EMAIL)
            page.fill("#password", PASSWORD)
            page.click("button:has-text('Sign in')")
            for _ in range(30):
                if "/auth" not in page.url:
                    break
                time.sleep(0.5)
            page.wait_for_load_state("networkidle", timeout=30000)
        print(f"[login] ok, URL={page.url}")
        screenshot(page, "10_after_login")

        # ---- NEW CHAT ----
        page.goto(OWUI_URL + "/", wait_until="networkidle")
        page.wait_for_timeout(1500)
        print(f"[new chat] URL={page.url}")
        screenshot(page, "11_new_chat")

        model_btn = page.locator("button[aria-label*='Selected model']").first
        if model_btn.count() > 0:
            print(f"[model] {model_btn.get_attribute('aria-label')}")

        before = gh_latest_issue()
        print(f"[gh] before: {before}")
        before_number = before["number"] if before else None

        # ---- PROMPT ----
        print(f"[send 1] {PROMPT}")
        send_message(page, PROMPT)
        page.wait_for_timeout(1500)
        screenshot(page, "12_prompt_sent")
        resp = wait_for_real_response(page, since_text="", timeout_s=180)
        print(f"[resp 1] ({len(resp)}) {resp[:300]}")
        screenshot(page, "13_resp_1")

        # ---- AUTO-ANSWER CLARIFICATIONS ----
        for i in range(1, 11):
            lr = resp.lower()
            if (
                ("push the story to github" in lr or "push the story" in lr)
                and ("make further changes" in lr or "1." in lr)
            ):
                print(f"[loop] final confirmation reached after {i - 1} clarification(s)")
                break
            print(f"[send f{i}] '1'")
            baseline = resp
            send_message(page, "1")
            resp = wait_for_real_response(page, since_text=baseline, timeout_s=180)
            print(f"[resp f{i}] ({len(resp)}) {resp[:200]}")
            screenshot(page, f"14_f{i}")
        else:
            raise RuntimeError("Never reached final confirmation step")

        # ---- CONFIRM PUSH ----
        print("[send push] 'push'")
        baseline = resp
        send_message(page, "push")
        push_resp = wait_for_real_response(page, since_text=baseline, timeout_s=180)
        print(f"[resp push] ({len(push_resp)})")
        print(push_resp[:1500])
        screenshot(page, "15_after_push")

        # ---- VERIFY GITHUB ----
        time.sleep(2)
        after = gh_latest_issue()
        print(f"[gh] after: {after}")
        after_number = after["number"] if after else None

        success = (
            after is not None
            and (before_number is None or after_number > before_number)
            and "iban" in (after["title"] or "").lower()
        )

        print("\n" + "=" * 60)
        print("RESULT:", "PASS" if success else "FAIL")
        print(f"  Before: #{before_number}")
        print(f"  After:  #{after_number} '{after['title'] if after else None}'")
        print(f"  URL:    {after['url'] if after else None}")
        print(f"  Push response mentions github.com:        {'github.com' in push_resp}")
        print(f"  Push response mentions issue number (#):  {'issue #' in push_resp.lower()}")
        print("=" * 60)

        browser.close()
        sys.exit(0 if success else 1)


if __name__ == "__main__":
    main()
