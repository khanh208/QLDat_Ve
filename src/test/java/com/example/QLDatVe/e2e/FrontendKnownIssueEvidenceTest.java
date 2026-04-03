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
    public void duplicateCashSeatShouldBeRejectedButBothUsersCanBook() throws Exception {
        resetMockState();

        openTripAndSeedUser(primaryDriver, primaryWait, 1, "cash-user-1", "cash1@example.com");
        openTripAndSeedUser(secondaryDriver, secondaryWait, 2, "cash-user-2", "cash2@example.com");

        selectSeat(primaryDriver, primaryWait, "D2");
        selectSeat(secondaryDriver, secondaryWait, "D2");
        choosePaymentMethod(primaryDriver, primaryWait, "cash");
        choosePaymentMethod(secondaryDriver, secondaryWait, "cash");

        captureCheckpointScreenshot(primaryDriver, "booking", "duplicate-cash-before-submit-user-1");
        captureCheckpointScreenshot(secondaryDriver, "booking", "duplicate-cash-before-submit-user-2");

        boolean firstUserSucceeded = submitCashBookingAndDetectSuccess(primaryDriver, primaryWait);
        boolean secondUserSucceeded = submitCashBookingAndDetectSuccess(secondaryDriver, secondaryWait);

        captureCheckpointScreenshot(primaryDriver, "booking", "duplicate-cash-after-submit-user-1");
        captureCheckpointScreenshot(secondaryDriver, "booking", "duplicate-cash-after-submit-user-2");

        Assert.assertFalse(
                "Known issue reproduced: both users booked seat D2 successfully with CASH.",
                firstUserSucceeded && secondUserSucceeded
        );
    }

    @Test
    public void duplicateMomoSeatShouldBeRejectedButBothUsersReachPaymentSuccess() throws Exception {
        resetMockState();

        openTripAndSeedUser(primaryDriver, primaryWait, 3, "momo-user-1", "momo1@example.com");
        openTripAndSeedUser(secondaryDriver, secondaryWait, 4, "momo-user-2", "momo2@example.com");

        selectSeat(primaryDriver, primaryWait, "C2");
        selectSeat(secondaryDriver, secondaryWait, "C2");
        choosePaymentMethod(primaryDriver, primaryWait, "momo");
        choosePaymentMethod(secondaryDriver, secondaryWait, "momo");

        captureCheckpointScreenshot(primaryDriver, "payment", "duplicate-momo-before-submit-user-1");
        captureCheckpointScreenshot(secondaryDriver, "payment", "duplicate-momo-before-submit-user-2");

        boolean firstUserReachedSuccess = submitMomoBookingAndReachPaymentSuccess(primaryDriver, primaryWait);
        boolean secondUserReachedSuccess = submitMomoBookingAndReachPaymentSuccess(secondaryDriver, secondaryWait);

        captureCheckpointScreenshot(primaryDriver, "payment", "duplicate-momo-after-submit-user-1");
        captureCheckpointScreenshot(secondaryDriver, "payment", "duplicate-momo-after-submit-user-2");

        Assert.assertFalse(
                "Known issue reproduced: both users reached payment success for seat C2 with MOMO.",
                firstUserReachedSuccess && secondUserReachedSuccess
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

    private void selectSeat(WebDriver driver, WebDriverWait wait, String seatNumber) {
        wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("[data-testid='seat-" + seatNumber + "']"))).click();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.cssSelector("[data-testid='selected-seats-summary']"), seatNumber));
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
        String component = inferComponentFromTestName(testName);
        captureScreenshot(primaryDriver, "fail", component, testName + "-primary-FAIL");
        captureScreenshot(secondaryDriver, "fail", component, testName + "-secondary-FAIL");
    }

    private void captureCheckpointScreenshot(WebDriver driver, String component, String checkpoint) {
        String testMethod = currentTestName.getMethodName() == null ? "unknown-test" : currentTestName.getMethodName();
        String label = String.format("%02d-%s-%s", screenshotCounter++, testMethod, checkpoint);
        captureScreenshot(driver, "pass", component, label);
    }

    private void captureScreenshot(WebDriver driver, String status, String component, String label) {
        if (driver == null || !(driver instanceof TakesScreenshot)) {
            return;
        }

        try {
            Path screenshotDirectory = Path.of(
                    "target",
                    "selenium-screenshots",
                    sanitizeForFileName(status),
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
}
