---
skill: frontend-test
role: QA Engineer / Frontend Tester
triggers:
  - frontend test
  - ui test
  - selenium test
  - acceptance test
  - e2e test
tags:
  - skill
  - frontend-test
  - selenium
  - e2e
  - acceptance-criteria
---

# Skill: frontend-test

> Workflow for creating Selenium UI tests based on Jira ticket acceptance criteria.
> The developer has already implemented the feature on a branch — the QA Engineer picks up the work and writes automated UI tests.

## Role
QA Engineer / Frontend Tester

## Required Domains
- testing → [[TestingStrategy]]
- architecture → [[CodingGuidelines]]
- security → [[SecurityGuidelines]]

## Preconditions
- Feature has been implemented and pushed to a feature branch
- Jira ticket exists with acceptance criteria
- Branch naming follows convention: `feat/{TICKET-ID}-description`

## Steps

### 1. Identify Ticket and Branch

- The argument `$ARGUMENTS` is the **Jira ticket ID** (e.g., `DEM-16`)
- Fetch the Jira ticket using the Jira MCP tool (`mcp__jira__jira_search`) to retrieve:
  - Summary (ticket title)
  - Description and **acceptance criteria**
  - Status and assignee
- Identify the corresponding **feature branch** by searching for a branch matching `feat/{TICKET-ID}-*`
- Check out the feature branch using `git checkout`

> [!important] Single source of truth
> The Jira ticket's acceptance criteria are the **binding specification** for the UI tests.
> Every acceptance criterion must map to at least one test case.

### 2. Analyze the Implementation

- Read the code changes on the feature branch compared to `main`:
  - `git diff main...HEAD --name-only` to identify changed files
  - Focus on **frontend changes** (components, pages, API calls)
  - Also review **backend changes** (new endpoints, changed responses) to understand the API contract
- Identify:
  - Which UI elements were added or changed
  - Which user interactions are expected (clicks, inputs, form submissions)
  - Which API endpoints the frontend calls
  - Which success/error states exist

### 3. Map Acceptance Criteria to Test Cases

- For each acceptance criterion from the Jira ticket, define one or more test cases
- Apply test naming convention: `should_expectedBehavior_when_condition()` (see [[TestingStrategy#2. Test Naming and Structure]])
- Document the mapping:

```
Acceptance Criterion: "User sees validation error for invalid IBAN"
→ Test: should_showValidationError_when_invalidIbanEntered()
→ Test: should_clearValidationError_when_ibanCorrected()
```

- Prioritize **happy path** tests first, then **error/edge cases**

### 4. Create Selenium UI Test

- Create a Selenium test class in `src/test/java/com/demobank/transfer/e2e/`
- Class naming: `{Feature}E2E.java` (e.g., `IbanValidationE2E.java`)
- Use the following structure:

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class FeatureE2E {

    private WebDriver driver;

    @BeforeEach
    void setUp() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        driver = new ChromeDriver(options);
        driver.get("http://localhost:5173");
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    void should_expectedBehavior_when_condition() {
        // Arrange — navigate to the right state
        // Act — perform user interaction
        // Assert — verify expected outcome
    }
}
```

- Apply **Arrange-Act-Assert** pattern in every test (see [[TestingStrategy#2. Test Naming and Structure]])
- Use explicit waits (`WebDriverWait`) instead of `Thread.sleep()` — never use implicit waits
- Use meaningful selectors: prefer `data-testid` attributes, then `id`, then CSS selectors. Avoid XPath where possible
- For form interactions, simulate real user behavior (type into fields, click buttons, wait for responses)

> [!warning] No production data
> Test data must be synthetic. See [[TestingStrategy#4. Test Data Management]].

### 5. Add Selenium Dependencies (if needed)

- Verify that `pom.xml` includes Selenium dependencies:
  - `org.seleniumhq.selenium:selenium-java`
  - `io.github.bonigarcia:webdrivermanager` (for automatic ChromeDriver management)
- If missing, add them with `<scope>test</scope>`

### 6. Run and Verify

- Start the backend: `mvn spring-boot:run`
- Start the frontend: `cd frontend && npm run dev`
- Run the Selenium tests: `mvn test -Dtest={TestClassName}`
- Verify:
  - All tests pass
  - Every acceptance criterion is covered by at least one test
  - No flaky tests (run twice to confirm stability)

> [!danger] All acceptance criteria must be covered
> If an acceptance criterion cannot be tested via UI, document why and suggest an alternative test approach (integration test, API test).

### 7. Commit and Update Ticket

- Commit the test with message: `test: Add Selenium UI tests for {TICKET-ID}`
- Follow [[PullRequestTemplate#4. Branch Convention|branch convention]]
- Update the Jira ticket with a comment listing the test cases created
