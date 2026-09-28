# Phase 2 reference — IBAN validation feature for bank-transfer-app
#
# Targets:
#   make up      Copy the IBAN-validation frontend implementation into bank-transfer-app.
#   make down    Revert the bank-transfer-app frontend to the pristine (pre-feature) state.
#   make diff    Show how the staged feature differs from what is currently checked in.
#   make help    List targets.
#
# Vite's dev server hot-reloads on the file change — no restart needed. The
# backend stays untouched (the feature talks to openiban.com directly and the
# submit payload still matches the existing 4-field TransferRequest record).

BANK_APP    := ../bank-transfer-app
COMP_REL    := frontend/src/components
SRC_TSX     := $(COMP_REL)/TransferForm.tsx
SRC_CSS     := $(COMP_REL)/TransferForm.css
DST_TSX     := $(BANK_APP)/$(COMP_REL)/TransferForm.tsx
DST_CSS     := $(BANK_APP)/$(COMP_REL)/TransferForm.css

.PHONY: up down diff help check

up: ## Copy feature impl into bank-transfer-app (Vite hot-reloads)
	@echo ">> feature-impl up: $(SRC_TSX) + $(SRC_CSS) -> $(BANK_APP)/$(COMP_REL)/"
	@test -d "$(BANK_APP)/$(COMP_REL)" || { echo "   ! target dir missing: $(BANK_APP)/$(COMP_REL)"; exit 1; }
	@cp -p "$(SRC_TSX)" "$(DST_TSX)"
	@cp -p "$(SRC_CSS)" "$(DST_CSS)"
	@echo "   = applied. If Vite is running it will hot-reload automatically."

down: ## Revert frontend files to the version checked into git (HEAD)
	@echo ">> feature-impl down: restoring $(BANK_APP)/$(COMP_REL)/ from git HEAD"
	@cd "$(BANK_APP)" && git checkout HEAD -- "$(COMP_REL)/TransferForm.tsx" "$(COMP_REL)/TransferForm.css"
	@echo "   = reverted."

diff: ## Show diff between staged feature and the current bank-transfer-app files
	@echo ">> feature-impl diff: staged vs. live"
	@diff -u "$(DST_TSX)" "$(SRC_TSX)" || true
	@echo
	@diff -u "$(DST_CSS)" "$(SRC_CSS)" || true

check: ## Quick smoke check — verify the test-required selectors are present in the source
	@echo ">> feature-impl check: selectors required by ../ui-test-reference"
	@for pat in 'id="iban-validation-status"' 'iban-status\b' 'iban-status--error' 'iban-check' 'iban-error-icon' 'bank-details\b' 'bank-details-item' 'Validating\.\.\.' 'IBAN verified'; do \
	    label=$$(printf '%s' "$$pat" | sed -e 's/\\b//g' -e 's/\\\.//g'); \
	    n=$$(grep -cE "$$pat" "$(SRC_TSX)" "$(SRC_CSS)" 2>/dev/null | awk -F: '{s+=$$2} END {print s+0}'); \
	    if [ "$$n" -gt 0 ]; then printf "   [OK]   %s\n" "$$label"; else printf "   [MISS] %s\n" "$$label"; fi; \
	done

help: ## Show available targets
	@awk 'BEGIN {FS = ":.*?## "} /^[a-zA-Z_-]+:.*?## / {printf "  %-8s %s\n", $$1, $$2}' $(MAKEFILE_LIST)
