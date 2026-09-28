# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run Commands

**Backend** (from project root `java/`):
- `mvn test` — run all backend tests
- `mvn test -Dtest=TransferControllerTest` — run a single test class
- `mvn spring-boot:run` — start backend on port 8080

**Frontend** (from `java/frontend/`):
- `npm install` — install dependencies
- `npm run dev` — start Vite dev server on port 5173
- `npm run build` — TypeScript check + production build

## Architecture

Split backend/frontend app. The Spring Boot backend serves a REST API, and the React frontend runs as a separate Vite dev server that proxies `/api` requests to `localhost:8080`.

**Backend**: Spring Boot 2.7.4, Java 17, Maven. Package `com.demobank.transfer`. Single REST endpoint `POST /api/transfers` in `TransferController`. API models are Java records (`TransferRequest`, `TransferResponse`). No validation, no persistence, no service layer. CORS is configured in `WebConfig` to allow the Vite dev server origin.

**Frontend**: React 18, TypeScript, Vite. Lives in `frontend/` subdirectory. `TransferForm` component handles all form state and API calls. Vite proxy config in `vite.config.ts` forwards `/api` to the backend.

**Design**: Demo Bank blue `#0033a0` is the primary accent color.
