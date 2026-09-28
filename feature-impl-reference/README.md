# Phase 2 Reference — Feature Implementation

This folder contains the **reference end-state** for Phase 2 of Demo 02:
the IBAN-validation feature that the Developer Agent is expected to ship on
top of the bare `bank-transfer-app/` scaffold. Same overlay pattern as
[`../ui-test-reference/`](../ui-test-reference/): the actual app stays without
the feature, `make up` lays it on top.

```
feature-impl-reference/
├── README.md                                       ← this file
├── Makefile                                        ← make up / down / diff / check
└── frontend/src/components/
    ├── TransferForm.tsx                            ← debounced openiban.com validation, DOM hooks for the E2E suite
    └── TransferForm.css                            ← status-line + bank-details styling
```

Source: adapted from PR #11 (`feature/issue-10-iban-validation-openiban`) —
the most complete of the four candidate implementations (PR 9 / 11 / 21 / 22).
PR 11 was extended so the DOM exposes the selectors and status texts that
[`../ui-test-reference/`](../ui-test-reference/) asserts against (`#iban-validation-status`,
`.iban-check`, `.iban-error-icon`, `.iban-status--error`, `.bank-details`,
`.bank-details-item`, status texts "Validating..." and "IBAN verified").

---

## What Phase 2 produces

`bank-transfer-app/` is intentionally checked in **without** the IBAN feature.
The Developer Agent's job is to add it. This folder is the canonical
end-state of that job, packaged as an overlay so the demo can be replayed any
number of times without leaving the repo dirty.

### Behavior delivered

| Behavior | DOM signal |
|---|---|
| Validation fires on blur **and** on input (debounced 800 ms) | `#iban-validation-status` text changes |
| Loading state visible for ≥ 800 ms | `.iban-status--loading` → text "Validating..." |
| Valid IBAN | `.iban-status--valid` → text "IBAN verified" + `.iban-check` icon |
| Invalid IBAN | `.iban-status--error` + `.iban-error-icon` |
| openiban.com unreachable | `.iban-status--warning` (graceful fallback — submit enabled) |
| Bank info from API | `.bank-details` panel with one or more `.bank-details-item` |
| Submit gating | disabled in `idle`/`loading`/`invalid`, enabled in `valid`/`unavailable` |

### Why this overlay (and not a real PR)

The four candidate PRs (#9, #11, #21, #22) each picked their own DOM naming
convention — none of them line up 1:1 with the test reference. Rather than
overwrite the as-implemented-by-the-agent state in those PRs, this folder
captures the **test-conformant target** as a separate, replayable artefact.
The four PRs remain on the record as "what each agent actually produced";
this folder is "what we ship to make Phase 3 green."

---

## How to use it

```bash
# 1) Apply the feature
cd feature-impl-reference
make up                                              # copies TransferForm.{tsx,css}

# 2) If Vite is running it picks up the change automatically. Otherwise:
(cd ../bank-transfer-app/frontend && npm run dev)    # http://localhost:5173
(cd ../bank-transfer-app && mvn spring-boot:run)     # http://localhost:8089

# 3) Run the matching E2E suite
cd ../ui-test-reference
make up                                              # pom edits + test copy + mvn test -Dtest=IbanValidationE2E
```

A passing run prints **`Tests run: 11, Failures: 0, Errors: 0`**.

To replay the demo from scratch:

```bash
cd feature-impl-reference
make down                                            # git checkout HEAD -- the two frontend files
```

### Or use the top-level orchestrator

The [`../Makefile`](../Makefile) chains this overlay together with the
dev servers and the E2E suite:

```bash
cd ..              # demo-02-iban-validation/
make serve         # backend + frontend
make feature       # ← calls this overlay's `make up`
make test          # ← calls ui-test-reference's `make up`
make clean         # stops servers + reverts everything
```

---

## Diagnostics

```bash
make check     # confirm the test-required selectors/texts are present in the source
make diff      # see what `make up` would change vs. what is currently in bank-transfer-app/
```

---

## Notes

- **No backend change.** PR 11 also added `bic` / `bankName` fields to
  `TransferRequest.java`. This overlay drops them — the frontend only sends
  the original 4-field payload (`recipientName`, `iban`, `amount`, `purpose`),
  so the unchanged `bank-transfer-app/` backend record accepts it as-is and
  no Maven recompile / Spring Boot restart is needed.
- **External network.** The frontend calls `https://openiban.com/validate/...`
  directly. Tests need outbound HTTPS; if openiban.com is unreachable, the
  unavailable-fallback path triggers (which the E2E suite tolerates).
- **Demo-paced timing.** Debounce is 800 ms and the loading state is
  floored at 800 ms via `Promise.all([fetch, sleep(800)])`. Two reasons:
  (1) the E2E suite pauses 500 ms between typing and TAB to keep itself
  watchable — debounce > pause ensures blur (not the debounce timer)
  triggers validation, so the "validation on blur" demo story holds; and
  (2) the 800 ms loading floor keeps "Validating..." visible long enough
  for both a human eye and Selenium's 500 ms poll cadence to register it.
  In production you would tune these down to ~250 ms / ~200 ms; here we
  trade snappiness for watchability.
