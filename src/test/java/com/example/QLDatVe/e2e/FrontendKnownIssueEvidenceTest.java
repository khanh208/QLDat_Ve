package com.example.QLDatVe.e2e;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;

public class FrontendKnownIssueEvidenceTest {

    private WebDriver primaryDriver;
    private WebDriver secondaryDriver;
    private WebDriverWait primaryWait;
    private WebDriverWait secondaryWait;
    private String baseUrl;
    private String browserName;
    private String apiBaseUrl;
    private int screenshotCounter;

    @Rule
    public TestName currentTestName = new TestName();

    @Rule
    public TestWatcher screenshotOnFailure = new TestWatcher() {
        @Override
        protected void failed(Throwable error, Description description) {
            captureFailureScreenshots(description.getMethodName());
        }
    };

    @Before
    public void setUp() throws Exception {
        String browser = System.getProperty("selenium.browser", "chrome").toLowerCase();
        browserName = browser;
        screenshotCounter = 1;
        baseUrl = System.getProperty("selenium.baseUrl", "http://127.0.0.1:3000");
        apiBaseUrl = System.getProperty("selenium.apiBaseUrl", "http://127.0.0.1:8081/api");

        primaryDriver = createDriver(browser);
        secondaryDriver = createDriver(browser);

        configureDriver(primaryDriver);
        configureDriver(secondaryDriver);

        primaryWait = new WebDriverWait(primaryDriver, Duration.ofSeconds(20));
        secondaryWait = new WebDriverWait(secondaryDriver, Duration.ofSeconds(20));
    }

    @After
    public void tearDown() {
        if (primaryDriver != null) {
            primaryDriver.quit();
        }
        if (secondaryDriver != null) {
            secondaryDriver.quit();
        }
    }

    @Test
    public void tc03_overlapSeatCash_shouldRejectOneBooking() throws Exception {
        resetMockState();

        openTripAndSeedUser(primaryDriver, primaryWait, 1, "tc03-user-a", "tc03a@example.com");
        openTripAndSeedUser(secondaryDriver, secondaryWait, 2, "tc03-user-b", "tc03b@example.com");

        selectSeats(primaryDriver, primaryWait, "D1", "D2");
        selectSeats(secondaryDriver, secondaryWait, "D2", "C2");
        choosePaymentMethod(primaryDriver, primaryWait, "cash");
        choosePaymentMethod(secondaryDriver, secondaryWait, "cash");

        captureScenarioScreenshot(primaryDriver, "TC03", "booking", "user-a", "before-submit");
        captureScenarioScreenshot(secondaryDriver, "TC03", "booking", "user-b", "before-submit");

        boolean firstUserSucceeded = submitCashBookingAndDetectSuccess(primaryDriver, primaryWait);
        boolean secondUserSucceeded = submitCashBookingAndDetectSuccess(secondaryDriver, secondaryWait);

        captureScenarioScreenshot(primaryDriver, "TC03", "booking", "user-a", "after-submit");
        captureScenarioScreenshot(secondaryDriver, "TC03", "booking", "user-b", "after-submit");

        Assert.assertFalse(
                "TC03 failed as expected: both bookings containing overlapping seat D2 were accepted.",
                firstUserSucceeded && secondUserSucceeded
        );
    }

    @Test
    public void tc02_sameSeatMomo_shouldRejectOneBooking() throws Exception {
        resetMockState();

        openTripAndSeedUser(primaryDriver, primaryWait, 3, "tc02-user-a", "tc02a@example.com");
        openTripAndSeedUser(secondaryDriver, secondaryWait, 4, "tc02-user-b", "tc02b@example.com");

        selectSeats(primaryDriver, primaryWait, "A2");
        selectSeats(secondaryDriver, secondaryWait, "A2");
        choosePaymentMethod(primaryDriver, primaryWait, "momo");
        choosePaymentMethod(secondaryDriver, secondaryWait, "momo");

        captureScenarioScreenshot(primaryDriver, "TC02", "payment", "user-a", "before-submit");
        captureScenarioScreenshot(secondaryDriver, "TC02", "payment", "user-b", "before-submit");

        boolean firstUserReachedSuccess = submitMomoBookingAndReachPaymentSuccess(primaryDriver, primaryWait);
        boolean secondUserReachedSuccess = submitMomoBookingAndReachPaymentSuccess(secondaryDriver, secondaryWait);

        captureScenarioScreenshot(primaryDriver, "TC02", "payment", "user-a", "after-submit");
        captureScenarioScreenshot(secondaryDriver, "TC02", "payment", "user-b", "after-submit");

        Assert.assertFalse(
                "TC02 failed as expected: both users reached MOMO success for the same seat A2.",
                firstUserReachedSuccess && secondUserReachedSuccess
        );
    }

