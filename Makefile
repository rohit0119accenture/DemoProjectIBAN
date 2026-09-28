# Demo 02 — IBAN Validation — top-level orchestration
#
# Targets:
#   make serve     Start backend (:8089) + frontend (:5173) in the background
#   make feature   Apply the IBAN-validation feature impl (frontend overlay)
#   make test      Apply pom edits + copy E2E test + run mvn test
#   make clean     Stop servers + revert bank-transfer-app to pristine state
#   make all       serve → feature → test (full replay)
#   make help      List targets
#
# The actual overlay logic lives in feature-impl-reference/ and
# ui-test-reference/ — this file only orchestrates them and manages the
# two long-running dev servers. Logs land in .run/ (gitignored).

SHELL := /bin/bash

BANK_APP      := bank-transfer-app
RUN_DIR       := .run
BACKEND_LOG   := $(RUN_DIR)/backend.log
FRONTEND_LOG  := $(RUN_DIR)/frontend.log
BACKEND_PORT  := 8089
FRONTEND_PORT := 5173

.PHONY: help all serve feature test clean _wait _require-servers

help: ## List available targets
	@awk 'BEGIN {FS = ":.*?## "} /^[a-zA-Z_-]+:.*?## / {printf "  make %-9s %s\n", $$1, $$2}' $(MAKEFILE_LIST)

all: serve feature test ## Full replay: serve + feature + test

serve: ## Start backend (:8089) + frontend (:5173) in the background
	@mkdir -p $(RUN_DIR)
	@if lsof -nP -iTCP:$(BACKEND_PORT) -sTCP:LISTEN -t >/dev/null 2>&1; then \
	    echo ">> backend already on :$(BACKEND_PORT) (PID $$(lsof -nP -iTCP:$(BACKEND_PORT) -sTCP:LISTEN -t))"; \
	else \
	    echo ">> starting backend on :$(BACKEND_PORT) (log: $(BACKEND_LOG))"; \
	    ( cd $(BANK_APP) && exec nohup mvn -q spring-boot:run > ../$(BACKEND_LOG) 2>&1 ) & \
	fi
	@if [ ! -d $(BANK_APP)/frontend/node_modules ]; then \
	    echo ">> installing frontend deps (one-shot)"; \
	    cd $(BANK_APP)/frontend && npm install --no-audit --no-fund >/dev/null; \
	fi
	@if lsof -nP -iTCP:$(FRONTEND_PORT) -sTCP:LISTEN -t >/dev/null 2>&1; then \
	    echo ">> frontend already on :$(FRONTEND_PORT) (PID $$(lsof -nP -iTCP:$(FRONTEND_PORT) -sTCP:LISTEN -t))"; \
	else \
	    echo ">> starting frontend on :$(FRONTEND_PORT) (log: $(FRONTEND_LOG))"; \
	    ( cd $(BANK_APP)/frontend && exec nohup npm run dev > ../../$(FRONTEND_LOG) 2>&1 ) & \
	fi
	@$(MAKE) --no-print-directory _wait

_wait:
	@echo ">> waiting for both ports to come up..."
	@deadline=$$((SECONDS+120)); \
	until lsof -nP -iTCP:$(BACKEND_PORT) -sTCP:LISTEN -t >/dev/null 2>&1; do \
	    if [ $$SECONDS -ge $$deadline ]; then \
	        echo "   ! backend timeout — tail $(BACKEND_LOG):"; tail -20 $(BACKEND_LOG); exit 1; \
	    fi; sleep 1; \
	done; \
	echo "   = backend ready: http://localhost:$(BACKEND_PORT)"
	@deadline=$$((SECONDS+60)); \
	until lsof -nP -iTCP:$(FRONTEND_PORT) -sTCP:LISTEN -t >/dev/null 2>&1; do \
	    if [ $$SECONDS -ge $$deadline ]; then \
	        echo "   ! frontend timeout — tail $(FRONTEND_LOG):"; tail -20 $(FRONTEND_LOG); exit 1; \
	    fi; sleep 1; \
	done; \
	echo "   = frontend ready: http://localhost:$(FRONTEND_PORT)"

feature: ## Apply the IBAN-validation feature impl (frontend overlay)
	@$(MAKE) --no-print-directory -C feature-impl-reference up

test: _require-servers ## Apply pom edits + copy E2E test + run mvn test
	@$(MAKE) --no-print-directory -C ui-test-reference up

_require-servers:
	@if ! lsof -nP -iTCP:$(BACKEND_PORT) -sTCP:LISTEN -t >/dev/null 2>&1; then \
	    echo "!! backend not running on :$(BACKEND_PORT) — run 'make serve' first"; exit 1; fi
	@if ! lsof -nP -iTCP:$(FRONTEND_PORT) -sTCP:LISTEN -t >/dev/null 2>&1; then \
	    echo "!! frontend not running on :$(FRONTEND_PORT) — run 'make serve' first"; exit 1; fi

clean: ## Stop servers + revert bank-transfer-app to pristine state
	@echo ">> stopping servers"
	@pids=$$(lsof -nP -iTCP:$(BACKEND_PORT) -sTCP:LISTEN -t 2>/dev/null); \
	    if [ -n "$$pids" ]; then kill $$pids 2>/dev/null && echo "   = backend stopped (PID $$pids)" || echo "   = backend kill failed"; \
	    else echo "   = backend not running"; fi
	@pids=$$(lsof -nP -iTCP:$(FRONTEND_PORT) -sTCP:LISTEN -t 2>/dev/null); \
	    if [ -n "$$pids" ]; then kill $$pids 2>/dev/null && echo "   = frontend stopped (PID $$pids)" || echo "   = frontend kill failed"; \
	    else echo "   = frontend not running"; fi
	@echo ">> reverting bank-transfer-app"
	@$(MAKE) --no-print-directory -C feature-impl-reference down 2>/dev/null || true
	@if ! git -C $(BANK_APP) diff --quiet -- pom.xml 2>/dev/null; then \
	    git -C $(BANK_APP) checkout -- pom.xml && echo "   = pom.xml reverted"; \
	else echo "   = pom.xml already pristine"; fi
	@if [ -d $(BANK_APP)/src/test/java/com/demobank/transfer/e2e ]; then \
	    rm -rf $(BANK_APP)/src/test/java/com/demobank/transfer/e2e && echo "   = E2E test directory removed"; \
	else echo "   = no E2E test directory to remove"; fi
	@if [ -d $(RUN_DIR) ]; then rm -rf $(RUN_DIR) && echo "   = $(RUN_DIR)/ removed"; fi
