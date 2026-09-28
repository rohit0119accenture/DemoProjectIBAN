---
skill: bugfix
role: Developer
triggers:
  - bug fix
  - defect
  - regression
tags:
  - skill
  - bugfix
  - debugging
---

# Skill: bugfix

> Structured workflow for bug fixes.
> Ensures fixes address the root cause, not just the symptom.

## Role
Developer

## Required Domains
- architecture → [[CodingGuidelines]]
- security → [[SecurityGuidelines]]
- testing → [[TestingStrategy]]

## Preconditions
- Bug ticket with reproduction steps exists
- Severity is classified

## Steps

### 1. Reproduce
- Reproduce the bug locally using the ticket description
- If not reproducible: return ticket to reporter with follow-up questions
- Identify the affected code location

### 2. Root Cause Analysis
- Don't just fix the symptom — understand the cause
- Check: Does the same problem exist elsewhere? (grep across codebase)
- Check: Is this a regression? If so — why didn't existing tests catch it?

### 3. Test-First Fix

> [!danger] Test first — always
> **First** write a failing test that reproduces the bug.
> Then implement the fix. Test must pass afterwards.
> Naming: `should_correctBehavior_when_previouslyBrokenCondition()`

### 4. Security Check
- Check: Does the bug have security implications?
- If input validation is affected → apply [[SecurityGuidelines#2. Input Validation & Injection Prevention]]
- If data was exposed → review [[IncidentResponseTemplate]]
- For security bugs: add Security Champion as reviewer

### 5. Regression Prevention
- Check: Do we need additional integration or E2E tests?
- Check: Should the [[TestingStrategy]] be extended?
- Check: Should a [[CodingGuidelines|Coding Guideline]] be added to prevent similar bugs?

### 6. Create PR
- Fill out PR template completely (see [[PullRequestTemplate]])
- Link bug ticket in PR
- Screenshots/logs: before-and-after comparison