    @Test
    public void tc24_differentSeatsCash_shouldAllowBothBookings() throws Exception {
        resetMockState();

        openTripAndSeedUser(primaryDriver, primaryWait, 5, "tc24-user-a", "tc24a@example.com");
        openTripAndSeedUser(secondaryDriver, secondaryWait, 6, "tc24-user-b", "tc24b@example.com");

        selectSeats(primaryDriver, primaryWait, "A2");
        selectSeats(secondaryDriver, secondaryWait, "C1");
        choosePaymentMethod(primaryDriver, primaryWait, "cash");
        choosePaymentMethod(secondaryDriver, secondaryWait, "cash");

        captureScenarioScreenshot(primaryDriver, "TC24", "booking", "user-a", "before-submit");
        captureScenarioScreenshot(secondaryDriver, "TC24", "booking", "user-b", "before-submit");

        boolean firstUserSucceeded = submitCashBookingAndDetectSuccess(primaryDriver, primaryWait);
        boolean secondUserSucceeded = submitCashBookingAndDetectSuccess(secondaryDriver, secondaryWait);

        captureScenarioScreenshot(primaryDriver, "TC24", "booking", "user-a", "after-submit");
        captureScenarioScreenshot(secondaryDriver, "TC24", "booking", "user-b", "after-submit");

        Assert.assertTrue(
                "TC24 should pass: two users booking different seats must both succeed.",
                firstUserSucceeded && secondUserSucceeded
        );
    }

    private void configureDriver(WebDriver driver) {
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
    }

    private WebDriver createDriver(String browser) {
        String remoteUrl = System.getProperty("selenium.remoteUrl");

        return switch (browser) {
            case "firefox" -> {
                FirefoxOptions options = new FirefoxOptions();
                options.addArguments("-headless");
                yield remoteUrl != null && !remoteUrl.isBlank()
                        ? buildRemoteDriver(remoteUrl, options)
                        : new FirefoxDriver(options);
            }
            case "edge" -> {
                EdgeOptions options = new EdgeOptions();
                options.addArguments("--headless=new", "--no-sandbox", "--disable-dev-shm-usage");
                yield remoteUrl != null && !remoteUrl.isBlank()
                        ? buildRemoteDriver(remoteUrl, options)
                        : new EdgeDriver(options);
            }
            default -> {
                ChromeOptions options = new ChromeOptions();
                options.addArguments("--headless=new", "--no-sandbox", "--disable-dev-shm-usage");
                yield remoteUrl != null && !remoteUrl.isBlank()
                        ? buildRemoteDriver(remoteUrl, options)
                        : new ChromeDriver(options);
            }
        };
    }

    private WebDriver buildRemoteDriver(String remoteUrl, Object options) {
        try {
            return new RemoteWebDriver(new URL(remoteUrl), (org.openqa.selenium.Capabilities) options);
        } catch (MalformedURLException exception) {
            throw new IllegalArgumentException("Invalid selenium.remoteUrl: " + remoteUrl, exception);
        }
    }

    private void resetMockState() throws Exception {
        HttpURLConnection connection = (HttpURLConnection) URI.create(apiBaseUrl + "/test/reset").toURL().openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        connection.connect();

        int responseCode = connection.getResponseCode();
        if (responseCode < 200 || responseCode >= 300) {
            throw new IllegalStateException("Unable to reset mock state. HTTP " + responseCode);
        }

        connection.disconnect();
    }

