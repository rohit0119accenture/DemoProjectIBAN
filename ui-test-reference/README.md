# Phase 3 Reference — UI Tester Deliverable

This folder contains the **reference end-state** for Phase 3 of Demo 02:
the artefacts a UI Tester (human or agent) is expected to produce on top of
the implementation that the Developer Agent delivered in Phase 2.

```
ui-test-reference/
├── README.md                                          ← this file
├── pom-additions.xml                                  ← Maven snippets (Selenium dep + surefire include)
└── src/test/java/com/demobank/transfer/e2e/
    └── IbanValidationE2E.java                         ← 11 Selenium tests, one per acceptance criterion
```

Source: extracted from `~/Projects/e-agentic-sdlc-demo-ui-test/java/` (a
self-contained snapshot of the demo's post-Phase-3 end state).

---

## What Phase 3 produces

The UI Tester agent maps the user story's acceptance criteria 1:1 to Selenium
test methods, then implements them against the real React frontend (rendered
in headless Chrome) talking to the real Spring Boot backend on the same dev
ports the developer uses (`8089` / `5173`).

### Acceptance Criteria → tests mapping

| AC | Description | Test method(s) |
|---|---|---|
| AC1 | IBAN validation on blur | `should_triggerIbanValidation_when_ibanEnteredAndFieldBlurred` |
| AC2 | Valid IBAN handling | `should_showValidCheckmark_when_validIbanEntered`, `should_displayBankDetails_when_validIbanReturned` |
| AC3 | Invalid IBAN handling | `should_showErrorMessage_when_invalidIbanEntered`, `should_preventFormSubmission_when_ibanIsInvalid` |
| AC4 | API timeout / failure | `should_showWarningMessage_when_validationApiUnavailable` |
| AC5 | Form submission gating | `should_disableSubmitButton_when_ibanNotValidated`, `should_enableSubmitButton_when_ibanIsValid`, `should_submitSuccessfully_when_validIbanAndFormComplete` |
| edge | Field cleared / re-edited | `should_clearValidationState_when_ibanFieldCleared`, `should_revalidateIban_when_ibanCorrected` |

**Totals**: 11 tests; 5 AC categories + 2 edge cases.

---

## How to integrate into `bank-transfer-app/`

Phase 3 starts on the same branch the Phase 2 PR sits on.

```bash
cd ../bank-transfer-app
git checkout feat/DEM-15-iban-validation                 # branch from Phase 2

# 1) Add Selenium dependency + surefire include — see pom-additions.xml
$EDITOR pom.xml                                          # merge snippets

# 2) Add the E2E test class
mkdir -p src/test/java/com/demobank/transfer/e2e
cp ../ui-test-reference/src/test/java/com/demobank/transfer/e2e/IbanValidationE2E.java \
   src/test/java/com/demobank/transfer/e2e/

# 3) Make sure backend + frontend are running on the standard dev ports
mvn spring-boot:run                                       # http://localhost:8089
(cd frontend && npm run dev)                              # http://localhost:5173

# 4) Run the suite
mvn test -Dtest=IbanValidationE2E
```

A passing run prints **`Tests run: 11, Failures: 0, Errors: 0`**.

### Replay shortcuts

`make up` in this folder runs steps 1, 2, and 4 in one go (idempotent
pom edit + test copy + `mvn test`). The top-level
[`../Makefile`](../Makefile) wraps that together with the dev servers and
the matching feature overlay:

```bash
cd ..              # demo-02-iban-validation/
make serve         # backend (:8089) + frontend (:5173)
make feature       # ← feature-impl-reference/make up
make test          # ← this folder's `make up`
make clean         # stops servers + reverts bank-transfer-app
```

---

## Test fixture notes (security & compliance)

- **Synthetic test data only** — `VALID_DE_IBAN = "DE89370400440532013000"` and
  `INVALID_IBAN = "DE00000000000000000000"`. Never use real IBANs of real
  people (Demo Bank `wiki/security/SecurityGuidelines.md` §4 and
  `wiki/testing/TestingStrategy.md` §4).
- **Visible browser, demo-paced.** This suite is wired for live demos, not
  CI: Chrome opens in a visible window (`--window-size=1280,900`,
  `--window-position=80,80`) and `pause()` sleeps 500 ms after every user
  action so a watching human can follow along. Adjust `ACTION_DELAY_MS`
  or re-add `--headless` if you ever want to run it fast on a headless
  host. Total wall-clock for the 11-test run: ~50–60 s.

---

## Where the demo shows this (slides)

| Slide | What it shows |
|---|---|
| `docs/slides/slide-09.jpg` | Phase 3 handover — IDE opens, Claude Code reads TestingStrategy, CodingGuidelines, SecurityGuidelines + Jira ticket |
| `docs/slides/slide-10.jpg` | Plan — 7 todo items, from "identify ticket" through "commit and update Jira ticket" |
| `docs/slides/slide-11.jpg` | Result — full `IbanValidationE2E.java` class, AC→test mapping table, "11/11 E2E tests passing" |
