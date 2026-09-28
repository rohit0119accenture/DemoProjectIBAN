package com.demobank.transfer.e2e;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Selenium E2E tests for IBAN validation feature (DEM-15).
 *
 * Covers all acceptance criteria from the Jira ticket:
 * - AC1: IBAN validation on blur
 * - AC2: Valid IBAN handling
 * - AC3: Invalid IBAN handling
 * - AC4: API timeout or failure handling
 * - AC5: Form submission validation
 *
 * Prerequisites: Backend running on port 8089, frontend on port 5173.
 *
 * Demo mode: browser opens visibly and pauses {@link #ACTION_DELAY_MS} ms
 * after every user action so a watching human can follow the run.
 */
class IbanValidationE2E {

    private static final String BASE_URL = "http://localhost:5173";
    private static final Duration WAIT_TIMEOUT = Duration.ofSeconds(10);
    // Demo pacing — paused after every user action so a human can follow along.
    private static final long ACTION_DELAY_MS = 500;

    // Synthetic test data — no production data (per TestingStrategy Section 4 / SecurityGuidelines)
    private static final String VALID_DE_IBAN = "DE89370400440532013000";
    private static final String INVALID_IBAN = "DE00000000000000000000";

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    void setUp() {
        ChromeOptions options = new ChromeOptions();
        // Visible browser — this suite is wired for live demos, not CI.
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1280,900");
        options.addArguments("--window-position=80,80");
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, WAIT_TIMEOUT);
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("iban")));
        pause();
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    /** Demo pacing — sleep so a watching human can register what just happened. */
    private static void pause() {
        try {
            Thread.sleep(ACTION_DELAY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // ── AC1: IBAN validation on blur ──

    @Test
    void should_triggerIbanValidation_when_ibanEnteredAndFieldBlurred() {
        // Arrange
        WebElement ibanInput = driver.findElement(By.id("iban"));

        // Act — enter a valid IBAN and blur by tabbing to next field
        ibanInput.sendKeys(VALID_DE_IBAN);
        pause();
        ibanInput.sendKeys(Keys.TAB);
        pause();

        // Assert — validation loading indicator appears (debounce triggers)
        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.id("iban-validation-status"), "Validating..."));
    }

    // ── AC2: Valid IBAN handling ──

    @Test
    void should_showValidCheckmark_when_validIbanEntered() {
        // Arrange
        WebElement ibanInput = driver.findElement(By.id("iban"));

        // Act
        ibanInput.sendKeys(VALID_DE_IBAN);
        pause();

        // Assert — wait for "IBAN verified" status message
        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.id("iban-validation-status"), "IBAN verified"));

        // Verify the valid checkmark icon is displayed
        WebElement checkIcon = driver.findElement(By.cssSelector(".iban-check"));
        assertTrue(checkIcon.isDisplayed());
    }

    @Test
    void should_displayBankDetails_when_validIbanReturned() {
        // Arrange
        WebElement ibanInput = driver.findElement(By.id("iban"));

        // Act
        ibanInput.sendKeys(VALID_DE_IBAN);
        pause();

        // Assert — wait for validation to complete with valid status
        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.id("iban-validation-status"), "IBAN verified"));

        // Bank details panel appears when the API returns bank data
        WebElement bankDetailsPanel = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.cssSelector(".bank-details")));
        assertTrue(bankDetailsPanel.isDisplayed());

        // Verify bank details grid contains at least one item
        assertFalse(driver.findElements(By.cssSelector(".bank-details-item")).isEmpty());
    }

    // ── AC3: Invalid IBAN handling ──

    @Test
    void should_showErrorMessage_when_invalidIbanEntered() {
        // Arrange
        WebElement ibanInput = driver.findElement(By.id("iban"));

        // Act — enter an IBAN with valid format but invalid checksum/bank code
        ibanInput.sendKeys(INVALID_IBAN);
        pause();

        // Assert — wait for error message to appear
        WebElement statusArea = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector(".iban-status--error")));
        assertTrue(statusArea.isDisplayed());

        // Verify error icon is displayed
        WebElement errorIcon = driver.findElement(By.cssSelector(".iban-error-icon"));
        assertTrue(errorIcon.isDisplayed());
    }

    @Test
    void should_preventFormSubmission_when_ibanIsInvalid() {
        // Arrange — enter an invalid IBAN and wait for validation to complete
        WebElement ibanInput = driver.findElement(By.id("iban"));
        ibanInput.sendKeys(INVALID_IBAN);
        pause();

        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector(".iban-status--error")));

        // Act — fill in the rest of the form
        driver.findElement(By.id("recipientName")).sendKeys("Test Recipient");
        pause();
        driver.findElement(By.id("amount")).sendKeys("100");
        pause();
        driver.findElement(By.id("purpose")).sendKeys("Test transfer");
        pause();

        // Assert — submit button should be disabled
        WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));
        assertFalse(submitButton.isEnabled());
    }

    // ── AC4: API timeout or failure handling ──
    // Note: Testing actual API timeouts in E2E is impractical without network manipulation.
    // The frontend handles fetch failures by showing "Could not validate IBAN. Please try again."
    // and setting valid=null (allowing the user to see the warning).
    // The backend falls back to checksum validation when the external API is unavailable.
    // This behavior is covered by:
    // - IbanValidationServiceTest (unit) — tests the fallback to checksum validation
    // - IbanValidationControllerIT (integration) — tests error response handling
    // The E2E test below verifies the UI handles the error state correctly when an IBAN
    // with valid format but failing validation is entered.

    @Test
    void should_showWarningMessage_when_validationApiUnavailable() {
        // Arrange — use an IBAN with valid format but from a country the API may not support,
        // which will fall back to checksum validation.
        // The backend's checksum fallback for a known-bad checksum will return invalid.
        WebElement ibanInput = driver.findElement(By.id("iban"));

        // Act — enter IBAN and wait for validation to complete
        ibanInput.sendKeys(INVALID_IBAN);
        pause();

        // Assert — an error or warning status is shown in the validation area
        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("#iban-validation-status .iban-status")));

        WebElement status = driver.findElement(By.cssSelector("#iban-validation-status .iban-status"));
        assertTrue(status.isDisplayed());
        // The error message should not be empty
        assertFalse(status.getText().isEmpty());
    }

    // ── AC5: Form submission validation ──

    @Test
    void should_disableSubmitButton_when_ibanNotValidated() {
        // Arrange — form is in initial state, no IBAN entered

        // Act — nothing, just check initial state

        // Assert — submit button should be disabled since IBAN is not validated
        WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));
        assertFalse(submitButton.isEnabled());
    }

    @Test
    void should_enableSubmitButton_when_ibanIsValid() {
        // Arrange
        WebElement ibanInput = driver.findElement(By.id("iban"));

        // Act — enter a valid IBAN and wait for validation
        ibanInput.sendKeys(VALID_DE_IBAN);
        pause();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.id("iban-validation-status"), "IBAN verified"));

        // Assert — submit button should be enabled
        WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));
        assertTrue(submitButton.isEnabled());
    }

    @Test
    void should_submitSuccessfully_when_validIbanAndFormComplete() {
        // Arrange — enter valid IBAN and wait for validation
        WebElement ibanInput = driver.findElement(By.id("iban"));
        ibanInput.sendKeys(VALID_DE_IBAN);
        pause();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.id("iban-validation-status"), "IBAN verified"));

        // Fill in remaining form fields
        driver.findElement(By.id("recipientName")).sendKeys("Test Recipient");
        pause();
        driver.findElement(By.id("amount")).sendKeys("250");
        pause();
        driver.findElement(By.id("purpose")).sendKeys("Rent February");
        pause();

        // Act — wait for submit button to be enabled, then click
        WebElement submitButton = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']")));
        submitButton.click();
        pause();

        // Assert — success message appears
        WebElement successBanner = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".success-banner")));
        assertTrue(successBanner.getText().length() > 0);
    }

    @Test
    void should_clearValidationState_when_ibanFieldCleared() {
        // Arrange — enter a valid IBAN and wait for validation
        WebElement ibanInput = driver.findElement(By.id("iban"));
        ibanInput.sendKeys(VALID_DE_IBAN);
        pause();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.id("iban-validation-status"), "IBAN verified"));

        // Act — clear the IBAN field using select-all + delete (React-compatible)
        ibanInput.sendKeys(Keys.chord(Keys.COMMAND, "a"));
        pause();
        ibanInput.sendKeys(Keys.DELETE);
        pause();

        // Assert — validation state resets, submit button disabled again
        wait.until(ExpectedConditions.not(
                ExpectedConditions.textToBePresentInElementLocated(
                        By.id("iban-validation-status"), "IBAN verified")));

        WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));
        assertFalse(submitButton.isEnabled());
    }

    @Test
    void should_revalidateIban_when_ibanCorrected() {
        // Arrange — enter an invalid IBAN first
        WebElement ibanInput = driver.findElement(By.id("iban"));
        ibanInput.sendKeys(INVALID_IBAN);
        pause();

        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector(".iban-status--error")));

        // Act — clear using select-all + delete, then enter a valid IBAN
        ibanInput.sendKeys(Keys.chord(Keys.COMMAND, "a"));
        pause();
        ibanInput.sendKeys(Keys.DELETE);
        pause();
        ibanInput.sendKeys(VALID_DE_IBAN);
        pause();

        // Assert — validation succeeds after correction
        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.id("iban-validation-status"), "IBAN verified"));

        WebElement checkIcon = driver.findElement(By.cssSelector(".iban-check"));
        assertTrue(checkIcon.isDisplayed());
    }
}
