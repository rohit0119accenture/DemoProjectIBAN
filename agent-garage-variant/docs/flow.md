# Demo 02 — flow detail

## Sequence

```
User → Open WebUI → user_story_creator pipe → n8n webhook
                                                 │
                                                 ▼
                                          Webhook node
                                                 │
                                                 ▼
                                          User Story Creator (LangChain Agent)
                                          uses Claude Sonnet 4.6 via Anthropic
                                          uses Simple Memory keyed by sessionId
                                          uses Structured Output Parser
                                                 │ JSON {action, title, text, message}
                                                 ▼
                                          Switch
                                          ┌── case 0: action == "push_to_github"
                                          │      │
                                          │      ▼
                                          │   Confirmation Gate (IF)
                                          │      │
                                          │      ├── true (short + confirmation keyword)
                                          │      │      ▼
                                          │      │   GitHub: Create issue
                                          │      │      ▼
                                          │      │   Set Response (append issue link)
                                          │      │      ▼
                                          │      │   Respond to Webhook → User
                                          │      │
                                          │      └── false
                                          │             ▼
                                          │          Require Confirmation
                                          │          (append "push not executed" note)
                                          │             ▼
                                          │          Respond to Webhook → User
                                          │
                                          └── default (any other action)
                                                 ▼
                                          Respond to Webhook → User
```

## Why two safeguards

Layer 1 — **prompt**: the system prompt forbids `push_to_github` unless the
user's *current* message is a short, unambiguous confirmation. Works ~100 % of
the time with Sonnet 4.6 but is technically defeatable with prompt injection.

Layer 2 — **Confirmation Gate** (workflow IF node): even if the LLM emits
`push_to_github` on a long message containing story content, the IF rejects
the call before the GitHub node ever runs. This is the actual safety boundary.

## Webhook contract

Request:
```json
POST /webhook/880e3fd7-2418-4344-80e0-91f8305cab86
Content-Type: application/json

{
  "sessionId": "<arbitrary stable string — typically OWUI chat_id>",
  "chatInput": "<user message>"
}
```

Response (HTTP 200):
```json
[
  {
    "output": {
      "action": "create" | "improve" | "push_to_github",
      "userStoryTitle": "…",
      "userStoryText": "…",
      "message": "…"
    }
  }
]
```

On success of the push branch, `output.message` ends with:
```
…
---
GitHub Issue #<N> created: https://github.com/<owner>/<repo>/issues/<N>
```

When the Confirmation Gate rejects, `output.message` ends with:
```
…
---
[Confirmation required] Der Push wurde NICHT ausgeführt. Bitte bestätige den Push
in einer separaten, kurzen Nachricht (z. B. "push" oder "2").
```

## Session memory

The agent uses n8n's `Simple Memory` (LangChain `BufferWindowMemory` style)
keyed by `sessionId`. Context window: last 20 turns. Two chats from the same
user can therefore be independent if they use different `sessionId` values.
The OWUI pipe constructs it from `__user__['id']` plus a slice of the first
message so each chat conversation gets its own bucket.
