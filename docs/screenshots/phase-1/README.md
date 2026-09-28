# Phase 1 — End-to-end run screenshots

Captured 2026-05-17 from a live run of the Agent Garage variant
(`http://localhost:3000` → User Story Assistant pipe → n8n workflow 6 →
GitHub issue creation).

| File | Step | What it shows |
|---|---|---|
| [`01-prompt.png`](01-prompt.png) | The canonical IBAN-validation prompt typed into the chat input | shows the OWUI input box just before submit |
| [`02-clarifications.png`](02-clarifications.png) | First two clarification turns | agent asks role, user answers `1`; agent asks validation trigger, user types `2` |
| [`03-story-rendered.png`](03-story-rendered.png) | The fully generated user story | title, background, AC1–AC3 visible (AC4 + definitions below the fold) |
| [`04-confirmation-and-push.png`](04-confirmation-and-push.png) | Confirmation menu + push | rest of the story (AC4 + definitions), the `1. changes / 2. push` menu, user typed `2`, success reply with the GitHub issue link |
| [`05-github-issue.png`](05-github-issue.png) | The created GitHub issue | issue #19 in `AccentureCodeFoundry/313885_agentic-demo-collection` with the verbatim story body |

These are referenced inline from [`../../RUNBOOK.md`](../../RUNBOOK.md)
(Phase 1 section). To regenerate after a UI/wording change, re-run the
flow and replace files with the same names — paths in the RUNBOOK stay
stable.

## Tip for re-capturing

Use the browser's full-page screenshot (Cmd + Shift + 4 / DevTools "Capture
full size screenshot") at ~1400 px width to match the existing aspect
ratios.
