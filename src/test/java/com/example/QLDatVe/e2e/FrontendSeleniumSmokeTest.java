package com.example.QLDatVe.e2e;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.List;

public class FrontendSeleniumSmokeTest {

    private WebDriver driver;
    private WebDriverWait wait;
    private String baseUrl;
    private String browserName;

    @Rule
    public TestWatcher screenshotOnFailure = new TestWatcher() {
        @Override
        protected void failed(Throwable error, Description description) {
            captureFailureScreenshot(description.getMethodName());
        }
    };

    @Before
    public void setUp() {
        String browser = System.getProperty("selenium.browser", "chrome").toLowerCase();
        browserName = browser;
        baseUrl = System.getProperty("selenium.baseUrl", "http://127.0.0.1:3000");
        driver = createDriver(browser);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    @After
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    public void homePageShouldRenderFeaturedTrips() {
        driver.get(baseUrl + "/");

        WebElement title = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("[data-testid='trip-results-title']")));
        List<WebElement> tripCards = wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(
                By.cssSelector("[data-testid^='trip-card-']"), 0));

        Assert.assertFalse(title.getText().isBlank());
        Assert.assertFalse(tripCards.isEmpty());
    }

    @Test
    public void cashBookingFlowShouldShowSuccessNotification() {
        driver.get(baseUrl + "/");
        seedLoggedInUser();

        WebElement selectTripButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("[data-testid='trip-select-1']")));
        selectTripButton.click();

        WebElement tripTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("[data-testid='trip-detail-title']")));
        Assert.assertTrue(tripTitle.getText().contains("#1"));

        WebElement seatB1 = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("[data-testid='seat-B1']")));
        seatB1.click();

        WebElement summary = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("[data-testid='selected-seats-summary']")));
        Assert.assertTrue(summary.getText().contains("B1"));

        WebElement cashOption = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("[data-testid='payment-cash-option']")));
        cashOption.click();
        wait.until(ExpectedConditions.elementSelectionStateToBe(
                By.cssSelector("[data-testid='payment-cash-radio']"), true));

        WebElement bookingButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("[data-testid='booking-submit-button']")));
        bookingButton.click();

        WebElement notification = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("[data-testid='global-notification']")));
        String notificationText = notification.getText().toLowerCase();
        Assert.assertTrue(notificationText.contains("thành công") || notificationText.contains("mock"));
    }

    @Test
    public void bookedSeatShouldBeMarkedUnavailable() {
        driver.get(baseUrl + "/trips/1");

        WebElement bookedSeat = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("[data-testid='seat-A1']")));

        Assert.assertEquals("booked", bookedSeat.getAttribute("data-seat-status"));
        Assert.assertTrue(driver.findElements(By.cssSelector("[data-testid='selected-seats-summary']")).isEmpty());
    }

    @Test
    public void unauthenticatedUserShouldBeRedirectedToLoginForMyBookings() {
        driver.get(baseUrl + "/");
        clearAuth();
        driver.navigate().to(baseUrl + "/my-bookings");

        wait.until((webDriver) -> webDriver.getCurrentUrl().contains("/login"));
        WebElement loginTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("[data-testid='login-page-title']")));

        Assert.assertFalse(loginTitle.getText().isBlank());
    }

    @Test
    public void myBookingsPageShouldRenderExistingBookingsForLoggedInUser() {
        driver.get(baseUrl + "/");
        seedLoggedInUser();
        driver.navigate().to(baseUrl + "/my-bookings");

        WebElement title = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("[data-testid='my-bookings-title']")));
        WebElement bookingCard = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("[data-testid='booking-card-701']")));
        WebElement bookingStatus = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("[data-testid='booking-status-701']")));

        Assert.assertFalse(title.getText().isBlank());
        Assert.assertTrue(bookingCard.getText().contains("#701"));
        Assert.assertFalse(bookingStatus.getText().isBlank());
    }

    @Test
    public void momoBookingFlowShouldReachPaymentSuccessPage() {
        driver.get(baseUrl + "/");
        seedLoggedInUser();

        WebElement selectTripButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("[data-testid='trip-select-1']")));
        selectTripButton.click();

        WebElement seatC1 = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("[data-testid='seat-C1']")));
        seatC1.click();

        WebElement summary = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("[data-testid='selected-seats-summary']")));
        Assert.assertTrue(summary.getText().contains("C1"));

        WebElement momoOption = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("[data-testid='payment-momo-option']")));
        momoOption.click();
        wait.until(ExpectedConditions.elementSelectionStateToBe(
                By.cssSelector("[data-testid='payment-momo-radio']"), true));

        WebElement bookingButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("[data-testid='booking-submit-button']")));
        bookingButton.click();

        WebElement paymentTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("[data-testid='payment-success-title']")));
        wait.until((webDriver) ->
                !webDriver.findElements(By.cssSelector("[data-testid='payment-success-alert']")).isEmpty()
                        || webDriver.getCurrentUrl().contains("/my-bookings"));

        Assert.assertFalse(paymentTitle.getText().isBlank());
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

    private void seedLoggedInUser() {
        JavascriptExecutor javascriptExecutor = (JavascriptExecutor) driver;
        javascriptExecutor.executeScript(
                "window.localStorage.setItem('user', arguments[0]);" +
                "window.localStorage.setItem('token', arguments[1]);",
                "{\"userId\":1,\"username\":\"selenium-user\",\"email\":\"selenium@example.com\"}",
                "mock-token");
    }

    private void clearAuth() {
        JavascriptExecutor javascriptExecutor = (JavascriptExecutor) driver;
        javascriptExecutor.executeScript(
                "window.localStorage.removeItem('user');" +
                "window.localStorage.removeItem('token');");
    }

    private void captureFailureScreenshot(String testName) {
        if (driver == null || !(driver instanceof TakesScreenshot)) {
            return;
        }

        try {
            Path screenshotDirectory = Path.of("target", "selenium-screenshots");
            Files.createDirectories(screenshotDirectory);

            String safeBrowser = (browserName == null || browserName.isBlank()) ? "unknown" : browserName;
            String fileName = safeBrowser + "-" + testName + "-" + System.currentTimeMillis() + ".png";
            Path screenshotPath = screenshotDirectory.resolve(fileName);

            Path tempScreenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE).toPath();
            Files.copy(tempScreenshot, screenshotPath, StandardCopyOption.REPLACE_EXISTING);

            System.out.println("Saved Selenium failure screenshot to: " + screenshotPath.toAbsolutePath());
        } catch (Exception exception) {
            System.err.println("Unable to capture Selenium failure screenshot: " + exception.getMessage());
        }
    }
}