    private void openTripAndSeedUser(WebDriver driver, WebDriverWait wait, int userId, String username, String email) {
        driver.get(baseUrl + "/");
        seedLoggedInUser(driver, userId, username, email);
        driver.navigate().to(baseUrl + "/trips/1");

        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("[data-testid='trip-detail-title']")));
    }

    private void seedLoggedInUser(WebDriver driver, int userId, String username, String email) {
        ((JavascriptExecutor) driver).executeScript(
                "window.localStorage.setItem('user', arguments[0]);" +
                "window.localStorage.setItem('token', arguments[1]);",
                String.format("{\"userId\":%d,\"username\":\"%s\",\"email\":\"%s\"}", userId, username, email),
                "mock-token-" + userId);
    }

    private void selectSeats(WebDriver driver, WebDriverWait wait, String... seatNumbers) {
        for (String seatNumber : seatNumbers) {
            wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("[data-testid='seat-" + seatNumber + "']"))).click();
            wait.until(ExpectedConditions.textToBePresentInElementLocated(
                    By.cssSelector("[data-testid='selected-seats-summary']"), seatNumber));
        }
    }

    private void choosePaymentMethod(WebDriver driver, WebDriverWait wait, String paymentMethod) {
        String optionSelector = "cash".equalsIgnoreCase(paymentMethod)
                ? "[data-testid='payment-cash-option']"
                : "[data-testid='payment-momo-option']";
        String radioSelector = "cash".equalsIgnoreCase(paymentMethod)
                ? "[data-testid='payment-cash-radio']"
                : "[data-testid='payment-momo-radio']";

        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(optionSelector))).click();
        wait.until(ExpectedConditions.elementSelectionStateToBe(By.cssSelector(radioSelector), true));
    }

    private boolean submitCashBookingAndDetectSuccess(WebDriver driver, WebDriverWait wait) {
        wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("[data-testid='booking-submit-button']"))).click();

        String notificationText = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("[data-testid='global-notification']"))).getText().toLowerCase();

        return notificationText.contains("thành công")
                || notificationText.contains("thanh cong")
                || notificationText.contains("mock")
                || notificationText.contains("success");
    }

    private boolean submitMomoBookingAndReachPaymentSuccess(WebDriver driver, WebDriverWait wait) {
        wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("[data-testid='booking-submit-button']"))).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("[data-testid='payment-success-title']")));
        wait.until(ExpectedConditions.or(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid='payment-success-alert']")),
                ExpectedConditions.urlContains("/my-bookings")
        ));

        return true;
    }

    private void captureFailureScreenshots(String testName) {
        String scenario = inferScenarioFromTestName(testName);
        String component = inferComponentFromTestName(testName);
        captureScreenshot(primaryDriver, "fail", scenario, component, testName + "-user-a-FAIL");
        captureScreenshot(secondaryDriver, "fail", scenario, component, testName + "-user-b-FAIL");
    }

    private void captureScenarioScreenshot(WebDriver driver,
                                           String scenario,
                                           String component,
                                           String actor,
                                           String checkpoint) {
        String label = String.format("%02d-%s-%s-%s", screenshotCounter++, scenario, actor, checkpoint);
        captureScreenshot(driver, "pass", scenario, component, label);
    }

    private void captureScreenshot(WebDriver driver, String status, String scenario, String component, String label) {
        if (driver == null || !(driver instanceof TakesScreenshot)) {
            return;
        }

        try {
            Path screenshotDirectory = Path.of(
                    "target",
                    "selenium-screenshots",
                    sanitizeForFileName(status),
                    sanitizeForFileName(scenario),
                    sanitizeForFileName(component));
            Files.createDirectories(screenshotDirectory);

            String safeBrowser = sanitizeForFileName(browserName == null || browserName.isBlank() ? "unknown" : browserName);
            String safeLabel = sanitizeForFileName(label);
            String fileName = safeBrowser + "-" + safeLabel + "-" + System.currentTimeMillis() + ".png";
            Path screenshotPath = screenshotDirectory.resolve(fileName);

            Path tempScreenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE).toPath();
            Files.copy(tempScreenshot, screenshotPath, StandardCopyOption.REPLACE_EXISTING);

            System.out.println("Saved Selenium screenshot to: " + screenshotPath.toAbsolutePath());
        } catch (Exception exception) {
            System.err.println("Unable to capture Selenium screenshot: " + exception.getMessage());
        }
    }

    private String sanitizeForFileName(String value) {
        return value.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private String inferComponentFromTestName(String testName) {
        if (testName == null || testName.isBlank()) {
            return "booking";
        }
        String normalized = testName.toLowerCase();
        if (normalized.contains("momo") || normalized.contains("payment")) {
            return "payment";
        }
        return "booking";
    }

    private String inferScenarioFromTestName(String testName) {
        if (testName == null || testName.isBlank()) {
            return "unknown";
        }
        String normalized = testName.toLowerCase();
        if (normalized.contains("tc02")) {
            return "TC02";
        }
        if (normalized.contains("tc03")) {
            return "TC03";
        }
        if (normalized.contains("tc24")) {
            return "TC24";
        }
        return "unknown";
    }
}
